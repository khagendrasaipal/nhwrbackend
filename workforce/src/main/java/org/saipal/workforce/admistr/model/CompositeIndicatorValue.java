package org.saipal.workforce.admistr.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.fmisutil.parser.RequestParser;

public class CompositeIndicatorValue {

	public String id;
	public String domain_id;
	public String indicator;
	public String fy;
	public String weight;
	public String value;
	
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
		return Arrays.asList("indicator", "value");
	}

	public static Map<String, String> rules() {
		Map<String, String> rules = new HashMap<>();
		rules.put("domain_id", "required");
		rules.put("fy", "required");
		rules.put("indicator", "required");
		rules.put("value", "required");
		rules.put("weight", "required");
		return rules;
	}
}
