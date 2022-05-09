package org.saipal.workforce.setup.publicDashboardSetup;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class PublicDashboardSetupService extends AutoService{
	@Autowired
	DB db;

	@Autowired
	Authenticated auth;

	private String table = "public_dashboard_setup";
	
	public ResponseEntity<Map<String, Object>> index() {
		String condition = "";
		if (!request("searchTerm").isEmpty()) {
			List<String> searchbles = PublicDashboardSetup.searchables();
			condition += "and (";
			for (String field : searchbles) {
				condition += field + " LIKE '%" + db.esc(request("searchTerm")) + "%' or ";
			}
			condition = condition.substring(0, condition.length() - 3);
			condition += ")";
		}
		condition += " and orgid="+session("orgid")+ "";
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
				.select("cast(public_dashboard_setup.id as char) as id, concat (public_dashboard_setup.fy,\"/\", public_dashboard_setup.fy+1) as fy, public_dashboard_setup.chart_type, data_elements.data_elements as indicator")
				.sqlBody("from " + table + " join data_elements ON public_dashboard_setup.indicator = data_elements.uid "+ condition)
				.paginate();
		if (result != null) {
			return ResponseEntity.ok(result);
		} else {
			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<Map<String, Object>> getindicators(String p_id) {
		String sql = "select id,data_elements from data_elements where pid=?";
		List<Tuple> admlvl = db.getResultList(sql, Arrays.asList(p_id));

		List<Map<String, Object>> list = new ArrayList<>();
		if (!admlvl.isEmpty()) {
			for (Tuple t : admlvl) {
				Map<String, Object> mapadmlvl = new HashMap<>();
				mapadmlvl.put("id", t.get("id"));
				mapadmlvl.put("data_elements", t.get("data_elements"));
				list.add(mapadmlvl);
			}
			return Messenger.getMessenger().setData(list).success();

		} else {
			return Messenger.getMessenger().setData(list).success();
		}
	}
	
	
	public ResponseEntity<Map<String, Object>> store() {
//		System.out.println("Create ");
		String sql = "";
		PublicDashboardSetup model = new PublicDashboardSetup();
		model.loadData(document);
		sql = "INSERT INTO public_dashboard_setup (p_id,fy,indicator,orgid,created_by,chart_type) VALUES (?,?,?,?,?,?)";
		DbResponse rowEffect = db.execute(sql,
				Arrays.asList(model.p_id, model.fy, model.indicator,session("orgid"),session("empid"), model.chart_type));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}
	
	public ResponseEntity<Map<String, Object>> edit(String id) {

		String sql = "select id,indicator,fy,p_id, chart_type from "
				+ table + " where id=?";
		Map<String, Object> data = db.getSingleResultMap(sql, Arrays.asList(id));
		return ResponseEntity.ok(data);
	}
	
	public ResponseEntity<Map<String, Object>> update(String id) {
		DbResponse rowEffect;
		PublicDashboardSetup model = new PublicDashboardSetup();
		model.loadData(document);

		String sql = "UPDATE public_dashboard_setup set indicator=?,fy=?,p_id=?,orgid=?,created_by=?, chart_type=? where id=?";
		rowEffect = db.execute(sql,
				Arrays.asList(model.indicator, model.fy,model.p_id,session("orgid"),session("empid"),model.chart_type,id));

		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}

	}
	
	public ResponseEntity<Map<String, Object>> destroy(String id) {

		String sql = "delete from public_dashboard_setup where id  = ?";
		DbResponse rowEffect = db.execute(sql, Arrays.asList(id));
		if (rowEffect.getErrorNumber() == 0) {
			return Messenger.getMessenger().success();

		} else {
			return Messenger.getMessenger().error();
		}
	}

}
