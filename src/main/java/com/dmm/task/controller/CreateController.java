package com.dmm.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;

@Controller
public class CreateController {
	
	@Autowired
	private TasksRepository tasks;
	
	@GetMapping("main/create/{date}")
	String createForm() {
		
		return "main/create/{date}";
	}
	
	@PostMapping("main/create/{date}")
	String createTasks(@Validated BindingResult bindingResult,
			@AuthenticationPrincipal com.dmm.task.service.AccountUserDetails user, Model model) {
		if (bindingResult.hasErrors()) {
			return "main";
		}
		
		Tasks task = new Tasks();
		task.setName(user.getName());
		task.setTitle(task.getTitle());
		task.setText(task.getText());
		task.setDate(task.getDate());

		tasks.save(task);

		return "redirect:/main?date=";
	}
}
