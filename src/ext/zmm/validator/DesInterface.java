package ext.zmm.validator;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import gvo.log.GvoLog;

public class DesInterface {

	static boolean ISRun=false;
	static DESService Desinter = new DESService();
	private GvoLog log = new GvoLog();
	static BaseBean logx = new BaseBean();
	static{
	//	String username = "dlltest";
	//	String password = "test2014";
		logx.writeLog("DesInterface start!!");
	//	init(username,password);
		init();
	}
	//关闭服务器连接
	public DesInterface(){
		
	//	Thread.sleep(1000);
	}
	
	//直接初始化并设置相关的数据
	public static void init()  {
		
		String sql = "select serverip,username,password from uf_password where uq='DesInterface'";
		RecordSet rs0 = new RecordSet();
		rs0.executeSql(sql);
		String User = "";
		String pwd = "";
		String s_ip = "";
		if(rs0.next()){
			s_ip = Util.null2String(rs0.getString("serverip")).trim();
			User = Util.null2String(rs0.getString("username")).trim();
			pwd = Util.null2String(rs0.getString("password")).trim();
		}
		
		logx.writeLog("init0{s_ip:" + s_ip + ",User:" + User + ",pwd=" + pwd + "}");
		
		if(s_ip.length() < 2 || User.length() < 2 || pwd.length() < 2){
			s_ip = "10.80.4.104";
			User = "dlltest";
			pwd = "test2014";
			
			logx.writeLog("init1{s_ip:" + s_ip + ",User:" + User + ",pwd=" + pwd + "}");
		}
		
		int id = 19440392;
		Desinter.SetLinkID(id);
		int type = 1;
		Desinter.SetLinkType(type);
		//Desinter.setServerMessage("10.80.4.203","10.80.4.203");
		Desinter.setServerMessage(s_ip,s_ip);
		Desinter.initSystemFile(User,pwd);
		ISRun = true;
	}

	//直接初始化并设置相关的数据
	public static int init_x()  {
		String sql = "select serverip,username,password from uf_password where uq='DesInterface'";
		RecordSet rs0 = new RecordSet();
		rs0.executeSql(sql);
		String User = "";
		String pwd = "";
		String s_ip = "";
		if(rs0.next()){
			s_ip = Util.null2String(rs0.getString("serverip")).trim();
			User = Util.null2String(rs0.getString("username")).trim();
			pwd = Util.null2String(rs0.getString("password")).trim();
		}
			
		logx.writeLog("init_x0{s_ip:" + s_ip + ",User:" + User + ",pwd=" + pwd + "}");
			
		if(s_ip.length() < 2 || User.length() < 2 || pwd.length() < 2){
			s_ip = "10.80.4.104";
			User = "dlltest";
			pwd = "test2014";
		
			logx.writeLog("init_x1{s_ip:" + s_ip + ",User:" + User + ",pwd=" + pwd + "}");
		}
			
		int id = 19440392;
		Desinter.SetLinkID(id);
		int type = 1;
		Desinter.SetLinkType(type);
		Desinter.setServerMessage(s_ip,s_ip);
		Desinter.initSystemFile(User,pwd);
		ISRun = true;
		
		int tmp_in = Desinter.getLastErrorID();
		
		return tmp_in;
	}
	
	//文件状态
	public boolean Filestate(String log_filename,String sfile){
		boolean bRet = false;
		int   []tt = Desinter.FileIsEncodeFile(sfile);
		if(tt[0]==1){
		   //接口操作正确
		   if(tt[1]==1){
			log.log("", "2", "判断文件"+sfile+"是否加解密", log_filename, "0", log_filename+"文件为加密文件！");
			bRet = true;
		   }else{
			System.out.println("为不加密文件！");//文件不是加密的
			log.log("", "2", "判断文件"+sfile+"是否加解密", log_filename, "0", log_filename+"文件为不加密文件！");
		   }
		}else{
		   //错误原因
		   int ID = Desinter.getLastErrorID();
		   log.log("", "2", "判断文件"+sfile+"是否加解密", log_filename, "1", log_filename+"加密判断完成！判断错误，错误码: "+  ID);
		}
		return bRet;
	}

	public  boolean Encode(String log_filename,String sfile,String pfile,boolean isenfile){
		boolean isSuccess = Desinter.fileOperationFile(sfile, pfile, isenfile);
		if(isSuccess){
		//	System.out.println((isenfile?"加密":"解密")+"成功!");
			log.log("", "2", "文件"+(isenfile?"加密":"解密"), log_filename, "0", "成功！");
		}else{
			 int error = Desinter.getLastErrorID();
			// System.out.println((isenfile?"加密":"解密")+"失败。错误码:"+ID);
			 log.log("", "2", "文件"+(isenfile?"加密":"解密"), log_filename, "1", "失败。错误码：" + error+";系统问题，需要检查后重新操作！");
		}
		return isSuccess;
	}
}