import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GmailSender {
	private static final String APPLICATION_NAME = "Haslett High Guild";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	
	private static final List<String> SCOPES = new ArrayList<String>() {{
		add(GmailScopes.GMAIL_COMPOSE);
		add(GmailScopes.GMAIL_SEND);
	}};
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
	
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = GmailSender.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
		
		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
				.setAccessType("offline")
				.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}
	
	public static void main(String... args) {
	
		
	}
	
	/**
	 * Create a MimeMessage using the parameters provided.
	 *
	 * @param to       email address of the receiver
	 * @param from     email address of the sender, the mailbox account
	 * @param subject  subject of the email
	 * @param bodyText body text of the email
	 * @return the MimeMessage to be used to send email
	 */
	static MimeMessage createEmail(String to,
	                               String from,
	                               String subject,
	                               String bodyText)
			throws javax.mail.MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		
		MimeMessage email = new MimeMessage(session);
		
		email.setFrom(new InternetAddress(from));
		email.addRecipient(javax.mail.Message.RecipientType.TO,
				new InternetAddress(to));
		email.setSubject(subject);
		email.setText(bodyText);
		return email;
	}
	
	/**
	 * Create a message from an email.
	 *
	 * @param emailContent Email to be set to raw of message
	 * @return a message containing a base64url encoded email
	 * @throws IOException if there was an error
	 */
	private static com.google.api.services.gmail.model.Message createMessageWithEmail(MimeMessage emailContent)
			throws IOException, javax.mail.MessagingException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		emailContent.writeTo(buffer);
		byte[] bytes = buffer.toByteArray();
		String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
		com.google.api.services.gmail.model.Message message = new com.google.api.services.gmail.model.Message();
		message.setRaw(encodedEmail);
		return message;
	}
	
	/**
	 * Send an email from the user's mailbox to its recipient.
	 *
	 * @param emailContent Email to be sent.
	 * @throws IOException if there was an error
	 */
	static void sendMessage(
			MimeMessage emailContent)
			throws IOException, javax.mail.MessagingException, GeneralSecurityException {
		
		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME)
				.build();
		
		
		com.google.api.services.gmail.model.Message message = createMessageWithEmail(emailContent);
		message = service.users().messages().send("me", message).execute();
		
		System.out.println("Message id: " + message.getId());
		System.out.println(message.toPrettyString());
	}
}

