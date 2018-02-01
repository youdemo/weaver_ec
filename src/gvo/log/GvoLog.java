package gvo.log;


import weaver.conn.RecordSet;

public class GvoLog {
		
	public String log_id(){
		int log_id = -1;
		RecordSet rs = new RecordSet();
		rs.executeSql("select gvo_all_log_seqno.nextval as log_id from dual");
		if(rs.next()){
			log_id = rs.getInt("log_id");
		}
		
		if(log_id > 0){
			rs.executeSql("insert into gvo_all_log(id) values("+log_id+")");
		}
		
//		log_id = new Random().nextInt(1000);
		
		return String.valueOf(log_id);
	}
	
	public void log(String oper_desc){
		String id = "gvo_all_log_seqno.nextval";
		String oper_emp = "0";String oper_type = "O";
		String oper_info = "";String is_success="O";String return_info="";
		
		log(id, oper_emp, oper_type, oper_info, oper_desc, is_success, return_info);
	}
	
	public void log_up(String id,String oper_emp,String oper_type,String oper_info,
			String oper_desc,String is_success,String return_info){
		RecordSet rs = new RecordSet();
		StringBuffer buffer = new StringBuffer();
		
		//   update gvo_all_log set oper_emp=,oper_time=,oper_type,oper_info=,oper_desc=,is_success=,return_info where id=	
		buffer.append("update gvo_all_log set oper_emp_code='");buffer.append(oper_emp);buffer.append("',");
		buffer.append("oper_type=");buffer.append(oper_type);buffer.append(",");
		buffer.append("oper_time=to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),");
		buffer.append("oper_info='");buffer.append(oper_info);buffer.append("',");
		buffer.append("oper_desc='");buffer.append(oper_desc);buffer.append("',");
		buffer.append("is_success='");buffer.append(is_success);buffer.append("',");
		buffer.append("return_info='");buffer.append(return_info);
		buffer.append("' where id=");buffer.append(id);
//		System.out.println(buffer.toString());
		rs.executeSql(buffer.toString());
	}
	
	public void log(String id,String oper_emp,String oper_type,String oper_info,
				String oper_desc,String is_success,String return_info){
		RecordSet rs = new RecordSet();
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("insert into gvo_all_log(id,oper_emp_code,oper_time,oper_type,oper_info,oper_desc,is_success,return_info) values(");
		buffer.append(id);buffer.append(",'");
		buffer.append(oper_emp);buffer.append("',to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),'");
		buffer.append(oper_type);buffer.append("','");
		buffer.append(oper_info);buffer.append("','");
		buffer.append(oper_desc);buffer.append("','");
		buffer.append(is_success);buffer.append("','");
		buffer.append(return_info);buffer.append("')");
	//	System.out.println(buffer.toString());
		rs.executeSql(buffer.toString());
	}
	
	public void log(String oper_emp,String oper_type,String oper_info,
			String oper_desc,String is_success,String return_info){
		RecordSet rs = new RecordSet();
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("insert into gvo_all_log(id,oper_emp_code,oper_time,oper_type,oper_info,oper_desc,is_success,return_info) values(");
		buffer.append("gvo_all_log_seqno.nextval,'");
		buffer.append(oper_emp);buffer.append("',to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),'");
		buffer.append(oper_type);buffer.append("','");
		buffer.append(oper_info);buffer.append("','");
		buffer.append(oper_desc);buffer.append("','");
		buffer.append(is_success);buffer.append("','");
		buffer.append(return_info);buffer.append("')");
	//	System.out.println(buffer.toString());
		rs.executeSql(buffer.toString());
	}
}
