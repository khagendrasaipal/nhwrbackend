package org.saipal.workforce.admistr.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
		
		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page")).setPerPage(request("perPage"))
				.setOrderBy(sort)
				.select("cast(tbl_workforce.id as char) as id,admin_province.nameen as province,admin_district.nameen as district,hfregistry.hf_name,orgname ")
				.sqlBody("from " + table + " join admin_province on admin_province.pid=tbl_workforce.provinceid  join admin_district on admin_district.districtid=tbl_workforce.districtid"
						+ " left join hfregistry on hfregistry.id=tbl_workforce.org " +condition).paginate();
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
		if (ofcstr.isEmpty()) {
			sql = "INSERT INTO tbl_workforce (id,orgtype,provinceid,districtid,palika,ward,org,orgname,authority,authlevel,ownership,ftype,created_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
			DbResponse rowEffect = db.execute(sql,
					Arrays.asList(pid,model.orgtype,model.provinceid, model.districtid,model.palika,model.ward,model.org,model.orgname,model.authority,model.authlevel,model.ownership,model.ftype,auth.getUserId()));
			if (rowEffect.getErrorNumber() == 0) {
				for(int i=1;i<=25;i++) {
					
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
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}


	public void getcsv(HttpServletResponse response) throws IOException {
		String id=request("id");
		String filename = "healthworkforce.csv";
		String sql = "select COALESCE(hfregistry.hf_name,'') as hf_name,COALESCE(tbl_level.namenp,'') as level,COALESCE(tbl_emptype.namenp,'') as emptype,COALESCE(tbl_council.namenp,'') as council,COALESCE(tbl_workforce.orgname,'')as orgname,tbl_samuha.namenp as groupname,COALESCE(tbl_upasamuha.namenp,'') as subgroup,tbl_post.namenp as postname,COALESCE(tbl_employee.namenp,'') as empname,"
				+ " COALESCE(tbl_employee.address,'') as address,COALESCE(tbl_employee.email,'') as email,COALESCE(tbl_employee.mobile,'') as mobile,COALESCE(tbl_employee.apoint_date,'') as apoint_date,COALESCE(tbl_employee.att_date,'') as att_date,COALESCE(tbl_employee.council_no,'') as council_no from tbl_darbandi_details as td"
				+ " left join hfregistry on hfregistry.id=td.orgid join tbl_workforce on tbl_workforce.id=td.workforce_id join tbl_samuha on tbl_samuha.id=td.groupid left join tbl_upasamuha on "
				+ " tbl_upasamuha.id=td.subgroupid join tbl_post on tbl_post.id=td.post  left join tbl_employee on tbl_employee.detailsid=td.id left join tbl_emptype on tbl_emptype.id=tbl_employee.emptype left join tbl_level on tbl_level.id=tbl_employee.level left join tbl_council on tbl_council.id=tbl_employee.council where td.workforce_id=?";

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
}
