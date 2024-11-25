package com.dmm.task.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.form.TaskForm;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class CreateController {

	@Autowired
	private TasksRepository tasks;

	@GetMapping("main/create/{date}")
	String createForm(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, Model model) {
		// モデルにフォーム用データを設定
		model.addAttribute("date", date);
		return "create";
	}

	@PostMapping("main/create")
	String createTasks(@Validated TaskForm taskForm, @AuthenticationPrincipal AccountUserDetails user, Model model) {

		model.addAttribute("tasks", tasks);
		model.addAttribute("taskForm", taskForm);

		Tasks task = new Tasks();
		task.setName(user.getName());
		task.setTitle(taskForm.getTitle());
		task.setText(taskForm.getText());
		task.setDone(false); // 初期状態では未完了
		task.setDate(taskForm.getDate());

		tasks.save(task);

		return "redirect:/main";
	}
}
