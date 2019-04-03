package gvo.ecsysn;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.Util;

public class SysOAHrmMidTable {
	
	public void sysMidTable() {
		sysHrActivitycode();
		sysHrJobtitlecode();
	}
	
	/**
	 * 同步职务中间表
	 */
	public void sysHrActivitycode() {
		RecordSetDataSource rsd = new RecordSetDataSource("oadb");
		String sql = "";
		String zwid = "";//职务id
		String zwms = "";//职务描述
		String zwjc = "";//职务简称
		String sxzt = "";//生效状态
		sql = "select * from uf_hr_activitycode";
		rsd.executeSql(sql);
		while(rsd.next()) {
			zwid = Util.null2String(rsd.getString("zwid"));
			zwms = Util.null2String(rsd.getString("zwms"));
			zwjc = Util.null2String(rsd.getString("zwjc"));
			sxzt = Util.null2String(rsd.getString("sxzt"));
			insertEcActivitycode(zwid,zwms,zwjc,sxzt);
		}
	}
	
	/**
	 * 同步岗位中间表
	 */
	public void sysHrJobtitlecode() {
		RecordSetDataSource rsd = new RecordSetDataSource("oadb");
		String sql = "";
		String positionNbr = "";//职位id
		String jobcode = "";//职务code
		String locationId = "";//地点ID
		String channel = "";//通道
		String sequence = "";//序列
		String descr = "";//职位描述
		String descrShort = "";//职位简称
		String status = ""; // 生效状态
		String deptId = ""; // 部门ID
		String vacancies = ""; // 编制人数
		String graderank = "";//jason标签
		String reportPosn = "";
		String damagePosn = "";
		sql = "select * from uf_hr_jobtitlecode";
		rsd.executeSql(sql);
		while(rsd.next()) {
			positionNbr = Util.null2String(rsd.getString("positionNbr"));
			jobcode = Util.null2String(rsd.getString("jobcode"));
			locationId = Util.null2String(rsd.getString("locationId"));
			channel = Util.null2String(rsd.getString("channel"));
			sequence = Util.null2String(rsd.getString("sequence"));
			descr = Util.null2String(rsd.getString("descr"));
			descrShort = Util.null2String(rsd.getString("descrShort"));
			status = Util.null2String(rsd.getString("status"));
			deptId = Util.null2String(rsd.getString("deptId"));
			vacancies = Util.null2String(rsd.getString("vacancies"));
			graderank = Util.null2String(rsd.getString("graderank"));
			reportPosn = Util.null2String(rsd.getString("reportPosn"));// 直接上级职位
			damagePosn = Util.null2String(rsd.getString("damagePosn"));// 危害岗位
			insertHrjobtitlecode(positionNbr,jobcode,locationId,channel,sequence,descr,descrShort,status,deptId,vacancies,graderank,reportPosn,damagePosn);
		}
	}
	
	/**
	 * 插入ec岗位中间表
	 * @param positionNbr
	 * @param jobcode
	 * @param locationId
	 * @param channel
	 * @param sequence
	 * @param descr
	 * @param descrShort
	 * @param status
	 * @param deptId
	 * @param vacancies
	 * @param graderank
	 */
	public void insertHrjobtitlecode(String positionNbr,String jobcode,String locationId,String channel,String sequence,String descr,String descrShort,String status,String deptId,String vacancies,String graderank,String reportPosn,String damagePosn) {
		RecordSet rs = new RecordSet();
		String sql="select count(id) as count from uf_hr_jobtitlecode where positionNbr = '" + positionNbr + "'";
		int count = 0;
		rs.executeSql(sql);
		if(rs.next()) {
			count = rs.getInt("count");
		}
		if (count > 0) {
			sql = "update uf_hr_jobtitlecode set jobcode = '" + jobcode + "',locationId = '" + locationId
					+ "',channel='" + channel + "',sequence='" + sequence + "',descr='" + descr + "',descrShort='" + descrShort + "',status='" + status + "',deptId='" + deptId + "'" + 
					",vacancies='" + vacancies + "',graderank='" + graderank + "',reportPosn='"+reportPosn+"',damagePosn='"+damagePosn+"' where positionNbr = '" + positionNbr+ "'";
			rs.executeSql(sql);
		} else {
			sql = "insert into uf_hr_jobtitlecode(positionNbr,jobcode,locationId,channel,sequence,descr,descrShort,status,deptId,vacancies,graderank,reportPosn,damagePosn) values ('"
					+ positionNbr + "','" + jobcode + "','" + locationId + "','" + channel + "','" + sequence+ "','" + descr+ "','" + descrShort+ "','" + status+ "','" + deptId+ "','" + vacancies+ "','" + graderank+ "','"+reportPosn+"','"+damagePosn+"')";
			rs.executeSql(sql);
		}
	}
	
	/**
	 * 插入ec职务中间表
	 * @param zwid
	 * @param zwms
	 * @param zwjc
	 * @param sxzt
	 */
	public void insertEcActivitycode(String zwid,String zwms,String zwjc,String sxzt) {
		RecordSet rs = new RecordSet();
		String sql="select count(1) as count from uf_hr_activitycode where zwid='"+zwid+"'";
		int count = 0;
		rs.executeSql(sql);
		if(rs.next()) {
			count = rs.getInt("count");
		}
		if (count > 0) {
			sql = "update uf_hr_activitycode set zwms = '" + zwms + "' , zwjc = '" + zwjc
					+ "',sxzt='"+sxzt+"' where zwid = '" + zwid + "'";
			rs.executeSql(sql);
		} else {
			sql = "insert into uf_hr_activitycode(zwid,zwms,zwjc,sxzt) values ('" + zwid + "','" + zwms + "','"
					+ zwjc + "','"+sxzt+"')";
			rs.executeSql(sql);
		}
	}
}
