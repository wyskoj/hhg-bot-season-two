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
public class Quest14 extends ListenerAdapter implements Serializable {
	
	/* USE THESE VARIABLES TO CONTROL THE BEHAVIOUR OF THE SUBMISSION BOT */
	/* ****************************************************************** */
	/* ****************************************************************** */
	
	/**
	 * The name of the quest. This is used in logging, so make sure that you set this when you setup this class.
	 * <p>
	 * Typically, this valid is just <code>quest-[quest#]</code> (e.g. <code>quest-1</code>).
	 */
	final String QUEST_NAME = "quest-14";
	
	/**
	 * If you do not want contestants to have the ability to use !submit, set this to <code>false</code>, <code>
	 * true</code> otherwise.
	 */
	final boolean CONTESTANT_USABLE = true;
	
	
	/**
	 * The list of valid codes. Uses the <code>ValidCode</code> object. Ensure these are LOWERCASE!
	 */
	transient ArrayList<ValidCode> validCodes = new ArrayList<ValidCode>() {{
		add(new ValidCode("fjueu5", 4));
		add(new ValidCode("wx89yk", 4));
		add(new ValidCode("ijt9xn", 4));
		add(new ValidCode("yz3p8d", 4));
		add(new ValidCode("3ma1t1", 4));
		add(new ValidCode("vqbt8d", 4));
		add(new ValidCode("aavbbi", 4));
		add(new ValidCode("apz716", 4));
		add(new ValidCode("uh748j", 4));
		add(new ValidCode("wv7mpa", 4));
		add(new ValidCode("5j3hrh", 4));
		add(new ValidCode("j1fg4l", 4));
		add(new ValidCode("anchng", 4));
		add(new ValidCode("rj4jfu", 4));
		add(new ValidCode("dkqj52", 4));
		add(new ValidCode("fpkhn8", 4));
		add(new ValidCode("sji7qd", 4));
		add(new ValidCode("29yx8f", 4));
		add(new ValidCode("jdftxx", 4));
		add(new ValidCode("aczbx4", 4));
		add(new ValidCode("sz4mt2", 4));
		add(new ValidCode("97nu3g", 4));
		add(new ValidCode("wwyitc", 2));
		add(new ValidCode("wd8cem", 2));
		add(new ValidCode("whsysu", 2));
		add(new ValidCode("regpdl", 2));
		add(new ValidCode("agents", 2));
		add(new ValidCode("thanku", 2));
		add(new ValidCode("vessel", 2));
		add(new ValidCode("statue", 2));
		add(new ValidCode("montyb", 2));
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
	final int COOLDOWN_DURATION = 5 * 60; // In seconds (60 = 1 min)
	/**
	 * Defines which types of submissions should a cooldown be implemented.
	 */
	final CooldownOn COOLDOWN_TYPE = CooldownOn.BOTH;
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
	
	HashMap<String, Integer> teamHandValue = new HashMap<String, Integer>() {{
		put("example-team", 0);
		put("alpha", 0);
		put("beta", 0);
		put("buses", 0);
		put("dads", 0);
		put("moms", 0);
		put("omniscient", 0);
		put("rona-time", 0);
	}};
	
	final String[] teams = new String[] {
			"example-team",
			"alpha",
			"beta",
			"buses",
			"dads",
			"moms",
			"omniscient",
			"rona-time"
	};
	
	HashMap<String, LocalDateTime> coolDowns = new HashMap<>();
	
	public static void main(String[] args) throws LoginException {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(HHG.BOT_TOKEN);
		builder.addEventListener(new Quest14());
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
			if (teamHasGottenAllCodes(teamName)) {
				channel.sendMessage(
						"You have already used all possible codes!"
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
			if (args.length < 3) {
				channel.sendMessage("You have to provide a team name. Try `!submit <code> <team name>`").queue();
				return;
			}
			if (usersCode == null) { // IF INVALID
				if (COOLDOWN_TYPE == CooldownOn.INVALID || COOLDOWN_TYPE == CooldownOn.BOTH) {
					applyCooldownNow(teamName);
				}
				channel.sendMessage("Sorry, that's not a valid code!").queue();
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
					channel.sendMessage("You have already used this code!").queue();
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
				
				final String teamToIncrement = args[2];
				if (!Arrays.asList(HHG.TEAM_NAMES).contains(teamToIncrement.toLowerCase())) {
					channel.sendMessage("That's not a valid team name.").queue();
					return;
				}
				
				usersCode.teamUsage.replace(teamName, true);
				int codeCount = howManyCodesATeamFound(teamName);
				channel.sendMessage("Your hand has been successfully dealt.").queue();
				HHGGuild.getTextChannelById(HHG.QUEST_LOG_CHANNEL_ID).sendMessage(String.format(
						"`[%s] [%s] %s (%s) submitted %s (%s) %s`",
						new Date().toString(),
						QUEST_NAME,
						teamName,
						HHGGuild.getMember(message.getAuthor()).getNickname(),
						userSubmission,
						"DEALT",
						""
				)).queue();
				
				if (COOLDOWN_TYPE == CooldownOn.VALID || COOLDOWN_TYPE == CooldownOn.BOTH) {
					applyCooldownNow(teamName);
				}
				if (!teamName.equals(teamToIncrement)) {
					HHGGuild.getTextChannelsByName(args[2],true).get(0).sendMessage(teamName + " dealt you " + usersCode.value + " points.").queue();
				} else {
					HHGGuild.getTextChannelsByName(args[2],true).get(0).sendMessage("You've dealt yourself " + usersCode.value + " points.").queue();
				}
				teamHandValue.replace(teamToIncrement, teamHandValue.get(teamToIncrement) + usersCode.value);
				if (teamHandValue.get(teamToIncrement) > 11) {
					teamHandValue.replace(teamToIncrement, 0);
					if (teamName.equals(teamToIncrement)) {
						HHGGuild.getTextChannelsByName(args[2],true).get(0).sendMessage("Oh no! You've busted yourself. You've gone back to a score of 0.").queue();
					} else {
						HHGGuild.getTextChannelsByName(args[2],true).get(0).sendMessage("Oh no! You've been busted by " + channel.getName() + ". You've gone back to a score of 0.").queue();
					}
				} else {
					
					HHGGuild.getTextChannelsByName(args[2],true).get(0).sendMessage("You're at a current score of " + teamHandValue.get(teamToIncrement) + ".").queue();
				}
				System.out.println(teamHandValue.toString());
			}
			updateFiles();
		} else { // not in team category
			channel.sendMessage(
					"Send this in your team channel!"
			).queue();
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
