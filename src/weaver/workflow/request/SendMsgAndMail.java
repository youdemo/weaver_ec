package weaver.workflow.request;
/*
* Created on 2009-01-13
* Copyright (c) 2001-2006 泛微软件
* 泛微协同商务系统，版权所有。
* 
*/
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetTrans;
import weaver.file.FileUpload;
import weaver.general.BaseBean;
import weaver.general.GCONST;
import weaver.general.SendMail;
import weaver.general.Util;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import weaver.hrm.resource.ResourceComInfo;
import weaver.system.SystemComInfo;
import weaver.systeminfo.SystemEnv;
import weaver.wechat.SaveAndSendWechat;
import weaver.wechat.SendThread;
import weaver.wechat.cache.ReminderCache;
import weaver.workflow.workflow.WFNodeDtlFieldManager;
import weaver.email.EmailWorkRunnable;
 
/**

 * Description: 发送短信和发送邮件
 * @author 梅运强
 * @version 1.0
 */

public class SendMsgAndMail extends BaseBean {
    
    private ResourceComInfo resourceComInfo=null;
    private ReminderCache reminderCache=null;
    private SaveAndSendWechat saveAndSendWechat = null;
    private WFNodeDtlFieldManager wFNodeDtlFieldManager = null;
    
    private String isIntervene = "0"; //是否来自 超时干预
    private ArrayList InterveneOperators = null; //超时干预者
    
    public SendMsgAndMail(){
        try{
            resourceComInfo = new ResourceComInfo();
            reminderCache = new ReminderCache();
            saveAndSendWechat = new SaveAndSendWechat();
            wFNodeDtlFieldManager = new WFNodeDtlFieldManager();
        }catch(Exception ex){}
    }
    
    public String getIsIntervene() {
		return isIntervene;
	}
	public void setIsIntervene(String isIntervene) {
		this.isIntervene = isIntervene;
	}
	public ArrayList getInterveneOperators() {
		return InterveneOperators;
	}
	public void setInterveneOperators(ArrayList interveneOperators) {
		InterveneOperators = interveneOperators;
	}



	/**
    * Description: 发送短信
    * @param rst
    * @param requestid 工作流ID
    * @param nextnodeid 下一个节点id 
    * @param user 
    * @return void
    */
    public void sendMsg(RecordSetTrans rst,int requestid,int nextnodeid,User user,String src,String nextnodetype) throws Exception{
        
    	// 归档节点不需短信提醒 begin
    	String guidangsql = " select b.archiveNoMsgAlert from workflow_requestbase a, workflow_base b where a.workflowid = b.id and a.currentnodetype = 3 and a.requestid=" + requestid;
        rst.executeSql(guidangsql);
        if(rst.next()) {
        	String archiveNoMsgAlert = Util.null2String(rst.getString("archiveNoMsgAlert"));
        	if("1".equals(archiveNoMsgAlert)) {
        		return;
        	}
        }
        // 归档节点不需短信提醒 end
        
    	int rqMessageType=0;
        String requestname = "";
        ArrayList prefixtmpList = new ArrayList();
        ArrayList suffixtmpList = new ArrayList();
        rst.executeSql("select messageType,requestname,requestlevel from workflow_requestbase where requestid="+requestid);
        if (rst.next()){
            rqMessageType = rst.getInt("messageType");
            requestname = rst.getString("requestname");
        }
        if(rqMessageType==1 || rqMessageType==2){
        	String rechrmnumbers = "";
        	String rechrmids = "";
        	
        	String tmp_isremark = "'0','1','4','8','9','7'";
        	if("1".equals(isIntervene)){
        		tmp_isremark += ",'5'";
        	}
        	
            rst.executeSql("select userid,userType,isremark from workflow_currentoperator where requestid = " + requestid + " and isremark in ("+tmp_isremark+") and nodeid="+nextnodeid);
            while(rst.next()){
                String receiver = rst.getString("userid");
                String receiverType = rst.getString("userType");
				int isremark = rst.getInt("isremark");
				
				if("1".equals(isIntervene) && null!=InterveneOperators){
					if(!InterveneOperators.contains(receiver)){ //只发送给干预人
						continue;
					}
				}
				
                String prefixtmp = "";
                String suffixtmp = "";
                if(src.equals("reject")){//流程退回 例如：退回提醒：XXX费用保险单被退回请你了解 
                	prefixtmp = SystemEnv.getHtmlLabelName(21784,user.getLanguage());
                	suffixtmp = SystemEnv.getHtmlLabelName(21786,user.getLanguage());
                }
                if(src.equals("submit")||src.equals("intervenor")){
	                if (isremark == 0 || isremark==5) {//审批或者确认
	                    if (nextnodetype.equals("1")){//审批
	                    	prefixtmp = SystemEnv.getHtmlLabelName(21782,user.getLanguage());
	                    }else{//确认
	                    	prefixtmp = SystemEnv.getHtmlLabelName(21796,user.getLanguage());
	                    	suffixtmp = SystemEnv.getHtmlLabelName(21797,user.getLanguage());
	                    }
	                } else if (isremark == 4) {//归档
	                	prefixtmp = SystemEnv.getHtmlLabelName(21792,user.getLanguage());
	                	suffixtmp = SystemEnv.getHtmlLabelName(21793,user.getLanguage());
	                } else {//8，9抄送
	                	prefixtmp = SystemEnv.getHtmlLabelName(21783,user.getLanguage());
	                	suffixtmp = SystemEnv.getHtmlLabelName(21785,user.getLanguage());
	                }
                }

                try{
                    if((rqMessageType==1 && !HrmUserVarify.isUserOnline(receiver)) || rqMessageType==2){
                        String rechrmnumber = "";
                        String rechrmid = "";
                        if(!"1".equals(receiverType)){//客户不提醒
                            rechrmid = ""+receiver;
                            rechrmnumber = resourceComInfo.getMobile(receiver);
                        }
                        if(!rechrmnumber.equals("")){
                        	rechrmnumbers = rechrmnumbers + "," + rechrmnumber;
                        	rechrmids = rechrmids + "," + rechrmid;
                        	prefixtmpList.add(prefixtmp);
                        	suffixtmpList.add(suffixtmp);
                        }
                    }
                }catch(Exception e){}
            }
            if(!rechrmnumbers.equals("")){
            	rechrmnumbers = rechrmnumbers.substring(1);
            	rechrmids = rechrmids.substring(1);
            	//抛出线程处理短信提醒等
				new Thread(new SmsWorkRunnable(rechrmnumbers, "", rechrmids, "", requestname, user, requestid, prefixtmpList, suffixtmpList)).start();
            }   
        }
    }
    
    /**
    * Description: 发送短信
    * @param rst
    * @param workflowid 工作流ID
    * @param requestid 请求ID
    * @param nextnodeid 下一个节点id
    * @param request 
    * @param fu  
    * @param src 提交方式 
    * @return void
    */
    public void sendMail(RecordSetTrans rst,
                         int workflowid,
                         int requestid,
                         int nextnodeid,
                         HttpServletRequest request,
                         FileUpload fu,
                         boolean isRequest,
                         String src,
                         String nextnodetype,
                         User user) throws Exception{
    	
    	// 归档节点不需邮件提醒 begin
    	String guidangsql = " select b.archiveNoMailAlert from workflow_requestbase a, workflow_base b where a.workflowid = b.id and a.currentnodetype = 3 and a.requestid=" + requestid;
        rst.executeSql(guidangsql);
        if(rst.next()) {
        	String archiveNoMailAlert = Util.null2String(rst.getString("archiveNoMailAlert"));
        	if("1".equals(archiveNoMailAlert)) {
        		return;
        	}
        }
        // 归档节点不需邮件提醒 end
        String mailMessageType = "0";
        String mailuserid = "-1";
        String mailtoaddress = "";//所有,但当执行退回(src.equals("reject"))操作的时候，mailtoaddress为退回人的email
        String copymail="";//抄送
        String approvalmail="";//审批
        String processmail="";//归档
        String realizemail="";//其它
        String crmmailtoaddress = "";//所有,但当执行退回(src.equals("reject"))操作的时候，mailtoaddress为退回人的email
        String crmcopymail="";//抄送客户
        String crmapprovalmail="";//客户审批
        String crmprocessmail="";//客户归档
        String crmrealizemail="";//其它(客户)
        String mailobject=SystemEnv.getHtmlLabelName(15031,user.getLanguage());
        
        String mailrequestname = "";
        String crmmailrequestname = "";
        int level=0;
        rst.executeSql("select messageType,requestname,requestlevel from workflow_requestbase where requestid="+requestid);
        if (rst.next()){
            mailrequestname=rst.getString("requestname");
            level=Util.getIntValue(rst.getString("requestlevel"),0);
            crmmailrequestname=mailrequestname;
        }
        rst.executeSql("select mailMessageType from  workflow_base  where id = " + workflowid);
        if(rst.next()){
            mailMessageType = rst.getString("mailMessageType");
        }
        if("1".equals(mailMessageType)){
            String noReceiveMailRemindUserIds=",";
            String tempUserId="";
            rst.executeSql("select userId from workflow_requestUserdefault where noReceiveMailRemind='1' ");
            while(rst.next()){
                tempUserId = rst.getString("userid");
                if(tempUserId!=null&&!tempUserId.equals("")){
                    noReceiveMailRemindUserIds += tempUserId + ",";
                }
            }
            mailobject=mailrequestname;
            if(level==2){
                mailobject+="("+SystemEnv.getHtmlLabelName(2087,user.getLanguage())+")";
            }else{
                if(level==1){
                    mailobject+="("+SystemEnv.getHtmlLabelName(15533,user.getLanguage())+")";
                }else{
                    mailobject+="("+SystemEnv.getHtmlLabelName(225,user.getLanguage())+")";
                }
            }
            String oaaddress = "";
            rst.executeSql("select oaaddress from systemset");
			if(rst.next()) oaaddress=rst.getString("oaaddress");
			
			String tmp_isremark = "'0','1','4','8','9','7'";
        	if("1".equals(isIntervene)){
        		tmp_isremark += ",'5'";
        	}
			
            rst.executeSql("select distinct isremark,userid,usertype from workflow_currentoperator where requestid = " + requestid + " and isremark in ("+tmp_isremark+") and nodeid="+nextnodeid);
            while(rst.next()){
                mailuserid = rst.getString("userid");
                int mailusertype=rst.getInt("usertype");
                int isremark=rst.getInt("isremark");
                
                if("1".equals(isIntervene) && null!=InterveneOperators){
					if(!InterveneOperators.contains(mailuserid)){ //只发送给干预人
						continue;
					}
				}
                
                String tempemail="";
                if(mailusertype==1){
                    RecordSet mailrs = new RecordSet();
                    mailrs.executeSql("select email from CRM_CUSTOMERINFO where id =" + mailuserid);
                    if(mailrs.next()){
                        tempemail=Util.null2String(mailrs.getString("email"));
                        if(!"".equals(tempemail)){
                            crmmailtoaddress += tempemail + ",";//当退回(src.equals("reject"))时候，mailtoaddress为退回人的email
                            if(isremark==0 || isremark==5){//审批或者确认
                                if(nextnodetype.equals("1"))//审批
                                    crmapprovalmail += tempemail + ",";
                                else//确认
                                    crmrealizemail += tempemail + ",";
                            }else if(isremark==4){//归档
                                crmprocessmail += tempemail + ",";
                            }else{//8，9抄送
                                crmcopymail += tempemail + ",";
                            }
                        }
                    }
                } else {
                    if (noReceiveMailRemindUserIds.indexOf("," + mailuserid + ",") == -1) {
                        tempemail = resourceComInfo.getEmail(mailuserid);
                        if (!"".equals(tempemail) && tempemail != null) {
                            mailtoaddress += tempemail + ",";//当退回(src.equals("reject"))时候，mailtoaddress为退回人的email
                            if (isremark == 0 || isremark==5) {//审批或者确认
                                if (nextnodetype.equals("1"))//审批
                                    approvalmail += tempemail + ",";
                                else//确认
                                    realizemail += tempemail + ",";
                            } else if (isremark == 4) {//归档
                                processmail += tempemail + ",";
                            } else {//8，9抄送
                                copymail += tempemail + ",";
                            }
                        }
                    }
                }
            }
									      
            if(request!=null || fu!=null || !"".equals(oaaddress)){
                String host="";
                if(isRequest && request!=null){
                    host= Util.getRequestHost(request);
                }else{
                    if(fu!=null)
                        host=Util.getRequestHost(fu.getRequest());
                }
                if(!"".equals(oaaddress))
                {
                	host = oaaddress;
                }
                else
	            {
	            	host = "http://"+host;
	            }
                String loginPage = "login/Login.jsp";
				String gotoPage = "workflow/request/ViewRequest.jsp";
				if(GCONST.getMailReminderSet()){
					loginPage = GCONST.getMailLoginPage();
					gotoPage = GCONST.getMailGotoPage();
				}
                mailrequestname = "(<a style='text-decoration: underline; color: blue;cursor:hand'  target='_blank' href=\""+
                                  host + "/"+loginPage+"?gopage=/"+gotoPage+"?requestid="+requestid+"\" >"+mailrequestname+"</a>)";
                crmmailrequestname = "(<a style='text-decoration: underline; color: blue;cursor:hand'  target='_blank' href=\""+
                                  host + "/"+loginPage+"?logintype=2&gopage=/"+gotoPage+"?requestid="+requestid+"\" >"+crmmailrequestname+"</a>)";                  
            }else{
				if(!"".equals(oaaddress)){
					String host=oaaddress;
					String loginPage = "login/Login.jsp";
					String gotoPage = "workflow/request/ViewRequest.jsp";
					if(GCONST.getMailReminderSet()){
						loginPage = GCONST.getMailLoginPage();
						gotoPage = GCONST.getMailGotoPage();
					}
					mailrequestname = "(<a style='text-decoration: underline; color: blue;cursor:hand'  target='_blank' href=\""+
									  host + "/"+loginPage+"?gopage=/"+gotoPage+"?requestid="+requestid+"\" >"+mailrequestname+"</a>)";
					crmmailrequestname = "(<a style='text-decoration: underline; color: blue;cursor:hand'  target='_blank' href=\""+
									  host + "/"+loginPage+"?logintype=2&gopage=/"+gotoPage+"?requestid="+requestid+"\" >"+crmmailrequestname+"</a>)";
				}else{
                  mailrequestname = "("+mailrequestname+")";
                  crmmailrequestname = "("+crmmailrequestname+")";
				}
            }
									      
            if(src.equals("reject")&&!"".equals(mailtoaddress)){//流程退回 例如：退回提醒：XXX费用保险单被退回请你了解 
                String mailbody = SystemEnv.getHtmlLabelName(21784,user.getLanguage()) + "：" + mailrequestname + SystemEnv.getHtmlLabelName(21786,user.getLanguage());		
                String mailsubject = SystemEnv.getHtmlLabelName(21784,user.getLanguage()) + "：" + mailobject + SystemEnv.getHtmlLabelName(21786,user.getLanguage());
                String title_beginS = Util.null2String(super.getPropValue("MailMessage","mailMSG.reject.title.begin"));
                String title_endS = Util.null2String(super.getPropValue("MailMessage","mailMSG.reject.title.end"));
                String content_beginS = Util.null2String(super.getPropValue("MailMessage","mailMSG.reject.content.begin"));
                String content_endS = Util.null2String(super.getPropValue("MailMessage","mailMSG.reject.content.end"));
                title_beginS = new String(title_beginS.getBytes("iso8859_1"));
                title_endS = new String(title_endS.getBytes("iso8859_1"));
                content_beginS = new String(content_beginS.getBytes("iso8859_1"));
                content_endS = new String(content_endS.getBytes("iso8859_1"));
                if(!title_beginS.equals("")||!title_endS.equals(""))
                    mailsubject = title_beginS + mailobject + title_endS;
                if(!content_beginS.equals("")||!content_endS.equals(""))
                    mailbody = content_beginS + mailrequestname + content_endS;
                SendRemindMail(mailtoaddress,mailsubject,mailbody);
            }
            if(src.equals("reject")&&!"".equals(crmmailtoaddress)){//流程退回 例如：退回提醒：XXX费用保险单被退回请你了解 
                String mailbody = SystemEnv.getHtmlLabelName(21784,user.getLanguage()) + "：" + crmmailrequestname + SystemEnv.getHtmlLabelName(21786,user.getLanguage());		
                String mailsubject = SystemEnv.getHtmlLabelName(21784,user.getLanguage()) + "：" + mailobject + SystemEnv.getHtmlLabelName(21786,user.getLanguage());
                SendRemindMail(crmmailtoaddress,mailsubject,mailbody);
            }
                        
            if(src.equals("submit")||src.equals("intervenor")){
                if(!copymail.equals("")){//抄送 例如：抄送提醒：XXX费用报销单已审批抄送给你查阅 
                    String mailbody  = SystemEnv.getHtmlLabelName(21783,user.getLanguage()) + "：" + mailrequestname + SystemEnv.getHtmlLabelName(21785,user.getLanguage());		
                    String mailsubject  = SystemEnv.getHtmlLabelName(21783,user.getLanguage()) + "：" + mailobject + SystemEnv.getHtmlLabelName(21785,user.getLanguage());
                    String title_beginS = Util.null2String(super.getPropValue("MailMessage","mailMSG.copy.title.begin"));
                    String title_endS = Util.null2String(super.getPropValue("MailMessage","mailMSG.copy.title.end"));
                    String content_beginS = Util.null2String(super.getPropValue("MailMessage","mailMSG.copy.content.begin"));
                    String content_endS = Util.null2String(super.getPropValue("MailMessage","mailMSG.copy.content.end"));
                    title_beginS = new String(title_beginS.getBytes("iso8859_1"));
                    title_endS = new String(title_endS.getBytes("iso8859_1"));
                    content_beginS = new String(content_beginS.getBytes("iso8859_1"));
                    content_endS = new String(content_endS.getBytes("iso8859_1"));
                    if(!title_beginS.equals("")||!title_endS.equals(""))
                        mailsubject = title_beginS + mailobject + title_endS;
                    if(!content_beginS.equals("")||!content_endS.equals(""))
                        mailbody = content_beginS + mailrequestname + content_endS;
                    SendRemindMail(copymail,mailsubject,mailbody);										
                }
                if(!approvalmail.equals("")){//审批 例如：审批提醒：有新的工作需要你审批XXX费用报销单 
                    String mailbody = SystemEnv.getHtmlLabelName(21782,user.getLanguage()) + mailrequestname;		
                    String mailsubject  = SystemEnv.getHtmlLabelName(21782,user.getLanguage()) + mailobject;
                    String title_beginS = Util.null2String(super.getPropValue("MailMessage","mailMSG.approve.title.begin"));
                    String title_endS = Util.null2String(super.getPropValue("MailMessage","mailMSG.approve.title.end"));
                    String content_beginS = Util.null2String(super.getPropValue("MailMessage","mailMSG.approve.content.begin"));
                    String content_endS = Util.null2String(super.getPropValue("MailMessage","mailMSG.approve.content.end"));
                    title_beginS = new String(title_beginS.getBytes("iso8859_1"));
                    title_endS = new String(title_endS.getBytes("iso8859_1"));
                    content_beginS = new String(content_beginS.getBytes("iso8859_1"));
                    content_endS = new String(content_endS.getBytes("iso8859_1"));
                    if(!title_beginS.equals("")||!title_endS.equals(""))
                        mailsubject = title_beginS + mailobject + title_endS;
                    if(!content_beginS.equals("")||!content_endS.equals(""))
                        mailbody = content_beginS + mailrequestname + content_endS;
                    SendRemindMail(approvalmail,mailsubject,mailbody);											
                }
                if(!realizemail.equals("")){//确认 例如：确认提醒：XXX费用报销单需要您的确认，请查阅
                    String mailbody = SystemEnv.getHtmlLabelName(21796,user.getLanguage()) + "：" + mailrequestname + SystemEnv.getHtmlLabelName(21797,user.getLanguage());		
                    String mailsubject  = SystemEnv.getHtmlLabelName(21796,user.getLanguage()) + "：" + mailobject + SystemEnv.getHtmlLabelName(21797,user.getLanguage());
                    String title_beginS = Util.null2String(super.getPropValue("MailMessage","mailMSG.confirm.title.begin"));
                    String title_endS = Util.null2String(super.getPropValue("MailMessage","mailMSG.confirm.title.end"));
                    String content_beginS = Util.null2String(super.getPropValue("MailMessage","mailMSG.confirm.content.begin"));
                    String content_endS = Util.null2String(super.getPropValue("MailMessage","mailMSG.confirm.content.end"));
                    title_beginS = new String(title_beginS.getBytes("iso8859_1"));
                    title_endS = new String(title_endS.getBytes("iso8859_1"));
                    content_beginS = new String(content_beginS.getBytes("iso8859_1"));
                    content_endS = new String(content_endS.getBytes("iso8859_1"));
                    if(!title_beginS.equals("")||!title_endS.equals(""))
                        mailsubject = title_beginS + mailobject + title_endS;
                    if(!content_beginS.equals("")||!content_endS.equals(""))
                        mailbody = content_beginS + mailrequestname + content_endS;
                    SendRemindMail(realizemail,mailsubject,mailbody);										
                }
                if(!processmail.equals("")){//归档 例如：归档提醒：XXX费用报销单已归档，请查阅
                    String mailbody = SystemEnv.getHtmlLabelName(21792,user.getLanguage()) + "：" + mailrequestname + SystemEnv.getHtmlLabelName(21793,user.getLanguage());		
                    String mailsubject  = SystemEnv.getHtmlLabelName(21792,user.getLanguage()) + "：" + mailobject + SystemEnv.getHtmlLabelName(21793,user.getLanguage());
                    String title_beginS = Util.null2String(super.getPropValue("MailMessage","mailMSG.processed.title.begin"));
                    String title_endS = Util.null2String(super.getPropValue("MailMessage","mailMSG.processed.title.end"));
                    String content_beginS = Util.null2String(super.getPropValue("MailMessage","mailMSG.processed.content.begin"));
                    String content_endS = Util.null2String(super.getPropValue("MailMessage","mailMSG.processed.content.end"));
                    title_beginS = new String(title_beginS.getBytes("iso8859_1"));
                    title_endS = new String(title_endS.getBytes("iso8859_1"));
                    content_beginS = new String(content_beginS.getBytes("iso8859_1"));
                    content_endS = new String(content_endS.getBytes("iso8859_1"));
                    if(!title_beginS.equals("")||!title_endS.equals(""))
                        mailsubject = title_beginS + mailobject + title_endS;
                    if(!content_beginS.equals("")||!content_endS.equals(""))
                        mailbody = content_beginS + mailrequestname + content_endS;
                    SendRemindMail(processmail,mailsubject,mailbody);										
                }
                
                if(!crmcopymail.equals("")){//抄送 例如：抄送提醒：XXX费用报销单已审批抄送给你查阅 
                    String mailbody  = SystemEnv.getHtmlLabelName(21783,user.getLanguage()) + "：" + crmmailrequestname + SystemEnv.getHtmlLabelName(21785,user.getLanguage());		
                    String mailsubject  = SystemEnv.getHtmlLabelName(21783,user.getLanguage()) + "：" + mailobject + SystemEnv.getHtmlLabelName(21785,user.getLanguage());
                    SendRemindMail(crmcopymail,mailsubject,mailbody);										
                }
                if(!crmapprovalmail.equals("")){//审批 例如：审批提醒：有新的工作需要你审批XXX费用报销单 
                    String mailbody = SystemEnv.getHtmlLabelName(21782,user.getLanguage()) + crmmailrequestname;		
                    String mailsubject  = SystemEnv.getHtmlLabelName(21782,user.getLanguage()) + mailobject;
                    SendRemindMail(crmapprovalmail,mailsubject,mailbody);											
                }
                if(!crmrealizemail.equals("")){//确认 例如：确认提醒：XXX费用报销单需要您的确认，请查阅
                    String mailbody = SystemEnv.getHtmlLabelName(21796,user.getLanguage()) + "：" + crmmailrequestname + SystemEnv.getHtmlLabelName(21797,user.getLanguage());		
                    String mailsubject  = SystemEnv.getHtmlLabelName(21796,user.getLanguage()) + "：" + mailobject + SystemEnv.getHtmlLabelName(21797,user.getLanguage());
                    SendRemindMail(crmrealizemail,mailsubject,mailbody);										
                }
                if(!crmprocessmail.equals("")){//归档 例如：归档提醒：XXX费用报销单已归档，请查阅
                    String mailbody = SystemEnv.getHtmlLabelName(21792,user.getLanguage()) + "：" + crmmailrequestname + SystemEnv.getHtmlLabelName(21793,user.getLanguage());		
                    String mailsubject  = SystemEnv.getHtmlLabelName(21792,user.getLanguage()) + "：" + mailobject + SystemEnv.getHtmlLabelName(21793,user.getLanguage());
                    SendRemindMail(crmprocessmail,mailsubject,mailbody);										
                }
            }
        }
    }
    
    /**
    * 发生流程提醒邮件
    * @param mailtoaddress   邮件地址
    * @param mailobject      邮件标题
    * @param mailrequestname 邮件内容
    */
    private void SendRemindMail(String mailtoaddress,String mailobject,String mailrequestname){
    	//将邮件提醒抛出线程处理
		//new Thread(new EmailWorkRunnable(mailtoaddress, mailobject, mailrequestname)).start();
        new WFPathUtil().getFixedThreadPool().execute(new EmailWorkRunnable(mailtoaddress, mailobject, mailrequestname));
    }
    
    
    /**
	 * 流程微信提醒       线程发送,不需要实时知道返回值
	 * @param mode 发送模式 默认0,无超链接, 1查看流程,需要超链接 2以后再定义
	 * @return
	 */
	 
     public void sendChats(RecordSetTrans rst,int workflowid,int requestid,int nextnodeid,User user,String src,String nextnodetype) throws Exception{
    	  int rqCharsType=0;
		        String requestname = ""; 
		        String chatsMsg ="";
		        rst.executeSql("select chatsType,requestname,requestlevel from workflow_requestbase where requestid="+requestid);
		        if (rst.next()){
		            rqCharsType = rst.getInt("chatsType");
		            requestname = rst.getString("requestname");
		        } 
		        String tmp_isremark = "'0','1','4','8','9','7'";
	        	if("1".equals(isIntervene)){
	        		tmp_isremark += ",'5'";
	        	}
		        if(rqCharsType==1){ 
		        	StringBuffer approvePersons =new StringBuffer(); //审批人员
		        	StringBuffer submitPersons =new StringBuffer(); // 确认人员
		        	StringBuffer archivePersons =new StringBuffer(); //归档人员 
		        	StringBuffer noticePersons =new StringBuffer();//抄送人员
		        	StringBuffer rejectPersons =new StringBuffer(); //退回人员
		            rst.executeSql("select userid,userType,isremark from workflow_currentoperator where requestid = " + requestid + " and isremark in ("+tmp_isremark+") and nodeid="+nextnodeid);
		            while(rst.next()){
		                String receiver = rst.getString("userid");
		                String receiverType = rst.getString("userType");
						int isremark = rst.getInt("isremark"); 
						
						if("1".equals(isIntervene) && null!=InterveneOperators){
							if(!InterveneOperators.contains(receiver)){ //只发送给干预人
								continue;
							}
						}
						
		                if(src.equals("reject")){//流程退回 例如：退回提醒：XXX费用保险单被退回请你了解 
		                	rejectPersons.append(receiver);
		                	rejectPersons.append(",");
		                }
		                if(src.equals("submit")||src.equals("intervenor")){
			                if (isremark == 0 || isremark==5) {//审批或者确认
			                    if (nextnodetype.equals("1")){//审批
			                    	approvePersons.append(receiver);
			                    	approvePersons.append(",");
			                    }else{//确认
			                    	submitPersons.append(receiver);
			                    	submitPersons.append(",");
			                    }
			                } else if (isremark == 4) {//归档
			                	archivePersons.append(receiver);
			                	archivePersons.append(",");
			                } else {//8，9抄送
			                	noticePersons.append(receiver);
			                	noticePersons.append(",");
			                }
		                }  
		            	
		            } 
		            if(approvePersons.indexOf(",")!=-1){
		            	chatsMsg=reminderCache.getReminderStr(requestname,"wf_approve",false); 
		            	sendChatsInfo(rst, workflowid, requestid, nextnodetype,chatsMsg,approvePersons.toString());
		            }
					if(submitPersons.indexOf(",")!=-1){ 
						chatsMsg=reminderCache.getReminderStr(requestname,"wf_submit",false);     
						sendChatsInfo(rst, workflowid, requestid, nextnodetype,chatsMsg,submitPersons.toString());
					}
					if(archivePersons.indexOf(",")!=-1){ 
						chatsMsg=reminderCache.getReminderStr(requestname,"wf_archive",false); 
						sendChatsInfo(rst, workflowid, requestid, nextnodetype,chatsMsg,archivePersons.toString());
					}
					if(noticePersons.indexOf(",")!=-1){ 
						chatsMsg=reminderCache.getReminderStr(requestname,"wf_notice",false); 
						sendChatsInfo(rst, workflowid, requestid, nextnodetype,chatsMsg,noticePersons.toString());
					}
					if(rejectPersons.indexOf(",")!=-1){
						chatsMsg=reminderCache.getReminderStr(requestname,"wf_reject",false); 
						sendChatsInfo(rst, workflowid, requestid, nextnodetype,chatsMsg,rejectPersons.toString());
					}
		        }
		    }
     
	     private void sendChatsInfo(RecordSetTrans rst, int workflowid,int requestid, String nextnodetype, String chatsMsg,String hrms)
			throws Exception {
				if(nextnodetype.equals("3")){ 
					   int notRemindifArchived= 0;
					   rst.execute("select notRemindifArchived from workflow_base where id = " + workflowid) ;
				        while(rst.next()){
				        	notRemindifArchived=rst.getInt("notRemindifArchived"); 
				        }
				        if(notRemindifArchived!=1){ 
				        	 Map map= new HashMap();
					         map.put("detailid",requestid);
					         saveAndSendWechat.setHrmid(hrms);
					         saveAndSendWechat.setMsg(chatsMsg);
					         saveAndSendWechat.setMode(1);
					         saveAndSendWechat.setParams(map);
					         saveAndSendWechat.send();
				        }
				   }else{ 
				       Map map= new HashMap();
				       map.put("detailid",requestid);
				       saveAndSendWechat.setHrmid(hrms);
				       saveAndSendWechat.setMsg(chatsMsg);
				       saveAndSendWechat.setMode(1);
				       saveAndSendWechat.setParams(map);
				       saveAndSendWechat.send();
				   }
		}
}
