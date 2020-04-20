package Powerup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Shield {
	
	String teamName;
	LocalDate date;
	
	static File shieldDBFile = new File("shields.db");
	
	public Shield(String teamName, LocalDate date) {
		this.teamName = teamName;
		this.date = date;
	}
	
	public static boolean teamHasActiveShield(String teamName) {
		if (allShields() == null)
			return false;
		//noinspection ConstantConditions
		for (Shield shield : allShields()) {
			if (LocalDate.now().equals(shield.date) && teamName.equals(shield.teamName)) {
				return true;
			}
		}
		return false;
	}
	
	static String shieldToDBLine(Shield shield) {
		return String.format("%s;%s",
				shield.teamName,
				shield.date.toString());
	}
	
	public static void registerShield(Shield shield) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(shieldDBFile, true));
		writer.write(shieldToDBLine(shield));
		writer.newLine();
		writer.close();
	}
	
	public static Shield parseShield(String logEntry) {
		// attackingteam;opposingteam;datetimestring
		String[] parts = logEntry.split(";");
		if (parts.length != 2) {
			throw new IllegalArgumentException("Failed to parse shield entry (incorrect arguments).");
		}
		return new Shield(parts[0], LocalDate.parse(parts[1]));
	}
	
	public static List<Shield> allShields() {
		try {
			Scanner inFile = new Scanner(shieldDBFile);
			List<Shield> shields = new ArrayList<>();
			while (inFile.hasNextLine()) {
				String line = inFile.nextLine();
				if (!line.trim().isEmpty()) {
					shields.add(parseShield(line));
				}
			}
			return shields;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
