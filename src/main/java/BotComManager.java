import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;

public class BotComManager {
	static public void changeTeamScore(String teamName, int value, JDA context) {
		TextChannel botCom = context.getGuildById(HHG.HHG_SERVER_ID).getTextChannelById(HHG.BOT_COM_CHANNEL_ID);
		if (value < 0) {
			botCom.sendMessage(String.format(
					"remove;%s;%d",
					teamName,
					-value
			)).complete();
		} else if (value > 0) {
			botCom.sendMessage(String.format(
					"add;%s;%d",
					teamName,
					value
			)).complete();
		}
		
		if (HHG.getTeamPoints(teamName) < 0) {
			System.out.println("recursion oof");
			changeTeamScore(teamName, -HHG.getTeamPoints(teamName), context);
		}
	}
}
