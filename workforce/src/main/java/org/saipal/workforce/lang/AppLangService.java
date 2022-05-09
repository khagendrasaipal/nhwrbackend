package org.saipal.workforce.lang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Tuple;

import org.saipal.fmisutil.util.DB;
import org.saipal.workforce.service.AutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class AppLangService {

	@Autowired
	private DB db;

	private String defaultKey = "0";

	@Autowired
	AutoService autoService;

	@Value("${spring.datasource.url}")
	private String url;

	public Map<String, Map<String, Translation>> appLangStroe = new HashMap<>();
	
	private String getDbName() {
		Pattern pattern = Pattern.compile(";databaseName=([^;]*)");
		Matcher matcher = pattern.matcher(url);
		matcher.find();
		String dbName = matcher.group(1);
		return dbName;
	}

	public Translation getTranslation(String appId, String langKey) {
		
		appId = getDbName() + appId;

		if (appLangStroe.containsKey(appId)) {
			Translation lng = appLangStroe.get(appId).get(langKey);
			if (lng != null && !lng.nepali.equals("null") && lng.nepali != null) {
				return lng;
			}
		}
		return getTranslation(langKey);
	}

	public Translation getTranslation(String langKey) {
		String defaultKey = getDbName() + this.defaultKey;
		if (appLangStroe.containsKey(defaultKey)) {
			Translation lng = appLangStroe.get(defaultKey).get(langKey);
			if (lng != null) {
				return lng;
			}
		}
		return null;
	}

	public void setTransLation(String key, Translation t) {
		String defaultKey = getDbName() + this.defaultKey;
		if (appLangStroe.containsKey(defaultKey)) {
			Map<String, Translation> st = appLangStroe.get(defaultKey);
			st.put(key, t);
		}
	}

	public void setTransLation(String appId, String key, Translation t) {
		appId = getDbName() + appId;
		if (appLangStroe.containsKey(appId)) {
			Map<String, Translation> st = appLangStroe.get(appId);
			st.put(key, t);
		}
	}

	public void pushAllTranslations() {
		if (appLangStroe.size() < 1) {
			String sql = "select * from tbllanguagefile";
			List<Tuple> trans = db.getResultList(sql);
			trans.forEach(t -> {
				pushTranslations(t);
			});
		}
	}
	
	public void loadAllTranslations() {
		String sql = "select appid,`keys`,nepali,english,nepali1,nepali2,nepali4,english1,english2,english4 from tbllanguagefile";
		List<Tuple> langData = db.getResultList(sql);
		for(Tuple t : langData) {
			pushTranslations(t);
		}
	}

	private void pushTranslations(Tuple t) {
		String dbname = getDbName();
		Translation tln = new Translation();
		tln.english = checkData(t.get("english"));
		tln.nepali = checkData(t.get("nepali"));

		tln.english1 = checkData(t.get("english1"));
		tln.nepali1 = checkData(t.get("nepali1"));

		tln.english2 = checkData(t.get("english2"));
		tln.nepali2 = checkData(t.get("nepali2"));

		tln.english4 = checkData(t.get("english4"));
		tln.nepali4 = checkData(t.get("nepali4"));

		String appId = dbname.toLowerCase() + t.get("appid");
		String key = (t.get("keys") + "").toLowerCase();
		if (appLangStroe.containsKey(appId)) {
			appLangStroe.get(appId).put(key, tln);
		} else {
			Map<String, Translation> st = new HashMap<>();
			st.put(key, tln);
			appLangStroe.put(appId, st);
		}
		
	}

	private String checkData(Object dta) {
		String data = dta + "";
		if (data.isEmpty() || data.isBlank()) {
			return null;
		}
		if (data.equalsIgnoreCase("null")) {
			return null;
		}
		return data;
	}
}
