package org.saipal.workforce.admistr.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.saipal.fmisutil.auth.Authenticated;
import org.saipal.fmisutil.service.AutoService;
import org.saipal.fmisutil.util.DB;
import org.saipal.fmisutil.util.DbResponse;
import org.saipal.fmisutil.util.Messenger;
import org.saipal.fmisutil.util.Paginator;
import org.saipal.workforce.admistr.model.Organization;
import org.saipal.workforce.admistr.model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
@Component
public class UsersServices extends AutoService {

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	private String table = "users";

	public ResponseEntity<Map<String, Object>> index() {
		String condition = "";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = Users.searchables();
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
				.select("id,email,fullname")
				.sqlBody("from " + table + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> store() {
		String sql = "";
		Users model = new Users();
		model.loadData(document);
		if(model.password.equals(model.cpassword)) {
			sql = "INSERT INTO users (fullname,email, password, orgid, role) VALUES (?,?,?,?,?)";
			DbResponse rowEffect = db.execute(sql, Arrays.asList(model.fullname, model.email, new BCryptPasswordEncoder().encode(model.password), model.orgid,
					model.role));
			if (rowEffect.getErrorNumber() == 0) {
				return Messenger.getMessenger().success();

			} else {
				return Messenger.getMessenger().error();
			}
		}else {
			return Messenger.getMessenger().setMessage("Password and confirm password donot matched.").error();
		}
		
		
	}
	
	

	

	public ResponseEntity<Map<String, Object>> getParent() {
		String sql = "select id,name from organization where 1=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("name", t.get("name"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}
	
	

	

	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select id,email,fullname,orgid,role from "
				+ table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		DbResponse rowEffect;
		Users model = new Users();
		model.loadData(document);
		if(model.password.equals(model.cpassword)) {
			String sql = "UPDATE users set fullname=?,email=?,password=?,orgid=?,role=? where id=?";
			rowEffect = db.execute(sql, Arrays.asList(model.fullname, model.email, model.password, model.orgid,
					model.role,id));

			if (rowEffect.getErrorNumber() == 0) {
				return Messenger.getMessenger().success();

			} else {
				return Messenger.getMessenger().error();
			}
		}else {
			return Messenger.getMessenger().setMessage("Password and confirm password donot matched.").error();
		}
		

	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {
		String sql = "delete from organization where id  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	

	public ResponseEntity<Map<String, Object>> getOrgs() {
		return Messenger.getMessenger().setData(session("orgname")).success();
	}
}
