package com.dmm.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;

@Controller
public class EditController {
	@Autowired
	private TasksRepository task;
	
	@GetMapping("/edit")
	String loginForm() {
		
		Tasks tasks = new Tasks();
		tasks.setName(tasks.getName());
		tasks.setTitle(tasks.getTitle());
		tasks.setText(tasks.getText());
		tasks.setDate(tasks.getDate());

		task.save(tasks);
		
		return "edit";
	}
}
