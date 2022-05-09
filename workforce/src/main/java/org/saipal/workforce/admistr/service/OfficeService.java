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
import org.saipal.workforce.admistr.model.Office;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
@Component
public class OfficeService extends AutoService{

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	private String table = "tbl_employee";
	
	public ResponseEntity<Map<String, Object>> index() {
		String condition = "";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = Office.searchables();
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
				.select("cast(officeidint as CHAR) as officeidint,code,officenamenp, officenameen,officenamelc, approved, disabled")
				.sqlBody("from " + table + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}
	
  public ResponseEntity<Map<String, Object>> store() {
		String offid= db.newIdInt();
		String sql = "";
		
		Office model = new Office();
		model.loadData(document);
		
		sql = "INSERT INTO tbl_employee (workforceid,darbandiid,detailsid,orgidint,nameen,namenp,address,email,mobile,emptype,apoint_date,att_date,education,council,level,council_no,created_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			DbResponse rowEffect = db.execute(sql, Arrays.asList(model.workforceid,model.darbandiid,model.detailsid,model.orgidint,model.nameen, model.namenp, model.address,model.email,model.mobile,model.emptype,model.apoint_date,model.att_date,model.education,model.council,model.level, model.council_no,
				auth.getUserId()));
			if (rowEffect.getErrorNumber() == 0) {
				return Messenger.getMessenger().success();
				
			} else {
				return Messenger.getMessenger().error();
			}
	}

	

	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select id,cast(workforceid as CHAR) as workforceid,cast(darbandiid as CHAR) as darbandiid,cast(orgidint as CHAR) as orgidint,cast(detailsid as CHAR) as detailsid,nameen,namenp,address,email,mobile,emptype,apoint_date,att_date,education,council,level,council_no from "+table+" where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql,Arrays.asList(id));
		return ResponseEntity.ok(data);
	}
	
	public ResponseEntity<Map<String, Object>> update(String id) {
		DbResponse rowEffect ;
		Office model = new Office();
		model.loadData(document);
		
			String sql = "UPDATE tbl_employee set workforceid=?,darbandiid=?,detailsid=?,orgidint=?,nameen=?,namenp=?,address=?,email=?,mobile=?,emptype=?,apoint_date=?,att_date=?,education=?,council=?,level=?,council_no=?,created_by=? where id=?";
			 rowEffect = db.execute(sql,
					Arrays.asList(model.workforceid,model.darbandiid,model.detailsid,model.orgidint,model.nameen, model.namenp, model.address,model.email,model.mobile,model.emptype,model.apoint_date,model.att_date,model.education,model.council,model.level, model.council_no,
							auth.getUserId(), id));
		
		
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
			
		} else {
			return Messenger.getMessenger().error();
		}
		
	}
	
	public ResponseEntity<Map<String, Object>> destroy(String id) {

		
		String sql = "delete from admin_office_str where officeidint  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();
			
		} else {
			return Messenger.getMessenger().error();
		}

}

//	public ResponseEntity<Map<String, Object>> getTreeStructure() {
//		String orgid=request("orgid");
//		
//		String sql = "select cast(officeidint as CHAR) as officeidint,cast(orgidint as CHAR) as orgidint,officenameen,officenamenp from admin_office_str where disabled=? and approved=? and orgidint=? and parentofficeid is null";
//		List<Tuple> ofcstr = db.getResultList(sql, Arrays.asList(0, 1,orgid));
//		List<Map<String, Object>> list = new ArrayList<>();
//		if (!ofcstr.isEmpty()) {
//			for (Tuple t : ofcstr) {
//				
//				Map<String, Object> mapOffice = new HashMap<>();
//				mapOffice.put("officeidint", t.get("officeidint"));
//				mapOffice.put("orgidint", t.get("orgidint"));
//				mapOffice.put("name", t.get("officenamenp"));
//				mapOffice.put("officenameen", t.get("officenameen"));
//				mapOffice.put("children",getChild(t.get("officeidint").toString()));
//				list.add(mapOffice);
//			}
//			return Messenger.getMessenger().setData(list).success();
//
//		} else {
//			return Messenger.getMessenger().error();
//		}
//	}
	
	public ResponseEntity<Map<String, Object>> getTreeStructure() {
		String orgid=request("orgid");
		
		String sql = "select tbl_darbandi.id,tbl_darbandi.post, cast(workforce_id as CHAR) as workforceid,tbl_darbandi.orgid as orgidint,hfregistry.hf_name,tbl_samuha.namenp as officenamenp,tbl_upasamuha.namenp as officenameen,tbl_post.namenp as postname from tbl_darbandi left join hfregistry on hfregistry.id=tbl_darbandi.orgid join tbl_samuha on tbl_samuha.id=tbl_darbandi.groupid left join tbl_upasamuha on tbl_upasamuha.id=tbl_darbandi.subgroupid join tbl_post on tbl_post.id=tbl_darbandi.post where  tbl_darbandi.orgid=? or workforce_id=?";
		List<Tuple> ofcstr = db.getResultList(sql, Arrays.asList(orgid,orgid));
		List<Map<String, Object>> list = new ArrayList<>();
		if (!ofcstr.isEmpty()) {
			for (Tuple t : ofcstr) {
				
				Map<String, Object> mapOffice = new HashMap<>();
				mapOffice.put("workforceid", t.get("workforceid"));
				mapOffice.put("darbandiid", t.get("id"));
				mapOffice.put("orgidint", t.get("orgidint"));
				mapOffice.put("name", t.get("officenamenp"));
				mapOffice.put("officenameen", t.get("officenameen"));
				mapOffice.put("children",getChild(t.get("post").toString(),orgid));
				mapOffice.put("hfname", t.get("hf_name"));
				mapOffice.put("post", t.get("postname"));
				mapOffice.put("status",0);
				list.add(mapOffice);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	private Object getChild(String id,String orgid) {
		String sql = "select tbl_darbandi_details.id,tbl_darbandi_details.post,cast(tbl_darbandi_details.darbandiid as char) as darbandiid ,tbl_darbandi_details.post_type, cast(workforce_id as CHAR) as workforceid,tbl_darbandi_details.orgid as orgidint,tbl_post.namenp as postname from tbl_darbandi_details join tbl_post on tbl_post.id=tbl_darbandi_details.post where  (tbl_darbandi_details.orgid=? or tbl_darbandi_details.workforce_id=?) and post=?";
		List<Tuple> ofcstr = db.getResultList(sql, Arrays.asList(orgid,orgid,id));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!ofcstr.isEmpty()) {
			for (Tuple t : ofcstr) {
				
				Map<String, Object> mapOffice = new HashMap<>();
				mapOffice.put("workforceid", t.get("workforceid"));
				mapOffice.put("darbandiid", t.get("darbandiid"));
				mapOffice.put("detailsid", t.get("id"));
				mapOffice.put("orgidint", t.get("orgidint"));
				mapOffice.put("name", t.get("postname"));
				mapOffice.put("children",getemp(t.get("id").toString(),orgid));
				mapOffice.put("status",0);
				if("1".equals(t.get("post_type").toString())) {
					mapOffice.put("posttype", "स्वीकृत दरबन्दी ");
				}else {
					mapOffice.put("posttype", "करार दरबन्दी ");
				}

				list.add(mapOffice);
			}
			return list;

		} else {
			return null;
		}
	}

	private Object getemp(String id, String orgid) {
		String sql = "select tbl_employee.id,cast(tbl_employee.workforceid as char) as workforceid,cast(tbl_employee.darbandiid as char) as darbandiid,cast(tbl_employee.detailsid as char) as detailsid,tbl_employee.orgidint,tbl_employee.nameen,tbl_employee.namenp,tbl_darbandi_details.post_type from tbl_employee join tbl_darbandi_details on tbl_darbandi_details.id=tbl_employee.detailsid where tbl_employee.detailsid=?";
		List<Tuple> ofcstr = db.getResultList(sql, Arrays.asList(id));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!ofcstr.isEmpty()) {
			for (Tuple t : ofcstr) {
				
				Map<String, Object> mapOffice = new HashMap<>();
				mapOffice.put("empid", t.get("id"));
				mapOffice.put("workforceid", t.get("workforceid"));
				mapOffice.put("darbandiid", t.get("darbandiid"));
				mapOffice.put("detailsid", t.get("detailsid"));
				mapOffice.put("orgidint", t.get("orgidint"));
				mapOffice.put("name", t.get("namenp"));
				mapOffice.put("children",null);
				mapOffice.put("status",1);
				if("1".equals(t.get("post_type").toString())) {
					mapOffice.put("posttype", "स्वीकृत दरबन्दी ");
				}else {
					mapOffice.put("posttype", "करार दरबन्दी ");
				}
				
			
				list.add(mapOffice);
			}
			return list;

		} else {
			return null;
		}
	}

//	public ResponseEntity<Map<String, Object>> getorgList() {
//		String sql = "select cast(officeidint as CHAR) as officeidint,cast(orgidint as CHAR) as orgidint,officenameen,officenamenp from admin_office_str where disabled=? and approved=? and orgidint=? and parentofficeid is null";
//		List<Tuple> ofcstr = db.getResultList(sql, Arrays.asList(0, 1,orgid));
//		List<Map<String, Object>> list = new ArrayList<>();
//		if (!ofcstr.isEmpty()) {
//			for (Tuple t : ofcstr) {
//				
//				Map<String, Object> mapOffice = new HashMap<>();
//				mapOffice.put("officeidint", t.get("officeidint"));
//				mapOffice.put("orgidint", t.get("orgidint"));
//				mapOffice.put("name", t.get("officenamenp"));
//				mapOffice.put("officenameen", t.get("officenameen"));
//				mapOffice.put("children",getChild(t.get("officeidint").toString()));
//				list.add(mapOffice);
//			}
//			return Messenger.getMessenger().setData(list).success();
//
//		} else {
//			return Messenger.getMessenger().error();
//		}
//	}

	

}
