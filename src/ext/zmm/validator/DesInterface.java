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
	//�رշ���������
	public DesInterface(){
		
	//	Thread.sleep(1000);
	}
	
	//ֱ�ӳ�ʼ����������ص�����
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

	//ֱ�ӳ�ʼ����������ص�����
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
	
	//�ļ�״̬
	public boolean Filestate(String log_filename,String sfile){
		boolean bRet = false;
		int   []tt = Desinter.FileIsEncodeFile(sfile);
		if(tt[0]==1){
		   //�ӿڲ�����ȷ
		   if(tt[1]==1){
			log.log("", "2", "�ж��ļ�"+sfile+"�Ƿ�ӽ���", log_filename, "0", log_filename+"�ļ�Ϊ�����ļ���");
			bRet = true;
		   }else{
			System.out.println("Ϊ�������ļ���");//�ļ����Ǽ��ܵ�
			log.log("", "2", "�ж��ļ�"+sfile+"�Ƿ�ӽ���", log_filename, "0", log_filename+"�ļ�Ϊ�������ļ���");
		   }
		}else{
		   //����ԭ��
		   int ID = Desinter.getLastErrorID();
		   log.log("", "2", "�ж��ļ�"+sfile+"�Ƿ�ӽ���", log_filename, "1", log_filename+"�����ж���ɣ��жϴ��󣬴�����: "+  ID);
		}
		return bRet;
	}

	public  boolean Encode(String log_filename,String sfile,String pfile,boolean isenfile){
		boolean isSuccess = Desinter.fileOperationFile(sfile, pfile, isenfile);
		if(isSuccess){
		//	System.out.println((isenfile?"����":"����")+"�ɹ�!");
			log.log("", "2", "�ļ�"+(isenfile?"����":"����"), log_filename, "0", "�ɹ���");
		}else{
			 int error = Desinter.getLastErrorID();
			// System.out.println((isenfile?"����":"����")+"ʧ�ܡ�������:"+ID);
			 log.log("", "2", "�ļ�"+(isenfile?"����":"����"), log_filename, "1", "ʧ�ܡ������룺" + error+";ϵͳ���⣬��Ҫ�������²�����");
		}
		return isSuccess;
	}
}