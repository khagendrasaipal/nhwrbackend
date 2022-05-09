package org.saipal.workforce.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Tuple;

import org.saipal.fmisutil.parser.DataGrid;
import org.saipal.fmisutil.util.DB;
import org.saipal.fmisutil.util.RecordSet;
import org.saipal.workforce.util.FmisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SourceCodeService extends SuperService {

	@Autowired
	DB db;
	
	@Autowired
	DB conn;
	
	private static final Logger LOG = LoggerFactory.getLogger(SourceCodeService.class);
	
	@Autowired
	Table2Sql t2s;

	public String getFormId(String path, String contextPath) {
		
		LOG.info(path);
		if (!contextPath.isBlank()) {
			path = path.replace(contextPath, "");
		}
		String sql = "select formid from sys_pageinfo where filepath=? limit 1";
		String id;
		Tuple result = db.getSingleResult(sql, Arrays.asList(path));
		if (result != null) {
			id = result.get("formid") + "";
			LOG.info("form id:" + id);
			return id;
		} else {
			throw new RuntimeException("cannot find form id");
		}
	}

	public String getPathByFormID(String formId) {
		String sql = "select filepath from sys_pageinfo where formid=?";
		Tuple result = db.getSingleResult(sql, Arrays.asList(formId));
		return result.get("filepath") + "";

	}

	public String getSourceCode(String event, String runat, String formId, String object) {
		String sql = "select cast (code as nvarchar(MAX)) as code from " + getTableName()
				+ " where formid=? and event=? and runat=? and objects=?";
		List<Tuple> tlist = db.getResultList(sql, Arrays.asList(formId, event, runat, object));

		if (tlist != null && tlist.size() > 0) {
			return tlist.get(0).get("code") + "";
		}

		return "";

	}

	public boolean isSourceCodeAvailable(String formid) {
		//String sql = "select count(*) as count from " + getTableName() + " where formid=?";
		String sql = "select count(codeid) as count from sys_tblsourcecode where formid=?";
		Tuple t = db.getSingleResult(sql, Arrays.asList(formid));
		int count = Integer.parseInt(t.get("count") + "");
		if (count > 0)
			return true;
		return false;
	}

	public void insertOrUpdateSourceCode(String formId, String event, String runAt, String code, String object) {
		insertOrUpdateSourceCode(formId, event, runAt, code, object, "0");
	}

	public void insertOrUpdateSourceCode(String formId, String event, String runAt, String code, String object,
			String isRibbon) {
		String query = "if exists (select * from " + getTableName()
				+ " where formid=? and event=? and runat=? and objects=?)" + " begin" + " update " + getTableName()
				+ " set code=? where formid=? and event=? and runat=? and objects=?" + " end" + " else" + " begin"
				+ " insert into " + getTableName() + "(formid,event,runat,code,objects) values (?,?,?,?,?)" + " end";
		List<Object> args = new ArrayList<Object>();
		args.add(formId);
		args.add(event);
		args.add(runAt);
		args.add(object);
		args.add(code);
		args.add(formId);
		args.add(event);
		args.add(runAt);
		args.add(object);
		args.add(formId);
		args.add(event);
		args.add(runAt);
		args.add(code);
		args.add(object);
		db.execute(query, args);
		if (isRibbon.equals("1")) {
			// ribbon code need to be updated.
			writeRibbonCode();
		}
		// writeFile(formId, runAt);
	}

	private void writeRibbonCode() {
		// TODO Auto-generated method stub
		String query = "select * from sys_tblsourcecode where event='onrender' and entrydate> '2020-05-18 13:14:45.250'";
		List<Tuple> tList = db.getResultList(query);
		String codes = "";
		for (Tuple t : tList) {
			String codeT = t.get("code") + "";
			// split to get comment of author
			String[] cArray = codeT.split("public");
			codeT = cArray[0] + "public boolean R_" + t.get("objects") + "_onrender(){"
					+ cArray[1].substring(cArray[1].indexOf('{') + 1);
			codes += codeT + "\n\n";
		}
		FmisUtil.writeRibbonServiceCode(codes);

	}

	@Override
	public Object getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> listColumns() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return "sys_tblsourcecode";
	}

	@Override
	public String getPrimaryKey() {
		// TODO Auto-generated method stub
		return "codeid";
	}

	public void writeFile(String formid) {
		writeFile(formid, true);
	}

	public void writeFile(String formid, boolean backup) {
		// writing server side code
		// LOG.info("writing server code");
		String sql = "select cast(s.code as nvarchar(MAX)) as code,filepath,objects from sys_tblsourcecode s inner join sys_pageinfo p on s.formid=p.formid where s.formid=? and runat='server' and event<>'onrender' ORDER BY CASE WHEN objects = 'General' THEN CASE WHEN event='General' then 1 else 2 end ELSE 3 END";
		// //System.out.println(sql);
		String code = "";
		String path;
		String imports = "";
		List<Tuple> codeList = db.getResultList(sql, Arrays.asList(formid));
		if (codeList.size() > 0) {
			for (Tuple t : codeList) {
				if ((t.get("objects") + "").equals("Include")) {
					imports = "\n" + t.get("code") + "\n";
				} else {
					code += t.get("code") + "\n\n";
				}
			}
			path = codeList.get(0).get("filepath") + "";
			FmisUtil.writeFile("src/main/java/org/sfmis/fmis", path, code, imports);
		}
		// end of writing server side code

		// writing client side code
		// LOG.info("writing client code");
		// StringBuffer tempCode;
		// sql = "select * from (select
		// 'document.getElementById(\"'+objects+'\").'+[event]+'='+objects+'_'+[event]+';'as
		// line1,objects,event,code from sys_tblsourcecode where formid=? and
		// runat='client')as a full join (select
		// 'document.getElementById(\"'+objects+'\").'+[event]+'='+objects+'_'+[event]+';'as
		// line2,objects as serverobject,event as serverevent,code as servercode from
		// sys_tblsourcecode where formid=? and runat='server' and event<>'onrender' and
		// objects<>'Include' and rtrim(ltrim(cast(code as nvarchar(max))))<>'')as b on
		// a.event=b.serverevent and a.objects=b.serverobject order by line1 desc";
		sql = "select * from (select 'document.getElementById(\"'+objects+'\").'+[event]+'='+objects+'_'+[event]+';'as line1,objects,event,code from sys_tblsourcecode where formid=? and runat='client')as a full join (select 'document.getElementById(\"'+objects+'\").'+[event]+'='+objects+'_'+[event]+';'as line2,objects as serverobject,event as serverevent,code as servercode from sys_tblsourcecode where formid=? and runat='server' and event<>'onrender' and objects<>'Include' and rtrim(ltrim(cast(code as nvarchar(max))))<>'')as b on a.event=b.serverevent and a.objects=b.serverobject full join (select 'document.getElementById(\"'+objects+'\").'+[event]+'='+objects+'_'+[event]+';'as line3,objects as objects1 ,event as events1,code as code1 from sys_tblsourcecode where formid=? and runat='client1') as c on  a.event=c.events1 and a.objects=c.objects1 order by line1 desc";
		code = "";
		codeList = db.getResultList(sql, Arrays.asList(formid, formid, formid));
		sql = "select * from sys_pageinfo where formid=?";
		Tuple pageInfo = db.getSingleResult(sql, Arrays.asList(formid));
		path = pageInfo.get("filepath") + "";
		String pathVar[];
		pathVar = path.split("/");
		String base;
		if (pathVar.length > 3)
			base = pathVar[1] + "/" + pathVar[2] + "/" + pathVar[3];
		else
			base = pathVar[1] + "/" + pathVar[2];

		for (Tuple t : codeList) {

			String line1Code = "";
			String line2Code = "";
			String funName = t.get("serverobject") + "_" + t.get("serverevent");
			String proceedServer = "\nproceedServer('" + base + "/invoke?methodName=" + funName + "');";
			// For General Global Decleration
			if (t.get("objects") != null && ((String) t.get("objects")).equalsIgnoreCase("general")) {
				code += t.get("code") + "\n";
			} else if (t.get("objects1") != null && ((String) t.get("objects1")).equalsIgnoreCase("general")) {
				code += t.get("code1") + "\n";
			} else { // For non global
				if (t.get("line2") != null) { // If there is server side code
					if (t.get("line1") != null) {
						line1Code = (String) t.get("code");
						code += "\n" + getFunctionDeclareation(line1Code) + "{\n";
						code += getDefinition(line1Code) + "\n";
						code += proceedServer + "\n";
						if (t.get("events1") != null) {
							line2Code = (String) t.get("code1");
							code += getDefinition(line2Code) + "\n";
						}
						code += "}\n";
						code += t.get("line2") != null ? t.get("line2") : t.get("line1") + "\n";
					} else {
						if (!((String) t.get("serverobject")).equalsIgnoreCase("general")) {
							code += "\nfunction " + funName + "(){\n";
							code += proceedServer;
							if (t.get("events1") != null) {
								line2Code = (String) t.get("code1");
								code += getDefinition(line2Code) + "\n";
							}
							code += "\n}\n";
							code += t.get("line2") != null ? t.get("line2") : t.get("line1") + "\n";
						}

					}
				} else {// if there is no server side code
					if (t.get("line1") != null) {
						line1Code = (String) t.get("code");
						code += "\n" + getFunctionDeclareation(line1Code) + "{\n";
						code += getDefinition(line1Code) + "\n";
						code += "//No server code\n";
						if (t.get("events1") != null) {
							line2Code = (String) t.get("code1");
							code += getDefinition(line2Code) + "\n";
						}
						code += "}\n";
						code += t.get("line1") + "\n";
					} else if (t.get("line3") != null) {
						code += (String) t.get("code1") + "\n";
						code += t.get("line3") + "\n";
					}
				}

			}

		}
		FmisUtil.writeFile("src/main/resources/templates", path, code);
		// end of writing client side code
	}

	public String getDefinition(String m_code) {
		int start = m_code.indexOf("{");
		int end = m_code.lastIndexOf("}");
		String ret = m_code.substring(start + 1, end);
		return ret;
	}

	public String getFunctionDeclareation(String m_code) {
		int start = 0;
		int end = m_code.indexOf("{");
		String ret = m_code.substring(start, end);
		return ret;
	}

	public synchronized List<Tuple> logFormElement(String formId, String queryString, String elementId) {
		int isRibbon = 0;
		List<List<Object>> elemArgs = new ArrayList<>();
		List<Object> temp;
		String[] splitParam;
		String[] elemList = queryString.split(",");
		String sql = "delete from sys_tblformelements where formid=? and inputtype<>'link'";
		db.execute(sql, Arrays.asList(formId));
		String ribbonId = "";
		sql = "insert into sys_tblformelements(formid,inputname,inputtype)values(?,?,?)";
		for (String elem : elemList) {
			splitParam = elem.split(":");
			if (!splitParam[1].equalsIgnoreCase("fieldset")) {
				temp = new ArrayList<>();
				temp.add(formId);
				temp.add(splitParam[0]);
				temp.add(splitParam[1]);
				elemArgs.add(temp);
			}
		}
		db.executeBulk(sql, elemArgs);
		LOG.info("after bulk update");
		if (elementId.contains("ribbon__")) {
			sql = "if(select count(inputname) from sys_tblformelements where inputname=? and formid=1)<1 begin insert into sys_tblformelements(formid,inputname,inputtype)values(1,?,'ribbonitem') end";
			db.execute(sql, Arrays.asList(elementId, elementId));
			isRibbon = 1;
			ribbonId = elementId.split("_")[2];

		} else {
			sql = "if(select count(inputname) from sys_tblformelements where inputname=? and formid=?)<1 begin insert into sys_tblformelements(formid,inputname,inputtype)values(?,?,'link') end";
			db.execute(sql, Arrays.asList(elementId, formId, formId, elementId));
		}

		List<Tuple> tList;
		if (isRibbon == 1) {
			sql = "select itemid as objectname,'used' as classname,replace(caption,' ','') as caption from sys_tbltabmenudetail where itemid=?";
			tList = db.getResultList(sql, Arrays.asList(ribbonId));
		} else {
			sql = "select objectname,classname,objectname as caption from dbo.sys_getelement(?)";
			tList = db.getResultList(sql, Arrays.asList(formId));
		}

		return tList;

	}

	public List<Tuple> getEventList(String formId, String object) {
		String sql = "select event from sys_tblsourcecode where formid=? and objects=?";
		return db.getResultList(sql, Arrays.asList(formId, object));
	}

	public void deleteSourceCode(String formId, String event, String runAt, String object) {
		deleteSourceCode(formId, event, runAt, object, "0");
	}

	public void deleteSourceCode(String formId, String event, String runAt, String object, String isRibbon) {
		String sql = "delete from sys_tblsourcecode where formid=? and event=? and runat=? and objects=?";
		db.execute(sql, Arrays.asList(formId, event, runAt, object));
		if (isRibbon.equals("1")) {
			// ribbon code need to be updated.
			writeRibbonCode();
		}
	}

	public void generateAllSourceCode() {
		// String sql = "select * from sys_pageinfo where filepath not like '%.asp%'";
		String sql = "select distinct a.* from sys_pageinfo as a inner join sys_tblsourcecode as b on a.formid=b.formid  where filepath not like '%.asp%'";
		List<Tuple> tList1;
		List<Tuple> tList = db.getResultList(sql);
		for (Tuple t : tList) {
			tList1 = db.getResultList("select * from sys_tblsourcecode where formid=?",
					Arrays.asList(t.get("formid") + ""));
			if (tList1.size() > 0) {
				writeFile(t.get("formid") + "", false);
			}
		}
		document.alert("Source code generated successfully");
	}

	public void backupSql() {
		t2s.dumpTable("sys_pageinfo", "sys_tblsourcecode");
		document.alert("Source code backup successful");
	}

	public void grid_basetable_onchange() {
		RecordSet rs = new RecordSet();
		String tablename = document.getElementById("txtbasetable").value();
		rs.open("select * from dbo.tabledesc('" + tablename + "')", conn);
		document.removeall("txttablefield");
		document.additem("txttablefield", ".........", "");
		if (rs.state == 1) {
			if (rs.recordCount > 0) {
				while (!rs.EOF()) {
					document.additem("txttablefield", rs.fields(0).value() + " (" + rs.fields(1).value() + ")",
							rs.fields(0).value());
					rs.moveNext();
				}
			}
			rs.close();
		}
	}

	public void saveGrid() {
		String txtgridid = conn.esc(request("txtgridid"));
		String txtformid = conn.esc(request("txtformid"));
		String txthtmlform = conn.esc(request("txthtmlform"));
		String txtformelements = conn.esc(request("txtformelements"));
		String txtbasetable = conn.esc(request("txtbasetable"));
		String txtgridtype = conn.esc(request("txtgridtype"));
		String txtrelatedfield = conn.esc(request("txtrelatedfield"));
		String txtsn = conn.esc(request("txtsn"));
		String txtdelete = conn.esc(request("txtdelete"));
		String txtgridwidth = conn.esc(request("txtgridwidth"));
		String txtgridheight = conn.esc(request("txtgridheight"));
		String dgridvalue = conn.esc(request("dgridvalue"));
		String dgridvalue_colname = conn.esc(request("dgridvalue_colname"));

		if (txtsn.isBlank())
			txtsn = "0";
		if (txtdelete.isBlank())
			txtdelete = "0";
		if (txtgridwidth.isBlank())
			txtgridwidth = "102";
		if (txtgridheight.isBlank())
			txtgridheight = "300";

		if (txtgridid.isBlank()) {
			document.alert("Invalid grid id");
			return;
		}

		if (txtformid.isBlank()) {
			document.alert("Invalid form id");
			return;
		}

		if (txtformelements.isBlank()) {
			document.alert("Invalid form elements");
			return;
		}

		DataGrid jdgrid = document.getGrid("dgrid");
		jdgrid.parseGrid();
		String elem[], elname, eltype;
		int l1;
		elem = txtformelements.split(",");
		conn.execute("delete from sys_tblformelements where formid=" + txtformid);
		for (l1 = 0; l1 < elem.length; l1++) {
			elname = elem[l1].split(":")[0].trim();
			eltype = elem[l1].split(":")[1].trim();
			if (!eltype.equalsIgnoreCase("fieldset")) {
				if (!elname.isBlank()) {
					conn.execute("insert into sys_tblformelements(formid,inputname,inputtype)values(" + txtformid + ",'"
							+ elname + "','" + eltype + "')");
				}
			}
		}

		long iid = newidint();
		conn.execute("delete from sys_tbldatagridmain where formid=" + txtformid + " and gridname='" + txtgridid + "'");
		conn.execute(
				"insert into sys_tbldatagridmain(iid,formid,formname,gridname,formelements,basetable,gridtype,pkfield,serialno,deletebutton,width,height) values("
						+ iid + ",'" + txtformid + "','" + txthtmlform + "','" + txtgridid + "','" + txtformelements
						+ "','" + txtbasetable + "','" + txtgridtype + "','" + txtrelatedfield + "','" + txtsn + "','"
						+ txtdelete + "','" + txtgridwidth + "'," + txtgridheight + ")");
		// Insert into sys_tbldatagriddetail
		conn.execute("delete from sys_tbldatagriddetail where formid=" + txtformid + " and gridid='" + txtgridid + "'");
		document.alert("" + jdgrid.totalRows);
		if (jdgrid.totalRows > 0) {
			while (!jdgrid.EOF()) {
				conn.execute(
						"insert into sys_tbldatagriddetail(did,iid,formid,gridid,colname,colorder,colwidth,inputfield,tablefield,readas,compulsory,[clear])values(dbo.newidint(),"
								+ iid + "," + txtformid + ",'" + txtgridid + "','" + jdgrid.getValue("colname") + "',"
								+ jdgrid.getValue("colorder") + "," + jdgrid.getValue("colwidth") + ",'"
								+ jdgrid.getValue("inputfield") + "','" + jdgrid.getValue("tablefield") + "','"
								+ conn.esc(jdgrid.getValue("readas")) + "'," + jdgrid.getValue("compulsory") + ","
								+ jdgrid.getValue("clear") + ")");
				jdgrid.moveNext();
			}
		}
		document.alert("Record has been saved");
	}

	public String getPkField(String tablename) {
		RecordSet rs = new RecordSet();
		rs.open("select  Columnname from vbwPklist where TableName='" + tablename + "'", conn);
		String ret = "";
		if (rs.state == 1) {
			if (rs.recordCount > 0)
				ret = rs.fields(0).value();
			else
				ret = "NO_PK_FIELD";
			rs.close();
		} else
			ret = "NO_PK_FIELD";

		return ret;
	}

	public String getdatatype(String tablename, String fieldname) {
		String ret = "";
		RecordSet rs = new RecordSet();
		rs.open("select [type] from dbo.tabledesc('" + tablename + "') where columnname='" + fieldname + "'", conn);
		if (rs.state == 1) {
			if (rs.recordCount > 0)
				ret = rs.fields(0).value();
			else
				ret = "varchar";
			rs.close();
		} else
			ret = "varchar";
		return ret;

	}

	public String getgridCodeSearch(long formid, String gridid) {
		String ret = "";
		RecordSet rsmain = new RecordSet();
		RecordSet rsdetail = new RecordSet();

		rsmain.open("select * from sys_tbldatagridmain where formid=" + formid + " and gridname='" + gridid + "'",
				conn);
		if (rsmain.state == 1) {
			if (rsmain.recordCount < 1)
				return ret;
		} else {
			return ret;
		}
		rsdetail.open("select * from sys_tbldatagriddetail where iid='" + rsmain.fields("iid").value()
				+ "'  order by colorder", conn);
		if (rsdetail.state == 1) {
			if (rsdetail.recordCount < 1) {
				ret = "//Not found";
				return ret;
			}
		} else {
			ret = "//Not found";
			return ret;
		}

		String PkField, PkDatatype;
		PkField = getPkField(rsmain.fields("basetable").value());
		PkDatatype = getdatatype(rsmain.fields("basetable").value(), PkField);
		String GridId, GridRs, GridSQL, JgridId, JrowId, Gcounter;
		GridId = rsmain.fields("gridname").value();
		Gcounter = rsmain.fields("gridname").value() + "counter";
		GridRs = rsmain.fields("gridname").value() + "Rs";
		GridSQL = rsmain.fields("gridname").value() + "SQL";
		JgridId = "j" + rsmain.fields("gridname").value();
		JrowId = JgridId + "row";

		ret = "RecordSet " + GridRs + "=new RecordSet();\n";
		ret = ret + "document.js(\" var " + JrowId + "=new gridrow(" + JgridId + ");\");\n";
		ret = ret + "document.js(\" " + JgridId + ".clearall();\");\n";
		String SelectField, DisplayField;

		SelectField = "";
		SelectField = PkField + " as id";

		DisplayField = "document.js(\"" + JrowId + ".fields('id','\" + " + GridRs
				+ ".fields(\"id\").value() + \"');\");\n";
		DisplayField = DisplayField + "document.js(\"" + JrowId + ".fields('sn','\" + " + Gcounter + " +  \"');\");\n";
		while (!rsdetail.EOF()) {
			if (!SelectField.isBlank())
				SelectField = SelectField + ",";
			if (rsdetail.fields("inputtype").value().contains("select")) {

				SelectField = SelectField + rsdetail.fields("tablefield").value() + " as "
						+ getColName(rsdetail.fields("colname").value()) + " ," + rsdetail.fields("readas").value()
						+ " as " + getColName(rsdetail.fields("colname").value() + "_text");
				DisplayField = DisplayField + "document.js(\"" + JrowId + ".fields('"
						+ getColName(rsdetail.fields("colname").value()) + "','\" + " + GridRs + ".fields(\""
						+ getColName(rsdetail.fields("colname").value()) + "\").value() + \"');\");\n";
				DisplayField = DisplayField + "document.js(\"" + JrowId + ".fields('"
						+ getColName(rsdetail.fields("colname").value() + "_text") + "','\" + " + GridRs + ".fields(\""
						+ getColName(rsdetail.fields("colname").value() + "_text") + "\").value() + \"');\");\n";
			} else {
				SelectField = SelectField + rsdetail.fields("tablefield").value() + " as "
						+ getColName(rsdetail.fields("colname").value());
				DisplayField = DisplayField + "document.js(\"" + JrowId + ".fields('"
						+ getColName(rsdetail.fields("colname").value()) + "','\" + " + GridRs + ".fields(\""
						+ getColName(rsdetail.fields("colname").value()) + "\").value() + \"');\");\n";
			}
			rsdetail.moveNext();

		}
		DisplayField = DisplayField + "document.js(\"" + JrowId + ".fields('action','Delete');\");\n";
		SelectField = "Select " + SelectField + " from " + rsmain.fields("basetable").value();
		ret = ret + "int " + Gcounter + ";\n";
		ret = ret + Gcounter + "=0;\n";
		ret = ret + GridRs + ".open(\"" + SelectField + "\",conn);\n";
		ret = ret + " if(" + GridRs + ".state==1){\n";
		ret = ret + "		if(" + GridRs + ".recordCount>0){\n";
		ret = ret + "				while(! " + GridRs + ".EOF()){\n";
		ret = ret + "					" + Gcounter + "++;\n";
		ret = ret + DisplayField;

		ret = ret + "document.js(\"" + JgridId + ".addrow(" + JrowId + ",0);\");\n";
		ret = ret + "					" + GridRs + ".moveNext();\n";
		ret = ret + "				}\n";
		ret = ret + "		}\n";
		ret = ret + "	" + GridRs + ".close();\n";

		ret = ret + "document.js(\"" + JgridId + ".render();\");\n";
		ret = ret + "}\n";
		return ret;
	}

	public String getgridCodeSave(long formid, String gridid) {
		String ret = "";
		RecordSet rsmain = new RecordSet();
		RecordSet rsdetail = new RecordSet();

		rsmain.open("select * from sys_tbldatagridmain where formid=" + formid + " and gridname='" + gridid + "'",
				conn);
		if (rsmain.state == 1) {
			if (rsmain.recordCount < 1)
				return ret;
		} else {
			return ret;
		}

		rsdetail.open("select * from sys_tbldatagriddetail where iid='" + rsmain.fields("iid").value() + "'", conn);
		if (rsdetail.state == 1) {
			if (rsdetail.recordCount < 1) {
				ret = "//Not found";
				return ret;
			}
		} else {
			ret = "//Not found";
			return ret;
		}

		String GridId, GridCol, GridValue, GridSQL;
		GridId = rsmain.fields("gridname").value();
		GridCol = rsmain.fields("gridname").value() + "_colname";
		GridValue = rsmain.fields("gridname").value() + "_value";
		GridSQL = rsmain.fields("gridname").value() + "_SQL";

		ret = "String " + GridId + "1," + GridCol + "," + GridValue + "," + GridSQL + ";\n";
		ret = ret + GridValue + "=conn.esc(request(\"" + GridValue + "\"));\n";
		ret = ret + GridCol + "=conn.esc(request(\"" + GridCol + "\"));\n";
		// DataGrid jdgrid=document.getGrid("dgrid");
		// jdgrid.parseGrid();
		ret = ret + "DataGrid " + GridId + "=document.getGrid(\"" + GridId + "\");\n";
		ret = ret + GridId + ".parseGrid();\n";
		ret = ret + "if(" + GridId + ".totalRows<1){\n";
		ret = ret + "	conn.execute(\"Delete from " + rsmain.fields("basetable").value()
				+ " where <WRITE CONDITION HERE>\");\n";
		ret = ret + "}\n else{\n";
		// Delete Code
		ret = ret + "//Delete invalid rows:: CHECK WHERE CONDITION ::\n";
		String Pkfield, PkDatatype;
		Pkfield = getPkField(rsmain.fields("basetable").value());
		PkDatatype = getdatatype(rsmain.fields("basetable").value(), Pkfield);
		if (PkDatatype == "varchar" || PkDatatype == "nvarchar" || PkDatatype == "text" || PkDatatype == "ntext"
				|| PkDatatype == "datetime" || PkDatatype == "char" || PkDatatype == "nchar")
			ret = ret + GridSQL + "=\"Delete from " + rsmain.fields("basetable").value() + " where " + Pkfield
					+ " not in ('\" + " + GridId
					+ ".getColString(\"id\").replace(\",\",\"','\") + \"')\";//ADD OTHER CONDITION HERE\n";
		else
			ret = ret + GridSQL + "=\"Delete from " + rsmain.fields("basetable").value() + " where " + Pkfield
					+ " not in (\" + " + GridId + ".getColString(\"id\") + \")\";//ADD OTHER CONDITION HERE\n";

		ret = ret + "conn.execute(" + GridSQL + ");\n";

		String Loopvar, ID, ID1;
		Loopvar = GridId + "_L";
		ID = GridId + "ID";
		ID1 = GridId + "ID1";
		ret = ret + "int " + Loopvar + ";\n";
		ret = ret + "long " + ID + ";\n";
		ret = ret + ID + "=newidint();\n";
		ret = ret + "for(" + Loopvar + "=0;" + Loopvar + "<" + GridId + ".totalRows; " + Loopvar + "++){\n";
		ret = ret + GridId + ".moveto(" + Loopvar + ");\n";
		if (PkDatatype == "varchar" || PkDatatype == "nvarchar" || PkDatatype == "text" || PkDatatype == "ntext"
				|| PkDatatype == "datetime" || PkDatatype == "char" || PkDatatype == "nchar")
			ret = ret + "" + GridSQL + " =\" if(select count(" + Pkfield + ") from "
					+ rsmain.fields("basetable").value() + " where  " + Pkfield + "='\" + " + GridId
					+ ".getValue(\"id\") + \"')<1\" + \"\\n\" + \"Begin\" +\"\\n\";\n";
		else
			ret = ret + "" + GridSQL + " =\" if(select count(" + Pkfield + ") from "
					+ rsmain.fields("basetable").value() + " where  " + Pkfield + "='\" + " + GridId
					+ ".getValue(\"id\") + \"')<1\" + \"\\n\" + \"Begin\" +\"\\n\";\n";
		// ret=ret & "" & GridSQL &" ="" if(select count(" & Pkfield &") from " &
		// rsmain.fields("basetable") &" where " & Pkfield &"="" & " & GridID
		// &".row.item(""id"") & "")<1"" & vbcrlf & ""Begin"" & vbcrlf" & vbcrlf

		// Insert SQL
		String Isql, Iloop, Ivalue, Uvalue, dt;
		Isql = "";
		Ivalue = "\" + " + ID + " + \"";
		Isql = Isql + "insert into " + rsmain.fields("basetable").value() + "(" + Pkfield;
		Uvalue = "";
		rsdetail.moveFirst();
		while (!rsdetail.EOF()) {
			dt = rsdetail.fields("datatype").value().toLowerCase();
			if (!Uvalue.isBlank())
				Uvalue = Uvalue + ",";
			Isql = Isql + "," + rsdetail.fields("tablefield").value();

			if (dt == "nvarchar" || dt == "nchar" || dt == "ntext") {
				Uvalue = Uvalue + rsdetail.fields("tablefield").value() + "=" + "N'\" + conn.esc(" + GridId
						+ ".getValue(\"" + getColName(rsdetail.fields("colname").value()) + "\"))";
				Ivalue = Ivalue + ",N'\" + conn.esc(" + GridId + ".getValue(\""
						+ getColName(rsdetail.fields("colname").value()) + "\"))";
			} else if (dt == "varchar" || dt == "char" || dt == "text" || dt == "datetime") {
				Uvalue = Uvalue + rsdetail.fields("tablefield").value() + "=" + "'\" + conn.esc(" + GridId
						+ ".getValue(\"" + getColName(rsdetail.fields("colname").value()) + "\"))";
				Ivalue = Ivalue + ",'\" + conn.esc(" + GridId + ".getValue(\""
						+ getColName(rsdetail.fields("colname").value()) + "\"))";
			} else {
				Uvalue = Uvalue + rsdetail.fields("tablefield").value() + "=" + " dbo.val('\" + conn.esc(" + GridId
						+ ".getValue(\"" + getColName(rsdetail.fields("colname").value()) + "\"))";
				// languagegrid_SQL=languagegrid_SQL + "insert into
				// acc_bank_balance(id,id)values(" + languagegridID + ",dbo.val('" +
				// conn.esc(languagegrid.getValue("sdf"))+"')";
				Ivalue = Ivalue + ",dbo.val('\" + conn.esc(" + GridId + ".getValue(\""
						+ getColName(rsdetail.fields("colname").value()) + "\"))" + "+\"')";
			}
			rsdetail.moveNext();
		}

		rsdetail.moveFirst();
		Isql = Isql + ")values(" + Ivalue + ")\";\n";
		Isql = "\n" + GridSQL + "=" + GridSQL + " + \"" + Isql + "\n";
		ret = ret + Isql;
		ret = ret + "" + GridSQL + "=" + GridSQL + "+\"\\n End\" +\"\\n  Else \\n Begin \\n\";\n";
		// 'Update SQL
		Isql = "Update " + rsmain.fields("basetable").value() + " set " + Uvalue + " + \" where " + Pkfield + "='\" + "
				+ GridId + ".getValue(\"id\") + \"'\";\n";
		ret = ret + GridSQL + "=" + GridSQL + " + \"" + Isql + "\n";
		ret = ret + "" + GridSQL + "=" + GridSQL + " + \"\\n End\";\n";
		ret = ret + "conn.execute(" + GridSQL + ");\n";
		ret = ret + "}\n";
		ret = ret + "}\n";
		return ret;
	}

	public String getGridCodeJs(long formid, String gridid) {
		double m_gw = 200;
		String ret = "";

		RecordSet mrs = new RecordSet();
		mrs.open("select * from sys_tbldatagridmain where formid='" + formid + "' and gridname='" + gridid + "'", conn);
		if (mrs.recordCount < 1) {
			mrs.close();
			return "//no code\n";// +"select * from sys_tbldatagridmain where formid=" + formid + " and
									// gridname='" + gridid + "'";
		}

		String dhtmlgrid, jgrid, griddiv, gridheader;

		m_gw = val(mrs.fields("width").value());
		dhtmlgrid = mrs.fields("gridname").value() + "_grid";
		jgrid = "j" + mrs.fields("gridname").value();
		griddiv = mrs.fields("gridname").value();
		gridheader = griddiv + "header";
		ret = "";
		ret = ret + "<style>" + "\n";
		ret = ret + ".objbox{" + "\n";
		ret = ret + "height:" + (cint(mrs.fields("height").value()) - 40) + "px !important;" + "\n";
		ret = ret + "}" + "\n";
		ret = ret + "#" + griddiv + "{" + "\n";
		ret = ret + "height:" + mrs.fields("height").value() + "px !important;" + "\n";
		ret = ret + "width:<" + "% = getFramewidth(" + cint(mrs.fields("width").value()) + ") %" + ">px !important;"
				+ "\n";
		ret = ret + "}" + "\n";
		ret = ret + ".xhdr{" + "\n";
		ret = ret + "height:27px !important;" + "\n";
		ret = ret + "}" + "\n";
		ret = ret + ".hdr{" + "\n";
		ret = ret + "height:27px !important;" + "\n";
		ret = ret + "}" + "\n";
		ret = ret + "</style>" + "\n";
		ret = ret + "<input type=\"hidden\" id=\"" + griddiv + "_value\" name=\"" + griddiv + "_value\" value=\"\" />"
				+ "\n";
		ret = ret + "<input type=\"hidden\" id=\"" + griddiv + "_rowid\" name=\"" + griddiv + "_rowid\" value=\"\" />"
				+ "\n";
		ret = ret + "<input type=\"hidden\" id=\"" + griddiv + "_colname\" name=\"" + griddiv
				+ "_colname\" value=\"\" />" + "\n";
		ret = ret + "<input type=\"hidden\" id=\"" + griddiv + "_datatype\" name=\"" + griddiv
				+ "_datatype\" value=\"\" />" + "\n";

		ret = ret + "<script language=\"datagrid\">" + "\n";
		ret = ret + "		var " + dhtmlgrid + " = new dhtmlXGridObject('" + griddiv + "');" + "\n";
		ret = ret + dhtmlgrid + ".setImagePath(\"js/dhtmlx/grid/codebase/imgs/\");" + "\n";

		String colname, colcaption, colalign, coltypes, colsorting, coldtype, footerstring, colwidth;
		int colcount;
		colcount = 0;
		String validatetext, addrowtext, editrowtext, clearrowtext;

		validatetext = "\n" + "//--------------Validate Function ---------------" + "\n" + "function validate_"
				+ griddiv + "()" + "\n" + "{" + "\n";
		addrowtext = "\n" + "//--------------Addrow Function ---------------" + "\n" + "function addrow_" + griddiv
				+ "()" + "\n" + "{" + "\n" + "	if(!validate_" + griddiv + "())" + "\n" + "		return;" + "\n"
				+ " var " + jgrid + "row= new gridrow(" + jgrid + ");" + "\n";
		editrowtext = "";
		clearrowtext = "";

		String sno = mrs.fields("seroa;mp").value();
		if (sno.equalsIgnoreCase("1")) {
			colname = "id,sn";
			colcaption = getColCaption("SN");
			colwidth = getColWidth(m_gw, 5);
			colcount = 2;
			colalign = "left,left";
			coltypes = "ro,ro";
			colsorting = "str,int";
			coldtype = "str,int";
			footerstring = "#cspan";
		} else {

			colname = "id";
			colcaption = "";
			colcount = 1;
			colwidth = getColWidth(m_gw, 5);
			colalign = "left";
			coltypes = "ro";
			colsorting = "str";
			coldtype = "str";
			footerstring = "#cspan";
		}

		RecordSet drs = new RecordSet();

		drs.open("select * from sys_tbldatagriddetail where iid=" + mrs.fields("iid").value() + " order by colorder",
				conn);

		if (drs.recordCount > 0) {
			while (!drs.EOF()) {
				colcount = colcount + 1;
				// Validation Code
				if (drs.fields("compulsory").value().equalsIgnoreCase("1"))
					validatetext = validatetext + "if(document.getElementById('" + drs.fields("inputfield").value()
							+ "').value=='')" + "\n" + "{" + "\n" + "alert('" + drs.fields("inputfield").value()
							+ " should not be empty');" + "\n" + "document.getElementById('"
							+ drs.fields("inputfield").value() + "').focus();" + "\n" + "return false;" + "\n" + "}"
							+ "\n";

				editrowtext = editrowtext + "document.getElementById(\"" + drs.fields("inputfield").value()
						+ "\").value=" + jgrid + "row.fields(\"" + getColName(drs.fields("colname").value()) + "\");"
						+ "\n";

				if (drs.fields("clear").value().equalsIgnoreCase("1"))
					clearrowtext = clearrowtext + "document.getElementById('" + drs.fields("inputfield").value()
							+ "').value='';" + "\n";

				if (drs.fields("inputtype").value().contains("select")) {// 'This is combobox

					// For combo box value
					if (colcaption.isBlank()) {
						colname = colname + "," + getColName(drs.fields("colname").value());
						colcaption = getColCaption(drs.fields("colname").value());
					} else {

						colname = colname + "," + getColName(drs.fields("colname").value());
						colcaption = colcaption + "," + getColCaption(drs.fields("colname").value());
					}
					colwidth = colwidth + "," + getColWidth(m_gw, 0);
					colalign = colalign + "," + getColAlign(drs.fields("datatype").value());
					coltypes = coltypes + ",ro";
					colsorting = colsorting + "," + getColSorting(drs.fields("datatype").value());
					coldtype = coldtype + "," + getColDatatype(drs.fields("datatype").value());
					footerstring = footerstring + ",+amp;nbsp;";
					// For Combo Box Text
					colcount = colcount + 1;
					colname = colname + "," + getColName(drs.fields("colname").value() + "_text");
					colcaption = colcaption + "," + getColCaption(drs.fields("colname").value());
					colwidth = colwidth + "," + getColWidth(m_gw, Integer.parseInt(drs.fields("colwidth").value()));
					colalign = colalign + "," + getColAlign(drs.fields("datatype").value());
					coltypes = coltypes + ",ro";
					colsorting = colsorting + ",str";
					coldtype = coldtype + ",str";
					footerstring = footerstring + ",+amp;nbsp;";
					addrowtext = addrowtext + jgrid + "row.fields(\"" + getColName(drs.fields("colname").value())
							+ "\",document.getElementById(\"" + drs.fields("inputfield").value() + "\").value);" + "\n";
					addrowtext = addrowtext + jgrid + "row.fields(\""
							+ getColName(drs.fields("colname").value() + "_text") + "\",text(\""
							+ drs.fields("inputfield").value() + "\"));\n";
					// jdgridrow.fields("colname",document.getElementById("txtcolname").value);
				} else {
					addrowtext = addrowtext + jgrid + "row.fields(\"" + getColName(drs.fields("colname").value())
							+ "\",document.getElementById(\"" + drs.fields("inputfield").value() + "\").value);" + "\n";

					if (colcaption.isBlank()) {
						colname = colname + "," + getColName(drs.fields("colname").value());
						colcaption = getColCaption(drs.fields("colname").value());
					} else {
						colname = colname + "," + getColName(drs.fields("colname").value());
						colcaption = colcaption + "," + getColCaption(drs.fields("colname").value());
					}
					colwidth = colwidth + "," + getColWidth(m_gw, Integer.parseInt(drs.fields("colwidth").value()));
					colalign = colalign + "," + getColAlign(drs.fields("datatype").value());
					coltypes = coltypes + ",ro";
					footerstring = footerstring + ",+amp;nbsp;";
					colsorting = colsorting + "," + getColSorting(drs.fields("datatype").value());
					coldtype = coldtype + "," + getColDatatype(drs.fields("datatype").value());
				}

				// Create Add value, Edit Value and Create Value
				drs.moveNext();
			}
			drs.close();
		}

		clearrowtext = clearrowtext + "document.getElementById('" + griddiv + "_rowid').value='';" + "\n";

		if (mrs.fields("deletebutton").value().equalsIgnoreCase("1")) {
			colcount = colcount + 1;
			colname = colname + "," + getColName("Action");
			colcaption = colcaption + "," + getColCaption("Action");
			colwidth = colwidth + "," + getColWidth(m_gw, 5);
			colalign = colalign + ",left";
			coltypes = coltypes + ",ro";
			colsorting = colsorting + ",str";
			coldtype = coldtype + ",str";
			footerstring = footerstring + ",+amp;nbsp;";
			addrowtext = addrowtext + jgrid + "row.fields(\"action\",\"Delete\");" + "\n";
		}
		ret = ret + dhtmlgrid + ".setHeader('" + colcaption + "');" + "\n";
		// 'dhtmlgrid,jgrid,griddiv,gridheader
		// 'var
		// gheader="id,sn,colname,colorder,colwidth,inputfield,tablefield,compulsory,clear,action"
		ret = ret + "var " + gridheader + "=\"" + colname + "\";" + "\n";
		// 'dgrid_grid.setInitWidths("");
		ret = ret + dhtmlgrid + ".setInitWidths(\"" + colwidth + "\");" + "\n";
		ret = ret + dhtmlgrid + ".setColAlign(\"" + colalign + "\");" + "\n"; // dgrid_grid.setColAlign("left,left,left,left,right,right,right,right,right,right");
		ret = ret + dhtmlgrid + ".setColTypes(\"" + coltypes + "\");" + "\n";
//'dgrid_grid.setColTypes("ro,ro,ro,ro,ro,ro,ro,ro,ro,ro");
		ret = ret + dhtmlgrid + ".setColSorting(\"" + colsorting + "\");" + "\n";
//'dgrid_grid.setColSorting("str,str,str,str,str,str,str,str,str,str");
		ret = ret + dhtmlgrid + ".init();" + "\n";
		ret = ret + dhtmlgrid + ".attachFooter(\"" + footerstring + "\",[\"text-align:left;\"]);" + "\n";
		// 'dgrid_grid.init();
		ret = ret + dhtmlgrid + ".enableLightMouseNavigation(false);" + "\n";
		// 'dgrid_grid.enableLightMouseNavigation(false);
		ret = ret + dhtmlgrid + ".attachEvent(\"onRowDblClicked\", function(rId,cInd){displaydata_" + griddiv
				+ "(rId,cInd)});" + "\n";
		// 'dgrid_grid.attachEvent("onRowDblClicked",
		// function(rId,cInd){displaydata_dgrid(rId,cInd)});
		ret = ret + dhtmlgrid + ".setSkin(\"dhx_skyblue\");" + "\n";
		// 'dgrid_grid.setSkin("dhx_skyblue");
		ret = ret + "var " + jgrid + "=new datagrid(" + dhtmlgrid + ",'" + griddiv + "');" + "\n";
		// 'var jdgrid=new datagrid(dgrid_grid,'dgridvalue');
		ret = ret + jgrid + ".setformat(" + gridheader + ");" + "\n";
		ret = ret + jgrid + ".setdatatype(\"" + coldtype + "\");" + "\n";
		ret = ret + "document.getElementById('" + griddiv + "_datatype').value='" + coldtype + "';" + "\n";

//'jdgrid.setformat(gheader);
		validatetext = validatetext + "	return true;" + "\n" + "}" + "\n";
		ret = ret + validatetext;
		// 'Addrow function
		ret = ret + addrowtext;
		ret = ret + "var rowid=document.getElementById(\"" + griddiv + "_rowid\").value" + "\n";
		ret = ret + "if(rowid=='')" + "\n";
		ret = ret + "	{" + "\n";
		ret = ret + "	rowid=randid()*-1;" + "\n";
		ret = ret + "	sn=" + jgrid + ".rows()+1" + "\n";
		ret = ret + "	" + jgrid + "row.fields(\"id\",rowid);" + "\n";
		if (mrs.fields("serialno").value().equalsIgnoreCase("1")) {
			ret = ret + "	" + jgrid + "row.fields(\"sn\",sn);" + "\n";
		}
		ret = ret + "	" + jgrid + ".addrow(" + jgrid + "row);" + "\n";
		ret = ret + "}" + "\n";
		ret = ret + "else" + "\n";
		ret = ret + "{" + "\n";
		ret = ret + "	var rowindex=" + dhtmlgrid + ".getRowIndex(rowid);" + "\n";
		ret = ret + "	 if(rowid=='-1')" + "\n";
		ret = ret + "	 rowindex=" + jgrid + ".getrowbyid(rowid);" + "\n";
		ret = ret + "	" + jgrid + "row.fields(\"id\",rowid);" + "\n";
		if (mrs.fields("serialno").value().equalsIgnoreCase("1")) {
			// if "" + mrs.fields("serialno").value="1" then
			ret = ret + "	" + jgrid + "row.fields(\"sn\",(rowindex+1));" + "\n";
		}
		ret = ret + "	" + jgrid + ".updaterow(rowindex," + jgrid + "row);" + "\n";
		ret = ret + "}" + "\n";
		ret = ret + clearrowtext;
		ret = ret + "}" + "\n";
		ret = ret + "\n" + "//-----------------------Edit Row Function------------------" + "\n";
		ret = ret + "function displaydata_" + griddiv + "(rId,cInd)" + "\n";
		ret = ret + "	{" + "\n";
		ret = ret + "	var rowindex=" + dhtmlgrid + ".getRowIndex(rId);" + "\n";
		ret = ret + "if(rowindex==\"-1\")" + "\n";
		ret = ret + "{" + "\n";
		ret = ret + "	rowindex=" + jgrid + ".getrowbyid(rId);" + "\n";
		ret = ret + "}" + "\n";
		if (mrs.fields("deletebutton").value().equalsIgnoreCase("1")) {
			// if "" + mrs.fields("deletebutton").value="1" then
			ret = ret + "if(cInd==" + (colcount - 2) + ")" + "\n";
			ret = ret + "{ " + "\n";
			ret = ret + "	if(document.getElementById(\"" + griddiv + "_rowid\").value!=\"\")" + "\n";
			ret = ret + "		return;" + "\n";
			ret = ret + "	if(confirm(decode(\"Are you sure to delete?\")))" + "\n";
			ret = ret + "	{" + "\n";
			ret = ret + "			" + jgrid + ".removerow(rowindex);	" + "\n";
			if (mrs.fields("serialno").value().equalsIgnoreCase("1")) {
				// if "" + mrs.fields("serialno").value="1" then
				ret = ret + "			" + jgrid + ".resetsn();" + "\n";
			}

			ret = ret + "	}" + "\n";
			ret = ret + "	return;" + "\n";
			ret = ret + "}" + "\n";
		}
		ret = ret + "var " + jgrid + "row=" + jgrid + ".getrow(rowindex);" + "\n";
		ret = ret + "document.getElementById(\"" + griddiv + "_rowid\").value=" + jgrid + "row.fields(\"id\");" + "\n";
		ret = ret + editrowtext + "\n";
		ret = ret + "}" + "\n";

		ret = ret + "//-------------------- Grid Onchange Event----------------" + "\n";
		ret = ret + "function " + griddiv + "value_onchange()" + "\n";
		ret = ret + "{" + "\n";
		ret = ret + "//Write your code here" + "\n";
		ret = ret + "}" + "\n";
		ret = ret + "</script>" + "\n";

		return ret;
	}

	String getColAlign(String dtype) {
		dtype = dtype.toLowerCase();
		String ret = "";
		if (dtype == "int" || dtype == "numeric" || dtype == "float")
			ret = "rogjt";
		else
			ret = "left";
		return ret;
	}

	String getColSorting(String dtype) {
		dtype = dtype.toLowerCase();
		String ret = "";
		if (dtype == "int" || dtype == "numeric" || dtype == "float")
			ret = "int";
		else
			ret = "str";
		return ret;
	}

	String getColDatatype(String dtype) {
		dtype = dtype.toLowerCase();
		String ret = "";
		if (dtype == "int" || dtype == "numeric" || dtype == "float")
			ret = "int";
		else if (dtype == "datetime")
			ret = "date";
		else
			ret = "str";
		return ret;
	}

	String getColWidth(double m_gw, int w) {
		String ret = "";
		if (w == 0)
			ret = "0";
		else
			ret = "{{gws_g(" + m_gw + "," + w + ")}}";
		return ret;
	}

	String getColName(String a) {
		if (1 == 1)
			return a;
		String ret = a.toLowerCase();
		if (ret.isBlank())
			return "";
		else {
			ret = ret.replaceAll("\\s+", "");// remove all white space
			ret = ret.replace("_", "");
			ret = ret.replace(".", "");
			ret = ret.replace("[", "");
			ret = ret.replace("]", "");
			ret = ret.replace("{", "");
			ret = ret.replace("}", "");
			ret = ret.replace("/", "");
			ret = ret.replace("\\", "");
			return ret;
		}

	}

	String getColCaption(String a) {
		String ret = a.replace("\"", "\\\"");
		ret = "{{wds(\"" + ret + "\")}}";
		return ret;
	}

	String getElementType(long formid, String elementname) {
		String ret = "";
		RecordSet rs = new RecordSet();
		String sql = "select inputtype from sys_tblformelements where formid=" + formid + " and inputname='"
				+ elementname + "'";
		rs.open(sql, conn);
		if (rs.state == 1) {
			if (rs.recordCount > 0)
				ret = rs.fields(0).value();
			rs.close();
		}
		return ret;
	}

}
