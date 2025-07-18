package su.rumishistem.rumiserver_desktop;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

public class BackgroundService {
	private enum NotifyAction {
		Close,
		Click
	};

	public BackgroundService() {
		//メニュー
		PopupMenu TrayMenu = new PopupMenu();

		//メニューの終了ボタン
		MenuItem ExitItem = new MenuItem("終了");
		ExitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		TrayMenu.add(ExitItem);

		//タスクトレイに常駐するやつ
		TrayIcon TI = new TrayIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "るみさーばーデスクトップ", TrayMenu);
		TI.setImageAutoSize(true);

		//通知をクリックした時の動作
		TI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "通知をクリックしました");
			}
		});

		try {
			SystemTray Tray = SystemTray.getSystemTray();
			Tray.add(TI);

			SendNotify("るみさーばーデスクトップ", "常駐しました");
		} catch (AWTException EX) {
			EX.printStackTrace();
			JOptionPane.showMessageDialog(null, "タスクトレイに常駐失敗", "エラー", JOptionPane.ERROR_MESSAGE);
		} catch (Exception EX) {
			EX.printStackTrace();
		}
	}

	private NotifyAction SendNotify(String Title, String Text) throws IOException {
		ProcessBuilder PB = new ProcessBuilder("dunstify", "--appname=るみさーばーデスクトップ", "-A", "default,default,\"click\"", Title, Text);
		Process P = PB.start();

		InputStream IS = P.getInputStream();
		BufferedReader BR = new BufferedReader(new InputStreamReader(IS));

		String Line;
		while ((Line = BR.readLine()) != null) {
			if (Line.contains("default")) {
				return NotifyAction.Click;
			} else {
				return NotifyAction.Close;
			}
		}

		return NotifyAction.Close;
	}
}
