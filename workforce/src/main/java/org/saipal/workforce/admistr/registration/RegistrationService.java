package org.saipal.workforce.admistr.registration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.saipal.fmisutil.auth.Authenticated;
import org.saipal.fmisutil.service.AutoService;
import org.saipal.fmisutil.util.DB;
import org.saipal.fmisutil.util.DbResponse;
import org.saipal.fmisutil.util.Messenger;
import org.saipal.fmisutil.util.Paginator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RegistrationService extends AutoService {
	
	@Autowired
	DB db;

	@Autowired
	Authenticated auth;
	
	@Autowired
	PasswordEncoder pe;
	
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
	
	
	public ResponseEntity<Map<String, Object>> changePasswordSelf(String id){
		String opassword = request("opassword");
		String password = request("password");
		String rpassword = request("rpassword");
		if(opassword.isBlank() || password.isBlank()|| rpassword.isBlank()) {
			return Messenger.getMessenger().setMessage("All Fields are required.").error();
		}
		if(!password.equals(rpassword)) {
			return Messenger.getMessenger().setMessage("Password and Confirm password does not match.").error();
		}
		
		Tuple t = db.getSingleResult("select password from users where id=?",Arrays.asList(id));
		if(t!=null) {
			String encPass = t.get("password")+"";
			if(pe.matches(request("opassword"), encPass)) {
				DbResponse rowEffect;
				String sql = "UPDATE users set password=? where id=?";
				rowEffect = db.execute(sql,Arrays.asList(pe.encode(password),id));
				if (rowEffect.getErrorNumber() == 0) {
					return Messenger.getMessenger().success();
				}
			}else {
				return Messenger.getMessenger().setMessage("Old password does not match.").error();
			}
		}
		return Messenger.getMessenger().setMessage("No such User exists.").error();
	}
	

	
	
	public ResponseEntity<Map<String, Object>> store() {

		String sql = "";
		
		

		Registration model = new Registration();
		model.loadData(document);
		sql = "INSERT INTO users (username,password,status,surname,firstName, email, mobile, role, province, district, municipality, orgid) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(model.username, pe.encode(model.password), model.status,model.surname, model.firstname, model.email, model.mobile,
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





