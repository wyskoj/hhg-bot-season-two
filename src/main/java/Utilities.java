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
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class file is for implementing the code for utility commands. These include
 * !send, !help, !commands, etc.
 */
public class Utilities extends ListenerAdapter {
	
	/* Declare ID and other final values */

	
	private static GuildController HHGController;
	private static Guild HHGGuild;
	
	private static HashMap<String, String> channelIDNicknames = new HashMap<>();
	
	public Utilities() {
	}
	
	public static void main(String[] args) throws LoginException {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(HHG.BOT_TOKEN);
		builder.addEventListener(new Utilities());
		/* Defined on start */
		builder.buildAsync();
		
		try {
			FileInputStream fileIn = new FileInputStream(HHG.CHANNEL_NICKNAMES_FILE);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			channelIDNicknames = (HashMap<String, String>) in.readObject();
			in.close();
			fileIn.close();
		} catch (Exception i) {
			i.printStackTrace();
		}
	}
	
	@Override
	public void onReady(ReadyEvent event) {
		HHGGuild = event.getJDA().getGuildById(HHG.HHG_SERVER_ID);
	}
	
	private boolean isMemberAdmin(Member member) {
		return member.getRoles().contains(HHGGuild.getRoleById(HHG.ADMIN_ROLE_ID));
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getAuthor().isBot())
			return;
		String message = event.getMessage().getContentRaw();
		Matcher commandMatcher = Pattern.compile("^!(\\w+)\\s(.+)$").matcher(message.trim());
		boolean found = commandMatcher.find();
		if (event.getChannel().getId().equals("671178922578280463")){
			event.getChannel().sendMessage("**Success!** You earned ***3*** points.").queue();
			return;
		}
		if (found) {
			String type = commandMatcher.group(1), arguments = commandMatcher.group(2);
			if ("send".equals(type)) {
				if (!event.getGuild().equals(HHGGuild) || isMemberAdmin(event.getMember())) {
					String[] argumentsSplit = arguments.split(" ");
					if (argumentsSplit.length == 2) {
						String sendMessageID = argumentsSplit[0], sendChannelID = argumentsSplit[1];
						TextChannel channelToSendIn;
						if (channelIDNicknames.get(sendChannelID) != null) {
							channelToSendIn = event.getGuild().getTextChannelById(channelIDNicknames.get(sendChannelID));
						} else {
							channelToSendIn = event.getGuild().getTextChannelById(sendChannelID);
						}
						if (channelToSendIn == null) {
							event.getChannel().sendMessage("Invalid channel ID.").queue();
							return;
						}
						Message messageToSend = null;
						for (TextChannel channel : event.getGuild().getTextChannels())
							try {
								messageToSend = channel.getMessageById(sendMessageID).complete();
							} catch (Exception ignored) {}
						if (messageToSend == null) {
							event.getChannel().sendMessage("Invalid message ID.").queue();
							return;
						}
						channelToSendIn.sendMessage(messageToSend.getContentRaw()).queue();
					}
				}
			} else if ("sendadd".equals(type)) {
				
				if (event.getGuild().equals(HHGGuild) && !isMemberAdmin(event.getMember())) {
					return;
				}
				
				String[] argumentsSplit = arguments.split(" ");
				
				if (argumentsSplit.length == 2) {
					String channelID = argumentsSplit[0], nickname = argumentsSplit[1];
					Channel channel = event.getGuild().getTextChannelById(channelID);
					if (channel == null) {
						event.getChannel().sendMessage("`" + channelID + "` was not recognized as a valid channel ID.").queue();
					} else {
						channelIDNicknames.put(nickname, channelID);
						try {
							FileOutputStream fileOut =
									new FileOutputStream(HHG.CHANNEL_NICKNAMES_FILE);
							ObjectOutputStream out = new ObjectOutputStream(fileOut);
							out.writeObject(channelIDNicknames);
							out.close();
							fileOut.close();
						} catch (IOException i) {
							i.printStackTrace();
						}
					}
				}
			} else if (type.equals("image")) {
				// do nothing
			} else {
//				 event.getChannel().sendMessage("`" + type + "` not recognized.").queue();
			}
		}
	}
	
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		Member leavingMember = event.getMember();
		TextChannel logChannel = HHGGuild.getTextChannelById(HHG.LOG_CHANNEL_ID);
		logChannel.sendMessage("`" + leavingMember.getUser().getName() + "` has left the server.").queue();
	}
}
