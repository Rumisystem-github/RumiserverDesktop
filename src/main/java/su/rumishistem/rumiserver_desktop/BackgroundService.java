package su.rumishistem.rumiserver_desktop;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import su.rumishistem.rumi_java_lib.FETCH;
import su.rumishistem.rumi_java_lib.FETCH_RESULT;
import su.rumishistem.rumi_java_lib.WebSocket.Client.WebSocketClient;
import su.rumishistem.rumi_java_lib.WebSocket.Client.EVENT.CLOSE_EVENT;
import su.rumishistem.rumi_java_lib.WebSocket.Client.EVENT.CONNECT_EVENT;
import su.rumishistem.rumi_java_lib.WebSocket.Client.EVENT.MESSAGE_EVENT;
import su.rumishistem.rumi_java_lib.WebSocket.Client.EVENT.WS_EVENT_LISTENER;

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

		try {
			SystemTray Tray = SystemTray.getSystemTray();
			Tray.add(TI);

			new Thread(new Runnable() {
				@Override
				public void run() {
					String HandShakeID = "hs";

					WebSocketClient WS = new WebSocketClient();
					ScheduledExecutorService[] Scheduler = {null};
					boolean[] HandShakeOK = {false};
					int[] HeartBeat = {0};

					WS.SET_EVENT_LISTENER(new WS_EVENT_LISTENER() {
						@Override
						public void MESSAGE(MESSAGE_EVENT e) {
							try {
								JsonNode message = new ObjectMapper().readTree(e.getMessage());
								System.out.println(message);

								if (HandShakeOK[0]) {
									switch (message.get("TYPE").asText()) {
										case "NOTIFY":{
											String Service = message.get("DATA").get("SERVICE").asText();
											String Title = message.get("DATA").get("TITLE").asText();
											String Text = message.get("DATA").get("TEXT").asText();

											switch (message.get("DATA").get("SERVICE").asText()) {
												case "ILANES":{
													Service = "いらねす";
													break;
												}

												case "RUMICHAT": {
													Service = "るみチャット";

													//部屋IDがあればそれを部屋名に変換する
													Matcher room_matcher = Pattern.compile("<R:([^>]+)>").matcher(Title);
													while (room_matcher.find()) {
														String room_id = room_matcher.group(1);
														FETCH ajax = new FETCH("https://chat.rumiserver.com/api/Room?ID=" + room_id);
														ajax.SetHEADER("TOKEN", Main.ConfigData.get("TOKEN").asText());
														FETCH_RESULT reslt = ajax.GET();
														JsonNode body = new ObjectMapper().readTree(reslt.GetString());

														if (body.get("STATUS").asBoolean()) {
															Title = Title.replace(room_matcher.group(0), body.get("ROOM").get("NAME").asText());
														}
													}
													break;
												}
											}

											SendNotify(Service, Title, Text);
											return;
										}
									}
								} else {
									if (message.get("REQUEST").asText().equals(HandShakeID)) {
										if (!message.get("STATUS").asBoolean()) {
											SendNotify("るみさーばーデスクトップ", "るみさーばーデスクトップ", "常駐できませんでした。");
											System.exit(1);
										}

										HandShakeOK[0] = true;
										HeartBeat[0] = message.get("HEARTBEAT").asInt();

										//通知発行
										new Thread(new Runnable() {
											@Override
											public void run() {
												try {
													SendNotify("るみさーばーデスクトップ", "るみさーばーデスクトップ", "常駐しました");
												} catch (Exception EX) {
													EX.printStackTrace();
													System.exit(1);
												}
											}
										}).start();

										Scheduler[0] = Executors.newSingleThreadScheduledExecutor();
										Scheduler[0].scheduleAtFixedRate(new Runnable() {
											@Override
											public void run() {
											}
										}, 0, HeartBeat[0], TimeUnit.MILLISECONDS);
									}
								}
							} catch (Exception EX) {
								EX.printStackTrace();
							}
						}

						@Override
						public void CONNECT(CONNECT_EVENT e) {
							try {
								e.SEND("[\""+HandShakeID+"\", \"HELO\", \""+Main.ConfigData.get("TOKEN").asText()+"\"]");
							} catch (Exception EX) {
								EX.printStackTrace();
								System.exit(1);
							}
						}

						@Override
						public void CLOSE(CLOSE_EVENT e) {
							HandShakeOK[0] = false;
							Scheduler[0].close();
						}

						@Override
						public void EXCEPTION(Exception e) {
						}
					});

					WS.CONNECT("wss://account.rumiserver.com/api/ws");
				}
			}).start();
		} catch (AWTException EX) {
			EX.printStackTrace();
			JOptionPane.showMessageDialog(null, "タスクトレイに常駐失敗", "エラー", JOptionPane.ERROR_MESSAGE);
		} catch (Exception EX) {
			EX.printStackTrace();
		}
	}

	private NotifyAction SendNotify(String Service, String Title, String Text) throws IOException {
		ProcessBuilder PB = new ProcessBuilder(Main.NotifycationProgramPath, Service, Title, Text);
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
