package org.saipal.workforce.admistr.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.fmisutil.parser.RequestParser;

public class Transfer {
	public String empid;
	public String orgtype;
	public String admlvl;
	public String provinceid;
	public String districtid;
	public String palika;
	public String org;
	public String officeid;
	public String wid;
	
	
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
		return Arrays.asList("org","hfregistry.hf_name","orgname");
	}

	public static Map<String, String> rules() {
		Map<String, String> rules = new HashMap<>();
		rules.put("orgtype", "required");
		
		
		return rules;
	}
}
