use std::env;
use notify_rust::Notification;

fn main() {
	let arg_list: Vec<String> = env::args().collect();

	if arg_list.len() != 4 {
		println!("引数が足りません。");
		println!("\"アプリ名\" \"タイトル\" \"本文\"");
		return;
	}

    let notification = Notification::new()
        .summary(&arg_list[2])
        .body(&arg_list[3])
		.action("default", "click")
        .timeout(5000)
		.appname(&arg_list[1])
        .show()
        .unwrap();

	notification.wait_for_action(|action| match action {
		"default" => std::process::exit(1),
		"__closed" => std::process::exit(0),
		_ => std::process::exit(255)
	});
}
