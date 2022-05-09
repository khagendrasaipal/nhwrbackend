package org.saipal.workforce.service;

import java.util.Arrays;
import javax.persistence.Tuple;

import org.saipal.fmisutil.util.DB;
import org.saipal.workforce.config.ContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService extends AutoService {

	private static final Logger LOG = LoggerFactory.getLogger(SystemConfigService.class);

	@Autowired
	DB db;

	public void setProjectId(String projectUrl) {
		String qurey = "select appid, baseurl from sys_init where url=?";
		Tuple tp = db.getSingleResult(qurey, Arrays.asList(projectUrl));
		if (tp != null) {
			ContextHolder.setTenantId(tp.get("appid") + "");
			//ContextHolder.setBaseUrl(tp.get("baseurl")+"");
		}
	}
	
	public boolean valideAndSetTenant(String projectUrl) {
		String qurey = "select appid, baseurl from sys_init where url=?";
		Tuple tp = db.getSingleResult(qurey, Arrays.asList(projectUrl));
		if(tp==null) {
			return false;
		}
		ContextHolder.setTenantId(tp.get("appid") + "");
		return true;
	}

	public boolean isValidTenant(String tenantName) {
		String qurey = "select count(url) as total from sys_init where url=?";
		Tuple tp = db.getSingleResult(qurey, Arrays.asList(tenantName));
		if (tp != null) {
			int total = Integer.parseInt(tp.get("total") + "");
			if (total > 0) {
				return true;
			}
		}
		return false;

	}

	public Tuple getInfo() {
		String sql = "select * from sys_tblproject where id=?";
		Tuple tuple = db.getSingleResult(sql, Arrays.asList(ContextHolder.getTenant()));
		return tuple;
	}
	
	public void logForm(String uri) {
		String sql = "";//"if (select count(filepath) from sys_pageinfo where filepath=?) < 1 begin";
		sql += " insert ignore into sys_pageinfo(filepath) values (?)";
		db.execute(sql, Arrays.asList(uri));
	}

}