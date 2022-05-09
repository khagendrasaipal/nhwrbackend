package org.saipal.workforce.omed;

import java.util.List;
import java.util.Map;

public class OMED {
	
	public String messageid;
	public String initialid;
	public String language;
	public String type;
	public String priority;
	public String privacy;
	public List<String> references;
	public String regno;
	public String dispatchno;
	public Dates dates;
	public ComEntity from;
	public List<ComEntity> to;
	public List<ComEntity> tocc;
	public String subject;
	public Body body;
	public String header;
	public String footer;
	public List<Attachments> attachments;
	public List<Routing> routeActions;
	public Map<String, Object> metadata;
	public int version = 1;
}