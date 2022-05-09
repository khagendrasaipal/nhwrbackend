package org.saipal.workforce.accesscontrol.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.saipal.fmisutil.auth.Authenticated;
import org.saipal.fmisutil.service.AutoService;
import org.saipal.fmisutil.util.DB;
import org.saipal.fmisutil.util.Messenger;
import org.saipal.fmisutil.util.Paginator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UsersService extends AutoService {

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	public static ResponseEntity<Map<String, Object>> index() {
		return null;
	}

	public ResponseEntity<Map<String, Object>> getActiveEmployee() {
		String orgid = request("org");
		String condition = "";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = Arrays.asList("firstnameen", "lastnameen", "firstnamenp", "firstnamenp",
					"username");
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE '%" + db.esc(request("searchTerm")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);
			condition += ")";
		}
		if (!orgid.equals("all")) {
			condition += " and hr_employee.orgid=" + orgid + " ";
		}
		if (!condition.isBlank()) {
			condition = " where 1=1 " + condition;
		}
		Paginator p = new Paginator();
		Map<String, Object> result = p.setPageNo(request("page"))
				.setPerPage(request("perPage"))
				.select("cast(hr_employee.id as CHAR) as id,firstnameen,lastnameen,firstnamenp,lastnamenp,pid ")
				.sqlBody(" from hr_employee join hr_darbandi on hr_darbandi.empid=hr_employee.id " + condition)
				.setGroupBy(" group by id,firstnameen,lastnameen,firstnamenp,lastnamenp,pid ")
				.setOrderBy(" hr_employee.firstnamenp")
				.paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getOrganizations() {
		String sql = "select cast(orgidint as char) as orgidint,orgnameen,orgnamenp from admin_org_strs where disabled=? and approved=?";
		List<Tuple> organization = db.getResultList(sql, Arrays.asList(0, 1));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!organization.isEmpty()) {
			for (Tuple t : organization) {
				Map<String, Object> mapOrganization = new HashMap<>();
				mapOrganization.put("orgidint", t.get("orgidint"));
				mapOrganization.put("orgnamenp", t.get("orgnamenp"));
				mapOrganization.put("orgnameen", t.get("orgnameen"));
				list.add(mapOrganization);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

}
