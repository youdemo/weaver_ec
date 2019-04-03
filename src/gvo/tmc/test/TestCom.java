package gvo.tmc.test;

import gvo.tmc.org.HrmOrgAction;
import gvo.tmc.org.HrmSubCompanyBean;

public class TestCom {
	public static void main(String[] args) {
		HrmOrgAction  hoa = new HrmOrgAction();  
		HrmSubCompanyBean hsb = new HrmSubCompanyBean();
		hsb.setSubCompanyCode("001");
		hsb.setSubCompanyName("testName");
		hsb.setSubCompanyDesc("NameTest");
		hsb.setIdOrCode(1);
		hsb.setSuperCode("001");
		hsb.setOrderBy(0);
		hsb.setStatus(0);
		hsb.addCusMap("tt1", "shsTest");
		hoa.operSubCompany(hsb);
	}
}
