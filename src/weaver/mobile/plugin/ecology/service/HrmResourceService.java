package weaver.mobile.plugin.ecology.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.ws.commons.util.Base64;
import weaver.hrm.tools.HrmResourceMobileTools;
import weaver.conn.BatchRecordSet;
import weaver.conn.RecordSet;
import weaver.file.FileUpload;
import weaver.file.Prop;
import weaver.general.BaseBean;
import weaver.general.GCONST;
import weaver.general.StaticObj;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import weaver.hrm.appdetach.AppDetachComInfo;
import weaver.hrm.company.CompanyComInfo;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.company.SubCompanyComInfo;
import weaver.hrm.group.GroupAction;
import weaver.hrm.job.JobTitlesComInfo;
import weaver.hrm.location.LocationComInfo;
import weaver.hrm.resource.ResourceComInfo;
import weaver.hrm.settings.ChgPasswdReminder;
import weaver.hrm.settings.HrmSettingsComInfo;
import weaver.ldap.LdapUtil;
import weaver.login.TokenJSCX;
import weaver.sms.SMSManager;
import weaver.systeminfo.SystemEnv;
import weaver.wechat.util.WechatUtil;
import weaver.workflow.workflow.WorkTypeComInfo;
import weaver.workflow.request.todo.RequestUtil;

public class HrmResourceService extends BaseBean {
	
	//统一使用AVAILABLE_WORKFLOW常量
	private static String formids = weaver.mobile.plugin.ecology.RequestOperation.AVAILABLE_WORKFLOW;
	private static Map<String, Object> ALL_USER = new HashMap<String, Object>();
	
	
	public int getUserId(String loginId) {
		try {
			String sql = "";
			
			String mode=Prop.getPropValue(GCONST.getConfigFile() , "authentic");
	    	if("ldap".equals(mode)){
	    		//loginid、account字段整合 qc:128484
	    		sql = "select id from HrmResource where loginid='" + loginId + "' and (accounttype is null  or accounttype=0 or accounttype=1) ";
				//sql = "select id from HrmResource where account='" + loginId + "' and (accounttype is null  or accounttype=0) ";
			} else {
				sql = "select id from HrmResource where loginid='" + loginId + "' and (accounttype is null  or accounttype=0 or accounttype=1) ";
			}
			
			sql += " union select id from HrmResourcemanager where loginid='" + loginId + "'";
			RecordSet rs = new RecordSet();
			rs.executeSql(sql);
			if (rs.next() && Util.getIntValue(rs.getString(1), 0) > 0) {
				return Util.getIntValue(rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public User getUserById(int userId) {
		RecordSet rs = new RecordSet();
		User user = null;
		String sql = "select * from HrmResource where id=" + userId + "";
		rs.executeSql(sql);
		if(rs.getCounts()==0){
			sql="select * from HrmResourceManager where id="+userId;
			rs.executeSql(sql); 
		}
		if (rs.next()) {
			user = new User();
			user.setUid(rs.getInt("id"));
			user.setLoginid(rs.getString("loginid"));
			user.setFirstname(rs.getString("firstname"));
			user.setLastname(rs.getString("lastname"));
			user.setAliasname(rs.getString("aliasname"));
			user.setTitle(rs.getString("title"));
			user.setTitlelocation(rs.getString("titlelocation"));
			user.setSex(rs.getString("sex"));
			user.setMobilecall(rs.getString("mobilecall"));
			user.setStatus(rs.getInt("status"));
			
			String languageidweaver = rs.getString("systemlanguage");
			user.setLanguage(Util.getIntValue(languageidweaver, 7));

			user.setTelephone(rs.getString("telephone"));
			user.setMobile(rs.getString("mobile"));
			user.setMobilecall(rs.getString("mobilecall"));
			user.setEmail(rs.getString("email"));
			user.setCountryid(rs.getString("countryid"));
			user.setLocationid(rs.getString("locationid"));
			user.setResourcetype(rs.getString("resourcetype"));
			user.setContractdate(rs.getString("contractdate"));
			user.setJobtitle(rs.getString("jobtitle"));
			user.setJobgroup(rs.getString("jobgroup"));
			user.setJobactivity(rs.getString("jobactivity"));
			user.setJoblevel(rs.getString("joblevel"));
			user.setSeclevel(rs.getString("seclevel"));
			user.setUserDepartment(Util.getIntValue(rs.getString("departmentid"), 0));
			user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
			user.setUserSubCompany2(Util.getIntValue(rs.getString("subcompanyid2"), 0));
			user.setUserSubCompany3(Util.getIntValue(rs.getString("subcompanyid3"), 0));
			user.setUserSubCompany4(Util.getIntValue(rs.getString("subcompanyid4"), 0));
			user.setManagerid(rs.getString("managerid"));
			user.setAssistantid(rs.getString("assistantid"));
			user.setPurchaselimit(rs.getString("purchaselimit"));
			user.setCurrencyid(rs.getString("currencyid"));
			user.setLogintype("1");
			user.setAccount(rs.getString("account"));
			user.setRemark(rs.getString("workcode"));//因User的Bean中无workcode字段，临时借用remark放置workcode编号字段
		}
		return user;
	}
	
	public List<String> getRelativeUser(int userId) throws Exception {
		List<String> result = new ArrayList<String>();
		String master = userId+"";
		
		RecordSet rs = new RecordSet();
		String sql = "select belongto from HrmResource where id=" + userId;
		rs.executeSql(sql);
		if (rs.next()) {
			String belongto = rs.getString("belongto");
			if (NumberUtils.toInt(belongto, -1)>0) {
				master = belongto;
			}
		}
		result.add(master);
		
		sql = "select id from HrmResource where belongto=" + master + " and (status = 0 or status = 1 or status = 2 or status = 3) and status != 10";
		rs.executeSql(sql);
		while (rs.next()) {
			String id = rs.getString("id");
			if (NumberUtils.toInt(id, -1)>0) {
				result.add(id);
			}
		}
		return result;
	}
	
	public Map setUserAvatar(FileUpload fu,User user){
		Map data = new HashMap();
		String uploaddata = Util.null2String(fu.getParameter("uploaddata"));
	    try {
			byte[] arrAppendData = Base64.decode(uploaddata);
	   	
		    String uploadPath = GCONST.getRootPath() + "messager"+ File.separatorChar + "usericon";
		    String iconName="loginid"+TimeUtil.getFormartString(Calendar.getInstance(),"yyyyMMddHHmmss")+".jpg";
			String targetUrl = uploadPath+ File.separatorChar +iconName;
		    //自动创建目录：
		    if (!new File(uploadPath).isDirectory())
		    	new File(uploadPath).mkdirs();
		    FileOutputStream outputStream = new FileOutputStream(new File(targetUrl));      
		    outputStream.write(arrAppendData);   
		    outputStream.close();    
		    RecordSet rs =  new RecordSet();
		    String strSql="update hrmresource set messagerurl='/messager/usericon/"+iconName+"' where id="+user.getUID();		
			rs.executeSql(strSql);   
			ResourceComInfo rci = new ResourceComInfo();
			rci.updateResourceInfoCache(user.getUID()+"");
			data.put("imgurl", "/messager/usericon/"+iconName);
			data.put("message", 1);
	    } catch (Exception e) {
	    	writeLog("Catch a exception during decode data", e);
	    	data.put("error", e.getMessage());
	    }
		return data;
	}
	
	public Map getUserByUserid(int userId,User user) throws Exception {
		ResourceComInfo rci = new ResourceComInfo();
		LocationComInfo lci = new LocationComInfo();
		JobTitlesComInfo jci = new JobTitlesComInfo();
		DepartmentComInfo dci = new DepartmentComInfo();
		SubCompanyComInfo sci = new SubCompanyComInfo();
			RecordSet rs = new RecordSet();
			String sql = "select * from HrmResource where id=" + userId + "";
			rs.executeSql(sql);
			Map userMap = new HashMap();
			if (rs.next()) {
				userMap.put("ID", rs.getString("id"));
				userMap.put("Name", rs.getString("lastname"));
				userMap.put("PYName", rs.getString("pinyinlastname"));
				userMap.put("HeaderURL", rs.getString("messagerurl"));
		
				String subcom = rs.getString("subcompanyid1");
				userMap.put("SubCompanyID", subcom);
				userMap.put("SubCompanyName", sci.getSubCompanyname(subcom));
				
				String dept = rs.getString("departmentid");
				userMap.put("DepartmentID", dept);
				userMap.put("DepartmentName", dci.getDepartmentname(dept));
				
				userMap.put("mobile", rs.getString("mobile"));
				userMap.put("tel", rs.getString("telephone"));
				userMap.put("email", rs.getString("email"));
				
				String jobtitle = rs.getString("jobtitle");
				userMap.put("title", jci.getJobTitlesname(jobtitle));
				
				String manager = rs.getString("managerid");
				userMap.put("managerID", manager);
				userMap.put("managerName", rci.getLastname(manager));
				
				userMap.put("statusName", this.getStatusName(rs.getString("status"), user));
				
				userMap.put("loginid", rs.getString("login_id"));
				
				userMap.put("showorder", rs.getString("dsporder"));
				
				String locationid = rs.getString("locationid");
				userMap.put("locationID", locationid);
				userMap.put("locationName", lci.getLocationname(locationid));
			}
			return userMap;
	}
	
	public Map getUser(int userId,User user) throws Exception {
		
		ResourceComInfo rci = new ResourceComInfo();
		LocationComInfo lci = new LocationComInfo();
		JobTitlesComInfo jci = new JobTitlesComInfo();
		DepartmentComInfo dci = new DepartmentComInfo();
		SubCompanyComInfo sci = new SubCompanyComInfo();
		
		User touser = getUserById(userId);
		//"jobtitle":"执行总裁","username":"杨文元","userid":"509","subcom":"集团管理总部","dept":"总裁办公室","iscur":"1"
		Map data = new HashMap();
		data.put("jobtitle", jci.getJobTitlesname(touser.getJobtitle()));
		data.put("name", touser.getLastname());
		data.put("id", touser.getUID()+"");
		data.put("subcom", sci.getSubCompanyname(touser.getUserSubCompany1()+""));
		data.put("dept", dci.getDepartmentname(touser.getUserDepartment()+""));
		data.put("iscur", (touser.getUID()==user.getUID())?"1":"0");
		//{"sex":"0","manager":"","status":"正式","location":"上海","lastname":"倪云","msgerurl":"","id":"508","jobtitle":"董事会主席",
		//"email":"ecologydemo@weaver.com.cn","dept":"董事会","subcom":"集团管理总部","telephone":"02150942228","mobile":"15000209807"}
		data.put("sex", touser.getSex());
		
		String manager = Util.null2String(touser.getManagerid());
		String managername = rci.getLastname(manager);
		data.put("manager", managername);
		
		String status = Util.null2String(touser.getStatus()+"");
		String statusname = "";

        if(status.equals("")) status = "8";
        if(status.equals("9")) statusname=SystemEnv.getHtmlLabelName(332,user.getLanguage());
        if(status.equals("0")) statusname=SystemEnv.getHtmlLabelName(15710,user.getLanguage());
        if(status.equals("1")) statusname=SystemEnv.getHtmlLabelName(15711,user.getLanguage());
        if(status.equals("2")) statusname=SystemEnv.getHtmlLabelName(480,user.getLanguage());
        if(status.equals("3")) statusname=SystemEnv.getHtmlLabelName(15844,user.getLanguage());
        if(status.equals("4")) statusname=SystemEnv.getHtmlLabelName(6094,user.getLanguage());
        if(status.equals("5")) statusname=SystemEnv.getHtmlLabelName(6091,user.getLanguage());
        if(status.equals("6")) statusname=SystemEnv.getHtmlLabelName(6092,user.getLanguage());
        if(status.equals("7")) statusname=SystemEnv.getHtmlLabelName(2245,user.getLanguage());
        if(status.equals("8")) statusname=SystemEnv.getHtmlLabelName(1831,user.getLanguage());
        data.put("status", statusname);

		String location = Util.null2String(touser.getLocationid());
		String locationname = lci.getLocationname(location);
		data.put("location", locationname);
		
		data.put("lastname", Util.null2String(touser.getLastname()));
		
		data.put("headerpic", Util.null2String(rci.getMessagerUrls(touser.getUID()+"")));
		
		data.put("id", Util.null2String(touser.getUID()+""));
		
		data.put("email", Util.null2String(touser.getEmail()));
		
		data.put("telephone", Util.null2String(touser.getTelephone()));
		
		data.put("mobile", Util.null2String(touser.getMobile()));
		
		data.put("workcode", Util.null2String(touser.getRemark()));
		
		data.put("isadmin", "0");
		if(HrmUserVarify.checkUserRight("Mobile:Setting", touser)) {
			data.put("isadmin", "1");
		}
		
		data.put("departmentid", rci.getDepartmentID(""+userId));
		
		data.put("subcompanyid", rci.getSubCompanyID(""+userId));
		
		return data;
	}
	
	/**
	 * @return
	 * 0:数据库密码验证
	 * 1:动态密码验证
	 * 2:LDAP验证
	 * 3:INI密码存储模式
	 * 
	 */
	public int getLoginType(String loginId) {
		String mode=Prop.getPropValue(GCONST.getConfigFile() , "authentic");
    	if(mode!=null&&mode.equals("ldap")){
    		 return 2;
    	}
    	
    	HrmSettingsComInfo sci=new HrmSettingsComInfo();
    	int needdynapass_sys = Util.getIntValue(sci.getNeeddynapass());
    	if(needdynapass_sys==1) {
    		return 1;
    	}
    	
    	try {
        	Class t = Class.forName("weaver.hrm.resource.UserSecComInfo");
        	Object usci = t.newInstance();
        	
        	StaticObj staticobj = StaticObj.getInstance();
        	List ids = ((List) staticobj.getRecordFromObj("UserSecInfo", "ids"));
        	List loginids = ((List) staticobj.getRecordFromObj("UserSecInfo", "loginids"));
        	List passwords = ((List) staticobj.getRecordFromObj("UserSecInfo", "passwords"));

        	if(usci!=null && ids!=null && loginids!=null && passwords!=null) {
        		return 3;
        	}
    	} catch(Exception e) {
//    		e.printStackTrace();
    	}
    	
		return 0;
	}

	
	/**
	 * @return
	 * 0:需要输入动态密码
	 * 1:登录成功
	 * 2:登录密码不匹配
	 * 3:用户id为空
	 * 4:用户不存在
	 * 5:出现异常
	 * 6:用户动态密码不匹配
	 * 7:动态密码短信发送失败
	 * 8:请输入密码令牌
	 * 9:密码令牌不正确
	 * 36:令牌未进行初始化操作
	 */
	public int checkLogin(String loginId, String password, String dynapass, String tokenpass, int policy) {
		
		// start---通过微信集成平台进行登陆认证----康渊炎
		int wxresult=weaver.weixin.sdkforthird.WechatApiForEc.checkUserLogin(loginId, password, dynapass, tokenpass, policy);
		if(wxresult!=weaver.weixin.sdkforthird.WechatApiForEc.NOCHECKBYEWECHAT){  // 如果来自微信集成平台访问
			return wxresult;
		}
		// end---通过微信集成平台进行登陆认证
		if("_wechat".equals(tokenpass)) {
			int tokenUid = WechatUtil.verifyToken(password);
			int loginUid = this.getUserId(loginId);
			if(tokenUid > 0 && loginUid > 0 && tokenUid==loginUid) {
				return 1;
			} else {
				return 4;
			}
		}
		
		int loginType = this.getLoginType(loginId);
		
		int result = -1;
		
		//policy 0 不启用 1 验证码 2 动态密码 3 动态令牌
		if(loginType==1 && policy==2) { //动态密码验证
			result = this.verifyLoginByDyna(loginId, password,dynapass);
		} else if(loginType==2 && policy==2) {  
			result = this.verifyLoginByDynaForAD(loginId, password,dynapass);
		}else if(loginType==2 && policy!=2){//LDAP验证
			result = this.verifyLoginByLdap(loginId, password);
		} else if(loginType==3) { //INI密码存储模式
			result = this.verifyLoginByIni(loginId, password);
		} else {
			// loginType == 0 默认数据库密码验证
			result = verifyLoginByDb(loginId, password);
		}
		
		if(result==1 && policy==3) {
			TokenJSCX tj = new TokenJSCX();
			int tokenkeyStatus = tj.checkTokenkeyStatus(loginId);
			if(tokenkeyStatus == 2) {
				return verifyLoginByToken(loginId,tokenpass);
			} else if(tokenkeyStatus == 1) {
				return 27;
			}
		}
		
		if(result==1 && policy==4) {
			result = this.verifyLoginForBindUDID(loginId, password,dynapass);
		}
		
		return result;
	}
	
	
	private int verifyLoginByDynaForAD(String loginId, String password,String dynapass) {
		
		try {
			String sql = "";
			RecordSet rs = new RecordSet();
		
			RecordSet rs1 = new RecordSet();
	        RecordSet rs3 = new RecordSet();
	        
	        String idTemp ="0";
	        //ad登陆验证
	        int result = verifyLoginByLdap(loginId, password);
	        String oaid = "";
	        if(result==1){
	        	oaid = isADAccountAndOneLoginid(loginId);
	        	if(!"".equals(oaid)){//OA中只存在一个AD账号，不进行大小写区分进行验证登录
	        		sql = "select id,needdynapass,mobile from HrmResource where upper(loginid)='"+loginId.toUpperCase()+"' and id="+oaid+" and (accounttype is null  or accounttype=0)";
	        	}else{
	        		sql = "select id,needdynapass,mobile from HrmResource where loginid='"+loginId+"' and (accounttype is null  or accounttype=0)";
	        	}
		        rs.executeSql(sql);
		        if(rs.next()&&(Util.getIntValue(rs.getString("id"),0)>0)) {
		            idTemp = rs.getString("id");
		            int needdynapass = rs.getInt("needdynapass");
		            if(needdynapass!=0&&needdynapass!=1){
		            	needdynapass= 1;
		            }
		            String mobile = rs.getString("mobile");
		            /*
		             * 系统人员导入有可能产生needdynapass,passwordstate等字段为NUll，在hrmpassword表中也没有
		             * 添加ID和Loginid，这些字段为Null或在表中没有记录，会出现登陆验证错误或动态密码错误。
		             * */
		            if(needdynapass==1){
		            	rs1.executeSql("select id from hrmpassword where id='"+idTemp+"'") ;
		            	if(rs1.next()) ;
		            	else rs1.executeSql("insert into hrmpassword(id,loginid) values("+idTemp+",'"+loginId+"')") ;
		            	

	                	if(dynapass==null||"".equals(dynapass)) {
	                 		
	                        	HrmSettingsComInfo sci=new HrmSettingsComInfo();
	                        	int needdynapass_sys = Util.getIntValue(sci.getNeeddynapass());
	                        	int dynapasslen = Util.getIntValue(sci.getDynapasslen());
	                        	//动态密码内容
	                        	String dypadcon = Util.null2String(sci.getDypadcon());
	
				                boolean ipaddress= false; 
				                int passwordstateip = 1;
				                if(needdynapass_sys==1) {
				                	if(!"".equals(oaid)){//OA中只存在一个AD账号，不进行大小写区分进行验证登录
				                		sql = "select password,usbstate as passwordstate from HrmResource where upper(loginid)='"+loginId.toUpperCase()+"' and id="+oaid+" and (accounttype is null  or accounttype=0)";
				                	}else{
				                		sql = "select password,usbstate as passwordstate from HrmResource where loginid='"+loginId+"' and (accounttype is null  or accounttype=0)";
				                	}
				                	rs3.executeSql(sql);
				                	if(rs3.next()) passwordstateip = rs3.getInt("passwordstate");
				                }
				                ipaddress = true;//checkIpSeg(request, loginid, passwordstateip);//检查网段策略
				                if ((passwordstateip==0 || passwordstateip ==2) && dynapasslen > 0&&ipaddress) {
				                    SMSManager sm = new SMSManager();
				                    String newdynapass = "";
				                    if(dypadcon.equals("0")){
				                    	newdynapass = Util.passwordBuilderNo(dynapasslen);
				                    }else if (dypadcon.equals("1")){
				                    	newdynapass = Util.passwordBuilderEn(dynapasslen);
				                    }else if (dypadcon.equals("2")){
				                    	newdynapass = Util.passwordBuilder(dynapasslen);
				                    }
				                    //String dynapass = Util.passwordBuilder(dynapasslen);
				                    String currentdatetime=TimeUtil.getCurrentTimeString();
				                    boolean flag = sm.sendSMS(mobile, "您在"+currentdatetime+"登陆系统的动态密码为：" + newdynapass+",IP："+"mobile");
				                    if (flag){
				                        rs3.executeSql("update hrmpassword set password='"+Util.getEncrypt(newdynapass)+"' where id=" + idTemp);
				                        return 0; //return login page and relogin by dynamic password
				                    } else {
				                    	return 7;
				                    }
	
				                } else {
				                	return 1;
				                }
				          
	                		
	                	} else {
	                        
	                        rs3.executeSql("select password from hrmpassword where id=" + idTemp +" and password='"+Util.getEncrypt(dynapass)+"'");
	                        if (rs3.next()) {
	                            rs3.executeSql("update hrmpassword set password='' where id=" + idTemp);
	                            return 1;
	                        } else {
	                 			return 6;
	                		}
	                	}
	                
		            }else{
		            	return 1;
		            }
		          
		        }else{
		        	return 4;
		        }
	        }else{
	        	return result;
	        }
	        
		} catch (Exception e) {
			// TODO: handle exception
			writeLog(e);
			return 5;
		}
	}

	private int verifyLoginForBindUDID(String loginId,String password,String dynapass) {
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		String passwordTemp ="";
		String idTemp ="0";
		String mobile = "";
		String sql = "select id,mobile from HrmResource where loginid='"+loginId+"'";
        rs.executeSql(sql);
        if(rs.next()&&(Util.getIntValue(rs.getString("id"),0)>0)) {
            idTemp = rs.getString("id");
            mobile = rs.getString("mobile");
        }
        /*
         * 系统人员导入有可能产生needdynapass,passwordstate等字段为NUll，在hrmpassword表中也没有
         * 添加ID和Loginid，这些字段为Null或在表中没有记录，会出现登陆验证错误或动态密码错误。
         * */
        rs1.executeSql("select id from hrmpassword where id='"+idTemp+"'") ;
        if(rs1.next()) ;
        else rs1.executeSql("insert into hrmpassword(id,loginid) values("+idTemp+",'"+loginId+"')") ;
        
		sql = "select password from HrmResource where id = " + idTemp;
        rs.executeSql(sql);
        if (rs.next()) {
            passwordTemp = Util.null2String(rs.getString("password"));
			if(dynapass==null||"".equals(dynapass)) {
	            if (passwordTemp.equals(Util.getEncrypt(password))) {
	            	int dynapasslen = 4;
	                if (dynapasslen > 0) {
	                    SMSManager sm = new SMSManager();
	                    String newdynapass = Util.passwordBuilderNo(dynapasslen);
	                    String currentdatetime=TimeUtil.getCurrentTimeString();
	                    boolean flag = sm.sendSMS(mobile, "您在"+currentdatetime+"设备绑定密码为：" + newdynapass+",IP："+"mobile");
	                    if (flag){
	                    	rs1.executeSql("update hrmpassword set password='"+Util.getEncrypt(newdynapass)+"' where id=" + idTemp);
	                        return 0; //return login page and relogin by dynamic password
	                    } else {
	                    	return 7;
	                    }
	                } else {
	                	return 1;
	                }
	                
	            } else {
	            	return 2;
	            }
	    	} else {
	    		rs1.executeSql("select password from hrmpassword where id=" + idTemp +" and password='"+Util.getEncrypt(dynapass)+"'");
	            if (rs1.next()) {
	            	rs1.executeSql("update hrmpassword set password='' where id=" + idTemp);
	                return 1;
	            } else {
	     			return 6;
	    		}
	    	}
        }
        return 5;
	}

	private int verifyLoginByIni(String loginId,String password){
		try {
			String sql = "select id from HrmResource where loginid='" + loginId + "' and (accounttype is null  or accounttype=0) union select id from HrmResourcemanager where loginid='" + loginId + "'";
	        String idTemp = "0";
			RecordSet rs = new RecordSet();
			rs.executeSql(sql);
	        if(rs.next() && Util.getIntValue(rs.getString(1), 0) > 0) {
	        	idTemp = rs.getString(1);
	        	
	        	if(idTemp==null||idTemp.equals("")) return 3;
	        	
	        	if(!idTemp.toLowerCase().equals("sysadmin")) {

		        	StaticObj staticobj = StaticObj.getInstance();
	
		        	List ids = ((List) staticobj.getRecordFromObj("UserSecInfo", "ids"));
		        	List loginids = ((List) staticobj.getRecordFromObj("UserSecInfo", "loginids"));
		        	List passwords = ((List) staticobj.getRecordFromObj("UserSecInfo", "passwords"));
		        	
		        	if(ids==null||loginids==null||passwords==null) {
			        	Class t = Class.forName("weaver.hrm.resource.UserSecComInfo");
			        	Object usci = t.newInstance();
	
			        	ids = ((List) staticobj.getRecordFromObj("UserSecInfo", "ids"));
			        	loginids = ((List) staticobj.getRecordFromObj("UserSecInfo", "loginids"));
			        	passwords = ((List) staticobj.getRecordFromObj("UserSecInfo", "passwords"));
		        	}
	
		        	if(ids!=null&&loginids!=null&&passwords!=null) {
		        		for(int i=0;i<loginids.size();i++) {
		        			String tid = (String)ids.get(i);
		        			String tloginid = (String)loginids.get(i);
		        			String tpassword = (String)passwords.get(i);
		        			if(tloginid!=null&&tloginid.equals(loginId)){
		        				if(tpassword!=null&&tpassword.equals(Util.getEncrypt(password))){
		        					return 1;
		        				} else {
		        					return 2;
		        				}
		        			}
		        		}
		        	}
	        	
	        	} else {
	        		
					char c_code[] = new char[32];
	                try {
	                    FileReader fr = new FileReader(GCONST.getRootPath() + File.separator + "WEB-INF" + File.separator + "keys" + File.separator + idTemp + ".sys");
	                    fr.read(c_code);
	                    fr.close();
	                    String realcode = (new String(c_code)).trim();
	                    if(realcode.equals(Util.getEncrypt(password)))
	                        return 1;
	                    else
	                    	return 2;
	                } catch(Exception e) {
	                    writeLog(e);
	                }
	        		
	        	}
	        	
        		return 3;
	        } else {
	        	return 4;
	        }
		} catch(Exception e){
//			e.printStackTrace();
			return 5;
		}
	}
	
	private int verifyLoginByToken(String loginid,String tokenpass){
		try {
			RecordSet rs = new RecordSet();

			String sql = "select tokenkey from HrmResource where loginid='" + loginid + "' and (accounttype is null  or accounttype=0)";
			String tokenkey = "";
			rs.executeSql(sql);
			if(rs.next()){
				tokenkey = rs.getString("tokenkey");
			}
			if(tokenkey!=null&&StringUtils.isNotEmpty(tokenpass)){
				TokenJSCX tj = new TokenJSCX();
				boolean isTokenAuthKeyPass=false;
				sql="select * from tokenJscx WHERE tokenKey='"+tokenkey+"'";
    			rs.execute(sql);
    			if(rs.next()){
        			if(tokenkey.startsWith("1"))
        			    isTokenAuthKeyPass=tj.checkDLKey(tokenkey,tokenpass);
        			else if(tokenkey.startsWith("2"))
        			    isTokenAuthKeyPass=tj.checkDLKey(tokenkey,tokenpass);
        			else if(tokenkey.startsWith("3"))
        				isTokenAuthKeyPass=tj.checkKey(tokenkey,tokenpass); 
        			
	        		if(isTokenAuthKeyPass)	
						return 1;
					else
						return 9;
    			}else
    				return 36;
			}else{
				return 8;
			}
		} catch(Exception e){
			e.printStackTrace();
			return 5;
		}
	}
	/**
     * 输入的loginid不区分大小写loginid时候只有一个用户在oa并且是ad账号，不进行大小写区分进行ad认证接口调用
     * @return 对应ad 账号的oa id
     */
    private String isADAccountAndOneLoginid(String loginid){
        System.out.println("~~~~"+loginid);
        String oaid = "";
        String countLoginidMatchCaseSql = "select isADAccount,id from hrmresource where isADAccount=1 and upper(loginid) = '"+ loginid.toUpperCase() +"'";
        RecordSet rs = new RecordSet();
        rs.executeSql(countLoginidMatchCaseSql);
        if(rs.next()) {
            int count = rs.getCounts();
            if(count==1){
                LdapUtil util = LdapUtil.getInstance();
                String authenticResult = util.authenticuser(loginid);//判断用户是否存在AD中存在，或被禁用
                if(authenticResult != null && !"uac".equals(authenticResult)) {
                	oaid = rs.getString("id");
                }
            }
        }
        return oaid;
    }
	private int verifyLoginByLdap(String login_id,String user_password){
		try {
			RecordSet rs = new RecordSet();
            ChgPasswdReminder reminder=new ChgPasswdReminder();
            String mode = Prop.getPropValue(GCONST.getConfigFile(), "authentic");
            /***解决AD同步过后分权管理员不能登录***/
            boolean isloginlpad = true;
            if (mode != null && mode.equals("ldap")){
            	rs.executeSql("select * from HrmResourceManager where loginid = '"+login_id+"'");
            	if(rs.next()) isloginlpad = false;
            }
            
            //loginid、account字段整合  判断账户类型是否为AD账户 qc:128484
            String isADAccount = "";
            isADAccount = isADAccount(login_id);
            
            if (ifEqlTarget(mode, "ldap") && ifEqlTarget(isADAccount, "1") && isloginlpad) {
            	LdapUtil util = LdapUtil.getInstance();
                boolean flag = util.authentic(login_id, user_password);
                if (flag) {
                	return 1;
                } else {
                	return 2;
                }
            } else {
            	return verifyLoginByDb(login_id,user_password);
            }
		} catch(Exception e){
			e.printStackTrace();
			return 5;
		}
	}
	/**
	 * 判断用户输入账号是否ad账号
	 * @param login_id
	 * @return
	 */
	private String isADAccount(String login_id) {
		String isADAccount = "";
		RecordSet rs = new RecordSet();
		rs.executeSql("select isADAccount from HrmResource where loginid = '"+login_id+"'");
		if(rs.next()) {
			isADAccount = rs.getString("isADAccount");
		}
		
		String oaid = "";
		String isOneAccountSql = "select count(id) from hrmresource where upper(loginid) = '"+ login_id.toUpperCase() +"'";
		rs.executeSql(isOneAccountSql);
		if(rs.next()) {//如果大小写不敏感只有一个账户
			if(1 == rs.getInt(1)){
				isADAccount = "1";
			}
		}
		if(!"".equals(isADAccount)){
			oaid = isADAccountAndOneLoginid(login_id);
		}
		if(!"".equals(oaid)){//OA中只存在一个AD账号，不进行大小写区分进行ad验证登录
		    isADAccount = "1";
		}else{
			isADAccount = "";
		}
		return isADAccount;
	}
	
	private int verifyLoginByDb(String login_id,String user_password){
		try {
			RecordSet rs = new RecordSet();
			String sql = "select id,password from HrmResource where loginid='" + login_id + "' and (accounttype is null  or accounttype=0) union select id,password from HrmResourcemanager where loginid='" + login_id + "'";
	        String passwordTemp = "";
			rs.executeSql(sql);
	        if(rs.next() && Util.getIntValue(rs.getString(1), 0) > 0) {
                passwordTemp = Util.null2String(rs.getString(2));
                if (passwordTemp.equals(Util.getEncrypt(user_password)))
                	return 1;
                else
                	return 2;
	        } else {
	        	return 3;
	        }
		} catch(Exception e){
			e.printStackTrace();
			return 5;
		}
	}
	
	private int verifyLoginByDyna(String loginId,String password,String dynapass) {
		try {
			String sql = "";
			RecordSet rs = new RecordSet();
			
			//判断是否为系统管理员登陆
			sql="select * from HrmResourcemanager where loginid='"+loginId+"'";
			rs.execute(sql);
			if(rs.next()){ 
				return this.verifyLoginByDb(loginId, password);
			}	
	        
	        RecordSet rs1 = new RecordSet();
	        RecordSet rs3 = new RecordSet();
	        
	        String idTemp ="0";
	        String passwordTemp ="";
	        sql = "select id,needdynapass,mobile from HrmResource where loginid='"+loginId+"' and (accounttype is null  or accounttype=0)";
	        rs.executeSql(sql);
	        if(rs.next()&&(Util.getIntValue(rs.getString("id"),0)>0)) {
	            idTemp = rs.getString("id");
	            int needdynapass = rs.getInt("needdynapass");
	            if(needdynapass!=0&&needdynapass!=1){
	            	needdynapass= 1;
	            }
	            String mobile = rs.getString("mobile");
	            /*
	             * 系统人员导入有可能产生needdynapass,passwordstate等字段为NUll，在hrmpassword表中也没有
	             * 添加ID和Loginid，这些字段为Null或在表中没有记录，会出现登陆验证错误或动态密码错误。
	             * */
	            if(needdynapass==1){
	            	rs1.executeSql("select id from hrmpassword where id='"+idTemp+"'") ;
	            	if(rs1.next()) ;
	            	else rs1.executeSql("insert into hrmpassword(id,loginid) values("+idTemp+",'"+loginId+"')") ;
	            }
	            sql = "select password from HrmResource where id = " + idTemp;
	            rs1.executeSql(sql);
	            if (rs1.next()) {
	                passwordTemp = Util.null2String(rs1.getString("password"));
	                if (needdynapass != 1) {
	                    if (passwordTemp.equals(Util.getEncrypt(password)))
	                        return 1;
	                    else 
	                    	return 2;
	                } else {
                    	if(dynapass==null||"".equals(dynapass)) {
                    		
                            if (passwordTemp.equals(Util.getEncrypt(password))) {
                        		
                            	HrmSettingsComInfo sci=new HrmSettingsComInfo();
                            	int needdynapass_sys = Util.getIntValue(sci.getNeeddynapass());
                            	int dynapasslen = Util.getIntValue(sci.getDynapasslen());
                            	//动态密码内容
                            	String dypadcon = Util.null2String(sci.getDypadcon());

				                boolean ipaddress= false; 
				                int passwordstateip = 1;
				                if(needdynapass_sys==1) {
				                	sql = "select password,usbstate as passwordstate from HrmResource where loginid='"+loginId+"' and (accounttype is null  or accounttype=0)";
				                	rs3.executeSql(sql);
				                	if(rs3.next()) passwordstateip = rs3.getInt("passwordstate");
				                }
				                ipaddress = true;//checkIpSeg(request, loginid, passwordstateip);//检查网段策略
				                if ((passwordstateip==0 || passwordstateip ==2) && dynapasslen > 0&&ipaddress) {
				                    SMSManager sm = new SMSManager();
				                    String newdynapass = "";
				                    if(dypadcon.equals("0")){
				                    	newdynapass = Util.passwordBuilderNo(dynapasslen);
				                    }else if (dypadcon.equals("1")){
				                    	newdynapass = Util.passwordBuilderEn(dynapasslen);
				                    }else if (dypadcon.equals("2")){
				                    	newdynapass = Util.passwordBuilder(dynapasslen);
				                    }
				                    //String dynapass = Util.passwordBuilder(dynapasslen);
				                    String currentdatetime=TimeUtil.getCurrentTimeString();
				                    boolean flag = sm.sendSMS(mobile, "您在"+currentdatetime+"登陆系统的动态密码为：" + newdynapass+",IP："+"mobile");
				                    if (flag){
				                        rs3.executeSql("update hrmpassword set password='"+Util.getEncrypt(newdynapass)+"' where id=" + idTemp);
				                        return 0; //return login page and relogin by dynamic password
				                    } else {
				                    	return 7;
				                    }

				                } else {
				                	return 1;
				                }
				                
                            } else {
                            	return 2;
                            }
                    		
                    	} else {
                            
                            rs3.executeSql("select password from hrmpassword where id=" + idTemp +" and password='"+Util.getEncrypt(dynapass)+"'");
                            if (rs3.next()) {
                                rs3.executeSql("update hrmpassword set password='' where id=" + idTemp);
                                return 1;
                            } else {
                     			return 6;
                    		}
                    	}
                    }
	            } else {
	            	return 3;
	            }
	        } else {
	        	return 4;
	        }
		} catch(Exception e) {
			e.printStackTrace();
			return 5;
		}
	}

	public Map getUserList(List conditions, int pageIndex, int pageSize, int hrmorder, User user) throws Exception {
		Map result = new HashMap();
		List list = new ArrayList();
		int count = 0;
		int pageCount = 0;
		int isHavePre = 0;
		int isHaveNext = 0;
		if (user != null) {
			RecordSet rs = new RecordSet();
			
			ResourceComInfo rci = new ResourceComInfo();
			LocationComInfo lci = new LocationComInfo();
			JobTitlesComInfo jci = new JobTitlesComInfo();
			DepartmentComInfo dci = new DepartmentComInfo();
			SubCompanyComInfo sci = new SubCompanyComInfo();
			
			String sql = " from hrmresource ";
			sql += " where (status = 0 or status = 1 or status = 2 or status = 3) and status != 10 ";

			for(int i=0;conditions!=null&&conditions.size()>0&&i<conditions.size();i++) {
				String condition = (String) conditions.get(i);
				if(StringUtils.isNotEmpty(condition)) {
					sql += " and " + condition + " ";
				}
			}

			sql = " select count(*) as c " + sql;

			rs.executeSql(sql);
			
			if(rs.next())
			count = rs.getInt("c");

			if (count <= 0) pageCount = 0;
			pageCount = count / pageSize + ((count % pageSize > 0)?1:0);
			
			isHaveNext = (pageIndex + 1 <= pageCount)?1:0;

			isHavePre = (pageIndex - 1 >= 1)?1:0;
			
			
			sql = " * from hrmresource ";
			sql += " where (status = 0 or status = 1 or status = 2 or status = 3) and status != 10 ";
			
			for(int i=0;conditions!=null&&conditions.size()>0&&i<conditions.size();i++) {
				String condition = (String) conditions.get(i);
				if(StringUtils.isNotEmpty(condition)) {
					sql += " and " + condition + " ";
				}
			}

			String orderBy = " order by dsporder asc,lastname asc,id asc ";
			String descOrderBy = " order by dsporder desc,lastname desc,id desc ";
			if(hrmorder == 1) {
				orderBy = " order by pinyinlastname asc,dsporder asc,id asc ";
				descOrderBy = " order by pinyinlastname desc,dsporder desc,id desc ";
			} else if(hrmorder == 2) {
				orderBy = " order by id asc,dsporder asc,lastname asc ";
				descOrderBy = " order by id desc,dsporder desc,lastname desc ";
			}
			sql += orderBy;
			
			if(pageIndex>0&&pageSize>0) {
				if (rs.getDBType().equals("oracle")) {
					sql = " select " + sql;
					sql = "select * from ( select row_.*, rownum rownum_ from ( " + sql + " ) row_ where rownum <= " + (pageIndex * pageSize) + ") where rownum_ > " + ((pageIndex - 1) * pageSize); 					
				} else {
					if(pageIndex>1) {
						int topSize = pageSize;
						if(pageSize * pageIndex > count) {
							topSize = count - (pageSize * (pageIndex - 1));
						}
						sql = " select top " + topSize + " * from ( select top  " + topSize + " * from ( select top " + (pageIndex * pageSize) + sql + " ) tbltemp1 "+descOrderBy+") tbltemp2 "+orderBy;
					} else {
						sql = " select top " + pageSize + sql;
					}
				}
			} else {
				sql = " select " + sql;
			}

			rs.executeSql(sql);
			while (rs.next()) {
				//{"sex":"0","manager":"","status":"正式","location":"上海","lastname":"倪云","msgerurl":"","id":"508","jobtitle":"董事会主席",
				//"email":"ecologydemo@weaver.com.cn","dept":"董事会","subcom":"集团管理总部","telephone":"02150942228","mobile":"15000209807"}
				Map map = new HashMap();
				
				map.put("sex", Util.null2String(rs.getString("sex")));
				
				String manager = Util.null2String(rs.getString("managerid"));
				String managername = rci.getLastname(manager);
				map.put("manager", managername);
				
				String status = Util.null2String(rs.getString("status"));
				String statusname = "";
                if(status.equals("")) status = "8";
                if(status.equals("9")) statusname=SystemEnv.getHtmlLabelName(332,user.getLanguage());
                if(status.equals("0")) statusname=SystemEnv.getHtmlLabelName(15710,user.getLanguage());
                if(status.equals("1")) statusname=SystemEnv.getHtmlLabelName(15711,user.getLanguage());
                if(status.equals("2")) statusname=SystemEnv.getHtmlLabelName(480,user.getLanguage());
                if(status.equals("3")) statusname=SystemEnv.getHtmlLabelName(15844,user.getLanguage());
                if(status.equals("4")) statusname=SystemEnv.getHtmlLabelName(6094,user.getLanguage());
                if(status.equals("5")) statusname=SystemEnv.getHtmlLabelName(6091,user.getLanguage());
                if(status.equals("6")) statusname=SystemEnv.getHtmlLabelName(6092,user.getLanguage());
                if(status.equals("7")) statusname=SystemEnv.getHtmlLabelName(2245,user.getLanguage());
                if(status.equals("8")) statusname=SystemEnv.getHtmlLabelName(1831,user.getLanguage());
                map.put("status", statusname);

				String location = Util.null2String(rs.getString("locationid"));
				String locationname = lci.getLocationname(location);
				map.put("location", locationname);
				
				map.put("lastname", Util.null2String(rs.getString("lastname")));
				
				map.put("msgerurl", Util.null2String(rs.getString("messagerurl")));
				
				map.put("id", Util.null2String(rs.getString("id")));
				
				String jobtitle = Util.null2String(rs.getString("jobtitle"));
				String jobtitlename = jci.getJobTitlesname(jobtitle);
				map.put("jobtitle", jobtitlename);
				
				map.put("email", Util.null2String(rs.getString("email")));
				
				String dept = Util.null2String(rs.getString("departmentid"));
				String deptname = dci.getDepartmentname(dept);
				map.put("dept", deptname);

				String subcom = Util.null2String(rs.getString("subcompanyid1"));
				String subcomname = sci.getSubCompanyname(subcom);
				map.put("subcom", subcomname);
				
				map.put("telephone", Util.null2String(rs.getString("telephone")));
				
				map.put("mobile", Util.null2String(rs.getString("mobile")));
				
				list.add(map);
				
			}
			
			result.put("result", "list");
			
			result.put("pagesize",pageSize+"");
			result.put("pageindex",pageIndex+"");
			result.put("count",count+"");
			result.put("pagecount",pageCount+"");
			result.put("ishavepre",isHavePre+"");
			result.put("ishavenext",isHaveNext+"");

			result.put("list",list);
			
		}
		return result;
	}
	
	private String getStatusName(String status, User user) {
		String statusname = "";
		if(status.equals("")) status = "8";
        if(status.equals("9")) statusname=SystemEnv.getHtmlLabelName(332,user.getLanguage());
        if(status.equals("0")) statusname=SystemEnv.getHtmlLabelName(15710,user.getLanguage());
        if(status.equals("1")) statusname=SystemEnv.getHtmlLabelName(15711,user.getLanguage());
        if(status.equals("2")) statusname=SystemEnv.getHtmlLabelName(480,user.getLanguage());
        if(status.equals("3")) statusname=SystemEnv.getHtmlLabelName(15844,user.getLanguage());
        if(status.equals("4")) statusname=SystemEnv.getHtmlLabelName(6094,user.getLanguage());
        if(status.equals("5")) statusname=SystemEnv.getHtmlLabelName(6091,user.getLanguage());
        if(status.equals("6")) statusname=SystemEnv.getHtmlLabelName(6092,user.getLanguage());
        if(status.equals("7")) statusname=SystemEnv.getHtmlLabelName(2245,user.getLanguage());
        if(status.equals("8")) statusname=SystemEnv.getHtmlLabelName(1831,user.getLanguage());
        
        return statusname;
	}

	public Map getUserCount(List conditions, User user) throws Exception {
		Map result = new HashMap();
		int count = 0;
		if (user != null) {
			RecordSet rs = new RecordSet();
			
			String sql = " from hrmresource ";
			sql += " where (status = 0 or status = 1 or status = 2 or status = 3) and status != 10 ";

			for(int i=0;conditions!=null&&conditions.size()>0&&i<conditions.size();i++) {
				String condition = (String) conditions.get(i);
				if(StringUtils.isNotEmpty(condition)) {
					sql += " and " + condition + " ";
				}
			}

			sql = " select count(*) as c " + sql;

			rs.executeSql(sql);
			
			if(rs.next())
			count = rs.getInt("c");
			
			
			result.put("result", "count");
			
			result.put("count",count+"");
			result.put("unread",count+"");
			
		}
		return result;
	}
	private void getAllUser(Map result,List allUser,RecordSet rs,User user){
		try {
			ResourceComInfo rci = new ResourceComInfo();
			JobTitlesComInfo jci = new JobTitlesComInfo();
			DepartmentComInfo dci = new DepartmentComInfo();
			SubCompanyComInfo sci = new SubCompanyComInfo();
			LocationComInfo lci = new LocationComInfo();

			// loginid、account字段整合 qc:128484
			// String loginid =
			// "ldap".equals(Prop.getPropValue(GCONST.getConfigFile() ,
			// "authentic")) ? "account" : "loginid";
			String loginid = "loginid";

			String sql = "select id,lastname,pinyinlastname,messagerurl,subcompanyid1,departmentid,mobile,mobilecall,telephone,email,jobtitle,managerid,status,"
					+ loginid
					+ " as login_id,dsporder,locationid from hrmresource where (status = 0 or status = 1 or status = 2 or status = 3) and status != 10 ";
			AppDetachComInfo adci = new AppDetachComInfo();
			String sqlwhere = adci.getScopeSqlByHrmResourceSearch(user
					.getUID()
					+ "");
			if (!("".equals(sqlwhere))) {
				sql = "select id,lastname,pinyinlastname,messagerurl,subcompanyid1,departmentid,mobile,mobilecall,telephone,email,jobtitle,managerid,status,"
						+ loginid
						+ " as login_id,dsporder,locationid from hrmresource where (status = 0 or status = 1 or status = 2 or status = 3) and status != 10 and "
						+ sqlwhere;
			}
			sql = sql
					+ "  and subcompanyid1 is not null and subcompanyid1 !=0 and departmentid is not null and departmentid !=0  order by id";

			rs.executeSql(sql);
			//HrmResourceMobileTools ht = new HrmResourceMobileTools();
			//List<String> showUserIds = ht.getMobileShowRight(user.getUID());
			while (rs.next()) {
				Map userMap = new HashMap();
				String uid = rs.getString("id");
				userMap.put("ID", uid);
				userMap.put("Name", rs.getString("lastname"));
				userMap.put("PYName", rs.getString("pinyinlastname"));
				userMap.put("HeaderURL", rs.getString("messagerurl"));

				String subcom = rs.getString("subcompanyid1");
				userMap.put("SubCompanyID", subcom);
				userMap
						.put("SubCompanyName", sci
								.getSubCompanyname(subcom));

				String dept = rs.getString("departmentid");
				userMap.put("DepartmentID", dept);
				userMap.put("DepartmentName", dci.getDepartmentname(dept));

				// boolean show =
				// ht.getMobileShowRight(rs.getInt("id"),user.getUID());
				String mobile = rs.getString("mobile");
				//if (!showUserIds.contains(uid)) {
				//	if (mobile.length() > 4) {
				//		mobile = mobile.substring(0, mobile.length() - 4)
				//				+ "****";
				//	}
				//}
				userMap.put("mobile", mobile);
				userMap.put("tel", rs.getString("telephone"));
				userMap.put("email", rs.getString("email"));
				userMap.put("mobilecall", rs.getString("mobilecall"));

				String jobtitle = rs.getString("jobtitle");
				userMap.put("title", jci.getJobTitlesname(jobtitle));

				String manager = rs.getString("managerid");
				userMap.put("managerID", manager);
				userMap.put("managerName", rci.getLastname(manager));

				userMap.put("statusName", this.getStatusName(rs
						.getString("status"), user));

				userMap.put("loginid", rs.getString("login_id"));

				userMap.put("showorder", rs.getString("dsporder"));

				String locationid = rs.getString("locationid");
				userMap.put("locationID", locationid);
				userMap
						.put("locationName", lci
								.getLocationname(locationid));

				allUser.add(userMap);
			}

			result.put("timestamp", getTableTimestamp("HrmResource"));
			result.put("data", allUser);

		} catch (Exception e) {
			writeLog(e);
			result.put("error", e.getMessage());
		}
	}

	public Map getAllUser(User user) {
		Map result = new HashMap();
		List allUser = new ArrayList();
		RecordSet rs = new RecordSet();
		AppDetachComInfo info = new AppDetachComInfo();
		//判断是否开启人力资源分权
		if (info.isUseAppDetach()) {
			getAllUser(result, allUser, rs, user);
		} else {
			Map<String, Object> ALL_USER_ = HrmResourceService.ALL_USER;
			allUser = (List) ALL_USER_.get("users");
			if (allUser == null || allUser.size() == 0) {
				allUser = new ArrayList();
				getAllUser(result, allUser, rs, user);
				ALL_USER_.put("users", allUser);
				ALL_USER_.put("synTime", System.currentTimeMillis());
				ALL_USER_.put("timestamp", result.get("timestamp"));
			} else {
				Long now = System.currentTimeMillis();
				Long synTime = (Long) ALL_USER_.get("synTime");
				if (synTime == null || synTime == 0L) {
					allUser = new ArrayList();
					getAllUser(result, allUser, rs, user);
					ALL_USER_.put("users", allUser);
					ALL_USER_.put("synTime", System.currentTimeMillis());
					ALL_USER_.put("timestamp", result.get("timestamp"));
				} else {
					//如果当前时间与缓存相差5分钟以内读取缓存数据不查询数据库
					if (now - synTime < 5L * 60L * 1000L) {
						List allUserNew = new ArrayList();
						HrmResourceMobileTools ht = new HrmResourceMobileTools();
						List<String> showUserIds = ht.getMobileShowRight(user.getUID());
						for (int i = 0; i < allUser.size(); i++) {
							Map userMap =(Map) allUser.get(i);
							Map userMapNew = getNewUser(userMap,new HashMap());
							String uid = (String) userMapNew.get("ID");
							if (!showUserIds.contains(uid)) {
								String mobile = (String) userMapNew.get("mobile");
								if (mobile.length() > 4) {
									mobile = mobile.substring(0, mobile.length() - 4)+ "****";
									userMapNew.put("mobile", mobile);
								}
							}
							allUserNew.add(userMapNew);
						}
						result.put("data", allUserNew);
						result.put("timestamp",ALL_USER_.get("timestamp"));
					} else {
						allUser = new ArrayList();
						getAllUser(result, allUser, rs, user);
						ALL_USER_.put("users", allUser);
						ALL_USER_.put("synTime", System.currentTimeMillis());
						ALL_USER_.put("timestamp", result.get("timestamp"));
					}
				}
			}

		}

		return result;
	}

	public Map getNewUser(Map userMapOld,Map userMapNew){
		userMapNew.put("ID", userMapOld.get("ID"));
		userMapNew.put("Name", userMapOld.get("Name"));
		userMapNew.put("PYName", userMapOld.get("PYName"));
		userMapNew.put("HeaderURL", userMapOld.get("HeaderURL"));
		userMapNew.put("SubCompanyID", userMapOld.get("SubCompanyID"));
		userMapNew.put("SubCompanyName", userMapOld.get("SubCompanyName"));
		userMapNew.put("DepartmentID", userMapOld.get("DepartmentID"));
		userMapNew.put("DepartmentName", userMapOld.get("DepartmentName"));
		userMapNew.put("mobile", userMapOld.get("mobile"));
		userMapNew.put("tel", userMapOld.get("tel"));
		userMapNew.put("email", userMapOld.get("email"));
		userMapNew.put("mobilecall", userMapOld.get("mobilecall"));
		userMapNew.put("title", userMapOld.get("title"));
		userMapNew.put("managerID", userMapOld.get("managerID"));
		userMapNew.put("managerName", userMapOld.get("managerName"));
		userMapNew.put("statusName", userMapOld.get("statusName"));
		userMapNew.put("loginid", userMapOld.get("loginid"));
		userMapNew.put("showorder", userMapOld.get("showorder"));
		userMapNew.put("locationID", userMapOld.get("locationID"));
		userMapNew.put("locationName", userMapOld.get("locationName"));
		return userMapNew;
	}

	public Map getAllDepartment(User user) {
		Map result = new HashMap();
		List allDept = new ArrayList();
		RecordSet rs = new RecordSet();
		
		try {
			DepartmentComInfo dci = new DepartmentComInfo();
			SubCompanyComInfo sci = new SubCompanyComInfo();
			
			String sql = "select * from HrmDepartment where canceled is null or canceled<>1 order by id";
			rs.executeSql(sql);
			AppDetachComInfo adci= new AppDetachComInfo();
			while(rs.next()) {
				String ids = rs.getString("id"); //部门
				if(adci.checkUserAppDetach(ids,"3",user)==0) continue;
				Map deptMap = new HashMap();
				deptMap.put("ID", rs.getString("id"));
				deptMap.put("Name", rs.getString("departmentname"));
				
				int supdepid = rs.getInt("supdepid");
				if(supdepid > 0) {
					int supdepididx = dci.getIdIndexKey(""+supdepid);
					if(supdepididx == -1) supdepid = 0;
				}else{
					supdepid = 0;
				}
				deptMap.put("supDepartmentID", ""+supdepid);
				deptMap.put("supDepartmentName", dci.getDepartmentname(""+supdepid));
				
				String subcompanyid = rs.getString("subcompanyid1");
				deptMap.put("SubCompanyID", subcompanyid);
				deptMap.put("SubCompanyName", sci.getSubCompanyname(subcompanyid));
				deptMap.put("showorder", rs.getString("showorder"));
				
				allDept.add(deptMap);
			}
			
			result.put("timestamp", getTableTimestamp("HrmDepartment"));
			result.put("data", allDept);
			
		} catch (Exception e) {
			writeLog(e);
			result.put("error", e.getMessage());
		}
		
		return result;
	}
	
	public Map getAllSubCompany(User user) {
		Map result = new HashMap();
		List allSubcom = new ArrayList();
		RecordSet rs = new RecordSet();
		
		try {
			SubCompanyComInfo sci = new SubCompanyComInfo();
			
			String sql = "select * from HrmSubCompany where canceled is null or canceled<>1 order by id";
			rs.executeSql(sql);
			AppDetachComInfo adci= new AppDetachComInfo();
			while(rs.next()) {
				String ids = rs.getString("id"); //分部
				if(adci.checkUserAppDetach(ids,"2",user)==0) continue;
				Map subcomMap = new HashMap();
				subcomMap.put("ID", rs.getString("id"));
				subcomMap.put("Name", rs.getString("subcompanyname"));
				
				int supsubcomid = rs.getInt("supsubcomid");
				if(supsubcomid<=0)
					supsubcomid = 0;
				subcomMap.put("supSubCompanyID", supsubcomid);
				subcomMap.put("supSubCompanyName", sci.getSubCompanyname(""+supsubcomid));
				
				subcomMap.put("companyID", rs.getString("companyid"));
				subcomMap.put("showorder", rs.getString("showorder"));
				
				allSubcom.add(subcomMap);
			}
			
			result.put("timestamp", getTableTimestamp("HrmSubCompany"));
			result.put("data", allSubcom);
			
		} catch (Exception e) {
			writeLog(e);
			result.put("error", e.getMessage());
		}
		
		return result;
	}
	
	public Map getAllCompany(User user) {
		Map result = new HashMap();
		List allCompany = new ArrayList();
		RecordSet rs = new RecordSet();
		
		try {
			String sql = "select * from HrmCompany order by id";
			rs.executeSql(sql);
			
			while(rs.next()) {
				Map comMap = new HashMap();
				comMap.put("ID", rs.getString("id"));
				comMap.put("Name", rs.getString("companyname"));
				
				allCompany.add(comMap);
			}
			
			result.put("timestamp", getTableTimestamp("HrmCompany"));
			result.put("data", allCompany);
			
		} catch (Exception e) {
			writeLog(e);
			result.put("error", e.getMessage());
		}
		
		return result;
	}
	
	public Map getUserGroups(User user) {
		Map result = new HashMap();
		
		List allGroup = new ArrayList();
		RecordSet rs = new RecordSet();
		
		try {
			GroupAction groupAct = new GroupAction();
			
			List groups = groupAct.getGroupsByUser(user);
			for(Object group : groups) {
				Map groupMap = (HashMap)group;
				
				Map gMap = new HashMap();
				gMap.put("ID", groupMap.get("id"));
				gMap.put("Name", groupMap.get("name"));
				gMap.put("groupType", groupMap.get("type"));
				gMap.put("UserID", ""+user.getUID());
				
				List<String> userList = new ArrayList<String>();
				rs.executeSql("select * from HrmGroupMembers where groupid="+groupMap.get("id"));
				while(rs.next()) {
					userList.add(rs.getString("userid"));
				}
				gMap.put("userList", userList);
				
				allGroup.add(gMap);
			}
			
			result.put("timestamp", getTableTimestamp("HrmGroup"));
			result.put("data", allGroup);
			
		} catch (Exception e) {
			writeLog(e);
			result.put("error", e.getMessage());
		}
		
		return result;
	}
	
	private static Map HrmGroupMember_Cache = null;
	
	public Map getGroupMember(User user) {
		Map result = new HashMap();
		
		try {
			String cur_timestamp = Util.null2String(getTableTimestamp("HrmGroupMember"));
			if(HrmGroupMember_Cache == null || !cur_timestamp.equals((String)HrmGroupMember_Cache.get("timestamp"))) {
				HrmGroupMember_Cache = new HashMap();
				
				List members = new ArrayList();
				RecordSet rs = new RecordSet();
				String sql = "select * from HrmGroupMembers order by groupid";
				rs.executeSql(sql);
				
				while(rs.next()) {
					Map memberMap = new HashMap();
					memberMap.put("GroupID", rs.getString("groupid"));
					memberMap.put("UserID", rs.getString("userid"));
					
					members.add(memberMap);
				}
				
				HrmGroupMember_Cache.put("timestamp", cur_timestamp);
				HrmGroupMember_Cache.put("data", members);
			}
			
			result = HrmGroupMember_Cache;
		} catch (Exception e) {
			writeLog(e);
			result.put("error", e.getMessage());
		}
		
		return result;
	}
	
	private String getTableTimestamp(String tableName) {
		List Timestamps = new ArrayList();
		
		try {
			RecordSet rs = new RecordSet();
			
			String sql = "select syncLastTime from mobileSyncInfo where syncTable='"+tableName+"'";
			rs.executeSql(sql);
			if(rs.next()) {
				return rs.getString("syncLastTime");
			}
			
		} catch (Exception e) {
			writeLog(e);
		}
		
		return "0";
	}
	
	public Map getTableStatus(String[] tablename, String[] timestamp) {
		Map result = new HashMap();
		
		if(tablename == null || tablename.length == 0) {
			result.put("error", "Illegal Argument!");
			return result;
		}
		
		RecordSet rs = new RecordSet();
		List tblStatus = new ArrayList();
		
		try {
			for(int i=0; i<tablename.length; i++) {
				String tbname = tablename[i];
				long tstamp = (timestamp != null && i < timestamp.length) ? NumberUtils.toLong(timestamp[i], 0) : 0;
				
				if("HrmGroup".equalsIgnoreCase(tbname)) {
					Map tbl = new HashMap();
					tbl.put("tablename", tbname);
					tbl.put("hasSync", "1");
					tblStatus.add(tbl);
				} else {
					String sql = "select syncLastTime from mobileSyncInfo where syncTable='"+tbname+"'";
					rs.executeSql(sql);
					
					if(rs.next()) {
						Map tbl = new HashMap();
						tbl.put("tablename", tbname);
						tbl.put("hasSync", "1");
						
						long syncLastTime = NumberUtils.toLong(rs.getString("syncLastTime"));
						if(syncLastTime > 0 && syncLastTime == tstamp) {
							tbl.put("hasSync", "0");
						}
						
						tblStatus.add(tbl);
					}
				}
			}
			
			result.put("data", tblStatus);
		} catch (Exception e) {
			writeLog(e);
			result.put("error", e.getMessage());
		}
		
		return result;
	}
	
	public Map<String, Object> getWorkPlanType(User user) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, String>> workPlanTypeForNewList = new ArrayList<Map<String, String>>();
		RecordSet recordSet = new RecordSet();
		
		try {
			recordSet.executeSql("select * from OverWorkPlan where wavailable = '1' order by id");
			while(recordSet.next()){
				Map<String, String> item=new HashMap<String, String>();
				item.put("id","-"+recordSet.getString("id"));
				item.put("name",recordSet.getString("workPlanName"));
				item.put("color",recordSet.getString("workPlanColor"));
				workPlanTypeForNewList.add(item);
			}
			recordSet.executeSql("SELECT * FROM WorkPlanType WHERE available = '1' ORDER BY displayOrder ASC");
			while(recordSet.next()){
				Map<String, String> item=new HashMap<String, String>();
				item.put("id",recordSet.getString("workPlanTypeID"));
				item.put("name",recordSet.getString("workPlanTypeName"));
				item.put("color",recordSet.getString("workPlanTypeColor"));
				workPlanTypeForNewList.add(item);
			}
			
			result.put("timestamp", getTableTimestamp("WorkPlanType"));
			result.put("data", workPlanTypeForNewList);
		}catch (Exception e) {
			writeLog(e);
			result.put("error", e.getMessage());
		}
		
		return result;
	}
	
	public Map<String, Object> getWorkFlowType(User user) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		try {
			List<Map<String, String>> workFlowTypeList = new ArrayList<Map<String, String>>();
			RecordSet recordSet = new RecordSet();
			WorkTypeComInfo workTypeComInfo = new WorkTypeComInfo();
			RequestUtil ru = new RequestUtil();
			int workflowtype = 0;
			
			recordSet.executeSql("select id,workflowname,workflowtype from workflow_base where isvalid in('1','3') and (isbill=0 or (isbill=1 and formid<0) or (isbill=1 and formid in ("+formids+"))) order by workflowtype,id");
			while(recordSet.next()){
				String curworkflowtype = recordSet.getString("workflowtype");
				int wfType = 1000000+Util.getIntValue(curworkflowtype);
				if(workflowtype != wfType) {
					Map<String, String> item=new HashMap<String, String>();
					item.put("id", ""+wfType);
					item.put("name", workTypeComInfo.getWorkTypename(curworkflowtype));
					item.put("parent", "0");
					item.put("isParent", "1");
					item.put("dsporder", workTypeComInfo.getWorkDsporder(curworkflowtype));
					workFlowTypeList.add(item);
					
					workflowtype = wfType;
				}
				
				Map<String, String> item=new HashMap<String, String>();
				item.put("id", recordSet.getString("id"));
				item.put("name", recordSet.getString("workflowname"));
				item.put("parent", ""+wfType);
				item.put("isParent", "0");
				item.put("dsporder", "0");
				workFlowTypeList.add(item);
			}
			
			//------------------------------------------
			if(ru.getOfsSetting().getIsuse()==1){//查询统一待办的数据
				RecordSet recordSet1 = new RecordSet();
				recordSet.executeSql("select sysid,SysShortName,SysFullName from ofs_sysinfo where cancel=0 order by sysid desc");
				while(recordSet.next()){
					String curworkflowtype = recordSet.getString("sysid");
					String typename = "";
					if(ru.getOfsSetting().getShowsysname().equals("2")){
						typename = Util.null2String(recordSet.getString("SysFullName"));
					}else{
						typename = Util.null2String(recordSet.getString("SysShortName"));
					}
					int wfType = Util.getIntValue(curworkflowtype);
					Map<String, String> item=new HashMap<String, String>();
					item.put("id", ""+wfType);
					item.put("name", typename);
					item.put("parent", "0");
					item.put("isParent", "1");
					item.put("dsporder", (1000-wfType)+"");
					workFlowTypeList.add(item);
					
					recordSet1.executeSql("select workflowid,workflowname from ofs_workflow where sysid="+curworkflowtype+" and Cancel=0");
					while(recordSet1.next()){
						Map<String, String> item1=new HashMap<String, String>();
						item1.put("id", recordSet1.getString("workflowid"));
						item1.put("name", recordSet1.getString("workflowname"));
						item1.put("parent", ""+wfType);
						item1.put("isParent", "0");
						item1.put("dsporder", "0");
						workFlowTypeList.add(item1);
					}
				}
			}
			//------------------------------------------
			
			result.put("timestamp", getTableTimestamp("WorkFlowType"));
			result.put("data", workFlowTypeList);
		}catch (Exception e) {
			writeLog(e);
			result.put("error", e.getMessage());
		}
		
		return result;
	}
	
	public Map<String, Object> getBlackWorkFlow(User user) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		try {
			List<String> wflist = new ArrayList<String>();
			RecordSet rs = new RecordSet();
			
			rs.executeSql("select workflowid from workflowBlacklist where userid="+user.getUID());
			while(rs.next()) {
				wflist.add(rs.getString("workflowid"));
			}
			
			result.put("data", wflist);
		} catch (Exception e) {
			writeLog(e);
			result.put("error", e.getMessage());
		}
		
		return result;
	}
	
	public Map<String, Object> setBlackWorkFlow(User user, String workflows) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		try {
			RecordSet recordSet = new RecordSet();
			
			recordSet.executeSql("delete from workflowBlacklist where userid="+user.getUID());
			
			String[] selwf = StringUtils.splitByWholeSeparator(workflows, ",");
			if(selwf != null && selwf.length > 0) {
				List<String> paraList = new ArrayList<String>();
				
				for(String wf : selwf) {
					paraList.add("" + user.getUID() + Util.getSeparator() + wf);
				}
				
				BatchRecordSet brs = new BatchRecordSet();
				brs.executeSqlBatch("insert into workflowBlacklist(userid,workflowid) values(?,?)", paraList);
			}
			
			result.put("result", "1");
		} catch (Exception e) {
			writeLog(e);
			result.put("error", e.getMessage());
		}
		
		return result;
	}
	
	public Map<String, Object> getHideModule(User user) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		try {
			List<String> wflist = new ArrayList<String>();
			RecordSet rs = new RecordSet();
			
			rs.executeSql("select moduleid from mobileHideModule where userid="+user.getUID());
			while(rs.next()) {
				wflist.add(rs.getString("moduleid"));
			}
			
			result.put("data", wflist);
		} catch (Exception e) {
			writeLog(e);
			result.put("error", e.getMessage());
		}
		return result;
	}
	
	public Map<String, Object> setHideModule(User user, String hidemodule) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		try {
			RecordSet recordSet = new RecordSet();
			
			recordSet.executeSql("delete from mobileHideModule where userid="+user.getUID());
			
			String[] modules = StringUtils.splitByWholeSeparator(hidemodule, ",");
			if(modules != null && modules.length > 0) {
				for(String module : modules) {
					recordSet.executeSql("insert into workflowBlacklist(userid, moduleid) values("+user.getUID()+", "+module+")");
				}
			}
			
			result.put("result", "1");
		} catch (Exception e) {
			writeLog(e);
			result.put("error", e.getMessage());
		}
		
		return result;
	}
	
	public Map<String, Object> getHrmSubCompanyTree(User user) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		
		try {
			CompanyComInfo cci = new CompanyComInfo();
			SubCompanyComInfo scci = new SubCompanyComInfo();
			
			Map<String, String> root = new HashMap<String, String>();
			String companyname = cci.getCompanyname("1");
			root.put("id", "0");
			root.put("pId", "-1");
			root.put("name", companyname);
			root.put("open", "true");
			data.add(root);
			
			while(scci.next()) {
				if("1".equals(scci.getCompanyiscanceled()))continue;
				
				String supsubcomid = scci.getSupsubcomid();
	            if("".equals(supsubcomid)) supsubcomid = "0";
	            
				Map<String, String> node = new HashMap<String, String>();
				node.put("id", scci.getSubCompanyid());
				node.put("pId", supsubcomid);
				node.put("name", scci.getSubCompanyname());
				data.add(node);
			}
		} catch (Exception e) {
			writeLog(e);
		}
		
		result.put("data", data);
		
		return result;
	}
	
	public boolean ifEqlTarget(String val, String target) {
    	if(val == null || val.equals("")) {
    		return false;
    	}
    	if(!val.equals(target)) {
    		return false;
    	}
    	return true;
	}

}
