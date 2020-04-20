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
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import javax.xml.soap.Text;
import java.util.List;

public class MessageCount extends ListenerAdapter {
	
	static net.dv8tion.jda.core.JDA JDA;
	
	
	public static void main(String[] args) throws LoginException {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(HHG.BOT_TOKEN);
		builder.addEventListener(new MessageCount());
		/* Defined on start */
		JDA = builder.buildAsync();
	}
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String content = event.getMessage().getContentRaw().trim().toLowerCase();
		TextChannel channel = event.getChannel();
		if (content.equals("!messagecount")) {
			if (!JDA.getGuildById(HHG.HHG_SERVER_ID).getMember(event.getAuthor()).getRoles().contains(JDA.getRoleById(HHG.ADMIN_ROLE_ID))) {
				return;
			}
			Message ffff = channel.sendMessage("Getting counts...").complete();
			StringBuilder builder = new StringBuilder();
			builder.append("```\n");
			List<TextChannel> channelList = JDA.getCategoryById(HHG.TEAM_CHANNEL_CATEGORY).getTextChannels();
			for (TextChannel channel1 : channelList) {
				int count = 0;
				for (Message ignored : channel1.getIterableHistory()) {
					count++;
				}
				builder.append(String.format("%20s %d%n", channel1.getName(), count));
				System.out.println(channel1.getName());
			}
			builder.append("```");
			ffff.editMessage(builder.toString()).complete();
		}
	}

	
	
}
