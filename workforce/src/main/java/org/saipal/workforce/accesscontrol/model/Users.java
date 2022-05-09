package org.saipal.workforce.accesscontrol.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.saipal.fmisutil.parser.RequestParser;

public class Users {
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
		return Arrays.asList("id","name", "post", "username");
	}
}
