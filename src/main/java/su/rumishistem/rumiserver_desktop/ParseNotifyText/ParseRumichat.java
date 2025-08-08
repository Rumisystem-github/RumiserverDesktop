package su.rumishistem.rumiserver_desktop.ParseNotifyText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import su.rumishistem.rumi_java_lib.FETCH;
import su.rumishistem.rumi_java_lib.FETCH_RESULT;
import su.rumishistem.rumiserver_desktop.Main;

public class ParseRumichat {
	public static String parse(String text) {
		text = room_id(text);
		text = group_id(text);

		return text;
	}
	
	private static String room_id(String text) {
		try {
			Matcher room_matcher = Pattern.compile("<R:([^>]+)>").matcher(text);
			while (room_matcher.find()) {
				String room_id = room_matcher.group(1);
				FETCH ajax = new FETCH("https://chat.rumiserver.com/api/Room?ID=" + room_id);
				ajax.SetHEADER("TOKEN", Main.ConfigData.get("TOKEN").asText());
				FETCH_RESULT reslt = ajax.GET();
				JsonNode body = new ObjectMapper().readTree(reslt.GetString());

				if (body.get("STATUS").asBoolean()) {
					text = text.replace(room_matcher.group(0), body.get("ROOM").get("NAME").asText());
				}
			}
		} catch (Exception EX) {
			EX.printStackTrace();
		}

		return text;
	}

	private static String group_id(String text) {
		try {
			Matcher room_matcher = Pattern.compile("<G:([^>]+)>").matcher(text);
			while (room_matcher.find()) {
				String room_id = room_matcher.group(1);
				FETCH ajax = new FETCH("https://chat.rumiserver.com/api/Group?ID=" + room_id);
				ajax.SetHEADER("TOKEN", Main.ConfigData.get("TOKEN").asText());
				FETCH_RESULT reslt = ajax.GET();
				JsonNode body = new ObjectMapper().readTree(reslt.GetString());

				if (body.get("STATUS").asBoolean()) {
					text = text.replace(room_matcher.group(0), body.get("GROUP").get("NAME").asText());
				}
			}
		} catch (Exception EX) {
			EX.printStackTrace();
		}

		return text;
	}
}
