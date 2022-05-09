package org.saipal.workforce.contorllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.fmisutil.service.AutoService;
import org.saipal.fmisutil.util.HttpRequest;
import org.saipal.fmisutil.util.Messenger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ClientCreateTestService extends AutoService{

	public String store() {
		
		ClientCreateTest model = new ClientCreateTest();
		model.loadData(document);
		
		String baseUrl = "http://localhost:8011";
		String client_id=model.client_id;
		String client_name=model.client_name;
		String redirect_uri=model.redirect_uri;
		String client_secret=model.client_secret;
		String status=model.status;
		String urlpart="?client_id="+client_id+"&redirect_uri="+redirect_uri+"&client_name="+client_name+"&client_secret="+client_secret+"&status="+status;
		String url = baseUrl + "/add-clients"+urlpart;
		HttpRequest requests = new HttpRequest();
		JSONObject responses = requests.get(url);
		System.out.println(responses);
		return responses.toString();
	}

	public List<Tuple> getOrgs(HttpServletRequest request) {
		String sql = "select * from organization where id="+request("orgid");
		List<String> argList = new ArrayList<String>();
		List<Tuple> tList = db.getResultList(sql, argList);
		return tList;
		
	}

	public ResponseEntity<Map<String, Object>> getProgram() {
		String sql = "select id,title from programme where 1=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("name", t.get("title"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getIndicators(String id) {
		String sql = "select uid,data_elements,data_elements_np from data_elements where pid=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(id));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("uid"));
				mapadmlvl.put("name", t.get("data_elements"));
				mapadmlvl.put("namenp", t.get("data_elements_np"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getData(HttpServletRequest request) throws Exception {
		String indId=request("iid");
		String fy=request("fy");
//		String ouid="YqQbkwADI71";
		String sql1="select * from organization where id="+session("orgid");
		List<String> argList1 = new ArrayList<String>();
		List<Tuple> tList1 = db.getResultList(sql1, argList1);
		String ouid=tList1.get(0).get("uid").toString();
		String pe=preparePeriods(fy);
		String pef = pe.replaceFirst(".$","");
//		System.out.println(pef);
		  String link = "/api/analytics.json?dimension=dx:"+indId+"&dimension=pe:"+pef+"&filter=ou:"+ouid;
//		  String link = "/api/29/analytics?dimension=dx:"+indId+",ou:"+ouid+"&filter=pe:"+pe;
		  System.out.println(getResponse(link));
		  return Messenger.getMessenger().setData(getResponse(link)).success();
//		return null;
	}

	private String preparePeriods(String fys) {
		String pe = "";
		int i;
        int fy=Integer.parseInt(fys) ;
            int nextFy = fy+1;
            for(i=12;i>0;i--){
                if(i>9){
                    pe +=fy+""+i+";";
                }else{
                    if(i<4){
                        pe +=nextFy+"0"+i+";";
                    }else{
                        pe +=fy+"0"+i+";";
                    }
                }
            }
        // }
//        pe = rtrim(pe,";");
        return pe;
	}

	private List<String> getResponse(String link) throws Exception {
		String user = "opml.dashboard";
		String pass= "#Mis@2020";
		String server = "http://hmis.gov.np/hmis";
		String uri = link;
		String credentials = "Basic "+Base64.getEncoder().encodeToString((user+":"+pass).getBytes());
		String url=server+uri;
		HttpRequest requests = new HttpRequest();
		JSONObject responses = requests.setHeader("Authorization",credentials)
				.setHeader("Accept", "application/json").get(url);
		JSONObject resp = responses.getJSONObject("data");

		JSONArray datas = resp.getJSONArray("rows");
		Map<Integer,String> mdata = new HashMap<>();
//		System.out.println(datas);
		for (int i = 0 ; i < datas.length(); i++) {
			JSONArray obj = datas.getJSONArray(i);
			int month = Integer.parseInt(obj.getString(1).substring(4));
			mdata.put(month, obj.getString(2));
		}
		Map<Integer,String> months = new LinkedHashMap<>();
		months.put(4,"Shrawn");
		months.put(5,"Bhadra");
		months.put(6,"Ashoj");
		months.put(7,"Kartik");
		months.put(8,"Mangsir");
		months.put(9,"Poush");
		months.put(10,"Magh");
		months.put(11,"Falgun");
		months.put(12,"Chaitra");
		months.put(1,"Baishakh");
		months.put(2,"Jestha");
		months.put(3,"Ashar");
		//("4","Shrawn","5","Bhadra","6","Ashoj","7","Kartik","8","Mangsir","9","Poush","10","Magh","11","Falgun","12","Chaitra","1","Baishakh","2","Jestha","3","Ashar");
		List<Integer> mindex = Arrays.asList(4,5,6,7,8,9,10,11,12,1,2,3);
		List<String> fdata = new ArrayList<>();
		List<String> mnths = new ArrayList<>();
		for(int m:mindex) {
			if(mdata.containsKey(m)) {
				fdata.add(mdata.get(m));
			}else {
				fdata.add("0");
			}
			
			mnths.add(months.get(m));
		}
//		System.out.println(fdata);
		return fdata;

	}

	public ResponseEntity<Map<String, Object>> getComposite() {
		String sql = "select weight,value,fy,concat(fy,\\\"/\\\",fy+1) as fys,composite_indicator.name as indicator from composite_indicators_value join composite_indicator on composite_indicator.id=composite_indicators_value.indicator where orgid=? group by indicator,fy order by fy";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(session("orgid")));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("weight", t.get("weight"));
				mapadmlvl.put("value", t.get("value"));
				mapadmlvl.put("fys", t.get("fys"));
				mapadmlvl.put("indicator", t.get("indicator"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public List<Tuple> getChartConfig(HttpServletRequest request) {
		String sql = "select indicator,concat(fy,\\\"/\\\",fy+1) as fys,chart_type,data_elements.data_elements as inameen,data_elements.data_elements_np as inamenp from public_dashboard_setup join data_elements on data_elements.uid=public_dashboard_setup.indicator where orgid="+request("orgid");
		List<String> argList = new ArrayList<String>();
		List<Tuple> tList = db.getResultList(sql, argList);
		return tList;
	}

	public List<Map<String, Object>> getWeatherInfo(HttpServletRequest request) throws JSONException {
		
		String apikey="b3677a60280c0c3589a6bf27bcab3aeb";
		String sql="select value from indicators_value where orgid=? and indicator=?";
		Map<String, Object> lat = db.getSingleResultMap(sql, Arrays.asList(request("orgid"),8));
		
		String sql1="select value from indicators_value where orgid=? and indicator=?";
		Map<String, Object> longs = db.getSingleResultMap(sql1, Arrays.asList(request("orgid"),9));		
		String lats=lat.get("value").toString();
		String lon=longs.get("value").toString();
	
		
		String url="https://api.openweathermap.org/data/2.5/weather?lat="+lats+"&lon="+lon+"&appid="+apikey+"&units=metric";
//		String url="https://api.openweathermap.org/data/2.5/weather?lat=27.89272170659029&lon=86.83190351004616&appid="+apikey+"&units=metric";
		HttpRequest requests = new HttpRequest();
		JSONObject responses = requests.setHeader("Accept", "application/json").get(url);
		JSONObject resp = responses.getJSONObject("data");
		String temp=resp.getJSONObject("main").get("temp").toString();
		JSONArray main=resp.getJSONArray("weather");
		JSONObject resps=main.getJSONObject(0);
		String type=resps.get("main").toString();
		String desc=resps.get("description").toString();
		String icon=resps.get("icon").toString();
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> mapadmlvl = new HashMap<>();
		mapadmlvl.put("temp", temp);
		mapadmlvl.put("type", type);
		mapadmlvl.put("desc", desc);
		mapadmlvl.put("icon", icon);
		list.add(mapadmlvl);
		// TODO Auto-generated method stub
		return list;
	}

	public ResponseEntity<Map<String, Object>> getDashboard() throws Exception {
		String sql = "select indicator,fy,concat(fy,\\\"/\\\",fy+1) as fys,chart_type,data_elements.data_elements as inameen,data_elements.data_elements_np as inamenp from public_dashboard_setup join data_elements on data_elements.uid=public_dashboard_setup.indicator where orgid="+request("orgid");
		List<String> argList = new ArrayList<String>();
		List<Tuple> tList = db.getResultList(sql, argList);
		String sql1="select * from organization where id="+request("orgid");
		List<String> argList1 = new ArrayList<String>();
		List<Tuple> tList1 = db.getResultList(sql1, argList1);
		String ouid=tList1.get(0).get("uid").toString();
		List<Map<String, Object>> list = new ArrayList<>();
		for (Tuple t : tList) {
//			String ouid="YqQbkwADI71";
			String pe=preparePeriods(t.get("fy").toString());
			String pef = pe.replaceFirst(".$","");
			 String link = "/api/analytics.json?dimension=dx:"+t.get("indicator").toString()+"&dimension=pe:"+pef+"&filter=ou:"+ouid;
			Map<String, Object> mapadmlvl = new HashMap<>();
			mapadmlvl.put("indicator", t.get("indicator"));
			mapadmlvl.put("chart_type", t.get("chart_type"));
			mapadmlvl.put("fys", t.get("fys"));
			mapadmlvl.put("inameen", t.get("inameen"));
			mapadmlvl.put("inamenp", t.get("inamenp"));
			mapadmlvl.put("data", getResponse(link));
			list.add(mapadmlvl);
		}
		return Messenger.getMessenger().setData(list).success();
	}

	public List<Tuple> getIntro(HttpServletRequest request) {
		String sql = "select * from indicators_value where orgid="+request("orgid")+" and indicator=10";
		List<String> argList = new ArrayList<String>();
		List<Tuple> tList = db.getResultList(sql, argList);
		return tList;
	}

	public String getHfCount(HttpServletRequest request) throws JSONException {
		String sql = "select * from organization where id="+request("orgid");
		List<String> argList = new ArrayList<String>();
		List<Tuple> tList = db.getResultList(sql, argList);
		String url="https://nhfr.mohp.gov.np/health-registry/auth-count?roles=palika&rvalues="+tList.get(0).get("adm_id").toString();
		HttpRequest requests = new HttpRequest();
		JSONObject responses = requests.setHeader("Accept", "application/json").get(url);
		JSONArray resp = responses.getJSONArray("data");
		return resp.get(0).toString();
	}

	public List<Tuple> getWards(HttpServletRequest request) {
		String sql = "select * from organization where id="+request("orgid");
		List<String> argList = new ArrayList<String>();
		List<Tuple> tList = db.getResultList(sql, argList);
		
		String sql1 = "select * from admin_local_level_structure where vcid="+tList.get(0).get("adm_id");
		List<String> argList1 = new ArrayList<String>();
		List<Tuple> tList1 = db.getResultList(sql1, argList1);
//		return tList;
		return tList1;
	}

	public List<Tuple> getUpdates(HttpServletRequest request) {
		String sql = "select * from indicators_value where cat_id=1 and indicator=1 and orgid="+request("orgid");
		List<String> argList = new ArrayList<String>();
		List<Tuple> tList = db.getResultList(sql, argList);
		return tList;
	}

	public String getLat(HttpServletRequest request) {
		String sql="select value from indicators_value where orgid=? and indicator=?";
		Map<String, Object> lat = db.getSingleResultMap(sql, Arrays.asList(request("orgid"),8));
		return lat.get("value").toString();
	}

	public String getLong(HttpServletRequest request) {
		String sql1="select value from indicators_value where orgid=? and indicator=?";
		Map<String, Object> longs = db.getSingleResultMap(sql1, Arrays.asList(request("orgid"),9));	
		return longs.get("value").toString();
	}

	public List<Tuple> getCensusData(HttpServletRequest request) {
		String sql = "select * from organization where id="+request("orgid");
		List<String> argList = new ArrayList<String>();
		List<Tuple> tList = db.getResultList(sql, argList);
		
		String sql1 = "select * from population where vcid="+tList.get(0).get("adm_id");
		List<String> argList1 = new ArrayList<String>();
		List<Tuple> tList1 = db.getResultList(sql1, argList1);
//		return tList;
		return tList1;
	}
}
