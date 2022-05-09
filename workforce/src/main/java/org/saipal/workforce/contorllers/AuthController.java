package org.saipal.workforce.contorllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.saipal.fmisutil.auth.Authenticated;
import org.saipal.fmisutil.util.DB;
import org.saipal.fmisutil.util.Messenger;
import org.saipal.workforce.service.AutoService;
import org.saipal.workforce.service.SystemConfigService;
import org.saipal.workforce.service.User;
import org.saipal.workforce.service.UserService;
import org.saipal.workforce.util.FmisUtil;
import org.saipal.workforce.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

	@Autowired
	PasswordEncoder en;
	
	@Autowired
	private SystemConfigService sysService;

	@Autowired
	JwtUtil jUtil;

	@Autowired
	DB db;
	
	@Autowired
	AutoService autoService;
	
	@Autowired
	Authenticated auth;
	
	@Autowired
	private UserService userService;
	private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);
	
	@GetMapping("/login")
	public String login(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && !auth.getClass().equals(AnonymousAuthenticationToken.class)) {
			return "redirect:apphome";
		}
		String ajaxString = request.getHeader("X-Requested-With");
		if (ajaxString != null && ajaxString.equalsIgnoreCase("XMLHttpRequest")) {
			response.getWriter().print("script $('#login').window('open');$('#login').window('refresh', '"
					+ FmisUtil.getBaseUrl(request) + "/relogin');");
			// return "script $('#login').window('open');$('#login').window('refresh',
			// '/relogin'); ";
			return null;
		}
		String error = null;
		if (model.getAttribute("errorMessage") != null) {
			error = model.getAttribute("errorMessage").toString();
		}

		Tuple tuple = sysService.getInfo();
		model.addAttribute("AppName", tuple.get("Appname").toString());
		model.addAttribute("AppFullName", tuple.get("AppFullName").toString());
		model.addAttribute("CopyRight", tuple.get("CopyRight").toString());
		model.addAttribute("errorMessage", error);
		return "login";
	}

	@RequestMapping("/logout-done")
	public String logoutDone(HttpServletRequest request, HttpServletResponse response) {
		autoService.removeCookie(request, response, "jwt");
		auth.clearAllStates();
		return "redirect:login";

	}

	@GetMapping("/ng-login")
	public String ngLogin(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
		return "nglogin";
	}

	@RequestMapping("/get-auth-token")
	@ResponseBody
	public Map<String, Object> oagAuth(@RequestParam String username, @RequestParam String password) {
		Map<String, Object> data = userService.auth(username, password);
		Map<String, Object> ret = new HashMap<>();
		if (data.get("user") != null) {

			User user = (User) data.get("user");
			final String jwt = jUtil.generateToken(user.getUserid());
			ret.put("token", jwt);
			ret.put("status", 200);
			ret.put("message", "Success");
		} else {
			ret.put("token", "");
			ret.put("status", 403);
			ret.put("message", "Invalid username or password");
		}
		return ret;
	}

	@RequestMapping("/auth-login")
	public String auth1(@RequestParam String username, @RequestParam String password, HttpServletRequest request,
			HttpServletResponse response, RedirectAttributes attr) throws Exception {
		Map<String, Object> data = userService.auth(username, password);
		if (data.get("user") != null) {
			String remoteAdr = request.getHeader("X-FORWADED-FOR");
			if (remoteAdr == null || "".equals(remoteAdr)) {
				remoteAdr = request.getRemoteAddr();
			}
			User user = (User) data.get("user");
			final String jwt = jUtil.generateToken(user.getUserid());

			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUserid(),
					null, new ArrayList<>());			
			if (SecurityContextHolder.getContext().getAuthentication() == null || SecurityContextHolder.getContext()
					.getAuthentication().getClass().equals(AnonymousAuthenticationToken.class)) {
				SecurityContextHolder.getContext().setAuthentication(token);
				autoService.addCookie("jwt", jwt,response);
			}
			return "redirect:apphome";
		}
		attr.addFlashAttribute("errorMessage", data.get("message") + "");
		return "redirect:login";
	}
	
	@ResponseBody
	@PostMapping(value="/sign-in")
	public ResponseEntity<Map<String, Object>> signIn(HttpServletRequest request) {
		String username = userService.request("username");
		String password = userService.request("password");
		String utype = userService.request("utype");
		
		if(username!=null && password != null) {
			Map<String, Object> data;
			if(utype.equals("1")) {
				 data = userService.authAdmin(username, password);
			}else {
				 data = userService.auth(username, password);
			}
			
			if (data.get("user") != null) {
				//String remoteAdr = request.getHeader("X-FORWADED-FOR");
				//if (remoteAdr == null || "".equals(remoteAdr)) {
				//	remoteAdr = request.getRemoteAddr();
				//}
				User user = (User) data.get("user");
				final String jwt = jUtil.generateToken(user.getUserid());
				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUserid(),
						null, new ArrayList<>());			
				if (SecurityContextHolder.getContext().getAuthentication() == null || SecurityContextHolder.getContext()
						.getAuthentication().getClass().equals(AnonymousAuthenticationToken.class)) {
					SecurityContextHolder.getContext().setAuthentication(token);
					if(utype.equals("1")) {
						 auth.setUserstateOnLogin(user.getUserid(),jUtil.extractId(jwt),userService.getUserInfoAdmin(user.getUserid()));
					}else {
						auth.setUserstateOnLogin(user.getUserid(),jUtil.extractId(jwt),userService.getUserInfo(user.getUserid()));
					}
					
					return Messenger.getMessenger().setMessage("Login Successful").setData(Map.of("user",data.get("user"),"token",jwt)).success();
				}
				return Messenger.getMessenger().setMessage("Internal Error").error();
			}
			return Messenger.getMessenger().setMessage("Invalid Username or Password").error();
		}
		return Messenger.getMessenger().setMessage("Invalid Request").error();
	}

//	@RequestMapping("/post-login")
//	public String postLogin(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
//		CustomUserDetails user = null;
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//		user = (CustomUserDetails) auth.getPrincipal();
//		authService.postLogin(user.getUid(), user.getUsername());
//		autoService.logSession("redirect", true);
//		return "redirect:" + autoService.session("baseurl") + "/applications";
//	}

	@PostMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> auth(@RequestParam String username, @RequestParam String password,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> data = userService.auth(username, password);
		if (data.get("user") != null) {
			User user = (User) data.get("user");
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUsername(),
					user.getPassword(), new ArrayList<>());
			if (SecurityContextHolder.getContext().getAuthentication() == null || SecurityContextHolder.getContext()
					.getAuthentication().getClass().equals(AnonymousAuthenticationToken.class)) {
				SecurityContextHolder.getContext().setAuthentication(token);
			}

			return ResponseEntity.status(HttpStatus.ACCEPTED)
					.body("{\"status\":\"1\",\"message\":\"Login successful\"}");
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body("{\"status\":\"1\",\"message\":\"" + data.get("message") + "\"}");
	}

	// @GetMapping("/en-pass")
	@ResponseBody
	public String encryptPassword() {
		if (true) {
			List<Tuple> userList = db.getResultList(
					"select uid,dbo.decript(password) as password,loginname from hr_user_info where userstatus=1");
			for (Tuple t : userList) {
				db.execute("update hr_user_info set password1='" + en.encode(t.get("password") + "")
						+ "' where uid='" + t.get("uid") + "'");
			}

			return "All Password encoded successfully";
		}
		return "";
	}

	@GetMapping("/temp-login/{userId}")
	public String tempLogin(@PathVariable String userId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Map<String, Object> data = userService.findUserByUserId(userId);
		if (data.get("message") != null) {
			response.getWriter().print("user not found");
			return null;
		}
		data.get("user");
		return "redirect:applications";

	}

	@GetMapping("/temp-login-destroy")
	public String destroyTempLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Object userID = autoService.session("olduserid");
		if (userID != null) {
			return "redirect:applications";
		} else {
			response.getWriter().print("cannot re-login");
			return null;
		}
	}

	@GetMapping("/relogin")
	public String relogin() {
		return "frmrelogin";
	}

	@GetMapping("/en-pass/{password}")
	@ResponseBody
	public String getBcrypt(@PathVariable String password) {
		LOG.info("passoword:" + password);
		return en.encode(password);
	}

	@GetMapping("/en-pass/{id}/{password}")
	@ResponseBody
	public String updatePassword(@PathVariable BigDecimal id, @PathVariable String password) {
		LOG.info("Password:" + password);
		db.execute("update hr_user_info set password1='" + en.encode(password) + "' where uid='" + id + "'");
		return "Password Changed Successfully";
	}

	@GetMapping("")
	public String defaultRoute(HttpServletRequest request) {
		return "index";
	}
	
	@PostMapping("get-user-token")
	@ResponseBody
	public Map<String,Object> getUserToken() {
		String userId = userService.request("userid");
		Map<String,Object> user = userService.findUserByUserId(userId);
		if(user.containsKey("user")) {
			String token = jUtil.generateToken(userId);
			user.put("token",token);
			return user;
		}else {
			return user;
		}
		
	}
}
