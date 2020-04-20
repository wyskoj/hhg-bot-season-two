/*
 * MIT License
 *
 * Copyright (c) 2020 Jacob Wysko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

import javax.mail.MessagingException;
import javax.security.auth.login.LoginException;
import java.io.*;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Only enter code here that pertains to the verification
 * of student emails.
 */
class UserVerification extends ListenerAdapter {
	
	/* Declare ID and other final values */
	
	
	private static GuildController HHGController;
	private static Guild HHGGuild;
	
	/**
	 * Logs text to the log text file. It stores the timestamp of the text
	 * before it.
	 * <br>
	 * <code>
	 * YYYY-MM-DD HH:MM:SS.MSS - [text]
	 * </code>
	 *
	 * @param text text to be logged
	 */
	private static void log(String text) {
		try {
			BufferedWriter writer = new BufferedWriter(
					new FileWriter(HHG.LOG_FILE, true) // Set true for append mode
			);
			writer.write(String.format("%23s", new Timestamp(new Date().getTime())) + " - " + text + "\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void logPrivateMessage(PrivateMessageReceivedEvent event) {
		User user = event.getAuthor();
		if (user.isBot()) {
			User recipient = event.getChannel().getUser();
			logMessage(event, user, recipient);
		} else {
			logMessage(event, user, user);
		}
	}
	
	private static void logMessage(PrivateMessageReceivedEvent event, User user, User recipient) {
		try {
			BufferedWriter writer = new BufferedWriter(
					new FileWriter("dms/" + recipient.getId() + ".txt", true) // Set true for append mode
			);
			writer.write(String.format("%-23s", new Timestamp(new Date().getTime())) + " [" + user.getName() + "#" + user.getDiscriminator() + "] " + event.getMessage().getContentRaw() + "\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws LoginException {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		String TESTING_TOKEN = "NjQxMDI1MjI5MTI0MTQxMDU2.XcCXWA.j2srl2X2lgRZPfIQ8mwQ1VZdbok";
		builder.setToken(TESTING_TOKEN);
		builder.addEventListener(new UserVerification());
		/* Defined on start */
		builder.buildAsync();
	}
	
	@Override
	public void onReady(ReadyEvent event) {
		HHGGuild = event.getJDA().getGuildById(HHG.HHG_SERVER_ID);
		HHGController = new GuildController(HHGGuild);
	}
	
	
	/**
	 * Returns a list of all student's names at Haslett High School.
	 *
	 * @return a list of all student's names at Haslett High School
	 * @throws FileNotFoundException if the names file was not found
	 */
	private List<String> getNames() throws FileNotFoundException {
		Scanner in = new Scanner(HHG.NAMES_FILE);
		List<String> names = new ArrayList<>();
		while (in.hasNextLine()) {
			names.add(in.nextLine());
		}
		return names;
	}
	
	/**
	 * Returns a list of all student's emails at Haslett High School.
	 *
	 * @return a list of all student's emails at Haslett High School
	 * @throws FileNotFoundException if the emails file was not found
	 */
	private List<String> getEmails() throws FileNotFoundException {
		Scanner in = new Scanner(HHG.EMAILS_FILE);
		List<String> names = new ArrayList<>();
		while (in.hasNextLine()) {
			names.add(in.nextLine());
		}
		return names;
	}
	
	/**
	 * Returns a list of all valid invite codes.
	 *
	 * @return a list of all valid invite codes
	 * @throws FileNotFoundException if the invite codes file was not found
	 */
	private List<String> getInviteCodes() throws FileNotFoundException {
		Scanner in = new Scanner(HHG.INVITE_CODES_FILE);
		List<String> names = new ArrayList<>();
		while (in.hasNextLine()) {
			names.add(in.nextLine());
		}
		return names;
	}
	
	/**
	 * Generates a random verification code. It is six digits long
	 * and uses the numbers 0-9 (inclusive).
	 *
	 * @return a random verification code
	 */
	private String randomVerificationCode() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			builder.append(new Random().nextInt(10));
		}
		return builder.toString();
	}
	
	/*
		Verification steps (value is set after description has happened):
		0 - Joined Guild
		1 - Sent Email
		2 - Confirmed Name
		3 - Sent Nickname
		4 - Sent Valid Invite Code
		5 - Sent Valid Verification Code
	 */
	
	/**
	 * Called when the bot is send a DM.
	 *
	 * @param event the event of the message
	 */
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		logPrivateMessage(event);
		/* If message sent was from a bot, ignore */
		if (event.getAuthor().isBot())
			return;
		/* If message is "help" */
		if (event.getMessage().getContentStripped().trim().toLowerCase().equals("help")) {
			/* Reply to user */
			event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("An administrator has been notified. Please wait patiently.").queue());
			/* Send message in help channel */
			HHGGuild.getTextChannelById(HHG.HELP_CHANNEL_ID).sendMessage(event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " has requested help!").queue();
			return;
		}
		
		String message = event.getMessage().getContentRaw();
		String userID = event.getAuthor().getId();
		
		/* If was sent in a DM */
		if (event.getChannel().getType() == ChannelType.PRIVATE) {
			String username = event.getAuthor().getName();
			try {
				Scanner in = new Scanner(new File("verification-process/" + userID + ".txt"));
				/* If the verification process has not started */
				if (!in.hasNext())
					return;
				String step = in.next();
				/* Dependent of the verification step */
				switch (step) {
					/* User has sent an email */
					case "0":
						List<String> names = getNames();
						List<String> emails = getEmails();
						int index = emails.indexOf(message.trim());
						if (index != -1) {
							event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("Are you `" + names.get(index) + "`?\n\nType `Yes` to continue, `No` to re-enter your email, or `Help` for assistance.").queue());
							setVerificationStep(userID, "1");
							writeEmail(userID, message.toLowerCase().trim());
						} else {
							event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("Hmm, I don't recognize that email address. Please try typing your email again, or `Help` for assistance.").queue());
						}
						log(username + " sent " + message.trim() + " as an email");
						break;
					/* User has sent Yes or No to their name */
					case "1":
						if (message.toLowerCase().trim().equals("yes")) {
							event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("Please enter the first name you would prefer to go by on the HHG server.").queue());
							setVerificationStep(userID, "2");
							log(username + " confirmed their name is " + getNames().get(getEmails().indexOf(readEmail(userID))));
						} else if (message.toLowerCase().trim().equals("no")) {
							event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("Please send your email address.").queue());
							setVerificationStep(userID, "0");
						}
						
						break;
					/* User has sent their first name */
					case "2":
						/* Generate their nickname. It is Capital case of the word they enter, plus the first letter of
						their last initial.
						 */
						String[] namesSplit = getNames().get(getEmails().indexOf(readEmail(userID))).split(" ");
						String nickname = String.valueOf(message.trim().charAt(0)).toUpperCase() + message.trim().toLowerCase().substring(1) + " " + namesSplit[namesSplit.length - 1].charAt(0);
						
						event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("Your nickname request of `" + nickname + "` has been applied. Administrators may change it if need be.\nIf you have a special invite code, please send that now. Otherwise, type `Skip` to move to the next step. Type `Help` for assistance.").queue());
						
						HHGGuild.getTextChannelById(HHG.LOG_CHANNEL_ID).sendMessage(String.format(
								"`%s` has requested `%s` for their nickname."
								, getNames().get(getEmails().indexOf(readEmail(userID))), nickname)).queue();
						log(String.format(
								"%s has requested %s for their nickname."
								, username, nickname));
						
						setVerificationStep(userID, "3");
						
						/* Set their nickname */
						HHGController.setNickname(HHGGuild.getMember(event.getAuthor()), nickname).queue();
						break;
					/* User has sent invite code or skip */
					case "3":
						/* if skip */
						if (message.trim().toLowerCase().equals("skip")) {
							
							event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("Your verification request has been submitted. **You will receive a verification email to your *school* address with instructions on how to proceed**, along with a reminder DM from this account. If you are in need of assistance, please type `Help`.").queue());
							
							setVerificationStep(userID, "4");
							
							String verificationCode = randomVerificationCode();
							writeVerificationCode(userID, verificationCode);
							
							HHGGuild.getTextChannelById(HHG.LOG_CHANNEL_ID).sendMessage(String.format(
									"`%s`'s verification code is `%s`"
									, username, verificationCode)).queue();
							
							log(username + "'s verification of " + verificationCode + " has been recorded");
							
							/* Send verification email!!!! */
							sendVerificationEmail(readEmail(userID), verificationCode, event.getAuthor());
							
						} else if (getInviteCodes().contains(message.trim().toLowerCase())) {
							event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("Your invite code has been recorded.\nYour verification request has been submitted. **You will receive a verification email to your *school* address with instructions on how to proceed.** If you are in need of assistance, please type `Help`.").queue());
							
							writeVerificationCode(userID, randomVerificationCode());
							setVerificationStep(userID, "4");
							
							String verificationCode = randomVerificationCode();
							writeVerificationCode(userID, verificationCode);
							HHGGuild.getTextChannelById(HHG.LOG_CHANNEL_ID).sendMessage(String.format(
									"`%s`'s verification code is `%s`"
									, username, verificationCode)).queue();
							
							log(username + "'s verification of " + verificationCode + " has been recorded");
							log(username + "'s invite code of " + message.trim().toLowerCase() + " has been recorded");
							
							/* Send verification email!!!! */
							sendVerificationEmail(readEmail(userID), verificationCode, event.getAuthor());
							
						} else {
							event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("Hmm, I didn't recognize that invite code. Either try sending it again, or type `Skip` to continue. Type `Help` to receive assistance.").queue());
						}
						break;
					/* User has sent verification code */
					case "4":
						if (readVerificationCode(userID).equals(message.trim())) {
							event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("That code is correct. You have been given access to the HHG server. Please check out <#666327828798636074> for some more info on how the Guild works and the available prize money.\n" +
									"If you are still experiencing problems, please type `Help` or DM on of the HHG Admins.").queue());
							setVerificationStep(userID, "5");
							HHGController.addRolesToMember(HHGGuild.getMember(event.getAuthor()), HHGGuild.getRoleById(HHG.CONTESTANT_ROLE_ID)).queue();
						} else {
							event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("That code is incorrect. Please submit the six digit code you received in the verification email which was sent to your school address.").queue());
						}
						break;
					default:
						break;
				}
			} catch (IOException | MessagingException | GeneralSecurityException e) {
				e.printStackTrace();
			}
		} else {
			if (event.getAuthor().isBot()) return;
			
			String messageContent = event.getMessage().getContentRaw();
			
			if (messageContent.trim().toLowerCase().equals("!ping")) {
				event.getChannel().sendMessage("Pong!").queue();
			}
		}
		
	}
	
	private void sendVerificationEmail(String emailAddress, String code, User user) throws IOException, MessagingException, GeneralSecurityException {
		GmailSender.sendMessage(
				GmailSender.createEmail(emailAddress, "me", "Haslett High Guild - Verification",
						"Hello, and welcome to the Haslett High Guild!\n\n" +
								"You are receiving this because your name and email has been provided for the Discord account " + user.getName() + "#" + user.getDiscriminator() + ". Your verification code is " + code + ". Please send this code via DM to the HHG Bot and do not share it with anyone else.\n\n" +
								"If you are not the previously mentioned user, and you did not request this email verification, please delete this email and do not distribute the verification code. Please do not reply to this email. If you would like more information about this verification attempt or if you need any other assistance, contact Simon Kwilinski at 20kwilinsi@haslett.k12.mi.us. Include in your email the Discord username listed above.\n\n" +
								"Sincerely,\n" +
								"The Haslett High Guild Administrators"));
	}
	
	private void writeVerificationCode(String userID, String code) {
		try {
			addMemberToFile(userID);
			PrintWriter writer = new PrintWriter(new File("verification-codes/" + userID + ".txt"));
			writer.print(code);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private String readVerificationCode(String userID) throws FileNotFoundException {
		Scanner in = new Scanner(new File("verification-codes/" + userID + ".txt"));
		return in.next();
	}
	
	private List<String> getMembersFromFile() throws FileNotFoundException {
		Scanner in = new Scanner(HHG.GUILD_MEMBER_IDS_FILE);
		List<String> ids = new ArrayList<>();
		while (in.hasNextLine()) ids.add(in.nextLine().trim());
		return ids;
	}
	
	/**
	 * Adds a member to the guild_members_ids.txt file.
	 *
	 * @param id the id of the user
	 * @throws FileNotFoundException if the file is not found
	 */
	private void addMemberToFile(String id) throws FileNotFoundException {
		List<String> members = getMembersFromFile();
		if (!members.contains(id)) {
			try {
				PrintWriter printWriter = new PrintWriter(HHG.GUILD_MEMBER_IDS_FILE);
				printWriter.println(id);
				printWriter.close();
				log("Added user " + id + " to member list");
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/*  Called when someone joins the server. It reads the current list of members, and adds
	 *  them to a list and sends them some information to greet them.
	 */
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent guildJoinEvent) {
		List<String> currentMembers = null;
		try {
			currentMembers = getMembersFromFile();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		User user = guildJoinEvent.getMember().getUser();
		String userID = guildJoinEvent.getUser().getId();
		/* If user has not joined the server before, and is not a bot */
		assert currentMembers != null;
		if (!currentMembers.contains(userID) && !user.isBot()) {
			/* Initiate verification with user */
			user.openPrivateChannel().queue(channel -> channel.sendMessage("Hello, welcome to the Haslett High " +
					"Guild!\n\nSecurity for Haslett students is a top priority on the Guild. To gain access to the " +
					"server, please enter your *school* email address for verification.").queue());
			log("Initiated verification process with user " + userID);
			setVerificationStep(userID, "0");
			
			HHGGuild.getTextChannelById(HHG.LOG_CHANNEL_ID).sendMessage(String.format("`%s`: `%s#%s` (ID: `%s`) joined and was sent a DM", new Timestamp(new Date().getTime()), user.getName(), user.getDiscriminator(), user.getId())).queue();
			
		}
		
	}
	
	private void setVerificationStep(String ID, String step) {
		try {
			addMemberToFile(ID);
			PrintWriter writer = new PrintWriter(new File("verification-process/" + ID + ".txt"));
			writer.print(step);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void writeEmail(String ID, String email) {
		try {
			addMemberToFile(ID);
			PrintWriter writer = new PrintWriter(new File("emails/" + ID + ".txt"));
			writer.print(email);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private String readEmail(String ID) throws FileNotFoundException {
		Scanner in = new Scanner(new File("emails/" + ID + ".txt"));
		if (in.hasNext())
			return in.next();
		return null;
	}
}