package org.saipal.workforce.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import org.saipal.fmisutil.ApplicationContextProvider;
import org.saipal.workforce.lang.AppLangService;
import org.saipal.workforce.lang.Translation;
import org.saipal.workforce.util.FmisSession;
import org.saipal.workforce.util.FmisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AutoService extends org.saipal.fmisutil.service.AutoService {
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	protected FmisUtil util;
	
	protected String vbcrlf = "\n";

	@Autowired
	AppLangService appls;

	

	public void logSessionOnly(String key, Object value) {
		FmisSession fmisSession = ApplicationContextProvider.getBean(FmisSession.class);
		fmisSession.setAttribute(key, value);
	}

	/**
	 * Converts given js formula to its equivalent excel formula
	 * 
	 * @param jsFormula jsFormula to be converted
	 * @param map       map containing the header and its equivalent excel column as
	 *                  key value pair
	 * @return equivalent excel formula of given js formula. Replace <AAA> with the
	 *         equivalent excel row value
	 */
	public String getExcelFormula(String jsFormula, Map<String, String> map) {
		List<Character> specChar = Arrays.asList('(', ')', '+', '-', '*', '/');
		StringBuffer buffString = new StringBuffer();
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < jsFormula.length(); i++) {
			char ch = jsFormula.charAt(i);
			if (!specChar.contains(ch)) {
				buffString.append(ch);

			} else if (buffString.length() > 0) {

				String temp = buffString.toString();
				String mapElem = map.get(temp);
				if (mapElem != null) {
					result.append(mapElem + "<AAA>");
				} else {
					result.append(temp);
				}
				result.append(ch);
				buffString = new StringBuffer();
			} else {
				result.append(ch);
			}
		}
		if (buffString.length() > 0) {
			String temp = buffString.toString();
			String mapElem = map.get(temp);
			if (mapElem != null)
				result.append(mapElem + "<AAA>");
			else
				result.append(temp);
		}
		return result.toString();
	}

	/**
	 * converts the given list of header to their equivalent excel column map
	 * 
	 * @param headers List of header of js formula
	 * @param start   Starting index of column in excel file
	 * @return map containing headers as key and their equivalent excel column as
	 *         value
	 */
	public Map<String, String> getExcelMap(List<String> headers, char start) {

		Map<String, String> ret = new HashMap<>();
		char st = (char) ((start + "").toUpperCase().charAt(0));
		String prefix = "";
		for (String s : headers) {
			if (st == 'Z' + 1) {
				prefix += "A";
				st = 'A';
			}
			ret.put(s, prefix + st);
			st++;
		}
		return ret;
	}

	public Map<String, Integer> getProgramLevel(String adminlevel) {
		Map<String, Integer> bList = new HashMap<>();

		List<Tuple> tList = db.getResultList("select * from awpb_admin_programlevel where adminlevel=" + adminlevel);
		bList.put("Bltotal", 0);

		if (tList.size() > 0) {
			putMap(bList, "Bl1", tList.get(0).get("broadsector"));
			putMap(bList, "Bl2", tList.get(0).get("sector"));
			putMap(bList, "Bl3", tList.get(0).get("subsector"));
			putMap(bList, "Bl4", tList.get(0).get("program"));
			putMap(bList, "Bl5", tList.get(0).get("activity"));
			putMap(bList, "Bl6", tList.get(0).get("localactivity"));
			putMap(bList, "Bl7", tList.get(0).get("sectorialactivity"));
			bList.put("Bl8", Integer.parseInt(tList.get(0).get("project") + ""));
			bList.put("Bl9", Integer.parseInt(tList.get(0).get("functionalheading") + ""));

		} else {
			putMap(bList, "Bl1", 1);
			putMap(bList, "Bl2", 1);
			putMap(bList, "Bl3", 1);
			putMap(bList, "Bl4", 1);
			putMap(bList, "Bl5", 1);
			putMap(bList, "Bl6", 1);
			putMap(bList, "Bl7", 1);
			bList.put("Bl8", 1);
			bList.put("Bl9", 1);

		}
		return bList;
	}

	private void putMap(Map<String, Integer> map, String key, Object value) {
		int n = Integer.parseInt(value + "");
		map.put("Bltotal", map.get("Bltotal") + n);
		map.put(key, n);
	}


	public void initdate(String fyid) {
		document.js("tranfy = " + fyid + ";");
		document.js("cfy = " + fyid + ";");
		document.js("todayNp = '" + dUtil.getNepDate(null).replace('-', '/') + "';");
		String dt = dUtil.getEngDate(null);
		document.js("todayEn = '" + dt + "'");
	}

	public void getFromToDate(String elemid) {
		String sql, fyid = "0", monthid = "0", tri = "0";
		fyid = document.getElementById(elemid).getValue(session("fyid"));
		if (fyid.isBlank())
			fyid = "0";
		tri = document.getElementById("trimister").getValue("0");
		monthid = document.getElementById("monthid").getValue("0");
		if (monthid.isBlank())
			monthid = "0";

		/*sql = "select sdate" + session("language") + " as startdate,case when edateint>todaydateint then todaydate"
				+ session("language") + " else edate" + session("language")
				+ " end as enddate from dbo.getdateintbetween(" + fyid + ",0," + monthid + ")";
				*/
		Tuple t=dUtil.getDateBetween(fyid, "0", monthid);
		
		// Tuple dt=dUtil.getDateIntBetween(fyid,tri,monthid);
		//RecordSet rs = new RecordSet();
		//rs.open(sql, db.getConnection());
		//if (rs.state == 1) {
			document.js("document.getElementById('txtdatefrom').value = '" + t.get("sdate"+session("language")) + "';");
			if(cint(t.get("edateint"))>cint(t.get("todaydateint"))) {
				document.js("document.getElementById('txtdateto').value = '" + t.get("todaydate"+session("language")) + "';");
			}
			else {
				document.js("document.getElementById('txtdateto').value = '" + t.get("edate"+session("language")) + "';");
			}
			
			//rs.close();
		//}

	}

	public void getFromToDate() {
		getFromToDate("fiscalyear");

	}

	public String getOrglist(String Permissionfor, String isAccount) {

		String sql;
		String ret = "";
		if (session("orgid").equals("88")) {
			sql = "select orgidint,code,orgname" + session("language")
					+ " as orgname from admin_org_str where account=1 order by code";
		} else {
			if (isAccount.equals("1")) {
				sql = "select orgidint,code,orgname" + session("language")
						+ " as orgname from admin_org_str where account=1 and (orgidint=" + session("orgid")
						+ " or keys like '" + session("orgkey") + "-%') order by orgname";
			} else {
				sql = "select orgidint,code,orgname" + session("language")
						+ " as orgname from admin_org_str where   (orgidint=" + session("orgid") + " or keys like '"
						+ session("orgkey") + "-%') order by orgname";
			}
		}
		List<Tuple> tList = db.getResultList(sql);
		if (tList.size() > 0) {
			// ' & orgrs.fields("code").value & " | "
			for (Tuple t : tList) {
				if (session("orgid").equals(t.get("orgidint"))) {
					ret += "<option value='" + t.get("orgidint") + "' selected='Selected'>" + t.get("orgname")
							+ "</option>" + vbcrlf;
				} else {
					ret += "<option value='" + t.get("orgidint") + "' >" + t.get("orgname") + "</option>" + vbcrlf;
				}
			}
		}
		return ret;

	}

	public String toDate(String valueDate) {
		String ret = "";
		if (!valueDate.isBlank()) {
			int i = -1;
			i = valueDate.indexOf("/");
			if (session("language").equalsIgnoreCase("En")) {
				if (i == 4)
					ret = dUtil.getEngDate(valueDate);
				else if (i == 1) {
					if (valueDate.length() > 10)
						valueDate = valueDate.substring(0, 10);
					LocalDate d2 = LocalDate.parse(valueDate);
					if (d2.getMonthValue() < 10)
						ret = "0" + d2.getMonthValue();
					else
						ret = "" + d2.getMonthValue();
					if (d2.getDayOfMonth() < 10)
						ret += "/0" + d2.getDayOfMonth();
					else
						ret += "/" + d2.getDayOfMonth();
					ret += "/" + d2.getYear();
				} else if (i == -1) {
					i = valueDate.indexOf("-");
					if (i > 0) {// date
						if (valueDate.length() > 10)
							valueDate = valueDate.substring(0, 10);
						LocalDate d2 = LocalDate.parse(valueDate);
						if (d2.getMonthValue() < 10)
							ret = "0" + d2.getMonthValue();
						else
							ret = "" + d2.getMonthValue();
						if (d2.getDayOfMonth() < 10)
							ret += "/0" + d2.getDayOfMonth();
						else
							ret += "/" + d2.getDayOfMonth();
						ret += "/" + d2.getYear();
					} else {// date int
						ret = dUtil.getEngDate(dUtil.intToDate(valueDate));
					}
				}
			} else {
				// begin
				String r = "";
				if (i == 4)
					ret = valueDate;
				else if (i == 1) {
					if (valueDate.length() > 10)
						valueDate = valueDate.substring(0, 10);
					LocalDate d2 = LocalDate.parse(valueDate);
					if (d2.getMonthValue() < 10)
						r = "0" + d2.getMonthValue();
					else
						r = "" + d2.getMonthValue();
					if (d2.getDayOfMonth() < 10)
						r += "/0" + d2.getDayOfMonth();
					else
						r += "/" + d2.getDayOfMonth();
					r += "/" + d2.getYear();
					ret = dUtil.getNepDate(r);
				} else if (i == -1) {
					i = valueDate.indexOf("-");
					if (i > 0) {// date
						if (valueDate.length() > 10)
							valueDate = valueDate.substring(0, 10);
						LocalDate d2 = LocalDate.parse(valueDate);
						if (d2.getMonthValue() < 10)
							r = "0" + d2.getMonthValue();
						else
							r = "" + d2.getMonthValue();
						if (d2.getDayOfMonth() < 10)
							r += "/0" + d2.getDayOfMonth();
						else
							r += "/" + d2.getDayOfMonth();
						r += "/" + d2.getYear();
						ret = dUtil.getNepDate(r);
					} else {
						ret = dUtil.intToDate(valueDate);
					}
				}
				// end

			}
		}

		return ret;
	}

	public String wds(String text) {
		if (text == null) {
			return "";
		}
		if (text.isBlank()) {
			return "";
		}
		String key = text.replaceAll("\\s", "").toLowerCase();
		Translation row = appls.getTranslation(auth.getAppId(), key);
		if (row != null) {
			if (auth.getLang() == null) {
				if (row.nepali == null) {
					return row.english;
				}
				return row.nepali;
			} else if (auth.getLang().equalsIgnoreCase("Np")) {
				if (row.nepali == null) {
					return row.english;
				} else {
					return row.nepali;
				}
			} else {
				return row.english;
			}
		} else {
			insertKey(key, text);
			Translation t = new Translation();
			t.english = text;
			t.nepali = text;
			appls.setTransLation(key, t);
			return text;
		}
	}
}
