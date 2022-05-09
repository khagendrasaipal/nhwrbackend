package org.saipal.workforce.setup.publicDashboardSetup;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.saipal.fmisutil.util.Messenger;
import org.saipal.fmisutil.util.ValidationService;
import org.saipal.fmisutil.util.Validator;
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
@RequestMapping("dashboard-setup")
public class PublicDashboardSetupController {
	
	@Autowired
	ValidationService validationService;
	
	@Autowired
	PublicDashboardSetupService objservice;
	
	@PostMapping("")
	public ResponseEntity<Map<String, Object>> store(HttpServletRequest request) {
		Validator validator = validationService.validate(PublicDashboardSetup.rules());
		if (validator.isFailed()) {

			return Messenger.getMessenger().setData(validator.getErrorMessages()).error();
		} else {

			return objservice.store();
		}
	}
	
	@GetMapping("")
	public ResponseEntity<Map<String, Object>> index(HttpServletRequest request) {
		return objservice.index();
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Map<String, Object>> edit(HttpServletRequest request, @PathVariable String id) {
		return objservice.edit(id);

	}
	
	@GetMapping("get-indicators/{id}")
	public ResponseEntity<Map<String, Object>> getindicators(HttpServletRequest request, @PathVariable String id) {
		return objservice.getindicators(id);

	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Map<String, Object>> update(HttpServletRequest request, @PathVariable String id) {
		Validator validator = validationService.validate(PublicDashboardSetup.rules());
		if (validator.isFailed()) {
			return Messenger.getMessenger().setData(validator.getErrorMessages()).error();
		} else {
			return objservice.update(id);
		}
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Object>> destroy(HttpServletRequest request, @PathVariable String id) {
		return objservice.destroy(id);
	}
	
}
