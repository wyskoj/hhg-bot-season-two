import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

public class HA extends ListenerAdapter {
	static net.dv8tion.jda.core.JDA JDA;
	public static void main(String[] args) throws LoginException {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(HHG.BOT_TOKEN);
		builder.addEventListener(new HA());
		/* Defined on start */
		JDA = builder.buildAsync();
	}
	
	public void onReady(ReadyEvent event) {
		System.out.println(HHG.getTeamPoints("example-team"));
	}
}
