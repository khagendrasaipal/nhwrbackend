package org.saipal.workforce.accesscontrol.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.saipal.fmisutil.auth.Authenticated;
import org.saipal.fmisutil.service.AutoService;
import org.saipal.fmisutil.util.DB;
import org.saipal.fmisutil.util.DbResponse;
import org.saipal.fmisutil.util.Messenger;
import org.saipal.fmisutil.util.Paginator;
import org.saipal.workforce.accesscontrol.model.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class PermissionService extends AutoService{

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;
	
	private String table = "xcs_permissions";
	
	public ResponseEntity<Map<String, Object>> index() {
		String condition = "";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = Permissions.searchables();
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE '%" + db.esc(request("searchTerm")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);
			condition += ")";
		}
		if(!condition.isBlank()) {
			condition = " where 1=1 " + condition;
		}
		String sort = "";
		if(!request("sortKey").isBlank()) {
			if(!request("sortDir").isBlank()) {
				sort = request("sortKey")+" "+request("sortDir");
			}
		}
		Paginator p = new Paginator();
		Map<String, Object> result = p
				.setPageNo(request("page"))
				.setPerPage(request("perPage"))
				.setOrderBy(sort)
				.select("cast(id as CHAR) as id,name,display_name")
				.sqlBody("from " + table + condition)
				.paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<Map<String, Object>> store() {

		String sql = "";

		Permissions model = new Permissions();
		model.loadData(document);
		sql = "INSERT INTO xcs_permissions (name,display_name) VALUES (?,?)";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.name, model.display_name));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select cast(id as CHAR) as id,name, display_name from "
				+ table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}
	
	public ResponseEntity<Map<String, Object>> update(String id) {
		DbResponse rowEffect;
		Permissions model = new Permissions();
		model.loadData(document);

		String sql = "UPDATE xcs_permissions set name=?, display_name=? where id=?";
		rowEffect = db.execute(sql,
				Arrays.asList(model.name, model.display_name,id));

		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}

	}
	
	public ResponseEntity<Map<String, Object>> destroy(String id) {

		String sql = "delete from xcs_permissions where id  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}
	

	
}
