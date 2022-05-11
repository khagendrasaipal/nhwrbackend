package org.saipal.workforce.admistr.registration;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.saipal.fmisutil.util.Messenger;
import org.saipal.fmisutil.util.ValidationService;
import org.saipal.fmisutil.util.Validator;
import org.saipal.workforce.admistr.registration.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-register")
public class RegistrationController {
	
	@Autowired
	ValidationService validationService;
	
	@Autowired
	RegistrationService registrationService;
	
	@PostMapping("")
	public ResponseEntity<Map<String, Object>> store(HttpServletRequest request) {
		Validator validator = validationService.validate(Registration.rules());
		if (validator.isFailed()) {

			return Messenger.getMessenger().setData(validator.getErrorMessages()).error();
		} else {

			return registrationService.store();
		}
	}
	
	
}
