package org.saipal.workforce.admistr.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.fmisutil.parser.RequestParser;

public class Qualification {


	public String id;
	public String council;
	public String level;
	public String nameen;
	public String namenp;
	public String status;
	
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
		return Arrays.asList("tbl_qualification.nameen", "tbl_qualification.namenp","tbl_council.nameen","tbl_edulevel.nameen");
	}

	public static Map<String, String> rules() {
		Map<String, String> rules = new HashMap<>();
		rules.put("council", "required");
		rules.put("level", "required");
		rules.put("namenp", "required");
		rules.put("nameen", "required");
		return rules;
	}
}
