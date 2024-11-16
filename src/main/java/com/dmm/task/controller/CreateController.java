package com.dmm.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;

@Controller
public class CreateController {
	
	@Autowired
	private TasksRepository task;
	
	@GetMapping("/create")
	String loginForm(@Validated BindingResult bindingResult,
			@AuthenticationPrincipal com.dmm.task.service.AccountUserDetails user, Model model) {
		
		Tasks tasks = new Tasks();
		tasks.setName(tasks.getName());
		tasks.setTitle(tasks.getTitle());
		tasks.setText(tasks.getText());
		tasks.setDate(tasks.getDate());

		task.save(tasks);

		return "create";
	}
}
