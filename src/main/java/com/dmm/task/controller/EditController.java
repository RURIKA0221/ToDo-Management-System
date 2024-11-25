package com.dmm.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
public class EditController {
	@Autowired
	private TasksRepository tasks;

	@GetMapping("main/edit/{id}")
	String editForm(@PathVariable Integer id, Model model) {
		// タスクIDに基づいてタスクを取得
		Tasks task = tasks.getById(id);
		model.addAttribute("task", task);
		return "edit";
	}

	@PostMapping("main/edit/{id}")
	String editTasks(@PathVariable Integer id, @Validated TaskForm taskForm,
			@AuthenticationPrincipal AccountUserDetails user, Model model) {

		// タスクIDに基づいてタスクを取得
		Tasks task = tasks.getById(id);
		
		model.addAttribute("tasks", tasks);
		model.addAttribute("taskForm", taskForm);

		task.setName(user.getName());
		task.setTitle(taskForm.getTitle());
		task.setText(taskForm.getText());
		task.setDone(taskForm.isDone()); // 初期状態では未完了
		task.setDate(taskForm.getDate());
		tasks.save(task);

		return "redirect:/main";

	}

	@PostMapping("main/delete/{id}")
	String deleteTask(@PathVariable Integer id) {
		// タスクIDに基づいて削除
		tasks.deleteById(id);
		return "redirect:/main";
	}
}
