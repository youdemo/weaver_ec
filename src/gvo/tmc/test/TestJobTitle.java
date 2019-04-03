package gvo.tmc.test;

public class TestJobTitle {

	public static void main(String[] args) {
		gvo.tmc.org.HrmJobTitleBean hjt = new gvo.tmc.org.HrmJobTitleBean();
		
		hjt.setJobtitlecode("80001021");
		hjt.setJobtitlename("软件开发");
		hjt.setJobtitlemark("开发技术");
		hjt.setJobtitleremark("开发技术");
		hjt.setDeptIdOrCode(1);
		hjt.setJobdepartmentid("");
		hjt.setJobdepartmentCode("10013");
		hjt.setSuperJobCode("80001020");
		hjt.setJobactivityName("技术人员");
		hjt.setJobGroupName("开发技术类");
		
	}
}
