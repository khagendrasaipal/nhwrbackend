package org.saipal.workforce.admistr.registration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.saipal.fmisutil.auth.Authenticated;
import org.saipal.fmisutil.service.AutoService;
import org.saipal.fmisutil.util.DB;
import org.saipal.fmisutil.util.DbResponse;
import org.saipal.fmisutil.util.Messenger;
import org.saipal.fmisutil.util.Paginator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RegistrationService extends AutoService {
	
	@Autowired
	DB db;

	@Autowired
	Authenticated auth;
	
	private String table = "users";
	
	
	public ResponseEntity<Map<String, Object>> index() {
		String condition = "";
		try {
			if (!request("searchTerm").isEmpty()) {
				List<String> searchbles = Registration.searchables();
				condition += "and (";
				for (String field : searchbles) {
					condition += field + " LIKE '%" + db.esc(request("searchTerm")) + "%' or ";
				}
				condition = condition.substring(0, condition.length() - 3);
				condition += ")";
			}
			condition = " and status=2 " + condition;
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
					.select("cast(users.id as char) as id,users.username, users.email, users.role, hfregistry.hf_name")
					.sqlBody("from " + table + " join hfregistry on hfregistry.id = users.orgid" + condition)
					.paginate();
			if (result != null) {
				return ResponseEntity.ok(result);
			} else {
				return Messenger.getMessenger().error();
			}
		}
		catch(Exception e) {
			System.out.println("Exception:" +e);
			return null;
		}
		
	}
	
	
	public ResponseEntity<Map<String, Object>> changePassword(String id){
		DbResponse rowEffect;
		Registration model = new Registration();
		model.loadData(document);

		String sql = "UPDATE users set password=? where id=?";
		rowEffect = db.execute(sql, Arrays.asList(new BCryptPasswordEncoder().encode(model.password),id));
		System.out.println(rowEffect.getMessage());
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}

	}
	
	

	
	
	public ResponseEntity<Map<String, Object>> store() {

		String sql = "";
		
		

		Registration model = new Registration();
		model.loadData(document);
		sql = "INSERT INTO users (username,password,status,surname,firstName, email, mobile, role, province, district, municipality, orgid) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.username, new BCryptPasswordEncoder().encode(model.password), model.status,model.surname, model.firstname, model.email, model.mobile,
				model.role, model.province, model.district, model.municipality, model.orgid));
		
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<Map<String, Object>> destroy(String id) {

		String sql = "delete from users where id  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}
}





