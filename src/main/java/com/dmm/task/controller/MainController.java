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
		// 今月 or 前月 or 翌月を判定
		if (date == null) { // 今月
			// その月の1日を取得する
			targetDate = LocalDate.now(); // 現在日時を取得
			targetDate = targetDate.withDayOfMonth(1); // 現在日時からその月の1日を取得
		} else { // 前月 or 翌月と判断
			targetDate = date; // 引数で受け取った日付をそのまま使う
		}

		// 当日のインスタンスを取得したあと、その月の1日のインスタンスを得る
		LocalDate firstDayOfMonth = targetDate.withDayOfMonth(1);

		// 曜日を表すDayOfWeekを取得し、上で取得したLocalDateに曜日の値（DayOfWeek#getValue)をマイナスして前月分のLocalDateを求める
		DayOfWeek firstDayOfWeek = targetDate.getDayOfWeek();
		//ここでtask用に1週目の前月分の開始日を設定
		LocalDate first = targetDate.minusDays(firstDayOfWeek.getValue());
		LocalDate current = targetDate.minusDays(firstDayOfWeek.getValue());

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
		model.addAttribute("month", dateTimeFormatter.format(targetDate));

		model.addAttribute("prev", firstDayOfMonth.minusMonths(1));
		model.addAttribute("next", firstDayOfMonth.plusMonths(1));

		// 2次元表になるので、ListのListを用意する
		List<List> calendar = new ArrayList<List>();
		// 1週間分のLocalDateを格納するListを用意する
		List<LocalDate> week = new ArrayList<LocalDate>();

		while (true) {
			// 1日ずつ増やしてLocalDateを求めていき、2．で作成したListへ格納していき、1週間分詰めたら1．のリストへ格納する
			week.add(current);
			current = current.plusDays(1);
			// 2週目以降は単純に1日ずつ日を増やしながらLocalDateを求めてListへ格納していき、
			// 土曜日になったら1．のリストへ格納して新しいListを生成する（月末を求めるにはLocalDate#lengthOfMonth()を使う）
			if (week.size() == 7) {
				calendar.add(week);
				week = new ArrayList<>();
			}
			if (current.compareTo(firstDayOfMonth.plusMonths(1).with(DayOfWeek.SATURDAY)) > 0) {
				break;
			}
		}
		// 最終週の翌月分をDayOfWeekの値を使って計算し、6．で生成したリストへ格納し、最後に1．で生成したリストへ格納する
		if (!week.isEmpty()) {
			int remainDays = DayOfWeek.SATURDAY.getValue() - week.get(week.size() - 1).getDayOfWeek().getValue();
			for (int i = 0; i < remainDays; i++) {
				week.add(current);
				current = current.plusDays(1);
			}
			calendar.add(week);
		}
		model.addAttribute("matrix", calendar);

		//ここでtask用に最終週の翌月分の終了日を設定
		LocalDate last = firstDayOfMonth.plusMonths(1).with(DayOfWeek.SATURDAY);

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

		for (Tasks task : taskList) {
			taskMap.add(task.getDate(), task);
		}
		// コレクションのデータをHTMLに連携
		model.addAttribute("tasks", taskMap);

		return "main";
	}
}
