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
