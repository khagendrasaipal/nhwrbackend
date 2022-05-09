package org.saipal.workforce.files;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.saipal.fmisutil.util.DB;
import org.saipal.fmisutil.util.HttpRequest;
import org.saipal.fmisutil.util.Messenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/file-repo")
public class FileController {
	
	@Autowired
	FileRepo fr;
	
	@Autowired
	DB db;
	
	@GetMapping("get-file/{id}")
	public ResponseEntity<ByteArrayResource> getFile(@PathVariable String id) {
		MediaFile f;
		try {
			f = fr.getFile(id);
			ByteArrayResource resource = new ByteArrayResource(f.fileStream.readAllBytes());
		    return ResponseEntity.ok()
		    		.header("Content-Disposition","inline; filename="+f.name)
		    		.header("length", resource.contentLength()+"")
		    		.header("Content-type", f.type)
		    		.body(resource);
//		    return Messenger.getMessenger().setData().success();
		    
		} catch (IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	@GetMapping("get-file")
	public String test() {
		String baseUrl = "http://localhost:8011";
		String url = baseUrl + "/add-clients?client_id=jshhshshs";
		String token = "akjdshfk";
		System.out.println(token);
		try {
			HttpRequest hreq = new HttpRequest();
			System.out.println(token);
			JSONObject response = hreq.setHeader("Authorization", "Bearer " + token).setHeader("Accept", "application/json").get(url);
			System.out.println(token);
			System.out.println(response);
			if (response.getInt("status_code") == 200) {
				JSONArray resp = response.getJSONArray("data");
				return resp.toString();
			}
			System.out.println("hello");
			return "here";
		} catch (JSONException e) {
			e.printStackTrace();
			System.out.println("bye");
			return "errr";
		}
	}
	
//	@ResponseBody
//	@PostMapping("storefile")
//	public String addFile(@RequestParam("file") MultipartFile file){
//		try {
//			return fr.addFile(file);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			//e.printStackTrace();
//			return "";
//		}
//	}
	
	@ResponseBody
	@PostMapping("storefile")
	public ResponseEntity<Map<String, Object>> addFile(@RequestParam("file") MultipartFile file){
		try {
			return fr.addFile(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
	}
	
	@ResponseBody
	@PostMapping("storefileMail")
	public ResponseEntity<Map<String, Object>> storefileMail(@RequestParam("file") MultipartFile file,@RequestParam("mailid") String mailid,@RequestParam("filename") String filename){
		
		String sql = "select count(id) as id from oas_mail_documents where mailid=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(mailid));
		System.out.println(data.get("id").toString());
		if(data.get("id").toString().equals("0")) {
			String sql1 = "select reg_no from oas_mails where mailid=?";
			Map<String, Object> datas = db.getSingleResultMap(sql1, Arrays.asList(mailid));
			String regno=datas.get("reg_no").toString()+".pdf";
			if(regno.equals(filename)) {
				try {
					return fr.addFile(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					return null;
				}
			}else {
				return Messenger.getMessenger().setMessage("Filename Must match with reg no.").error();
			}
			
			
		}else {
			try {
				return fr.addFile(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				return null;
			}
		}
		
	}
	
	
	
	@GetMapping("")
	public String fileUploader() {
		return "uploader";
	}
}
