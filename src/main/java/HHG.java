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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

class HHG {
	static final String HHG_SERVER_ID = "640905392909713439";
	static final String LOG_CHANNEL_ID = "641988345336954910";
	static final String HELP_CHANNEL_ID = "643120931341860865";
	static final String CONTESTANT_ROLE_ID = "641989978141884427";
	static final String ADMIN_ROLE_ID = "640906714442366977";
	static final String CHANNEL_NICKNAMES_FILE = "channel-nicknames.db";
	static final String TEAM_CHANNEL_CATEGORY = "641988219256307715";
	static final String IMAGE_VERIFICATION_CHANNEL = "671886377700294697";
	static final String BOT_COM_CHANNEL_ID = "675101263238660129";
	static final String LEADERBOARD_CHANNEL_ID = "641988335232876554";
	static final String LEADERBOARD_MESSAGE_ID = "676214272346423316";
	static final String QUEST_LOG_CHANNEL_ID = "676608204683214860";
	
	static final String BOT_TOKEN = "";
	
	static final File GUILD_MEMBER_IDS_FILE = new File("guild-members-ids.txt");
	static final File LOG_FILE = new File("log.txt");
	static final File NAMES_FILE = new File("master-name-list.txt");
	static final File EMAILS_FILE = new File("master-email-list.txt");
	static final File INVITE_CODES_FILE = new File("invite-codes.txt");
	static final File SCORES_FILE = new File("scores.json");
	
	static final String USER_AGENT = "HHG";
	
	static final String CHANGE_PRELIMINARY_SCORE_GET = "https://www.haslett.pw/change_preliminary_points.php";
	static final String GET_PRELIMINARY_SCORE_GET = "https://www.haslett.pw/get_preliminary_points.php";
	
	static final String[] TEAM_NAMES = new String[] {"example-team","alpha","3musketeers", "aggressively-average", "albino-africa", "beta", "buses", "dads", "delta", "dutch-dynasty", "epsilon", "example-team", "french-flies", "gamma", "hotpink-chi-unicorns", "iota", "kappa", "krusty-krab", "lambda", "moms", "omega", "omniscient", "rho", "rona-time", "sendgods", "sigma", "underdogs", "zeta"};
	
	
	static String sendGET(String GET_URL) throws IOException {
		URL obj = new URL(GET_URL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		StringBuilder response = new StringBuilder();
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		}
		return response.toString();
	}
	
	static int getTeamPoints(@SuppressWarnings("SameParameterValue") String teamName) {
		Scanner in = null;
		try {
			in = new Scanner(SCORES_FILE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder builder = new StringBuilder();
		assert in != null;
		while (in.hasNextLine()) builder.append(in.nextLine());
		
		JSONObject jsonObject = new JSONObject(builder.toString());
		JSONArray teamArray = ((JSONArray) jsonObject.get("teams"));
		for (int i = 0; i < teamArray.length(); i++) {
			if (((JSONObject) teamArray.get(i)).get("name").equals(teamName)) {
				return (int) ((JSONObject) teamArray.get(i)).get("score");
			}
		}
		return 0;
	}
	
	
}
