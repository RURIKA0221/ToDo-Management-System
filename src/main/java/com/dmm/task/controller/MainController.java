package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;

@Controller
public class MainController {
	@Autowired
	private TasksRepository task;
	
	@GetMapping("/main")
	public String main(Model model) {
		
		// 2次元表になるので、ListのListを用意する
		List<List> calendar = new ArrayList<List>();
		// 1週間分のLocalDateを格納するListを用意する
		List<LocalDate> week = new ArrayList<LocalDate>();
		// 当日のインスタンスを取得したあと、その月の1日のインスタンスを得る
		LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
		// 曜日を表すDayOfWeekを取得し、上で取得したLocalDateに曜日の値（DayOfWeek#getValue)をマイナスして前月分のLocalDateを求める
		DayOfWeek firstDayOfWeek = firstDayOfMonth.getDayOfWeek();
		LocalDate current = firstDayOfMonth.minusDays(firstDayOfWeek.getValue());
		while (true) {
			// 1日ずつ増やしてLocalDateを求めていき、2．で作成したListへ格納していき、1週間分詰めたら1．のリストへ格納する
			current = current.plusDays(1);
			week.add(current);
			// 2週目以降は単純に1日ずつ日を増やしながらLocalDateを求めてListへ格納していき、
			// 土曜日になったら1．のリストへ格納して新しいListを生成する（月末を求めるにはLocalDate#lengthOfMonth()を使う）
			if(week.size()==7) {
				calendar.add(week);
				week = new ArrayList<>();
			}
			if(current.getDayOfMonth() == firstDayOfMonth.lengthOfMonth()) {
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
		
		List<Tasks> list = task.findAll(Sort.by(Sort.Direction.DESC, "id"));
		
		// 日付とタスクを紐付ける
	    MultiValueMap<LocalDate, Tasks> tasks = new LinkedMultiValueMap<LocalDate, Tasks>();

	    // コレクションのデータをHTMLに連携
	    model.addAttribute("tasks.get(day)", list);

		return "main";
	}
}
