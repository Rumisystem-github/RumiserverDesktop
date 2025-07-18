package su.rumishistem.rumiserver_desktop;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

public class Main {
	public static void main(String[] args) {
		if (!SystemTray.isSupported()) {
			JOptionPane.showMessageDialog(null, "システムトレイはサポートされていません。", "エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

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

			TI.displayMessage("るみさーばーデスクトップ", "常駐しました", TrayIcon.MessageType.INFO);
		} catch (AWTException EX) {
			EX.printStackTrace();
			JOptionPane.showMessageDialog(null, "タスクトレイに常駐失敗", "エラー", JOptionPane.ERROR_MESSAGE);
		}
	}
}
