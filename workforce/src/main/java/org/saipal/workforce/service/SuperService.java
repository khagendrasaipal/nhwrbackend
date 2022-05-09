package org.saipal.workforce.service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.saipal.fmisutil.util.LangService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class SuperService extends AutoService {

	static String sql;

	@Autowired
	protected LangService lService;

	static List<String> args = new ArrayList<String>();

	private static final Logger LOG = LoggerFactory.getLogger(SuperService.class);
	/**
	 * Map containing the selection criteria for the selection
	 */
	protected Map<String, String> selectionArgs;

	public List<String> ignorableFields() {
		return null;
	}

	public Map<String, String> customFormFields() {
		return null;
	}

	public abstract Object getClassName();

	public abstract List<String> listColumns();

	public abstract String getTableName();

	public abstract String getPrimaryKey();

	public SuperService() {
		selectionArgs = new HashMap<String, String>();
	}

	public Map<String, String> getFormFields() {
		Map<String, String> formFields = new HashMap<>();
		for (Field f : getClassName().getClass().getDeclaredFields()) {
			if (Modifier.isPrivate(f.getModifiers())) {
				f.setAccessible(true);
			}
			if (customFormFields() != null && customFormFields().containsKey(f.getName())) {
				formFields.put(f.getName(), wds(customFormFields().get(f.getName())));
			} else {
				String field = f.getName();
				field = field.substring(0, 1).toUpperCase() + field.substring(1);
				formFields.put(f.getName(), wds(field));
			}
		}
		return formFields;
	}
}
