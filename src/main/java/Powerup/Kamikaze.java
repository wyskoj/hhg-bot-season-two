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

package Powerup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Kamikaze {
	String attackingTeam;
	String opposingTeam;
	LocalDateTime timeOfAttack;
	
	static File kamikazeDB = new File("kamikazes.db");
	
	public Kamikaze(String attackingTeam, String opposingTeam, LocalDateTime timeOfAttack) {
		this.attackingTeam = attackingTeam;
		this.opposingTeam = opposingTeam;
		this.timeOfAttack = timeOfAttack;
	}
	
	static String kamikazeToDBLine(Kamikaze kamikaze) {
		return String.format("%s;%s;%s",
				kamikaze.attackingTeam,
				kamikaze.opposingTeam,
				kamikaze.timeOfAttack.toString());
	}
	
	public static void registerNewKamikaze(Kamikaze kamikaze) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(kamikazeDB, true));
		writer.write(kamikazeToDBLine(kamikaze));
		writer.newLine();
		writer.close();
	}
	
	public static Kamikaze parseKamikaze(String logEntry) {
		// attackingteam;opposingteam;datetimestring
		String[] parts = logEntry.split(";");
		if (parts.length != 3) {
			throw new IllegalArgumentException("Failed to parse kamikaze entry (incorrect arguments).");
		}
		return new Kamikaze(parts[0], parts[1], LocalDateTime.parse(parts[2]));
	}
	
	public static List<Kamikaze> allKamikazeAttacks() {
		try {
			Scanner inFile = new Scanner(kamikazeDB);
			List<Kamikaze> kamikazes = new ArrayList<>();
			while (inFile.hasNextLine()) {
				String line = inFile.nextLine();
				if (!line.trim().isEmpty()) {
					kamikazes.add(parseKamikaze(line));
				}
			}
			return kamikazes;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean teamHasAttackedTeamToday(String attackingTeam, String opposingTeam) {
		List<Kamikaze> kamikazes = allKamikazeAttacks();
		if (kamikazes == null)
			return false;
		for (Kamikaze kamikaze : kamikazes) {
			if (kamikaze.attackingTeam.equals(attackingTeam) && kamikaze.opposingTeam.equals(opposingTeam)
			&& kamikaze.timeOfAttack.getDayOfYear() == LocalDateTime.now().getDayOfYear()) {
				return true;
			}
		}
		return false;
	}
}
