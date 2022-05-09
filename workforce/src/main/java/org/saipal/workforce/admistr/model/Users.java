package org.saipal.workforce.admistr.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.fmisutil.parser.RequestParser;

public class Users {

	public String id;
	public String fullname;
	public String email;
    public String password;
    public String cpassword;
    public String orgid;
    public String role;
    
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
		return Arrays.asList("fullname", "email", "id");
	}

	public static Map<String, String> rules() {
		Map<String, String> rules = new HashMap<>();
		rules.put("fullname", "required");
		rules.put("email", "required");
		rules.put("password", "required");
		rules.put("cpassword", "required");
		rules.put("orgid", "required");
		
		return rules;
	}
}
