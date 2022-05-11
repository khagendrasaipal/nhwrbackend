package org.saipal.workforce.admistr.registration;

import java.util.Arrays;
import java.util.Map;

import org.saipal.fmisutil.auth.Authenticated;
import org.saipal.fmisutil.service.AutoService;
import org.saipal.fmisutil.util.DB;
import org.saipal.fmisutil.util.DbResponse;
import org.saipal.fmisutil.util.Messenger;
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
	
//	private String table = "users";

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
		
}
