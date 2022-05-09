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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class OrganizationService extends AutoService {
	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	private String table = "organization";

	public ResponseEntity<Map<String, Object>> index() {
		String condition = "";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = Organization.searchables();
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
				.select("id,code,name")
				.sqlBody("from " + table + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> store() {
		String sql = "";
		Organization model = new Organization();
		model.loadData(document);
		sql = "INSERT INTO organization (code, name, adm_level, adm_id, parent) VALUES (?,?,?,?,?)";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.code, model.name, model.adm_level, model.adm_id,
				model.parent));
		if (rowEffect.getErrorNumber() == 0) {
			
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
		
	}
	
	public ResponseEntity<Map<String, Object>> getAdminlvl() {
		String sql = "select cast(levelid as CHAR) as levelid,levelnameen,levelnamenp from admin_level where disabled=? and approved=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(0, 1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("levelid", t.get("levelid"));
				mapadmlvl.put("levelnamenp", t.get("levelnamenp"));
				mapadmlvl.put("levelnameen", t.get("levelnameen"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getFederal() {
		String sql = "select cast(id as CHAR) as id,nameen,namenp from admin_federal where disabled=? and approved=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(0, 1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("namenp", t.get("namenp"));
				mapadmlvl.put("nameen", t.get("nameen"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
//			return Messenger.getMessenger().error();
			return Messenger.getMessenger().setData(list).success();
		}
	}

	public ResponseEntity<Map<String, Object>> getParent(String id) {
		String sql = "select id,name from organization where adm_id=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(id));

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
	
	public ResponseEntity<Map<String, Object>> getParentOrgs() {
		String admin=request("adminid");
		String level=request("levelid");
		String sql = "select cast(orgidint as CHAR) as orgidint,orgnameen,orgnamenp from admin_org_strs where disabled=? and approved=? and adminlevel=? and adminid=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(0, 1,level,admin));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("orgidint", t.get("orgidint"));
				mapadmlvl.put("orgnamenp", t.get("orgnamenp"));
				mapadmlvl.put("orgnameen", t.get("orgnameen"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
		}
	}

	public ResponseEntity<Map<String, Object>> getOrglevel() {
		String sql = "select cast(levelid as CHAR) as levelid,levelnameen,levelnamenp from admin_org_level where disabled=? and approved=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(0, 1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("levelid", t.get("levelid"));
				mapadmlvl.put("levelnamenp", t.get("levelnamenp"));
				mapadmlvl.put("levelnameen", t.get("levelnameen"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}


	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select id as orgidint,code,name, adm_level, cast(adm_id as char) as adm_id,parent from "
				+ table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		DbResponse rowEffect;
		Organization model = new Organization();
		model.loadData(document);

		String sql = "UPDATE organization set code=?,name=?,adm_level=?,adm_id=?,parent=? where id=?";
		rowEffect = db.execute(sql, Arrays.asList(model.code, model.name, model.adm_level, model.adm_id,
				model.parent,id));

		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
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

	public ResponseEntity<Map<String, Object>> getAdmin(String id) {
		
		String sql="";
		if(id.equals("1")) {
    	    sql = "select id,namenp,nameen as name from admin_federal where disabled=? and approved=?";
		}
		if(id.equals("2")) {
			 sql = "select pid as id,nameen as name,namenp from admin_province where disabled=? and approved=?";
		}
		if(id.equals("4")) {
			 sql = "select cast(vcid as char) as id,nameen as name,namenp from admin_local_level_structure where disabled=? and approved=?";
		}
		List<Tuple> province = db.getResultList(sql, Arrays.asList(0, 1));
		List<Map<String, Object>> list = new ArrayList<>();
		if (!province.isEmpty()) {
			for (Tuple t : province) {
				
				Map<String, Object> mapProvince = new HashMap<>();
				mapProvince.put("id", t.get("id"));
				mapProvince.put("namenp", t.get("namenp"));
				mapProvince.put("name", t.get("name"));
				list.add(mapProvince);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();

		}
	}

	public ResponseEntity<Map<String, Object>> getOrgs() {
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> mapProvince = new HashMap<>();
		mapProvince.put("orgid", session("orgid"));
		mapProvince.put("orgname", session("orgname"));
		list.add(mapProvince);
		return Messenger.getMessenger().setData(list).success();
	}

}
