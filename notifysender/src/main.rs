use notify_rust::Notification;

fn main() {
    Notification::new()
        .summary("新しい通知です")
        .body("これはRustアプリケーションからのテスト通知です。")
        .icon("dialog-information") // 通知に表示されるアイコン (OSによって表示方法が異なる場合があります)
        .timeout(5000) // 通知の表示時間 (ミリ秒)
		.appname("るみさーばーデスクトップ")
        .show()
        .unwrap();
}
