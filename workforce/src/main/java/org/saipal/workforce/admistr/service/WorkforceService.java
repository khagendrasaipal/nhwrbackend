package org.saipal.workforce.admistr.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.internal.build.AllowSysOut;
import org.saipal.fmisutil.auth.Authenticated;
import org.saipal.fmisutil.service.AutoService;
import org.saipal.fmisutil.util.DB;
import org.saipal.fmisutil.util.DbResponse;
import org.saipal.fmisutil.util.Messenger;
import org.saipal.fmisutil.util.Paginator;
import org.saipal.workforce.admistr.model.Workforce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
@Component
public class WorkforceService extends AutoService {

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	private String table = "tbl_workforce";

	 CSVPrinter csvPrinter;
	 public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	  static String[] HEADERs = { "Id", "Title", "Description", "Published" };
	  static String SHEET = "Tutorials";

	public ResponseEntity<Map<String, Object>> index() {
		String condition = "";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = Workforce.searchables();
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
		if(session("role").equals("superuser")) {
			
		}else {
			condition= condition + " where tbl_workforce.created_by=" +auth.getUserId();
		}
		
		
		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage"))
				.setOrderBy(sort)
				.select("cast(tbl_workforce.id as char) as id,admin_province.nameen as province,admin_local_level_structure.namenp as vcname,admin_district.nameen as district,hfregistry.hf_name,tbl_office.namenp as orgname ")
				.sqlBody("from " + table + " left join admin_province on admin_province.pid=tbl_workforce.provinceid  left join admin_district on admin_district.districtid=tbl_workforce.districtid"
						+ " left join hfregistry on hfregistry.id=tbl_workforce.org left join tbl_office on tbl_office.id=tbl_workforce.officeid left join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid  " +condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> store() {
		String pid=db.newIdInt();
		
		String sql = "";
		Workforce model = new Workforce();
		model.loadData(document);
		String sql1="select * from tbl_workforce where org=? and org!=?";
		List<Tuple> ofcstr = db.getResultList(sql1, Arrays.asList(model.org,0));
		if(model.admlvl.equals("1")) {
			String sql5="select * from tbl_workforce where officeid=?";
			List<Tuple> orgs = db.getResultList(sql5, Arrays.asList(model.officeid));
			if(!orgs.isEmpty()) {
				return Messenger.getMessenger().setMessage("Data already Inserted for this organization. Please update From darbandi.").error();
			}
		}
		if(model.admlvl.equals("2")) {
			String sql5="select * from tbl_workforce where officeid=? and provinceid=?";
			List<Tuple> orgs = db.getResultList(sql5, Arrays.asList(model.officeid,model.provinceid));
			if(!orgs.isEmpty()) {
				return Messenger.getMessenger().setMessage("Data already Inserted for this organization. Please update From darbandi.").error();
			}
		}
		if(model.admlvl.equals("3")) {
			String sql5="select * from tbl_workforce where officeid=? and districtid=?";
			List<Tuple> orgs = db.getResultList(sql5, Arrays.asList(model.officeid,model.districtid));
			if(!orgs.isEmpty()) {
				return Messenger.getMessenger().setMessage("Data already Inserted for this organization. Please update From darbandi.").error();
			}
		}
		if(model.admlvl.equals("4")) {
			String sql5="select * from tbl_workforce where palika=? and muncid=?";
			List<Tuple> orgs = db.getResultList(sql5, Arrays.asList(model.palika,model.palika));
			if(!orgs.isEmpty()) {
				return Messenger.getMessenger().setMessage("Data already Inserted for this organization. Please update From darbandi.").error();
			}
		}
		if (ofcstr.isEmpty()) {
			DbResponse rowEffect;
			sql = "INSERT INTO tbl_workforce (id,orgtype,admlvl,provinceid,districtid,palika,ward,org,officeid,muncid,authority,authlevel,ownership,ftype,created_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			if(model.admlvl.equals("4")){
				 rowEffect = db.execute(sql,
						Arrays.asList(pid,model.orgtype,model.admlvl,model.provinceid, model.districtid,model.palika,model.ward,model.org,model.officeid,model.palika,model.authority,model.authlevel,model.ownership,model.ftype,auth.getUserId()));
			}else {
				 rowEffect = db.execute(sql,
						Arrays.asList(pid,model.orgtype,model.admlvl,model.provinceid, model.districtid,model.palika,model.ward,model.org,model.officeid,0,model.authority,model.authlevel,model.ownership,model.ftype,auth.getUserId()));
			}
			
			if (rowEffect.getErrorNumber() == 0) {
				for(int i=1;i<=50;i++) {
					
					if("".equals(request("groupid"+i))) {
						
					}else {
						String did=db.newIdInt();
						String sql2="INSERT INTO tbl_darbandi(id,workforce_id,orgid,groupid,subgroupid,post,post_no,post_no_karar) values(?,?,?,?,?,?,?,?)";
						DbResponse rowEffect2 = db.execute(sql2,
								Arrays.asList(did,pid,model.org,request("groupid"+i),request("subgroupid"+i),request("post"+i),request("post_count"+i),request("post_count_karar"+i)));
						for(int k=1;k<=Integer.parseInt(request("post_count"+i));k++) {
							String sql3="INSERT INTO tbl_darbandi_details(darbandiid,workforce_id,orgid,groupid,subgroupid,post,post_type) values(?,?,?,?,?,?,?)";
							DbResponse rowEffect3 = db.execute(sql3,
									Arrays.asList(did,pid,model.org,request("groupid"+i),request("subgroupid"+i),request("post"+i),1));
						}
						for(int k=1;k<=Integer.parseInt(request("post_count_karar"+i));k++) {
							String sql4="INSERT INTO tbl_darbandi_details(darbandiid,workforce_id,orgid,groupid,subgroupid,post,post_type) values(?,?,?,?,?,?,?)";
							DbResponse rowEffect4 = db.execute(sql4,
									Arrays.asList(did,pid,model.org,request("groupid"+i),request("subgroupid"+i),request("post"+i),2));
						}
					}
				}
				return Messenger.getMessenger().setData(pid).success();

			} else {
				return Messenger.getMessenger().error();
			}
		}else {
			return Messenger.getMessenger().setMessage("Data already Inserted for this organization. Please update From darbandi.").error();
		}
		
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select id,code,name from "
				+ table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		DbResponse rowEffect;
		Workforce model = new Workforce();
		model.loadData(document);

		String sql = "UPDATE tbl_workforce set provinceid=?,districtid=?,palika=?,ward=?,org=?,authority=?,authlevel=?,ownership=?,ftype=? where id=?";
		rowEffect = db.execute(sql,
				Arrays.asList(model.provinceid, model.districtid,model.palika,model.ward,model.org,model.authority,model.authlevel,model.ownership,model.ftype,id));

		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}

	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {

		String sql = "delete from tbl_workforce where id  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		
		String sql1 = "delete from tbl_darbandi where workforce_id  = ?";
		DbResponse rowEffect1 = db.execute(sql1, Arrays.asList(id));
		String sql2 = "delete from tbl_darbandi_details where workforce_id  = ?";
		DbResponse rowEffect2 = db.execute(sql2, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}


	public void getcsv(HttpServletResponse response) throws IOException {
		String id=request("id");
		String filename = "healthworkforce.csv";
//		String sql = "select COALESCE(hfregistry.hf_name,'') as hf_name,COALESCE(tbl_level.namenp,'') as level,COALESCE(tbl_emptype.namenp,'') as emptype,COALESCE(tbl_council.namenp,'') as council,COALESCE(tbl_workforce.orgname,'')as orgname,tbl_samuha.namenp as groupname,COALESCE(tbl_upasamuha.namenp,'') as subgroup,tbl_post.namenp as postname,COALESCE(tbl_employee.namenp,'') as empname,"
//				+ " COALESCE(tbl_employee.address,'') as address,COALESCE(tbl_employee.email,'') as email,COALESCE(tbl_employee.mobile,'') as mobile,COALESCE(tbl_employee.apoint_date,'') as apoint_date,COALESCE(tbl_employee.att_date,'') as att_date,COALESCE(tbl_employee.council_no,'') as council_no from tbl_darbandi_details as td"
//				+ " left join hfregistry on hfregistry.id=td.orgid join tbl_workforce on tbl_workforce.id=td.workforce_id join tbl_samuha on tbl_samuha.id=td.groupid left join tbl_upasamuha on "
//				+ " tbl_upasamuha.id=td.subgroupid join tbl_post on tbl_post.id=td.post  left join tbl_employee on tbl_employee.detailsid=td.id left join tbl_emptype on tbl_emptype.id=tbl_employee.emptype left join tbl_level on tbl_level.id=tbl_employee.level left join tbl_council on tbl_council.id=tbl_employee.council where td.workforce_id=?";

		String sql = "select COALESCE(hfregistry.hf_name,'') as hf_name,COALESCE(tbl_level.namenp,'') as level,COALESCE(tbl_emptype.namenp,'') as emptype,COALESCE(tbl_council.namenp,'') as council,COALESCE(tbl_office.namenp,'')as orgname,tbl_samuha.namenp as groupname,COALESCE(tbl_upasamuha.namenp,'') as subgroup,tbl_post.namenp as postname,COALESCE(tbl_employee.namenp,'') as empname,"
				+ " COALESCE(tbl_employee.address,'') as address,COALESCE(tbl_employee.email,'') as email,COALESCE(tbl_employee.mobile,'') as mobile,COALESCE(tbl_employee.apoint_date,'') as apoint_date,COALESCE(tbl_employee.att_date,'') as att_date,COALESCE(tbl_employee.council_no,'') as council_no from tbl_darbandi_details as td"
				+ "  left join hfregistry on hfregistry.id=td.orgid join tbl_workforce on tbl_workforce.id=td.workforce_id left join tbl_office on tbl_office.id=tbl_workforce.officeid join tbl_samuha on tbl_samuha.id=td.groupid left join tbl_upasamuha on "
				+ " tbl_upasamuha.id=td.subgroupid join tbl_post on tbl_post.id=td.post  left join tbl_employee on tbl_employee.detailsid=td.id left join tbl_emptype on tbl_emptype.id=tbl_employee.emptype left join tbl_level on tbl_level.id=tbl_employee.level left join tbl_council on tbl_council.id=tbl_employee.council where td.workforce_id=?";
System.out.println(sql);
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(id));
		
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("records");

		int rowCount = 0;
		Row row = sheet.createRow(rowCount++);
		
		Cell cell = row.createCell(0);
		cell.setCellValue("Hf Name");
		
		cell = row.createCell(1);
		cell.setCellValue("Orgname");

		cell = row.createCell(2);
		cell.setCellValue("Group");

		cell = row.createCell(3);
		cell.setCellValue("Subgroup");
		
		cell = row.createCell(4);
		cell.setCellValue("Employename");
		
		cell = row.createCell(5);
		cell.setCellValue("Address");
		
		cell = row.createCell(6);
		cell.setCellValue("email");

		cell = row.createCell(7);
		cell.setCellValue("Mobile");
		
		cell = row.createCell(8);
		cell.setCellValue("Appoint Date");
		
		cell = row.createCell(9);
		cell.setCellValue("Attend Date");
		
		cell = row.createCell(10);
		cell.setCellValue("Level");
		
		cell = row.createCell(11);
		cell.setCellValue("Council");
		
		cell = row.createCell(12);
		cell.setCellValue("Council no");
		
		cell = row.createCell(13);
		cell.setCellValue("Emp Type");

			for (Tuple t : admlvl) {
				row = sheet.createRow(rowCount++);

				int columnCount = 0;
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("hf_name")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("orgname")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("groupname")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("subgroup")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("empname")+" ");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("address")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("email")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("mobile")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("apoint_date")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("att_date")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("level")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("council")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("council_no")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("emptype")+"");
			}
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=records.xlsx";
			response.setHeader(headerKey, headerValue);
			
			ServletOutputStream outputStream = response.getOutputStream();
			wb.write(outputStream);
			wb.close();

			outputStream.close();

		
}
	
	public void downloadreportold(HttpServletResponse response) throws IOException {
		String aid=request("admid");
		String pid=request("pid");
		String oid=request("orgid");
		String filename = "healthworkforce.csv";
		String sql="";
		if(aid.equals("1")) {
			if(oid.equals("0")) {
				 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=1 group by tbl_darbandi.post";
			}else {
				 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+" group by tbl_darbandi.post";
			}
		}
		
		if(aid.equals("2")) {
			if(pid.equals("0")) {
				sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 group by tbl_darbandi.post";
			}else {
				if(oid.equals("0")) {
					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
				}else {
					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.officeid="+oid+" group by tbl_darbandi.post";
				}
			}
		}
		
		if(aid.equals("4")) {
			if(oid.equals("0")) {
				 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 group by tbl_darbandi.post";
			}else {
				 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+oid+" group by tbl_darbandi.post";
			}
		}
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
		
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("records");

		int rowCount = 0;
		Row row = sheet.createRow(rowCount++);
		
		Cell cell = row.createCell(0);
		cell.setCellValue("पद");
		
		cell = row.createCell(1);
		cell.setCellValue("स्वीकृत दरबन्दी संख्या");

		cell = row.createCell(2);
		cell.setCellValue("स्वीकृत दरबन्दी वाहेकको संख्या");

		cell = row.createCell(3);
		cell.setCellValue("जम्मा संख्या");
		
		cell = row.createCell(4);
		cell.setCellValue("स्थायी");
		

		cell = row.createCell(5);
		cell.setCellValue("समिति स्थायी");
		
		cell = row.createCell(6);
		cell.setCellValue("अस्थायी");

		cell = row.createCell(7);
		cell.setCellValue("छात्रबृत्ति करार");

		cell = row.createCell(8);
		cell.setCellValue("कार्यक्रम करार");
		
		cell = row.createCell(9);
		cell.setCellValue("दरबन्दी करार");

		cell = row.createCell(10);
		cell.setCellValue("समिति करार");
		
		cell = row.createCell(11);
		cell.setCellValue("अन्य करार");
		
		
		
		
		   Map<Integer, Object> map;
	        map = new HashMap<Integer, Object>();
	        
			for (Tuple t : admlvl) {
				List<Map<String, Object>> list = new ArrayList<>();
		        Map<String, Object> mapadmlvl = new HashMap<>();
				for(int i=1;i<=8;i++) {
					mapadmlvl.put(i+"",getKarar(t.get("post").toString(),aid,pid,oid,i));
					
				}
				list.add(mapadmlvl);
			
				row = sheet.createRow(rowCount++);

				int columnCount = 0;
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("postname")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("sdarbandi")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("kdarbandi")+"");
				cell = row.createCell(columnCount++);
				cell.setCellValue(Integer.parseInt(t.get("kdarbandi").toString())+Integer.parseInt(t.get("sdarbandi").toString()));
				cell = row.createCell(columnCount++);
				cell.setCellValue(list.get(0).get("1").toString());
				cell = row.createCell(columnCount++);
				cell.setCellValue(list.get(0).get("8").toString());
				cell = row.createCell(columnCount++);
				cell.setCellValue(list.get(0).get("2").toString());
				cell = row.createCell(columnCount++);
				cell.setCellValue(list.get(0).get("3").toString());
				cell = row.createCell(columnCount++);
				cell.setCellValue(list.get(0).get("4").toString());
				cell = row.createCell(columnCount++);
				cell.setCellValue(list.get(0).get("5").toString());
				cell = row.createCell(columnCount++);
				cell.setCellValue(list.get(0).get("6").toString());
				cell = row.createCell(columnCount++);
				cell.setCellValue(list.get(0).get("7").toString());
				cell = row.createCell(columnCount++);
				
			}
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=records.xlsx";
			response.setHeader(headerKey, headerValue);
			
			ServletOutputStream outputStream = response.getOutputStream();
			wb.write(outputStream);
			wb.close();

			outputStream.close();

		
}
	
	public void downloadreport(HttpServletResponse response) throws IOException {
//		admid=4&pid=3&hfid=0&did=28&munc=100612250354650800&ofc=0&orgtype=1
		String orgtype=request("orgtype");
		String aid=request("admid");
		String pid=request("pid");
		String did=request("did");
		String munc=request("munc");
		String oid=request("ofc");
		String hfid=request("hfid");
		String filename = "healthworkforce.csv";
		String sql="";
		
		if("1".equals(orgtype)) {
			if(aid.equals("1")) {
				if(oid.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=1";				
					Tuple t = db.getSingleResult(q1);
					sql = "select tbl_office.namenp as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join tbl_office on tbl_office.id=tbl_workforce.officeid where tbl_workforce.admlvl=1 group by tbl_workforce.officeid";
					
//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=1 group by tbl_darbandi.post";
				}else {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select tbl_office.namenp as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join tbl_office on tbl_office.id=tbl_workforce.officeid where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+" ";
//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+" group by tbl_darbandi.post";
				}
			}else if(aid.equals("2")) {
			if(pid.equals("0")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2";				
				Tuple t = db.getSingleResult(q1);
				sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 group by tbl_workforce.provinceid";
				}else {
				if(oid.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+"";
					
					
//					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
				}else {
					
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" and  tbl_workforce.officeid="+oid+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" and  tbl_workforce.officeid="+oid+"";
//					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.officeid="+oid+" group by tbl_darbandi.post";
				}
			}
		}else {
			if(pid.equals("0")) {
				if(did.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 group by tbl_workforce.muncid";

//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 group by tbl_darbandi.post";
				}else {
					if(munc.equals("0")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+"";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.muncid";

//						 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_darbandi.post";
					}else {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+"";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_workforce.muncid";

//						 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_darbandi.post";
					}
				}
			
			
		}else {
			if(did.equals("0")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+"";				
				Tuple t = db.getSingleResult(q1);
				sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+" group by tbl_workforce.muncid";

//				 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
			}else {
				if(munc.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.muncid";

//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_darbandi.post";
				}else {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+"";

					
//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_darbandi.post";
				}
				
			}
			
			
		}
	}
		}else {
			if(pid.equals("0")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4";				
				Tuple t = db.getSingleResult(q1);
				sql = "select hfregistry.hf_name as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join hfregistry on hfregistry.id=tbl_workforce.org where tbl_workforce.orgtype=4 group by tbl_workforce.org";
				
//				sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=4 group by tbl_darbandi.post ";
			}else {
				if(did.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.provinceid="+pid+"";				
					Tuple t = db.getSingleResult(q1);
					sql = "select hfregistry.hf_name as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join hfregistry on hfregistry.id=tbl_workforce.org where tbl_workforce.orgtype=4 and tbl_workforce.provinceid="+pid+" group by tbl_workforce.org";
					
				}else {
					if(munc.equals("0")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.districtid="+did+"";				
						Tuple t = db.getSingleResult(q1);
						sql = "select hfregistry.hf_name as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join hfregistry on hfregistry.id=tbl_workforce.org where tbl_workforce.orgtype=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.org";
						
					}else {
						if(hfid.equals("0")) {
							String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.palika="+munc+"";				
							Tuple t = db.getSingleResult(q1);
							sql = "select hfregistry.hf_name as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join hfregistry on hfregistry.id=tbl_workforce.org where tbl_workforce.orgtype=4 and tbl_workforce.palika="+munc+" group by tbl_workforce.org";
							
						}else {
							String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id join tbl_workforce on tbl_workforce.id=td.workforce_id where tbl_workforce.orgtype=4 and tbl_workforce.org="+hfid+"";				
							Tuple t = db.getSingleResult(q1);
							sql = "select hfregistry.hf_name as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join hfregistry on hfregistry.id=tbl_workforce.org where tbl_workforce.orgtype=4 and tbl_workforce.org="+hfid+" group by tbl_workforce.org";
							
						}
					}
				}
			}
		}
		System.out.println(sql);

		String sq2="select namenp,id from tbl_post order by level";
		List<Tuple> posts = db.getResultList(sq2, Arrays.asList());
		List<Map<String, Object>> lists = new ArrayList<>();
		Map<String, Object> mappost = new HashMap<>();
		for (Tuple t : posts) {	
			mappost.put(t.get("id").toString(), t.get("namenp"));
		}
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
//		System.out.println(admlvl.get(0).toString());
		List<TupleElement<?>> ltpe;
		List<String> flds = new ArrayList<>();
		if(!admlvl.isEmpty()) {
		 ltpe = admlvl.get(0).getElements();
		 
			for(TupleElement tp : ltpe) {
				if(tp.getAlias().startsWith("d")) {
					flds.add(tp.getAlias().substring(1));
					
				}
			}
		}else {
			ltpe=null;
//			flds=null;
		}
		
//		System.out.println(flds);
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("records");

		int rowCount = 0;
		Row row = sheet.createRow(rowCount++);
		
		Cell cell = row.createCell(0);
		
		
		
		
		
		   Map<Integer, Object> map;
	        map = new HashMap<Integer, Object>();
	        int columnCount = 0;
	        int columnCountnew = 0;
			row = sheet.createRow(rowCount++);
			cell = row.createCell(columnCount++);
			cell.setCellValue("Post");
			
			for(String fld:flds) {
				sheet.addMergedRegion(new CellRangeAddress(
						rowCount-1, //first row (0-based)
						rowCount-1, //last row  (0-based)
						columnCount, //first column (0-based)
						columnCount+1  //last column  (0-based)
				));
				cell = row.createCell(columnCount++);
				cell.setCellValue(mappost.get(fld)+"");
				columnCount++;
				
			}
			
			
			
			row = sheet.createRow(rowCount++);
			cell = row.createCell(columnCountnew++);
			if(aid.equals("1") || orgtype.equals("4")) {
				cell.setCellValue("Organization/PostType");
			}
			if(aid.equals("2")) {
				cell.setCellValue("Province/PostType");
			}
			if(aid.equals("4")) {
				cell.setCellValue("Local Level/PostType");
			}
//			cell.setCellValue("Province/PostType");
			for(String fld:flds) {
				cell = row.createCell(columnCountnew++);
				cell.setCellValue("दरबन्दी");
				cell = row.createCell(columnCountnew++);
				cell.setCellValue("कार्यरत");
			}

	        
			for (Tuple t : admlvl) {
				List<Map<String, Object>> list = new ArrayList<>();
		        Map<String, Object> mapadmlvl = new HashMap<>();
		   
				row = sheet.createRow(rowCount++);
				columnCount = 0;
				cell = row.createCell(columnCount++);
				cell.setCellValue(t.get("provincename")+"");
				for(String fld:flds) {
					cell = row.createCell(columnCount++);
					cell.setCellValue(t.get("d"+fld)+"");
					cell = row.createCell(columnCount++);
					cell.setCellValue(Integer.parseInt(t.get("k"+fld).toString())+Integer.parseInt(t.get("d"+fld).toString()));
				}
			
			}
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=records.xlsx";
			response.setHeader(headerKey, headerValue);
			
			ServletOutputStream outputStream = response.getOutputStream();
			wb.write(outputStream);
			wb.close();

			outputStream.close();

		
}

	private Object getPost(String pid, int i) {
		String sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
		return admlvl;
	}

	private Object getKarar(String post, String aid, String pid, String oid,int emptype) {
		String sql="";
		if(aid.equals("1")) {
			if(oid.equals("0")) {
				 sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=1 and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
			}else {
				 sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=1 and and tbl_darbandi.post= "+post+" tbl_workforce.officeid="+oid+" and tbl_employee.emptype="+emptype;
			}
		}
		
		if(aid.equals("4")) {
			if(oid.equals("0")) {
				 sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=4 and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
			}else {
				 sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=4  and tbl_darbandi.post= "+post+" and tbl_workforce.muncid="+oid+" and tbl_employee.emptype="+emptype;
			}
		}
		
		if(aid.equals("2")) {
			if(pid.equals("0")) {
				sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=2 and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
			}else {
				if(oid.equals("0")) {
					sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
				}else {
					sql="select count(tbl_employee.id) as karar from tbl_employee join tbl_darbandi on tbl_darbandi.id=tbl_employee.darbandiid  join tbl_workforce on tbl_workforce.id=tbl_employee.workforceid where tbl_workforce.admlvl=2 and tbl_workforce.officeid="+oid+" and tbl_darbandi.post= "+post+" and tbl_employee.emptype="+emptype;
				}
			}
		}
		
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
		
		return admlvl.get(0).get("karar");
	}

	public List<Tuple> gettable() {
		String orgtype=request("orgtype");
		String aid=request("admid");
		String pid=request("pid");
		String did=request("did");
		String munc=request("munc");
		String oid=request("ofc");
		String hfid=request("hfid");
		String filename = "healthworkforce.csv";
		String sql="";
		
		if("1".equals(orgtype)) {
			if(aid.equals("1")) {
				if(oid.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
					Tuple t = db.getSingleResult(q1);
					sql = "select tbl_office.namenp as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join tbl_office on tbl_office.id=tbl_workforce.officeid where tbl_workforce.admlvl=1 group by tbl_workforce.officeid";
					
//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=1 group by tbl_darbandi.post";
				}else {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
					Tuple t = db.getSingleResult(q1);
					sql = "select tbl_office.namenp as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join tbl_office on tbl_office.id=tbl_workforce.officeid where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+" ";
//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=1 and tbl_workforce.officeid="+oid+" group by tbl_darbandi.post";
				}
			}else if(aid.equals("2")) {
			if(pid.equals("0")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
				Tuple t = db.getSingleResult(q1);
				sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 group by tbl_workforce.provinceid";
				}else {
				if(oid.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+"";
					
					
//					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
				}else {
					
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_province.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_province on admin_province.pid=tbl_workforce.provinceid where tbl_workforce.admlvl=2 and tbl_workforce.provinceid="+pid+" and  tbl_workforce.officeid="+oid+"";
//					sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=2 and tbl_workforce.officeid="+oid+" group by tbl_darbandi.post";
				}
			}
		}else {
			if(pid.equals("0")) {
				if(did.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 group by tbl_workforce.muncid";

//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 group by tbl_darbandi.post";
				}else {
					if(munc.equals("0")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.muncid";

//						 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_darbandi.post";
					}else {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
						Tuple t = db.getSingleResult(q1);
						sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_workforce.muncid";

//						 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_darbandi.post";
					}
				}
			
			
		}else {
			if(did.equals("0")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
				Tuple t = db.getSingleResult(q1);
				sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+" group by tbl_workforce.muncid";

//				 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.provinceid="+pid+" group by tbl_darbandi.post";
			}else {
				if(munc.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.muncid";

//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.districtid="+did+" group by tbl_darbandi.post";
				}else {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
					Tuple t = db.getSingleResult(q1);
					sql = "select admin_local_level_structure.nameen as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join admin_local_level_structure on admin_local_level_structure.vcid=tbl_workforce.muncid where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+"";

					
//					 sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.admlvl=4 and tbl_workforce.muncid="+munc+" group by tbl_darbandi.post";
				}
				
			}
			
			
		}
	}
		}else {
			if(pid.equals("0")) {
				String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
				Tuple t = db.getSingleResult(q1);
				sql = "select hfregistry.hf_name as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join hfregistry on hfregistry.id=tbl_workforce.org where tbl_workforce.orgtype=4 group by tbl_workforce.org";
				
//				sql="select sum(post_no) as sdarbandi,sum(post_no_karar) as kdarbandi,post,tbl_post.namenp as postname from tbl_darbandi join tbl_post on tbl_post.id=tbl_darbandi.post join tbl_workforce on tbl_workforce.id=tbl_darbandi.workforce_id where tbl_workforce.orgtype=4 group by tbl_darbandi.post ";
			}else {
				if(did.equals("0")) {
					String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
					Tuple t = db.getSingleResult(q1);
					sql = "select hfregistry.hf_name as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join hfregistry on hfregistry.id=tbl_workforce.org where tbl_workforce.orgtype=4 and tbl_workforce.provinceid="+pid+" group by tbl_workforce.org";
					
				}else {
					if(munc.equals("0")) {
						String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
						Tuple t = db.getSingleResult(q1);
						sql = "select hfregistry.hf_name as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join hfregistry on hfregistry.id=tbl_workforce.org where tbl_workforce.orgtype=4 and tbl_workforce.districtid="+did+" group by tbl_workforce.org";
						
					}else {
						if(hfid.equals("0")) {
							String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
							Tuple t = db.getSingleResult(q1);
							sql = "select hfregistry.hf_name as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join hfregistry on hfregistry.id=tbl_workforce.org where tbl_workforce.orgtype=4 and tbl_workforce.palika="+munc+" group by tbl_workforce.org";
							
						}else {
							String q1="SELECT GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no else 0 end) AS ',CONCAT('''d',tp.id,'''') ) ORDER BY tp.id) AS `dcases`,GROUP_CONCAT(DISTINCT CONCAT('sum(case when tbd.post = ''',tp.id,''' then post_no_karar else 0 end) AS ',CONCAT('''k',tp.id,'''') ) ORDER BY tp.id) AS `kcases` FROM `tbl_post` tp JOIN tbl_darbandi td ON td.post=tp.id";				
							Tuple t = db.getSingleResult(q1);
							sql = "select hfregistry.hf_name as provincename,"+t.get("dcases")+","+t.get("kcases")+" "+"from tbl_darbandi tbd join tbl_post on tbl_post.id=tbd.post join tbl_workforce on tbl_workforce.id=tbd.workforce_id join hfregistry on hfregistry.id=tbl_workforce.org where tbl_workforce.orgtype=4 and tbl_workforce.org="+hfid+" group by tbl_workforce.org";
							
						}
					}
				}
			}
		}

		String sq2="select namenp,id from tbl_post order by level";
		List<Tuple> posts = db.getResultList(sq2, Arrays.asList());
		List<Map<String, Object>> lists = new ArrayList<>();
		Map<String, Object> mappost = new HashMap<>();
		for (Tuple t : posts) {	
			mappost.put(t.get("id").toString(), t.get("namenp"));
		}
//		Map<String, Object> data= (Map<String, Object>) db.getResultList(sql, Arrays.asList());
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList());
//		System.out.println(admlvl.get(0).toString());
		List<TupleElement<?>> ltpe;
		List<String> flds = new ArrayList<>();
		if(!admlvl.isEmpty()) {
		 ltpe = admlvl.get(0).getElements();
		 
			for(TupleElement tp : ltpe) {
				if(tp.getAlias().startsWith("d")) {
					flds.add(tp.getAlias().substring(1));
				}
			}
		}else {
			ltpe=null;
//			flds=null;
		}
		
//		System.out.println(flds);
		List<Map<String, Object>> list = new ArrayList<>();
		for (Tuple t : admlvl) {
//			
//	        Map<String, Object> mapadmlvl = new HashMap<>();
//	        mapadmlvl.put("name", t.get("provincename"));
//	        mapadmlvl.put(admlvl.get(0).getElements().get(0).getAlias(), t.get(admlvl.get(0).getElements().get(0).getAlias()));
//	        list.add(mapadmlvl);
//	        admlvl.get(0).get(tp.getAlias());
//			for(String fld:flds) {
				
//				 mapadmlvl.put("name", t.get("provincename"));
//				 mapadmlvl.put(t.get("d"+fld).toString(),t.get("d"+fld));
//				 mapadmlvl.put(t.get("d"+fld).toString(),t.get("d"+fld));
//				 mapadmlvl.put(t.get("k"+fld)+"",Integer.parseInt(t.get("k"+fld).toString())+Integer.parseInt(t.get("d"+fld).toString()));
//				 list.add(mapadmlvl);
//			}
		
		}
		return admlvl;
			
//			
//		Map<String, Object> datas = new HashMap<>();
//			datas.put("post", mappost);
//			datas.put("record", list);
//			
//			
//			return Messenger.getMessenger().setData(datas).success();

		
	}
	
	
}
