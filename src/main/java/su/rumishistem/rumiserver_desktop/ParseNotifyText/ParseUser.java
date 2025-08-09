package su.rumishistem.rumiserver_desktop.ParseNotifyText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import su.rumishistem.rumi_java_lib.FETCH;
import su.rumishistem.rumi_java_lib.FETCH_RESULT;
import su.rumishistem.rumiserver_desktop.Main;

public class ParseUser {
	public static String parse(String text) {
		text = Mention(text);

		return text;
	}
	
	private static String Mention(String text) {
		try {
			Matcher room_matcher = Pattern.compile("<@([^>]+)>").matcher(text);
			while (room_matcher.find()) {
				String room_id = room_matcher.group(1);
				FETCH ajax = new FETCH("https://account.rumiserver.com/api/User?ID=" + room_id);
				ajax.SetHEADER("TOKEN", Main.ConfigData.get("TOKEN").asText());
				FETCH_RESULT reslt = ajax.GET();
				JsonNode body = new ObjectMapper().readTree(reslt.GetString());

				if (body.get("STATUS").asBoolean()) {
					text = text.replace(room_matcher.group(0), body.get("ACCOUNT").get("NAME").asText());
				}
			}
		} catch (Exception EX) {
			EX.printStackTrace();
		}

		return text;
	}
}
