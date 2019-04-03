package gvo.ecsysn;


import java.util.HashMap;
import java.util.Map;

import javax.webbeans.New;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.general.Util;
import gvo.tmc.org.HrmDepartmentBean;
import gvo.tmc.org.HrmJobTitleBean;
import gvo.tmc.org.HrmOrgAction;
import gvo.tmc.org.HrmResourceBean;
import gvo.tmc.org.HrmSubCompanyBean;
import gvo.tmc.org.ReturnInfo;

public class Ecsysnorganize {
	BaseBean log = new BaseBean();
	//分部同步
	public  void operSubCompany(){		
		
		//RecordSet rs = new RecordSet();
		HrmOrgAction  hoaSub = new HrmOrgAction(); 
		String subCompanyCode;// 分部编码  区分标示		
		String subCompanyName;// 分部简称		
		String subCompanyDesc;// 分部全称		
		int idOrCode;// 上级关系      0通过ID  1是通过编码	
		String superID;// 上级id   如果没有上级补充为0 		
		String superCode;// 上级唯一编码   如果没有上级编码为空 
		String cancel="";
		int status = 0;// 状态   1 封存   0正常		
		String  orderBy ="";// 排序		 
		Map<String,String> subcusMap;// 自定义字段 
		
		RecordSetDataSource rsdSub = new RecordSetDataSource("oadb");	
		
		String subSql="   select subCompanyCode,subCompanyName,subCompanyDesc,supsubcomid,canceled,"+
  			"(select subcompanycode from HrmSubCompany where id=a.supsubcomid) superCode,a.showorder from HrmSubCompany a where a.subcompanycode is not null order by a.id asc";
		rsdSub.execute(subSql);
		 		
		while(rsdSub.next()){
			idOrCode=1;
			subCompanyCode=Util.null2String(rsdSub.getString("subCompanyCode"));
			subCompanyName=Util.null2String(rsdSub.getString("subCompanyName"));
			subCompanyDesc=Util.null2String(rsdSub.getString("subCompanyDesc"));
			superID=Util.null2String(rsdSub.getString("supsubcomid"));
			superCode=Util.null2String(rsdSub.getString("superCode"));
			cancel=Util.null2String(rsdSub.getString("canceled"));
			orderBy=Util.null2String(rsdSub.getString("showorder"));
			if("".equals(orderBy)){
				orderBy = "0";
			}
			HrmSubCompanyBean hscBean=new HrmSubCompanyBean();
			
			hscBean.setSubCompanyCode(subCompanyCode);
			hscBean.setSubCompanyName(subCompanyName);
			hscBean.setSubCompanyDesc(subCompanyDesc);
			hscBean.setOrderBy(Integer.valueOf(orderBy));
			hscBean.setIdOrCode(1);

			hscBean.setSuperCode(superCode);
						
			if("1".equals(cancel)){
			      hscBean.setStatus(1);
			      
				}else {
					hscBean.setStatus(0);
				}
			
			ReturnInfo result=hoaSub.operSubCompany(hscBean);			
			if(result.isTure()){
				
				}else{
					log.writeLog("同步分部失败:"+Util.null2String(rsdSub.getString("subCompanyCode"))+" result:"+result.getRemark());
				}
		
		}
	
	}
	
	public void operDepartment(){
		
		HrmOrgAction  hoaDepart = new HrmOrgAction(); 				
		String departmentcode;// 部门编号		
		String departmentname;// 部门简称		
		String departmentark;// 部门全称	
		int idOrCode;// 上级关系      0通过ID  1是通过编码		
		String superID;// 上级id   如果没有上级补充为0 		
		String superCode;// 上级唯一编码   如果没有上级编码为空 
		int comIdOrCode;// 所属公司ID		
		String subcompanyid1;// 所属公司编码
		String subcompanyCode;// 状态   0正常  1封存
		String cancel="";
		String orderby="";
		Map<String,String> cusDepartMap;
		HrmOrgAction  hoa = new HrmOrgAction();  
		RecordSetDataSource rsdSub = new RecordSetDataSource("oadb");
		
		String sql="select departmentcode,departmentname,departmentmark,(select departmentcode from hrmdepartment where id=a.supdepid) as superCode,(select subCompanyCode from hrmsubcompany where id=a.subcompanyid1) as subcompanyCode,canceled,showorder,(select zzlx from hrmdepartmentdefined where deptid=a.id) as zzlx from hrmdepartment a where a.departmentcode is not null order by a.id asc";
		
		rsdSub.executeSql(sql);
		while(rsdSub.next()){
			HrmDepartmentBean hdb = new HrmDepartmentBean();
			hdb.setDepartmentcode(Util.null2String(rsdSub.getString("departmentcode")));
			hdb.setDepartmentname(Util.null2String(rsdSub.getString("departmentname")));
			hdb.setDepartmentark(Util.null2String(rsdSub.getString("departmentmark")));
		    hdb.setIdOrCode(1);
		    hdb.setSuperCode(Util.null2String(rsdSub.getString("superCode")));
		    hdb.setComIdOrCode(1);
		    hdb.setSubcompanyCode(Util.null2String(rsdSub.getString("subcompanyCode")));
		    orderby=Util.null2String(rsdSub.getString("showorder"));
		    cancel=Util.null2String(rsdSub.getString("canceled"));
		    if("".equals(orderby)){
		    	orderby="0";
		    }
		    hdb.setOrderBy(Integer.valueOf(orderby));
		    if("1".equals(cancel)){
		    	hdb.setStatus(1);
			      
			}else {
				hdb.setStatus(0);
				
			}
		    hdb.addCusMap("zzlx", Util.null2String(rsdSub.getString("zzlx")));
		    ReturnInfo result=hoa.operDept(hdb);
		    if(result.isTure()){
				
			}else{
				log.writeLog("同步部门失败:"+Util.null2String(rsdSub.getString("departmentcode"))+" result:"+result.getRemark());
			}
		    
		}
		
	
	}
	
	public void operjobtitle(){
		HrmOrgAction  hoa = new HrmOrgAction();  
		RecordSetDataSource rsd= new RecordSetDataSource("oadb");
		String sql="select id,jobtitlemark,jobtitlename,Jobtitleremark,( select jobactivityname from hrmjobactivities where id=a.jobactivityid) jobactivityName,(select departmentcode from hrmdepartment where id=a.jobdepartmentid) as jobdepartmentcode,(select departmentcode from hrmdepartment where id=a.jobdepartmentid) as jobdepartmentcode,( select c.jobgroupname from hrmjobactivities b,hrmjobgroups c where b.jobgroupid=c.id and b.id=a.jobactivityid) jobgroupname from hrmjobtitles a order by id asc";
		rsd.executeSql(sql);
		while(rsd.next()){
			HrmJobTitleBean hjt = new HrmJobTitleBean();
			hjt.setJobtitlecode(Util.null2String(rsd.getString("id")));
			hjt.setJobtitlemark(Util.null2String(rsd.getString("jobtitlemark")));
			hjt.setJobtitlename(Util.null2String(rsd.getString("jobtitlename")));
			hjt.setJobtitleremark(Util.null2String(rsd.getString("Jobtitleremark")));
			hjt.setJobactivityName(Util.null2String(rsd.getString("jobactivityName")));
	
			hjt.setDeptIdOrCode(1); 
			hjt.setJobdepartmentCode(Util.null2String(rsd.getString("jobdepartmentcode")));
			hjt.setJobGroupName(Util.null2String(rsd.getString("jobgroupname"))); 
			ReturnInfo result=hoa.operJobtitle(hjt);
			 if(result.isTure()){
					
				}else{
					log.writeLog("同步岗位失败:"+Util.null2String(rsd.getString("jobtitlemark"))+" result:"+result.getRemark());
				}
		    
		}
	}
	
	public void operResource(){
		HrmOrgAction  hoa = new HrmOrgAction();  
		RecordSetDataSource rsd= new RecordSetDataSource("oadb");
		RecordSetDataSource rsd_dt1= new RecordSetDataSource("oadb");
		String sql_dt1="";
		String Seclevel="";
		String dsporder="";
		String id="";
		String belongtocode="";
		String field0="";//开户银行名称
		String sql="select id, workcode,loginid,status, lastname,sex,jobtitle as jobtitleCode,(select departmentCode from hrmdepartment where id=a.departmentid) departmentCode,(select workcode from hrmresource where id=a.managerid ) managerCode,(select workcode from hrmresource where id=a.belongto) belongtoCode,nationality,systemlanguage,"+
                   "password,maritalstatus,telephone,mobile,mobilecall,email,dsporder,createdate,(select locationname from hrmlocations where id=locationid) as locationid,workroom,homeaddress,enddate,datefield1,datefield2,datefield3,datefield4,datefield5,"+
				   "numberfield1,numberfield2,numberfield3,numberfield4,numberfield5,textfield1,textfield2,textfield3,textfield4,textfield5,tinyintfield1,tinyintfield2,tinyintfield3,tinyintfield4,tinyintfield5,"+
				   "jobactivitydesc,certificatenum,nativeplace,educationlevel,residentplace,policy,degree,height,accumfundaccount,birthplace,folk,extphone,fax,weight,tempresidentnumber,probationenddate,"+
				   "bankid1,accountid1,joblevel,costcenterid,assistantid,seclevel "+
				   "from hrmresource a where a.workcode is not null order by id asc";
		rsd.executeSql(sql);
		while(rsd.next()){
			id=Util.null2String(rsd.getString("id"));
			HrmResourceBean hrb = new HrmResourceBean();
			hrb.setWorkcode(Util.null2String(rsd.getString("workcode")));
			hrb.setLoginid(Util.null2String(rsd.getString("loginid")));
			hrb.setStatus(Util.null2String(rsd.getString("status")));
			hrb.setLastname(Util.null2String(rsd.getString("lastname")));
			hrb.setSex(Util.null2String(rsd.getString("sex")));
			hrb.setBirthday(Util.null2String(rsd.getString("birthday")));
			Seclevel=Util.null2String(rsd.getString("seclevel"));
			if("".equals(Seclevel)){
				Seclevel="10";
			}
			hrb.setSeclevel(Integer.valueOf(Seclevel));
			hrb.setJobIdOrCode(1);
			hrb.setJobtitleCode(Util.null2String(rsd.getString("jobtitleCode")));
			hrb.setDeptIdOrCode(1);
			hrb.setDepartmentCode(Util.null2String(rsd.getString("departmentCode")));
			hrb.setManagerIdOrCode(1);
			hrb.setManagerCode(Util.null2String(rsd.getString("managerCode")));
			belongtocode=Util.null2String(rsd.getString("belongtoCode"));
			// 所属主帐号  通过id或Code识别   0通过id  1通过编码   -1是不启用【主账号】
			if(belongtocode.length()>0){
			hrb.setBelongIdOrCode(1);
			hrb.setBelongtoCode(Util.null2String(rsd.getString("belongtoCode")));
			}
			hrb.setNationality(Util.null2String(rsd.getString("nationality")));
			hrb.setSystemlanguage(Util.null2String(rsd.getString("systemlanguage")));
			hrb.setPassword(Util.null2String(rsd.getString("password")));
			hrb.setMaritalstatus(Util.null2String(rsd.getString("maritalstatus")));
			hrb.setTelephone(Util.null2String(rsd.getString("telephone")));
			hrb.setMobile(Util.null2String(rsd.getString("mobile")));
			hrb.setMobilecall(Util.null2String(rsd.getString("mobilecall")));
			hrb.setEmail(Util.null2String(rsd.getString("email")));
			dsporder=Util.null2String(rsd.getString("dsporder"));
			if("".equals(dsporder)){
				dsporder="0";
			}
			hrb.setDsporder(Integer.valueOf(dsporder));
			hrb.setCreatedate(Util.null2String(rsd.getString("createdate")));
			hrb.setLocationid(Util.null2String(rsd.getString("locationid")));
			hrb.setWorkroom(Util.null2String(rsd.getString("workroom")));
			hrb.setHomeaddress(Util.null2String(rsd.getString("homeaddress")));
			hrb.setEnddate(Util.null2String(rsd.getString("enddate")));
			hrb.setDatefield1(Util.null2String(rsd.getString("datefield1")));
			hrb.setDatefield2(Util.null2String(rsd.getString("datefield2")));
			hrb.setDatefield3(Util.null2String(rsd.getString("datefield3")));
			hrb.setDatefield4(Util.null2String(rsd.getString("datefield4")));
			hrb.setDatefield5(Util.null2String(rsd.getString("datefield5")));
			hrb.setNumberfield1(Util.null2String(rsd.getString("numberfield1")));
			hrb.setNumberfield2(Util.null2String(rsd.getString("numberfield2")));
			hrb.setNumberfield3(Util.null2String(rsd.getString("numberfield3")));
			hrb.setNumberfield4(Util.null2String(rsd.getString("numberfield4")));
			hrb.setNumberfield5(Util.null2String(rsd.getString("numberfield5")));
			hrb.setTextfield1(Util.null2String(rsd.getString("textfield1")));
			hrb.setTextfield2(Util.null2String(rsd.getString("textfield2")));
			hrb.setTextfield3(Util.null2String(rsd.getString("textfield3")));
			hrb.setTextfield4(Util.null2String(rsd.getString("textfield4")));
			hrb.setTextfield5(Util.null2String(rsd.getString("textfield5")));
			
			hrb.setTinyintfield1(Util.null2String(rsd.getString("tinyintfield1")));
			hrb.setTinyintfield1(Util.null2String(rsd.getString("tinyintfield2")));
			hrb.setTinyintfield1(Util.null2String(rsd.getString("tinyintfield3")));
			hrb.setTinyintfield1(Util.null2String(rsd.getString("tinyintfield4")));
			hrb.setTinyintfield1(Util.null2String(rsd.getString("tinyintfield5")));
			hrb.setJobactivitydesc(Util.null2String(rsd.getString("jobactivitydesc")));
			hrb.setCertificatenum(Util.null2String(rsd.getString("certificatenum")));
			hrb.setNativeplace(Util.null2String(rsd.getString("nativeplace")));
			hrb.setEducationlevel(Util.null2String(rsd.getString("educationlevel")));
			hrb.setResidentplace(Util.null2String(rsd.getString("residentplace")));
			hrb.setPolicy(Util.null2String(rsd.getString("policy")));
			hrb.setDegree(Util.null2String(rsd.getString("degree")));
			hrb.setHeight(Util.null2String(rsd.getString("height")));
			hrb.setAccumfundaccount(Util.null2String(rsd.getString("accumfundaccount")));
			hrb.setBirthplace(Util.null2String(rsd.getString("birthplace")));
			hrb.setFolk(Util.null2String(rsd.getString("folk")));
			hrb.setExtphone(Util.null2String(rsd.getString("extphone")));
			hrb.setFax(Util.null2String(rsd.getString("fax")));
			hrb.setWeight(Util.null2String(rsd.getString("weight")));
			hrb.setTempresidentnumber(Util.null2String(rsd.getString("tempresidentnumber")));
			hrb.setProbationenddate(Util.null2String(rsd.getString("probationenddate")));
			hrb.setBankid1(Util.null2String(rsd.getString("bankid1")));
			hrb.setAccountid1(Util.null2String(rsd.getString("accountid1")));
			hrb.setJoblevel(Util.null2String(rsd.getString("joblevel")));
			hrb.setCostcenterid(Util.null2String(rsd.getString("costcenterid")));
			hrb.setAssistantid(Util.null2String(rsd.getString("assistantid")));
			sql_dt1=" select(select bankname from formtable_main_134 where bankid=field12 and rownum=1) bankname from cus_fielddata where id="+id+" and scopeid=1";
			rsd_dt1.executeSql(sql_dt1);
			if(rsd_dt1.next()){
				field0 = Util.null2String(rsd_dt1.getString("bankname"));
				hrb.addCusMap2("field0", field0);
			}
			sql_dt1="select * from cus_fielddata where scopeid='-1' and id="+id;
			rsd_dt1.executeSql(sql_dt1);
			if(rsd_dt1.next()){
				hrb.addCusMap("field2", Util.null2String(rsd_dt1.getString("field15")));//卡号
				hrb.addCusMap("field3", Util.null2String(rsd_dt1.getString("field16")));//关系人姓名
				hrb.addCusMap("field4", Util.null2String(rsd_dt1.getString("field17")));//关系人工号
				hrb.addCusMap("field5", Util.null2String(rsd_dt1.getString("field18")));//与本人关系
				hrb.addCusMap("field6", Util.null2String(rsd_dt1.getString("field19")));//推荐人姓名
				hrb.addCusMap("field7", Util.null2String(rsd_dt1.getString("field20")));//推荐人工号
				hrb.addCusMap("field8", Util.null2String(rsd_dt1.getString("field21")));//与被推荐人关系
				hrb.addCusMap("field9", Util.null2String(rsd_dt1.getString("field25")));//人员类型
				hrb.addCusMap("field10", Util.null2String(rsd_dt1.getString("field27")));//指导人
				hrb.addCusMap("field11", Util.null2String(rsd_dt1.getString("field31")));//工作地点
				hrb.addCusMap("field12", Util.null2String(rsd_dt1.getString("field32")));//通道
				hrb.addCusMap("field13", Util.null2String(rsd_dt1.getString("field33")));//序列
				//hrb.addCusMap("field14", Util.null2String(rsd_dt1.getString("field34")));//职等
				hrb.addCusMap("field24", Util.null2String(rsd_dt1.getString("field37")));//合同类型？？
				hrb.addCusMap("field25", Util.null2String(rsd_dt1.getString("field36")));//社保缴纳地
				hrb.addCusMap("field26", Util.null2String(rsd_dt1.getString("field40")));//甲方主体
			}
			sql_dt1="select * from cus_fielddata where scopeid='1' and id="+id;
			rsd_dt1.executeSql(sql_dt1);
			if(rsd_dt1.next()){
				hrb.addCusMap2("field18", Util.null2String(rsd_dt1.getString("field22")));//支付工资
				hrb.addCusMap2("field19", Util.null2String(rsd_dt1.getString("field23")));//离职类型
				hrb.addCusMap2("field20", Util.null2String(rsd_dt1.getString("field24")));//离职原因
				hrb.addCusMap2("field21", Util.null2String(rsd_dt1.getString("field30")));//账户名称
				hrb.addCusMap2("field22", Util.null2String(rsd_dt1.getString("field28")));//账户名称
				hrb.addCusMap2("field23", Util.null2String(rsd_dt1.getString("field29")));//联行号
				
			}
			ReturnInfo result=hoa.operResource(hrb);
			 if(result.isTure()){
					
				}else{
					log.writeLog("人员同步失败:"+Util.null2String(rsd.getString("workcode"))+" result:"+result.getRemark());
				}
			
			
		}
	}
	
	public String getEcryid(String oaryid) {
		RecordSetDataSource rsd= new RecordSetDataSource("oadb");
		RecordSet rSet = new RecordSet();
		String ryid = "";
		String workcode = "";
		if("".equals(oaryid)) {
			return "";
		}
		String sql = "select workcode from hrmresource where id="+oaryid;
		rsd.executeSql(sql);
		if(rsd.next()) {
			workcode = Util.null2String(rsd.getString("workcode"));
		}
		if(!"".equals(workcode)) {
			sql="select id from hrmresource where workcode='"+workcode+"'";
			rSet.executeSql(sql);
			if(rSet.next()) {
				ryid = Util.null2String(rSet.getString("id"));
			}
		}
		return ryid;
	}
	
}
