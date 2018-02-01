package weaver.mobile.plugin.ecology.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.json.JSONObject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weaver.conn.ConnStatement;
import weaver.conn.RecordSet;
import weaver.file.Prop;
import weaver.general.GCONST;
import weaver.general.MD5;
import weaver.general.StaticObj;
import weaver.general.Util;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.company.SubCompanyComInfo;
import weaver.hrm.resource.ResourceComInfo;
import weaver.hrm.settings.ChgPasswdReminder;
import weaver.hrm.settings.HrmSettingsComInfo;
import weaver.hrm.settings.RemindSettings;
import weaver.proj.util.CodeUtil;
import weaver.systeminfo.SysMaintenanceLog;
import weaver.systeminfo.setting.HrmUserSettingComInfo;

public class AuthService {
	static {
		Protocol.registerProtocol("https", new Protocol("https", new EasySSLProtocolSocketFactory(), 443));
	}
	
	private static Log logger = LogFactory.getLog(AuthService.class);
	
	HrmResourceService hrs = new HrmResourceService();
	
	private final String mobileCacheKey = "mobile4.0";
	
	public Map login(String loginId,String password,String secrect,String loginTokenFromThird,String dynapass,String tokenpass,String language,String ipaddress,int policy,List auths) {
		
		Map result = new HashMap();

		if (loginId != null && !"".equals(loginId) && password != null && !"".equals(password) && ipaddress != null && !"".equals(ipaddress)) {
			int status = 4;
			if(loginTokenFromThird != null && !"".equals(loginTokenFromThird)){
				
				String tokenfromClient =  CodeUtil.hexSHA1(secrect+loginId+password);
				
				if(tokenfromClient.equals(loginTokenFromThird)){
					try {
						ConnStatement statement  = new ConnStatement();

						String sql = "select id,password from HrmResource where loginid= ? union select id,password from HrmResourcemanager where loginid= ? ";
						
						statement.setStatementSql(sql);
						statement.setString(1, loginId);
						statement.setString(2, loginId);
						statement.executeQuery();
				        if(statement.next() && Util.getIntValue(statement.getString(1), 0) > 0){
				        	status = 1;
				        }	
					} catch(Exception e){
						e.printStackTrace();
					}
				}else{
					result.put("message", "对不起，您所提供的token不正确！");
					return result;
				}
			}else{
				status=hrs.checkLogin(loginId, password, dynapass, tokenpass, policy);
			}
			/*
			 * 0:需要输入动态密码
			 * 1:登录成功返回用户id
			 * 2:登录密码不匹配
			 * 3:用户id为空
			 * 4:用户不存在
			 * 5:出现异常
			 * 6:用户动态密码不匹配
			 * 7:动态密码短信发送失败
			 * 8:需要输入密码令牌
			 * 9:密码令牌不正确
			 * 
			 * 10:没有权限
			 * 11:用户名,密码,ip为空
			 * 
			 * 18:次账号不允许登录
			 * 19:密码被锁定
			 * 20:密码已过期
			 * 
			 */
			return loginByTypes(loginId, password, language, ipaddress, auths, result,
					status);
		} else {
			result.put("message", "11");
		}
		return result;
	}

	public Map loginByTypes(String loginId, String password, String language,
			String ipaddress, List auths, Map result, int status) {
		if (status == 1) {
			int userid = hrs.getUserId(loginId);
			//检查用户权限
		    List userGroupidList=checkMobileUserRight(""+userid, auths); //返回用户具有权限访问的用户组id
		    HrmUserSettingComInfo userSetting;
			try {
				userSetting = new HrmUserSettingComInfo();
				 String belongtoshow = userSetting.getBelongtoshowByUserId(userid+"");
				 if("1".equals(belongtoshow)){
					 List<String> relatives = hrs.getRelativeUser(userid);
					 for(String relid:relatives){
						 List userGroupidListtemp=checkMobileUserRight(relid, auths); 
						 userGroupidList.addAll(userGroupidListtemp);
					 }
				 }
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		   
		    if(userGroupidList.size()==0) {
		    	result.put("message", "10"); //用户没有权限
		    } else {
		    	//检查密码是否过期
		    	
				RecordSet rs = new RecordSet();

				String openPasswordLock="";
				HrmSettingsComInfo settings = new HrmSettingsComInfo();
				openPasswordLock = settings.getOpenPasswordLock();

				boolean isLocked = false;
				boolean isExpired = false;
				if("1".equals(openPasswordLock)){
					rs.executeSql("select passwordlock from hrmresource where passwordlock>0 and id="+userid);
					if(rs.next()){
				    	result.put("message", "19");
				    	isLocked = true;
					}
				}
				ChgPasswdReminder reminder=new ChgPasswdReminder();
				RemindSettings rsettings=reminder.getRemindSettings();
				
				String PasswordChangeReminderstr = Util.null2String(rsettings.getPasswordChangeReminder());
				boolean PasswordChangeReminder = false;
				if(!"sysadmin".equals(loginId)&&"1".equals(PasswordChangeReminderstr)){
					PasswordChangeReminder = true;
				}
				int passwdReminder = 0;
				if(PasswordChangeReminder){
					passwdReminder = 1;
				}
				String ChangePasswordDays = rsettings.getChangePasswordDays();
				String DaysToRemind = rsettings.getDaysToRemind();
				String passwdchgdate = "";
				int passwdchgeddate = 0;
				int passwdreminddatenum = 0;
				int passwdelse = 0;
				String passwdreminddate = "";
				String canpass = "0";
				String canremind = "0";
				if(PasswordChangeReminder){
					rs.executeSql("select passwdchgdate from hrmresource where id = "+userid);
					if(rs.next()){
						passwdchgdate = rs.getString(1);
						passwdchgeddate = weaver.general.TimeUtil.dateInterval(passwdchgdate,weaver.general.TimeUtil.getCurrentDateString());

						if(passwdchgeddate<Integer.parseInt(ChangePasswordDays)){
							canpass = "1";
						}
						passwdreminddate = weaver.general.TimeUtil.dateAdd(passwdchgdate,Integer.parseInt(ChangePasswordDays)-Integer.parseInt(DaysToRemind));
						try {
							passwdreminddatenum = weaver.general.TimeUtil.dateInterval(passwdreminddate,weaver.general.TimeUtil.getCurrentDateString());
						} catch(Exception ex) {
							passwdreminddatenum = 0;
						}
						passwdelse = Integer.parseInt(DaysToRemind) - passwdreminddatenum;
						if(passwdreminddatenum>=0){
							canremind = "1";
						}
					}else{
//							isExpired = false;
					}
				}else{
//						isExpired = false;
				}
				
				if(passwdReminder==1&&!canpass.equals("1")) {
			    	result.put("message", "20");
					isExpired = true;
				}
				
				
		    	if(!isLocked&&!isExpired) {

		            //次账号不允许登陆，只能通过主账号登陆后切换至次账号
					String sql = "select accounttype,belongto from hrmresource where id=" + userid;
					rs.execute(sql);
					//2017-08-22 tangjianyong
					//if(rs.next()&&"1".equals(Util.null2String(rs.getString("accounttype")))&&Util.getIntValue(rs.getString("belongto"), -1)>0) {
						//result.put("message", "18");
					//} else {
					
				    	User user = hrs.getUserById(userid);
				    	result.put("groups", userGroupidList);//将用户组放入结果集中
				    	if (user != null) {
				    		
							user.setLanguage(getLanguageCode(language));
							
							user.setLoginip(ipaddress);
							UUID uuid = UUID.randomUUID();
							String sessionKey= uuid.toString();
							putSessionId(sessionKey, user);
							
							result.put("sessionkey", sessionKey);
							
							try {
					    		Calendar today = Calendar.getInstance();
						    	String currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
						    	
						    	char separater = Util.getSeparator();
					            rs.execute("HrmResource_UpdateLoginDate", "" + user.getUID() + separater + currentdate);

					            SysMaintenanceLog log = new SysMaintenanceLog();
					            log.resetParameter();
					            log.setRelatedId(user.getUID());
					            log.setRelatedName((user.getFirstname() + " " + user.getLastname()).trim());
					            log.setOperateType("6");
					            log.setOperateDesc("");
					            log.setOperateItem("60");
					            log.setOperateUserid(user.getUID());
					            log.setClientAddress("mobile");
					            log.setSysLogInfo();
					    	} catch (Exception e) {
							}
					    	
					    	//微信模块认证用
					    	if("1".equals(Prop.getPropValue("Messager2","IsUseEMessager"))) {
					    		String psw2=Util.getEncrypt(password).toLowerCase();
					    		String lowerloginid = loginId.toLowerCase();
					    		String strSql="select count(0) from HrmMessagerAccount where userid='"+lowerloginid+"'";
					    		rs.executeSql(strSql);
					    		if(rs.next()){
					    			if(rs.getInt(1)>0){
					    				strSql="update HrmMessagerAccount set psw='"+psw2+"' where userid='"+lowerloginid+"'";
					    			} else {
					    				strSql="insert into HrmMessagerAccount(userid,psw) values('"+lowerloginid+"','"+psw2+"')";
					    			}
					    		}
					    		rs.executeSql(strSql);
					    	}
						}
				    	result.put("message", "1"); //登陆成功
					//}
		    	}
		    }
		} else {
			
			result.put("message", status+"");
			
		}
		return result;
	}
	
	public Map login(String loginId,String password,String dynapass,String tokenpass,String language,String ipaddress,int policy,List auths) {
		
		Map result = new HashMap();

		if (loginId != null && !"".equals(loginId) && password != null && !"".equals(password) && ipaddress != null && !"".equals(ipaddress)) {
			int status =hrs.checkLogin(loginId, password, dynapass, tokenpass, policy);
			return loginByTypes(loginId, password, language, ipaddress, auths, result,
					status);
		} else {
			result.put("message", "11");
		}
		return result;
	}
	/**
	 * 后台管理员登陆
	 * @param loginId   登陆id默认为loginid
	 * @param password  登陆密码
	 * @param ipaddress 登陆ip地址
	 * @return
	 */
	public Map adminLogin(String loginId,String password,String ipaddress) {
		
		Map result = new HashMap();

		if (loginId != null && !"".equals(loginId) && password != null && !"".equals(password) && ipaddress != null && !"".equals(ipaddress)) {
			
			int status=hrs.checkLogin(loginId, password, "", "", 0); 
			/*
			 * 1:登录成功返回用户id
			 * 2:登录密码不匹配
			 * 3:用户不存在
			 * 
			 * 10:没有权限
			 * 11:用户名,密码,ip为空
			 * 
			 */
			if (status == 1) {
				User user = new User() ;
				RecordSet rs=new RecordSet();
				String sql = "select id,systemlanguage from hrmresource where loginid='" + loginId + "' union select id,systemlanguage from HrmResourcemanager where loginid='" + loginId + "'";
				rs.execute(sql);
				if(rs.next()){
					
					user.setUid(rs.getInt("id"));
					user.setLoginid(loginId);
					user.setLanguage(Util.getIntValue(rs.getString("systemlanguage"),0));
					user.setLogintype("1");
					user.setLoginip(ipaddress);
					
					UUID uuid = UUID.randomUUID();
					String sessionKey= uuid.toString();
					putSessionId(sessionKey, user);
					
					result.put("sessionkey", sessionKey);
				}
				
				if (!HrmUserVarify.checkUserRight("Mobile:Setting", user)) {
					status=10;
				}
				
			   result.put("message", ""+status);
			} else {
			   result.put("message", status+"");
			}
		} else {
			result.put("message", "11");
		}
		return result;
	}
	
	public Map login(String id,String language,String ipaddress) {
		Map result = new HashMap();

		if (id != null && !"".equals(id) && ipaddress != null && !"".equals(ipaddress)) {
			
	    	User user = hrs.getUserById(NumberUtils.toInt(id));
	    	if (user != null) {
	    		
				user.setLanguage(getLanguageCode(language));
				
				user.setLoginip(ipaddress);
				UUID uuid = UUID.randomUUID();
				String sessionKey= uuid.toString();
				putSessionId(sessionKey, user);
				
				result.put("sessionkey", sessionKey);
		    	result.put("message", "1"); //登陆成功
			} else {
				result.put("message", "3");
			}
		} else {
			result.put("message", "11");
		}
		return result;
	}
    

	public boolean verify(String sessionKey) throws Exception {
		User user = (User) gutSessionId(sessionKey);
		if(user==null) return false;
		return true;
	}

	public boolean verify(String loginID,String password) throws Exception {
		User user = (User) gutSessionId(loginID,password);
		if(user==null) return false;
		return true;
	}

	public User getCurrUser(String sessionKey) throws Exception {
		User user = (User) gutSessionId(sessionKey);
		return user;
	}
	
	private int getLanguageCode(String language) {
		if(language.toUpperCase().indexOf("TW")>-1) {
			return 9;
		}

		if(language.toUpperCase().indexOf("HK")>-1) {
			return 9;
		}

		if(language.toUpperCase().indexOf("HANT")>-1) {
			return 9;
		}
		
		if(language.toUpperCase().indexOf("EN")>-1) {
			return 8;
		}
	
		return 7;
	}
		
	private static void putSessionId(String sessionID,User user){
		long sessionTimeOut = NumberUtils.toLong(Prop.getPropValue("EMobile4", "sessionTimeOut"));
		sessionTimeOut = (sessionTimeOut == 0) ? 10000 : sessionTimeOut;
		sessionTimeOut = sessionTimeOut*60*1000;
		if(sessionTimeOut > 0) {
			long failureTime = new Date().getTime() - sessionTimeOut;
			RecordSet rs = new RecordSet();
			String strSql  =  "delete from emobileLoginKey where logintime<"+failureTime;
			rs.executeSql(strSql);
		}
		RecordSet rs = new RecordSet();
		String strSql  =  "insert into emobileLoginKey (sessionkey,userid,logintime,syslanguage) values ('"+sessionID+"',"+user.getUID()+","+new Date().getTime()+","+user.getLanguage()+")";
		rs.executeSql(strSql);
	}
	
	private  Object gutSessionId(String sessionID){
		RecordSet rs = new RecordSet();
		String strSql  =  "select * from emobileLoginKey where sessionkey = '"+sessionID+"'";
		rs.executeSql(strSql);
		if(rs.next()){
			int userid = rs.getInt("userid");
			User user =  hrs.getUserById(userid);
			user.setLanguage(rs.getInt("syslanguage"));
			return user;
		}
		
		return null;
	}

	private Object gutSessionId(String loginID,String password){
		String sessionID = "";
		RecordSet rs = new RecordSet();
		String strSql  =  "select * from emobileLoginKey";
		rs.executeSql(strSql);
		
		while(rs.next()){
			sessionID = rs.getString("sessionkey");
			int userid = rs.getInt("userid");
			User user = hrs.getUserById(userid);
			if(user!=null){
				if(user.getLoginid().equalsIgnoreCase(loginID)) {
					break;
				}
			}
		}
		
		if(sessionID!=null&&!"".equals(sessionID)) return gutSessionId(sessionID);
		return null;
	}
	
	private List getOrgIdsList(String orgId,int orgType){
		List returnList=new ArrayList();
		
		if(orgId==null||"".equals(orgId)) return returnList;
		
		returnList.add(orgId);
		
		try {
			if(orgType==1) {
				SubCompanyComInfo scci = new SubCompanyComInfo();
				scci.setTofirstRow();
		        while (scci.next()) {
					String supid = scci.getSupsubcomid();
					if (supid.equals("")) supid = "0";
					if (!supid.equals(orgId)) continue;
				
					String id = scci.getSubCompanyid();
					returnList.add(id);
					
					List sonList = getOrgIdsList(id,orgType);
					if(sonList!=null) returnList.addAll(sonList);
				}
			} else if(orgType==2) {
				DepartmentComInfo dci = new DepartmentComInfo();
				dci.setTofirstRow();
		        while (dci.next()) {
					String supid = dci.getDepartmentsupdepid();
					if (supid.equals("")) supid = "0";
					if (!supid.equals(orgId)) continue;
				
					String id = dci.getDepartmentid();
					returnList.add(id);
					
					List sonList = getOrgIdsList(id,orgType);
					if(sonList!=null) returnList.addAll(sonList);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return returnList;
	}

	
	public void clearPluginUserCache() {
		StaticObj staticobj = StaticObj.getInstance();
		
		staticobj.removeObject(mobileCacheKey);
	}

	private String getConditionDigest(List conditionList, List<String> keys) {
		StringBuffer sbuf = new StringBuffer();
		
		if(conditionList != null && conditionList.size() > 0 && keys != null && keys.size() > 0) {
			for(Object condition : conditionList) {
				Map conMap = (Map)condition;
				for(String key : keys) {
					sbuf.append('#');
					sbuf.append(StringUtils.defaultIfEmpty((String)conMap.get(key), ""));
				}
			}
		}
		
		return DigestUtils.md5Hex(sbuf.toString());
	}
	/**
	 * 根据接口条件获取插件所有人员id
	 * @param conditionList   条件参数  list(map)
	 * @return
	 */
	public List getPluginAllUserId(List conditionList) {
		List<String> allids = new ArrayList<String>();

		try {
			String type = this.getConditionDigest(conditionList, Arrays.asList("type", "seclevel", "value"));
			
			String pluginAllUserCacheKey = "PluginLicense_"+type+"_AllUserId";
			String pluginAllUserTimeCacheKey = "PluginLicense_"+type+"_Time";
			
			StaticObj staticobj = StaticObj.getInstance();

			Long pluginAllUserTime = (Long)staticobj.getRecordFromObj(mobileCacheKey, pluginAllUserTimeCacheKey);
			
			long currentTime = (new Date()).getTime();
			
			if(staticobj.getRecordFromObj(mobileCacheKey, pluginAllUserCacheKey)!=null&&pluginAllUserTime!=null&&(currentTime-pluginAllUserTime.longValue())<=1000*60*15){
				return (List)staticobj.getRecordFromObj(mobileCacheKey, pluginAllUserCacheKey);
			}
			
			RecordSet rs = new RecordSet();
			RecordSet rs1 = new RecordSet();
			
			for(Object condition : conditionList) {
				Map<String, String> conditionMap = (Map<String, String>)condition;
				String resourceids = ",";
				int sharetype = Util.getIntValue((String)conditionMap.get("type"), 0);
				String sharevalue = (String)conditionMap.get("value");
				if(sharetype == 3) {//角色
					if(sharevalue == null || "".equals(sharevalue)) continue;
					String setroleid = sharevalue.substring(0, sharevalue.length()-1);
					String setrolelevel = sharevalue.substring(sharevalue.length()-1); 
					if(setroleid == null || "".equals(setroleid)) continue;
					rs1.executeSql("select resourceid from hrmrolemembers where   rolelevel >= '"+setrolelevel+"' and roleid = "+setroleid);
					while(rs1.next()) {
						resourceids  += (String)rs1.getString("resourceid")+",";
					}
					conditionMap.put("resourceids", resourceids);
				}
				
			}
			
			
			String sql = "select id,seclevel,subcompanyid1,departmentid  from HrmResource where status in (0,1,2,3) and (accounttype is null or accounttype=0) and loginid is not null";
			
			if(!"oracle".equals(rs.getDBType())) sql += " and loginid<>''";
			
			rs.executeSql(sql);
			while(rs.next()){
				String uid = rs.getString("id");
				int seclevel = rs.getInt("seclevel");
				String subcompanyid = rs.getString("subcompanyid1");
				String departmentid = rs.getString("departmentid");
				boolean flag = false;
				for(Object condition : conditionList) {
					Map<String, String> conditionMap = (Map<String, String>)condition;
					int sharetype = Util.getIntValue((String)conditionMap.get("type"), 0);
					int shareseclevel = Util.getIntValue((String)conditionMap.get("seclevel"), 0);
					String sharevalue = (String)conditionMap.get("value");
					if(sharetype != 0 && seclevel < shareseclevel) continue; //安全级别判断
					
					if(sharetype == 0 && uid.equals(sharevalue)) {//人员
						flag = true;
						break;
					} else if(sharetype == 1) {//分部
						List sharesubcompanyids = getOrgIdsList(sharevalue, 1);
						if(sharesubcompanyids.contains(subcompanyid)) {
							flag = true;
							break;
						}
					} else if(sharetype == 2) {//部门
						List sharedepartmentids = getOrgIdsList(sharevalue, 2);
						if(sharedepartmentids.contains(departmentid)) {
							flag = true;
							break;
						}
					} else if(sharetype == 3) {//角色
						if(conditionMap.get("resourceids")!=null){
							String ids = (String)conditionMap.get("resourceids");
							if(ids.contains(","+uid+",")){
								flag = true;
								break;
							}
						}
						
					} else if(sharetype == 4) {//所有人
						flag = true;
						break;
					}
				}
				if(flag) allids.add(uid);
			}
			
			staticobj.putRecordToObj(mobileCacheKey, pluginAllUserCacheKey, allids);
			staticobj.putRecordToObj(mobileCacheKey, pluginAllUserTimeCacheKey, new Long(currentTime));
		} catch (Exception e) {
			logger.error("", e);
		}
		
		return allids;
	}
	
	/**
	 * 根据接口条件获取插件所有人员id
	 * @param type            插件类型  mobile|messager
	 * @param conditionList   条件参数  list(map)
	 * @return
	 */
	public List checkMobileUserRight(String userid,List conditionList) {
		
			RecordSet rs1 = new RecordSet();
			List groupidList=new ArrayList(); //当前用户所具有权限用户组id列表
			try {
				ResourceComInfo rci = new ResourceComInfo();
				String mode = Util.null2String(Prop.getPropValue(GCONST.getConfigFile(), "authentic"));
				
				String departmentid=rci.getDepartmentID(userid); //用户部门id
				String subcompanyid=rci.getSubCompanyID(userid); //用户分部id
				String status=rci.getStatus(userid);
				int seclevel0 = Util.getIntValue(rci.getSeclevel(userid),0);
				
				//loginid、account字段整合 qc:128484
				String login_id = Util.null2String(rci.getLoginID(userid));
				
				//String login_id = "ldap".equals(mode) ? Util.null2String(rci.getAccount(userid)) : Util.null2String(rci.getLoginID(userid));
				
				if(!"".equals(login_id)&&!status.equals("0")&&!status.equals("1")&&!status.equals("2")&&!status.equals("3")) 
					return groupidList;
				
				for(int i=0;i<conditionList.size();i++){
					Map map=(Map)conditionList.get(i);
					int sharetype = Util.getIntValue((String)map.get("type"),0);
					int seclevel = Util.getIntValue((String)map.get("seclevel"),0);
					String sharevalue = Util.null2String((String)map.get("value"));
					String groupid=Util.null2String((String)map.get("groupid"));
					
					if(groupidList.indexOf(groupid)!=-1) continue;
					
					boolean flag = false;
					
					if(sharetype==0){
						if(sharevalue.equals(userid)) flag = true;
					} else if(sharetype==1){
						//分部
						List sharesubcompanyids = getOrgIdsList(sharevalue,1);
						if(sharesubcompanyids.contains(subcompanyid)) //分部条件中包含用户所属分部
							flag = true;
					} else if(sharetype==2){
						//部门
						List sharedepartmentids = getOrgIdsList(sharevalue,2);
						if(sharedepartmentids.contains(departmentid)) //部门条件中包含用户所属部门
							flag = true;
					} else if(sharetype==3){
						//角色
						if(sharevalue!=null&&!"".equals(sharevalue)){
						
						String setroleid = sharevalue.substring(0,sharevalue.length()-1);
						String setrolelevel = sharevalue.substring(sharevalue.length()-1);
						
						if(setroleid!=null&&!"".equals(setroleid)&&setrolelevel!=null&&!"".equals(setrolelevel))
							rs1.executeSql("select resourceid,roleid,rolelevel from hrmrolemembers where roleid = " + setroleid+" and resourceid="+userid+" and rolelevel>="+setrolelevel);
							if(rs1.next())
								flag = true;
						}
					} else if(sharetype==4){
						//所有人
						flag = true;
					}
					
					if(flag&&(seclevel==0||seclevel0>=seclevel)) flag = true;
					else flag = false;
					
					if(flag)
						groupidList.add(groupid);
				}
			} catch(Exception e){
				e.printStackTrace();
			}
		return groupidList;
	}
	
	public boolean verifyQuickLogin(String verifyurl, String verifyid) {
		boolean result = true;
		String requestURL = null;
		GetMethod method = null;
		
		try {
			String mobileHost = Prop.getPropValue("EMobile4","serverUrl");
			mobileHost = mobileHost.endsWith("/") ? mobileHost.substring(0, mobileHost.length()-1) : mobileHost;
			verifyurl = verifyurl.startsWith("/") ? verifyurl.substring(1) : verifyurl;
			
			requestURL = mobileHost + "/" +URLDecoder.decode(verifyurl,"UTF-8");
			
			if(requestURL.indexOf("?")>-1) requestURL+="&";
			else requestURL+="?";
			
			requestURL+="verifyid="+verifyid;
	        
	        HttpClient httpClient = new HttpClient();
	        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
	        httpClient.getHttpConnectionManager().getParams().setSoTimeout(2000);
	        
	        //设置访问编码
	        httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
	        //设置 HttpClient 接收 Cookie,用与浏览器一样的策略
	        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
	        //让服务器知道访问源为浏览器 
	        httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; rv:8.0.1) Gecko/20100101 Firefox/8.0.1");
	        
	        method = new GetMethod(requestURL);
	        method.setRequestHeader("Connection","Keep-Alive");
	        
	        int status = httpClient.executeMethod(method);
	        
	        String verifyResult = "";
	        
	        if(status == HttpStatus.SC_OK) {
		        
				InputStream is = method.getResponseBodyAsStream();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				int len = 0;
				byte[] b = new byte[1024];
				while ((len = is.read(b, 0, b.length)) != -1) {
					os.write(b, 0, len);
				}
				byte[] content = os.toByteArray();
	
				is.close();
				os.close();
		        
		        long length = method.getResponseContentLength();
		        
		        verifyResult = new String(content);
		        
		        logger.debug("verifyResult:"+verifyResult);
		        
	        } else {
	        	throw new Exception("The request("+requestURL+") from server return " + status);
	        }
	        
	        JSONObject jo = JSONObject.fromObject(verifyResult);
			
			if(!"true".equals(jo.get("result").toString())) {
				result = false;
			}
		} catch(Exception e) {
			logger.error("requestURL("+requestURL+")", e);
			result = false;
		} finally {
			if(method!=null) method.releaseConnection();
		}
		
		return result;
	}
	
}
