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
import org.saipal.workforce.admistr.model.CompositeIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
@Component
public class CompositeIndicatorService extends AutoService {

	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	private String table = "composite_indicator";

	public ResponseEntity<Map<String, Object>> index() {
		String condition = "";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = CompositeIndicator.searchables();
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
				.select("id,code,name")
				.sqlBody("from " + table + condition).paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> store() {
		String sql = "";
		CompositeIndicator model = new CompositeIndicator();
		model.loadData(document);
		sql = "INSERT INTO composite_indicator (domain_id,code,name) VALUES (?,?,?)";
		DbResponse rowEffect = db.execute(sql,
				Arrays.asList(model.domain_id,model.code, model.name));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select id,code,name,domain_id from "
				+ table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}

	public ResponseEntity<Map<String, Object>> update(String id) {
		DbResponse rowEffect;
		CompositeIndicator model = new CompositeIndicator();
		model.loadData(document);

		String sql = "UPDATE composite_indicator set code=?,name=?,domain_id=? where id=?";
		rowEffect = db.execute(sql,
				Arrays.asList(model.code, model.name,model.domain_id,id));

		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}

	}

	public ResponseEntity<Map<String, Object>> destroy(String id) {

		String sql = "delete from composite_indicator where id  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

	public ResponseEntity<Map<String, Object>> getCategory() {
		String sql = "select id,name from domain where 1=?";
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
			return Messenger.getMessenger().error();
		}
	}
}