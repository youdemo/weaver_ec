package weaver.workflow.request;

/*
* Created on 2006-05-18
* Copyright (c) 2001-2006 泛微软件
* 泛微协同商务系统，版权所有。
* 
*/
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import oracle.sql.CLOB;

import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.aegis.type.java5.IgnoreProperty;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.weaver.ofs.webservices.AnyType2AnyTypeMapEntry;

import weaver.WorkPlan.CreateWorkplanByWorkflow;
import weaver.common.StringUtil;
import weaver.conn.ConnStatement;
import weaver.conn.RecordSet;
import weaver.conn.RecordSetTrans;
import weaver.crm.Maint.CustomerInfoComInfo;
import weaver.docs.docs.DocExtUtil;
import weaver.docs.share.DocShareUtil;
import weaver.file.FileUpload;
import weaver.file.Prop;
import weaver.fna.costStandard.CostStandard;
import weaver.fna.general.FnaCommon;
import weaver.fna.maintenance.FnaAdvanceAmountControl;
import weaver.fna.maintenance.FnaBorrowAmountControl;
import weaver.fna.maintenance.FnaBudgetControl;
import weaver.general.BaseBean;
import weaver.general.GCONST;
import weaver.general.LocateUtil;
import weaver.general.SendMail;
import weaver.general.StaticObj;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.hrm.resource.ResourceComInfo;
import weaver.interfaces.web.OfsClient;
import weaver.interfaces.web.PostWorkflowInf;
import weaver.interfaces.workflow.action.Action;
import weaver.mobile.webservices.workflow.soa.RequestPreProcessing;
import weaver.soa.workflow.request.RequestService;
import weaver.system.SysWFLMonitor;
import weaver.system.SystemComInfo;
import weaver.systeminfo.SystemEnv;
import weaver.workflow.bean.Track;
import weaver.workflow.bean.Trackdetail;
import weaver.workflow.exceldesign.ExcelLayoutManager;
import weaver.workflow.field.FieldComInfo;
import weaver.workflow.form.FormFieldlabelMainManager;
import weaver.workflow.mode.FieldInfo;
import weaver.workflow.monitor.Monitor;
import weaver.workflow.msg.PoppupRemindInfoUtil;
import weaver.workflow.report.RequestDeleteLog;
import weaver.workflow.request.WFAutoApproveUtils.AutoApproveParams;
import weaver.workflow.workflow.WFModeNodeFieldManager;
import weaver.workflow.workflow.WFNodeDtlFieldManager;
import weaver.workflow.workflow.WFSubDataAggregation;
import weaver.workflow.workflow.WfFunctionManageUtil;
import weaver.workflow.workflow.WorkflowAllComInfo;
import weaver.workflow.workflow.WorkflowRequestComInfo;
import weaver.worktask.request.RequestCreateByWF;


/**

 * Description: 流程处理基本类
 * @author ben
 * @version 1.0 
 */

public class RequestManager extends BaseBean {
	private static RequestIdUpdate requestIdUpdate = new RequestIdUpdate();  
	//同步的方法产生ID必须声明成STATIC对象    
	private RecordSet rs;
	private RecordSet rs1;
	private RecordSet rs2;
	private RecordSet rs3;
	private RecordSet rs4;
	private HttpServletRequest request;
	private FileUpload fu;			//modify by xhheng @20050315 for 附件上传,通过 FileUpload 获取原request参数
	private boolean isRequest=true;		//记录是否是HttpServletRequest对象,用以判断是从request还是fu中获取参数值
	private User user;

private String isMultiDoc = "";//多文档新建

	private String src = "";               // 操作类型
	private String iscreate = "";          // 是否为创建节点 是:1 否 0
	private int requestid = 0;            // 请求id
	private int workflowid = 0;           // 工作流id
	private String workflowtype = "";      // 工作流类型
	private int isremark = 0;             // 是否是批注提交 1: 是
	private int formid = 0;               // 表单或者单据的id
	private int isbill = 0;               // 是否单据 0:否 1:是
	private int billid = 0;               // 如果是单据,对应的单据表的id
	private int nodeid = 0;               // 节点id
	private String nodetype = "";          // 节点类型  0:创建 1:审批 2:实现 3:归档
	private String requestname = "";       // 请求名称
	private String requestmark = "";       // 请求的编号
	private String requestlevel = "";      // 请求重要级别 0:正常 1:重要 2:紧急
	private String remark = "";            // 签字批注
	private String clientType = "";        // 客户端类型(空为PC，1为WebClient，2为iPhone，3为iPad，4为Android)
  private int handWrittenSign;           // 手写签章对写附件ID号。
  private int speechAttachment;          // 语音附件上传附件ID号。
  private String remarkLocation;         // 签字意见添加位置信息
  private String signatureAppendfix;    // 签字意见上传附件
  private String signdocids="";           //相关文档
  private String signworkflowids="";      //相关流程
	private String messageType="";			// 短信提醒类型
	private String chatsType="";			// 微信提醒类型 (QC:98106)
	private String needwfback="";			//操作人是否需要流程反馈		TD9144

	private int nextnodeid = 0;           // 下一个节点的id
	private String nextnodetype = "";      // 下一个节点的类型
	private int lastnodeid = 0;           // 前一个节点的id
	private String lastnodetype = "";      // 前一个节点的类型
	private String status = "";            // 出口名称
	private int passedgroups = 0;         // 当前节点已经通过的组
	private int totalgroups = 0;          // 当前节点总的组
	private int creater = 0;              // 请求创建者
	private int creatertype = 0;         // 创建者类型 0:内部 1:外部
	private String createdate = "";        // 请求创建日期
	private String createtime = "";        // 请求创建时间
	private int lastoperator = 0;         // 前一个节点的操作者
	private int lastoperatortype = 0;    // 前一个节点的操作者类型 0:内部 1:外部
	private String lastoperatedate = "";   // 前一个节点的操作日期
	private String lastoperatetime = "";   // 前一个节点的操作时间
	private float nodepasstime = -1;      // 节点超时的时间 (小时)
	private float nodelefttime = -1;      // 节点剩下的时间 (小时)
	private int deleted = 0;              // 是否删除 0: 否 1: 是
	private int userid = 0;
	private int usertype = 0;
	private int userlanguage = 0;
	private int operatorgroup = 0;        // 操作者组 id
	private int typeid=0;          //判断是否为抄送
	private int groupdetailid=0;          //操作组实际id
	private String docids = "";             // 相关文档的id  (用作共享)
	private String crmids = "";             // 相关客户的id  (用作共享)
	private String hrmids = "";             // 相关人力资源的id  (用作共享)
	private String prjids = "";             // 相关项目的id  (用作共享)
	private String cptids = "";             // 相关资产的id  (用作共享)
	private String flowFrom = "";           //判断流程是否为自动为用户创建，如果是，则不用保存明细和基本信息
	private String currentdate = "";       // 当前日期
	private String currenttime = "";       // 当前时间
    private String logdate = "";       // 流转日志日期
	private String logtime = "";       // 流转日志时间
    private String billtablename = "";     // 当为单据的时候, 单据的主表的表名
    private String submitNodeId="";           //干预后流转至的节点及节点类型
    private String Intervenorid="";        //干预流转至的人
	private String IntervenoridType="";    //干预流转至的人类型
    private boolean isoracle = false;
	private Hashtable operatorsht = null;
	private char flag = Util.getSeparator();
	private String Procpara = "";
	private String sql = "";
	private int secLevel=0;
	private boolean isStart = true;	//流程是否已开始流转 by cyril
	private boolean isTrack = true; //功能开关,是否记录修改日志 by cyril
	private boolean isAgent = false; //是否代理的流程
	private int agentId = -1;
	private int sn = 0;//修改的序列
	private boolean executesuccess = true;
	private boolean islogsuccess = false;
	PoppupRemindInfoUtil poppupRemindInfoUtil = new PoppupRemindInfoUtil(true);//xwj for td3450 20060111
	private ResourceComInfo resourceComInfo=null;
	private CustomerInfoComInfo customerInfoComInfo=null;
	private SendMsgAndMail sendMsgAndMail = null;
	private int selectvalue = -1;//文件上传关联的下拉框选择的值
//	private String selectfieldid = "";//文件上传关联的下拉框字段id
	private int uploadType = 0;//附件上传目录类型
	private String selectedfieldid = "";//选中的字段id
	private String selectvaluesql = "";
	private String hasTriggeredSubwf="";//已触发子流程，防止死循环  add by fanggsh 20060706 for TD4531	
    private  final String ifchangstatus=Util.null2String(getPropValue(GCONST.getConfigFile() , "ecology.changestatus"));
    private String message="";
    private String isFromEditDocument="false"; //是否点击正文触发  true:是   false或其他：否
    private int isagentCreater=0;
    private int beagenter=0;
    private boolean CanModify=false;
    private int IsPending=-1;
    private int RequestKey=0;
    private int currentopratorInsFirstid=0;
    private String coadsigntype="2";
    private String IsBeForwardPending="";
    private String IsSubmitedOpinion="";
    private String IsBeForwardModify="";
	  private String IsBeForwardSubmit="";
    private String coadispending="";
    private String coadismodify="";
    private String isrejectremind="";
    private String rejectremindnodes="";
    private String docrowindex;
    private int temprowindex = 0;
	  private String tempsrc = "";
		private Map operator89mp = new HashMap();
	  private Map operatortype89mp = new HashMap();
	  private Map agentoperator89mp = new HashMap();
    private ArrayList operator89List = new ArrayList();//放当前找到的抄送人
    private ArrayList operatortype89List = new ArrayList();//放当前找到的抄送人的类型，0，人力资源；1，客户
    private ArrayList agentoperator89List = new ArrayList();//如果有代理，放的代理人的ID
    private Map htmlfieldMap = new HashMap();//by alan for 15769
    /**  异常流转相关参数 liuzy QC146873**/
    private boolean isNeedChooseOperator = false;	//是否允许手动选择操作者提交
    private Map<String,Object> eh_operatorMap = new HashMap<String,Object>();
    private ArrayList requestexceptiontypes = new ArrayList();
    // added by pony on 2006-05-09 for TD4264 begin
	/**  签字意见字段类型map  **/
	//private static final Map OpinionTypeMap = new HashMap(9);
    private static final Map OpinionTypeMap = new java.util.concurrent.ConcurrentHashMap(9);
	
	static {
		OpinionTypeMap.put(OpinionFieldConstant.DOCUMENT_TYPE,
				OpinionFieldConstant.DOC_NAME);
		OpinionTypeMap.put(OpinionFieldConstant.MUTI_DOCUMENT_TYPE,
				OpinionFieldConstant.MUTI_DOC_NAME);
		OpinionTypeMap.put(OpinionFieldConstant.PROJECT_TYPE,
				OpinionFieldConstant.PROJECT_NAME);
		OpinionTypeMap.put(OpinionFieldConstant.MUTI_PROJECT_TYPE,
				OpinionFieldConstant.MUTI_PROJECT_NAME);
		OpinionTypeMap.put(OpinionFieldConstant.CUSTOMER_TYPE,
				OpinionFieldConstant.CUSTOMER_NAME);
		OpinionTypeMap.put(OpinionFieldConstant.MUTI_CUSTOMER_TYPE,
				OpinionFieldConstant.MUTI_CUSTOMER_NAME);
		OpinionTypeMap.put(OpinionFieldConstant.RESOURCES_TYPE,
				OpinionFieldConstant.RESOURCES_NAME);
		OpinionTypeMap.put(OpinionFieldConstant.WORKFLOW_TYPE,
				OpinionFieldConstant.WORKFLOW_NAME);
		OpinionTypeMap.put(OpinionFieldConstant.ACCESSORIES_TYPE,
				OpinionFieldConstant.ACCESSORIES_NAME);
	}
	//added end.
    private ArrayList nextnodeids;
    private ArrayList nextlinkids;
    private ArrayList nextlinknames;
    private ArrayList nextnodetypes;
    private ArrayList operatorshts;
    private ArrayList nextnodeattrs;
    private ArrayList nextnodepassnums;
    private ArrayList linkismustpasss;
    private int nextnodeattr=0;
    private WFLinkInfo wflinkinfo;
    private boolean canflowtonextnode=false;
    private String innodeids="";
    private int SignType=0;
    private int enableIntervenor=1;//是否启用节点及出口附加操作
    private RecordSetTrans rstrans = null;//是用来将流程用的事物(rst)传到外部接口中供action调用
    boolean coadcansubmit=true;
    boolean showcoadjutant=false;
	boolean isWorkFlowToDoc=false;  //节点后附加操作是否为流程存为文档
	private int RejectToNodeid=0;//退回到指定节点  td30785
	private int RejectToType = 0 ;//退回到指定节点的类型，主要针对分叉，1：重新分叉，2：取消合并
	private int SubmitToNodeid=0; // 提交至退回节点
	private String messageid = "";//提醒信息id
	private String messagecontent = "";//提醒信息内容
	private String isFirstSubmit = "";
	
	private List requestCheckAddinRulesList=null;//节点后附加操作或出口附加规则记录表
    private Map requestCheckAddinRulesMap=null;//节点后附加操作或出口附加规则记录

    private String isAutoApprove ="0";//允许自动批准
    private String isAutoCommit = "0";//允许处理节点自动提交
    private int istest = 0;
    private boolean hasEflowToAssignNode = false;
    private Map<Integer,AutoApproveParams> nodeInfoCache = new HashMap<Integer,AutoApproveParams>(); //可以自动提交节点的信息 key nodeid
    private int nodeattribute; //当前节点节点属性
    @IgnoreProperty
	public List getRequestCheckAddinRulesList() {
		return requestCheckAddinRulesList;
	}
    
    private static boolean isExeOldFlowlogic = false;
    
    static {
        init();
    }
    
    public static final String ERROR_NOTFOUND_G = "流程基本信息不存在，因为无法在workflow_base中查询到当前请求信息。";
    public static final String ERROR_NOTFOUND_NNL = "工作流下一节点错误，因为没有找到符合条件的出口。";
    public static final String ERROR_NOTFOUND_SUBMITERROR = "工作流提交信息错误，因为提交的参数：submitNodeId不正确。";
    public static final String ERROR_NOTFOUND_NNNO = "工作流下一节点操作者错误（依次逐个处理），因为无法获取到下一个操作者。";
    
    private String oldformsignaturemd5 = "";
    
    private boolean isMakeOperateLog = true;
    
    private Map<String, Integer> newAddDetailRowPerInfo = new HashMap<String, Integer>();
    
    private boolean hasCoadjutant = false;
    
    /**
	 * 构造函数，初始化日期，得到人力资源，客户缓存
	 */
	public RequestManager() {
		Calendar today = Calendar.getInstance();
		currentdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" +
				Util.add0(today.get(Calendar.MONTH) + 1, 2) + "-" +
				Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);

		currenttime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" +
				Util.add0(today.get(Calendar.MINUTE), 2) + ":" +
				Util.add0(today.get(Calendar.SECOND), 2);
		//logdate=currentdate;
		//logtime=currenttime;
		rs = new RecordSet();
		rs1= new RecordSet();
		rs2= new RecordSet();
		rs3= new RecordSet();
		rs4= new RecordSet();
		
		//取数据库服务器的当前时间
		rs.executeProc("GetDBDateAndTime","");
		if(rs.next()){
		    currentdate = rs.getString("dbdate");
		    currenttime = rs.getString("dbtime");
		}
		logdate=currentdate;
		logtime=currenttime;
		//取数据库服务器的当前时间
		
        this.nextlinkids=new ArrayList();
        this.nextlinknames=new ArrayList();
        this.nextnodeids=new ArrayList();
        this.nextnodetypes=new ArrayList();
        this.operatorshts=new ArrayList();
        this.nextnodeattrs=new ArrayList();
        this.nextnodepassnums=new ArrayList();
        this.linkismustpasss=new ArrayList();
        isoracle = rs.getDBType().equals("oracle");
        nextnodeattr=0;
        SignType=0;
        isagentCreater=0;
        beagenter=0;
        CanModify=false;
        IsPending=-1;
        RequestKey=0;
        coadsigntype="2";
        showcoadjutant=false;
        this.operator89List = new ArrayList();
        this.agentoperator89List = new ArrayList();
        this.operatortype89List = new ArrayList();
		try {
		   sendMsgAndMail = new SendMsgAndMail();
		   resourceComInfo = new ResourceComInfo();
		   customerInfoComInfo = new CustomerInfoComInfo();
            wflinkinfo=new WFLinkInfo();
        } catch (Exception ex) {
		}
        requestCheckAddinRulesList=new ArrayList();
	}

	/**
	 * 获得提醒信息的id
	 * @return
	 */
    public String getMessageid() {
		return messageid;
	}

    /**
     * 设置提醒信息的id
     * @param messageid
     */
	public void setMessageid(String messageid) {
		this.messageid = messageid;
	}

	/**
	 * 获得提醒信息的内容
	 * @return
	 */
	public String getMessagecontent() {
		return messagecontent;
	}

	/**
	 * 设置提醒信息的内容
	 * @param messagecontent
	 */
	public void setMessagecontent(String messagecontent) {
        if(!isRequest){
            request=fu.getRequest();
        }
        if(request != null){
            HttpSession session = request.getSession(false);
            session.setAttribute("errormsg_"+user.getUID()+"_"+requestid, messagecontent);
        }
		this.messagecontent = messagecontent;
	}
	
    /**
     * 获得协办人会签关系
     * @return
     */
    public String getCoadsigntype() {
        return coadsigntype;
    }

    /**
     * 设置协办人会签关系
     * @param coadsigntype
     */
    public void setCoadsigntype(String coadsigntype) {
        this.coadsigntype = coadsigntype;
    }

    /**
     * 获得请求主键
     * @return
     */
    public int getRequestKey() {
        return RequestKey;
    }

    /**
     * 设置请求主键
     * @param requestKey
     */
    public void setRequestKey(int requestKey) {
        RequestKey = requestKey;
    }

    /**
     * 获得转发人(协办人)提交后被转发人未查看仍停留在待办
     * @return
     */
    public int getIsPending() {
        return IsPending;
    }

    /**
     * 设置转发人（协办人）提交后被转发人未查看仍停留在待办
     * @param isPending
     */
    public void setIsPending(int isPending) {
        IsPending = isPending;
    }

    /**
     * 获得是否能修改表单
     * @return
     */
    public boolean getCanModify() {
        return CanModify;
    }

    /**
     * 设置是否可修改表单
     * @param isModify
     */
    public void setCanModify(boolean isModify) {
        CanModify = isModify;
    }
    
    /**
     * 获得图形化主表计算公式关联的字段
     * @return
     */
    private ArrayList getCalfields() {
        ArrayList calfieldlist=new ArrayList();
        if(isRequest){
            if(request!=null) calfieldlist=Util.TokenizerString(Util.null2String(request.getParameter("calfields")),",");
        }else{
            if(fu!=null) calfieldlist=Util.TokenizerString(Util.null2String(fu.getParameter("calfields")),",");
        }
        return calfieldlist;
    }

    /**
     * 获得图形化明细表计算公式关联的字段
     * @return
     */
    private ArrayList getCaldetfields() {
        ArrayList caldetfieldlist=new ArrayList();
        if(isRequest){
            if(request!=null) caldetfieldlist=Util.TokenizerString(Util.null2String(request.getParameter("caldetfields")),",");
        }else{
            if(fu!=null) caldetfieldlist=Util.TokenizerString(Util.null2String(fu.getParameter("caldetfields")),",");
        }
        return caldetfieldlist;
    }

    /**
	 * 获得流程中使用的事物
	 * return RecordSetTrans rst
	 */
	public RecordSetTrans getRsTrans() {
		return rstrans;
	}	
	public void SetRsTrans(RecordSetTrans rst){
		this.rstrans = rst;
	}
    /**
     * 获得是否为代理人创建的流程
     * @return  1：代理人创建 0：本人创建
     */
    public int getIsagentCreater() {
        return isagentCreater;
    }

    /**
     * 设置是否为代理人创建的流程
     * @param isagentCreater 1：代理人创建 0：本人创建
     */
    public void setIsagentCreater(int isagentCreater) {
        this.isagentCreater = isagentCreater;
    }

    /**
     * 获得被代理人
     * @return
     */
    public int getBeAgenter() {
        return beagenter;
    }

    /**
     * 设置被代理人
     * @param beagenterid
     */
    public void setBeAgenter(int beagenterid) {
        this.beagenter = beagenterid;
    }

    /**
     * 设置流程干预会签关系
     * @param signType
     */
    public void setSignType(int signType) {
        SignType = signType;
    }

    /**
     * 获得返回错误消息号
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置返回错误消息号
     * @param message
     */
    public void setMessage(String message) {
        if(!isRequest){
            request=fu.getRequest();
        }
        if(request != null){
            HttpSession session = request.getSession(false);
            session.setAttribute("errormsgid_"+user.getUID()+"_"+requestid, message);
        }
        this.message = message;
    }

    /**
	 * 得到已触发子流程
	 * @return 已触发子流程
	 */
    public String getHasTriggeredSubwf() {
        return hasTriggeredSubwf;
    }
    /**
	 * 设置已触发子流程
	 * @param hasTriggeredSubwf 已触发子流程
	 */
    public void setHasTriggeredSubwf(String hasTriggeredSubwf) {
    	this.hasTriggeredSubwf = hasTriggeredSubwf;
    }

    /**
	 * 得到 是否来自于维护正文
	 * @return 是否来自于维护正文
	 */
    public String getIsFromEditDocument() {
        return isFromEditDocument;
    }
    /**
	 * 设置 是否来自于维护正文
	 * @param isFromEditDocument 是否来自于维护正文
	 */
    public void setIsFromEditDocument(String isFromEditDocument) {
    	this.isFromEditDocument = isFromEditDocument;
    }
    
    /**
	 * 得到用户id
	 * @return 用户id
	 */
    public int getUserId() {
        return userid;
	}
    
    /**
	 * 得到用户登录类型   0：内部用户，1：外部用户
	 * @return 用户登录类型
	 *
	 */
    public int getUserType() {
    	return usertype;
	}
	/**
	 * 得到用户对象
	 * @return 用户对象
	 */
	public User getUser() {
		return user;
	}

	/**
	 * 得到单据表名
	 * @return 单据表名
     */
	public String getBillTableName() {
        return billtablename;
	}

	/**
	 * 得到请求当前日期
	 * @return 当前日期
	 */
	public String getCurrentDate() {
		return this.currentdate;
	}
	/**
	 * 得到请求当前时间
	 * @return 当前时间
	 */
	public String getCurrentTime() {
		return this.currenttime;
	}	

    /**
	 * 设置是否是HttpServletRequest对象
	 * @param isRequest 是否是HttpServletRequest对象
	 */
    public void setIsRequest(boolean isRequest) {
    	this.isRequest = isRequest;
    }
	
   /**
    * 得到递交的节点ID
    * @return 节点ID
    */
    public String getSubmitNodeId() {
        return submitNodeId;
    }
  /**
   * 设置递交的节点ID
   * @param submitNodeId 递交的节点ID
   */
    public void setSubmitNodeId(String submitNodeId) {
        this.submitNodeId = submitNodeId;
    }
  /**
   * 得到流程干预人
   * @return 流程干预人
   */
    public String getIntervenorid() {
        return Intervenorid;
    }
   /**
    * 设置流程干预人
    * @param intervenorid 流程干预人
    */
    public void setIntervenorid(String intervenorid) {
        Intervenorid = intervenorid;
    }
	/**
   * 得到流程干预人类型
   * @return 流程干预人类型
   */
    public String getIntervenoridType() {
        return IntervenoridType;
    }
   /**
    * 设置流程干预人类型
    * @param intervenorid 流程干预人类型
    */
    public void setIntervenoridType(String IntervenoridType) {
        Intervenorid = IntervenoridType;
    }
   /**
    * 设置操作类型（递交，保存，退回等）
    * @param src 操作类型
    */
    public void setSrc(String src) {
		this.src = src;
	}
   /**
    * 设置多文档新建字段
    * @param isMultiDoc
    */
    public void setIsMultiDoc(String isMultiDoc) {
    	if(!isMultiDoc.equals(""))
    		this.isMultiDoc = isMultiDoc;
	}
    /**
     * 设置流程从何而来（是新建流程还是其他模块触发的流程）
     * @param flowFrom 流程从何而来
     */
	public void setFlowFrom(String flowFrom) {
		this.flowFrom = flowFrom;
	}
	/**
	 * 设置是否为创建节点
	 * @param iscreate 否为创建节点
	 */
	public void setIscreate(String iscreate) {
		this.iscreate = iscreate;
	}

  /**
   * 设置请求ID
   * @param requestid 请求ID
   */
	public void setRequestid(int requestid) {
		this.requestid = requestid;
	}
   /**
    * 设置工作流ID
    * @param workflowid 工作流ID
    */
	public void setWorkflowid(int workflowid) {
		this.workflowid = workflowid;
	}
    /**
     * 设置流程类型
     * @param workflowtype 流程类型
     */
	public void setWorkflowtype(String workflowtype) {
		this.workflowtype = workflowtype;
	}
  /**
   * 设置是否是批注提交 1: 是
   * @param isremark 是否是批注提交 1: 是
   */
	public void setIsremark(int isremark) {
		this.isremark = isremark;
	}
   /**
    * 设置表单ID 
    * @param formid 表单ID
    */
	public void setFormid(int formid) {
		this.formid = formid;
	}
  /**
   * 设置是否为单据
   * @param isbill 是否为单据（0/1）
   */
	public void setIsbill(int isbill) {
		this.isbill = isbill;
	}
   /**
    * 设置单据ID
    * @param billid 单据ID
    */
	public void setBillid(int billid) {
		this.billid = billid;
	}
   /**
    * 设置节点ID
    * @param nodeid 节点ID
    */
	public void setNodeid(int nodeid) {
		this.nodeid = nodeid;
	}
	   /**
	    * 设置节点类型
	    * @param nodetype 节点类型
	    */
	public void setNodetype(String nodetype) {
		this.nodetype = nodetype;
	}
   /**
    * 设置请求标题
    * @param requestname 请求标题
    */
	public void setRequestname(String requestname) {
		this.requestname = Util.StringReplace(Util.htmlFilter4UTF8(requestname),",","，");
	}
    /**
     * 设置请求紧急程度
     * @param requestlevel 紧急程度
     */
	public void setRequestlevel(String requestlevel) {
		this.requestlevel = requestlevel;
	}
   /**
    * 设置节点的当前状态
    * @param remark 当前状态
    */
	public void setRemark(String remark) {
		this.remark = remark;
	}

    /**
     * 获得签字意见相关流程id
     * @return
     */
    public String getSignworkflowids() {
        return signworkflowids;
    }

    /**
     * 设置签字意见相关流程id
     * @param signworkflowids
     */
    public void setSignworkflowids(String signworkflowids) {
        this.signworkflowids = signworkflowids;
    }

    /**
     * 获得签字意见相关文档id
     * @return
     */
    public String getSigndocids() {
        return signdocids;
    }

    /**
     * 设置签字意见相关文档id
     * @param signdocids
     */
    public void setSigndocids(String signdocids) {
        this.signdocids = signdocids;
    }

    /**
    * 设置下一个节点
    * @param nextnodeid 下一个节点
    */
	public void setNextNodeid(int nextnodeid) {
		this.nextnodeid = nextnodeid;
	}
   /**
	 * 设置下一个节点类型
	 * @param nextnodetype 下一个节点类型
	 */
	public void setNextNodetype(String nextnodetype) {
		this.nextnodetype = nextnodetype;
	}
	   /**
	    * 设置最后一个一个节点
	    * @param lastnodeid 最后一个节点ID
	    */
	public void setLastNodeid(int lastnodeid) {
		this.lastnodeid = lastnodeid;
	}
   /**
    * 设置最后一个一个节点类型
    * @param lastnodetype 后一个一个节点类型
    */
	public void setLastnodetype(String lastnodetype) {
		this.lastnodetype = lastnodetype;
	}
   /**
    * 设置请求当前状态
    * @param status 当前状态
    */
	public void setStatus(String status) {
		this.status = status;
	}
    /**
     * 设置请求当前节点已经提交的操作组数目
     * @param passedgroups 提交的操作组数目
     */
	public void setPassedGroups(int passedgroups) {
		this.passedgroups = passedgroups;
	}
	  /**
     * 设置请求当前节点总共需要提交的操作组数目
     * @param totalgroups 总共需要提交的操作组数目
     */
	public void setTotalGroups(int totalgroups) {
		this.totalgroups = totalgroups;
	}
   /**
    * 设置请求创建人
    * @param creater 请求创建人
    */
	public void setCreater(int creater) {
		this.creater = creater;
	}
   /**
    * 设置请求创建人类型
    * @param creatertype 请求创建人类型
    */
	public void setCreatertype(int creatertype) {
		this.creatertype = creatertype;
	}
   /**
    * 设置请求创建日期
    * @param createdate 创建日期
    */
	public void setCreatedate(String createdate) {
		this.createdate = createdate;
	}
    /**
     * 设置请求创建时间
     * @param createtime 创建时间
     */
	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}
   /**
    * 设置最后一次操作者
    * @param lastoperator 最后一次操作者
    */
	public void setLastoperator(int lastoperator) {
		this.lastoperator = lastoperator;
	}
  /**
   * 设置当前最后一次操作者类型
   * @param lastoperatortype 操作者类型
   */
	public void setLastoperatortype(int lastoperatortype) {
		this.lastoperatortype = lastoperatortype;
	}
   /**
    * 设置当前最后一次操作请求的日期
    * @param lastoperatedate 最后一次操作请求的日期
    */
	public void setLastoperatedate(String lastoperatedate) {
		this.lastoperatedate = lastoperatedate;
	}
   /**
    * 设置当前最后一次操作请求的时间
    * @param lastoperatetime 最后一次操作请求的时间
    */
	public void setLastoperatetime(String lastoperatetime) {
		this.lastoperatetime = lastoperatetime;
	}
   /**
    * 设置节点超时时间
    * @param nodepasstime 节点超时时间
    */
	public void setNodepasstime(float nodepasstime) {
		this.nodepasstime = nodepasstime;
	}
	/**
	 * 设置操作人是否需要流程反馈
	 * @param needwfback 0：不需要；1：需要
	 */
	public void setNeedwfback(String needwfback){
		this.needwfback = needwfback;
	}
	 /**
	   * 设置节点还有多少时间超时
	   * @param nodelefttime 还有多少时间超时
	   */
	public void setNodelefttime(float nodelefttime) {
		this.nodelefttime = nodelefttime;
	}
   /**
    * 设置从页面获取的请求
    * @param request 从页面获取的请求
    */
	public void setRequest(HttpServletRequest request) {
		this.request=request;
		this.isRequest=true;
	}
	
	public HttpServletRequest getRequest() {
		return request;
	}
	
	 /**
	    * 设置从页面获取的请求（含附件上传）
	    * @param request 从页面获取的请求
	    */
	public void setRequest(FileUpload request) {
		this.fu=request;
		this.isRequest=false;
	}
   /**
    * 设置请求当前操作用户
    * @param user 当前操作用户  @see User
    */ 
	public void setUser(User user) {
		this.user = user;
		this.userid = user.getUID();
		//下面这条语句造成流程模块的usertype与文档模块的usertype不一致！影响了功能开发，应该在单独的TD中去掉。
		this.usertype = (user.getLogintype()).equals("1") ? 0 : 1;
		this.userlanguage = user.getLanguage();
	}
  /**
   * 设置应用到的单据表名 （如果流程用的是单据）
   * @param billtablename 单据表名
   */
	public void setBilltablename(String billtablename) {
		this.billtablename = billtablename;
	}
  /**
   * 设置请求相关文挡
   * @param Docids 相关文挡
   */
	public void setDocids(String Docids) {
		this.docids = Docids;
	}
	  /**
	   * 设置请求相关客户
	   * @param Crmids 相关客户
	   */
	public void setCrmids(String Crmids) {
		this.crmids = Crmids;
	}
	  /**
	   * 设置请求相关项目
	   * @param Prjids 相关项目
	   */
	public void setPrjids(String Prjids) {
		this.prjids = Prjids;
	}
	  /**
	   * 设置请求相关人力资源
	   * @param hrmids 相关人力资源
	   */
	public void setHrmids(String hrmids) {
		this.hrmids = hrmids;
	}
	  /**
	   * 设置请求相关资产
	   * @param cptids 相关资产
	   */
	public void setCptids(String cptids) {
		this.cptids = cptids;
	}
   /**
    * 得到页面请求类型（提交，保存，退回等等）
    * @return 页面请求类型（提交，保存，退回等等）
    */
	public String getSrc() {
		return this.src;
	}
   /**
    * 得到请求的当前节点是否创建节点
    * @return 请求的当前节点是否创建节点
    */
	public String getIscreate() {
		return this.iscreate;
	}

  /**
   * 得到请求ID
   * @return 请求ID
   */
	public int getRequestid() {
		return this.requestid;
	}
   /**
    * 得到流程ID
    * @return 流程ID
    */
	public int getWorkflowid() {
		return this.workflowid;
	}
   /**
    * 得到流程类型ID
    * @return 流程类型ID
    */
	public String getWorkflowtype() {
		return this.workflowtype;
	}
   /**
    * 得到请求批注
    * @return 请求批注
    */
	public int getIsremark() {
		return this.isremark;
	}
   /**
    * 得到流程关联的表单ID
    * @return 表单ID
    */
	public int getFormid() {
		return this.formid;
	}
    /**
     * 得到流程关联的是是否是单据
     * @return 是否是单据（0/1）
     */
	public int getIsbill() {
		return this.isbill;
	}
   /**
    * 得到流程关联的单据ID（如果流程关联的是单据）
    * @return 单据ID
    */
	public int getBillid() {
		return this.billid;
	}
   /**
    * 得到请求当前的节点ID
    * @return 节点ID
    */
	public int getNodeid() {
		return this.nodeid;
	}
   /**
    * 得到请求当前节点类型
    * @return 当前节点类型
    */
	public String getNodetype() {
		return this.nodetype;
	}
   /**
    * 得到请求标题
    * @return 请求标题
    */
	public String getRequestname() {
		return this.requestname;
	}
   /**
    * 得到请求的紧急程度
    * @return 紧急程度
    */
	public String getRequestlevel() {
		return this.requestlevel;
	}
   /**
    * 得到请求的当前节点的状态（已提交，已归档等）
    * @return 节点的状态（已提交，已归档等）
    */ 
	public String getRemark() {
		return this.remark;
	}
   /**
    * 得到请求的下一个节点
    * @return 下一个节点
    */
	public int getNextNodeid() {
		return this.nextnodeid;
	}
   /**
    * 得到请求的下一个节点类型
    * @return 下一个节点类型
    */
	public String getNextNodetype() {
		return this.nextnodetype;
	}
  /**
   * 得到请求的当前最后一次操作的节点ID
   * @return 节点ID
   */
	public int getLastNodeid() {
		return this.lastnodeid;
	}
  /**
   * 得到请求的当前最后一次操作的节点类型
   * @return 节点类型
   */
	public String getLastnodetype() {
		return this.lastnodetype;
	}
   /**
	* 得到请求当前状态
	* @return 当前状态
	*/
	public String getStatus() {
		return this.status;
	}
  /**
	* 得到请求当前节点已提交的操作组数量
	* @return 操作组数量
	*/
	public int getPassedGroups() {
		return this.passedgroups;
	}
  /**
	* 得到请求当前节点总共需要提交的操作组数量
	* @return 操作组数量
	*/
	public int getTotalGroups() {
		return this.totalgroups;
	}
   /**
	* 得到请求的创建者
	* @return 创建者
	*/
	public int getCreater() {
		return this.creater;
	}
  /**
	* 得到请求的创建者类型
	* @return 创建者类型
	*/
	public int getCreatertype() {
		return this.creatertype;
	}
  /**
	* 得到请求的创建日期
	* @return 创建日期
	*/
	public String getCreatedate() {
		return this.createdate;
	}
   /**
	* 得到请求的创建时间
	* @return 创建时间
	*/
	public String getCreatetime() {
		return this.createtime;
	}
   /**
    * 得到请求当前最后一次操作的操作者
    * @return 操作者
    */
	public int getLastoperator() {
		return this.lastoperator;
	}
   /**
	 * 得到请求当前最后一次操作的操作者类型
	 * @return 操作者类型
	 */
	public int getLastoperatortype() {
		return this.lastoperatortype;
	}
	/**
	 * 得到请求当前最后一次操作日期
	 * @return 操作日期
	 */
	public String getLastoperatedate() {
		return this.lastoperatedate;
	}
	/**
	 * 得到请求当前最后一次操作时间
	 * @return 操作时间
	 */
	public String getLastoperatetime() {
		return this.lastoperatetime;
	}
	/**
	 * 得到请求当前节点的超时时间
	 * @return 超时时间
	 */
	public float getNodepasstime() {
		return this.nodepasstime;
	}
	/**
	 * 得到操作人是否需要流程反馈
	 */
	public String getNeedwfback(){
		return this.needwfback;
	}
	/**
	 * 得到请求当前节点的通过还剩下的时间（不超时）
	 * @return 剩下的时间
	 */
	public float getNodelefttime() {
		return this.nodelefttime;
	}
  /**
   * 得到下一节点的操作者
   * @return 下一节点的操作者
   */
	public Hashtable getCurrentOperator() {
		return operatorsht;
	}

	/**
	 * 得到是否需要前台选择操作者提交
	 */
	public boolean isNeedChooseOperator() {
		return isNeedChooseOperator;
	}

	/**
	 * added by cyrll on 2008-06-25
	 * 处理SQL语句的NULL问题
	 * @param s
	 * @return
	 */
	public String disposeSqlNull(String s) {
		if(s==null) 
			s = "NULL";
		else
			s = "'"+s+"'";
		return s;
	}
	/**
	 * 允许自动批准
	 * @return
	 */
   public String getIsAutoApprove() {
        return isAutoApprove;
    }

    public void setIsAutoApprove(String isAutoApprove) {
        this.isAutoApprove = isAutoApprove;
    }
    /**
     * 允许处理节点自动提交
     * @return
     */
    public String getIsAutoCommit() {
        return isAutoCommit;
    }
    public void setIsAutoCommit(String isAutoCommit) {
        this.isAutoCommit = isAutoCommit;
    }
    public int getIstest() {
		return istest;
	}
	public boolean isHasEflowToAssignNode() {
		return hasEflowToAssignNode;
	}
    public String getLogdate() {
        return logdate;
    }

    public String getLogtime() {
        return logtime;
    }
	public int getNodeattribute() {
		return nodeattribute;
	}
	public void setNodeattribute(int nodeattribute) {
		this.nodeattribute = nodeattribute;
	}
	/*
    * 保存请求的相关信息
    * description:第一步：存储基本信息（具体单据或workflow_form）,
    * 第二步：处理文档（附件）
    * 第三步：处理节点前（后）附加操作，
    * 第四步：处理明细信息
    * @return 是否保存成功
    */
	public boolean saveRequestInfo() {
	    removeErrorMsg();
		boolean fnaCostStandardFlag = true;
		boolean fnaWfValidatorFlag = true;
		
		if (isbill == 1) {
			rs.executeSql("select tablename from workflow_bill where id = " + formid); // 查询工作流单据表的信息
			if (rs.next())
				billtablename = rs.getString("tablename");          // 获得单据的主表
			else
				return false;
		}
		WfFunctionManageUtil WfFunctionManageUtil = new WfFunctionManageUtil();
		if(!WfFunctionManageUtil.haveOtherOperationRight(requestid))
		{
			return false;
		}
		if (src.equals("delete")){
			if(!WfFunctionManageUtil.IsShowDelButtonByReject(requestid,workflowid)){
				setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_04);
				return false;
			}
	    }
		
		WFCoadjutantManager wfcm=new WFCoadjutantManager();
        boolean isCoadjutantCanSubmit = false;
        rs.executeSql("select groupdetailid from workflow_currentoperator where id="+RequestKey);
        if(rs.next()){
            wfcm.getCoadjutantRights(rs.getInt("groupdetailid"));
            isCoadjutantCanSubmit = wfcm.getCoadjutantCanNextNode(requestid, Util.getIntValue(wfcm.getSigntype(),2), RequestKey, isremark);
        }
		
		/*判断当前节点是否启用子流程归档后才能提交，如果启用并且存在未归档请求则不允许向下流转*/
		boolean isAllSubWorkflowEnded = SubWorkflowTriggerService.isAllSubWorkflowEnded(this, "" + nodeid);
		if(src.equals("submit") 
			&& !isAllSubWorkflowEnded 
			&& (isremark == 0 || (isremark == 7 && isCoadjutantCanSubmit))){
			setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_05);
			JSONObject subwfinfo = new JSONObject();
            try {
                subwfinfo.put("details",SubWorkflowTriggerService.getAllSubWorkflowEndedMessage(this, "" + nodeid));
            } catch (JSONException e) {
                e.printStackTrace();
            }
			setMessagecontent(subwfinfo.toString());
			return false;
		}
		
		String result = getUpLoadTypeForSelect(workflowid);//当前工作流附件上传类型
		if (result.indexOf(",") != -1) {
		    selectedfieldid = result.substring(0,result.indexOf(","));//附件上传目录所选字段id
		    uploadType = Integer.valueOf(result.substring(result.indexOf(",")+1)).intValue();//附件上传目录类型
		}
		
		//added by cyril on 2008-07-18 for td:8835
		Map newMap = new HashMap();
		Map oldMap = new HashMap();
		Map typemap1 = new HashMap();
		//end added by cyril on 2008-07-18 for td:8835
		//qc:76256 xwz
		//背景:流程提交(创建、审批)均偶发性出现丢失的情况。原因：后台偶发性获取不到标题数据导致数据丢失。故此加强处理。
		//优点:只要创建节点提交，标题不丢失，那么直到归档也不会再丢失，如果创建丢失，那么就会生成一个系统默认的标题。
		//缺点:如果创建节点提交丢失标题，这样生成的默认标题和客户的自定义标题不符，可能引发质疑。
		requestname = requestname.trim();
        if(null == requestname || "".equals(requestname)){
			String createdate_temp = "";
			if(requestid > 0){
				rs2.executeSql("select requestname from workflow_requestbase where requestid  = "+ requestid);
				if(rs2.next()) requestname = Util.null2String(rs2.getString("requestname"));
			}else{
				createdate_temp = logdate;
				rs2.executeSql("select workflowname from workflow_base where id = "+ workflowid);
				if(rs2.next()) requestname=Util.null2String(rs2.getString("workflowname"))+"-"+user.getUsername()+"-"+createdate_temp;
			}
		}
		// 当操作类型为删除，退回，重新打开，激活（将删除的恢复） 和批注提交时， 不修改表单和单据信息
		if (src.equals("delete") || src.equals("reopen") || src.equals("active") || ((isremark == 1||isremark==7)&&!CanModify) || src.equals("supervise") || isremark == 9 || src.equals("intervenor"))
			return true;

		// 当为创建节点新建请求的时候,获得新的 requestid , 并初始化请求,如果是单据,需要先得到单据的 billid
		if (iscreate.equals("1")) {
			// 获得新的请求id , 单据id(如果是单据) 并更新请求表和单据表(如果是单据)
			//			---------- xwj for td1940 on 2005-06-02 begin -------------------
					   if(isbill == 1){
                          requestIdUpdate.setBilltablename(billtablename);
                      }
					  //requestid 和 billid 一起返回，避免同一时间提交的流程，billid出错
					  int rvalue[] = requestIdUpdate.getRequestNewId(billtablename);
					  requestid = rvalue[0];
					  if (requestid == -1) {
						  return false;
					  }
					  /*
					  rs.executeProc("workflow_RequestID_Update", "");
					  if (rs.next())
						  requestid = Util.getIntValue(rs.getString(1), 0);
					  else
						  return false;
					  */
					  //---------- xwj for td1940 on 2005-06-02 end -------------------

			    if (isbill == 1) {
				//rs.executeSql("insert into " + billtablename + "(requestid) values( " + requestid + " ) "); // 初始化主表, 每一个主表必须有requestid 字段
				//rs.executeSql("select max(id) from " + billtablename); // 获得billid , 即最大的主表id,每一个主表必须有自增长的id作为主键
				//上述方式在并发时数据不正确改为以下方式
				//billid = requestIdUpdate.getBillid();
				billid = rvalue[1];
				if (billid==-1)	
		          return false;
			}

			executesuccess = rs.executeSql("insert into workflow_form (requestid,billformid,billid) values(" + requestid + "," + formid + "," + billid + ")"); // 初始化请求信息表
			if (!executesuccess) {
			    this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
			    return false;
			}

			// 初始化请求基本信息和操作者基本信息
			createdate = currentdate;
			createtime = currenttime;
			lastnodeid = nodeid;
			lastnodetype = nodetype;
			status = "";
			passedgroups = 0;
			totalgroups = 0;
			lastoperator = 0;
			lastoperatortype = 0;
			lastoperatedate = "";
			lastoperatetime = "";
			deleted = 0;
			nodepasstime = -1;
			nodelefttime = -1;
			isremark = 0;          // 当前操作者
			operatorgroup = 0;
			groupdetailid=0;   //创建节点设为0，不用取得实际id
			creater = userid;
			creatertype = usertype;
            if(isagentCreater==1){
                if(beagenter<1){
                rs.executeSql("select bagentuid from workflow_agentConditionSet where iscreateagenter=1 and agentuid="+userid+" and workflowid="+workflowid+" order by agentbatch desc  ,id desc");
                if(rs.next()){
                    creater=rs.getInt(1);
                    creatertype=0;
                }
                }else{
                    creater=beagenter;
                    creatertype=0;
                }
            }

			// 加入请求基本信息
			//modify by xhheng @ 2005/01/24 for 消息提醒 Request06
            /********/
			if(isoracle){
				Procpara = "" + requestid + flag + workflowid + flag + lastnodeid + flag + lastnodetype + flag
						+ nodeid + flag + nodetype + flag + status + flag + passedgroups + flag + totalgroups
						+ flag + requestname + flag + creater + flag + createdate + flag + createtime + flag
						+ lastoperator + flag + lastoperatedate + flag + lastoperatetime + flag + deleted + flag
						+ creatertype + flag + lastoperatortype + flag + nodepasstime + flag + nodelefttime + flag
						+ docids + flag + crmids + flag + "empty_clob()" + flag + prjids + flag + cptids + flag + messageType + flag + chatsType;//微信提醒(QC:98106)
            }else{
            	Procpara = "" +requestid + flag + workflowid + flag + lastnodeid + flag + lastnodetype + flag
					+ nodeid + flag + nodetype + flag + status + flag + passedgroups + flag + totalgroups
					+ flag + requestname + flag + creater + flag + createdate + flag + createtime + flag
					+ lastoperator + flag + lastoperatedate + flag + lastoperatetime + flag + deleted + flag
					+ creatertype + flag + lastoperatortype + flag + nodepasstime + flag + nodelefttime + flag
					+ docids + flag + crmids + flag + hrmids + flag + prjids + flag + cptids + flag + messageType + flag + chatsType;//微信提醒(QC:98106)
            }
			/********/
			executesuccess = rs.executeProc("workflow_requestbase_insertnew", Procpara);
			
			//为何insett和update要分2步做？add by ben 2006-03-30
			rs.executeProc("workflow_Rbase_UpdateLevel", "" + requestid + flag + requestlevel);
			if (!executesuccess) {
	             this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
			    return false;
			}

			/* ---- xwj for td2104 on 20050802 begin---- */
			if(isOldOrNewFlag(requestid)){//老数据 相对td2104之前
				Procpara = "" + requestid + flag + userid + flag + operatorgroup + flag + workflowid + flag
								+ workflowtype + flag + creatertype + flag + isremark + flag + -1 +
				flag + -1 + flag + "0" + flag + -1+flag + groupdetailid+flag+this.currentdate+flag+this.currenttime;
			executesuccess = rs.executeProc("workflow_CurrentOperator_I2", Procpara);
			}
			else{
                if(isagentCreater==1&&beagenter>0){
                    Procpara = "" + requestid + flag + userid + flag + operatorgroup + flag + workflowid + flag
							+ workflowtype + flag + creatertype + flag + isremark + flag + lastnodeid + flag + beagenter
                            + flag + "2" + flag + -1 + flag + groupdetailid+flag+this.currentdate+flag+this.currenttime;
                    executesuccess = rs.executeProc("workflow_CurrentOperator_I2", Procpara);
                    Procpara = "" + requestid + flag + beagenter + flag + operatorgroup + flag + workflowid + flag
					+ workflowtype + flag + creatertype + flag + "2" + flag + lastnodeid + flag + userid
                    + flag + "1" + flag + -1 + flag + groupdetailid+flag+this.currentdate+flag+this.currenttime;
            executesuccess = rs.executeProc("workflow_CurrentOperator_I2", Procpara);
                }else{
                    Procpara = "" + requestid + flag + userid + flag + operatorgroup + flag + workflowid + flag
							+ workflowtype + flag + creatertype + flag + isremark + flag + lastnodeid + flag + -1
                            + flag + "0" + flag + -1 + flag + groupdetailid+flag+this.currentdate+flag+this.currenttime;
                    executesuccess = rs.executeProc("workflow_CurrentOperator_I2", Procpara);
                }
			}
			/* ---- xwj for td2104 on 20050802 end---- */

			if (!executesuccess) return false;
            rs.executeSql("delete from workflow_nownode where requestid="+requestid);
            rs.executeSql("insert into workflow_nownode(requestid,nownodeid,nownodetype,nownodeattribute) values("+requestid+","+lastnodeid+",0,0)");
		}else{
            // 查询当前请求的一些基本信息
			rs.executeProc("workflow_Requestbase_SByID", requestid + "");
			if (rs.next()) {
				lastnodeid = Util.getIntValue(rs.getString("lastnodeid"), 0);
				lastnodetype = Util.null2String(rs.getString("lastnodetype"));
				passedgroups = Util.getIntValue(rs.getString("passedgroups"), 0);
				totalgroups = Util.getIntValue(rs.getString("totalgroups"), 0);
				creater = Util.getIntValue(rs.getString("creater"), 0);
				creatertype = Util.getIntValue(rs.getString("creatertype"), 0);
				requestmark = Util.null2String(rs.getString("requestmark"));
			}
			if(isbill==1 && billid<=0){
				getBillId();
			}
		}

        if(!"1".equals(this.iscreate)){//其它节点也可以修改紧急程度和是否短信提醒，同一节点以最后一个有效操作者的修改提交到下一个节点。创建节点不重复更新。
            //add by mackjoe at 2008-02-21 for TD8232
            //检查是否是当前节点操作人 避免同一节点操作人重复提交
            int sleepsum=0;
            while(true){
                if(GCONST.WFProcessing.indexOf(requestid+"_"+nodeid)==-1){
                    break;
                }else{
                    try{
                        Thread.sleep(1000);   //等待1秒钟
                        sleepsum++;
                        if(sleepsum>1000){
                            GCONST.WFProcessing.remove(requestid+"_"+nodeid);
                            this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_03);  //等待1000秒后流程流转失败,提示：流程流转超时，请重试。
                            return false;
                        }
                    }catch(Exception e) {e.printStackTrace();}
                }
            }
            if(!src.equals("supervise")&&!src.equals("intervenor")){
                if(isremark!=1&&isremark!=9){
                    rs.executeSql("select 1 from workflow_currentoperator where isremark not in('2','4') and (takisremark<>-2 or takisremark is null) and requestid="+requestid+" and userid="+userid+" and nodeid="+nodeid);
                    if(rs.getCounts()<1){
                        this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_02); //已经流转到下一节点，不可以再提交
                        return false;
                    }
                }
            }
            //end by mackjoe
            
            if(!"true".equals(isFromEditDocument)){
                RequestSaveCheckManager requestSaveCheckManager=new RequestSaveCheckManager();
                String returnMessage=requestSaveCheckManager.getReturnMessage(this,this.isRequest,this.fu,this.request);
                if(!returnMessage.equals("")){
                    setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_04);
                    saveRequestLog("1");
                    return false;            	
                }             	
            }            
            
            //added by cyril on 2008-07-18 for td:8835
            rs.execute("select requestname,requestlevel,messageType from workflow_requestbase where requestid="+requestid);
    		if(rs.next()){
	            //标题\短信\级别纳入
				for(int i=0; i<3; i++) {
					Track t = new Track();
					t.setFieldId(-1);
					t.setFieldType("1");		/*存浏览按钮对应位置*/
					t.setFieldHtmlType("1");	/*存浏览按钮类型*/
					t.setFieldId(-1);				/*将ID也取得*/
					t.setNodeId(nodeid);	/*节点ID*/
					t.setRequestId(requestid);	/*请求ID*/
					t.setIsBill(1);	/*是否为表单*/
					t.setModifierIP(this.getIp());	/*IP地址*/
					t.setOptKind(src);	/*日志操作类型*/
					if(i==0 && !"".equals(this.requestname)) {
						t.setFieldName("requestname");
						t.setFieldOldText(rs.getString("requestname"));
						t.setFieldLableId(229);/*单据对应的LEABLE*/
						oldMap.put(t.getFieldName(), t);
						newMap.put("requestname", this.requestname);
					}
					else if(i==1 && !"".equals(this.requestlevel)) {
						t.setFieldName("requestlevel");
						t.setFieldOldText(rs.getString("requestlevel"));
						t.setFieldLableId(15534);/*单据对应的LEABLE*/
						oldMap.put(t.getFieldName(), t);
						newMap.put("requestlevel", this.requestlevel);
					}
					else if(i==2 && !"".equals(this.messageType)) {
						t.setFieldName("messageType");
						t.setFieldOldText(rs.getString("messageType"));
						t.setFieldLableId(17586);/*单据对应的LEABLE*/
						oldMap.put(t.getFieldName(), t);
						newMap.put("messageType", this.messageType);
					}
					
				}
				//end 标题\短信\级别纳入
    		}
            //end by cyril
            if((!"".equals(this.requestlevel))&&(!"".equals(this.messageType))){
        		rs.executeSql("update workflow_requestbase set messageType="+Util.getIntValue(this.messageType,-1)+",requestLevel="+Util.getIntValue(this.requestlevel,-1)+" where  requestid="+this.requestid);
        	}else if((!"".equals(this.requestlevel))&&"".equals(this.messageType)){
        		rs.executeSql("update workflow_requestbase set requestLevel="+Util.getIntValue(this.requestlevel,-1)+" where  requestid="+this.requestid);       		
        	}else if("".equals(this.requestlevel)&&(!"".equals(this.messageType))){
        		rs.executeSql("update workflow_requestbase set messageType="+Util.getIntValue(this.messageType,-1)  +" where  requestid="+this.requestid);        		
        	}
            //流程标题没有更新，执行dml时，无法获取当先表单对应流程标题
            if(!"".equals(this.requestname) && this.requestname !=null){
            	rs.executeSql("update workflow_requestbase set requestname='" + Util.fromScreen2(requestname, userlanguage) + "' where  requestid="+this.requestid);  
            }
            //微信提醒(QC:98106)
            if(!"".equals(this.chatsType)){ 
        		rs.executeSql("update workflow_requestbase set chatsType="+Util.getIntValue(this.chatsType,-1)+" where  requestid="+this.requestid); 
            }
        }
		
        //查询当前流程是否跟随文档关联人赋权
		int docRightByOperator=0;
		rs.execute("select docRightByOperator from workflow_base where id="+workflowid);
		if(rs.next()){
			docRightByOperator=Util.getIntValue(rs.getString("docRightByOperator"),0);
		}
        if (flowFrom.equals("")) //如果流程不是由流程计划自动创建进行以下步骤
        {
		// 开始更新请求信息表, 对于表单,为 workflow_form , 对于单据, 为 billtablename 所指定的表
		String updateclause = "";
		String fieldid = "";
		String fieldname = "";
		String fielddbtype = "";
		String fieldhtmltype = "";
		String fieldtype = "";
		String fieldlable = "";
		FieldComInfo fieldComInfo = new FieldComInfo();
		Map locationMap = new HashMap();

		//added begin for td4527
		if(isbill == 1){
			selectvaluesql = "select * from workflow_billfield where billid="+formid+" order by dsporder";
		}else{
			selectvaluesql = "select fieldid,fieldorder,isdetail from workflow_formfield " +
					"where formid="+formid+" and (isdetail<>'1' or isdetail is null) order by fieldid  ";
		}
		rs4.executeSql(selectvaluesql);
		while(rs4.next()){
			if (isbill == 1) {
				String viewtype = Util.null2String(rs4.getString("viewtype"));// 如果是单据的从表字段,不进行操作
				if (viewtype.equals("1")) continue;
				fieldid = Util.null2String(rs4.getString("id"));
				fieldhtmltype = Util.null2String(rs4.getString("fieldhtmltype"));
			} else {
				fieldid = Util.null2String(rs4.getString(1));
				fieldhtmltype = Util.null2String(fieldComInfo.getFieldhtmltype(fieldid));
			}
			if("5".equals(fieldhtmltype)){
				if(selectedfieldid.equals(fieldid)){
					if(isRequest){
						selectvalue = Util.getIntValue(request.getParameter("field" + fieldid), 0);
					}else{
						selectvalue = Util.getIntValue(fu.getParameter("field" + fieldid), 0);
					}
				}
			}
		}
		//added end.
		
		/*
		 * added by cyril on 2008-06-23 for TD:8835
		 * 功能:流程修改痕迹
		 */
		StringBuffer s = new StringBuffer();
		String fieldsSql = "";//查询字段名称用的
		String billFieldsSql = "";//查询单据字段名称用
		
		//检查操作人是否为被代理人
		s.append("select agentorbyagentid from workflow_currentoperator where agentorbyagentid<>"+userid+" and requestid="+requestid+" and nodeid="+nodeid);
		executesuccess = rs.executeSql(s.toString());
		if (!executesuccess) {
			writeLog(s.toString());
			saveRequestLog("1");
			return false;
		}
		if(rs.next()) {
			if(rs.getInt("agentorbyagentid")!=-1 && userid!=rs.getInt("agentorbyagentid")) {
				isAgent = true;
				agentId = rs.getInt("agentorbyagentid");
			}
		}
				
		//检查流程是否是在流转过程中,是否记录表单日志
		s = new StringBuffer();
		s.append("select t1.ismodifylog, t2.status from workflow_base t1, workflow_requestbase t2 where t1.id=t2.workflowid and t2.requestid="+requestid);
		executesuccess = rs.executeSql(s.toString());
		if (!executesuccess) {
			writeLog(s.toString());
			saveRequestLog("1");
			return false;
		}
		if(rs.next()) {
			isTrack = (rs.getString("ismodifylog")!=null && "1".equals(rs.getString("ismodifylog")));
			isStart = (rs.getString("status")!=null && !"".equals(rs.getString("status")));
			//System.out.println("isStart="+isStart);
		}
		
		if(isTrack) {			
			if(!"1".equals(this.iscreate) && isStart) {
				//取得原有字段
				List fields = this.getFieldsName();
				if(fields!=null && fields.size()>0){
					//取得原记录
					s = new StringBuffer();
					s.append("select ");
					for(int i=0; i<fields.size(); i++) {
						Track t = (Track) fields.get(i);
						s.append(t.getFieldName());//只取出字段名
						if(i<(fields.size()-1)){
							s.append(",");
						}
					}
					if (isbill == 1) {
						s.append(" from " + billtablename + " where id = " + billid);
					} else {
						s.append(" from workflow_form where requestid="+requestid);
					}
					//System.out.println("sql="+s.toString());
					executesuccess = rs.executeSql(s.toString());
					if (!executesuccess) {
						writeLog(s.toString());
						saveRequestLog("1");
						this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_01);
						return false;
					}
					if(rs.next()) {
						for(int i=0; i<fields.size(); i++) {
							Track t = (Track) fields.get(i);
							t.setFieldOldText(rs.getString(t.getFieldName()));
							oldMap.put(t.getFieldName(), t);
						}
					}
				}
			}
		}
		/*
		 * end by cyril on 2008-06-23 for TD:8835
		 */
		
		if (isbill == 1) {
			executesuccess = rs.executeProc("workflow_billfield_Select", formid + "");
		} else {
			executesuccess = rs.executeSql("select t2.fieldid,t2.fieldorder,t2.isdetail,t1.fieldlable,t1.langurageid from workflow_fieldlable t1,workflow_formfield t2 where t1.formid=t2.formid and t1.fieldid=t2.fieldid and (t2.isdetail<>'1' or t2.isdetail is null)  and t2.formid="+formid+"  and t1.langurageid="+userlanguage+" order by t2.fieldorder");
		}		
		
		if (!executesuccess) {
			saveRequestLog("1");
			this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_01);
			return false;
		}
        FieldInfo fieldinfo=new FieldInfo();
        ArrayList editfields=new ArrayList();
        ArrayList detaileditfields=new ArrayList();
        if(!"1".equals(iscreate)){
            editfields=fieldinfo.getSaveMainFields(formid,isbill,workflowid,nodeid,getCalfields());
            detaileditfields=fieldinfo.getSaveDetailFields(formid,isbill,workflowid,nodeid,getCaldetfields());
            //Html模式-新表单设计器-公式赋值字段添加
            new ExcelLayoutManager().manageFormulaDestFile(workflowid, nodeid, 0, editfields, detaileditfields);
        }
        Map<String,String> fieldTypeCache = new HashMap<String,String>();
		while (rs.next()) {
			if (isbill == 1) {
				String viewtype = Util.null2String(rs.getString("viewtype"));   // 如果是单据的从表字段,不进行操作
				if (viewtype.equals("1")) continue;
				fieldid = Util.null2String(rs.getString("id"));
				fieldname = Util.null2String(rs.getString("fieldname"));
				fielddbtype = Util.null2String(rs.getString("fielddbtype"));
				fieldhtmltype = Util.null2String(rs.getString("fieldhtmltype"));
				fieldtype = Util.null2String(rs.getString("type"));
				fieldlable = Util.null2String(rs.getString("fieldlable"));
			} else {
				fieldid = Util.null2String(rs.getString(1));
				fieldname = Util.null2String(fieldComInfo.getFieldname(fieldid));
				fielddbtype = Util.null2String(fieldComInfo.getFielddbtype(fieldid));
				fieldhtmltype = Util.null2String(fieldComInfo.getFieldhtmltype(fieldid));
				fieldtype = Util.null2String(fieldComInfo.getFieldType(fieldid));
			}
			
			if(isbill != 1 && StringUtil.isNull(fieldname)){
			    RecordSet rstemp = new RecordSet();
			    String deleteDatasql = "delete from workflow_formfield where formid = "+formid+" and fieldid = " +fieldid;
			    rstemp.execute(deleteDatasql);
			    this.writeLog(deleteDatasql);
			    continue;
			}

            if (fieldhtmltype.equals("3") && (fieldtype.equals("1") || fieldtype.equals("17"))) { // 人力资源字段
				String tempvalueid="";
				if(isRequest)
					tempvalueid = Util.null2String(request.getParameter("field" + fieldid));
				else
					tempvalueid = Util.null2String(fu.getParameter("field" + fieldid));
				if (!tempvalueid.equals("")) hrmids += "," + tempvalueid;
				//多人资源特殊处理
				if (fieldtype.equals("17")) {
				    try {
				        WFPathBrowserUtil.updateBrowInfo(fu, requestid, fieldid);
				    } catch (Exception e9) {
				        e9.printStackTrace();
				    }
				}
			} else if (fieldhtmltype.equals("3") && (fieldtype.equals("7") || fieldtype.equals("18"))) {   // 客户字段
				String tempvalueid ="";
				if(isRequest)
					tempvalueid = Util.null2String(request.getParameter("field" + fieldid));
				else
					tempvalueid = Util.null2String(fu.getParameter("field" + fieldid));
				
				//过滤没有权限的客户
				tempvalueid = resourceAuthorityFilter(fieldhtmltype, fieldtype, tempvalueid);
				if (!tempvalueid.equals("")) crmids += "," + tempvalueid;
			} else if (fieldhtmltype.equals("3") && (fieldtype.equals("8")|| fieldtype.equals("135"))) {                             // 项目字段
				String tempvalueid ="";
				if(isRequest)
					tempvalueid = Util.null2String(request.getParameter("field" + fieldid));
				else
					tempvalueid = Util.null2String(fu.getParameter("field" + fieldid));
				//过滤没有权限的项目
				tempvalueid = resourceAuthorityFilter(fieldhtmltype, fieldtype, tempvalueid);
				if (!tempvalueid.equals("")) prjids += "," + tempvalueid;
			} else if (fieldhtmltype.equals("3") && (fieldtype.equals("9") || fieldtype.equals("37"))) {  // 文档字段
				String tempvalueid ="";
				if(isRequest)
					tempvalueid = Util.null2String(request.getParameter("field" + fieldid));
				else
					tempvalueid = Util.null2String(fu.getParameter("field" + fieldid));
				//过滤没有权限的文档
				tempvalueid = resourceAuthorityFilter(fieldhtmltype, fieldtype, tempvalueid);
				if (!tempvalueid.equals("")) docids += "," + tempvalueid;
				//跟随文档关联人赋权
				if(docRightByOperator==1){
					//在Workflow_DocSource表中删除当前字段被删除的文档
					if (!tempvalueid.equals(""))
					{
					rs1.execute("delete from Workflow_DocSource where requestid =" + requestid + " and fieldid =" + fieldid + " and docid not in (" + tempvalueid + ")");
					}
					else
					{
					rs1.execute("delete from Workflow_DocSource where requestid =" + requestid + " and fieldid =" + fieldid);
					}
					//在Workflow_DocSource表中添加当前字段新增加的文档
					String[] mdocid=Util.TokenizerString2(tempvalueid,",");
					for(int i=0;i<mdocid.length; i++){
						if(mdocid[i]!=null && !mdocid[i].equals("")){
							rs1.executeProc("Workflow_DocSource_Insert",""+requestid + flag + nodeid + flag + fieldid + flag + mdocid[i] + flag + userid + flag + "1");//由于usertype不一致，这里的usertype直接指定为1，只处理内部用户
						}
					}
				}
			} else if (fieldhtmltype.equals("3") && fieldtype.equals("23")) {                           // 资产字段
				String tempvalueid ="";
				if(isRequest)
					tempvalueid = Util.null2String(request.getParameter("field" + fieldid));
				else
					tempvalueid = Util.null2String(fu.getParameter("field" + fieldid));
				//过滤没有权限的资产
				tempvalueid = resourceAuthorityFilter(fieldhtmltype, fieldtype, tempvalueid);
				if (!tempvalueid.equals("")) cptids += "," + tempvalueid;
			}
            //处理节点如果不可编辑择跳过
            if(!"1".equals(iscreate)&&editfields.indexOf(fieldid)<0 && !fieldhtmltype.equals("9")) continue;
            

			
			if (isoracle) {
				if ((fielddbtype.toUpperCase()).indexOf("INT") >= 0&&!"224".equals(fieldtype)&&!"225".equals(fieldtype))
					
				/*------modified by xwj for td3297 20051130 --- begin --*/
					           
								if("5".equals(fieldhtmltype)){
								if(isRequest){
									 if (!(Util.null2String(request.getParameter("field" + fieldid))).equals("")) {
										 if(null == newMap.get(fieldname))
											 updateclause += fieldname + " = " + Util.getIntValue(request.getParameter("field" + fieldid), -1) + ",";
										 newMap.put(fieldname, String.valueOf(Util.getIntValue(request.getParameter("field" + fieldid), -1)));//填入Map中,用于比较 by cyril
									 }
                                     else {
                                    	 if(null == newMap.get(fieldname))
                                    		 updateclause += fieldname + " = NULL,";
                                    	 newMap.put(fieldname, null);//填入Map中,用于比较 by cyril
                                     }
                                }else{
									 if (!(Util.null2String(fu.getParameter("field" + fieldid))).equals("")) {
										 if(null == newMap.get(fieldname))
											 updateclause += fieldname + " = " + Util.getIntValue(fu.getParameter("field" + fieldid), -1) + ",";
										 newMap.put(fieldname, String.valueOf(Util.getIntValue(fu.getParameter("field" + fieldid), -1)));//填入Map中,用于比较 by cyril
									 }
                                     else {
                                    	 if(null == newMap.get(fieldname))
                                    		 updateclause += fieldname + " = NULL,";
                                    	 newMap.put(fieldname, null);//填入Map中,用于比较 by cyril
                                     }
                                }
								}
								else{
									if(isRequest){
									  //权限过滤
			                            String tempvalueid = resourceAuthorityFilter(fieldhtmltype, fieldtype, Util.null2String(request.getParameter("field" + fieldid)));
										 if (!(Util.null2String(tempvalueid)).equals("")) {
											 if(null == newMap.get(fieldname))
												 updateclause += fieldname + " = " + Util.getIntValue(tempvalueid, 0) + ",";
											 newMap.put(fieldname, String.valueOf(Util.getIntValue(tempvalueid, 0)));//填入Map中,用于比较 by cyril
										 }
                                         else {
                                        	 if(null == newMap.get(fieldname))
                                        		 updateclause += fieldname + " = NULL,";
                                        	 newMap.put(fieldname, null);//填入Map中,用于比较 by cyril
                                         }
                                    }else{
                                        String tempvalueid = resourceAuthorityFilter(fieldhtmltype, fieldtype, Util.null2String(fu.getParameter("field" + fieldid)));
										 if (!(Util.null2String(tempvalueid)).equals("")) {
											 if(null == newMap.get(fieldname))
												 updateclause += fieldname + " = " + Util.getIntValue(tempvalueid, 0) + ",";
											 newMap.put(fieldname, String.valueOf(Util.getIntValue(tempvalueid, 0)));//填入Map中,用于比较 by cyril
										 }
                                         else {
                                        	 if(null == newMap.get(fieldname))
                                        		 updateclause += fieldname + " = NULL,";
                                        	 newMap.put(fieldname, null);//填入Map中,用于比较 by cyril
                                         }
                                    }

								}
							/*------modified by xwj for td3297 20051130 --- end  --*/
				else if (((fielddbtype.toUpperCase()).indexOf("NUMBER") >= 0||(fielddbtype.toUpperCase()).indexOf("FLOAT") >= 0)&&!"224".equals(fieldtype)&&!"225".equals(fieldtype)){
					int digitsIndex = fielddbtype.indexOf(",");
					int decimaldigits = 2;
		        	if(digitsIndex > -1){
		        		decimaldigits = Util.getIntValue(fielddbtype.substring(digitsIndex+1, fielddbtype.length()-1).trim(), 2);
		        	}else{
		        		decimaldigits = 2;
		        	}
					if(isRequest){
						 if (!(Util.null2String(request.getParameter("field" + fieldid))).equals("")) {
							 if(null == newMap.get(fieldname))
								 updateclause += fieldname + " = " + Util.getPointValue2(request.getParameter("field" + fieldid),decimaldigits) + ",";
							 newMap.put(fieldname, Util.getPointValue2(request.getParameter("field" + fieldid),decimaldigits));//填入Map中,用于比较 by cyril
						 }
                         else {
                        	 if(null == newMap.get(fieldname))
                        		 updateclause += fieldname + " = NULL,";
                        	 newMap.put(fieldname, null);//填入Map中,用于比较 by cyril
                         }
                    }
						else {
							if (!(Util.null2String(fu.getParameter("field" + fieldid))).equals("")) {
								if(null == newMap.get(fieldname))
									updateclause += fieldname + " = " + Util.getPointValue2(fu.getParameter("field" + fieldid),decimaldigits) + ",";
								newMap.put(fieldname, Util.getPointValue2(fu.getParameter("field" + fieldid),decimaldigits));//填入Map中,用于比较 by cyril
							}
	                        else {
	                        	if(null == newMap.get(fieldname))
	                        		updateclause += fieldname + " = NULL,";
	                            newMap.put(fieldname, null);//填入Map中,用于比较 by cyril
	                        }
						}
			}else if(fieldhtmltype.equals("3") && "17".equals(fieldtype)){
				if(isRequest){
					if(null == newMap.get(fieldname))
						updateclause += fieldname + " = '' ,";
					//newMap.put(fieldname, "empty_clob()");//填入Map中,用于比较 by cyril
					typemap1.put(fieldname, Util.null2String(request.getParameter("field" + fieldid)));//填入Map中,用于比较 by cyril
               }else {
            	    if(null == newMap.get(fieldname))
            		   updateclause += fieldname + " = '' ,";
					//newMap.put(fieldname, "empty_clob()");//填入Map中,用于比较 by cyril
					typemap1.put(fieldname, Util.null2String(fu.getParameter("field" + fieldid)));//填入Map中,用于比较 by cyril
				}
			}else if(fieldhtmltype.equals("6")){
					//add by xhheng @20050315 for 附件上传
					String tempvalue="";

					//处理附件
					if(isRequest)
						tempvalue=Util.null2String(request.getParameter("field" + fieldid));
					else
						tempvalue=Util.null2String(fu.getParameter("field" + fieldid));

			DocExtUtil docExtUtil = new DocExtUtil();
			String[] oldUploadIdsStrs = null;
			String oldUploadIdsStrs_ = "";
			RecordSet rsUploadId = new RecordSet();
			if (isbill == 1) {
				rsUploadId.executeSql("select "+fieldname+" from "+ billtablename +" where requestid = " + requestid);
			}else{
				rsUploadId.executeSql("select "+fieldname+" from workflow_form where requestid = " + requestid);
			}
			if(rsUploadId.next()){
				oldUploadIdsStrs_ = rsUploadId.getString(fieldname);
			}
			if(oldUploadIdsStrs_ != null && !"".equals(oldUploadIdsStrs_)){
			oldUploadIdsStrs = Util.TokenizerString2(oldUploadIdsStrs_, ",");
			}
			if(!tempvalue.equals("")){
				if(null == newMap.get(fieldname))
					updateclause += fieldname + " = '" + fillFullNull(tempvalue) + "',";
				newMap.put(fieldname, tempvalue);//填入Map中,用于比较 by cyril
			}

			else if(tempvalue.equals("")){
				if(null == newMap.get(fieldname))
					updateclause += fieldname + " = '',";
				newMap.put(fieldname, null);//填入Map中,用于比较 by cyril
			}
			else{
			}
			if(oldUploadIdsStrs != null){
			for( int y =0; y < oldUploadIdsStrs.length; y++){
				if(tempvalue.indexOf(oldUploadIdsStrs[y]) == -1){
					//if( Util.getIntValue(oldUploadIdsStrs[y],0) != 0 )
					//docExtUtil.deleteDoc(Integer.parseInt(oldUploadIdsStrs[y]));
					if( Util.getIntValue(oldUploadIdsStrs[y],0) != 0 ){
						String clientIp = "";
						if(isRequest){
							if (request != null)
								clientIp = Util.null2String(request.getRemoteAddr());
						}else{
							if (fu != null)
								clientIp = Util.null2String(fu.getRemoteAddr());
						}						
						docExtUtil.deleteDoc(Integer.parseInt(oldUploadIdsStrs[y]),user,clientIp);							
					}					
				}
			}
			}
			/* ----- added by xwj for td1949 end ------*/

			}          
			else if (fieldhtmltype.equals("9")){  //如果为pc端提交 地址为空
			    String preData = "";
			    String tempvalue = "";
                if (isRequest){
                    preData = Util.null2String(request.getParameter("field" + fieldid));
                }else{
                    preData = Util.null2String(fu.getParameter("field" + fieldid));
                }
                if(preData.equals("")){
                    tempvalue = nodeid + LocateUtil.SPLIT_FIELD + userid + LocateUtil.SPLIT_FIELD+"0"+LocateUtil.SPLIT_FIELD+""
                        +LocateUtil.SPLIT_FIELD +"0"+ LocateUtil.SPLIT_FIELD+"0";                    
                }else{
                    tempvalue = preData + LocateUtil.SPLIT_LOCATION + nodeid + LocateUtil.SPLIT_FIELD + userid + LocateUtil.SPLIT_FIELD+"0"+LocateUtil.SPLIT_FIELD+""
                        +LocateUtil.SPLIT_FIELD +"0"+ LocateUtil.SPLIT_FIELD+"0";
                }
                locationMap.put(fieldname, tempvalue);
                newMap.put(fieldname, tempvalue);
			}else{
					//modify by xhheng @ 20041229 for TD 1495
					//以"是否是用户输入"作为是否作html转换的标准，故当为系统默认提醒流程时，不做html转换
					boolean ishtml = false;
					String thetempvalue ="";
					if(workflowid==1){
						if(isRequest){
							//thetempvalue = Util.toScreen(request.getParameter("field" + fieldid), userlanguage);
							thetempvalue = Util.toHtml100(request.getParameter("field" + fieldid));							
						}else{
							//thetempvalue = Util.toScreen(fu.getParameter("field" + fieldid), userlanguage);
							thetempvalue = Util.toHtml100(fu.getParameter("field" + fieldid));							
						}
					}else{
						if(fieldhtmltype.equals("3")&&(fieldtype.equals("161") || fieldtype.equals("162"))){
							if (isRequest){
                               thetempvalue = Util.null2String(request.getParameter("field" + fieldid));
                           }else{
                               thetempvalue = Util.null2String(fu.getParameter("field" + fieldid));
						   }
                           thetempvalue = thetempvalue.trim();
						} else if(fieldhtmltype.equals("3")&&(fieldtype.equals("224") || fieldtype.equals("225")|| fieldtype.equals("226")|| fieldtype.equals("227"))){
								//获取流程主表单的数据，并且插入到主表单
							   if (isRequest){
	                               thetempvalue = Util.null2String(request.getParameter("field" + fieldid));
	                           }else{
	                               thetempvalue = Util.null2String(fu.getParameter("field" + fieldid));
							   }
	                           thetempvalue = thetempvalue.trim();
	                           //System.out.println("总算可以了"+thetempvalue);
						} else{
						if(isRequest) {
							//thetempvalue = Util.StringReplace(Util.toHtml10(request.getParameter("field" + fieldid))," ","&nbsp;");
							//thetempvalue = Util.toHtmlForWorkflow(request.getParameter("field" + fieldid));							
						 if (fieldhtmltype.equals("2")&&fieldtype.equals("2"))
						   {
							thetempvalue = Util.toHtml100(request.getParameter("field" + fieldid));
							thetempvalue = rePlaceWordMark(thetempvalue);
							ishtml = true;
						   }else if(fieldhtmltype.equals("1")&&fieldtype.equals("1")){
							   thetempvalue = Util.toHtmlForWorkflow(request.getParameter("field" + fieldid));
						   }else{
							   thetempvalue = Util.StringReplace(Util.toHtml10(request.getParameter("field" + fieldid))," ","&nbsp;");
						   }
							}
						else
						{
							//thetempvalue = Util.StringReplace(Util.toHtml10(fu.getParameter("field" + fieldid))," ","&nbsp;");
							//thetempvalue = Util.toHtmlForWorkflow(fu.getParameter("field" + fieldid));							
						 if (fieldhtmltype.equals("2")&&fieldtype.equals("2"))
						   {
							thetempvalue = Util.toHtml100(fu.getParameter("field" + fieldid));
							thetempvalue = rePlaceWordMark(thetempvalue);
							ishtml = true;
						   }else if(fieldhtmltype.equals("1")&&fieldtype.equals("1")){
							   thetempvalue = Util.toHtmlForWorkflow(Util.htmlFilter4UTF8(fu.getParameter("field" + fieldid)));
						   }else{
							   thetempvalue = Util.StringReplace(Util.toHtml10(Util.htmlFilter4UTF8(fu.getParameter("field" + fieldid)))," ","&nbsp;");
						   }
						}
						}
					}
					//1：文档， 2：客户， 3：项目， 4：资产 权限过滤
                    thetempvalue = resourceAuthorityFilter(fieldhtmltype, fieldtype, thetempvalue);
					if (thetempvalue.equals("")) thetempvalue = " ";
					//判断如果是HTML类型，单独处理
					if(ishtml) {
						htmlfieldMap.put(fieldname, thetempvalue);
					}
					else
					updateclause += fieldname + " = '" + fillFullNull(thetempvalue) + "',";
					newMap.put(fieldname, thetempvalue);//填入Map中,用于比较 by cyril
				}
			} 
            else
            // 非ORACLE数据库
            {
				if ((fielddbtype.toUpperCase()).indexOf("INT") >= 0&&!"224".equals(fieldtype)&&!"225".equals(fieldtype))//lu_Z_OSAP_INFORECORD_MAINTAIN_MAS
				/*------modified by xwj for td3297 20051130 --- begin --*/
				{
				    if("5".equals(fieldhtmltype))
                    {
				        if(isRequest)
                        {
                            if (!"".equals(request.getParameter("field" + fieldid)))
                            {
                            	if(null == newMap.get(fieldname))
                            		updateclause += fieldname + " = " + Util.getIntValue(request.getParameter("field" + fieldid), -1) + ",";
                                newMap.put(fieldname, String.valueOf(Util.getIntValue(request.getParameter("field" + fieldid), -1)));//填入Map中,用于比较 by cyril
                        
                            }
                            else
                            {
                            	if(null == newMap.get(fieldname))
                            		updateclause += fieldname + " = NULL,";
                                newMap.put(fieldname, null);//填入Map中,用于比较 by cyril
                            }
                        }
                        else
                        {
                            if (!"".equals(fu.getParameter("field" + fieldid)))
                            {
                            	if(null == newMap.get(fieldname))
                            		updateclause += fieldname + " = " + Util.getIntValue(fu.getParameter("field" + fieldid), -1) + ",";
                                newMap.put(fieldname, String.valueOf(Util.getIntValue(fu.getParameter("field" + fieldid), -1)));//填入Map中,用于比较 by cyril
                            }
                            else
                            {
                            	if(null == newMap.get(fieldname))
                            		updateclause += fieldname + " = NULL,";
                                newMap.put(fieldname, null);//填入Map中,用于比较 by cyril
                            }
                        }   
                    }
				    else
                    {
				        
				        if(isRequest)
                        {
				            //权限过滤
	                        String tempvalueid = resourceAuthorityFilter(fieldhtmltype, fieldtype, Util.null2String(request.getParameter("field" + fieldid)));
                            if (!"".equals(tempvalueid))
                            {  
                            	if(null == newMap.get(fieldname))
                            		updateclause += fieldname + " = " + Util.getIntValue(tempvalueid, 0) + ",";
                                newMap.put(fieldname, String.valueOf(Util.getIntValue(tempvalueid, 0)));//填入Map中,用于比较 by cyril
                            }
                            else
                            {
                            	if(null == newMap.get(fieldname))
                            		updateclause += fieldname + " = NULL,";
                                newMap.put(fieldname, null);//填入Map中,用于比较 by cyril
                            }
                        }
                        else
                        {
                          //权限过滤
                            String tempvalueid = resourceAuthorityFilter(fieldhtmltype, fieldtype, Util.null2String(fu.getParameter("field" + fieldid)));
                            if (!"".equals(tempvalueid))
                            {
                            	if(null == newMap.get(fieldname))
                            		updateclause += fieldname + " = " + Util.getIntValue(tempvalueid, 0) + ",";
                                newMap.put(fieldname, String.valueOf(Util.getIntValue(tempvalueid, 0)));//填入Map中,用于比较 by cyril
                            }
                            else
                            {
                            	if(null == newMap.get(fieldname))
                            		updateclause += fieldname + " = NULL,";
                                newMap.put(fieldname, null);//填入Map中,用于比较 by cyril
                            }
                        }
                    }
				}
                
				/*------modified by xwj for td3297 20051130 --- end --*/
				else if (((fielddbtype.toUpperCase()).indexOf("DECIMAL") >= 0||(fielddbtype.toUpperCase()).indexOf("FLOAT") >= 0)&&!"224".equals(fieldtype)&&!"225".equals(fieldtype))
                {
					int digitsIndex = fielddbtype.indexOf(",");
					int decimaldigits = 2;
		        	if(digitsIndex > -1){
		        		decimaldigits = Util.getIntValue(fielddbtype.substring(digitsIndex+1, fielddbtype.length()-1).trim(), 2);
		        	}else{
		        		decimaldigits = 2;
		        	}
					if(isRequest)
                    {
                        if (!"".equals(request.getParameter("field" + fieldid)))
                        {
                        	if(null == newMap.get(fieldname))
                        		updateclause += fieldname + " = " + Util.getPointValue2(request.getParameter("field" + fieldid),decimaldigits) + ",";
                            newMap.put(fieldname, Util.getPointValue2(request.getParameter("field" + fieldid),decimaldigits));//填入Map中,用于比较 by cyril
                        }
                        else
                        {
                        	if(null == newMap.get(fieldname))
                        		updateclause += fieldname + " = NULL,";
                            newMap.put(fieldname, null);//填入Map中,用于比较 by cyril
                        }
                    }
                    else
                    {
                        if (!"".equals(fu.getParameter("field" + fieldid)))
                        {
                        	if(null == newMap.get(fieldname))
                        		updateclause += fieldname + " = " + Util.getPointValue2(fu.getParameter("field" + fieldid),decimaldigits) + ",";
                            newMap.put(fieldname, Util.getPointValue2(fu.getParameter("field" + fieldid),decimaldigits));//填入Map中,用于比较 by cyril
                        }
                        else
                        {
                        	if(null == newMap.get(fieldname))
                        		updateclause += fieldname + " = NULL,";
                            newMap.put(fieldname, null);//填入Map中,用于比较 by cyril
                        }
                    }
                }
                
                else if(fieldhtmltype.equals("6")){
					//add by xhheng @20050315 for 附件上传
					String tempvalue="";

					//处理附件
					if(isRequest)
						tempvalue=Util.null2String(request.getParameter("field" + fieldid));
					else
						tempvalue=Util.null2String(fu.getParameter("field" + fieldid));
					//由RequestAddShareInfo统一处理附件上传字段的共享

                    /* ----- added by xwj for td1949 begin ------*/
					String[] oldUploadIdsStrs = null;
					DocExtUtil docExtUtil = new DocExtUtil();
					String oldUploadIdsStrs_ = "";
					RecordSet rsUploadId = new RecordSet();
					if (isbill == 1) {
						rsUploadId.executeSql("select "+fieldname+" from "+ billtablename +" where requestid = " + requestid);
					}else{
						rsUploadId.executeSql("select "+fieldname+" from workflow_form where requestid = " + requestid);
					}
					if(rsUploadId.next()){
						oldUploadIdsStrs_ = rsUploadId.getString(fieldname);
					}
					if(oldUploadIdsStrs_ != null && !"".equals(oldUploadIdsStrs_)){
					oldUploadIdsStrs = Util.TokenizerString2(oldUploadIdsStrs_, ",");
					}
					if(!tempvalue.equals("")){
						if(null == newMap.get(fieldname))
							updateclause += fieldname + " = '" + tempvalue + "',";
						newMap.put(fieldname, tempvalue);//填入Map中,用于比较 by cyril
					}

					else if(tempvalue.equals("")){
						if(null == newMap.get(fieldname))
							updateclause += fieldname + " = '',";
						newMap.put(fieldname, "");//填入Map中,用于比较 by cyril
					}
					else{
					}
					if(oldUploadIdsStrs != null){
					for( int y =0; y < oldUploadIdsStrs.length; y++){
						if(tempvalue.indexOf(oldUploadIdsStrs[y]) == -1){
							//if( Util.getIntValue(oldUploadIdsStrs[y],0) != 0 )
							//docExtUtil.deleteDoc(Integer.parseInt(oldUploadIdsStrs[y]));
							if( Util.getIntValue(oldUploadIdsStrs[y],0) != 0 ){
								String clientIp = "";
								if(isRequest){
									if (request != null)
										clientIp = Util.null2String(request.getRemoteAddr());
								}else{
									if (fu != null)
										clientIp = Util.null2String(fu.getRemoteAddr());
								}								
								docExtUtil.deleteDoc(Integer.parseInt(oldUploadIdsStrs[y]),user,clientIp);								
							}							
						}
					}
					}
					/* ----- added by xwj for td1949 end ------*/

				}else if (fieldhtmltype.equals("9")){  //如果为pc端提交 地址为空
	                String preData = "";
	                String tempvalue = "";
	                if (isRequest){
	                    preData = Util.null2String(request.getParameter("field" + fieldid));
	                }else{
	                    preData = Util.null2String(fu.getParameter("field" + fieldid));
	                }
	                if(preData.equals("")){
	                    tempvalue = nodeid + LocateUtil.SPLIT_FIELD + userid + LocateUtil.SPLIT_FIELD+"0"+LocateUtil.SPLIT_FIELD+""
	                        +LocateUtil.SPLIT_FIELD +"0"+ LocateUtil.SPLIT_FIELD+"0";                    
	                }else{
	                    tempvalue = preData + LocateUtil.SPLIT_LOCATION + nodeid + LocateUtil.SPLIT_FIELD + userid + LocateUtil.SPLIT_FIELD+"0"+LocateUtil.SPLIT_FIELD+""
	                        +LocateUtil.SPLIT_FIELD +"0"+ LocateUtil.SPLIT_FIELD+"0";
	                }
	                updateclause += fieldname + " = '" + fillFullNull(tempvalue) + "',";
	                newMap.put(fieldname, tempvalue);
	            }else{
					//modify by xhheng @ 20041229 for TD 1495
					boolean ishtml = false;
					String thetempvalue ="";
					if(workflowid==1){
						if(isRequest){
							//thetempvalue = Util.toScreen(request.getParameter("field" + fieldid), userlanguage);
							thetempvalue = Util.toHtml100(request.getParameter("field" + fieldid));							
						}else{
							//thetempvalue = Util.toScreen(fu.getParameter("field" + fieldid), userlanguage);
							thetempvalue = Util.toHtml100(fu.getParameter("field" + fieldid));							
						}
					}else{
						if(isRequest)
						{
							//thetempvalue = Util.StringReplace(Util.fromScreen2(request.getParameter("field" + fieldid), userlanguage)," ","&nbsp;");
							//thetempvalue = Util.toHtmlForWorkflow(request.getParameter("field" + fieldid));
						   if (fieldhtmltype.equals("2")&&fieldtype.equals("2"))
						   {
								thetempvalue = Util.toHtml100(request.getParameter("field" + fieldid));
								thetempvalue = rePlaceWordMark(thetempvalue);
								ishtml = true;
						   }else if(fieldhtmltype.equals("3")&&(fieldtype.equals("224")||fieldtype.equals("225")||fieldtype.equals("226")||fieldtype.equals("227"))){
							   thetempvalue = Util.null2String(request.getParameter("field" + fieldid));//防止空格转成&nbsp
							   thetempvalue = thetempvalue.replace("\'","\''");//处理单引号插入数据库的问题。
						   }
						   else if(fieldhtmltype.equals("1")&&fieldtype.equals("1")){
							   thetempvalue = Util.toHtmlForWorkflow(request.getParameter("field" + fieldid));
						   }else{
							   thetempvalue = Util.StringReplace(Util.fromScreen2(request.getParameter("field" + fieldid), userlanguage)," ","&nbsp;");
						   }
						}
						else
						{
							//thetempvalue = Util.StringReplace(Util.fromScreen2(fu.getParameter("field" + fieldid), userlanguage)," ","&nbsp;");
							//thetempvalue = Util.toHtmlForWorkflow(fu.getParameter("field" + fieldid));
						 if (fieldhtmltype.equals("2")&&fieldtype.equals("2"))
						   {
							thetempvalue = Util.toHtml100(fu.getParameter("field" + fieldid));
							thetempvalue = rePlaceWordMark(thetempvalue);
							ishtml = true;
						   }else if(fieldhtmltype.equals("3")&&(fieldtype.equals("224")||fieldtype.equals("225")||fieldtype.equals("226")||fieldtype.equals("227"))){
							   thetempvalue = Util.null2String(fu.getParameter("field" + fieldid));//防止空格转成&nbsp
						   }
						   else if(fieldhtmltype.equals("1")&&fieldtype.equals("1")){
							   thetempvalue = Util.toHtmlForWorkflow(Util.htmlFilter4UTF8(fu.getParameter("field" + fieldid)));
						   }else{
							   thetempvalue = Util.StringReplace(Util.fromScreen2(Util.htmlFilter4UTF8(fu.getParameter("field" + fieldid)), userlanguage)," ","&nbsp;");
						   }
						}
					}
					
					//1：文档， 2：客户， 3：项目， 4：资产 权限过滤
                    thetempvalue = resourceAuthorityFilter(fieldhtmltype, fieldtype, thetempvalue);
					if (thetempvalue.equals("")) thetempvalue = " ";
					//判断如果是HTML类型，单独处理
					if(ishtml) {
						htmlfieldMap.put(fieldname, thetempvalue);
					}else{
						if(null == newMap.get(fieldname))
							updateclause += fieldname + " = '" + thetempvalue + "',";
					}
					newMap.put(fieldname, thetempvalue);//填入Map中,用于比较 by cyril
				}
			}
			
            //添加文本处理，如果文本长度超过字段长度，则增加字段长度
			if((fieldhtmltype.equals("1") && "1".equals(fieldtype))||(fieldhtmltype.equals("2") && !"2".equals(fieldtype) && isoracle)){
			    fieldTypeCache.put(fieldname,fielddbtype);
            }
		}
		
		/*
		 * added by cyril on 2008-06-23 for TD:8835
		 * 功能:修改痕迹功能
		 */
		//获取当前修改时间 
		String nowtime = this.currentdate+" "+this.currenttime;
		//System.out.println("iscreate="+iscreate+" isStart="+isStart+" isTrack="+isTrack);
		if(!"1".equals(this.iscreate) && isStart && isTrack) {
			List cprList = new ArrayList();
			Iterator it;
			/*test
			System.out.println("=============newMap==============");
			Iterator it = newMap.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String key = entry.getKey().toString();
				String value = "";
				if(entry.getValue()!=null)
					value = replaceFirst(String.valueOf(entry.getValue()));
				System.out.println("[newMap]<"+key+">=["+value+"]");
			}
			System.out.println("=============oldMap==============");
			it = oldMap.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String key = entry.getKey().toString();
				Track t = (Track) oldMap.get(key);
				if(t!=null) {
					System.out.println("[oldMap]<"+key+">=["+t.getFieldOldText()+"]");
				}
			}
			//end test*/
			
			//遍历新的map
			it = newMap.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String key = entry.getKey().toString();
				String value = "";
				if(entry.getValue()!=null) {
					if(String.valueOf(entry.getValue()).equals(" ")) value = "";
					else value = String.valueOf(entry.getValue());
				}
				Track t = (Track) oldMap.get(key);
				if((t==null && value!=null)
						|| (t!=null && !t.getFieldOldText().equals(value))
						) {
					if(t==null) {
						//新增字段的处理
						s = new StringBuffer();
						t = new Track();
						if (isbill == 1) {
							s.append("select t2.id, t2.fieldname, t2.fielddbtype, t2.fieldhtmltype, t2.type, t2.fieldlabel ");
							s.append("from  workflow_bill t1, workflow_billfield t2, workflow_form t3 ");
							s.append("where t1.id=t3.billformid and t2.billid = t1.id ");
							s.append("and t2.viewtype=0 and t3.requestid ="+requestid);
							s.append(" and t2.fieldname='"+key+"'");
						} else {
							s.append("select t1.id, t1.fieldname, t1.fielddbtype, t1.fieldhtmltype, t1.type,");
							s.append("(select fieldlable from workflow_fieldlable t where t.langurageid = 7 and t.fieldid = t2.fieldid and t.formid = t2.formid) fieldNameCn,");
							s.append("(select fieldlable from workflow_fieldlable t where t.langurageid = 8 and t.fieldid = t2.fieldid and t.formid = t2.formid) fieldNameEn, ");
							s.append("(select fieldlable from workflow_fieldlable t where t.langurageid = 9 and t.fieldid = t2.fieldid and t.formid = t2.formid) fieldNameTw ");
							s.append("from workflow_formdict t1, workflow_formfield t2, workflow_form t3,workflow_fieldlable t4 ");
							s.append("where t1.id=t2.fieldid and t2.formid=t3.billformid and t4.langurageid = "+userlanguage+" and t4.fieldid = t2.fieldid and t4.formid = t2.formid and t3.requestid =" + requestid);
							s.append(" and t1.fieldname='"+key+"'");
						}
						//System.out.println(isbill+"===="+s.toString());
						executesuccess = rs.executeSql(s.toString());
						if (!executesuccess) {
							writeLog(s.toString());
							saveRequestLog("1");
							this.writeLog(s.toString());
							this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_01);
							return false;
						}
						t.setFieldName(rs.getString("fieldname"));		/*存字段名称*/
						t.setFieldType(rs.getString("type"));		/*存浏览按钮对应位置*/
						t.setFieldHtmlType(rs.getString("fieldhtmltype"));	/*存浏览按钮类型*/
						t.setFieldId(rs.getInt("id"));				/*将ID也取得*/
						//如果是表单则填以下二项
						if(isbill!=1) {
							t.setFieldNameCn(rs.getString("fieldNameCn"));	/*取字段名称*/
							t.setFieldNameEn(rs.getString("fieldNameEn"));	/*取字段名称*/
							t.setFieldNameTw(rs.getString("fieldNameTw"));	/*取字段名称*/
						}
						t.setNodeId(nodeid);	/*节点ID*/
						t.setRequestId(requestid);	/*请求ID*/
						t.setIsBill(isbill);	/*是否为表单*/
						//如果是单据则填LEABLE
						if(isbill==1)
							t.setFieldLableId(rs.getInt("fieldlabel"));/*单据对应的LEABLE*/
						t.setModifierIP(this.getIp());	/*IP地址*/
						t.setOptKind(src);	/*日志操作类型*/
					}
					t.setFieldNewText(String.valueOf(value));
					cprList.add(t);
				}
			}
			//记录修改日志
			for(int i=0; i<cprList.size(); i++) {
				Track t = (Track) cprList.get(i);
				//System.out.println(t.getFieldName()+":\""+t.getFieldOldText()+"\"<==>\""+t.getFieldNewText()+"\"");
				if(t!=null) {
					//如果是浮点数，只是后面小数位数发生了变化，那么在这里不记录到数据库中
					String tempfiledtype = Util.null2String(t.getFieldType());
					String tempfieldhtmltype = Util.null2String(t.getFieldHtmlType());
					String FieldOldText = Util.null2String(t.getFieldOldText());
					String FieldNewText = Util.null2String(t.getFieldNewText());
					if(!FieldOldText.equals("")&&!FieldNewText.equals("")&&tempfieldhtmltype.equals("1")&&(tempfiledtype.equals("3")||tempfiledtype.equals("4")||tempfiledtype.equals("5"))){
						if(Util.getDoubleValue(FieldOldText)==Util.getDoubleValue(FieldNewText)){
							continue;
						}
					}
					s = new StringBuffer();
					 //src.equals("save") 保存的痕迹,其他为流程修改的痕迹
					s.append("insert into workflow_track (");
					s.append("optKind,requestId,nodeId,isBill,fieldLableId,");
					s.append("fieldId,fieldHtmlType,fieldType,fieldNameCn,fieldNameEn,fieldNameTw,fieldOldText,fieldNewText,");
					s.append("modifierType,agentId,modifierId,modifierIP,modifyTime");
					s.append(") values (");
					s.append(this.disposeSqlNull(t.getOptKind())+",");
					s.append(t.getRequestId()+",");
					s.append(t.getNodeId()+",");
					s.append(t.getIsBill()+",");
					s.append(t.getFieldLableId()+",");
					s.append(t.getFieldId()+",");
					s.append(this.disposeSqlNull(t.getFieldHtmlType())+",");
					s.append(this.disposeSqlNull(t.getFieldType())+",");
					s.append(this.disposeSqlNull(t.getFieldNameCn())+",");
					s.append(this.disposeSqlNull(t.getFieldNameEn())+",");
					s.append(this.disposeSqlNull(t.getFieldNameTw())+",");
					s.append(this.disposeSqlNull(Util.null2String(t.getFieldOldText()))+",");
					s.append(this.disposeSqlNull(Util.null2String(t.getFieldNewText()))+",");
					s.append(usertype+",");
					s.append(agentId+",");
					s.append(userid+",");
					s.append(this.disposeSqlNull(t.getModifierIP())+",");
					s.append(this.disposeSqlNull(nowtime));
					s.append(")");
					//System.out.println("insert master sql="+s.toString());
					executesuccess = rs.executeSql(s.toString());
					if (!executesuccess) {
						writeLog(s.toString());
						saveRequestLog("1");
						//return false;
					}
				}
			}
		}
		/*
		 * end by cyril on 2008-06-23 for TD:8835
		 */
		
        //System.out.println(updateclause);
		if (!updateclause.equals("")) {
			updateclause = updateclause.substring(0, updateclause.length() - 1);
			//updateclause = filterClause(updateclause);
			if (isbill == 1) {
				updateclause = " update " + billtablename + " set " + updateclause + " where id = " + billid;
			} else {
				updateclause = "update workflow_form set " + updateclause + " where requestid=" + requestid;
			}
			//System.out.println("updateclause = "+updateclause);
			executesuccess = rs.executeSql(updateclause);
			if(!executesuccess){
                //查询是不是表字段对不上，如果是则把表字段添加进物理表中 add by jhy
                boolean _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(formid,billtablename,"",isbill,fieldTypeCache,updateclause);
                if(!_result){
                    this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
                    saveRequestLog("1");
                    return false;
                }
                fieldTypeCache.clear();
                //表字段添加成功后重新执行数据更新sql,如果成功则继续执行，否则则报错
                executesuccess = rs.executeSql(updateclause);
                if(!executesuccess){
                    this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
                    writeLog("执行出错的SQL:"+updateclause);
                    //this.setMessagecontent(WorkflowRequestMessage.getBottomInfo(SystemEnv.getHtmlLabelName(126573, userlanguage), workflowid, WorkflowRequestMessage.WF_FORMFIELD_SET).toString());
                    saveRequestLog("1");
                    return false;
                }
			}
			//System.out.println(updateclause);
			/******/
			
			if(isoracle){
				try {
					String hrmsql = "";
					if (isbill == 1) {
						hrmsql = " update "+billtablename+" set";
					} else {
						hrmsql = "update workflow_form set ";
					}
					int hrmindex = 0;
					String hrmsp = " ";
					Iterator fdit = typemap1.entrySet().iterator();
					while(fdit.hasNext()) {
						hrmindex++;
						Map.Entry entry = (Map.Entry) fdit.next();
						String key = entry.getKey().toString();
						String value = "";
						if(entry.getValue()!=null) {
							if(String.valueOf(entry.getValue()).equals(" ")) value = "";
							else value = String.valueOf(entry.getValue());
						}
						if(hrmindex >1){
							hrmsql += hrmsp+" , "+key + "=? ";
						}else{
							hrmsql += hrmsp+" "+key + "=? ";
						}
					}
					if (isbill == 1) {
						hrmsql += " where id = " + billid ;
					} else {
						hrmsql += " where requestid=" + requestid;
					}
					
					//System.out.println("hrmsql = "+hrmsql);
					if(hrmindex>0) {
						ConnStatement hrmstatement = null;
						try {
							hrmstatement = new ConnStatement();
							hrmstatement.setStatementSql(hrmsql);
							fdit = typemap1.entrySet().iterator();
							hrmindex = 0;
							while(fdit.hasNext()) {
								hrmindex++;
								Map.Entry entry = (Map.Entry) fdit.next();
								String key = entry.getKey().toString();
								String value = "";
								if(entry.getValue()!=null) {
									if(String.valueOf(entry.getValue()).equals(" ")) value = "";
									else value = String.valueOf(entry.getValue());
								}
								hrmstatement.setString(hrmindex, value);
							}
							hrmstatement.executeUpdate();
						} catch (Exception e) {
						    boolean _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(formid,billtablename,"",isbill,null,null);
						    if(!_result){
			                    this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
			                    saveRequestLog("1");
			                    return false;
			                }
						    try{
						        hrmstatement.executeUpdate();
						    }catch(Exception ee){
						        writeLog(e);
						        saveRequestLog("1");
						        writeLog("执行出错的SQL:"+hrmsql);
						        //this.setMessagecontent(WorkflowRequestMessage.getBottomInfo(SystemEnv.getHtmlLabelName(126573, userlanguage), workflowid, WorkflowRequestMessage.WF_FORMFIELD_SET).toString());
						        this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
						        return false;
						    }
						} finally {
							if(hrmstatement!=null) hrmstatement.close();
						}
					}
				}catch(Exception e){
					writeLog(e);
					saveRequestLog("1");
					this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
					return false;
				}
			//
			/******/
			}
		}
		
		if(isoracle){
            try {
                String locatesql = "";
                if (isbill == 1) {
                    locatesql = " update "+billtablename+" set";
                } else {
                    locatesql = "update workflow_form set ";
                }
                int locateindex = 0;
                String locatesp = " ";
                Iterator fdit = locationMap.entrySet().iterator();
                while(fdit.hasNext()) {
                    locateindex++;
                    Map.Entry entry = (Map.Entry) fdit.next();
                    String key = entry.getKey().toString();
                    String value = "";
                    if(entry.getValue()!=null) {
                        if(String.valueOf(entry.getValue()).equals(" ")) value = "";
                        else value = String.valueOf(entry.getValue());
                    }
                    if(locateindex >1){
                        locatesql += locatesp+" , "+key + "=? ";
                    }else{
                        locatesql += locatesp+" "+key + "=? ";
                    }
                }
                if (isbill == 1) {
                    locatesql += " where id = " + billid ;
                } else {
                    locatesql += " where requestid=" + requestid;
                }
                
                //System.out.println("hrmsql = "+hrmsql);
                if(locateindex>0) {
                    ConnStatement locateStatement = null;
                    try {
                        locateStatement = new ConnStatement();
                        locateStatement.setStatementSql(locatesql);
                        fdit = locationMap.entrySet().iterator();
                        locateindex = 0;
                        while(fdit.hasNext()) {
                            locateindex++;
                            Map.Entry entry = (Map.Entry) fdit.next();
                            String key = entry.getKey().toString();
                            String value = "";
                            if(entry.getValue()!=null) {
                                if(String.valueOf(entry.getValue()).equals(" ")) value = "";
                                else value = String.valueOf(entry.getValue());
                            }
                            locateStatement.setString(locateindex, value);
                        }
                        locateStatement.executeUpdate();
                    } catch (Exception e) {
                        writeLog(e);
                        saveRequestLog("1");
                        return false;
                    } finally {
                        if(locateStatement!=null) locateStatement.close();
                    }
                }
            }catch(Exception e){
                writeLog(e);
                saveRequestLog("1");
                return false;
            }
		}
		
		//add by alan for 15769
		try {
			String fdsql = "";
			if (isbill == 1) {
				fdsql = " update " + billtablename + " set ";
			} else {
				fdsql = "update workflow_form set ";
			}
			int fdcn = 0;
			String fdsp = "";
			Iterator fdit = htmlfieldMap.entrySet().iterator();
			while(fdit.hasNext()) {
				fdcn++;
				Map.Entry entry = (Map.Entry) fdit.next();
				String key = entry.getKey().toString();
				String value = "";
				if(entry.getValue()!=null) {
					if(String.valueOf(entry.getValue()).equals(" ")) value = "";
					else value = String.valueOf(entry.getValue());
				}
				fdsql += fdsp+" "+key+"=? ";
				fdsp = ",";
			}
			if (isbill == 1) {
				fdsql += " where id = " + billid;
			} else {
				fdsql += " where requestid=" + requestid;
			}
			
			if(fdcn>0) {
				ConnStatement fdst = null;
				try {
					fdst = new ConnStatement();
					fdst.setStatementSql(fdsql);
					fdit = htmlfieldMap.entrySet().iterator();
					fdcn = 0;
					while(fdit.hasNext()) {
						fdcn++;
						Map.Entry entry = (Map.Entry) fdit.next();
						String key = entry.getKey().toString();
						String value = "";
						if(entry.getValue()!=null) {
							if(String.valueOf(entry.getValue()).equals(" ")) value = "";
							else value = String.valueOf(entry.getValue());
						}
						fdst.setString(fdcn, Util.htmlFilter4UTF8(value));
					}
					fdst.executeUpdate();
				} catch (Exception e) {
                    boolean _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(formid,billtablename,"",isbill,null,null);
                    if(!_result){
                        this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
                        saveRequestLog("1");
                        return false;
                    }
                    try{
                        fdst.executeUpdate();
                    }catch(Exception ee){
                        writeLog(ee);
                        saveRequestLog("1");
                        writeLog("执行出错的SQL:"+fdsql);
                        //this.setMessagecontent(WorkflowRequestMessage.getBottomInfo(SystemEnv.getHtmlLabelName(126573, userlanguage), workflowid, WorkflowRequestMessage.WF_FORMFIELD_SET).toString());
                        this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
                        return false;
                    }
				} finally {
					if(fdst!=null) fdst.close();
				}
			}
		}catch(Exception e){
			writeLog(e);
			saveRequestLog("1");
			this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
			return false;
		}		
        //-----基本信息处理结束-----

		//###################SAVE the detailed form data START  ########## Add by Charoes Huang On Feb 25,2004
		//增加单据多明细保存 by mackjoe at 2005-07-20
		//如果字段在该节点不可见则不要编辑 by ben 2006-06-1
		if (src.equals("save") || src.equals("submit")) {
			//重新定义二个MAP
			newMap = new HashMap();
			oldMap = new HashMap();
			Map dtMap = new HashMap();
			List fields = new ArrayList();
			try {
				//System.out.println(isbill);
				if(isbill==1){	//单据
                    fieldinfo.setRequestid(requestid);
                    fieldinfo.GetDetailTableField(formid,isbill,userlanguage);
                    ArrayList detailfieldids=fieldinfo.getDetailTableFields();
                    ArrayList detailfieldnames=fieldinfo.getDetailDBFieldNames();
                    ArrayList detaildbtypes=fieldinfo.getDetailFieldDBTypes();
                    ArrayList detailtables=fieldinfo.getDetailTableNames();
                    ArrayList detailkeys=fieldinfo.getDetailTableKeys();
                    boolean isexpbill=getModeid(workflowid,nodeid,formid,isbill); //特定图形化单据

                	String fieldInfo_skipDetailTableNames_formid7 = "";
            		try{
            			if(isbill==1 && formid==7){
        	    			fieldInfo_skipDetailTableNames_formid7 = Util.null2String(new String(
        	    					Util.null2String(this.getPropValue("FieldInfo", "fieldInfo_skipDetailTableNames_formid7")).getBytes("ISO-8859-1"), "gbk")).trim().toLowerCase();
            			}
            		}catch(Exception ex1){
            			this.writeLog(ex1);
            		}
            		
                    for(int i=0;i<detailfieldids.size();i++){
						temprowindex = 0;
                        ArrayList fieldids=(ArrayList)detailfieldids.get(i);
                        ArrayList fieldnames=(ArrayList)detailfieldnames.get(i);
                        ArrayList fielddbtypes=(ArrayList)detaildbtypes.get(i);
                        String detailtable=(String)detailtables.get(i);
                        String detailkey=(String)detailkeys.get(i);
                		try{
    	                    if(isbill==1 && formid==7 && !"".equals(Util.null2String(detailtable).trim()) && !"".equals(fieldInfo_skipDetailTableNames_formid7) 
    	                    		&& ((","+fieldInfo_skipDetailTableNames_formid7+",").toLowerCase()).indexOf((","+Util.null2String(detailtable).trim()+",").toLowerCase()) >= 0){
    	                    	continue;
    	                    }
                		}catch(Exception ex1){
                			this.writeLog(ex1);
                		}

                        /**
                         * added by cyril on 2008-06-25
                         * 功能:取得明细字段
                         */
                        if (isStart && isTrack) {
                            fields = new ArrayList();
                            for (int l = 0; l < fieldids.size(); l++) {
                                ArrayList dtlfieldids = Util.TokenizerString((String) fieldids.get(l), "_");
                                Trackdetail td = new Trackdetail();
                                td.setFieldName((String) fieldnames.get(l));        /*存字段名称*/
                                td.setFieldType((String) dtlfieldids.get(2));        /*存浏览按钮对应位置*/
                                td.setFieldHtmlType((String) dtlfieldids.get(3));    /*存浏览按钮类型*/
                                td.setFieldId(Util.getIntValue(((String) dtlfieldids.get(0)).substring(5)));                /*将ID也取得*/
                                td.setFieldGroupId(i);    /*明细组*/
                                td.setNodeId(nodeid);    /*节点ID*/
                                td.setRequestId(requestid);    /*请求ID*/
                                td.setIsBill(isbill);    /*是否为表单*/
                                td.setModifierIP(this.getIp());    /*IP地址*/
                                td.setOptKind(src);    /*日志操作类型*/
                                td.setModifyTime(nowtime);//修改时间
                                fields.add(td);
                            }
                        }
                        /**
                         * end by cyril on 2008-06-05
                         */
                        if(detailkey==null || detailkey.trim().equals("")){
                            detailkey="mainid";
                        }
                        if (formid == 201) {
                        	detailkey="detailrequestid";
                        }
                        //是否为自定义表单
                        if(!isexpbill&&(detailtable.indexOf("formtable_main_")!=-1 || detailtable.startsWith("uf_"))) isexpbill=true;
                        RecordSet detailrs=new RecordSet();
                        int rowsum =-1;
//                        boolean caninsert = false;
                        String fieldid1 = "";
						String fieldname1 = "";
						String fieldvalue1 = "";
						String fieldhtmltype1 = "";
						String fielddbtype1 = "";
						String type1 = "";
                        if(iscreate.equals("1")) isexpbill=false;//排除新建流程时
                        if (isexpbill) {
                            //获取当前组的提交明细行序号串
                            String submitids = "";
                            if (isRequest)
                                submitids = Util.null2String(request.getParameter("submitdtlid" + i));
                            else
                                submitids = Util.null2String(fu.getParameter("submitdtlid" + i));
                            //String[] submitidAy=submitids.split(",");
                            String[] submitidAy = Util.TokenizerString2(submitids, ",");

                            /**
                             * modified by cyril on 2008-06-25
                             * 此段程序应该提前,判断删除的数据就不需要做修改动作了
                             * 删除的时候将记录保存到日志表后再物理删除记录
                             */
                            //拼装被删除的原有明细id串
                            String deldtlids = "";
                            if (deldtlids.equals("")) {
                                if (isRequest)
                                    deldtlids = Util.null2String(request.getParameter("deldtlid" + i));
                                else
                                    deldtlids = Util.null2String(fu.getParameter("deldtlid" + i));
                            } else {
                                String tempdeldtlids = "";
                                if (isRequest)
                                    tempdeldtlids = Util.null2String(request.getParameter("deldtlid" + i));
                                else
                                    tempdeldtlids = Util.null2String(fu.getParameter("deldtlid" + i));
                                if (!tempdeldtlids.equals(""))
                                    deldtlids += "," + tempdeldtlids;
                            }
							if(!deldtlids.equals("")){
								deldtlids = deldtlids.replaceAll(",{2,}",",");
								if(deldtlids.indexOf(",")==0)deldtlids = deldtlids.substring(1,deldtlids.length());
								if(deldtlids.lastIndexOf(",")==deldtlids.length()-1)deldtlids = deldtlids.substring(0,deldtlids.length()-1);
							}
                            //明细行循环
                            for (int k = 0; k < submitidAy.length; k++) {
                                //判断明细属性：新增、修改
                                String Dtlid = "";
                                if (isRequest)
                                    Dtlid = Util.null2String(request.getParameter("dtl_id_" + i + "_" + submitidAy[k]));
                                else
                                    Dtlid = Util.null2String(fu.getParameter("dtl_id_" + i + "_" + submitidAy[k]));

                                //新增明细
                                if (Dtlid.equals("")) {
                                    boolean hasMultiDoc = false;
                                    String sql1 = "insert into " + detailtable + " ( " + detailkey + ",";
                                    String sql2 = " values (" + billid + ",";
                                    if (formid == 156 || formid == 157 || formid == 158 || formid == 159) {
                                        int dsporder=0;
                                        rs.executeSql("select max(dsporder) from "+detailtable+" where "+detailkey+"="+billid);
                                        if(rs.next()){
                                            dsporder=rs.getInt(1)+1;
                                        }
                                        sql1 += "dsporder,";
                                        sql2 += dsporder+ ",";
                                    }else if(formid==7){
                                        int rowno=0;
                                        rs.executeSql("select max(rowno) from "+detailtable+" where "+detailkey+"="+billid);
                                        if(rs.next()){
                                            rowno=rs.getInt(1)+1;
                                        }
                                        sql1 += "rowno,";
                                        sql2 += rowno+ ",";
                                    }

                                    int nullLength = 0;
                                    List newList = new ArrayList();
                                    for (int j = 0; j < fieldids.size(); j++) {
                                        fieldname1 = (String) fieldnames.get(j);
                                        fieldid1 = (String) fieldids.get(j);
                                        fielddbtype1 = (String) fielddbtypes.get(j);
                                        int indxno = fieldid1.lastIndexOf("_");
                                        if (indxno > -1) {
                                            fieldhtmltype1 = fieldid1.substring(indxno + 1);
                                            fieldid1 = fieldid1.substring(0, indxno);
                                            indxno = fieldid1.lastIndexOf("_");
                                            if (indxno > -1) {
                                                type1 = fieldid1.substring(indxno + 1);
                                                fieldid1 = fieldid1.substring(0, indxno);
                                                indxno = fieldid1.lastIndexOf("_");
                                                if (indxno > -1) {
                                                    fieldid1 = fieldid1.substring(0, indxno);
                                                }
                                            }
                                        }
                                        if (workflowid == 1) {
                                            if (isRequest) {
                                                //fieldvalue1 = Util.toScreen(request.getParameter(fieldid1+"_"+k),userlanguage);
                                                fieldvalue1 = Util.toHtml100(request.getParameter(fieldid1 + "_" + submitidAy[k]));
                                            } else {
                                                //fieldvalue1 = Util.toScreen(fu.getParameter(fieldid1+"_"+k),userlanguage);
                                                fieldvalue1 = Util.toHtml100(fu.getParameter(fieldid1 + "_" + submitidAy[k]));
                                            }
                                        } else {
                                            if (isRequest)
                                                fieldvalue1 = Util.fromScreen2(request.getParameter(fieldid1 + "_" + submitidAy[k]), userlanguage);
                                            else
                                                fieldvalue1 = Util.fromScreen2(fu.getParameter(fieldid1 + "_" + submitidAy[k]), userlanguage);
                                        }
                                        //单行长度如果<4000,则自动增加长度,超过4000提示xx字段值过长保存失败
                                        //多行文本，则长度限制4000,超过直接报错
                                        if((fieldhtmltype1.equals("1") &&"1".equals(type1))||(fieldhtmltype1.equals("2") && "1".equals(type1) && isoracle)){
                                            fieldTypeCache.put(fieldname1,fielddbtype1);
                                        }
                                        
                                     
                                        //1：文档， 2：客户， 3：项目， 4：资产 权限过滤
                                        fieldvalue1 = resourceAuthorityFilter(fieldhtmltype1, type1, fieldvalue1);
                                        //空值处理
                                        if (fieldvalue1.trim().equals("")) {
                                            nullLength++;
                                            if (!(fieldhtmltype1.equals("2") || (fieldhtmltype1.equals("1") && (type1.equals("1") || type1.equals("5")))) && !(fieldhtmltype1.equals("3") && (type1.equals("2") || type1.equals("19") || type1.equals("161") || type1.equals("162")))) {//非文本的时候(去掉日期，自定义浏览框)
                                                fieldvalue1 = "NULL";
                                            }
                                            if (fieldhtmltype1.equals("5")) {
                                                fieldvalue1 = "NULL";
                                            } else if (fieldhtmltype1.equals("4")) { /*--- mackjoe for td5197 20061031 begin---*/
                                                fieldvalue1 = "0";
                                                nullLength--;
                                            }
                                            /*--- mackjoe for td5197 20061031 end---*/
                                        }else if(!fielddbtype1.toLowerCase().startsWith("browser.") && (fielddbtype1.toUpperCase()).indexOf("NUMBER")>=0 || (fielddbtype1.toUpperCase()).indexOf("FLOAT")>=0 || (fielddbtype1.toUpperCase()).indexOf("DECIMAL")>=0){
                                        	int digitsIndex = fielddbtype1.indexOf(",");
                        					int decimaldigits = 2;
                        		        	if(digitsIndex > -1){
                        		        		decimaldigits = Util.getIntValue(fielddbtype1.substring(digitsIndex+1, fielddbtype1.length()-1).trim(), 2);
                        		        	}else{
                        		        		decimaldigits = 2;
                        		        	}
                        		        	fieldvalue1 = Util.getPointValue2(fieldvalue1, decimaldigits);
                                        }

                                        if (j == fieldids.size() - 1) {//the last
                                            if ((fielddbtype1.toLowerCase().startsWith("text") || fielddbtype1.toLowerCase().startsWith("char") || fielddbtype1.toLowerCase().startsWith("varchar") || fielddbtype1.toLowerCase().startsWith("browser"))&&!"NULL".equals(fieldvalue1)) {
                                                sql1 += fieldname1 + ")";
                                                sql2 += "'" + Util.toHtmlForSpace(Util.htmlFilter4UTF8(Util.null2String(fieldvalue1))) + "')";
                                            }else if(fieldhtmltype1.equals("3")&&(type1.equals("256")||type1.equals("257"))){
	                                           	 sql1 += fieldname1 + ")";
	                                             sql2 += "'" + Util.htmlFilter4UTF8(Util.null2String(fieldvalue1)) + "')";
	                                        }else if(fieldhtmltype1.equals("3")&&type1.equals("17")&&isoracle){
												if("NULL".equals(fieldvalue1)) fieldvalue1="";
                                            	sql1 += fieldname1 + " )";
                                            	sql2 += " '' )";
                                            	if(!fieldvalue1.equals("NULL")) {
                                            	    dtMap.put(fieldname1, fieldvalue1);
                                            	}
                                            } else {
                                                sql1 += fieldname1 + ")";
                                                sql2 += fieldvalue1 + ")";
                                            }

                                        } else {
                                            if ((fielddbtype1.toLowerCase().startsWith("text") || fielddbtype1.toLowerCase().startsWith("char") || fielddbtype1.toLowerCase().startsWith("varchar") || fielddbtype1.toLowerCase().startsWith("browser"))&&!"NULL".equals(fieldvalue1)) {
                                                sql1 += fieldname1 + ",";
                                                sql2 += "'" + Util.toHtmlForSpace(Util.htmlFilter4UTF8(Util.null2String(fieldvalue1))) + "',";
                                            }else if(fieldhtmltype1.equals("3")&&(type1.equals("256")||type1.equals("257"))){
                                            	sql1 += fieldname1 + ",";
                                                sql2 += "'" + Util.htmlFilter4UTF8(Util.null2String(fieldvalue1)) + "',";
                                            }else if(fieldhtmltype1.equals("3")&&type1.equals("17")&&isoracle){
												if("NULL".equals(fieldvalue1)) fieldvalue1="";
                                            	sql1 += fieldname1 + " ,";
                                            	sql2 += " '' ,";
                                            	if(!fieldvalue1.equals("NULL")) {
                                            	    dtMap.put(fieldname1, fieldvalue1);
                                            	}
                                            } else {
                                                sql1 += fieldname1 + ",";
                                                sql2 += fieldvalue1 + ",";
                                            }
                                        }

                                        String tempvalueid = Util.null2String(fieldvalue1);
                                        if (fieldhtmltype1.equals("3") && (type1.equals("1") || type1.equals("17"))) { // 人力资源字段
                                            if (!tempvalueid.equals("") && !tempvalueid.equals("NULL")){
                                            	hrmids += "," + tempvalueid;
                                            	//hrmids += ", empty_clob() ";
                                            	//dtMap.put("hrmids", tempvalueid);
                                            }
                                        } else if (fieldhtmltype1.equals("3") && (type1.equals("7") || type1.equals("18"))) {   // 客户字段
                                            if (!tempvalueid.equals("") && !tempvalueid.equals("NULL"))
                                                crmids += "," + tempvalueid;
                                        } else if (fieldhtmltype1.equals("3") && (type1.equals("8") || type1.equals("135"))) {   // 项目字段
                                            if (!tempvalueid.equals("") && !tempvalueid.equals("NULL"))
                                                prjids += "," + tempvalueid;
                                        } else if (fieldhtmltype1.equals("3") && (type1.equals("9") || type1.equals("37"))) {  // 文档字段
                                            if (!tempvalueid.equals("") && !tempvalueid.equals("NULL"))
                                                docids += "," + tempvalueid;
                                        } else if (fieldhtmltype1.equals("3") && type1.equals("23")) {                           // 资产字段
                                            if (!tempvalueid.equals("") && !tempvalueid.equals("NULL"))
                                                cptids += "," + tempvalueid;
                                        }
                                        if (("field"+isMultiDoc).equals(fieldid1 + "_" + submitidAy[k]))
                                        {
                                        	hasMultiDoc = true;
                                        	docrowindex = ""+temprowindex;
                                        }
                                        /**
                                         * added by cyril on 2008-06-26
                                         * 将匹配的值放入相应字段中
                                         */
                                        if (isStart && isTrack) {
                                            for (int m = 0; m < fields.size(); m++) {
                                                Trackdetail td = (Trackdetail) fields.get(m);
                                                if (td.getFieldName().equals(fieldname1)) {
                                                    String tmpstr = Util.StringReplaceOnce(fieldvalue1, " ", "");
                                                    if (tmpstr.equals("NULL")) {
                                                        tmpstr = "";
                                                    }
                                                    td.setFieldNewText(tmpstr);
                                                    newList.add(td);
                                                }
                                            }
                                        }
                                        /**
                                         * end add by cyril on 2008-06-26
                                         */
                                    }

                                    if (nullLength != fieldids.size() || hasMultiDoc) {
                                        //System.out.println(sql1 + sql2);
                                    	temprowindex ++;
                                        executesuccess = detailrs.executeSql(sql1 + sql2);
                                        if (!executesuccess) {
                                            //在新增的时候检测billfield表明细字段是否和物理表字段相同
                                            boolean _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(formid,billtablename,detailtable,isbill,null,null);
                                            if(!_result){
                                                saveRequestLog("1");  
                                                this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
                                                return false;
                                            }
                                            executesuccess = detailrs.executeSql(sql1 + sql2);
                                            if(!executesuccess){
                                                this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
                                                writeLog("执行出错的SQL:"+sql1 + sql2);
                                                //this.setMessagecontent(WorkflowRequestMessage.getBottomInfo(SystemEnv.getHtmlLabelName(126573, userlanguage), workflowid, WorkflowRequestMessage.WF_FORMFIELD_SET).toString());
                                                saveRequestLog("1");
                                                return false;
                                            }
                                        }
                                        int detailRowNum = 0;
                                        detailrs.executeSql("select max(id) dtid from " + detailtable + " where "+detailkey+" =" + billid);
                                        if(detailrs.next()){
                                            detailRowNum = Util.getIntValue(detailrs.getString("dtid"));
                                        }
                                        newAddDetailRowPerInfo.put("dtl_id_" + i + "_" + submitidAy[k], detailRowNum);
                                        
                                        /////////////////////////
                                        /***明细多人力clob***/
                                        
                            			if(isoracle){
                            				try {
                            					//String maxidsql = "select max(id) dtid from " + detailtable + " where "+detailkey+" =" + billid;
                            					//detailrs.executeSql(maxidsql);
                            					//if(detailrs.next()){
                            				    if(detailRowNum > 0){
                            						//String dtid = Util.null2String(detailrs.getString("dtid"));
                            						String dtid = detailRowNum + "";
                            						if(!"".equals(dtid)){
                            							String hrmsql = "update "+detailtable+" set ";
		                            					int hrmindex = 0;
		                            					String hrmsp = " ";
		                            					Iterator fdit = dtMap.entrySet().iterator();
		                            					while(fdit.hasNext()) {
		                            						hrmindex++;
		                            						Map.Entry entry = (Map.Entry) fdit.next();
		                            						String key = entry.getKey().toString();
		                            						String value = "";
		                            						if(entry.getValue()!=null) {
		                            							if(String.valueOf(entry.getValue()).equals(" ")) value = "";
		                            							else value = String.valueOf(entry.getValue());
		                            						}
		                            						hrmsql += hrmsp+" "+key+"=? ";
        		                    						hrmsp = ",";
		                            					}
		                            					hrmsql += " where id = " + dtid + " and " + detailkey + " = " + billid;
		                            					
		                            					//System.out.println("hrmsql = "+hrmsql);
		                            					if(hrmindex>0) {
		                            						ConnStatement hrmsta = null;
        		                    						try {
        		                    							hrmsta = new ConnStatement();
        		                    							hrmsta.setStatementSql(hrmsql);
        		                    							fdit = dtMap.entrySet().iterator();
        		                    							hrmindex = 0;
        		                    							while(fdit.hasNext()) {
        		                    								hrmindex++;
        		                    								Map.Entry entry = (Map.Entry) fdit.next();
        		                    								String key = entry.getKey().toString();
        		                    								String value = "";
        		                    								if(entry.getValue()!=null) {
        		                    									if(String.valueOf(entry.getValue()).equals(" ")) value = "";
        		                    									else value = String.valueOf(entry.getValue());
        		                    								}
        		                    								hrmsta.setString(hrmindex, value);
        		                    							}
        		                    							hrmsta.executeUpdate();
        		                    						} catch (Exception e) {
		                            							writeLog(e);
		                            							saveRequestLog("1");
	                                                            this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
		                            							return false;
		                            						} finally {
		                            							if(hrmsta!=null) hrmsta.close();
		                            						}
		                            					}
                            						}
                            					}
                            				}catch(Exception e){
                            					writeLog(e);
                            					saveRequestLog("1");
                            					this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_01);
                            					return false;
                            				}
                            			//
                            			}
                                    /******/
                                        /////////////////////////
                                        /**
									 * added by cyril on 2008-06-26
									 * 新增时为每个字段都存下明细
									 */
									if(isStart && isTrack) {
										sn++;
										//System.out.println("A----SN="+sn);
										for(int j=0; j<fields.size(); j++) {
											Trackdetail td = (Trackdetail) fields.get(j);
											td.setOptType(1);//类型为新增
											for(int m=0; m<newList.size(); m++) {
												Trackdetail tmptd = (Trackdetail) newList.get(m);
												//System.out.println("tmp="+tmptd.getFieldNewText());
												if(td.getFieldName().equals(tmptd.getFieldName())) {
													td.setFieldNewText(tmptd.getFieldNewText());
												}
											}
											if(!this.insertDetail(td)) {//每个字段都要存明细
												return false;
											}
										}
									}
									/**
									 * end add by cyril on 2008-06-26
									 */
                                    }
                                } else if (checkIdDel(deldtlids, Dtlid)) { //检查是否删除的ID
                                    fieldTypeCache.clear();
                                    //修改明细
                                    String sql1 = "";
                                    Map modifyMap = new HashMap();
                                    for (int j = 0; j < fieldids.size(); j++) {
                                        fieldname1 = (String) fieldnames.get(j);
                                        fieldid1 = (String) fieldids.get(j);
                                        fielddbtype1 = (String) fielddbtypes.get(j);
                                        int indxno = fieldid1.lastIndexOf("_");
                                        if (indxno > -1) {
                                            fieldhtmltype1 = fieldid1.substring(indxno + 1);
                                            fieldid1 = fieldid1.substring(0, indxno);
                                            indxno = fieldid1.lastIndexOf("_");
                                            if (indxno > -1) {
                                                type1 = fieldid1.substring(indxno + 1);
                                                fieldid1 = fieldid1.substring(0, indxno);
                                                indxno = fieldid1.lastIndexOf("_");
                                                if (indxno > -1) {
                                                    fieldid1 = fieldid1.substring(0, indxno);
                                                }
                                            }
                                        }
                                        if (workflowid == 1) {
                                            if (isRequest) {
                                                //fieldvalue1 = Util.toScreen(request.getParameter(fieldid1+"_"+k),userlanguage);
                                                fieldvalue1 = Util.null2String(request.getParameter(fieldid1 + "_" + submitidAy[k]));
                                            } else {
                                                //fieldvalue1 = Util.toScreen(fu.getParameter(fieldid1+"_"+k),userlanguage);
                                                fieldvalue1 = Util.null2String(fu.getParameter(fieldid1 + "_" + submitidAy[k]));
                                            }
                                        } else {
                                            if (isRequest)
                                                fieldvalue1 = Util.null2String(request.getParameter(fieldid1 + "_" + submitidAy[k]));
                                            else
                                                fieldvalue1 = Util.null2String(fu.getParameter(fieldid1 + "_" + submitidAy[k]));
                                        }
                                        
                                        if((fieldhtmltype1.equals("1") &&"1".equals(type1))||(fieldhtmltype1.equals("2") && "1".equals(type1) && isoracle)){
                                            fieldTypeCache.put(fieldname1,fielddbtype1);
                                        }
                                        
                                        //1：文档， 2：客户， 3：项目， 4：资产 权限过滤
                                        fieldvalue1 = resourceAuthorityFilter(fieldhtmltype1, type1, fieldvalue1);
                                        String tempvalueid = Util.null2String(fieldvalue1);
                                        if (fieldhtmltype1.equals("3") && (type1.equals("1") || type1.equals("17"))) { // 人力资源字段
                                            if (!tempvalueid.equals("") && !tempvalueid.equals("NULL")){
                                                hrmids += "," + tempvalueid;
                                                //hrmids += ", empty_clob() ";
                                                //dtMap.put(fieldname1, tempvalueid);
                                            }
                                        } else if (fieldhtmltype1.equals("3") && (type1.equals("7") || type1.equals("18"))) {   // 客户字段
                                            if (!tempvalueid.equals("") && !tempvalueid.equals("NULL"))
                                                crmids += "," + tempvalueid;
                                        } else if (fieldhtmltype1.equals("3") && (type1.equals("8") || type1.equals("135"))) {   // 项目字段
                                            if (!tempvalueid.equals("") && !tempvalueid.equals("NULL"))
                                                prjids += "," + tempvalueid;
                                        } else if (fieldhtmltype1.equals("3") && (type1.equals("9") || type1.equals("37"))) {  // 文档字段
                                            if (!tempvalueid.equals("") && !tempvalueid.equals("NULL"))
                                                docids += "," + tempvalueid;
                                        } else if (fieldhtmltype1.equals("3") && type1.equals("23")) {                           // 资产字段
                                            if (!tempvalueid.equals("") && !tempvalueid.equals("NULL"))
                                                cptids += "," + tempvalueid;
                                        }
                                        if (fieldid1.length()>5&&detaileditfields.indexOf(fieldid1.substring(5)) < 0) {
                                            continue;
                                        }
                                        
                                        if(fieldhtmltype1.equals("6")){
    										//附件上传

    										DocExtUtil docExtUtil = new DocExtUtil();
    										String[] oldUploadIdsStrs = null;
    										String oldUploadIdsStrs_ = "";
    										RecordSet rsUploadId = new RecordSet();
    										
    										rsUploadId.executeSql("select "+fieldname1+" from "+detailtable+" where id="+Dtlid);
    										//System.out.println("select "+fieldname1+" from "+detailtable+" where id="+Dtlid);
    										if(rsUploadId.next()){
    											oldUploadIdsStrs_ = rsUploadId.getString(fieldname1);
    										}
    										if(oldUploadIdsStrs_ != null && !"".equals(oldUploadIdsStrs_)){
    											oldUploadIdsStrs = Util.TokenizerString2(oldUploadIdsStrs_, ",");
    										}
    										
    										if(oldUploadIdsStrs != null){
    											for( int y =0; y < oldUploadIdsStrs.length; y++){
    												if(fieldvalue1.indexOf(oldUploadIdsStrs[y]) == -1){
    													//if( Util.getIntValue(oldUploadIdsStrs[y],0) != 0 )
    													//docExtUtil.deleteDoc(Integer.parseInt(oldUploadIdsStrs[y]));
    													if( Util.getIntValue(oldUploadIdsStrs[y],0) != 0 ){
    														String clientIp = "";
    														if(isRequest){
    															if (request != null)
    																clientIp = Util.null2String(request.getRemoteAddr());
    														}else{
    															if (fu != null)
    																clientIp = Util.null2String(fu.getRemoteAddr());
    														}						
    														docExtUtil.deleteDoc(Integer.parseInt(oldUploadIdsStrs[y]),user,clientIp);							
    													}					
    												}
    											}
    										}
    										/* ----- added by xwj for td1949 end ------*/
    									}
                                        
                                        if (("field"+isMultiDoc).equals(fieldid1 + "_" + submitidAy[k]))
    									{
                                        	docrowindex = ""+temprowindex;
    									}
                                        //空值处理
                                        if (fieldvalue1.trim().equals("")) {
                                            if (!(fieldhtmltype1.equals("2") || (fieldhtmltype1.equals("1") && (type1.equals("1") || type1.equals("5")))) && !(fieldhtmltype1.equals("3") && (type1.equals("2") || type1.equals("19")||type1.equals("161")||type1.equals("162")))) {//非文本的时候(去掉日期，自定义浏览框)
                                                fieldvalue1 = "NULL";
                                            }
                                            if (fieldhtmltype1.equals("5")) {
                                                fieldvalue1 = "NULL";
                                            } else if (fieldhtmltype1.equals("4")) { /*--- mackjoe for td5197 20061031 begin---*/
                                                fieldvalue1 = "0";
                                            }
                                            /*--- mackjoe for td5197 20061031 end---*/
                                        }else if(!fielddbtype1.toLowerCase().startsWith("browser.") && (fielddbtype1.toUpperCase()).indexOf("NUMBER")>=0 || (fielddbtype1.toUpperCase()).indexOf("FLOAT")>=0 || (fielddbtype1.toUpperCase()).indexOf("DECIMAL")>=0){
                                        	int digitsIndex = fielddbtype1.indexOf(",");
                        					int decimaldigits = 2;
                        		        	if(digitsIndex > -1){
                        		        		decimaldigits = Util.getIntValue(fielddbtype1.substring(digitsIndex+1, fielddbtype1.length()-1).trim(), 2);
                        		        	}else{
                        		        		decimaldigits = 2;
                        		        	}
                        		        	fieldvalue1 = Util.getPointValue2(fieldvalue1, decimaldigits);
                                        }
                                        //更新串拼装
                                        
                                        if (sql1.equals("")) {
                                            if ((fielddbtype1.startsWith("text") || fielddbtype1.startsWith("char") || fielddbtype1.startsWith("varchar") || fielddbtype1.startsWith("browser"))&&!"NULL".equals(fieldvalue1)) {
                                                sql1 += fieldname1 + "=" + "'" + Util.toHtmlForSpace(Util.toHtml10(Util.htmlFilter4UTF8(fieldvalue1))) + "' ";
                                            }else if(fieldhtmltype1.equals("3")&&(type1.equals("256")||type1.equals("257"))){
                                            	sql1 += fieldname1 + "=" + "'" + Util.toHtml10(fieldvalue1) + "' ";
                                            }else if(fieldhtmltype1.equals("3")&&type1.equals("17")&&isoracle){
												if("NULL".equals(fieldvalue1)) fieldvalue1="";
                                            	sql1 += fieldname1 + " = '' ";
                                            	if(!fieldvalue1.equals("NULL")) {
                                            	    dtMap.put(fieldname1, fieldvalue1);
                                            	}
                                            } else {
                                                sql1 += fieldname1 + "=" + fieldvalue1 + " ";
                                            }

                                        } else {
                                            if ((fielddbtype1.startsWith("text") || fielddbtype1.startsWith("char") || fielddbtype1.startsWith("varchar") || fielddbtype1.startsWith("browser"))&&!"NULL".equals(fieldvalue1)) {
                                                sql1 += "," + fieldname1 + "=" + "'" + Util.toHtmlForSpace(Util.toHtml10(Util.htmlFilter4UTF8(fieldvalue1))) + "' ";
                                            }else if(fieldhtmltype1.equals("3")&&(type1.equals("256")||type1.equals("257"))){
                                            	 sql1 += "," + fieldname1 + "=" + "'" + Util.toHtml10(Util.htmlFilter4UTF8(fieldvalue1)) + "' ";
                                            }else if(fieldhtmltype1.equals("3")&&type1.equals("17")&&isoracle){
												if("NULL".equals(fieldvalue1)) fieldvalue1="";
                                            	sql1 += "," + fieldname1 + " = '' ";
                                            	if(!fieldvalue1.equals("NULL")) {
                                            	    dtMap.put(fieldname1, fieldvalue1);
                                            	}
                                            } else {
                                                sql1 += "," + fieldname1 + "=" + fieldvalue1 + " ";
                                            }
                                        }
                                        String tmpStr = Util.StringReplaceOnce(fieldvalue1, " ", "");
                                        if(tmpStr.equals("NULL"))
                                            tmpStr = "";
                                        modifyMap.put(fieldname1, tmpStr);//用来对比是否做过改动 by cyril on 2008-06-26 for TD:8835
                                    }

                                    if (!sql1.equals("")) {
                                        if(formid==156||formid==157||formid==158||formid==159){
                                            sql1="update " + detailtable + " set " + sql1 + " where dsporder=" + Dtlid+" and "+detailkey+" =" + billid;
                                            if(isStart && isTrack) this.updateOrDeleteDetailLog(fields, 2, i, ""+billid, modifyMap, detailtable);
                                        }else {
                                        	if(this.formid<0){
                                        		sql1="update " + detailtable + " set " + sql1 + " where id=" + Dtlid+" and "+detailkey+" =" + billid;
                                        	}else{
                                        		sql1="update " + detailtable + " set " + sql1 + " where id=" + Dtlid;
                                        	}
                                            if(isStart && isTrack) this.updateOrDeleteDetailLog(fields, 2, i, Dtlid, modifyMap, detailtable);
                                        }
                                        temprowindex ++;
                                        executesuccess = rs.executeSql( sql1);
                                        if (!executesuccess) {
                                            
                                            //在新增的时候检测billfield表明细字段是否和物理表字段相同
                                            boolean _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(formid,billtablename,detailtable,isbill,fieldTypeCache,sql1);
                                            if(!_result){
                                                saveRequestLog("1");    
                                                this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
                                                
                                                return false;
                                            }
                                            executesuccess = rs.executeSql( sql1);
                                            if(!executesuccess){
                                                saveRequestLog("1");
                                                writeLog("执行出错的SQL:"+sql1);
                                                //this.setMessagecontent(WorkflowRequestMessage.getBottomInfo(SystemEnv.getHtmlLabelName(126573, userlanguage), workflowid, WorkflowRequestMessage.WF_FORMFIELD_SET).toString());
                                                this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
                                                return false;
                                            }
                                        }
                                        //////////////
                                        /***明细多人力clob***/
                                        
                            			if(isoracle){
                            				try {
                            					String hrmsql = " update "+detailtable+" set ";
                            					int hrmindex = 0;
                            					String hrmsp = " ";
                            					Iterator fdit = dtMap.entrySet().iterator();
                            					while(fdit.hasNext()) {
                            						hrmindex++;
                            						Map.Entry entry = (Map.Entry) fdit.next();
                            						String key = entry.getKey().toString();
                            						String value = "";
                            						if(entry.getValue()!=null) {
                            							if(String.valueOf(entry.getValue()).equals(" ")) value = "";
                            							else value = String.valueOf(entry.getValue());
                            						}
                            						hrmsql += hrmsp+" "+key+"=? ";
		                    						hrmsp = ",";
                            					}
                            					if(this.formid<0){
                            						hrmsql += " where id = " + Dtlid + " and "+detailkey+" =" + billid;
                            					} else {
                            						hrmsql += " where id = " + Dtlid;
                            					}
                            					//System.out.println("hrmsql = "+hrmsql);
                            					if(hrmindex>0) {
                            						ConnStatement hrmstatement = null;
                            						try {
                            							hrmstatement = new ConnStatement();
                            							hrmstatement.setStatementSql(hrmsql);
		                    							fdit = dtMap.entrySet().iterator();
		                    							hrmindex = 0;
                        								while(fdit.hasNext()) {
		                    								hrmindex++;
		                    								Map.Entry entry = (Map.Entry) fdit.next();
		                    								String key = entry.getKey().toString();
		                    								String value = "";
		                    								if(entry.getValue()!=null) {
		                    									if(String.valueOf(entry.getValue()).equals(" ")) value = "";
		                    									else value = String.valueOf(entry.getValue());
		                    								}
		                    								hrmstatement.setString(hrmindex, value);
                        								}
                        								hrmstatement.executeUpdate();
                            						} catch (Exception e) {
                            							writeLog(e);
                            							saveRequestLog("1");
                                                        this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_01);
                            							return false;
                            						} finally {
                            							if(hrmstatement!=null) hrmstatement.close();
                            						}
                            					}
                            				}catch(Exception e){
                            					writeLog(e);
                            					saveRequestLog("1");
                            					this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_01);
                            					return false;
                            				}
                            			//
                            			}
                                    /******/
                                        //////////////
                                    }
                                }
                            }
                            if (!deldtlids.equals("")) {
                                if (isStart && isTrack) {
                                    //存删除的日志
                                    List tmpList = Util.TokenizerString(deldtlids, ",");
                                    for (int j = 0; j < tmpList.size(); j++) {
                                        this.updateOrDeleteDetailLog(fields, 3, i, tmpList.get(j).toString(), null, detailtable);
                                        //System.out.println("存删除的ID="+tmpList.get(j).toString());
                                    }
                                }
                                String sql1="";
                                if(formid == 156 || formid == 157 || formid == 158 || formid == 159) {
                                    sql1 = "delete from  " + detailtable + " where dsporder in(" + deldtlids + ") and " + detailkey + " =" + billid;
                                } else {
                                    sql1 = "delete from " + detailtable + " where id in(" + deldtlids + ")";
                                }
                                //System.out.println(sql1);
                                detailrs.executeSql(sql1);
                            }
                        } else {
                            WFNodeDtlFieldManager wfndfm=new WFNodeDtlFieldManager();
                            wfndfm.setNodeid(nodeid);
                            wfndfm.setGroupid(i);
                            wfndfm.selectWfNodeDtlField();
                            String dtldelete =wfndfm.getIsdelete();
                            if(detaileditfields.size()>0||dtldelete.equals("1")||iscreate.equals("1")){
                        	if (formid == 201) {
                        		if(fieldids.size()>0) detailrs.executeSql("delete from "+detailtable+" where "+detailkey+" =" + requestid);
                            }else {
                            	if(fieldids.size()>0) detailrs.executeSql("delete from "+detailtable+" where "+detailkey+" =" + billid);
                            }
						//System.out.println("det sql="+"delete from "+detailtable+" where "+detailkey+" =" + billid);
                        if(isRequest)
                            rowsum = Util.getIntValue(Util.null2String(request.getParameter("indexnum"+i)));
                        else
                            rowsum = Util.getIntValue(Util.null2String(fu.getParameter("indexnum"+i)));
                        String submitids = "";
                        if (isRequest)
                            submitids = Util.null2String(request.getParameter("submitdtlid" + i));
                        else
                            submitids = Util.null2String(fu.getParameter("submitdtlid" + i));
                        //String[] submitidAy=submitids.split(",");
                        //writeLog("submitids = " + submitids);
                        String[] submitidAy = Util.TokenizerString2(submitids, ",");
                        for(int k=0;k<submitidAy.length;k++){
                        	boolean hasMultiDoc = false;
                        	int rowcx = Util.getIntValue(submitidAy[k]);
                            String sql1 = "insert into "+detailtable+" ( "+detailkey+",";
                            String sql2 = " values (" + billid + ",";
                            if (formid == 201) {
                            	sql2 = " values (" + requestid + ",";
                            }
                            if(formid==156||formid==157||formid==158||formid==159){
                                sql1+="dsporder,";
                                sql2+=rowcx+",";
                            } else if (formid == 7) {
                                sql1 += "rowno,";
                                sql2 += rowcx + ",";
                            }
                            int nullLength = 0;
							for(int j=0;j<fieldids.size();j++){
                                fieldname1 = (String)fieldnames.get(j);
								fieldid1 = (String)fieldids.get(j);
								fielddbtype1=(String)fielddbtypes.get(j);
                                int indxno=fieldid1.lastIndexOf("_");
                                if(indxno>-1){
                                    fieldhtmltype1=fieldid1.substring(indxno+1);
                                    fieldid1=fieldid1.substring(0,indxno);
                                    indxno=fieldid1.lastIndexOf("_");
                                    if(indxno>-1){
                                        type1=fieldid1.substring(indxno+1);
                                        fieldid1=fieldid1.substring(0,indxno);
                                        indxno=fieldid1.lastIndexOf("_");
                                        if(indxno>-1){
                                            fieldid1=fieldid1.substring(0,indxno);
                                        }
                                    }
                                }
								if(workflowid==1){
									if(isRequest){
										//fieldvalue1 = Util.toScreen(request.getParameter(fieldid1+"_"+k),userlanguage);
										fieldvalue1 = Util.toHtml100(request.getParameter(fieldid1+"_"+rowcx));										
									}else{
										//fieldvalue1 = Util.toScreen(fu.getParameter(fieldid1+"_"+k),userlanguage);
										fieldvalue1 = Util.toHtml100(fu.getParameter(fieldid1+"_"+rowcx));										
									}
								}else{
									if(isRequest)
										fieldvalue1 = Util.fromScreen2(request.getParameter(fieldid1+"_"+rowcx),userlanguage);
									else
										fieldvalue1 = Util.fromScreen2(fu.getParameter(fieldid1+"_"+rowcx),userlanguage);
								}
								
								//1：文档， 2：客户， 3：项目， 4：资产 权限过滤
                                fieldvalue1 = resourceAuthorityFilter(fieldhtmltype1, type1, fieldvalue1);
                                //空值处理
                                if(fieldvalue1.trim().equals("")){
									nullLength++;
									if (!(fieldhtmltype1.equals("2") || (fieldhtmltype1.equals("1") && (type1.equals("1")||type1.equals("5"))))&&!(fieldhtmltype1.equals("3")&&(type1.equals("2")||type1.equals("19")||type1.equals("161")||type1.equals("162")))) {//非文本的时候(去掉日期)
										fieldvalue1 = "NULL";
									}
                                    if(fieldhtmltype1.equals("5")){
                                        fieldvalue1 = "NULL";
									}else if(fieldhtmltype1.equals("4")){ /*--- mackjoe for td5197 20061031 begin---*/
                                        fieldvalue1 = "0";
                                        nullLength--;
                                    }
                                    /*--- mackjoe for td5197 20061031 end---*/
                                }else if(!fielddbtype1.toLowerCase().startsWith("browser.") && (fielddbtype1.toUpperCase()).indexOf("NUMBER")>=0 || (fielddbtype1.toUpperCase()).indexOf("FLOAT")>=0 || (fielddbtype1.toUpperCase()).indexOf("DECIMAL")>=0){
                                	int digitsIndex = fielddbtype1.indexOf(",");
                					int decimaldigits = 2;
                		        	if(digitsIndex > -1){
                		        		decimaldigits = Util.getIntValue(fielddbtype1.substring(digitsIndex+1, fielddbtype1.length()-1).trim(), 2);
                		        	}else{
                		        		decimaldigits = 2;
                		        	}
                		        	fieldvalue1 = Util.getPointValue2(fieldvalue1, decimaldigits);
                                }

								if (j == fieldids.size() - 1) {//the last
									if ((fielddbtype1.toLowerCase().startsWith("text") || fielddbtype1.toLowerCase().startsWith("char") || fielddbtype1.toLowerCase().startsWith("varchar") || fielddbtype1.toLowerCase().startsWith("browser"))&&!"NULL".equals(fieldvalue1)) {
										sql1 += fieldname1 + ")";
										sql2 += "'" + Util.toHtmlForSpace(Util.htmlFilter4UTF8(Util.null2String(fieldvalue1))) + "')";
									}else if(fieldhtmltype1.equals("3")&&(type1.equals("256")||type1.equals("257"))){
										sql1 += fieldname1 + ")";
										sql2 += "'" + Util.htmlFilter4UTF8(Util.null2String(fieldvalue1)) + "')";
                                    }else if(fieldhtmltype1.equals("3")&&type1.equals("17")&&isoracle){
										if("NULL".equals(fieldvalue1)) fieldvalue1="";
                                    	sql1 += fieldname1 + ")";
										sql2 += " '' )";
										if(!fieldvalue1.equals("NULL")) {
										    dtMap.put(fieldname1, fieldvalue1);
										}
                                    } else {
										sql1 += fieldname1 + ")";
										sql2 += fieldvalue1 + ")";
									}

								} else {
									if ((fielddbtype1.toLowerCase().startsWith("text") || fielddbtype1.toLowerCase().startsWith("char") || fielddbtype1.toLowerCase().startsWith("varchar") || fielddbtype1.toLowerCase().startsWith("browser"))&&!"NULL".equals(fieldvalue1)) {
										sql1 += fieldname1 + ",";
										sql2 += "'" + Util.toHtmlForSpace(Util.htmlFilter4UTF8(Util.null2String(fieldvalue1))) + "',";
									}else if(fieldhtmltype1.equals("3")&&(type1.equals("256")||type1.equals("257"))){
										sql1 += fieldname1 + ",";
										sql2 += "'" + Util.htmlFilter4UTF8(Util.null2String(fieldvalue1)) + "',";
                                    }else if(fieldhtmltype1.equals("3")&&type1.equals("17")&&isoracle){
										if("NULL".equals(fieldvalue1)) fieldvalue1="";
                                    	sql1 += fieldname1 + ",";
										sql2 += " '' ,";
										if(!fieldvalue1.equals("NULL")) {
										    dtMap.put(fieldname1, fieldvalue1);
										}
                                    } else {
										sql1 += fieldname1 + ",";
										sql2 += fieldvalue1 + ",";
									}
								}

								String tempvalueid = Util.null2String(fieldvalue1);								
								if (fieldhtmltype1.equals("3") && (type1.equals("1") || type1.equals("17"))) { // 人力资源字段
									if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")){
										hrmids += "," + tempvalueid;
										//hrmids += ", empty_clob() ";
										//dtMap.put(fieldname1, tempvalueid);
									}
								} else if (fieldhtmltype1.equals("3") && (type1.equals("7") || type1.equals("18"))) {   // 客户字段
									if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")) crmids += "," + tempvalueid;
								} else if (fieldhtmltype1.equals("3") && (type1.equals("8")|| type1.equals("135"))) {   // 项目字段
									if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")) prjids += "," + tempvalueid;
								} else if (fieldhtmltype1.equals("3") && (type1.equals("9") || type1.equals("37"))) {  // 文档字段
									if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")) docids += "," + tempvalueid;
								} else if (fieldhtmltype1.equals("3") && type1.equals("23")) {                           // 资产字段
									if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")) cptids += "," + tempvalueid;
								}
								if(("field"+isMultiDoc).equals(fieldid1 + "_" + rowcx))
								{
									hasMultiDoc = true;
									docrowindex = ""+temprowindex;
								}
								
                            }

							if (nullLength != fieldids.size() || hasMultiDoc) 
							{
								temprowindex ++;
								executesuccess = detailrs.executeSql(sql1 + sql2);
							}

							if (!executesuccess) {
		                        //在新增的时候检测billfield表明细字段是否和物理表字段相同
                                boolean _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(formid,billtablename,detailtable,isbill,null,null);
                                if(!_result){
                                    saveRequestLog("1");    
                                    this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
                                    return false;
                                }
                                executesuccess = detailrs.executeSql(sql1 + sql2);
                                if (!executesuccess) {
                                    saveRequestLog("1");
                                    writeLog("执行出错的SQL:"+sql1 + sql2);
                                    //this.setMessagecontent(WorkflowRequestMessage.getBottomInfo(SystemEnv.getHtmlLabelName(126573, userlanguage), workflowid, WorkflowRequestMessage.WF_FORMFIELD_SET).toString());
                                    this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
                                    return false;
                                }
							}
							//////////////
                            /***明细多人力clob***/
                            
                			if(isoracle){
                				try {
                					String maxidsql = "select max(id) dtid from " + detailtable + " where "+detailkey+" =" + billid;
                					detailrs.executeSql(maxidsql);
                					if(detailrs.next()){
                						String dtid = Util.null2String(detailrs.getString("dtid"));
                						if(!"".equals(dtid)){
                							String hrmsql = "update "+detailtable+" set ";
		                					int hrmindex = 0;
		                					String hrmsp = " ";
		                					Iterator fdit = dtMap.entrySet().iterator();
		                					while(fdit.hasNext()) {
		                						hrmindex++;
		                						Map.Entry entry = (Map.Entry) fdit.next();
		                						String key = entry.getKey().toString();
		                						String value = "";
		                						if(entry.getValue()!=null) {
		                							if(String.valueOf(entry.getValue()).equals(" ")) value = "";
		                							else value = String.valueOf(entry.getValue());
		                						}
		                						hrmsql += hrmsp+" "+key+"=? ";
	                    						hrmsp = ",";
		                					}
		                					hrmsql += " where id = " + dtid + " and " + detailkey + " = " + billid;
		                					//System.out.println("hrmsql = "+hrmsql);
		                					if(hrmindex>0) {
		                						ConnStatement hrmsta = null;
	                    						try {
	                    							hrmsta = new ConnStatement();
	                    							hrmsta.setStatementSql(hrmsql);
	                    							fdit = dtMap.entrySet().iterator();
	                    							hrmindex = 0;
	                    							while(fdit.hasNext()) {
	                    								hrmindex++;
	                    								Map.Entry entry = (Map.Entry) fdit.next();
	                    								String key = entry.getKey().toString();
	                    								String value = "";
	                    								if(entry.getValue()!=null) {
	                    									if(String.valueOf(entry.getValue()).equals(" ")) value = "";
	                    									else value = String.valueOf(entry.getValue());
	                    								}
	                    								hrmsta.setString(hrmindex, value);
	                    							}
	                    							hrmsta.executeUpdate();
	                    						} catch (Exception e) {
		                							writeLog(e);
		                							saveRequestLog("1");
	                                                this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_01);
		                							return false;
		                						} finally {
		                							if(hrmsta!=null) hrmsta.close();
		                						}
		                					}
                						}
                					}
                				}catch(Exception e){
                					writeLog(e);
                					saveRequestLog("1");
                					this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_01);
                					return false;
                				}
                			//
                			}
                        /******/
                        //////////////
                        }
                    }
                    }
                        dtMap.clear();
                    }
				}else{	//表单 增加多明细by ben 2006-04-26
					ArrayList fieldids1 = new ArrayList();				//字段队列
					ArrayList fieldnames1 = new ArrayList();			//单据的字段的表字段名队列
					ArrayList fieldhtmltypes1 = new ArrayList();		//html类型
					ArrayList fielddbtypes = new ArrayList();
					ArrayList types1 = new ArrayList();					//类型
	                int detailGroupId=0;
	                String deldtlids="";
	                int bflength = 0;//多明细时判断上次明细时的长度
	                int rows=0;
					String  sql="select distinct groupId from workflow_formfield where formid="+formid+" and isdetail='1' order by groupId";
	                rs2.execute(sql);
	                //明细组循环
	                while (rs2.next()){
	                	fields = new ArrayList();//by cyril 每次明细都重取字段
		                detailGroupId=rs2.getInt(1);

		                //获取当前明细组的字段信息
						sql = " select distinct a.fieldid, b.fieldname, b.fieldhtmltype, b.type, b.fielddbtype,a.groupId, "+
							  "(select fieldlable from workflow_fieldlable t where t.langurageid = 7 and t.fieldid = a.fieldid and t.formid = a.formid) fieldNameCn,"+
							  "(select fieldlable from workflow_fieldlable t where t.langurageid = 8 and t.fieldid = a.fieldid and t.formid = a.formid) fieldNameEn, "+
							  "(select fieldlable from workflow_fieldlable t where t.langurageid = 9 and t.fieldid = a.fieldid and t.formid = a.formid) fieldNameTw "+
							  " from workflow_formfield a, workflow_formdictdetail b,workflow_fieldlable c "+
							  " where a.isdetail='1' and a.fieldid=b.id and c.fieldid = a.fieldid and c.formid = a.formid and c.langurageid = "+userlanguage+" and a.formid=" + formid+" and a.groupId=" + detailGroupId;
						//System.out.println("===============>"+sql);
                        rs.executeSql(sql);
						while (rs.next()) {
							fieldids1.add(Util.null2String(rs.getString("fieldid")));
							fieldnames1.add(Util.null2String(rs.getString("fieldname")));
							fieldhtmltypes1.add(Util.null2String(rs.getString("fieldhtmltype")));
							fielddbtypes.add(Util.null2String(rs.getString("fielddbtype")));
							types1.add(Util.null2String(rs.getString("type")));
							/**
							 * added by cyril on 2008-06-25
							 * 功能:取得明细字段
							 */
							if(isStart && isTrack) {
								Trackdetail td = new Trackdetail();
								td.setFieldName(rs.getString("fieldname"));		/*存字段名称*/
								td.setFieldType(rs.getString("type"));		/*存浏览按钮对应位置*/
								td.setFieldHtmlType(rs.getString("fieldhtmltype"));	/*存浏览按钮类型*/
								td.setFieldId(rs.getInt("fieldid"));				/*将ID也取得*/
								td.setFieldGroupId(rs.getInt("groupId"));	/*明细组*/
								//如果是表单则填以下二项
								if(isbill!=1) {
									td.setFieldNameCn(rs.getString("fieldNameCn"));	/*取字段名称*/
									td.setFieldNameEn(rs.getString("fieldNameEn"));	/*取字段名称*/
									td.setFieldNameTw(rs.getString("fieldNameTw"));	/*取字段名称*/
								}
								td.setNodeId(nodeid);	/*节点ID*/
								td.setRequestId(requestid);	/*请求ID*/
								td.setIsBill(isbill);	/*是否为表单*/
								td.setModifierIP(this.getIp());	/*IP地址*/
								td.setOptKind(src);	/*日志操作类型*/
								td.setModifyTime(nowtime);//修改时间
								fields.add(td);
							}
							/**
							 * end by cyril on 2008-06-05
							 */
						}
							    
					    //获取当前组的提交明细行序号串
					    String submitids="";
					    if(isRequest)
					    	submitids = Util.null2String(request.getParameter("submitdtlid"+rows));
						else
							submitids = Util.null2String(fu.getParameter("submitdtlid"+rows));
					    //String[] submitidAy=submitids.split(",");
					    String[] submitidAy=Util.TokenizerString2(submitids,",");
						
					    /**
						 * modified by cyril on 2008-06-25
						 * 此段程序应该提前,判断删除的数据就不需要做修改动作了
						 * 删除的时候将记录保存到日志表后再物理删除记录
						 */
						//拼装被删除的原有明细id串
						if(deldtlids.equals("")){
							if(isRequest)
								deldtlids = Util.null2String(request.getParameter("deldtlid" + detailGroupId));
							else
								deldtlids = Util.null2String(fu.getParameter("deldtlid" + detailGroupId));
						}else{
							String tempdeldtlids="";
							if(isRequest)
								tempdeldtlids = Util.null2String(request.getParameter("deldtlid" + detailGroupId));
							else
								tempdeldtlids = Util.null2String(fu.getParameter("deldtlid" + detailGroupId));
							if(!tempdeldtlids.equals(""))
								deldtlids += "," + tempdeldtlids;
						}
						if(!deldtlids.equals("")){
							deldtlids = deldtlids.replaceAll(",{2,}",",");
							if(deldtlids.indexOf(",")==0)deldtlids = deldtlids.substring(1,deldtlids.length());
							if(deldtlids.lastIndexOf(",")==deldtlids.length()-1)deldtlids = deldtlids.substring(0,deldtlids.length()-1);
						}
						if(bflength<deldtlids.length() && isStart && isTrack) {
							if(bflength>0)
								bflength++;
							//存删除的日志
							if(!deldtlids.equals("")) {
								List tmpList = Util.TokenizerString(deldtlids.substring(bflength), ",");
								for(int j=0; j<tmpList.size(); j++) {
									this.updateOrDeleteDetailLog(fields, 3, detailGroupId, tmpList.get(j).toString(), null, "Workflow_formdetail");
									//System.out.println("存删除的ID="+tmpList.get(j).toString());
								}
							}
						}
						bflength = deldtlids.length();
						/**
						 * end modified by cyril on 2008-06-25 
						 */
					    //明细行循环
						temprowindex = 0;
						for (int i = 0; i < submitidAy.length; i++) {
							//判断明细属性：新增、修改
							String Dtlid ="";
							if(isRequest)
								Dtlid = Util.null2String(request.getParameter("dtl_id_" + detailGroupId +"_"+ submitidAy[i]));
							else
								Dtlid = Util.null2String(fu.getParameter("dtl_id_" + detailGroupId +"_"+ submitidAy[i]));
							
							//新增明细
							if(Dtlid.equals("")){
								boolean hasMultiDoc = false;
								Object fieldid1 = null;
								String fieldname1 = "";
								String fieldvalue1 = "";
								String fieldhtmltype1 = "";
								String fielddbtype1 = "";
								String type1 = "";
								String sql1 = "insert into Workflow_formdetail ( requestid,groupId,";
								String sql2 = " values (" + requestid + ","+detailGroupId+" ,";
			
								int nullLength = 0;
								List newList = new ArrayList();
								for (int j = 0; j < fieldnames1.size(); j++) {
									fieldname1 = fieldnames1.get(j) + "";
									fieldid1 = fieldids1.get(j);
									fieldhtmltype1 = fieldhtmltypes1.get(j) + "";
									type1 = types1.get(j) + "";
									fielddbtype1 = fielddbtypes.get(j) + "";
									if(isRequest)
										fieldvalue1 = Util.null2String(request.getParameter("field" + fieldid1 + "_" + submitidAy[i]));
									else
										fieldvalue1 = Util.null2String(fu.getParameter("field" + fieldid1 + "_" + submitidAy[i]));

									
									//1：文档， 2：客户， 3：项目， 4：资产 权限过滤
                                    fieldvalue1 = resourceAuthorityFilter(fieldhtmltype1, type1, fieldvalue1);
									
									if (fieldvalue1.equals("")) {
			
										nullLength++;
										if (!(fieldhtmltype1.equals("2") || (fieldhtmltype1.equals("1") && (type1.equals("1")||type1.equals("5"))))&&!(fieldhtmltype1.equals("3")&&(type1.equals("2")||type1.equals("19")||type1.equals("161")||type1.equals("162")))) {//非文本的时候(去掉日期)
											fieldvalue1 = "NULL";
										}
										/*--- xwj for td3297 20051130 begin---*/
										if(fieldhtmltype1.equals("5")){
                                            fieldvalue1 = "NULL";
										}else if(fieldhtmltype1.equals("4")){   /*--- mackjoe for td5197 20061031 begin---*/
                                            fieldvalue1 = "0";
                                            nullLength--;
										}
										/*--- mackjoe for td5197 20061031 end---*/
										/*--- xwj for td3297 20051130 begin---*/
                                    }else if(!fielddbtype1.toLowerCase().startsWith("browser.") && (fielddbtype1.toUpperCase()).indexOf("NUMBER")>=0 || (fielddbtype1.toUpperCase()).indexOf("FLOAT")>=0 || (fielddbtype1.toUpperCase()).indexOf("DECIMAL")>=0){
                                    	int digitsIndex = fielddbtype1.indexOf(",");
                    					int decimaldigits = 2;
                    		        	if(digitsIndex > -1){
                    		        		decimaldigits = Util.getIntValue(fielddbtype1.substring(digitsIndex+1, fielddbtype1.length()-1).trim(), 2);
                    		        	}else{
                    		        		decimaldigits = 2;
                    		        	}
                    		        	fieldvalue1 = Util.getPointValue2(fieldvalue1, decimaldigits);
                                    }
									if (j == fieldnames1.size() - 1) {//the last
										if ((fielddbtype1.startsWith("text") || fielddbtype1.startsWith("char") || fielddbtype1.startsWith("varchar")||fielddbtype1.indexOf(".")>-1)&&!"NULL".equals(fieldvalue1)) {
											sql1 += fieldname1 + ")";
											sql2 += "'" + Util.toHtmlForSpace((Util.toHtml10(Util.htmlFilter4UTF8(fieldvalue1)))) + "')";
										}else if(fieldhtmltype1.equals("3")&&type1.equals("17")&&isoracle){
											if("NULL".equals(fieldvalue1)) fieldvalue1="";
	                                    	sql1 += fieldname1 + " ) ";
											sql2 += " '' ) ";
											if(!fieldvalue1.equals("NULL")) {
											    dtMap.put(fieldname1, fieldvalue1);
											}
	                                    } else {
											sql1 += fieldname1 + ")";
											sql2 += fieldvalue1 + ")";
										}
			
									} else {
										if ((fielddbtype1.startsWith("text") || fielddbtype1.startsWith("char") || fielddbtype1.startsWith("varchar")||fielddbtype1.indexOf(".")>-1)&&!"NULL".equals(fieldvalue1)) {
											sql1 += fieldname1 + ",";
											sql2 += "'" + Util.toHtmlForSpace((Util.toHtml10(Util.htmlFilter4UTF8(fieldvalue1)))) + "',";
										}else if(fieldhtmltype1.equals("3")&&type1.equals("17")&&isoracle){
											if("NULL".equals(fieldvalue1)) fieldvalue1="";
	                                    	sql1 += fieldname1 + ",";
											sql2 += " '' ,";
											if(!fieldvalue1.equals("NULL")) {
											    dtMap.put(fieldname1, fieldvalue1);
											}
	                                    } else {
											sql1 += fieldname1 + ",";
											sql2 += fieldvalue1 + ",";
										}
									}
									
									String tempvalueid = Util.null2String(fieldvalue1);								
									if (fieldhtmltype1.equals("3") && (type1.equals("1") || type1.equals("17"))) { // 人力资源字段
										if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")){
											hrmids += "," + tempvalueid;
											//hrmids += ", empty_clob() ";
											//dtMap.put(fieldname1, tempvalueid);
										}
									} else if (fieldhtmltype1.equals("3") && (type1.equals("7") || type1.equals("18"))) {   // 客户字段
										if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")) crmids += "," + tempvalueid;
									} else if (fieldhtmltype1.equals("3") && (type1.equals("8")|| type1.equals("135"))) {   // 项目字段
										if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")) prjids += "," + tempvalueid;
									} else if (fieldhtmltype1.equals("3") && (type1.equals("9") || type1.equals("37"))) {  // 文档字段
										if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")) docids += "," + tempvalueid;
									} else if (fieldhtmltype1.equals("3") && type1.equals("23")) {                           // 资产字段
										if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")) cptids += "," + tempvalueid;
									}									
									if ((isMultiDoc).equals(fieldid1 + "_" + submitidAy[i]))
									{
										hasMultiDoc = true;
										docrowindex = ""+temprowindex;
									}
									/**
									 * added by cyril on 2008-06-26
									 * 将匹配的值放入相应字段中
									 */
									if(isStart && isTrack) {
										for(int k=0; k<fields.size(); k++) {
											Trackdetail td = (Trackdetail) fields.get(k);
											if(td.getFieldName().equals(fieldname1)) {
												String tmpstr = Util.StringReplaceOnce(fieldvalue1, " ", "");
												if(tmpstr.equals("NULL")) {
													tmpstr = "";
												}
												td.setFieldNewText(tmpstr);
												newList.add(td);
											}
										}
									}
									/**
									 * end add by cyril on 2008-06-26
									 */
								}

								if (nullLength != fieldnames1.size() || hasMultiDoc) {
									/**
									 * added by cyril on 2008-06-26
									 * 新增时为每个字段都存下明细
									 */
									if(isStart && isTrack) {
										sn++;
										//System.out.println("A----SN="+sn);
										for(int j=0; j<fields.size(); j++) {
											Trackdetail td = (Trackdetail) fields.get(j);
											td.setOptType(1);//类型为新增
											for(int k=0; k<newList.size(); k++) {
												Trackdetail tmptd = (Trackdetail) newList.get(k);
												//System.out.println("tmp="+tmptd.getFieldNewText());
												if(td.getFieldName().equals(tmptd.getFieldName())) {
													td.setFieldNewText(tmptd.getFieldNewText());
												}
											}
											if(!this.insertDetail(td)) {//每个字段都要存明细
												return false;
											}
										}
									}
									/**
									 * end add by cyril on 2008-06-26
									 */
									temprowindex ++;
									executesuccess = rs.executeSql(sql1 + sql2);
									
									
									if (!executesuccess) {
		                                //在新增的时候检测billfield表明细字段是否和物理表字段相同
		                                boolean _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(formid,billtablename,"workflow_formdetail",isbill,null,null);
		                                if(!_result){
		                                    saveRequestLog("1");    
		                                    this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
		                                    return false;
		                                }
		                                executesuccess = rs.executeSql(sql1 + sql2);
		                                if (!executesuccess) {
		                                    saveRequestLog("1");
		                                    writeLog("执行出错的SQL:"+sql1 + sql2);
		                                    //this.setMessagecontent(WorkflowRequestMessage.getBottomInfo(SystemEnv.getHtmlLabelName(126573, userlanguage), workflowid, WorkflowRequestMessage.WF_FORMFIELD_SET).toString());
		                                    this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
		                                    return false;
		                                }
		                            }
									
									
									int detailRowNum = 0;
									rs.executeSql("select max(id) dtid from Workflow_formdetail where requestid =" + requestid + " and groupId = "+ detailGroupId);
                                    if(rs.next()){
                                        detailRowNum = Util.getIntValue(rs.getString("dtid"));
                                    }
                                    newAddDetailRowPerInfo.put("dtl_id_" + detailGroupId +"_"+ submitidAy[i], detailRowNum);
                                    
									//////////////////////
			                            /***明细多人力clob***/
			                            
			                			if(isoracle){
			                				try {
			                					//String maxidsql = "select max(id) dtid from Workflow_formdetail where requestid =" + requestid + " and groupId = "+ detailGroupId;
			                					//rs.executeSql(maxidsql);
			                					//if(rs.next()){
			                				    if (detailRowNum > 0) {
			                						String dtid = detailRowNum + "";//Util.null2String(rs.getString("dtid"));
			                						if(!"".equals(dtid)){
			                							String hrmsql = "update workflow_formdetail set ";
					                					int hrmindex = 0;
					                					String hrmsp = " ";
					                					Iterator fdit = dtMap.entrySet().iterator();
					                					while(fdit.hasNext()) {
					                						hrmindex++;
					                						Map.Entry entry = (Map.Entry) fdit.next();
					                						String key = entry.getKey().toString();
					                						String value = "";
					                						if(entry.getValue()!=null) {
					                							if(String.valueOf(entry.getValue()).equals(" ")) value = "";
					                							else value = String.valueOf(entry.getValue());
					                						}
					                						hrmsql += hrmsp+" "+key+"=? ";
		        		            						hrmsp = ",";
					                					}
					                					hrmsql += " where id = "+dtid+" and requestid = " + requestid + " and groupid = "+ detailGroupId;
					                					
					                					//System.out.println("hrmsql = "+hrmsql);
					                					if(hrmindex>0) {
					                						ConnStatement hrmsta = null;
		        		            						try {
		        		            							hrmsta = new ConnStatement();
		        		            							hrmsta.setStatementSql(hrmsql);
		        		            							fdit = dtMap.entrySet().iterator();
		        		            							hrmindex = 0;
		        		            							while(fdit.hasNext()) {
		        		            								hrmindex++;
		        		            								Map.Entry entry = (Map.Entry) fdit.next();
		        		            								String key = entry.getKey().toString();
		        		            								String value = "";
		        		            								if(entry.getValue()!=null) {
		        		            									if(String.valueOf(entry.getValue()).equals(" ")) value = "";
		        		            									else value = String.valueOf(entry.getValue());
		        		            								}
		        		            								hrmsta.setString(hrmindex, value);
		        		            							}
		        		            							hrmsta.executeUpdate();
		        		            						} catch (Exception e) {
					                							writeLog(e);
					                							saveRequestLog("1");
	                                                            this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_01);
					                							return false;
					                						} finally {
					                							if(hrmsta!=null) hrmsta.close();
					                						}
					                					}
			                						}
			                					}
			                				}catch(Exception e){
			                					writeLog(e);
			                					saveRequestLog("1");
			                					this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_01);
			                					return false;
			                				}
			                			//
			                			}
			                        /******/
									//////////////////////
								}
			
								if (!executesuccess) {
									saveRequestLog("1");
									this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
									return false;
								}
							}else if(checkIdDel(deldtlids,Dtlid)){ //检查是否删除的ID
							    fieldTypeCache.clear();
								//修改明细
								Object fieldid1 = null;
								String fieldname1 = "";
								String fieldvalue1 = "";
								String fieldhtmltype1 = "";
								String fielddbtype1 = "";
								String type1 = "";
								String sql1 = "";
								Map modifyMap = new HashMap();
			
								for (int j = 0; j < fieldnames1.size(); j++) {
									fieldname1 = fieldnames1.get(j) + "";
									fieldid1 = fieldids1.get(j);
									fieldhtmltype1 = fieldhtmltypes1.get(j) + "";
									type1 = types1.get(j) + "";
									fielddbtype1 = fielddbtypes.get(j) + "";
			
									if(isRequest)
										fieldvalue1 = Util.null2String(request.getParameter("field" + fieldid1 + "_" + submitidAy[i]));
									else
										fieldvalue1 = Util.null2String(fu.getParameter("field" + fieldid1 + "_" + submitidAy[i]));
                                    String tempvalueid = Util.null2String(fieldvalue1);
									if (fieldhtmltype1.equals("3") && (type1.equals("1") || type1.equals("17"))) { // 人力资源字段
										if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")){
											hrmids += "," + tempvalueid;
											//hrmids += ", empty_clob() ";
											//dtMap.put(fieldname1, tempvalueid);
										}
									} else if (fieldhtmltype1.equals("3") && (type1.equals("7") || type1.equals("18"))) {   // 客户字段
										if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")) crmids += "," + tempvalueid;
									} else if (fieldhtmltype1.equals("3") && (type1.equals("8")|| type1.equals("135"))) {   // 项目字段
										if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")) prjids += "," + tempvalueid;
									} else if (fieldhtmltype1.equals("3") && (type1.equals("9") || type1.equals("37"))) {  // 文档字段
										if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")) docids += "," + tempvalueid;
									} else if (fieldhtmltype1.equals("3") && type1.equals("23")) {                           // 资产字段
										if (!tempvalueid.equals("")&&!tempvalueid.equals("NULL")) cptids += "," + tempvalueid;
									}
									if ((isMultiDoc).equals(fieldid1 + "_" + submitidAy[i]))
									{
										docrowindex = ""+temprowindex;
									}
                                    if(detaileditfields.indexOf(fieldid1)<0){
                                        continue;
                                    }
                                    
                                    if((fieldhtmltype1.equals("1") &&"1".equals(type1))||(fieldhtmltype1.equals("2") && "1".equals(type1) && isoracle)){
                                        fieldTypeCache.put(fieldname1,fielddbtype1);
                                    }
                                    
                                    if(fieldhtmltype1.equals("6")){
										//附件上传

										DocExtUtil docExtUtil = new DocExtUtil();
										String[] oldUploadIdsStrs = null;
										String oldUploadIdsStrs_ = "";
										RecordSet rsUploadId = new RecordSet();
										
										rsUploadId.executeSql("select "+fieldname1+" from Workflow_formdetail where id="+Dtlid);
										
										if(rsUploadId.next()){
											oldUploadIdsStrs_ = rsUploadId.getString(fieldname1);
										}
										if(oldUploadIdsStrs_ != null && !"".equals(oldUploadIdsStrs_)){
											oldUploadIdsStrs = Util.TokenizerString2(oldUploadIdsStrs_, ",");
										}
										
										if(oldUploadIdsStrs != null){
											for( int y =0; y < oldUploadIdsStrs.length; y++){
												if(fieldvalue1.indexOf(oldUploadIdsStrs[y]) == -1){
													//if( Util.getIntValue(oldUploadIdsStrs[y],0) != 0 )
													//docExtUtil.deleteDoc(Integer.parseInt(oldUploadIdsStrs[y]));
													if( Util.getIntValue(oldUploadIdsStrs[y],0) != 0 ){
														String clientIp = "";
														if(isRequest){
															if (request != null)
																clientIp = Util.null2String(request.getRemoteAddr());
														}else{
															if (fu != null)
																clientIp = Util.null2String(fu.getRemoteAddr());
														}						
														docExtUtil.deleteDoc(Integer.parseInt(oldUploadIdsStrs[y]),user,clientIp);							
													}					
												}
											}
										}
										/* ----- added by xwj for td1949 end ------*/
									}
                                    
                                    //1：文档， 2：客户， 3：项目， 4：资产 权限过滤
                                    fieldvalue1 = resourceAuthorityFilter(fieldhtmltype1, type1, fieldvalue1);
                                    
									//空值处理
									if (fieldvalue1.equals("")) {
										if (!(fieldhtmltype1.equals("2") || (fieldhtmltype1.equals("1") && (type1.equals("1")||type1.equals("5"))))&&!(fieldhtmltype1.equals("3")&&(type1.equals("2")||type1.equals("19")||type1.equals("161")||type1.equals("162")))) {//非文本的时候(去掉日期)
											fieldvalue1 = "NULL";
										}
										if(fieldhtmltype1.equals("5")){
											fieldvalue1 = "NULL";
										}
                                        /*--- mackjoe for td5197 20061031 begin---*/
                                        if(fieldhtmltype1.equals("4")){
                                            fieldvalue1 = "0";
                                        }
                                        /*--- mackjoe for td5197 20061031 end---*/
                                     }else if(!fielddbtype1.toLowerCase().startsWith("browser.") && (fielddbtype1.toUpperCase()).indexOf("NUMBER")>=0 || (fielddbtype1.toUpperCase()).indexOf("FLOAT")>=0 || (fielddbtype1.toUpperCase()).indexOf("DECIMAL")>=0){
                                        	int digitsIndex = fielddbtype1.indexOf(",");
                        					int decimaldigits = 2;
                        		        	if(digitsIndex > -1){
                        		        		decimaldigits = Util.getIntValue(fielddbtype1.substring(digitsIndex+1, fielddbtype1.length()-1).trim(), 2);
                        		        	}else{
                        		        		decimaldigits = 2;
                        		        	}
                        		        	fieldvalue1 = Util.getPointValue2(fieldvalue1, decimaldigits);
                                     }
									//更新串拼装
									if (sql1.equals("")) {
										if ((fielddbtype1.startsWith("text") || fielddbtype1.startsWith("char") || fielddbtype1.startsWith("varchar") || fielddbtype1.startsWith("browser"))&&!"NULL".equals(fieldvalue1)) {
											sql1 += fieldname1 + "=" + "'" + Util.toHtmlForSpace(Util.toHtml10(Util.htmlFilter4UTF8(fieldvalue1))) + "' ";
										} else if(fieldhtmltype1.equals("3")&&type1.equals("17")&&isoracle){
											if("NULL".equals(fieldvalue1)) fieldvalue1="";
	                                    	sql1 += fieldname1 + " = '' ";
	                                    	if(!fieldvalue1.equals("NULL")) {
	                                    	    dtMap.put(fieldname1, fieldvalue1);
	                                    	}
	                                    }else {
											sql1 += fieldname1 + "=" + fieldvalue1 + " ";
										}
			
									} else {
										if ((fielddbtype1.startsWith("text") || fielddbtype1.startsWith("char") || fielddbtype1.startsWith("varchar") || fielddbtype1.startsWith("browser"))&&!"NULL".equals(fieldvalue1)) {
											sql1 += "," + fieldname1 + "=" + "'" + Util.toHtmlForSpace(Util.toHtml10(Util.htmlFilter4UTF8(fieldvalue1))) + "' ";
										} else if(fieldhtmltype1.equals("3")&&type1.equals("17")&&isoracle){
											if("NULL".equals(fieldvalue1)) fieldvalue1="";
	                                    	sql1 += "," + fieldname1 + " = '' ";
	                                    	if(!fieldvalue1.equals("NULL")) {
	                                    	    dtMap.put(fieldname1, fieldvalue1);
	                                    	}
	                                    }else {
											sql1 += "," + fieldname1 + "=" + fieldvalue1 + " ";
										}
									}
									
									String tmpStr = Util.StringReplaceOnce(fieldvalue1, " ", "");
									if(tmpStr.equals("NULL"))
										tmpStr = "";
									modifyMap.put(fieldname1, tmpStr);//用来对比是否做过改动 by cyril on 2008-06-26 for TD:8835
								}
			
								if (!sql1.equals("")) {
									if(isStart && isTrack) {
										this.updateOrDeleteDetailLog(fields, 2, detailGroupId, Dtlid, modifyMap, "Workflow_formdetail");//先要将修改的原记录存入LOG by cyril for TD:8835
									}
									temprowindex ++;
									executesuccess = rs.executeSql("update Workflow_formdetail set "+sql1+" where id="+Dtlid);
									
									if(!executesuccess){
									    boolean _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(formid,billtablename,"workflow_formdetail",isbill,fieldTypeCache,sql1);
									    if(!_result){
                                            saveRequestLog("1");    
                                            this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
                                            return false;
                                        }
                                        executesuccess = rs.executeSql("update Workflow_formdetail set "+sql1+" where id="+Dtlid);
                                        if (!executesuccess) {
                                            saveRequestLog("1");
                                            writeLog("执行出错的SQL:update Workflow_formdetail set "+sql1+" where id="+Dtlid);
                                            //this.setMessagecontent(WorkflowRequestMessage.getBottomInfo(SystemEnv.getHtmlLabelName(126573, userlanguage), workflowid, WorkflowRequestMessage.WF_FORMFIELD_SET).toString());
                                            this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
                                            return false;
                                        }
									    
									}
									

									//////////////////////
			                            /***明细多人力clob***/
			                            
			                			if(isoracle){
			                				try {
			                					String hrmsql = " update Workflow_formdetail set ";
			                					int hrmindex = 0;
			                					String hrmsp = " ";
			                					Iterator fdit = dtMap.entrySet().iterator();
			                					while(fdit.hasNext()) {
			                						hrmindex++;
			                						Map.Entry entry = (Map.Entry) fdit.next();
			                						String key = entry.getKey().toString();
			                						String value = "";
			                						if(entry.getValue()!=null) {
			                							if(String.valueOf(entry.getValue()).equals(" ")) value = "";
			                							else value = String.valueOf(entry.getValue());
			                						}
			                						hrmsql += hrmsp+" "+key+"=? ";
	    		            						hrmsp = ",";
			                					}
			                					hrmsql += " where id =" + Dtlid;
			                					
			                					//System.out.println("hrmsql = "+hrmsql);
			                					if(hrmindex>0) {
			                						ConnStatement hrmstatement = null;
			                						try {
			                							hrmstatement = new ConnStatement();
			                							hrmstatement.setStatementSql(hrmsql);
	    		            							fdit = dtMap.entrySet().iterator();
	    		            							hrmindex = 0;
	    		            							while(fdit.hasNext()) {
	    		            								hrmindex++;
	    		            								Map.Entry entry = (Map.Entry) fdit.next();
	    		            								String key = entry.getKey().toString();
	    		            								String value = "";
	    		            								if(entry.getValue()!=null) {
	    		            									if(String.valueOf(entry.getValue()).equals(" ")) value = "";
	    		            									else value = String.valueOf(entry.getValue());
	    		            								}
	    		            								hrmstatement.setString(hrmindex, value);
	    		            							}
	    		            							hrmstatement.executeUpdate();
	    		            						} catch (Exception e) {
			                							writeLog(e);
			                							saveRequestLog("1");
			                							this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_01);
			                							return false;
			                						} finally {
			                							if(hrmstatement!=null) hrmstatement.close();
			                						}
			                					}
			                				}catch(Exception e){
			                					writeLog(e);
			                					saveRequestLog("1");
                                                this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_01);
			                					return false;
			                				}
			                			//
			                			}
			                        /******/
									//////////////////////
									if (!executesuccess) {
										saveRequestLog("1");
										this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
										return false;
									}
								}
							}
							dtMap.clear();
						}
						fieldids1.clear();
						fieldnames1.clear();
						fieldhtmltypes1.clear();
						fielddbtypes.clear();
						types1.clear();
						rows++;
					}
	                
					//删除被删除的原有明细
					if (!deldtlids.equals("")) {
						rs.executeSql("delete from Workflow_formdetail where id in(" +deldtlids+")" );
					}
				}
			}
		    catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (!hrmids.equals("")) hrmids = hrmids.substring(1);
		if (!crmids.equals("")) crmids = crmids.substring(1);
		if (!prjids.equals("")) prjids = prjids.substring(1);
		if (!docids.equals("")) docids = docids.substring(1);
		if (!cptids.equals("")) cptids = cptids.substring(1);
		
		
		try{
			if(src.equals("submit")){
				//财务--费用控制(校验)
				FnaBudgetControl fbc = new FnaBudgetControl();
				StringBuffer errorInfo = new StringBuffer();
				fnaWfValidatorFlag = fbc.getFnaWfValidator(workflowid, formid, requestid, creater, user, errorInfo, 0);
				
				if(!fnaWfValidatorFlag){
	                this.setMessageid(WorkflowRequestMessage.WF_CUSTOM_ERROR);
					setMessagecontent(errorInfo.toString());
				}else{
					//财务--费用标准(校验)
					CostStandard csUtil = new CostStandard();
					Map map = csUtil.getCostStandardMsg(workflowid, requestid, creater);
					if("false".equals((String)map.get("flag"))){
						fnaCostStandardFlag = false;
		                this.setMessageid(WorkflowRequestMessage.WF_CUSTOM_ERROR);
						setMessagecontent((String)map.get("errorInfo"));
					}
				}
			}
		}catch(Exception e){
			fnaCostStandardFlag = false;
			fnaWfValidatorFlag = false;
            this.setMessageid(WorkflowRequestMessage.WF_CUSTOM_ERROR);
			setMessagecontent("程序异常,请联系系统管理员");
			new BaseBean().writeLog(e);
		}
		
		 if(fnaCostStandardFlag && fnaWfValidatorFlag){
			// 开始节点自动赋值操作(用在处理节点前，节点后等的赋值操作)
	        String rejectbackflag = "";//退回时，当前节点的节点后附加操作是否触发。rejectbackflag=1，表示触发。
	        RecordSet tempRecordSet = new RecordSet(); 
	        tempRecordSet.executeSql("select rejectbackflag from workflow_flownode where workflowid="+workflowid+" and nodeid="+nodeid);
	        if(tempRecordSet.next()){
	        	rejectbackflag = Util.null2String(tempRecordSet.getString("rejectbackflag"));
	        }		
       
	        if((!src.equals("save")&&(!src.equals("reject")||(src.equals("reject")&&!rejectbackflag.equals("1"))))&& enableIntervenor==1){//用于修改，保存也会执行节点后附加操作
				//判断当前节点是否为流程存为文档
				//rs.executeSql("select * from workflow_addinoperate  where workflowid="+workflowid+" and objid="+nodeid+" and ispreadd=0 and type=2  and customervalue='action.WorkflowToDoc' ");
				rs.executeSql("select * from workflowactionview  where workflowid="+workflowid+" and nodeid="+nodeid+" and ispreoperator='0' and (id='action.WorkflowToDoc' or id='WorkflowToDoc') and isused = 1");									
				if(rs.next())
				 {
					   isWorkFlowToDoc=true;
				 }
				try {
					 //由于objtype为"1: 节点自动赋值",不为"0 :出口自动赋值"，不用改变除状态外的文档相关信息，故可不用给user、clienIp、src赋值  fanggsh TD5121			
					RequestCheckAddinRules requestCheckAddinRules = new RequestCheckAddinRules();
					requestCheckAddinRules.resetParameter();
					//add by cyril on 2008-07-28 for td:8835 事务无法开启查询,只能传入
		            requestCheckAddinRules.setTrack(isTrack);
		            requestCheckAddinRules.setStart(isStart);
		            requestCheckAddinRules.setNodeid(nodeid);
		            //end by cyril on 2008-07-28 for td:8835
					requestCheckAddinRules.setRequestid(requestid);
					requestCheckAddinRules.setWorkflowid(workflowid);
					requestCheckAddinRules.setObjid(nodeid);
					requestCheckAddinRules.setObjtype(1);               // 1: 节点自动赋值 0 :出口自动赋值
					requestCheckAddinRules.setIsbill(isbill);
					requestCheckAddinRules.setFormid(formid);
					requestCheckAddinRules.setIspreadd("0");//xwj for td3130 20051123
					requestCheckAddinRules.setRequestManager(this);
					requestCheckAddinRules.setUser(user);
					String clientIp = "";
                                     if(isRequest){
                                         if (request != null)
                                             clientIp = Util.null2String(Util.getIpAddr(request));
                                     }else{
                                         if (fu != null)
                                             clientIp = Util.null2String(fu.getRemoteAddr());
                                     }                       
                                     requestCheckAddinRules.setClientIp(clientIp);//add by fanggsh 20061016 fot TD5121
                                     requestCheckAddinRules.setSrc(src);
					requestCheckAddinRules.checkAddinRules();
					if(requestCheckAddinRules.getDoDmlResult().startsWith("-1")){
						if("-1".equals(requestCheckAddinRules.getDoDmlResult())){
							this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_07);
							this.setMessagecontent(WorkflowRequestMessage.resolveDetailInfo(SystemEnv.getHtmlLabelNames("18010,83071", user.getLanguage())));
						}else{
							this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_07);
							this.setMessagecontent(WorkflowRequestMessage.resolveDetailInfo(SystemEnv.getHtmlLabelNames("18010,83071,127353,82986", user.getLanguage())+":"+requestCheckAddinRules.getDoDmlResult().replace("-1,", "")));
						}
						return false;
					}
					
					requestCheckAddinRulesMap=new HashMap();
					requestCheckAddinRulesMap.put("objId",""+nodeid);
					requestCheckAddinRulesMap.put("objType","1");// 1: 节点自动赋值 0 :出口自动赋值
					requestCheckAddinRulesMap.put("isPreAdd","0");			
					requestCheckAddinRulesList.add(requestCheckAddinRulesMap);
				} catch (Exception erca) {
					////writeLog(erca);
					saveRequestLog("1");
	                this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_07);
		            if(erca.getMessage().indexOf("workflow interface action error")>-1){
		                            writeLog(erca);
                    }
		            if("".equals(this.getMessage())){
		                this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_07);
		            }
		            if("".equals(this.getMessagecontent())){
		                this.setMessagecontent(WorkflowRequestMessage.resolveDetailInfo(SystemEnv.getHtmlLabelNames("18010,83071", user.getLanguage())));
		            }
		            return false;
				}
				}
	        }
		}
        //###################Save the detailed form data END// #############
        //added by mackjoe at 2007-01-11 TD5731 将流程编号从option页面移到这里处理,解决一些使用特殊option的单据无法使用流程编号功能
        //生成编号放在流向下一节点的时候  fanggsh 20070719 for TD6963
//        if(iscreate.equals("1")){
//            CodeBuild cbuild = new CodeBuild(formid);
//            String  codeFields=cbuild.haveCode();
//            if (!codeFields.equals("")){
//                cbuild.getFlowCodeStr(requestid,isbill);
//            }
//        }

        //当表单内容保存时，根据请求id插入督办权限表
        if(!src.equals("submit")){
            WFUrgerManager WFUrgerManager = new WFUrgerManager();
            WFUrgerManager.setLogintype(Util.getIntValue(user.getLogintype()));
            WFUrgerManager.setUserid(user.getUID());
            WFUrgerManager.insertUrgerByRequestid(requestid);
        }
        
		if(!isRequest){
			request=fu.getRequest();
		}
		if(request != null){
			HttpSession session = (HttpSession) request.getSession(false);
			session.setAttribute("requestidForAllBill",String.valueOf(requestid));
		}
		try{
			WorkflowRequestComInfo workflowRequestComInfo = new WorkflowRequestComInfo();
			workflowRequestComInfo.deleteRequestInfoCache(""+this.requestid);
		}catch(Exception e){
			
		}
		
		//从requet取自由流程设置信息保存
	    if(this.fu != null){
	    	int requestid = this.getRequestid() ;
	    	int nodeid = this.getNodeid();
	    	saveFreeWorkflowSetting(this.fu, this.workflowid, 
	          requestid, nodeid, this.user.getLanguage());
	    }

	    try{
			//更新流程请求标题字段Start
			new SetNewRequestTitle().getAllRequestName(rs,requestid+"",requestname,workflowid+"",nodeid+"",formid,isbill,userlanguage);
			//更新流程请求标题字段End
		}catch(Exception e){
		}

	    try{
	    	if(fnaCostStandardFlag && fnaWfValidatorFlag){
		    	int userId = 0;
		    	if(user!=null){
		    		userId = user.getUID();
		    	}
				//插入 借款、还款（冲销借款）、报销（冲销借款）流程的借款（冲销）金额 调整记录
				new FnaBorrowAmountControl().saveFnaAmountAdjustLogs(workflowid, formid, nodeid, src, requestid, userId);
	    	}
		}catch(Exception e){
			new BaseBean().writeLog(e);
		}

	    try{
	    	int userId = 0;
	    	if(user!=null){
	    		userId = user.getUID();
	    	}
			//插入 借款、还款（冲销借款）、报销（冲销借款）流程的借款（冲销）金额 调整记录
			new FnaAdvanceAmountControl().saveFnaAmountAdjustLogs(workflowid, formid, nodeid, src, requestid, userId);
		}catch(Exception e){
			new BaseBean().writeLog(e);
		}
		return true;
	}


    private void addLenMsg(String fieldid,int len) {
        String fieldname = "";
        if(isbill == 1){
            RecordSet temprs = new RecordSet();
            temprs.executeQuery("select b.indexdesc from workflow_billfield a,htmllabelindex b where a.fieldlabel = b.id and a.id = ?", fieldid);
            if(temprs.next()){
                fieldname = Util.null2String(temprs.getString(1));
            }
        }else{
            FormFieldlabelMainManager ffmm = new FormFieldlabelMainManager();
            ffmm.resetParameter();
            ffmm.setFormid(formid);
            ffmm.setFieldid(Util.getIntValue(fieldid,0));
            ffmm.setLanguageid(userlanguage);
            try {
                ffmm.selectSingleFormField();
            } catch (Exception e) {
                e.printStackTrace();
            }
            fieldname = ffmm.getFieldlabel();
        }
        
        JSONObject msgobj = new JSONObject();
        try {
            msgobj.put("details", WorkflowRequestMessage.assemMsgInfo(SystemEnv.getHtmlLabelName(126571,userlanguage), fieldname,String.valueOf(len)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
        this.setMessagecontent(msgobj.toString());
    }
	
    
    /**
     * 检测自定义表单,billfile表字段和物理表字段是否对应，若不对应则在物理表中添加对应字段
     * @param isoracle 
     * @param formid 
     * @param billtablename
     * @param detailtablename
     * @return
     */
            
            
    
        
     
    /**
     * 当需要保存的数据长度超过表字段长度，则自动增加表字段长度
     * @param textvalue  要保存的值
     * @param fieldid     
     * @param fieldname
     * @param fielddbtype
     * @param billtable
     * @return
     */
        
        //如果数据长度大于字段长度
            //String _sql = "update workflow_billfield set fielddbtype = ? where id = ?"; 
            //_rs.executeUpdate(_sql, fielddbtype,fieldid);
            //修改表字段长度
    
	
	/**
     * 将request中的参数转化为map
     * @param request
     * @return
     */
    private Map<String, String> convertRequestToMap(FileUpload fu){
    	Map<String, String> params = new HashMap<String, String>();  
    	if(fu == null) return params;
    	try{
    		Enumeration<String> paramNames = fu.getParameterNames();  
	        while( paramNames.hasMoreElements() ) {  
	        	String paramName = paramNames.nextElement();  
	        	String[] paramValues = fu.getParameterValues(paramName);  
	        	if (paramValues.length >= 1) {  
	        		String paramValue = paramValues[0];  
	        		//处理中文乱码
	        		//paramValue = new String(paramValue.getBytes("ISO8859_1") ,"UTF-8");
	        		if (paramValue.length() != 0) {  
	        			params.put(paramName, paramValue);  
	        		}  
	        	} 
	        }
    	}catch(Exception e){
    		return params;
    	}
        return params;
    }
	
	 /**
	 * 保存自由流程设置信息
	 * @param request
	 * @param requestId
	 */
	 private void saveFreeWorkflowSetting(FileUpload fu, int workflowid, int requestId, int nodeid, int language){
		 Map<String, String> params = this.convertRequestToMap(fu);

		 boolean freeWorkflowChanged = Boolean.valueOf(params.get("freeWorkflowChanged"));
		 if (freeWorkflowChanged) {
			 int freeNode = Util.getIntValue(params.get("freeNode"),0);
			 int freeDuty = Util.getIntValue(params.get("freeDuty"),0);

			 WFFreeFlowManager wfFreeFlowManager = new WFFreeFlowManager();
			 if( freeNode == 1 ){
				 if(freeDuty == 1 || "0".equals(nodetype)){
					 wfFreeFlowManager.SaveFreeFlow(params,requestId,nodeid,language);
				 }else if(freeDuty == 2){
					 wfFreeFlowManager.SaveFreeFlowForAllChildren(params,workflowid,requestId,nodeid,language);
				 }
			 }
		 }
	 }

	/**
	 * 为了兼容老方法才加上的
	 * @return
	 */
	private boolean updateOrDeleteDetailLog(List fields, int optType, int detailGroupId, String id, Map modifyMap) {
		return updateOrDeleteDetailLog(fields, optType, detailGroupId, id, modifyMap, "Workflow_formdetail");
	}
	
	
/**
 * created by cyril on 2008-06-26
 * 功能:在做更新或删除明细时记录修改日志
 * @param fields		字段名称
 * @param optType		当前的操作类型 2:修改; 3:删除
 * 						2:修改有二种情况,一种是在修改前,modifyMap不为空,另一种是在修改后,只为将修改的记录取出后保存modifyMap==null
 * @param detailGroupId	当前明细的组别
 * @param id			修改记录的ID
 * @param modifyMap		修改的内容
 * @return
 */
private boolean updateOrDeleteDetailLog(List fields, int optType, int detailGroupId, String id, Map modifyMap, String detailTableName) {
	if(detailTableName==null || "".equals(detailTableName)){
		return true;
	}
	StringBuffer s = new StringBuffer();
	StringBuffer fieldNameStr = new StringBuffer();
	s.append("select ");
	for(int k=0; k<fields.size(); k++) {
		Trackdetail td = (Trackdetail) fields.get(k);
		fieldNameStr.append(td.getFieldName());
		if(k<(fields.size()-1)) fieldNameStr.append(",");
		else fieldNameStr.append(" ");
	}
	if(fieldNameStr.toString().trim().equals("")){
		return true;
	}
	s.append(" "+fieldNameStr+" ");
	s.append("from "+detailTableName+" ");
	s.append("where id="+id);
	executesuccess = rs.executeSql(s.toString());
	//System.out.println("取原记录:"+s.toString());
	if (!executesuccess) {
		saveRequestLog("1");
		return false;
	}
	boolean isModify = false;
	if(optType!=2) isModify = true;
	//取得原记录
	List oldList = new ArrayList();
	if(rs.next()) {
		for(int i=0; i<fields.size(); i++) {
			Trackdetail td = (Trackdetail) fields.get(i);
			td.setFieldOldText(rs.getString(td.getFieldName()));
			//System.out.println("字段名称:"+td.getFieldName()+" 值="+rs.getString(td.getFieldName()));
			oldList.add(td);
		}
	}
	
	//判断记录是否被修改
	if(optType==2) {
		WFModeNodeFieldManager wFModeNodeFieldManager=new WFModeNodeFieldManager();
		boolean isMode=wFModeNodeFieldManager.getIsModeByWorkflowIdAndNodeId(workflowid, nodeid);
		//取得节点显示的字段,不显示的不判断
		Map verifyMap = new HashMap();
		String sql = " select distinct b.fieldname "+
		  " from workflow_formfield a, workflow_formdictdetail b "+
		  " where a.isdetail='1' and a.fieldid=b.id and a.formid=" + formid+" and a.groupId=" + detailGroupId+
		  " and a.fieldid in (select fieldid from ";
		  if(isMode) sql += " workflow_modeview ";
		  else sql += " workflow_nodeform ";
		  sql += "where isView=1 and nodeid="+nodeid+")";
        if(!detailTableName.equals("Workflow_formdetail")){
            sql = " select fieldname "+
		  " from workflow_billfield "+
		  " where viewtype='1' and billid=" + formid+
		  " and id in (select fieldid from ";
		  if(isMode) sql += " workflow_modeview ";
		  else sql += " workflow_nodeform ";
		  sql += "where isView=1 and nodeid="+nodeid+")";
        }
		//System.out.println("SQL="+sql);
		rs.executeSql(sql);
		while(rs.next()) {
			verifyMap.put(rs.getString("fieldname"), "C");
		}
		for(int i=0; i<oldList.size(); i++) {
			Trackdetail td = (Trackdetail) oldList.get(i);
			if(verifyMap.get(td.getFieldName())!=null && modifyMap.get(td.getFieldName())!=null && !modifyMap.get(td.getFieldName()).toString().equals(td.getFieldOldText())) {
				isModify = true;
				break;
			}
		}
	}
	if(isModify) {
		sn++;
		//System.out.println("MorD----SN="+sn);
		//将删除或修改过的明细记录放入日志表
		for(int k=0; k<oldList.size(); k++) {
			Trackdetail td = (Trackdetail) oldList.get(k);
			td.setFieldGroupId(detailGroupId);
			td.setOptType(optType);//类型
			if(optType==2 && modifyMap.get(td.getFieldName())!=null) {
				td.setFieldNewText(modifyMap.get(td.getFieldName()).toString());
			}
			if(!this.insertDetail(td)) {//插入明细日志;
				return false;
			}
		}
	}
	return true;
}

	
/**
 * 明细修改日志均为插入动作,所以本方法抽取公用
 * created by cyril on 2008-06-26
 * @param td	明细的对象类
 * @return
 */
private boolean insertDetail(Trackdetail td) {
	StringBuffer s = new StringBuffer();
	s.append("insert into workflow_trackdetail (");
	s.append("sn, optKind,optType,requestId,nodeId,isBill,");
	s.append("fieldLableId,fieldGroupId,fieldId,fieldHtmlType,fieldType,fieldNameCn,fieldNameEn,fieldNameTw,");
	s.append("fieldOldText,fieldNewText,modifierType,agentId,modifierId,modifierIP,modifyTime) ");
	s.append("values (");
	s.append(this.sn+",");
	s.append(this.disposeSqlNull(td.getOptKind())+",");
	s.append(td.getOptType()+",");
	s.append(td.getRequestId()+",");
	s.append(td.getNodeId()+",");
	s.append(isbill+",");
	s.append(td.getFieldLableId()+",");
	s.append(td.getFieldGroupId()+",");
	s.append(td.getFieldId()+",");
	s.append(this.disposeSqlNull(td.getFieldHtmlType())+",");
	s.append(this.disposeSqlNull(td.getFieldType())+",");
	s.append(this.disposeSqlNull(td.getFieldNameCn())+",");
	s.append(this.disposeSqlNull(td.getFieldNameEn())+",");
	s.append(this.disposeSqlNull(td.getFieldNameTw())+",");
	s.append(this.disposeSqlNull(Util.toHtml(Util.null2String(td.getFieldOldText())))+",");
	s.append(this.disposeSqlNull(Util.toHtml(Util.null2String(td.getFieldNewText())))+",");
	s.append(usertype+",");
	s.append(agentId+",");
	s.append(userid+",");
	s.append(this.disposeSqlNull(td.getModifierIP())+",");
	s.append(this.disposeSqlNull(td.getModifyTime()));
	s.append(")");
	executesuccess = rs.executeSql(s.toString());//插入明细日志);
	if (!executesuccess) {
		saveRequestLog("1");
		//return false;
	}
	//System.out.println("detail insert sql="+s.toString());
	return true;
}
	
	/**
	 * 检查ID是否被删除
	 * @param delIds	已删除列表
	 * @param id		检查的ID
	 * @return
	 */
	private boolean checkIdDel(String delIds, String id) {
		if(delIds!=null && !delIds.equals("")) {
			List list = Util.TokenizerString(delIds, ",");
			for(int i=0; i<list.size(); i++) {
				if(list.get(i).toString().equals(id)) {
					return false;
				}
			}
		}
		return true;
	}
    /**
     * 请求流到下一个节点
     * @return 是否流到下一个节点
     */
	public boolean flowNextNode() {
		
	    //---------------------------------------------------------------------------------------------------
        // 用于线程同步，防止同一节点操作人重复提交
        // 当方法：flownextnode开始执行时，会将当前节点的请求放入application变量(GCONST.WFProcessing)中
        // 当前节点的其他操作人，必须等待application变量(GCONST.WFProcessing)中没有当前请求时，才可以继续执行
        // application变量(GCONST.WFProcessing)中的当前请求会在方法：flownextnode执行完毕后清除
        // 此方法必须保证在执行完成后，从GCONST.WFProcessing中清除当前请求！
        //---------------------------------------------------------------------------------------------------
	    //初始化操作日志对象
        RequestOperationLogManager rolm = null;
        try {
            removeErrorMsg();
        	ConnStatement statement = null;
		ArrayList poppuplist=new ArrayList();
		//add by liaodong for qc76119,76120,54194,64652 start
		String penetrateFlag = "";  
		String lastNodeId = "";
		//end
		//add by liaodong for qc48451 in 2013年10月21日 start
		boolean isfixbill = false;
		//end
        WFForwardManager wffm=new WFForwardManager();
        WFCoadjutantManager wfcm=new WFCoadjutantManager();
        wffm.init();
        wffm.setWorkflowid(workflowid);
        wffm.setNodeid(nodeid);
        wffm.setIsremark(""+isremark);
        wffm.setRequestid(requestid);
        wffm.setBeForwardid(RequestKey);
        wffm.getWFNodeInfo();
        IsBeForwardPending=wffm.getIsBeForwardPending();
        IsSubmitedOpinion=wffm.getIsSubmitedOpinion();
        IsBeForwardModify=wffm.getIsBeForwardModify();
		IsBeForwardSubmit = wffm.getIsBeForwardSubmit();
        rs.executeSql("select groupdetailid from workflow_currentoperator where id="+RequestKey);
        if(rs.next()){
            wfcm.getCoadjutantRights(rs.getInt("groupdetailid"));
            coadispending=wfcm.getIspending();
            coadismodify=wfcm.getIsmodify();
			coadsigntype = wfcm.getSigntype();
        }
        // add by liaodong for qc48451 in 2013年10月21日 start
        String  fixbillSql = "select * from workflow_bill  where id >0 and id= "+formid;
        rs.executeSql(fixbillSql);
        if(rs.next()){
        	isfixbill = true;
        }
        //end
        try{
	        rs.execute("select isvalid from workflow_base where id="+workflowid);
			if(rs.next()){
				int isvalid_t = Util.getIntValue(rs.getString("isvalid"), 0);
				if(isvalid_t == 2){
					istest = 1;
				}
			}
        }catch(Exception e){}

        //协办人条件 65537 yl
        String sqlll = "select  distinct wp.ispending,wp.ismodify,wp.issubmitdesc,wp.isforward,wc.userid,wp.signtype from workflow_groupdetail wp,workflow_currentoperator wc where wp.id = wc.groupdetailid and wc.requestid = "+requestid+" and IsCoadjutant='1' and wc.isremark ='7'";

        rs.executeSql(sqlll);
        ArrayList<String> isforwards = new ArrayList();//协办人是否可以转发
        ArrayList<String> coadispendings = new ArrayList(); //协办人是否一直待办
        ArrayList<String> coadismodifys = new ArrayList();//协办人是否可以修改
        ArrayList<String> isSubmitedOpinions = new ArrayList();//协办人是否可以提交
        ArrayList<String> userids = new ArrayList();//协办人组
        ArrayList<String> coadsigntypes = new ArrayList();//协办人会签条件
        while(rs.next()){
        	if("2".equals(rs.getString(6)) && "0".equals(rs.getString(3))){//如果协办人关系是不影响流程流转且没有勾选仅能在主办人提交前提交，则默认它为为查看始终停留在待办
        		coadispendings.add("1");
        	}else{
        		coadispendings.add(rs.getString(1));
        	}  
            coadismodifys.add(rs.getString(2));
            isSubmitedOpinions.add(rs.getString(3));
            isforwards.add(rs.getString(4));
            userids.add(rs.getString(5));
            coadsigntypes.add(rs.getString(6));
        }



        String isfeedback="";
        String isnullnotfeedback="";
        rs.executeSql("select t.isfeedback,t.isnullnotfeedback,t1.nodeattribute from workflow_flownode t, workflow_nodebase t1  where t.nodeid = t1.id and t.workflowid="+workflowid+" and t.nodeid="+nodeid);
        if(rs.next()){
            isfeedback=Util.null2String(rs.getString("isfeedback"));
            isnullnotfeedback=Util.null2String(rs.getString("isnullnotfeedback"));
            nodeattribute = Util.getIntValue(rs.getString("nodeattribute"), 0);
        }
        if(isremark==7&&"2".equals(coadsigntype)){
			tempsrc = src;
            src="save";
        }
		//重新对billid赋值
		getBillId();		

		//由request或fu获得needwfback的值。如没有，则默认为1（需反馈）TD9144
		if(needwfback==null || "".equals(needwfback)){
			if(isRequest){
				if(request!=null){
					needwfback = ""+Util.getIntValue(request.getParameter("needwfback"), 1);
				}
			}else{
				if(fu!=null){
					needwfback = ""+Util.getIntValue(fu.getParameter("needwfback"), 1);
				}
			}
		}
        String cnodetype="";
		ArrayList operatorsWfEnd = new ArrayList(); //xwj for td3450 20060111
		boolean haspassthisnode = false;
		
		//提交节点, 记录已经完成的操作组ID集合，当流程不可以流向下一节点时，更新该操作组的其他人员
        String finishndgpids = "";
		
        ArrayList tempusers=new ArrayList();
		ArrayList tempusersType=new ArrayList();
        String groupdetailids="";
		String groupdetailid4sql = "";
        ArrayList submituserids=new ArrayList();
        ArrayList submitusertypes=new ArrayList();
        canflowtonextnode=true;
        coadcansubmit=true;
        showcoadjutant=false;
        /**
		 * added by cyril on 2008-07-29 for td:8835
		 * 检查流程状态
		 */
		StringBuffer s = new StringBuffer();
		s.append("select t1.ismodifylog, t2.status, t2.currentnodetype,t1.isAutoApprove,t1.isAutoCommit from workflow_base t1, workflow_requestbase t2 where t1.id=t2.workflowid and t2.requestid="+requestid);
		executesuccess = rs.executeSql(s.toString());
		if (!executesuccess) {
			writeLog(s.toString());
			saveRequestLog("1");
			return false;
		}
	  String tempcurrentnodetype = "";
		if(rs.next()) {
			isTrack = (rs.getString("ismodifylog")!=null && "1".equals(rs.getString("ismodifylog")));
			isStart = (rs.getString("status")!=null && !"".equals(rs.getString("status")));
			tempcurrentnodetype = Util.null2String(rs.getString("currentnodetype"));
			//System.out.println("isStart="+isStart);
			this.setIsAutoApprove(Util.null2s(rs.getString("isAutoApprove"), "0"));
			this.setIsAutoCommit(Util.null2s(rs.getString("isAutoCommit"), "0"));
		}
		/**
		 * end by cyril on 2008-07-29 for td:8835
		 */
		//add by xhheng @20050303 for TD 1545
		//批量提交时将不调用saveRequestInfo()函数，故需要再此重新获取billtablename值，以便单据节点的正确寻找
		if (isbill == 1) {
			rs.executeSql("select tablename from workflow_bill where id = " + formid); // 查询工作流单据表的信息
			if (rs.next())
				billtablename = rs.getString("tablename");          // 获得单据的主表
			else
				return false;
		}
        //writeLog("isremark:"+isremark);
        // 查询下一个节点 
        RecordSetTrans rst=new RecordSetTrans();
        rst.setAutoCommit(false);
        SetRsTrans(rst);
        //出发子流程
        ArrayList nodeidList_sub = new ArrayList();
        ArrayList triggerTimeList_sub = new ArrayList();
        ArrayList hasTriggeredSubwfList_sub = new ArrayList();
        //触发日程
		ArrayList nodeidList_wp = new ArrayList();
        ArrayList createTimeList_wp = new ArrayList();
        
        //是否重新计算相关资源的共享权限，只有流程流转至下一节点时，才进行计算
        boolean isReCalculatePermission = false;;
        try{
                // 查询当前请求的一些基本信息
				rst.executeProc("workflow_Requestbase_SByID", requestid + "");
				if (rst.next()) {
					lastnodeid = Util.getIntValue(rst.getString("lastnodeid"), 0);
					lastnodetype = Util.null2String(rst.getString("lastnodetype"));
					passedgroups = Util.getIntValue(rst.getString("passedgroups"), 0);
					totalgroups = Util.getIntValue(rst.getString("totalgroups"), 0);
					creater = Util.getIntValue(rst.getString("creater"), 0);
					creatertype = Util.getIntValue(rst.getString("creatertype"), 0);
					requestmark = Util.null2String(rst.getString("requestmark"));
					cnodetype=Util.null2String(rst.getString("currentnodetype"));
					oldformsignaturemd5 = Util.null2String(rst.getString("formsignaturemd5"));
				} else {
					saveRequestLog("1");
					if(rst!=null){
					    new Exception(RequestManager.ERROR_NOTFOUND_G).printStackTrace();
						rst.rollback();
					}		
					this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_01);
                    return false;
				}
            GCONST.WFProcessing.add(requestid+"_"+nodeid);
            if (isMakeOperateLog) {
                //初始化日志对象， 记录初始数据
                rolm = new RequestOperationLogManager(this);
                rolm.flowTransStartBefore();
            }
            if (src.equals("submit") || src.equals("reject") || src.equals("reopen") || src.equals("intervenor")) {
	
				
			
	
				// 如果是提交节点, 查询通过了的组数
    			int copnodeid=nodeid;
    			int submitgroups=0;
    			int totalunsubmitgroups=0;
    			
				if (src.equals("submit")) {
                    String tempisremark="isremark = '0'";
                    if(coadsigntype.equals("0")){
                    	tempisremark="(isremark = '0' or isremark = '7')";
                    }else if(coadsigntype.equals("1")){
                    	int signorder = 0;
                        tempisremark="(isremark = '0' or isremark = '7')";
                        rs.executeSql("select signorder from workflow_groupdetail where id="+wfcm.getGroupdetailid());
                        if(rs.next()) signorder = Util.getIntValue(rs.getString("signorder"),0);
                        if(isremark==0){   //未操作者
                        	//查询协办人是否已提交
                            rs.executeSql("select 1 from workflow_currentoperator where isremark = '7' and requestid=" + requestid +" and nodeid="+nodeid+" and exists(select 1 from workflow_coadjutant where coadjutantid=workflow_currentoperator.id and organizedid="+RequestKey+" and requestid="+requestid+")");
                            if(rs.next()) coadcansubmit=false;
							if(wfcm.getSignorder()==2 && coadcansubmit==false)coadcansubmit=true; //如果是依次处理并且协办人没有提交
                        }else if(isremark==7){  //协办人
                        	//查询主办人是否存在未提交
                            rs.executeSql("select 1 from workflow_currentoperator where isremark = '0' and requestid=" + requestid +" and nodeid="+nodeid+" and exists(select 1 from workflow_coadjutant where organizedid=workflow_currentoperator.id and coadjutantid="+RequestKey+" and requestid="+requestid+")");
                            if(rs.next()) coadcansubmit=false;
                            if(signorder==0){
                                rs.executeSql("select 1 from workflow_currentoperator where isremark = '2' and preisremark='0' and (isreject is null or isreject=0) and requestid='" + requestid +"' and nodeid='"+nodeid+"' and groupdetailid ='"+wfcm.getGroupdetailid()+"'");
                                if(rs.next()) coadcansubmit=true;
                            }
                        }  
                    }
                    if(coadcansubmit){
                        
                        
                        
                        
                      //为了兼容部分老客户，此处做了分支处理，可以通过修改配置文件来配置程序流转的逻辑
                        //else部分的逻辑不再维护，修改此处逻辑时，请注意！
                        if (!isExeOldFlowlogic) {
                            // 如果是提交节点, 查询当前用户提交组数和总共的未提交组数
                            rst.executeSql("select nodeid from workflow_currentoperator where "+tempisremark+" and requestid=" + requestid + " and userid=" + userid + " and usertype=" + usertype+" and nodeid="+nodeid + "order by id desc");
                            if (rst.next()){
                                copnodeid = Util.getIntValue(Util.null2String(rst.getString(1)), 0);
                            }else{
                                rst.executeSql("select nodeid from workflow_currentoperator where "+tempisremark+" and requestid=" + requestid + " and userid=" + userid + " and usertype=" + usertype + " order by id desc");
                                if (rst.next()) copnodeid = Util.getIntValue(Util.null2String(rst.getString(1)), 0);
                            }
                            //---------------------------------------------------------------------------
                            // 同一操作组中的操作项之间的关系为非会签，操作组与操作组之间为会签关系（改造） START
                            //---------------------------------------------------------------------------
                            int canPassGroupCount = 0;
                            //创建节点提交或者操作组被删除（流下下一节点的判断）
                            boolean isCreateNdOrNdgpNotExist = false;
                            // 查询当前节点需要提交用户所在的操作组，并迭代
                            // 当且仅当 每一个操作组都可以通过时，才可以向下流转
                            // 同一操作组，所有的操作项的关系为：非会签关系，当当前用户所在的操作项为非会签或所在操作组为会签且其他人已经提交时，此操作组通过！
                            // 否则，请求不可以向下流转
                            String notpassgpnumsql = "SELECT distinct wg.groupid "
                                + " FROM workflow_currentoperator wc LEFT JOIN workflow_groupdetail wg "
                                + "     ON wc.groupdetailid=wg.id "
                                + " WHERE " + tempisremark + " and requestid=" + requestid 
                                + " and nodeid=" + nodeid;
                            rst.executeSql(notpassgpnumsql);
                            int nodegroupcount = rst.getCounts();
                            while (rst.next()){
                                int tempgrouid = Util.getIntValue(Util.null2String(rst.getString(1)), 0);
                                //操作组被删除或者是创建节点
                                if (tempgrouid == 0) {
                                    isCreateNdOrNdgpNotExist = true;
                                }
                                //当前用户是否在当前操作组中，如果不在，则流程不需要流转到下一节点！（非同一操作组，关系为：会签）
                                String smgroupsql = "SELECT distinct groupdetailid, groupid "
                                    + " FROM workflow_currentoperator "
                                    + " WHERE " + tempisremark + " and requestid=" + requestid 
                                    + " and userid=" + userid + " and usertype=" + usertype 
                                    + " and nodeid=" + nodeid
                                    + " and groupdetailid in (SELECT id FROM workflow_groupdetail WHERE groupid=" + tempgrouid + ")";
                                
                                //当同一操作组通过时候，其中包含的“依次逐个处理”组，则不需要继续流程
                                String temp_groupdetailids = "";
                                String temp_groupdetailid4sql = "";
                                
                                rs2.executeSql(smgroupsql);
                                while (rs2.next()){
                                    int tempGroupDetailID = Util.getIntValue(Util.null2String(rs2.getString(1)), 0);
                                    
                                    int tempsubmitgroups = 0;
                                    int temptotalgroups = 0;
                                    //查找：当前用户在当前操作项中可以提交的 组数
                                    rs3.executeSql("select count(distinct groupid) from workflow_currentoperator "
                                            + " where " + tempisremark + " and requestid=" + requestid 
                                            + " and userid=" + userid + " and usertype=" + usertype
                                            + " and nodeid=" + nodeid + " and groupdetailid=" + tempGroupDetailID
                                            );
                                    if (rs3.next()){
                                        tempsubmitgroups = Util.getIntValue(Util.null2String(rs3.getString(1)), 0);
                                    }
                                    
                                    //查询当前节点是否存在协办人，如果存在则按协办人条件进行判断（当存在协办人时，主办人不再局限于部门的人员而是整个节点操作组都将是主办人）
                                    rs3.executeSql("select groupdetailid from workflow_currentoperator "
                                            + " where (isremark='7' or preisremark='7') and requestid='"+requestid + "'"
                                            + " and nodeid='" + nodeid + "' and groupdetailid='" + wfcm.getGroupdetailid() + "'"
                                            + " and groupdetailid=" + tempGroupDetailID);
                                    if(rs3.next() && (isremark==0 || isremark==7)){
                                        temptotalgroups = wfcm.getTotalnumSubmitGroups(isremark,nodeid,requestid,userid,rst);
                                    }else{
                                        rs3.executeSql("select count(distinct groupid) from workflow_currentoperator "
                                                + " where " + tempisremark + " and requestid=" + requestid 
                                                + " and nodeid=" + nodeid + " and groupdetailid=" + tempGroupDetailID);
                                        if (rs3.next()){
                                            temptotalgroups = Util.getIntValue(Util.null2String(rs3.getString(1)), 0);                      
                                        }
                                    }
                                    
                                    //判断该人所在组是否含有依次逐个递交的组，如果有一个，则passedgroups-1，并且进入得到下个操作者的方法
                                    rs4.execute("select * from workflow_groupdetail where id=" + tempGroupDetailID);
                                    if (rs4.next()) {   
                                        int type = rs4.getInt("type");
                                        int signorder = rs4.getInt("signorder");
                                        if (WFPathUtil.isContinuousProcessing(type) && signorder == 2 && isremark!=7) {
                                             rs3.execute("select * from workflow_agentpersons where requestid="+requestid+" and (groupdetailid="+rs2.getInt("groupdetailid")+" or groupdetailid is null)");
                                             if (rs3.next() && !rs3.getString("receivedPersons").equals("")) {
                                                 tempsubmitgroups--;
                                                 if (temp_groupdetailids.equals("")) {
                                                     temp_groupdetailids = rs2.getString("groupdetailid") + "_" + rs2.getString("groupid");
                                                 } else {
                                                     temp_groupdetailids = temp_groupdetailids + "," + rs2.getString("groupdetailid") + "_" + rs2.getString("groupid");
                                                 }
                                                 
                                                 if (temp_groupdetailid4sql.equals("")) {
                                                     temp_groupdetailid4sql = rs2.getString("groupdetailid");
                                                 } else {
                                                     temp_groupdetailid4sql = temp_groupdetailid4sql + "," + rs2.getString("groupdetailid");
                                                 }
                                             }
                                        }
                
                                    }
                                    
                                    //同一操作项，有其他未提交的用户，则不能向下流转
                                    if (tempsubmitgroups >= temptotalgroups) {
                                        //通过的操作数
                                        canPassGroupCount++;
                                        //通过的操作组ID
                                        if ("".equals(finishndgpids)) {
                                            finishndgpids += tempgrouid;
                                        } else {
                                            finishndgpids += "," + tempgrouid;
                                        }
                                        //该操作组通过, 清空该操作组内的依次逐个处理
                                        temp_groupdetailids = "";
                                        temp_groupdetailid4sql = "";
                                        break;
                                    }
                                }
                                
                                //该操作组内的依次逐个处理项
                                if (groupdetailids.equals("")) {
                                    groupdetailids = temp_groupdetailids;
                                } else if (!"".equals(temp_groupdetailids)){
                                    groupdetailids = groupdetailids + "," + temp_groupdetailids;
                                }
                                if (groupdetailid4sql.equals("")) {
                                    groupdetailid4sql = temp_groupdetailid4sql;
                                } else if (!"".equals(temp_groupdetailid4sql)){
                                    groupdetailid4sql = groupdetailid4sql + "," + temp_groupdetailid4sql;
                                }
                                
                            }
                            
                            //操作组被删除，或者是创建节点提交
                            if (isCreateNdOrNdgpNotExist) {
                                rst.executeSql("select count(distinct groupid) from workflow_currentoperator where "+tempisremark+" and requestid=" + requestid + " and userid=" + userid + " and usertype=" + usertype+" and nodeid="+nodeid);
                                if (rst.next()){
                                    passedgroups += Util.getIntValue(rst.getString(1), 0);
                                    submitgroups = Util.getIntValue(rst.getString(1), 0);
                                }
                                
                                rst.executeSql("select count(distinct groupid) from workflow_currentoperator where "+tempisremark+" and requestid=" + requestid +" and nodeid="+nodeid);
                                if (rst.next()){
                                    totalunsubmitgroups = Util.getIntValue(rst.getString(1), 0);                        
                                }
                            } else {
                                submitgroups = canPassGroupCount;
                                totalunsubmitgroups = nodegroupcount;
                            }
                            //---------------------------------------------------------------------------
                            // 同一操作组中的操作项之间的关系为非会签，操作组与操作组之间为会签关系（改造） END
                            //---------------------------------------------------------------------------
                        } else {
                            //-----------------------------------------------------
                            // 注意：此case中为老的逻辑不再维护 START
                            //-----------------------------------------------------
                        
                        
                        
                        
					rst.executeSql("select count(distinct groupid) from workflow_currentoperator where "+tempisremark+" and requestid=" + requestid + " and userid=" + userid + " and usertype=" + usertype+" and nodeid="+nodeid);
					if (rst.next()){
						passedgroups += Util.getIntValue(rst.getString(1), 0);
    					submitgroups = Util.getIntValue(rst.getString(1), 0);
					}
    				// 如果是提交节点, 查询当前用户提交组数和总共的未提交组数
					rst.executeSql("select nodeid from workflow_currentoperator where "+tempisremark+" and requestid=" + requestid + " and userid=" + userid + " and usertype=" + usertype+" and nodeid="+nodeid + "order by id desc");
    				if (rst.next()){
                        copnodeid = Util.getIntValue(rst.getString(1), 0);
                    }else{
                        rst.executeSql("select nodeid from workflow_currentoperator where "+tempisremark+" and requestid=" + requestid + " and userid=" + userid + " and usertype=" + usertype + " order by id desc");
                        if (rst.next()) copnodeid = Util.getIntValue(rst.getString(1), 0);
                    }
    				
    				//rst.executeSql("select count(distinct groupid) from workflow_currentoperator where "+tempisremark+" and requestid=" + requestid +" and nodeid="+nodeid);
    				//if (rst.next()) totalunsubmitgroups = Util.getIntValue(rst.getString(1), 0);
    				
						//查询当前节点是否存在协办人，如果存在则按协办人条件进行判断（当存在协办人时，主办人不再局限于部门的人员而是整个节点操作组都将是主办人）
	    				rst.executeSql("select groupdetailid from workflow_currentoperator where (isremark='7' or preisremark='7') and requestid='"+requestid+"' and nodeid='"+nodeid+"' and groupdetailid='"+wfcm.getGroupdetailid()+"'");
	    				if(rst.next() && (isremark==0 || isremark==7)){
	    					totalunsubmitgroups = wfcm.getTotalnumSubmitGroups(isremark,nodeid,requestid,userid,rst);
	    				}else{
		        			rst.executeSql("select count(distinct groupid) from workflow_currentoperator where "+tempisremark+" and requestid=" + requestid +" and nodeid="+nodeid);
		        			if (rst.next()){
		        				totalunsubmitgroups = Util.getIntValue(rst.getString(1), 0);    					
		        			}
	    				}	    				
//		    				if(coadsigntype.equals("0")){
//		        				rst.executeSql("select count(distinct groupid) from workflow_currentoperator where "+tempisremark+" and requestid=" + requestid +" and nodeid="+nodeid+" and not exists(select * from workflow_currentoperator b where b.groupid=workflow_currentoperator.groupid and (b.preisremark = '0' or b.preisremark = '7')  and b.isremark='2'and b.requestid=" + requestid +" and b.nodeid="+nodeid+")");
//		        				if (rst.next()){
//		        					totalunsubmitgroups = Util.getIntValue(rst.getString(1), 0);       					
//		        				}
//		    				}else{
//		        				rst.executeSql("select count(distinct groupid) from workflow_currentoperator where "+tempisremark+" and requestid=" + requestid +" and nodeid="+nodeid);
//		        				if (rst.next()){
//		        					totalunsubmitgroups = Util.getIntValue(rst.getString(1), 0);    					
//		        				}
//		    				}
    				
    				
					//判断该人所在组是否含有依次逐个递交的组，如果有一个，则passedgroups-1，并且进入得到下个操作者的方法
					rst.execute("select distinct groupdetailid,groupid from workflow_currentoperator where "+tempisremark+" and requestid=" + requestid + " and userid=" + userid + " and usertype=" + usertype+" and nodeid="+nodeid);
    					
					while (rst.next())
					{
						rs2.execute("select * from workflow_groupdetail where id="+rst.getInt("groupdetailid"));
						if (rs2.next())
						{   int type = rs2.getInt("type");
						    int signorder = rs2.getInt("signorder");
						    if (WFPathUtil.isContinuousProcessing(type) && signorder == 2 && isremark!=7)
						    //update by 	84163
							{    //判断是否还有剩余节点
								 rs3.execute("select * from workflow_agentpersons where requestid="+requestid+" and (groupdetailid="+rst.getInt("groupdetailid")+" or groupdetailid is null)");
								 if (rs3.next()&&!rs3.getString("receivedPersons").equals(""))
								 {passedgroups--;
								 submitgroups--;
								 groupdetailids=groupdetailids.equals("")?rst.getString("groupdetailid")+"_"+rst.getString("groupid"):groupdetailids+","+rst.getString("groupdetailid")+"_"+rst.getString("groupid");
								 groupdetailid4sql = groupdetailid4sql.equals("")?rst.getString("groupdetailid"):groupdetailid4sql+","+rst.getString("groupdetailid");
								 }
							}
	
						}
                    }
	
					//-----------------------------------------------------
                    // 注意：此case中为老的逻辑不再维护 END
                    //-----------------------------------------------------
                }
					
					}
				}			
	
	            // 只对可以通过的提交节点和拒绝节点查询下一个节点
	
				//poppupRemindInfoUtil.updatePoppupRemindInfo(userid,0,""+usertype,requestid);//xwj for td3450 20060111
				
				if ((src.equals("submit") && submitgroups >= totalunsubmitgroups && copnodeid==nodeid&&coadcansubmit) || src.equals("reject") || src.equals("reopen") || isremark==5|| src.equals("intervenor")) {
                    showcoadjutant=true;
					int isreject = 0;
					int isreopen = 0;
					int linkid = 0;
					 if (src.equals("reject")){
							isreject = 1;
							//td30785
							 if(isRequest){
	                            if (request != null){
	                            RejectToNodeid=Util.getIntValue(request.getParameter("RejectToNodeid"));
	                            RejectToType=Util.getIntValue(request.getParameter("RejectToType"));
	                            }
	                        }else{
	                            if (fu != null){
	                            RejectToNodeid=Util.getIntValue(fu.getParameter("RejectToNodeid"));
								RejectToType=Util.getIntValue(fu.getParameter("RejectToType"));
	                            }
	                        }
							//System.out.println("RejectToNodeid = "+RejectToNodeid+" RejectToType="+RejectToType);
					 }else if("submit".equals(src)) {
						 if(this.SubmitToNodeid <= 0) {
							if(isRequest) {
								if(request != null) {
									this.SubmitToNodeid = Util.getIntValue(request.getParameter("SubmitToNodeid"), 0);
								}
							}else {
								if(fu != null) {
									this.SubmitToNodeid = Util.getIntValue(fu.getParameter("SubmitToNodeid"), 0);
								}
							}
						 }
		                }
					else if (src.equals("reopen")) isreopen = 1;
	                if(isremark!=5 && !src.equals("intervenor")){
	                
					//注意：requestNodeFlow.getNextNodes()方法之前不能通过rst修改workflow_currentoperator表的数据，不然会死锁。
					// 查询下一个节点的操作者
					RequestNodeFlow requestNodeFlow = new RequestNodeFlow();
					requestNodeFlow.setRequestid(requestid);
					requestNodeFlow.setNodeid(nodeid);
					requestNodeFlow.setNodetype(nodetype);
					requestNodeFlow.setWorkflowid(workflowid);
					requestNodeFlow.setUserid(userid);
					requestNodeFlow.setUsertype(usertype);
					requestNodeFlow.setLanguageid(this.userlanguage);
					requestNodeFlow.setCreaterid(creater);
					requestNodeFlow.setCreatertype(creatertype);
					requestNodeFlow.setFormid(formid);
					requestNodeFlow.setIsbill(isbill);
					requestNodeFlow.setBillid(billid);
					requestNodeFlow.setBilltablename(billtablename);
					requestNodeFlow.setIsreject(isreject);
					requestNodeFlow.setIsreopen(isreopen);
					requestNodeFlow.setRejectToNodeid(RejectToNodeid);
					requestNodeFlow.setRejectToType(RejectToType);
					requestNodeFlow.setSubmitToNodeid(this.SubmitToNodeid);
					requestNodeFlow.setRecordSet(rs);
					requestNodeFlow.setUser(user);
					requestNodeFlow.setRecordSetTrans(rst);
					requestNodeFlow.setIsFromRequestManager(1);
					//add by liaodong for qc48451 in 2013年10月21日 start 
					requestNodeFlow.setIsfixbill(isfixbill);
					//end
                    // add by liaodong for qc86096 in 2013-11-19 start
					requestNodeFlow.setCurrentdate(currentdate);
					requestNodeFlow.setCurrenttime(currenttime);
					//end
					requestNodeFlow.setEh_operatorMap(eh_operatorMap);
					requestNodeFlow.setTonextnode(true);
					//计算节点操作者
					requestNodeFlow.getNextNodes();
                    this.nextnodeids=requestNodeFlow.getNextnodeids();
                    this.nextnodetypes=requestNodeFlow.getNextnodetypes();
                    this.nextlinkids=requestNodeFlow.getNextlinkids();
                    this.nextlinknames=requestNodeFlow.getNextlinknames();
                    this.operatorshts=requestNodeFlow.getOperatorshts();
                    this.nextnodeattrs=requestNodeFlow.getNextnodeattrs();
                    this.nextnodepassnums=requestNodeFlow.getNextnodepassnums();
                    this.linkismustpasss=requestNodeFlow.getLinkismustpasss();
                    this.requestexceptiontypes = requestNodeFlow.getRequestexceptiontypes();
                    this.hasEflowToAssignNode = requestNodeFlow.isHasEflowToAssignNode();
                    this.hasCoadjutant = requestNodeFlow.isHasCoadjutant();
                    //System.out.println("nextnodeidsize:"+nextnodeids.size());
					//add by liaodong for qc76119,76120,54194,64652 start
                    penetrateFlag = requestNodeFlow.getPenetrateId(); 
                    lastNodeId = requestNodeFlow.getLastNodeId();
                    
                   //end
                    if (nextnodeids.size()<1) {
						saveRequestLog("1");
    					if(rst!=null){
    					    new Exception(RequestManager.ERROR_NOTFOUND_NNL).printStackTrace();
    						rst.rollback();
    					}
    					this.isNeedChooseOperator = requestNodeFlow.isNeedChooseOperator();
    					if(!this.isNeedChooseOperator){
    					    this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_07);
    					}
    					this.setMessagecontent(requestNodeFlow.getMessagecontent());
                        return false;
					}else{
					    if("0".equals(isFirstSubmit)){
					        String returnvalue = "";
					        for (int i = 0; i < nextlinkids.size(); i++) {
					            String viewNodeIdSQL = "select tipsinfo from workflow_nodelink where id=" + nextlinkids.get(i);
					            if(RejectToNodeid != 0){
					                viewNodeIdSQL+=" and  destnodeid="+RejectToNodeid ;
					            } 
					            rst.executeSql(viewNodeIdSQL);
					            if (rst.next()) {
					                if (returnvalue.equals("")) returnvalue = Util.null2String(rst.getString("tipsinfo"));
					                else returnvalue += "\\n" + weaver.general.Util.null2String(rst.getString("tipsinfo"));
					            }
					        }
					        
					        if (!"".equals(returnvalue)) {
					            this.setMessage(WorkflowRequestMessage.WF_CUSTOM_LINK_TIP);
					            this.setMessagecontent(returnvalue);
					            rst.rollback();
					            return false;
					        }
					    }
					}
                    wflinkinfo.setSrc(src);
                    wflinkinfo.setSubmitToNodeid(this.SubmitToNodeid);
                    for(int i=0;i<nextnodeids.size();i++){
					nextnodeid = Util.getIntValue((String)nextnodeids.get(i));
					nextnodetype = (String)nextnodetypes.get(i);
					linkid = Util.getIntValue((String)nextlinkids.get(i));
					status = (String)nextlinknames.get(i);
					operatorsht = (Hashtable)operatorshts.get(i);
                    nextnodeattr=Util.getIntValue((String)nextnodeattrs.get(i));
                    totalgroups = operatorsht.size();
                    if(nextnodeattr==3||nextnodeattr==4||nextnodeattr==5) {
                    	canflowtonextnode=wflinkinfo.FlowToNextNode(requestid,nodeid,nextnodeid,nextnodeattr+"",Util.getIntValue((String)nextnodepassnums.get(i)),Util.getIntValue((String)linkismustpasss.get(i)));
                    }
                    //System.out.println("nextnodeid="+nextnodeid+" nextnodeattr="+nextnodeattr+" canflowtonextnode:"+canflowtonextnode);
                    haspassthisnode = true;

	                // 查询当前请求的一些基本信息
					rst.executeProc("workflow_Requestbase_SByID", requestid + "");
					if (rst.next()) {
						requestmark = Util.null2String(rst.getString("requestmark"));
					} else {
						saveRequestLog("1");
    					if(rst!=null){
    					    new Exception(RequestManager.ERROR_NOTFOUND_G).printStackTrace();
    						rst.rollback();
    					}						
                        return false;
					}					

					
					// 查询下一个节点的超时设置
					rst.executeProc("workflow_NodeLink_SPasstime", "" + nextnodeid + flag + "0");
					if (rst.next()){
						nodepasstime = Util.getFloatValue(rst.getString("nodepasstime"), -1);
					}
	                }
	                }
                    else{
                        if(isRequest){
                            if (request != null){
                            submitNodeId=Util.null2String(request.getParameter("submitNodeId"));
                            Intervenorid=Util.null2String(request.getParameter("Intervenorid"));
							IntervenoridType=Util.null2String(request.getParameter("IntervenoridType"));
                            SignType=Util.getIntValue(request.getParameter("SignType"),0);
                            }
                        }else{
                            if (fu != null){
                            submitNodeId=Util.null2String(fu.getParameter("submitNodeId"));
                            Intervenorid=Util.null2String(fu.getParameter("Intervenorid"));
							IntervenoridType=Util.null2String(fu.getParameter("IntervenoridType"));
                            SignType=Util.getIntValue(fu.getParameter("SignType"),0);
                            }
                        }
                        ArrayList tempnodes=Util.TokenizerString(submitNodeId,"_");
	                    tempusers=Util.TokenizerString(Intervenorid,",");
						tempusersType=Util.TokenizerString(IntervenoridType,",");
	                    if(tempnodes.size()>=2){
	                        nextnodeid=Util.getIntValue((String)tempnodes.get(0));
	                        nextnodetype=(String)tempnodes.get(1);
	                        nextnodeattr=wflinkinfo.getNodeAttribute(nextnodeid);
                            nextnodeids.add(""+nextnodeid);
                            nextnodetypes.add(nextnodetype);
                            nextnodeattrs.add(""+nextnodeattr);
                            totalgroups=tempusers.size();
                            String tmpnodeids=wflinkinfo.getBrancheNode(nextnodeid,workflowid,"",requestid);
                            if(!tmpnodeids.equals("")){
                                rst.executeSql("select a.nodeid,b.nodetype from workflow_currentoperator a,workflow_flownode b where a.workflowid=b.workflowid and a.nodeid=b.nodeid and a.isremark='0' and a.requestid="+requestid+" and a.nodeid in("+tmpnodeids+")");
                                if(rst.next()){
                                    nodeid=rst.getInt(1);
                                    nodetype=Util.null2String(rst.getString(2));
                                }
                            }
                            rst.executeSql("select nodename from workflow_flownode,workflow_nodebase where workflow_nodebase.id=workflow_flownode.nodeid and workflowid="+workflowid+" and workflow_flownode.nodeid="+nodeid+" order by nodetype");
	                        if(rst.next()){
	                        status = rst.getString("nodename");
	                        }
	                        rst.executeSql("select nodename from workflow_flownode,workflow_nodebase where workflow_nodebase.id=workflow_flownode.nodeid and workflowid="+workflowid+" and workflow_flownode.nodeid="+nextnodeid+" order by nodetype");
	                        if(rst.next()){
	                        status += SystemEnv.getHtmlLabelName(18195,userlanguage)+SystemEnv.getHtmlLabelName(15322,userlanguage)+rst.getString("nodename");
	                        }
	                    }else{
	    					if(rst!=null){
	    					    new Exception(RequestManager.ERROR_NOTFOUND_SUBMITERROR).printStackTrace();
	    						rst.rollback();
	    					}	      
	                        this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_07);
	                        this.setMessagecontent(WorkflowRequestMessage.resolveDetailInfo("未选择流程干预节点"));
                            return false;
	                    }
                        canflowtonextnode=true;
                    }
	                //处理超时流程提醒
	                String remindusers="";
	                String usertypes="";
	                rst.executeSql("select wfreminduser,wfusertypes from workflow_currentoperator where isremark='0' and requestid="+requestid);
	                if(rst.next()){
	                    remindusers=rst.getString("wfreminduser");
	                    usertypes=rst.getString("wfusertypes");
	                }
	                ArrayList wfremindusers=Util.TokenizerString(remindusers,",");
	                ArrayList wfusertypes=Util.TokenizerString(usertypes,",");
	                rst.executeSql("select userid,usertype,nodeid from workflow_currentoperator where requestid="+requestid +" group by userid,usertype,nodeid");
	                while(rst.next()){
	                        String tempuserid=rst.getString("userid");
	                        String tempusertype=rst.getString("usertype");
							int tempnodeid=Util.getIntValue(rst.getString("nodeid"),0);
	                    if(wfremindusers.indexOf(tempuserid)<0){
							if(nodeid == tempnodeid){
								wfremindusers.add(tempuserid);
								wfusertypes.add(tempusertype);
							}
	                    }
	                }
	                for(int i=0;i<wfremindusers.size();i++){
	                    poppupRemindInfoUtil.updatePoppupRemindInfo(Util.getIntValue((String)wfremindusers.get(i)),10,(String)wfusertypes.get(i),requestid);
	                    poppupRemindInfoUtil.updatePoppupRemindInfo(Util.getIntValue((String)wfremindusers.get(i)),0,(String)wfusertypes.get(i),requestid);
	                }
	                
	                //----------------------------------------------
                    // 获取表单数据签名 start
                    //----------------------------------------------
                    String formsignaturemd5 = "";
                    try {
                        if ("submit".equals(src)) {
                            formsignaturemd5 = WFPathUtil.getFormValMD5(workflowid, requestid, isbill, formid, requestname, nextnodeid);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //----------------------------------------------
                    // 获取表单数据签名 end
                    //----------------------------------------------
	                // 更新 requestbase 表信息
	                if (src.equals("submit")&&!((isremark==1||isremark==7)&&!CanModify)) {
						// 处理编号
						if (lastnodetype.equals("0") && requestmark.equals("")) {
							requestmark = doRequestMark();
						}
						sql = " update workflow_requestbase set " +
								" lastnodeid = " + nodeid +
								" ,lastnodetype = '" + nodetype;
                                if(canflowtonextnode){
                                    if(nextnodeattr==1){
                                        sql+="' ,currentnodeid = " + nextnodeid +
                                        " ,currentnodetype = '" + nextnodetype;
                                        status = SystemEnv.getHtmlLabelName(21394,userlanguage);
                                    }else if(nextnodeattr==2){
                                        sql+="' ,currentnodeid = " + nextnodeid +
                                        " ,currentnodetype = '" + nextnodetype;
                                        status = SystemEnv.getHtmlLabelName(21395,userlanguage);
                                    }else{
                                        sql+="' ,currentnodeid = " + nextnodeid +
                                        " ,currentnodetype = '" + nextnodetype;
                                    }
                                }else{
                                    status = SystemEnv.getHtmlLabelName(21395,userlanguage);
                                }
                                sql+="' ,status = '" + Util.fromScreen2(status, userlanguage) + "' "+
                                " ,passedgroups = 0" +
								" ,totalgroups = " + totalgroups +
								" ,requestname = '" + Util.fromScreen2(requestname, userlanguage) + "' " +
								" ,requestmark = '" + requestmark + "' " +
								" ,lastoperator = " + userid ;
								if(!tempcurrentnodetype.equals("3")){//归档流程不更新lastoperatedate和lastoperatetime
									sql = sql+
								" ,lastoperatedate = '" + currentdate + "' " +
								" ,lastoperatetime = '" + currenttime + "' " ;
							  }
							  sql = sql+
								" ,lastoperatortype = " + usertype +
								" ,nodepasstime = " + nodepasstime +
								" ,nodelefttime = " + nodepasstime +
								" ,docids = '" + docids + "' " +
								" ,crmids = '" + crmids + "' ";
								if(isoracle){
									sql+=" ,hrmids = ? ";
								}else{
									sql+=" ,hrmids = '" + hrmids + "' ";
								}
								//" ,hrmids = '" + hrmids + "' " +
								sql+=" ,prjids = '" + prjids + "' " +
								" ,cptids = '" + cptids + "' ";
								//" ,cptids = '" + cptids + "' " +
								//" where requestid = " + requestid;
					} else{
						sql = " update workflow_requestbase set " +
								" lastnodeid = " + nodeid +
								" ,lastnodetype = '" + nodetype;
                                if(canflowtonextnode){
                                    if(nextnodeattr==1){
                                        sql+="' ,currentnodeid = " + nextnodeid +
                                        " ,currentnodetype = '" + nextnodetype;
                                        status = SystemEnv.getHtmlLabelName(21394,userlanguage);
                                    }else if(nextnodeattr==2){
                                        sql+="' ,currentnodeid = " + nextnodeid +
                                        " ,currentnodetype = '" + nextnodetype;
                                        status = SystemEnv.getHtmlLabelName(21395,userlanguage);
                                    }else{
                                        sql+="' ,currentnodeid = " + nextnodeid +
                                        " ,currentnodetype = '" + nextnodetype;
                                    }
                                }else{
                                    status = SystemEnv.getHtmlLabelName(21395,userlanguage);
                                }
								sql+="' ,status = '" + Util.fromScreen2(status, userlanguage) + "' "+
                                " ,passedgroups = 0" +
								" ,totalgroups = " + totalgroups +
								" ,lastoperator = " + userid ;
								if(!tempcurrentnodetype.equals("3")){//归档流程不更新lastoperatedate和lastoperatetime
									sql = sql+
								" ,lastoperatedate = '" + currentdate + "' " +
								" ,lastoperatetime = '" + currenttime + "' " ;
							  }
							  sql = sql+
								" ,lastoperatortype = " + usertype +
								" ,nodepasstime = " + nodepasstime +
								" ,nodelefttime = " + nodepasstime;
								//" ,nodelefttime = " + nodepasstime +
								//" where requestid = " + requestid;
                    }
	                
	                //更新签名信息
                    if (!"".equals(formsignaturemd5)) {
                        sql += ", formsignaturemd5='" + formsignaturemd5 + "' ";
                    }
                    sql+=" where requestid = " + requestid;
                    //writeLog(" update workflow_requestbase sql is " + sql);
                    if(isoracle&&src.equals("submit")&&!((isremark==1||isremark==7)&&!CanModify)&&!("0".equals(coadismodify)&&isremark==7)){
                    	executesuccess = rst.executeUpdate(sql, new Object[]{hrmids});
                    }else{
                    	executesuccess = rst.executeSql(sql);
                    }
					
	                if (!executesuccess) {
						saveRequestLog("1");
    					if(rst!=null){
    						rst.rollback();
    					}	
                        this.writeLog(sql);
                        this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_06);
                        this.setMessagecontent(WorkflowRequestMessage.resolveDetailInfo("流程提交，更新RequestBase出错"));
                        return false;
					}
					/******/
					
					/*if(isoracle&&src.equals("submit")&&!((isremark==1||isremark==7)&&!CanModify)){
						ConnStatement statement1 = null;
						try {
							String sql1 = "select hrmids from workflow_requestbase where requestid="+requestid+" for update";
							statement1 = new ConnStatement();
							statement1.setStatementSql(sql1, false);
			                if(statement1.next()){
				                CLOB theclob = statement1.getClob(1);
				                char[] contentchar = hrmids.toCharArray();
				                Writer contentwrite = theclob.getCharacterOutputStream();
				                contentwrite.write(contentchar);
				                contentwrite.flush();
				                contentwrite.close();
			                }
						} catch (Exception e) {
							rs.writeLog(e);
							e.printStackTrace();
						} finally {
							if(statement1!=null) statement1.close();
						}
					}
					*/
					/******/
					
					
                    //TD9427 获得所有下一个节点的nodeid，用于查询流程生成计划任务设置	add by chuj
                    String wf_nextnodeids = "";
                    for(int n=0;n<nextnodeids.size();n++){
                            nextnodeid=Util.getIntValue((String)nextnodeids.get(n));
                            wf_nextnodeids += (""+nextnodeid+", ");		//TD9427
                    if((isremark!=5&&!src.equals("intervenor")) && enableIntervenor==1){
					// 出口自动赋值处理
					try {
                        linkid=Util.getIntValue((String)nextlinkids.get(n));
                        RequestCheckAddinRules requestCheckAddinRules = new RequestCheckAddinRules();
                        //add by cyril on 2008-07-28 for td:8835 事务无法开启查询,只能传入
                        requestCheckAddinRules.setTrack(isTrack);
                        requestCheckAddinRules.setStart(isStart);
                        requestCheckAddinRules.setNodeid(nodeid);
                        //end by cyril on 2008-07-28 for td:8835
						requestCheckAddinRules.resetParameter();
						requestCheckAddinRules.setRequestid(requestid);
						requestCheckAddinRules.setWorkflowid(workflowid);
						requestCheckAddinRules.setObjid(linkid);
						requestCheckAddinRules.setObjtype(0);               // 1: 节点自动赋值 0 :出口自动赋值
						requestCheckAddinRules.setIsbill(isbill);
						requestCheckAddinRules.setFormid(formid);
						requestCheckAddinRules.setIspreadd("0");//xwj for td3130 20051123
						requestCheckAddinRules.setUser(user);//add by fanggsh 20061016 fot TD5121
						String clientIp = "";
						if(isRequest){
							if (request != null)
								clientIp = Util.null2String(request.getRemoteAddr());
						}else{
							if (fu != null)
								clientIp = Util.null2String(fu.getRemoteAddr());
						}						
						requestCheckAddinRules.setClientIp(clientIp);//add by fanggsh 20061016 fot TD5121
						requestCheckAddinRules.setSrc(src);//add by fanggsh 20061016 fot TD5121
						requestCheckAddinRules.setRequestManager(this);
						requestCheckAddinRules.checkAddinRules();
						
						requestCheckAddinRulesMap=new HashMap();
						requestCheckAddinRulesMap.put("objId",""+linkid);
						requestCheckAddinRulesMap.put("objType","0");// 1: 节点自动赋值 0 :出口自动赋值
						requestCheckAddinRulesMap.put("isPreAdd","0");			
						requestCheckAddinRulesList.add(requestCheckAddinRulesMap);
					} catch (Exception erca) {
						//writeLog(erca);
                        if(erca.getMessage().indexOf("workflow interface action error")>-1){
                            writeLog(erca);
	    					if(rst!=null){
	    						rst.rollback();
	    					}
	    					if("".equals(this.getMessageid())){
	    					    this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_07);
	    					}
	    					if("".equals(this.getMessagecontent())){
	    					    //出口附加操作执行失败
	    					    this.setMessagecontent(WorkflowRequestMessage.resolveDetailInfo(SystemEnv.getHtmlLabelNames("15587,15616,83071", user.getLanguage())));
	    					}
                            return false;}
					}
	                }
	
	                //出口自动赋值处理  xwj for td3130 20051123
	                String drawbackflag = "";//退回时，下一个节点的节点前附加操作是否触发。drawbackflag=1，表示触发。
	                RecordSet tempRecordSet = new RecordSet(); 
	                tempRecordSet.executeSql("select drawbackflag from workflow_flownode where workflowid="+workflowid+" and nodeid="+nextnodeid);
	                if(tempRecordSet.next()){
	               		drawbackflag = Util.null2String(tempRecordSet.getString("drawbackflag"));
	                }
				
	                int nextnodeattr=Util.getIntValue((String)nextnodeattrs.get(n),0);
	                
	                //当前节点类型是分之的合并节点(3.按通过分支数合并节点; 4.指定分支合并节点)
	                boolean isMergeNode = (nextnodeattr == 3 || nextnodeattr == 4|| nextnodeattr == 5);
	                boolean canflowtonextnode = false;
	                //如果是合并节点，对其进行判断是否可流转至下一节点。
	                if(isMergeNode && !src.equals("intervenor")){
	                	canflowtonextnode = wflinkinfo.FlowToNextNode(requestid,nodeid,nextnodeid, String.valueOf(nextnodeattr), Util.getIntValue((String)nextnodepassnums.get(n)), Util.getIntValue((String)linkismustpasss.get(n)));
	                }
                  
	    
	              if( ((!src.equals("reject") && !isMergeNode) || (!src.equals("reject") && isMergeNode && canflowtonextnode)  || (src.equals("reject") && drawbackflag.equals("1")) || (src.equals("intervenor") && isMergeNode)) &&  enableIntervenor==1){
								 try {
									 //由于objtype为"1: 节点自动赋值",不为"0 :出口自动赋值"，不用改变除状态外的文档相关信息，故可不用给user、clienIp、src赋值   fanggsh TD5121
									 RequestCheckAddinRules requestCheckAddinRules = new RequestCheckAddinRules();
									 requestCheckAddinRules.resetParameter();
									//add by cyril on 2008-07-28 for td:8835 事务无法开启查询,只能传入
			                        requestCheckAddinRules.setTrack(isTrack);
			                        requestCheckAddinRules.setStart(isStart);
			                        requestCheckAddinRules.setNodeid(nodeid);
			                        //end by cyril on 2008-07-28 for td:8835
									 requestCheckAddinRules.setRequestid(requestid);
									 requestCheckAddinRules.setWorkflowid(workflowid);
									 requestCheckAddinRules.setObjid(nextnodeid);
									 requestCheckAddinRules.setObjtype(1);
									 requestCheckAddinRules.setIsbill(isbill);
									 requestCheckAddinRules.setFormid(formid);
									 requestCheckAddinRules.setIspreadd("1");
									 requestCheckAddinRules.setRequestManager(this);
									 requestCheckAddinRules.setUser(user);	
									 String clientIp = "";
                                     if(isRequest){
                                         if (request != null)
                                             clientIp = Util.null2String(Util.getIpAddr(request));
                                     }else{
                                         if (fu != null)
                                             clientIp = Util.null2String(fu.getRemoteAddr());
                                     }                       
                                     requestCheckAddinRules.setClientIp(clientIp);//add by fanggsh 20061016 fot TD5121
                                     requestCheckAddinRules.setSrc(src);
									 requestCheckAddinRules.checkAddinRules();
									 
									 requestCheckAddinRulesMap=new HashMap();
									 requestCheckAddinRulesMap.put("objId",""+nextnodeid);
									 requestCheckAddinRulesMap.put("objType","1");// 1: 节点自动赋值 0 :出口自动赋值
									 requestCheckAddinRulesMap.put("isPreAdd","1");			
									 requestCheckAddinRulesList.add(requestCheckAddinRulesMap);
								 } catch (Exception erca) {
									 //writeLog(erca);
                                     if(erca.getMessage().indexOf("workflow interface action error")>-1){
                                       writeLog(erca);
           	    					     if(rst!=null){
        	    						     rst.rollback();
        	    					     }   
           	    					   if("".equals(this.getMessage())){
           	    					       this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_07);
           	    					   }
           	    					   if("".equals(this.getMessagecontent())){
           	    					       this.setMessagecontent(WorkflowRequestMessage.resolveDetailInfo(SystemEnv.getHtmlLabelNames("18009,83071", user.getLanguage())));
           	    					   }
                                       return false;}
								 }
							}
							   //add  by liaodong for qc40998 in 20130918 start 
			                	 //出口根据勾选的退回进行存档，节点后附加操作根据触发退回是否 
				                String rejectbackflag = "";//退回时，下一个节点的节点后附加操作是否触发。rejectbackflag=0，表示触发。
				                RecordSet tempRejectRecordSet = new RecordSet(); 
				                tempRejectRecordSet.executeSql("select rejectbackflag from workflow_flownode where workflowid="+workflowid+" and nodeid="+nodeid);
				                if(tempRejectRecordSet.next()){
				                	rejectbackflag = Util.null2String(tempRejectRecordSet.getString("rejectbackflag"));
				                }
				                String forkflag = "";//退回时，下一个节点的节点后附加操作是否触发。forkflag=1，表示触发。
				                RecordSet tempForkRecordSet = new RecordSet(); 
				                tempForkRecordSet.executeSql("select isreject from workflow_nodelink where workflowid="+workflowid+" and id="+linkid);
				                if(tempForkRecordSet.next()){
				                	forkflag = Util.null2String(tempForkRecordSet.getString("isreject"));
				                }
				               if(src.equals("reject")){ //退回时处理
				            	   if("0".equals(rejectbackflag)){ //节点后操作判断是否另存文档
				            		     //rs.executeSql("select * from workflow_addinoperate  where workflowid="+workflowid+" and isnode=1 and objid="+nodeid+" and ispreadd = 0  and type=2 and customervalue='action.WorkflowToDoc' ");
				            		     rs.executeSql("select * from workflowactionview  where workflowid="+workflowid+" and nodeid="+nodeid+" and ispreoperator='0' and (id='action.WorkflowToDoc' or id='WorkflowToDoc') and isused = 1");
					                	 if(rs.next()){
					                	    isWorkFlowToDoc=true;
					                	 }
					                }
									if("1".equals(forkflag)){
										 //rs.executeSql("select * from workflow_addinoperate  where workflowid="+workflowid+" and isnode=0 and objid="+linkid+" and ispreadd='0'  and type=2 and customervalue='action.WorkflowToDoc' ");
										 rs.executeSql("select * from workflowactionview  where workflowid="+workflowid+" and nodelinkid="+linkid+" and (id='action.WorkflowToDoc' or id='WorkflowToDoc') and isused = 1");
					                	 if(rs.next()){
					                		 isWorkFlowToDoc=true;
					                	 }
					                }
									if("1".equals(drawbackflag)){
										 rs.executeSql("select * from workflowactionview  where workflowid="+workflowid+" and nodeid="+nextnodeid+" and ispreoperator='1' and (id='action.WorkflowToDoc' or id='WorkflowToDoc') and isused = 1");
					                	 //rs.executeSql("select * from workflow_addinoperate  where workflowid="+workflowid+" and isnode=1 and objid="+nextnodeid+" and ispreadd = 1  and type=2 and customervalue='action.WorkflowToDoc' ");
					                	 if(rs.next()){
					                		 isWorkFlowToDoc=true;
					                	 }
					                }
				               }else{ //非退回时处理
				            	    String todocsql = "";
	                                if(linkid == 0){
										linkid = -1;
									}
				            	    if("intervenor".equals(src)){
				            	    	if(enableIntervenor == 1){   //执行节点前附加操作
				            	    		todocsql = "select * from workflowactionview  where workflowid="+workflowid+" and ((nodeid="+nextnodeid+" and ispreoperator='1') or nodelinkid="+linkid+" or (nodeid="+nodeid+" and ispreoperator='0')) and (id='action.WorkflowToDoc' or id='WorkflowToDoc') and isused = 1 ";
				            	    	}else{  //不执行节点前附加操作
				            	    		todocsql = "select * from workflowactionview  where workflowid="+workflowid+" and (nodelinkid="+linkid+" or (nodeid="+nodeid+" and ispreoperator='0')) and (id='action.WorkflowToDoc' or id='WorkflowToDoc') and isused = 1 ";
				            	    	}
				            	    }else{    //原有的逻辑
				            	    	todocsql = "select * from workflowactionview  where workflowid="+workflowid+" and ((nodeid="+nextnodeid+" and ispreoperator='1') or nodelinkid="+linkid+" or (nodeid="+nodeid+" and ispreoperator='0')) and (id='action.WorkflowToDoc' or id='WorkflowToDoc') and isused = 1 ";
				            	    }
				            	    rs.executeSql(todocsql);
				                	 if(rs.next()){
				                		 isWorkFlowToDoc=true;
				                	 }
				               }
				                
			                //end

                    }
					
					//TD9472	2	Start	查询流程生成计划任务（总体）设置，取得到达nextnodeids时和离开nodeid时生成计划任务的情况，生成计划任务
                    int isusedworktask = Util.getIntValue(getPropValue("worktask","isusedworktask"), 0);
                    if(isusedworktask == 1){
						sql = "select * from workflow_createtask where wfid="+this.workflowid+" and ((nodeid="+this.nodeid+" and changetime=2) or (changetime=1 and nodeid in ("+wf_nextnodeids+"0)))";
						rst.execute(sql);
						while(rst.next()){
							int creatertype_tmp = Util.getIntValue(rst.getString("creatertype"), 0);
							if(creatertype_tmp == 0){
								continue;
							}
							int createtaskid_tmp = Util.getIntValue(rst.getString("id"), 0);
							int wffieldid_tmp = Util.getIntValue(rst.getString("wffieldid"), 0);
							int taskid_tmp = Util.getIntValue(rst.getString("taskid"), 0);
							int changemode_tmp = Util.getIntValue(rst.getString("changemode"), 0);
							int changenodeid_tmp = Util.getIntValue(rst.getString("nodeid"), 0);
							int changetime_tmp = Util.getIntValue(rst.getString("changetime"), 0);
							if(changenodeid_tmp!=this.nodeid && (","+wf_nextnodeids+",").indexOf(","+changenodeid_tmp+",")>-1){//首先保证触发节点是操作后到达的节点，并且触发节点在到达的节点之中
								if(changetime_tmp==1 && (("submit".equals(src) && changemode_tmp==2) || ("reject".equals(src) && changemode_tmp==1))){
									//必须是达到触发的设置，如果勾选前一节点退回（changemode_tmp==2），那么如果是提交的，就不触发了；如果勾选前一节点提交（changemode_tmp==1），那么如果是退回，就不触发了
									continue;
								}
							}
							if(changenodeid_tmp==this.nodeid){//如果是离开节点
								if(changetime_tmp==2 && (("submit".equals(src) && changemode_tmp==2) || ("reject".equals(src) && changemode_tmp==1))){
									//必须是离开触发的设置，如果勾选退回（changemode_tmp==2），那么如果是提交的，就不触发了；如果勾选提交（changemode_tmp==1），那么如果是退回，就不触发了
									continue;
								}
							}
							RequestCreateByWF requestCreateByWF = new RequestCreateByWF();
							requestCreateByWF.setWf_formid(this.formid);
							requestCreateByWF.setWf_isbill(this.isbill);
							requestCreateByWF.setWf_wfid(this.workflowid);
							requestCreateByWF.setWf_requestid(this.requestid);
							requestCreateByWF.setWt_creatertype(creatertype_tmp);
							requestCreateByWF.setWt_creater(this.creater);
							requestCreateByWF.setWf_fieldid(wffieldid_tmp);
							requestCreateByWF.setWt_wtid(taskid_tmp);
							requestCreateByWF.setCreatetaskid(createtaskid_tmp);
							requestCreateByWF.createWT();
	                    }
                    }
					//TD9472	2	End

                    // 更新操作者信息 1,删除当前批注人 2, 将当前操作者根据组的追踪情况处理 3, 加入新的操作者
					RecordSet rs1 = new RecordSet();
	                //modify by mackjoe at 2005-09-29 td1772 不屏蔽删除转发人
					rst.executeSql("update workflow_currentoperator set isremark='2', preisremark='5',operatedate='"+currentdate+"',operatetime='"+currenttime+"' where isremark ='5' and requestid=" + requestid+" and nodeid="+nodeid);
					if(src.equals("reject")) {
						rst.executeProc("workflow_CurOpe_UpdatebyReject", "" + requestid+flag+nodeid);
                        if(wflinkinfo.getNodeAttribute(nodeid)!=2){
                            rst.executeSql("update workflow_currentoperator set isreject='1' where (isreject is null or isreject!='1') and requestid="+requestid);
                        }else{
                        	if(this.nextnodeattr!=2){
                        		sql = "update workflow_currentoperator set isremark='2',operatedate='"+currentdate+"',operatetime='"+currenttime+"' where userid != "+userid+" and nodeid in (select nownodeid from workflow_nownode where requestid="+requestid+") and (isremark='0' or isremark='1') and requestid=" + requestid;
								rst.executeSql(sql);
                        	}
                        }
						//TD17640 如果当前操作者同时被转发，如果是同节点或不是分叉中间节点，都设置为已操作
						sql = "update workflow_currentoperator set isremark='2',operatedate='"+currentdate+"',operatetime='"+currenttime+"' where isremark='1' and (nodeid="+nodeid+" or nodeid in (select a.nodeid from workflow_flownode a,workflow_nodebase b where a.nodeid=b.id and b.nodeattribute in ('0','1','3','4','5')  and a.workflowid="+workflowid+")) and requestid=" + requestid+" and userid=" + userid + " and usertype=" + usertype;
						rst.executeSql(sql);
						//TD7382 解决协办人退回后 节点操作者依然含有协办人的问题
						sql = "update workflow_currentoperator set isremark='2',operatedate='"+currentdate+"',operatetime='"+currenttime+"' where isremark='7' and nodeid="+nodeid+" and requestid=" + requestid;
						rst.executeSql(sql);
						//退回后，同节点其他操作者流程转为已办
						sql = "update workflow_currentoperator set isremark='2',operatedate='"+currentdate+"',operatetime='"+currenttime+"' where userid != "+userid+" and nodeid="+nodeid+" and (isremark='0' or (isremark='1' and takisremark='2')) and requestid=" + requestid;
						rst.executeSql(sql);
						
						if(this.RejectToType>0){
                    		sql = "update workflow_currentoperator set isremark='2',operatedate='"+currentdate+"',operatetime='"+currenttime+"' where userid != "+userid+" and nodeid in (select nownodeid from workflow_nownode where requestid="+requestid+") and (isremark='0' or isremark='1') and requestid=" + requestid;
                    		rst.executeSql(sql);
                    	}
					}
					else {
                        if(isremark==0||isremark==7){
                            
                            //是否执行老的逻辑，即操作组同批次为非会签关系
                            if (!isExeOldFlowlogic) {
                                //当前流程可以流向下一节点，需要更新同一操作组（节点操作组）的其他人员（同一操作组的同批次之间为非会签关系）
                                rst.executeSql("select distinct userid,usertype from workflow_currentoperator where isremark = '"+isremark+"' and requestid=" + requestid + "and nodeid="+nodeid+" and groupid in(select distinct groupid from workflow_currentoperator where isremark = '"+isremark+"' and requestid=" + requestid +" and nodeid="+nodeid+")");                                
                            } else {
                                //当前流程可以留下下一节点，需要更新与当前人员同组（会签的组）的人员
                                rst.executeSql("select distinct userid,usertype from workflow_currentoperator where isremark = '"+isremark+"' and requestid=" + requestid + "and nodeid="+nodeid+" and groupid in(select distinct groupid from workflow_currentoperator where isremark = '"+isremark+"' and requestid=" + requestid +" and userid=" + userid +" and usertype=" + usertype+" and nodeid="+nodeid+")");
                            }
                            while(rst.next()){
                                submituserids.add(rst.getString("userid"));
                                submitusertypes.add(rst.getString("usertype"));
                            }
                            List tmpgroupidList=new ArrayList();
                            
                          //是否执行老的逻辑，即操作组同批次为非会签关系
                            if (!isExeOldFlowlogic) {
                                //当前流程可以流向下一节点，需要更新同一操作组（节点操作组）的其他人员（同一操作组的同批次之间为非会签关系）
                                rst.executeSql("select distinct groupid from workflow_currentoperator where isremark = '"+isremark+"' and requestid=" + requestid + " and nodeid="+nodeid);
                            } else {
                                //当前流程可以留下下一节点，需要更新与当前人员同组（会签的组）的人员
                                rst.executeSql("select distinct groupid from workflow_currentoperator where isremark = '"+isremark+"' and requestid=" + requestid + " and userid=" + userid + " and usertype=" + usertype+" and nodeid="+nodeid);                               
                            }
                            while(rst.next()){
                            	tmpgroupidList.add(""+Util.getIntValue(rst.getString(1), 0));
                            }
                            for(int i=0;i<tmpgroupidList.size();i++) {
                                int tmpgroupid = Util.getIntValue((String)tmpgroupidList.get(i), 0);

                                if(!"0".equals(needwfback)){
                                    //rst.executeProc("workflow_CurOpe_UpdatebySubmit", "" +userid +flag + requestid + flag + tmpgroupid+flag+nodeid+flag+isremark + flag + this.currentdate + flag + this.currenttime);
                                    workflow_CurOpe_UpdatebySubmit(rst, requestid, nodeid, userid, currentdate, currenttime, tmpgroupid, isremark);
                                    if(isremark==7){
                                    	rst.executeSql("update workflow_currentoperator set operatedate='"+this.currentdate+"',operatetime='"+this.currenttime+"' where  isremark='7' and requestid ="+requestid+" and userid="+userid+" and nodeid="+nodeid);
                                    }else{
                                    	rst.executeSql("update workflow_currentoperator set operatedate='"+currentdate+"',operatetime='"+currenttime+"' where (isremark = '5' or isremark='0' or isremark='1' or isremark='8' or isremark='9' or isremark='7') and requestid ="+requestid+" and userid="+userid+" and nodeid="+nodeid+" and groupid="+tmpgroupid);
										this.updateworkflowcurrenttakingopsoperator(rst,tmpgroupid);

                                    }
                                    if( (isremark==7 && wfcm.getSigntype().equals("0")) || (wfcm.getSignorder()==0 && wfcm.getSigntype().equals("1"))){//协办人非会签 协办人提交到下一节点后未提交的主办人和协办人流程都置为已办 
            	    					rst.executeSql("update workflow_currentoperator set isremark ='2' where ( isremark='0' or isremark='7') and requestid ="+requestid+" and nodeid="+nodeid);
                                    }
                                }else{
                                	//boolean istrue=  rst.executeProc("workflow_CurOpe_UbySubmitNB", "" +userid +flag + requestid + flag + tmpgroupid+flag+nodeid+flag+isremark + flag + this.currentdate + flag + this.currenttime);
                                    workflow_CurOpe_UbySubmitNB(rst, requestid, nodeid, userid, currentdate, currenttime, tmpgroupid, isremark);
                                	rst.executeSql("update workflow_currentoperator set operatedate='"+this.currentdate+"',operatetime='"+this.currenttime+"' where (isremark = '5' or isremark='0' or isremark='1' or isremark='8' or isremark='9' or isremark='7') and requestid ="+requestid+" and userid="+userid+" and nodeid="+nodeid);
									this.updateworkflowcurrenttakingopsoperator(rst,tmpgroupid);
            	    				if((isremark==7 && wfcm.getSigntype().equals("0")) || (wfcm.getSignorder()==0 && wfcm.getSigntype().equals("1"))){//部门非会签协办人会签 提交到下一节点后未提交的主办人和协办人流程都置为已办 
            	    					rst.executeSql("update workflow_currentoperator set isremark ='2' where ( isremark='0' or isremark='7') and requestid ="+requestid+" and nodeid="+nodeid);
            	    				}                                  
								}
                            }
                        }
	                    if(isremark==5||src.equals("intervenor")){//流程干预者（超时）提交时更新节点操作者，被转发者及被抄送者信息。
	                        rst.executeSql("update workflow_currentoperator set isremark='2',operatedate='"+currentdate+"',operatetime='"+currenttime+"' where (isremark = '5' or isremark='0' or isremark='1' or isremark='8' or isremark='9' or isremark='7') and requestid=" + requestid+" and nodeid="+nodeid );
                            String curr_nodeattribute = "0";
	                        String next_nodeattribute = "0";
	                        rst.executeSql("select nodeattribute from workflow_nodebase where id="+nodeid);
	                        if(rst.next()){
	                        	curr_nodeattribute = rst.getString(1);
	                        }
	                        rst.executeSql("select nodeattribute from workflow_nodebase where id="+nextnodeid);
	                        if(rst.next()){
	                        	next_nodeattribute = rst.getString(1);
	                        }
	                        if(!next_nodeattribute.equals("2")&&curr_nodeattribute.equals("2")){
	                        	rst.executeSql("update workflow_currentoperator set isremark='2',operatedate='"+currentdate+"',operatetime='"+currenttime+"' where (isremark = '5' or isremark='0' or isremark='1' or isremark='8' or isremark='9' or isremark='7') and requestid=" + requestid+" and nodeid!="+nextnodeid );
	                        }
	                        if(next_nodeattribute.equals("2")&&curr_nodeattribute.equals("2")){
	                        	//中间节点干预到中间节点 不操作
	                        	rst.executeSql("update workflow_currentoperator set takisremark=null where requestid=" + requestid+" and takisremark=-2 and nodeid="+nodeid);
	                        }else{
	                        	//更新意见征询标识 ,takisremark=null
	                        	rst.executeSql("update workflow_currentoperator set takisremark=null where requestid=" + requestid+" and takisremark=-2 and nodeid!="+nextnodeid);
	                        }
	                    }else{
                            if(canflowtonextnode && (nextnodeattr==3 || nextnodeattr==4|| nextnodeattr==5)){
                                innodeids= wflinkinfo.getSummaryNodes(nextnodeid,workflowid,"",requestid);
                                if(innodeids.equals("")) innodeids="0";
                                //System.out.println("innodeids:"+innodeids);
                                List tmpgroupidList=new ArrayList();    
                                rst.executeSql("select distinct groupid from workflow_currentoperator where (isremark='0' or isremark = '5') and requestid=" + requestid + " and userid=" + userid + " and usertype=" + usertype+" and nodeid in("+innodeids+")");
                                while (rst.next()) {
                                	tmpgroupidList.add(""+Util.getIntValue(rst.getString(1), 0));
                                }
                                for(int i=0;i<tmpgroupidList.size();i++){
                                    int tmpgroupid = Util.getIntValue((String)tmpgroupidList.get(i), 0);

                                    rst.executeSql("update workflow_currentoperator set operatedate='"+currentdate+"',operatetime='"+currenttime+"' where (isremark='0' or isremark = '5') and requestid=" + requestid+ " and nodeid in("+innodeids+") and groupid="+tmpgroupid);
									this.updateworkflowcurrenttakingopsoperator(rst,innodeids,tmpgroupid);
                                }
                                rst.executeSql("update workflow_currentoperator set isremark='2' where (isremark='0' or isremark = '5' or (isremark=1 and takisremark=2)) and requestid=" + requestid+" and nodeid in("+innodeids+")" );
								

                            }
	                    }
                        if(isremark==0){
                            String isremarktmp = "";
                            //65537 yl 协办人模块
                            //isforwards //协办人是否可以转发
                            //coadispendings  //协办人是否一直待办
                            //coadismodifys//协办人是否可以修改
                            //isSubmitedOpinions//协办人是否可以提交
                            //userids //协办人组
                            //coadsigntypes //协办人会签条件
                            if(null!=userids && userids.size()>0){
                                for(int i = 0;i<userids.size();i++){
                                    if(!coadispendings.get(i).equals("1")){  //没有勾选“未查看停留在待办”
                                        sql = "update workflow_currentoperator set isremark='2' where isremark='7' and requestid=" + requestid+" and nodeid="+nodeid + " and userid= "+userids.get(i);
                                        rst.executeSql(sql);
                                    }else if(coadispendings.get(i).equals("1")){ //如果勾选了并且查看过则改为已办
                                    	 sql = "select viewtype from  workflow_currentoperator where isremark='7' and requestid=" + requestid+" and nodeid="+nodeid + " and userid= "+userids.get(i);
                                    	 rs1.executeSql(sql);
                                         if(rs1.next() && (rs1.getString("viewtype").equals("-2") || rs1.getString("viewtype").equals("-1") )){
                                             sql = "update workflow_currentoperator set isremark='2' where isremark='7' and requestid=" + requestid+" and nodeid="+nodeid + " and userid= "+userids.get(i);
                                             rst.executeSql(sql);
                                         }
                                    }
									wffm.updateForwardRemark(rst, IsBeForwardPending, IsSubmitedOpinion, IsBeForwardSubmit, true);
                                }
                            }else{
                                if(!coadsigntype.equals("1")&& !coadispending.equals("1")){  //会签关系为非会签或不影响流程流转 并且没有勾选“未查看停留在待办”
                                    sql = "update workflow_currentoperator set isremark='2' where isremark='7' and requestid=" + requestid+" and nodeid="+nodeid;
                                    rst.executeSql(sql);
                                }
                                wffm.updateForwardRemark(rst, IsBeForwardPending, IsSubmitedOpinion, IsBeForwardSubmit, true);								
                            }
                            //TD17640 如果当前操作者同时被转发，如果是同节点或不是分叉中间节点，都设置为已操作
                            sql = "update workflow_currentoperator set isremark='2',operatedate='"+currentdate+"',operatetime='"+currenttime+"' where isremark='1' and (nodeid="+nodeid+" or nodeid in (select a.nodeid from workflow_flownode a,workflow_nodebase b where a.nodeid=b.id and b.nodeattribute in ('0','1','3','4')  and a.workflowid="+workflowid+")) and requestid=" + requestid+" and userid=" + userid + " and usertype=" + usertype;
                            rst.executeSql(sql);
                        }else if(isremark==7&&coadsigntype.equals("0")){   //协办人提交 并且会签关系为非会签
                            String isremarktmp = "";
                            rst.executeSql("update workflow_currentoperator set isremark='2' where (isremark='0' or isremark='7') and requestid='"+requestid+"' and nodeid='"+nodeid+"' and groupdetailid in("+wfcm.getSameGroupdetailids()+")");
                            wffm.updateForwardRemark(rst, IsBeForwardPending, IsSubmitedOpinion, IsBeForwardSubmit, true);                           
                        }
	                }
	
	                //操作人插入操作
	                if(isremark==5||src.equals("intervenor")){
                        canflowtonextnode=true;
                        setOperatorByremark5(tempusers,tempusersType,rst);
	                }else{
	                	setOperator(rst);
	                }
	
	                //将更新操作移到消息提醒前 td3608
	                if (canflowtonextnode&&nextnodetype.equals("3")) {
						Procpara = "" + creater + flag + creatertype + flag + requestid;
	
						//流转下一节点，清空节点签字意见权限控制
						RequestRemarkRight reqRight = new RequestRemarkRight();
						reqRight.setRequestid(requestid);
						reqRight.setNodeid(nodeid);
						reqRight.deleteAllRight();   //归档时，清除所有的签字意见权限控制
						//modify by xhheng @20050520 for TD1725,添加条件 isremark='0' 使能区分历史操作人和归档人
						// 2005-03-24 Guosheng for TD1725**************************************
						rst.executeSql("update  workflow_currentoperator  set isremark='4'  where (isremark='0' or isremark='5') and requestid = " +  requestid);
						//考虑到流程转发和抄送的接收人可能也是归档人，如果该人还没处理，在待办事宜里应该显示 by ben for td5020
						rst.executeSql("update workflow_currentoperator set islasttimes='1' where islasttimes='0' and (isremark='1' or isremark='8' or isremark='9') and requestid = " + requestid+" and exists (select 1 from workflow_currentoperator a where a.requestid=workflow_currentoperator.requestid and a.userid=workflow_currentoperator.userid and a.usertype=workflow_currentoperator.usertype and a.isremark='4')");
						rst.executeSql("update workflow_currentoperator set islasttimes='0' where islasttimes='1' and isremark='4' and requestid = " + requestid+" and exists (select 1 from workflow_currentoperator a where a.requestid=workflow_currentoperator.requestid and a.userid=workflow_currentoperator.userid and a.usertype=workflow_currentoperator.usertype and (a.isremark='1' or a.isremark='8' or a.isremark='9'))");
						
						rst.executeSql("update  workflow_currentoperator  set iscomplete=1  where isremark!='8' and isremark!='9' and isremark!='7' and requestid = " +  requestid );//抄送者未做操作不更新
						//update by fanggsh for TD4739  20060808 果归档前的最后一个操作者是本人的话，则从提醒中去掉。
						if(!operatorsWfEnd.contains(creater+"_"+creatertype)&&!(userid+"_"+usertype).equals(creater+"_"+creatertype)){//xwj for td3450 20060111
							//poppupRemindInfoUtil.insertPoppupRemindInfo(creater,1,""+creatertype,requestid,requestname,workflowid);
							Map map=new HashMap();
						    map.put("userid",""+Util.getIntValue(rs.getString("creater")));
						    map.put("type","1");
						    map.put("logintype",""+creatertype);
						    map.put("requestid",""+requestid);
						    map.put("requestname",""+requestname);
						    map.put("workflowid",""+workflowid);
						    map.put("creater",""+creater);
						    poppuplist.add(map);
						    poppupRemindInfoUtil.insertPoppupRemindInfo(poppuplist);
						}
					}
	
	                //add by xhheng @20050125 for 消息提醒 request06 ,短信发送
					//int rqMessageType=0;
					//String mailrequestname = "";
					//int level=0;
					//rst.executeSql("select messageType,requestname,requestlevel from workflow_requestbase where requestid="+requestid);
					//if (rst.next()) {
					//  rqMessageType=rst.getInt("messageType");
					//  mailrequestname=rst.getString("requestname");
					//  level=Util.getIntValue(rst.getString("requestlevel"),0);
					//}
					 for(int n=0;n<nextnodeids.size();n++){
						 nextnodeid=Util.getIntValue((String)nextnodeids.get(n));
						 if(istest != 1){
							 //发送短信、微信、邮件提醒
							 sendMsgAndMail.sendMsg(rst,requestid,nextnodeid,user,src,nextnodetype);
							 sendMsgAndMail.sendChats(rst,workflowid,requestid,nextnodeid,user,src,nextnodetype);//微信提醒(QC:98106)
							 sendMsgAndMail.sendMail(rst,workflowid,requestid,nextnodeid,request,fu,isRequest,src,nextnodetype,user);
						 }
					 }

				    //triggerStatus  ""  成功
					//               "1" 子流程创建人无值
					String triggerStatus="";
					boolean nextNodeHasCurrentNode=false;
					for(int n=0;n<nextnodeids.size();n++){
						nextnodeid=Util.getIntValue((String)nextnodeids.get(n));
						if(nextnodeid==nodeid){
							nextNodeHasCurrentNode=true;
						}
					}
					if(nextNodeHasCurrentNode==false && nextnodeid>0 && nextnodeid!=nodeid){
						nodeidList_sub.add(""+nodeid);
						triggerTimeList_sub.add("2");
						hasTriggeredSubwfList_sub.add(hasTriggeredSubwf);
						//triggerStatus=subwfTriggerManager.TriggerSubwf(this,nodeid,"2",hasTriggeredSubwf,user);
					}
					if(nextnodeids!=null && nextnodeids.size()>0 && !nextnodeids.contains(""+nodeid)){
						nodeidList_wp.add(""+nodeid);
						createTimeList_wp.add("2");//离开节点
					}

					for(int n=0;n<nextnodeids.size();n++){
						nextnodeid=Util.getIntValue((String)nextnodeids.get(n));
						if(nextnodeid>0&&nextnodeid!=nodeid){
							nodeidList_sub.add(""+nextnodeid);
							triggerTimeList_sub.add("1");
							hasTriggeredSubwfList_sub.add(hasTriggeredSubwf);
							//triggerStatus=subwfTriggerManager.TriggerSubwf(this,nextnodeid,"1",hasTriggeredSubwf,user);

							//TD13304 在这里记录需要触发日程的节点和触发类型（到达节点或离开节点），到流程事务外处理
							nodeidList_wp.add(""+nextnodeid);
							createTimeList_wp.add("1");//到达节点
						}
					}
					isReCalculatePermission = true;
					
					//流转下一节点，清空节点签字意见权限控制
					RequestRemarkRight reqRight = new RequestRemarkRight();
					reqRight.setRequestid(requestid);
					reqRight.setNodeid(nodeid);
					reqRight.deleteRemarkRight();
				} else {
					//处理超时流程提醒
	                if (!groupdetailids.equals("")&&coadcansubmit){

	                }

					// 更新操作者信息 1,删除当前组批注人 2, 将当前操作者根据组的追踪情况处理
					RecordSet rs1 = new RecordSet();
                    if(isremark==0||isremark==7){

                    }
	//				如果是依次逐个递交
	                if (!groupdetailids.equals("")&&coadcansubmit && isremark!=7)
	                {
	                	int isreject = 0;
		                if (src.equals("reject"))
							isreject = 1;
	        			// 查询下一个操作者
	    				RequestNodeFlow requestNodeFlow = new RequestNodeFlow();
	    				requestNodeFlow.setRequestid(requestid);
	    				requestNodeFlow.setNodeid(nodeid);
	    				requestNodeFlow.setNodetype(nodetype);
	    				requestNodeFlow.setWorkflowid(workflowid);
	    				requestNodeFlow.setUserid(userid);
	    				requestNodeFlow.setUsertype(usertype);
	    				requestNodeFlow.setCreaterid(creater);
	    				requestNodeFlow.setCreatertype(creatertype);
	    				requestNodeFlow.setFormid(formid);
	    				requestNodeFlow.setIsbill(isbill);
	    				requestNodeFlow.setBillid(billid);
	    				requestNodeFlow.setBilltablename(billtablename);
	    				requestNodeFlow.setRecordSet(rs);
	    				requestNodeFlow.setRecordSetTrans(rst);
	    				requestNodeFlow.setIsreject(isreject);
						 //add by liaodong for qc48451 in 2013年10月21日 start 
						requestNodeFlow.setIsfixbill(isfixbill);
						requestNodeFlow.setLanguageid(this.userlanguage);
						//end
						// add by liaodong for qc86096 in 2013-11-19 start
					   requestNodeFlow.setCurrentdate(currentdate);
					   requestNodeFlow.setCurrenttime(currenttime);
					   //end
	    				boolean hasnextnodeoperator = requestNodeFlow.getNextOrderOperator(groupdetailids);
	    				if (!hasnextnodeoperator) {
	    					saveRequestLog("1");
	    					if(rst!=null){
	    					    new Exception(RequestManager.ERROR_NOTFOUND_NNNO).printStackTrace();
	    						rst.rollback();
	    					}
	                        this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_07);
	                        this.setMessagecontent(requestNodeFlow.getMessagecontent());
                            return false;
	    				}

	    				nextnodeid=nodeid;
	    				nextnodetype=nodetype;
                        nextnodeattr=wflinkinfo.getNodeAttribute(nextnodeid);
                        nextnodeids.add(""+nextnodeid);
                        nextnodetypes.add(nodetype);
                        nextnodeattrs.add(""+nextnodeattr);
                        operatorsht = requestNodeFlow.getOperators();
                        operatorshts.add(operatorsht);
	    				haspassthisnode = true;//added by pony for TD4485
                       //add by liaodong for qc76119,76120,54194,64652 start
	                    penetrateFlag = requestNodeFlow.getPenetrateId(); 
	                    lastNodeId = requestNodeFlow.getLastNodeId();
	                   //end
						//处理超时流程提醒
		                this.updatePoppupRemindInfoThisJava(rst,this.requestid);

						// 更新操作者信息 1,删除当前组批注人 2, 将当前操作者根据组的追踪情况处理
	                    if(isremark==0||isremark==7){
                            this.updateworkflowcurrentoperator(rst,submituserids,submitusertypes, finishndgpids);
	                    }	    				
	    				
	    				if(isremark==0){
							wffm.updateForwardRemark(rst, IsBeForwardPending, IsSubmitedOpinion, IsBeForwardSubmit, false);
                        }else if(isremark==7&&coadsigntype.equals("0")){   //协办人提交
                            String isremarktmp = "";
                            if (!coadispending.equals("1")) {   //协办人未查看停留在待办
                            	isremarktmp = " (isremark='0' and viewtype=0) ";
                            }
                            if (!isremarktmp.equals("")){
                                sql = "update workflow_currentoperator set isremark='2' where (" + isremarktmp + ") and (exists (select 1 from workflow_currentoperator c where c.requestid="+requestid+" and c.nodeid=workflow_currentoperator.nodeid and c.groupdetailid=workflow_currentoperator.groupdetailid and c.id=" + RequestKey + " and exists(select id from workflow_groupdetail g where g.id=c.groupdetailid and g.signtype='0') and exists (select 1 from workflow_coadjutant a where a.requestid="+requestid+" and a.coadjutantid=" + RequestKey + ")) ";
                                sql+=")";
                                rst.executeSql(sql);
                            }
							wffm.updateForwardRemark(rst, IsBeForwardPending, IsSubmitedOpinion, IsBeForwardSubmit, false);
                        }
						sql = "update workflow_currentoperator set isremark='2' where (isremark='0' or isremark='5') and requestid=" + requestid + " and nodeid=" + nodeid+" and groupdetailid in ("+groupdetailid4sql+")";
                        rst.executeSql(sql);
	    				setOperator(rst);
                        showcoadjutant=true;
                        if(istest != 1){
	                        //依次逐个提交对下一个操作者短信提醒 ==start==
	                        sendMsgAndMail.sendMsg(rst,requestid,nextnodeid,user,src,nextnodetype);
	                        //依次逐个提交对下一个操作者短信提醒 ==end==
	                        sendMsgAndMail.sendChats(rst,workflowid,requestid,nextnodeid,user,src,nextnodetype);//微信提醒(QC:98106)
	                        //依次逐个提交对下一个操作者邮件提醒 ==start==
	                        sendMsgAndMail.sendMail(rst,workflowid,requestid,nextnodeid,request,fu,isRequest,src,nextnodetype,user);
	                        //依次逐个提交对下一个操作者邮件提醒 ==end==
                        }
                        isReCalculatePermission = true;
	                }else{
                        nextnodeid=nodeid;
                        nextnodetype=nodetype;
                        nextnodeattr=wflinkinfo.getNodeAttribute(nextnodeid);
                        nextnodeids.add(""+nextnodeid);
                        nextnodetypes.add(nodetype);
                        nextnodeattrs.add(""+nextnodeattr);
                        operatorshts.add(operatorsht);
                        
                        WFAutoApproveUtils.processApproveLog(rst,this);
						// 更新操作者信息 1,删除当前组批注人 2, 将当前操作者根据组的追踪情况处理
	                    if(isremark==0||isremark==7){
                            this.updateworkflowcurrentoperator(rst,submituserids,submitusertypes, finishndgpids);
	                    }  
	                    
                        if(isremark==0){
							int coadjutantuser = 0;
                            
							coadjutantuser = wfcm.getCoadjutantUser(wfcm.getGroupdetailid(), nodeid, requestid,rst);
                            if("0".equals(wfcm.getSigntype())){ //协办人非会签
                            	boolean isupcoad = false;
                            	if(wfcm.getSignorder()==1 || wfcm.getSignorder()==2){
                            		rst.executeSql("select userid from workflow_currentoperator where isremark='0' and requestid='"+requestid+"' and nodeid='"+nodeid+"' and groupdetailid ='"+wfcm.getGroupdetailid()+"'");                             	  
                            	    isupcoad = !rst.next();
									if(userid==coadjutantuser) isupcoad = true;
                            	}else if(wfcm.getSignorder()==0){
                            		rst.executeSql("select userid from workflow_currentoperator where isremark='2' and preisremark='0' and  (isreject is null or isreject=0) and requestid='"+requestid+"' and nodeid='"+nodeid+"' and groupdetailid ='"+wfcm.getGroupdetailid()+"'");                          	  
                            	    isupcoad = rst.next();
                            	}
                            	if(isupcoad){
                                    if(!wfcm.getIspending().equals("1")){  //没有勾选“未查看停留在待办”
                                        sql = "update workflow_currentoperator set isremark='2' where isremark='7' and requestid=" + requestid+" and nodeid="+nodeid + " and userid= "+coadjutantuser;
                                        rst.executeSql(sql);
                                    }else if(wfcm.getIspending().equals("1")){ //如果勾选了并且查看过则改为已办
                                    	 sql = "select viewtype from  workflow_currentoperator where isremark='7' and requestid=" + requestid+" and nodeid="+nodeid + " and userid= "+coadjutantuser;
									     rst.executeSql(sql);
                                         if(rst.next() && (rst.getString("viewtype").equals("-2") || rst.getString("viewtype").equals("-1") )){
                                             sql = "update workflow_currentoperator set isremark='2' where isremark='7' and requestid=" + requestid+" and nodeid="+nodeid + " and userid= "+coadjutantuser;
                                             rst.executeSql(sql);
                                         }
                                    }
                                }
                            }else if("1".equals(wfcm.getSigntype())){ //协办人会签 主办人非会签，当协办人提交后，一个主办人提交后其他主办人不再提交（基于多操作组时）
                            	if(wfcm.getSignorder()==0){
                            		rs1.executeSql("select userid from workflow_currentoperator where isremark='2' and preisremark='7' and groupdetailid='"+wfcm.getGroupdetailid()+"' and requestid='"+requestid+"'");
                            	    if(rs1.next()){
                            	    	rst.executeSql("update workflow_currentoperator set isremark='2' where isremark='0' and requestid='" + requestid+"' and nodeid='"+nodeid + "' and groupdetailid='"+wfcm.getGroupdetailid()+"'");
                            	    }
                            	}else if(wfcm.getSignorder()==1 && isremark==0 && userid==coadjutantuser){
                            		    rst.executeSql("update workflow_currentoperator set isremark='2',operatedate='"+this.currentdate+"',operatetime='"+this.currenttime+"' where isremark='7' and requestid='" + requestid+"' and nodeid='"+nodeid + "' and userid='"+userid+"' and groupdetailid='"+wfcm.getGroupdetailid()+"'");
                            	}
                            }else if("2".equals(wfcm.getSigntype())){ 
                                if(!wfcm.getIspending().equals("1")){  //没有勾选“未查看停留在待办”
                                	rst.executeSql("select id from  workflow_currentoperator where isremark='0' and requestid=" + requestid+" and nodeid="+nodeid + " and groupdetailid= "+wfcm.getGroupdetailid());
                                    if(!rst.next()){
	                                    sql = "update workflow_currentoperator set isremark='2' where isremark='7' and requestid=" + requestid+" and nodeid="+nodeid + " and userid= "+coadjutantuser;
	                                    rst.executeSql(sql);
                                    }
                                }else if(wfcm.getIspending().equals("1")){ //如果勾选了并且查看过则改为已办
                                	 sql = "select viewtype from  workflow_currentoperator where isremark='7' and requestid=" + requestid+" and nodeid="+nodeid + " and userid= "+coadjutantuser;
                                	 rst.executeSql(sql);
                                     if(rst.next() && (rst.getString("viewtype").equals("-2") || rst.getString("viewtype").equals("-1") )){
                                    	 rst.executeSql("select id from  workflow_currentoperator where isremark='0' and requestid=" + requestid+" and nodeid="+nodeid + " and groupdetailid= "+wfcm.getGroupdetailid());
                                         if(!rst.next()){
	                                    	 sql = "update workflow_currentoperator set isremark='2' where isremark='7' and requestid=" + requestid+" and nodeid="+nodeid + " and userid= "+coadjutantuser;
	                                         rst.executeSql(sql);
                                         }
                                     } 
                                }
                            }

                            wffm.updateForwardRemark(rst, IsBeForwardPending, IsSubmitedOpinion, IsBeForwardSubmit, true);
							
                        }else if(isremark==7&&coadsigntype.equals("0")){   //协办人提交 会签关系为非会签
                            String isremarktmp = "";
							rst.executeSql("update workflow_currentoperator set isremark='2' where (isremark='0' or isremark='7') and requestid='"+requestid+"' and nodeid='"+nodeid+"' and groupdetailid in("+wfcm.getSameGroupdetailids()+")");
							
							wffm.updateForwardRemark(rst, IsBeForwardPending, IsSubmitedOpinion, IsBeForwardSubmit, false);
                          
                        }else if(isremark==7&&coadsigntype.equals("1")){//协办人会签关系
                        	if(wfcm.getSignorder()==0){
                        		rs1.executeSql("select userid from workflow_currentoperator where isremark='2' and preisremark='0' and (isreject is null or isreject='0') and groupdetailid='"+wfcm.getGroupdetailid()+"' and requestid='"+requestid+"'");
                        	    if(rs1.next()){
                        	    	rst.executeSql("update workflow_currentoperator set isremark='2' where isremark='0' and requestid='" + requestid+"' and nodeid='"+nodeid + "' and groupdetailid='"+wfcm.getGroupdetailid()+"'");
                        	    }
                        	}
                        }
                    }

                    // 更新 requestbase 表信息
					//因为强制收回后status更改了，所以此处要修改该节点的status add by ben 2006-3-29
					//rst.execute("select *  from workflow_nodelink where destnodeid="+nodeid+" and (isreject='' or isreject is null)");
					//if (rst.next())
					status = new RequestNodeFlow().getLinkName(nodeid,billid,requestid,isbill,billtablename);
	                if(canflowtonextnode){
                        if(nextnodeattr==1){
                            status = SystemEnv.getHtmlLabelName(21394,userlanguage);
                        }else if(nextnodeattr==2){
                            status = SystemEnv.getHtmlLabelName(21395,userlanguage);
                        }
                    }else{
                        status = SystemEnv.getHtmlLabelName(21395,userlanguage);
                    }
	                
	                //----------------------------------------------
	                // 获取表单数据签名 start
	                //----------------------------------------------
	                String formsignaturemd5 = "";
	                try {
	                    if ("submit".equals(src)) {
	                        formsignaturemd5 = WFPathUtil.getFormValMD5(workflowid, requestid, isbill, formid, requestname, this.nodeid);
	                    }
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	                //----------------------------------------------
                    // 获取表单数据签名 end
                    //----------------------------------------------
					if (status.equals(""))
					{
						sql = " update workflow_requestbase set " +
						" passedgroups = " + passedgroups +

						" ,requestname = '" + Util.fromScreen2(requestname, userlanguage) + "' " +
						" ,lastoperator = " + userid ;
						if(!tempcurrentnodetype.equals("3")){//归档流程不更新lastoperatedate和lastoperatetime
						sql = sql +
						" ,lastoperatedate = '" + currentdate + "' " +
						" ,lastoperatetime = '" + currenttime + "' " ;
					  }
					  sql = sql +
						" ,lastoperatortype = " + usertype ;
                            if(!((isremark==1||isremark==7)&&!CanModify)){
							sql+=" ,docids = '" + docids + "' " +
							" ,crmids = '" + crmids + "' ";
							if(isoracle){
								sql+=" ,hrmids = ? ";
							}else{
								sql+=" ,hrmids = '"+hrmids+"' ";
							}
							sql+=" ,prjids = '" + prjids + "' " +
							" ,cptids = '" + cptids + "' " ;
                            }
							//sql+=" where requestid = " + requestid;
					}
					else
					{
					sql = " update workflow_requestbase set " +
							" passedgroups = " + passedgroups +
							" ,status = '" + Util.fromScreen2(status, userlanguage) + "' " +
							" ,requestname = '" + Util.fromScreen2(requestname, userlanguage) + "' " +
							" ,lastoperator = " + userid ;
						  if(!tempcurrentnodetype.equals("3")){//归档流程不更新lastoperatedate和lastoperatetime
						  sql = sql +
						  " ,lastoperatedate = '" + currentdate + "' " +
						  " ,lastoperatetime = '" + currenttime + "' " ;
					    }
					    sql = sql +
							" ,lastoperatortype = " + usertype;
                            if(!((isremark==1||isremark==7)&&!CanModify)){
							sql+=" ,docids = '" + docids + "' " +
							" ,crmids = '" + crmids + "' ";
							if(isoracle){
								sql+=" ,hrmids = ? ";
							}else{
								sql+=" ,hrmids = '"+hrmids+"' ";
							}
							sql+=" ,prjids = '" + prjids + "' " +
							" ,cptids = '" + cptids + "' " ;
                            }
							//sql+=" where requestid = " + requestid;
					}
					
					//更新签名信息
                    if (!"".equals(formsignaturemd5)) {
                        sql += ", formsignaturemd5='" + formsignaturemd5 + "' ";
                    }
                    sql+=" where requestid = " + requestid;
                    
					//rs.executeSql(sql);
					
					if(isoracle&&!("0".equals(coadismodify)&&isremark==7)){
                    	rst.executeUpdate(sql, new Object[]{hrmids});
                    }else{
                    	rst.executeSql(sql);
                    }
					
					/******/
					/*
					if(isoracle){
						ConnStatement statement2 = null;
						try {
							String sql2 = "select hrmids from workflow_requestbase where requestid="+requestid+" for update";
							statement2 = new ConnStatement();
							statement2.setStatementSql(sql2, false);
							statement2.executeQuery();
			                if(statement2.next()){
				                CLOB theclob = statement2.getClob(1);
				                char[] contentchar = hrmids.toCharArray();
				                Writer contentwrite = theclob.getCharacterOutputStream();
				                contentwrite.write(contentchar);
				                contentwrite.flush();
				                contentwrite.close();
			                }
						} catch (Exception e) {
							rs.writeLog(e);
							e.printStackTrace();
						} finally {
							if(statement2!=null) statement2.close();
						}
					}*/
					/******/
                    //依次逐个提交对下一个操作者短信提醒 ==start==
                    //sendMsgAndMail.sendMsg(rst,requestid,nextnodeid,user);
                    //依次逐个提交对下一个操作者短信提醒 ==end==
                    
                    //依次逐个提交对下一个操作者邮件提醒 ==start==
                    //sendMsgAndMail.sendMail(rst,workflowid,requestid,nextnodeid,request,fu,isRequest,src,nextnodetype,user);
                    //依次逐个提交对下一个操作者邮件提醒 ==end==
                    
	            }
	
				//将已查看操作人的查看状态置为（-1：新提交未查看）
				if (!ifchangstatus.equals("")&&isfeedback.equals("1")&&((isnullnotfeedback.equals("1")&&!Util.replace(remark, "\\<script\\>initFlashVideo\\(\\)\\;\\<\\/script\\>", "", 0, false).equals(""))||!isnullnotfeedback.equals("1")))
				{
					rst.executeSql("update workflow_currentoperator set viewtype =-1  where needwfback='1' and requestid=" + requestid + " and userid<>" + userid + " and viewtype=-2");
				}
				//将自己的查看状态置为（-2：已提交已查看）
				//TD21155，要求把当前操作人的未查看记录（operatedate为空的）改为当前时间。oracle和sqlserver下的SQL好像是一样的
				rst.executeSql("update workflow_currentoperator set viewtype =-2 where requestid=" + requestid + "  and userid=" + userid + " and usertype = "+usertype+" and viewtype<>-2");
				rst.executeSql("update workflow_currentoperator set operatedate='"+this.currentdate+"', operatetime='"+this.currenttime+"' where (operatedate is null or operatedate='') and requestid=" + requestid + "  and userid=" + userid + " and usertype = "+usertype+" and viewtype=-2");
			} else {                                        // save,active,delete等不流到下一个节点的
				if (src.equals("save")) {
					if (isremark == 1) {                   // 属于批注提交的节点
						// 更新操作者信息 1,删除当前批注人
						 if (cnodetype.equals("3"))
						 {
						  rs.executeSql("update workflow_currentoperator set islasttimes =0 where requestid=" + requestid + "  and userid=" + userid + " and usertype = "+usertype+"  " );
						  rs.executeSql("update workflow_currentoperator set isremark ='2', islasttimes =1,iscomplete=1 where requestid=" + requestid + "  and userid=" + userid + " and usertype = "+usertype+"  and isremark='1'" );
						  //代理人从已办设置为办结
						   rs.executeSql("update workflow_currentoperator set iscomplete=1 where requestid=" + requestid + " and isremark='2' and  agenttype = '1' and agentorbyagentid="+userid+" ");
						 }
						 else
						{
							 if(!"0".equals(needwfback)){
								 //rst.executeProc("workflow_CurOpe_UbyForward", "" + requestid + flag + userid + flag + usertype);
							     workflow_CurOpe_UbyForward(rst, requestid, userid, usertype, currentdate, currenttime);
							 }else{
								 //rst.executeProc("workflow_CurOpe_UbyForwardNB", "" + requestid + flag + userid + flag + usertype);
							     workflow_CurOpe_UbyForwardNB(rst, requestid, userid, usertype, currentdate, currenttime);
							 }
						}
	        			//将已查看操作人的查看状态置为（-1：新提交未查看）
	                    
	                	//考虑到流程转发的接收人可能也是归档人，如果该人处理后，在归档事宜里应该显示 by ben for td5020
						rst.executeSql("update workflow_currentoperator set islasttimes='0' where islasttimes='1' and isremark='2' and  requestid="+requestid+" and userid="+userid+" and usertype="+usertype+"  and exists (select 1 from workflow_currentoperator a where a.requestid=workflow_currentoperator.requestid and a.userid=workflow_currentoperator.userid and a.usertype=workflow_currentoperator.usertype and a.isremark='4')");
						rst.executeSql("update workflow_currentoperator set islasttimes='1' where islasttimes='0' and isremark='4' and requestid="+requestid+" and userid="+userid+" and usertype="+usertype+"  and id=(select max(id) from workflow_currentoperator  a where a.islasttimes=workflow_currentoperator.islasttimes and a.isremark=workflow_currentoperator.isremark and a.requestid=workflow_currentoperator.requestid and a.userid=workflow_currentoperator.userid and a.usertype=workflow_currentoperator.usertype)");
						
	                    //TD4294  删除workflow_currentoperator表中orderdate、ordertime列 fanggsh begin
						if (!ifchangstatus.equals("")&&isfeedback.equals("1")&&((isnullnotfeedback.equals("1")&&!Util.replace(remark, "\\<script\\>initFlashVideo\\(\\)\\;\\<\\/script\\>", "", 0, false).equals(""))||!isnullnotfeedback.equals("1"))) {
							rst.executeSql("update workflow_currentoperator set viewtype =-1  where needwfback='1' and requestid=" + requestid + " and userid<>" + userid + " and viewtype=-2");
						}
	        			haspassthisnode = true;//added by pony on 2006-06-09 for TD4463
					} else {
						////因为强制收回后status更改了，所以此处要修改该节点的status add by ben 2006-3-29
						//rst.execute("select * from  workflow_nodelink where destnodeid="+nodeid+" and (isreject='' or isreject is null)");
						//if (rst.next())
						if(isremark==7 && "2".endsWith(coadsigntype) && tempsrc.equals("submit")){ //当协办人会签关系是不影响流程流转且来自提交时提交后进入到已办而不是始终在待办可提交多次
							rst.executeSql("select * from workflow_currentoperator where iscomplete='1' and requestid='"+requestid+"'");
							if(rst.next()){
								rst.executeSql("update workflow_currentoperator set isremark='2',iscomplete='1',preisremark='7',operatedate='"+currentdate+"',operatetime='"+currenttime+"' where isremark ='7' and requestid=" + requestid+" and nodeid="+nodeid);
							}else{
								rst.executeSql("update workflow_currentoperator set isremark='2',preisremark='7',operatedate='"+currentdate+"',operatetime='"+currenttime+"' where isremark ='7' and requestid=" + requestid+" and nodeid="+nodeid+" and userid='"+userid+"'");							
							}
						}
						status = new RequestNodeFlow().getLinkName(nodeid,billid,requestid,isbill,billtablename);
                        nextnodeattr=wflinkinfo.getNodeAttribute(nodeid);
						if(canflowtonextnode){
                            if(nextnodeattr==1){
                                status = SystemEnv.getHtmlLabelName(21394,userlanguage);
                            }else if(nextnodeattr==2){
                                status = SystemEnv.getHtmlLabelName(21395,userlanguage);
                            }
                        }else{
                            status = SystemEnv.getHtmlLabelName(21395,userlanguage);
                        }
						
						//----------------------------------------------
	                    // 获取表单数据签名 start
	                    //----------------------------------------------
	                    String formsignaturemd5 = "";
	                    try {
	                        if ("save".equals(src)) {
	                            formsignaturemd5 = WFPathUtil.getFormValMD5(workflowid, requestid, isbill, formid, requestname, this.nodeid);
	                        }
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                    //----------------------------------------------
	                    // 获取表单数据签名 end
	                    //----------------------------------------------
						
						// 更新 requestbase 表信息
						if (status.equals(""))
						{
							sql = " update workflow_requestbase set " +
							" requestname = '" + Util.fromScreen2(requestname, userlanguage) + "' " ;
							if(!"true".equals(isFromEditDocument)){
								sql=sql+" ,lastoperator = " + userid ;
							}
							
							
						  if(!tempcurrentnodetype.equals("3")&&!"true".equals(isFromEditDocument)){//归档流程不更新lastoperatedate和lastoperatetime
						  sql = sql +
						  " ,lastoperatedate = '" + currentdate + "' " +
						  " ,lastoperatetime = '" + currenttime + "' " ;
					    }
					    sql = sql +
							" ,lastoperatortype = " + usertype ;
                            if(!((isremark==1||isremark==7)&&!CanModify)){
							sql+=" ,docids = '" + docids + "' " +
							" ,crmids = '" + crmids + "' ";
							if(isoracle){
								sql+=" ,hrmids = ? ";
							}else{
								sql+=" ,hrmids = '"+hrmids+"' ";
							}
							sql+=" ,prjids = '" + prjids + "' " +
							" ,cptids = '" + cptids + "' " ;
                            }
							//sql+=" where requestid = " + requestid;
						}
						else
						{
						sql = " update workflow_requestbase set " +
								" requestname = '" + Util.fromScreen2(requestname, userlanguage) + "' " +
								" ,status = '" + Util.fromScreen2(status, userlanguage) + "' " ;
								if(!"true".equals(isFromEditDocument)){
									sql=sql+" ,lastoperator = " + userid ;
								}
						    if(!tempcurrentnodetype.equals("3")&&!"true".equals(isFromEditDocument)){//归档流程不更新lastoperatedate和lastoperatetime
						    sql = sql +
						    " ,lastoperatedate = '" + currentdate + "' " +
						    " ,lastoperatetime = '" + currenttime + "' " ;
					      }
					      sql = sql +
								" ,lastoperatortype = " + usertype ;
                            if(!((isremark==1||isremark==7)&&!CanModify)){
							sql+=" ,docids = '" + docids + "' " +
							" ,crmids = '" + crmids + "' ";
							if(isoracle){
								sql+=" ,hrmids = ? ";
							}else{
								sql+=" ,hrmids = '"+hrmids+"' ";
							}
							sql+=" ,prjids = '" + prjids + "' " +
							" ,cptids = '" + cptids + "' " ;
                            }
							//sql+=" where requestid = " + requestid;
						}
						
						if (!"".equals(formsignaturemd5)) {
						    sql += ", formsignaturemd5='" + formsignaturemd5 + "' ";
						}
						
						sql+=" where requestid = " + requestid;
						//rs.executeSql(sql);
						if(isoracle&&!("0".equals(coadismodify)&&isremark==7)){
	                    	rst.executeUpdate(sql, new Object[]{hrmids});
	                    }else{
	                    	rst.executeSql(sql);
	                    }
						/******/
						/*
						if(isoracle){
							ConnStatement statement3 = null;
							try {
								String sql3 = "select hrmids from workflow_requestbase where requestid="+requestid+" for update";
								statement3 = new ConnStatement();
								statement3.setStatementSql(sql3, false);
								statement3.executeQuery();
				                if(statement3.next()){
					                CLOB theclob = statement3.getClob(1);
					                char[] contentchar = hrmids.toCharArray();
					                Writer contentwrite = theclob.getCharacterOutputStream();
					                contentwrite.write(contentchar);
					                contentwrite.flush();
					                contentwrite.close();
				                }
							} catch (Exception e) {
								rs.writeLog(e);
								e.printStackTrace();
							} finally {
								if(statement3!=null) statement3.close();
							}
						}*/
						/******/
					}
					
				}
				if (src.equals("delete")) {
				    /*
					//删除流程中所带附件 QC46085
					Monitor monitor=new Monitor();
					monitor.delWfAcc(""+requestid);
					
	                //add by mackjoe at 2005-10-20 增加流程删除时，删除单据中的相关数据
	                SysWFLMonitor wflm=new SysWFLMonitor();
	                wflm.WorkflowDel(requestid+"");
	                //end by mackjoe
	                // 2005-03-24 Guosheng for TD1725**************************************
					poppupRemindInfoUtil.deletePoppupRemindInfo(requestid,0);
					poppupRemindInfoUtil.deletePoppupRemindInfo(requestid,1);
                    poppupRemindInfoUtil.deletePoppupRemindInfo(requestid,10);
					sql = "delete from workflow_logviewusers where exists (select 1 from workflow_requestLog where workflow_requestLog.requestid = " + requestid + " and workflow_requestLog.logid = workflow_logviewusers.logid)";  //删除签字意见权限控制的表中的数据
                    rst.executeSql(sql);
					sql = "delete workflow_currentoperator where requestid =" + requestid  ;
					rst.executeSql(sql);
					sql = "delete from  workflow_form where requestid=" + requestid  ;
					rst.executeSql(sql);
					sql = "delete from  workflow_requestLog where requestid=" + requestid  ;
					rst.executeSql(sql);
					sql = "delete from  workflow_requestViewLog where id=" + requestid ;

                    rst.executeSql(sql);
					sql = "delete from  workflow_requestbase where requestid=" + requestid ;
					rst.executeSql(sql);
					//删除流程对应共享
					sql = "delete from  Workflow_SharedScope where requestid=" + requestid ;
					rst.executeSql(sql);

					
					//特殊处理(费用流程，在流程删除时，同时，已经配置生效、释放冻结预算)
					try {
		        		FnaCommon fnaCommon = new FnaCommon();
		        		fnaCommon.doWfForceOver(requestid, 0, true);
		            }catch(Exception easi) {
		            	this.writeLog(easi);
					}

                    sql="delete from workflow_groupdetail where EXISTS(select 1 from workflow_nodegroup where workflow_groupdetail.groupid=workflow_nodegroup.id and EXISTS(select 1 from workflow_nodebase b where workflow_nodegroup.nodeid=b.id and b.IsFreeNode='1' and b.requestid="+requestid+"))";
                    rs.executeSql(sql);
                    sql="delete from workflow_nodegroup where EXISTS(select 1 from workflow_nodebase b where workflow_nodegroup.nodeid=b.id and b.IsFreeNode='1' and b.requestid="+requestid+")";
                    rs.executeSql(sql);
                    sql="delete from workflow_nodemode where EXISTS(select 1 from workflow_nodebase b where workflow_nodemode.nodeid=b.id and b.IsFreeNode='1' and b.requestid="+requestid+")";
                    rs.executeSql(sql);
                    sql="delete from workflow_nodeform where EXISTS(select 1 from workflow_nodebase b where workflow_nodeform.nodeid=b.id and b.IsFreeNode='1' and b.requestid="+requestid+")";
                    rs.executeSql(sql);
                    sql="delete from workflow_flownode where EXISTS(select 1 from workflow_nodebase b where workflow_flownode.nodeid=b.id and b.IsFreeNode='1' and b.requestid="+requestid+")";
                    rs.executeSql(sql);
                    sql="delete from workflow_nodelink where EXISTS(select 1 from workflow_nodebase b where workflow_nodelink.nodeid=b.id and b.IsFreeNode='1' and b.requestid="+requestid+") or EXISTS(select 1 from workflow_nodebase b where workflow_nodelink.destnodeid=b.id and b.IsFreeNode='1' and b.requestid="+requestid+") or wfrequestid="+requestid;
					rst.executeSql(sql);
                    sql="delete from workflow_nodebase where IsFreeNode='1' and requestid="+requestid;
					rst.executeSql(sql);
					// 2005-03-24 Guosheng for TD1725**************************************
                    // 2014-9-3 add delete log
                      RequestDeleteLog log = new RequestDeleteLog();
                      log.setRequestId(String.valueOf(requestid));
                      log.setRequestName(Util.fromScreen2(requestname, userlanguage));
                      log.setOperateUserId(String.valueOf(userid));
                      log.setOperateDate(currentdate);
                      log.setOperateTime(currenttime);
                      log.setWorkflowId(String.valueOf(workflowid));
                      String clientip = "";
                        if (isRequest) {
                          if (request != null)
                            clientip = Util.null2String(request.getRemoteAddr());
                        } else {
                          if (fu != null)
                            clientip = Util.null2String(fu.getRemoteAddr());
                        }
                      log.setClientAddress(clientip);
                      log.save(rst);
                      
                      */
				    
				    RequestDeleteLog log = new RequestDeleteLog();
                    log.setRequestId(String.valueOf(requestid));
                    log.setRequestName(Util.fromScreen2(requestname, userlanguage));
                    log.setOperateUserId(String.valueOf(userid));
                    log.setOperateDate(currentdate);
                    log.setOperateTime(currenttime);
                    log.setWorkflowId(String.valueOf(workflowid));
                    String clientip = "";
                      if (isRequest) {
                        if (request != null)
                          clientip = Util.null2String(request.getRemoteAddr());
                      } else {
                        if (fu != null)
                          clientip = Util.null2String(fu.getRemoteAddr());
                      }
                    log.setClientAddress(clientip);
                    
                    
                    
                    //由于并非实际删除流程，这里流程相关附件就不删除了
                    RequestDeleteUtils rdu = new RequestDeleteUtils(requestid,rst,log);
                    rdu.executeDeleteRequest();
                    
                    //特殊处理(费用流程，在流程删除时，同时，已经配置生效、释放冻结预算)
                    try {
                        FnaCommon fnaCommon = new FnaCommon();
                        fnaCommon.doWfForceOver(requestid, 0, true);
                    }catch(Exception easi) {
                        this.writeLog(easi);
                    }
                    deleteToDo(String.valueOf(requestid));
				}
				if (src.equals("active")) {
					// 更新 requestbase 表信息, 将 deleted 设置为0
					sql = " update workflow_requestbase set " +
							" lastoperator = " + userid ;
						  if(!tempcurrentnodetype.equals("3")){//归档流程不更新lastoperatedate和lastoperatetime
						  sql = sql +
						  " ,lastoperatedate = '" + currentdate + "' " +
						  " ,lastoperatetime = '" + currenttime + "' " ;
					    }
					    sql = sql +
							" ,lastoperatortype = " + usertype +
							" ,deleted = 0 " +
							" where requestid = " + requestid;
					rst.executeSql(sql);
				}
                //流程督办处理
                if (src.equals("supervise")) {
					// 更新查看过的人为有回复未查看，显示为黄色new
                    if (!ifchangstatus.equals("")&&isfeedback.equals("1")&&((isnullnotfeedback.equals("1")&&!Util.replace(remark, "\\<script\\>initFlashVideo\\(\\)\\;\\<\\/script\\>", "", 0, false).equals(""))||!isnullnotfeedback.equals("1"))) {
					sql = " update workflow_currentoperator set viewtype = -1 where needwfback='1' and requestid = " + requestid+" and viewtype=-2 and userid <>"+userid;
                    rst.executeSql(sql);
                    }
                    // 更新 requestbase 表信息
					sql = " update workflow_requestbase set " +
							" lastoperator = " + userid ;
						  if(!tempcurrentnodetype.equals("3")){//归档流程不更新lastoperatedate和lastoperatetime
						  sql = sql +
						  " ,lastoperatedate = '" + currentdate + "' " +
						  " ,lastoperatetime = '" + currenttime + "' " ;
					    }
					    sql = sql +
							" ,lastoperatortype = " + usertype +
							" where requestid = " + requestid;
					rst.executeSql(sql);
                }
            }                                       // 请求的基本表信息和操作者信息结束
            
			rst.commit();
			//推送处理start
			WFPathUtil wfutil = new WFPathUtil();
			//有需要提醒的信息，才抛出处理
            if (poppupRemindInfoUtil.getPoppuplist() != null && !poppupRemindInfoUtil.getPoppuplist().isEmpty()) {
                wfutil.getFixedThreadPool().execute(poppupRemindInfoUtil);
            }
			wfutil.getFixedThreadPool().execute(new RequestPreProcessing(iscreate, workflowid, isbill, formid, requestid, requestname, oldformsignaturemd5, nodeid, nextnodeid, haspassthisnode, finishndgpids, user, false));
			//new Thread().start();
			//推送处理end
        }catch(Exception exception){
            exception.printStackTrace();
        	rst.rollback();
            return false;
        }
        if(src.equals("submit") || src.equals("reject") || src.equals("reopen") || src.equals("save") || src.equals("supervise") || src.equals("intervenor")){
            //处理退回提醒
            if(src.equals("reject")){
                String ischangrejectnode="";
                sql="select isrejectremind,ischangrejectnode from workflow_flownode where workflowid="+workflowid + " and nodeid=" + this.nodeid;
                rs.executeSql(sql);
                if(rs.next()){
                    isrejectremind=Util.null2String(rs.getString("isrejectremind"));
                    ischangrejectnode=Util.null2String(rs.getString("ischangrejectnode"));
                }
                if(isrejectremind.equals("1")){
                    if(ischangrejectnode.equals("1")){
                        if(isRequest){
                            if (request != null){
                                rejectremindnodes = Util.null2String(request.getParameter("RejectNodes"));
                            }
                        } else {
                            if (fu != null){
                                rejectremindnodes = Util.null2String(fu.getParameter("RejectNodes"));
                            }
                        }
                    }else{
                        rejectremindnodes="-1";
                    }
                    //System.out.println("rejectremindnodes:"+rejectremindnodes);
                    if(!rejectremindnodes.equals("")){
                        //获得提醒人
                        sql="select distinct userid,usertype from workflow_currentoperator where requestid="+requestid+" and not (userid="+userid+" and usertype="+usertype+") ";
                        if(!rejectremindnodes.equals("-1")){
                            sql+=" and nodeid in("+rejectremindnodes+")";
                        }
                        //System.out.println(sql);
                        rs.executeSql(sql);
                        while(rs.next()){
                           // poppupRemindInfoUtil.insertPoppupRemindInfo(Util.getIntValue(rs.getString("userid")),14,rs.getString("usertype"),requestid,requestname,workflowid);
                            Map map=new HashMap();
						    map.put("userid",""+Util.getIntValue(rs.getString("userid")));
						    map.put("type","14");
						    map.put("logintype",""+rs.getString("usertype"));
						    map.put("requestid",""+requestid);
						    map.put("requestname",""+requestname);
						    map.put("workflowid",""+workflowid);
						    map.put("creater",""+creater);
						    poppuplist.add(map);
                        }
                        poppupRemindInfoUtil.insertPoppupRemindInfo(poppuplist);
                    }
                }
            }
        	islogsuccess = saveRequestLog2();
            //由于下面会进行islasttimes的更新动作，等更新完成后再记录修改日志
//            if (rolm != null) {
//                //流程提交完成， 记录最新数据， 并与之前数据做对比， 记录修改日志
//                rolm.flowTransSubmitAfter();
//            }
			//下一节点自动批准 如何解决分叉流程
			if(nodeInfoCache.size() > 0){
			    WFAutoApproveThreadPoolUtil.getFixedThreadPool().execute(new WFAutoApproveUtils(this,nodeInfoCache.get(nodeInfoCache.keySet().iterator().next())));
            }
        }
		 //add by liaodong for qc76119,76120,54194,64652 start  
           //回写穿透时workflow_requestLog日志penetrateFlag lastNodeId 
	       if(!"".equals(penetrateFlag)){ //有穿透的功能时候
	    	   //查询requestId下面的最新的流程
	    	    if(penetrateFlag.indexOf(",")>=0){
	    	    	penetrateFlag = penetrateFlag.substring(penetrateFlag.indexOf(",")+1,penetrateFlag.length());
	    	    	updatePrenetrateLog(penetrateFlag, lastNodeId);
	    	    }else{
	    	    	updatePrenetrateLog(penetrateFlag, lastNodeId);
	    	    }
	       }
       //end

       //子流程归档设置，数据汇总主流程，插入明细相关信息，不考虑主流程明细是否只读qc:
       try{
		   //是否开启子流程全部归档才能提交,子流程归档时调用
		   if(WFSubDataAggregation.checkSubProcessSummary(this.requestid)){
			   String cmainRequestId = SubWorkflowTriggerService.getMainRequestId(this.requestid);
			   if (cmainRequestId != null && !(cmainRequestId.length()==0)) {
				   if (canflowtonextnode&&nextnodetype.equals("3")) {
					   WFSubDataAggregation.addMainRequestDetail(cmainRequestId,this.requestid+"",-1,user);
				   }
			   }
		   }
		   		   
		   WFSubDataAggregation.subWfFileReminMainWf(this.requestid,user,request,fu,this.isRequest);
		   
		   //主流程到达汇总节点时判断子流程是否已归档，已归档则进行汇总
		   List<String> subList = WFSubDataAggregation.getSubRequestIdByMain(this.requestid,workflowid,nextnodeid);
		   if(subList.size()>0){
			   for(int r=0;r<subList.size();r++){
				   WFSubDataAggregation.addMainRequestDetail(this.requestid+"",subList.get(r),nextnodeid,user);
			   }
		   }
		   
       }catch(Exception e){
    	   e.printStackTrace();
       }
	       
        /**
         * 触发子流程 为了防止死锁，移到事物结束后处理。子流程触发失败，不影响主流程流转
         * chujun
         * Start
         */
		try{
			ExecutorService fixedThreadPool = new WFPathUtil().getFixedThreadPool();
			for(int i=0; i<nodeidList_sub.size(); i++){
				int nodeid_tmp = Util.getIntValue((String)nodeidList_sub.get(i), 0);
				String triggerTime_tmp = Util.null2String((String)triggerTimeList_sub.get(i));
				String hasTriggeredSubwf_tmp = Util.null2String((String)hasTriggeredSubwfList_sub.get(i));
				SubWorkflowTriggerServiceThread triggerService = new SubWorkflowTriggerServiceThread(this, nodeid_tmp, hasTriggeredSubwf, user,triggerTime_tmp);
				// 线程池执行子流程
				fixedThreadPool.execute(triggerService);
			}
		}catch(Exception e){
			writeLog(e);
		}
        //触发子流程End

        /**
         * 流程触发日程 TD13304
         */
        try{
			String clientip = "";
			if(isRequest){
				if (request != null){
					clientip = Util.null2String(request.getRemoteAddr());
				}
			}else{
				if (fu != null){
					clientip = Util.null2String(fu.getRemoteAddr());
				}
			}
        	String sqlExt = "";
        	for(int i=0; i<nodeidList_wp.size(); i++){
        		String nodeid_tmp = Util.null2String((String)nodeidList_wp.get(i));
        		String createTime_tmp = Util.null2String((String)createTimeList_wp.get(i));
        		sqlExt += " (nodeid="+nodeid_tmp+" and changetime="+createTime_tmp+") or";
        	}
        	if(!"".equals(sqlExt) && src.equals("submit")){
        		CreateWorkplanByWorkflow createWorkplanByWorkflow = null;
        		sqlExt = sqlExt.substring(0, sqlExt.length()-2);
        		RecordSet rs_wp = new RecordSet();
        		rs_wp.execute("select * from workflow_createplan where wfid="+this.workflowid+" and ("+sqlExt+") order by id");
        		while(rs_wp.next()){
        			int createplanid = Util.getIntValue(rs_wp.getString("id"), 0);
        			int plantypeid_tmp = Util.getIntValue(rs_wp.getString("plantypeid"), 0);
        			int creatertype_tmp = Util.getIntValue(rs_wp.getString("creatertype"), 0);
        			int wffieldid_tmp = Util.getIntValue(rs_wp.getString("wffieldid"), 0);
        			createWorkplanByWorkflow = new CreateWorkplanByWorkflow();
        			createWorkplanByWorkflow.setCreateplanid(createplanid);
        			createWorkplanByWorkflow.setWorkplantypeid(plantypeid_tmp);
        			createWorkplanByWorkflow.setWp_creatertype(creatertype_tmp);
        			createWorkplanByWorkflow.setWf_fieldid(wffieldid_tmp);
        			createWorkplanByWorkflow.setWf_formid(this.formid);
        			createWorkplanByWorkflow.setWf_isbill(this.isbill);
        			createWorkplanByWorkflow.setWf_requestid(this.requestid);
        			createWorkplanByWorkflow.setWf_wfid(this.workflowid);
        			createWorkplanByWorkflow.setUser(this.user);
        			createWorkplanByWorkflow.setRemoteAddr(clientip);
        			createWorkplanByWorkflow.createWorkplan();
        		}
        	}
        }catch(Exception e){
        	writeLog(e);
        }
        
		// 处理共享信息
		//modify by xhheng @20050406 for TD 1758
		if ((src.equals("save") && isremark == 1) || src.equals("submit") || src.equals("reject") || src.equals("intervenor")) {
			if(istest != 1){
			try {
			    User sharedUser = user;
			    try {
    			    if (src.equals("intervenor")) {
    			        rs.execute("select userid from workflow_currentoperator where requestid=" + requestid + " and (isremark=0 or preisremark=0) order by id");
    	                if(rs.next()){
    	                    int sharedUserid = Util.getIntValue(rs.getString("userid"));
    	                    if (sharedUserid > 0) {
    	                        sharedUser = new User(sharedUserid);
    	                    }
    	                }
    	                haspassthisnode = true;
    			    }
			    } catch (Exception e1) {
			        e1.printStackTrace();
			    }
                RequestAddShareInfo shareinfo = new RequestAddShareInfo();
                for(int n=0;n<nextnodeids.size();n++){
                    nextnodeid=Util.getIntValue((String)nextnodeids.get(n),0);
                shareinfo.setRequestid(requestid);
                shareinfo.SetWorkFlowID(workflowid);
                shareinfo.SetNowNodeID(nodeid);
                if(nextnodeid==0)
                	shareinfo.SetNextNodeID(nodeid);
                else
                	shareinfo.SetNextNodeID(nextnodeid);
                shareinfo.setIsbill(isbill);
				shareinfo.setUser(sharedUser);
                shareinfo.SetIsWorkFlow(1);
                shareinfo.setBillTableName(billtablename);
                shareinfo.setHaspassnode(haspassthisnode);
                shareinfo.setRequestCheckAddinRulesList(requestCheckAddinRulesList);
                
                shareinfo.setReCalculatePermission(isReCalculatePermission);
				shareinfo.addShareInfo();
                }
            }catch(Exception easi) {
			}
			}//end if(istest != 1){

			//流程归档，删除Workflow_DocSource数据
            if (canflowtonextnode&&nextnodetype.equals("3")) {
				rs.execute("select docRightByOperator from workflow_base where id="+workflowid);
				if(rs.next()){
					if(Util.getIntValue(rs.getString("docRightByOperator"),0)==1){
						rs.execute("delete from Workflow_DocSource where requestid =" + requestid );
					}
				}
                //删除流程干预的逐个递交操作组
                rs.execute("delete from workflow_groupdetail where groupid=-2 and id in(select groupdetailid from workflow_currentoperator where groupid=-2 and requestid =" + requestid+")" );
                rs.execute("delete from workflow_agentpersons where requestid =" + requestid );
            }

            for(int i=0;i<submituserids.size();i++){
                int tempworkflowcurrid=0;
                int tempislasttimes=0;
                rs.executeSql("select id,islasttimes from workflow_currentoperator where (isremark = '5' or isremark='0' or isremark='1' or isremark='8' or isremark='9' or isremark='7') and requestid=" + requestid + " and userid=" + submituserids.get(i) + " and usertype=" + submitusertypes.get(i) + " order by id");
                while (rs.next()) {
                    int tislasttimes = rs.getInt("islasttimes");
                    if (tislasttimes != 1) {
                        tempworkflowcurrid = rs.getInt("id");
                    } else {
                        tempislasttimes = 1;
                        break;
                    }
                }
                if (tempislasttimes == 0 && tempworkflowcurrid > 0) {
                    rs.executeSql("update workflow_currentoperator set islasttimes=0 where requestid=" + requestid + " and userid=" + submituserids.get(i) + " and usertype=" + submitusertypes.get(i));
                    rs.executeSql("update workflow_currentoperator set islasttimes=1 where id=" + tempworkflowcurrid);
                }
            }
        }
			try{
			//把这个流程的所有操作者都找一遍，只要islasttimes有2个为1，就要清掉。不管客户的
			RecordSet rs001 = new RecordSet();
			RecordSet rs002 = new RecordSet();
			String sqltmp = "select userid, islasttimes, id, isremark from workflow_currentoperator where usertype=0 and requestid="+this.requestid+" order by userid, islasttimes,case isremark when '4' then '1.5' when '8' then '1.4' when '9' then '1.4' else isremark end desc, id asc";
			ArrayList userid2List = new ArrayList();
			ArrayList islasttimes2List = new ArrayList();
			ArrayList id2List = new ArrayList();
			ArrayList isRemark2List = new ArrayList();
			rs001.execute(sqltmp);
			while(rs001.next()){
				int userid_tmp = Util.getIntValue(rs001.getString("userid"), 0);
				int islasttimes_tmp = Util.getIntValue(rs001.getString("islasttimes"), 0);
				int id_tmp = Util.getIntValue(rs001.getString("id"), 0);
				userid2List.add(""+userid_tmp);
				islasttimes2List.add(""+islasttimes_tmp);
				id2List.add(""+id_tmp);
				isRemark2List.add(Util.null2String(rs001.getString("isremark")));
			}
			int userid_t = 0;
			int islasttimes_t = -1;
			int id_t = 0;
			int isremark_t = -1;
			for(int cx=0; cx<userid2List.size(); cx++){
				int userid_tmp = Util.getIntValue((String)userid2List.get(cx), 0);
				int islasttimes_tmp = Util.getIntValue((String)islasttimes2List.get(cx), 0);
				int id_tmp = Util.getIntValue((String)id2List.get(cx), 0);
				int isremark_tmp = Util.getIntValue((String)isRemark2List.get(cx));
				if(userid_tmp == userid_t){
					if(islasttimes_t==1 && islasttimes_tmp==1){//同用户，2个islasttimes
						sqltmp = "update workflow_currentoperator set islasttimes=0 where id="+id_t;
						rs002.execute(sqltmp);
					} else if (islasttimes_t == 0 && islasttimes_tmp==1 && isremark_t ==0 && (isremark_tmp == 8 || isremark_tmp == 9)) {
					    sqltmp = "update workflow_currentoperator set islasttimes=1 where id="+id_t;
                        rs002.execute(sqltmp);
                        sqltmp = "update workflow_currentoperator set islasttimes=0 where id="+id_tmp;
                        rs002.execute(sqltmp);
                    }
				}else{
					if(islasttimes_t==0){//已经是不同用户了，前面用户最后一个的islasttimes还是0
						sqltmp = "update workflow_currentoperator set islasttimes=1 where id="+id_t;
						rs002.execute(sqltmp);
					}

				}
				userid_t = userid_tmp;
				islasttimes_t = islasttimes_tmp;
				id_t = id_tmp;
				isremark_t = isremark_tmp;
			}
			if(islasttimes_t==0){//考虑最后一个情况，如果islasttimes是0
				sqltmp = "update workflow_currentoperator set islasttimes=1 where id="+id_t;
				rs002.execute(sqltmp);
			}

			if(wflinkinfo.getNodeAttribute(nodeid)==2){
				this.CheckUserIsLasttimes(requestid, nodeid, user);
			}
		}catch(Exception e){
			writeLog(e);
		}
        if(src.equals("submit") || src.equals("reject") || src.equals("reopen") || src.equals("save") || src.equals("supervise") || src.equals("intervenor")){

            if (rolm != null) {
                //流程提交完成， 记录最新数据， 并与之前数据做对比， 记录修改日志
                rolm.flowTransSubmitAfter();
               }
        }
		
		if(nextnodetype.equals("3")){//归档时回写流程信息外部数据TD10797
		    RequestManageWriteBackAction writebackaction = new RequestManageWriteBackAction();
		    writebackaction.doWriteBack(this.requestid);
		}
		//TD 24650 ——xyz 如果节点后附加操作为流程存为文档，那么等所有操作执行之后，来执行存为文档操作 
		if(isWorkFlowToDoc&&nodeid!=nextnodeid)
		{
			 Action action= (Action)StaticObj.getServiceByFullname("action.WorkflowToDoc", Action.class);
             RequestService requestService=new  RequestService();
             //String msg=action.execute(requestService.getRequest(requestid));
             String msg=action.execute(requestService.getRequest(this));                        
		}
		//主流程自动流转
		SubWorkflowTriggerService.judgeMainWfAutoFlowNextNode(this.requestid);
		
		try{
			//更新流程请求标题字段Start 防止节点附加操作修改相关值
			new SetNewRequestTitle().getAllRequestName(rs,requestid+"",requestname,workflowid+"",nodeid+"",formid,isbill,userlanguage);
			//更新流程请求标题字段End
		}catch(Exception e){
		}
		
		if(src.equals("intervenor")&&!nextnodetype.equals("3")){
			//归档节点流程干预到非归档节点 要修改该状态。
			rs.executeSql("update workflow_currentoperator set iscomplete=0 where requestid="+requestid);
			rs.executeSql("update workflow_currentoperator set isremark='2' where isremark='4' and requestid="+requestid);
			//子流程归档汇总标记撤销
            rs.executeSql(" update workflow_requestbase set dataaggregated = '' where requestid = " + requestid);
		}
        if(nextnodetype.equals("3")){
            rs.executeSql("update workflow_currentoperator set isremark='4' where isremark='2' and nodeId="+nextnodeid+" and requestid="+requestid);
        }

        if(nextnodetype.equals("0")){
        	//流程以任意状态流转到创建节点，则清除预算费用数据
			try {
        		FnaCommon fnaCommon = new FnaCommon();
        		fnaCommon.doWfForceOver(requestid, 0, true);
            }catch(Exception easi) {
            	new BaseBean().writeLog(easi);
			}
        }
        //当表单内容保存时，根据请求id插入督办权限表
        WFUrgerManager WFUrgerManager = new WFUrgerManager();
        WFUrgerManager.setLogintype(Util.getIntValue(user.getLogintype()));
        WFUrgerManager.setUserid(user.getUID());
        WFUrgerManager.insertUrgerByRequestid(requestid);
        
        //统一待办
        PostWorkflowInf postworkflowinf = new PostWorkflowInf();
        postworkflowinf.operateToDo(String.valueOf(this.requestid));
        
		//处理工作流外部接口的代码
		return doOutWork(this.workflowid, this.requestid);        
		
		
		//清除GCONST.WFProcessing中的值，保证当前节点的其他操作人能够正常提交
        } finally {
            GCONST.WFProcessing.remove(requestid+"_"+nodeid);
        }
   }

   /**
	 * 用于穿透的时候回写日志记录表信息
	 * @param penetrateFlag
	 *           
	 * @param lastNodeId
	 */
	private void updatePrenetrateLog(String penetrateFlag, String lastNodeId) {
		String maxRequestLogSql = " select MAX(LOGID) logid from workflow_requestLog where requestid = "+requestid+"";
		 rs.executeSql(maxRequestLogSql);
		 if(rs.next()){
			 String maxRequestLogId = rs.getString("logid");
			 String requestLogSql=" select logtype,operatedate,operatetime,remark,clientip,receivedPersons,showorder,agentorbyagentid," +
			 		   "agenttype,LOGID,annexdocids,requestLogId,operatorDept," +
			 		    "signdocids,signworkflowids,isMobile,HandWrittenSign,SpeechAttachment,remarkLocation " +
			 		    "from workflow_requestLog where LOGID = '"+maxRequestLogId+"'";
			 rs.executeSql(requestLogSql);
			 if(rs.next()){
				 String penetrateremark = Util.null2String(rs.getString("remark"));
				 if(penetrateFlag.indexOf(",")>=0){
					 String updatePenSql = "update workflow_penetrateLog set logtype = '"+rs.getString("logtype")+"'," +
							 "operatedate = '"+rs.getString("operatedate")+"',operatetime = '"+rs.getString("operatetime")+"',remark=''," +
							 "clientip='"+rs.getString("clientip")+"',receivedPersons='',showorder='"+rs.getString("showorder")+"'," +
							 "agentorbyagentid='"+rs.getString("agentorbyagentid")+"',agenttype='"+rs.getString("agenttype")+"',LOGID='"+rs.getString("LOGID")+"'," +
							 "annexdocids='"+rs.getString("annexdocids")+"',requestLogId='"+rs.getString("requestLogId")+"',operatorDept='"+rs.getString("operatorDept")+"'," +
							 "signdocids='"+rs.getString("signdocids")+"',signworkflowids='"+rs.getString("signworkflowids")+"',isMobile='"+rs.getString("isMobile")+"',HandWrittenSign='"+rs.getString("HandWrittenSign")+"'," +
							 "SpeechAttachment = '"+rs.getString("SpeechAttachment")+"',"+ "remarkLocation = '"+rs.getString("remarkLocation") +"'  where id in ("+penetrateFlag+")";
					 rs.executeSql(updatePenSql);
					 ////////////////
					 try {
							String fdsql = " update workflow_penetrateLog set receivedPersons = ?,remark = ? where id in ("+penetrateFlag+")";
							ConnStatement fdst = null;
							try {
								fdst = new ConnStatement();
								fdst.setStatementSql(fdsql);
								fdst.setString(1, rs.getString("receivedPersons"));
								fdst.setString(2, penetrateremark);
								fdst.executeUpdate();
							} catch (Exception e) {
								writeLog(e);
							} finally {
								if(fdst!=null) fdst.close();
							}
							
						}catch(Exception e){
							writeLog(e);
						}
					 ////////////////
				 }else{
					 String updatePenSql = "update workflow_penetrateLog set logtype = '"+rs.getString("logtype")+"'," +
				 		"operatedate = '"+rs.getString("operatedate")+"',operatetime = '"+rs.getString("operatetime")+"',remark=''," +
				 		"clientip='"+rs.getString("clientip")+"',receivedPersons='',showorder='"+rs.getString("showorder")+"'," +
				 		"agentorbyagentid='"+rs.getString("agentorbyagentid")+"',agenttype='"+rs.getString("agenttype")+"',LOGID='"+rs.getString("LOGID")+"'," +
				 		 "annexdocids='"+rs.getString("annexdocids")+"',requestLogId='"+rs.getString("requestLogId")+"',operatorDept='"+rs.getString("operatorDept")+"'," +
				 		 "signdocids='"+rs.getString("signdocids")+"',signworkflowids='"+rs.getString("signworkflowids")+"',isMobile='"+rs.getString("isMobile")+"',HandWrittenSign='"+rs.getString("HandWrittenSign")+"'," +
				 		 "SpeechAttachment = '"+rs.getString("SpeechAttachment")+"',"+ "remarkLocation = '"+rs.getString("remarkLocation") + "'  where id= '"+penetrateFlag+"'";
				     rs.executeSql(updatePenSql);
				     ////////////////
					 try {
						String fdsql = " update workflow_penetrateLog set receivedPersons = ?,remark = ? where id = '"+penetrateFlag+"'";
						ConnStatement fdst = null;
						try {
							fdst = new ConnStatement();
							fdst.setStatementSql(fdsql);
							fdst.setString(1, rs.getString("receivedPersons"));
							fdst.setString(2, penetrateremark);
							fdst.executeUpdate();
						} catch (Exception e) {
							writeLog(e);
						} finally {
							if(fdst!=null) fdst.close();
						}
					  }catch(Exception e){
							writeLog(e);
					  }
					  ////////////////
				 }
			 }
			 if(!"".equals(lastNodeId)){
				  String updateRequestSql ="select requestid,workflowid,nodeid,logtype," +
				 		                  "operatedate,operatetime,operator,remark,clientip," +
				 		                  "operatortype,destnodeid,receivedPersons,showorder," +
				 		                  "agentorbyagentid,agenttype,LOGID,annexdocids,requestLogId," +
				 		                  "operatorDept,signdocids,signworkflowids,isMobile,HandWrittenSign," +
				 		                  "SpeechAttachment,remarkLocation from  workflow_requestLog where LOGID = '"+maxRequestLogId+"'";
				 rs.executeSql(updateRequestSql); 
				 if(rs.next()){
					 try{
						//取数据库服务器的当前时间 start
							String penetrateSql = "insert into workflow_penetrateLog(requestid,workflowid,nodeid,logtype,operatedate," +
									"operatetime,operator,remark,clientip,operatortype,destnodeid,receivedPersons,showorder," +
									"agentorbyagentid,agenttype,LOGID,annexdocids,requestLogId,operatorDept," +
									"signdocids,signworkflowids,isMobile,HandWrittenSign,SpeechAttachment,remarkLocation) values("+
									"'"+rs.getString("requestid")+"','"+rs.getString("workflowid")+"','"+lastNodeId+"','z'," +
									"'"+rs.getString("operatedate")+"','"+rs.getString("operatetime")+"','"+rs.getString("operator")+"',''," +
									"'"+rs.getString("clientip")+"','"+rs.getString("operatortype")+"','"+rs.getString("destnodeid")+"','"+rs.getString("receivedPersons")+"'," +
									"'"+rs.getString("showorder")+"','"+rs.getString("agentorbyagentid")+"','"+rs.getString("agenttype")+"','"+rs.getString("LOGID")+"'," +
									"'"+rs.getString("annexdocids")+"','"+rs.getString("requestLogId")+"','"+rs.getString("operatorDept")+"','"+rs.getString("signdocids")+"'," +
									"'"+rs.getString("signworkflowids")+"','"+rs.getString("isMobile")+"','"+rs.getString("HandWrittenSign")+"','"+rs.getString("SpeechAttachment")+"','"+rs.getString("remarkLocation")+"')";
							if("oracle".equals(rs.getDBType())){
							    penetrateSql = "insert into workflow_penetrateLog(id,requestid,workflowid,nodeid,logtype,operatedate," +
								"operatetime,operator,remark,clientip,operatortype,destnodeid,receivedPersons,showorder," +
								"agentorbyagentid,agenttype,LOGID,annexdocids,requestLogId,operatorDept," +
								"signdocids,signworkflowids,isMobile,HandWrittenSign,SpeechAttachment,remarkLocation) values("+
								"workflowpenetratelog_Id.nextval,'"+rs.getString("requestid")+"','"+rs.getString("workflowid")+"','"+lastNodeId+"','z'," +
								"'"+rs.getString("operatedate")+"','"+rs.getString("operatetime")+"','"+rs.getString("operator")+"',''," +
								"'"+rs.getString("clientip")+"','"+rs.getString("operatortype")+"','"+rs.getString("destnodeid")+"',''," +
								"'"+rs.getString("showorder")+"','"+rs.getString("agentorbyagentid")+"','"+rs.getString("agenttype")+"','"+rs.getString("LOGID")+"'," +
								"'"+rs.getString("annexdocids")+"','"+rs.getString("requestLogId")+"','"+rs.getString("operatorDept")+"','"+rs.getString("signdocids")+"'," +
								"'"+rs.getString("signworkflowids")+"','"+rs.getString("isMobile")+"','"+rs.getString("HandWrittenSign")+"','"+rs.getString("SpeechAttachment")+"','"+rs.getString("remarkLocation")+"')";
							}
							String penetrateremark = Util.null2String(rs.getString("remark"));
							rs.executeSql(penetrateSql);
							////////////////
							if("oracle".equals(rs.getDBType())){ 
								try {
									String sqllog = "select max(id) rid from workflow_penetrateLog where requestid = "+rs.getString("requestid")+" and workflowid = " + rs.getString("workflowid") ;
									rs1.executeSql(sqllog); 
									if(rs1.next()){
										String receivedPersonids = Util.null2String(rs1.getString("rid"));
										if(!"".equals(receivedPersonids)){
											String fdsql = " update workflow_penetrateLog set receivedPersons = ?,remark = ? where id = '"+receivedPersonids+"'";
											ConnStatement fdst = null;
											try {
												fdst = new ConnStatement();
												fdst.setStatementSql(fdsql);
												fdst.setString(1, rs.getString("receivedPersons"));
												fdst.setString(2, penetrateremark);
												fdst.executeUpdate();
											} catch (Exception e) {
												writeLog(e);
											} finally {
												if(fdst!=null) fdst.close();
											}
										}
									}
								}catch(Exception e){
									writeLog(e);
								}
							}else{
								try {
									String sqllog = "select max(id) rid from workflow_penetrateLog where requestid = "+rs.getString("requestid")+" and workflowid = " + rs.getString("workflowid") ;
									rs1.executeSql(sqllog); 
									if(rs1.next()){
										String receivedPersonids = Util.null2String(rs1.getString("rid"));
										if(!"".equals(receivedPersonids)){
											String fdsql = " update workflow_penetrateLog set remark = ? where id = '"+receivedPersonids+"'";
											ConnStatement fdst = null;
											try {
												fdst = new ConnStatement();
												fdst.setStatementSql(fdsql);
												fdst.setString(1, penetrateremark);
												fdst.executeUpdate();
											} catch (Exception e) {
												writeLog(e);
											} finally {
												if(fdst!=null) fdst.close();
											}
										}
									}
								}catch(Exception e){
									writeLog(e);
								}
							}
							////////////////
					 }catch(Exception e){
						 
					 }
					 
				 }
			 }
		 }
	}




	/**
	 * 读取配置信息，由workflowid得到对应的处理类的名称，然后执行其处理方法.<br>
	 * @param workflowid 工作流id
	 * @param requestid 工组流一个请求地id
	 * @return 读取是否成功
	 */
	private boolean doOutWork(int workflowid, int requestid) {
		//得到配置文件的全路径名称
		String outWorkFileName = "requestmapping";
		String className = "";
		RequestOutWork work = null;
		try {
			String hasOutWork = getPropValue(GCONST.getConfigFile(), "hasOutWork");
			if (hasOutWork != null && hasOutWork.equals("true")) {
				className = getPropValue(outWorkFileName, "" + workflowid);
				if (className != null) {
					Class cl = Class.forName(className);
					//实例化处理类
					work = (RequestOutWork) cl.newInstance();
					HashMap dataInfo = new HashMap();
					dataInfo.put("workflowid",this.workflowid+"");
					dataInfo.put("userid",this.userid+"");
					dataInfo.put("usertype",this.usertype+"");
					dataInfo.put("src",this.src);
					dataInfo.put("iscreate",this.iscreate);
					dataInfo.put("requestlevel",this.requestlevel);
					dataInfo.put("requestmark",this.requestmark);
					dataInfo.put("nextnodetype",this.getNextNodetype());
					return work.execute(requestid,dataInfo);
				}

			}

		} catch (ClassNotFoundException e) {
			this.writeLog("get class instance error!");
		} catch (InstantiationException e) {
			this.writeLog("get class instance error!");
		} catch (IllegalAccessException e) {
			this.writeLog("get class instance error!");
		} catch (RequestOutWorkException e) {
			this.writeLog(e.getMessage());
		}
		return true;
	}

  /**
   * 外部jsp文件调用，由于必须再判断之前就保存日志，所以这个函数屏蔽掉，仅仅返回之前saveRequestLog2() 的返回结果
   */
  public boolean saveRequestLog() {
    return islogsuccess;
  }

  /**
   * 代替saveRequestLog()保存日志信息
   * 
   * @return 保存是否成功
   */
  public boolean saveRequestLog2() {
    if (src.equals("save")) {
      if (isremark == 1 || isremark == 9 ||(isremark==7 && "submit".equals(tempsrc)))
        saveRequestLog("9"); // 批注日志类型为9
      else
        saveRequestLog("1"); // 保存日志类型为1
    } else if (src.equals("submit")) {
      FlowExceptionHandle flowExceptionHandle = new FlowExceptionHandle();
      ArrayList _alluserids = new ArrayList();
      for (int n = 0; n < nextnodeids.size(); n++) {
        nextnodeid = Util.getIntValue((String) nextnodeids.get(n), 0);
        nextnodeattr = Util.getIntValue((String) nextnodeattrs.get(n), 0);
        try{
	        Hashtable _operatorsht = (Hashtable)operatorshts.get(n);
	        Enumeration tempKeys = operatorsht.keys();
			while (tempKeys.hasMoreElements()) {
				String tempKey = (String) tempKeys.nextElement();
				ArrayList tempoperators = (ArrayList) operatorsht.get(tempKey);
				_alluserids.addAll(tempoperators);
			}
        }catch(Exception e){
        	
        }
        
        if (isremark == 7 && coadsigntype.equals("2")) {
          saveRequestLog("9");
        } else {
          if (nodetype.equals("1")) {
            saveRequestLog("0");
          } else{
            saveRequestLog("2");
          }
        }
        //异常处理信息存workflow_requestexceptionflow表
        int requestexceptiontype = 0;
        if(requestexceptiontypes.size() > n)
        	requestexceptiontype = Util.getIntValue((String)requestexceptiontypes.get(n));
        flowExceptionHandle.saveRequestExceptionFlowInfo(requestid, nodeid, nextnodeid, requestexceptiontype, eh_operatorMap);
      }
    } else if (src.equals("reject")){
      //saveRequestLog("3");
    	ArrayList _alluserids = new ArrayList();
	    for (int n = 0; n < nextnodeids.size(); n++) {
	      nextnodeid = Util.getIntValue((String) nextnodeids.get(n), 0);
	      nextnodeattr = Util.getIntValue((String) nextnodeattrs.get(n), 0);
	      try{
		        Hashtable _operatorsht = (Hashtable)operatorshts.get(n);
		        Enumeration tempKeys = operatorsht.keys();
				while (tempKeys.hasMoreElements()) {
					String tempKey = (String) tempKeys.nextElement();
					ArrayList tempoperators = (ArrayList) operatorsht.get(tempKey);
					_alluserids.addAll(tempoperators);
				}
	      }catch(Exception e){
	      	
	      }
	      saveRequestLog("3");
	    }
  }else if (src.equals("reopen"))
      saveRequestLog("4");
    else if (src.equals("delete"))
      saveRequestLog("5");
    else if (src.equals("active"))
      saveRequestLog("6");
    else if (src.equals("supervise")) {
      ArrayList nownodelist = Util.TokenizerString(wflinkinfo.getNowNodeids(requestid), ",");
      for (int n = 0; n < nownodelist.size(); n++) {
        int nownodeid = Util.getIntValue((String) nownodelist.get(n), 0);
        if (nownodeid > 0)
          saveRequestLog("s", nownodeid, nownodeid);
      }
    } else if (src.equals("intervenor")) {
      saveRequestLog("i");
    }

    return true;
  }
  
  /**
   * 流程新建且签字意见中包含相关文档时，需要调用此方法进行处理其中的链接
   * 将-1替换为当前请求的id
   * @param remarkstr
   * @return
   */
  private String remarkBeforeSaveHandle(String remarkstr) {
      String result = "";
      StringBuffer rksb = new StringBuffer();
      Pattern ptrn = Pattern.compile("requestid=\\{#\\[currentRequestid\\]#\\}");
      result = ptrn.matcher(remarkstr).replaceAll("requestid=" + requestid);

      return result;
  }
  
  private void saveRequestLog(String logtype) {
    // 图形化表单类型的流程创建文档，点击“正文”，并且签字意见为空的时候不记录签字意见。
    if ("true".equals(isFromEditDocument) && "".equals(this.remark) && "".equals(signdocids) && "".equals(signworkflowids)) {
      return;
    }
    //创建节点处理链接中的requestid
    if ("1".equals(iscreate)) {
        this.remark = remarkBeforeSaveHandle(this.remark);
    }
    // added by mackjoe at 20080415 TD8580 增加流程签字上传附件功能
    String ismode = "";
    rs.executeSql("select ismode from workflow_flownode where workflowid=" + workflowid + " and nodeid=" + nodeid);
    if (rs.next()) {
      ismode = Util.null2String(rs.getString("ismode"));
    }
    
    int currentid = 0 ;
    rs.executeSql("select id from workflow_currentoperator where requestid="+requestid+" and userid="+userid+" and usertype="+usertype+" and islasttimes=1 ");
    if(rs.next()){
    	currentid = Util.getIntValue(rs.getString("id"),0);
    }
    String annexdocids = "";
    //如果从手机版提交流程，则从 signatureAppendfix 字段获取相当附件值。
    if(WorkflowSpeechAppend.isFromMobile(clientType)){
    	annexdocids = signatureAppendfix;
    }else{
    	if (!ismode.equals("1") || src.equals("supervise") || src.equals("intervenor")) {
    		RequestAnnexUpload rau = new RequestAnnexUpload();
    		rau.setRequest(fu);
    		rau.setUser(user);
    		annexdocids = rau.AnnexUpload();
    	} else {
    		String hasSign = "0";// 模板中是否设置了签字
    		rs.executeSql("select * from workflow_modeview where formid=" + formid + " and nodeid=" + nodeid + " and fieldid=-4");
    		if (rs.next())
    			hasSign = "1";
    		if ("1".equals(hasSign)) {// 模板中设置了签字
    			if (isRequest) {
    				if (request != null)
    					annexdocids = Util.null2String(request.getParameter("qianzi"));
    			} else {
    				if (fu != null)
    					annexdocids = Util.null2String(fu.getParameter("qianzi"));
    			}
    		} else {// 模板中没有设置签字，按普通方式上传签字意见的附件
    			RequestAnnexUpload rau = new RequestAnnexUpload();
    			rau.setRequest(fu);
    			rau.setUser(user);
    			annexdocids = rau.AnnexUpload();
    		}
    	}
    }
    
    
    // 获取签字意见相关文档，相关流程
    if (isRequest) {
      if (request != null) {
        signdocids = Util.null2String(request.getParameter("signdocids"));
        signworkflowids = Util.null2String(request.getParameter("signworkflowids"));
      }
    } else {
      if (fu != null) {
        signdocids = Util.null2String(fu.getParameter("signdocids"));
        signworkflowids = Util.null2String(fu.getParameter("signworkflowids"));
      }
    }

    String clientip = "";
    if (isRequest) {
      if (request != null)
        clientip = Util.null2String(request.getRemoteAddr());
    } else {
      if (fu != null)
        clientip = Util.null2String(fu.getRemoteAddr());
    }

    int requestLogId = 0;
    if (isRequest) {
      if (request != null)
        requestLogId = Util.getIntValue(request.getParameter("workflowRequestLogId"), 0);
    } else {
      if (fu != null)
        requestLogId = Util.getIntValue(fu.getParameter("workflowRequestLogId"), 0);
    }

    if (nextnodeid == 0)
      nextnodeid = nodeid;
    String personStr = "";
    String receivedPersonids = "";
    String personStr1 = "";
    String receivedPersonids1 = "";
    String isremarkStr_t = "";// 如果是提交、批准或者退回，为了防止当前操作人A，现在虽然是代理人，但是下一节点就是正常操作人，那么如果不限制isremark，找出来的就是非代理情况
    if ("0".equals(logtype) || "2".equals(logtype) || "3".equals(logtype)) {
      isremarkStr_t = " and isremark='2' ";
    }
    /* ---------------- xwj for td2104 on 20050802 B E G I N ------------------ */
    if (isOldOrNewFlag(requestid)) {// 老数据, 相对 td2104 之前
		rs.executeSql("select userid,usertype from workflow_currentoperator where isremark = 0 and requestid = " + requestid);
		while (rs.next()) {
			if ("0".equals(rs.getString("usertype"))) {
				personStr += Util.toScreen(resourceComInfo.getResourcename(rs.getString("userid")), user.getLanguage()) + ",";
				receivedPersonids += Util.null2String(rs.getString("userid")) +",";
			} else {
				personStr += Util.toScreen(customerInfoComInfo.getCustomerInfoname(rs.getString("userid")), user.getLanguage()) + ",";
				receivedPersonids += Util.null2String(rs.getString("userid")) +",";
			}
		}
		//System.out.println(" personStr = "+personStr);
		Procpara = "" + requestid + flag + workflowid + flag + nodeid + flag + logtype + flag + logdate + flag + logtime + flag + userid + flag + clientip + flag + usertype + flag + nextnodeid + flag + personStr.trim() + flag + -1 + flag + "0" + flag + -1 + flag + annexdocids + flag + requestLogId + flag + signdocids + flag + signworkflowids + flag + clientType + flag + speechAttachment + flag + handWrittenSign + flag + receivedPersonids + flag + remarkLocation;
		//Procpara = "" + requestid + flag + workflowid + flag + nodeid + flag + logtype + flag + logdate + flag + logtime + flag + userid + flag + remark + flag + clientip + flag + usertype + flag + nextnodeid + flag + personStr.trim() + flag + -1 + flag + "0" + flag + -1 + flag + annexdocids + flag + requestLogId + flag + signdocids + flag + signworkflowids + flag + clientType + flag + speechAttachment + flag + handWrittenSign + flag + receivedPersonids;
		if (logdate.equals("")) {
			String currentString = execRequestlog(Procpara,rs,flag,remark);
			if(!"".equals(currentString) && currentString.indexOf("~~current~~") > -1){
				String [] arraycurrent = Util.TokenizerString2(currentString, "~~current~~");
				logdate = arraycurrent[0];
				logtime = arraycurrent[1];
			}
			//rs.executeProc("workflow_RequestLog_Insert_New", Procpara);
			//if (rs.next()) {
			//	logdate = Util.null2String(rs.getString(1));
			//	logtime = Util.null2String(rs.getString(2));
			//}
		}else{
			//rs.executeProc("workflow_RequestLogCurDate_New", Procpara);
			execRequestlog(Procpara,rs,flag,remark);
		}
	} else {
		String tempSQL = "";
		int agentorbyagentid = -1;
		int agenttype = 0;
		int showorder = 1;
	
		ResourceComInfo resourceComInfo = null;
		try {
			resourceComInfo = new ResourceComInfo();
		} catch (Exception ex) {
	        // writeLog(ex.toString());
		}
		String tempisremark = "isremark in ('0','4','f')";  //f 自动提交 或 自动批准
		//如果当前节点存在协办人并且协办人关系为会签或者非会签 主办人关系为会签或依次处理 则接收人应包含协办人
		rs.executeSql("select signorder,signtype from workflow_groupdetail where type=42 and groupid in (select id from workflow_nodegroup where nodeid="+nodeid+")");
		if(rs.next()){
			int signorder = rs.getInt("signorder");
			int signtype = rs.getInt("signtype");
			if(signtype!=2 && (signorder==0 || signorder==1 || signorder==2 ||(signorder==0 && src.equals("reject")))){
				tempisremark = "isremark in ('0','4','7','f')";
			}
		}
		rs.executeSql("select userid,usertype,agentorbyagentid, agenttype,isremark,nodeid from workflow_currentoperator where " + tempisremark + " and requestid = " + requestid + " and nodeid in(" + wflinkinfo.getBrancheNode(nextnodeid, workflowid, "" + nextnodeid, requestid) + ") order by showorder asc");
		String useridStr = "";
		while (rs.next()) {
			//当前节点为自动批准、提交，并且当前人和下一节点操作人相同，表示查询的是当前节点操作状态记录（解决分叉中间点问题）
			if("submit".equals(src) &&"f".equals(rs.getString("isremark")) && nodeid == rs.getInt("nodeid") && user.getUID() == rs.getInt("userid")){
				continue;
			}
			if ("0".equals(rs.getString("usertype"))) {
				if (rs.getInt("agenttype") == 0) {
					String tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(rs.getString("userid")), user.getLanguage());
					String tempUseridStr = Util.null2String(rs.getString("userid"));
					if ((personStr.indexOf("," + tempPersonStr + ",") == -1 && personStr.indexOf(tempPersonStr + ",") == -1) || (useridStr.indexOf("," + tempUseridStr + ",") == -1 && useridStr.indexOf(tempUseridStr + ",") == -1)) {
						personStr += tempPersonStr + ",";
						useridStr += tempUseridStr + ",";
						receivedPersonids += tempUseridStr + ",";
					}
				} else if (rs.getInt("agenttype") == 2) {
					String tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(rs.getString("agentorbyagentid")), user.getLanguage()) + "->" + Util.toScreen(resourceComInfo.getResourcename(rs.getString("userid")), user.getLanguage());
					String tempUseridStr = Util.null2String(rs.getString("agentorbyagentid"));
					if ((personStr.indexOf("," + tempPersonStr + ",") == -1 && personStr.indexOf(tempPersonStr + ",") == -1) || (useridStr.indexOf("," + tempUseridStr + ",") == -1 && useridStr.indexOf(tempUseridStr + ",") == -1)) {
						personStr += tempPersonStr + ",";
						useridStr += tempUseridStr + ",";
						receivedPersonids += tempUseridStr + ",";
					}
				} else {
				}
			} else {
				String tempPersonStr = Util.toScreen(customerInfoComInfo.getCustomerInfoname(rs.getString("userid")), user.getLanguage());
				String tempUseridStr = Util.null2String(rs.getString("userid"));
				if ((personStr.indexOf("," + tempPersonStr + ",") == -1 && personStr.indexOf(tempPersonStr + ",") == -1) || (useridStr.indexOf("," + tempUseridStr + ",") == -1 && useridStr.indexOf(tempUseridStr + ",") == -1)) {
					personStr += tempPersonStr + ",";
					useridStr += tempUseridStr + ",";
					receivedPersonids += tempUseridStr + ",";
				}
			}
		}
	
		if ((logtype.equals("0") || logtype.equals("2")) && showcoadjutant) {// 提交到下一节点时，下一节点的抄送另外保存
	        // 抄送协办人
			sql = "select userid,usertype,agentorbyagentid, agenttype from workflow_currentoperator where isremark ='7' and nodeid in(" + wflinkinfo.getBrancheNode(nextnodeid, workflowid, "" + nextnodeid, requestid) + ") and requestid = " + requestid;
			sql += " and id>=" + currentopratorInsFirstid;
	        sql += " order by id desc";
	        rs.executeSql(sql);
	
	        while (rs.next()) {
	        	if ("0".equals(rs.getString("usertype"))) {
	        		if (rs.getInt("agenttype") == 0) {
	        			String tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(rs.getString("userid")), user.getLanguage());
	        			if (personStr1.indexOf("," + tempPersonStr + ",") == -1 && personStr1.indexOf(tempPersonStr + ",") == -1) {
	        				personStr1 += tempPersonStr + ",";
	        				receivedPersonids1 += Util.null2String(rs.getString("userid")) + ",";
	        			}
	        		} else if (rs.getInt("agenttype") == 2) {
	        			String tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(rs.getString("agentorbyagentid")), user.getLanguage()) + "->" + Util.toScreen(resourceComInfo.getResourcename(rs.getString("userid")), user.getLanguage());
	        			if (personStr1.indexOf("," + tempPersonStr + ",") == -1 && personStr1.indexOf(tempPersonStr + ",") == -1) {
	        				personStr1 += tempPersonStr + ",";
	        				receivedPersonids1 += Util.null2String(rs.getString("userid")) + ",";
	        			}
	        		} else {
	        		}
	        	} else {
	        		String tempPersonStr = Util.toScreen(customerInfoComInfo.getCustomerInfoname(rs.getString("userid")), user.getLanguage());
	        		if (personStr1.indexOf("," + tempPersonStr + ",") == -1 && personStr1.indexOf(tempPersonStr + ",") == -1) {
	        			personStr1 += tempPersonStr + ",";
	        			receivedPersonids1 += Util.null2String(rs.getString("userid")) + ",";
	        		}
	        	}
	        }
	        // 抄送
	        // TD23439，根据之前operator89List的值来分析是否有被炒送人
	        if (operator89mp != null && !operator89mp.isEmpty()) {
	        	operator89List = (ArrayList)operator89mp.get(String.valueOf(nextnodeid));
	        	operatortype89List = (ArrayList)operatortype89mp.get(String.valueOf(nextnodeid));
	        	agentoperator89List = (ArrayList)agentoperator89mp.get(String.valueOf(nextnodeid));
	        	
	        	if (operator89List == null) operator89List = new ArrayList();
	        	if (operatortype89List == null) operatortype89List = new ArrayList();
	        	if (agentoperator89List == null) agentoperator89List = new ArrayList();
	        } 
	        
	        for (int cc = 0; cc < operator89List.size(); cc++) {
	        	int operatortype_tmp = Util.getIntValue((String) operatortype89List.get(cc), 0);
	        	int operator_tmp = Util.getIntValue((String) operator89List.get(cc), 0);
	        	int agentoperator_tmp = Util.getIntValue((String) agentoperator89List.get(cc), 0);
	        	if (operatortype_tmp == 0) {
	        		if (agentoperator_tmp == 0) {
	        			String tempPersonStr = Util.toScreen(resourceComInfo.getResourcename("" + operator_tmp), user.getLanguage());
	        			if (personStr1.indexOf("," + tempPersonStr + ",") == -1 && personStr1.indexOf(tempPersonStr + ",") == -1) {
	        				personStr1 += tempPersonStr + ",";
	        				receivedPersonids1 += Util.null2String(operator89List.get(cc)) + ",";
	        			}
	        		} else if (agentoperator_tmp > 0) {
	        			String tempPersonStr = Util.toScreen(resourceComInfo.getResourcename("" + operator_tmp), user.getLanguage()) + "->" + Util.toScreen(resourceComInfo.getResourcename("" + agentoperator_tmp), user.getLanguage());
	        			if (personStr1.indexOf("," + tempPersonStr + ",") == -1 && personStr1.indexOf(tempPersonStr + ",") == -1) {
	        				personStr1 += tempPersonStr + ",";
	        				receivedPersonids1 += Util.null2String(operator89List.get(cc)) + ",";
	        			}
	        		} else {
	        		}
	        	} else {
	        		String tempPersonStr = Util.toScreen(customerInfoComInfo.getCustomerInfoname("" + operator_tmp), user.getLanguage());
	        		if (personStr1.indexOf("," + tempPersonStr + ",") == -1 && personStr1.indexOf(tempPersonStr + ",") == -1) {
	        			personStr1 += tempPersonStr + ",";
	        			receivedPersonids1 += Util.null2String(operator89List.get(cc)) + ",";
	        		}
	        	}
	        }
		}
		if (!canflowtonextnode && (nextnodeattr == 3 || nextnodeattr == 4|| nextnodeattr==5)) {
			personStr = SystemEnv.getHtmlLabelName(21399, user.getLanguage()) + ",";
		}
		tempSQL = "select agentorbyagentid, agenttype, showorder from workflow_currentoperator where nodeid = " + nodeid + " and requestid = " + requestid + " and userid = " + userid + isremarkStr_t + " order by operatedate desc,operatetime desc, id desc";
		rs.executeSql(tempSQL);
		if (rs.next()) {
			agentorbyagentid = rs.getInt("agentorbyagentid");
	        agenttype = rs.getInt("agenttype");
	        showorder = rs.getInt("showorder");
		}
		if (logtype.equals("9") || logtype.equals("7")) { // logtype=9时 , "被转发人" 在
	                                                        // "操作人"后显示
			rs.executeSql("select max(showorder) as maxshow from workflow_requestlog where requestid = " + requestid + " and nodeid = " + nodeid + " and (logtype = '9' or logtype = '7')");
	        rs.next();
	        if (rs.getInt("maxshow") != -1) {
	        	showorder = rs.getInt("maxshow") + 1;
	        } else {
	        	showorder = 10000;
	        }
	        rs.executeSql("select receivedpersons,receivedpersonids from workflow_requestlog where requestid = " + requestid + " and nodeid = " + nodeid + " and (logtype = '2' or logtype = '0' or logtype = '3') order by operatedate desc,operatetime desc");// logtype在数据库中是char(1)字段
	        if (rs.next()) {
	        	personStr = Util.null2String(rs.getString(1));
	        	receivedPersonids = Util.null2String(rs.getString(2));
	        }
		}
	
		Procpara = "" + requestid + flag + workflowid + flag + nodeid + flag + logtype + flag + logdate + flag + logtime + flag + userid + flag + clientip + flag + usertype + flag + nextnodeid + flag + personStr.trim() + flag + agentorbyagentid + flag + agenttype + flag + showorder + flag + annexdocids + flag + requestLogId + flag + signdocids + flag + signworkflowids + flag + clientType + flag + speechAttachment + flag + handWrittenSign + flag + receivedPersonids +flag+ remarkLocation;
		//Procpara = "" + requestid + flag + workflowid + flag + nodeid + flag + logtype + flag + logdate + flag + logtime + flag + userid + flag + remark + flag + clientip + flag + usertype + flag + nextnodeid + flag + personStr.trim() + flag + agentorbyagentid + flag + agenttype + flag + showorder + flag + annexdocids + flag + requestLogId + flag + signdocids + flag + signworkflowids + flag + clientType + flag + speechAttachment + flag + handWrittenSign + flag + receivedPersonids;
		if (logdate.equals("")) {
	        //rs.executeProc("workflow_RequestLog_Insert_New", Procpara);
	        //if (rs.next()) {
	        //	logdate = Util.null2String(rs.getString(1));
	        //	logtime = Util.null2String(rs.getString(2));
	        //}
	        String currentString = execRequestlog(Procpara,rs,flag,remark);
			if(!"".equals(currentString) && currentString.indexOf("~~current~~") > -1){
				String [] arraycurrent = Util.TokenizerString2(currentString, "~~current~~");
				logdate = arraycurrent[0];
				logtime = arraycurrent[1];
			}
		} else {
			//rs.executeProc("workflow_RequestLogCurDate_New", Procpara);
			execRequestlog(Procpara,rs,flag,remark);
		}
	      // 抄送另外保存
		if (!personStr1.trim().equals("")) {
	        // 解决抄送人也把签字意见显示出来问题，把remark置为空，同时，相关附件、流程、文档也要空
	        // userid + flag + remark + flag，其中，remark在抄送中为空
			Procpara = "" + requestid + flag + workflowid + flag + nodeid + flag + "t" + flag + logdate + flag + logtime + flag + userid + flag + clientip + flag + usertype + flag + nextnodeid + flag + personStr1.trim() + flag + agentorbyagentid + flag + agenttype + flag + showorder + flag + "" + flag + requestLogId + flag + "" + flag + "" + flag + clientType + flag + speechAttachment + flag + handWrittenSign + flag + receivedPersonids1 +flag+ remarkLocation;
			if (logdate.equals("")) {
				//rs.executeProc("workflow_RequestLog_Insert_New", Procpara);
				//if (rs.next()) {
				//	logdate = Util.null2String(rs.getString(1));
				//	logtime = Util.null2String(rs.getString(2));
				//}
				String currentString = execRequestlog(Procpara,rs,flag,"");
				if(!"".equals(currentString) && currentString.indexOf("~~current~~") > -1){
					String [] arraycurrent = Util.TokenizerString2(currentString, "~~current~~");
					logdate = arraycurrent[0];
					logtime = arraycurrent[1];
				}
			} else {
				//rs.executeProc("workflow_RequestLogCurDate_New", Procpara);
				execRequestlog(Procpara,rs,flag,"");
			}
		}
	}
    
    RequestSignRelevanceWithMe reqsignwm = new RequestSignRelevanceWithMe();
    reqsignwm.inertRelevanceInfo(workflowid+"", requestid+"", nodeid+"", logtype+"", logdate, logtime, userid+"", remark);
  }

  /**
   * 根据节点写日志
   * 
   * @param logtype
   * @param nownodeid
   * @param nownextnodeid
   */
  private void saveRequestLog(String logtype, int nownodeid, int nownextnodeid) {
    String ismode = "";
    rs.executeSql("select ismode from workflow_flownode where workflowid=" + workflowid + " and nodeid=" + nownodeid);
    if (rs.next()) {
      ismode = Util.null2String(rs.getString("ismode"));
    }
    
    //相关附件
    String annexdocids = "";
    //如果从手机版提交流程，则从 signatureAppendfix 字段获取相当附件值。
    if(WorkflowSpeechAppend.isFromMobile(clientType)){
    	annexdocids = signatureAppendfix;
    }else{
    	if (!ismode.equals("1") || src.equals("supervise") || src.equals("intervenor")) {
    		RequestAnnexUpload rau = new RequestAnnexUpload();
    		rau.setRequest(fu);
    		rau.setUser(user);
    		annexdocids = rau.AnnexUpload();
    	} else {
    		if (isRequest) {
    			if (request != null)
    				annexdocids = Util.null2String(request.getParameter("qianzi"));
    		} else {
    			if (fu != null)
    				annexdocids = Util.null2String(fu.getParameter("qianzi"));
    		}
    	}
    }
    
    // 获取签字意见相关文档，相关流程
    if (isRequest) {
    	if (request != null) {
    		signdocids = Util.null2String(request.getParameter("signdocids"));
    		signworkflowids = Util.null2String(request.getParameter("signworkflowids"));
    	}
    } else {
    	if (fu != null) {
    		signdocids = Util.null2String(fu.getParameter("signdocids"));
    		signworkflowids = Util.null2String(fu.getParameter("signworkflowids"));
    	}
    }

    String clientip = "";
    if (isRequest) {
      if (request != null)
        clientip = Util.null2String(request.getRemoteAddr());
    } else {
      if (fu != null)
        clientip = Util.null2String(fu.getRemoteAddr());
    }

    int requestLogId = 0;
    if (isRequest) {
      if (request != null)
        requestLogId = Util.getIntValue(request.getParameter("workflowRequestLogId"), 0);
    } else {
      if (fu != null)
        requestLogId = Util.getIntValue(fu.getParameter("workflowRequestLogId"), 0);
    }

    if (nownextnodeid == 0)
      nownextnodeid = nownodeid;
    String personStr = "";
    String personStr1 = "";
    String receivedPersonids = "";
    String receivedPersonids1 = "";
    String isremarkStr_t = "";// 如果是提交、批准或者退回，为了防止当前操作人A，现在虽然是代理人，但是下一节点就是正常操作人，那么如果不限制isremark，找出来的就是非代理情况
    if ("0".equals(logtype) || "2".equals(logtype) || "3".equals(logtype) || "h".equals(logtype)) {
      isremarkStr_t = " and isremark='2' ";
    }
    /* ---------------- xwj for td2104 on 20050802 B E G I N ------------------ */
    if (isOldOrNewFlag(requestid)) {// 老数据, 相对 td2104 之前
      rs.executeSql("select userid,usertype from workflow_currentoperator where isremark = 0 and requestid = " + requestid);
      while (rs.next()) {
        if ("0".equals(rs.getString("usertype"))) {
          personStr += Util.toScreen(resourceComInfo.getResourcename(rs.getString("userid")), user.getLanguage()) + ",";
          receivedPersonids += Util.null2String(rs.getString("userid")) + ",";
        } else {
          personStr += Util.toScreen(customerInfoComInfo.getCustomerInfoname(rs.getString("userid")), user.getLanguage()) + ",";
          receivedPersonids += Util.null2String(rs.getString("userid")) + ",";
        }
      }

      Procpara = "" + requestid + flag + workflowid + flag + nownodeid + flag + logtype + flag + logdate + flag + logtime + flag + userid + flag + clientip + flag + usertype + flag + nownextnodeid + flag + personStr.trim() + flag + -1 + flag + "0" + flag + -1 + flag + annexdocids + flag + requestLogId + flag + signdocids + flag + signworkflowids + flag + clientType + flag + speechAttachment + flag + handWrittenSign + flag + receivedPersonids +flag + remarkLocation;
      //Procpara = "" + requestid + flag + workflowid + flag + nownodeid + flag + logtype + flag + logdate + flag + logtime + flag + userid + flag + remark + flag + clientip + flag + usertype + flag + nownextnodeid + flag + personStr.trim() + flag + -1 + flag + "0" + flag + -1 + flag + annexdocids + flag + requestLogId + flag + signdocids + flag + signworkflowids + flag + clientType + flag + speechAttachment + flag + handWrittenSign + flag + receivedPersonids;
      if (logdate.equals("")) {
    	  String currentString = execRequestlog(Procpara,rs,flag,remark);
    	  if(!"".equals(currentString) && currentString.indexOf("~~current~~") > -1){
    		  String [] arraycurrent = Util.TokenizerString2(currentString, "~~current~~");
    		  logdate = arraycurrent[0];
    		  logtime = arraycurrent[1];
    	  }
        //rs.executeProc("workflow_RequestLog_Insert_New", Procpara);
        //if (rs.next()) {
        //  logdate = Util.null2String(rs.getString(1));
        //  logtime = Util.null2String(rs.getString(2));
        //}
      } else {
        //rs.executeProc("workflow_RequestLogCurDate_New", Procpara);
        execRequestlog(Procpara,rs,flag,remark);
      }
    } else {
      String tempSQL = "";
      int agentorbyagentid = -1;
      int agenttype = 0;
      int showorder = 1;

      ResourceComInfo resourceComInfo = null;
      try {
        resourceComInfo = new ResourceComInfo();
      } catch (Exception ex) {
      }

      rs.executeSql("select userid,usertype,agentorbyagentid, agenttype from workflow_currentoperator where isremark in ('0','4') and requestid = " + requestid + " and nodeid in(" + wflinkinfo.getBrancheNode(nownextnodeid, workflowid, "" + nownextnodeid, requestid) + ") order by showorder asc");
      String _useridStr = "";
      while (rs.next()) {
        if ("0".equals(rs.getString("usertype"))) {
          if (rs.getInt("agenttype") == 0) {
            String tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(rs.getString("userid")), user.getLanguage());
            String tempUseridStr = Util.null2String(rs.getString("userid"));
            if ((personStr.indexOf("," + tempPersonStr + ",") == -1 && personStr.indexOf(tempPersonStr + ",") == -1) || (_useridStr.indexOf("," + tempUseridStr + ",") == -1 && _useridStr.indexOf(tempUseridStr + ",") == -1)) {
              personStr += tempPersonStr + ",";
              _useridStr += tempUseridStr + ",";
              receivedPersonids += tempUseridStr + ",";
            }
          } else if (rs.getInt("agenttype") == 2) {
            String tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(rs.getString("agentorbyagentid")), user.getLanguage()) + "->" + Util.toScreen(resourceComInfo.getResourcename(rs.getString("userid")), user.getLanguage());
            String tempUseridStr = Util.null2String(rs.getString("agentorbyagentid"));
            if ((personStr.indexOf("," + tempPersonStr + ",") == -1 && personStr.indexOf(tempPersonStr + ",") == -1) || (_useridStr.indexOf("," + tempUseridStr + ",") == -1 && _useridStr.indexOf(tempUseridStr + ",") == -1)) {
              personStr += tempPersonStr + ",";
              _useridStr += tempUseridStr + ",";
              receivedPersonids += tempUseridStr + ",";
            }
          } else {
          }
        } else {
          String tempPersonStr = Util.toScreen(customerInfoComInfo.getCustomerInfoname(rs.getString("userid")), user.getLanguage());
          String tempUseridStr = Util.null2String(rs.getString("userid"));
          if ((personStr.indexOf("," + tempPersonStr + ",") == -1 && personStr.indexOf(tempPersonStr + ",") == -1) || (_useridStr.indexOf("," + tempUseridStr + ",") == -1 && _useridStr.indexOf(tempUseridStr + ",") == -1)) {
            personStr += tempPersonStr + ",";
            _useridStr += tempUseridStr + ",";
            receivedPersonids += tempUseridStr + ",";
          }
        }
      }

      if (logtype.equals("0") || logtype.equals("2")) {// 提交到下一节点时，下一节点的抄送另外保存
        // 抄送协办人
        sql = "select userid,usertype,agentorbyagentid, agenttype from workflow_currentoperator where isremark ='7' and nodeid = " + nownextnodeid + " and requestid = " + requestid;
        sql += " and id>=" + currentopratorInsFirstid;
        sql += " order by id desc";
        rs.executeSql(sql);

        if (rs.next()) {
          if ("0".equals(rs.getString("usertype"))) {
            if (rs.getInt("agenttype") == 0) {
              String tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(rs.getString("userid")), user.getLanguage());
              if (personStr1.indexOf("," + tempPersonStr + ",") == -1 && personStr1.indexOf(tempPersonStr + ",") == -1) {
                personStr1 += tempPersonStr + ",";
                receivedPersonids1 += Util.null2String(rs.getString("userid")) + ",";
              }
            } else if (rs.getInt("agenttype") == 2) {
              String tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(rs.getString("agentorbyagentid")), user.getLanguage()) + "->" + Util.toScreen(resourceComInfo.getResourcename(rs.getString("userid")), user.getLanguage());
              if (personStr1.indexOf("," + tempPersonStr + ",") == -1 && personStr1.indexOf(tempPersonStr + ",") == -1) {
                personStr1 += tempPersonStr + ",";
                receivedPersonids1 += Util.null2String(rs.getString("userid")) + ",";
              }
            } else {
            }
          } else {
            String tempPersonStr = Util.toScreen(customerInfoComInfo.getCustomerInfoname(rs.getString("userid")), user.getLanguage());
            if (personStr1.indexOf("," + tempPersonStr + ",") == -1 && personStr1.indexOf(tempPersonStr + ",") == -1) {
              personStr1 += tempPersonStr + ",";
              receivedPersonids1 += Util.null2String(rs.getString("userid")) + ",";
            }
          }
        }
        // 抄送
        // TD23439，根据之前operator89List的值来分析是否有被炒送人
         if (operator89mp != null && !operator89mp.isEmpty()) {
        	operator89List = (ArrayList)operator89mp.get(String.valueOf(nextnodeid));
        	operatortype89List = (ArrayList)operatortype89mp.get(String.valueOf(nextnodeid));
        	agentoperator89List = (ArrayList)agentoperator89mp.get(String.valueOf(nextnodeid));
        	
        	if (operator89List == null) operator89List = new ArrayList();
        	if (operatortype89List == null) operatortype89List = new ArrayList();
        	if (agentoperator89List == null) agentoperator89List = new ArrayList();
         }
        
        for (int cc = 0; cc < operator89List.size(); cc++) {
          int operatortype_tmp = Util.getIntValue((String) operatortype89List.get(cc), 0);
          int operator_tmp = Util.getIntValue((String) operator89List.get(cc), 0);
          int agentoperator_tmp = Util.getIntValue((String) agentoperator89List.get(cc), 0);
          if (operatortype_tmp == 0) {
            if (agentoperator_tmp == 0) {
              String tempPersonStr = Util.toScreen(resourceComInfo.getResourcename("" + operator_tmp), user.getLanguage());
              if (personStr1.indexOf("," + tempPersonStr + ",") == -1 && personStr1.indexOf(tempPersonStr + ",") == -1) {
                personStr1 += tempPersonStr + ",";
                receivedPersonids1 += Util.null2String(operator89List.get(cc)) + ",";
              }
            } else if (agentoperator_tmp > 0) {
              String tempPersonStr = Util.toScreen(resourceComInfo.getResourcename("" + operator_tmp), user.getLanguage()) + "->" + Util.toScreen(resourceComInfo.getResourcename("" + agentoperator_tmp), user.getLanguage());
              if (personStr1.indexOf("," + tempPersonStr + ",") == -1 && personStr1.indexOf(tempPersonStr + ",") == -1) {
                personStr1 += tempPersonStr + ",";
                receivedPersonids1 += Util.null2String(operator89List.get(cc)) + ",";
              }
            } else {
            }
          } else {
            String tempPersonStr = Util.toScreen(customerInfoComInfo.getCustomerInfoname("" + operator_tmp), user.getLanguage());
            if (personStr1.indexOf("," + tempPersonStr + ",") == -1 && personStr1.indexOf(tempPersonStr + ",") == -1) {
              personStr1 += tempPersonStr + ",";
              receivedPersonids1 += Util.null2String(operator89List.get(cc)) + ",";
            }
          }
        }
      }
      if (!canflowtonextnode && (nextnodeattr == 3 || nextnodeattr == 4|| nextnodeattr==5)) {
        personStr = SystemEnv.getHtmlLabelName(21399, user.getLanguage()) + ",";
      }
      tempSQL = "select agentorbyagentid, agenttype, showorder from workflow_currentoperator where nodeid = " + nownodeid + " and requestid = " + requestid + " and userid = " + userid + isremarkStr_t + " order by operatedate desc,operatetime desc, id desc";
      rs.executeSql(tempSQL);
      if (rs.next()) {
        agentorbyagentid = rs.getInt("agentorbyagentid");
        agenttype = rs.getInt("agenttype");
        showorder = rs.getInt("showorder");
      }
      if (logtype.equals("9") || logtype.equals("7")) { // logtype=9时 , "被转发人" 在
                                                        // "操作人"后显示
        rs.executeSql("select max(showorder) as maxshow from workflow_requestlog where requestid = " + requestid + " and nodeid = " + nownodeid + " and (logtype = '9' or logtype = '7')");
        rs.next();
        if (rs.getInt("maxshow") != -1) {
          showorder = rs.getInt("maxshow") + 1;
        } else {
          showorder = 10000;
        }
        rs.executeSql("select receivedpersons,receivedPersonids from workflow_requestlog where requestid = " + requestid + " and nodeid = " + nownodeid + " and (logtype = '2' or logtype = '0' or logtype = '3') order by operatedate desc,operatetime desc");// logtype在数据库中是char(1)字段
        if (rs.next()) {
          personStr = Util.null2String(rs.getString(1));
          receivedPersonids = Util.null2String(rs.getString(2));
        }
      }

      Procpara = "" + requestid + flag + workflowid + flag + nownodeid + flag + logtype + flag + logdate + flag + logtime + flag + userid + flag + clientip + flag + usertype + flag + nownextnodeid + flag + personStr.trim() + flag + agentorbyagentid + flag + agenttype + flag + showorder + flag + annexdocids + flag + requestLogId + flag + signdocids + flag + signworkflowids + flag + clientType + flag + speechAttachment + flag + handWrittenSign + flag + receivedPersonids + flag + remarkLocation;
      //Procpara = "" + requestid + flag + workflowid + flag + nownodeid + flag + logtype + flag + logdate + flag + logtime + flag + userid + flag + remark + flag + clientip + flag + usertype + flag + nownextnodeid + flag + personStr.trim() + flag + agentorbyagentid + flag + agenttype + flag + showorder + flag + annexdocids + flag + requestLogId + flag + signdocids + flag + signworkflowids + flag + clientType + flag + speechAttachment + flag + handWrittenSign + flag + receivedPersonids;
      if (logdate.equals("")) {
        //rs.executeProc("workflow_RequestLog_Insert_New", Procpara);
        //if (rs.next()) {
        //  logdate = Util.null2String(rs.getString(1));
        //  logtime = Util.null2String(rs.getString(2));
        //}
        String currentString = execRequestlog(Procpara,rs,flag,remark);
  	    if(!"".equals(currentString) && currentString.indexOf("~~current~~") > -1){
  		  String [] arraycurrent = Util.TokenizerString2(currentString, "~~current~~");
  		  logdate = arraycurrent[0];
  		  logtime = arraycurrent[1];
  	    }
      } else {
        //rs.executeProc("workflow_RequestLogCurDate_New", Procpara);
        execRequestlog(Procpara,rs,flag,remark);
      }
      // 抄送另外保存
      if (!personStr1.trim().equals("")) {
        // 解决抄送人也把签字意见显示出来问题，把remark置为空，同时，相关附件、流程、文档也要空
        // userid + flag + remark + flag，其中，remark在抄送中为空
        Procpara = "" + requestid + flag + workflowid + flag + nownodeid + flag + "t" + flag + logdate + flag + logtime + flag + userid + flag + clientip + flag + usertype + flag + nownextnodeid + flag + personStr1.trim() + flag + agentorbyagentid + flag + agenttype + flag + showorder + flag + "" + flag + requestLogId + flag + "" + flag + "" + flag + clientType + flag + speechAttachment + flag + handWrittenSign + flag + receivedPersonids1 +flag + remarkLocation;
        //Procpara = "" + requestid + flag + workflowid + flag + nownodeid + flag + "t" + flag + logdate + flag + logtime + flag + userid + flag + "" + flag + clientip + flag + usertype + flag + nownextnodeid + flag + personStr1.trim() + flag + agentorbyagentid + flag + agenttype + flag + showorder + flag + "" + flag + requestLogId + flag + "" + flag + "" + flag + clientType + flag + speechAttachment + flag + handWrittenSign + flag + receivedPersonids1;
        if (logdate.equals("")) {
          //rs.executeProc("workflow_RequestLog_Insert_New", Procpara);
          //if (rs.next()) {
          //  logdate = Util.null2String(rs.getString(1));
          //  logtime = Util.null2String(rs.getString(2));
          //}
          String currentString = execRequestlog(Procpara,rs,flag,"");
          if(!"".equals(currentString) && currentString.indexOf("~~current~~") > -1){
    		  String [] arraycurrent = Util.TokenizerString2(currentString, "~~current~~");
    		  logdate = arraycurrent[0];
    		  logtime = arraycurrent[1];
          }
        } else {
          //rs.executeProc("workflow_RequestLogCurDate_New", Procpara);
          execRequestlog(Procpara,rs,flag,"");
        }
      }
    }
  }

  private String doRequestMark() {
	RecordSetTrans rst1=new RecordSetTrans();
	rst1.setAutoCommit(false);
	try{
		String needmark = "";
		rst1.executeSql(" select needmark from workflow_base where id = " + workflowid);
		if (rst1.next()) needmark = Util.null2String(rs.getString("needmark"));
		
		if (needmark.equals("1")) {
		  String newcurrentdate = Util.StringReplace(currentdate, "-", "");
		  rst1.executeSql("select requestmark from workflow_requestmark where markdate = '" + currentdate + "' ");
		  if (rst1.next()) {
		    int requestmark = Util.getIntValue(rs.getString("requestmark"), 0);
		    rst1.executeSql("update workflow_requestmark set requestmark = requestmark+1 where markdate = '" + currentdate + "' ");
		    return newcurrentdate + Util.add0(requestmark + 1, 4);
		  } else {
			  rst1.executeSql("insert into workflow_requestmark values('" + currentdate + "',1) ");
		    return newcurrentdate + Util.add0(1, 4);
		  }
		}
		rst1.commit();
    }catch(Exception exception){
        exception.printStackTrace();
      	rst1.rollback();
        return "";
    }
    return "";
  }

  public String getMessageType() {
    return messageType;
  }

  public void setMessageType(String messageType) {
    if(messageType==null || messageType.equals("")){
      messageType="0";
    }
    this.messageType = messageType;
  }

  /**
   * @author xwj  20050802
   *判断当前流程是否为老数据(相对于 td2104 以前)
   * @param requestid 请求ID
   * @return 是否是老数据
   */
   public boolean isOldOrNewFlagTrans(int requestid,RecordSetTrans rst)throws Exception{
    /*  
    boolean isOldWf = false;
    try{
      rst.executeSql("select nodeid from workflow_currentoperator where requestid = " + requestid);
      while(rst.next()){
        if(rst.getString("nodeid") == null || "".equals(rst.getString("nodeid")) || "-1".equals(rst.getString("nodeid"))){
         isOldWf = true;
        }
      }
    }catch(Exception exception){
      throw exception;
    }  */ 
    return false;

   }
  /**
   * @author xwj  20050802
   *判断当前流程是否为老数据(相对于 td2104 以前)
   * @param requestid 请求ID
   * @return 是否是老数据
   */
   public boolean isOldOrNewFlag(int requestid){
    boolean isOldWf = false;
    RecordSet  rs_ = new RecordSet();
    rs_.executeSql("select nodeid from workflow_currentoperator where requestid = " + requestid);
    while(rs_.next()){
      if(rs_.getString("nodeid") == null || "".equals(rs_.getString("nodeid")) || "-1".equals(rs_.getString("nodeid"))){
       isOldWf = true;
      }
    }
    return isOldWf;
   }
   
	 /**
	  * 流程下一节点操作者的插入,并触发提醒信息
	  *
	  */
	 public void setOperator(RecordSetTrans rst)throws Exception{
		 //记录日志
		 WFAutoApproveUtils.processApproveLog(rst,this);
		 wfAgentCondition wfAgentCondition=new wfAgentCondition();
		 ArrayList poppuplist=new ArrayList();
         WFCoadjutantManager coadjutantmanager=new WFCoadjutantManager();
         WFAutoApproveUtils wfautoApproveUtil = new WFAutoApproveUtils();
         int organizedid=0;
         rst.executeSql("select nodeattribute from workflow_nodebase where id="+this.nodeid);
         rst.next();
         int nodeattr = rst.getInt(1);
         //-----------------------------------------------------------------
         //操作者循环过程中，可能需要用到，上一个操作者的信息，
         //为了优化性能，不再每次都将查询结果保存下来，而是将原始的操作者信息保存下来
         //待需要用到结果信息时，再使用保存下来 信息进行查询
         // start
         //-----------------------------------------------------------------
         String prevOperatorid = "";
         int prevOperatortype = 0;
         int prevOperatorisremark = 0;
         int prevOperatornodeid = 0;
         int prevgroupdetailid = 0;
         //-----------------------------------------------------------------
         // end
         //-----------------------------------------------------------------
         Date d2 = null;
         ArrayList operatorsWfNew = new ArrayList();
         ArrayList operatorsWfEnd = new ArrayList(); 
         for(int n=0;n<nextnodeids.size();n++){
            nextnodeid=Util.getIntValue((String)nextnodeids.get(n),0);
            operatorsht=(Hashtable)operatorshts.get(n);
            nextnodetype=Util.null2String((String)nextnodetypes.get(n));
            nextnodeattr=Util.getIntValue((String)nextnodeattrs.get(n),0);
            
			//各节点抄送人初始化
			operator89List = new ArrayList();
			operatortype89List = new ArrayList();
			agentoperator89List = new ArrayList();
			//操作人更新结束
			int showorder = 0;
			TreeMap map = new TreeMap(new ComparatorUtilBean());
			Enumeration tempKeys = operatorsht.keys();
			while (tempKeys.hasMoreElements()) {
				String tempKey = (String) tempKeys.nextElement();
				ArrayList tempoperators = (ArrayList) operatorsht.get(tempKey);
				map.put(tempKey,tempoperators);
			}
			try{
				
			int verSuccussPerson = 0;
			//获取当前请求操作者的代理情况
			//在插入操作者迭代过程中在单独查询
			Map<String, String> operatorAgentInfo = wfAgentCondition.getAgentInfoByResouce(
			        String.valueOf(workflowid)
			        , wfAgentCondition.getAlloperator(operatorsht, map)
			        , String.valueOf(requestid));
			
			Iterator iterator = map.keySet().iterator();
			//Date d1 = new Date();
			//System.out.println("人员计算插入开始...");
			List<List<Object>> insertOperatorParamsList = new ArrayList<List<Object>>();
			String operatornames = "";
			while(iterator.hasNext()) {
				String operatorgroup = (String) iterator.next();
				ArrayList operators = (ArrayList) operatorsht.get(operatorgroup);

				for (int i = 0; i < operators.size(); i++) {
					showorder++;
					String operatorandtype = (String) operators.get(i);
					String[] operatorandtypes = Util.TokenizerString2(operatorandtype, "_");
					String opertor = operatorandtypes[0];
					String opertortype = operatorandtypes[1];
					groupdetailid = Util.getIntValue(operatorandtypes[2],-1);
					typeid= Util.getIntValue(operatorandtypes[3],0);
                    if(!canflowtonextnode&&typeid!=-3&&typeid!=-4&&typeid!=-5) continue;
					//代理数据检索
					boolean isbeAgent=false;
					String agenterId="";

					if (opertortype.equals("0")){   //客户不用考虑代理 by ben 2008-1-10
						//agenterId=wfAgentCondition.getAgentid(""+workflowid,""+opertor,""+requestid);
					    agenterId = operatorAgentInfo.get(opertor);
					    if(agenterId != null && !agenterId.equals("")){
					        isbeAgent=true;
					    }

					}
					String hrmstatusSql = "";
					String tempStatus = "";
					String operatorname = "";
					if (!isbeAgent) {
					 	//在非归档节点，如果下一节点操作者为离职人员，且没有代理，则流程不流转（针对type为：3， 8， 17，18的情况）
						if (!"3".equals(nextnodetype)) {
							//根据用户类型来查询不同的表（员工表和客户信息表）。
							if (opertortype.equals("0")){
								hrmstatusSql = "select status from hrmresource where id=" + opertor;
								tempStatus = resourceComInfo.getStatus(opertor);
								operatorname = resourceComInfo.getLastname(opertor);
							}else{
								hrmstatusSql = "select status,name from CRM_CustomerInfo where id=" + opertor;
								rst.execute(hrmstatusSql);
	                            if (rst.next()) {
	                                tempStatus = rst.getString("status");
	                                operatornames = Util.null2String(rst.getString("name"));
	                            }
							}
							
							//非系统管理员且不是被代理人且为离职人员
							if (opertortype.equals("0")){
    							if (!"".equals(tempStatus) && ("5".equals(tempStatus) || "4".equals(tempStatus) || "6".equals(tempStatus))) {
    							    if(typeid != -3 && typeid != -4 && typeid != -5){
    							        operatornames += operatorname+",";
    							    }
    								continue;
    							}
							}
						}
					 }else{
						if (!"3".equals(nextnodetype)) {
							//根据用户类型来查询不同的表（员工表和客户信息表）。
							hrmstatusSql = "select status,lastname from hrmresource where id=" + agenterId;
							rst.execute(hrmstatusSql);
							if (rst.next()) {
								tempStatus = rst.getString("status");
								operatorname = Util.null2String(rst.getString("lastname"));
							}
							//非系统管理员且不是被代理人且为离职人员
							if (opertortype.equals("0")){
    							if (!"".equals(tempStatus) && ("5".equals(tempStatus) || "4".equals(tempStatus) || "6".equals(tempStatus))) {
    							    if(typeid != -3 && typeid != -4 && typeid != -5){
    							        operatornames += operatorname+",";
    							    }
    								isbeAgent = false;
    							}
							}
						} 
					 }
					 if(typeid != -3 && typeid != -4 && typeid != -5) verSuccussPerson++;

					//当符合代理条件时添加代理人
					String Procpara1="";
					
					int tempremark=0;
					if (typeid==-3){//抄送（不需提交）
						tempremark=8;
					}
					if (typeid==-4){//抄送（需提交）
						tempremark=9;
					}
                    if (typeid==-5){//抄送协办人
						tempremark=7;
                        rst.executeSql("update workflow_CurrentOperator set isremark='2' where isremark='7' and requestid="+requestid+" and userid="+opertor+" and usertype="+opertortype+" and nodeid="+nodeid);
					}

                    
    	            //如下变量定义为避免合并节点重复提交作判断。如果是合并节点的抄送类型，则需保证只抄送一次。
    	            //当前节点类型是分之的合并节点(3.按通过分支数合并节点; 4.指定分支合并节点)
    	            boolean isMergeNode = (nextnodeattr == 3 || nextnodeattr == 4 || nextnodeattr == 5);
    	            //当前操作者类型是抄送(7.抄送协办人; 8.抄送不需提交; 9.抄送需提交   )
    	            boolean isCopyOperation = (tempremark == 7 || tempremark == 8 || tempremark == 9);
                    
    	            //如果既非合并节点，也不是抄送类型，则正常执行。
    	            if(isMergeNode && isCopyOperation){
    	            //如果既是合并节点，也是抄送类型，则进一步判断当前是否达到合并节点的条件。
    	            
    	                //合并节点是否可以走到下一节点
    	                boolean canFlowToNextNode = wflinkinfo.FlowToNextNode(requestid, nodeid, nextnodeid, String.valueOf(nextnodeattr), Util.getIntValue((String) nextnodepassnums.get(n)), Util.getIntValue((String) linkismustpasss.get(n)), rst);
    	                
    	                //System.out.println("requestid="+requestid+" nodeid="+nodeid+" nextnodeid="+nextnodeid+" nextnodeattr="+String.valueOf(nextnodeattr)+" canFlowToNextNode = "+canFlowToNextNode);
    	                if(canFlowToNextNode){
    	                    //老数据, 相对 td2104 之前
    						if(isOldOrNewFlagTrans(requestid,rst)){
    							if(isbeAgent){
    								//设置被代理人已操作
    								Procpara = "" + requestid + flag + opertor + flag + operatorgroup + flag
    								+ workflowid + flag + workflowtype + flag + opertortype + flag + "2" + flag + -1 +
    								flag + -1 + flag + "0" + flag + -1+ flag +groupdetailid+flag+this.currentdate+flag+this.currenttime;
    								rst.executeProc("workflow_CurrentOperator_I2", Procpara);
    								//设置代理人
    								Procpara1 = "" + requestid + flag + agenterId + flag + operatorgroup + flag
    								+ workflowid + flag + workflowtype + flag + opertortype + flag + tempremark + flag + -1 +
    								flag + -1 + flag + "0" + flag + -1 + flag +groupdetailid+flag+this.currentdate+flag+this.currenttime;
    								rst.executeProc("workflow_CurrentOperator_I2", Procpara1);
    								if(tempremark==8 || tempremark==9){
    									operator89List.add(""+opertor);
    									operatortype89List.add("0");
    									agentoperator89List.add(""+agenterId);
    								}
    							}else{
    								Procpara = "" + requestid + flag + opertor + flag + operatorgroup + flag
    								+ workflowid + flag + workflowtype + flag + opertortype + flag + tempremark + flag + -1 +
    								flag + -1 + flag + "0" + flag + -1 + flag +groupdetailid+flag+this.currentdate+flag+this.currenttime;
    								rst.executeProc("workflow_CurrentOperator_I2", Procpara);
    								if(tempremark==8 || tempremark==9){
    									operator89List.add(""+opertor);
    									operatortype89List.add(""+opertortype);
    									agentoperator89List.add("0");
    								}
    							}
    						} else {
    							if(isbeAgent){
    								//设置被代理人已操作
    								Procpara = "" + requestid + flag + opertor + flag + operatorgroup + flag
    								+ workflowid + flag + workflowtype + flag + opertortype + flag + "2" + flag + nextnodeid +
    								flag + agenterId + flag + "1" + flag + showorder+ flag +groupdetailid+flag+this.currentdate+flag+this.currenttime;
    								rst.executeProc("workflow_CurrentOperator_I2", Procpara);
    								//设置代理人
    								Procpara1 = "" + requestid + flag + agenterId + flag + operatorgroup + flag
    								+ workflowid + flag + workflowtype + flag + opertortype + flag + tempremark + flag + nextnodeid +
    								flag + opertor + flag + "2" + flag + showorder+ flag +groupdetailid+flag+this.currentdate+flag+this.currenttime;
    								rst.executeProc("workflow_CurrentOperator_I2", Procpara1);
    								if(tempremark==8 || tempremark==9){
    									operator89List.add(""+opertor);
    									operatortype89List.add("0");
    									agentoperator89List.add(""+agenterId);
    								}
    							}else{
    								Procpara = "" + requestid + flag + opertor + flag + operatorgroup + flag
    								+ workflowid + flag + workflowtype + flag + opertortype + flag + tempremark + flag + nextnodeid +
    								flag + -1 + flag + "0" + flag + showorder+ flag +groupdetailid+flag+this.currentdate+flag+this.currenttime;
    								rst.executeProc("workflow_CurrentOperator_I2", Procpara);
    								if(tempremark==8 || tempremark==9){
    									operator89List.add(""+opertor);
    									operatortype89List.add(""+opertortype);
    									agentoperator89List.add("0");
    								}
    							}

    						}
    						
    						//-----------------------------------------------------------------
                            //操作者循环过程中，可能需要用到，上一个操作者的信息，
                            //为了优化性能，不再每次都将查询结果保存下来，而是将原始的操作者信息保存下来
                            //待需要用到结果信息时，再使用保存下来 信息进行查询
                            // start
                            //-----------------------------------------------------------------
                           // currentopratorInsFirstid为0时，查询一次，为其赋值，后续不再执行sql
                           if (currentopratorInsFirstid == 0) {
                               if(isbeAgent){
                                   sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + agenterId + " and usertype=" + usertype + " and isremark='" + tempremark + "' and nodeid=" + nextnodeid + " order by id desc";                      
                               }else{
                                   sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + opertor + " and usertype=" + usertype + " and isremark='" + tempremark + "' and nodeid=" + nextnodeid + " order by id desc";                        
                               }
                               rst.execute(sql);
                               if (rst.next()) {
                                   currentopratorInsFirstid = rst.getInt("id");
                               }
                           }
                           //如果是协办人，则根据保存的原始信息进行查询
                           //先查询出上次的信息，再查处本次需要的信息
                           if (tempremark == 7) {
                               sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + prevOperatorid + " and usertype=" + prevOperatortype + " and isremark='" + prevOperatorisremark + "' and nodeid=" + prevOperatornodeid + " order by id desc";                        
                               rst.execute(sql);
                               if (rst.next()) {
                                   organizedid = rst.getInt("id");
                               }
                               
                               if(isbeAgent){
                                   sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + agenterId + " and usertype=" + usertype + " and isremark='" + tempremark + "' and nodeid=" + nextnodeid + " order by id desc";                      
                               }else{
                                   sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + opertor + " and usertype=" + usertype + " and isremark='" + tempremark + "' and nodeid=" + nextnodeid + " order by id desc";                        
                               }
                               rst.execute(sql);
                               if (rst.next()) {
                                   int currentid = rst.getInt("id");
                                   coadjutantmanager.getCoadjutantRights(groupdetailid);
                                   coadjutantmanager.SaveCoadjutantRights(requestid, organizedid, currentid,rst);
                               }
                           } else {
                               //非协办人，保存当前操作人的信息
                               if(isbeAgent){
                                   prevOperatorid = agenterId;
                               }else{
                                   prevOperatorid = opertor;
                               }
                               prevOperatortype = usertype;
                               prevOperatorisremark = tempremark;
                               prevOperatornodeid = nextnodeid;
                           }
                           //-----------------------------
                           //end
                           //-----------------------------
    						
    						/*
    						//保存协办人操作权限
    	                    //sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + opertor + " and usertype=" + usertype + " and isremark='" + tempremark + "' and nodeid=" + nextnodeid + " order by id desc";
    						if(isbeAgent){
    		                    sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + agenterId + " and usertype=" + usertype + " and isremark='" + tempremark + "' and nodeid=" + nextnodeid + " order by id desc";						
    						}else{
    		                    sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + opertor + " and usertype=" + usertype + " and isremark='" + tempremark + "' and nodeid=" + nextnodeid + " order by id desc";						
    						}
    	                    rst.execute(sql);
    	                    if (rst.next()) {
    	                        int currentid = rst.getInt("id");
    	                        if(currentopratorInsFirstid==0) currentopratorInsFirstid=currentid;
    	                        if (tempremark == 7) {
    	                            coadjutantmanager.getCoadjutantRights(groupdetailid);
    	                            coadjutantmanager.SaveCoadjutantRights(requestid, organizedid, currentid,rst);
    	                        } else {
    	                            organizedid = currentid;
    	                        }
    	                    }
    	                    */
    	                }
    	            } else {
                        //老数据, 相对 td2104 之前
    					if(isOldOrNewFlagTrans(requestid,rst)){
    						if(isbeAgent){
    							//设置被代理人已操作
    							Procpara = "" + requestid + flag + opertor + flag + operatorgroup + flag
    							+ workflowid + flag + workflowtype + flag + opertortype + flag + "2" + flag + -1 +
    							flag + -1 + flag + "0" + flag + -1+ flag +groupdetailid+flag+this.currentdate+flag+this.currenttime;
    							rst.executeProc("workflow_CurrentOperator_I2", Procpara);
    							//设置代理人
    							Procpara1 = "" + requestid + flag + agenterId + flag + operatorgroup + flag
    							+ workflowid + flag + workflowtype + flag + opertortype + flag + tempremark + flag + -1 +
    							flag + -1 + flag + "0" + flag + -1 + flag +groupdetailid+flag+this.currentdate+flag+this.currenttime;
    							rst.executeProc("workflow_CurrentOperator_I2", Procpara1);
    							if(tempremark==8 || tempremark==9){
    								operator89List.add(""+opertor);
    								operatortype89List.add("0");
    								agentoperator89List.add(""+agenterId);
    							}
    						}else{
    							Procpara = "" + requestid + flag + opertor + flag + operatorgroup + flag
    							+ workflowid + flag + workflowtype + flag + opertortype + flag + tempremark + flag + -1 +
    							flag + -1 + flag + "0" + flag + -1 + flag +groupdetailid+flag+this.currentdate+flag+this.currenttime;
    							rst.executeProc("workflow_CurrentOperator_I2", Procpara);
    							if(tempremark==8 || tempremark==9){
    								operator89List.add(""+opertor);
    								operatortype89List.add(""+opertortype);
    								agentoperator89List.add("0");
    							}
    						}
    					} else {
    					    if (this.hasCoadjutant || currentopratorInsFirstid == 0) {
    						if(isbeAgent){
    							//设置被代理人已操作
    						    //QC152344,防止代理抄送引起的问题
    						    if(!wfCurrentOperatorAgent(rst, opertor, operatorgroup, opertortype, agenterId, showorder)){
                                    Procpara = "" + requestid + flag + opertor + flag + operatorgroup + flag
                                    + workflowid + flag + workflowtype + flag + opertortype + flag + "2" + flag + nextnodeid +
                                    flag + agenterId + flag + "1" + flag + showorder+ flag +groupdetailid+flag+this.currentdate+flag+this.currenttime;
                                    rst.executeProc("workflow_CurrentOperator_I2", Procpara);
    						    }
    							//设置代理人
    							Procpara1 = "" + requestid + flag + agenterId + flag + operatorgroup + flag
    							+ workflowid + flag + workflowtype + flag + opertortype + flag + tempremark + flag + nextnodeid +
    							flag + opertor + flag + "2" + flag + showorder+ flag +groupdetailid+flag+this.currentdate+flag+this.currenttime;
    							rst.executeProc("workflow_CurrentOperator_I2", Procpara1);
    							if(tempremark==8 || tempremark==9){
    								operator89List.add(""+opertor);
    								operatortype89List.add("0");
    								agentoperator89List.add(""+agenterId);
    							}
    						}else{
    							Procpara = "" + requestid + flag + opertor + flag + operatorgroup + flag
    							+ workflowid + flag + workflowtype + flag + opertortype + flag + tempremark + flag + nextnodeid +
    							flag + -1 + flag + "0" + flag + showorder+ flag +groupdetailid+flag+this.currentdate+flag+this.currenttime;
    							rst.executeProc("workflow_CurrentOperator_I2", Procpara);
    							if(tempremark==8 || tempremark==9){
    								operator89List.add(""+opertor);
    								operatortype89List.add(""+opertortype);
    								agentoperator89List.add("0");
    							}
    						}
    					    } else {
    					        if(isbeAgent){
                                    List<Object> insertSqlParamList = new ArrayList();
                                    //insertSqlParamList.add(requestid);
                                    insertSqlParamList.add(opertor);
                                    insertSqlParamList.add(operatorgroup);
                                    //insertSqlParamList.add(workflowid);
                                    //insertSqlParamList.add(workflowtype);
                                    insertSqlParamList.add(opertortype);
                                    insertSqlParamList.add("2");
                                    insertSqlParamList.add(nextnodeid);
                                    insertSqlParamList.add(agenterId);
                                    insertSqlParamList.add(1);
                                    insertSqlParamList.add(showorder);
                                    //insertSqlParamList.add(groupdetailid);
                                    insertSqlParamList.add(currentdate);
                                    insertSqlParamList.add(currenttime);
                                    insertSqlParamList.add(groupdetailid);
                                    insertSqlParamList.add("2");
                                    insertOperatorParamsList.add(insertSqlParamList);
                                    insertSqlParamList = new ArrayList();

                                    //insertSqlParamList.add(requestid);
                                    insertSqlParamList.add(agenterId);
                                    insertSqlParamList.add(operatorgroup);
                                    //insertSqlParamList.add(workflowid);
                                    //insertSqlParamList.add(workflowtype);
                                    insertSqlParamList.add(opertortype);
                                    insertSqlParamList.add(tempremark);
                                    insertSqlParamList.add(nextnodeid);
                                    insertSqlParamList.add(opertor);
                                    insertSqlParamList.add(2);
                                    insertSqlParamList.add(showorder);
                                    //insertSqlParamList.add(groupdetailid);
                                    insertSqlParamList.add(currentdate);
                                    insertSqlParamList.add(currenttime);
                                    insertSqlParamList.add(groupdetailid);
                                    insertSqlParamList.add(tempremark);
                                    insertOperatorParamsList.add(insertSqlParamList);
                                    
                                    if(tempremark==8 || tempremark==9){
                                        operator89List.add(""+opertor);
                                        operatortype89List.add("0");
                                        agentoperator89List.add(""+agenterId);
                                    }
                                }else{
                                    List<Object> insertSqlParamList = new ArrayList();
                                    //insertSqlParamList.add(requestid);
                                    insertSqlParamList.add(opertor);
                                    insertSqlParamList.add(operatorgroup);
                                    //insertSqlParamList.add(workflowid);
                                    //insertSqlParamList.add(workflowtype);
                                    insertSqlParamList.add(opertortype);
                                    insertSqlParamList.add(tempremark);
                                    insertSqlParamList.add(nextnodeid);
                                    insertSqlParamList.add(-1);
                                    insertSqlParamList.add(0);
                                    insertSqlParamList.add(showorder);
                                    //insertSqlParamList.add(groupdetailid);
                                    insertSqlParamList.add(currentdate);
                                    insertSqlParamList.add(currenttime);
                                    insertSqlParamList.add(groupdetailid);
                                    insertSqlParamList.add(tempremark);
                                    insertOperatorParamsList.add(insertSqlParamList);
                                    
                                    if(tempremark==8 || tempremark==9){
                                        operator89List.add(""+opertor);
                                        operatortype89List.add(""+opertortype);
                                        agentoperator89List.add("0");
                                    }
                                }
    					    }
    					}
    					
    					//-----------------------------------------------------------------
                        //操作者循环过程中，可能需要用到，上一个操作者的信息，
                        //为了优化性能，不再每次都将查询结果保存下来，而是将原始的操作者信息保存下来
                        //待需要用到结果信息时，再使用保存下来 信息进行查询
                        // start
                        //-----------------------------------------------------------------
                       // currentopratorInsFirstid为0时，查询一次，为其赋值，后续不再执行sql
                       if (currentopratorInsFirstid == 0) {
                           if(isbeAgent){
                               sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + agenterId + " and usertype=" + usertype + " and isremark='" + tempremark + "' and nodeid=" + nextnodeid + " order by id desc";                      
                           }else{
                               sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + opertor + " and usertype=" + usertype + " and isremark='" + tempremark + "' and nodeid=" + nextnodeid + " order by id desc";                        
                           }
                           rst.execute(sql);
                           if (rst.next()) {
                               currentopratorInsFirstid = rst.getInt("id");
                           }
                       }
                       //如果是协办人，则根据保存的原始信息进行查询
                       //先查询出上次的信息，再查处本次需要的信息
                       if (tempremark == 7) {
                           sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + prevOperatorid + " and usertype=" + prevOperatortype + " and isremark='" + prevOperatorisremark + "' and nodeid=" + prevOperatornodeid + " order by id desc";                        
                           rst.execute(sql);
                           if (rst.next()) {
                               organizedid = rst.getInt("id");
                           }
                           
                           if(isbeAgent){
                               sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + agenterId + " and usertype=" + usertype + " and isremark='" + tempremark + "' and nodeid=" + nextnodeid + " order by id desc";                      
                           }else{
                               sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + opertor + " and usertype=" + usertype + " and isremark='" + tempremark + "' and nodeid=" + nextnodeid + " order by id desc";                        
                           }
                           rst.execute(sql);
                           if (rst.next()) {
                               int currentid = rst.getInt("id");
                               coadjutantmanager.getCoadjutantRights(groupdetailid);
                               coadjutantmanager.SaveCoadjutantRights(requestid, organizedid, currentid,rst);
                           }
                       } else {
                           //非协办人，保存当前操作人的信息
                           if(isbeAgent){
                               prevOperatorid = agenterId;
                           }else{
                               prevOperatorid = opertor;
                           }
                           prevOperatortype = usertype;
                           prevOperatorisremark = tempremark;
                           prevOperatornodeid = nextnodeid;
                       }
                       //-----------------------------
                       //end
                       //-----------------------------
    					
    					/*
    					//保存协办人操作权限
                        //sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + opertor + " and usertype=" + usertype + " and isremark='" + tempremark + "' and nodeid=" + nextnodeid + " order by id desc";
    					if(isbeAgent){
    	                    sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + agenterId + " and usertype=" + usertype + " and isremark='" + tempremark + "' and nodeid=" + nextnodeid + " order by id desc";						
    					}else{
    	                    sql = "select id from workflow_CurrentOperator where requestid=" + requestid + " and userid=" + opertor + " and usertype=" + usertype + " and isremark='" + tempremark + "' and nodeid=" + nextnodeid + " order by id desc";						
    					}
                        rst.execute(sql);
                        if (rst.next()) {
                            int currentid = rst.getInt("id");
                            if(currentopratorInsFirstid==0) currentopratorInsFirstid=currentid;
                            if (tempremark == 7) {
                                coadjutantmanager.getCoadjutantRights(groupdetailid);
                                coadjutantmanager.SaveCoadjutantRights(requestid, organizedid, currentid,rst);
                            } else {
                                organizedid = currentid;
                            }
                        }
                        */
    	            }
    	                
					//对代理人判断提醒
					Procpara = opertor + flag + opertortype + flag + requestid;

					if (nextnodetype.equals("3")){
						if(isbeAgent){
							//update by fanggsh for TD4739  20060808 果归档前的最后一个操作者是本人的话，则从提醒中去掉。							
							if(!operatorsWfEnd.contains(agenterId+"_"+opertortype)&&!(userid+"_"+usertype).equals(agenterId+"_"+opertortype)){
								//poppupRemindInfoUtil.insertPoppupRemindInfo(Integer.parseInt(agenterId),1,opertortype,requestid,requestname,workflowid);
								Map popmap=new HashMap();
							    popmap.put("userid",""+Integer.parseInt(agenterId));
							    popmap.put("type","1");
							    popmap.put("logintype",""+opertortype);
							    popmap.put("requestid",""+requestid);
							    popmap.put("requestname",""+requestname);
							    popmap.put("workflowid",""+workflowid);
							    popmap.put("creater",""+creater);
							    poppuplist.add(popmap);
								operatorsWfEnd.add(agenterId+"_"+opertortype);
							}
						}else{
							//update by fanggsh for TD4739  20060808 果归档前的最后一个操作者是本人的话，则从提醒中去掉。							
							if(!operatorsWfEnd.contains(opertor+"_"+opertortype)&&!(userid+"_"+usertype).equals(opertor+"_"+opertortype)){
								//poppupRemindInfoUtil.insertPoppupRemindInfo(Integer.parseInt(opertor),1,opertortype,requestid,requestname,workflowid);
								 Map popmap=new HashMap();
								    popmap.put("userid",""+Integer.parseInt(opertor));
								    popmap.put("type","1");
								    popmap.put("logintype",""+opertortype);
								    popmap.put("requestid",""+requestid);
								    popmap.put("requestname",""+requestname);
								    popmap.put("workflowid",""+workflowid);
								    popmap.put("creater",""+creater);
								    poppuplist.add(popmap);
								operatorsWfEnd.add(opertor+"_"+opertortype);
						  }
						}
					}else{
					   if(isbeAgent){
							if(!operatorsWfNew.contains(agenterId+"_"+opertortype)){
							//poppupRemindInfoUtil.insertPoppupRemindInfo(Integer.parseInt(agenterId),0,opertortype,requestid,requestname,workflowid);
							Map popmap=new HashMap();
						    popmap.put("userid",""+Integer.parseInt(agenterId));
						    popmap.put("type","0");
						    popmap.put("logintype",""+opertortype);
						    popmap.put("requestid",""+requestid);
						    popmap.put("requestname",""+requestname);
						    popmap.put("workflowid",""+workflowid);
						    popmap.put("creater",""+creater);
						    poppuplist.add(popmap);
							operatorsWfNew.add(agenterId+"_"+opertortype);
							}
						}else{
							if(!operatorsWfNew.contains(opertor+"_"+opertortype)){
							//poppupRemindInfoUtil.insertPoppupRemindInfo(Integer.parseInt(opertor),0,opertortype,requestid,requestname,workflowid);
								Map popmap=new HashMap();
							    popmap.put("userid",""+Integer.parseInt(opertor));
							    popmap.put("type","0");
							    popmap.put("logintype",""+opertortype);
							    popmap.put("requestid",""+requestid);
							    popmap.put("requestname",""+requestname);
							    popmap.put("workflowid",""+workflowid);
							    popmap.put("creater",""+creater);
							    poppuplist.add(popmap);
							operatorsWfNew.add(opertor+"_"+opertortype);
							}
						}
					}
				}
				  //poppupRemindInfoUtil.insertPoppupRemindInfo(poppuplist);
			}
			if (insertOperatorParamsList != null && !insertOperatorParamsList.isEmpty()) {
                //防止workflowtype的值为空
                if ("".equals(Util.null2String(workflowtype))) {
                    WorkflowAllComInfo workflowComInfo = new WorkflowAllComInfo();
                    workflowtype = workflowComInfo.getWorkflowtype(""+workflowid);
                }
                 
                rst.executeBatchSql("INSERT INTO workflow_currentoperator ( requestid, userid, groupid, workflowid, workflowtype, usertype, isremark, nodeid" +
                        ", agentorbyagentid, agenttype, showorder, receivedate, receivetime, viewtype, iscomplete, islasttimes, groupdetailid, preisremark, needwfback ) " +
                        " VALUES (" + requestid + ", ?, ?, " + workflowid + ", " + workflowtype + ", ?, ?, ?, ?, ?, " +
                        " ?, ?, ?, 0,0,1, ?, ?, '1' )", insertOperatorParamsList);
            }
            //d2 = new Date();
            //System.out.println("人员计算插入完成，耗时：" + (d2.getTime() - d1.getTime())/1000 + "秒");
			//Date d2 = new Date();
			//System.out.println("人员计算插入完成，耗时：" + (d2.getTime() - d1.getTime())/1000 + "秒");
			//System.out.println("人员提醒信息插入完成，耗时：" + (new Date().getTime() - d2.getTime())/1000 + "秒");
			//抄送
			String tempNextNodeId = String.valueOf(nextnodeid); 
			operator89mp.put(tempNextNodeId, operator89List);
			operatortype89mp.put(tempNextNodeId, operatortype89List);
			agentoperator89mp.put(tempNextNodeId, agentoperator89List);
			
			//没有下一节点操作者
			if (verSuccussPerson == 0 && nextnodeattr !=3 && nextnodeattr != 4 && nextnodeattr != 5 && canflowtonextnode) {
			    this.setMessage(WorkflowRequestMessage.WF_REQUEST_ERROR_CODE_07);
			    if(StringUtil.isNotNull(operatornames)){
			        operatornames =  operatornames.substring(0,operatornames.length()-1);
			    }
	            this.setMessagecontent(WorkflowRequestMessage.getRMOperatorOutInfo(nextnodeid,userlanguage,workflowid,operatornames));
				throw new Exception();
			}
			//操作人更新结束
            //更新当前节点表
            if(canflowtonextnode){
            	if(nodeattr==2&&nextnodeattr!=2){//中间节点退回到主干节点
            		//rst.executeSql("update workflow_currentoperator set isremark=2 ,operatedate='"+this.currentdate+"',operatetime='"+this.currenttime+"' where nodeid!="+nextnodeid+" and (isremark=0 or (isremark=2 and (operatedate is null or operatedate=''))) and requestid="+requestid+" and preisremark=0");//所有节点操作人的待办 改成已办
					rst.executeSql("update workflow_currentoperator set isremark=2 ,operatedate='"+this.currentdate+"',operatetime='"+this.currenttime+"' where nodeid!="+nextnodeid+" and isremark=0 and requestid="+requestid+" and preisremark=0");//所有节点操作人的待办 改成已办
					rs.executeSql("select id,groupid,nodeid from workflow_currentoperator  where nodeid!="+nextnodeid+" and (isremark=2 and ( operatedate is null or operatedate='')) and requestid="+requestid+" and preisremark=0");//所有节点操作人的待办 改成已办
            		while(rs.next()){
            			String _tempid = rs.getString(1);
            			String _tempgroupid = rs.getString(2);
            			String _tempnodeid = rs.getString(3);
            			rst.executeSql("select operatedate,operatetime from workflow_currentoperator where requestid="+requestid+" and nodeid="+nodeid+" and isremark=2 and preisremark=0 and groupid="+_tempgroupid);
            			if(rst.next()){
            				String _tempdate = rs.getString(1);
            				String _temptime = rs.getString(2);
            				rst.executeSql("update workflow_currentoperator set operatedate='"+_tempdate+"',operatetime='"+_temptime+"' where id="+_tempid);
            			}
            		}
            		rst.executeSql("delete from workflow_nownode where requestid="+requestid);//删掉所有的当前节点
            	}
            	try{
		            if(innodeids.equals("")||innodeids.equals("0")){
		            	innodeids=nodeid+"";
		            }
		            rst.executeSql("delete from workflow_nownode where nownodeid in("+innodeids+") and requestid="+requestid);
		            rst.executeSql("insert into workflow_nownode(requestid,nownodeid,nownodetype,nownodeattribute) values("+requestid+","+nextnodeid+","+nextnodetype+","+nextnodeattr+")");
            	}catch(Exception e){
            		System.out.println(e.getMessage());
            	}
            }
         }catch(Exception exception){
        	writeLog(exception);
	     	throw exception;
	     }
         }
         try{
			boolean isautoApprove = wfautoApproveUtil.isAutoApprove(this, rst, nodeInfoCache,poppuplist,nextnodeattr);
			if(!isautoApprove){
			  //这里只设置提醒信息，不进行插入， 等事务commit之后，再抛出线程处理，防止提醒先到， 流程还未真正提交导致的没有权限问题
                poppupRemindInfoUtil.setPoppuplist(poppuplist);
                //poppupRemindInfoUtil.insertPoppupRemindInfo(poppuplist);
                //WFPathUtil wfutil = new WFPathUtil();
                //wfutil.getFixedThreadPool().execute(poppupRemindInfoUtil);
            }

            //System.out.println("人员提醒信息插入完成，耗时：" + (new Date().getTime() - d2.getTime())/1000 + "秒");
         }catch(Exception exception2){
             throw exception2;
         }
         
     }

    /**
     * 流程干预插入流转人至currentoperator
     * @param operators 操作者
     */
    public void setOperatorByremark5(ArrayList operators,ArrayList operatorsType,RecordSetTrans rst)throws Exception{  
        //记录日志
        WFAutoApproveUtils.processApproveLog(rst,this);
    	ArrayList poppuplist=new ArrayList();
		ArrayList operatorsWfNew = new ArrayList();
 	    ArrayList operatorsWfEnd = new ArrayList(); 	
        for(int n=0;n<nextnodeids.size();n++){
            nextnodeid=Util.getIntValue((String)nextnodeids.get(n),0);
            nextnodetype=Util.null2String((String)nextnodetypes.get(n));
            nextnodeattr=Util.getIntValue((String)nextnodeattrs.get(n),0);
 		  wfAgentCondition wfAgentCondition=new wfAgentCondition();
          //操作人更新结束
 			int showorder = 0;
 			String opertortype = "0";
             groupdetailid=0;
         try{
             for (int i = 0; i < operators.size(); i++) {
                 if(SignType==2){
                     if(!nextnodetype.equals("3")&&!nextnodetype.equals("3")&&i>0) break;
                     operatorgroup=-2;
                     groupdetailid=-2;
                     String tempStr="";
					 String tempStrType="";
                     for (int k = i+1; k< operators.size(); k++){
					    tempStr += operators.get(k) + ",";
					}
                     rst.executeProc("workflow_groupdetail_Insert",""+operatorgroup+flag+5+flag+operatorgroup+flag+0+flag+-1+flag+""+flag+""+flag+0+flag+2+flag+""+flag+""+flag+""+flag+""+flag+""+flag+""+flag+""+flag+""+flag+""+flag+""+flag+"");
                     if(rst.next()){
                         groupdetailid=rst.getInt(1);
                     }
                     rst.executeSql("delete from workflow_agentpersons where requestid="+requestid+" and  (groupdetailid in(select b.id from workflow_nodegroup a,workflow_groupdetail b where a.id=b.groupid and a.nodeid in("+nodeid+","+nextnodeid+")) or groupdetailid is null)");//groupdetailid is null主要为了兼容老的数据
                     rst.executeSql("insert into workflow_agentpersons values (" + requestid + ",'" + tempStr.trim() + "',"+groupdetailid+",'')");
                 }
                     showorder++; //xwj for td2104 on 20050802
 					int opertor = Util.getIntValue((String)operators.get(i));
						//默认为内部员工
 					if (operatorsType != null && i < operatorsType.size()) {
 						opertortype = (String)operatorsType.get(i);
 					} else {
 						opertortype = "0";
 					}
					
					
                     if(opertor>0){

 					//modify by xhheng @20050109 for 流程代理
 					//代理数据检索
                 		boolean isbeAgent=false;
    					String agenterId="";
    					// 代理逻辑处理
    					if (opertortype.equals("0")){	
    						agenterId=wfAgentCondition.getAgentid(""+workflowid,""+opertor,""+ requestid);
    						if(!agenterId.equals("")){
    							isbeAgent=true;
    						}
    					}
 					//当符合代理条件时添加代理人
 					String Procpara1="";

 					/*-------- xwj for td2104 on 20050802  begin --------- */
 					if(isOldOrNewFlagTrans(requestid,rst)){//老数据, 相对 td2104 之前
 						if(isbeAgent){
 												//设置被代理人已操作
 												Procpara = "" + requestid + flag + opertor + flag + operatorgroup + flag
 												+ workflowid + flag + workflowtype + flag + opertortype + flag + "2" + flag + -1 +
 												flag + -1 + flag + "0" + flag + -1+ flag +groupdetailid;
 												rst.executeProc("workflow_CurrentOperator_I", Procpara);
 												//设置代理人
 												Procpara1 = "" + requestid + flag + agenterId + flag + operatorgroup + flag
 												+ workflowid + flag + workflowtype + flag + opertortype + flag + "0" + flag + -1 +
 												flag + -1 + flag + "0" + flag + -1 + flag +groupdetailid;
 												rst.executeProc("workflow_CurrentOperator_I", Procpara1);
 											}else{
 												Procpara = "" + requestid + flag + opertor + flag + operatorgroup + flag
 												+ workflowid + flag + workflowtype + flag + opertortype + flag + "0" + flag + -1 +
 												flag + -1 + flag + "0" + flag + -1 + flag +groupdetailid;
 												rst.executeProc("workflow_CurrentOperator_I", Procpara);
 											}
 					}else{
 												if(isbeAgent){
 													//设置被代理人已操作
 													Procpara = "" + requestid + flag + opertor + flag + operatorgroup + flag
 													+ workflowid + flag + workflowtype + flag + opertortype + flag + "2" + flag + nextnodeid +
 													flag + agenterId + flag + "1" + flag + showorder+ flag +groupdetailid;
 													rst.executeProc("workflow_CurrentOperator_I", Procpara);
 													//设置代理人
 													Procpara1 = "" + requestid + flag + agenterId + flag + operatorgroup + flag
 													+ workflowid + flag + workflowtype + flag + opertortype + flag + "0" + flag + nextnodeid +
 													flag + opertor + flag + "2" + flag + showorder+ flag +groupdetailid;
 													rst.executeProc("workflow_CurrentOperator_I", Procpara1);
 												}else{
 													Procpara = "" + requestid + flag + opertor + flag + operatorgroup + flag
 													+ workflowid + flag + workflowtype + flag + opertortype + flag + "0" + flag + nextnodeid +
 													flag + -1 + flag + "0" + flag + showorder+ flag +groupdetailid;
 													rst.executeProc("workflow_CurrentOperator_I", Procpara);
 												}
 					}

 					//对代理人判断提醒

 					/*--xwj for td3450 20060111 begin--*/
 					Procpara = opertor + flag + opertortype + flag + requestid;

 					if (nextnodetype.equals("3")){
 						if(isbeAgent){
 							//update by fanggsh for TD4739  20060808 果归档前的最后一个操作者是本人的话，则从提醒中去掉。							
 							if(!operatorsWfEnd.contains(agenterId+"_"+opertortype)&&!(userid+"_"+usertype).equals(agenterId+"_"+opertortype)){
 								//poppupRemindInfoUtil.insertPoppupRemindInfo(Integer.parseInt(agenterId),1,opertortype,requestid,requestname,workflowid);
 								Map popmap=new HashMap();
 							    popmap.put("userid",""+Integer.parseInt(agenterId));
 							    popmap.put("type","1");
 							    popmap.put("logintype",""+opertortype);
 							    popmap.put("requestid",""+requestid);
 							    popmap.put("requestname",""+requestname);
 							    popmap.put("workflowid",""+workflowid);
 							   popmap.put("creater",""+creater);
 							    poppuplist.add(popmap);
 								operatorsWfEnd.add(agenterId+"_"+opertortype);
 							}
 						}else{
 							//update by fanggsh for TD4739  20060808 果归档前的最后一个操作者是本人的话，则从提醒中去掉。							
 							if(!operatorsWfEnd.contains(opertor+"_"+opertortype)&&!(userid+"_"+usertype).equals(opertor+"_"+opertortype)){
 								//poppupRemindInfoUtil.insertPoppupRemindInfo(opertor,1,opertortype,requestid,requestname,workflowid);
 								Map popmap=new HashMap();
 							    popmap.put("userid",""+opertor);
 							    popmap.put("type","1");
 							    popmap.put("logintype",""+opertortype);
 							    popmap.put("requestid",""+requestid);
 							    popmap.put("requestname",""+requestname);
 							    popmap.put("workflowid",""+workflowid);
 							   popmap.put("creater",""+creater);
 							    poppuplist.add(popmap);
 								operatorsWfEnd.add(opertor+"_"+opertortype);
 						  }
 						}
 					}
 					else{
 					   if(isbeAgent){
 							if(!operatorsWfNew.contains(agenterId+"_"+opertortype)){
 							//poppupRemindInfoUtil.insertPoppupRemindInfo(Integer.parseInt(agenterId),0,opertortype,requestid,requestname,workflowid);
 								 Map popmap=new HashMap();
 							    popmap.put("userid",""+Integer.parseInt(agenterId));
 							    popmap.put("type","0");
 							    popmap.put("logintype",""+opertortype);
 							    popmap.put("requestid",""+requestid);
 							    popmap.put("requestname",""+requestname);
 							    popmap.put("workflowid",""+workflowid);
 							   popmap.put("creater",""+creater);
 							    poppuplist.add(popmap);
 							operatorsWfNew.add(agenterId+"_"+opertortype);
 							}
 						}else{
 							if(!operatorsWfNew.contains(opertor+"_"+opertortype)){
 							//poppupRemindInfoUtil.insertPoppupRemindInfo(opertor,0,opertortype,requestid,requestname,workflowid);
 								Map popmap=new HashMap();
							    popmap.put("userid",""+opertor);
							    popmap.put("type","0");
							    popmap.put("logintype",""+opertortype);
							    popmap.put("requestid",""+requestid);
							    popmap.put("requestname",""+requestname);
							    popmap.put("workflowid",""+workflowid);
							    popmap.put("creater",""+creater);
							    poppuplist.add(popmap);
 							operatorsWfNew.add(opertor+"_"+opertortype);
 							}
 						}
 					}
                    if(SignType==1){
                        if(nextnodeattr==2){
                            operatorgroup--;
                        }else{
                            operatorgroup++;
                        }
                    }
                 
             }
             
            // poppupRemindInfoUtil.insertPoppupRemindInfo(poppuplist);
 			//操作人更新结束
             //更新当前节点表
            if(canflowtonextnode){
            	if(nextnodeattr!=2){//中间推荐退回到主干节点
            		rst.executeSql("delete from workflow_nownode where requestid="+requestid);//删掉所有的当前节点
            	}
            if(innodeids.equals("")||innodeids.equals("0")) innodeids=nodeid+"";
            rst.executeSql("delete from workflow_nownode where nownodeid in("+innodeids+") and requestid="+requestid);
            rst.executeSql("delete from workflow_nownode where nownodeid="+nodeid+" and requestid="+requestid);
            rst.executeSql("insert into workflow_nownode(requestid,nownodeid,nownodetype,nownodeattribute) values("+requestid+","+nextnodeid+","+nextnodetype+","+nextnodeattr+")");
            }
          }
        }catch(Exception exception){
    		throw exception;
		}
        }
		   try{
             poppupRemindInfoUtil.insertPoppupRemindInfo(poppuplist);
         }catch(Exception exception2){
             throw exception2;
         }

     }
    

	/**
	 * 获取当前操作人最新一次操作的日志编号
	 * 
	 * @param userId
	 * @param requestid
	 * @return
	 */
	private int getNewRequestLogId(int userId, int requestid) {
		int logid = 0;
		StringBuffer sb = new StringBuffer()
				.append(
						"SELECT MAX(LOGID) AS ID FROM WORKFLOW_REQUESTLOG WHERE OPERATOR=")
				.append(userId).append(" AND REQUESTID=").append(requestid);
		RecordSet rs = new RecordSet();
		rs.executeSql(sb.toString());
		if (rs.next()) {
			logid = rs.getInt("ID");
		}
		return logid;
	}

	/**
	 * 判断表是否有记录
	 * 
	 * @param workflowid
	 * @param requestid
	 * @param requestLogId
	 * @return
	 */
	private int loadRequestLogId(String workflowid, int requestid,
			int requestLogId) {
		int logtype = 0;
		StringBuffer sb = new StringBuffer().append("SELECT LOGTYPE FROM ")
				.append(OpinionFieldConstant.TABLE_NAME_PREFIX + workflowid).append(" WHERE REQUESTID=")
				.append(requestid).append(" AND REQUESTLOGID=").append(
						requestLogId);
		RecordSet rs = new RecordSet();
		rs.executeSql(sb.toString());
		if (rs.next()) {
			logtype = rs.getInt("LOGTYPE");
		}
		return logtype;
	}


	/**
	 * 获取日志类型
	 * 
	 * @return  日志类型
	 */
	private int getLogType() {
		int logtype = 0;
		if (src.equals("save")) {
			if (isremark == 1){
				logtype = 9; // 批注日志类型为9
			}else if(isremark == -1){
				logtype = 2;
			}else{
				logtype = 1; // 保存日志类型为1
			}
		} else if (src.equals("submit")) {
			if (isremark == 7){
				logtype = 9; // 批注日志类型为9
			}else{
			    logtype = 2;
            }
		}
		return logtype;
	}
	
	/**
	 * 判断创建的表是否存在。存在返回true.不存在返回false;
	 * 
	 * @param tablename 表名
	 * @return 存在返回true.不存在返回false;
	 */
	private boolean isExistTable(String tablename) {
		StringBuffer sb = new StringBuffer().append(
				"SELECT ID FROM WFOpinionTableNames ").append(" WHERE NAME='")
				.append(tablename).append("'");
		RecordSet rs = new RecordSet();
		rs.executeSql(sb.toString());
		if (rs.next()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 返回附件目录上传所选字段id
	 * @param workflowid 流程ID
	 * @return 字段id
	 */
	public String getUpLoadTypeForSelect(int workflowid){
		int type = 0;
		String selectedCateLog = "";
		String result = "";
		StringBuffer sb = new StringBuffer().append(
			"select * from workflow_base ").append(" WHERE id=")
			.append(workflowid);
		RecordSet rs = new RecordSet();
		rs.executeSql(sb.toString());
		if (rs.next()) {
			type = Util.getIntValue(rs.getString("catelogType"),0);   //解决升级过程后数据库中catelogType字段一般为NULL的情况 by ben

			selectedCateLog = Util.null2String(rs.getString("selectedCateLog"));
			result = selectedCateLog + "," + type;
		} 
		return result;
	}
	
	/**
	 * 返回附件上传目录的类型 0：固定目录 1：选择目录
	 * @param workflowid 流程ID
	 * @return 0：固定目录 1：选择目录
	 */
	public int getUpLoadType(int workflowid){
		int type = 0;
		StringBuffer sb = new StringBuffer().append(
			"select * from workflow_base ").append(" WHERE id=")
			.append(workflowid);
		RecordSet rs = new RecordSet();
		rs.executeSql(sb.toString());
		if (rs.next()) {
			type = rs.getInt("catelogType");
		} 
		return type;
	}
	
	/**
	 * 判断当为选择目录时附件上传目录是否设置完全
	 * @param workflowid 流程ID
	 * @return 是否设置完全
	 */
	public boolean hasUsedType(int workflowid) {
		boolean isuse = true;
		StringBuffer sb = new StringBuffer().append(
				"select a.* from workflow_selectitem a ")
				.append(" left join workflow_base b ")
				.append(" on a.fieldid = b.selectedCateLog ")
				.append(" where (a.docPath is null or a.docCategory is null ")
				.append(" or a.docPath='' or a.docCategory='') ")
			    .append(" and a.isAccordToSubCom='0'")
				.append(" and b.id=").append(workflowid);
		RecordSet rs = new RecordSet();
		rs.executeSql(sb.toString());
		if (rs.next()) {
			isuse = false;
		}
		return isuse;
	}
	
	/**
	 * added by cyril on 2008-06-26
	 * 根据请求获取主字段名称
	 * @return	字段名称的集合
	 * @throws Exception
	 */
	private List getFieldsName() {
		StringBuffer s = new StringBuffer();
		if (isbill == 1) {
			s.append("select t2.id, t2.fieldname, t2.fielddbtype, t2.fieldhtmltype, t2.type, t2.fieldlabel ");
			s.append("from  workflow_bill t1, workflow_billfield t2, workflow_form t3 ");
			s.append("where t1.id=t3.billformid and t2.billid = t1.id ");
			s.append("and t2.viewtype=0 and t3.requestid ="+requestid);
			//billFieldsSql = s.toString();
		} else {
			s.append("select t1.id, t1.fieldname, t1.fielddbtype, t1.fieldhtmltype, t1.type,");
			s.append("(select fieldlable from workflow_fieldlable t where t.langurageid = 7 and t.fieldid = t2.fieldid and t.formid = t2.formid) fieldNameCn,");
            s.append("(select fieldlable from workflow_fieldlable t where t.langurageid = 8 and t.fieldid = t2.fieldid and t.formid = t2.formid) fieldNameEn, ");
            s.append("(select fieldlable from workflow_fieldlable t where t.langurageid = 9 and t.fieldid = t2.fieldid and t.formid = t2.formid) fieldNameTw ");
			s.append("from workflow_formdict t1, workflow_formfield t2, workflow_form t3,workflow_fieldlable t4 ");
			s.append("where t1.id=t2.fieldid and t2.formid=t3.billformid and t4.langurageid = "+userlanguage+" and t4.fieldid = t2.fieldid and t4.formid = t2.formid and t3.requestid =" + requestid);
			//fieldsSql = s.toString();
		}
		executesuccess = rs.executeSql(s.toString());
		if (!executesuccess) {
			writeLog(s.toString());
			saveRequestLog("1");
			//return false;
		}
		List fields = new ArrayList();
		while(rs.next()) {
			Track t = new Track();
			t.setFieldName(rs.getString("fieldname"));		/*存字段名称*/
			t.setFieldType(rs.getString("type"));		/*存浏览按钮对应位置*/
			t.setFieldHtmlType(rs.getString("fieldhtmltype"));	/*存浏览按钮类型*/
			t.setFieldId(rs.getInt("id"));				/*将ID也取得*/
			//如果是表单则填以下二项
			if(isbill!=1) {
				t.setFieldNameCn(rs.getString("fieldNameCn"));	/*取字段名称*/
				t.setFieldNameEn(rs.getString("fieldNameEn"));	/*取字段名称*/
				t.setFieldNameTw(rs.getString("fieldNameTw"));	/*取字段名称*/
			}
			t.setNodeId(nodeid);	/*节点ID*/
			t.setRequestId(requestid);	/*请求ID*/
			t.setIsBill(isbill);	/*是否为表单*/
			//如果是单据则填LEABLE
			if(isbill==1)
				t.setFieldLableId(rs.getInt("fieldlabel"));/*单据对应的LEABLE*/
			t.setModifierIP(this.getIp());	/*IP地址*/
			t.setOptKind(src);	/*日志操作类型*/
			fields.add(t);
		}
		return fields;
	}
	
	/**
	 * 获取IP地址
	 * @return
	 */
	public String getIp() {
		String ip = "";
		if(isRequest){
			if (request != null)
				ip = Util.null2String(request.getRemoteAddr());
		}else{
			if (fu != null)
				ip = Util.null2String(fu.getRemoteAddr());
		}
		return ip;
	}
    /**
     * 发生流程提醒邮件
     * @param mailtoaddress   邮件地址
     * @param mailobject      邮件标题
     * @param mailrequestname 邮件内容
     */
	private void SendRemindMail(String mailtoaddress,String mailobject,String mailrequestname){
		try{
		    mailtoaddress = mailtoaddress.substring(0,mailtoaddress.length()-1);
		    SendMail sm = new SendMail();
		    SystemComInfo systemComInfo = new SystemComInfo();
		    String defmailserver = systemComInfo.getDefmailserver();
		    String defneedauth = systemComInfo.getDefneedauth();
		    String defmailuser = systemComInfo.getDefmailuser();
		    String defmailpassword = systemComInfo.getDefmailpassword();
			String defmailfrom=systemComInfo.getDefmailfrom();
		    sm.setMailServer(defmailserver);
		    if (defneedauth.equals("1")) {
		       sm.setNeedauthsend(true);
		       sm.setUsername(defmailuser);
		       sm.setPassword(defmailpassword);
		    } else{
		       sm.setNeedauthsend(false);
		}
           //mailobject邮件标题，mailrequestname邮件正文
		   sm.sendhtml(defmailfrom, mailtoaddress, null, null,mailobject , mailrequestname, 3, "3");
		}catch(Exception e){
		   writeLog(e);
		}
	}

	/**
	 *获得billid，billid为单据主表的id
	 *在这地方队billid重新赋值是因为，在operation页面有些billid是错误的
	 *这样导致获得节点操作人出错
	 */
	public void getBillId(){
		if(isbill == 1){
			rs.executeSql("select * from workflow_bill where id = " + formid);
			rs.next();
			String tablename = Util.null2String(rs.getString("tablename"));
			if(!tablename.equals("")){
				rs.executeSql("select * from " + tablename + " where requestid = " + requestid);
				if(rs.next()){
					billid = rs.getInt("id");
				}				
			}
		}				
	}

    /**
     * 获得是否为图形化
     * @return
     */
    public boolean getModeid(int curworkflowid,int curnodeid,int curformid,int curisbill){
        RecordSet currs=new RecordSet();
        String ismode="";
        int modeid=0;
        int showdes=0;
        currs.executeSql("select ismode,showdes from workflow_flownode where workflowid="+curworkflowid+" and nodeid="+curnodeid);
        if(currs.next()){
            ismode=Util.null2String(currs.getString("ismode"));
            showdes=Util.getIntValue(Util.null2String(currs.getString("showdes")),0);
        }
        if(ismode.equals("1") && showdes!=1){
            currs.executeSql("select id from workflow_nodemode where isprint='0' and workflowid="+curworkflowid+" and nodeid="+curnodeid);
            if(currs.next()){
                modeid=currs.getInt("id");
            }else{
                currs.executeSql("select id from workflow_formmode where isprint='0' and formid="+curformid+" and isbill='"+curisbill+"'");
                if(currs.next()){
                    modeid=currs.getInt("id");
                }
            }
        }
        return modeid>0?true:false;
    }

	public String getDocrowindex()
	{
		return docrowindex;
	}

	public void setDocrowindex(String docrowindex)
	{
		this.docrowindex = docrowindex;
	}

	private void updatePoppupRemindInfoThisJava(RecordSetTrans rst,int requestId )throws Exception{
        String remindusers="";
        String usertypes="";
        rst.executeSql("select wfreminduser,wfusertypes from workflow_currentoperator where (isremark='0' or isremark='7') and requestid="+requestid);
        if(rst.next()){
            remindusers=rst.getString("wfreminduser");
            usertypes=rst.getString("wfusertypes");
        }
        ArrayList wfremindusers=Util.TokenizerString(remindusers,",");
        ArrayList wfusertypes=Util.TokenizerString(usertypes,",");
        rst.executeSql("select userid,usertype from workflow_currentoperator where requestid="+requestid +" group by userid,usertype");
        while(rst.next()){
            String tempuserid=rst.getString("userid");
            String tempusertype=rst.getString("usertype");
            if(wfremindusers.indexOf(tempuserid)<0){
                wfremindusers.add(tempuserid);
                wfusertypes.add(tempusertype);
            }
        }
        for(int i=0;i<wfremindusers.size();i++){
            poppupRemindInfoUtil.updatePoppupRemindInfo(Util.getIntValue((String)wfremindusers.get(i)),10,(String)wfusertypes.get(i),requestid);
            poppupRemindInfoUtil.updatePoppupRemindInfo(Util.getIntValue((String)wfremindusers.get(i)),0,(String)wfusertypes.get(i),requestid);
        }		
	}
	
    private void updateworkflowcurrentoperator(RecordSetTrans rst,ArrayList submituserids,ArrayList submitusertypes, String finishndgpids)throws Exception{
        rst.executeSql("select distinct userid,usertype from workflow_currentoperator where isremark = '"+isremark+"' and requestid=" + requestid + "and nodeid=" + nodeid + " and groupid in(select distinct groupid from workflow_currentoperator where isremark = '"+isremark+"' and requestid=" + requestid + " and userid=" + userid + " and usertype=" + usertype + " and nodeid=" + nodeid + ")");
        while (rst.next()) {
            submituserids.add(rst.getString("userid"));
            submitusertypes.add(rst.getString("usertype"));
        }
        
        List tmpgroupidList=new ArrayList();              
        
        //需要更新已经完成操作组的其他人员
        if (!"".equals(finishndgpids)) {
            rst.executeSql("select distinct groupid from workflow_currentoperator where isremark = '"+isremark+"' and requestid=" + requestid + " and nodeid="+nodeid + " and (groupdetailid in (SELECT id FROM workflow_groupdetail WHERE groupid in (" + finishndgpids + ")) OR (userid=" + userid + " and usertype=" + usertype + "))");
        } else {
            rst.executeSql("select distinct groupid from workflow_currentoperator where isremark = '"+isremark+"' and requestid=" + requestid + " and userid=" + userid + " and usertype=" + usertype+" and nodeid="+nodeid);
        }
        
        while(rst.next()){
        	tmpgroupidList.add(""+Util.getIntValue(rst.getString(1), 0));
        }
        for(int i=0;i<tmpgroupidList.size();i++) {
            int tmpgroupid = Util.getIntValue((String)tmpgroupidList.get(i), 0);
			if(!"0".equals(needwfback)){
				//rst.executeProc("workflow_CurOpe_UpdatebySubmit", "" +userid +flag + requestid + flag + tmpgroupid+flag+nodeid+flag+isremark + flag + this.currentdate + flag + this.currenttime);
			    workflow_CurOpe_UpdatebySubmit(rst, requestid, nodeid, userid, currentdate, currenttime, tmpgroupid, isremark);
				rst.executeSql("update workflow_currentoperator set operatedate='"+currentdate+"',operatetime='"+currenttime+"' where (isremark = '5' or isremark='0' or isremark='1' or isremark='8' or isremark='9' or isremark='7') and requestid ="+requestid+" and userid="+userid+" and nodeid="+nodeid+" and groupid="+tmpgroupid);
				this.updateworkflowcurrenttakingopsoperator(rst, tmpgroupid);
			}else{
				//rst.executeProc("workflow_CurOpe_UbySubmitNB", "" +userid +flag + requestid + flag + tmpgroupid+flag+nodeid+flag+isremark + flag + this.currentdate + flag + this.currenttime);
			    workflow_CurOpe_UbySubmitNB(rst, requestid, nodeid, userid, currentdate, currenttime, tmpgroupid, isremark);
				rst.executeSql("update workflow_currentoperator set operatedate='"+currentdate+"',operatetime='"+currenttime+"' where (isremark = '5' or isremark='0' or isremark='1' or isremark='8' or isremark='9' or isremark='7') and requestid ="+requestid+" and userid="+userid+" and nodeid="+nodeid+" and groupid="+tmpgroupid);
				this.updateworkflowcurrenttakingopsoperator(rst,tmpgroupid);
			}
		}		
	}

	  private void updateworkflowcurrenttakingopsoperator(RecordSetTrans rst, int tmpgroupid)throws Exception{
		  //System.out.println("----8239+====sql=="+"update workflow_currentoperator set viewtype=-2 , isremark=2 ,  operatedate='"+currentdate+"',operatetime='"+currenttime+"' where (isremark = '1' and takisremark='2') and requestid ="+requestid+" and nodeid="+nodeid+" and groupid="+tmpgroupid);
		 rst.executeSql("update workflow_currentoperator set viewtype=-2 , isremark=2 ,  operatedate='"+currentdate+"',operatetime='"+currenttime+"' where (isremark = '1' and takisremark='2') and requestid ="+requestid+" and nodeid="+nodeid+" and groupid="+tmpgroupid);
	 
	  }

	  	  private void updateworkflowcurrenttakingopsoperator(RecordSetTrans rst, String nodeids, int tmpgroupid)throws Exception{
		  //System.out.println("----8239+====sql=="+"update workflow_currentoperator set viewtype=-2 , isremark=2 ,  operatedate='"+currentdate+"',operatetime='"+currenttime+"' where (isremark = '1' and takisremark='2') and requestid ="+requestid+" and nodeid in ("+nodeids+")  and groupid="+tmpgroupid);
		 rst.executeSql("update workflow_currentoperator set viewtype=-2 , isremark=2 ,  operatedate='"+currentdate+"',operatetime='"+currenttime+"' where (isremark = '1' and takisremark='2') and requestid ="+requestid+" and nodeid in ("+nodeids+") and groupid="+tmpgroupid);
	 
	  }

	  	  

	  	  
	  	/**
       * 从PROCEDURE workflow_CurOpe_UpdatebySubmit改造
       * @param rst
       * @param nodeid
       * @param tmpgroupid
       * @param isremark
       * @throws Exception
       */
      private static void workflow_CurOpe_UpdatebySubmit(RecordSetTrans rst, int requestid, int nodeid, int userid, String currentdate, String currenttime, int tmpgroupid, int isremark) throws Exception {
          
          String upSql = "update workflow_currentoperator set operatedate ='" + currentdate + "', operatetime='" + currenttime + "', viewtype=-2 "
                              + " where requestid=" + requestid
                              + " and userid=" + userid + " and isremark='" + isremark + "' " + " and groupid=" + tmpgroupid + " and nodeid=" + nodeid;
          rst.executeSql(upSql);
          
          upSql = "update workflow_currentoperator set isremark = '2' where requestid = " + requestid + " and isremark='" + isremark + "' and groupid=" + tmpgroupid + " and nodeid=" + nodeid;
          rst.executeSql(upSql);
          WFLinkInfo wflinkinfo = new WFLinkInfo(); 
          int nodeattr = wflinkinfo.getNodeAttribute(nodeid);
          
          if (nodeattr == 2) {
              upSql = " update workflow_currentoperator set isremark = '2' "
                              + " where requestid = " + requestid 
                              + " and (isremark = '5' or isremark = '8' or isremark = '9') and userid = " + userid + " and nodeid =" + nodeid;
          } else {
              upSql = "update workflow_currentoperator set isremark = '2' "
                              + "where requestid = " + requestid + " and (isremark = '5' or isremark = '8' or isremark = '9') and userid=" + userid;
          }
          rst.executeSql(upSql);
      }
      
      /**
       * PROCEDURE workflow_CurOpe_UbySubmitNB 改造而来
       * @param rst
       * @param nodeid
       * @param tmpgroupid
       * @param isremark
       * @throws Exception
       */
      private static void workflow_CurOpe_UbySubmitNB(RecordSetTrans rst, int requestid, int nodeid, int userid, String currentdate, String currenttime, int tmpgroupid, int isremark) throws Exception {
          
          String upSql = "update workflow_currentoperator set operatedate ='" + currentdate + "', operatetime ='" + currenttime + "', viewtype=-2"
                              + " where requestid=" + requestid
                              + " and userid = " + userid
                              + " and isremark = '" + isremark + "' "
                              + " and groupid = " + tmpgroupid
                              + " and nodeid = " + nodeid;
          rst.executeSql(upSql);
          
          upSql = "update workflow_currentoperator set isremark = '2', needwfback = '0'"
                              + " where requestid = " + requestid
                              + " and isremark = '" + isremark + "' "
                              + " and groupid = " + tmpgroupid
                              + " and nodeid = " +  nodeid;
          rst.executeSql(upSql);
          
          WFLinkInfo wflinkinfo = new WFLinkInfo(); 
          int nodeattr = wflinkinfo.getNodeAttribute(nodeid);
          
          if (nodeattr == 2) {
              upSql = " update workflow_currentoperator set isremark = '2', needwfback = '0'"
                              + " where requestid = " + requestid
                              + " and (isremark = '5' or isremark = '8' or isremark = '9') "
                              + " and userid = " + userid
                              + " and nodeid = " + nodeid;
          } else {
              upSql = " update workflow_currentoperator set isremark = '2', needwfback = '0'"
                              + " where requestid = " + requestid
                              + " and (isremark = '5' or isremark = '8' or isremark = '9')"
                              + " and userid = " + userid;
          }
          rst.executeSql(upSql);
      }
      
      /**
       * PROCEDURE workflow_CurOpe_UbyForward 改造而来
       * @param rst
       * @throws Exception
       */
      private static void workflow_CurOpe_UbyForward(RecordSetTrans rst, int requestid, int userid, int usertype, String currentdate, String currenttime) throws Exception {
          String upSql = "update workflow_currentoperator set isremark = 2, operatedate = '" + currentdate + "', operatetime = '" + currenttime + "'"
                              + " where requestid = " + requestid + " and userid =" + userid 
                              + " and usertype = " + usertype + " and (isremark = 1 or isremark = 8 or isremark = 9)";
          rst.executeSql(upSql);
      }
      
      /**
       * PROCEDURE workflow_CurOpe_UbyForwardNB 改造而来
       * @param rst
       * @throws Exception
       */
      private static void workflow_CurOpe_UbyForwardNB(RecordSetTrans rst, int requestid, int userid, int usertype, String currentdate, String currenttime) throws Exception {
          String upSql = " update workflow_currentoperator set isremark='2', operatedate = '" + currentdate + "', operatetime = '" + currenttime + "', needwfback  = '0'"
                              + " where requestid = " + requestid
                              + " and userid = " + userid
                              + " and usertype = " + usertype
                              + " and (isremark = '1' or isremark = '8' or isremark = '9')";
          
          rst.executeSql(upSql);
      }
	public void setRejectToNodeid(int rejectToNodeid) {
		RejectToNodeid = rejectToNodeid;
	}

	public void setRejectToType(int RejectToType) {
		this.RejectToType = RejectToType;
	}
	
	public void setSubmitToNodeid(int submitToNodeid) {
		this.SubmitToNodeid = submitToNodeid;
	}
	
  public String getClientType() {
    return clientType;
  }

  public void setClientType(String clientType) {
    this.clientType = clientType;
  }

  public int getHandWrittenSign() {
    return handWrittenSign;
  }

  public void setHandWrittenSign(int handWrittenSign) {
    this.handWrittenSign = handWrittenSign;
  }

  public int getSpeechAttachment() {
    return speechAttachment;
  }

  public void setSpeechAttachment(int speechAttachment) {
    this.speechAttachment = speechAttachment;
  }		
  public int getEnableIntervenor() {
		return enableIntervenor;
	}

	public void setEnableIntervenor(int enableIntervenor) {
		this.enableIntervenor = enableIntervenor;
	}

   //add by liaodong for qc53580 in 20130916 start 
   public String fillFullNull(String obj){
	   if("".equals(obj.trim())){
		   return "";
	   }else{
		   return obj; 
	   }
	  
  }

  public String getSignatureAppendfix() {
  	return signatureAppendfix;
  }
  
  public void setSignatureAppendfix(String signatureAppendfix) {
  	this.signatureAppendfix = signatureAppendfix;
  }
  public String getChatsType() {
		return chatsType;
	}

	public void setChatsType(String chatsType) {
		 if(chatsType==null || chatsType.equals("")){
			 chatsType="0";
		    }
		this.chatsType = chatsType;
	}
	
	private String[] parseArgument(String s, char separator)
    {
		String[] args;
        int i = 0;
        int j = 0;
        if(s.trim().equals("")){
          args=new String[0];
          return args;
        }
        for(j = 0; j < s.length(); j++)
            if(s.charAt(j) == separator)
                i++;

        args = new String[i + 1];
        j = 0;
        i = 0;
        while((j = s.indexOf(separator)) != -1)
        {
            args[i++] = s.substring(0, j);
            s = s.substring(j + 1);
        }
        args[i] = s;
        return args;
    }
	
	/**
	 * 代替存储过程保存签字意见信息,新增接收人id,clob类型
	 * para rstlogpara flag
	 * talbe workflow_requestlog
	 * ***/
	public String execRequestlog(String rstlogpara , RecordSet rslog , char flag , String remarknew){
		String returnStr = "";
		if(!"".equals(rstlogpara)){
			//String [] arraypara = Util.TokenizerString2(rstlogpara, String.valueOf(flag));
			String [] arraypara = rstlogpara.split(String.valueOf(flag));
			if(arraypara.length < 22) {
				arraypara = this.parseArgument(rstlogpara, flag);
			}
			//System.out.println("arraypara.length = "+arraypara.length);
			String requestidlog = arraypara[0];
			String workflowidlog = arraypara[1];
			String nodeidlog = arraypara[2];
			String logtypelog = arraypara[3];
			String logdatelog = arraypara[4];
			String logtimelog = arraypara[5];
			String useridlog = arraypara[6];
			//String remarklog = arraypara[7];
			String clientiplog = arraypara[7];
			String usertypelog = arraypara[8];
			String nextnodeidlog = arraypara[9];
			String operate1log = arraypara[10];
			String agentorbyagentid1log = arraypara[11];
			String agenttype1log = arraypara[12];
			String showorder1log = arraypara[13];
			String annexdocidslog = arraypara[14];
			String requestLogIdlog = arraypara[15];
			String signdocidslog = arraypara[16];
			String signworkflowidslog = arraypara[17];
			String clientTypelog = arraypara[18];
			String speechAttachmentlog = arraypara[19];
			String handWrittenSignlog = arraypara[20];
			
			//如果receivedPersonids为空，split的length就会少1，所以可能出现指针越界
			String receivedPersonids = "";
			String remarkLocation = "";
			if(arraypara.length == 23){
				receivedPersonids = arraypara[21];
				remarkLocation = arraypara[22];
			}else if(arraypara.length == 22){
			    receivedPersonids = arraypara[21];
			}
			
			int count2 = 0;
			Date date = new Date();
			SimpleDateFormat sdfdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			String currentStr = sdfdate.format(date);
			String currentdate = "";
			String currenttime = "";
			if (logdatelog.equals("")){
				currentdate = currentStr.substring(0,10).trim();
				currenttime = currentStr.substring(10).trim();
			}else{
				currentdate = logdatelog;
				currenttime = logtimelog;
			}
			returnStr = currentdate+"~~current~~"+currenttime;
			int operatorDept1 = Util.getIntValue(resourceComInfo.getDepartmentID(""+useridlog),0);
			ConnStatement logstatement = null;
			try {
				if("0".equals(usertypelog) || "1".equals(usertypelog)){
					if("1".equals(logtypelog)){
						String logsql = " SELECT 1 FROM workflow_requestlog WHERE requestid="+requestidlog +
										" AND nodeid="+nodeidlog+" AND logtype='"+logtypelog+"' AND OPERATOR = "+useridlog +
										" AND operatortype = "+usertypelog;
						rslog.executeSql(logsql);
						int count1 =  rslog.getCounts();
						if(count1 > 0){
							String updatelogsql = " UPDATE workflow_requestlog SET operatedate= ?,operatetime= ?," + 
													" remark= ?, clientip= ?, destnodeid= ?, annexdocids= ?, " +
													" requestLogId= ?, signdocids= ?, signworkflowids= ?, isMobile= ?," +
													" SpeechAttachment= ?, HandWrittenSign= ?, remarkLocation=?  where requestid="+requestidlog + 
													" AND nodeid="+nodeidlog+" AND logtype='"+logtypelog+"' AND OPERATOR = "+useridlog +
													" AND operatortype = "+usertypelog;
							logstatement = new ConnStatement();
							logstatement.setStatementSql(updatelogsql);
							logstatement.setString(1, currentdate);
							logstatement.setString(2, currenttime);
							logstatement.setString(3, remarknew);
							logstatement.setString(4, clientiplog);
							logstatement.setInt(5, Integer.parseInt(nodeidlog));
							logstatement.setString(6, annexdocidslog);
							logstatement.setInt(7, Util.getIntValue(requestLogIdlog,0));
							logstatement.setString(8, signdocidslog);
							logstatement.setString(9, signworkflowidslog);
							logstatement.setString(10, clientTypelog);
							logstatement.setInt(11, Util.getIntValue(speechAttachmentlog,0));
							logstatement.setInt(12, Util.getIntValue(handWrittenSignlog,0));
							logstatement.setString(13, remarkLocation);
							logstatement.executeUpdate();
						}else{
							String insertlogsql = " INSERT INTO workflow_requestlog (requestid,workflowid,nodeid,logtype, operatedate, " +
													" operatetime,OPERATOR, remark,clientip,operatortype,destnodeid,receivedPersons, " +
													" agentorbyagentid,agenttype,showorder,annexdocids,requestLogId,operatorDept, " + 
													" signdocids,signworkflowids,isMobile,HandWrittenSign,SpeechAttachment,receivedPersonids,remarkLocation) " +
													" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
							logstatement = new ConnStatement();
							logstatement.setStatementSql(insertlogsql);
							logstatement.setInt(1, Integer.parseInt(requestidlog));
							logstatement.setInt(2, Integer.parseInt(workflowidlog));
							logstatement.setInt(3, Integer.parseInt(nodeidlog));
							logstatement.setString(4, logtypelog);
							logstatement.setString(5, currentdate);
							logstatement.setString(6, currenttime);
							logstatement.setInt(7, Integer.parseInt(useridlog));
							logstatement.setString(8, remarknew);
							logstatement.setString(9, clientiplog);
							logstatement.setInt(10, Integer.parseInt(usertypelog));
							logstatement.setInt(11, Integer.parseInt(nextnodeidlog));
							logstatement.setString(12, operate1log);
							logstatement.setInt(13, Integer.parseInt(agentorbyagentid1log));
							logstatement.setString(14, agenttype1log);
							logstatement.setInt(15, Integer.parseInt(showorder1log));
							logstatement.setString(16, annexdocidslog);
							logstatement.setInt(17, Util.getIntValue(requestLogIdlog,0));
							logstatement.setString(18, String.valueOf(operatorDept1));
							logstatement.setString(19, signdocidslog);
							logstatement.setString(20, signworkflowidslog);
							logstatement.setString(21, clientTypelog);
							logstatement.setInt(22, Util.getIntValue(handWrittenSignlog,0));
							logstatement.setInt(23, Util.getIntValue(speechAttachmentlog,0));
							logstatement.setString(24, receivedPersonids);
							logstatement.setString(25, remarkLocation);
							logstatement.executeUpdate();
						}
					}else{
						String deletelogsql = " DELETE workflow_requestlog WHERE requestid="+requestidlog+" AND nodeid="+nodeidlog +
												" AND (logtype='1') AND OPERATOR = "+useridlog+" AND operatortype = "+usertypelog;
						rslog.executeSql(deletelogsql);
						String insertlog = " INSERT INTO workflow_requestlog (requestid,workflowid,nodeid,logtype, operatedate, " +
								" operatetime,OPERATOR, remark,clientip,operatortype,destnodeid,receivedPersons, " +
								" agentorbyagentid,agenttype,showorder,annexdocids,requestLogId,operatorDept, " + 
								" signdocids,signworkflowids,isMobile,HandWrittenSign,SpeechAttachment,receivedPersonids,remarkLocation) " +
								" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
						logstatement = new ConnStatement();
						logstatement.setStatementSql(insertlog);
						logstatement.setInt(1, Integer.parseInt(requestidlog));
						logstatement.setInt(2, Integer.parseInt(workflowidlog));
						logstatement.setInt(3, Integer.parseInt(nodeidlog));
						logstatement.setString(4, logtypelog);
						logstatement.setString(5, currentdate);
						logstatement.setString(6, currenttime);
						logstatement.setInt(7, Integer.parseInt(useridlog));
						logstatement.setString(8, remarknew);
						logstatement.setString(9, clientiplog);
						logstatement.setInt(10, Integer.parseInt(usertypelog));
						logstatement.setInt(11, Integer.parseInt(nextnodeidlog));
						logstatement.setString(12, operate1log);
						logstatement.setInt(13, Integer.parseInt(agentorbyagentid1log));
						logstatement.setString(14, agenttype1log);
						logstatement.setInt(15, Integer.parseInt(showorder1log));
						logstatement.setString(16, annexdocidslog);
						logstatement.setInt(17, Util.getIntValue(requestLogIdlog,0));
						logstatement.setString(18, String.valueOf(operatorDept1));
						logstatement.setString(19, signdocidslog);
						logstatement.setString(20, signworkflowidslog);
						logstatement.setString(21, clientTypelog);
						logstatement.setInt(22, Util.getIntValue(handWrittenSignlog,0));
						logstatement.setInt(23, Util.getIntValue(speechAttachmentlog, 0));
						logstatement.setString(24, receivedPersonids);
						logstatement.setString(25, remarkLocation);
						logstatement.executeUpdate();
					}
				}
				
				/**
				 * 处理签字意见的权限,督办(s)所有人都有权限，其他的都只给日志的操作人赋予权限
				 */
				if ("s".equals(logtypelog) || "9".equals(logtypelog)
						|| "2".equals(logtypelog) || "3".equals(logtypelog)
						|| "4".equals(logtypelog) || "5".equals(logtypelog)
						|| "6".equals(logtypelog) || "e".equals(logtypelog)
						|| "0".equals(logtypelog) || "t".equals(logtypelog)
						|| "i".equals(logtypelog) || "7".equals(logtypelog)
						|| "h".equals(logtypelog) || "a".equals(logtypelog)
						|| "b".equals(logtypelog) || "j".equals(logtypelog)) {
					
					String rsql = "select currentnodeid,currentnodetype from workflow_requestbase where requestid = " + requestidlog;
					rslog.executeSql(rsql);
					String curnodeid = "";
					String curnodetype = "";
					if(rslog.next()){
						curnodeid = rslog.getString("currentnodeid");
						curnodetype = rslog.getString("currentnodetype");
					}
					int nodeattr = wflinkinfo.getNodeAttribute(Util.getIntValue(curnodeid,-1));
			        Set<String> branchNodeSet = new HashSet<String>();
			        if(nodeattr == 2){   //分支中间节点
			        	String branchnodes = "";
			        	branchnodes = wflinkinfo.getNowNodeids(Util.getIntValue(requestidlog,-1));
			        	if(!"".equals(branchnodes)){
			        		String [] strs = branchnodes.split(",");
			        		for(int k = 0; k < strs.length; k++){
			        			String nodestr = strs[k];
			        			if(!"-1".equals(nodestr)){
			        				branchNodeSet.add(nodestr);
			        			}
			        		}
			        	}
			        }
					
					//流程的非归档节点，且插入日志的节点仍然是流程的当前节点，则需要日志权限控制
					if(!"3".equals(curnodetype) && (curnodeid.equals(nodeidlog) || branchNodeSet.contains(nodeidlog))){  
						logstatement.close();    //先关闭，让插入log的操作先提交,否则会导致下面的查询语句，查询不到logid,for oracle
						String logsql = " select logid from workflow_requestlog where workflowid = "
								+ workflowidlog
								+ " and nodeid = "
								+ nodeidlog
								+ " and logtype = '"
								+ logtypelog
								+ "' and requestid = "
								+ requestidlog
								+ " and operatedate = '"
								+ currentdate
								+ "' and operatetime = '"
								+ currenttime
								+ "' and operator = " + useridlog;

						rslog.executeSql(logsql);
						int logid = -1;
						if (rslog.next()) {
							logid = rslog.getInt("logid");
						}

						int loguserid = Util.getIntValue(useridlog, 0);
						if ("s".equals(logtypelog)) { // 督办所有人有权限
							loguserid = -1;
						}

						String rightSql = "insert into workflow_logviewusers (logid,userid) values (? , ?)";
						logstatement.close();
						logstatement = new ConnStatement();
						logstatement.setStatementSql(rightSql);
						logstatement.setInt(1, logid);
						logstatement.setInt(2, loguserid);
						logstatement.executeUpdate();
					}
				}
			} catch (Exception e) {
				writeLog(e);
			} finally {
				if(logstatement!=null) logstatement.close();
			}
		}
		return returnStr;
	}
	
	/**
	 * 资源权限过滤器
	 * @param type 1：文档， 2：客户， 3：项目， 4：资产
	 * @param resstr
	 * @return
	 */
	private String resourceAuthorityFilter(String fieldhtmltype, String fieldtype, String resstr) {
	    String result = resstr;
	    
	    if ("".equals(resstr)) {
	        return result;
	    }
	    
	    if ("3".equals(fieldhtmltype)) {
	        if (fieldtype.equals("9") || fieldtype.equals("37")) {  // 文档字段
	           // result = new DocShareUtil().docRightFilter(user, resstr);
	            result = new DocShareUtil().docRightFilterForRequestId(user, resstr, requestid);
	        } else if (fieldtype.equals("8")|| fieldtype.equals("135")) { //项目
	            result = new weaver.cpt.util.CommonShareManager().getPrjFilterids(resstr, user);
	        } else if (fieldtype.equals("23")) { //资产
	            result = new weaver.cpt.util.CommonShareManager().getCptFilterids(resstr, user);
	        } else if (fieldtype.equals("7") || fieldtype.equals("18")) { //客户
	            result = weaver.crm.customer.CustomerShareUtil.customerRightFilter(String.valueOf(user.getUID()), resstr);
	        }
	    }
	    return result;
	}
	
	public static String filterClause(String causes){
		String returnval="";
		Map<String,String> tempMap = new HashMap<String,String>();
		StringTokenizer tokens = new StringTokenizer(causes,",");
		while(tokens.hasMoreElements()){
			String setKV = tokens.nextToken();
			if(null == tempMap.get(setKV))
				returnval += setKV+",";
			tempMap.put(setKV,setKV);
		}
		return returnval.substring(0,returnval.length()-1);
	}
	
	/**
	 * 替换多行文本编辑字段从word拷贝带过来的类似<?xml:namespace prefix="o" ns="urn:schemas-microsoft-com:office:office"><o:p></o:p></?xml:namespace>标记
	 */
	private String rePlaceWordMark(String content){
		String text = content;
		String mark = "xml:namespace";
		int xmlIdx_b = text.indexOf(mark);
		if(xmlIdx_b > -1){
			String str_f = text.substring(0, xmlIdx_b);
			str_f = str_f.substring(0, str_f.lastIndexOf("<"));
			String str_e = text.substring(xmlIdx_b+mark.length());
			int xmlIdx_e = str_e.indexOf(mark);
			if(xmlIdx_e > -1){
				str_e = str_e.substring(xmlIdx_e+mark.length());
				str_e = str_e.substring(str_e.indexOf(">")+1);
				text = str_f + str_e;
			}
		}
		return text;
	}
	
	/**
	 * 流转异常处理，由用户指定操作者，PC端信息封装
	 */
	public void createEh_operatorMap_pc(){
		String eh_setoperator = "";
		if(isRequest && request != null)
			eh_setoperator = Util.null2String(request.getParameter("eh_setoperator"));
		else if(fu != null)
			eh_setoperator = Util.null2String(fu.getParameter("eh_setoperator"));
		eh_operatorMap.put("eh_setoperator", eh_setoperator);
		String eh_relationship = "";
		if(isRequest && request != null)
			eh_relationship = Util.null2String(request.getParameter("eh_relationship"));
		else if(fu != null)
			eh_relationship = Util.null2String(fu.getParameter("eh_relationship"));
		eh_operatorMap.put("eh_relationship", eh_relationship);
		String eh_operators = "";
		if(isRequest && request != null)
			eh_operators = Util.null2String(request.getParameter("eh_operators"));
		else if(fu != null)
			eh_operators = Util.null2String(fu.getParameter("eh_operators"));
		eh_operatorMap.put("eh_operators", eh_operators);
	}
	
	/**
	 * 判断当前自己操作的节点是否已意见征询
	 * @param requestid
	 * @param userid
	 * @return
	 */
	public boolean checkNodeOperatorComment(int requestid,int userid,int nodeid){
		return false;	//QC204474,不可以此条件阻止流程提交
	}
	
	/**
	 * 流转异常处理，由用户指定操作者，Mobile端信息封装
	 */
	public void createEh_operatorMap_mobile(Map<String,Object> _eh_operatorMap){
		this.eh_operatorMap = _eh_operatorMap;
	}

    public String getIsFirstSubmit() {
        return isFirstSubmit;
    }

    public void setIsFirstSubmit(String isFirstSubmit) {
        this.isFirstSubmit = isFirstSubmit;
    }
    
    public String getRemarkLocation() {
        return remarkLocation;
    }

    public void setRemarkLocation(String remarkLocation) {
        this.remarkLocation = remarkLocation;
    }
    
    /**
     * 清除session中的消息内容
     */
    public void removeErrorMsg(){
        if(!isRequest){
            request=fu.getRequest();
        }
        if(request != null){
            HttpSession session = request.getSession(false);
            session.removeAttribute("errormsg_"+user.getUID()+"_"+requestid);
            session.removeAttribute("errormsgid_"+user.getUID()+"_"+requestid);
        }
    }
    /**
     * 重写插入节点操作者的方法(被代理人，未操作的情况下不将islasttimes设定为0)
     * @param rst
     * @param opertor
     * @param operatorgroup
     * @param opertortype
     * @param agenterId
     * @param showorder
     * @return
     */
    private boolean wfCurrentOperatorAgent(RecordSetTrans rst,
            String opertor,
            String operatorgroup,
            String opertortype,
            String agenterId,
            int showorder){
        boolean returnFlag = false;

        try {
            if(requestid > 0 &&  !"".equals(opertor)){
                //判断当前操作者的状态是否为被代理人，是否未操作
                String execSql = " select * from workflow_currentoperator where requestid = ? ";
                execSql += " AND userid = ? ";
                execSql += " AND usertype = ? ";
                execSql += " AND nodeid = ? ";
                execSql += " AND isremark = 0 ";
                execSql += " AND agenttype = 2 ";
                rst.executeQuery(execSql,requestid,opertor,opertortype,nextnodeid);
                if(rst.next()){
                    String workflowtype2 = "0";
                    //如果workflowtype为空，则从workflow_base中获取
                    if(workflowtype == null || workflowtype.equals("")){
                        execSql = "SELECT workflowtype FROM workflow_base WHERE id = ? ";
                        rst.executeQuery(execSql,workflowid);
                        if(rst.next()){
                            workflowtype2 = rst.getString("workflowtype");
                        }
                    }else{
                        workflowtype2 = workflowtype;
                    }
                    //拼接SQL
                    execSql = "";
                    execSql += "        INSERT INTO workflow_currentoperator  ";
                    execSql += "          (requestid,    ";
                    execSql += "           userid,   ";
                    execSql += "           groupid,  ";
                    execSql += "           workflowid,   ";
                    execSql += "           workflowtype,     ";
                    execSql += "           usertype,     ";
                    execSql += "           isremark,     ";
                    execSql += "           nodeid,   ";
                    execSql += "           agentorbyagentid,     ";
                    execSql += "           agenttype,    ";
                    execSql += "           showorder,    ";
                    execSql += "           receivedate,  ";
                    execSql += "           receivetime,  ";
                    execSql += "           viewtype,     ";
                    execSql += "           iscomplete,   ";
                    execSql += "           islasttimes,  ";
                    execSql += "           groupdetailid,    ";
                    execSql += "           preisremark,  ";
                    execSql += "           needwfback)   ";
                    execSql += "        VALUES   ";
                    execSql += "          (" + requestid + ",   ";
                    execSql += "           " + opertor + ",  ";
                    execSql += "           " + operatorgroup + ",     ";
                    execSql += "           " + workflowid + ",  ";
                    execSql += "           " + workflowtype2 + ",    ";
                    execSql += "           " + opertortype + ",    ";
                    execSql += "           2,    ";
                    execSql += "           " + nextnodeid + ",   ";
                    execSql += "           " + agenterId + ",     ";
                    execSql += "           1,    ";
                    execSql += "           " + showorder + ",    ";
                    execSql += "           '" + this.currentdate + "',  ";
                    execSql += "           '" + this.currenttime + "',  ";
                    execSql += "           0,    ";
                    execSql += "           0,    ";
                    execSql += "           0,    ";
                    execSql += "           " + groupdetailid + ",  ";
                    execSql += "           2,    ";
                    execSql += "           '1')     ";
                    rst.executeSql(execSql);
                    returnFlag = true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return returnFlag;
    }

    public void setMakeOperateLog(boolean isMakeOperateLog) {
        this.isMakeOperateLog = isMakeOperateLog;
    }
	
	 public void CheckUserIsLasttimes(int requestid,int currentnodeid,User user){
		RecordSet rs = new RecordSet();
		try{
			String sql = "select id from workflow_currentoperator where requestid="+requestid+" and nodeid="+currentnodeid+" and userid="+user.getUID()+" and isremark=2 and islasttimes=1 and preisremark=0 " ;
			System.out.println(sql);
			rs.executeSql(sql);
			if(rs.next()){
				int tempid = rs.getInt("id");
				int otempid = 0 ;
				sql = "select c.id from workflow_currentoperator c where c.requestid="+requestid+" and c.nodeid!="+currentnodeid+" and c.nodeid in (select nownodeid from workflow_nownode where requestid=c.requestid) and userid="+user.getUID()+" and c.isremark=0 and c.islasttimes=0 and c.preisremark=0 order by c.id";
				rs.executeSql(sql);
				if(rs.next()){
					otempid = rs.getInt("id");
				}
				System.out.println("tempid = "+tempid+" otempid="+otempid);
				if(otempid>0){
					rs.executeSql("update workflow_currentoperator set islasttimes=0 where requestid="+requestid+" and id="+tempid);
					rs.executeSql("update workflow_currentoperator set islasttimes=1 where requestid="+requestid+" and id="+otempid);
				}
			}
		}catch(Exception e){
			
		}
	}
	
    public Map<String, Integer> getNewAddDetailRowPerInfo() {
        return newAddDetailRowPerInfo;
    }
    
    private static void init() {
        // 增加配置文件：workflowFlowLogicConfig.properties,
        // 在此文件中配置是否执行老的逻辑（同一操作组同批次之间的关系为会签），此配置文件供技术支持同事修改
        // 注意：老逻辑不再维护 
        try {
            isExeOldFlowlogic = Util.getIntValue((new BaseBean()).getPropValue("workflowFlowLogicConfig", "oldlogic"), 0) == 1;
        } catch (Exception e) {
        }
    }
    
    public void deleteToDo(String requestid) {
        writeLog("===删除流程传输统一代办库---start " + requestid + "===");
        String syscode = Util.null2String(Prop.getPropValue("FZJT", "syscode"));
        Map dataMap1 = buildDataMap(
          syscode, 
          requestid);
        try
        {
          AnyType2AnyTypeMapEntry[] dataArray1 = OfsClient.buildDataArray(dataMap1);
          AnyType2AnyTypeMapEntry[] resultArray1 = OfsClient.getClient().deleteRequestInfoByMap(dataArray1);
          OfsClient.printResultArray(resultArray1);
        }
        catch (Exception e) {
          writeLog(e);
        }
        writeLog("===删除流程传输统一代办库---end " + requestid + "==="); 
     }

      public Map<String, String> buildDataMap(String syscode, String flowid) {
        Map dataMap = new HashMap();
        dataMap.put("syscode", syscode);
        dataMap.put("flowid", flowid);
        return dataMap; 
       } 
}