package org.saipal.workforce.contorllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
	
	public List<Map<String, Object>> gettabledata2(HttpServletRequest request) {
		String orgtype=request("orgtype");
		String aid=request("admid");
		String pid=request("pid");
		String did=request("did");
		String munc=request("munc");
		String oid=request("ofc");
		String hfid=request("hfid");
		
		String filename = "healthworkforce.csv";
		String sql="";
		
		if("1".equals(orgtype)) {
			if(aid.equals("")) {
				sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=1 group by tbl_darbandi.post";
			}
			if(aid.equals("1")) {
				if(oid.equals("0")) {
					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=1 group by tbl_darbandi.post";
				}else {
					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+" group by tbl_darbandi.post";
				}
			}
			if(aid.equals("2")) {
				if(pid.equals("0")) {
					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 group by tbl_darbandi.post";
				}else {
					if(oid.equals("0")) {
						sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
					}else {
						sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.officeid="+oid+" group by tbl_darbandi.post";
					}
				}
			}
			if(aid.equals("4")) {
				if(pid.equals("0")) {
					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 group by tbl_darbandi.post";
				}else {
					if(did.equals("0")) {
						sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
					}else {
						if(munc.equals("0")) {
							sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_darbandi.post";
						}else {
							sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_darbandi.post";
						}
					}
					 
				}
			}
			
		}else {
			if(pid.equals("0")) {
				sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=4 group by tbl_darbandi.post";
			}else {
				if(did.equals("0")) {
					
					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
				}else {
					if(munc.equals("0")) {
						sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.districtid="+did+" group by tbl_darbandi.post";
					}else {
						if(hfid.equals("0")) {
							sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.palika="+munc+" group by tbl_darbandi.post";
						}else {
							sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.org="+hfid+" group by tbl_darbandi.post";
						}
					}
				}
			}
		}
//		System.out.println(sql);

		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
		
		
		
		
		
		
		   Map<Integer, Object> map;
	        map = new HashMap<Integer, Object>();
	        List<Map<String, Object>> lists = new ArrayList<>();
	        for (Tuple t : admlvl) {
	        	List<Map<String, Object>> list = new ArrayList<>();
		        Map<String, Object> mapadmlvl = new HashMap<>();
				for(int i=1;i<=8;i++) {
					mapadmlvl.put(i+"",getKarar(t.get("post").toString(),aid,pid,oid,i,orgtype,did,munc,hfid));
					
				}
				list.add(mapadmlvl);
				Map<String, Object> maps = new HashMap<>();
				maps.put("postname", t.get("postname"));
				maps.put("sdarbandi", t.get("sdarbandi"));
				maps.put("dworking", t.get("dworking"));
				maps.put("kworking", t.get("kworking"));
				maps.put("tworking", Integer.parseInt(t.get("kworking").toString())+Integer.parseInt(t.get("dworking").toString()));
				maps.put("1", list.get(0).get("1").toString());
				maps.put("8", list.get(0).get("8").toString());
				maps.put("2", list.get(0).get("2").toString());
				maps.put("3", list.get(0).get("3").toString());
				maps.put("4", list.get(0).get("4").toString());
				maps.put("5", list.get(0).get("5").toString());
				maps.put("6", list.get(0).get("6").toString());
				maps.put("7", list.get(0).get("7").toString());
				lists.add(maps);
			}
	        
	        return lists;
	        
//			for (Tuple t : admlvl) {
//				List<Map<String, Object>> list = new ArrayList<>();
//		        Map<String, Object> mapadmlvl = new HashMap<>();
//				for(int i=1;i<=8;i++) {
//					mapadmlvl.put(i+"",getKarar(t.get("post").toString(),aid,pid,oid,i,orgtype,did,munc,hfid));
//					
//				}
//				list.add(mapadmlvl);
				
				
				
				
			
//				row = sheet.createRow(rowCount++);
//
//				int columnCount = 0;
//				cell = row.createCell(columnCount++);
//				cell.setCellValue(t.get("postname")+"");
//				cell = row.createCell(columnCount++);
//				cell.setCellValue(t.get("sdarbandi")+"");
//				cell = row.createCell(columnCount++);
//				cell.setCellValue(t.get("dworking")+"");
//				cell = row.createCell(columnCount++);
//				cell.setCellValue(t.get("kworking")+"");
//				cell = row.createCell(columnCount++);
//				cell.setCellValue(Integer.parseInt(t.get("dworking").toString())+Integer.parseInt(t.get("kworking").toString()));
//				cell = row.createCell(columnCount++);
//				cell.setCellValue(list.get(0).get("1").toString());
//				cell = row.createCell(columnCount++);
//				cell.setCellValue(list.get(0).get("8").toString());
//				cell = row.createCell(columnCount++);
//				cell.setCellValue(list.get(0).get("2").toString());
//				cell = row.createCell(columnCount++);
//				cell.setCellValue(list.get(0).get("3").toString());
//				cell = row.createCell(columnCount++);
//				cell.setCellValue(list.get(0).get("4").toString());
//				cell = row.createCell(columnCount++);
//				cell.setCellValue(list.get(0).get("5").toString());
//				cell = row.createCell(columnCount++);
//				cell.setCellValue(list.get(0).get("6").toString());
//				cell = row.createCell(columnCount++);
//				cell.setCellValue(list.get(0).get("7").toString());
//				cell = row.createCell(columnCount++);
//				
//			}
			
		
		
		
	}
	
	public List<Map<String, Object>> getpivotdata2(HttpServletRequest request) {
		String orgtype=request("orgtype");
		String aid=request("admid");
		String pid=request("pid");
		String did=request("did");
		String munc=request("munc");
		String oid=request("ofc");
		String hfid=request("hfid");
		String type=request("type");
		
		String filename = "healthworkforce.csv";
		String sql="";
		if(!type.equals("")) {
		if(type.equals("5")) {
			sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=4  group by tbl_darbandi.post";
		}else {
			sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=1 and  tbl_workforce.admlvl="+type+ "  group by tbl_darbandi.post";
		}
		}else {
			sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,sum(dworking) as dworking,sum(kworking) as kworking,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id   group by tbl_darbandi.post";
		}
		
		
		
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
		
		
		
		
		
		
		   Map<Integer, Object> map;
	        map = new HashMap<Integer, Object>();
	        List<Map<String, Object>> lists = new ArrayList<>();
	        for (Tuple t : admlvl) {
	        	List<Map<String, Object>> list = new ArrayList<>();
		        Map<String, Object> mapadmlvl = new HashMap<>();
				for(int i=1;i<=8;i++) {
					mapadmlvl.put(i+"",getKararPivot(t.get("post").toString(),i,type));
					
				}
				list.add(mapadmlvl);
				Map<String, Object> maps = new HashMap<>();
				maps.put("postname", t.get("postname"));
				maps.put("sdarbandi", t.get("sdarbandi"));
				maps.put("dworking", t.get("dworking"));
				maps.put("kworking", t.get("kworking"));
				maps.put("tworking", Integer.parseInt(t.get("kworking").toString())+Integer.parseInt(t.get("dworking").toString()));
				maps.put("1", list.get(0).get("1").toString());
				maps.put("8", list.get(0).get("8").toString());
				maps.put("2", list.get(0).get("2").toString());
				maps.put("3", list.get(0).get("3").toString());
				maps.put("4", list.get(0).get("4").toString());
				maps.put("5", list.get(0).get("5").toString());
				maps.put("6", list.get(0).get("6").toString());
				maps.put("7", list.get(0).get("7").toString());
				lists.add(maps);
			}
	        
	        return lists;
	        

			
		
		
		
	}
	
	private Object getKararPivot(String post, int i,String type) {
		String sql="";
		if(type.equals("")) {
			  sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_darbandi.post= "+post+" and tbl_employee.emptype="+i;
		}else {
			if(type.equals("5")) {
				  sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.orgtype=4 and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+i;
			}else {
				  sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ " and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+i;
			}
		}
		
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
		
		return admlvl.get(0).get("karar");
	}

	private Object getKarar(String post, String aid, String pid, String oid,int emptype,String orgtype,String did,String munc,String hfid) {
		String sql="";
		if("1".equals(orgtype)) {
			if(aid.equals("")) {
				 sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.orgtype=1 and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
			}
			if(aid.equals("1")) {
				if(oid.equals("0")) {
					 sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=1 and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
				}else {
					
					 sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=1 and  tbl_darbandi.post= "+post+" and tbl_workforce.officeid="+oid+" and tbl_employee.emptype="+emptype;
				}
			}
			
			if(aid.equals("2")) {
				if(pid.equals("0")) {
					sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=2 and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
				}else {
					if(oid.equals("0")) {
						sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
					}else {
						sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=2 and tbl_workforce.officeid="+oid+" and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
					}
				}
			}
			
			
			if(aid.equals("4")) {
				if(pid.equals("0")) {
					 sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=4 and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
				}else {
					if(did.equals("0")) {
						 sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=4 and tbl_darbandi.post= "+post+" and tbl_workforce.provinceid="+pid+" and tbl_employee.emptype="+emptype;
					}else {
						if(munc.equals("0")) {
							sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=4 and tbl_darbandi.post= "+post+" and tbl_workforce.districtid="+did+" and tbl_employee.emptype="+emptype;
						}else {
							sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=4 and tbl_darbandi.post= "+post+" and tbl_workforce.muncid="+munc+" and tbl_employee.emptype="+emptype;
						}
					}
					 
				}
			}
		}else {
			if(pid.equals("0")) {
				sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.orgtype=4 and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
			}else {
				if(did.equals("0")) {
					sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.orgtype=4 and tbl_workforce.provinceid="+pid+" and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
					
				}else {
					if(munc.equals("0")) {
						sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.orgtype=4 and tbl_workforce.districtid="+did+" and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
					}else {
						if(hfid.equals("0")) {
							sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.orgtype=4 and tbl_workforce.palika="+munc+" and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
						}else {
							sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.orgtype=4 and tbl_workforce.org="+hfid+" and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
						}
					}
				}
			}
		}

		
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
		
		return admlvl.get(0).get("karar");
	}



	public List<Tuple> gettabledata(HttpServletRequest request) {
		String orgtype=request("orgtype");
		String aid=request("admid");
		String pid=request("pid");
		String did=request("did");
		String munc=request("munc");
		String oid=request("ofc");
		String hfid=request("hfid");
		String filename = "healthworkforce.csv";
		String sql="";
		
		try {

			if("1".equals(orgtype)) {
				if(aid.equals("")) {
					
					if(pid.equals("200")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=1";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=1 group by tbl_workforce.palika";
						
//						sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=4 group by tbl_darbandi.post ";
					}else if(pid.equals("0")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=1";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=1 group by tbl_workforce.provinceid";
						
					}else if(pid.equals("100")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=1";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=1 group by tbl_workforce.districtid";
						
					}
				}
				else if(aid.equals("1")) {
					if(oid.equals("0")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=1";				
						Tuple t = db.getSingleResult(q1);
						sql = "select tbl_office.namenp as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join tbl_office on tbl_office.id=tbl_workforce.officeid where tbl_workforce.admlvl=1 group by tbl_workforce.officeid";
						
//						 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=1 group by tbl_darbandi.post";
					}else {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+"";				
						Tuple t = db.getSingleResult(q1);
						sql = "select tbl_office.namenp as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join tbl_office on tbl_office.id=tbl_workforce.officeid where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+" ";
//						 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+" group by tbl_darbandi.post";
					}
				}else if(aid.equals("2")) {
				if(pid.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id  join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 group by tbl_workforce.provinceid";
					}else if(pid.equals("100")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id  join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.admlvl=2 group by tbl_workforce.districtid";
						
					}else if(pid.equals("200")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id  join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.admlvl=2 group by tbl_workforce.palika";
						
					}
				else {
					if(oid.equals("0")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+"";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+"";
						
						
//						sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
					}else {
						
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" and  tbl_workforce.officeid="+oid+"";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" and  tbl_workforce.officeid="+oid+"";
//						sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.officeid="+oid+" group by tbl_darbandi.post";
					}
				}
			}else {
				if(pid.equals("200")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4";				
					Tuple t = db.getSingleResult(q1);
//					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4 group by tbl_workforce.provinceid";
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4  group by tbl_workforce.muncid";
//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 group by tbl_darbandi.post";
			
				}else if(pid.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4";				
					Tuple t = db.getSingleResult(q1);
//					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4 group by tbl_workforce.provinceid";
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4  group by tbl_workforce.provinceid";

				}else if(pid.equals("100")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4";				
					Tuple t = db.getSingleResult(q1);
//					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4 group by tbl_workforce.provinceid";
					sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.admlvl=4  group by tbl_workforce.districtid";

				}
					
//					if(did.equals("0")) {
//						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4";				
//						Tuple t = db.getSingleResult(q1);
////						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4 group by tbl_workforce.provinceid";
//						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4  group by tbl_workforce.muncid";
////						 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 group by tbl_darbandi.post";
//					}else {
//						if(munc.equals("0")) {
//							String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+"";				
//							Tuple t = db.getSingleResult(q1);
//							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.muncid";
	//
////							 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_darbandi.post";
//						}else {
//							String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+"";				
//							Tuple t = db.getSingleResult(q1);
//							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_workforce.muncid";
	//
////							 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_darbandi.post";
//						}
//					}
//				
				
			else {
				if(did.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+" group by tbl_workforce.districtid";

//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
				}else {
					if(munc.equals("0")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+"";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.muncid";

//						 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_darbandi.post";
					}else {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+"";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+"";

						
//						 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_darbandi.post";
					}
					
				}
				
				
			}
		}
			}else {
				if(pid.equals("200")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4 group by tbl_workforce.palika";
					
//					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=4 group by tbl_darbandi.post ";
				}else if(pid.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=4 group by tbl_workforce.provinceid";
					
				}else if(pid.equals("100")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=4 group by tbl_workforce.districtid";
					
				}
				else {
					if(did.equals("0")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.provinceid="+pid+"";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=4 and tbl_workforce.provinceid="+pid+" group by tbl_workforce.districtid";
						
					}else {
						if(munc.equals("0")) {
							String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.districtid="+did+"";				
							Tuple t = db.getSingleResult(q1);
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.palika";
							
						}else {
							if(hfid.equals("0")) {
								String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.palika="+munc+"";				
								Tuple t = db.getSingleResult(q1);
								sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4 and tbl_workforce.palika="+munc+" group by tbl_workforce.palika";
								
							}else {
								String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.org="+hfid+"";				
								Tuple t = db.getSingleResult(q1);
								sql = "select hfregistry.hf_name as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join hfregistry on hfregistry.id=tbl_workforce.org where tbl_workforce.orgtype=4 and tbl_workforce.org="+hfid+" group by tbl_workforce.org";
								
							}
						}
					}
				}
			}
			
			String sq2="select namenp,id from tbl_post order by level";
			List<Tuple> posts = db.getResultList(sq2, Arrays.asList());
			List<Map<String, Object>> lists = new ArrayList<>();
			Map<String, Object> mappost = new HashMap<>();
			for (Tuple t : posts) {	
				mappost.put(t.get("id").toString(), t.get("namenp"));
			}

			List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
//			System.out.println(admlvl);
			List<TupleElement<?>> ltpe;
			List<String> flds = new ArrayList<>();
			if(admlvl==null) {
				return null;
			}
			
			if(!admlvl.isEmpty()) {
			 ltpe = admlvl.get(0).getElements();
			 
				for(TupleElement tp : ltpe) {
					if(tp.getAlias().startsWith("d")) {
						flds.add(tp.getAlias().substring(1));
					}
				}
			}else {
				ltpe=null;
			
			}
		
			
				return admlvl;
		
			
		} catch (Exception e) {
			return null;
		}
		
		
		
		
	}

	public List<String> getField(HttpServletRequest request) {
		String orgtype=request("orgtype");
		String aid=request("admid");
		String pid=request("pid");
		String did=request("did");
		String munc=request("munc");
		String oid=request("ofc");
		String hfid=request("hfid");
		String filename = "healthworkforce.csv";
		String sql="";
		
		if("1".equals(orgtype)) {
			if(aid.equals("")) {
				
				if(pid.equals("200")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=1";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=1 group by tbl_workforce.palika";
					
//					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=4 group by tbl_darbandi.post ";
				}else if(pid.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=1";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=1 group by tbl_workforce.provinceid";
					
				}else if(pid.equals("100")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=1";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=1 group by tbl_workforce.districtid";
					
				}
			}
			else if(aid.equals("1")) {
				if(oid.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=1";				
					Tuple t = db.getSingleResult(q1);
					sql = "select tbl_office.namenp as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join tbl_office on tbl_office.id=tbl_workforce.officeid where tbl_workforce.admlvl=1 group by tbl_workforce.officeid";
					
//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=1 group by tbl_darbandi.post";
				}else {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select tbl_office.namenp as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join tbl_office on tbl_office.id=tbl_workforce.officeid where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+" ";
//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+" group by tbl_darbandi.post";
				}
			}else if(aid.equals("2")) {
			if(pid.equals("0")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2";				
				Tuple t = db.getSingleResult(q1);
				sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id  join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 group by tbl_workforce.provinceid";
				}else if(pid.equals("100")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id  join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.admlvl=2 group by tbl_workforce.districtid";
					
				}else if(pid.equals("200")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id  join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.admlvl=2 group by tbl_workforce.palika";
					
				}
			else {
				if(oid.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+"";
					
					
//					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
				}else {
					
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" and  tbl_workforce.officeid="+oid+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" and  tbl_workforce.officeid="+oid+"";
//					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.officeid="+oid+" group by tbl_darbandi.post";
				}
			}
		}else {
			if(pid.equals("200")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4";				
				Tuple t = db.getSingleResult(q1);
//				sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4 group by tbl_workforce.provinceid";
				sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4  group by tbl_workforce.muncid";
//				 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 group by tbl_darbandi.post";
		
			}else if(pid.equals("0")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4";				
				Tuple t = db.getSingleResult(q1);
//				sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4 group by tbl_workforce.provinceid";
				sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4  group by tbl_workforce.provinceid";

			}else if(pid.equals("100")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4";				
				Tuple t = db.getSingleResult(q1);
//				sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4 group by tbl_workforce.provinceid";
				sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.admlvl=4  group by tbl_workforce.districtid";

			}
				
//				if(did.equals("0")) {
//					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4";				
//					Tuple t = db.getSingleResult(q1);
////					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4 group by tbl_workforce.provinceid";
//					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4  group by tbl_workforce.muncid";
////					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 group by tbl_darbandi.post";
//				}else {
//					if(munc.equals("0")) {
//						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+"";				
//						Tuple t = db.getSingleResult(q1);
//						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.muncid";
//
////						 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_darbandi.post";
//					}else {
//						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+"";				
//						Tuple t = db.getSingleResult(q1);
//						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_workforce.muncid";
//
////						 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_darbandi.post";
//					}
//				}
//			
			
		else {
			if(did.equals("0")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+"";				
				Tuple t = db.getSingleResult(q1);
				sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+" group by tbl_workforce.districtid";

//				 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
			}else {
				if(munc.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.muncid";

//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_darbandi.post";
				}else {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+"";

					
//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_darbandi.post";
				}
				
			}
			
			
		}
	}
		}else {
			if(pid.equals("200")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4";				
				Tuple t = db.getSingleResult(q1);
				sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4 group by tbl_workforce.palika";
				
//				sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=4 group by tbl_darbandi.post ";
			}else if(pid.equals("0")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4";				
				Tuple t = db.getSingleResult(q1);
				sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=4 group by tbl_workforce.provinceid";
				
			}else if(pid.equals("100")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4";				
				Tuple t = db.getSingleResult(q1);
				sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=4 group by tbl_workforce.districtid";
				
			}
			else {
				if(did.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.provinceid="+pid+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=4 and tbl_workforce.provinceid="+pid+" group by tbl_workforce.districtid";
					
				}else {
					if(munc.equals("0")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.districtid="+did+"";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.palika";
						
					}else {
						if(hfid.equals("0")) {
							String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.palika="+munc+"";				
							Tuple t = db.getSingleResult(q1);
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4 and tbl_workforce.palika="+munc+" group by tbl_workforce.palika";
							
						}else {
							String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.org="+hfid+"";				
							Tuple t = db.getSingleResult(q1);
							sql = "select hfregistry.hf_name as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join hfregistry on hfregistry.id=tbl_workforce.org where tbl_workforce.orgtype=4 and tbl_workforce.org="+hfid+" group by tbl_workforce.org";
							
						}
					}
				}
			}
		}
		String sq2="select namenp,id from tbl_post order by level";
		List<Tuple> posts = db.getResultList(sq2, Arrays.asList());
		List<Map<String, Object>> lists = new ArrayList<>();
		Map<String, Object> mappost = new HashMap<>();
		for (Tuple t : posts) {	
			mappost.put(t.get("id").toString(), t.get("namenp"));
		}
//		Map<String, Object> data= (Map<String, Object>) db.getResultList(sql, Arrays.asList());
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
//		System.out.println(admlvl.get(0).toString());
		List<TupleElement<?>> ltpe;
		List<String> flds = new ArrayList<>();
		if(admlvl==null) {
			return null;
		}
		if(!admlvl.isEmpty()) {
		 ltpe = admlvl.get(0).getElements();
		 
			for(TupleElement tp : ltpe) {
				if(tp.getAlias().startsWith("d")) {
					flds.add(tp.getAlias().substring(1));
				}
			}
		}else {
			ltpe=null;
//			flds=null;
		}
		return flds;
	}

	public Map<String, Object> getPost(HttpServletRequest request) {
		String orgtype=request("orgtype");
		String aid=request("admid");
		String pid=request("pid");
		String did=request("did");
		String munc=request("munc");
		String oid=request("ofc");
		String hfid=request("hfid");
		String filename = "healthworkforce.csv";
		String sql="";
		

		if("1".equals(orgtype)) {
			if(aid.equals("")) {
				
				if(pid.equals("200")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=1";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=1 group by tbl_workforce.palika";
					
//					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=4 group by tbl_darbandi.post ";
				}else if(pid.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=1";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=1 group by tbl_workforce.provinceid";
					
				}else if(pid.equals("100")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=1";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=1 group by tbl_workforce.districtid";
					
				}
			}
			else if(aid.equals("1")) {
				if(oid.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=1";				
					Tuple t = db.getSingleResult(q1);
					sql = "select tbl_office.namenp as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join tbl_office on tbl_office.id=tbl_workforce.officeid where tbl_workforce.admlvl=1 group by tbl_workforce.officeid";
					
//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=1 group by tbl_darbandi.post";
				}else {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select tbl_office.namenp as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join tbl_office on tbl_office.id=tbl_workforce.officeid where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+" ";
//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+" group by tbl_darbandi.post";
				}
			}else if(aid.equals("2")) {
			if(pid.equals("0")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2";				
				Tuple t = db.getSingleResult(q1);
				sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id  join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 group by tbl_workforce.provinceid";
				}else if(pid.equals("100")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id  join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.admlvl=2 group by tbl_workforce.districtid";
					
				}else if(pid.equals("200")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id  join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.admlvl=2 group by tbl_workforce.palika";
					
				}
			else {
				if(oid.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+"";
					
					
//					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
				}else {
					
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" and  tbl_workforce.officeid="+oid+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" and  tbl_workforce.officeid="+oid+"";
//					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.officeid="+oid+" group by tbl_darbandi.post";
				}
			}
		}else {
			if(pid.equals("200")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4";				
				Tuple t = db.getSingleResult(q1);
//				sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4 group by tbl_workforce.provinceid";
				sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4  group by tbl_workforce.muncid";
//				 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 group by tbl_darbandi.post";
		
			}else if(pid.equals("0")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4";				
				Tuple t = db.getSingleResult(q1);
//				sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4 group by tbl_workforce.provinceid";
				sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4  group by tbl_workforce.provinceid";

			}else if(pid.equals("100")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4";				
				Tuple t = db.getSingleResult(q1);
//				sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4 group by tbl_workforce.provinceid";
				sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.admlvl=4  group by tbl_workforce.districtid";

			}
				
//				if(did.equals("0")) {
//					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4";				
//					Tuple t = db.getSingleResult(q1);
////					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=4 group by tbl_workforce.provinceid";
//					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4  group by tbl_workforce.muncid";
////					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 group by tbl_darbandi.post";
//				}else {
//					if(munc.equals("0")) {
//						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+"";				
//						Tuple t = db.getSingleResult(q1);
//						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.muncid";
//
////						 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_darbandi.post";
//					}else {
//						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+"";				
//						Tuple t = db.getSingleResult(q1);
//						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_workforce.muncid";
//
////						 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_darbandi.post";
//					}
//				}
//			
			
		else {
			if(did.equals("0")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+"";				
				Tuple t = db.getSingleResult(q1);
				sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+" group by tbl_workforce.districtid";

//				 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
			}else {
				if(munc.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.muncid";

//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_darbandi.post";
				}else {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+"";

					
//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_darbandi.post";
				}
				
			}
			
			
		}
	}
		}else {
			if(pid.equals("200")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4";				
				Tuple t = db.getSingleResult(q1);
				sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4 group by tbl_workforce.palika";
				
//				sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=4 group by tbl_darbandi.post ";
			}else if(pid.equals("0")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4";				
				Tuple t = db.getSingleResult(q1);
				sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=4 group by tbl_workforce.provinceid";
				
			}else if(pid.equals("100")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4";				
				Tuple t = db.getSingleResult(q1);
				sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=4 group by tbl_workforce.districtid";
				
			}
			else {
				if(did.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.provinceid="+pid+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=4 and tbl_workforce.provinceid="+pid+" group by tbl_workforce.districtid";
					
				}else {
					if(munc.equals("0")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.districtid="+did+"";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.palika";
						
					}else {
						if(hfid.equals("0")) {
							String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.palika="+munc+"";				
							Tuple t = db.getSingleResult(q1);
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4 and tbl_workforce.palika="+munc+" group by tbl_workforce.palika";
							
						}else {
							String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.org="+hfid+"";				
							Tuple t = db.getSingleResult(q1);
							sql = "select hfregistry.hf_name as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join hfregistry on hfregistry.id=tbl_workforce.org where tbl_workforce.orgtype=4 and tbl_workforce.org="+hfid+" group by tbl_workforce.org";
							
						}
					}
				}
			}
		}
		String sq2="select namenp,id from tbl_post order by level";
		List<Tuple> posts = db.getResultList(sq2, Arrays.asList());
		List<Map<String, Object>> lists = new ArrayList<>();
		Map<String, Object> mappost = new HashMap<>();
		for (Tuple t : posts) {	
			mappost.put(t.get("id").toString(), t.get("namenp"));
		}
//		Map<String, Object> data= (Map<String, Object>) db.getResultList(sql, Arrays.asList());
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
//		System.out.println(admlvl.get(0).toString());
		List<TupleElement<?>> ltpe;
		List<String> flds = new ArrayList<>();
//		if(!admlvl.isEmpty()) {
//		 ltpe = admlvl.get(0).getElements();
//		 
//			for(TupleElement tp : ltpe) {
//				if(tp.getAlias().startsWith("d")) {
//					flds.add(tp.getAlias().substring(1));
//				}
//			}
//		}else {
//			ltpe=null;
////			flds=null;
//		}
//		System.out.println(mappost);
		return mappost;
	}

	public List<Tuple> getpivotdata(HttpServletRequest request) {
		String filter=request("filter");
		String type=request("type");
		String sql="";
		
		String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id ";				
		Tuple t = db.getSingleResult(q1);
		if(request("row").equals("province") && request("column").equals("post")) {
			if(filter.equals("") && type.equals("")) {
			
			sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid  group by tbl_workforce.provinceid";
			}else {
				if(!filter.equals("") && !type.equals("")) {
					if(type.equals("5")) {
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=4 and tbl_workforce.provinceid="+filter+ "  group by tbl_workforce.provinceid";
					}else {
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ " and tbl_workforce.provinceid="+filter+ "  group by tbl_workforce.provinceid";
					}
					
					
				}
				
				if(filter.equals("") && !type.equals("")) {
					if(type.equals("5")) {
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=4   group by tbl_workforce.provinceid";
					}else {
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ "  group by tbl_workforce.provinceid";
					}				
					
				}
				if(!filter.equals("") && type.equals("")) {
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where  tbl_workforce.provinceid="+filter+ "  group by tbl_workforce.provinceid";			
					
				}
			}
		}
		if(request("row").equals("district") && request("column").equals("post")) {
			if(filter.equals("") && type.equals("")) {
				sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid  group by tbl_workforce.districtid";
				
				}else {
					if(!filter.equals("") && !type.equals("")) {
						if(type.equals("5")) {
							sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=4 and tbl_workforce.districtid="+filter+ "  group by tbl_workforce.districtid";
						}else {
							sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ " and tbl_workforce.districtid="+filter+ "  group by tbl_workforce.districtid";
						}
						
						
					}
					
					if(filter.equals("") && !type.equals("")) {
						if(type.equals("5")) {
							sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=4   group by tbl_workforce.districtid";
						}else {
							sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ "   group by tbl_workforce.districtid";
						}				
						
					}
					if(!filter.equals("") && type.equals("")) {
						sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.districtid="+filter+ "  group by tbl_workforce.districtid";			
						
					}
				}
			
			
			
		}
		if(request("row").equals("palika") && request("column").equals("post")) {
			if(filter.equals("") && type.equals("")) {
				sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika  group by tbl_workforce.palika";
				
				}else {
					if(!filter.equals("") && !type.equals("")) {
						if(type.equals("5")) {
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4 and tbl_workforce.palika="+filter+ "  group by tbl_workforce.palika";
						}else {
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika  where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ " and tbl_workforce.palika="+filter+ "   group by tbl_workforce.palika";
						}				
						
					}
					
					if(filter.equals("") && !type.equals("")) {
						if(type.equals("5")) {
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4   group by tbl_workforce.palika";
						}else {
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika  where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ "  group by tbl_workforce.palika";
						}				
						
					}
					if(!filter.equals("") && type.equals("")) {
						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where  tbl_workforce.palika="+filter+ "  group by tbl_workforce.palika";			
						
					}
				}
			
			
			
		}
		String sq2="select namenp,id from tbl_post order by level";
		List<Tuple> posts = db.getResultList(sq2, Arrays.asList());
		List<Map<String, Object>> lists = new ArrayList<>();
		Map<String, Object> mappost = new HashMap<>();
		for (Tuple tt : posts) {	
			mappost.put(tt.get("id").toString(), tt.get("namenp"));
		}

		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
//		System.out.println(admlvl);
		List<TupleElement<?>> ltpe;
		List<String> flds = new ArrayList<>();
		if(admlvl==null) {
			return null;
		}
		
		if(!admlvl.isEmpty()) {
		 ltpe = admlvl.get(0).getElements();
		 
			for(TupleElement tp : ltpe) {
				if(tp.getAlias().startsWith("d")) {
					flds.add(tp.getAlias().substring(1));
				}
			}
		}else {
			ltpe=null;
		
		}
	
		
			return admlvl;
		
	}

	public List<String> getpivotField(HttpServletRequest request) {
		String filter=request("filter");
		String type=request("type");
		String sql="";
		
		String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id ";				
		Tuple t = db.getSingleResult(q1);
		if(request("row").equals("province") && request("column").equals("post")) {
			if(filter.equals("") && type.equals("")) {
			
			sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid  group by tbl_workforce.provinceid";
			}else {
				if(!filter.equals("") && !type.equals("")) {
					if(type.equals("5")) {
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=4 and tbl_workforce.provinceid="+filter+ "  group by tbl_workforce.provinceid";
					}else {
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ " and tbl_workforce.provinceid="+filter+ "  group by tbl_workforce.provinceid";
					}
					
					
				}
				
				if(filter.equals("") && !type.equals("")) {
					if(type.equals("5")) {
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=4   group by tbl_workforce.provinceid";
					}else {
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ "  group by tbl_workforce.provinceid";
					}				
					
				}
				if(!filter.equals("") && type.equals("")) {
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where  tbl_workforce.provinceid="+filter+ "  group by tbl_workforce.provinceid";			
					
				}
			}
		}
		if(request("row").equals("district") && request("column").equals("post")) {
			if(filter.equals("") && type.equals("")) {
				sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid  group by tbl_workforce.districtid";
				
				}else {
					if(!filter.equals("") && !type.equals("")) {
						if(type.equals("5")) {
							sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=4 and tbl_workforce.districtid="+filter+ "  group by tbl_workforce.districtid";
						}else {
							sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ " and tbl_workforce.districtid="+filter+ "  group by tbl_workforce.districtid";
						}
						
						
					}
					
					if(filter.equals("") && !type.equals("")) {
						if(type.equals("5")) {
							sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=4   group by tbl_workforce.districtid";
						}else {
							sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ "   group by tbl_workforce.districtid";
						}				
						
					}
					if(!filter.equals("") && type.equals("")) {
						sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.districtid="+filter+ "  group by tbl_workforce.districtid";			
						
					}
				}
			
			
			
		}
		if(request("row").equals("palika") && request("column").equals("post")) {
			if(filter.equals("") && type.equals("")) {
				sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika  group by tbl_workforce.palika";
				
				}else {
					if(!filter.equals("") && !type.equals("")) {
						if(type.equals("5")) {
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4 and tbl_workforce.palika="+filter+ "  group by tbl_workforce.palika";
						}else {
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika  where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ " and tbl_workforce.palika="+filter+ "   group by tbl_workforce.palika";
						}				
						
					}
					
					if(filter.equals("") && !type.equals("")) {
						if(type.equals("5")) {
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4   group by tbl_workforce.palika";
						}else {
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika  where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ "  group by tbl_workforce.palika";
						}				
						
					}
					if(!filter.equals("") && type.equals("")) {
						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where  tbl_workforce.palika="+filter+ "  group by tbl_workforce.palika";			
						
					}
				}
			
			
			
		}
		String sq2="select namenp,id from tbl_post order by level";
		List<Tuple> posts = db.getResultList(sq2, Arrays.asList());
		List<Map<String, Object>> lists = new ArrayList<>();
		Map<String, Object> mappost = new HashMap<>();
		for (Tuple tt : posts) {	
			mappost.put(tt.get("id").toString(), tt.get("namenp"));
		}

		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
		List<TupleElement<?>> ltpe;
		List<String> flds = new ArrayList<>();
		if(admlvl==null) {
			return null;
		}
		if(!admlvl.isEmpty()) {
		 ltpe = admlvl.get(0).getElements();
		 
			for(TupleElement tp : ltpe) {
				if(tp.getAlias().startsWith("d")) {
					flds.add(tp.getAlias().substring(1));
				}
			}
		}else {
			ltpe=null;
		}
		return flds;
	}

	public Map<String, Object> getpivotPost(HttpServletRequest request) {
		String filter=request("filter");
		String type=request("type");
		String sql="";
		
		String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then dworking else 0 end) AS ',CONCAT('''w',tp.id,'''') ) ORDER BY tp.id) AS `wcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then kworking else 0 end) AS ',CONCAT('''n',tp.id,'''') ) ORDER BY tp.id) AS `ncases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id ";				
		Tuple t = db.getSingleResult(q1);
		if(request("row").equals("province") && request("column").equals("post")) {
			if(filter.equals("") && type.equals("")) {
			
			sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid  group by tbl_workforce.provinceid";
			}else {
				if(!filter.equals("") && !type.equals("")) {
					if(type.equals("5")) {
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=4 and tbl_workforce.provinceid="+filter+ "  group by tbl_workforce.provinceid";
					}else {
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ " and tbl_workforce.provinceid="+filter+ "  group by tbl_workforce.provinceid";
					}
					
					
				}
				
				if(filter.equals("") && !type.equals("")) {
					if(type.equals("5")) {
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=4   group by tbl_workforce.provinceid";
					}else {
						sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ "  group by tbl_workforce.provinceid";
					}				
					
				}
				if(!filter.equals("") && type.equals("")) {
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where  tbl_workforce.provinceid="+filter+ "  group by tbl_workforce.provinceid";			
					
				}
			}
		}
		if(request("row").equals("district") && request("column").equals("post")) {
			if(filter.equals("") && type.equals("")) {
				sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid  group by tbl_workforce.districtid";
				
				}else {
					if(!filter.equals("") && !type.equals("")) {
						if(type.equals("5")) {
							sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=4 and tbl_workforce.districtid="+filter+ "  group by tbl_workforce.districtid";
						}else {
							sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ " and tbl_workforce.districtid="+filter+ "  group by tbl_workforce.districtid";
						}
						
						
					}
					
					if(filter.equals("") && !type.equals("")) {
						if(type.equals("5")) {
							sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=4   group by tbl_workforce.districtid";
						}else {
							sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ "   group by tbl_workforce.districtid";
						}				
						
					}
					if(!filter.equals("") && type.equals("")) {
						sql = "select admin_district.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_district on admin_district.districtid=tbl_workforce.districtid where tbl_workforce.districtid="+filter+ "  group by tbl_workforce.districtid";			
						
					}
				}
			
			
			
		}
		if(request("row").equals("palika") && request("column").equals("post")) {
			if(filter.equals("") && type.equals("")) {
				sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika  group by tbl_workforce.palika";
				
				}else {
					if(!filter.equals("") && !type.equals("")) {
						if(type.equals("5")) {
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4 and tbl_workforce.palika="+filter+ "  group by tbl_workforce.palika";
						}else {
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika  where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ " and tbl_workforce.palika="+filter+ "   group by tbl_workforce.palika";
						}				
						
					}
					
					if(filter.equals("") && !type.equals("")) {
						if(type.equals("5")) {
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where tbl_workforce.orgtype=4   group by tbl_workforce.palika";
						}else {
							sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika  where tbl_workforce.orgtype=1 and tbl_workforce.admlvl="+type+ "  group by tbl_workforce.palika";
						}				
						
					}
					if(!filter.equals("") && type.equals("")) {
						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+","+t.get("wcases")+","+t.get("ncases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.palika where  tbl_workforce.palika="+filter+ "  group by tbl_workforce.palika";			
						
					}
				}
			
			
			
		}
		String sq2="select namenp,id from tbl_post order by level";
		List<Tuple> posts = db.getResultList(sq2, Arrays.asList());
		List<Map<String, Object>> lists = new ArrayList<>();
		Map<String, Object> mappost = new HashMap<>();
		for (Tuple tt : posts) {	
			mappost.put(tt.get("id").toString(), tt.get("namenp"));
		}

		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
		List<TupleElement<?>> ltpe;
		List<String> flds = new ArrayList<>();

		return mappost;
		
	}
}
