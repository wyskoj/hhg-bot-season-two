package Powerup;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Scanner;

public class Vault {
	
	/**
	 * Checks to see if a team has an active vault.
	 *
	 * @param teamName the team to check
	 * @return true if team has active vault, false otherwise
	 */
	public static boolean teamHasActiveVault(String teamName) {
		return vaultFileFromTeamName(teamName).exists();
	}
	
	/**
	 * Returns the associated vault File with the team, even if the file does not exist.
	 *
	 * @param teamName the team to get vault File
	 * @return this team's vault File
	 */
	public static File vaultFileFromTeamName(String teamName) {
		return new File("vaults/" + teamName + ".json");
	}
	
	/**
	 * Returns a <code>LocalDate</code> representing the date to return this team's vault on. Returns null if team does
	 * not have active vault.
	 *
	 * @param teamName the team to check
	 * @return a <code>LocalDate</code> representing the date to return this team's vault on
	 */
	public static LocalDate dateToReturnVault(String teamName) throws FileNotFoundException {
		if (!teamHasActiveVault(teamName)) return null;
		Scanner in = new Scanner(vaultFileFromTeamName(teamName));
		
		StringBuilder b = new StringBuilder();
		while (in.hasNextLine()) b.append(in.nextLine());
		
		JSONObject jsonObject = new JSONObject(b.toString());
		int dateOfYear = (int) jsonObject.get("date");
		return Year.of(2020).atDay(dateOfYear);
	}
	
	/**
	 * Returns the day of the week the vault is to be returned on. Returns null if team does not have active vault or
	 * error.
	 *
	 * @param teamName the team to check
	 * @return "Monday", "Tuesday", "Wednesday", "Thursday", or null if no active vault
	 */
	public static String dayOfWeekToReturnVault(String teamName) {
		if (!teamHasActiveVault(teamName)) return null;
		try {
			LocalDate date = dateToReturnVault(teamName);
			assert date != null;
			return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean registerVault(String teamName, int priceOfVault) {
		try {
			JSONObject jsonObject = new JSONObject();
			
			jsonObject.put("date", LocalDate.now().plus(Period.ofDays(7)).getDayOfYear());
			jsonObject.put("value", priceOfVault);
			
			
			FileWriter writer = new FileWriter(vaultFileFromTeamName(teamName));
			writer.write(jsonObject.toString());
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
