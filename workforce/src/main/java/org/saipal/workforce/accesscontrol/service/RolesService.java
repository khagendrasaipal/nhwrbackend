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
import org.saipal.workforce.accesscontrol.model.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class RolesService extends AutoService {

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	private String table = "xcs_roles";

	public ResponseEntity<Map<String, Object>> getPermissions() {
		String roleId = request("roleid");
		if (!roleId.isBlank()) {
			String sql = "select cast(xp.id as CHAR) as id,name, display_name,ifnull(xrp.role_id,0) as rid from xcs_permissions as xp "
					+ "left join xcs_roles_perms as xrp on xrp.perm_id=xp.id and xrp.role_id=?";
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
		String role = request("role");
		String prms = request("prms");
		JSONArray jarr;
		try {
			if (prms.contains("[")) {
				jarr = new JSONArray(prms);
			} else {
				jarr = new JSONArray();
				jarr.put(prms);
			}
			String sql="insert into xcs_roles_perms (role_id,perm_id) values(?,?)";
			List<List<Object>> params = new ArrayList<>();
			if(jarr.length() > 0) {
				for(int i=0;i<jarr.length();i++) {
					params.add(List.of(role,jarr.get(i)));
				}
			}
			db.execute("delete from xcs_roles_perms where role_id=?",Arrays.asList(role));
			db.executeBulk(sql,params);
			return Messenger.getMessenger().success();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Messenger.getMessenger().error();
	}

	public ResponseEntity<Map<String, Object>> index() {
		String condition = "";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = Roles.searchables();
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
				.select("cast(id as CHAR) as id,name,display_name").sqlBody("from " + table + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> store() {

		String sql = "";

		Roles model = new Roles();
		model.loadData(document);
		sql = "INSERT INTO xcs_roles (name,display_name) VALUES (?,?)";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.name, model.display_name));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select cast(id as CHAR) as id,name, display_name from " + table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		DbResponse rowEffect;
		Roles model = new Roles();
		model.loadData(document);

		String sql = "UPDATE xcs_roles set name=?, display_name=? where id=?";
		rowEffect = db.execute(sql, Arrays.asList(model.name, model.display_name, id));

		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}

	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {

		String sql = "delete from xcs_roles where id  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

}
