package org.saipal.workforce.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.saipal.fmisutil.ApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//class designed to bridge the result set object of Entity Manager and JDBC
public class MyTuple implements Tuple {
	private static final Logger LOG = LoggerFactory.getLogger(MyTuple.class);
	public List<String> keys;
	public List<Object> values;
	public Map<String, Object> val;
	public String sql;

	public MyTuple() {
		// TODO Auto-generated constructor stub
		keys = new ArrayList<>();
		values = new ArrayList<>();
		val = new HashMap<>();
	}

	public MyTuple(String sql) {
		// TODO Auto-generated constructor stub
		this.sql = sql;
		keys = new ArrayList<>();
		values = new ArrayList<>();
		val = new HashMap<>();
	}

	@Override
	public <X> X get(TupleElement<X> tupleElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> X get(String alias, Class<X> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(String alias) {
		FmisUtil util = ApplicationContextProvider.getBean(FmisUtil.class);
		if (keys.indexOf(alias.toLowerCase()) == -1) {
			if (util.getDevMode().equals("1")) {
				throw new RuntimeException("index not found in tuple for " + alias);
			}
			LOG.error("index not found in tuple for " + alias);
			LOG.error("Sql " + sql);
		}
		return values.get(keys.indexOf(alias.toLowerCase()));

	}

	@Override
	public <X> X get(int i, Class<X> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(int i) {
		// TODO Auto-generated method stub
		return values.get(i);
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TupleElement<?>> getElements() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addObject(String key, Object value) {
		keys.add(key.toLowerCase());
		values.add(value);
		val.put(key.toLowerCase(), value);

	}

}
