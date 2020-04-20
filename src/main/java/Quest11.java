import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

public class Quest11 extends ListenerAdapter {
	static net.dv8tion.jda.core.JDA JDA;
	public static void main(String[] args) throws LoginException {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(HHG.BOT_TOKEN);
		builder.addEventListener(new Quest11());
		/* Defined on start */
		JDA = builder.buildAsync();
	}
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		final Guild guildById = JDA.getGuildById(HHG.HHG_SERVER_ID);
		if (event.getMessage().getContentRaw().trim().toLowerCase().equals("!addpoint")
		&& guildById.getMember(event.getAuthor()).getRoles().contains(
				guildById.getRoleById(HHG.ADMIN_ROLE_ID)
		)) {
			BotComManager.changeTeamScore(event.getChannel().getName(),1,JDA);
			event.getChannel().sendMessage("You got a point! :)").complete();
		}
	}
}
