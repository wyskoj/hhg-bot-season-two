import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@SuppressWarnings("unused")
public class Quest7Submission extends ListenerAdapter implements Serializable {
	public static final String TRAITOR_TEAM = "aggressively-average";
	
	/* USE THESE VARIABLES TO CONTROL THE BEHAVIOUR OF THE SUBMISSION BOT */
	/* ****************************************************************** */
	/* ****************************************************************** */
	
	/**
	 * The name of the quest. This is used in logging, so make sure that you set this when you setup this class.
	 * <p>
	 * Typically, this valid is just <code>quest-[quest#]</code> (e.g. <code>quest-1</code>).
	 */
	final String QUEST_NAME = "quest-7";
	
	/**
	 * If you do not want contestants to have the ability to use !submit, set this to <code>false</code>, <code>
	 * true</code> otherwise.
	 */
	final boolean CONTESTANT_USABLE = true;
	
	
	/**
	 * The list of valid codes. Uses the <code>ValidCode</code> object. Ensure these are LOWERCASE!
	 */
	transient ArrayList<ValidCode> validCodes = new ArrayList<ValidCode>() {{
		add(new ValidCode("aggressively-average", 3));
		add(new ValidCode("3musketeers", -4));
		add(new ValidCode("albino-africa", -4));
		add(new ValidCode("beta", -4));
		add(new ValidCode("buses", -4));
		add(new ValidCode("dads", -4));
		add(new ValidCode("hotpink-chi-unicorns", -4));
		add(new ValidCode("krusty-krab", -4));
		add(new ValidCode("moms", -4));
		add(new ValidCode("omniscient", -4));
		add(new ValidCode("rona-time", -4));
		add(new ValidCode("sendgods", -4));
		add(new ValidCode("sigma", -4));
		add(new ValidCode("underdogs", -4));
	}};
	
	/**
	 * The usage type for codes.
	 * <ul>
	 *     <li><code>CodeUse.ONCE_PER_TEAM</code> — For when you want each
	 *     team to be able to submit the same code.</li>
	 *     <li><code>CodeUse.ONCE_PER_ALL</code> — For when you want each
	 *     code be only usable once for all teams (e.g., if team A submits
	 *     code A, team B cannot submit code A).</li>
	 * </ul>
	 */
	final CodeUse CODE_USE = CodeUse.ONCE_BY_ALL;
	
	/**
	 * Useful for when you want the maximum number of codes to be submitted less than the possible number.
	 */
	final int MAX_SUBMITTABLE = Integer.MAX_VALUE;
	
	/**
	 * Negs can be applied when a team submits an invalid code.
	 */
	final boolean NEGS = false;
	/**
	 * The amount to neg by for each invalid submission.
	 */
	final int NEG_VALUE = 4; // Not used if NEGS == FALSE
	
	// COOLDOWN //
	/**
	 * Useful for limiting the number of codes a team can submit in a given time frame.
	 */
	final boolean USE_COOLDOWN = true;
	/**
	 * The amount of time a team must wait after submitting an invalid code.
	 */
	final int COOLDOWN_DURATION = 1800; // In seconds (60 = 1 min)
	/**
	 * Defines which types of submissions should a cooldown be implemented.
	 */
	final CooldownOn COOLDOWN_TYPE = CooldownOn.VALID;
	/* ****************************************************************** */
	/* ****************************************************************** */
	
	
	static JDA JDA;
	private static Guild HHGGuild;
	
	
	enum CodeUse {
		ONCE_BY_ALL,
		ONCE_PER_TEAM
	}
	
	enum CooldownOn {
		VALID,
		INVALID,
		BOTH
	}
	
	HashMap<String, LocalDateTime> coolDowns = new HashMap<>();
	
	public static void main(String[] args) throws LoginException {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(HHG.BOT_TOKEN);
		builder.addEventListener(new Quest7Submission());
		/* Defined on start */
		JDA = builder.buildAsync();
	}
	
	File questDirectory = new File("quest/" + QUEST_NAME);
	File codesFile = new File("quest/" + QUEST_NAME + "/codes.serial");
	File coolDownsFile = new File("quest/" + QUEST_NAME + "/cooldowns.serial");
	
	public void onReady(ReadyEvent event) {
		HHGGuild = JDA.getGuildById(HHG.HHG_SERVER_ID);
		teamCategory = HHGGuild.getCategoryById(HHG.TEAM_CHANNEL_CATEGORY);
		
		// Try to load any data if it exists
		
		if (questDirectory.exists() && Objects.requireNonNull(questDirectory.listFiles()).length != 0) {
			
			try {
				ObjectInputStream codesInput = new ObjectInputStream(new FileInputStream(codesFile));
				ObjectInputStream coolDownsInput = new ObjectInputStream(new FileInputStream(coolDownsFile));
				validCodes = (ArrayList<ValidCode>) codesInput.readObject();
				coolDowns = (HashMap<String, LocalDateTime>) coolDownsInput.readObject();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		updateFiles();
	}
	
	ValidCode getValidCodeByCode(String code) {
		for (ValidCode valid_code : validCodes) {
			if (valid_code.code.equals(code)) {
				return valid_code;
			}
		}
		return null;
	}
	
	static Category teamCategory;
	
	boolean teamHasGottenAllCodes(String teamName) {
		boolean allCodes = true;
		for (ValidCode validCode : validCodes) {
			if (!validCode.teamUsage.get(teamName)) {
				allCodes = false;
				break;
			}
		}
		return allCodes;
	}
	
	int howManyCodesATeamFound(String teamName) {
		int count = 0;
		for (ValidCode code : validCodes) {
			if (code.teamUsage.get(teamName)) {
				count++;
			}
		}
		return count;
	}
	
	void updateFiles() {
		if (!questDirectory.exists()) {
			questDirectory.mkdirs();
		}
		try {
			File codesFile = new File("quest/" + QUEST_NAME + "/codes.serial");
			File cooldownsFile = new File("quest/" + QUEST_NAME + "/cooldowns.serial");
			
			FileOutputStream f1 = new FileOutputStream(codesFile);
			FileOutputStream f2 = new FileOutputStream(cooldownsFile);
			ObjectOutputStream codesOut = new ObjectOutputStream(f1);
			ObjectOutputStream coolDownsOut = new ObjectOutputStream(f2);
			ArrayList<ValidCode> validCodes1 = (ArrayList<ValidCode>) validCodes.clone();
			HashMap<String, LocalDateTime> coolDowns1 = (HashMap<String, LocalDateTime>) coolDowns.clone();
			codesOut.writeObject(validCodes1);
			coolDownsOut.writeObject(coolDowns1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getAuthor().isBot()) return;
		final Message message = event.getMessage();
		final String messageContent = message.getContentRaw();
		final Category messageCategory = message.getCategory();
		final TextChannel channel = message.getTextChannel();
		final String teamName = channel.getName();
		
		if (!messageCategory.equals(teamCategory)) return;
		String[] args = messageContent.trim().toLowerCase().split(" ");
		if (!args[0].equals("!submit")) return;
		
		String userSubmission = args[1];
		
		if (channel.getName().equals(TRAITOR_TEAM)) {
			return;
		}
		
		ValidCode usersCode = getValidCodeByCode(userSubmission);
		if (messageCategory.equals(teamCategory)) {

			LocalDateTime now = LocalDateTime.now();
			LocalDateTime lastCooldown = coolDowns.get(teamName);
			if (lastCooldown != null) {
				long lastCooldownEpoch = lastCooldown.toEpochSecond(ZoneOffset.UTC);
				long nowEpoch = now.toEpochSecond(ZoneOffset.UTC);
				if (Math.abs(lastCooldownEpoch - nowEpoch) < COOLDOWN_DURATION) {
					channel.sendMessage(String.format("Please wait %d seconds to submit again.",
							COOLDOWN_DURATION - (int) Math.abs(lastCooldownEpoch - nowEpoch)
					)).queue();
					updateFiles();
					return;
				}
			}
			if (usersCode == null) { // IF INVALID
				channel.sendMessage("That's not a valid team.").queue();
				return;
			} else { // IF VALID
				if (usersCode.code.equals(TRAITOR_TEAM) && usersCode.teamUsage.get(teamName)) {
					channel.sendMessage("You've already won!").queue();
					updateFiles();
					return;
				}
				if (usersCode.teamUsage.get(teamName)) {
					channel.sendMessage("You've already tried this team.").queue();
					updateFiles();
					return;
				}
				if (usersCode.code.equals(TRAITOR_TEAM)) {
					for (Map.Entry<String, Boolean> entry : getValidCodeByCode(TRAITOR_TEAM).teamUsage.entrySet()) {
						if (entry.getValue()) {
							channel.sendMessage("Sorry, someone beat you to it!").queue();
							return;
						}
					}
					channel.sendMessage("**Congratulations!** You successfully identified the traitor. You've been awarded **3** points!").queue();
					BotComManager.changeTeamScore(teamName, 3, JDA);
					usersCode.teamUsage.replace(teamName, true);
					HHGGuild.getTextChannelById(HHG.QUEST_LOG_CHANNEL_ID).sendMessage(String.format(
							"`[%s] [%s] %s (%s) submitted %s (%s) %s`",
							new Date().toString(),
							QUEST_NAME,
							teamName,
							HHGGuild.getMember(message.getAuthor()).getNickname(),
							userSubmission,
							"CORRECT",
							""
					)).queue();
					updateFiles();
				} else {
					channel.sendMessage("**Aw!** You guessed incorrectly. You lost **4** points.").queue();
					BotComManager.changeTeamScore(teamName, -4, JDA);
					usersCode.teamUsage.replace(teamName, true);
					HHGGuild.getTextChannelById(HHG.QUEST_LOG_CHANNEL_ID).sendMessage(String.format(
							"`[%s] [%s] %s (%s) submitted %s (%s) %s`",
							new Date().toString(),
							QUEST_NAME,
							teamName,
							HHGGuild.getMember(message.getAuthor()).getNickname(),
							userSubmission,
							"WRONG",
							""
					)).queue();
					BotComManager.changeTeamScore(usersCode.code, 1, JDA);
					for (TextChannel channel1 : HHGGuild.getCategoryById(HHG.TEAM_CHANNEL_CATEGORY).getTextChannels()) {
						if (channel1.getName().equals(usersCode.code)) {
							channel1.sendMessage("**" + teamName + "** incorrectly guessed you! You earned a point.").queue();
							break;
						}
					}
					
					applyCooldownNow(teamName);
					updateFiles();
				}
			}
			updateFiles();
		} else { // not in team category
			channel.sendMessage(
					"Send this in your team channel!"
			).queue();
		}
		updateFiles();
	}
	
	private void applyCooldownNow(String teamName) {
		if (coolDowns.get(teamName) == null) {
			coolDowns.put(teamName, LocalDateTime.now());
		} else {
			coolDowns.replace(teamName, LocalDateTime.now());
		}
	}
}
