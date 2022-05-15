package org.saipal.workforce.admistr.registration;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.fmisutil.parser.RequestParser;

public class Registration {
	
	public String username;
	public String password;
	public String surname;
	public String firstname;
	public String email;
	public String mobile; //NOT REQUIRED
	public String role;	
	public String province;
	public String district;
	public int status = 2;
	
	public String municipality; //NOT REQUIRED IN CERTAIN CASES
	public int orgid = 10546; 
	
	
	
	public void loadData(RequestParser doc) {
		for (Field f : this.getClass().getFields()) {
			String fname = f.getName();
			try {
				f.set(this, doc.getElementById(fname).value);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static List<String> searchables() {
		return Arrays.asList("users.username","users.email");
	}
	

	public static Map<String, String> rules() {
		Map<String, String> rules = new HashMap<>();
		rules.put("username", "required");	
		rules.put("password", "required");
		rules.put("surname", "required");
		rules.put("firstname", "required");
		rules.put("email", "required");
		
		rules.put("role", "required");	
		rules.put("province", "required");	
		rules.put("district", "required");	
		return rules;
	}
}
