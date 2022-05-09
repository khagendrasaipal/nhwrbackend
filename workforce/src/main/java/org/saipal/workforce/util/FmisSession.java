package org.saipal.workforce.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.saipal.fmisutil.util.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class FmisSession {
	@Autowired
	DB db;

	private static Logger log = LoggerFactory.getLogger(FmisSession.class);
	private Map<String, Object> data;

	public String session(String key) {
		if (data == null) {
			return "";
		}
		return data.get(key.toLowerCase()) == null ? "" : data.get(key.toLowerCase()) + "";
	}

	public void setAttribute(String key, Object value) {
		if (data == null) {
			data = new HashMap<>();
		}
		data.put(key.toLowerCase(), value);
	}

	public Object getAttribute(String key) {
		if (data == null) {
			return "";
		}
		return data.get(key.toLowerCase()) == null ? "" : data.get(key.toLowerCase());
	}

	public void initSession(String sessionid) {
		if (data == null) {
			data = new HashMap<>();
		}
		String sql = "select variablename,valuess from sys_useractivity where sessionid=?";

		List<Tuple> tList = db.getResultList(sql, Arrays.asList(sessionid));
		if (tList.size() > 0) {
			for (Tuple t : tList) {
				data.put((t.get("variablename") + "").toLowerCase(), t.get("valuess"));
			}
		}
	}

	public void removeAttribute(String key) {
		if (data != null) {
			if (data.get("sessionid") != null) {
				if (data.get(key) != null) {
					data.remove(key);
					String sql = "delete from sys_useractivity where sessionid=? and variablename=?";
					db.execute(sql, Arrays.asList(data.get("sessionid") + "", key.toLowerCase()));
				} else {
					log.info("session attribute not found with key " + key);
				}
			} else {
				log.info("session id not found in map");
			}
		} else {
			log.info("Session map is empty");
		}

	}

}
