package org.saipal.workforce.admistr.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.fmisutil.parser.RequestParser;

public class Organization {
	
	public String id;
	public String code;
	public String name;
    public String adm_level;
    public String adm_id;
    public String parent;
    
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
		return Arrays.asList("name", "code", "id");
	}

	public static Map<String, String> rules() {
		Map<String, String> rules = new HashMap<>();
		rules.put("code", "required");
		rules.put("name", "required");
		rules.put("adm_level", "required");
		rules.put("adm_id", "required");
		return rules;
	}

}
