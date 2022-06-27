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
import org.saipal.workforce.admistr.model.SubGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class SubGroupService extends AutoService {

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	private String table = "tbl_upasamuha";
	
	public ResponseEntity<Map<String, Object>> index() {
		String condition = "";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = SubGroup.searchables();
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE '%" + db.esc(request("searchTerm")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);
			condition += ")";
		}
		condition += " and created_by= "+auth.getUserId()+ " ";
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
				.select("id,nameen,namenp")
				.sqlBody("from " + table + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> store() {
		String sql = "";
		SubGroup model = new SubGroup();
		model.loadData(document);
		sql = "INSERT INTO tbl_upasamuha (samuha,nameen,namenp,status,created_by) VALUES (?,?,?,?,?)";
		DbResponse rowEffect = db.execute(sql,
				Arrays.asList(model.samuha,model.nameen, model.namenp,model.status,auth.getUserId()));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select id,nameen,namenp,samuha from "
				+ table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		DbResponse rowEffect;
		SubGroup model = new SubGroup();
		model.loadData(document);

		String sql = "UPDATE tbl_upasamuha set nameen=?,namenp=?,status=?,samuha=? where id=?";
		rowEffect = db.execute(sql,
				Arrays.asList(model.nameen, model.namenp,model.status,model.samuha,id));

		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}

	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {

		String sql = "delete from tbl_upasamuha where id  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<Map<String, Object>> getSamuha() {
		String sql = "select id,namenp from tbl_samuha where 1=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("namenp", t.get("namenp"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<Map<String, Object>> getUpaSamuha() {
		String sql = "select id,namenp from tbl_upasamuha where 1=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("namenp", t.get("namenp"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<Map<String, Object>> getLevel() {
		String sql = "select id,namenp from tbl_level where 1=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("namenp", t.get("namenp"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getUpaSamuha(String id) {
		String sql = "select id,namenp from tbl_upasamuha where samuha=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(id));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("namenp", t.get("namenp"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getProvince() {
		String sql = "select pid,nameen from admin_province where 1=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("pid", t.get("pid"));
				mapadmlvl.put("namenp", t.get("nameen"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getDistrict(String id) {
		String sql = "select districtid,namenp from admin_district where provinceid=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(id));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("districtid", t.get("districtid"));
				mapadmlvl.put("namenp", t.get("namenp"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getPalika(String id) {
		String sql = "select cast(vcid as char) as vcid,namenp from admin_local_level_structure where districtid=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(id));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("vcid", t.get("vcid"));
				mapadmlvl.put("namenp", t.get("namenp"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getWard(String id) {
		String sql = "select cast(vcid as char) as vcid,namenp,numberofward from admin_local_level_structure where vcid=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(id));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("vcid", t.get("vcid"));
				mapadmlvl.put("numberofward", t.get("numberofward"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getHf() {
		String sql = "select cast(hf_code as char) as hfcode,hf_name,id from hfregistry where municipality=? and ward=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(request("mid"),request("wn")));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("hfcode", t.get("hfcode"));
				mapadmlvl.put("hfname", t.get("hf_name"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<Map<String, Object>> gethfbymunc() {
		String sql = "select cast(hf_code as char) as hfcode,hf_name,id from hfregistry where municipality=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(request("mid")));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
//				mapadmlvl.put("hfcode", t.get("hfcode"));
				mapadmlvl.put("namenp", t.get("hf_name"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getownership() {
		String sql = "select id,name from ownership where 1=?";
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

	public ResponseEntity<Map<String, Object>> gethftype() {
		String sql = "select id,name from facility_level where 1=?";
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

	public ResponseEntity<Map<String, Object>> gethfdetails(String id) {
		String sql = "select id,province,district,municipality,ward,type,ownership,authlevel,level from hfregistry where id=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(id));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("type", t.get("type"));
				mapadmlvl.put("ownership", t.get("ownership"));
				mapadmlvl.put("level", t.get("level"));
				mapadmlvl.put("authlevel", t.get("authlevel"));
				mapadmlvl.put("province", t.get("province"));
				mapadmlvl.put("district", t.get("district"));
				mapadmlvl.put("municipality", t.get("municipality"));
				mapadmlvl.put("ward", t.get("ward"));
				mapadmlvl.put("hfid", t.get("id"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getPost(String id) {
		String sql = "select id,namenp from tbl_post where samuha=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(id));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("namenp", t.get("namenp"));
				
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getEmptype() {
		String sql = "select id,namenp from tbl_emptype where 1=? order by code";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("namenp", t.get("namenp"));
				
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<Map<String, Object>> getethincity() {
		String sql = "select id,name from tbl_ethnicity where 1=? order by id";
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
	
	public ResponseEntity<Map<String, Object>> getEmps() {
		String council=request("cno");
		String sql = "select * from tbl_employee where council_no=? limit 1";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(council));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("nameen", t.get("nameen"));
				mapadmlvl.put("namenp", t.get("namenp"));
				mapadmlvl.put("address", t.get("address"));
				mapadmlvl.put("email", t.get("email"));
				mapadmlvl.put("mobile", t.get("mobile"));
				
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<Map<String, Object>> getEmpinfo() {
		String id=request("empid");
		String sql = "select * from tbl_employee where id=? limit 1";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(id));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("nameen", t.get("nameen"));
				mapadmlvl.put("namenp", t.get("namenp"));
				mapadmlvl.put("address", t.get("address"));
				mapadmlvl.put("email", t.get("email"));
				mapadmlvl.put("mobile", t.get("mobile"));
				mapadmlvl.put("council", t.get("council"));
				mapadmlvl.put("council_no", t.get("council_no"));
				mapadmlvl.put("emptype", t.get("emptype"));
				mapadmlvl.put("level", t.get("level"));
				mapadmlvl.put("education", t.get("education"));
				mapadmlvl.put("qualification", t.get("qualification"));
				mapadmlvl.put("pis", t.get("pis"));
				
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getCouncil() {
		String sql = "select id,namenp from tbl_council where 1=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("namenp", t.get("namenp"));
				
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getorgs() {
//		System.out.println(session("role"));
		String sql="";
		if(session("role").equals("superuser")) {
			 sql = "select cast(tbl_workforce.id as char) as id, tbl_workforce.org,tbl_office.namenp as orgname,hfregistry.hf_name,hfregistry.hf_code,admin_local_level_structure.namenp as vcname from tbl_workforce left join hfregistry on hfregistry.id=tbl_workforce.org left join tbl_office on tbl_office.id=tbl_workforce.officeid left join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where 1=?";
		}else {
			 sql = "select cast(tbl_workforce.id as char) as id, tbl_workforce.org,tbl_office.namenp as orgname,hfregistry.hf_name,hfregistry.hf_code, admin_local_level_structure.namenp as vcname from tbl_workforce left join hfregistry on hfregistry.id=tbl_workforce.org left join tbl_office on tbl_office.id=tbl_workforce.officeid left join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.created_by=?";
		}
		
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(auth.getUserId()));
		
//		String sql1 = "select tbl_workforce.orgname,cast(tbl_workforce.id as char) as id from tbl_workforce  where 1=? and org=?";
//		List<Tuple> admlvl1 = db.getResultList(sql1, Arrays.asList(1,0));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("hfname", t.get("hf_name"));
				mapadmlvl.put("hfcode", t.get("hf_code"));
				mapadmlvl.put("orgname", t.get("orgname"));
				mapadmlvl.put("vcname", t.get("vcname"));
				
				list.add(mapadmlvl);
			}

			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getadmlvl() {
		String sql = "select levelid,levelnamenp from admin_level where disabled=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(0));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("levelid"));
				mapadmlvl.put("namenp", t.get("levelnamenp"));
				
				list.add(mapadmlvl);
			}
			System.out.println(list.get(0).get("namenp")); 
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();

		}
	}

	public ResponseEntity<Map<String, Object>> gethfo(String id) {
		String sql="";
		if("4".equals(id)) {
			 sql = "select cast(vcid as char) as id,namenp,nameen from admin_local_level_structure where 4=? order by namenp";
		}else {
			 sql = "select id,namenp,nameen from tbl_office where admlvl=?";
		}
	
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(id));

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
			return Messenger.getMessenger().setData(list).success();

		}
	}

	public ResponseEntity<Map<String, Object>> getoffices(String id) {
		String sql = "select id,namenp,nameen from tbl_office where province=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(id));

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
			return Messenger.getMessenger().setData(list).success();

		}
	}

	public ResponseEntity<Map<String, Object>> getedulevel() {
		String sql = "select id,namenp from tbl_edulevel where 1=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("namenp", t.get("namenp"));
				
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();

		}
	}

	public ResponseEntity<Map<String, Object>> getqualification() {
		String council=request("cid");
		String level=request("eid");
		String sql = "select id,namenp from tbl_qualification where council=? and level=? order by nameen";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(council,level));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("namenp", t.get("namenp"));
				
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();

		}
	}

	public ResponseEntity<Map<String, Object>> getTransfer() {
		String wid=request("wid");
		String sql = "select * from tbl_workforce where id=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(wid));
		
		String sql1 = "select tbl_transfer.empid,tbl_transfer.id as tid,tbl_employee.namenp as empname from tbl_transfer join tbl_employee on tbl_employee.id=tbl_transfer.empid where orgtype=? and admlvl=? and officeid=? and orgid=? and muncid=?";
		List<Tuple> transfer = db.getResultList(sql1, Arrays.asList(admlvl.get(0).get("orgtype"),admlvl.get(0).get("admlvl"),admlvl.get(0).get("officeid"),admlvl.get(0).get("org"),admlvl.get(0).get("palika")));
		List<Map<String, Object>> list = new ArrayList<>();
		if(transfer!=null) {
		for (Tuple t : transfer) {
			Map<String, Object> mapadmlvl = new HashMap<>();
			mapadmlvl.put("tid", t.get("tid"));
			mapadmlvl.put("empid", t.get("empid"));
			mapadmlvl.put("empname", t.get("empname"));
			
			list.add(mapadmlvl);
		}
		return Messenger.getMessenger().setData(list).success();
		}else {
			return Messenger.getMessenger().setData(list).success();
		}
		
	}

	public ResponseEntity<Map<String, Object>> getDistrict() {
		String sql = "select districtid,namenp from admin_district";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("pid", t.get("districtid"));
				mapadmlvl.put("namenp", t.get("namenp"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getPalikas() {
		String sql = "select cast(vcid as char) as vcid,namenp from admin_local_level_structure ";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("pid", t.get("vcid"));
				mapadmlvl.put("namenp", t.get("namenp"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
		}
	}

	public ResponseEntity<Map<String, Object>> getCounts() {
		String sql = "select count(tbl_employee.id) as sanghiya from tbl_employee join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=1 and tbl_employee.soft_delete=0";
		List<Tuple> sanghiya = db.getResultList(sql, Arrays.asList());
		
		String sql1 = "select count(tbl_employee.id) as pradesh from tbl_employee join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid  where tbl_workforce.admlvl=2 and tbl_employee.soft_delete=0";
		List<Tuple> pradesh = db.getResultList(sql1, Arrays.asList());
		
		String sql2 = "select count(tbl_employee.id) as local from tbl_employee join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=4 and tbl_employee.soft_delete=0";
		List<Tuple> local = db.getResultList(sql2, Arrays.asList());
		
		String sql3 = "select count(tbl_employee.id) as hf from tbl_employee join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.orgtype=4 and tbl_employee.soft_delete=0";
		List<Tuple> hf = db.getResultList(sql3, Arrays.asList());

		List<Map<String, Object>> list = new ArrayList<>();
		
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("sangh", sanghiya.get(0).get("sanghiya"));
				mapadmlvl.put("pradesh", pradesh.get(0).get("pradesh"));
				mapadmlvl.put("local", local.get(0).get("local"));
				mapadmlvl.put("hf", hf.get(0).get("hf"));
				list.add(mapadmlvl);
			
			return Messenger.getMessenger().setData(list).success();

		
	}

	public ResponseEntity<Map<String, Object>> getsanctioned() {
		String sql = "select COALESCE(sum(tbl_darbandi.post_no),0) as tdarbandi,COALESCE(sum(tbl_darbandi.dworking),0) as wdarbandi from tbl_darbandi  join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id  where tbl_workforce.provinceid=1";
		List<Tuple> p1 = db.getResultList(sql, Arrays.asList());
		
		String sql2 = "select COALESCE(sum(tbl_darbandi.post_no),0) as tdarbandi,COALESCE(sum(tbl_darbandi.dworking),0) as wdarbandi from tbl_darbandi  join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id  where tbl_workforce.provinceid=2";
		List<Tuple> p2 = db.getResultList(sql2, Arrays.asList());
		
		String sql3 = "select COALESCE(sum(tbl_darbandi.post_no),0) as tdarbandi,COALESCE(sum(tbl_darbandi.dworking),0) as wdarbandi from tbl_darbandi  join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id  where tbl_workforce.provinceid=3";
		List<Tuple> p3 = db.getResultList(sql3, Arrays.asList());
		
		String sql4 = "select COALESCE(sum(tbl_darbandi.post_no),0) as tdarbandi,COALESCE(sum(tbl_darbandi.dworking),0) as wdarbandi from tbl_darbandi  join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id  where tbl_workforce.provinceid=4";
		List<Tuple> p4 = db.getResultList(sql4, Arrays.asList());
		
		String sql5 = "select COALESCE(sum(tbl_darbandi.post_no),0) as tdarbandi,COALESCE(sum(tbl_darbandi.dworking),0) as wdarbandi from tbl_darbandi  join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id  where tbl_workforce.provinceid=5";
		List<Tuple> p5 = db.getResultList(sql5, Arrays.asList());
		
		String sql6 = "select COALESCE(sum(tbl_darbandi.post_no),0) as tdarbandi,COALESCE(sum(tbl_darbandi.dworking),0) as wdarbandi from tbl_darbandi  join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id  where tbl_workforce.provinceid=6";
		List<Tuple> p6 = db.getResultList(sql6, Arrays.asList());
		
		String sql7 = "select COALESCE(sum(tbl_darbandi.post_no),0) as tdarbandi,COALESCE(sum(tbl_darbandi.dworking),0) as wdarbandi from tbl_darbandi  join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id  where tbl_workforce.provinceid=7";
		List<Tuple> p7 = db.getResultList(sql7, Arrays.asList());
		
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> mapadmlvl = new HashMap<>();
		mapadmlvl.put("p1d", p1.get(0).get("tdarbandi"));
		mapadmlvl.put("p1w", p1.get(0).get("wdarbandi"));
		
		mapadmlvl.put("p2d", p2.get(0).get("tdarbandi"));
		mapadmlvl.put("p2w", p2.get(0).get("wdarbandi"));
		
		mapadmlvl.put("p3d", p3.get(0).get("tdarbandi"));
		mapadmlvl.put("p3w", p3.get(0).get("wdarbandi"));
		
		mapadmlvl.put("p4d", p4.get(0).get("tdarbandi"));
		mapadmlvl.put("p4w", p4.get(0).get("wdarbandi"));
		
		mapadmlvl.put("p5d", p5.get(0).get("tdarbandi"));
		mapadmlvl.put("p5w", p5.get(0).get("wdarbandi"));
		
		mapadmlvl.put("p6d", p6.get(0).get("tdarbandi"));
		mapadmlvl.put("p6w", p6.get(0).get("wdarbandi"));
		
		mapadmlvl.put("p7d", p7.get(0).get("tdarbandi"));
		mapadmlvl.put("p7w", p7.get(0).get("wdarbandi"));
		
		list.add(mapadmlvl);
			return Messenger.getMessenger().setData(list).success();

		
	}

	public ResponseEntity<Map<String, Object>> ethnicitycount() {
		String sql = "select tbl_ethnicity.name as ename,count(tbl_employee.id) as ecount from tbl_ethnicity left join tbl_employee on tbl_employee.ethnicity=tbl_ethnicity.id  group by tbl_ethnicity.id";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("name", t.get("ename"));
				mapadmlvl.put("count", t.get("ecount"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
		}
	}

}
