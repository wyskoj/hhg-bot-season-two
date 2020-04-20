import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DingDong extends ListenerAdapter {
	static net.dv8tion.jda.core.JDA JDA;
	public static void main(String[] args) throws LoginException {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(HHG.BOT_TOKEN);
		builder.addEventListener(new DingDong());
		/* Defined on start */
		JDA = builder.buildAsync();
	}
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		
		Pattern teamSend = Pattern.compile("!message\\s+([a-zA-Z-]+)\\s+(.+)");
		final String triggerMessageText = event.getMessage().getContentRaw();
		Matcher teamSendMatcher = teamSend.matcher(triggerMessageText);
		if (teamSendMatcher.find()) {
			boolean isValid = false;
			for (TextChannel channel : JDA.getCategoryById(HHG.TEAM_CHANNEL_CATEGORY).getTextChannels()) {
				if (channel.equals(event.getChannel())) {
					isValid = true;
					break;
				}
			}
			if (!isValid) return;;
			String teamToSendTo = teamSendMatcher.group(1).toLowerCase().trim();
			String messageToSend = teamSendMatcher.group(2);
			if (messageToSend.contains("@everyone") || messageToSend.contains("@here")) {
				event.getChannel().sendMessage("<@" + event.getAuthor().getId() + "> nice try...").queue();
				return;
			}
			boolean validTeamName  =false;
			for (String teamName : HHG.TEAM_NAMES) {
				if (teamToSendTo.equals(teamName)) {
					validTeamName = true;
					break;
				}
			}
			if (!validTeamName) {
				event.getChannel().sendMessage(String.format(
						"%s is not a valid team!", teamToSendTo
				)).queue();
				return;
			}
			for (TextChannel channel : JDA.getGuildById(HHG.HHG_SERVER_ID).getCategoryById(HHG.TEAM_CHANNEL_CATEGORY).getTextChannels()) {
				if (channel.getName().equals(teamToSendTo)) {
					channel.sendMessage("You have a special secret message from **" + event.getChannel().getName() + "**:\n\n" + messageToSend).queue();
					event.getChannel().sendMessage("Your message has been sent!").queue();
				}
			}
		}
		
		if (!event.getChannel().getId().equals("684756780580208649")) return;
		if (triggerMessageText.trim().toLowerCase().equals("ding")) {
			event.getChannel().sendMessage(":bell: dong :bell:").queue();
		}
		if (triggerMessageText.trim().toLowerCase().equals("dong")) {
			event.getChannel().sendMessage(":bell: ding :bell:").queue();
		}
		if (triggerMessageText.trim().toLowerCase().equals("deng")) {
			event.getChannel().sendMessage(":bell: dang :bell:").queue();
		}
		if (triggerMessageText.trim().toLowerCase().equals("dang")) {
			event.getChannel().sendMessage(":bell: deng :bell:").queue();
		}
		if (triggerMessageText.trim().toLowerCase().equals("baby shark")) {
			event.getChannel().sendMessage("doo dot doo doo doot dodo").queue();
		}
	}
}
