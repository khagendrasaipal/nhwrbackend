package org.saipal.workforce.omed;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.saipal.fmisutil.ApplicationContextProvider;
import org.saipal.fmisutil.util.DB;

public class OmedBuilder {
	private DB db = ApplicationContextProvider.getBean(DB.class);
	private OMED doc;
	
	private void setDefaults() {
		doc.language = OmedOptions.Language.Np.name();
		doc.priority = OmedOptions.Priority.NORMAL.name();
		doc.privacy = OmedOptions.Privacy.NORMAL.name();
		doc.messageid = db.newIdInt();
		doc.type = OmedOptions.LetterType.LETTER.name();
		doc.initialid = doc.messageid;
	}

	public OmedBuilder() {
		doc = new OMED();
		setDefaults();
	}

	private String getBase64File(FileInputStream fstrm) {
		try {
			return Base64.getEncoder().encodeToString(fstrm.readAllBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}
	}

	private String getBase64File(String path) {
		try {
			return Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(path)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}
	}

	public OmedBuilder setHeader(FileInputStream fstrm) {
		doc.header = getBase64File(fstrm);
		return this;
	}

	public OmedBuilder setFooter(FileInputStream fstream) {
		doc.footer = getBase64File(fstream);
		return this;
	}

	public OmedBuilder setHeader(String path) {
		doc.header = getBase64File(path);
		return this;
	}

	public OmedBuilder setFooter(String path) {
		doc.footer = getBase64File(path);
		return this;
	}

	public OmedBuilder setFrom(String name, String address, String departmnet, String user) {
		doc.from.type = OmedOptions.Orgtype.GOVORG.name();
		doc.from.name = name;
		doc.from.address = address;
		doc.from.department = departmnet;
		doc.from.user = user;
		return this;
	}

	public OmedBuilder setFrom(String type, String name, String address, String departmnet, String user) {
		doc.from.type = type;
		doc.from.name = name;
		doc.from.address = address;
		doc.from.department = departmnet;
		doc.from.user = user;
		return this;
	}

	public OmedBuilder addTo(String name, String address, String departmnet, String user) {
		if (doc.to == null) {
			doc.to = new ArrayList<>();
		}
		ComEntity ent = new ComEntity();
		ent.type = OmedOptions.Orgtype.GOVORG.name();
		ent.name = name;
		ent.address = address;
		ent.department = departmnet;
		ent.user = user;
		doc.to.add(ent);
		return this;
	}

	public OmedBuilder addTo(String type, String name, String address, String departmnet, String user) {
		if (doc.to == null) {
			doc.to = new ArrayList<>();
		}
		ComEntity ent = new ComEntity();
		ent.type = type;
		ent.name = name;
		ent.address = address;
		ent.department = departmnet;
		ent.user = user;
		doc.to.add(ent);
		return this;
	}

	public OmedBuilder addToCc(String name, String address, String departmnet, String user) {
		if (doc.tocc == null) {
			doc.tocc = new ArrayList<>();
		}
		ComEntity ent = new ComEntity();
		ent.type = OmedOptions.Orgtype.GOVORG.name();
		ent.name = name;
		ent.address = address;
		ent.department = departmnet;
		ent.user = user;
		doc.tocc.add(ent);
		return this;
	}

	public OmedBuilder addToCc(String type, String name, String address, String departmnet, String user) {
		if (doc.tocc == null) {
			doc.tocc = new ArrayList<>();
		}
		ComEntity ent = new ComEntity();
		ent.type = type;
		ent.name = name;
		ent.address = address;
		ent.department = departmnet;
		ent.user = user;
		doc.tocc.add(ent);
		return this;
	}

	public OmedBuilder addReferences(String refid) {
		if (doc.references == null) {
			doc.references = new ArrayList<>();
		}
		doc.references.add(refid);
		return this;
	}

	public OmedBuilder addAttachments(String id, String name, String mimetype, String filePath, String creatdby) {
		if (doc.attachments == null) {
			doc.attachments = new ArrayList<>();
		}
		Attachments atch = new Attachments();
		atch.id = id;
		atch.name = name;
		atch.mimetype = mimetype;
		atch.content = getBase64File(filePath);
		atch.createdby = creatdby;
		atch.attachdtime = (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).format(new Date());
		doc.attachments.add(atch);
		return this;
	}
	
	public OmedBuilder setSubject(String subject) {
		doc.subject = subject;
		return this;
	}
	
	public OmedBuilder setPrivacy(String privacy) {
		doc.privacy = privacy;
		return this;
	}
	
	public OmedBuilder setPriority(String priority) {
		doc.priority = priority;
		return this;
	}
	
	public OmedBuilder setLanguage(String lang) {
		doc.language = lang;
		return this;
	}
	
	public OmedBuilder setRegNo(String regno) {
		doc.regno = regno;
		return this;
	}
	public OmedBuilder setDispatchNo(String dispatchno) {
		doc.dispatchno = dispatchno;
		return this;
	}
	
	
	public OmedBuilder setType(String type) {
		doc.type = type;
		return this;
	}

	public OmedBuilder setBodyText(String text) {
		Body b = new Body();
		b.contenttype = OmedOptions.contentType.TEXT.name();
		b.contentid = "";
		b.content = text;
		doc.body = b;
		return this;
	}

	public OmedBuilder setBodyAtt(String id, FileInputStream stream) {
		Body b = new Body();
		b.contenttype = OmedOptions.contentType.ATT.name();
		b.contentid = id;
		b.content = getBase64File(stream);
		doc.body = b;
		return this;
	}

	public OmedBuilder setBodyAttID(String id) {
		Body b = new Body();
		b.contenttype = OmedOptions.contentType.ATTID.name();
		b.contentid = id;
		b.content = "";
		doc.body = b;
		return this;
	}

	public OmedBuilder addMetaData(String key, Object value) {
		doc.metadata.put(key, value);
		return this;
	}
	
	public OmedBuilder includeDocument(OMED doc) {
		doc.body.includes = doc;
		return this;
	}
	
	public OmedBuilder addRouteAction(String action,String user, String touser,String frommsgid,String tomsgid,String datetime) {
		Routing rt = new Routing();
		rt.action = action;
		rt.user = user;
		rt.touser = touser;
		rt.frommsgid = frommsgid;
		rt.tomsgid = tomsgid;
		rt.datetime = datetime;
		doc.routeActions.add(rt);
		return this;
	}
	public OmedBuilder addRouteAction(String action,String user, String touser,String frommsgid,String tomsgid,String datetime,String remarks) {
		Routing rt = new Routing();
		rt.action = action;
		rt.user = user;
		rt.touser = touser;
		rt.frommsgid = frommsgid;
		rt.tomsgid = tomsgid;
		rt.datetime = datetime;
		rt.remarks = remarks;
		doc.routeActions.add(rt);
		return this;
	}
	
	public OMED getDocument() {
		return doc;
	}
	
	public String getJsonDocument() {
		ObjectMapper obj = new ObjectMapper();
		String jsonString = "";
		try {
			jsonString = obj.writeValueAsString(doc);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonString;
	}
}
