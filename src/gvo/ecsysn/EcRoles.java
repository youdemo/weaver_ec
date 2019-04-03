package gvo.ecsysn;

import java.util.HashMap;
import java.util.Map;

import gvo.tmc.util.TmcDBUtil;
import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.Util;

public class EcRoles {

	// 全量数据
	/**
	 * 创建2个系统对应表
	 	create table gvo_other_role(
	 		other_id int,
	 		now_id int
	 	)
	 	
	 	create table gvo_HrmRoleMembers(
	 		roleid int,
	 		resourceid int,
	 		rolelevel int,
	 		orderby int
	 	)
	 	
	 * 
	 */
	String oadb = "oadb";
	
	public  void sysRole(){
		RecordSetDataSource rsd = new RecordSetDataSource(oadb);
		RecordSetDataSource rsd_dt = new RecordSetDataSource(oadb);
		RecordSet rs = new RecordSet();
		String sql_dt = "";
		// 全量数据检查
		String sql = "select * from HrmRoles where isdefault is null or isdefault=''";
		rsd.execute(sql);
		while(rsd.next()){
			String id = Util.null2String(rsd.getString("id"));
			// rolesmark,rolesname,docid,isdefault,type,subcompanyid,ecology_pinyin_search
			String rolesmark = Util.null2String(rsd.getString("rolesmark"));
			String rolesname = Util.null2String(rsd.getString("rolesname"));
			if(rolesname.length()>50){
				rolesname=rolesname.substring(0, 50);  
			}
			String docid = Util.null2String(rsd.getString("docid"));
			String isdefault = Util.null2String(rsd.getString("isdefault"));
			String type = Util.null2String(rsd.getString("type"));
			String subcompanyid = Util.null2String(rsd.getString("subcompanyid"));
			String ecology_pinyin_search = Util.null2String(rsd.getString("ecology_pinyin_search"));
			String subcompanycode = "";
			sql_dt = "select subcompanycode from hrmsubcompany where id="+subcompanyid;
			rsd_dt.executeSql(sql_dt);
			if(rsd_dt.next()) {
				subcompanycode = Util.null2String(rsd_dt.getString("subcompanycode"));
			}
			subcompanyid = "";
			sql_dt = "select id from hrmsubcompany where subcompanycode='"+subcompanycode+"'";
			rs.executeSql(sql_dt);
			if(rs.next()) {
				subcompanyid = Util.null2String(rs.getString("id"));
			}
			String now_id = "";  
			
			sql_dt = "select now_id from gvo_other_role where other_id="+id;
			rs.executeSql(sql_dt);
			if(rs.next()){
				now_id = Util.null2String(rs.getString("now_id"));
				// 更新
				sql_dt = "update HrmRoles set rolesmark='"+rolesmark+"',"
					 +"rolesname='"+rolesname+"',"
					 +"docid='"+docid+"',"
					 +"isdefault='"+isdefault+"',"
					 +"type='"+type+"',"
					 +"subcompanyid='"+subcompanyid+"',"
					 +"ecology_pinyin_search='"+ecology_pinyin_search+"' where id="+now_id;
				rs.executeSql(sql_dt);
			}else{
				// 插入
				TmcDBUtil tdu = new TmcDBUtil();
				Map<String,String> mapStr = new HashMap<String,String>();
				// ,rolesname,docid,isdefault,type,subcompanyid,ecology_pinyin_search
				mapStr.put("rolesmark", rolesmark);
				mapStr.put("rolesname", rolesname);
				mapStr.put("docid", docid);
				mapStr.put("isdefault", isdefault);
				mapStr.put("type", type);
				mapStr.put("subcompanyid", subcompanyid);
				mapStr.put("ecology_pinyin_search", ecology_pinyin_search);
				tdu.insert(mapStr, "HrmRoles");
				
				// 获取插入的角色ID
				sql_dt = "select max(id) as maxID from HrmRoles where rolesmark='"+rolesmark+"'";
				rs.executeSql(sql_dt);
				if(rs.next()){
					now_id = Util.null2String(rs.getString("maxID"));
				}
				if(!"".equals(now_id)){
				mapStr = new HashMap<String,String>();
				mapStr.put("other_id", id);
				mapStr.put("now_id", now_id);
				tdu.insert(mapStr, "gvo_other_role");
				}
			}
			// 处理人员
			// 0  删除已有数据；数据全量获取过来
			if(!"".equals(now_id)){
				sql_dt = "delete from gvo_HrmRoleMembers where roleid = " + now_id;
				rs.executeSql(sql_dt);
			}
			boolean isContainAdmin = false;
			String admin_rolelevel = "";
			String admin_orderby = "";
			// roleid,resourceid,rolelevel,orderby
			sql_dt = "select * from HrmRoleMembers where roleid="+id;
			rsd_dt.executeSql(sql_dt);
			while(rsd_dt.next()){
				String t_resourceid = Util.null2String(rsd_dt.getString("resourceid"));
				String t_rolelevel = Util.null2String(rsd_dt.getString("rolelevel"));
				String t_orderby = Util.null2String(rsd_dt.getString("orderby"));
				
				if("1".equals(t_resourceid)) {
					isContainAdmin = true;
					admin_rolelevel = t_rolelevel;
					admin_orderby = t_orderby;
					continue;
				}
				
				String emp_id = getNowHrmId(t_resourceid);
				if(Util.null2String(emp_id).length() < 1) continue;
				
				TmcDBUtil tdu = new TmcDBUtil();
				if(!"".equals(now_id)){
				Map<String,String> mapStr = new HashMap<String,String>();
				mapStr.put("roleid", now_id);
				mapStr.put("resourceid", emp_id);
				mapStr.put("rolelevel", t_rolelevel);
				mapStr.put("orderby", t_orderby);
				
				tdu.insert(mapStr, "gvo_HrmRoleMembers");
				}
			}
			
			// 1 删除没有的结果集
			if(!"".equals(now_id)){
				sql_dt  = "delete from HrmRoleMembers where roleid="+now_id 
					+ " and resourceid not in(select resourceid from gvo_HrmRoleMembers where roleid="
					+ now_id + ")";
				rs.executeSql(sql_dt);
			// 2 插入的结果集
				sql_dt = "insert into HrmRoleMembers(roleid,resourceid,rolelevel,orderby) "
				 +" select roleid,resourceid,rolelevel,orderby from gvo_HrmRoleMembers where roleid="+now_id
				 +" and resourceid not in(select resourceid from HrmRoleMembers where roleid="+ now_id+")";
				rs.executeSql(sql_dt);
			
			// 3 管理员单独处理
			if(isContainAdmin){
				TmcDBUtil tdu = new TmcDBUtil();
				Map<String,String> mapStr = new HashMap<String,String>();
				mapStr.put("roleid", now_id);
				mapStr.put("resourceid", "1");
				mapStr.put("rolelevel", admin_rolelevel);
				mapStr.put("orderby", admin_orderby);
				
				tdu.insert(mapStr, "HrmRoleMembers");
			}
			}
		}
	}
	
	public String getNowHrmId(String id){
		if(id == null||"".equals(id)) return ""; 
		String emp_id = "";
		RecordSetDataSource rsd = new RecordSetDataSource(oadb);
		String sql = "select workcode from hrmresource where id="+id;
		rsd.executeSql(sql);
		String wc = "";
		if(rsd.next()){
			wc = Util.null2String(rsd.getString("workcode"));
		}
		if(wc.length() < 1) return "";
		RecordSet rs = new RecordSet();
		
		sql = "select * from hrmresource where workcode = '" + wc + "'";
		rs.executeSql(sql);
		if(rs.next()){
			emp_id = Util.null2String(rs.getString("id"));
		}
		return emp_id;
	}
}
