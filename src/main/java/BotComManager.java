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
