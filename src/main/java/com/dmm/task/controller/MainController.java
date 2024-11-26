package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class MainController {

	@Autowired
	private TasksRepository tasks;

	@GetMapping("/main")
	public String main(Model model, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
			@AuthenticationPrincipal AccountUserDetails user) {

		LocalDate targetDate;
		// 指定された日付がない場合は、現在の日付を基準とする
		if (date == null) {
			// 現在日時を取得
			targetDate = LocalDate.now();
			// 現在日時からその月の1日を取得
			targetDate = targetDate.withDayOfMonth(1);
		} else {
			// 引数で受け取った日付をそのまま使う
			targetDate = date;
		}

		// 当日のインスタンスを取得したあと、その月の1日のインスタンスを得る
		LocalDate firstDayOfMonth = targetDate.withDayOfMonth(1);

		LocalDate current;
		
		// 曜日を表すDayOfWeekを取得
		DayOfWeek firstDayOfWeek = targetDate.getDayOfWeek();
		//もしfirstDayOfWeekが日曜日なら前月分はなし
		if(firstDayOfWeek ==DayOfWeek.SUNDAY) {
			current = targetDate;
		}else {
		// 上で取得したtargetDateに曜日の値をマイナスして前月分のLocalDateを求める
		current = targetDate.minusDays(firstDayOfWeek.getValue());}

		// ここでtask用に1週目の前月分の開始日を設定
		LocalDate first = targetDate.minusDays(firstDayOfWeek.getValue());

		// カレンダー上部に「○○年○○月」と表示
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
		model.addAttribute("month", dateTimeFormatter.format(targetDate));

		// 前月、翌月分の処理
		model.addAttribute("prev", firstDayOfMonth.minusMonths(1));
		model.addAttribute("next", firstDayOfMonth.plusMonths(1));

		// 2次元表になるので、ListのListを用意する
		List<List> calendar = new ArrayList<List>();
		// 1週間分のLocalDateを格納するListを用意する
		List<LocalDate> week = new ArrayList<LocalDate>();

		while (true) {
			// 1日ずつ増やしてLocalDateを求めていき、weekへ格納
			week.add(current);
			current = current.plusDays(1);
			// 土曜日になって1週間分詰めたらcalenderへ格納。また新しいweekを生成する
			if (week.size() == 7) {
				calendar.add(week);
				week = new ArrayList<>();
			}
			// 翌月の最初の土曜日を超えたらループ終了
			if (current.compareTo(firstDayOfMonth.plusMonths(1).with(DayOfWeek.SATURDAY)) > 0) {
				break;
			}
		}

		// ここでtask用に最終週の翌月分の終了日を設定
		LocalDate last = firstDayOfMonth.plusMonths(1).with(DayOfWeek.SATURDAY);

		// 最終週の翌月分をweekへ格納し、calenderへ格納
		if (!week.isEmpty()) {
			// 最終週で翌月分を埋める日数
			int remainDays = DayOfWeek.SATURDAY.getValue() - week.get(week.size() - 1).getDayOfWeek().getValue();
			for (int i = 0; i < remainDays; i++) {
				week.add(current);
				current = current.plusDays(1);
			}
			calendar.add(week);
		}
		// celenderモデルに設定
		model.addAttribute("matrix", calendar);

		// taskリストを生成
		List<Tasks> taskList = new ArrayList<Tasks>();

		// adminかどうかの処理
		boolean isAdmin = false;
		for (GrantedAuthority authority : user.getAuthorities()) {
			if ("ROLE_ADMIN".equals(authority.getAuthority())) {
				isAdmin = true;
				break;
			}
		}

		if (isAdmin) {
			// adminなら全タスク取得
			taskList = tasks.findByDateBetween2(first, last);
		} else {
			// userなら自身のタスクのみ取得
			taskList = tasks.findByDateBetween(first, last, user.getName());
		}

		// 日付とタスクを紐付ける
		MultiValueMap<LocalDate, Tasks> taskMap = new LinkedMultiValueMap<LocalDate, Tasks>();

		// taskリストの数だけtaskをtaskMapに追加
		for (Tasks task : taskList) {
			taskMap.add(task.getDate(), task);
		}
		// コレクションのデータをHTMLに連携
		model.addAttribute("tasks", taskMap);

		return "main";
	}
}
