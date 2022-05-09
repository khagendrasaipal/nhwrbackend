package org.saipal.workforce.accesscontrol.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.saipal.fmisutil.auth.Authenticated;
import org.saipal.fmisutil.service.AutoService;
import org.saipal.fmisutil.util.DB;
import org.saipal.fmisutil.util.DbResponse;
import org.saipal.fmisutil.util.Messenger;
import org.saipal.fmisutil.util.Paginator;
import org.saipal.workforce.accesscontrol.model.Adminusers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
@Component
public class AdminuserService extends AutoService{

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	private String table = "xcs_users";
	
	public ResponseEntity<Map<String, Object>> index() {
		String condition = "";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = Adminusers.searchables();
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE '%" + db.esc(request("searchTerm")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);
			condition += ")";
		}
		if (!condition.isBlank()) {
			condition = " where 1=1 " + condition;
		}
		
		String sort = "";
		if(!request("sortKey").isBlank()) {
			if(!request("sortDir").isBlank()) {
				sort = request("sortKey")+" "+request("sortDir");
			}
		}

		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage"))
				.setOrderBy(sort)
				.select("cast(id as CHAR) as id,name,username").sqlBody("from " + table + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<Map<String, Object>> getPermissions() {
		String roleId = request("roleid");
		if (!roleId.isBlank()) {
			String sql = "select cast(xp.id as CHAR) as id,name, display_name,ifnull(xrp.role_id,0) as rid from xcs_roles as xp "
					+ "left join xcs_user_roles as xrp on xrp.role_id=xp.id and xrp.user_id=?";
			List<Tuple> permission = db.getResultList(sql, Arrays.asList(roleId));

			List<Map<String, Object>> list = new ArrayList<>();
			if (!permission.isEmpty()) {
				for (Tuple t : permission) {
					Map<String, Object> mapPermission = new HashMap<>();
					mapPermission.put("id", t.get("id"));
					mapPermission.put("name", t.get("name"));
					mapPermission.put("display_name", t.get("display_name"));
					mapPermission.put("rid", t.get("rid"));
					list.add(mapPermission);
				}
				return Messenger.getMessenger().setData(list).success();
			}
		}
		return Messenger.getMessenger().error();
	}
	
	public ResponseEntity<Map<String, Object>> savePermissions() {
		String userid = request("userid");
		String prms = request("prms");
		JSONArray jarr;
		try {
			if (prms.contains("[")) {
				jarr = new JSONArray(prms);
			} else {
				jarr = new JSONArray();
				jarr.put(prms);
			}
			String sql="insert into xcs_user_roles (user_id,role_id) values(?,?)";
			List<List<Object>> params = new ArrayList<>();
			if(jarr.length() > 0) {
				for(int i=0;i<jarr.length();i++) {
					params.add(List.of(userid,jarr.get(i)));
				}
			}
			db.execute("delete from xcs_user_roles where user_id=?",Arrays.asList(userid));
			db.executeBulk(sql,params);
			return Messenger.getMessenger().success();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Messenger.getMessenger().error();
	}
	
	public ResponseEntity<Map<String, Object>> changePassword(){
		String userid=request("userid");
		String password=request("password");
		String cpassword=request("cpassword");
		if(password.equals(cpassword)) {
			String sql1="update xcs_users set password=? where id=?";
			DbResponse row= db.execute(sql1,Arrays.asList(new BCryptPasswordEncoder().encode(password),userid));
			if(row.getErrorNumber()==0) {
				return Messenger.getMessenger().success();
			}else {
				return Messenger.getMessenger().setData(row.getMessage()).error();
			}

		}else {
			return Messenger.getMessenger().setMessage("Password and confirm password donot matched.").error();
		}
		
	}

	public ResponseEntity<Map<String, Object>> store() {
		String sql = "";
		
		Adminusers model = new Adminusers();
		model.loadData(document);
		sql = "INSERT INTO xcs_users (organization,name, username, password, type, projectid) VALUES (?,?,?,?,?, ?)";
		System.out.println("Here");
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.organization, model.name, model.username, new BCryptPasswordEncoder().encode(model.password), model.type, auth.getAppId()));
		if (rowEffect.getErrorNumber() == 0) {
			System.out.println("If Here");
			return Messenger.getMessenger().success();

		} else {
			System.out.println("Else Here");
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getOrganization() {
		String sql = "select cast(orgidint as char) as orgidint,orgnameen,orgnamenp from admin_org_strs where disabled=? and approved=?";
		List<Tuple> organization = db.getResultList(sql, Arrays.asList(0, 1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!organization.isEmpty()) {
			for (Tuple t : organization) {
				Map<String, Object> mapOrganization = new HashMap<>();
				mapOrganization.put("orgidint", t.get("orgidint"));
				mapOrganization.put("orgnamenp", t.get("orgnamenp"));
				mapOrganization.put("orgnameen", t.get("orgnameen"));
				list.add(mapOrganization);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {
		String sql = "delete from xcs_users where id  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {
		String sql = "select cast(id as CHAR) as id, name, cast(organization as char) as organization, username, type from "
				+ table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		DbResponse rowEffect;
		Adminusers model = new Adminusers();
		model.loadData(document);

		String sql = "UPDATE xcs_users set organization=?, name=?, username=?, password=?, type=? where id=?";
		rowEffect = db.execute(sql,
				Arrays.asList(model.organization, model.name, model.username,new BCryptPasswordEncoder().encode(model.password), model.type,id));

		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

}
