import java.io.Serializable;
import java.util.HashMap;

public class ValidCode implements Serializable {
	private static final long serialVersionUID = 1L;
	String code;
	int value;
	
	HashMap<String, Boolean> teamUsage = new HashMap<>();

	public ValidCode(String code, int value) {
		this.code = code;
		this.value = value;
		
		for (String teamName : HHG.TEAM_NAMES) {
			teamUsage.put(teamName, false);
		}
	}
}
