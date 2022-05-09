package org.saipal.workforce.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Tuple;

import org.saipal.fmisutil.util.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Table2Sql {

	@Autowired
	DB db;

	public void dumpTable(String... tables) {
		StringBuffer result = new StringBuffer();
		for (String table : tables) {
			if (checkTableExists(table)) {
				result.append("\n\n-- " + table);
				result.append("\n-- CREATE TABLE " + table);
				result.append(getColumns(table));
				result.append("\n\n-- Data for " + table + "\n");
				result.append(getData(table));
			}
		}

		try {
			File file = new File("src/main/resources/static/backup/codebackup.sql");
			FileWriter writer = new FileWriter(file);
			writer.write(result.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String getData(String table) {
		String data = "", sql = "";
		sql = "select * from " + table;
		List<Tuple> dt = db.getResultList(sql);
		int colCount = countColumns(table);
		if (dt.size() > 0) {
			for (Tuple t : dt) {
				data += "INSERT INTO " + table + " VALUES (";
				for (int i = 0; i < colCount; i++) {
					if (i > 0) {
						data += ", ";
					}
					Object value = t.get(i);
					if (value == null) {
						data += "NULL";
					} else {
						String outputValue = value.toString();
						outputValue = outputValue.replaceAll("'", "''");
						data += sqlN(table, i) + "'" + outputValue + "'";
					}
				}
				data += ");\n";
			}
			
		}
		return data;

	}

	private String sqlN(String table, int field) {
		String sql = "SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME=? and ORDINAL_POSITION=" + (field + 1);
		Tuple dt = db.getSingleResult(sql, Arrays.asList(table));
		String columnName = dt.get("DATA_TYPE") + "";
		if (columnName.equalsIgnoreCase("nvarchar") || columnName.equalsIgnoreCase("ntext")) {
			return "N";
		}
		return "";

	}

	private int countColumns(String table) {
		String sql = "SELECT count(*) cols FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME=?";
		Tuple count = db.getSingleResult(sql, Arrays.asList(table));
		return Integer.parseInt(count.get("cols").toString());
	}

	private String getColumns(String table) {
		String result = "\n-- (";
		List<String> defs = new ArrayList<>();
		String columnNameQuote = "\"";
		String sql = "SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME=?";
		List<Tuple> cols = db.getResultList(sql, Arrays.asList(table));
		for (Tuple t : cols) {
			String columnName = t.get("COLUMN_NAME") + "";
			String columnType = t.get("DATA_TYPE") + "";
			String defalt = t.get("COLUMN_DEFAULT") + "";
			if (defalt.equalsIgnoreCase("null")) {
				defalt = "";
			} else {
				defalt = "-- ALTER TABLE " + table + " ADD CONSTRAINT df_" + columnName + " DEFAULT " + defalt + " FOR "
						+ columnName + ";\n";
				defs.add(defalt);
			}
			String columnSize = t.get("CHARACTER_MAXIMUM_LENGTH") + "";
			String prec = t.get("NUMERIC_PRECISION") + "," + t.get("NUMERIC_SCALE");
			String nullable = t.get("IS_NULLABLE") + "";
			String nullString = "NULL";
			if ("NO".equalsIgnoreCase(nullable)) {
				nullString = "NOT NULL";
			}
			if (columnType.equalsIgnoreCase("numeric")) {
				if ("NULL,NULL".equals(prec)) {
					result += (" " + columnNameQuote + columnName + columnNameQuote + " " + columnType + " "
							+ nullString);
				} else {
					result += (" " + columnNameQuote + columnName + columnNameQuote + " " + columnType + "(" + prec
							+ ")" + " " + nullString);
				}
			} else if (columnType.equalsIgnoreCase("text") || columnType.equalsIgnoreCase("ntext")) {
				result += (" " + columnNameQuote + columnName + columnNameQuote + " " + columnType + " "
						+ nullString);
			} else {
				if (columnSize.equalsIgnoreCase("null")) {
					result += (" " + columnNameQuote + columnName + columnNameQuote + " " + columnType + " "
							+ nullString);
				} else {
					result += (" " + columnNameQuote + columnName + columnNameQuote + " " + columnType + " ("
							+ columnSize + ")" + " " + nullString);
				}

			}
			result +=", ";
		}
		result =result.replaceAll(", $", "")+ "\n-- );\n";
		if (defs.size() > 0) {
			for (String def : defs) {
				result += def;
			}
		}
		return result;

	}

	private boolean checkTableExists(String table) {
		String sql = "SELECT * FROM INFORMATION_SCHEMA.TABLES" + " WHERE TABLE_SCHEMA = 'dbo'" + " AND  TABLE_NAME =?";
		try {
			Tuple t = db.getSingleResult(sql, Arrays.asList(table));
			if (t == null) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
	}
}
