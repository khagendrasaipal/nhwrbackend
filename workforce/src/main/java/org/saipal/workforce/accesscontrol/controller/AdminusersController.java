package org.saipal.workforce.accesscontrol.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.saipal.fmisutil.util.Messenger;
import org.saipal.fmisutil.util.ValidationService;
import org.saipal.fmisutil.util.Validator;
import org.saipal.workforce.accesscontrol.model.Adminusers;
import org.saipal.workforce.accesscontrol.model.Permissions;
import org.saipal.workforce.accesscontrol.service.AdminuserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("access-control/admin-user")
public class AdminusersController {

	@Autowired
	ValidationService validationService;
	
	@Autowired
	AdminuserService adminService;
	
	@GetMapping("get-organizations")
	public ResponseEntity<Map<String,Object>> getOrganization(HttpServletRequest request){
		return adminService.getOrganization();
	}  
	
	@PostMapping("")
	public ResponseEntity<Map<String, Object>> store(HttpServletRequest request) {
		Validator validator = validationService.validate(Adminusers.rules());
		if (validator.isFailed()) {

			return Messenger.getMessenger().setData(validator.getErrorMessages()).error();
		} else {

			return adminService.store();
		}
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Map<String, Object>> edit(HttpServletRequest request, @PathVariable String id) {
		return adminService.edit(id);

	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Map<String, Object>> update(HttpServletRequest request, @PathVariable String id) {
		Validator validator = validationService.validate(Adminusers.rules());
		if (validator.isFailed()) {
			return Messenger.getMessenger().setData(validator.getErrorMessages()).error();
		} else {
			return adminService.update(id);
		}
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Object>> destroy(HttpServletRequest request, @PathVariable String id) {
		return adminService.destroy(id);
	}
	
	
	@GetMapping("")
	public ResponseEntity<Map<String, Object>> index(HttpServletRequest request) {
		return adminService.index();
	}
	
	@GetMapping("/get-permissions")
	public ResponseEntity<Map<String, Object>> getPermissions(HttpServletRequest request) {
		return adminService.getPermissions();
	}
	
	@PostMapping("save-permissions")
	public ResponseEntity<Map<String, Object>> savePermissions() {
		return adminService.savePermissions();
	}
	
	@PostMapping("change-password")
	public ResponseEntity<Map<String, Object>> changePassword() {
		return adminService.changePassword();
	}
}
