import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import javax.xml.soap.Text;
import java.util.List;

public class MessageCount extends ListenerAdapter {
	
	static net.dv8tion.jda.core.JDA JDA;
	
	
	public static void main(String[] args) throws LoginException {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(HHG.BOT_TOKEN);
		builder.addEventListener(new MessageCount());
		/* Defined on start */
		JDA = builder.buildAsync();
	}
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String content = event.getMessage().getContentRaw().trim().toLowerCase();
		TextChannel channel = event.getChannel();
		if (content.equals("!messagecount")) {
			if (!JDA.getGuildById(HHG.HHG_SERVER_ID).getMember(event.getAuthor()).getRoles().contains(JDA.getRoleById(HHG.ADMIN_ROLE_ID))) {
				return;
			}
			Message ffff = channel.sendMessage("Getting counts...").complete();
			StringBuilder builder = new StringBuilder();
			builder.append("```\n");
			List<TextChannel> channelList = JDA.getCategoryById(HHG.TEAM_CHANNEL_CATEGORY).getTextChannels();
			for (TextChannel channel1 : channelList) {
				int count = 0;
				for (Message ignored : channel1.getIterableHistory()) {
					count++;
				}
				builder.append(String.format("%20s %d%n", channel1.getName(), count));
				System.out.println(channel1.getName());
			}
			builder.append("```");
			ffff.editMessage(builder.toString()).complete();
		}
	}

	
	
}
