package gvo.ecsysn;


import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.general.Util;

public class SysnOAMatrixInfo {
	public void sysnMatrix() {
		RecordSet rs = new RecordSet();		
		String sql = "";
		String oaTableName = "";// oa矩阵表名
		String ecTableName = "";// ec矩阵表名
		sql = "select * from uf_sysoa_matrixmap";
		rs.executeSql(sql);
		while(rs.next()) {
			oaTableName = Util.null2String(rs.getString("oatable"));
			ecTableName = Util.null2String(rs.getString("ectable"));
			if(!"".equals(oaTableName)&&!"".equals(ecTableName)) {
				copyMatirxData(oaTableName,ecTableName);
			}
			
		}
		
	}
	
	public void copyMatirxData(String oaTableName,String ecTableName) {
		RecordSetDataSource rsd = new RecordSetDataSource("oadb");
		RecordSetDataSource rsd_dt = new RecordSetDataSource("oadb");
		String fb = "";//分部
		String bm = "";//部门
		String zg = "";//主管
		String bmjl = "";//部门经理
		String zxzjl = "";//中心总经理
		String fzc = "";//副总裁
		String zc = "";//总裁
		String hrbp = "";//HRBP
		String subcompanycode = "";//
		String departmentcode = "";
		String sql_dt = "";
		String sql = "select * from "+oaTableName+" order by dataorder asc";
		rsd.executeSql(sql);
		while(rsd.next()) {
			fb = Util.null2String(rsd.getString("fb"));
			bm = Util.null2String(rsd.getString("bm"));
			zg = Util.null2String(rsd.getString("zg"));
			bmjl = Util.null2String(rsd.getString("bmjl"));
			zxzjl = Util.null2String(rsd.getString("zxzjl"));
			fzc = Util.null2String(rsd.getString("fzc"));
			zc = Util.null2String(rsd.getString("zc"));
			hrbp = Util.null2String(rsd.getString("hrbp"));
			
			sql_dt = "select subcompanycode from hrmsubcompany where id="+fb;
			rsd_dt.executeSql(sql_dt);
			if(rsd_dt.next()) {
				subcompanycode = Util.null2String(rsd_dt.getString("subcompanycode"));
			}
			
			sql_dt = "select departmentcode from hrmdepartment where id="+bm;
			rsd_dt.executeSql(sql_dt);
			if(rsd_dt.next()) {
				departmentcode = Util.null2String(rsd_dt.getString("departmentcode"));
			}
			insertOrUpdateMatrix(ecTableName,subcompanycode,departmentcode,getWorkcodes(zg),getWorkcodes(bmjl),getWorkcodes(zxzjl),getWorkcodes(fzc),getWorkcodes(zc),getWorkcodes(hrbp));
		}
	}
	
	
	
	
	
	
	public void insertOrUpdateMatrix(String ecTableName,String subcompanycode,String departmentcode,String zgcode,String bmjlcode,String zxzjlcode,String fzccode,String zccode,String hrbpcode) {
		RecordSet rs = new RecordSet();
		BaseBean log = new BaseBean();
		String sql = "";
		String matrixTable = ecTableName;
		String Dataorder = "0";
		String mtid = "";//矩阵数据id
		String fb = "";
		String bm = "";
		String zg = "";
		String bmjl = "";
		String zxzjl = "";
		String fzc = "";
		String zc = "";
		String hrbp = "";
		sql = "select id from hrmdepartment where departmentcode='"+departmentcode+"'";
		rs.execute(sql);
		if(rs.next()) {
			bm = Util.null2String(rs.getString("id"));
		}
		sql = "select id from hrmsubcompany where subcompanycode='"+subcompanycode+"'";
		rs.execute(sql);
		if(rs.next()) {
			fb = Util.null2String(rs.getString("id"));
		}
		if("".equals(bm) || "".equals(fb)) {
			log.writeLog("insertOrUpdateMatrix 部门或公司为空 bm:"+departmentcode+" gs:"+subcompanycode);
			return;
		}
		zg = getRyids(zgcode);
		bmjl = getRyids(bmjlcode);
		zxzjl = getRyids(zxzjlcode);
		fzc = getRyids(fzccode);
		zc = getRyids(zccode);
		hrbp = getRyids(hrbpcode);
		sql = "select nvl(max(dataorder),0)+1 as Dataorder from "+matrixTable;
		rs.executeSql(sql);
		if(rs.next()) {
			Dataorder = Util.null2String(rs.getString("Dataorder"));
		}
		sql="select uuid from "+matrixTable+" where fb='"+fb+"' and bm='"+bm+"'";
		rs.executeSql(sql);
		if(rs.next()) {
			mtid = Util.null2String(rs.getString("uuid"));
		}
		if("".equals(mtid)) {
			sql = "insert into "+matrixTable+"(uuid,Dataorder,fb,bm,zg,bmjl,zxzjl,fzc,zc,hrbp)"
					+ " values(sys_guid(),"+Dataorder+",'"+fb+"','"+bm+"','"+zg+"','"+bmjl+"'," +
							"'"+zxzjl+"','"+fzc+"','"+zc+"','"+hrbp+"')";
		   boolean result=rs.executeSql(sql);
		   if(!result) {
			   log.writeLog("insertOrUpdateMatrix sql:"+sql);
			   return ;
		   }
		}else {
			sql="update "+matrixTable+" set zg='"+zg+"',bmjl='"+bmjl+"',zxzjl='"+zxzjl+"',fzc='"+fzc+"',zc='"+zc+"',hrbp='"+hrbp+"' where uuid='"+mtid+"'";
			boolean result=rs.executeSql(sql);
			if(!result) {
				log.writeLog("insertOrUpdateMatrix sql:"+sql);
				return ;
			}
		}
	}
	
	public String getWorkcodes(String ryids) {
		RecordSetDataSource rsd = new RecordSetDataSource("oadb");
		String sql = "";
		String workcode = "";
		String workcodes = "";
		String flag = "";
		if("".equals(ryids)) {
			return "";
		}
		sql = "select workcode from hrmresource where id in("+ryids+")";
		rsd.executeSql(sql);
		while(rsd.next()) {
			workcode = Util.null2String(rsd.getString("workcode"));
			if(!"".equals(workcode)) {
				workcodes = workcodes+flag+"'"+workcode+"'";
				flag = ",";
			}
		}
		return workcodes;
	}
	
	public String getRyids(String workcodes) {
		RecordSet rs = new RecordSet();
		String sql = "";
		String ryid = "";
		String ryids = "";
		String flag = "";
		if("".equals(workcodes)) {
			return "";
		}
		sql = "select id from hrmresource where workcode in("+workcodes+") ";
		rs.executeSql(sql);
		while(rs.next()) {
			ryid = Util.null2String(rs.getString("id"));
			if(!"".equals(ryid)) {
				ryids = ryids+flag+ryid;
				flag = ",";
			}
		}
		return ryids;
	}
}
