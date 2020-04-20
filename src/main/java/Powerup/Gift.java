package Powerup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Gift {
	String buyingTeam;
	String receivingTeam;
	int value;
	LocalDate date;
	
	static File giftDBFile = new File("gifts.db");
	
	public Gift(String buyingTeam, String receivingTeam, int value, LocalDate date) {
		this.buyingTeam = buyingTeam;
		this.receivingTeam = receivingTeam;
		this.value = value;
		this.date = date;
	}
	
	public static void registerGift(Gift gift) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(giftDBFile, true));
		writer.write(giftToDBLine(gift));
		writer.newLine();
		writer.close();
	}
	
	public static String giftToDBLine(Gift gift) {
		return String.format("%s;%s;%d;%s",
				gift.buyingTeam,
				gift.receivingTeam,
				gift.value,
				gift.date.toString());
	}
	
	public static Gift parseGift(String line) {
		String[] parts = line.split(";");
		if (parts.length != 4) {
			throw new IllegalArgumentException("Failed to parse gift entry (incorrect arguments).");
		}
		return new Gift(parts[0], parts[1], Integer.parseInt(parts[2]), LocalDate.parse(parts[3]));
	}
	
	public static List<Gift> allGifts() {
		try {
			Scanner inFile = new Scanner(giftDBFile);
			List<Gift> gifts = new ArrayList<>();
			while (inFile.hasNextLine()) {
				String line = inFile.nextLine();
				if (!line.trim().isEmpty()) {
					gifts.add(parseGift(line));
				}
			}
			return gifts;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean teamHasGiftedTeamToday(String buyingTeam, String receivingTeam) {
		if (allGifts() == null)
			return false;
		//noinspection ConstantConditions
		for (Gift gift : allGifts()) {
			if (gift.buyingTeam.equals(buyingTeam) && gift.receivingTeam.equals(receivingTeam)
					&& gift.date.getDayOfYear() == LocalDate.now().getDayOfYear()) {
				return true;
			}
		}
		return false;
	}
}
