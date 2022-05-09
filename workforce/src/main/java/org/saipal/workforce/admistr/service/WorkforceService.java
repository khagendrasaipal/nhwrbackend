package org.saipal.workforce.admistr.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.saipal.fmisutil.auth.Authenticated;
import org.saipal.fmisutil.service.AutoService;
import org.saipal.fmisutil.util.DB;
import org.saipal.fmisutil.util.DbResponse;
import org.saipal.fmisutil.util.Messenger;
import org.saipal.fmisutil.util.Paginator;
import org.saipal.workforce.admistr.model.Workforce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
//		System.out.println(ofcstr.size());
		if (ofcstr.isEmpty()) {
			sql = "INSERT INTO tbl_workforce (id,orgtype,provinceid,districtid,palika,ward,org,orgname,authority,authlevel,ownership,ftype,created_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
			DbResponse rowEffect = db.execute(sql,
					Arrays.asList(pid,model.orgtype,model.provinceid, model.districtid,model.palika,model.ward,model.org,model.orgname,model.authority,model.authlevel,model.ownership,model.ftype,auth.getUserId()));
			if (rowEffect.getErrorNumber() == 0) {
				for(int i=1;i<=25;i++) {
					
					if("".equals(request("groupid"+i))) {
						
					}else {
//						System.out.println(request("groupid"+i));
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
				return Messenger.getMessenger().success();

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


	public ByteArrayInputStream getcsv(HttpServletResponse response) {
		String id=request("id");
		String filename = "healthworkforce.csv";
		String sql = "select hfregistry.hf_name,tbl_workforce.orgname,tbl_samuha.namenp as groupname,tbl_upasamuha.namenp as subgroup,tbl_post.namenp as postname,tbl_employee.namenp as empname from tbl_darbandi_details as td"
				+ " left join hfregistry on hfregistry.id=td.orgid join tbl_workforce on tbl_workforce.id=td.workforce_id join tbl_samuha on tbl_samuha.id=td.groupid left join tbl_upasamuha on "
				+ " tbl_upasamuha.id=td.subgroupid join tbl_post on tbl_post.id=td.post left join tbl_employee on tbl_employee.detailsid=td.id where td.workforce_id=?";
//		System.out.println(sql);
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(id));
		
		response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=contacts.xlsx");
		
//		String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
//		  String[] HEADERs = { "Id", "Title", "Description", "Published" };
//		  String SHEET = "Tutorials";
		
		  try(Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {

	            
		      Sheet sheet = workbook.createSheet("contacts.xlsx");
		      // Header
		      Row headerRow = sheet.createRow(0);
		      for (int col = 0; col < HEADERs.length; col++) {
		        Cell cell = headerRow.createCell(col);
		        cell.setCellValue(HEADERs[col]);
		      }
		      int rowIdx = 1;
		      int i = 2;
		      for (Tuple t : admlvl) {
		    	  Row row = sheet.createRow(rowIdx++);
		          row.createCell(0).setCellValue(t.get("groupname")+"");
		          row.createCell(1).setCellValue(t.get("groupname")+"");
		          row.createCell(2).setCellValue(t.get("groupname")+"");
		          row.createCell(3).setCellValue(t.get("groupname")+"");
		      }
		      workbook.write(out);
		      System.out.println("yaha xcu ma");
		      return new ByteArrayInputStream(out.toByteArray());
		     
		    } catch (IOException e) {
		    	System.out.println(e.getMessage());
		      throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
		    }
		
//		response.setContentType("text/csv");
//		response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
//				"attachment; filename=\"" + filename + "\"");
//		
//		 try {
//			csvPrinter = new CSVPrinter(response.getWriter(),
//					CSVFormat.EXCEL.withHeader("HF name", "orgname", "Group","subgroup","employee"));
//			if (!admlvl.isEmpty()) {
//				for (Tuple t : admlvl) {
//					csvPrinter.printRecord(Arrays.asList(t.get("hf_name"),t.get("orgname"),t.get("groupname"),t.get("subgroup"),t.get("empname")));
//				}
//				
//			
//		}
//		}catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if(csvPrinter != null)
//				csvPrinter.close();
//		}
		
}
}
