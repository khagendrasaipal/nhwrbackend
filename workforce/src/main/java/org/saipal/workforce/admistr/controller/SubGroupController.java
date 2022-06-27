package org.saipal.workforce.admistr.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.saipal.fmisutil.util.Messenger;
import org.saipal.fmisutil.util.ValidationService;
import org.saipal.fmisutil.util.Validator;
import org.saipal.workforce.admistr.model.Category;
import org.saipal.workforce.admistr.model.SubGroup;
import org.saipal.workforce.admistr.service.SubGroupService;
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
@RequestMapping("subgroup")
public class SubGroupController {

	@Autowired
	ValidationService validationService;
	
	@Autowired
	SubGroupService objService;
	
	@PostMapping("")
	public ResponseEntity<Map<String, Object>> store(HttpServletRequest request) {
		Validator validator = validationService.validate(SubGroup.rules());
		if (validator.isFailed()) {

			return Messenger.getMessenger().setData(validator.getErrorMessages()).error();
		} else {

			return objService.store();
		}
	}
	
	@GetMapping("")
	public ResponseEntity<Map<String, Object>> index(HttpServletRequest request) {
		return objService.index();
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Map<String, Object>> edit(HttpServletRequest request, @PathVariable String id) {
		return objService.edit(id);

	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Map<String, Object>> update(HttpServletRequest request, @PathVariable String id) {
		Validator validator = validationService.validate(SubGroup.rules());
		if (validator.isFailed()) {
			return Messenger.getMessenger().setData(validator.getErrorMessages()).error();
		} else {
			return objService.update(id);
		}
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Object>> destroy(HttpServletRequest request, @PathVariable String id) {
		return objService.destroy(id);
	}
	
	@GetMapping("get-samuha")
	public ResponseEntity<Map<String, Object>> getSamuha(HttpServletRequest request) {
		return objService.getSamuha();

	}
	
	@GetMapping("get-upasamuha")
	public ResponseEntity<Map<String, Object>> getUpaSamuha(HttpServletRequest request) {
		return objService.getUpaSamuha();

	}
	
	@GetMapping("get-subgroup/{id}")
	public ResponseEntity<Map<String, Object>> getsubgroup(HttpServletRequest request,@PathVariable String id) {
		return objService.getUpaSamuha(id);

	}
	
	@GetMapping("get-post/{id}")
	public ResponseEntity<Map<String, Object>> getpost(HttpServletRequest request,@PathVariable String id) {
		return objService.getPost(id);

	}
	
	@GetMapping("get-level")
	public ResponseEntity<Map<String, Object>> getLevel(HttpServletRequest request) {
		return objService.getLevel();

	}
	@GetMapping("get-emptype")
	public ResponseEntity<Map<String, Object>> getEmptype(HttpServletRequest request) {
		return objService.getEmptype();

	}
	
	@GetMapping("get-ethincity")
	public ResponseEntity<Map<String, Object>> getethincity(HttpServletRequest request) {
		return objService.getethincity();

	}
	
	@GetMapping("get-emps")
	public ResponseEntity<Map<String, Object>> getEmps(HttpServletRequest request) {
		return objService.getEmps();

	}
	@GetMapping("get-empinfo")
	public ResponseEntity<Map<String, Object>> getEmpinfo(HttpServletRequest request) {
		return objService.getEmpinfo();

	}
	
	@GetMapping("get-transfer")
	public ResponseEntity<Map<String, Object>> getTransfer(HttpServletRequest request) {
		return objService.getTransfer();

	}
	
	@GetMapping("get-council")
	public ResponseEntity<Map<String, Object>> getCouncil(HttpServletRequest request) {
		return objService.getCouncil();

	}
	
	@GetMapping("get-provinces")
	public ResponseEntity<Map<String, Object>> getProvince(HttpServletRequest request) {
		return objService.getProvince();

	}
	
	@GetMapping("get-ownership")
	public ResponseEntity<Map<String, Object>> getownership(HttpServletRequest request) {
		return objService.getownership();

	}
	
	@GetMapping("get-hftype")
	public ResponseEntity<Map<String, Object>> gethftype(HttpServletRequest request) {
		return objService.gethftype();

	}
	
	@GetMapping("get-district/{id}")
	public ResponseEntity<Map<String, Object>> getdistrict(HttpServletRequest request,@PathVariable String id) {
		return objService.getDistrict(id);

	}
	
	@GetMapping("get-districts")
	public ResponseEntity<Map<String, Object>> getdistricts(HttpServletRequest request) {
		return objService.getDistrict();

	}
	
	@GetMapping("get-hf-details/{id}")
	public ResponseEntity<Map<String, Object>> gethfdetails(HttpServletRequest request,@PathVariable String id) {
		return objService.gethfdetails(id);

	}
	
	@GetMapping("get-palika/{id}")
	public ResponseEntity<Map<String, Object>> getpalika(HttpServletRequest request,@PathVariable String id) {
		return objService.getPalika(id);

	}
	
	@GetMapping("get-palikas")
	public ResponseEntity<Map<String, Object>> getpalikas(HttpServletRequest request) {
		return objService.getPalikas();

	}
	

	@GetMapping("get-counts")
	public ResponseEntity<Map<String, Object>> getCounts(HttpServletRequest request) {
		return objService.getCounts();

	}
	
	@GetMapping("get-ethnicitycount")
	public ResponseEntity<Map<String, Object>> ethnicitycount(HttpServletRequest request) {
		return objService.ethnicitycount();

	}
	
	@GetMapping("get-sanctioned")
	public ResponseEntity<Map<String, Object>> getsanctioned(HttpServletRequest request) {
		return objService.getsanctioned();

	}
	
	@GetMapping("get-ward/{id}")
	public ResponseEntity<Map<String, Object>> getward(HttpServletRequest request,@PathVariable String id) {
		return objService.getWard(id);

	}
	
	@GetMapping("get-hf")
	public ResponseEntity<Map<String, Object>> gethf(HttpServletRequest request) {
		return objService.getHf();

	}
	@GetMapping("get-hfbymunc")
	public ResponseEntity<Map<String, Object>> gethfbymunc(HttpServletRequest request) {
		return objService.gethfbymunc();

	}
	@GetMapping("get-orgs")
	public ResponseEntity<Map<String, Object>> getorgs(HttpServletRequest request) {
		return objService.getorgs();

	}
	
	@GetMapping("get-admlvl")
	public ResponseEntity<Map<String, Object>> getadmlvl(HttpServletRequest request) {
		return objService.getadmlvl();

	}
	
	@GetMapping("get-edulevel")
	public ResponseEntity<Map<String, Object>> getedulevel(HttpServletRequest request) {
		return objService.getedulevel();

	}
	
	@GetMapping("get-qualification")
	public ResponseEntity<Map<String, Object>> getqualification(HttpServletRequest request) {
		return objService.getqualification();

	}
	@GetMapping("get-hfo/{id}")
	public ResponseEntity<Map<String, Object>> gethfo(HttpServletRequest request,@PathVariable String id) {
		return objService.gethfo(id);

	}
	
	@GetMapping("get-offices/{id}")
	public ResponseEntity<Map<String, Object>> getoffices(HttpServletRequest request,@PathVariable String id) {
		return objService.getoffices(id);

	}
}
