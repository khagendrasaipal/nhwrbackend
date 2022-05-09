package org.saipal.workforce.admistr.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.fmisutil.parser.RequestParser;

public class Workforce {


	public String id;
	public String provinceid;
	public String districtid;
	public String palika;
	public String ward;
	public String org;
	public String authority;
	public String authlevel;
	public String ownership;
	public String ftype;
	public String orgtype;
	public String orgname;
	
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
		return Arrays.asList("provinceid", "org");
	}

	public static Map<String, String> rules() {
		Map<String, String> rules = new HashMap<>();
		rules.put("provinceid", "required");
		rules.put("districtid", "required");
		rules.put("palika", "required");
		rules.put("ward", "required");
		
		
		return rules;
	}
}
