package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.SystemLogDao;
import com.example.demo.model.SystemLog;

@CrossOrigin(origins = "http://localhost:8081", maxAge = 3600)
@RestController
public class CMSController {
	@Autowired
	private SystemLogDao dao;
	
	@GetMapping("/cms/all")
	@PreAuthorize("hasRole('ADMIN')")
	public List<SystemLog> searchAll() {	
		
		return dao.findAll();
		
	}
}
