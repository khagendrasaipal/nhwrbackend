package org.saipal.workforce.admistr.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.transaction.Transactional;

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
	@Transactional
  public ResponseEntity<Map<String, Object>> store() {
		String offid= db.newIdInt();
		String sql = "";
		
		Office model = new Office();
		model.loadData(document);
		String sqls="select max(id) as mid from tbl_employee";
		  List<Tuple> emp = db.getResultList(sqls, Arrays.asList());
		  int eid=0;
		  if(emp.get(0).get("mid")==null) {
			   eid=0;
		  }else {
			   eid=Integer.parseInt(emp.get(0).get("mid").toString());
		  }
		 
		  String formattedStr = String.format("%08d", eid+1);
		  String code="NHWR"+formattedStr;
//		System.out.println("here");
		sql = "INSERT INTO tbl_employee (workforceid,darbandiid,detailsid,orgidint,nameen,namenp,address,email,mobile,emptype,apoint_date,att_date,education,qualification,council,level,council_no,created_by,pis,dob,gender,ethnicity,code) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			DbResponse rowEffect = db.execute(sql, Arrays.asList(model.workforceid,model.darbandiid,model.detailsid,model.orgidint,model.nameen, model.namenp, model.address,model.email,model.mobile,model.emptype,model.apoint_date,model.att_date,model.education,model.qualification,model.council,model.level, model.council_no,
				auth.getUserId(),model.pis,model.dob,model.gender,model.ethnicity,code));
//			System.out.println(rowEffect.getMessage());
			if (rowEffect.getErrorNumber() == 0) {
				String sql1="select * from tbl_darbandi_details where id=?";
				  List<Tuple> ofcstr = db.getResultList(sql1, Arrays.asList(model.detailsid));
				  String emptype=ofcstr.get(0).get("post_type").toString();
				  
				  String sql2="select * from tbl_darbandi where id=?";
				  List<Tuple> dar = db.getResultList(sql2, Arrays.asList(model.darbandiid));
				  int demp=Integer.parseInt(dar.get(0).get("dworking").toString())+1;
				  int kemp=Integer.parseInt(dar.get(0).get("kworking").toString())+1;
				  if(emptype.equals("1")) {
					  String sql3="update tbl_darbandi set dworking=? where id=?";
					  DbResponse rowEffect2 = db.execute(sql3,
								Arrays.asList(demp,model.darbandiid));
				  }
				  if(emptype.equals("2")) {
					  String sql4="update tbl_darbandi set kworking=? where id=?";
					  DbResponse rowEffect3 = db.execute(sql4,
								Arrays.asList(kemp,model.darbandiid));
				  }
				  
				  String sql5="update tbl_transfer set status=? where id=?";
				  DbResponse rowEffect5 = db.execute(sql5,
							Arrays.asList(request("tid")));
				
				return Messenger.getMessenger().success();
				
			} else {
				return Messenger.getMessenger().error();
			}
	}
  
  public ResponseEntity<Map<String, Object>> storeDarbandi() {
	  int post_no=Integer.parseInt(request("post_no"));
	  int post_no_karar=Integer.parseInt(request("post_no_karar"));
	  String darbandiid=request("darbandiid");
	  String sql1="select * from tbl_darbandi where id=?";
	  List<Tuple> ofcstr = db.getResultList(sql1, Arrays.asList(darbandiid));
	  String postid=ofcstr.get(0).get("post").toString();
	  String groupid=ofcstr.get(0).get("groupid").toString();
	  String subgroupid=ofcstr.get(0).get("subgroupid").toString();
	  String wid=ofcstr.get(0).get("workforce_id").toString();
	  String orgid=ofcstr.get(0).get("orgid").toString();
	  String pn=ofcstr.get(0).get("post_no").toString();
	  String pk=ofcstr.get(0).get("post_no_karar").toString();
	  int newpn=post_no+Integer.parseInt(pn);
	  int newpk=post_no_karar+Integer.parseInt(pk);
	  String sql2 = "UPDATE tbl_darbandi set post_no=?, post_no_karar=? where id=?";
	  DbResponse rowEffect2 = db.execute(sql2, Arrays.asList(newpn,newpk,darbandiid));
	  if (rowEffect2.getErrorNumber() == 0) {
		  
		  for(int k=1;k<=post_no;k++) {
				String sql3="INSERT INTO tbl_darbandi_details(darbandiid,workforce_id,orgid,groupid,subgroupid,post,post_type) values(?,?,?,?,?,?,?)";
				DbResponse rowEffect3 = db.execute(sql3,
						Arrays.asList(darbandiid,wid,orgid,groupid,subgroupid,postid,1));
			}
		  for(int l=1;l<=post_no_karar;l++) {
				String sql4="INSERT INTO tbl_darbandi_details(darbandiid,workforce_id,orgid,groupid,subgroupid,post,post_type) values(?,?,?,?,?,?,?)";
				DbResponse rowEffect4 = db.execute(sql4,
						Arrays.asList(darbandiid,wid,orgid,groupid,subgroupid,postid,2));
			}
			return Messenger.getMessenger().success();
			
		} else {
			return Messenger.getMessenger().error();
		}
	 
  }
  
  public ResponseEntity<Map<String, Object>> addpost() {
	 
	  String workforceid=request("workforceid");
	  String sql1="select * from tbl_workforce where id=?";
	  List<Tuple> ofcstr = db.getResultList(sql1, Arrays.asList(workforceid));
	  String orgid=ofcstr.get(0).get("org").toString();
	  try {
		  for(int i=1;i<=50;i++) {
				
				if("".equals(request("groupid"+i))) {
					
				}else {
					String did=db.newIdInt();
					String sql2="INSERT INTO tbl_darbandi(id,workforce_id,orgid,groupid,subgroupid,post,post_no,post_no_karar) values(?,?,?,?,?,?,?,?)";
					DbResponse rowEffect2 = db.execute(sql2,
							Arrays.asList(did,workforceid,orgid,request("groupid"+i),request("subgroupid"+i),request("post"+i),request("post_count"+i),request("post_count_karar"+i)));
					
					for(int k=1;k<=Integer.parseInt(request("post_count"+i));k++) {
						String sql3="INSERT INTO tbl_darbandi_details(darbandiid,workforce_id,orgid,groupid,subgroupid,post,post_type) values(?,?,?,?,?,?,?)";
						DbResponse rowEffect3 = db.execute(sql3,
								Arrays.asList(did,workforceid,orgid,request("groupid"+i),request("subgroupid"+i),request("post"+i),1));
					}
					for(int k=1;k<=Integer.parseInt(request("post_count_karar"+i));k++) {
						String sql4="INSERT INTO tbl_darbandi_details(darbandiid,workforce_id,orgid,groupid,subgroupid,post,post_type) values(?,?,?,?,?,?,?)";
						DbResponse rowEffect4 = db.execute(sql4,
								Arrays.asList(did,workforceid,orgid,request("groupid"+i),request("subgroupid"+i),request("post"+i),2));
					}
				}
			}
		  return Messenger.getMessenger().success();
		
	} catch (Exception e) {
		return Messenger.getMessenger().error();
	}
	  
  }


	

	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select id,cast(workforceid as CHAR) as workforceid,cast(darbandiid as CHAR) as darbandiid,cast(orgidint as CHAR) as orgidint,cast(detailsid as CHAR) as detailsid,pis,nameen,namenp,address,email,mobile,emptype,apoint_date,att_date,education,qualification,council,level,council_no,dob,gender,ethnicity from "+table+" where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql,Arrays.asList(id));
		return ResponseEntity.ok(data);
	}
	
	public ResponseEntity<Map<String, Object>> update(String id) {
		DbResponse rowEffect ;
		Office model = new Office();
		model.loadData(document);
		
			String sql = "UPDATE tbl_employee set workforceid=?,darbandiid=?,detailsid=?,orgidint=?,nameen=?,namenp=?,address=?,email=?,mobile=?,emptype=?,apoint_date=?,att_date=?,education=?,qualification=?,council=?,level=?,council_no=?,created_by=?,pis=?,dob=?,gender=?,ethnicity=? where id=?";
			 rowEffect = db.execute(sql,
					Arrays.asList(model.workforceid,model.darbandiid,model.detailsid,model.orgidint,model.nameen, model.namenp, model.address,model.email,model.mobile,model.emptype,model.apoint_date,model.att_date,model.education,model.qualification,model.council,model.level, model.council_no,
							auth.getUserId(),model.pis,model.dob,model.gender,model.ethnicity, id));
		
		
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
	@Transactional
	public ResponseEntity<Map<String, Object>> removeEmp(String id) {
		 String sql1="select * from tbl_employee where id=?";
		 List<Tuple> ofcstr = db.getResultList(sql1, Arrays.asList(id));
		 String did=ofcstr.get(0).get("detailsid").toString();
		 
		 String sql2="select * from tbl_darbandi_details where id=?";
		 List<Tuple> drb = db.getResultList(sql2, Arrays.asList(did));
		 String etype=drb.get(0).get("post_type").toString();
		 String darid=drb.get(0).get("darbandiid").toString();
		 
		 String sql3="select * from tbl_darbandi where id=?";
		 List<Tuple> work = db.getResultList(sql3, Arrays.asList(darid));
		 int dwork=Integer.parseInt(work.get(0).get("dworking").toString())-1;
		 int kwork=Integer.parseInt(work.get(0).get("kworking").toString())-1;
	
	String sql = "UPDATE tbl_employee set soft_delete=? where id=?";
	DbResponse rowEffect = db.execute(sql, Arrays.asList(1,id));
	if (rowEffect.getErrorNumber() == 0) {
		if(etype.equals("1")) {
			  String sql4="update tbl_darbandi set dworking=? where id=?";
			  DbResponse rowEffect2 = db.execute(sql4,
						Arrays.asList(dwork,darid));
		  }
		  if(etype.equals("2")) {
			  String sql4="update tbl_darbandi set kworking=? where id=?";
			  DbResponse rowEffect4 = db.execute(sql4,
						Arrays.asList(kwork,darid));
		  }
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
//		System.out.println(orgid);
		String sql = "select cast(tbl_darbandi.id as char) as did,tbl_darbandi.post, cast(workforce_id as CHAR) as workforceid,tbl_darbandi.orgid as orgidint,hfregistry.hf_name,tbl_samuha.namenp as officenamenp,tbl_upasamuha.namenp as officenameen,tbl_post.namenp as postname from tbl_darbandi left join hfregistry on hfregistry.id=tbl_darbandi.orgid join tbl_samuha on tbl_samuha.id=tbl_darbandi.groupid left join tbl_upasamuha on tbl_upasamuha.id=tbl_darbandi.subgroupid join tbl_post on tbl_post.id=tbl_darbandi.post where  tbl_darbandi.workforce_id=? group by tbl_darbandi.post";
		List<Tuple> ofcstr = db.getResultList(sql, Arrays.asList(orgid));
		List<Map<String, Object>> list = new ArrayList<>();
		if (!ofcstr.isEmpty()) {
			for (Tuple t : ofcstr) {
				
				Map<String, Object> mapOffice = new HashMap<>();
				mapOffice.put("workforceid", t.get("workforceid"));
				mapOffice.put("darbandiid", t.get("did"));
				mapOffice.put("orgidint", t.get("orgidint"));
				mapOffice.put("name", t.get("officenamenp"));
				mapOffice.put("officenameen", t.get("officenameen"));
				mapOffice.put("children",getChild(t.get("post").toString(),orgid));
				mapOffice.put("hfname", t.get("hf_name"));
				mapOffice.put("post", t.get("postname"));
				mapOffice.put("status",0);
				mapOffice.put("flag",0);
				list.add(mapOffice);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
//			return Messenger.getMessenger().error();
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
					mapOffice.put("posttype", "स्वीकृत दरबन्दी वाहेकको");
				}

				list.add(mapOffice);
			}
			return list;

		} else {
			return null;
		}
	}

	private Object getemp(String id, String orgid) {
		String sql = "select tbl_employee.id,cast(tbl_employee.workforceid as char) as workforceid,cast(tbl_employee.darbandiid as char) as darbandiid,cast(tbl_employee.detailsid as char) as detailsid,tbl_employee.orgidint,tbl_employee.nameen,tbl_employee.namenp,tbl_darbandi_details.post_type from tbl_employee join tbl_darbandi_details on tbl_darbandi_details.id=tbl_employee.detailsid where tbl_employee.detailsid=? and tbl_employee.soft_delete=?";
		List<Tuple> ofcstr = db.getResultList(sql, Arrays.asList(id,0));

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
					mapOffice.put("posttype", "स्वीकृत दरबन्दी वाहेकको");
				}
				
			
				list.add(mapOffice);
			}
			return list;

		} else {
			return null;
		}
	}

	public ResponseEntity<Map<String, Object>> removePost() {
		String wid=request("wid");
		String did=request("did");
		String detailsid=request("detailsid");
		String sql1="select * from tbl_darbandi_details where id=?";
		List<Tuple> ofcstr = db.getResultList(sql1, Arrays.asList(detailsid));
		String ptype=ofcstr.get(0).get("post_type").toString();
		String sql = "delete from tbl_darbandi_details where id  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(detailsid));
		
		String sql3="select * from tbl_darbandi where id=?";
		List<Tuple> tbl = db.getResultList(sql3, Arrays.asList(did));
		int pd=Integer. parseInt(tbl.get(0).get("post_no").toString());
		int pk=Integer. parseInt(tbl.get(0).get("post_no_karar").toString());
		int pdf=pd-1;
		int pkf=pk-1;
		
		String sql2="";
		if (rowEffect.getErrorNumber() == 0) {
			if(ptype.equals("1")) {
				 sql2 = "UPDATE tbl_darbandi set post_no="+pdf+" where id=?";
			}else {
				 sql2 = "UPDATE tbl_darbandi set post_no_karar="+pkf+" where id=?";
			}
			
			DbResponse rowEffect2 = db.execute(sql2, Arrays.asList(did));
			return Messenger.getMessenger().success();
			
		} else {
			return Messenger.getMessenger().error();
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
