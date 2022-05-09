package org.saipal.workforce.accesscontrol.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.saipal.fmisutil.util.ValidationService;
import org.saipal.workforce.accesscontrol.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("access-control/users")
public class UsersController {
	
	@Autowired
	UsersService userService;
	
	@GetMapping("/get-organizations")
	public ResponseEntity<Map<String, Object>> getProvince(HttpServletRequest request) {
		return userService.getOrganizations();
	}
	
	@GetMapping("")
	public ResponseEntity<Map<String, Object>> index(HttpServletRequest request) {
		return userService.index();
	}
	
	@GetMapping("/get-activeemployee")
	public ResponseEntity<Map<String, Object>> getEmployee(HttpServletRequest request) {
		return userService.getActiveEmployee();
	}
	
}
