package org.saipal.workforce.admistr.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.saipal.fmisutil.service.AutoService;
import org.saipal.fmisutil.util.Messenger;
import org.saipal.fmisutil.util.ValidationService;
import org.saipal.fmisutil.util.Validator;
import org.saipal.workforce.admistr.model.Workforce;
import org.saipal.workforce.admistr.service.WorkforceService;
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
@RequestMapping("registry")
public class WorkforceController extends AutoService {

	@Autowired
	ValidationService validationService;
	
	@Autowired
	WorkforceService WorkforceService;
	@PostMapping("")
	public ResponseEntity<Map<String, Object>> store(HttpServletRequest request) {
		Validator validator = validationService.validate(Workforce.rules());
		if (validator.isFailed()) {

			return Messenger.getMessenger().setData(validator.getErrorMessages()).error();
		} else {

			return WorkforceService.store();
		}
	}
	
	@GetMapping("")
	public ResponseEntity<Map<String, Object>> index(HttpServletRequest request) {
		return WorkforceService.index();
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Map<String, Object>> edit(HttpServletRequest request, @PathVariable String id) {
		return WorkforceService.edit(id);

	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Map<String, Object>> update(HttpServletRequest request, @PathVariable String id) {
		Validator validator = validationService.validate(Workforce.rules());
		if (validator.isFailed()) {
			return Messenger.getMessenger().setData(validator.getErrorMessages()).error();
		} else {
			return WorkforceService.update(id);
		}
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Object>> destroy(HttpServletRequest request, @PathVariable String id) {
		return WorkforceService.destroy(id);
	}
	
	@GetMapping("/downloadcsv")
	public void getward(HttpServletResponse response,HttpServletRequest request) throws IOException {
		
		 WorkforceService.getcsv(response);

	}
	@GetMapping("/downloadreport")
	public void downloadreport(HttpServletResponse response,HttpServletRequest request) throws IOException {
		
		 WorkforceService.downloadreport(response);

	}
	
	@GetMapping("/pivotexcel")
	public void pivotexcel(HttpServletResponse response,HttpServletRequest request) throws IOException {
		if((request("row").equals("province")||request("row").equals("district")||request("row").equals("palika"))&& request("column").equals("post")) {
			WorkforceService.downloadreportpivot(response);
		}else if(request("row").equals("post") && request("column").equals("emptype")){ 
			 WorkforceService.downloadreportpivot2(response);
		}
			else {
		
			
		}
		
		

	}
	
	@GetMapping("/downloadreport2")
	public void downloadreport2(HttpServletResponse response,HttpServletRequest request) throws IOException {
		
		 WorkforceService.downloadreport2(response);

	}
	@GetMapping("get-report-table")
	public List<Tuple> gettable(HttpServletRequest request) {
		return WorkforceService.gettable();

	}
}
