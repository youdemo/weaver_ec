package gvo.ecsysn;

import weaver.general.BaseBean;
import weaver.interfaces.schedule.BaseCronJob;

public class SysnGVOOrganize extends BaseCronJob{
	public void execute() { 
		BaseBean log = new BaseBean();
		log.writeLog("组织同步开始:");
		Ecsysnorganize esc = new Ecsysnorganize();
		EcRoles er = new EcRoles();
		esc.operSubCompany();
		esc.operDepartment();
		esc.operjobtitle();
		esc.operResource();
		er.sysRole();
		SysOAHrmMidTable somt = new SysOAHrmMidTable();
		somt.sysMidTable();//同步中间表
		SysnOAMatrixInfo som = new SysnOAMatrixInfo();
		som.sysnMatrix();//同步矩阵	
		log.writeLog("组织同步结束:");
	} 

}
