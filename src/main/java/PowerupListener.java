import Powerup.Gift;
import Powerup.Kamikaze;
import Powerup.Shield;
import Powerup.Vault;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import static java.lang.Integer.parseInt;

public class PowerupListener extends ListenerAdapter {
	
	private static class DailyCheck extends TimerTask {
		public void run() {
			// TODO implement this
		}
	}
	
	static net.dv8tion.jda.core.JDA JDA;
	
	static Guild guild;
	static Category teamCategory;
	
	List<DayOfWeek> tueThruFri = new ArrayList<DayOfWeek>(){{
		add(DayOfWeek.TUESDAY);
		add(DayOfWeek.WEDNESDAY);
		add(DayOfWeek.THURSDAY);
		add(DayOfWeek.FRIDAY);
	}};
	List<DayOfWeek> monThruThu = new ArrayList<DayOfWeek>(){{
		add(DayOfWeek.MONDAY);
		add(DayOfWeek.TUESDAY);
		add(DayOfWeek.WEDNESDAY);
		add(DayOfWeek.THURSDAY);
	}};
	
	public static void main(String[] args) throws LoginException {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(HHG.BOT_TOKEN);
		builder.addEventListener(new PowerupListener());
		/* Defined on start */
		JDA = builder.buildAsync();
		
	}
	
	public void onReady(ReadyEvent event) {
		guild = JDA.getGuildById(HHG.HHG_SERVER_ID);
		teamCategory = guild.getCategoryById(HHG.TEAM_CHANNEL_CATEGORY);
		System.out.println(HHG.getTeamPoints("example-team"));
	}
	
	TextChannel getTeamChannelByName(String teamName) {
		for (TextChannel channel : teamCategory.getTextChannels()) {
			if (channel.getName().equals(teamName)) {
				return channel;
			}
		}
		return null;
	}
	
	boolean currentlyBetween0745And1400() {
		LocalTime now = LocalTime.now();
		if (now.getHour() < 7 || now.getHour() > 14) {
			return false;
		} else if (now.getHour() == 7) {
			return now.getMinute() >= 45;
		} else if (now.getHour() == 14) {
			return now.getMinute() == 0;
		}
		return true;
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		
		Message message = event.getMessage();
		String messageContent = message.getContentRaw().trim();
		MessageChannel channel = message.getChannel();
		String teamName = channel.getName();
		
		LocalDateTime now = LocalDateTime.now();
		
		if (messageContent.toLowerCase().startsWith("!powerup")) {
			if (!message.getCategory().equals(teamCategory)) {
				channel.sendMessage("Use this command in your team channel.").queue();
				return;
			}
			if (messageContent.equalsIgnoreCase("!powerup")) {
				channel.sendMessage("Here's a list of syntaxes for powerups. Visit the powerup store for more details:" +
						"\n```" +
						"\n!powerup buy vault [point value]" +
						"\n!powerup buy kamikaze [team to attack]" +
						"\n!powerup buy shield" +
						"\n!powerup buy clue" +
						"\n!powerup buy gift [point value] [team to gift]" +
						"\n```").queue();
			} else {
				String[] args = messageContent
						.toLowerCase()
						.replaceAll("\\[", "")
						.replaceAll("]", "")
						.split("\\s");
				
				// 0 = "!powerup"
				// 1 = "buy"
				// 2 = type
				// 3 = point value
				// 4 = opposing team
				
				if (!args[1].equals("buy")) return;
				
				switch (args[2]) {
					case "vault": //!powerup buy vault [value]
						if (args.length != 4) {
							channel.sendMessage("Please check your syntax. The syntax is:\n" +
									"`!powerup buy vault [point value]`").queue();
							return;
						}
						int vaultBuyingPoints;
						try {
							vaultBuyingPoints = parseInt(args[3]);
						} catch (NumberFormatException e) {
							channel.sendMessage("Please check your syntax. The correct syntax is:\n" +
									"`!powerup buy vault [point value]`").queue();
							return;
						}
						
						if (Vault.teamHasActiveVault(teamName)) {
							channel.sendMessage("You already have an active vault. Your vault powerup will be" +
									" returned on next " + Vault.dayOfWeekToReturnVault(teamName) + ".").queue();
						} else {
							if (vaultBuyingPoints > HHG.getTeamPoints(teamName) / 2) {
								channel.sendMessage("You cannot vault that many points.").queue();
							} else {
								if (Vault.registerVault(teamName, vaultBuyingPoints)) {
									channel.sendMessage("You successfully purchased a Vault powerup for " +
											vaultBuyingPoints + " points. It will be returned next " +
											Vault.dayOfWeekToReturnVault(teamName) + " for a " +
											(int) Math.floor(vaultBuyingPoints * 1.5) + " point dividend.").queue();
									BotComManager.changeTeamScore(teamName, -vaultBuyingPoints, JDA);
								} else {
									channel.sendMessage("Something didn't work right. Try again.").queue();
								}
							}
						}
						break;
					case "kamikaze": //!powerup buy kamikaze [team-name]
						String opposingTeam;
						boolean valid;
						try {
							opposingTeam = args[3].toLowerCase().trim();
							valid = false;
							for (String name : HHG.TEAM_NAMES) {
								if (name.equals(opposingTeam)) {
									valid = true;
									break;
								}
							}
						} catch (Exception e) {
							channel.sendMessage("Please check your syntax. The correct syntax is:\n" +
									"`!powerup buy kamikaze [opposing team]`").queue();
							return;
						}
						
						if (!valid) {
							channel.sendMessage(opposingTeam + " is not a valid team.").queue();
							return;
						}
						if (now.getDayOfWeek().equals(DayOfWeek.SATURDAY) ||
								now.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
							channel.sendMessage("You can only purchase a Kamikaze on weekdays.").queue();
							return;
						}
						if (Shield.teamHasActiveShield(teamName)) {
							channel.sendMessage("You cannot purchase Kamikaze when you have an active Shield. " +
									"You'll have to wait until tomorrow when your Shield expires.").queue();
							return;
						}
						if (ScoreListener.isTeamInLastPlace(teamName)) {
							channel.sendMessage("You cannot purchase Kamikaze when in last place on the " +
									"leaderboard.").queue();
							return;
						}
						if (HHG.getTeamPoints(teamName) < 5) {
							channel.sendMessage("You need 5 or more points to purchase Kamikaze (in case they " +
									"have a shield).").queue();
							return;
						}
						if (args.length != 4) {
							channel.sendMessage("Please check your syntax. The correct syntax is:\n" +
									"`!powerup buy kamikaze [opposing team]`").queue();
							return;
						}
						
						
						
						if (Kamikaze.teamHasAttackedTeamToday(teamName, opposingTeam)) {
							channel.sendMessage("You cannot purchase Kamikaze twice against the same team" +
									" twice in one day!!").queue();
							return;
						}
						if (HHG.getTeamPoints(opposingTeam) < 1) {
							channel.sendMessage("Your opposing team needs at least 1 point to Kamikaze them!").queue();
							return;
						}
						try {
							Kamikaze.registerNewKamikaze(new Kamikaze(teamName, opposingTeam, now));
							channel.sendMessage("You successfully purchased a Kamikaze against **" + opposingTeam
									+ "** for" +
									" two points.").queue();
							BotComManager.changeTeamScore(teamName, -2, JDA);
							if (Shield.teamHasActiveShield(opposingTeam)) {
								channel.sendMessage("**" + opposingTeam + "** blocked your Kamikaze attempt with a shield. "
										+ "You lost an additional 3 points.").queue();
								BotComManager.changeTeamScore(teamName, -3, JDA);
								getTeamChannelByName(opposingTeam).sendMessage("**" + teamName + "** attempted to " +
										"hit you with Kamikaze, but were blocked by your shield.").queue();
							} else {
								getTeamChannelByName(opposingTeam).sendMessage("**" + teamName + "** hit you" +
										" with a Kamikaze powerup. You lost 4 points.").queue();
								BotComManager.changeTeamScore(opposingTeam, -4, JDA);
								if (HHG.getTeamPoints(teamName) < 0) {
									BotComManager.changeTeamScore(opposingTeam, -HHG.getTeamPoints(teamName), JDA);
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					case "shield": // !powerup buy shield
						if (now.getDayOfWeek().equals(DayOfWeek.SATURDAY) ||
								now.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
							channel.sendMessage("You can only purchase a Shield on weekdays.").queue();
							return;
						}
						if (Shield.teamHasActiveShield(teamName)) {
							channel.sendMessage("You already have an active shield, bro!").queue();
							return;
						}
						if (HHG.getTeamPoints(teamName) < 4) {
							channel.sendMessage("You must have at least 4 points to buy a Shield.").queue();
							return;
						}
						try {
							Shield.registerShield(new Shield(teamName, LocalDate.now()));
							BotComManager.changeTeamScore(teamName, -4, JDA);
							channel.sendMessage("Congratulations! You successfully purchased a Shield" +
									" powerup for 4 points. Any incoming Kamikaze attacks will be reversed" +
									" and hit your opponent with 3 points.").queue();
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					case "clue": // !powerup buy clue
						if (!tueThruFri.contains(LocalDate.now().getDayOfWeek()) || !currentlyBetween0745And1400()) { // if not tue - fri
							channel.sendMessage("You may only purchase the Clue powerup on Quest days between" +
									" 7:45 AM and 2:00 PM.").queue();
							return;
						}
						if (HHG.getTeamPoints(teamName) < 2) {
							channel.sendMessage("You need at least 2 points to buy a Clue.").queue();
							return;
						}
						TextChannel clueChannel = guild.getTextChannelById("675406286720073758");
						if (!clueChannel.hasLatestMessage()) {
							channel.sendMessage("You cannot buy a clue right now. Either there was not a " +
									"clue written for this Quest, or there is not a live Quest today.\n\nIf you " +
									"believe this is an error, please notify an administrator.").queue();
							return;
						}
						String latestID = clueChannel.getLatestMessageId();
						String clueContents = clueChannel.getMessageById(latestID).complete().getContentRaw();
						channel.sendMessage("You have successfully purchased a clue for 2 points. Here is your clue:\n\n" + clueContents).queue();
						BotComManager.changeTeamScore(teamName, -2, JDA);
						break;
					case "gift": // !powerup buy gift [point value] [team to gift]
						String receivingTeam;
						boolean valid1;
						try {
							receivingTeam = args[4].toLowerCase().trim();
							valid1 = false;
							for (String name : HHG.TEAM_NAMES) {
								if (name.equals(receivingTeam)) {
									valid1 = true;
									break;
								}
							}
						} catch (Exception e) {
							channel.sendMessage("Please check your syntax. The correct syntax is:\n" +
									"`!powerup buy gift [point value] [team to gift]`").queue();
							return;
						}
						if (!valid1) {
							channel.sendMessage(receivingTeam + " is not a valid team.").queue();
							return;
						}
						int value;
						try {
							value = parseInt(args[3].trim());
						} catch (Exception e) {
							channel.sendMessage("Please check your syntax. The correct syntax is:\n" +
									"`!powerup buy gift [point value] [team to gift]`").queue();
							return;
						}
						if (!monThruThu.contains(LocalDate.now().getDayOfWeek())) {
							channel.sendMessage("You can only send Gifts on Monday through Thursday.").queue();
							return;
						}
						if (Gift.teamHasGiftedTeamToday(teamName, receivingTeam)) {
							channel.sendMessage("You can only send one Gift per team per day.").queue();
							return;
						}
						if (value > 3 || value < 1) {
							channel.sendMessage("You cannot gift " + value + " points.").queue();
							return;
						}
						try {
							Gift.registerGift(new Gift(teamName, receivingTeam, value, LocalDate.now()));
							BotComManager.changeTeamScore(teamName, -value, JDA);
							BotComManager.changeTeamScore(receivingTeam, value, JDA);
							channel.sendMessage("You have gifted **" + receivingTeam + "** *" + value + "* points.").queue();
							getTeamChannelByName(receivingTeam).sendMessage("You have been gifted *" + value + "* points by **" + teamName + "**.").queue();
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					
					default:
						channel.sendMessage("Hmm. I can't make any sense of this. Try again.").queue();
				}
				
			}
		} // if starts with "!powerup"
		
	}
}
