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
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ScoreListener extends ListenerAdapter {
	static net.dv8tion.jda.core.JDA JDA;
	
	static FileWriter writer;
	
	static Guild guild;
	static TextChannel botComChannel;
	static TextChannel leaderboardChannel;
	
	public static void main(String[] args) throws LoginException {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(HHG.BOT_TOKEN);
		builder.addEventListener(new ScoreListener());
		/* Defined on start */
		JDA = builder.buildAsync();
		
	}
	
	public void onReady(ReadyEvent event) {
		guild = JDA.getGuildById(HHG.HHG_SERVER_ID);
		botComChannel = guild.getTextChannelById(HHG.BOT_COM_CHANNEL_ID);
		System.out.println(HHG.getTeamPoints("example-team"));
		leaderboardChannel = guild.getTextChannelById(HHG.LEADERBOARD_CHANNEL_ID);
		updateScoreboard();
	}
	
	/**
	 * ONLY TO BE USED IN LeaderBoardListener
	 */
	private void changePoints(String teamName, int value) {
		try {
			Scanner in = new Scanner(HHG.SCORES_FILE);
			StringBuilder builder = new StringBuilder();
			while (in.hasNextLine()) builder.append(in.nextLine());
			JSONObject jsonObject = new JSONObject(builder.toString());
			JSONArray teamArray = ((JSONArray) jsonObject.get("teams"));
			for (int i = 0; i < teamArray.length(); i++) {
				JSONObject team = (JSONObject) teamArray.get(i);
				int before;
				if (team.get("name").equals(teamName)) {
					before = (int) team.get("score");
					teamArray.remove(i);
					JSONObject newTeam = new JSONObject();
					newTeam.put("name", teamName);
					newTeam.put("score", before + value);
					teamArray.put(newTeam);
					writer = new FileWriter(HHG.SCORES_FILE);
					writer.write(jsonObject.toString());
					writer.close();
					return;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void editLeaderBoardMessage(String messageContents) {
		leaderboardChannel.getMessageById(HHG.LEADERBOARD_MESSAGE_ID).complete().editMessage(messageContents).complete();
	}
	
	public static boolean isTeamInLastPlace(String teamName) {
		
		HashMap<String, Integer> hashMap = new HashMap<>();
		for (String team : HHG.TEAM_NAMES) {
			hashMap.put(team, HHG.getTeamPoints(team));
		}
		Map<String, Integer> sorted = sortByComparator(hashMap);
		Iterator iterator = sorted.entrySet().iterator();
		
		List<String> teamNames = new ArrayList<>();
		List<Integer> teamScores = new ArrayList<>();
		
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			teamNames.add((String) pair.getKey());
			teamScores.add((Integer) pair.getValue());
			iterator.remove();
		}
		
		int lowestScore = Integer.MAX_VALUE;
		for (int i = 0; i < teamNames.size(); i++) {
			if (teamScores.get(i) < lowestScore) lowestScore = teamScores.get(i);
		}
		return hashMap.get(teamName) == lowestScore;

	}
	
	
	@SuppressWarnings("rawtypes")
	private String scoresToBeautyString() {
		StringBuilder builder = new StringBuilder();
		HashMap<String, Integer> hashMap = new HashMap<>();
		for (String team : HHG.TEAM_NAMES) {
			hashMap.put(team, HHG.getTeamPoints(team));
		}
		Map<String, Integer> sorted = sortByComparator(hashMap);
		Iterator iterator = sorted.entrySet().iterator();
		
		
		builder.append("**THE HASLETT HIGH GUILD LEADERBOARD**\n");
		
		
		List<String> teamNames = new ArrayList<>();
		List<Integer> teamScores = new ArrayList<>();
		
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			teamNames.add((String) pair.getKey());
			teamScores.add((Integer) pair.getValue());
			iterator.remove();
		}
		
		List<Integer> rankings = new ArrayList<>();
		builder.append("```");
		builder.append("Rank | Team Name             | Score\n");
		builder.append("-----+-----------------------+------\n");
		for (int i = 1; i <= teamNames.size(); i++) {
			if (i == 1) {
				rankings.add(i);
				addToLeaderBoardRow(builder, teamNames, teamScores, rankings, i);
				continue;
			}
			if (teamScores.get(i - 1).equals(teamScores.get(i - 2))) {
				rankings.add(rankings.get(i - 2));
			} else {
				rankings.add(i);
			}
			addToLeaderBoardRow(builder, teamNames, teamScores, rankings, i);
		}
		
		builder.append("```");
		
		
		return builder.toString();
	}
	
	private void addToLeaderBoardRow(StringBuilder builder, List<String> teamNames, List<Integer> teamScores, List<Integer> rankings, int i) {
		builder.append(
				String.format(
						"%-5s| %-20s  | %3d",
						rankings.get(i - 1) + ".",
						teamNames.get(i - 1),
						teamScores.get(i - 1)
				)
		);
		builder.append("\n");
	}
	
	private void updateScoreboard() {
		editLeaderBoardMessage(scoresToBeautyString());
	}
	
	private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {
		
		List<Map.Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());
		
		// Sorting the list based on values
		list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
		
		// Maintaining insertion order with the help of LinkedList
		Map<String, Integer> sortedMap = new LinkedHashMap<>();
		for (Map.Entry<String, Integer> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		
		return sortedMap;
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (!event.getChannel().equals(botComChannel)) return;
		Message message = event.getMessage();
		String messageContents = message.getContentDisplay();
		/*
		messages should be sent in this format:
		[add,remove];[team name];[value]
		for example:
		add;zeta;3
		will add 3 points to zeta
		 */
		String[] args = messageContents.split(";");
		if (args.length != 3) return;
		
		String affectedTeam = args[1];
		int value = Integer.parseInt(args[2]);
		switch (args[0]) {
			case "add":
				changePoints(affectedTeam, value);
				break;
			case "remove":
				changePoints(affectedTeam, -value);
				break;
		}
		
		updateScoreboard();
		
		
	}
}
