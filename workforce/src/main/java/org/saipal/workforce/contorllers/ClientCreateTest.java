package org.saipal.workforce.contorllers;

import java.lang.reflect.Field;

import org.saipal.fmisutil.parser.RequestParser;

public class ClientCreateTest {

	public String id;
	public String client_name;
	public String client_id;
	public String redirect_uri;
	public String status;
	public String client_secret;
	
	
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
	
	
}
