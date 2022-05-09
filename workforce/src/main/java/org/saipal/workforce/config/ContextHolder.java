
package org.saipal.workforce.config;

import java.sql.Connection;

public class ContextHolder {

	private static final ThreadLocal<String> CONTEXTTENANT = new ThreadLocal<>();
	private static final ThreadLocal<String> BASEURL = new ThreadLocal<>();
	private static final ThreadLocal<String> CONTEXTVDIR = new ThreadLocal<>();
	private static final ThreadLocal<Connection> CONN = new ThreadLocal<>();
	
	public static void setTenantId(String tenant) {
		CONTEXTTENANT.set(tenant);
	}

	public static String getTenant() {
		return CONTEXTTENANT.get();
	}

	public static String getVDir() {
		return CONTEXTVDIR.get();
	}

	public static void setCon(Connection con) {
		
		CONN.set(con);
	}

	public static Connection getCon() {
		return CONN.get();
	}

	public static void setVDir(String tenant) {
		CONTEXTVDIR.set(tenant);
	}

	public static void clear() {
		CONTEXTTENANT.remove();
		CONTEXTVDIR.remove();
		BASEURL.remove();
		CONN.remove();
	}

	public static void setBaseUrl(String url) {
		BASEURL.set(url);
		
	}
	
	public static String getBaseUrl() {
		return BASEURL.get();
	}

}