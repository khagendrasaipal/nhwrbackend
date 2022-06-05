package org.saipal.workforce.contorllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.persistence.Tuple;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.fmisutil.ApplicationContextProvider;
import org.saipal.fmisutil.util.DB;
import org.saipal.fmisutil.util.HttpRequest;
import org.saipal.workforce.omed.DocumentStorageService;
import org.saipal.workforce.omed.OMED;
import org.saipal.workforce.omed.OmedBuilder;
import org.saipal.workforce.omed.OmedParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.mysql.cj.xdevapi.JsonArray;

@Controller
@RequestMapping("/")
public class MainController {
	
	@Autowired
	DocumentStorageService ds;
	
	@Autowired
	Environment env;
	
	@Autowired
	ClientCreateTestService createClient;
	
	@Autowired
	DB db;
	
	
	@ResponseBody
	@GetMapping("/test/token")
	public String index(HttpServletRequest request) {
		String baseUrl = "http://localhost:8011/token";
		try {
			HttpRequest hreq = new HttpRequest();
			hreq.setParam("client_id", env.getProperty("oascentral.client_id"));
			hreq.setParam("client_secret", env.getProperty("oascentral.client_secret"));
			hreq.setParam("code", env.getProperty("oascentral.code"));
			hreq.setParam("grant_type", "authorization_code");
			JSONObject response = hreq.post(baseUrl);
			if (response.getInt("status_code") == 200) {
				JSONObject resp = response.getJSONObject("data");
				System.out.println(resp.toString());
				//return resp.toString();
			}
			System.out.println(response);
		} catch (JSONException e) {
			e.printStackTrace();
			System.out.println("bye");
			return "errr";
		}
		return "I'm working fine";
	}
	
	@ResponseBody
	@GetMapping("/test/create")
	public String create(HttpServletRequest request) {
		String accessToken="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkZXZpbnN0YW5jZSIsImV4cCI6MTY0NDkwMTgyMiwiaWF0IjoxNjQ0ODE1NDIyLCJqdGkiOiI5OTY0NDgzMjQ2ODY5NzExNiJ9.OvpFhclYRbH5ZKbXKJI6f3Thf4xFiRUEAQqvihm4zMM";
		String baseUrl = "http://localhost:8011/sync/table";
		String sql = "select * from admin_org_strs where orgidint=?";
		List<Tuple> orgs = db.getResultList(sql, Arrays.asList("99550740053229891"));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!orgs.isEmpty()) {
			for (Tuple t : orgs) {
				Map<String, Object> mapOrg = new HashMap<>();
				
				mapOrg.put("orgidint", t.get("orgidint"));
				mapOrg.put("parentorgid", t.get("parentorgid"));
				mapOrg.put("code", t.get("code"));
				mapOrg.put("orgnamenp", t.get("orgnamenp"));
				mapOrg.put("orgnameen", t.get("orgnameen"));
				mapOrg.put("orgnamelc", t.get("orgnamelc"));
				mapOrg.put("adminlevel", t.get("adminlevel"));
				mapOrg.put("adminid", t.get("adminid"));
				mapOrg.put("orglevelid", t.get("orglevelid"));
				mapOrg.put("provinceid", t.get("provinceid"));
				mapOrg.put("districtid", t.get("districtid"));
				mapOrg.put("vcid", t.get("vcid"));
				mapOrg.put("wardno", t.get("wardno"));
				mapOrg.put("approved", t.get("approved"));
				mapOrg.put("disabled", t.get("disabled"));
				mapOrg.put("enterby", t.get("enterby"));
				
				list.add(mapOrg);
			}
		}
		
		try {
			JSONObject jo = new JSONObject();
			jo.put("table", "admin_org_strs");
			jo.put("action", "create");
			jo.put("rows", list);
			JSONArray ja = new JSONArray();
			ja.put(jo);

			HttpRequest hreq = new HttpRequest();
			hreq.setParam("data",ja.toString());
			
			JSONObject response = hreq.setHeader("Authorization", "Bearer " + accessToken).post(baseUrl);
			
			if (response.getInt("status_code") == 200) {
				JSONObject resp = response.getJSONObject("data");
				System.out.println(resp.toString());
				return resp.toString();
			}
			System.out.println(response);
		} catch (JSONException e) {
			e.printStackTrace();
			System.out.println("bye");
			return "errr";
		}
		return "I'm working fine";
	}
	
	@GetMapping("/test/dashboard")
	public String dash(Model model,HttpServletRequest request) throws JSONException {
//		System.out.println(request("orgid"));
		List<Tuple> org = createClient.getOrgs(request);
		List<Tuple> ward = createClient.getWards(request);
		List<Tuple> chart=createClient.getChartConfig(request);
		List<Map<String, Object>> weather=createClient.getWeatherInfo(request);
		List<Tuple> updates = createClient.getUpdates(request);
		String hf=createClient.getHfCount(request);
		List<Tuple> census=createClient.getCensusData(request);
		hf = hf.replace("[","");
		hf = hf.replace("]","");
		hf=hf.replace("\"", "");
		String wards="0";
		String update="स्वास्थ्य ड्यासबोर्ड मुख्य रूपले ग्राफिकल चार्टहरू, नक्साहरू र तथ्याङ्कहरूको सहायताले स्वास्थ्य संकेतकहरूको प्रगतिको निरीक्षण गर्न प्रयोग हुन्छ । सान्दर्भिक चार्टहरू, नक्साहरू र तथ्याङ्कहरू पनि ड्यासबोर्डबाट डाउनलोड गर्न सकिन्छ ।";
		if (!ward.isEmpty()) {
			wards=ward.get(0).get("numberofward").toString();
		}
		if (!updates.isEmpty()) {
			update=updates.get(0).get("value").toString();
		}
//		System.out.println(weather.get(0).get("temp"));
		String orgname=org.get(0).get("name").toString();
		model.addAttribute("orgname", orgname);
		model.addAttribute("chart",chart);
		model.addAttribute("ward",wards);
		model.addAttribute("temp",weather.get(0).get("temp"));
		model.addAttribute("type",weather.get(0).get("type"));
		model.addAttribute("desc",weather.get(0).get("desc"));
		model.addAttribute("icon",weather.get(0).get("icon"));
		model.addAttribute("hf",hf);
		model.addAttribute("orgid",org.get(0).get("id").toString());
		model.addAttribute("update",update);
		model.addAttribute("census",census.get(0));
		return "test/dashboard";
	}
	
	@GetMapping("/registry/getData")
	public String webs(Model model,HttpServletRequest request) throws JSONException {
		
		List<Tuple> chart=createClient.gettabledata(request);
		List<String> field=createClient.getField(request);
		Map<String, Object> post=createClient.getPost(request);
		System.out.println(post.get("800"));
		
		model.addAttribute("chart",chart);
		model.addAttribute("field",field);
		model.addAttribute("post",post);
		
		return "test/web";
	}

	
	@GetMapping("/dboard/getProgram")
	public ResponseEntity<Map<String, Object>> getProgrma(HttpServletRequest request) {
		return createClient.getProgram();

	}
	
	@GetMapping("/dboard/getIndicators/{id}")
	public ResponseEntity<Map<String, Object>> getIndicators(HttpServletRequest request,@PathVariable String id) {
		return createClient.getIndicators(id);

	}
	
	@GetMapping("/dboard/getData")
	public ResponseEntity<Map<String, Object>> getData(HttpServletRequest request) throws Exception {
		return createClient.getData(request);

	}
	
	@GetMapping("/dboard/getComposite")
	public ResponseEntity<Map<String, Object>> getComposite(HttpServletRequest request) {
		return createClient.getComposite();

	}
	
	@GetMapping("/getDashboard")
	public ResponseEntity<Map<String, Object>> getdashboard(HttpServletRequest request) throws Exception {
		return createClient.getDashboard();

	}
	
	
//	@ResponseBody
//	@GetMapping("/test/authorize")
//	public String authorize(HttpServletRequest request) {
//		String baseUrl = "http://localhost:8011";
//		String client_id="devinstance";
//		String redirect_uri="dev-machine";
//		String state=UUID.randomUUID().toString();
//		String response_type="code";
//		String urlpart="?client_id="+client_id+"&redirect_uri="+redirect_uri+"&state="+state+"&response_type="+response_type;
//		String url = baseUrl + "/authorize"+urlpart;
//		HttpRequest requests = new HttpRequest();
//		JSONObject responses = requests.get(url);
//		System.out.println(responses);
//		return "I'm working fine";
//	}
//	
//	@ResponseBody
//	@GetMapping("/test/create-client")
//	public String createClient(HttpServletRequest request) {
//		
//		String baseUrl = "http://localhost:8011";
//		String client_id="devinstance";
//		String client_name="devinstance";
//		String redirect_uri="dev-machine";
//		String client_secret="devinstance";
//		String status="1";
//		String urlpart="?client_id="+client_id+"&redirect_uri="+redirect_uri+"&client_name="+client_name+"&client_secret="+client_secret+"&status="+status;
//		String url = baseUrl + "/add-clients"+urlpart;
//		HttpRequest requests = new HttpRequest();
//		JSONObject responses = requests.get(url);
//		System.out.println(responses);
//		return responses.toString();
//	}
//	
//
//	
//	@ResponseBody
//	@PostMapping("/client-create-test")
//	public String ClientCreationTest(HttpServletRequest request) {
//		
//		return createClient.store();
//	}
//	
//	@ResponseBody
//	@GetMapping("test-omed")
//	public void testOmed() {
//		OmedBuilder bldr = new OmedBuilder();
//		bldr.setSubject("test subject");
//		bldr.setBodyText("This is a sample test");
//		bldr.addAttachments("iid", "name", "text", "C:\\Users\\user\\Downloads\\oasDoc.txt", "bhim");
//		OMED doc = bldr.getDocument();
//		String id = ds.storeDocument(doc);
//		System.out.println("docid: "+id);
//		OMED storedData = ds.getDocumentById(id);
//		OMED storeData1 = ds.getDocumentByMessageId(doc.messageid);
//		System.out.println("1. "+OmedParser.getJsonString(storedData));
//		System.out.println("2. "+OmedParser.getJsonString(storeData1));
//		System.out.println(OmedParser.getJsonString(doc));
//	}
//	
//	@RequestMapping(value = "list-routes")
//	@ResponseBody
//	public List<EndPoint> getAllUrl() {
//		RequestMappingHandlerMapping mapping = ApplicationContextProvider.getBean(RequestMappingHandlerMapping.class);
//		// Get the corresponding information of url and class and method
//		Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
//		List<EndPoint> endPoints=new ArrayList<>();
//		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
//		for (Entry<RequestMappingInfo, HandlerMethod> m : map.entrySet()) {
//			Map<String, String> map1 = new HashMap<String, String>();
//			RequestMappingInfo info = m.getKey();
//			HandlerMethod method = m.getValue();
//			PatternsRequestCondition p = info.getPatternsCondition();
//			EndPoint ep = new EndPoint();
//			for (String url : p.getPatterns()) {
//				map1.put("url", url);
//				ep.url = url;
//			}
//			map1.put("className", method.getMethod().getDeclaringClass().getName()); // class name
//			ep.mapping = method.getMethod().getDeclaringClass().getName()+":"+method.getMethod().getName();
//			map1.put("method", method.getMethod().getName()); // method name
//			RequestMethodsRequestCondition methodsCondition = info.getMethodsCondition();
//			List<String> methods=new ArrayList<>();
//			for (RequestMethod requestMethod : methodsCondition.getMethods()) {
//				map1.put("type", requestMethod.toString());
//				methods.add(requestMethod.toString());
//			}
//			ep.mapping = methods+"";//(ep.mapping+"("+methods+")");
//			endPoints.add(ep);
//			list.add(map1);
//		}
//
//		// JSONArray jsonArray = JSONArray.fromObject(list);
//		Collections.sort(endPoints);
//		return endPoints;
//	}
//	
}

class EndPoint implements Comparable<EndPoint>{
	public String url;
	public String mapping;
	@Override
	public int compareTo(EndPoint ep) {
		// TODO Auto-generated method stub
		return url.compareToIgnoreCase(ep.url);
	}

}
