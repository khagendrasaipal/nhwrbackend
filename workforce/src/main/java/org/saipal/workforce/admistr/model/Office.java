package org.saipal.workforce.admistr.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.saipal.fmisutil.parser.RequestParser;

public class Office {

	public String orgidint;
	public String workforceid;
	public String darbandiid;
	public String detailsid;
    public String nameen;
    public String namenp;
    public String address;
    public String email;
    public String mobile;
    public String emptype;
    public String apoint_date;
    public String att_date;
    public String education;
    public String council;
    public String level;
    public String council_no;
    public String pis;
    public String qualification;
    public String dob;
    public String gender;
    public String ethnicity;
    
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
		return Arrays.asList("council_no","tbl_employee.nameen","tbl_employee.namenp");
	}

	public static Map<String, String> rules() {
		Map<String, String> rules = new HashMap<>();
		rules.put("orgidint", "required");
		rules.put("workforceid", "required");
		rules.put("darbandiid", "required");
		rules.put("detailsid", "required");
		rules.put("nameen", "required");
		rules.put("namenp", "required");
		rules.put("emptype", "required");
		rules.put("council", "required");
		rules.put("level", "required");
		rules.put("council_no", "required");
		
		return rules;
	}
}
