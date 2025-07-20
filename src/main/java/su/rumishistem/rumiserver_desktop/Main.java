package su.rumishistem.rumiserver_desktop;

import java.awt.SystemTray;
import java.io.*;
import javax.swing.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import su.rumishistem.rumi_java_lib.RESOURCE.RESOURCE_MANAGER;
import su.rumishistem.rumiserver_desktop.Form.SetupForm;

public class Main {
	public static File ConfigFile = null;
	public static String NotifycationProgramPath = null;
	public static JsonNode ConfigData = null;

	public static void main(String[] args) throws IOException {
		//システムトレイが使えるかチェックする
		if (!SystemTray.isSupported()) {
			JOptionPane.showMessageDialog(null, "システムトレイはサポートされていません。", "エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		//OSごとに処理を別ける
		String OSName = System.getProperty("os.name").toUpperCase();
		if (OSName.contains("NUX") || OSName.contains("NIX") || OSName.contains("AIX")) {
			//Linux
			ConfigFile = new File(System.getProperty("user.home") + "/.config/RumiServer/Config.json");

			//通知を発行するプログラムを置く
			NotifycationProgramPath = System.getProperty("user.home") + "/.local/bin/rumiserver_notifycation";
			File NotifycationProgramFile = new File(NotifycationProgramPath);
			if (NotifycationProgramFile.exists() == false) {
				NotifycationProgramFile.createNewFile();
				NotifycationProgramFile.setExecutable(true);

				FileOutputStream FOS = new FileOutputStream(NotifycationProgramFile);
				FOS.write(new RESOURCE_MANAGER(Main.class).getResourceData("/notifysender_linux"));
				FOS.flush();
				FOS.close();
			}
		} else {
			JOptionPane.showMessageDialog(null, "サポートされていないOSです。", "エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		//設定ファイルを色々
		if (!ConfigFile.exists()) {
			//セットアップを開く
			new SetupForm();
			return;
		} else {
			ConfigData = new ObjectMapper().readTree(ConfigFile);
		}

		new BackgroundService();
	}
}
