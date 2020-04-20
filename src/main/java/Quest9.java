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
public class Quest9 extends ListenerAdapter implements Serializable {
	
	/* USE THESE VARIABLES TO CONTROL THE BEHAVIOUR OF THE SUBMISSION BOT */
	/* ****************************************************************** */
	/* ****************************************************************** */
	
	/**
	 * The name of the quest. This is used in logging, so make sure that you set this when you setup this class.
	 * <p>
	 * Typically, this valid is just <code>quest-[quest#]</code> (e.g. <code>quest-1</code>).
	 */
	final String QUEST_NAME = "quest-9";
	
	/**
	 * If you do not want contestants to have the ability to use !submit, set this to <code>false</code>, <code>
	 * true</code> otherwise.
	 */
	final boolean CONTESTANT_USABLE = true;
	
	
	/**
	 * The list of valid codes. Uses the <code>ValidCode</code> object. Ensure these are LOWERCASE!
	 */
	transient ArrayList<ValidCode> validCodes = new ArrayList<ValidCode>() {{
		add(new ValidCode("round", 1));
		add(new ValidCode("scale", 1));
		add(new ValidCode("march", 1));
		
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
	final boolean NEGS = true;
	/**
	 * The amount to neg by for each invalid submission.
	 */
	final int NEG_VALUE = 1; // Not used if NEGS == FALSE
	
	// COOLDOWN //
	/**
	 * Useful for limiting the number of codes a team can submit in a given time frame.
	 */
	final boolean USE_COOLDOWN = true;
	/**
	 * The amount of time a team must wait after submitting an invalid code.
	 */
	final int COOLDOWN_DURATION = 60 * 10; // In seconds (60 = 1 min)
	/**
	 * Defines which types of submissions should a cooldown be implemented.
	 */
	final CooldownOn COOLDOWN_TYPE = CooldownOn.BOTH;
	
	/**
	 * Dont fucking touch this bitch
	 */
	int batchNumber = 1;
	
	String[][] codeSets = new String[][] {
			{"round", "scale", "march"},
			{"mount", "olympus", "jupiter"},
			{"play", "triangle", "hook"},
			{"washington", "teacher", "capital"},
			{"bank", "thief", "shop"},
			{"ruler", "hollywood", "iron"},
	};
	
	String[] clueTexts = new String[] {
			"",
			"divine",
			"instrument",
			"union",
			"iscariot",
			"powerful"
	};
	
	String[] urls = new String[] {
			"",
			"https://cdn.discordapp.com/attachments/674247071846629406/681852874699046933/Board_1.jpg",
			"https://cdn.discordapp.com/attachments/674247071846629406/681852883561611264/Board_2.jpg",
			"https://cdn.discordapp.com/attachments/674247071846629406/681852888284266526/Board_3.jpg",
			"https://cdn.discordapp.com/attachments/674247071846629406/681852894776918066/Board_4.jpg",
			"https://cdn.discordapp.com/attachments/674247071846629406/681852898015313945/Board_5.jpg",
			"https://cdn.discordapp.com/attachments/674247071846629406/681852900028448892/Board_6.jpg"
	};
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
		builder.addEventListener(new Quest9());
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
		
		if (messageContent.trim().toLowerCase().equals("!updatecodes") && HHGGuild.getMember(message.getAuthor()).getRoles().contains(HHGGuild.getRoleById(HHG.ADMIN_ROLE_ID))) {
			resetCooldowns();
			validCodes.clear();
			HHGGuild.getTextChannelById("641987483739095051").sendMessage("The next clue is **" + clueTexts[batchNumber] + "**.\n" + urls[batchNumber]).queue();
			for (int i = 0; i < codeSets[batchNumber].length; i++) {
				validCodes.add(new ValidCode(codeSets[batchNumber][i], 1));
			}
			batchNumber++;
			channel.sendMessage("updated.").queue();
			
			
			return;
		}
		
		if (!messageCategory.equals(teamCategory)) return;
		
		String[] args = messageContent.trim().toLowerCase().split(" ");
		
		
		
		if (!args[0].equals("!submit")) return;
		
		String userSubmission = args[1];
		
		if (!CONTESTANT_USABLE && HHGGuild.getMember(message.getAuthor()).getRoles().contains(HHGGuild.getRoleById(HHG.CONTESTANT_ROLE_ID))) {
			channel.sendMessage("An admin must use this command for you!").queue();
			return;
		}
		
		
		
		ValidCode usersCode = getValidCodeByCode(userSubmission);
		if (messageCategory.equals(teamCategory)) {
			int count = 0;
			for (ValidCode valid_code : validCodes) {
				if (valid_code.teamUsage.get(teamName)) {
					count++;
				}
			}
			if (isInPreviousSet(userSubmission)) {
				channel.sendMessage("Sorry, this code isn't good anymore!").queue();
				HHGGuild.getTextChannelById(HHG.QUEST_LOG_CHANNEL_ID).sendMessage(String.format(
						"`[%s] [%s] %s (%s) submitted %s (%s) %s`",
						new Date().toString(),
						QUEST_NAME,
						teamName,
						HHGGuild.getMember(message.getAuthor()).getNickname(),
						userSubmission,
						"EXPIRED",
						""
				)).queue();
				return;
			}
			if (count >= MAX_SUBMITTABLE) {
				channel.sendMessage(
						"You have already submitted the max number of codes!"
				).queue();
				return;
			}
			if (teamHasGottenAllCodes(teamName)) {
				channel.sendMessage(
						"You have already submitted all possible codes!"
				).queue();
				return;
			}
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime lastCooldown = coolDowns.get(teamName);
			if (lastCooldown != null) {
				long lastCooldownEpoch = lastCooldown.toEpochSecond(ZoneOffset.UTC);
				long nowEpoch = now.toEpochSecond(ZoneOffset.UTC);
				if (Math.abs(lastCooldownEpoch - nowEpoch) < COOLDOWN_DURATION) {
					channel.sendMessage(String.format("Please wait %d seconds to submit again.",
							COOLDOWN_DURATION - (int) Math.abs(lastCooldownEpoch - nowEpoch)
					)).queue();
					return;
				}
			}
			if (usersCode == null) { // IF INVALID
				if (COOLDOWN_TYPE == CooldownOn.INVALID || COOLDOWN_TYPE == CooldownOn.BOTH) {
					applyCooldownNow(teamName);
				}
				channel.sendMessage("Sorry, that's not a valid code!").queue();
				if (NEGS) {
					
						BotComManager.changeTeamScore(teamName, -NEG_VALUE, JDA);
						channel.sendMessage("You've lost **" + NEG_VALUE + "** point" + (NEG_VALUE == 1 ? "" : "s") + ".").queue();

				}
				HHGGuild.getTextChannelById(HHG.QUEST_LOG_CHANNEL_ID).sendMessage(String.format(
						"`[%s] [%s] %s (%s) submitted %s (%s) %s`",
						new Date().toString(),
						QUEST_NAME,
						teamName,
						HHGGuild.getMember(message.getAuthor()).getNickname(),
						userSubmission,
						"INVALID",
						""
				)).queue();
			} else { // IF VALID
				if (usersCode.teamUsage.get(teamName)) {
					channel.sendMessage("You have already gotten this code!").queue();
					return;
				}
				
				if (CODE_USE == CodeUse.ONCE_BY_ALL) {
					HashMap<String, Boolean> codeUsage = usersCode.teamUsage;
					for (Map.Entry<String, Boolean> entry : codeUsage.entrySet()) {
						if (entry.getValue()) {
							channel.sendMessage("Sorry, this code has already been used!").queue();
							
							HHGGuild.getTextChannelById(HHG.QUEST_LOG_CHANNEL_ID).sendMessage(String.format(
									"`[%s] [%s] %s (%s) submitted %s (%s) %s`",
									new Date().toString(),
									QUEST_NAME,
									teamName,
									HHGGuild.getMember(message.getAuthor()).getNickname(),
									userSubmission,
									"INVALIDATED",
									""
							)).queue();
							return;
						}
					}
				}
				
				
				usersCode.teamUsage.replace(teamName, true);
				int codeCount = howManyCodesATeamFound(teamName);
				channel.sendMessage("Congrats! You found a valid code. You have gotten " + codeCount + " code" +
						(codeCount != 1 ? "s" : "") + ".").queue();
				HHGGuild.getTextChannelById(HHG.QUEST_LOG_CHANNEL_ID).sendMessage(String.format(
						"`[%s] [%s] %s (%s) submitted %s (%s) %s`",
						new Date().toString(),
						QUEST_NAME,
						teamName,
						HHGGuild.getMember(message.getAuthor()).getNickname(),
						userSubmission,
						"VALID",
						"for " + usersCode.value + " points"
				)).queue();
				if (teamHasGottenAllCodes(teamName)) {
					channel.sendMessage("You have found all valid codes!").queue();
				}
				BotComManager.changeTeamScore(teamName, usersCode.value, JDA);
				if (COOLDOWN_TYPE == CooldownOn.VALID || COOLDOWN_TYPE == CooldownOn.BOTH) {
					applyCooldownNow(teamName);
				}
			}
			updateFiles();
		} else { // not in team category
			channel.sendMessage(
					"Send this in your team channel!"
			).queue();
		}
	}
	
	private boolean isInPreviousSet(String userSubmission) {
		boolean isInPreviousSet = false;
		for (int i = 0; i < batchNumber - 1; i++) {
			for (int j = 0; j < codeSets[i].length; j++) {
				if (codeSets[i][j].equals(userSubmission)) {
					isInPreviousSet = true;
					break;
				}
			}
		}
		return isInPreviousSet;
	}
	
	private void resetCooldowns() {
		Iterator iterator = coolDowns.entrySet().iterator();
		List<String> keys = new ArrayList<>();
		while (iterator.hasNext()) {
			Map.Entry<String, LocalDateTime> entry = (Map.Entry<String, LocalDateTime>) iterator.next();
			keys.add(entry.getKey());
		}
		for (String key : keys) {
			coolDowns.replace(key, null);
		}
	}
	
	private void applyCooldownNow(String teamName) {
		if (coolDowns.get(teamName) == null) {
			coolDowns.put(teamName, LocalDateTime.now());
		} else {
			coolDowns.replace(teamName, LocalDateTime.now());
		}
	}
}
