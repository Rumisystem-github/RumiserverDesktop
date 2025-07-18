package su.rumishistem.rumiserver_desktop.Form;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.JOptionPane;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import su.rumishistem.rumi_java_lib.EXCEPTION_READER;
import su.rumishistem.rumi_java_lib.FETCH;
import su.rumishistem.rumi_java_lib.FETCH_RESULT;
import su.rumishistem.rumiserver_desktop.Main;

public class SetupForm extends Frame {
	private final int WindowWidth = 500;
	private final int WindowHeight = 200;

	public SetupForm() {
		super("ログイン");
		setSize(WindowWidth, WindowHeight);
		setVisible(true);
		setResizable(false);
		setLayout(new FlowLayout());

		//閉じるボタン
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		Panel p = new Panel();
		p.setLayout(new GridLayout(3, 1, 10, 10));

		TextField UIDInput = new TextField();
		TextField PassInput = new TextField();
		Button SubmitButton = new Button("ログイン");

		p.add(UIDInput);
		p.add(PassInput);
		p.add(SubmitButton);

		add(p);

		SubmitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SubmitButton.disable();

				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							String UID = UIDInput.getText();
							String Pass = PassInput.getText();

							JsonNode Result = Login(UID, Pass, null);

							if (Result.get("STATUS").asBoolean()) {
								if (!Result.get("SESSION_ID").isNull()) {
									//ログインおけ
									ConfigWrite(Result.get("SESSION_ID").asText());
								} else {
									//セッションIDが無いなら追加情報を求める
									switch (Result.get("REQUEST").asText()) {
										//TOTP
										case "TOTP": {
											String TOTP = JOptionPane.showInputDialog(null, "TOTPのコードをどうぞ", "二段階認証", JOptionPane.QUESTION_MESSAGE);
											JsonNode TOTPResult = Login(UID, Pass, TOTP);
											if (TOTPResult.get("STATUS").asBoolean()) {
												ConfigWrite(TOTPResult.get("SESSION_ID").asText());
											} else {
												JOptionPane.showMessageDialog(null, "TOTPコードがおかしいです。", "エラー", JOptionPane.ERROR_MESSAGE);
											}
											break;
										}
									}
								}
							} else {
								SubmitButton.enable();
								JOptionPane.showMessageDialog(null, "ユーザーIDかパスワードが違います。", "エラー", JOptionPane.ERROR_MESSAGE);
							}
						} catch (Exception EX) {
							EX.printStackTrace();
							JOptionPane.showMessageDialog(null, EXCEPTION_READER.READ(EX), "エラー", JOptionPane.ERROR_MESSAGE);
						}
					}
				}).start();
			}
		});
	}

	private void ConfigWrite(String SESSION) throws JsonProcessingException, IOException {
		Main.ConfigFile.getParentFile().mkdirs();
		Main.ConfigFile.createNewFile();

		HashMap<String, String> ConfigData = new HashMap<String, String>();
		ConfigData.put("TOKEN", SESSION);

		FileOutputStream FOS = new FileOutputStream(Main.ConfigFile);
		FOS.write(new ObjectMapper().writeValueAsString(ConfigData).getBytes());
		FOS.flush();
		FOS.close();

		JOptionPane.showMessageDialog(null, "ログインに成功しました、アプリを再起動してください。", "ログインしました", JOptionPane.INFORMATION_MESSAGE);
		System.exit(0);
	}

	private JsonNode Login(String UID, String Pass, String TOTP) throws MalformedURLException, JsonMappingException, JsonProcessingException {
		LinkedHashMap<String, String> Body = new LinkedHashMap<String, String>();
		Body.put("UID", UID);
		Body.put("PASS", Pass);
		Body.put("TOTP", TOTP);

		FETCH Ajax = new FETCH("https://account.rumiserver.com/api/Session");
		FETCH_RESULT AjaxResult = Ajax.POST(new ObjectMapper().writeValueAsString(Body).getBytes());
		JsonNode Result = new ObjectMapper().readTree(AjaxResult.GetString());
		return Result;
	}
}
