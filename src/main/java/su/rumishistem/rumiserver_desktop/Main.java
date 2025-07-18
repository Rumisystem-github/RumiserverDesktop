package su.rumishistem.rumiserver_desktop;

import java.awt.SystemTray;
import java.io.*;
import javax.swing.*;

import su.rumishistem.rumiserver_desktop.Form.SetupForm;
import su.rumishistem.rumiserver_desktop.Type.OSType;

public class Main {
	public static File ConfigFile = null;
	public static OSType OS = OSType.None;

	public static void main(String[] args) throws IOException {
		//システムトレイが使えるかチェックする
		if (!SystemTray.isSupported()) {
			JOptionPane.showMessageDialog(null, "システムトレイはサポートされていません。", "エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		//OSごとに処理を別ける
		String OSName = System.getProperty("os.name").toUpperCase();
		if (OSName.contains("WIN")) {
			//Windows
			OS = OSType.Windows;
			ConfigFile = new File(System.getenv("APPDATA") + "\\RumiServer\\Config.json");
		} else if (OSName.contains("NUX") || OSName.contains("NIX") || OSName.contains("AIX")) {
			//Linux
			OS = OSType.Linux;
			ConfigFile = new File(System.getProperty("user.home") + "/.config/RumiServer/Config.json");
		} else {
			JOptionPane.showMessageDialog(null, "サポートされていないOSです。", "エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		//設定ファイルを色々
		if (!ConfigFile.exists()) {
			//セットアップを開く
			new SetupForm();
			return;
		}

		new BackgroundService();
	}
}
