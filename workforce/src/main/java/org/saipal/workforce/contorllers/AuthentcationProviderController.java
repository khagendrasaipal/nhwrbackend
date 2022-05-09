package org.saipal.workforce.contorllers;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.saipal.fmisutil.auth.Authenticated;
import org.saipal.workforce.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthentcationProviderController {
	
	@Autowired
	AuthService authService;
	
	@Autowired
	Authenticated auth;
	
	@PostMapping("authenticate")
	public Map<String,Object> authenticate() {
		return authService.authenticate();
	}
	
	@GetMapping("/check-token")
	public Map<String,Object> checkToken(){
		//return authService.checkToken();
		Map<String,Object> obj = new HashMap<>();
		obj.put("status", 1);
		obj.put("message", "Successful");
		Map<String,String> userInfo = new HashMap<>();
		userInfo.put("userId", auth.getUserId());
		userInfo.put("orgId", auth.getOrgId());
		userInfo.put("appId", auth.getAppId());
		userInfo.put("adminId", auth.getAdminId());
		userInfo.put("adminLevel", auth.getAdminLevel());
		userInfo.put("sessionid", auth.getJti());
		userInfo.put("requestid", auth.getExtraInfo("requestid")+"");
		obj.put("data",new JSONObject(userInfo).toString());
		return obj;
	}
	
	@PostMapping("check-url-permission")
	public Map<String,Object> checkUriPrmission(String url) {
		boolean hasPerm = authService.checkPermission(url);
		Map<String,Object> obj = new HashMap<>();
		if(hasPerm) {
			obj.put("status", 1);
			obj.put("message", "Url is permitted");
		}else {
			obj.put("status", 0);
			obj.put("message", "Url not permitted for this user");
		}
		return obj;
		
	}
	
	@PostMapping("check-permission")
	public Object checkPermission(String permission) {
		Map<String,Object> obj = new HashMap<>();
		obj.put("status", 0);
		obj.put("message", "Not Implemented Yet.");
		return obj;
	}
}
