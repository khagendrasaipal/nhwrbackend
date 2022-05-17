package org.saipal.workforce.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.saipal.fmisutil.auth.Authenticated;
import org.saipal.fmisutil.util.DB;
import org.saipal.workforce.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService extends AutoService {

	@Autowired
	DB db;
	
	private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	JwtUtil jwtUtil;
	
	@Autowired
	Authenticated auth;
	
	@Autowired
	UserService userDetailService;

	public boolean checkPermission(String uri) {
		String userTypeId = auth.getUserTypeId();
		String userType = userTypeId.equals("")? "-1":userTypeId;
		String treasury = util.request("treasury");
		if (!treasury.isBlank()) {
			uri += "?treasury=" + treasury;
		}
		
		String sql = "if exists (select * from hr_usertypemenulist where href=?) " + "begin "
				+ "select count(*) as count from hr_usertypemenulist where href=? and usertype=? " + "end " + "else "
				+ "begin " + "select 1 as count " + "end ";

		Tuple t = db.getSingleResult(sql, Arrays.asList(uri, uri, userType));
		if(t != null) {
			int count = Integer.parseInt(t.get("count") + "");
			if (count != 0) {
				return true;
			}
		}
		return false;
	}
	
	public Map<String, Object> authenticate() {
		String username = request("username");
		String password = request("password");
		String projectId = request("projectid");
		List<User> users = new ArrayList<>();
		Map<String, Object> data = new HashMap<>();
		// check login here
		String sql = "select uid,loginname,password,userstatus,orgid from hr_user_info where loginname=? and projectid=? and userstatus=1";
		List<Tuple> tList = db.getResultList(sql, Arrays.asList(username, projectId));

		tList.forEach((t) -> {
			if (passwordEncoder.matches(password, t.get("password") + "")) {
				users.add(new User(t.get("uid") + "", t.get("loginname") + "", t.get("password") + "",t.get("orgid") + "",""));
			}
		});

		if (users.size() > 1) {
			data.put("message", "More than one account exists for " + username);
		} else if (users.size() == 0) {
			data.put("message", "Invalid Username or password");
		} else {
			User user = users.get(0);
			data.put("username", user.getUsername());
			data.put("token",jwtUtil.createToken(user.getUserid()));			 
			auth.setUserstate(userDetailService.getUserInfo(user.getUserid()));
			return data;
		}
		return data;
	}

	public Map<String, Object> checkToken() {
		return null;

	}

	public String canAccess(String url) {
		return url;

	}

	public String canAccessPermission(String permname) {
		return permname;

	}
}