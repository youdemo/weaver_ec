package weaver.workflow.request;

import cn.com.weaver.ofs.webservices.AnyType2AnyTypeMapEntry;
import cn.com.weaver.ofs.webservices.OfsTodoDataWebServicePortTypeProxy;
import java.io.PrintStream;
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.codehaus.xfire.aegis.type.java5.IgnoreProperty;
import org.json.JSONException;
import org.json.JSONObject;
import weaver.WorkPlan.CreateWorkplanByWorkflow;
import weaver.common.StringUtil;
import weaver.conn.ConnStatement;
import weaver.conn.RecordSet;
import weaver.conn.RecordSetTrans;
import weaver.cpt.util.CommonShareManager;
import weaver.crm.Maint.CustomerInfoComInfo;
import weaver.crm.customer.CustomerShareUtil;
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
import weaver.system.SystemComInfo;
import weaver.systeminfo.SystemEnv;
import weaver.workflow.bean.Track;
import weaver.workflow.bean.Trackdetail;
import weaver.workflow.exceldesign.ExcelLayoutManager;
import weaver.workflow.field.FieldComInfo;
import weaver.workflow.form.FormFieldlabelMainManager;
import weaver.workflow.mode.FieldInfo;
import weaver.workflow.msg.PoppupRemindInfoUtil;
import weaver.workflow.report.RequestDeleteLog;
import weaver.workflow.workflow.WFModeNodeFieldManager;
import weaver.workflow.workflow.WFNodeDtlFieldManager;
import weaver.workflow.workflow.WFSubDataAggregation;
import weaver.workflow.workflow.WfFunctionManageUtil;
import weaver.workflow.workflow.WorkflowAllComInfo;
import weaver.workflow.workflow.WorkflowRequestComInfo;
import weaver.worktask.request.RequestCreateByWF;

public class aaa extends BaseBean
{
  private static RequestIdUpdate requestIdUpdate = new RequestIdUpdate();
  private RecordSet rs;
  private RecordSet rs1;
  private RecordSet rs2;
  private RecordSet rs3;
  private RecordSet rs4;
  private HttpServletRequest request;
  private FileUpload fu;
  private boolean isRequest = true;
  private User user;
  private String isMultiDoc = "";

  private String src = "";
  private String iscreate = "";
  private int requestid = 0;
  private int workflowid = 0;
  private String workflowtype = "";
  private int isremark = 0;
  private int formid = 0;
  private int isbill = 0;
  private int billid = 0;
  private int nodeid = 0;
  private String nodetype = "";
  private String requestname = "";
  private String requestmark = "";
  private String requestlevel = "";
  private String remark = "";
  private String clientType = "";
  private int handWrittenSign;
  private int speechAttachment;
  private String remarkLocation;
  private String signatureAppendfix;
  private String signdocids = "";
  private String signworkflowids = "";
  private String messageType = "";
  private String chatsType = "";
  private String needwfback = "";

  private int nextnodeid = 0;
  private String nextnodetype = "";
  private int lastnodeid = 0;
  private String lastnodetype = "";
  private String status = "";
  private int passedgroups = 0;
  private int totalgroups = 0;
  private int creater = 0;
  private int creatertype = 0;
  private String createdate = "";
  private String createtime = "";
  private int lastoperator = 0;
  private int lastoperatortype = 0;
  private String lastoperatedate = "";
  private String lastoperatetime = "";
  private float nodepasstime = -1.0F;
  private float nodelefttime = -1.0F;
  private int deleted = 0;
  private int userid = 0;
  private int usertype = 0;
  private int userlanguage = 0;
  private int operatorgroup = 0;
  private int typeid = 0;
  private int groupdetailid = 0;
  private String docids = "";
  private String crmids = "";
  private String hrmids = "";
  private String prjids = "";
  private String cptids = "";
  private String flowFrom = "";
  private String currentdate = "";
  private String currenttime = "";
  private String logdate = "";
  private String logtime = "";
  private String billtablename = "";
  private String submitNodeId = "";
  private String Intervenorid = "";
  private String IntervenoridType = "";
  private boolean isoracle = false;
  private Hashtable operatorsht = null;
  private char flag = Util.getSeparator();
  private String Procpara = "";
  private String sql = "";
  private int secLevel = 0;
  private boolean isStart = true;
  private boolean isTrack = true;
  private boolean isAgent = false;
  private int agentId = -1;
  private int sn = 0;
  private boolean executesuccess = true;
  private boolean islogsuccess = false;
  PoppupRemindInfoUtil poppupRemindInfoUtil = new PoppupRemindInfoUtil(true);
  private ResourceComInfo resourceComInfo = null;
  private CustomerInfoComInfo customerInfoComInfo = null;
  private SendMsgAndMail sendMsgAndMail = null;
  private int selectvalue = -1;

  private int uploadType = 0;
  private String selectedfieldid = "";
  private String selectvaluesql = "";
  private String hasTriggeredSubwf = "";
  private final String ifchangstatus = Util.null2String(???.getPropValue(GCONST.getConfigFile(), "ecology.changestatus"));
  private String message = "";
  private String isFromEditDocument = "false";
  private int isagentCreater = 0;
  private int beagenter = 0;
  private boolean CanModify = false;
  private int IsPending = -1;
  private int RequestKey = 0;
  private int currentopratorInsFirstid = 0;
  private String coadsigntype = "2";
  private String IsBeForwardPending = "";
  private String IsSubmitedOpinion = "";
  private String IsBeForwardModify = "";
  private String IsBeForwardSubmit = "";
  private String coadispending = "";
  private String coadismodify = "";
  private String isrejectremind = "";
  private String rejectremindnodes = "";
  private String docrowindex;
  private int temprowindex = 0;
  private String tempsrc = "";
  private Map operator89mp = new HashMap();
  private Map operatortype89mp = new HashMap();
  private Map agentoperator89mp = new HashMap();
  private ArrayList operator89List = new ArrayList();
  private ArrayList operatortype89List = new ArrayList();
  private ArrayList agentoperator89List = new ArrayList();
  private Map htmlfieldMap = new HashMap();

  private boolean isNeedChooseOperator = false;
  private Map<String, Object> eh_operatorMap = new HashMap();
  private ArrayList requestexceptiontypes = new ArrayList();

  private static final Map OpinionTypeMap = new ConcurrentHashMap(9);
  private ArrayList nextnodeids;
  private ArrayList nextlinkids;
  private ArrayList nextlinknames;
  private ArrayList nextnodetypes;
  private ArrayList operatorshts;
  private ArrayList nextnodeattrs;
  private ArrayList nextnodepassnums;
  private ArrayList linkismustpasss;
  private int nextnodeattr = 0;
  private WFLinkInfo wflinkinfo;
  private boolean canflowtonextnode = false;
  private String innodeids = "";
  private int SignType = 0;
  private int enableIntervenor = 1;
  private RecordSetTrans rstrans = null;
  boolean coadcansubmit = true;
  boolean showcoadjutant = false;
  boolean isWorkFlowToDoc = false;
  private int RejectToNodeid = 0;
  private int RejectToType = 0;
  private int SubmitToNodeid = 0;
  private String messageid = "";
  private String messagecontent = "";
  private String isFirstSubmit = "";

  private List requestCheckAddinRulesList = null;
  private Map requestCheckAddinRulesMap = null;

  private String isAutoApprove = "0";
  private String isAutoCommit = "0";
  private int istest = 0;
  private boolean hasEflowToAssignNode = false;
  private Map<Integer, WFAutoApproveUtils.AutoApproveParams> nodeInfoCache = new HashMap();
  private int nodeattribute;
  private static boolean isExeOldFlowlogic;
  public static final String ERROR_NOTFOUND_G = "流程基本信息不存在，因为无法在workflow_base中查询到当前请求信息。";
  public static final String ERROR_NOTFOUND_NNL = "工作流下一节点错误，因为没有找到符合条件的出口。";
  public static final String ERROR_NOTFOUND_SUBMITERROR = "工作流提交信息错误，因为提交的参数：submitNodeId不正确。";
  public static final String ERROR_NOTFOUND_NNNO = "工作流下一节点操作者错误（依次逐个处理），因为无法获取到下一个操作者。";
  private String oldformsignaturemd5 = "";

  private boolean isMakeOperateLog = true;

  private Map<String, Integer> newAddDetailRowPerInfo = new HashMap();

  private boolean hasCoadjutant = false;

  static
  {
    OpinionTypeMap.put("1", 
      "DocumentId");
    OpinionTypeMap.put("2", 
      "Muti_documentId");
    OpinionTypeMap.put("3", 
      "ProjectId");
    OpinionTypeMap.put("4", 
      "Muti_projectId");
    OpinionTypeMap.put("5", 
      "CustomerId");
    OpinionTypeMap.put("6", 
      "Muti_customerId");
    OpinionTypeMap.put("7", 
      "ResourcesId");
    OpinionTypeMap.put("8", 
      "WorkflowId");
    OpinionTypeMap.put("9", 
      "AccessoriesId");

    isExeOldFlowlogic = false;

    init();
  }

  @IgnoreProperty
  public List getRequestCheckAddinRulesList()
  {
    return this.requestCheckAddinRulesList;
  }

  public RequestManager()
  {
    Calendar today = Calendar.getInstance();
    this.currentdate = Util.add0(today.get(1), 4) + "-" + 
      Util.add0(today.get(2) + 1, 2) + "-" + 
      Util.add0(today.get(5), 2);

    this.currenttime = Util.add0(today.get(11), 2) + ":" + 
      Util.add0(today.get(12), 2) + ":" + 
      Util.add0(today.get(13), 2);

    this.rs = new RecordSet();
    this.rs1 = new RecordSet();
    this.rs2 = new RecordSet();
    this.rs3 = new RecordSet();
    this.rs4 = new RecordSet();

    this.rs.executeProc("GetDBDateAndTime", "");
    if (this.rs.next()) {
      this.currentdate = this.rs.getString("dbdate");
      this.currenttime = this.rs.getString("dbtime");
    }
    this.logdate = this.currentdate;
    this.logtime = this.currenttime;

    this.nextlinkids = new ArrayList();
    this.nextlinknames = new ArrayList();
    this.nextnodeids = new ArrayList();
    this.nextnodetypes = new ArrayList();
    this.operatorshts = new ArrayList();
    this.nextnodeattrs = new ArrayList();
    this.nextnodepassnums = new ArrayList();
    this.linkismustpasss = new ArrayList();
    this.isoracle = this.rs.getDBType().equals("oracle");
    this.nextnodeattr = 0;
    this.SignType = 0;
    this.isagentCreater = 0;
    this.beagenter = 0;
    this.CanModify = false;
    this.IsPending = -1;
    this.RequestKey = 0;
    this.coadsigntype = "2";
    this.showcoadjutant = false;
    this.operator89List = new ArrayList();
    this.agentoperator89List = new ArrayList();
    this.operatortype89List = new ArrayList();
    try {
      this.sendMsgAndMail = new SendMsgAndMail();
      this.resourceComInfo = new ResourceComInfo();
      this.customerInfoComInfo = new CustomerInfoComInfo();
      this.wflinkinfo = new WFLinkInfo();
    } catch (Exception localException) {
    }
    this.requestCheckAddinRulesList = new ArrayList();
  }

  public String getMessageid()
  {
    return this.messageid;
  }

  public void setMessageid(String messageid)
  {
    this.messageid = messageid;
  }

  public String getMessagecontent()
  {
    return this.messagecontent;
  }

  public void setMessagecontent(String messagecontent)
  {
    if (!(this.isRequest)) {
      this.request = this.fu.getRequest();
    }
    if (this.request != null) {
      HttpSession session = this.request.getSession(false);
      session.setAttribute("errormsg_" + this.user.getUID() + "_" + this.requestid, messagecontent);
    }
    this.messagecontent = messagecontent;
  }

  public String getCoadsigntype()
  {
    return this.coadsigntype;
  }

  public void setCoadsigntype(String coadsigntype)
  {
    this.coadsigntype = coadsigntype;
  }

  public int getRequestKey()
  {
    return this.RequestKey;
  }

  public void setRequestKey(int requestKey)
  {
    this.RequestKey = requestKey;
  }

  public int getIsPending()
  {
    return this.IsPending;
  }

  public void setIsPending(int isPending)
  {
    this.IsPending = isPending;
  }

  public boolean getCanModify()
  {
    return this.CanModify;
  }

  public void setCanModify(boolean isModify)
  {
    this.CanModify = isModify;
  }

  private ArrayList getCalfields()
  {
    ArrayList calfieldlist = new ArrayList();
    if (this.isRequest) {
      if (this.request != null) calfieldlist = Util.TokenizerString(Util.null2String(this.request.getParameter("calfields")), ",");
    }
    else if (this.fu != null) calfieldlist = Util.TokenizerString(Util.null2String(this.fu.getParameter("calfields")), ",");

    return calfieldlist;
  }

  private ArrayList getCaldetfields()
  {
    ArrayList caldetfieldlist = new ArrayList();
    if (this.isRequest) {
      if (this.request != null) caldetfieldlist = Util.TokenizerString(Util.null2String(this.request.getParameter("caldetfields")), ",");
    }
    else if (this.fu != null) caldetfieldlist = Util.TokenizerString(Util.null2String(this.fu.getParameter("caldetfields")), ",");

    return caldetfieldlist;
  }

  public RecordSetTrans getRsTrans()
  {
    return this.rstrans; }

  public void SetRsTrans(RecordSetTrans rst) {
    this.rstrans = rst;
  }

  public int getIsagentCreater()
  {
    return this.isagentCreater;
  }

  public void setIsagentCreater(int isagentCreater)
  {
    this.isagentCreater = isagentCreater;
  }

  public int getBeAgenter()
  {
    return this.beagenter;
  }

  public void setBeAgenter(int beagenterid)
  {
    this.beagenter = beagenterid;
  }

  public void setSignType(int signType)
  {
    this.SignType = signType;
  }

  public String getMessage()
  {
    return this.message;
  }

  public void setMessage(String message)
  {
    if (!(this.isRequest)) {
      this.request = this.fu.getRequest();
    }
    if (this.request != null) {
      HttpSession session = this.request.getSession(false);
      session.setAttribute("errormsgid_" + this.user.getUID() + "_" + this.requestid, message);
    }
    this.message = message;
  }

  public String getHasTriggeredSubwf()
  {
    return this.hasTriggeredSubwf;
  }

  public void setHasTriggeredSubwf(String hasTriggeredSubwf)
  {
    this.hasTriggeredSubwf = hasTriggeredSubwf;
  }

  public String getIsFromEditDocument()
  {
    return this.isFromEditDocument;
  }

  public void setIsFromEditDocument(String isFromEditDocument)
  {
    this.isFromEditDocument = isFromEditDocument;
  }

  public int getUserId()
  {
    return this.userid;
  }

  public int getUserType()
  {
    return this.usertype;
  }

  public User getUser()
  {
    return this.user;
  }

  public String getBillTableName()
  {
    return this.billtablename;
  }

  public String getCurrentDate()
  {
    return this.currentdate;
  }

  public String getCurrentTime()
  {
    return this.currenttime;
  }

  public void setIsRequest(boolean isRequest)
  {
    this.isRequest = isRequest;
  }

  public String getSubmitNodeId()
  {
    return this.submitNodeId;
  }

  public void setSubmitNodeId(String submitNodeId)
  {
    this.submitNodeId = submitNodeId;
  }

  public String getIntervenorid()
  {
    return this.Intervenorid;
  }

  public void setIntervenorid(String intervenorid)
  {
    this.Intervenorid = intervenorid;
  }

  public String getIntervenoridType()
  {
    return this.IntervenoridType;
  }

  public void setIntervenoridType(String IntervenoridType)
  {
    this.Intervenorid = IntervenoridType;
  }

  public void setSrc(String src)
  {
    this.src = src;
  }

  public void setIsMultiDoc(String isMultiDoc)
  {
    if (!(isMultiDoc.equals("")))
      this.isMultiDoc = isMultiDoc;
  }

  public void setFlowFrom(String flowFrom)
  {
    this.flowFrom = flowFrom;
  }

  public void setIscreate(String iscreate)
  {
    this.iscreate = iscreate;
  }

  public void setRequestid(int requestid)
  {
    this.requestid = requestid;
  }

  public void setWorkflowid(int workflowid)
  {
    this.workflowid = workflowid;
  }

  public void setWorkflowtype(String workflowtype)
  {
    this.workflowtype = workflowtype;
  }

  public void setIsremark(int isremark)
  {
    this.isremark = isremark;
  }

  public void setFormid(int formid)
  {
    this.formid = formid;
  }

  public void setIsbill(int isbill)
  {
    this.isbill = isbill;
  }

  public void setBillid(int billid)
  {
    this.billid = billid;
  }

  public void setNodeid(int nodeid)
  {
    this.nodeid = nodeid;
  }

  public void setNodetype(String nodetype)
  {
    this.nodetype = nodetype;
  }

  public void setRequestname(String requestname)
  {
    this.requestname = Util.StringReplace(Util.htmlFilter4UTF8(requestname), ",", "，");
  }

  public void setRequestlevel(String requestlevel)
  {
    this.requestlevel = requestlevel;
  }

  public void setRemark(String remark)
  {
    this.remark = remark;
  }

  public String getSignworkflowids()
  {
    return this.signworkflowids;
  }

  public void setSignworkflowids(String signworkflowids)
  {
    this.signworkflowids = signworkflowids;
  }

  public String getSigndocids()
  {
    return this.signdocids;
  }

  public void setSigndocids(String signdocids)
  {
    this.signdocids = signdocids;
  }

  public void setNextNodeid(int nextnodeid)
  {
    this.nextnodeid = nextnodeid;
  }

  public void setNextNodetype(String nextnodetype)
  {
    this.nextnodetype = nextnodetype;
  }

  public void setLastNodeid(int lastnodeid)
  {
    this.lastnodeid = lastnodeid;
  }

  public void setLastnodetype(String lastnodetype)
  {
    this.lastnodetype = lastnodetype;
  }

  public void setStatus(String status)
  {
    this.status = status;
  }

  public void setPassedGroups(int passedgroups)
  {
    this.passedgroups = passedgroups;
  }

  public void setTotalGroups(int totalgroups)
  {
    this.totalgroups = totalgroups;
  }

  public void setCreater(int creater)
  {
    this.creater = creater;
  }

  public void setCreatertype(int creatertype)
  {
    this.creatertype = creatertype;
  }

  public void setCreatedate(String createdate)
  {
    this.createdate = createdate;
  }

  public void setCreatetime(String createtime)
  {
    this.createtime = createtime;
  }

  public void setLastoperator(int lastoperator)
  {
    this.lastoperator = lastoperator;
  }

  public void setLastoperatortype(int lastoperatortype)
  {
    this.lastoperatortype = lastoperatortype;
  }

  public void setLastoperatedate(String lastoperatedate)
  {
    this.lastoperatedate = lastoperatedate;
  }

  public void setLastoperatetime(String lastoperatetime)
  {
    this.lastoperatetime = lastoperatetime;
  }

  public void setNodepasstime(float nodepasstime)
  {
    this.nodepasstime = nodepasstime;
  }

  public void setNeedwfback(String needwfback)
  {
    this.needwfback = needwfback;
  }

  public void setNodelefttime(float nodelefttime)
  {
    this.nodelefttime = nodelefttime;
  }

  public void setRequest(HttpServletRequest request)
  {
    this.request = request;
    this.isRequest = true;
  }

  public HttpServletRequest getRequest() {
    return this.request;
  }

  public void setRequest(FileUpload request)
  {
    this.fu = request;
    this.isRequest = false;
  }

  public void setUser(User user)
  {
    this.user = user;
    this.userid = user.getUID();

    this.usertype = ((user.getLogintype().equals("1")) ? 0 : 1);
    this.userlanguage = user.getLanguage();
  }

  public void setBilltablename(String billtablename)
  {
    this.billtablename = billtablename;
  }

  public void setDocids(String Docids)
  {
    this.docids = Docids;
  }

  public void setCrmids(String Crmids)
  {
    this.crmids = Crmids;
  }

  public void setPrjids(String Prjids)
  {
    this.prjids = Prjids;
  }

  public void setHrmids(String hrmids)
  {
    this.hrmids = hrmids;
  }

  public void setCptids(String cptids)
  {
    this.cptids = cptids;
  }

  public String getSrc()
  {
    return this.src;
  }

  public String getIscreate()
  {
    return this.iscreate;
  }

  public int getRequestid()
  {
    return this.requestid;
  }

  public int getWorkflowid()
  {
    return this.workflowid;
  }

  public String getWorkflowtype()
  {
    return this.workflowtype;
  }

  public int getIsremark()
  {
    return this.isremark;
  }

  public int getFormid()
  {
    return this.formid;
  }

  public int getIsbill()
  {
    return this.isbill;
  }

  public int getBillid()
  {
    return this.billid;
  }

  public int getNodeid()
  {
    return this.nodeid;
  }

  public String getNodetype()
  {
    return this.nodetype;
  }

  public String getRequestname()
  {
    return this.requestname;
  }

  public String getRequestlevel()
  {
    return this.requestlevel;
  }

  public String getRemark()
  {
    return this.remark;
  }

  public int getNextNodeid()
  {
    return this.nextnodeid;
  }

  public String getNextNodetype()
  {
    return this.nextnodetype;
  }

  public int getLastNodeid()
  {
    return this.lastnodeid;
  }

  public String getLastnodetype()
  {
    return this.lastnodetype;
  }

  public String getStatus()
  {
    return this.status;
  }

  public int getPassedGroups()
  {
    return this.passedgroups;
  }

  public int getTotalGroups()
  {
    return this.totalgroups;
  }

  public int getCreater()
  {
    return this.creater;
  }

  public int getCreatertype()
  {
    return this.creatertype;
  }

  public String getCreatedate()
  {
    return this.createdate;
  }

  public String getCreatetime()
  {
    return this.createtime;
  }

  public int getLastoperator()
  {
    return this.lastoperator;
  }

  public int getLastoperatortype()
  {
    return this.lastoperatortype;
  }

  public String getLastoperatedate()
  {
    return this.lastoperatedate;
  }

  public String getLastoperatetime()
  {
    return this.lastoperatetime;
  }

  public float getNodepasstime()
  {
    return this.nodepasstime;
  }

  public String getNeedwfback()
  {
    return this.needwfback;
  }

  public float getNodelefttime()
  {
    return this.nodelefttime;
  }

  public Hashtable getCurrentOperator()
  {
    return this.operatorsht;
  }

  public boolean isNeedChooseOperator()
  {
    return this.isNeedChooseOperator;
  }

  public String disposeSqlNull(String s)
  {
    if (s == null)
      s = "NULL";
    else
      s = "'" + s + "'";
    return s;
  }

  public String getIsAutoApprove()
  {
    return this.isAutoApprove;
  }

  public void setIsAutoApprove(String isAutoApprove) {
    this.isAutoApprove = isAutoApprove;
  }

  public String getIsAutoCommit()
  {
    return this.isAutoCommit; }

  public void setIsAutoCommit(String isAutoCommit) {
    this.isAutoCommit = isAutoCommit; }

  public int getIstest() {
    return this.istest; }

  public boolean isHasEflowToAssignNode() {
    return this.hasEflowToAssignNode; }

  public String getLogdate() {
    return this.logdate;
  }

  public String getLogtime() {
    return this.logtime; }

  public int getNodeattribute() {
    return this.nodeattribute; }

  public void setNodeattribute(int nodeattribute) {
    this.nodeattribute = nodeattribute;
  }

  public boolean saveRequestInfo()
  {
    removeErrorMsg();
    boolean fnaCostStandardFlag = true;
    boolean fnaWfValidatorFlag = true;

    if (this.isbill == 1) {
      this.rs.executeSql("select tablename from workflow_bill where id = " + this.formid);
      if (this.rs.next())
        this.billtablename = this.rs.getString("tablename");
      else
        return false;
    }
    WfFunctionManageUtil WfFunctionManageUtil = new WfFunctionManageUtil();
    if (!(WfFunctionManageUtil.haveOtherOperationRight(this.requestid)))
    {
      return false;
    }
    if ((this.src.equals("delete")) && 
      (!(WfFunctionManageUtil.IsShowDelButtonByReject(this.requestid, this.workflowid)))) {
      setMessage("24676");
      return false;
    }

    WFCoadjutantManager wfcm = new WFCoadjutantManager();
    boolean isCoadjutantCanSubmit = false;
    this.rs.executeSql("select groupdetailid from workflow_currentoperator where id=" + this.RequestKey);
    if (this.rs.next()) {
      wfcm.getCoadjutantRights(this.rs.getInt("groupdetailid"));
      isCoadjutantCanSubmit = wfcm.getCoadjutantCanNextNode(this.requestid, Util.getIntValue(wfcm.getSigntype(), 2), this.RequestKey, this.isremark);
    }

    boolean isAllSubWorkflowEnded = SubWorkflowTriggerService.isAllSubWorkflowEnded(this, this.nodeid);
    if ((this.src.equals("submit")) && 
      (!(isAllSubWorkflowEnded)) && ((
      (this.isremark == 0) || ((this.isremark == 7) && (isCoadjutantCanSubmit))))) {
      setMessage("28083");
      JSONObject subwfinfo = new JSONObject();
      try {
        subwfinfo.put("details", SubWorkflowTriggerService.getAllSubWorkflowEndedMessage(this, this.nodeid));
      } catch (JSONException e) {
        e.printStackTrace();
      }
      setMessagecontent(subwfinfo.toString());
      return false;
    }

    String result = getUpLoadTypeForSelect(this.workflowid);
    if (result.indexOf(",") != -1) {
      this.selectedfieldid = result.substring(0, result.indexOf(","));
      this.uploadType = Integer.valueOf(result.substring(result.indexOf(",") + 1)).intValue();
    }

    Map newMap = new HashMap();
    Map oldMap = new HashMap();
    Map typemap1 = new HashMap();

    this.requestname = this.requestname.trim();
    if ((this.requestname == null) || ("".equals(this.requestname))) {
      String createdate_temp = "";
      if (this.requestid > 0) {
        this.rs2.executeSql("select requestname from workflow_requestbase where requestid  = " + this.requestid);
        if (this.rs2.next()) this.requestname = Util.null2String(this.rs2.getString("requestname"));
      } else {
        createdate_temp = this.logdate;
        this.rs2.executeSql("select workflowname from workflow_base where id = " + this.workflowid);
        if (this.rs2.next()) this.requestname = Util.null2String(this.rs2.getString("workflowname")) + "-" + this.user.getUsername() + "-" + createdate_temp;
      }
    }

    if ((this.src.equals("delete")) || (this.src.equals("reopen")) || (this.src.equals("active")) || ((((this.isremark == 1) || (this.isremark == 7))) && (((!(this.CanModify)) || (this.src.equals("supervise")) || (this.isremark == 9) || (this.src.equals("intervenor")))))) {
      return true;
    }

    if (this.iscreate.equals("1"))
    {
      if (this.isbill == 1) {
        requestIdUpdate.setBilltablename(this.billtablename);
      }

      int[] rvalue = requestIdUpdate.getRequestNewId(this.billtablename);
      this.requestid = rvalue[0];
      if (this.requestid == -1) {
        return false;
      }

      if (this.isbill == 1)
      {
        this.billid = rvalue[1];
        if (this.billid == -1) {
          return false;
        }
      }
      this.executesuccess = this.rs.executeSql("insert into workflow_form (requestid,billformid,billid) values(" + this.requestid + "," + this.formid + "," + this.billid + ")");
      if (!(this.executesuccess)) {
        setMessage("126222");
        return false;
      }

      this.createdate = this.currentdate;
      this.createtime = this.currenttime;
      this.lastnodeid = this.nodeid;
      this.lastnodetype = this.nodetype;
      this.status = "";
      this.passedgroups = 0;
      this.totalgroups = 0;
      this.lastoperator = 0;
      this.lastoperatortype = 0;
      this.lastoperatedate = "";
      this.lastoperatetime = "";
      this.deleted = 0;
      this.nodepasstime = -1.0F;
      this.nodelefttime = -1.0F;
      this.isremark = 0;
      this.operatorgroup = 0;
      this.groupdetailid = 0;
      this.creater = this.userid;
      this.creatertype = this.usertype;
      if (this.isagentCreater == 1) {
        if (this.beagenter < 1) {
          this.rs.executeSql("select bagentuid from workflow_agentConditionSet where iscreateagenter=1 and agentuid=" + this.userid + " and workflowid=" + this.workflowid + " order by agentbatch desc  ,id desc");
          if (this.rs.next()) {
            this.creater = this.rs.getInt(1);
            this.creatertype = 0;
          }
        } else {
          this.creater = this.beagenter;
          this.creatertype = 0;
        }

      }

      if (this.isoracle)
        this.Procpara = this.requestid + this.flag + this.workflowid + this.flag + this.lastnodeid + this.flag + this.lastnodetype + this.flag + 
          this.nodeid + this.flag + this.nodetype + this.flag + this.status + this.flag + this.passedgroups + this.flag + this.totalgroups + 
          this.flag + this.requestname + this.flag + this.creater + this.flag + this.createdate + this.flag + this.createtime + this.flag + 
          this.lastoperator + this.flag + this.lastoperatedate + this.flag + this.lastoperatetime + this.flag + this.deleted + this.flag + 
          this.creatertype + this.flag + this.lastoperatortype + this.flag + this.nodepasstime + this.flag + this.nodelefttime + this.flag + 
          this.docids + this.flag + this.crmids + this.flag + "empty_clob()" + this.flag + this.prjids + this.flag + this.cptids + this.flag + this.messageType + this.flag + this.chatsType;
      else {
        this.Procpara = this.requestid + this.flag + this.workflowid + this.flag + this.lastnodeid + this.flag + this.lastnodetype + this.flag + 
          this.nodeid + this.flag + this.nodetype + this.flag + this.status + this.flag + this.passedgroups + this.flag + this.totalgroups + 
          this.flag + this.requestname + this.flag + this.creater + this.flag + this.createdate + this.flag + this.createtime + this.flag + 
          this.lastoperator + this.flag + this.lastoperatedate + this.flag + this.lastoperatetime + this.flag + this.deleted + this.flag + 
          this.creatertype + this.flag + this.lastoperatortype + this.flag + this.nodepasstime + this.flag + this.nodelefttime + this.flag + 
          this.docids + this.flag + this.crmids + this.flag + this.hrmids + this.flag + this.prjids + this.flag + this.cptids + this.flag + this.messageType + this.flag + this.chatsType;
      }

      this.executesuccess = this.rs.executeProc("workflow_requestbase_insertnew", this.Procpara);

      this.rs.executeProc("workflow_Rbase_UpdateLevel", this.requestid + this.flag + this.requestlevel);
      if (!(this.executesuccess)) {
        setMessage("126222");
        return false;
      }

      if (isOldOrNewFlag(this.requestid)) {
        this.Procpara = this.requestid + this.flag + this.userid + this.flag + this.operatorgroup + this.flag + this.workflowid + this.flag + 
          this.workflowtype + this.flag + this.creatertype + this.flag + this.isremark + this.flag + -1 + 
          this.flag + -1 + this.flag + "0" + this.flag + -1 + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
        this.executesuccess = this.rs.executeProc("workflow_CurrentOperator_I2", this.Procpara);
      }
      else if ((this.isagentCreater == 1) && (this.beagenter > 0)) {
        this.Procpara = this.requestid + this.flag + this.userid + this.flag + this.operatorgroup + this.flag + this.workflowid + this.flag + 
          this.workflowtype + this.flag + this.creatertype + this.flag + this.isremark + this.flag + this.lastnodeid + this.flag + this.beagenter + 
          this.flag + "2" + this.flag + -1 + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
        this.executesuccess = this.rs.executeProc("workflow_CurrentOperator_I2", this.Procpara);
        this.Procpara = this.requestid + this.flag + this.beagenter + this.flag + this.operatorgroup + this.flag + this.workflowid + this.flag + 
          this.workflowtype + this.flag + this.creatertype + this.flag + "2" + this.flag + this.lastnodeid + this.flag + this.userid + 
          this.flag + "1" + this.flag + -1 + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
        this.executesuccess = this.rs.executeProc("workflow_CurrentOperator_I2", this.Procpara);
      } else {
        this.Procpara = this.requestid + this.flag + this.userid + this.flag + this.operatorgroup + this.flag + this.workflowid + this.flag + 
          this.workflowtype + this.flag + this.creatertype + this.flag + this.isremark + this.flag + this.lastnodeid + this.flag + -1 + 
          this.flag + "0" + this.flag + -1 + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
        this.executesuccess = this.rs.executeProc("workflow_CurrentOperator_I2", this.Procpara);
      }

      if (!(this.executesuccess)) return false;
      this.rs.executeSql("delete from workflow_nownode where requestid=" + this.requestid);
      this.rs.executeSql("insert into workflow_nownode(requestid,nownodeid,nownodetype,nownodeattribute) values(" + this.requestid + "," + this.lastnodeid + ",0,0)");
    }
    else {
      this.rs.executeProc("workflow_Requestbase_SByID", this.requestid);
      if (this.rs.next()) {
        this.lastnodeid = Util.getIntValue(this.rs.getString("lastnodeid"), 0);
        this.lastnodetype = Util.null2String(this.rs.getString("lastnodetype"));
        this.passedgroups = Util.getIntValue(this.rs.getString("passedgroups"), 0);
        this.totalgroups = Util.getIntValue(this.rs.getString("totalgroups"), 0);
        this.creater = Util.getIntValue(this.rs.getString("creater"), 0);
        this.creatertype = Util.getIntValue(this.rs.getString("creatertype"), 0);
        this.requestmark = Util.null2String(this.rs.getString("requestmark"));
      }
      if ((this.isbill == 1) && (this.billid <= 0)) {
        getBillId();
      }
    }

    if (!("1".equals(this.iscreate)))
    {
      int sleepsum = 0;

      while (GCONST.WFProcessing.indexOf(this.requestid + "_" + this.nodeid) != -1) {
        try
        {
          do {
            Thread.sleep(1000L);
            ++sleepsum; }
          while (sleepsum <= 1000);
          GCONST.WFProcessing.remove(this.requestid + "_" + this.nodeid);
          setMessage("21270");
          return false;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if ((!(this.src.equals("supervise"))) && (!(this.src.equals("intervenor"))) && 
        (this.isremark != 1) && (this.isremark != 9)) {
        this.rs.executeSql("select 1 from workflow_currentoperator where isremark not in('2','4') and (takisremark<>-2 or takisremark is null) and requestid=" + this.requestid + " and userid=" + this.userid + " and nodeid=" + this.nodeid);
        if (this.rs.getCounts() < 1) {
          setMessage("21266");
          return false;
        }

      }

      if (!("true".equals(this.isFromEditDocument))) {
        RequestSaveCheckManager requestSaveCheckManager = new RequestSaveCheckManager();
        String returnMessage = requestSaveCheckManager.getReturnMessage(this, this.isRequest, this.fu, this.request);
        if (!(returnMessage.equals(""))) {
          setMessage("24676");
          saveRequestLog("1");
          return false;
        }

      }

      this.rs.execute("select requestname,requestlevel,messageType from workflow_requestbase where requestid=" + this.requestid);
      if (this.rs.next())
      {
        for (int i = 0; i < 3; ++i) {
          Track t = new Track();
          t.setFieldId(-1);
          t.setFieldType("1");
          t.setFieldHtmlType("1");
          t.setFieldId(-1);
          t.setNodeId(this.nodeid);
          t.setRequestId(this.requestid);
          t.setIsBill(1);
          t.setModifierIP(getIp());
          t.setOptKind(this.src);
          if ((i == 0) && (!("".equals(this.requestname)))) {
            t.setFieldName("requestname");
            t.setFieldOldText(this.rs.getString("requestname"));
            t.setFieldLableId(229);
            oldMap.put(t.getFieldName(), t);
            newMap.put("requestname", this.requestname);
          }
          else if ((i == 1) && (!("".equals(this.requestlevel)))) {
            t.setFieldName("requestlevel");
            t.setFieldOldText(this.rs.getString("requestlevel"));
            t.setFieldLableId(15534);
            oldMap.put(t.getFieldName(), t);
            newMap.put("requestlevel", this.requestlevel);
          }
          else if ((i == 2) && (!("".equals(this.messageType)))) {
            t.setFieldName("messageType");
            t.setFieldOldText(this.rs.getString("messageType"));
            t.setFieldLableId(17586);
            oldMap.put(t.getFieldName(), t);
            newMap.put("messageType", this.messageType);
          }

        }

      }

      if ((!("".equals(this.requestlevel))) && (!("".equals(this.messageType))))
        this.rs.executeSql("update workflow_requestbase set messageType=" + Util.getIntValue(this.messageType, -1) + ",requestLevel=" + Util.getIntValue(this.requestlevel, -1) + " where  requestid=" + this.requestid);
      else if ((!("".equals(this.requestlevel))) && ("".equals(this.messageType)))
        this.rs.executeSql("update workflow_requestbase set requestLevel=" + Util.getIntValue(this.requestlevel, -1) + " where  requestid=" + this.requestid);
      else if (("".equals(this.requestlevel)) && (!("".equals(this.messageType)))) {
        this.rs.executeSql("update workflow_requestbase set messageType=" + Util.getIntValue(this.messageType, -1) + " where  requestid=" + this.requestid);
      }

      if ((!("".equals(this.requestname))) && (this.requestname != null)) {
        this.rs.executeSql("update workflow_requestbase set requestname='" + Util.fromScreen2(this.requestname, this.userlanguage) + "' where  requestid=" + this.requestid);
      }

      if (!("".equals(this.chatsType))) {
        this.rs.executeSql("update workflow_requestbase set chatsType=" + Util.getIntValue(this.chatsType, -1) + " where  requestid=" + this.requestid);
      }

    }

    int docRightByOperator = 0;
    this.rs.execute("select docRightByOperator from workflow_base where id=" + this.workflowid);
    if (this.rs.next()) {
      docRightByOperator = Util.getIntValue(this.rs.getString("docRightByOperator"), 0);
    }
    if (this.flowFrom.equals(""))
    {
      Iterator fdit;
      Map.Entry entry;
      String key;
      Map.Entry entry;
      String value;
      String key;
      boolean _result;
      String value;
      String updateclause = "";
      String fieldid = "";
      String fieldname = "";
      String fielddbtype = "";
      String fieldhtmltype = "";
      String fieldtype = "";
      String fieldlable = "";
      FieldComInfo fieldComInfo = new FieldComInfo();
      Map locationMap = new HashMap();

      if (this.isbill == 1)
        this.selectvaluesql = "select * from workflow_billfield where billid=" + this.formid + " order by dsporder";
      else {
        this.selectvaluesql = "select fieldid,fieldorder,isdetail from workflow_formfield where formid=" + 
          this.formid + " and (isdetail<>'1' or isdetail is null) order by fieldid  ";
      }
      this.rs4.executeSql(this.selectvaluesql);
      while (this.rs4.next()) {
        if (this.isbill == 1) {
          String viewtype = Util.null2String(this.rs4.getString("viewtype"));
          if (viewtype.equals("1")) continue;
          fieldid = Util.null2String(this.rs4.getString("id"));
          fieldhtmltype = Util.null2String(this.rs4.getString("fieldhtmltype"));
        } else {
          fieldid = Util.null2String(this.rs4.getString(1));
          fieldhtmltype = Util.null2String(fieldComInfo.getFieldhtmltype(fieldid));
        }
        if ((!("5".equals(fieldhtmltype))) || 
          (!(this.selectedfieldid.equals(fieldid)))) continue;
        if (this.isRequest)
          this.selectvalue = Util.getIntValue(this.request.getParameter("field" + fieldid), 0);
        else {
          this.selectvalue = Util.getIntValue(this.fu.getParameter("field" + fieldid), 0);
        }

      }

      StringBuffer s = new StringBuffer();
      String fieldsSql = "";
      String billFieldsSql = "";

      s.append("select agentorbyagentid from workflow_currentoperator where agentorbyagentid<>" + this.userid + " and requestid=" + this.requestid + " and nodeid=" + this.nodeid);
      this.executesuccess = this.rs.executeSql(s.toString());
      if (!(this.executesuccess)) {
        writeLog(s.toString());
        saveRequestLog("1");
        return false;
      }
      if ((this.rs.next()) && 
        (this.rs.getInt("agentorbyagentid") != -1) && (this.userid != this.rs.getInt("agentorbyagentid"))) {
        this.isAgent = true;
        this.agentId = this.rs.getInt("agentorbyagentid");
      }

      s = new StringBuffer();
      s.append("select t1.ismodifylog, t2.status from workflow_base t1, workflow_requestbase t2 where t1.id=t2.workflowid and t2.requestid=" + this.requestid);
      this.executesuccess = this.rs.executeSql(s.toString());
      if (!(this.executesuccess)) {
        writeLog(s.toString());
        saveRequestLog("1");
        return false;
      }
      if (this.rs.next()) {
        this.isTrack = ((this.rs.getString("ismodifylog") != null) && ("1".equals(this.rs.getString("ismodifylog"))));
        this.isStart = ((this.rs.getString("status") != null) && (!("".equals(this.rs.getString("status")))));
      }

      if ((this.isTrack) && 
        (!("1".equals(this.iscreate))) && (this.isStart))
      {
        List fields = getFieldsName();
        if ((fields != null) && (fields.size() > 0))
        {
          Track t;
          s = new StringBuffer();
          s.append("select ");
          for (int i = 0; i < fields.size(); ++i) {
            t = (Track)fields.get(i);
            s.append(t.getFieldName());
            if (i < fields.size() - 1) {
              s.append(",");
            }
          }
          if (this.isbill == 1)
            s.append(" from " + this.billtablename + " where id = " + this.billid);
          else {
            s.append(" from workflow_form where requestid=" + this.requestid);
          }

          this.executesuccess = this.rs.executeSql(s.toString());
          if (!(this.executesuccess)) {
            writeLog(s.toString());
            saveRequestLog("1");
            setMessage("126223");
            return false;
          }
          if (this.rs.next()) {
            for (i = 0; i < fields.size(); ++i) {
              t = (Track)fields.get(i);
              t.setFieldOldText(this.rs.getString(t.getFieldName()));
              oldMap.put(t.getFieldName(), t);
            }

          }

        }

      }

      if (this.isbill == 1)
        this.executesuccess = this.rs.executeProc("workflow_billfield_Select", this.formid);
      else {
        this.executesuccess = this.rs.executeSql("select t2.fieldid,t2.fieldorder,t2.isdetail,t1.fieldlable,t1.langurageid from workflow_fieldlable t1,workflow_formfield t2 where t1.formid=t2.formid and t1.fieldid=t2.fieldid and (t2.isdetail<>'1' or t2.isdetail is null)  and t2.formid=" + this.formid + "  and t1.langurageid=" + this.userlanguage + " order by t2.fieldorder");
      }

      if (!(this.executesuccess)) {
        saveRequestLog("1");
        setMessage("126223");
        return false;
      }
      FieldInfo fieldinfo = new FieldInfo();
      ArrayList editfields = new ArrayList();
      ArrayList detaileditfields = new ArrayList();
      if (!("1".equals(this.iscreate))) {
        editfields = fieldinfo.getSaveMainFields(this.formid, this.isbill, this.workflowid, this.nodeid, getCalfields());
        detaileditfields = fieldinfo.getSaveDetailFields(this.formid, this.isbill, this.workflowid, this.nodeid, getCaldetfields());

        new ExcelLayoutManager().manageFormulaDestFile(this.workflowid, this.nodeid, 0, editfields, detaileditfields);
      }
      Map fieldTypeCache = new HashMap();
      while (this.rs.next()) {
        if (this.isbill == 1) {
          String viewtype = Util.null2String(this.rs.getString("viewtype"));
          if (viewtype.equals("1")) continue;
          fieldid = Util.null2String(this.rs.getString("id"));
          fieldname = Util.null2String(this.rs.getString("fieldname"));
          fielddbtype = Util.null2String(this.rs.getString("fielddbtype"));
          fieldhtmltype = Util.null2String(this.rs.getString("fieldhtmltype"));
          fieldtype = Util.null2String(this.rs.getString("type"));
          fieldlable = Util.null2String(this.rs.getString("fieldlable"));
        } else {
          fieldid = Util.null2String(this.rs.getString(1));
          fieldname = Util.null2String(fieldComInfo.getFieldname(fieldid));
          fielddbtype = Util.null2String(fieldComInfo.getFielddbtype(fieldid));
          fieldhtmltype = Util.null2String(fieldComInfo.getFieldhtmltype(fieldid));
          fieldtype = Util.null2String(fieldComInfo.getFieldType(fieldid));
        }

        if ((this.isbill != 1) && (StringUtil.isNull(fieldname))) {
          RecordSet rstemp = new RecordSet();
          String deleteDatasql = "delete from workflow_formfield where formid = " + this.formid + " and fieldid = " + fieldid;
          rstemp.execute(deleteDatasql);
          writeLog(deleteDatasql);
        }
        else
        {
          String tempvalueid;
          int digitsIndex;
          String tempvalue;
          String preData;
          boolean ishtml;
          int decimaldigits;
          String tempvalue;
          String thetempvalue;
          String oldUploadIdsStrs_;
          RecordSet rsUploadId;
          int y;
          String clientIp;
          if ((fieldhtmltype.equals("3")) && (((fieldtype.equals("1")) || (fieldtype.equals("17"))))) {
            tempvalueid = "";
            if (this.isRequest)
              tempvalueid = Util.null2String(this.request.getParameter("field" + fieldid));
            else
              tempvalueid = Util.null2String(this.fu.getParameter("field" + fieldid));
            if (!(tempvalueid.equals("")))
            {
              RequestManager tmp5943_5942 = this; tmp5943_5942.hrmids = tmp5943_5942.hrmids + "," + tempvalueid;
            }
            if (fieldtype.equals("17"))
              try {
                WFPathBrowserUtil.updateBrowInfo(this.fu, this.requestid, fieldid);
              } catch (Exception e9) {
                e9.printStackTrace();
              }
          }
          else if ((fieldhtmltype.equals("3")) && (((fieldtype.equals("7")) || (fieldtype.equals("18"))))) {
            tempvalueid = "";
            if (this.isRequest)
              tempvalueid = Util.null2String(this.request.getParameter("field" + fieldid));
            else {
              tempvalueid = Util.null2String(this.fu.getParameter("field" + fieldid));
            }

            tempvalueid = resourceAuthorityFilter(fieldhtmltype, fieldtype, tempvalueid);
            if (!(tempvalueid.equals("")))
            {
              RequestManager tmp6144_6143 = this; tmp6144_6143.crmids = tmp6144_6143.crmids + "," + tempvalueid; }
          } else if ((fieldhtmltype.equals("3")) && (((fieldtype.equals("8")) || (fieldtype.equals("135"))))) {
            tempvalueid = "";
            if (this.isRequest)
              tempvalueid = Util.null2String(this.request.getParameter("field" + fieldid));
            else {
              tempvalueid = Util.null2String(this.fu.getParameter("field" + fieldid));
            }
            tempvalueid = resourceAuthorityFilter(fieldhtmltype, fieldtype, tempvalueid);
            if (!(tempvalueid.equals("")))
            {
              RequestManager tmp6312_6311 = this; tmp6312_6311.prjids = tmp6312_6311.prjids + "," + tempvalueid; }
          } else if ((fieldhtmltype.equals("3")) && (((fieldtype.equals("9")) || (fieldtype.equals("37"))))) {
            tempvalueid = "";
            if (this.isRequest)
              tempvalueid = Util.null2String(this.request.getParameter("field" + fieldid));
            else {
              tempvalueid = Util.null2String(this.fu.getParameter("field" + fieldid));
            }
            tempvalueid = resourceAuthorityFilter(fieldhtmltype, fieldtype, tempvalueid);
            if (!(tempvalueid.equals("")))
            {
              RequestManager tmp6480_6479 = this; tmp6480_6479.docids = tmp6480_6479.docids + "," + tempvalueid;
            }
            if (docRightByOperator == 1)
            {
              if (!(tempvalueid.equals("")))
              {
                this.rs1.execute("delete from Workflow_DocSource where requestid =" + this.requestid + " and fieldid =" + fieldid + " and docid not in (" + tempvalueid + ")");
              }
              else
              {
                this.rs1.execute("delete from Workflow_DocSource where requestid =" + this.requestid + " and fieldid =" + fieldid);
              }

              String[] mdocid = Util.TokenizerString2(tempvalueid, ",");
              for (int i = 0; i < mdocid.length; ++i) {
                if ((mdocid[i] != null) && (!(mdocid[i].equals(""))))
                  this.rs1.executeProc("Workflow_DocSource_Insert", this.requestid + this.flag + this.nodeid + this.flag + fieldid + this.flag + mdocid[i] + this.flag + this.userid + this.flag + "1");
              }
            }
          }
          else if ((fieldhtmltype.equals("3")) && (fieldtype.equals("23"))) {
            tempvalueid = "";
            if (this.isRequest)
              tempvalueid = Util.null2String(this.request.getParameter("field" + fieldid));
            else {
              tempvalueid = Util.null2String(this.fu.getParameter("field" + fieldid));
            }
            tempvalueid = resourceAuthorityFilter(fieldhtmltype, fieldtype, tempvalueid);
            if (!(tempvalueid.equals("")))
            {
              RequestManager tmp6896_6895 = this; tmp6896_6895.cptids = tmp6896_6895.cptids + "," + tempvalueid;
            }
          }
          if ((!("1".equals(this.iscreate))) && (editfields.indexOf(fieldid) < 0) && (!(fieldhtmltype.equals("9")))) {
            continue;
          }

          if (this.isoracle) {
            if ((fielddbtype.toUpperCase().indexOf("INT") >= 0) && (!("224".equals(fieldtype))) && (!("225".equals(fieldtype))))
            {
              if ("5".equals(fieldhtmltype)) {
                if (this.isRequest) {
                  if (!(Util.null2String(this.request.getParameter("field" + fieldid)).equals(""))) {
                    if (newMap.get(fieldname) == null)
                      updateclause = updateclause + fieldname + " = " + Util.getIntValue(this.request.getParameter(new StringBuilder("field").append(fieldid).toString()), -1) + ",";
                    newMap.put(fieldname, String.valueOf(Util.getIntValue(this.request.getParameter("field" + fieldid), -1)));
                  }
                  else {
                    if (newMap.get(fieldname) == null)
                      updateclause = updateclause + fieldname + " = NULL,";
                    newMap.put(fieldname, null);
                  }
                }
                else if (!(Util.null2String(this.fu.getParameter("field" + fieldid)).equals(""))) {
                  if (newMap.get(fieldname) == null)
                    updateclause = updateclause + fieldname + " = " + Util.getIntValue(this.fu.getParameter(new StringBuilder("field").append(fieldid).toString()), -1) + ",";
                  newMap.put(fieldname, String.valueOf(Util.getIntValue(this.fu.getParameter("field" + fieldid), -1)));
                }
                else {
                  if (newMap.get(fieldname) == null)
                    updateclause = updateclause + fieldname + " = NULL,";
                  newMap.put(fieldname, null);
                }

              }
              else if (this.isRequest)
              {
                tempvalueid = resourceAuthorityFilter(fieldhtmltype, fieldtype, Util.null2String(this.request.getParameter("field" + fieldid)));
                if (!(Util.null2String(tempvalueid).equals(""))) {
                  if (newMap.get(fieldname) == null)
                    updateclause = updateclause + fieldname + " = " + Util.getIntValue(tempvalueid, 0) + ",";
                  newMap.put(fieldname, String.valueOf(Util.getIntValue(tempvalueid, 0)));
                }
                else {
                  if (newMap.get(fieldname) == null)
                    updateclause = updateclause + fieldname + " = NULL,";
                  newMap.put(fieldname, null);
                }
              } else {
                tempvalueid = resourceAuthorityFilter(fieldhtmltype, fieldtype, Util.null2String(this.fu.getParameter("field" + fieldid)));
                if (!(Util.null2String(tempvalueid).equals(""))) {
                  if (newMap.get(fieldname) == null)
                    updateclause = updateclause + fieldname + " = " + Util.getIntValue(tempvalueid, 0) + ",";
                  newMap.put(fieldname, String.valueOf(Util.getIntValue(tempvalueid, 0)));
                }
                else {
                  if (newMap.get(fieldname) == null)
                    updateclause = updateclause + fieldname + " = NULL,";
                  newMap.put(fieldname, null);
                }

              }

            }
            else if ((((fielddbtype.toUpperCase().indexOf("NUMBER") >= 0) || (fielddbtype.toUpperCase().indexOf("FLOAT") >= 0))) && (!("224".equals(fieldtype))) && (!("225".equals(fieldtype)))) {
              digitsIndex = fielddbtype.indexOf(",");
              decimaldigits = 2;
              if (digitsIndex > -1)
                decimaldigits = Util.getIntValue(fielddbtype.substring(digitsIndex + 1, fielddbtype.length() - 1).trim(), 2);
              else {
                decimaldigits = 2;
              }
              if (this.isRequest) {
                if (!(Util.null2String(this.request.getParameter("field" + fieldid)).equals(""))) {
                  if (newMap.get(fieldname) == null)
                    updateclause = updateclause + fieldname + " = " + Util.getPointValue2(this.request.getParameter(new StringBuilder("field").append(fieldid).toString()), decimaldigits) + ",";
                  newMap.put(fieldname, Util.getPointValue2(this.request.getParameter("field" + fieldid), decimaldigits));
                }
                else {
                  if (newMap.get(fieldname) == null)
                    updateclause = updateclause + fieldname + " = NULL,";
                  newMap.put(fieldname, null);
                }

              }
              else if (!(Util.null2String(this.fu.getParameter("field" + fieldid)).equals(""))) {
                if (newMap.get(fieldname) == null)
                  updateclause = updateclause + fieldname + " = " + Util.getPointValue2(this.fu.getParameter(new StringBuilder("field").append(fieldid).toString()), decimaldigits) + ",";
                newMap.put(fieldname, Util.getPointValue2(this.fu.getParameter("field" + fieldid), decimaldigits));
              }
              else {
                if (newMap.get(fieldname) == null)
                  updateclause = updateclause + fieldname + " = NULL,";
                newMap.put(fieldname, null);
              }
            }
            else if ((fieldhtmltype.equals("3")) && ("17".equals(fieldtype))) {
              if (this.isRequest) {
                if (newMap.get(fieldname) == null) {
                  updateclause = updateclause + fieldname + " = '' ,";
                }
                typemap1.put(fieldname, Util.null2String(this.request.getParameter("field" + fieldid)));
              } else {
                if (newMap.get(fieldname) == null) {
                  updateclause = updateclause + fieldname + " = '' ,";
                }
                typemap1.put(fieldname, Util.null2String(this.fu.getParameter("field" + fieldid)));
              }
            } else if (fieldhtmltype.equals("6"))
            {
              tempvalue = "";

              if (this.isRequest)
                tempvalue = Util.null2String(this.request.getParameter("field" + fieldid));
              else {
                tempvalue = Util.null2String(this.fu.getParameter("field" + fieldid));
              }
              DocExtUtil docExtUtil = new DocExtUtil();
              String[] oldUploadIdsStrs = null;
              oldUploadIdsStrs_ = "";
              rsUploadId = new RecordSet();
              if (this.isbill == 1)
                rsUploadId.executeSql("select " + fieldname + " from " + this.billtablename + " where requestid = " + this.requestid);
              else {
                rsUploadId.executeSql("select " + fieldname + " from workflow_form where requestid = " + this.requestid);
              }
              if (rsUploadId.next()) {
                oldUploadIdsStrs_ = rsUploadId.getString(fieldname);
              }
              if ((oldUploadIdsStrs_ != null) && (!("".equals(oldUploadIdsStrs_)))) {
                oldUploadIdsStrs = Util.TokenizerString2(oldUploadIdsStrs_, ",");
              }
              if (!(tempvalue.equals(""))) {
                if (newMap.get(fieldname) == null)
                  updateclause = updateclause + fieldname + " = '" + fillFullNull(tempvalue) + "',";
                newMap.put(fieldname, tempvalue);
              }
              else if (tempvalue.equals("")) {
                if (newMap.get(fieldname) == null)
                  updateclause = updateclause + fieldname + " = '',";
                newMap.put(fieldname, null);
              }

              if (oldUploadIdsStrs != null) {
                for (y = 0; y < oldUploadIdsStrs.length; ++y) {
                  if ((tempvalue.indexOf(oldUploadIdsStrs[y]) != -1) || 
                    (Util.getIntValue(oldUploadIdsStrs[y], 0) == 0)) continue;
                  clientIp = "";
                  if (this.isRequest) {
                    if (this.request != null)
                      clientIp = Util.null2String(this.request.getRemoteAddr());
                  }
                  else if (this.fu != null) {
                    clientIp = Util.null2String(this.fu.getRemoteAddr());
                  }
                  docExtUtil.deleteDoc(Integer.parseInt(oldUploadIdsStrs[y]), this.user, clientIp);
                }

              }

            }
            else if (fieldhtmltype.equals("9")) {
              preData = "";
              tempvalue = "";
              if (this.isRequest)
                preData = Util.null2String(this.request.getParameter("field" + fieldid));
              else {
                preData = Util.null2String(this.fu.getParameter("field" + fieldid));
              }
              if (preData.equals(""))
                tempvalue = this.nodeid + "////~~weaversplit~~////" + this.userid + "////~~weaversplit~~////" + "0" + "////~~weaversplit~~////" + 
                  "////~~weaversplit~~////" + "0" + "////~~weaversplit~~////" + "0";
              else {
                tempvalue = preData + "/////~~weaversplit~~/////" + this.nodeid + "////~~weaversplit~~////" + this.userid + "////~~weaversplit~~////" + "0" + "////~~weaversplit~~////" + 
                  "////~~weaversplit~~////" + "0" + "////~~weaversplit~~////" + "0";
              }
              locationMap.put(fieldname, tempvalue);
              newMap.put(fieldname, tempvalue);
            }
            else
            {
              ishtml = false;
              thetempvalue = "";
              if (this.workflowid == 1) {
                if (this.isRequest)
                {
                  thetempvalue = Util.toHtml100(this.request.getParameter("field" + fieldid));
                }
                else {
                  thetempvalue = Util.toHtml100(this.fu.getParameter("field" + fieldid));
                }
              }
              else if ((fieldhtmltype.equals("3")) && (((fieldtype.equals("161")) || (fieldtype.equals("162"))))) {
                if (this.isRequest)
                  thetempvalue = Util.null2String(this.request.getParameter("field" + fieldid));
                else {
                  thetempvalue = Util.null2String(this.fu.getParameter("field" + fieldid));
                }
                thetempvalue = thetempvalue.trim();
              } else if ((fieldhtmltype.equals("3")) && (((fieldtype.equals("224")) || (fieldtype.equals("225")) || (fieldtype.equals("226")) || (fieldtype.equals("227")))))
              {
                if (this.isRequest)
                  thetempvalue = Util.null2String(this.request.getParameter("field" + fieldid));
                else {
                  thetempvalue = Util.null2String(this.fu.getParameter("field" + fieldid));
                }
                thetempvalue = thetempvalue.trim();
              }
              else if (this.isRequest)
              {
                if ((fieldhtmltype.equals("2")) && (fieldtype.equals("2")))
                {
                  thetempvalue = Util.toHtml100(this.request.getParameter("field" + fieldid));
                  thetempvalue = rePlaceWordMark(thetempvalue);
                  ishtml = true;
                } else if ((fieldhtmltype.equals("1")) && (fieldtype.equals("1"))) {
                  thetempvalue = Util.toHtmlForWorkflow(this.request.getParameter("field" + fieldid));
                } else {
                  thetempvalue = Util.StringReplace(Util.toHtml10(this.request.getParameter("field" + fieldid)), " ", "&nbsp;");
                }

              }
              else if ((fieldhtmltype.equals("2")) && (fieldtype.equals("2")))
              {
                thetempvalue = Util.toHtml100(this.fu.getParameter("field" + fieldid));
                thetempvalue = rePlaceWordMark(thetempvalue);
                ishtml = true;
              } else if ((fieldhtmltype.equals("1")) && (fieldtype.equals("1"))) {
                thetempvalue = Util.toHtmlForWorkflow(Util.htmlFilter4UTF8(this.fu.getParameter("field" + fieldid)));
              } else {
                thetempvalue = Util.StringReplace(Util.toHtml10(Util.htmlFilter4UTF8(this.fu.getParameter("field" + fieldid))), " ", "&nbsp;");
              }

              thetempvalue = resourceAuthorityFilter(fieldhtmltype, fieldtype, thetempvalue);
              if (thetempvalue.equals("")) thetempvalue = " ";

              if (ishtml) {
                this.htmlfieldMap.put(fieldname, thetempvalue);
              }
              else
                updateclause = updateclause + fieldname + " = '" + fillFullNull(thetempvalue) + "',";
              newMap.put(fieldname, thetempvalue);
            }

          }
          else if ((fielddbtype.toUpperCase().indexOf("INT") >= 0) && (!("224".equals(fieldtype))) && (!("225".equals(fieldtype))))
          {
            if ("5".equals(fieldhtmltype))
            {
              if (this.isRequest)
              {
                if (!("".equals(this.request.getParameter("field" + fieldid))))
                {
                  if (newMap.get(fieldname) == null)
                    updateclause = updateclause + fieldname + " = " + Util.getIntValue(this.request.getParameter(new StringBuilder("field").append(fieldid).toString()), -1) + ",";
                  newMap.put(fieldname, String.valueOf(Util.getIntValue(this.request.getParameter("field" + fieldid), -1)));
                }
                else
                {
                  if (newMap.get(fieldname) == null)
                    updateclause = updateclause + fieldname + " = NULL,";
                  newMap.put(fieldname, null);
                }

              }
              else if (!("".equals(this.fu.getParameter("field" + fieldid))))
              {
                if (newMap.get(fieldname) == null)
                  updateclause = updateclause + fieldname + " = " + Util.getIntValue(this.fu.getParameter(new StringBuilder("field").append(fieldid).toString()), -1) + ",";
                newMap.put(fieldname, String.valueOf(Util.getIntValue(this.fu.getParameter("field" + fieldid), -1)));
              }
              else
              {
                if (newMap.get(fieldname) == null)
                  updateclause = updateclause + fieldname + " = NULL,";
                newMap.put(fieldname, null);
              }

            }
            else if (this.isRequest)
            {
              ishtml = resourceAuthorityFilter(fieldhtmltype, fieldtype, Util.null2String(this.request.getParameter("field" + fieldid)));
              if (!("".equals(ishtml)))
              {
                if (newMap.get(fieldname) == null)
                  updateclause = updateclause + fieldname + " = " + Util.getIntValue(ishtml, 0) + ",";
                newMap.put(fieldname, String.valueOf(Util.getIntValue(ishtml, 0)));
              }
              else
              {
                if (newMap.get(fieldname) == null)
                  updateclause = updateclause + fieldname + " = NULL,";
                newMap.put(fieldname, null);
              }

            }
            else
            {
              ishtml = resourceAuthorityFilter(fieldhtmltype, fieldtype, Util.null2String(this.fu.getParameter("field" + fieldid)));
              if (!("".equals(ishtml)))
              {
                if (newMap.get(fieldname) == null)
                  updateclause = updateclause + fieldname + " = " + Util.getIntValue(ishtml, 0) + ",";
                newMap.put(fieldname, String.valueOf(Util.getIntValue(ishtml, 0)));
              }
              else
              {
                if (newMap.get(fieldname) == null)
                  updateclause = updateclause + fieldname + " = NULL,";
                newMap.put(fieldname, null);
              }

            }

          }
          else if ((((fielddbtype.toUpperCase().indexOf("DECIMAL") >= 0) || (fielddbtype.toUpperCase().indexOf("FLOAT") >= 0))) && (!("224".equals(fieldtype))) && (!("225".equals(fieldtype))))
          {
            ishtml = fielddbtype.indexOf(",");
            thetempvalue = 2;
            if (ishtml > true)
              thetempvalue = Util.getIntValue(fielddbtype.substring(ishtml + true, fielddbtype.length() - 1).trim(), 2);
            else {
              thetempvalue = 2;
            }
            if (this.isRequest)
            {
              if (!("".equals(this.request.getParameter("field" + fieldid))))
              {
                if (newMap.get(fieldname) == null)
                  updateclause = updateclause + fieldname + " = " + Util.getPointValue2(this.request.getParameter(new StringBuilder("field").append(fieldid).toString()), thetempvalue) + ",";
                newMap.put(fieldname, Util.getPointValue2(this.request.getParameter("field" + fieldid), thetempvalue));
              }
              else
              {
                if (newMap.get(fieldname) == null)
                  updateclause = updateclause + fieldname + " = NULL,";
                newMap.put(fieldname, null);
              }

            }
            else if (!("".equals(this.fu.getParameter("field" + fieldid))))
            {
              if (newMap.get(fieldname) == null)
                updateclause = updateclause + fieldname + " = " + Util.getPointValue2(this.fu.getParameter(new StringBuilder("field").append(fieldid).toString()), thetempvalue) + ",";
              newMap.put(fieldname, Util.getPointValue2(this.fu.getParameter("field" + fieldid), thetempvalue));
            }
            else
            {
              if (newMap.get(fieldname) == null)
                updateclause = updateclause + fieldname + " = NULL,";
              newMap.put(fieldname, null);
            }

          }
          else if (fieldhtmltype.equals("6"))
          {
            ishtml = "";

            if (this.isRequest)
              ishtml = Util.null2String(this.request.getParameter("field" + fieldid));
            else {
              ishtml = Util.null2String(this.fu.getParameter("field" + fieldid));
            }

            String[] oldUploadIdsStrs = null;
            DocExtUtil docExtUtil = new DocExtUtil();
            oldUploadIdsStrs_ = "";
            rsUploadId = new RecordSet();
            if (this.isbill == 1)
              rsUploadId.executeSql("select " + fieldname + " from " + this.billtablename + " where requestid = " + this.requestid);
            else {
              rsUploadId.executeSql("select " + fieldname + " from workflow_form where requestid = " + this.requestid);
            }
            if (rsUploadId.next()) {
              oldUploadIdsStrs_ = rsUploadId.getString(fieldname);
            }
            if ((oldUploadIdsStrs_ != null) && (!("".equals(oldUploadIdsStrs_)))) {
              oldUploadIdsStrs = Util.TokenizerString2(oldUploadIdsStrs_, ",");
            }
            if (!(ishtml.equals(""))) {
              if (newMap.get(fieldname) == null)
                updateclause = updateclause + fieldname + " = '" + ishtml + "',";
              newMap.put(fieldname, ishtml);
            }
            else if (ishtml.equals("")) {
              if (newMap.get(fieldname) == null)
                updateclause = updateclause + fieldname + " = '',";
              newMap.put(fieldname, "");
            }

            if (oldUploadIdsStrs != null) {
              for (y = 0; y < oldUploadIdsStrs.length; ++y) {
                if ((ishtml.indexOf(oldUploadIdsStrs[y]) != -1) || 
                  (Util.getIntValue(oldUploadIdsStrs[y], 0) == 0)) continue;
                clientIp = "";
                if (this.isRequest) {
                  if (this.request != null)
                    clientIp = Util.null2String(this.request.getRemoteAddr());
                }
                else if (this.fu != null) {
                  clientIp = Util.null2String(this.fu.getRemoteAddr());
                }
                docExtUtil.deleteDoc(Integer.parseInt(oldUploadIdsStrs[y]), this.user, clientIp);
              }

            }

          }
          else if (fieldhtmltype.equals("9")) {
            ishtml = "";
            thetempvalue = "";
            if (this.isRequest)
              ishtml = Util.null2String(this.request.getParameter("field" + fieldid));
            else {
              ishtml = Util.null2String(this.fu.getParameter("field" + fieldid));
            }
            if (ishtml.equals(""))
              thetempvalue = this.nodeid + "////~~weaversplit~~////" + this.userid + "////~~weaversplit~~////" + "0" + "////~~weaversplit~~////" + 
                "////~~weaversplit~~////" + "0" + "////~~weaversplit~~////" + "0";
            else {
              thetempvalue = ishtml + "/////~~weaversplit~~/////" + this.nodeid + "////~~weaversplit~~////" + this.userid + "////~~weaversplit~~////" + "0" + "////~~weaversplit~~////" + 
                "////~~weaversplit~~////" + "0" + "////~~weaversplit~~////" + "0";
            }
            updateclause = updateclause + fieldname + " = '" + fillFullNull(thetempvalue) + "',";
            newMap.put(fieldname, thetempvalue);
          }
          else {
            ishtml = false;
            thetempvalue = "";
            if (this.workflowid == 1) {
              if (this.isRequest)
              {
                thetempvalue = Util.toHtml100(this.request.getParameter("field" + fieldid));
              }
              else {
                thetempvalue = Util.toHtml100(this.fu.getParameter("field" + fieldid));
              }
            }
            else if (this.isRequest)
            {
              if ((fieldhtmltype.equals("2")) && (fieldtype.equals("2")))
              {
                thetempvalue = Util.toHtml100(this.request.getParameter("field" + fieldid));
                thetempvalue = rePlaceWordMark(thetempvalue);
                ishtml = true;
              } else if ((fieldhtmltype.equals("3")) && (((fieldtype.equals("224")) || (fieldtype.equals("225")) || (fieldtype.equals("226")) || (fieldtype.equals("227"))))) {
                thetempvalue = Util.null2String(this.request.getParameter("field" + fieldid));
                thetempvalue = thetempvalue.replace("'", "''");
              }
              else if ((fieldhtmltype.equals("1")) && (fieldtype.equals("1"))) {
                thetempvalue = Util.toHtmlForWorkflow(this.request.getParameter("field" + fieldid));
              } else {
                thetempvalue = Util.StringReplace(Util.fromScreen2(this.request.getParameter("field" + fieldid), this.userlanguage), " ", "&nbsp;");
              }

            }
            else if ((fieldhtmltype.equals("2")) && (fieldtype.equals("2")))
            {
              thetempvalue = Util.toHtml100(this.fu.getParameter("field" + fieldid));
              thetempvalue = rePlaceWordMark(thetempvalue);
              ishtml = true;
            } else if ((fieldhtmltype.equals("3")) && (((fieldtype.equals("224")) || (fieldtype.equals("225")) || (fieldtype.equals("226")) || (fieldtype.equals("227"))))) {
              thetempvalue = Util.null2String(this.fu.getParameter("field" + fieldid));
            }
            else if ((fieldhtmltype.equals("1")) && (fieldtype.equals("1"))) {
              thetempvalue = Util.toHtmlForWorkflow(Util.htmlFilter4UTF8(this.fu.getParameter("field" + fieldid)));
            } else {
              thetempvalue = Util.StringReplace(Util.fromScreen2(Util.htmlFilter4UTF8(this.fu.getParameter("field" + fieldid)), this.userlanguage), " ", "&nbsp;");
            }

            thetempvalue = resourceAuthorityFilter(fieldhtmltype, fieldtype, thetempvalue);
            if (thetempvalue.equals("")) thetempvalue = " ";

            if (ishtml) {
              this.htmlfieldMap.put(fieldname, thetempvalue);
            }
            else if (newMap.get(fieldname) == null) {
              updateclause = updateclause + fieldname + " = '" + thetempvalue + "',";
            }
            newMap.put(fieldname, thetempvalue);
          }

          if (((fieldhtmltype.equals("1")) && ("1".equals(fieldtype))) || ((fieldhtmltype.equals("2")) && (!("2".equals(fieldtype))) && (this.isoracle))) {
            fieldTypeCache.put(fieldname, fielddbtype);
          }

        }

      }

      String nowtime = this.currentdate + " " + this.currenttime;

      if ((!("1".equals(this.iscreate))) && (this.isStart) && (this.isTrack)) {
        List cprList = new ArrayList();

        Iterator it = newMap.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry entry = (Map.Entry)it.next();
          String key = entry.getKey().toString();
          String value = "";
          if (entry.getValue() != null) {
            if (String.valueOf(entry.getValue()).equals(" ")) value = "";
            else value = String.valueOf(entry.getValue());
          }
          Track t = (Track)oldMap.get(key);
          if ((((t != null) || (value == null))) && ((
            (t == null) || (t.getFieldOldText().equals(value)))))
            continue;
          if (t == null)
          {
            s = new StringBuffer();
            t = new Track();
            if (this.isbill == 1) {
              s.append("select t2.id, t2.fieldname, t2.fielddbtype, t2.fieldhtmltype, t2.type, t2.fieldlabel ");
              s.append("from  workflow_bill t1, workflow_billfield t2, workflow_form t3 ");
              s.append("where t1.id=t3.billformid and t2.billid = t1.id ");
              s.append("and t2.viewtype=0 and t3.requestid =" + this.requestid);
              s.append(" and t2.fieldname='" + key + "'");
            } else {
              s.append("select t1.id, t1.fieldname, t1.fielddbtype, t1.fieldhtmltype, t1.type,");
              s.append("(select fieldlable from workflow_fieldlable t where t.langurageid = 7 and t.fieldid = t2.fieldid and t.formid = t2.formid) fieldNameCn,");
              s.append("(select fieldlable from workflow_fieldlable t where t.langurageid = 8 and t.fieldid = t2.fieldid and t.formid = t2.formid) fieldNameEn, ");
              s.append("(select fieldlable from workflow_fieldlable t where t.langurageid = 9 and t.fieldid = t2.fieldid and t.formid = t2.formid) fieldNameTw ");
              s.append("from workflow_formdict t1, workflow_formfield t2, workflow_form t3,workflow_fieldlable t4 ");
              s.append("where t1.id=t2.fieldid and t2.formid=t3.billformid and t4.langurageid = " + this.userlanguage + " and t4.fieldid = t2.fieldid and t4.formid = t2.formid and t3.requestid =" + this.requestid);
              s.append(" and t1.fieldname='" + key + "'");
            }

            this.executesuccess = this.rs.executeSql(s.toString());
            if (!(this.executesuccess)) {
              writeLog(s.toString());
              saveRequestLog("1");
              writeLog(s.toString());
              setMessage("126223");
              return false;
            }
            t.setFieldName(this.rs.getString("fieldname"));
            t.setFieldType(this.rs.getString("type"));
            t.setFieldHtmlType(this.rs.getString("fieldhtmltype"));
            t.setFieldId(this.rs.getInt("id"));

            if (this.isbill != 1) {
              t.setFieldNameCn(this.rs.getString("fieldNameCn"));
              t.setFieldNameEn(this.rs.getString("fieldNameEn"));
              t.setFieldNameTw(this.rs.getString("fieldNameTw"));
            }
            t.setNodeId(this.nodeid);
            t.setRequestId(this.requestid);
            t.setIsBill(this.isbill);

            if (this.isbill == 1)
              t.setFieldLableId(this.rs.getInt("fieldlabel"));
            t.setModifierIP(getIp());
            t.setOptKind(this.src);
          }
          t.setFieldNewText(String.valueOf(value));
          cprList.add(t);
        }

        for (int i = 0; i < cprList.size(); ++i) {
          Track t = (Track)cprList.get(i);

          if (t == null)
            continue;
          String tempfiledtype = Util.null2String(t.getFieldType());
          String tempfieldhtmltype = Util.null2String(t.getFieldHtmlType());
          String FieldOldText = Util.null2String(t.getFieldOldText());
          String FieldNewText = Util.null2String(t.getFieldNewText());
          if ((!(FieldOldText.equals(""))) && (!(FieldNewText.equals(""))) && (tempfieldhtmltype.equals("1")) && (((tempfiledtype.equals("3")) || (tempfiledtype.equals("4")) || (tempfiledtype.equals("5")))) && 
            (Util.getDoubleValue(FieldOldText) == Util.getDoubleValue(FieldNewText))) {
            continue;
          }

          s = new StringBuffer();

          s.append("insert into workflow_track (");
          s.append("optKind,requestId,nodeId,isBill,fieldLableId,");
          s.append("fieldId,fieldHtmlType,fieldType,fieldNameCn,fieldNameEn,fieldNameTw,fieldOldText,fieldNewText,");
          s.append("modifierType,agentId,modifierId,modifierIP,modifyTime");
          s.append(") values (");
          s.append(disposeSqlNull(t.getOptKind()) + ",");
          s.append(t.getRequestId() + ",");
          s.append(t.getNodeId() + ",");
          s.append(t.getIsBill() + ",");
          s.append(t.getFieldLableId() + ",");
          s.append(t.getFieldId() + ",");
          s.append(disposeSqlNull(t.getFieldHtmlType()) + ",");
          s.append(disposeSqlNull(t.getFieldType()) + ",");
          s.append(disposeSqlNull(t.getFieldNameCn()) + ",");
          s.append(disposeSqlNull(t.getFieldNameEn()) + ",");
          s.append(disposeSqlNull(t.getFieldNameTw()) + ",");
          s.append(disposeSqlNull(Util.null2String(t.getFieldOldText())) + ",");
          s.append(disposeSqlNull(Util.null2String(t.getFieldNewText())) + ",");
          s.append(this.usertype + ",");
          s.append(this.agentId + ",");
          s.append(this.userid + ",");
          s.append(disposeSqlNull(t.getModifierIP()) + ",");
          s.append(disposeSqlNull(nowtime));
          s.append(")");

          this.executesuccess = this.rs.executeSql(s.toString());
          if (!(this.executesuccess)) {
            writeLog(s.toString());
            saveRequestLog("1");
          }

        }

      }

      if (!(updateclause.equals(""))) {
        updateclause = updateclause.substring(0, updateclause.length() - 1);

        if (this.isbill == 1)
          updateclause = " update " + this.billtablename + " set " + updateclause + " where id = " + this.billid;
        else {
          updateclause = "update workflow_form set " + updateclause + " where requestid=" + this.requestid;
        }

        this.executesuccess = this.rs.executeSql(updateclause);
        if (!(this.executesuccess))
        {
          boolean _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(this.formid, this.billtablename, "", this.isbill, fieldTypeCache, updateclause);
          if (!(_result)) {
            setMessage("126222");
            saveRequestLog("1");
            return false;
          }
          fieldTypeCache.clear();

          this.executesuccess = this.rs.executeSql(updateclause);
          if (!(this.executesuccess)) {
            setMessage("126222");
            writeLog("执行出错的SQL:" + updateclause);

            saveRequestLog("1");
            return false;
          }

        }

        if (this.isoracle) {
          try {
            String hrmsql = "";
            if (this.isbill == 1)
              hrmsql = " update " + this.billtablename + " set";
            else {
              hrmsql = "update workflow_form set ";
            }
            int hrmindex = 0;
            String hrmsp = " ";
            fdit = typemap1.entrySet().iterator();
            while (fdit.hasNext()) {
              ++hrmindex;
              entry = (Map.Entry)fdit.next();
              key = entry.getKey().toString();
              value = "";
              if (entry.getValue() != null) {
                if (String.valueOf(entry.getValue()).equals(" ")) value = "";
                else value = String.valueOf(entry.getValue());
              }
              if (hrmindex > 1)
                hrmsql = hrmsql + hrmsp + " , " + key + "=? ";
              else {
                hrmsql = hrmsql + hrmsp + " " + key + "=? ";
              }
            }
            if (this.isbill == 1)
              hrmsql = hrmsql + " where id = " + this.billid;
            else {
              hrmsql = hrmsql + " where requestid=" + this.requestid;
            }

            if (hrmindex > 0) {
              ConnStatement hrmstatement = null;
              try {
                hrmstatement = new ConnStatement();
                hrmstatement.setStatementSql(hrmsql);
                fdit = typemap1.entrySet().iterator();
                hrmindex = 0;
                while (fdit.hasNext()) {
                  ++hrmindex;
                  entry = (Map.Entry)fdit.next();
                  key = entry.getKey().toString();
                  value = "";
                  if (entry.getValue() != null) {
                    if (String.valueOf(entry.getValue()).equals(" ")) value = "";
                    else value = String.valueOf(entry.getValue());
                  }
                  hrmstatement.setString(hrmindex, value);
                }
                hrmstatement.executeUpdate();
              } catch (Exception e) {
                _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(this.formid, this.billtablename, "", this.isbill, null, null);
                if (!(_result)) {
                  setMessage("126222");
                  saveRequestLog("1");
                  return false;
                }
                try {
                  hrmstatement.executeUpdate();
                } catch (Exception ee) {
                  writeLog(e);
                  saveRequestLog("1");
                  writeLog("执行出错的SQL:" + hrmsql);

                  setMessage("126222");

                  if (hrmstatement != null) hrmstatement.close();
                  return false;
                }
              } finally {
                if (hrmstatement != null) hrmstatement.close();
              }
            }
          } catch (Exception e) {
            writeLog(e);
            saveRequestLog("1");
            setMessage("126222");
            return false;
          }

        }

      }

      if (this.isoracle) {
        try {
          String locatesql = "";
          if (this.isbill == 1)
            locatesql = " update " + this.billtablename + " set";
          else {
            locatesql = "update workflow_form set ";
          }
          int locateindex = 0;
          String locatesp = " ";
          fdit = locationMap.entrySet().iterator();
          while (fdit.hasNext()) {
            ++locateindex;
            entry = (Map.Entry)fdit.next();
            e = entry.getKey().toString();
            _result = "";
            if (entry.getValue() != null) {
              if (String.valueOf(entry.getValue()).equals(" ")) _result = "";
              else _result = String.valueOf(entry.getValue());
            }
            if (locateindex > 1)
              locatesql = locatesql + locatesp + " , " + e + "=? ";
            else {
              locatesql = locatesql + locatesp + " " + e + "=? ";
            }
          }
          if (this.isbill == 1)
            locatesql = locatesql + " where id = " + this.billid;
          else {
            locatesql = locatesql + " where requestid=" + this.requestid;
          }

          if (locateindex > 0) {
            ConnStatement locateStatement = null;
            try {
              locateStatement = new ConnStatement();
              locateStatement.setStatementSql(locatesql);
              fdit = locationMap.entrySet().iterator();
              locateindex = 0;
              while (fdit.hasNext()) {
                ++locateindex;
                e = (Map.Entry)fdit.next();
                _result = e.getKey().toString();
                ee = "";
                if (e.getValue() != null) {
                  if (String.valueOf(e.getValue()).equals(" ")) ee = "";
                  else ee = String.valueOf(e.getValue());
                }
                locateStatement.setString(locateindex, ee);
              }
              locateStatement.executeUpdate();
            } catch (Exception e) {
              writeLog(e);
              saveRequestLog("1");
              return false;
            } finally {
              if (locateStatement != null) locateStatement.close();
            }
          }
        } catch (Exception locatesql) {
          writeLog(e);
          saveRequestLog("1");
          return false;
        }
      }

      try
      {
        String fdsql = "";
        if (this.isbill == 1)
          fdsql = " update " + this.billtablename + " set ";
        else {
          fdsql = "update workflow_form set ";
        }
        int fdcn = 0;
        String fdsp = "";
        fdit = this.htmlfieldMap.entrySet().iterator();
        while (fdit.hasNext()) {
          ++fdcn;
          entry = (Map.Entry)fdit.next();
          e = entry.getKey().toString();
          _result = "";
          if (entry.getValue() != null) {
            if (String.valueOf(entry.getValue()).equals(" ")) _result = "";
            else _result = String.valueOf(entry.getValue());
          }
          fdsql = fdsql + fdsp + " " + e + "=? ";
          fdsp = ",";
        }
        if (this.isbill == 1)
          fdsql = fdsql + " where id = " + this.billid;
        else {
          fdsql = fdsql + " where requestid=" + this.requestid;
        }

        if (fdcn > 0) {
          ConnStatement fdst = null;
          try {
            fdst = new ConnStatement();
            fdst.setStatementSql(fdsql);
            fdit = this.htmlfieldMap.entrySet().iterator();
            fdcn = 0;
            while (fdit.hasNext()) {
              ++fdcn;
              e = (Map.Entry)fdit.next();
              _result = e.getKey().toString();
              ee = "";
              if (e.getValue() != null) {
                if (String.valueOf(e.getValue()).equals(" ")) ee = "";
                else ee = String.valueOf(e.getValue());
              }
              fdst.setString(fdcn, Util.htmlFilter4UTF8(ee));
            }
            fdst.executeUpdate();
          } catch (Exception e) {
            _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(this.formid, this.billtablename, "", this.isbill, null, null);
            if (!(_result)) {
              setMessage("126222");
              saveRequestLog("1");
              return false;
            }
            try {
              fdst.executeUpdate();
            } catch (Exception ee) {
              writeLog(ee);
              saveRequestLog("1");
              writeLog("执行出错的SQL:" + fdsql);

              setMessage("126222");

              if (fdst != null) fdst.close();
              return false;
            }
          } finally {
            if (fdst != null) fdst.close();
          }
        }
      } catch (Exception fdsql) {
        writeLog(e);
        saveRequestLog("1");
        setMessage("126222");
        return false;
      }

      if ((this.src.equals("save")) || (this.src.equals("submit")))
      {
        newMap = new HashMap();
        oldMap = new HashMap();
        Map dtMap = new HashMap();
        List fields = new ArrayList();
        try
        {
          label17219: String fieldname1;
          String fieldvalue1;
          String fieldhtmltype1;
          String fielddbtype1;
          String type1;
          int j;
          Map.Entry entry;
          String key;
          String value;
          if (this.isbill == 1) {
            fieldinfo.setRequestid(this.requestid);
            fieldinfo.GetDetailTableField(this.formid, this.isbill, this.userlanguage);
            ArrayList detailfieldids = fieldinfo.getDetailTableFields();
            ArrayList detailfieldnames = fieldinfo.getDetailDBFieldNames();
            ArrayList detaildbtypes = fieldinfo.getDetailFieldDBTypes();
            ArrayList detailtables = fieldinfo.getDetailTableNames();
            ArrayList detailkeys = fieldinfo.getDetailTableKeys();
            boolean isexpbill = getModeid(this.workflowid, this.nodeid, this.formid, this.isbill);

            String fieldInfo_skipDetailTableNames_formid7 = "";
            try {
              if ((this.isbill == 1) && (this.formid == 7))
                fieldInfo_skipDetailTableNames_formid7 = Util.null2String(new String(
                  Util.null2String(getPropValue("FieldInfo", "fieldInfo_skipDetailTableNames_formid7")).getBytes("ISO-8859-1"), "gbk")).trim().toLowerCase();
            }
            catch (Exception ex1) {
              writeLog(ex1);
            }

            for (int i = 0; i < detailfieldids.size(); ++i) {
              this.temprowindex = 0;
              ArrayList fieldids = (ArrayList)detailfieldids.get(i);
              ArrayList fieldnames = (ArrayList)detailfieldnames.get(i);
              ArrayList fielddbtypes = (ArrayList)detaildbtypes.get(i);
              String detailtable = (String)detailtables.get(i);
              String detailkey = (String)detailkeys.get(i);
              try {
                if ((this.isbill != 1) || (this.formid != 7) || ("".equals(Util.null2String(detailtable).trim())) || ("".equals(fieldInfo_skipDetailTableNames_formid7)) || 
                  ("," + fieldInfo_skipDetailTableNames_formid7 + ",".toLowerCase().indexOf("," + Util.null2String(detailtable).trim() + ",".toLowerCase()) < 0))
                  break label17219;
              }
              catch (Exception detailrs)
              {
                boolean hasMultiDoc;
                int j;
                boolean _result;
                int indxno;
                String dtid;
                int digitsIndex;
                String tempvalueid;
                String hrmsql;
                int decimaldigits;
                int hrmindex;
                Iterator fdit;
                Map.Entry entry;
                ConnStatement hrmsta;
                String key;
                Map.Entry entry;
                String value;
                String key;
                String value;
                writeLog(ex1);

                if ((this.isStart) && (this.isTrack)) {
                  fields = new ArrayList();
                  for (int l = 0; l < fieldids.size(); ++l) {
                    ArrayList dtlfieldids = Util.TokenizerString((String)fieldids.get(l), "_");
                    Trackdetail td = new Trackdetail();
                    td.setFieldName((String)fieldnames.get(l));
                    td.setFieldType((String)dtlfieldids.get(2));
                    td.setFieldHtmlType((String)dtlfieldids.get(3));
                    td.setFieldId(Util.getIntValue(((String)dtlfieldids.get(0)).substring(5)));
                    td.setFieldGroupId(i);
                    td.setNodeId(this.nodeid);
                    td.setRequestId(this.requestid);
                    td.setIsBill(this.isbill);
                    td.setModifierIP(getIp());
                    td.setOptKind(this.src);
                    td.setModifyTime(nowtime);
                    fields.add(td);
                  }

                }

                if ((detailkey == null) || (detailkey.trim().equals(""))) {
                  detailkey = "mainid";
                }
                if (this.formid == 201) {
                  detailkey = "detailrequestid";
                }

                if ((!(isexpbill)) && (((detailtable.indexOf("formtable_main_") != -1) || (detailtable.startsWith("uf_"))))) isexpbill = true;
                RecordSet detailrs = new RecordSet();
                int rowsum = -1;

                String fieldid1 = "";
                fieldname1 = "";
                fieldvalue1 = "";
                fieldhtmltype1 = "";
                fielddbtype1 = "";
                type1 = "";
                if (this.iscreate.equals("1")) isexpbill = false;
                if (isexpbill)
                {
                  String submitids = "";
                  if (this.isRequest)
                    submitids = Util.null2String(this.request.getParameter("submitdtlid" + i));
                  else {
                    submitids = Util.null2String(this.fu.getParameter("submitdtlid" + i));
                  }
                  String[] submitidAy = Util.TokenizerString2(submitids, ",");

                  String deldtlids = "";
                  if (deldtlids.equals("")) {
                    if (this.isRequest)
                      deldtlids = Util.null2String(this.request.getParameter("deldtlid" + i));
                    else
                      deldtlids = Util.null2String(this.fu.getParameter("deldtlid" + i));
                  } else {
                    String tempdeldtlids = "";
                    if (this.isRequest)
                      tempdeldtlids = Util.null2String(this.request.getParameter("deldtlid" + i));
                    else
                      tempdeldtlids = Util.null2String(this.fu.getParameter("deldtlid" + i));
                    if (!(tempdeldtlids.equals("")))
                      deldtlids = deldtlids + "," + tempdeldtlids;
                  }
                  if (!(deldtlids.equals(""))) {
                    deldtlids = deldtlids.replaceAll(",{2,}", ",");
                    if (deldtlids.indexOf(",") == 0) deldtlids = deldtlids.substring(1, deldtlids.length());
                    if (deldtlids.lastIndexOf(",") == deldtlids.length() - 1) deldtlids = deldtlids.substring(0, deldtlids.length() - 1);
                  }

                  for (int k = 0; k < submitidAy.length; ++k)
                  {
                    String Dtlid = "";
                    if (this.isRequest)
                      Dtlid = Util.null2String(this.request.getParameter("dtl_id_" + i + "_" + submitidAy[k]));
                    else {
                      Dtlid = Util.null2String(this.fu.getParameter("dtl_id_" + i + "_" + submitidAy[k]));
                    }

                    if (Dtlid.equals(""))
                    {
                      int m;
                      hasMultiDoc = false;
                      String sql1 = "insert into " + detailtable + " ( " + detailkey + ",";
                      String sql2 = " values (" + this.billid + ",";
                      if ((this.formid == 156) || (this.formid == 157) || (this.formid == 158) || (this.formid == 159)) {
                        int dsporder = 0;
                        this.rs.executeSql("select max(dsporder) from " + detailtable + " where " + detailkey + "=" + this.billid);
                        if (this.rs.next()) {
                          dsporder = this.rs.getInt(1) + 1;
                        }
                        sql1 = sql1 + "dsporder,";
                        sql2 = sql2 + dsporder + ",";
                      } else if (this.formid == 7) {
                        int rowno = 0;
                        this.rs.executeSql("select max(rowno) from " + detailtable + " where " + detailkey + "=" + this.billid);
                        if (this.rs.next()) {
                          rowno = this.rs.getInt(1) + 1;
                        }
                        sql1 = sql1 + "rowno,";
                        sql2 = sql2 + rowno + ",";
                      }

                      int nullLength = 0;
                      List newList = new ArrayList();
                      for (j = 0; j < fieldids.size(); ++j) {
                        fieldname1 = (String)fieldnames.get(j);
                        fieldid1 = (String)fieldids.get(j);
                        fielddbtype1 = (String)fielddbtypes.get(j);
                        indxno = fieldid1.lastIndexOf("_");
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
                        if (this.workflowid == 1) {
                          if (this.isRequest)
                          {
                            fieldvalue1 = Util.toHtml100(this.request.getParameter(fieldid1 + "_" + submitidAy[k]));
                          }
                          else {
                            fieldvalue1 = Util.toHtml100(this.fu.getParameter(fieldid1 + "_" + submitidAy[k]));
                          }
                        }
                        else if (this.isRequest)
                          fieldvalue1 = Util.fromScreen2(this.request.getParameter(fieldid1 + "_" + submitidAy[k]), this.userlanguage);
                        else {
                          fieldvalue1 = Util.fromScreen2(this.fu.getParameter(fieldid1 + "_" + submitidAy[k]), this.userlanguage);
                        }

                        if (((fieldhtmltype1.equals("1")) && ("1".equals(type1))) || ((fieldhtmltype1.equals("2")) && ("1".equals(type1)) && (this.isoracle))) {
                          fieldTypeCache.put(fieldname1, fielddbtype1);
                        }

                        fieldvalue1 = resourceAuthorityFilter(fieldhtmltype1, type1, fieldvalue1);

                        if (fieldvalue1.trim().equals("")) {
                          ++nullLength;
                          if ((!(fieldhtmltype1.equals("2"))) && (((!(fieldhtmltype1.equals("1"))) || ((!(type1.equals("1"))) && (!(type1.equals("5")))))) && (((!(fieldhtmltype1.equals("3"))) || ((!(type1.equals("2"))) && (!(type1.equals("19"))) && (!(type1.equals("161"))) && (!(type1.equals("162"))))))) {
                            fieldvalue1 = "NULL";
                          }
                          if (fieldhtmltype1.equals("5")) {
                            fieldvalue1 = "NULL";
                          } else if (fieldhtmltype1.equals("4")) {
                            fieldvalue1 = "0";
                            --nullLength;
                          }
                        }
                        else if (((!(fielddbtype1.toLowerCase().startsWith("browser."))) && (fielddbtype1.toUpperCase().indexOf("NUMBER") >= 0)) || (fielddbtype1.toUpperCase().indexOf("FLOAT") >= 0) || (fielddbtype1.toUpperCase().indexOf("DECIMAL") >= 0)) {
                          digitsIndex = fielddbtype1.indexOf(",");
                          decimaldigits = 2;
                          if (digitsIndex > -1)
                            decimaldigits = Util.getIntValue(fielddbtype1.substring(digitsIndex + 1, fielddbtype1.length() - 1).trim(), 2);
                          else {
                            decimaldigits = 2;
                          }
                          fieldvalue1 = Util.getPointValue2(fieldvalue1, decimaldigits);
                        }

                        if (j == fieldids.size() - 1) {
                          if ((((fielddbtype1.toLowerCase().startsWith("text")) || (fielddbtype1.toLowerCase().startsWith("char")) || (fielddbtype1.toLowerCase().startsWith("varchar")) || (fielddbtype1.toLowerCase().startsWith("browser")))) && (!("NULL".equals(fieldvalue1)))) {
                            sql1 = sql1 + fieldname1 + ")";
                            sql2 = sql2 + "'" + Util.toHtmlForSpace(Util.htmlFilter4UTF8(Util.null2String(fieldvalue1))) + "')";
                          } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("256")) || (type1.equals("257"))))) {
                            sql1 = sql1 + fieldname1 + ")";
                            sql2 = sql2 + "'" + Util.htmlFilter4UTF8(Util.null2String(fieldvalue1)) + "')";
                          } else if ((fieldhtmltype1.equals("3")) && (type1.equals("17")) && (this.isoracle)) {
                            if ("NULL".equals(fieldvalue1)) fieldvalue1 = "";
                            sql1 = sql1 + fieldname1 + " )";
                            sql2 = sql2 + " '' )";
                            if (!(fieldvalue1.equals("NULL")))
                              dtMap.put(fieldname1, fieldvalue1);
                          }
                          else {
                            sql1 = sql1 + fieldname1 + ")";
                            sql2 = sql2 + fieldvalue1 + ")";
                          }

                        }
                        else if ((((fielddbtype1.toLowerCase().startsWith("text")) || (fielddbtype1.toLowerCase().startsWith("char")) || (fielddbtype1.toLowerCase().startsWith("varchar")) || (fielddbtype1.toLowerCase().startsWith("browser")))) && (!("NULL".equals(fieldvalue1)))) {
                          sql1 = sql1 + fieldname1 + ",";
                          sql2 = sql2 + "'" + Util.toHtmlForSpace(Util.htmlFilter4UTF8(Util.null2String(fieldvalue1))) + "',";
                        } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("256")) || (type1.equals("257"))))) {
                          sql1 = sql1 + fieldname1 + ",";
                          sql2 = sql2 + "'" + Util.htmlFilter4UTF8(Util.null2String(fieldvalue1)) + "',";
                        } else if ((fieldhtmltype1.equals("3")) && (type1.equals("17")) && (this.isoracle)) {
                          if ("NULL".equals(fieldvalue1)) fieldvalue1 = "";
                          sql1 = sql1 + fieldname1 + " ,";
                          sql2 = sql2 + " '' ,";
                          if (!(fieldvalue1.equals("NULL")))
                            dtMap.put(fieldname1, fieldvalue1);
                        }
                        else {
                          sql1 = sql1 + fieldname1 + ",";
                          sql2 = sql2 + fieldvalue1 + ",";
                        }

                        tempvalueid = Util.null2String(fieldvalue1);
                        if ((fieldhtmltype1.equals("3")) && (((type1.equals("1")) || (type1.equals("17"))))) {
                          if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                          {
                            RequestManager tmp20041_20040 = this; tmp20041_20040.hrmids = tmp20041_20040.hrmids + "," + tempvalueid;
                          }

                        }
                        else if ((fieldhtmltype1.equals("3")) && (((type1.equals("7")) || (type1.equals("18"))))) {
                          if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                          {
                            RequestManager tmp20130_20129 = this; tmp20130_20129.crmids = tmp20130_20129.crmids + "," + tempvalueid; }
                        } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("8")) || (type1.equals("135"))))) {
                          if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                          {
                            RequestManager tmp20220_20219 = this; tmp20220_20219.prjids = tmp20220_20219.prjids + "," + tempvalueid; }
                        } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("9")) || (type1.equals("37"))))) {
                          if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                          {
                            RequestManager tmp20310_20309 = this; tmp20310_20309.docids = tmp20310_20309.docids + "," + tempvalueid; }
                        } else if ((fieldhtmltype1.equals("3")) && (type1.equals("23")) && 
                          (!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                        {
                          RequestManager tmp20389_20388 = this; tmp20389_20388.cptids = tmp20389_20388.cptids + "," + tempvalueid;
                        }
                        if ("field" + this.isMultiDoc.equals(fieldid1 + "_" + submitidAy[k]))
                        {
                          hasMultiDoc = true;
                          this.docrowindex = this.temprowindex;
                        }

                        if ((this.isStart) && (this.isTrack)) {
                          for (m = 0; m < fields.size(); ++m) {
                            Trackdetail td = (Trackdetail)fields.get(m);
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

                      }

                      if ((nullLength == fieldids.size()) && (!(hasMultiDoc)))
                        continue;
                      this.temprowindex += 1;
                      this.executesuccess = detailrs.executeSql(sql1 + sql2);
                      if (!(this.executesuccess))
                      {
                        _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(this.formid, this.billtablename, detailtable, this.isbill, null, null);
                        if (!(_result)) {
                          saveRequestLog("1");
                          setMessage("126222");
                          return false;
                        }
                        this.executesuccess = detailrs.executeSql(sql1 + sql2);
                        if (!(this.executesuccess)) {
                          setMessage("126222");
                          writeLog("执行出错的SQL:" + sql1 + sql2);

                          saveRequestLog("1");
                          return false;
                        }
                      }
                      int detailRowNum = 0;
                      detailrs.executeSql("select max(id) dtid from " + detailtable + " where " + detailkey + " =" + this.billid);
                      if (detailrs.next()) {
                        detailRowNum = Util.getIntValue(detailrs.getString("dtid"));
                      }
                      this.newAddDetailRowPerInfo.put("dtl_id_" + i + "_" + submitidAy[k], Integer.valueOf(detailRowNum));

                      if (this.isoracle)
                      {
                        try
                        {
                          if (detailRowNum > 0)
                          {
                            dtid = detailRowNum;
                            if (!("".equals(dtid))) {
                              hrmsql = "update " + detailtable + " set ";
                              hrmindex = 0;
                              hrmsp = " ";
                              fdit = dtMap.entrySet().iterator();
                              while (fdit.hasNext()) {
                                ++hrmindex;
                                entry = (Map.Entry)fdit.next();
                                key = entry.getKey().toString();
                                value = "";
                                if (entry.getValue() != null) {
                                  if (String.valueOf(entry.getValue()).equals(" ")) value = "";
                                  else value = String.valueOf(entry.getValue());
                                }
                                hrmsql = hrmsql + hrmsp + " " + key + "=? ";
                                hrmsp = ",";
                              }
                              hrmsql = hrmsql + " where id = " + dtid + " and " + detailkey + " = " + this.billid;

                              if (hrmindex > 0) {
                                hrmsta = null;
                                try {
                                  hrmsta = new ConnStatement();
                                  hrmsta.setStatementSql(hrmsql);
                                  fdit = dtMap.entrySet().iterator();
                                  hrmindex = 0;
                                  while (fdit.hasNext()) {
                                    ++hrmindex;
                                    entry = (Map.Entry)fdit.next();
                                    key = entry.getKey().toString();
                                    value = "";
                                    if (entry.getValue() != null) {
                                      if (String.valueOf(entry.getValue()).equals(" ")) value = "";
                                      else value = String.valueOf(entry.getValue());
                                    }
                                    hrmsta.setString(hrmindex, value);
                                  }
                                  hrmsta.executeUpdate();
                                } catch (Exception e) {
                                  writeLog(e);
                                  saveRequestLog("1");
                                  setMessage("126222");
                                  return false;
                                } finally {
                                  if (hrmsta != null) hrmsta.close();
                                }
                              }
                            }
                          }
                        } catch (Exception e) {
                          writeLog(e);
                          saveRequestLog("1");
                          setMessage("126223");
                          return false;
                        }

                      }

                      if ((this.isStart) && (this.isTrack)) {
                        this.sn += 1;

                        for (int j = 0; j < fields.size(); ++j) {
                          Trackdetail td = (Trackdetail)fields.get(j);
                          td.setOptType(1);
                          for (hrmindex = 0; hrmindex < newList.size(); ++hrmindex) {
                            Trackdetail tmptd = (Trackdetail)newList.get(hrmindex);

                            if (td.getFieldName().equals(tmptd.getFieldName())) {
                              td.setFieldNewText(tmptd.getFieldNewText());
                            }
                          }
                          if (!(insertDetail(td))) {
                            return false;
                          }

                        }

                      }

                    }
                    else if (checkIdDel(deldtlids, Dtlid)) {
                      fieldTypeCache.clear();

                      String sql1 = "";
                      Map modifyMap = new HashMap();
                      for (int j = 0; j < fieldids.size(); ++j) {
                        fieldname1 = (String)fieldnames.get(j);
                        fieldid1 = (String)fieldids.get(j);
                        fielddbtype1 = (String)fielddbtypes.get(j);
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
                        if (this.workflowid == 1) {
                          if (this.isRequest)
                          {
                            fieldvalue1 = Util.null2String(this.request.getParameter(fieldid1 + "_" + submitidAy[k]));
                          }
                          else {
                            fieldvalue1 = Util.null2String(this.fu.getParameter(fieldid1 + "_" + submitidAy[k]));
                          }
                        }
                        else if (this.isRequest)
                          fieldvalue1 = Util.null2String(this.request.getParameter(fieldid1 + "_" + submitidAy[k]));
                        else {
                          fieldvalue1 = Util.null2String(this.fu.getParameter(fieldid1 + "_" + submitidAy[k]));
                        }

                        if (((fieldhtmltype1.equals("1")) && ("1".equals(type1))) || ((fieldhtmltype1.equals("2")) && ("1".equals(type1)) && (this.isoracle))) {
                          fieldTypeCache.put(fieldname1, fielddbtype1);
                        }

                        fieldvalue1 = resourceAuthorityFilter(fieldhtmltype1, type1, fieldvalue1);
                        String tempvalueid = Util.null2String(fieldvalue1);
                        if ((fieldhtmltype1.equals("3")) && (((type1.equals("1")) || (type1.equals("17"))))) {
                          if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                          {
                            RequestManager tmp22080_22079 = this; tmp22080_22079.hrmids = tmp22080_22079.hrmids + "," + tempvalueid;
                          }

                        }
                        else if ((fieldhtmltype1.equals("3")) && (((type1.equals("7")) || (type1.equals("18"))))) {
                          if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                          {
                            RequestManager tmp22169_22168 = this; tmp22169_22168.crmids = tmp22169_22168.crmids + "," + tempvalueid; }
                        } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("8")) || (type1.equals("135"))))) {
                          if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                          {
                            RequestManager tmp22259_22258 = this; tmp22259_22258.prjids = tmp22259_22258.prjids + "," + tempvalueid; }
                        } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("9")) || (type1.equals("37"))))) {
                          if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                          {
                            RequestManager tmp22349_22348 = this; tmp22349_22348.docids = tmp22349_22348.docids + "," + tempvalueid; }
                        } else if ((fieldhtmltype1.equals("3")) && (type1.equals("23")) && 
                          (!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                        {
                          RequestManager tmp22428_22427 = this; tmp22428_22427.cptids = tmp22428_22427.cptids + "," + tempvalueid;
                        }
                        if ((fieldid1.length() > 5) && (detaileditfields.indexOf(fieldid1.substring(5)) < 0)) {
                          continue;
                        }

                        if (fieldhtmltype1.equals("6"))
                        {
                          DocExtUtil docExtUtil = new DocExtUtil();
                          String[] oldUploadIdsStrs = null;
                          String oldUploadIdsStrs_ = "";
                          RecordSet rsUploadId = new RecordSet();

                          rsUploadId.executeSql("select " + fieldname1 + " from " + detailtable + " where id=" + Dtlid);

                          if (rsUploadId.next()) {
                            oldUploadIdsStrs_ = rsUploadId.getString(fieldname1);
                          }
                          if ((oldUploadIdsStrs_ != null) && (!("".equals(oldUploadIdsStrs_)))) {
                            oldUploadIdsStrs = Util.TokenizerString2(oldUploadIdsStrs_, ",");
                          }

                          if (oldUploadIdsStrs != null) {
                            for (int y = 0; y < oldUploadIdsStrs.length; ++y) {
                              if ((fieldvalue1.indexOf(oldUploadIdsStrs[y]) != -1) || 
                                (Util.getIntValue(oldUploadIdsStrs[y], 0) == 0)) continue;
                              clientIp = "";
                              if (this.isRequest) {
                                if (this.request != null)
                                  clientIp = Util.null2String(this.request.getRemoteAddr());
                              }
                              else if (this.fu != null) {
                                clientIp = Util.null2String(this.fu.getRemoteAddr());
                              }
                              docExtUtil.deleteDoc(Integer.parseInt(oldUploadIdsStrs[y]), this.user, clientIp);
                            }

                          }

                        }

                        if ("field" + this.isMultiDoc.equals(fieldid1 + "_" + submitidAy[k]))
                        {
                          this.docrowindex = this.temprowindex;
                        }

                        if (fieldvalue1.trim().equals("")) {
                          if ((!(fieldhtmltype1.equals("2"))) && (((!(fieldhtmltype1.equals("1"))) || ((!(type1.equals("1"))) && (!(type1.equals("5")))))) && (((!(fieldhtmltype1.equals("3"))) || ((!(type1.equals("2"))) && (!(type1.equals("19"))) && (!(type1.equals("161"))) && (!(type1.equals("162"))))))) {
                            fieldvalue1 = "NULL";
                          }
                          if (fieldhtmltype1.equals("5"))
                            fieldvalue1 = "NULL";
                          else if (fieldhtmltype1.equals("4")) {
                            fieldvalue1 = "0";
                          }
                        }
                        else if (((!(fielddbtype1.toLowerCase().startsWith("browser."))) && (fielddbtype1.toUpperCase().indexOf("NUMBER") >= 0)) || (fielddbtype1.toUpperCase().indexOf("FLOAT") >= 0) || (fielddbtype1.toUpperCase().indexOf("DECIMAL") >= 0)) {
                          int digitsIndex = fielddbtype1.indexOf(",");
                          int decimaldigits = 2;
                          if (digitsIndex > -1)
                            decimaldigits = Util.getIntValue(fielddbtype1.substring(digitsIndex + 1, fielddbtype1.length() - 1).trim(), 2);
                          else {
                            decimaldigits = 2;
                          }
                          fieldvalue1 = Util.getPointValue2(fieldvalue1, decimaldigits);
                        }

                        if (sql1.equals("")) {
                          if ((((fielddbtype1.startsWith("text")) || (fielddbtype1.startsWith("char")) || (fielddbtype1.startsWith("varchar")) || (fielddbtype1.startsWith("browser")))) && (!("NULL".equals(fieldvalue1)))) {
                            sql1 = sql1 + fieldname1 + "=" + "'" + Util.toHtmlForSpace(Util.toHtml10(Util.htmlFilter4UTF8(fieldvalue1))) + "' ";
                          } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("256")) || (type1.equals("257"))))) {
                            sql1 = sql1 + fieldname1 + "=" + "'" + Util.toHtml10(fieldvalue1) + "' ";
                          } else if ((fieldhtmltype1.equals("3")) && (type1.equals("17")) && (this.isoracle)) {
                            if ("NULL".equals(fieldvalue1)) fieldvalue1 = "";
                            sql1 = sql1 + fieldname1 + " = '' ";
                            if (!(fieldvalue1.equals("NULL")))
                              dtMap.put(fieldname1, fieldvalue1);
                          }
                          else {
                            sql1 = sql1 + fieldname1 + "=" + fieldvalue1 + " ";
                          }

                        }
                        else if ((((fielddbtype1.startsWith("text")) || (fielddbtype1.startsWith("char")) || (fielddbtype1.startsWith("varchar")) || (fielddbtype1.startsWith("browser")))) && (!("NULL".equals(fieldvalue1)))) {
                          sql1 = sql1 + "," + fieldname1 + "=" + "'" + Util.toHtmlForSpace(Util.toHtml10(Util.htmlFilter4UTF8(fieldvalue1))) + "' ";
                        } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("256")) || (type1.equals("257"))))) {
                          sql1 = sql1 + "," + fieldname1 + "=" + "'" + Util.toHtml10(Util.htmlFilter4UTF8(fieldvalue1)) + "' ";
                        } else if ((fieldhtmltype1.equals("3")) && (type1.equals("17")) && (this.isoracle)) {
                          if ("NULL".equals(fieldvalue1)) fieldvalue1 = "";
                          sql1 = sql1 + "," + fieldname1 + " = '' ";
                          if (!(fieldvalue1.equals("NULL")))
                            dtMap.put(fieldname1, fieldvalue1);
                        }
                        else {
                          sql1 = sql1 + "," + fieldname1 + "=" + fieldvalue1 + " ";
                        }

                        String tmpStr = Util.StringReplaceOnce(fieldvalue1, " ", "");
                        if (tmpStr.equals("NULL"))
                          tmpStr = "";
                        modifyMap.put(fieldname1, tmpStr);
                      }

                      if (!(sql1.equals(""))) {
                        if ((this.formid == 156) || (this.formid == 157) || (this.formid == 158) || (this.formid == 159)) {
                          sql1 = "update " + detailtable + " set " + sql1 + " where dsporder=" + Dtlid + " and " + detailkey + " =" + this.billid;
                          if ((this.isStart) && (this.isTrack)) updateOrDeleteDetailLog(fields, 2, i, this.billid, modifyMap, detailtable);
                        } else {
                          if (this.formid < 0)
                            sql1 = "update " + detailtable + " set " + sql1 + " where id=" + Dtlid + " and " + detailkey + " =" + this.billid;
                          else {
                            sql1 = "update " + detailtable + " set " + sql1 + " where id=" + Dtlid;
                          }
                          if ((this.isStart) && (this.isTrack)) updateOrDeleteDetailLog(fields, 2, i, Dtlid, modifyMap, detailtable);
                        }
                        this.temprowindex += 1;
                        this.executesuccess = this.rs.executeSql(sql1);
                        if (!(this.executesuccess))
                        {
                          boolean _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(this.formid, this.billtablename, detailtable, this.isbill, fieldTypeCache, sql1);
                          if (!(_result)) {
                            saveRequestLog("1");
                            setMessage("126222");

                            return false;
                          }
                          this.executesuccess = this.rs.executeSql(sql1);
                          if (!(this.executesuccess)) {
                            saveRequestLog("1");
                            writeLog("执行出错的SQL:" + sql1);

                            setMessage("126222");
                            return false;
                          }

                        }

                        if (!(this.isoracle)) continue;
                        try {
                          String hrmsql = " update " + detailtable + " set ";
                          int hrmindex = 0;
                          String hrmsp = " ";
                          Iterator fdit = dtMap.entrySet().iterator();
                          while (fdit.hasNext()) {
                            ++hrmindex;
                            entry = (Map.Entry)fdit.next();
                            key = entry.getKey().toString();
                            value = "";
                            if (entry.getValue() != null) {
                              if (String.valueOf(entry.getValue()).equals(" ")) value = "";
                              else value = String.valueOf(entry.getValue());
                            }
                            hrmsql = hrmsql + hrmsp + " " + key + "=? ";
                            hrmsp = ",";
                          }
                          if (this.formid < 0)
                            hrmsql = hrmsql + " where id = " + Dtlid + " and " + detailkey + " =" + this.billid;
                          else {
                            hrmsql = hrmsql + " where id = " + Dtlid;
                          }

                          if (hrmindex > 0) {
                            ConnStatement hrmstatement = null;
                            try {
                              hrmstatement = new ConnStatement();
                              hrmstatement.setStatementSql(hrmsql);
                              fdit = dtMap.entrySet().iterator();
                              hrmindex = 0;
                              while (fdit.hasNext()) {
                                ++hrmindex;
                                Map.Entry entry = (Map.Entry)fdit.next();
                                String key = entry.getKey().toString();
                                String value = "";
                                if (entry.getValue() != null) {
                                  if (String.valueOf(entry.getValue()).equals(" ")) value = "";
                                  else value = String.valueOf(entry.getValue());
                                }
                                hrmstatement.setString(hrmindex, value);
                              }
                              hrmstatement.executeUpdate();
                            } catch (Exception e) {
                              writeLog(e);
                              saveRequestLog("1");
                              setMessage("126223");
                              return false;
                            } finally {
                              if (hrmstatement != null) hrmstatement.close();
                            }
                          }
                        } catch (Exception e) {
                          writeLog(e);
                          saveRequestLog("1");
                          setMessage("126223");
                          return false;
                        }

                      }

                    }

                  }

                  if (!(deldtlids.equals(""))) {
                    if ((this.isStart) && (this.isTrack))
                    {
                      List tmpList = Util.TokenizerString(deldtlids, ",");
                      for (j = 0; j < tmpList.size(); ++j) {
                        updateOrDeleteDetailLog(fields, 3, i, tmpList.get(j).toString(), null, detailtable);
                      }
                    }

                    String sql1 = "";
                    if ((this.formid == 156) || (this.formid == 157) || (this.formid == 158) || (this.formid == 159))
                      sql1 = "delete from  " + detailtable + " where dsporder in(" + deldtlids + ") and " + detailkey + " =" + this.billid;
                    else {
                      sql1 = "delete from " + detailtable + " where id in(" + deldtlids + ")";
                    }

                    detailrs.executeSql(sql1);
                  }
                } else {
                  WFNodeDtlFieldManager wfndfm = new WFNodeDtlFieldManager();
                  wfndfm.setNodeid(this.nodeid);
                  wfndfm.setGroupid(i);
                  wfndfm.selectWfNodeDtlField();
                  String dtldelete = wfndfm.getIsdelete();
                  if ((detaileditfields.size() > 0) || (dtldelete.equals("1")) || (this.iscreate.equals("1"))) {
                    if (this.formid == 201) {
                      if (fieldids.size() > 0) detailrs.executeSql("delete from " + detailtable + " where " + detailkey + " =" + this.requestid);
                    }
                    else if (fieldids.size() > 0) detailrs.executeSql("delete from " + detailtable + " where " + detailkey + " =" + this.billid);

                    if (this.isRequest)
                      rowsum = Util.getIntValue(Util.null2String(this.request.getParameter("indexnum" + i)));
                    else
                      rowsum = Util.getIntValue(Util.null2String(this.fu.getParameter("indexnum" + i)));
                    String submitids = "";
                    if (this.isRequest)
                      submitids = Util.null2String(this.request.getParameter("submitdtlid" + i));
                    else {
                      submitids = Util.null2String(this.fu.getParameter("submitdtlid" + i));
                    }

                    String[] submitidAy = Util.TokenizerString2(submitids, ",");
                    for (int k = 0; k < submitidAy.length; ++k) {
                      hasMultiDoc = false;
                      int rowcx = Util.getIntValue(submitidAy[k]);
                      String sql1 = "insert into " + detailtable + " ( " + detailkey + ",";
                      String sql2 = " values (" + this.billid + ",";
                      if (this.formid == 201) {
                        sql2 = " values (" + this.requestid + ",";
                      }
                      if ((this.formid == 156) || (this.formid == 157) || (this.formid == 158) || (this.formid == 159)) {
                        sql1 = sql1 + "dsporder,";
                        sql2 = sql2 + rowcx + ",";
                      } else if (this.formid == 7) {
                        sql1 = sql1 + "rowno,";
                        sql2 = sql2 + rowcx + ",";
                      }
                      int nullLength = 0;
                      for (_result = false; _result < fieldids.size(); ++_result) {
                        fieldname1 = (String)fieldnames.get(_result);
                        fieldid1 = (String)fieldids.get(_result);
                        fielddbtype1 = (String)fielddbtypes.get(_result);
                        entry = fieldid1.lastIndexOf("_");
                        if (entry > -1) {
                          fieldhtmltype1 = fieldid1.substring(entry + 1);
                          fieldid1 = fieldid1.substring(0, entry);
                          entry = fieldid1.lastIndexOf("_");
                          if (entry > -1) {
                            type1 = fieldid1.substring(entry + 1);
                            fieldid1 = fieldid1.substring(0, entry);
                            entry = fieldid1.lastIndexOf("_");
                            if (entry > -1) {
                              fieldid1 = fieldid1.substring(0, entry);
                            }
                          }
                        }
                        if (this.workflowid == 1) {
                          if (this.isRequest)
                          {
                            fieldvalue1 = Util.toHtml100(this.request.getParameter(fieldid1 + "_" + rowcx));
                          }
                          else {
                            fieldvalue1 = Util.toHtml100(this.fu.getParameter(fieldid1 + "_" + rowcx));
                          }
                        }
                        else if (this.isRequest)
                          fieldvalue1 = Util.fromScreen2(this.request.getParameter(fieldid1 + "_" + rowcx), this.userlanguage);
                        else {
                          fieldvalue1 = Util.fromScreen2(this.fu.getParameter(fieldid1 + "_" + rowcx), this.userlanguage);
                        }

                        fieldvalue1 = resourceAuthorityFilter(fieldhtmltype1, type1, fieldvalue1);

                        if (fieldvalue1.trim().equals("")) {
                          ++nullLength;
                          if ((!(fieldhtmltype1.equals("2"))) && (((!(fieldhtmltype1.equals("1"))) || ((!(type1.equals("1"))) && (!(type1.equals("5")))))) && (((!(fieldhtmltype1.equals("3"))) || ((!(type1.equals("2"))) && (!(type1.equals("19"))) && (!(type1.equals("161"))) && (!(type1.equals("162"))))))) {
                            fieldvalue1 = "NULL";
                          }
                          if (fieldhtmltype1.equals("5")) {
                            fieldvalue1 = "NULL";
                          } else if (fieldhtmltype1.equals("4")) {
                            fieldvalue1 = "0";
                            --nullLength;
                          }
                        }
                        else if (((!(fielddbtype1.toLowerCase().startsWith("browser."))) && (fielddbtype1.toUpperCase().indexOf("NUMBER") >= 0)) || (fielddbtype1.toUpperCase().indexOf("FLOAT") >= 0) || (fielddbtype1.toUpperCase().indexOf("DECIMAL") >= 0)) {
                          key = fielddbtype1.indexOf(",");
                          value = 2;
                          if (key > -1)
                            value = Util.getIntValue(fielddbtype1.substring(key + 1, fielddbtype1.length() - 1).trim(), 2);
                          else {
                            value = 2;
                          }
                          fieldvalue1 = Util.getPointValue2(fieldvalue1, value);
                        }

                        if (_result == fieldids.size() - 1) {
                          if ((((fielddbtype1.toLowerCase().startsWith("text")) || (fielddbtype1.toLowerCase().startsWith("char")) || (fielddbtype1.toLowerCase().startsWith("varchar")) || (fielddbtype1.toLowerCase().startsWith("browser")))) && (!("NULL".equals(fieldvalue1)))) {
                            sql1 = sql1 + fieldname1 + ")";
                            sql2 = sql2 + "'" + Util.toHtmlForSpace(Util.htmlFilter4UTF8(Util.null2String(fieldvalue1))) + "')";
                          } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("256")) || (type1.equals("257"))))) {
                            sql1 = sql1 + fieldname1 + ")";
                            sql2 = sql2 + "'" + Util.htmlFilter4UTF8(Util.null2String(fieldvalue1)) + "')";
                          } else if ((fieldhtmltype1.equals("3")) && (type1.equals("17")) && (this.isoracle)) {
                            if ("NULL".equals(fieldvalue1)) fieldvalue1 = "";
                            sql1 = sql1 + fieldname1 + ")";
                            sql2 = sql2 + " '' )";
                            if (!(fieldvalue1.equals("NULL")))
                              dtMap.put(fieldname1, fieldvalue1);
                          }
                          else {
                            sql1 = sql1 + fieldname1 + ")";
                            sql2 = sql2 + fieldvalue1 + ")";
                          }

                        }
                        else if ((((fielddbtype1.toLowerCase().startsWith("text")) || (fielddbtype1.toLowerCase().startsWith("char")) || (fielddbtype1.toLowerCase().startsWith("varchar")) || (fielddbtype1.toLowerCase().startsWith("browser")))) && (!("NULL".equals(fieldvalue1)))) {
                          sql1 = sql1 + fieldname1 + ",";
                          sql2 = sql2 + "'" + Util.toHtmlForSpace(Util.htmlFilter4UTF8(Util.null2String(fieldvalue1))) + "',";
                        } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("256")) || (type1.equals("257"))))) {
                          sql1 = sql1 + fieldname1 + ",";
                          sql2 = sql2 + "'" + Util.htmlFilter4UTF8(Util.null2String(fieldvalue1)) + "',";
                        } else if ((fieldhtmltype1.equals("3")) && (type1.equals("17")) && (this.isoracle)) {
                          if ("NULL".equals(fieldvalue1)) fieldvalue1 = "";
                          sql1 = sql1 + fieldname1 + ",";
                          sql2 = sql2 + " '' ,";
                          if (!(fieldvalue1.equals("NULL")))
                            dtMap.put(fieldname1, fieldvalue1);
                        }
                        else {
                          sql1 = sql1 + fieldname1 + ",";
                          sql2 = sql2 + fieldvalue1 + ",";
                        }

                        key = Util.null2String(fieldvalue1);
                        if ((fieldhtmltype1.equals("3")) && (((type1.equals("1")) || (type1.equals("17"))))) {
                          if ((!(key.equals(""))) && (!(key.equals("NULL"))))
                          {
                            RequestManager tmp27209_27208 = this; tmp27209_27208.hrmids = tmp27209_27208.hrmids + "," + key;
                          }

                        }
                        else if ((fieldhtmltype1.equals("3")) && (((type1.equals("7")) || (type1.equals("18"))))) {
                          if ((!(key.equals(""))) && (!(key.equals("NULL"))))
                          {
                            RequestManager tmp27298_27297 = this; tmp27298_27297.crmids = tmp27298_27297.crmids + "," + key; }
                        } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("8")) || (type1.equals("135"))))) {
                          if ((!(key.equals(""))) && (!(key.equals("NULL"))))
                          {
                            RequestManager tmp27388_27387 = this; tmp27388_27387.prjids = tmp27388_27387.prjids + "," + key; }
                        } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("9")) || (type1.equals("37"))))) {
                          if ((!(key.equals(""))) && (!(key.equals("NULL"))))
                          {
                            RequestManager tmp27478_27477 = this; tmp27478_27477.docids = tmp27478_27477.docids + "," + key; }
                        } else if ((fieldhtmltype1.equals("3")) && (type1.equals("23")) && 
                          (!(key.equals(""))) && (!(key.equals("NULL"))))
                        {
                          RequestManager tmp27557_27556 = this; tmp27557_27556.cptids = tmp27557_27556.cptids + "," + key;
                        }
                        if (!("field" + this.isMultiDoc.equals(fieldid1 + "_" + rowcx)))
                          continue;
                        hasMultiDoc = true;
                        this.docrowindex = this.temprowindex;
                      }

                      if ((nullLength != fieldids.size()) || (hasMultiDoc))
                      {
                        this.temprowindex += 1;
                        this.executesuccess = detailrs.executeSql(sql1 + sql2);
                      }

                      if (!(this.executesuccess))
                      {
                        _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(this.formid, this.billtablename, detailtable, this.isbill, null, null);
                        if (!(_result)) {
                          saveRequestLog("1");
                          setMessage("126222");
                          return false;
                        }
                        this.executesuccess = detailrs.executeSql(sql1 + sql2);
                        if (!(this.executesuccess)) {
                          saveRequestLog("1");
                          writeLog("执行出错的SQL:" + sql1 + sql2);

                          setMessage("126222");
                          return false;
                        }

                      }

                      if (!(this.isoracle)) continue;
                      try {
                        String maxidsql = "select max(id) dtid from " + detailtable + " where " + detailkey + " =" + this.billid;
                        detailrs.executeSql(maxidsql);
                        if (detailrs.next()) {
                          entry = Util.null2String(detailrs.getString("dtid"));
                          if (!("".equals(entry))) {
                            key = "update " + detailtable + " set ";
                            value = 0;
                            hrmsp = " ";
                            fdit = dtMap.entrySet().iterator();
                            while (fdit.hasNext()) {
                              ++value;
                              hrmsta = (Map.Entry)fdit.next();
                              e = hrmsta.getKey().toString();
                              key = "";
                              if (hrmsta.getValue() != null) {
                                if (String.valueOf(hrmsta.getValue()).equals(" ")) key = "";
                                else key = String.valueOf(hrmsta.getValue());
                              }
                              key = key + hrmsp + " " + e + "=? ";
                              hrmsp = ",";
                            }
                            key = key + " where id = " + entry + " and " + detailkey + " = " + this.billid;

                            if (value > 0) {
                              hrmsta = null;
                              try {
                                hrmsta = new ConnStatement();
                                hrmsta.setStatementSql(key);
                                fdit = dtMap.entrySet().iterator();
                                value = 0;
                                while (fdit.hasNext()) {
                                  ++value;
                                  e = (Map.Entry)fdit.next();
                                  key = e.getKey().toString();
                                  value = "";
                                  if (e.getValue() != null) {
                                    if (String.valueOf(e.getValue()).equals(" ")) value = "";
                                    else value = String.valueOf(e.getValue());
                                  }
                                  hrmsta.setString(value, value);
                                }
                                hrmsta.executeUpdate();
                              } catch (Exception e) {
                                writeLog(e);
                                saveRequestLog("1");
                                setMessage("126223");
                                return false;
                              } finally {
                                if (hrmsta != null) hrmsta.close();
                              }
                            }
                          }
                        }
                      } catch (Exception e) {
                        writeLog(e);
                        saveRequestLog("1");
                        setMessage("126223");
                        return false;
                      }

                    }

                  }

                }

                dtMap.clear(); }
            }
            break label35068: }
          ArrayList fieldids1 = new ArrayList();
          ArrayList fieldnames1 = new ArrayList();
          ArrayList fieldhtmltypes1 = new ArrayList();
          ArrayList fielddbtypes = new ArrayList();
          ArrayList types1 = new ArrayList();
          int detailGroupId = 0;
          String deldtlids = "";
          int bflength = 0;
          int rows = 0;
          String sql = "select distinct groupId from workflow_formfield where formid=" + this.formid + " and isdetail='1' order by groupId";
          this.rs2.execute(sql);

          while (this.rs2.next()) {
            fields = new ArrayList();
            detailGroupId = this.rs2.getInt(1);

            sql = " select distinct a.fieldid, b.fieldname, b.fieldhtmltype, b.type, b.fielddbtype,a.groupId, (select fieldlable from workflow_fieldlable t where t.langurageid = 7 and t.fieldid = a.fieldid and t.formid = a.formid) fieldNameCn,(select fieldlable from workflow_fieldlable t where t.langurageid = 8 and t.fieldid = a.fieldid and t.formid = a.formid) fieldNameEn, (select fieldlable from workflow_fieldlable t where t.langurageid = 9 and t.fieldid = a.fieldid and t.formid = a.formid) fieldNameTw  from workflow_formfield a, workflow_formdictdetail b,workflow_fieldlable c  where a.isdetail='1' and a.fieldid=b.id and c.fieldid = a.fieldid and c.formid = a.formid and c.langurageid = " + 
              this.userlanguage + " and a.formid=" + this.formid + " and a.groupId=" + detailGroupId;

            this.rs.executeSql(sql);
            while (this.rs.next()) {
              fieldids1.add(Util.null2String(this.rs.getString("fieldid")));
              fieldnames1.add(Util.null2String(this.rs.getString("fieldname")));
              fieldhtmltypes1.add(Util.null2String(this.rs.getString("fieldhtmltype")));
              fielddbtypes.add(Util.null2String(this.rs.getString("fielddbtype")));
              types1.add(Util.null2String(this.rs.getString("type")));

              if ((this.isStart) && (this.isTrack)) {
                Trackdetail td = new Trackdetail();
                td.setFieldName(this.rs.getString("fieldname"));
                td.setFieldType(this.rs.getString("type"));
                td.setFieldHtmlType(this.rs.getString("fieldhtmltype"));
                td.setFieldId(this.rs.getInt("fieldid"));
                td.setFieldGroupId(this.rs.getInt("groupId"));

                if (this.isbill != 1) {
                  td.setFieldNameCn(this.rs.getString("fieldNameCn"));
                  td.setFieldNameEn(this.rs.getString("fieldNameEn"));
                  td.setFieldNameTw(this.rs.getString("fieldNameTw"));
                }
                td.setNodeId(this.nodeid);
                td.setRequestId(this.requestid);
                td.setIsBill(this.isbill);
                td.setModifierIP(getIp());
                td.setOptKind(this.src);
                td.setModifyTime(nowtime);
                fields.add(td);
              }

            }

            String submitids = "";
            if (this.isRequest)
              submitids = Util.null2String(this.request.getParameter("submitdtlid" + rows));
            else {
              submitids = Util.null2String(this.fu.getParameter("submitdtlid" + rows));
            }
            String[] submitidAy = Util.TokenizerString2(submitids, ",");

            if (deldtlids.equals("")) {
              if (this.isRequest)
                deldtlids = Util.null2String(this.request.getParameter("deldtlid" + detailGroupId));
              else
                deldtlids = Util.null2String(this.fu.getParameter("deldtlid" + detailGroupId));
            } else {
              String tempdeldtlids = "";
              if (this.isRequest)
                tempdeldtlids = Util.null2String(this.request.getParameter("deldtlid" + detailGroupId));
              else
                tempdeldtlids = Util.null2String(this.fu.getParameter("deldtlid" + detailGroupId));
              if (!(tempdeldtlids.equals("")))
                deldtlids = deldtlids + "," + tempdeldtlids;
            }
            if (!(deldtlids.equals(""))) {
              deldtlids = deldtlids.replaceAll(",{2,}", ",");
              if (deldtlids.indexOf(",") == 0) deldtlids = deldtlids.substring(1, deldtlids.length());
              if (deldtlids.lastIndexOf(",") == deldtlids.length() - 1) deldtlids = deldtlids.substring(0, deldtlids.length() - 1);
            }
            if ((bflength < deldtlids.length()) && (this.isStart) && (this.isTrack)) {
              if (bflength > 0) {
                ++bflength;
              }
              if (!(deldtlids.equals(""))) {
                List tmpList = Util.TokenizerString(deldtlids.substring(bflength), ",");
                for (int j = 0; j < tmpList.size(); ++j) {
                  updateOrDeleteDetailLog(fields, 3, detailGroupId, tmpList.get(j).toString(), null, "Workflow_formdetail");
                }
              }
            }

            bflength = deldtlids.length();

            this.temprowindex = 0;
            for (int i = 0; i < submitidAy.length; ++i)
            {
              String Dtlid = "";
              if (this.isRequest)
                Dtlid = Util.null2String(this.request.getParameter("dtl_id_" + detailGroupId + "_" + submitidAy[i]));
              else {
                Dtlid = Util.null2String(this.fu.getParameter("dtl_id_" + detailGroupId + "_" + submitidAy[i]));
              }

              if (Dtlid.equals(""))
              {
                int k;
                boolean hasMultiDoc = false;
                Object fieldid1 = null;
                fieldname1 = "";
                fieldvalue1 = "";
                fieldhtmltype1 = "";
                fielddbtype1 = "";
                type1 = "";
                String sql1 = "insert into Workflow_formdetail ( requestid,groupId,";
                String sql2 = " values (" + this.requestid + "," + detailGroupId + " ,";

                int nullLength = 0;
                List newList = new ArrayList();
                for (j = 0; j < fieldnames1.size(); ++j) {
                  fieldname1 = fieldnames1.get(j);
                  fieldid1 = fieldids1.get(j);
                  fieldhtmltype1 = fieldhtmltypes1.get(j);
                  type1 = types1.get(j);
                  fielddbtype1 = fielddbtypes.get(j);
                  if (this.isRequest)
                    fieldvalue1 = Util.null2String(this.request.getParameter("field" + fieldid1 + "_" + submitidAy[i]));
                  else {
                    fieldvalue1 = Util.null2String(this.fu.getParameter("field" + fieldid1 + "_" + submitidAy[i]));
                  }

                  fieldvalue1 = resourceAuthorityFilter(fieldhtmltype1, type1, fieldvalue1);

                  if (fieldvalue1.equals(""))
                  {
                    ++nullLength;
                    if ((!(fieldhtmltype1.equals("2"))) && (((!(fieldhtmltype1.equals("1"))) || ((!(type1.equals("1"))) && (!(type1.equals("5")))))) && (((!(fieldhtmltype1.equals("3"))) || ((!(type1.equals("2"))) && (!(type1.equals("19"))) && (!(type1.equals("161"))) && (!(type1.equals("162"))))))) {
                      fieldvalue1 = "NULL";
                    }

                    if (fieldhtmltype1.equals("5")) {
                      fieldvalue1 = "NULL";
                    } else if (fieldhtmltype1.equals("4")) {
                      fieldvalue1 = "0";
                      --nullLength;
                    }

                  }
                  else if (((!(fielddbtype1.toLowerCase().startsWith("browser."))) && (fielddbtype1.toUpperCase().indexOf("NUMBER") >= 0)) || (fielddbtype1.toUpperCase().indexOf("FLOAT") >= 0) || (fielddbtype1.toUpperCase().indexOf("DECIMAL") >= 0)) {
                    int digitsIndex = fielddbtype1.indexOf(",");
                    int decimaldigits = 2;
                    if (digitsIndex > -1)
                      decimaldigits = Util.getIntValue(fielddbtype1.substring(digitsIndex + 1, fielddbtype1.length() - 1).trim(), 2);
                    else {
                      decimaldigits = 2;
                    }
                    fieldvalue1 = Util.getPointValue2(fieldvalue1, decimaldigits);
                  }
                  if (j == fieldnames1.size() - 1) {
                    if ((((fielddbtype1.startsWith("text")) || (fielddbtype1.startsWith("char")) || (fielddbtype1.startsWith("varchar")) || (fielddbtype1.indexOf(".") > -1))) && (!("NULL".equals(fieldvalue1)))) {
                      sql1 = sql1 + fieldname1 + ")";
                      sql2 = sql2 + "'" + Util.toHtmlForSpace(Util.toHtml10(Util.htmlFilter4UTF8(fieldvalue1))) + "')";
                    } else if ((fieldhtmltype1.equals("3")) && (type1.equals("17")) && (this.isoracle)) {
                      if ("NULL".equals(fieldvalue1)) fieldvalue1 = "";
                      sql1 = sql1 + fieldname1 + " ) ";
                      sql2 = sql2 + " '' ) ";
                      if (!(fieldvalue1.equals("NULL")))
                        dtMap.put(fieldname1, fieldvalue1);
                    }
                    else {
                      sql1 = sql1 + fieldname1 + ")";
                      sql2 = sql2 + fieldvalue1 + ")";
                    }

                  }
                  else if ((((fielddbtype1.startsWith("text")) || (fielddbtype1.startsWith("char")) || (fielddbtype1.startsWith("varchar")) || (fielddbtype1.indexOf(".") > -1))) && (!("NULL".equals(fieldvalue1)))) {
                    sql1 = sql1 + fieldname1 + ",";
                    sql2 = sql2 + "'" + Util.toHtmlForSpace(Util.toHtml10(Util.htmlFilter4UTF8(fieldvalue1))) + "',";
                  } else if ((fieldhtmltype1.equals("3")) && (type1.equals("17")) && (this.isoracle)) {
                    if ("NULL".equals(fieldvalue1)) fieldvalue1 = "";
                    sql1 = sql1 + fieldname1 + ",";
                    sql2 = sql2 + " '' ,";
                    if (!(fieldvalue1.equals("NULL")))
                      dtMap.put(fieldname1, fieldvalue1);
                  }
                  else {
                    sql1 = sql1 + fieldname1 + ",";
                    sql2 = sql2 + fieldvalue1 + ",";
                  }

                  String tempvalueid = Util.null2String(fieldvalue1);
                  if ((fieldhtmltype1.equals("3")) && (((type1.equals("1")) || (type1.equals("17"))))) {
                    if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                    {
                      RequestManager tmp30821_30820 = this; tmp30821_30820.hrmids = tmp30821_30820.hrmids + "," + tempvalueid;
                    }

                  }
                  else if ((fieldhtmltype1.equals("3")) && (((type1.equals("7")) || (type1.equals("18"))))) {
                    if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                    {
                      RequestManager tmp30910_30909 = this; tmp30910_30909.crmids = tmp30910_30909.crmids + "," + tempvalueid; }
                  } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("8")) || (type1.equals("135"))))) {
                    if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                    {
                      RequestManager tmp31000_30999 = this; tmp31000_30999.prjids = tmp31000_30999.prjids + "," + tempvalueid; }
                  } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("9")) || (type1.equals("37"))))) {
                    if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                    {
                      RequestManager tmp31090_31089 = this; tmp31090_31089.docids = tmp31090_31089.docids + "," + tempvalueid; }
                  } else if ((fieldhtmltype1.equals("3")) && (type1.equals("23")) && 
                    (!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                  {
                    RequestManager tmp31169_31168 = this; tmp31169_31168.cptids = tmp31169_31168.cptids + "," + tempvalueid;
                  }
                  if (this.isMultiDoc.equals(fieldid1 + "_" + submitidAy[i]))
                  {
                    hasMultiDoc = true;
                    this.docrowindex = this.temprowindex;
                  }

                  if ((this.isStart) && (this.isTrack)) {
                    for (k = 0; k < fields.size(); ++k) {
                      Trackdetail td = (Trackdetail)fields.get(k);
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

                }

                if ((nullLength != fieldnames1.size()) || (hasMultiDoc))
                {
                  if ((this.isStart) && (this.isTrack)) {
                    this.sn += 1;

                    for (j = 0; j < fields.size(); ++j) {
                      Trackdetail td = (Trackdetail)fields.get(j);
                      td.setOptType(1);
                      for (k = 0; k < newList.size(); ++k) {
                        Trackdetail tmptd = (Trackdetail)newList.get(k);

                        if (td.getFieldName().equals(tmptd.getFieldName())) {
                          td.setFieldNewText(tmptd.getFieldNewText());
                        }
                      }
                      if (!(insertDetail(td))) {
                        return false;
                      }

                    }

                  }

                  this.temprowindex += 1;
                  this.executesuccess = this.rs.executeSql(sql1 + sql2);

                  if (!(this.executesuccess))
                  {
                    boolean _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(this.formid, this.billtablename, "workflow_formdetail", this.isbill, null, null);
                    if (!(_result)) {
                      saveRequestLog("1");
                      setMessage("126222");
                      return false;
                    }
                    this.executesuccess = this.rs.executeSql(sql1 + sql2);
                    if (!(this.executesuccess)) {
                      saveRequestLog("1");
                      writeLog("执行出错的SQL:" + sql1 + sql2);

                      setMessage("126222");
                      return false;
                    }

                  }

                  int detailRowNum = 0;
                  this.rs.executeSql("select max(id) dtid from Workflow_formdetail where requestid =" + this.requestid + " and groupId = " + detailGroupId);
                  if (this.rs.next()) {
                    detailRowNum = Util.getIntValue(this.rs.getString("dtid"));
                  }
                  this.newAddDetailRowPerInfo.put("dtl_id_" + detailGroupId + "_" + submitidAy[i], Integer.valueOf(detailRowNum));

                  if (this.isoracle)
                  {
                    try
                    {
                      if (detailRowNum > 0) {
                        String dtid = detailRowNum;
                        if (!("".equals(dtid))) {
                          String hrmsql = "update workflow_formdetail set ";
                          int hrmindex = 0;
                          String hrmsp = " ";
                          fdit = dtMap.entrySet().iterator();
                          while (fdit.hasNext()) {
                            ++hrmindex;
                            Map.Entry entry = (Map.Entry)fdit.next();
                            String key = entry.getKey().toString();
                            String value = "";
                            if (entry.getValue() != null) {
                              if (String.valueOf(entry.getValue()).equals(" ")) value = "";
                              else value = String.valueOf(entry.getValue());
                            }
                            hrmsql = hrmsql + hrmsp + " " + key + "=? ";
                            hrmsp = ",";
                          }
                          hrmsql = hrmsql + " where id = " + dtid + " and requestid = " + this.requestid + " and groupid = " + detailGroupId;

                          if (hrmindex > 0) {
                            ConnStatement hrmsta = null;
                            try {
                              hrmsta = new ConnStatement();
                              hrmsta.setStatementSql(hrmsql);
                              fdit = dtMap.entrySet().iterator();
                              hrmindex = 0;
                              while (fdit.hasNext()) {
                                ++hrmindex;
                                entry = (Map.Entry)fdit.next();
                                key = entry.getKey().toString();
                                value = "";
                                if (entry.getValue() != null) {
                                  if (String.valueOf(entry.getValue()).equals(" ")) value = "";
                                  else value = String.valueOf(entry.getValue());
                                }
                                hrmsta.setString(hrmindex, value);
                              }
                              hrmsta.executeUpdate();
                            } catch (Exception key) {
                              writeLog(e);
                              saveRequestLog("1");
                              setMessage("126223");
                              return false;
                            } finally {
                              if (hrmsta != null) hrmsta.close();
                            }
                          }
                        }
                      }
                    } catch (Exception e) {
                      writeLog(e);
                      saveRequestLog("1");
                      setMessage("126223");
                      return false;
                    }

                  }

                }

                if (this.executesuccess) break label34959;
                saveRequestLog("1");
                setMessage("126222");
                return false;
              }
              if (checkIdDel(deldtlids, Dtlid)) {
                fieldTypeCache.clear();

                Object fieldid1 = null;
                String fieldname1 = "";
                String fieldvalue1 = "";
                String fieldhtmltype1 = "";
                String fielddbtype1 = "";
                String type1 = "";
                String sql1 = "";
                Map modifyMap = new HashMap();

                for (int j = 0; j < fieldnames1.size(); ++j) {
                  fieldname1 = fieldnames1.get(j);
                  fieldid1 = fieldids1.get(j);
                  fieldhtmltype1 = fieldhtmltypes1.get(j);
                  type1 = types1.get(j);
                  fielddbtype1 = fielddbtypes.get(j);

                  if (this.isRequest)
                    fieldvalue1 = Util.null2String(this.request.getParameter("field" + fieldid1 + "_" + submitidAy[i]));
                  else
                    fieldvalue1 = Util.null2String(this.fu.getParameter("field" + fieldid1 + "_" + submitidAy[i]));
                  String tempvalueid = Util.null2String(fieldvalue1);
                  if ((fieldhtmltype1.equals("3")) && (((type1.equals("1")) || (type1.equals("17"))))) {
                    if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                    {
                      RequestManager tmp32661_32660 = this; tmp32661_32660.hrmids = tmp32661_32660.hrmids + "," + tempvalueid;
                    }

                  }
                  else if ((fieldhtmltype1.equals("3")) && (((type1.equals("7")) || (type1.equals("18"))))) {
                    if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                    {
                      RequestManager tmp32750_32749 = this; tmp32750_32749.crmids = tmp32750_32749.crmids + "," + tempvalueid; }
                  } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("8")) || (type1.equals("135"))))) {
                    if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                    {
                      RequestManager tmp32840_32839 = this; tmp32840_32839.prjids = tmp32840_32839.prjids + "," + tempvalueid; }
                  } else if ((fieldhtmltype1.equals("3")) && (((type1.equals("9")) || (type1.equals("37"))))) {
                    if ((!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                    {
                      RequestManager tmp32930_32929 = this; tmp32930_32929.docids = tmp32930_32929.docids + "," + tempvalueid; }
                  } else if ((fieldhtmltype1.equals("3")) && (type1.equals("23")) && 
                    (!(tempvalueid.equals(""))) && (!(tempvalueid.equals("NULL"))))
                  {
                    RequestManager tmp33009_33008 = this; tmp33009_33008.cptids = tmp33009_33008.cptids + "," + tempvalueid;
                  }
                  if (this.isMultiDoc.equals(fieldid1 + "_" + submitidAy[i]))
                  {
                    this.docrowindex = this.temprowindex;
                  }
                  if (detaileditfields.indexOf(fieldid1) < 0) {
                    continue;
                  }

                  if (((fieldhtmltype1.equals("1")) && ("1".equals(type1))) || ((fieldhtmltype1.equals("2")) && ("1".equals(type1)) && (this.isoracle))) {
                    fieldTypeCache.put(fieldname1, fielddbtype1);
                  }

                  if (fieldhtmltype1.equals("6"))
                  {
                    DocExtUtil docExtUtil = new DocExtUtil();
                    String[] oldUploadIdsStrs = null;
                    String oldUploadIdsStrs_ = "";
                    RecordSet rsUploadId = new RecordSet();

                    rsUploadId.executeSql("select " + fieldname1 + " from Workflow_formdetail where id=" + Dtlid);

                    if (rsUploadId.next()) {
                      oldUploadIdsStrs_ = rsUploadId.getString(fieldname1);
                    }
                    if ((oldUploadIdsStrs_ != null) && (!("".equals(oldUploadIdsStrs_)))) {
                      oldUploadIdsStrs = Util.TokenizerString2(oldUploadIdsStrs_, ",");
                    }

                    if (oldUploadIdsStrs != null) {
                      for (int y = 0; y < oldUploadIdsStrs.length; ++y) {
                        if ((fieldvalue1.indexOf(oldUploadIdsStrs[y]) != -1) || 
                          (Util.getIntValue(oldUploadIdsStrs[y], 0) == 0)) continue;
                        String clientIp = "";
                        if (this.isRequest) {
                          if (this.request != null)
                            clientIp = Util.null2String(this.request.getRemoteAddr());
                        }
                        else if (this.fu != null) {
                          clientIp = Util.null2String(this.fu.getRemoteAddr());
                        }
                        docExtUtil.deleteDoc(Integer.parseInt(oldUploadIdsStrs[y]), this.user, clientIp);
                      }

                    }

                  }

                  fieldvalue1 = resourceAuthorityFilter(fieldhtmltype1, type1, fieldvalue1);

                  if (fieldvalue1.equals("")) {
                    if ((!(fieldhtmltype1.equals("2"))) && (((!(fieldhtmltype1.equals("1"))) || ((!(type1.equals("1"))) && (!(type1.equals("5")))))) && (((!(fieldhtmltype1.equals("3"))) || ((!(type1.equals("2"))) && (!(type1.equals("19"))) && (!(type1.equals("161"))) && (!(type1.equals("162"))))))) {
                      fieldvalue1 = "NULL";
                    }
                    if (fieldhtmltype1.equals("5")) {
                      fieldvalue1 = "NULL";
                    }

                    if (fieldhtmltype1.equals("4")) {
                      fieldvalue1 = "0";
                    }
                  }
                  else if (((!(fielddbtype1.toLowerCase().startsWith("browser."))) && (fielddbtype1.toUpperCase().indexOf("NUMBER") >= 0)) || (fielddbtype1.toUpperCase().indexOf("FLOAT") >= 0) || (fielddbtype1.toUpperCase().indexOf("DECIMAL") >= 0)) {
                    int digitsIndex = fielddbtype1.indexOf(",");
                    int decimaldigits = 2;
                    if (digitsIndex > -1)
                      decimaldigits = Util.getIntValue(fielddbtype1.substring(digitsIndex + 1, fielddbtype1.length() - 1).trim(), 2);
                    else {
                      decimaldigits = 2;
                    }
                    fieldvalue1 = Util.getPointValue2(fieldvalue1, decimaldigits);
                  }

                  if (sql1.equals("")) {
                    if ((((fielddbtype1.startsWith("text")) || (fielddbtype1.startsWith("char")) || (fielddbtype1.startsWith("varchar")) || (fielddbtype1.startsWith("browser")))) && (!("NULL".equals(fieldvalue1)))) {
                      sql1 = sql1 + fieldname1 + "=" + "'" + Util.toHtmlForSpace(Util.toHtml10(Util.htmlFilter4UTF8(fieldvalue1))) + "' ";
                    } else if ((fieldhtmltype1.equals("3")) && (type1.equals("17")) && (this.isoracle)) {
                      if ("NULL".equals(fieldvalue1)) fieldvalue1 = "";
                      sql1 = sql1 + fieldname1 + " = '' ";
                      if (!(fieldvalue1.equals("NULL")))
                        dtMap.put(fieldname1, fieldvalue1);
                    }
                    else {
                      sql1 = sql1 + fieldname1 + "=" + fieldvalue1 + " ";
                    }

                  }
                  else if ((((fielddbtype1.startsWith("text")) || (fielddbtype1.startsWith("char")) || (fielddbtype1.startsWith("varchar")) || (fielddbtype1.startsWith("browser")))) && (!("NULL".equals(fieldvalue1)))) {
                    sql1 = sql1 + "," + fieldname1 + "=" + "'" + Util.toHtmlForSpace(Util.toHtml10(Util.htmlFilter4UTF8(fieldvalue1))) + "' ";
                  } else if ((fieldhtmltype1.equals("3")) && (type1.equals("17")) && (this.isoracle)) {
                    if ("NULL".equals(fieldvalue1)) fieldvalue1 = "";
                    sql1 = sql1 + "," + fieldname1 + " = '' ";
                    if (!(fieldvalue1.equals("NULL")))
                      dtMap.put(fieldname1, fieldvalue1);
                  }
                  else {
                    sql1 = sql1 + "," + fieldname1 + "=" + fieldvalue1 + " ";
                  }

                  String tmpStr = Util.StringReplaceOnce(fieldvalue1, " ", "");
                  if (tmpStr.equals("NULL"))
                    tmpStr = "";
                  modifyMap.put(fieldname1, tmpStr);
                }

                if (!(sql1.equals(""))) {
                  if ((this.isStart) && (this.isTrack)) {
                    updateOrDeleteDetailLog(fields, 2, detailGroupId, Dtlid, modifyMap, "Workflow_formdetail");
                  }
                  this.temprowindex += 1;
                  this.executesuccess = this.rs.executeSql("update Workflow_formdetail set " + sql1 + " where id=" + Dtlid);

                  if (!(this.executesuccess)) {
                    boolean _result = WorkflowRequestMessage.checkBillFieldAndFMTableField(this.formid, this.billtablename, "workflow_formdetail", this.isbill, fieldTypeCache, sql1);
                    if (!(_result)) {
                      saveRequestLog("1");
                      setMessage("126222");
                      return false;
                    }
                    this.executesuccess = this.rs.executeSql("update Workflow_formdetail set " + sql1 + " where id=" + Dtlid);
                    if (!(this.executesuccess)) {
                      saveRequestLog("1");
                      writeLog("执行出错的SQL:update Workflow_formdetail set " + sql1 + " where id=" + Dtlid);

                      setMessage("126222");
                      return false;
                    }

                  }

                  if (this.isoracle) {
                    try {
                      String hrmsql = " update Workflow_formdetail set ";
                      int hrmindex = 0;
                      String hrmsp = " ";
                      Iterator fdit = dtMap.entrySet().iterator();
                      while (fdit.hasNext()) {
                        ++hrmindex;
                        Map.Entry entry = (Map.Entry)fdit.next();
                        String key = entry.getKey().toString();
                        String value = "";
                        if (entry.getValue() != null) {
                          if (String.valueOf(entry.getValue()).equals(" ")) value = "";
                          else value = String.valueOf(entry.getValue());
                        }
                        hrmsql = hrmsql + hrmsp + " " + key + "=? ";
                        hrmsp = ",";
                      }
                      hrmsql = hrmsql + " where id =" + Dtlid;

                      if (hrmindex > 0) {
                        ConnStatement hrmstatement = null;
                        try {
                          hrmstatement = new ConnStatement();
                          hrmstatement.setStatementSql(hrmsql);
                          fdit = dtMap.entrySet().iterator();
                          hrmindex = 0;
                          while (fdit.hasNext()) {
                            ++hrmindex;
                            Map.Entry entry = (Map.Entry)fdit.next();
                            String key = entry.getKey().toString();
                            String value = "";
                            if (entry.getValue() != null) {
                              if (String.valueOf(entry.getValue()).equals(" ")) value = "";
                              else value = String.valueOf(entry.getValue());
                            }
                            hrmstatement.setString(hrmindex, value);
                          }
                          hrmstatement.executeUpdate();
                        } catch (Exception e) {
                          writeLog(e);
                          saveRequestLog("1");
                          setMessage("126223");
                          return false;
                        } finally {
                          if (hrmstatement != null) hrmstatement.close();
                        }
                      }
                    } catch (Exception e) {
                      writeLog(e);
                      saveRequestLog("1");
                      setMessage("126223");
                      return false;
                    }

                  }

                  if (!(this.executesuccess)) {
                    saveRequestLog("1");
                    setMessage("126222");
                    return false;
                  }
                }
              }
              label34959: dtMap.clear();
            }
            fieldids1.clear();
            fieldnames1.clear();
            fieldhtmltypes1.clear();
            fielddbtypes.clear();
            types1.clear();
            ++rows;
          }

          if (!(deldtlids.equals(""))) {
            this.rs.executeSql("delete from Workflow_formdetail where id in(" + deldtlids + ")");
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }

      if (!(this.hrmids.equals(""))) label35068: this.hrmids = this.hrmids.substring(1);
      if (!(this.crmids.equals(""))) this.crmids = this.crmids.substring(1);
      if (!(this.prjids.equals(""))) this.prjids = this.prjids.substring(1);
      if (!(this.docids.equals(""))) this.docids = this.docids.substring(1);
      if (!(this.cptids.equals(""))) this.cptids = this.cptids.substring(1);

      try
      {
        if (this.src.equals("submit"))
        {
          FnaBudgetControl fbc = new FnaBudgetControl();
          StringBuffer errorInfo = new StringBuffer();
          fnaWfValidatorFlag = fbc.getFnaWfValidator(this.workflowid, this.formid, this.requestid, this.creater, this.user, errorInfo, 0);

          if (!(fnaWfValidatorFlag)) {
            setMessageid("1");
            setMessagecontent(errorInfo.toString());
          }
          else {
            CostStandard csUtil = new CostStandard();
            Map map = csUtil.getCostStandardMsg(this.workflowid, this.requestid, this.creater);
            if ("false".equals((String)map.get("flag"))) {
              fnaCostStandardFlag = false;
              setMessageid("1");
              setMessagecontent((String)map.get("errorInfo"));
            }
          }
        }
      } catch (Exception fbc) {
        fnaCostStandardFlag = false;
        fnaWfValidatorFlag = false;
        setMessageid("1");
        setMessagecontent("程序异常,请联系系统管理员");
        new BaseBean().writeLog(e);
      }

      if ((fnaCostStandardFlag) && (fnaWfValidatorFlag))
      {
        String rejectbackflag = "";
        RecordSet tempRecordSet = new RecordSet();
        tempRecordSet.executeSql("select rejectbackflag from workflow_flownode where workflowid=" + this.workflowid + " and nodeid=" + this.nodeid);
        if (tempRecordSet.next()) {
          rejectbackflag = Util.null2String(tempRecordSet.getString("rejectbackflag"));
        }

        if ((!(this.src.equals("save"))) && (((!(this.src.equals("reject"))) || ((this.src.equals("reject")) && (!(rejectbackflag.equals("1")))))) && (this.enableIntervenor == 1))
        {
          this.rs.executeSql("select * from workflowactionview  where workflowid=" + this.workflowid + " and nodeid=" + this.nodeid + " and ispreoperator='0' and (id='action.WorkflowToDoc' or id='WorkflowToDoc') and isused = 1");
          if (this.rs.next())
          {
            this.isWorkFlowToDoc = true;
          }
          try
          {
            RequestCheckAddinRules requestCheckAddinRules = new RequestCheckAddinRules();
            requestCheckAddinRules.resetParameter();

            requestCheckAddinRules.setTrack(this.isTrack);
            requestCheckAddinRules.setStart(this.isStart);
            requestCheckAddinRules.setNodeid(this.nodeid);

            requestCheckAddinRules.setRequestid(this.requestid);
            requestCheckAddinRules.setWorkflowid(this.workflowid);
            requestCheckAddinRules.setObjid(this.nodeid);
            requestCheckAddinRules.setObjtype(1);
            requestCheckAddinRules.setIsbill(this.isbill);
            requestCheckAddinRules.setFormid(this.formid);
            requestCheckAddinRules.setIspreadd("0");
            requestCheckAddinRules.setRequestManager(this);
            requestCheckAddinRules.setUser(this.user);
            String clientIp = "";
            if (this.isRequest) {
              if (this.request != null)
                clientIp = Util.null2String(Util.getIpAddr(this.request));
            }
            else if (this.fu != null) {
              clientIp = Util.null2String(this.fu.getRemoteAddr());
            }
            requestCheckAddinRules.setClientIp(clientIp);
            requestCheckAddinRules.setSrc(this.src);
            requestCheckAddinRules.checkAddinRules();
            if (requestCheckAddinRules.getDoDmlResult().startsWith("-1")) {
              if ("-1".equals(requestCheckAddinRules.getDoDmlResult())) {
                setMessage("126221");
                setMessagecontent(WorkflowRequestMessage.resolveDetailInfo(SystemEnv.getHtmlLabelNames("18010,83071", this.user.getLanguage())));
              } else {
                setMessage("126221");
                setMessagecontent(WorkflowRequestMessage.resolveDetailInfo(SystemEnv.getHtmlLabelNames("18010,83071,127353,82986", this.user.getLanguage()) + ":" + requestCheckAddinRules.getDoDmlResult().replace("-1,", "")));
              }
              return false;
            }

            this.requestCheckAddinRulesMap = new HashMap();
            this.requestCheckAddinRulesMap.put("objId", this.nodeid);
            this.requestCheckAddinRulesMap.put("objType", "1");
            this.requestCheckAddinRulesMap.put("isPreAdd", "0");
            this.requestCheckAddinRulesList.add(this.requestCheckAddinRulesMap);
          }
          catch (Exception erca) {
            saveRequestLog("1");
            setMessage("126221");
            if (erca.getMessage().indexOf("workflow interface action error") > -1) {
              writeLog(erca);
            }
            if ("".equals(getMessage())) {
              setMessage("126221");
            }
            if ("".equals(getMessagecontent())) {
              setMessagecontent(WorkflowRequestMessage.resolveDetailInfo(SystemEnv.getHtmlLabelNames("18010,83071", this.user.getLanguage())));
            }
            return false;
          }

        }

      }

    }

    if (!(this.src.equals("submit"))) {
      WFUrgerManager WFUrgerManager = new WFUrgerManager();
      WFUrgerManager.setLogintype(Util.getIntValue(this.user.getLogintype()));
      WFUrgerManager.setUserid(this.user.getUID());
      WFUrgerManager.insertUrgerByRequestid(this.requestid);
    }

    if (!(this.isRequest)) {
      this.request = this.fu.getRequest();
    }
    if (this.request != null) {
      HttpSession session = this.request.getSession(false);
      session.setAttribute("requestidForAllBill", String.valueOf(this.requestid));
    }
    try {
      WorkflowRequestComInfo workflowRequestComInfo = new WorkflowRequestComInfo();
      workflowRequestComInfo.deleteRequestInfoCache(this.requestid);
    }
    catch (Exception localException1)
    {
    }

    if (this.fu != null) {
      int requestid = getRequestid();
      int nodeid = getNodeid();
      saveFreeWorkflowSetting(this.fu, this.workflowid, 
        requestid, nodeid, this.user.getLanguage());
    }

    try
    {
      new SetNewRequestTitle().getAllRequestName(this.rs, this.requestid, this.requestname, this.workflowid, this.nodeid, this.formid, this.isbill, this.userlanguage);
    }
    catch (Exception localException2)
    {
    }
    try {
      if ((fnaCostStandardFlag) && (fnaWfValidatorFlag)) {
        userId = 0;
        if (this.user != null) {
          userId = this.user.getUID();
        }

        new FnaBorrowAmountControl().saveFnaAmountAdjustLogs(this.workflowid, this.formid, this.nodeid, this.src, this.requestid, userId);
      }
    } catch (Exception userId) {
      new BaseBean().writeLog(userId);
    }
    try
    {
      userId = 0;
      if (this.user != null) {
        userId = this.user.getUID();
      }

      new FnaAdvanceAmountControl().saveFnaAmountAdjustLogs(this.workflowid, this.formid, this.nodeid, this.src, this.requestid, userId);
    } catch (Exception userId) {
      new BaseBean().writeLog(e);
    }
    return true;
  }

  private void addLenMsg(String fieldid, int len)
  {
    String fieldname = "";
    if (this.isbill == 1) {
      RecordSet temprs = new RecordSet();
      temprs.executeQuery("select b.indexdesc from workflow_billfield a,htmllabelindex b where a.fieldlabel = b.id and a.id = ?", new Object[] { fieldid });
      if (temprs.next())
        fieldname = Util.null2String(temprs.getString(1));
    }
    else {
      FormFieldlabelMainManager ffmm = new FormFieldlabelMainManager();
      ffmm.resetParameter();
      ffmm.setFormid(this.formid);
      ffmm.setFieldid(Util.getIntValue(fieldid, 0));
      ffmm.setLanguageid(this.userlanguage);
      try {
        ffmm.selectSingleFormField();
      } catch (Exception e) {
        e.printStackTrace();
      }
      fieldname = ffmm.getFieldlabel();
    }

    JSONObject msgobj = new JSONObject();
    try {
      msgobj.put("details", WorkflowRequestMessage.assemMsgInfo(SystemEnv.getHtmlLabelName(126571, this.userlanguage), new String[] { fieldname, String.valueOf(len) }));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    setMessage("126222");
    setMessagecontent(msgobj.toString());
  }

  private Map<String, String> convertRequestToMap(FileUpload fu)
  {
    Map params = new HashMap();
    if (fu == null) return params;
    try {
      Enumeration paramNames = fu.getParameterNames();
      while (paramNames.hasMoreElements()) {
        String paramName = (String)paramNames.nextElement();
        String[] paramValues = fu.getParameterValues(paramName);
        if (paramValues.length >= 1) {
          String paramValue = paramValues[0];

          if (paramValue.length() != 0)
            params.put(paramName, paramValue);
        }
      }
    }
    catch (Exception e) {
      return params;
    }
    return params;
  }

  private void saveFreeWorkflowSetting(FileUpload fu, int workflowid, int requestId, int nodeid, int language)
  {
    Map params = convertRequestToMap(fu);

    boolean freeWorkflowChanged = Boolean.valueOf((String)params.get("freeWorkflowChanged")).booleanValue();
    if (freeWorkflowChanged) {
      int freeNode = Util.getIntValue((String)params.get("freeNode"), 0);
      int freeDuty = Util.getIntValue((String)params.get("freeDuty"), 0);

      WFFreeFlowManager wfFreeFlowManager = new WFFreeFlowManager();
      if (freeNode == 1)
        if ((freeDuty == 1) || ("0".equals(this.nodetype)))
          wfFreeFlowManager.SaveFreeFlow(params, requestId, nodeid, language);
        else if (freeDuty == 2)
          wfFreeFlowManager.SaveFreeFlowForAllChildren(params, workflowid, requestId, nodeid, language);
    }
  }

  private boolean updateOrDeleteDetailLog(List fields, int optType, int detailGroupId, String id, Map modifyMap)
  {
    return updateOrDeleteDetailLog(fields, optType, detailGroupId, id, modifyMap, "Workflow_formdetail");
  }

  private boolean updateOrDeleteDetailLog(List fields, int optType, int detailGroupId, String id, Map modifyMap, String detailTableName)
  {
    Trackdetail td;
    if ((detailTableName == null) || ("".equals(detailTableName))) {
      return true;
    }
    StringBuffer s = new StringBuffer();
    StringBuffer fieldNameStr = new StringBuffer();
    s.append("select ");
    for (int k = 0; k < fields.size(); ++k) {
      Trackdetail td = (Trackdetail)fields.get(k);
      fieldNameStr.append(td.getFieldName());
      if (k < fields.size() - 1) fieldNameStr.append(",");
      else fieldNameStr.append(" ");
    }
    if (fieldNameStr.toString().trim().equals("")) {
      return true;
    }
    s.append(" " + fieldNameStr + " ");
    s.append("from " + detailTableName + " ");
    s.append("where id=" + id);
    this.executesuccess = this.rs.executeSql(s.toString());

    if (!(this.executesuccess)) {
      saveRequestLog("1");
      return false;
    }
    boolean isModify = false;
    if (optType != 2) isModify = true;

    List oldList = new ArrayList();
    if (this.rs.next()) {
      for (int i = 0; i < fields.size(); ++i) {
        td = (Trackdetail)fields.get(i);
        td.setFieldOldText(this.rs.getString(td.getFieldName()));

        oldList.add(td);
      }

    }

    if (optType == 2) {
      WFModeNodeFieldManager wFModeNodeFieldManager = new WFModeNodeFieldManager();
      boolean isMode = wFModeNodeFieldManager.getIsModeByWorkflowIdAndNodeId(this.workflowid, this.nodeid);

      Map verifyMap = new HashMap();
      String sql = " select distinct b.fieldname  from workflow_formfield a, workflow_formdictdetail b  where a.isdetail='1' and a.fieldid=b.id and a.formid=" + 
        this.formid + " and a.groupId=" + detailGroupId + 
        " and a.fieldid in (select fieldid from ";
      if (isMode) sql = sql + " workflow_modeview ";
      else sql = sql + " workflow_nodeform ";
      sql = sql + "where isView=1 and nodeid=" + this.nodeid + ")";
      if (!(detailTableName.equals("Workflow_formdetail"))) {
        sql = " select fieldname  from workflow_billfield  where viewtype='1' and billid=" + 
          this.formid + 
          " and id in (select fieldid from ";
        if (isMode) sql = sql + " workflow_modeview ";
        else sql = sql + " workflow_nodeform ";
        sql = sql + "where isView=1 and nodeid=" + this.nodeid + ")";
      }

      this.rs.executeSql(sql);
      while (this.rs.next()) {
        verifyMap.put(this.rs.getString("fieldname"), "C");
      }
      for (int i = 0; i < oldList.size(); ++i) {
        Trackdetail td = (Trackdetail)oldList.get(i);
        if ((verifyMap.get(td.getFieldName()) != null) && (modifyMap.get(td.getFieldName()) != null) && (!(modifyMap.get(td.getFieldName()).toString().equals(td.getFieldOldText())))) {
          isModify = true;
          break;
        }
      }
    }
    if (isModify) {
      this.sn += 1;

      for (int k = 0; k < oldList.size(); ++k) {
        td = (Trackdetail)oldList.get(k);
        td.setFieldGroupId(detailGroupId);
        td.setOptType(optType);
        if ((optType == 2) && (modifyMap.get(td.getFieldName()) != null)) {
          td.setFieldNewText(modifyMap.get(td.getFieldName()).toString());
        }
        if (!(insertDetail(td))) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean insertDetail(Trackdetail td)
  {
    StringBuffer s = new StringBuffer();
    s.append("insert into workflow_trackdetail (");
    s.append("sn, optKind,optType,requestId,nodeId,isBill,");
    s.append("fieldLableId,fieldGroupId,fieldId,fieldHtmlType,fieldType,fieldNameCn,fieldNameEn,fieldNameTw,");
    s.append("fieldOldText,fieldNewText,modifierType,agentId,modifierId,modifierIP,modifyTime) ");
    s.append("values (");
    s.append(this.sn + ",");
    s.append(disposeSqlNull(td.getOptKind()) + ",");
    s.append(td.getOptType() + ",");
    s.append(td.getRequestId() + ",");
    s.append(td.getNodeId() + ",");
    s.append(this.isbill + ",");
    s.append(td.getFieldLableId() + ",");
    s.append(td.getFieldGroupId() + ",");
    s.append(td.getFieldId() + ",");
    s.append(disposeSqlNull(td.getFieldHtmlType()) + ",");
    s.append(disposeSqlNull(td.getFieldType()) + ",");
    s.append(disposeSqlNull(td.getFieldNameCn()) + ",");
    s.append(disposeSqlNull(td.getFieldNameEn()) + ",");
    s.append(disposeSqlNull(td.getFieldNameTw()) + ",");
    s.append(disposeSqlNull(Util.toHtml(Util.null2String(td.getFieldOldText()))) + ",");
    s.append(disposeSqlNull(Util.toHtml(Util.null2String(td.getFieldNewText()))) + ",");
    s.append(this.usertype + ",");
    s.append(this.agentId + ",");
    s.append(this.userid + ",");
    s.append(disposeSqlNull(td.getModifierIP()) + ",");
    s.append(disposeSqlNull(td.getModifyTime()));
    s.append(")");
    this.executesuccess = this.rs.executeSql(s.toString());
    if (!(this.executesuccess)) {
      saveRequestLog("1");
    }

    return true;
  }

  private boolean checkIdDel(String delIds, String id)
  {
    if ((delIds != null) && (!(delIds.equals("")))) {
      List list = Util.TokenizerString(delIds, ",");
      for (int i = 0; i < list.size(); ++i) {
        if (list.get(i).toString().equals(id)) {
          return false;
        }
      }
    }
    return true;
  }

  public boolean flowNextNode()
  {
    RequestOperationLogManager rolm = null;
    try
    {
      String sqltmp;
      removeErrorMsg();
      ConnStatement statement = null;
      ArrayList poppuplist = new ArrayList();

      String penetrateFlag = "";
      String lastNodeId = "";

      boolean isfixbill = false;

      WFForwardManager wffm = new WFForwardManager();
      WFCoadjutantManager wfcm = new WFCoadjutantManager();
      wffm.init();
      wffm.setWorkflowid(this.workflowid);
      wffm.setNodeid(this.nodeid);
      wffm.setIsremark(this.isremark);
      wffm.setRequestid(this.requestid);
      wffm.setBeForwardid(this.RequestKey);
      wffm.getWFNodeInfo();
      this.IsBeForwardPending = wffm.getIsBeForwardPending();
      this.IsSubmitedOpinion = wffm.getIsSubmitedOpinion();
      this.IsBeForwardModify = wffm.getIsBeForwardModify();
      this.IsBeForwardSubmit = wffm.getIsBeForwardSubmit();
      this.rs.executeSql("select groupdetailid from workflow_currentoperator where id=" + this.RequestKey);
      if (this.rs.next()) {
        wfcm.getCoadjutantRights(this.rs.getInt("groupdetailid"));
        this.coadispending = wfcm.getIspending();
        this.coadismodify = wfcm.getIsmodify();
        this.coadsigntype = wfcm.getSigntype();
      }

      String fixbillSql = "select * from workflow_bill  where id >0 and id= " + this.formid;
      this.rs.executeSql(fixbillSql);
      if (this.rs.next()) {
        isfixbill = true;
      }
      try
      {
        this.rs.execute("select isvalid from workflow_base where id=" + this.workflowid);
        if (this.rs.next()) {
          int isvalid_t = Util.getIntValue(this.rs.getString("isvalid"), 0);
          if (isvalid_t == 2)
            this.istest = 1;
        }
      }
      catch (Exception localException1)
      {
      }
      String sqlll = "select  distinct wp.ispending,wp.ismodify,wp.issubmitdesc,wp.isforward,wc.userid,wp.signtype from workflow_groupdetail wp,workflow_currentoperator wc where wp.id = wc.groupdetailid and wc.requestid = " + this.requestid + " and IsCoadjutant='1' and wc.isremark ='7'";

      this.rs.executeSql(sqlll);
      ArrayList isforwards = new ArrayList();
      ArrayList coadispendings = new ArrayList();
      ArrayList coadismodifys = new ArrayList();
      ArrayList isSubmitedOpinions = new ArrayList();
      ArrayList userids = new ArrayList();
      ArrayList coadsigntypes = new ArrayList();
      while (this.rs.next()) {
        if (("2".equals(this.rs.getString(6))) && ("0".equals(this.rs.getString(3))))
          coadispendings.add("1");
        else {
          coadispendings.add(this.rs.getString(1));
        }
        coadismodifys.add(this.rs.getString(2));
        isSubmitedOpinions.add(this.rs.getString(3));
        isforwards.add(this.rs.getString(4));
        userids.add(this.rs.getString(5));
        coadsigntypes.add(this.rs.getString(6));
      }

      String isfeedback = "";
      String isnullnotfeedback = "";
      this.rs.executeSql("select t.isfeedback,t.isnullnotfeedback,t1.nodeattribute from workflow_flownode t, workflow_nodebase t1  where t.nodeid = t1.id and t.workflowid=" + this.workflowid + " and t.nodeid=" + this.nodeid);
      if (this.rs.next()) {
        isfeedback = Util.null2String(this.rs.getString("isfeedback"));
        isnullnotfeedback = Util.null2String(this.rs.getString("isnullnotfeedback"));
        this.nodeattribute = Util.getIntValue(this.rs.getString("nodeattribute"), 0);
      }
      if ((this.isremark == 7) && ("2".equals(this.coadsigntype))) {
        this.tempsrc = this.src;
        this.src = "save";
      }

      getBillId();

      if ((this.needwfback == null) || ("".equals(this.needwfback))) {
        if (this.isRequest) {
          if (this.request != null) {
            this.needwfback = Util.getIntValue(this.request.getParameter("needwfback"), 1);
          }
        }
        else if (this.fu != null) {
          this.needwfback = Util.getIntValue(this.fu.getParameter("needwfback"), 1);
        }
      }

      String cnodetype = "";
      ArrayList operatorsWfEnd = new ArrayList();
      boolean haspassthisnode = false;

      String finishndgpids = "";

      ArrayList tempusers = new ArrayList();
      ArrayList tempusersType = new ArrayList();
      String groupdetailids = "";
      String groupdetailid4sql = "";
      ArrayList submituserids = new ArrayList();
      ArrayList submitusertypes = new ArrayList();
      this.canflowtonextnode = true;
      this.coadcansubmit = true;
      this.showcoadjutant = false;

      StringBuffer s = new StringBuffer();
      s.append("select t1.ismodifylog, t2.status, t2.currentnodetype,t1.isAutoApprove,t1.isAutoCommit from workflow_base t1, workflow_requestbase t2 where t1.id=t2.workflowid and t2.requestid=" + this.requestid);
      this.executesuccess = this.rs.executeSql(s.toString());
      if (!(this.executesuccess)) {
        writeLog(s.toString());
        saveRequestLog("1");
        return false;
      }
      String tempcurrentnodetype = "";
      if (this.rs.next()) {
        this.isTrack = ((this.rs.getString("ismodifylog") != null) && ("1".equals(this.rs.getString("ismodifylog"))));
        this.isStart = ((this.rs.getString("status") != null) && (!("".equals(this.rs.getString("status")))));
        tempcurrentnodetype = Util.null2String(this.rs.getString("currentnodetype"));

        setIsAutoApprove(Util.null2s(this.rs.getString("isAutoApprove"), "0"));
        setIsAutoCommit(Util.null2s(this.rs.getString("isAutoCommit"), "0"));
      }

      if (this.isbill == 1) {
        this.rs.executeSql("select tablename from workflow_bill where id = " + this.formid);
        if (this.rs.next())
          this.billtablename = this.rs.getString("tablename");
        else {
          return false;
        }
      }

      RecordSetTrans rst = new RecordSetTrans();
      rst.setAutoCommit(false);
      SetRsTrans(rst);

      ArrayList nodeidList_sub = new ArrayList();
      ArrayList triggerTimeList_sub = new ArrayList();
      ArrayList hasTriggeredSubwfList_sub = new ArrayList();

      ArrayList nodeidList_wp = new ArrayList();
      ArrayList createTimeList_wp = new ArrayList();

      boolean isReCalculatePermission = false;
      try
      {
        rst.executeProc("workflow_Requestbase_SByID", this.requestid);
        if (rst.next()) {
          this.lastnodeid = Util.getIntValue(rst.getString("lastnodeid"), 0);
          this.lastnodetype = Util.null2String(rst.getString("lastnodetype"));
          this.passedgroups = Util.getIntValue(rst.getString("passedgroups"), 0);
          this.totalgroups = Util.getIntValue(rst.getString("totalgroups"), 0);
          this.creater = Util.getIntValue(rst.getString("creater"), 0);
          this.creatertype = Util.getIntValue(rst.getString("creatertype"), 0);
          this.requestmark = Util.null2String(rst.getString("requestmark"));
          cnodetype = Util.null2String(rst.getString("currentnodetype"));
          this.oldformsignaturemd5 = Util.null2String(rst.getString("formsignaturemd5"));
        } else {
          saveRequestLog("1");
          if (rst != null) {
            new Exception("流程基本信息不存在，因为无法在workflow_base中查询到当前请求信息。").printStackTrace();
            rst.rollback();
          }
          setMessage("126223");

          GCONST.WFProcessing.remove(this.requestid + "_" + this.nodeid);

          return false;
        }
        GCONST.WFProcessing.add(this.requestid + "_" + this.nodeid);
        if (this.isMakeOperateLog)
        {
          rolm = new RequestOperationLogManager(this);
          rolm.flowTransStartBefore();
        }
        if ((this.src.equals("submit")) || (this.src.equals("reject")) || (this.src.equals("reopen")) || (this.src.equals("intervenor")))
        {
          int copnodeid = this.nodeid;
          int submitgroups = 0;
          int totalunsubmitgroups = 0;

          if (this.src.equals("submit")) {
            String tempisremark = "isremark = '0'";
            if (this.coadsigntype.equals("0")) {
              tempisremark = "(isremark = '0' or isremark = '7')";
            } else if (this.coadsigntype.equals("1")) {
              int signorder = 0;
              tempisremark = "(isremark = '0' or isremark = '7')";
              this.rs.executeSql("select signorder from workflow_groupdetail where id=" + wfcm.getGroupdetailid());
              if (this.rs.next()) signorder = Util.getIntValue(this.rs.getString("signorder"), 0);
              if (this.isremark == 0)
              {
                this.rs.executeSql("select 1 from workflow_currentoperator where isremark = '7' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and exists(select 1 from workflow_coadjutant where coadjutantid=workflow_currentoperator.id and organizedid=" + this.RequestKey + " and requestid=" + this.requestid + ")");
                if (this.rs.next()) this.coadcansubmit = false;
                if ((wfcm.getSignorder() == 2) && (!(this.coadcansubmit))) this.coadcansubmit = true;
              } else if (this.isremark == 7)
              {
                this.rs.executeSql("select 1 from workflow_currentoperator where isremark = '0' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and exists(select 1 from workflow_coadjutant where organizedid=workflow_currentoperator.id and coadjutantid=" + this.RequestKey + " and requestid=" + this.requestid + ")");
                if (this.rs.next()) this.coadcansubmit = false;
                if (signorder == 0) {
                  this.rs.executeSql("select 1 from workflow_currentoperator where isremark = '2' and preisremark='0' and (isreject is null or isreject=0) and requestid='" + this.requestid + "' and nodeid='" + this.nodeid + "' and groupdetailid ='" + wfcm.getGroupdetailid() + "'");
                  if (this.rs.next()) this.coadcansubmit = true;
                }
              }
            }
            if (this.coadcansubmit)
            {
              if (!(isExeOldFlowlogic))
              {
                rst.executeSql("select nodeid from workflow_currentoperator where " + tempisremark + " and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + " and nodeid=" + this.nodeid + "order by id desc");
                if (rst.next()) {
                  copnodeid = Util.getIntValue(Util.null2String(rst.getString(1)), 0);
                } else {
                  rst.executeSql("select nodeid from workflow_currentoperator where " + tempisremark + " and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + " order by id desc");
                  if (rst.next()) copnodeid = Util.getIntValue(Util.null2String(rst.getString(1)), 0);

                }

                int canPassGroupCount = 0;

                boolean isCreateNdOrNdgpNotExist = false;

                String notpassgpnumsql = "SELECT distinct wg.groupid  FROM workflow_currentoperator wc LEFT JOIN workflow_groupdetail wg      ON wc.groupdetailid=wg.id  WHERE " + 
                  tempisremark + " and requestid=" + this.requestid + 
                  " and nodeid=" + this.nodeid;
                rst.executeSql(notpassgpnumsql);
                int nodegroupcount = rst.getCounts();
                while (rst.next()) {
                  int tempgrouid = Util.getIntValue(Util.null2String(rst.getString(1)), 0);

                  if (tempgrouid == 0) {
                    isCreateNdOrNdgpNotExist = true;
                  }

                  String smgroupsql = "SELECT distinct groupdetailid, groupid  FROM workflow_currentoperator  WHERE " + 
                    tempisremark + " and requestid=" + this.requestid + 
                    " and userid=" + this.userid + " and usertype=" + this.usertype + 
                    " and nodeid=" + this.nodeid + 
                    " and groupdetailid in (SELECT id FROM workflow_groupdetail WHERE groupid=" + tempgrouid + ")";

                  String temp_groupdetailids = "";
                  String temp_groupdetailid4sql = "";

                  this.rs2.executeSql(smgroupsql);
                  while (this.rs2.next()) {
                    int tempGroupDetailID = Util.getIntValue(Util.null2String(this.rs2.getString(1)), 0);

                    int tempsubmitgroups = 0;
                    int temptotalgroups = 0;

                    this.rs3.executeSql("select count(distinct groupid) from workflow_currentoperator  where " + 
                      tempisremark + " and requestid=" + this.requestid + 
                      " and userid=" + this.userid + " and usertype=" + this.usertype + 
                      " and nodeid=" + this.nodeid + " and groupdetailid=" + tempGroupDetailID);

                    if (this.rs3.next()) {
                      tempsubmitgroups = Util.getIntValue(Util.null2String(this.rs3.getString(1)), 0);
                    }

                    this.rs3.executeSql("select groupdetailid from workflow_currentoperator  where (isremark='7' or preisremark='7') and requestid='" + 
                      this.requestid + "'" + 
                      " and nodeid='" + this.nodeid + "' and groupdetailid='" + wfcm.getGroupdetailid() + "'" + 
                      " and groupdetailid=" + tempGroupDetailID);
                    if ((this.rs3.next()) && (((this.isremark == 0) || (this.isremark == 7)))) {
                      temptotalgroups = wfcm.getTotalnumSubmitGroups(this.isremark, this.nodeid, this.requestid, this.userid, rst);
                    } else {
                      this.rs3.executeSql("select count(distinct groupid) from workflow_currentoperator  where " + 
                        tempisremark + " and requestid=" + this.requestid + 
                        " and nodeid=" + this.nodeid + " and groupdetailid=" + tempGroupDetailID);
                      if (this.rs3.next()) {
                        temptotalgroups = Util.getIntValue(Util.null2String(this.rs3.getString(1)), 0);
                      }

                    }

                    this.rs4.execute("select * from workflow_groupdetail where id=" + tempGroupDetailID);
                    if (this.rs4.next()) {
                      int type = this.rs4.getInt("type");
                      int signorder = this.rs4.getInt("signorder");
                      if ((WFPathUtil.isContinuousProcessing(type)) && (signorder == 2) && (this.isremark != 7)) {
                        this.rs3.execute("select * from workflow_agentpersons where requestid=" + this.requestid + " and (groupdetailid=" + this.rs2.getInt("groupdetailid") + " or groupdetailid is null)");
                        if ((this.rs3.next()) && (!(this.rs3.getString("receivedPersons").equals("")))) {
                          --tempsubmitgroups;
                          if (temp_groupdetailids.equals(""))
                            temp_groupdetailids = this.rs2.getString("groupdetailid") + "_" + this.rs2.getString("groupid");
                          else {
                            temp_groupdetailids = temp_groupdetailids + "," + this.rs2.getString("groupdetailid") + "_" + this.rs2.getString("groupid");
                          }

                          if (temp_groupdetailid4sql.equals(""))
                            temp_groupdetailid4sql = this.rs2.getString("groupdetailid");
                          else {
                            temp_groupdetailid4sql = temp_groupdetailid4sql + "," + this.rs2.getString("groupdetailid");
                          }
                        }

                      }

                    }

                    if (tempsubmitgroups < temptotalgroups)
                      continue;
                    ++canPassGroupCount;

                    if ("".equals(finishndgpids))
                      finishndgpids = finishndgpids + tempgrouid;
                    else {
                      finishndgpids = finishndgpids + "," + tempgrouid;
                    }

                    temp_groupdetailids = "";
                    temp_groupdetailid4sql = "";
                    break;
                  }

                  if (groupdetailids.equals(""))
                    groupdetailids = temp_groupdetailids;
                  else if (!("".equals(temp_groupdetailids))) {
                    groupdetailids = groupdetailids + "," + temp_groupdetailids;
                  }
                  if (groupdetailid4sql.equals(""))
                    groupdetailid4sql = temp_groupdetailid4sql;
                  else if (!("".equals(temp_groupdetailid4sql))) {
                    groupdetailid4sql = groupdetailid4sql + "," + temp_groupdetailid4sql;
                  }

                }

                if (isCreateNdOrNdgpNotExist) {
                  rst.executeSql("select count(distinct groupid) from workflow_currentoperator where " + tempisremark + " and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + " and nodeid=" + this.nodeid);
                  if (rst.next()) {
                    this.passedgroups += Util.getIntValue(rst.getString(1), 0);
                    submitgroups = Util.getIntValue(rst.getString(1), 0);
                  }

                  rst.executeSql("select count(distinct groupid) from workflow_currentoperator where " + tempisremark + " and requestid=" + this.requestid + " and nodeid=" + this.nodeid);
                  if (rst.next())
                    totalunsubmitgroups = Util.getIntValue(rst.getString(1), 0);
                }
                else {
                  submitgroups = canPassGroupCount;
                  totalunsubmitgroups = nodegroupcount;
                }

              }
              else
              {
                rst.executeSql("select count(distinct groupid) from workflow_currentoperator where " + tempisremark + " and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + " and nodeid=" + this.nodeid);
                if (rst.next()) {
                  this.passedgroups += Util.getIntValue(rst.getString(1), 0);
                  submitgroups = Util.getIntValue(rst.getString(1), 0);
                }

                rst.executeSql("select nodeid from workflow_currentoperator where " + tempisremark + " and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + " and nodeid=" + this.nodeid + "order by id desc");
                if (rst.next()) {
                  copnodeid = Util.getIntValue(rst.getString(1), 0);
                } else {
                  rst.executeSql("select nodeid from workflow_currentoperator where " + tempisremark + " and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + " order by id desc");
                  if (rst.next()) copnodeid = Util.getIntValue(rst.getString(1), 0);

                }

                rst.executeSql("select groupdetailid from workflow_currentoperator where (isremark='7' or preisremark='7') and requestid='" + this.requestid + "' and nodeid='" + this.nodeid + "' and groupdetailid='" + wfcm.getGroupdetailid() + "'");
                if ((rst.next()) && (((this.isremark == 0) || (this.isremark == 7)))) {
                  totalunsubmitgroups = wfcm.getTotalnumSubmitGroups(this.isremark, this.nodeid, this.requestid, this.userid, rst);
                } else {
                  rst.executeSql("select count(distinct groupid) from workflow_currentoperator where " + tempisremark + " and requestid=" + this.requestid + " and nodeid=" + this.nodeid);
                  if (rst.next()) {
                    totalunsubmitgroups = Util.getIntValue(rst.getString(1), 0);
                  }

                }

                rst.execute("select distinct groupdetailid,groupid from workflow_currentoperator where " + tempisremark + " and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + " and nodeid=" + this.nodeid);

                while (rst.next())
                {
                  this.rs2.execute("select * from workflow_groupdetail where id=" + rst.getInt("groupdetailid"));
                  if (this.rs2.next()) {
                    int type = this.rs2.getInt("type");
                    int signorder = this.rs2.getInt("signorder");
                    if ((!(WFPathUtil.isContinuousProcessing(type))) || (signorder != 2) || (this.isremark == 7)) {
                      continue;
                    }
                    this.rs3.execute("select * from workflow_agentpersons where requestid=" + this.requestid + " and (groupdetailid=" + rst.getInt("groupdetailid") + " or groupdetailid is null)");
                    if ((this.rs3.next()) && (!(this.rs3.getString("receivedPersons").equals("")))) {
                      this.passedgroups -= 1;
                      --submitgroups;
                      groupdetailids = groupdetailids + "," + rst.getString("groupdetailid") + "_" + rst.getString("groupid");
                      groupdetailid4sql = groupdetailid4sql + "," + rst.getString("groupdetailid");
                    }

                  }

                }

              }

            }

          }

          if (((this.src.equals("submit")) && (submitgroups >= totalunsubmitgroups) && (copnodeid == this.nodeid) && (this.coadcansubmit)) || (this.src.equals("reject")) || (this.src.equals("reopen")) || (this.isremark == 5) || (this.src.equals("intervenor"))) {
            this.showcoadjutant = true;
            int isreject = 0;
            int isreopen = 0;
            int linkid = 0;
            if (this.src.equals("reject")) {
              isreject = 1;

              if (this.isRequest) {
                if (this.request != null) {
                  this.RejectToNodeid = Util.getIntValue(this.request.getParameter("RejectToNodeid"));
                  this.RejectToType = Util.getIntValue(this.request.getParameter("RejectToType"));
                }
              }
              else if (this.fu != null) {
                this.RejectToNodeid = Util.getIntValue(this.fu.getParameter("RejectToNodeid"));
                this.RejectToType = Util.getIntValue(this.fu.getParameter("RejectToType"));
              }

            }
            else if ("submit".equals(this.src)) {
              if (this.SubmitToNodeid <= 0) {
                if (this.isRequest) {
                  if (this.request != null) {
                    this.SubmitToNodeid = Util.getIntValue(this.request.getParameter("SubmitToNodeid"), 0);
                  }
                }
                else if (this.fu != null) {
                  this.SubmitToNodeid = Util.getIntValue(this.fu.getParameter("SubmitToNodeid"), 0);
                }
              }

            }
            else if (this.src.equals("reopen")) { isreopen = 1; }
            if ((this.isremark != 5) && (!(this.src.equals("intervenor"))))
            {
              RequestNodeFlow requestNodeFlow = new RequestNodeFlow();
              requestNodeFlow.setRequestid(this.requestid);
              requestNodeFlow.setNodeid(this.nodeid);
              requestNodeFlow.setNodetype(this.nodetype);
              requestNodeFlow.setWorkflowid(this.workflowid);
              requestNodeFlow.setUserid(this.userid);
              requestNodeFlow.setUsertype(this.usertype);
              requestNodeFlow.setLanguageid(this.userlanguage);
              requestNodeFlow.setCreaterid(this.creater);
              requestNodeFlow.setCreatertype(this.creatertype);
              requestNodeFlow.setFormid(this.formid);
              requestNodeFlow.setIsbill(this.isbill);
              requestNodeFlow.setBillid(this.billid);
              requestNodeFlow.setBilltablename(this.billtablename);
              requestNodeFlow.setIsreject(isreject);
              requestNodeFlow.setIsreopen(isreopen);
              requestNodeFlow.setRejectToNodeid(this.RejectToNodeid);
              requestNodeFlow.setRejectToType(this.RejectToType);
              requestNodeFlow.setSubmitToNodeid(this.SubmitToNodeid);
              requestNodeFlow.setRecordSet(this.rs);
              requestNodeFlow.setUser(this.user);
              requestNodeFlow.setRecordSetTrans(rst);
              requestNodeFlow.setIsFromRequestManager(1);

              requestNodeFlow.setIsfixbill(isfixbill);

              requestNodeFlow.setCurrentdate(this.currentdate);
              requestNodeFlow.setCurrenttime(this.currenttime);

              requestNodeFlow.setEh_operatorMap(this.eh_operatorMap);
              requestNodeFlow.setTonextnode(true);

              requestNodeFlow.getNextNodes();
              this.nextnodeids = requestNodeFlow.getNextnodeids();
              this.nextnodetypes = requestNodeFlow.getNextnodetypes();
              this.nextlinkids = requestNodeFlow.getNextlinkids();
              this.nextlinknames = requestNodeFlow.getNextlinknames();
              this.operatorshts = requestNodeFlow.getOperatorshts();
              this.nextnodeattrs = requestNodeFlow.getNextnodeattrs();
              this.nextnodepassnums = requestNodeFlow.getNextnodepassnums();
              this.linkismustpasss = requestNodeFlow.getLinkismustpasss();
              this.requestexceptiontypes = requestNodeFlow.getRequestexceptiontypes();
              this.hasEflowToAssignNode = requestNodeFlow.isHasEflowToAssignNode();
              this.hasCoadjutant = requestNodeFlow.isHasCoadjutant();

              penetrateFlag = requestNodeFlow.getPenetrateId();
              lastNodeId = requestNodeFlow.getLastNodeId();

              if (this.nextnodeids.size() < 1) {
                saveRequestLog("1");
                if (rst != null) {
                  new Exception("工作流下一节点错误，因为没有找到符合条件的出口。").printStackTrace();
                  rst.rollback();
                }
                this.isNeedChooseOperator = requestNodeFlow.isNeedChooseOperator();
                if (!(this.isNeedChooseOperator)) {
                  setMessage("126221");
                }
                setMessagecontent(requestNodeFlow.getMessagecontent());

                GCONST.WFProcessing.remove(this.requestid + "_" + this.nodeid);

                return false;
              }
              if ("0".equals(this.isFirstSubmit)) {
                String returnvalue = "";
                for (int i = 0; i < this.nextlinkids.size(); ++i) {
                  String viewNodeIdSQL = "select tipsinfo from workflow_nodelink where id=" + this.nextlinkids.get(i);
                  if (this.RejectToNodeid != 0) {
                    viewNodeIdSQL = viewNodeIdSQL + " and  destnodeid=" + this.RejectToNodeid;
                  }
                  rst.executeSql(viewNodeIdSQL);
                  if (rst.next()) {
                    if (returnvalue.equals("")) returnvalue = Util.null2String(rst.getString("tipsinfo"));
                    else returnvalue = returnvalue + "\\n" + Util.null2String(rst.getString("tipsinfo"));
                  }
                }

                if (!("".equals(returnvalue))) {
                  setMessage("2");
                  setMessagecontent(returnvalue);
                  rst.rollback();

                  GCONST.WFProcessing.remove(this.requestid + "_" + this.nodeid);

                  return false;
                }
              }

              this.wflinkinfo.setSrc(this.src);
              this.wflinkinfo.setSubmitToNodeid(this.SubmitToNodeid);
              for (int i = 0; i < this.nextnodeids.size(); ++i) {
                this.nextnodeid = Util.getIntValue((String)this.nextnodeids.get(i));
                this.nextnodetype = ((String)this.nextnodetypes.get(i));
                linkid = Util.getIntValue((String)this.nextlinkids.get(i));
                this.status = ((String)this.nextlinknames.get(i));
                this.operatorsht = ((Hashtable)this.operatorshts.get(i));
                this.nextnodeattr = Util.getIntValue((String)this.nextnodeattrs.get(i));
                this.totalgroups = this.operatorsht.size();
                if ((this.nextnodeattr == 3) || (this.nextnodeattr == 4) || (this.nextnodeattr == 5)) {
                  this.canflowtonextnode = this.wflinkinfo.FlowToNextNode(this.requestid, this.nodeid, this.nextnodeid, this.nextnodeattr, Util.getIntValue((String)this.nextnodepassnums.get(i)), Util.getIntValue((String)this.linkismustpasss.get(i)));
                }

                haspassthisnode = true;

                rst.executeProc("workflow_Requestbase_SByID", this.requestid);
                if (rst.next()) {
                  this.requestmark = Util.null2String(rst.getString("requestmark"));
                } else {
                  saveRequestLog("1");
                  if (rst != null) {
                    new Exception("流程基本信息不存在，因为无法在workflow_base中查询到当前请求信息。").printStackTrace();
                    rst.rollback();
                  }

                  GCONST.WFProcessing.remove(this.requestid + "_" + this.nodeid);

                  return false;
                }

                rst.executeProc("workflow_NodeLink_SPasstime", this.nextnodeid + this.flag + "0");
                if (rst.next())
                  this.nodepasstime = Util.getFloatValue(rst.getString("nodepasstime"), -1.0F);
              }
            }
            else
            {
              if (this.isRequest) {
                if (this.request != null) {
                  this.submitNodeId = Util.null2String(this.request.getParameter("submitNodeId"));
                  this.Intervenorid = Util.null2String(this.request.getParameter("Intervenorid"));
                  this.IntervenoridType = Util.null2String(this.request.getParameter("IntervenoridType"));
                  this.SignType = Util.getIntValue(this.request.getParameter("SignType"), 0);
                }
              }
              else if (this.fu != null) {
                this.submitNodeId = Util.null2String(this.fu.getParameter("submitNodeId"));
                this.Intervenorid = Util.null2String(this.fu.getParameter("Intervenorid"));
                this.IntervenoridType = Util.null2String(this.fu.getParameter("IntervenoridType"));
                this.SignType = Util.getIntValue(this.fu.getParameter("SignType"), 0);
              }

              ArrayList tempnodes = Util.TokenizerString(this.submitNodeId, "_");
              tempusers = Util.TokenizerString(this.Intervenorid, ",");
              tempusersType = Util.TokenizerString(this.IntervenoridType, ",");
              if (tempnodes.size() >= 2) {
                this.nextnodeid = Util.getIntValue((String)tempnodes.get(0));
                this.nextnodetype = ((String)tempnodes.get(1));
                this.nextnodeattr = this.wflinkinfo.getNodeAttribute(this.nextnodeid);
                this.nextnodeids.add(this.nextnodeid);
                this.nextnodetypes.add(this.nextnodetype);
                this.nextnodeattrs.add(this.nextnodeattr);
                this.totalgroups = tempusers.size();
                String tmpnodeids = this.wflinkinfo.getBrancheNode(this.nextnodeid, this.workflowid, "", this.requestid);
                if (!(tmpnodeids.equals(""))) {
                  rst.executeSql("select a.nodeid,b.nodetype from workflow_currentoperator a,workflow_flownode b where a.workflowid=b.workflowid and a.nodeid=b.nodeid and a.isremark='0' and a.requestid=" + this.requestid + " and a.nodeid in(" + tmpnodeids + ")");
                  if (rst.next()) {
                    this.nodeid = rst.getInt(1);
                    this.nodetype = Util.null2String(rst.getString(2));
                  }
                }
                rst.executeSql("select nodename from workflow_flownode,workflow_nodebase where workflow_nodebase.id=workflow_flownode.nodeid and workflowid=" + this.workflowid + " and workflow_flownode.nodeid=" + this.nodeid + " order by nodetype");
                if (rst.next()) {
                  this.status = rst.getString("nodename");
                }
                rst.executeSql("select nodename from workflow_flownode,workflow_nodebase where workflow_nodebase.id=workflow_flownode.nodeid and workflowid=" + this.workflowid + " and workflow_flownode.nodeid=" + this.nextnodeid + " order by nodetype");
                if (rst.next()) {
                  RequestManager tmp6654_6653 = this; tmp6654_6653.status = tmp6654_6653.status + SystemEnv.getHtmlLabelName(18195, this.userlanguage) + SystemEnv.getHtmlLabelName(15322, this.userlanguage) + rst.getString("nodename");
                }
              } else {
                if (rst != null) {
                  new Exception("工作流提交信息错误，因为提交的参数：submitNodeId不正确。").printStackTrace();
                  rst.rollback();
                }
                setMessage("126221");
                setMessagecontent(WorkflowRequestMessage.resolveDetailInfo("未选择流程干预节点"));

                GCONST.WFProcessing.remove(this.requestid + "_" + this.nodeid);

                return false;
              }
              this.canflowtonextnode = true;
            }

            String remindusers = "";
            String usertypes = "";
            rst.executeSql("select wfreminduser,wfusertypes from workflow_currentoperator where isremark='0' and requestid=" + this.requestid);
            if (rst.next()) {
              remindusers = rst.getString("wfreminduser");
              usertypes = rst.getString("wfusertypes");
            }
            ArrayList wfremindusers = Util.TokenizerString(remindusers, ",");
            ArrayList wfusertypes = Util.TokenizerString(usertypes, ",");
            rst.executeSql("select userid,usertype,nodeid from workflow_currentoperator where requestid=" + this.requestid + " group by userid,usertype,nodeid");
            while (rst.next()) {
              String tempuserid = rst.getString("userid");
              String tempusertype = rst.getString("usertype");
              int tempnodeid = Util.getIntValue(rst.getString("nodeid"), 0);
              if ((wfremindusers.indexOf(tempuserid) >= 0) || 
                (this.nodeid != tempnodeid)) continue;
              wfremindusers.add(tempuserid);
              wfusertypes.add(tempusertype);
            }

            for (int i = 0; i < wfremindusers.size(); ++i) {
              this.poppupRemindInfoUtil.updatePoppupRemindInfo(Util.getIntValue((String)wfremindusers.get(i)), 10, (String)wfusertypes.get(i), this.requestid);
              this.poppupRemindInfoUtil.updatePoppupRemindInfo(Util.getIntValue((String)wfremindusers.get(i)), 0, (String)wfusertypes.get(i), this.requestid);
            }

            String formsignaturemd5 = "";
            try {
              if ("submit".equals(this.src))
                formsignaturemd5 = WFPathUtil.getFormValMD5(this.workflowid, this.requestid, this.isbill, this.formid, this.requestname, this.nextnodeid);
            }
            catch (Exception e) {
              e.printStackTrace();
            }

            if ((this.src.equals("submit")) && ((((this.isremark != 1) && (this.isremark != 7)) || (this.CanModify))))
            {
              if ((this.lastnodetype.equals("0")) && (this.requestmark.equals(""))) {
                this.requestmark = doRequestMark();
              }
              this.sql = " update workflow_requestbase set  lastnodeid = " + 
                this.nodeid + 
                " ,lastnodetype = '" + this.nodetype;
              if (this.canflowtonextnode)
                if (this.nextnodeattr == 1)
                {
                  RequestManager tmp7268_7267 = this; tmp7268_7267.sql = tmp7268_7267.sql + "' ,currentnodeid = " + this.nextnodeid + 
                    " ,currentnodetype = '" + this.nextnodetype;
                  this.status = SystemEnv.getHtmlLabelName(21394, this.userlanguage);
                } else if (this.nextnodeattr == 2)
                {
                  RequestManager tmp7341_7340 = this; tmp7341_7340.sql = tmp7341_7340.sql + "' ,currentnodeid = " + this.nextnodeid + 
                    " ,currentnodetype = '" + this.nextnodetype;
                  this.status = SystemEnv.getHtmlLabelName(21395, this.userlanguage);
                }
                else
                {
                  RequestManager tmp7406_7405 = this; tmp7406_7405.sql = tmp7406_7405.sql + "' ,currentnodeid = " + this.nextnodeid + 
                    " ,currentnodetype = '" + this.nextnodetype;
                }
              else {
                this.status = SystemEnv.getHtmlLabelName(21395, this.userlanguage);
              }
              RequestManager tmp7471_7470 = this; tmp7471_7470.sql = tmp7471_7470.sql + "' ,status = '" + Util.fromScreen2(this.status, this.userlanguage) + "' " + 
                " ,passedgroups = 0" + 
                " ,totalgroups = " + this.totalgroups + 
                " ,requestname = '" + Util.fromScreen2(this.requestname, this.userlanguage) + "' " + 
                " ,requestmark = '" + this.requestmark + "' " + 
                " ,lastoperator = " + this.userid;
              if (!(tempcurrentnodetype.equals("3"))) {
                this.sql = this.sql + 
                  " ,lastoperatedate = '" + this.currentdate + "' " + 
                  " ,lastoperatetime = '" + this.currenttime + "' ";
              }
              this.sql = this.sql + 
                " ,lastoperatortype = " + this.usertype + 
                " ,nodepasstime = " + this.nodepasstime + 
                " ,nodelefttime = " + this.nodepasstime + 
                " ,docids = '" + this.docids + "' " + 
                " ,crmids = '" + this.crmids + "' ";
              if (this.isoracle) {
                this.sql += " ,hrmids = ? ";
              }
              else
              {
                RequestManager tmp7801_7800 = this; tmp7801_7800.sql = tmp7801_7800.sql + " ,hrmids = '" + this.hrmids + "' ";
              }
              RequestManager tmp7842_7841 = this; tmp7842_7841.sql = tmp7842_7841.sql + " ,prjids = '" + this.prjids + "' " + 
                " ,cptids = '" + this.cptids + "' ";
            }
            else
            {
              this.sql = " update workflow_requestbase set  lastnodeid = " + 
                this.nodeid + 
                " ,lastnodetype = '" + this.nodetype;
              if (this.canflowtonextnode)
                if (this.nextnodeattr == 1)
                {
                  RequestManager tmp7957_7956 = this; tmp7957_7956.sql = tmp7957_7956.sql + "' ,currentnodeid = " + this.nextnodeid + 
                    " ,currentnodetype = '" + this.nextnodetype;
                  this.status = SystemEnv.getHtmlLabelName(21394, this.userlanguage);
                } else if (this.nextnodeattr == 2)
                {
                  RequestManager tmp8030_8029 = this; tmp8030_8029.sql = tmp8030_8029.sql + "' ,currentnodeid = " + this.nextnodeid + 
                    " ,currentnodetype = '" + this.nextnodetype;
                  this.status = SystemEnv.getHtmlLabelName(21395, this.userlanguage);
                }
                else
                {
                  RequestManager tmp8095_8094 = this; tmp8095_8094.sql = tmp8095_8094.sql + "' ,currentnodeid = " + this.nextnodeid + 
                    " ,currentnodetype = '" + this.nextnodetype;
                }
              else {
                this.status = SystemEnv.getHtmlLabelName(21395, this.userlanguage);
              }
              RequestManager tmp8160_8159 = this; tmp8160_8159.sql = tmp8160_8159.sql + "' ,status = '" + Util.fromScreen2(this.status, this.userlanguage) + "' " + 
                " ,passedgroups = 0" + 
                " ,totalgroups = " + this.totalgroups + 
                " ,lastoperator = " + this.userid;
              if (!(tempcurrentnodetype.equals("3"))) {
                this.sql = this.sql + 
                  " ,lastoperatedate = '" + this.currentdate + "' " + 
                  " ,lastoperatetime = '" + this.currenttime + "' ";
              }
              this.sql = this.sql + 
                " ,lastoperatortype = " + this.usertype + 
                " ,nodepasstime = " + this.nodepasstime + 
                " ,nodelefttime = " + this.nodepasstime;
            }

            if (!("".equals(formsignaturemd5)))
            {
              RequestManager tmp8380_8379 = this; tmp8380_8379.sql = tmp8380_8379.sql + ", formsignaturemd5='" + formsignaturemd5 + "' ";
            }
            RequestManager tmp8419_8418 = this; tmp8419_8418.sql = tmp8419_8418.sql + " where requestid = " + this.requestid;

            if ((this.isoracle) && (this.src.equals("submit")) && ((((this.isremark != 1) && (this.isremark != 7)) || ((this.CanModify) && (((!("0".equals(this.coadismodify))) || (this.isremark != 7)))))))
              this.executesuccess = rst.executeUpdate(this.sql, new Object[] { this.hrmids });
            else {
              this.executesuccess = rst.executeSql(this.sql);
            }

            if (!(this.executesuccess)) {
              saveRequestLog("1");
              if (rst != null) {
                rst.rollback();
              }
              writeLog(this.sql);
              setMessage("126222");
              setMessagecontent(WorkflowRequestMessage.resolveDetailInfo("流程提交，更新RequestBase出错"));

              GCONST.WFProcessing.remove(this.requestid + "_" + this.nodeid);

              return false;
            }

            String wf_nextnodeids = "";
            for (int n = 0; n < this.nextnodeids.size(); ++n) {
              this.nextnodeid = Util.getIntValue((String)this.nextnodeids.get(n));
              wf_nextnodeids = wf_nextnodeids + this.nextnodeid + ", ";
              if ((this.isremark != 5) && (!(this.src.equals("intervenor"))) && (this.enableIntervenor == 1)) {
                try
                {
                  linkid = Util.getIntValue((String)this.nextlinkids.get(n));
                  RequestCheckAddinRules requestCheckAddinRules = new RequestCheckAddinRules();

                  requestCheckAddinRules.setTrack(this.isTrack);
                  requestCheckAddinRules.setStart(this.isStart);
                  requestCheckAddinRules.setNodeid(this.nodeid);

                  requestCheckAddinRules.resetParameter();
                  requestCheckAddinRules.setRequestid(this.requestid);
                  requestCheckAddinRules.setWorkflowid(this.workflowid);
                  requestCheckAddinRules.setObjid(linkid);
                  requestCheckAddinRules.setObjtype(0);
                  requestCheckAddinRules.setIsbill(this.isbill);
                  requestCheckAddinRules.setFormid(this.formid);
                  requestCheckAddinRules.setIspreadd("0");
                  requestCheckAddinRules.setUser(this.user);
                  String clientIp = "";
                  if (this.isRequest) {
                    if (this.request != null)
                      clientIp = Util.null2String(this.request.getRemoteAddr());
                  }
                  else if (this.fu != null) {
                    clientIp = Util.null2String(this.fu.getRemoteAddr());
                  }
                  requestCheckAddinRules.setClientIp(clientIp);
                  requestCheckAddinRules.setSrc(this.src);
                  requestCheckAddinRules.setRequestManager(this);
                  requestCheckAddinRules.checkAddinRules();

                  this.requestCheckAddinRulesMap = new HashMap();
                  this.requestCheckAddinRulesMap.put("objId", linkid);
                  this.requestCheckAddinRulesMap.put("objType", "0");
                  this.requestCheckAddinRulesMap.put("isPreAdd", "0");
                  this.requestCheckAddinRulesList.add(this.requestCheckAddinRulesMap);
                }
                catch (Exception erca) {
                  if (erca.getMessage().indexOf("workflow interface action error") > -1) {
                    writeLog(erca);
                    if (rst != null) {
                      rst.rollback();
                    }
                    if ("".equals(getMessageid())) {
                      setMessage("126221");
                    }
                    if ("".equals(getMessagecontent()))
                    {
                      setMessagecontent(WorkflowRequestMessage.resolveDetailInfo(SystemEnv.getHtmlLabelNames("15587,15616,83071", this.user.getLanguage())));
                    }

                    GCONST.WFProcessing.remove(this.requestid + "_" + this.nodeid);

                    return false;
                  }
                }
              }

              String drawbackflag = "";
              RecordSet tempRecordSet = new RecordSet();
              tempRecordSet.executeSql("select drawbackflag from workflow_flownode where workflowid=" + this.workflowid + " and nodeid=" + this.nextnodeid);
              if (tempRecordSet.next()) {
                drawbackflag = Util.null2String(tempRecordSet.getString("drawbackflag"));
              }

              int nextnodeattr = Util.getIntValue((String)this.nextnodeattrs.get(n), 0);

              boolean isMergeNode = (nextnodeattr == 3) || (nextnodeattr == 4) || (nextnodeattr == 5);
              boolean canflowtonextnode = false;

              if ((isMergeNode) && (!(this.src.equals("intervenor")))) {
                canflowtonextnode = this.wflinkinfo.FlowToNextNode(this.requestid, this.nodeid, this.nextnodeid, String.valueOf(nextnodeattr), Util.getIntValue((String)this.nextnodepassnums.get(n)), Util.getIntValue((String)this.linkismustpasss.get(n)));
              }

              if (((!(this.src.equals("reject"))) && (!(isMergeNode))) || ((!(this.src.equals("reject"))) && (isMergeNode) && (canflowtonextnode)) || ((this.src.equals("reject")) && (drawbackflag.equals("1"))) || ((this.src.equals("intervenor")) && (isMergeNode) && (this.enableIntervenor == 1))) {
                try
                {
                  RequestCheckAddinRules requestCheckAddinRules = new RequestCheckAddinRules();
                  requestCheckAddinRules.resetParameter();

                  requestCheckAddinRules.setTrack(this.isTrack);
                  requestCheckAddinRules.setStart(this.isStart);
                  requestCheckAddinRules.setNodeid(this.nodeid);

                  requestCheckAddinRules.setRequestid(this.requestid);
                  requestCheckAddinRules.setWorkflowid(this.workflowid);
                  requestCheckAddinRules.setObjid(this.nextnodeid);
                  requestCheckAddinRules.setObjtype(1);
                  requestCheckAddinRules.setIsbill(this.isbill);
                  requestCheckAddinRules.setFormid(this.formid);
                  requestCheckAddinRules.setIspreadd("1");
                  requestCheckAddinRules.setRequestManager(this);
                  requestCheckAddinRules.setUser(this.user);
                  String clientIp = "";
                  if (this.isRequest) {
                    if (this.request != null)
                      clientIp = Util.null2String(Util.getIpAddr(this.request));
                  }
                  else if (this.fu != null) {
                    clientIp = Util.null2String(this.fu.getRemoteAddr());
                  }
                  requestCheckAddinRules.setClientIp(clientIp);
                  requestCheckAddinRules.setSrc(this.src);
                  requestCheckAddinRules.checkAddinRules();

                  this.requestCheckAddinRulesMap = new HashMap();
                  this.requestCheckAddinRulesMap.put("objId", this.nextnodeid);
                  this.requestCheckAddinRulesMap.put("objType", "1");
                  this.requestCheckAddinRulesMap.put("isPreAdd", "1");
                  this.requestCheckAddinRulesList.add(this.requestCheckAddinRulesMap);
                }
                catch (Exception erca) {
                  if (erca.getMessage().indexOf("workflow interface action error") > -1) {
                    writeLog(erca);
                    if (rst != null) {
                      rst.rollback();
                    }
                    if ("".equals(getMessage())) {
                      setMessage("126221");
                    }
                    if ("".equals(getMessagecontent())) {
                      setMessagecontent(WorkflowRequestMessage.resolveDetailInfo(SystemEnv.getHtmlLabelNames("18009,83071", this.user.getLanguage())));
                    }

                    GCONST.WFProcessing.remove(this.requestid + "_" + this.nodeid);

                    return false;
                  }
                }
              }

              String rejectbackflag = "";
              RecordSet tempRejectRecordSet = new RecordSet();
              tempRejectRecordSet.executeSql("select rejectbackflag from workflow_flownode where workflowid=" + this.workflowid + " and nodeid=" + this.nodeid);
              if (tempRejectRecordSet.next()) {
                rejectbackflag = Util.null2String(tempRejectRecordSet.getString("rejectbackflag"));
              }
              String forkflag = "";
              RecordSet tempForkRecordSet = new RecordSet();
              tempForkRecordSet.executeSql("select isreject from workflow_nodelink where workflowid=" + this.workflowid + " and id=" + linkid);
              if (tempForkRecordSet.next()) {
                forkflag = Util.null2String(tempForkRecordSet.getString("isreject"));
              }
              if (this.src.equals("reject")) {
                if ("0".equals(rejectbackflag))
                {
                  this.rs.executeSql("select * from workflowactionview  where workflowid=" + this.workflowid + " and nodeid=" + this.nodeid + " and ispreoperator='0' and (id='action.WorkflowToDoc' or id='WorkflowToDoc') and isused = 1");
                  if (this.rs.next()) {
                    this.isWorkFlowToDoc = true;
                  }
                }
                if ("1".equals(forkflag))
                {
                  this.rs.executeSql("select * from workflowactionview  where workflowid=" + this.workflowid + " and nodelinkid=" + linkid + " and (id='action.WorkflowToDoc' or id='WorkflowToDoc') and isused = 1");
                  if (this.rs.next()) {
                    this.isWorkFlowToDoc = true;
                  }
                }
                if ("1".equals(drawbackflag)) {
                  this.rs.executeSql("select * from workflowactionview  where workflowid=" + this.workflowid + " and nodeid=" + this.nextnodeid + " and ispreoperator='1' and (id='action.WorkflowToDoc' or id='WorkflowToDoc') and isused = 1");

                  if (this.rs.next())
                    this.isWorkFlowToDoc = true;
                }
              }
              else {
                String todocsql = "";
                if (linkid == 0) {
                  linkid = -1;
                }
                if ("intervenor".equals(this.src)) {
                  if (this.enableIntervenor == 1)
                    todocsql = "select * from workflowactionview  where workflowid=" + this.workflowid + " and ((nodeid=" + this.nextnodeid + " and ispreoperator='1') or nodelinkid=" + linkid + " or (nodeid=" + this.nodeid + " and ispreoperator='0')) and (id='action.WorkflowToDoc' or id='WorkflowToDoc') and isused = 1 ";
                  else
                    todocsql = "select * from workflowactionview  where workflowid=" + this.workflowid + " and (nodelinkid=" + linkid + " or (nodeid=" + this.nodeid + " and ispreoperator='0')) and (id='action.WorkflowToDoc' or id='WorkflowToDoc') and isused = 1 ";
                }
                else {
                  todocsql = "select * from workflowactionview  where workflowid=" + this.workflowid + " and ((nodeid=" + this.nextnodeid + " and ispreoperator='1') or nodelinkid=" + linkid + " or (nodeid=" + this.nodeid + " and ispreoperator='0')) and (id='action.WorkflowToDoc' or id='WorkflowToDoc') and isused = 1 ";
                }
                this.rs.executeSql(todocsql);
                if (this.rs.next()) {
                  this.isWorkFlowToDoc = true;
                }

              }

            }

            int isusedworktask = Util.getIntValue(getPropValue("worktask", "isusedworktask"), 0);
            if (isusedworktask == 1) {
              this.sql = "select * from workflow_createtask where wfid=" + this.workflowid + " and ((nodeid=" + this.nodeid + " and changetime=2) or (changetime=1 and nodeid in (" + wf_nextnodeids + "0)))";
              rst.execute(this.sql);
              while (rst.next()) {
                int creatertype_tmp = Util.getIntValue(rst.getString("creatertype"), 0);
                if (creatertype_tmp == 0) {
                  continue;
                }
                int createtaskid_tmp = Util.getIntValue(rst.getString("id"), 0);
                int wffieldid_tmp = Util.getIntValue(rst.getString("wffieldid"), 0);
                int taskid_tmp = Util.getIntValue(rst.getString("taskid"), 0);
                int changemode_tmp = Util.getIntValue(rst.getString("changemode"), 0);
                int changenodeid_tmp = Util.getIntValue(rst.getString("nodeid"), 0);
                int changetime_tmp = Util.getIntValue(rst.getString("changetime"), 0);
                if ((changenodeid_tmp != this.nodeid) && ("," + wf_nextnodeids + ",".indexOf("," + changenodeid_tmp + ",") > -1) && 
                  (changetime_tmp == 1)) { if (("submit".equals(this.src)) && (changemode_tmp == 2)) continue; if (("reject".equals(this.src)) && (changemode_tmp == 1)) {
                    continue;
                  }
                }

                if ((changenodeid_tmp == this.nodeid) && 
                  (changetime_tmp == 2)) { if (("submit".equals(this.src)) && (changemode_tmp == 2)) continue; if (("reject".equals(this.src)) && (changemode_tmp == 1)) {
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

            RecordSet rs1 = new RecordSet();

            rst.executeSql("update workflow_currentoperator set isremark='2', preisremark='5',operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where isremark ='5' and requestid=" + this.requestid + " and nodeid=" + this.nodeid);
            if (this.src.equals("reject")) {
              rst.executeProc("workflow_CurOpe_UpdatebyReject", this.requestid + this.flag + this.nodeid);
              if (this.wflinkinfo.getNodeAttribute(this.nodeid) != 2) {
                rst.executeSql("update workflow_currentoperator set isreject='1' where (isreject is null or isreject!='1') and requestid=" + this.requestid);
              }
              else if (this.nextnodeattr != 2) {
                this.sql = "update workflow_currentoperator set isremark='2',operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where userid != " + this.userid + " and nodeid in (select nownodeid from workflow_nownode where requestid=" + this.requestid + ") and (isremark='0' or isremark='1') and requestid=" + this.requestid;
                rst.executeSql(this.sql);
              }

              this.sql = "update workflow_currentoperator set isremark='2',operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where isremark='1' and (nodeid=" + this.nodeid + " or nodeid in (select a.nodeid from workflow_flownode a,workflow_nodebase b where a.nodeid=b.id and b.nodeattribute in ('0','1','3','4','5')  and a.workflowid=" + this.workflowid + ")) and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype;
              rst.executeSql(this.sql);

              this.sql = "update workflow_currentoperator set isremark='2',operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where isremark='7' and nodeid=" + this.nodeid + " and requestid=" + this.requestid;
              rst.executeSql(this.sql);

              this.sql = "update workflow_currentoperator set isremark='2',operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where userid != " + this.userid + " and nodeid=" + this.nodeid + " and (isremark='0' or (isremark='1' and takisremark='2')) and requestid=" + this.requestid;
              rst.executeSql(this.sql);

              if (this.RejectToType > 0) {
                this.sql = "update workflow_currentoperator set isremark='2',operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where userid != " + this.userid + " and nodeid in (select nownodeid from workflow_nownode where requestid=" + this.requestid + ") and (isremark='0' or isremark='1') and requestid=" + this.requestid;
                rst.executeSql(this.sql);
              }
            }
            else
            {
              List tmpgroupidList;
              String isremarktmp;
              int i;
              int tmpgroupid;
              if ((this.isremark == 0) || (this.isremark == 7))
              {
                if (!(isExeOldFlowlogic))
                {
                  rst.executeSql("select distinct userid,usertype from workflow_currentoperator where isremark = '" + this.isremark + "' and requestid=" + this.requestid + "and nodeid=" + this.nodeid + " and groupid in(select distinct groupid from workflow_currentoperator where isremark = '" + this.isremark + "' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + ")");
                }
                else {
                  rst.executeSql("select distinct userid,usertype from workflow_currentoperator where isremark = '" + this.isremark + "' and requestid=" + this.requestid + "and nodeid=" + this.nodeid + " and groupid in(select distinct groupid from workflow_currentoperator where isremark = '" + this.isremark + "' and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + " and nodeid=" + this.nodeid + ")");
                }
                while (rst.next()) {
                  submituserids.add(rst.getString("userid"));
                  submitusertypes.add(rst.getString("usertype"));
                }
                tmpgroupidList = new ArrayList();

                if (!(isExeOldFlowlogic))
                {
                  rst.executeSql("select distinct groupid from workflow_currentoperator where isremark = '" + this.isremark + "' and requestid=" + this.requestid + " and nodeid=" + this.nodeid);
                }
                else {
                  rst.executeSql("select distinct groupid from workflow_currentoperator where isremark = '" + this.isremark + "' and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + " and nodeid=" + this.nodeid);
                }
                while (rst.next()) {
                  tmpgroupidList.add(Util.getIntValue(rst.getString(1), 0));
                }
                for (i = 0; i < tmpgroupidList.size(); ++i) {
                  tmpgroupid = Util.getIntValue((String)tmpgroupidList.get(i), 0);

                  if (!("0".equals(this.needwfback)))
                  {
                    workflow_CurOpe_UpdatebySubmit(rst, this.requestid, this.nodeid, this.userid, this.currentdate, this.currenttime, tmpgroupid, this.isremark);
                    if (this.isremark == 7) {
                      rst.executeSql("update workflow_currentoperator set operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where  isremark='7' and requestid =" + this.requestid + " and userid=" + this.userid + " and nodeid=" + this.nodeid);
                    } else {
                      rst.executeSql("update workflow_currentoperator set operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where (isremark = '5' or isremark='0' or isremark='1' or isremark='8' or isremark='9' or isremark='7') and requestid =" + this.requestid + " and userid=" + this.userid + " and nodeid=" + this.nodeid + " and groupid=" + tmpgroupid);
                      updateworkflowcurrenttakingopsoperator(rst, tmpgroupid);
                    }

                    if (((this.isremark == 7) && (wfcm.getSigntype().equals("0"))) || ((wfcm.getSignorder() == 0) && (wfcm.getSigntype().equals("1"))))
                      rst.executeSql("update workflow_currentoperator set isremark ='2' where ( isremark='0' or isremark='7') and requestid =" + this.requestid + " and nodeid=" + this.nodeid);
                  }
                  else
                  {
                    workflow_CurOpe_UbySubmitNB(rst, this.requestid, this.nodeid, this.userid, this.currentdate, this.currenttime, tmpgroupid, this.isremark);
                    rst.executeSql("update workflow_currentoperator set operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where (isremark = '5' or isremark='0' or isremark='1' or isremark='8' or isremark='9' or isremark='7') and requestid =" + this.requestid + " and userid=" + this.userid + " and nodeid=" + this.nodeid);
                    updateworkflowcurrenttakingopsoperator(rst, tmpgroupid);
                    if (((this.isremark == 7) && (wfcm.getSigntype().equals("0"))) || ((wfcm.getSignorder() == 0) && (wfcm.getSigntype().equals("1")))) {
                      rst.executeSql("update workflow_currentoperator set isremark ='2' where ( isremark='0' or isremark='7') and requestid =" + this.requestid + " and nodeid=" + this.nodeid);
                    }
                  }
                }
              }
              if ((this.isremark == 5) || (this.src.equals("intervenor"))) {
                rst.executeSql("update workflow_currentoperator set isremark='2',operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where (isremark = '5' or isremark='0' or isremark='1' or isremark='8' or isremark='9' or isremark='7') and requestid=" + this.requestid + " and nodeid=" + this.nodeid);
                String curr_nodeattribute = "0";
                String next_nodeattribute = "0";
                rst.executeSql("select nodeattribute from workflow_nodebase where id=" + this.nodeid);
                if (rst.next()) {
                  curr_nodeattribute = rst.getString(1);
                }
                rst.executeSql("select nodeattribute from workflow_nodebase where id=" + this.nextnodeid);
                if (rst.next()) {
                  next_nodeattribute = rst.getString(1);
                }
                if ((!(next_nodeattribute.equals("2"))) && (curr_nodeattribute.equals("2"))) {
                  rst.executeSql("update workflow_currentoperator set isremark='2',operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where (isremark = '5' or isremark='0' or isremark='1' or isremark='8' or isremark='9' or isremark='7') and requestid=" + this.requestid + " and nodeid!=" + this.nextnodeid);
                }
                if ((next_nodeattribute.equals("2")) && (curr_nodeattribute.equals("2")))
                {
                  rst.executeSql("update workflow_currentoperator set takisremark=null where requestid=" + this.requestid + " and takisremark=-2 and nodeid=" + this.nodeid);
                }
                else {
                  rst.executeSql("update workflow_currentoperator set takisremark=null where requestid=" + this.requestid + " and takisremark=-2 and nodeid!=" + this.nextnodeid);
                }
              }
              else if ((this.canflowtonextnode) && (((this.nextnodeattr == 3) || (this.nextnodeattr == 4) || (this.nextnodeattr == 5)))) {
                this.innodeids = this.wflinkinfo.getSummaryNodes(this.nextnodeid, this.workflowid, "", this.requestid);
                if (this.innodeids.equals("")) this.innodeids = "0";

                tmpgroupidList = new ArrayList();
                rst.executeSql("select distinct groupid from workflow_currentoperator where (isremark='0' or isremark = '5') and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + " and nodeid in(" + this.innodeids + ")");
                while (rst.next()) {
                  tmpgroupidList.add(Util.getIntValue(rst.getString(1), 0));
                }
                for (i = 0; i < tmpgroupidList.size(); ++i) {
                  tmpgroupid = Util.getIntValue((String)tmpgroupidList.get(i), 0);

                  rst.executeSql("update workflow_currentoperator set operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where (isremark='0' or isremark = '5') and requestid=" + this.requestid + " and nodeid in(" + this.innodeids + ") and groupid=" + tmpgroupid);
                  updateworkflowcurrenttakingopsoperator(rst, this.innodeids, tmpgroupid);
                }
                rst.executeSql("update workflow_currentoperator set isremark='2' where (isremark='0' or isremark = '5' or (isremark=1 and takisremark=2)) and requestid=" + this.requestid + " and nodeid in(" + this.innodeids + ")");
              }

              if (this.isremark == 0) {
                isremarktmp = "";

                if ((userids != null) && (userids.size() > 0)) {
                  for (i = 0; i < userids.size(); ++i) {
                    if (!(((String)coadispendings.get(i)).equals("1"))) {
                      this.sql = "update workflow_currentoperator set isremark='2' where isremark='7' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and userid= " + ((String)userids.get(i));
                      rst.executeSql(this.sql);
                    } else if (((String)coadispendings.get(i)).equals("1")) {
                      this.sql = "select viewtype from  workflow_currentoperator where isremark='7' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and userid= " + ((String)userids.get(i));
                      rs1.executeSql(this.sql);
                      if ((rs1.next()) && (((rs1.getString("viewtype").equals("-2")) || (rs1.getString("viewtype").equals("-1"))))) {
                        this.sql = "update workflow_currentoperator set isremark='2' where isremark='7' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and userid= " + ((String)userids.get(i));
                        rst.executeSql(this.sql);
                      }
                    }
                    wffm.updateForwardRemark(rst, this.IsBeForwardPending, this.IsSubmitedOpinion, this.IsBeForwardSubmit, true);
                  }
                } else {
                  if ((!(this.coadsigntype.equals("1"))) && (!(this.coadispending.equals("1")))) {
                    this.sql = "update workflow_currentoperator set isremark='2' where isremark='7' and requestid=" + this.requestid + " and nodeid=" + this.nodeid;
                    rst.executeSql(this.sql);
                  }
                  wffm.updateForwardRemark(rst, this.IsBeForwardPending, this.IsSubmitedOpinion, this.IsBeForwardSubmit, true);
                }

                this.sql = "update workflow_currentoperator set isremark='2',operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where isremark='1' and (nodeid=" + this.nodeid + " or nodeid in (select a.nodeid from workflow_flownode a,workflow_nodebase b where a.nodeid=b.id and b.nodeattribute in ('0','1','3','4')  and a.workflowid=" + this.workflowid + ")) and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype;
                rst.executeSql(this.sql);
              } else if ((this.isremark == 7) && (this.coadsigntype.equals("0"))) {
                isremarktmp = "";
                rst.executeSql("update workflow_currentoperator set isremark='2' where (isremark='0' or isremark='7') and requestid='" + this.requestid + "' and nodeid='" + this.nodeid + "' and groupdetailid in(" + wfcm.getSameGroupdetailids() + ")");
                wffm.updateForwardRemark(rst, this.IsBeForwardPending, this.IsSubmitedOpinion, this.IsBeForwardSubmit, true);
              }

            }

            if ((this.isremark == 5) || (this.src.equals("intervenor"))) {
              this.canflowtonextnode = true;
              setOperatorByremark5(tempusers, tempusersType, rst);
            } else {
              setOperator(rst);
            }

            if ((this.canflowtonextnode) && (this.nextnodetype.equals("3"))) {
              this.Procpara = this.creater + this.flag + this.creatertype + this.flag + this.requestid;

              RequestRemarkRight reqRight = new RequestRemarkRight();
              reqRight.setRequestid(this.requestid);
              reqRight.setNodeid(this.nodeid);
              reqRight.deleteAllRight();

              rst.executeSql("update  workflow_currentoperator  set isremark='4'  where (isremark='0' or isremark='5') and requestid = " + this.requestid);

              rst.executeSql("update workflow_currentoperator set islasttimes='1' where islasttimes='0' and (isremark='1' or isremark='8' or isremark='9') and requestid = " + this.requestid + " and exists (select 1 from workflow_currentoperator a where a.requestid=workflow_currentoperator.requestid and a.userid=workflow_currentoperator.userid and a.usertype=workflow_currentoperator.usertype and a.isremark='4')");
              rst.executeSql("update workflow_currentoperator set islasttimes='0' where islasttimes='1' and isremark='4' and requestid = " + this.requestid + " and exists (select 1 from workflow_currentoperator a where a.requestid=workflow_currentoperator.requestid and a.userid=workflow_currentoperator.userid and a.usertype=workflow_currentoperator.usertype and (a.isremark='1' or a.isremark='8' or a.isremark='9'))");

              rst.executeSql("update  workflow_currentoperator  set iscomplete=1  where isremark!='8' and isremark!='9' and isremark!='7' and requestid = " + this.requestid);

              if ((!(operatorsWfEnd.contains(this.creater + "_" + this.creatertype))) && (!(this.userid + "_" + this.usertype.equals(this.creater + "_" + this.creatertype))))
              {
                Map map = new HashMap();
                map.put("userid", Util.getIntValue(this.rs.getString("creater")));
                map.put("type", "1");
                map.put("logintype", this.creatertype);
                map.put("requestid", this.requestid);
                map.put("requestname", this.requestname);
                map.put("workflowid", this.workflowid);
                map.put("creater", this.creater);
                poppuplist.add(map);
                this.poppupRemindInfoUtil.insertPoppupRemindInfo(poppuplist);
              }

            }

            for (int n = 0; n < this.nextnodeids.size(); ++n) {
              this.nextnodeid = Util.getIntValue((String)this.nextnodeids.get(n));
              if (this.istest == 1)
                continue;
              this.sendMsgAndMail.sendMsg(rst, this.requestid, this.nextnodeid, this.user, this.src, this.nextnodetype);
              this.sendMsgAndMail.sendChats(rst, this.workflowid, this.requestid, this.nextnodeid, this.user, this.src, this.nextnodetype);
              this.sendMsgAndMail.sendMail(rst, this.workflowid, this.requestid, this.nextnodeid, this.request, this.fu, this.isRequest, this.src, this.nextnodetype, this.user);
            }

            String triggerStatus = "";
            boolean nextNodeHasCurrentNode = false;
            for (int n = 0; n < this.nextnodeids.size(); ++n) {
              this.nextnodeid = Util.getIntValue((String)this.nextnodeids.get(n));
              if (this.nextnodeid == this.nodeid) {
                nextNodeHasCurrentNode = true;
              }
            }
            if ((!(nextNodeHasCurrentNode)) && (this.nextnodeid > 0) && (this.nextnodeid != this.nodeid)) {
              nodeidList_sub.add(this.nodeid);
              triggerTimeList_sub.add("2");
              hasTriggeredSubwfList_sub.add(this.hasTriggeredSubwf);
            }

            if ((this.nextnodeids != null) && (this.nextnodeids.size() > 0) && (!(this.nextnodeids.contains(this.nodeid)))) {
              nodeidList_wp.add(this.nodeid);
              createTimeList_wp.add("2");
            }

            for (n = 0; n < this.nextnodeids.size(); ++n) {
              this.nextnodeid = Util.getIntValue((String)this.nextnodeids.get(n));
              if ((this.nextnodeid > 0) && (this.nextnodeid != this.nodeid)) {
                nodeidList_sub.add(this.nextnodeid);
                triggerTimeList_sub.add("1");
                hasTriggeredSubwfList_sub.add(this.hasTriggeredSubwf);

                nodeidList_wp.add(this.nextnodeid);
                createTimeList_wp.add("1");
              }
            }
            isReCalculatePermission = true;

            RequestRemarkRight reqRight = new RequestRemarkRight();
            reqRight.setRequestid(this.requestid);
            reqRight.setNodeid(this.nodeid);
            reqRight.deleteRemarkRight();
          }
          else {
            if (!(groupdetailids.equals("")));
            RecordSet rs1 = new RecordSet();
            if ((this.isremark == 0) || (
              (!(groupdetailids.equals(""))) && (this.coadcansubmit) && (this.isremark != 7)))
            {
              int isreject = 0;
              if (this.src.equals("reject")) {
                isreject = 1;
              }
              RequestNodeFlow requestNodeFlow = new RequestNodeFlow();
              requestNodeFlow.setRequestid(this.requestid);
              requestNodeFlow.setNodeid(this.nodeid);
              requestNodeFlow.setNodetype(this.nodetype);
              requestNodeFlow.setWorkflowid(this.workflowid);
              requestNodeFlow.setUserid(this.userid);
              requestNodeFlow.setUsertype(this.usertype);
              requestNodeFlow.setCreaterid(this.creater);
              requestNodeFlow.setCreatertype(this.creatertype);
              requestNodeFlow.setFormid(this.formid);
              requestNodeFlow.setIsbill(this.isbill);
              requestNodeFlow.setBillid(this.billid);
              requestNodeFlow.setBilltablename(this.billtablename);
              requestNodeFlow.setRecordSet(this.rs);
              requestNodeFlow.setRecordSetTrans(rst);
              requestNodeFlow.setIsreject(isreject);

              requestNodeFlow.setIsfixbill(isfixbill);
              requestNodeFlow.setLanguageid(this.userlanguage);

              requestNodeFlow.setCurrentdate(this.currentdate);
              requestNodeFlow.setCurrenttime(this.currenttime);

              boolean hasnextnodeoperator = requestNodeFlow.getNextOrderOperator(groupdetailids);
              if (!(hasnextnodeoperator)) {
                saveRequestLog("1");
                if (rst != null) {
                  new Exception("工作流下一节点操作者错误（依次逐个处理），因为无法获取到下一个操作者。").printStackTrace();
                  rst.rollback();
                }
                setMessage("126221");
                setMessagecontent(requestNodeFlow.getMessagecontent());

                GCONST.WFProcessing.remove(this.requestid + "_" + this.nodeid);

                return false;
              }

              this.nextnodeid = this.nodeid;
              this.nextnodetype = this.nodetype;
              this.nextnodeattr = this.wflinkinfo.getNodeAttribute(this.nextnodeid);
              this.nextnodeids.add(this.nextnodeid);
              this.nextnodetypes.add(this.nodetype);
              this.nextnodeattrs.add(this.nextnodeattr);
              this.operatorsht = requestNodeFlow.getOperators();
              this.operatorshts.add(this.operatorsht);
              haspassthisnode = true;

              penetrateFlag = requestNodeFlow.getPenetrateId();
              lastNodeId = requestNodeFlow.getLastNodeId();

              updatePoppupRemindInfoThisJava(rst, this.requestid);

              if ((this.isremark == 0) || (this.isremark == 7)) {
                updateworkflowcurrentoperator(rst, submituserids, submitusertypes, finishndgpids);
              }

              if (this.isremark == 0) {
                wffm.updateForwardRemark(rst, this.IsBeForwardPending, this.IsSubmitedOpinion, this.IsBeForwardSubmit, false);
              } else if ((this.isremark == 7) && (this.coadsigntype.equals("0"))) {
                String isremarktmp = "";
                if (!(this.coadispending.equals("1"))) {
                  isremarktmp = " (isremark='0' and viewtype=0) ";
                }
                if (!(isremarktmp.equals(""))) {
                  this.sql = "update workflow_currentoperator set isremark='2' where (" + isremarktmp + ") and (exists (select 1 from workflow_currentoperator c where c.requestid=" + this.requestid + " and c.nodeid=workflow_currentoperator.nodeid and c.groupdetailid=workflow_currentoperator.groupdetailid and c.id=" + this.RequestKey + " and exists(select id from workflow_groupdetail g where g.id=c.groupdetailid and g.signtype='0') and exists (select 1 from workflow_coadjutant a where a.requestid=" + this.requestid + " and a.coadjutantid=" + this.RequestKey + ")) ";
                  this.sql += ")";
                  rst.executeSql(this.sql);
                }
                wffm.updateForwardRemark(rst, this.IsBeForwardPending, this.IsSubmitedOpinion, this.IsBeForwardSubmit, false);
              }
              this.sql = "update workflow_currentoperator set isremark='2' where (isremark='0' or isremark='5') and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and groupdetailid in (" + groupdetailid4sql + ")";
              rst.executeSql(this.sql);
              setOperator(rst);
              this.showcoadjutant = true;
              if (this.istest != 1)
              {
                this.sendMsgAndMail.sendMsg(rst, this.requestid, this.nextnodeid, this.user, this.src, this.nextnodetype);

                this.sendMsgAndMail.sendChats(rst, this.workflowid, this.requestid, this.nextnodeid, this.user, this.src, this.nextnodetype);

                this.sendMsgAndMail.sendMail(rst, this.workflowid, this.requestid, this.nextnodeid, this.request, this.fu, this.isRequest, this.src, this.nextnodetype, this.user);
              }

              isReCalculatePermission = true;
            } else {
              this.nextnodeid = this.nodeid;
              this.nextnodetype = this.nodetype;
              this.nextnodeattr = this.wflinkinfo.getNodeAttribute(this.nextnodeid);
              this.nextnodeids.add(this.nextnodeid);
              this.nextnodetypes.add(this.nodetype);
              this.nextnodeattrs.add(this.nextnodeattr);
              this.operatorshts.add(this.operatorsht);

              WFAutoApproveUtils.processApproveLog(rst, this);

              if ((this.isremark == 0) || (this.isremark == 7)) {
                updateworkflowcurrentoperator(rst, submituserids, submitusertypes, finishndgpids);
              }

              if (this.isremark == 0) {
                int coadjutantuser = 0;

                coadjutantuser = wfcm.getCoadjutantUser(wfcm.getGroupdetailid(), this.nodeid, this.requestid, rst);
                if ("0".equals(wfcm.getSigntype())) {
                  boolean isupcoad = false;
                  if ((wfcm.getSignorder() == 1) || (wfcm.getSignorder() == 2)) {
                    rst.executeSql("select userid from workflow_currentoperator where isremark='0' and requestid='" + this.requestid + "' and nodeid='" + this.nodeid + "' and groupdetailid ='" + wfcm.getGroupdetailid() + "'");
                    isupcoad = !(rst.next());
                    if (this.userid == coadjutantuser) isupcoad = true;
                  } else if (wfcm.getSignorder() == 0) {
                    rst.executeSql("select userid from workflow_currentoperator where isremark='2' and preisremark='0' and  (isreject is null or isreject=0) and requestid='" + this.requestid + "' and nodeid='" + this.nodeid + "' and groupdetailid ='" + wfcm.getGroupdetailid() + "'");
                    isupcoad = rst.next();
                  }
                  if (isupcoad)
                    if (!(wfcm.getIspending().equals("1"))) {
                      this.sql = "update workflow_currentoperator set isremark='2' where isremark='7' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and userid= " + coadjutantuser;
                      rst.executeSql(this.sql);
                    } else if (wfcm.getIspending().equals("1")) {
                      this.sql = "select viewtype from  workflow_currentoperator where isremark='7' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and userid= " + coadjutantuser;
                      rst.executeSql(this.sql);
                      if ((rst.next()) && (((rst.getString("viewtype").equals("-2")) || (rst.getString("viewtype").equals("-1"))))) {
                        this.sql = "update workflow_currentoperator set isremark='2' where isremark='7' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and userid= " + coadjutantuser;
                        rst.executeSql(this.sql);
                      }
                    }
                }
                else if ("1".equals(wfcm.getSigntype())) {
                  if (wfcm.getSignorder() == 0) {
                    rs1.executeSql("select userid from workflow_currentoperator where isremark='2' and preisremark='7' and groupdetailid='" + wfcm.getGroupdetailid() + "' and requestid='" + this.requestid + "'");
                    if (rs1.next())
                      rst.executeSql("update workflow_currentoperator set isremark='2' where isremark='0' and requestid='" + this.requestid + "' and nodeid='" + this.nodeid + "' and groupdetailid='" + wfcm.getGroupdetailid() + "'");
                  }
                  else if ((wfcm.getSignorder() == 1) && (this.isremark == 0) && (this.userid == coadjutantuser)) {
                    rst.executeSql("update workflow_currentoperator set isremark='2',operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where isremark='7' and requestid='" + this.requestid + "' and nodeid='" + this.nodeid + "' and userid='" + this.userid + "' and groupdetailid='" + wfcm.getGroupdetailid() + "'");
                  }
                } else if ("2".equals(wfcm.getSigntype())) {
                  if (!(wfcm.getIspending().equals("1"))) {
                    rst.executeSql("select id from  workflow_currentoperator where isremark='0' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and groupdetailid= " + wfcm.getGroupdetailid());
                    if (!(rst.next())) {
                      this.sql = "update workflow_currentoperator set isremark='2' where isremark='7' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and userid= " + coadjutantuser;
                      rst.executeSql(this.sql);
                    }
                  } else if (wfcm.getIspending().equals("1")) {
                    this.sql = "select viewtype from  workflow_currentoperator where isremark='7' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and userid= " + coadjutantuser;
                    rst.executeSql(this.sql);
                    if ((rst.next()) && (((rst.getString("viewtype").equals("-2")) || (rst.getString("viewtype").equals("-1"))))) {
                      rst.executeSql("select id from  workflow_currentoperator where isremark='0' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and groupdetailid= " + wfcm.getGroupdetailid());
                      if (!(rst.next())) {
                        this.sql = "update workflow_currentoperator set isremark='2' where isremark='7' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and userid= " + coadjutantuser;
                        rst.executeSql(this.sql);
                      }
                    }
                  }
                }

                wffm.updateForwardRemark(rst, this.IsBeForwardPending, this.IsSubmitedOpinion, this.IsBeForwardSubmit, true);
              }
              else if ((this.isremark == 7) && (this.coadsigntype.equals("0"))) {
                String isremarktmp = "";
                rst.executeSql("update workflow_currentoperator set isremark='2' where (isremark='0' or isremark='7') and requestid='" + this.requestid + "' and nodeid='" + this.nodeid + "' and groupdetailid in(" + wfcm.getSameGroupdetailids() + ")");

                wffm.updateForwardRemark(rst, this.IsBeForwardPending, this.IsSubmitedOpinion, this.IsBeForwardSubmit, false);
              }
              else if ((this.isremark == 7) && (this.coadsigntype.equals("1")) && 
                (wfcm.getSignorder() == 0)) {
                rs1.executeSql("select userid from workflow_currentoperator where isremark='2' and preisremark='0' and (isreject is null or isreject='0') and groupdetailid='" + wfcm.getGroupdetailid() + "' and requestid='" + this.requestid + "'");
                if (rs1.next()) {
                  rst.executeSql("update workflow_currentoperator set isremark='2' where isremark='0' and requestid='" + this.requestid + "' and nodeid='" + this.nodeid + "' and groupdetailid='" + wfcm.getGroupdetailid() + "'");
                }

              }

            }

            this.status = new RequestNodeFlow().getLinkName(this.nodeid, this.billid, this.requestid, this.isbill, this.billtablename);
            if (this.canflowtonextnode) {
              if (this.nextnodeattr == 1)
                this.status = SystemEnv.getHtmlLabelName(21394, this.userlanguage);
              else if (this.nextnodeattr == 2)
                this.status = SystemEnv.getHtmlLabelName(21395, this.userlanguage);
            }
            else {
              this.status = SystemEnv.getHtmlLabelName(21395, this.userlanguage);
            }

            String formsignaturemd5 = "";
            try {
              if ("submit".equals(this.src))
                formsignaturemd5 = WFPathUtil.getFormValMD5(this.workflowid, this.requestid, this.isbill, this.formid, this.requestname, this.nodeid);
            }
            catch (Exception e) {
              e.printStackTrace();
            }

            if (this.status.equals(""))
            {
              this.sql = " update workflow_requestbase set  passedgroups = " + 
                this.passedgroups + 
                " ,requestname = '" + Util.fromScreen2(this.requestname, this.userlanguage) + "' " + 
                " ,lastoperator = " + this.userid;
              if (!(tempcurrentnodetype.equals("3"))) {
                this.sql = this.sql + 
                  " ,lastoperatedate = '" + this.currentdate + "' " + 
                  " ,lastoperatetime = '" + this.currenttime + "' ";
              }
              this.sql = this.sql + 
                " ,lastoperatortype = " + this.usertype;
              if (((this.isremark != 1) && (this.isremark != 7)) || (this.CanModify))
              {
                RequestManager tmp17940_17939 = this; tmp17940_17939.sql = tmp17940_17939.sql + " ,docids = '" + this.docids + "' " + 
                  " ,crmids = '" + this.crmids + "' ";
                if (this.isoracle) {
                  this.sql += " ,hrmids = ? ";
                }
                else
                {
                  RequestManager tmp18038_18037 = this; tmp18038_18037.sql = tmp18038_18037.sql + " ,hrmids = '" + this.hrmids + "' ";
                }
                RequestManager tmp18079_18078 = this; tmp18079_18078.sql = tmp18079_18078.sql + " ,prjids = '" + this.prjids + "' " + 
                  " ,cptids = '" + this.cptids + "' ";
              }

            }
            else
            {
              this.sql = " update workflow_requestbase set  passedgroups = " + 
                this.passedgroups + 
                " ,status = '" + Util.fromScreen2(this.status, this.userlanguage) + "' " + 
                " ,requestname = '" + Util.fromScreen2(this.requestname, this.userlanguage) + "' " + 
                " ,lastoperator = " + this.userid;
              if (!(tempcurrentnodetype.equals("3"))) {
                this.sql = this.sql + 
                  " ,lastoperatedate = '" + this.currentdate + "' " + 
                  " ,lastoperatetime = '" + this.currenttime + "' ";
              }
              this.sql = this.sql + 
                " ,lastoperatortype = " + this.usertype;
              if (((this.isremark != 1) && (this.isremark != 7)) || (this.CanModify))
              {
                RequestManager tmp18358_18357 = this; tmp18358_18357.sql = tmp18358_18357.sql + " ,docids = '" + this.docids + "' " + 
                  " ,crmids = '" + this.crmids + "' ";
                if (this.isoracle) {
                  this.sql += " ,hrmids = ? ";
                }
                else
                {
                  RequestManager tmp18456_18455 = this; tmp18456_18455.sql = tmp18456_18455.sql + " ,hrmids = '" + this.hrmids + "' ";
                }
                RequestManager tmp18497_18496 = this; tmp18497_18496.sql = tmp18497_18496.sql + " ,prjids = '" + this.prjids + "' " + 
                  " ,cptids = '" + this.cptids + "' ";
              }

            }

            if (!("".equals(formsignaturemd5)))
            {
              RequestManager tmp18568_18567 = this; tmp18568_18567.sql = tmp18568_18567.sql + ", formsignaturemd5='" + formsignaturemd5 + "' ";
            }
            RequestManager tmp18607_18606 = this; tmp18607_18606.sql = tmp18607_18606.sql + " where requestid = " + this.requestid;

            if ((this.isoracle) && (((!("0".equals(this.coadismodify))) || (this.isremark != 7))))
              rst.executeUpdate(this.sql, new Object[] { this.hrmids });
            else {
              rst.executeSql(this.sql);
            }

          }

          if ((!(this.ifchangstatus.equals(""))) && (isfeedback.equals("1")) && ((((isnullnotfeedback.equals("1")) && (!(Util.replace(this.remark, "\\<script\\>initFlashVideo\\(\\)\\;\\<\\/script\\>", "", 0, false).equals("")))) || (!(isnullnotfeedback.equals("1"))))))
          {
            rst.executeSql("update workflow_currentoperator set viewtype =-1  where needwfback='1' and requestid=" + this.requestid + " and userid<>" + this.userid + " and viewtype=-2");
          }

          rst.executeSql("update workflow_currentoperator set viewtype =-2 where requestid=" + this.requestid + "  and userid=" + this.userid + " and usertype = " + this.usertype + " and viewtype<>-2");
          rst.executeSql("update workflow_currentoperator set operatedate='" + this.currentdate + "', operatetime='" + this.currenttime + "' where (operatedate is null or operatedate='') and requestid=" + this.requestid + "  and userid=" + this.userid + " and usertype = " + this.usertype + " and viewtype=-2");
        } else {
          if (this.src.equals("save")) {
            if (this.isremark == 1)
            {
              if (cnodetype.equals("3"))
              {
                this.rs.executeSql("update workflow_currentoperator set islasttimes =0 where requestid=" + this.requestid + "  and userid=" + this.userid + " and usertype = " + this.usertype + "  ");
                this.rs.executeSql("update workflow_currentoperator set isremark ='2', islasttimes =1,iscomplete=1 where requestid=" + this.requestid + "  and userid=" + this.userid + " and usertype = " + this.usertype + "  and isremark='1'");

                this.rs.executeSql("update workflow_currentoperator set iscomplete=1 where requestid=" + this.requestid + " and isremark='2' and  agenttype = '1' and agentorbyagentid=" + this.userid + " ");
              }
              else if (!("0".equals(this.needwfback)))
              {
                workflow_CurOpe_UbyForward(rst, this.requestid, this.userid, this.usertype, this.currentdate, this.currenttime);
              }
              else {
                workflow_CurOpe_UbyForwardNB(rst, this.requestid, this.userid, this.usertype, this.currentdate, this.currenttime);
              }

              rst.executeSql("update workflow_currentoperator set islasttimes='0' where islasttimes='1' and isremark='2' and  requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + "  and exists (select 1 from workflow_currentoperator a where a.requestid=workflow_currentoperator.requestid and a.userid=workflow_currentoperator.userid and a.usertype=workflow_currentoperator.usertype and a.isremark='4')");
              rst.executeSql("update workflow_currentoperator set islasttimes='1' where islasttimes='0' and isremark='4' and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + "  and id=(select max(id) from workflow_currentoperator  a where a.islasttimes=workflow_currentoperator.islasttimes and a.isremark=workflow_currentoperator.isremark and a.requestid=workflow_currentoperator.requestid and a.userid=workflow_currentoperator.userid and a.usertype=workflow_currentoperator.usertype)");

              if ((!(this.ifchangstatus.equals(""))) && (isfeedback.equals("1")) && ((((isnullnotfeedback.equals("1")) && (!(Util.replace(this.remark, "\\<script\\>initFlashVideo\\(\\)\\;\\<\\/script\\>", "", 0, false).equals("")))) || (!(isnullnotfeedback.equals("1")))))) {
                rst.executeSql("update workflow_currentoperator set viewtype =-1  where needwfback='1' and requestid=" + this.requestid + " and userid<>" + this.userid + " and viewtype=-2");
              }
              haspassthisnode = true;
            }
            else
            {
              if ((this.isremark == 7) && ("2".endsWith(this.coadsigntype)) && (this.tempsrc.equals("submit"))) {
                rst.executeSql("select * from workflow_currentoperator where iscomplete='1' and requestid='" + this.requestid + "'");
                if (rst.next())
                  rst.executeSql("update workflow_currentoperator set isremark='2',iscomplete='1',preisremark='7',operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where isremark ='7' and requestid=" + this.requestid + " and nodeid=" + this.nodeid);
                else {
                  rst.executeSql("update workflow_currentoperator set isremark='2',preisremark='7',operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where isremark ='7' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and userid='" + this.userid + "'");
                }
              }
              this.status = new RequestNodeFlow().getLinkName(this.nodeid, this.billid, this.requestid, this.isbill, this.billtablename);
              this.nextnodeattr = this.wflinkinfo.getNodeAttribute(this.nodeid);
              if (this.canflowtonextnode) {
                if (this.nextnodeattr == 1)
                  this.status = SystemEnv.getHtmlLabelName(21394, this.userlanguage);
                else if (this.nextnodeattr == 2)
                  this.status = SystemEnv.getHtmlLabelName(21395, this.userlanguage);
              }
              else {
                this.status = SystemEnv.getHtmlLabelName(21395, this.userlanguage);
              }

              String formsignaturemd5 = "";
              try {
                if ("save".equals(this.src))
                  formsignaturemd5 = WFPathUtil.getFormValMD5(this.workflowid, this.requestid, this.isbill, this.formid, this.requestname, this.nodeid);
              }
              catch (Exception e) {
                e.printStackTrace();
              }

              if (this.status.equals(""))
              {
                this.sql = " update workflow_requestbase set  requestname = '" + 
                  Util.fromScreen2(this.requestname, this.userlanguage) + "' ";
                if (!("true".equals(this.isFromEditDocument))) {
                  this.sql = this.sql + " ,lastoperator = " + this.userid;
                }

                if ((!(tempcurrentnodetype.equals("3"))) && (!("true".equals(this.isFromEditDocument)))) {
                  this.sql = this.sql + 
                    " ,lastoperatedate = '" + this.currentdate + "' " + 
                    " ,lastoperatetime = '" + this.currenttime + "' ";
                }
                this.sql = this.sql + 
                  " ,lastoperatortype = " + this.usertype;
                if (((this.isremark != 1) && (this.isremark != 7)) || (this.CanModify))
                {
                  RequestManager tmp20103_20102 = this; tmp20103_20102.sql = tmp20103_20102.sql + " ,docids = '" + this.docids + "' " + 
                    " ,crmids = '" + this.crmids + "' ";
                  if (this.isoracle) {
                    this.sql += " ,hrmids = ? ";
                  }
                  else
                  {
                    RequestManager tmp20201_20200 = this; tmp20201_20200.sql = tmp20201_20200.sql + " ,hrmids = '" + this.hrmids + "' ";
                  }
                  RequestManager tmp20242_20241 = this; tmp20242_20241.sql = tmp20242_20241.sql + " ,prjids = '" + this.prjids + "' " + 
                    " ,cptids = '" + this.cptids + "' ";
                }

              }
              else
              {
                this.sql = " update workflow_requestbase set  requestname = '" + 
                  Util.fromScreen2(this.requestname, this.userlanguage) + "' " + 
                  " ,status = '" + Util.fromScreen2(this.status, this.userlanguage) + "' ";
                if (!("true".equals(this.isFromEditDocument))) {
                  this.sql = this.sql + " ,lastoperator = " + this.userid;
                }
                if ((!(tempcurrentnodetype.equals("3"))) && (!("true".equals(this.isFromEditDocument)))) {
                  this.sql = this.sql + 
                    " ,lastoperatedate = '" + this.currentdate + "' " + 
                    " ,lastoperatetime = '" + this.currenttime + "' ";
                }
                this.sql = this.sql + 
                  " ,lastoperatortype = " + this.usertype;
                if (((this.isremark != 1) && (this.isremark != 7)) || (this.CanModify))
                {
                  RequestManager tmp20555_20554 = this; tmp20555_20554.sql = tmp20555_20554.sql + " ,docids = '" + this.docids + "' " + 
                    " ,crmids = '" + this.crmids + "' ";
                  if (this.isoracle) {
                    this.sql += " ,hrmids = ? ";
                  }
                  else
                  {
                    RequestManager tmp20653_20652 = this; tmp20653_20652.sql = tmp20653_20652.sql + " ,hrmids = '" + this.hrmids + "' ";
                  }
                  RequestManager tmp20694_20693 = this; tmp20694_20693.sql = tmp20694_20693.sql + " ,prjids = '" + this.prjids + "' " + 
                    " ,cptids = '" + this.cptids + "' ";
                }

              }

              if (!("".equals(formsignaturemd5)))
              {
                RequestManager tmp20765_20764 = this; tmp20765_20764.sql = tmp20765_20764.sql + ", formsignaturemd5='" + formsignaturemd5 + "' ";
              }
              RequestManager tmp20804_20803 = this; tmp20804_20803.sql = tmp20804_20803.sql + " where requestid = " + this.requestid;

              if ((this.isoracle) && (((!("0".equals(this.coadismodify))) || (this.isremark != 7))))
                rst.executeUpdate(this.sql, new Object[] { this.hrmids });
              else {
                rst.executeSql(this.sql);
              }

            }

          }

          if (this.src.equals("delete"))
          {
            RequestDeleteLog log = new RequestDeleteLog();
            log.setRequestId(String.valueOf(this.requestid));
            log.setRequestName(Util.fromScreen2(this.requestname, this.userlanguage));
            log.setOperateUserId(String.valueOf(this.userid));
            log.setOperateDate(this.currentdate);
            log.setOperateTime(this.currenttime);
            log.setWorkflowId(String.valueOf(this.workflowid));
            String clientip = "";
            if (this.isRequest) {
              if (this.request != null)
                clientip = Util.null2String(this.request.getRemoteAddr());
            }
            else if (this.fu != null) {
              clientip = Util.null2String(this.fu.getRemoteAddr());
            }
            log.setClientAddress(clientip);

            RequestDeleteUtils rdu = new RequestDeleteUtils(this.requestid, rst, log);
            rdu.executeDeleteRequest();
            try
            {
              FnaCommon fnaCommon = new FnaCommon();
              fnaCommon.doWfForceOver(this.requestid, 0, true);
            } catch (Exception easi) {
              writeLog(easi);
            }
            deleteToDo(this.requestid);
          }
          if (this.src.equals("active"))
          {
            this.sql = " update workflow_requestbase set  lastoperator = " + 
              this.userid;
            if (!(tempcurrentnodetype.equals("3"))) {
              this.sql = this.sql + 
                " ,lastoperatedate = '" + this.currentdate + "' " + 
                " ,lastoperatetime = '" + this.currenttime + "' ";
            }
            this.sql = this.sql + 
              " ,lastoperatortype = " + this.usertype + 
              " ,deleted = 0 " + 
              " where requestid = " + this.requestid;
            rst.executeSql(this.sql);
          }

          if (this.src.equals("supervise"))
          {
            if ((!(this.ifchangstatus.equals(""))) && (isfeedback.equals("1")) && ((((isnullnotfeedback.equals("1")) && (!(Util.replace(this.remark, "\\<script\\>initFlashVideo\\(\\)\\;\\<\\/script\\>", "", 0, false).equals("")))) || (!(isnullnotfeedback.equals("1")))))) {
              this.sql = " update workflow_currentoperator set viewtype = -1 where needwfback='1' and requestid = " + this.requestid + " and viewtype=-2 and userid <>" + this.userid;
              rst.executeSql(this.sql);
            }

            this.sql = " update workflow_requestbase set  lastoperator = " + 
              this.userid;
            if (!(tempcurrentnodetype.equals("3"))) {
              this.sql = this.sql + 
                " ,lastoperatedate = '" + this.currentdate + "' " + 
                " ,lastoperatetime = '" + this.currenttime + "' ";
            }
            this.sql = this.sql + 
              " ,lastoperatortype = " + this.usertype + 
              " where requestid = " + this.requestid;
            rst.executeSql(this.sql);
          }
        }

        rst.commit();

        WFPathUtil wfutil = new WFPathUtil();

        if ((this.poppupRemindInfoUtil.getPoppuplist() != null) && (!(this.poppupRemindInfoUtil.getPoppuplist().isEmpty()))) {
          wfutil.getFixedThreadPool().execute(this.poppupRemindInfoUtil);
        }
        wfutil.getFixedThreadPool().execute(new RequestPreProcessing(this.iscreate, this.workflowid, this.isbill, this.formid, this.requestid, this.requestname, this.oldformsignaturemd5, this.nodeid, this.nextnodeid, haspassthisnode, finishndgpids, this.user, false));
      }
      catch (Exception exception)
      {
        exception.printStackTrace();
        rst.rollback();

        GCONST.WFProcessing.remove(this.requestid + "_" + this.nodeid);

        return false;
      }
      if ((this.src.equals("submit")) || (this.src.equals("reject")) || (this.src.equals("reopen")) || (this.src.equals("save")) || (this.src.equals("supervise")) || (this.src.equals("intervenor")))
      {
        if (this.src.equals("reject")) {
          String ischangrejectnode = "";
          this.sql = "select isrejectremind,ischangrejectnode from workflow_flownode where workflowid=" + this.workflowid + " and nodeid=" + this.nodeid;
          this.rs.executeSql(this.sql);
          if (this.rs.next()) {
            this.isrejectremind = Util.null2String(this.rs.getString("isrejectremind"));
            ischangrejectnode = Util.null2String(this.rs.getString("ischangrejectnode"));
          }
          if (this.isrejectremind.equals("1")) {
            if (ischangrejectnode.equals("1")) {
              if (this.isRequest) {
                if (this.request != null) {
                  this.rejectremindnodes = Util.null2String(this.request.getParameter("RejectNodes"));
                }
              }
              else if (this.fu != null) {
                this.rejectremindnodes = Util.null2String(this.fu.getParameter("RejectNodes"));
              }
            }
            else {
              this.rejectremindnodes = "-1";
            }

            if (!(this.rejectremindnodes.equals("")))
            {
              this.sql = "select distinct userid,usertype from workflow_currentoperator where requestid=" + this.requestid + " and not (userid=" + this.userid + " and usertype=" + this.usertype + ") ";
              if (!(this.rejectremindnodes.equals("-1")))
              {
                RequestManager tmp22108_22107 = this; tmp22108_22107.sql = tmp22108_22107.sql + " and nodeid in(" + this.rejectremindnodes + ")";
              }

              this.rs.executeSql(this.sql);
              while (this.rs.next())
              {
                Map map = new HashMap();
                map.put("userid", Util.getIntValue(this.rs.getString("userid")));
                map.put("type", "14");
                map.put("logintype", this.rs.getString("usertype"));
                map.put("requestid", this.requestid);
                map.put("requestname", this.requestname);
                map.put("workflowid", this.workflowid);
                map.put("creater", this.creater);
                poppuplist.add(map);
              }
              this.poppupRemindInfoUtil.insertPoppupRemindInfo(poppuplist);
            }
          }
        }
        this.islogsuccess = saveRequestLog2();

        if (this.nodeInfoCache.size() > 0) {
          WFAutoApproveThreadPoolUtil.getFixedThreadPool().execute(new WFAutoApproveUtils(this, (WFAutoApproveUtils.AutoApproveParams)this.nodeInfoCache.get(this.nodeInfoCache.keySet().iterator().next())));
        }

      }

      if (!("".equals(penetrateFlag)))
      {
        if (penetrateFlag.indexOf(",") >= 0) {
          penetrateFlag = penetrateFlag.substring(penetrateFlag.indexOf(",") + 1, penetrateFlag.length());
          updatePrenetrateLog(penetrateFlag, lastNodeId);
        } else {
          updatePrenetrateLog(penetrateFlag, lastNodeId);
        }

      }

      try
      {
        if (WFSubDataAggregation.checkSubProcessSummary(this.requestid)) {
          String cmainRequestId = SubWorkflowTriggerService.getMainRequestId(this.requestid);
          if ((cmainRequestId != null) && (cmainRequestId.length() != 0) && 
            (this.canflowtonextnode) && (this.nextnodetype.equals("3"))) {
            WFSubDataAggregation.addMainRequestDetail(cmainRequestId, this.requestid, -1, this.user);
          }

        }

        WFSubDataAggregation.subWfFileReminMainWf(this.requestid, this.user, this.request, this.fu, this.isRequest);

        List subList = WFSubDataAggregation.getSubRequestIdByMain(this.requestid, this.workflowid, this.nextnodeid);
        if (subList.size() > 0) {
          for (int r = 0; r < subList.size(); ++r)
            WFSubDataAggregation.addMainRequestDetail(this.requestid, (String)subList.get(r), this.nextnodeid, this.user);
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }

      try
      {
        ExecutorService fixedThreadPool = new WFPathUtil().getFixedThreadPool();
        for (int i = 0; i < nodeidList_sub.size(); ++i) {
          int nodeid_tmp = Util.getIntValue((String)nodeidList_sub.get(i), 0);
          String triggerTime_tmp = Util.null2String((String)triggerTimeList_sub.get(i));
          String hasTriggeredSubwf_tmp = Util.null2String((String)hasTriggeredSubwfList_sub.get(i));
          SubWorkflowTriggerServiceThread triggerService = new SubWorkflowTriggerServiceThread(this, nodeid_tmp, this.hasTriggeredSubwf, this.user, triggerTime_tmp);

          fixedThreadPool.execute(triggerService);
        }
      } catch (Exception fixedThreadPool) {
        writeLog(e);
      }

      try
      {
        String clientip = "";
        if (this.isRequest) {
          if (this.request != null) {
            clientip = Util.null2String(this.request.getRemoteAddr());
          }
        }
        else if (this.fu != null) {
          clientip = Util.null2String(this.fu.getRemoteAddr());
        }

        String sqlExt = "";
        for (int i = 0; i < nodeidList_wp.size(); ++i) {
          String nodeid_tmp = Util.null2String((String)nodeidList_wp.get(i));
          String createTime_tmp = Util.null2String((String)createTimeList_wp.get(i));
          sqlExt = sqlExt + " (nodeid=" + nodeid_tmp + " and changetime=" + createTime_tmp + ") or";
        }
        if ((!("".equals(sqlExt))) && (this.src.equals("submit"))) {
          CreateWorkplanByWorkflow createWorkplanByWorkflow = null;
          sqlExt = sqlExt.substring(0, sqlExt.length() - 2);
          RecordSet rs_wp = new RecordSet();
          rs_wp.execute("select * from workflow_createplan where wfid=" + this.workflowid + " and (" + sqlExt + ") order by id");
          while (rs_wp.next()) {
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
      } catch (Exception clientip) {
        writeLog(e);
      }

      if (((this.src.equals("save")) && (this.isremark == 1)) || (this.src.equals("submit")) || (this.src.equals("reject")) || (this.src.equals("intervenor"))) {
        if (this.istest != 1) {
          try {
            User sharedUser = this.user;
            try {
              if (this.src.equals("intervenor")) {
                this.rs.execute("select userid from workflow_currentoperator where requestid=" + this.requestid + " and (isremark=0 or preisremark=0) order by id");
                if (this.rs.next()) {
                  int sharedUserid = Util.getIntValue(this.rs.getString("userid"));
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
            for (int n = 0; n < this.nextnodeids.size(); ++n) {
              this.nextnodeid = Util.getIntValue((String)this.nextnodeids.get(n), 0);
              shareinfo.setRequestid(this.requestid);
              shareinfo.SetWorkFlowID(this.workflowid);
              shareinfo.SetNowNodeID(this.nodeid);
              if (this.nextnodeid == 0)
                shareinfo.SetNextNodeID(this.nodeid);
              else
                shareinfo.SetNextNodeID(this.nextnodeid);
              shareinfo.setIsbill(this.isbill);
              shareinfo.setUser(sharedUser);
              shareinfo.SetIsWorkFlow(1);
              shareinfo.setBillTableName(this.billtablename);
              shareinfo.setHaspassnode(haspassthisnode);
              shareinfo.setRequestCheckAddinRulesList(this.requestCheckAddinRulesList);

              shareinfo.setReCalculatePermission(isReCalculatePermission);
              shareinfo.addShareInfo();
            }
          }
          catch (Exception localException2)
          {
          }
        }
        if ((this.canflowtonextnode) && (this.nextnodetype.equals("3"))) {
          this.rs.execute("select docRightByOperator from workflow_base where id=" + this.workflowid);
          if ((this.rs.next()) && 
            (Util.getIntValue(this.rs.getString("docRightByOperator"), 0) == 1)) {
            this.rs.execute("delete from Workflow_DocSource where requestid =" + this.requestid);
          }

          this.rs.execute("delete from workflow_groupdetail where groupid=-2 and id in(select groupdetailid from workflow_currentoperator where groupid=-2 and requestid =" + this.requestid + ")");
          this.rs.execute("delete from workflow_agentpersons where requestid =" + this.requestid);
        }

        for (int i = 0; i < submituserids.size(); ++i) {
          int tempworkflowcurrid = 0;
          int tempislasttimes = 0;
          this.rs.executeSql("select id,islasttimes from workflow_currentoperator where (isremark = '5' or isremark='0' or isremark='1' or isremark='8' or isremark='9' or isremark='7') and requestid=" + this.requestid + " and userid=" + submituserids.get(i) + " and usertype=" + submitusertypes.get(i) + " order by id");
          while (this.rs.next()) {
            int tislasttimes = this.rs.getInt("islasttimes");
            if (tislasttimes != 1) {
              tempworkflowcurrid = this.rs.getInt("id");
            } else {
              tempislasttimes = 1;
              break;
            }
          }
          if ((tempislasttimes == 0) && (tempworkflowcurrid > 0)) {
            this.rs.executeSql("update workflow_currentoperator set islasttimes=0 where requestid=" + this.requestid + " and userid=" + submituserids.get(i) + " and usertype=" + submitusertypes.get(i));
            this.rs.executeSql("update workflow_currentoperator set islasttimes=1 where id=" + tempworkflowcurrid);
          }
        }
      }
      try
      {
        RecordSet rs001 = new RecordSet();
        RecordSet rs002 = new RecordSet();
        sqltmp = "select userid, islasttimes, id, isremark from workflow_currentoperator where usertype=0 and requestid=" + this.requestid + " order by userid, islasttimes,case isremark when '4' then '1.5' when '9' then '1.4' else isremark end desc, id asc";
        ArrayList userid2List = new ArrayList();
        ArrayList islasttimes2List = new ArrayList();
        ArrayList id2List = new ArrayList();
        ArrayList isRemark2List = new ArrayList();
        rs001.execute(sqltmp);
        while (rs001.next()) {
          int userid_tmp = Util.getIntValue(rs001.getString("userid"), 0);
          int islasttimes_tmp = Util.getIntValue(rs001.getString("islasttimes"), 0);
          int id_tmp = Util.getIntValue(rs001.getString("id"), 0);
          userid2List.add(userid_tmp);
          islasttimes2List.add(islasttimes_tmp);
          id2List.add(id_tmp);
          isRemark2List.add(Util.null2String(rs001.getString("isremark")));
        }
        int userid_t = 0;
        int islasttimes_t = -1;
        int id_t = 0;
        int isremark_t = -1;
        for (int cx = 0; cx < userid2List.size(); ++cx) {
          int userid_tmp = Util.getIntValue((String)userid2List.get(cx), 0);
          int islasttimes_tmp = Util.getIntValue((String)islasttimes2List.get(cx), 0);
          int id_tmp = Util.getIntValue((String)id2List.get(cx), 0);
          int isremark_tmp = Util.getIntValue((String)isRemark2List.get(cx));
          if (userid_tmp == userid_t) {
            if ((islasttimes_t == 1) && (islasttimes_tmp == 1)) {
              sqltmp = "update workflow_currentoperator set islasttimes=0 where id=" + id_t;
              rs002.execute(sqltmp);
            } else if ((islasttimes_t == 0) && (islasttimes_tmp == 1) && (isremark_t == 0) && (((isremark_tmp == 8) || (isremark_tmp == 9)))) {
              sqltmp = "update workflow_currentoperator set islasttimes=1 where id=" + id_t;
              rs002.execute(sqltmp);
              sqltmp = "update workflow_currentoperator set islasttimes=0 where id=" + id_tmp;
              rs002.execute(sqltmp);
            }
          }
          else if (islasttimes_t == 0) {
            sqltmp = "update workflow_currentoperator set islasttimes=1 where id=" + id_t;
            rs002.execute(sqltmp);
          }

          userid_t = userid_tmp;
          islasttimes_t = islasttimes_tmp;
          id_t = id_tmp;
          isremark_t = isremark_tmp;
        }
        if (islasttimes_t == 0) {
          sqltmp = "update workflow_currentoperator set islasttimes=1 where id=" + id_t;
          rs002.execute(sqltmp);
        }

        if (this.wflinkinfo.getNodeAttribute(this.nodeid) == 2)
          CheckUserIsLasttimes(this.requestid, this.nodeid, this.user);
      }
      catch (Exception rs001) {
        writeLog(e);
      }
      if ((((this.src.equals("submit")) || (this.src.equals("reject")) || (this.src.equals("reopen")) || (this.src.equals("save")) || (this.src.equals("supervise")) || (this.src.equals("intervenor")))) && 
        (rolm != null))
      {
        rolm.flowTransSubmitAfter();
      }

      if (this.nextnodetype.equals("3")) {
        RequestManageWriteBackAction writebackaction = new RequestManageWriteBackAction();
        writebackaction.doWriteBack(this.requestid);
      }

      if ((this.isWorkFlowToDoc) && (this.nodeid != this.nextnodeid))
      {
        Action action = (Action)StaticObj.getServiceByFullname("action.WorkflowToDoc", Action.class);
        RequestService requestService = new RequestService();

        sqltmp = action.execute(requestService.getRequest(this));
      }

      SubWorkflowTriggerService.judgeMainWfAutoFlowNextNode(this.requestid);
      try
      {
        new SetNewRequestTitle().getAllRequestName(this.rs, this.requestid, this.requestname, this.workflowid, this.nodeid, this.formid, this.isbill, this.userlanguage);
      }
      catch (Exception localException3)
      {
      }
      if ((this.src.equals("intervenor")) && (!(this.nextnodetype.equals("3"))))
      {
        this.rs.executeSql("update workflow_currentoperator set iscomplete=0 where requestid=" + this.requestid);
        this.rs.executeSql("update workflow_currentoperator set isremark='2' where isremark='4' and requestid=" + this.requestid);

        this.rs.executeSql(" update workflow_requestbase set dataaggregated = '' where requestid = " + this.requestid);
      }
      if (this.nextnodetype.equals("3")) {
        this.rs.executeSql("update workflow_currentoperator set isremark='4' where isremark='2' and nodeId=" + this.nextnodeid + " and requestid=" + this.requestid);
      }

      if (this.nextnodetype.equals("0")) {
        try
        {
          FnaCommon fnaCommon = new FnaCommon();
          fnaCommon.doWfForceOver(this.requestid, 0, true);
        } catch (Exception easi) {
          new BaseBean().writeLog(easi);
        }
      }

      WFUrgerManager WFUrgerManager = new WFUrgerManager();
      WFUrgerManager.setLogintype(Util.getIntValue(this.user.getLogintype()));
      WFUrgerManager.setUserid(this.user.getUID());
      WFUrgerManager.insertUrgerByRequestid(this.requestid);

      RecordSet rst1 = new RecordSet();
      int isvalid1 = 0;
      rst1.execute("select isvalid from workflow_base where id=" + this.workflowid);
      if (rst1.next()) {
        isvalid1 = Util.getIntValue(rst1.getString("isvalid"), 0);
        if (isvalid1 == 1) {
          PostWorkflowInf postworkflowinf = new PostWorkflowInf();
          postworkflowinf.operateToDo(this.requestid);
        }

      }

      return doOutWork(this.workflowid, this.requestid);
    }
    finally
    {
      GCONST.WFProcessing.remove(this.requestid + "_" + this.nodeid);
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
    writeLog("===删除流程传输统一代办库---end " + requestid + "==="); }

  public Map<String, String> buildDataMap(String syscode, String flowid) {
    Map dataMap = new HashMap();
    dataMap.put("syscode", syscode);
    dataMap.put("flowid", flowid);
    return dataMap; } 
  // ERROR //
  private void updatePrenetrateLog(String penetrateFlag, String lastNodeId) { // Byte code:
    //   0: new 604	java/lang/StringBuilder
    //   3: dup
    //   4: ldc_w 3682
    //   7: invokespecial 620	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   10: aload_0
    //   11: getfield 292	weaver/workflow/request/RequestManager:requestid	I
    //   14: invokevirtual 738	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   17: invokevirtual 629	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   20: astore_3
    //   21: aload_0
    //   22: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   25: aload_3
    //   26: invokevirtual 943	weaver/conn/RecordSet:executeSql	(Ljava/lang/String;)Z
    //   29: pop
    //   30: aload_0
    //   31: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   34: invokevirtual 653	weaver/conn/RecordSet:next	()Z
    //   37: ifeq +2645 -> 2682
    //   40: aload_0
    //   41: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   44: ldc_w 3684
    //   47: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   50: astore 4
    //   52: new 604	java/lang/StringBuilder
    //   55: dup
    //   56: ldc_w 3686
    //   59: invokespecial 620	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   62: aload 4
    //   64: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   67: ldc_w 922
    //   70: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   73: invokevirtual 629	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   76: astore 5
    //   78: aload_0
    //   79: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   82: aload 5
    //   84: invokevirtual 943	weaver/conn/RecordSet:executeSql	(Ljava/lang/String;)Z
    //   87: pop
    //   88: aload_0
    //   89: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   92: invokevirtual 653	weaver/conn/RecordSet:next	()Z
    //   95: ifeq +1099 -> 1194
    //   98: aload_0
    //   99: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   102: ldc_w 3688
    //   105: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   108: invokestatic 474	weaver/general/Util:null2String	(Ljava/lang/String;)Ljava/lang/String;
    //   111: astore 6
    //   113: aload_1
    //   114: ldc_w 769
    //   117: invokevirtual 1025	java/lang/String:indexOf	(Ljava/lang/String;)I
    //   120: iflt +540 -> 660
    //   123: new 604	java/lang/StringBuilder
    //   126: dup
    //   127: ldc_w 3689
    //   130: invokespecial 620	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   133: aload_0
    //   134: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   137: ldc_w 3691
    //   140: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   143: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   146: ldc_w 1426
    //   149: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   152: ldc_w 3693
    //   155: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   158: aload_0
    //   159: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   162: ldc_w 3695
    //   165: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   168: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   171: ldc_w 3697
    //   174: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   177: aload_0
    //   178: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   181: ldc_w 3699
    //   184: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   187: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   190: ldc_w 3701
    //   193: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   196: ldc_w 3703
    //   199: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   202: aload_0
    //   203: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   206: ldc_w 3705
    //   209: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   212: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   215: ldc_w 3706
    //   218: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   221: aload_0
    //   222: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   225: ldc_w 3708
    //   228: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   231: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   234: ldc_w 1426
    //   237: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   240: ldc_w 3710
    //   243: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   246: aload_0
    //   247: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   250: ldc_w 1272
    //   253: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   256: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   259: ldc_w 3712
    //   262: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   265: aload_0
    //   266: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   269: ldc_w 3714
    //   272: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   275: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   278: ldc_w 3716
    //   281: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   284: aload_0
    //   285: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   288: ldc_w 3718
    //   291: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   294: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   297: ldc_w 1426
    //   300: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   303: ldc_w 3720
    //   306: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   309: aload_0
    //   310: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   313: ldc_w 3722
    //   316: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   319: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   322: ldc_w 3724
    //   325: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   328: aload_0
    //   329: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   332: ldc_w 3726
    //   335: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   338: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   341: ldc_w 3728
    //   344: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   347: aload_0
    //   348: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   351: ldc_w 3730
    //   354: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   357: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   360: ldc_w 1426
    //   363: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   366: ldc_w 3732
    //   369: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   372: aload_0
    //   373: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   376: ldc_w 3734
    //   379: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   382: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   385: ldc_w 3735
    //   388: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   391: aload_0
    //   392: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   395: ldc_w 3737
    //   398: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   401: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   404: ldc_w 3738
    //   407: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   410: aload_0
    //   411: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   414: ldc_w 3740
    //   417: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   420: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   423: ldc_w 3742
    //   426: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   429: aload_0
    //   430: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   433: ldc_w 3744
    //   436: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   439: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   442: ldc_w 1426
    //   445: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   448: ldc_w 3746
    //   451: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   454: aload_0
    //   455: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   458: ldc_w 3748
    //   461: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   464: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   467: ldc_w 1426
    //   470: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   473: ldc_w 3750
    //   476: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   479: aload_0
    //   480: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   483: ldc_w 3752
    //   486: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   489: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   492: ldc_w 3753
    //   495: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   498: aload_1
    //   499: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   502: ldc_w 1070
    //   505: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   508: invokevirtual 629	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   511: astore 7
    //   513: aload_0
    //   514: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   517: aload 7
    //   519: invokevirtual 943	weaver/conn/RecordSet:executeSql	(Ljava/lang/String;)Z
    //   522: pop
    //   523: new 604	java/lang/StringBuilder
    //   526: dup
    //   527: ldc_w 3755
    //   530: invokespecial 620	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   533: aload_1
    //   534: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   537: ldc_w 1070
    //   540: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   543: invokevirtual 629	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   546: astore 8
    //   548: aconst_null
    //   549: astore 9
    //   551: new 1637	weaver/conn/ConnStatement
    //   554: dup
    //   555: invokespecial 1639	weaver/conn/ConnStatement:<init>	()V
    //   558: astore 9
    //   560: aload 9
    //   562: aload 8
    //   564: invokevirtual 1640	weaver/conn/ConnStatement:setStatementSql	(Ljava/lang/String;)V
    //   567: aload 9
    //   569: iconst_1
    //   570: aload_0
    //   571: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   574: ldc_w 2573
    //   577: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   580: invokevirtual 1643	weaver/conn/ConnStatement:setString	(ILjava/lang/String;)V
    //   583: aload 9
    //   585: iconst_2
    //   586: aload 6
    //   588: invokevirtual 1643	weaver/conn/ConnStatement:setString	(ILjava/lang/String;)V
    //   591: aload 9
    //   593: invokevirtual 1647	weaver/conn/ConnStatement:executeUpdate	()I
    //   596: pop
    //   597: goto +39 -> 636
    //   600: astore 10
    //   602: aload_0
    //   603: aload 10
    //   605: invokevirtual 1268	weaver/workflow/request/RequestManager:writeLog	(Ljava/lang/Object;)V
    //   608: aload 9
    //   610: ifnull +584 -> 1194
    //   613: aload 9
    //   615: invokevirtual 1650	weaver/conn/ConnStatement:close	()V
    //   618: goto +576 -> 1194
    //   621: astore 11
    //   623: aload 9
    //   625: ifnull +8 -> 633
    //   628: aload 9
    //   630: invokevirtual 1650	weaver/conn/ConnStatement:close	()V
    //   633: aload 11
    //   635: athrow
    //   636: aload 9
    //   638: ifnull +556 -> 1194
    //   641: aload 9
    //   643: invokevirtual 1650	weaver/conn/ConnStatement:close	()V
    //   646: goto +548 -> 1194
    //   649: astore 8
    //   651: aload_0
    //   652: aload 8
    //   654: invokevirtual 1268	weaver/workflow/request/RequestManager:writeLog	(Ljava/lang/Object;)V
    //   657: goto +537 -> 1194
    //   660: new 604	java/lang/StringBuilder
    //   663: dup
    //   664: ldc_w 3689
    //   667: invokespecial 620	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   670: aload_0
    //   671: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   674: ldc_w 3691
    //   677: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   680: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   683: ldc_w 1426
    //   686: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   689: ldc_w 3693
    //   692: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   695: aload_0
    //   696: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   699: ldc_w 3695
    //   702: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   705: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   708: ldc_w 3697
    //   711: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   714: aload_0
    //   715: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   718: ldc_w 3699
    //   721: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   724: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   727: ldc_w 3701
    //   730: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   733: ldc_w 3703
    //   736: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   739: aload_0
    //   740: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   743: ldc_w 3705
    //   746: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   749: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   752: ldc_w 3706
    //   755: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   758: aload_0
    //   759: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   762: ldc_w 3708
    //   765: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   768: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   771: ldc_w 1426
    //   774: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   777: ldc_w 3710
    //   780: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   783: aload_0
    //   784: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   787: ldc_w 1272
    //   790: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   793: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   796: ldc_w 3712
    //   799: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   802: aload_0
    //   803: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   806: ldc_w 3714
    //   809: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   812: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   815: ldc_w 3716
    //   818: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   821: aload_0
    //   822: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   825: ldc_w 3718
    //   828: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   831: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   834: ldc_w 1426
    //   837: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   840: ldc_w 3720
    //   843: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   846: aload_0
    //   847: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   850: ldc_w 3722
    //   853: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   856: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   859: ldc_w 3724
    //   862: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   865: aload_0
    //   866: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   869: ldc_w 3726
    //   872: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   875: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   878: ldc_w 3728
    //   881: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   884: aload_0
    //   885: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   888: ldc_w 3730
    //   891: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   894: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   897: ldc_w 1426
    //   900: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   903: ldc_w 3732
    //   906: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   909: aload_0
    //   910: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   913: ldc_w 3734
    //   916: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   919: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   922: ldc_w 3735
    //   925: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   928: aload_0
    //   929: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   932: ldc_w 3737
    //   935: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   938: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   941: ldc_w 3738
    //   944: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   947: aload_0
    //   948: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   951: ldc_w 3740
    //   954: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   957: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   960: ldc_w 3742
    //   963: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   966: aload_0
    //   967: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   970: ldc_w 3744
    //   973: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   976: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   979: ldc_w 1426
    //   982: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   985: ldc_w 3746
    //   988: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   991: aload_0
    //   992: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   995: ldc_w 3748
    //   998: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1001: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1004: ldc_w 1426
    //   1007: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1010: ldc_w 3750
    //   1013: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1016: aload_0
    //   1017: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1020: ldc_w 3752
    //   1023: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1026: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1029: ldc_w 3757
    //   1032: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1035: aload_1
    //   1036: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1039: ldc_w 922
    //   1042: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1045: invokevirtual 629	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1048: astore 7
    //   1050: aload_0
    //   1051: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1054: aload 7
    //   1056: invokevirtual 943	weaver/conn/RecordSet:executeSql	(Ljava/lang/String;)Z
    //   1059: pop
    //   1060: new 604	java/lang/StringBuilder
    //   1063: dup
    //   1064: ldc_w 3759
    //   1067: invokespecial 620	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   1070: aload_1
    //   1071: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1074: ldc_w 922
    //   1077: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1080: invokevirtual 629	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1083: astore 8
    //   1085: aconst_null
    //   1086: astore 9
    //   1088: new 1637	weaver/conn/ConnStatement
    //   1091: dup
    //   1092: invokespecial 1639	weaver/conn/ConnStatement:<init>	()V
    //   1095: astore 9
    //   1097: aload 9
    //   1099: aload 8
    //   1101: invokevirtual 1640	weaver/conn/ConnStatement:setStatementSql	(Ljava/lang/String;)V
    //   1104: aload 9
    //   1106: iconst_1
    //   1107: aload_0
    //   1108: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1111: ldc_w 2573
    //   1114: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1117: invokevirtual 1643	weaver/conn/ConnStatement:setString	(ILjava/lang/String;)V
    //   1120: aload 9
    //   1122: iconst_2
    //   1123: aload 6
    //   1125: invokevirtual 1643	weaver/conn/ConnStatement:setString	(ILjava/lang/String;)V
    //   1128: aload 9
    //   1130: invokevirtual 1647	weaver/conn/ConnStatement:executeUpdate	()I
    //   1133: pop
    //   1134: goto +39 -> 1173
    //   1137: astore 10
    //   1139: aload_0
    //   1140: aload 10
    //   1142: invokevirtual 1268	weaver/workflow/request/RequestManager:writeLog	(Ljava/lang/Object;)V
    //   1145: aload 9
    //   1147: ifnull +47 -> 1194
    //   1150: aload 9
    //   1152: invokevirtual 1650	weaver/conn/ConnStatement:close	()V
    //   1155: goto +39 -> 1194
    //   1158: astore 11
    //   1160: aload 9
    //   1162: ifnull +8 -> 1170
    //   1165: aload 9
    //   1167: invokevirtual 1650	weaver/conn/ConnStatement:close	()V
    //   1170: aload 11
    //   1172: athrow
    //   1173: aload 9
    //   1175: ifnull +19 -> 1194
    //   1178: aload 9
    //   1180: invokevirtual 1650	weaver/conn/ConnStatement:close	()V
    //   1183: goto +11 -> 1194
    //   1186: astore 8
    //   1188: aload_0
    //   1189: aload 8
    //   1191: invokevirtual 1268	weaver/workflow/request/RequestManager:writeLog	(Ljava/lang/Object;)V
    //   1194: ldc_w 284
    //   1197: aload_2
    //   1198: invokevirtual 685	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1201: ifne +1481 -> 2682
    //   1204: new 604	java/lang/StringBuilder
    //   1207: dup
    //   1208: ldc_w 3761
    //   1211: invokespecial 620	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   1214: aload 4
    //   1216: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1219: ldc_w 922
    //   1222: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1225: invokevirtual 629	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1228: astore 6
    //   1230: aload_0
    //   1231: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1234: aload 6
    //   1236: invokevirtual 943	weaver/conn/RecordSet:executeSql	(Ljava/lang/String;)Z
    //   1239: pop
    //   1240: aload_0
    //   1241: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1244: invokevirtual 653	weaver/conn/RecordSet:next	()Z
    //   1247: ifeq +1435 -> 2682
    //   1250: new 604	java/lang/StringBuilder
    //   1253: dup
    //   1254: ldc_w 3763
    //   1257: invokespecial 620	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   1260: aload_0
    //   1261: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1264: ldc_w 3044
    //   1267: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1270: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1273: ldc_w 3765
    //   1276: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1279: aload_0
    //   1280: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1283: ldc_w 3045
    //   1286: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1289: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1292: ldc_w 3765
    //   1295: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1298: aload_2
    //   1299: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1302: ldc_w 3767
    //   1305: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1308: ldc_w 922
    //   1311: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1314: aload_0
    //   1315: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1318: ldc_w 3695
    //   1321: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1324: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1327: ldc_w 3765
    //   1330: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1333: aload_0
    //   1334: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1337: ldc_w 3699
    //   1340: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1343: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1346: ldc_w 3765
    //   1349: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1352: aload_0
    //   1353: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1356: ldc_w 3769
    //   1359: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1362: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1365: ldc_w 3771
    //   1368: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1371: ldc_w 922
    //   1374: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1377: aload_0
    //   1378: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1381: ldc_w 3705
    //   1384: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1387: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1390: ldc_w 3765
    //   1393: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1396: aload_0
    //   1397: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1400: ldc_w 3773
    //   1403: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1406: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1409: ldc_w 3765
    //   1412: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1415: aload_0
    //   1416: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1419: ldc_w 3775
    //   1422: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1425: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1428: ldc_w 3765
    //   1431: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1434: aload_0
    //   1435: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1438: ldc_w 2573
    //   1441: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1444: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1447: ldc_w 1426
    //   1450: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1453: ldc_w 922
    //   1456: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1459: aload_0
    //   1460: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1463: ldc_w 3708
    //   1466: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1469: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1472: ldc_w 3765
    //   1475: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1478: aload_0
    //   1479: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1482: ldc_w 1272
    //   1485: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1488: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1491: ldc_w 3765
    //   1494: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1497: aload_0
    //   1498: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1501: ldc_w 3714
    //   1504: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1507: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1510: ldc_w 3765
    //   1513: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1516: aload_0
    //   1517: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1520: ldc_w 3718
    //   1523: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1526: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1529: ldc_w 1426
    //   1532: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1535: ldc_w 922
    //   1538: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1541: aload_0
    //   1542: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1545: ldc_w 3722
    //   1548: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1551: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1554: ldc_w 3765
    //   1557: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1560: aload_0
    //   1561: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1564: ldc_w 3726
    //   1567: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1570: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1573: ldc_w 3765
    //   1576: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1579: aload_0
    //   1580: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1583: ldc_w 3730
    //   1586: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1589: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1592: ldc_w 3765
    //   1595: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1598: aload_0
    //   1599: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1602: ldc_w 3734
    //   1605: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1608: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1611: ldc_w 1426
    //   1614: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1617: ldc_w 922
    //   1620: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1623: aload_0
    //   1624: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1627: ldc_w 3737
    //   1630: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1633: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1636: ldc_w 3765
    //   1639: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1642: aload_0
    //   1643: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1646: ldc_w 3740
    //   1649: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1652: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1655: ldc_w 3765
    //   1658: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1661: aload_0
    //   1662: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1665: ldc_w 3744
    //   1668: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1671: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1674: ldc_w 3765
    //   1677: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1680: aload_0
    //   1681: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1684: ldc_w 3748
    //   1687: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1690: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1693: ldc_w 3765
    //   1696: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1699: aload_0
    //   1700: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1703: ldc_w 3752
    //   1706: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1709: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1712: ldc_w 1777
    //   1715: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1718: invokevirtual 629	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1721: astore 7
    //   1723: ldc_w 683
    //   1726: aload_0
    //   1727: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1730: invokevirtual 680	weaver/conn/RecordSet:getDBType	()Ljava/lang/String;
    //   1733: invokevirtual 685	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1736: ifeq +457 -> 2193
    //   1739: new 604	java/lang/StringBuilder
    //   1742: dup
    //   1743: ldc_w 3777
    //   1746: invokespecial 620	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   1749: aload_0
    //   1750: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1753: ldc_w 3044
    //   1756: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1759: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1762: ldc_w 3765
    //   1765: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1768: aload_0
    //   1769: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1772: ldc_w 3045
    //   1775: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1778: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1781: ldc_w 3765
    //   1784: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1787: aload_2
    //   1788: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1791: ldc_w 3767
    //   1794: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1797: ldc_w 922
    //   1800: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1803: aload_0
    //   1804: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1807: ldc_w 3695
    //   1810: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1813: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1816: ldc_w 3765
    //   1819: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1822: aload_0
    //   1823: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1826: ldc_w 3699
    //   1829: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1832: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1835: ldc_w 3765
    //   1838: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1841: aload_0
    //   1842: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1845: ldc_w 3769
    //   1848: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1851: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1854: ldc_w 3771
    //   1857: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1860: ldc_w 922
    //   1863: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1866: aload_0
    //   1867: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1870: ldc_w 3705
    //   1873: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1876: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1879: ldc_w 3765
    //   1882: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1885: aload_0
    //   1886: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1889: ldc_w 3773
    //   1892: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1895: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1898: ldc_w 3765
    //   1901: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1904: aload_0
    //   1905: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1908: ldc_w 3775
    //   1911: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1914: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1917: ldc_w 3771
    //   1920: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1923: ldc_w 922
    //   1926: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1929: aload_0
    //   1930: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1933: ldc_w 3708
    //   1936: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1939: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1942: ldc_w 3765
    //   1945: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1948: aload_0
    //   1949: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1952: ldc_w 1272
    //   1955: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1958: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1961: ldc_w 3765
    //   1964: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1967: aload_0
    //   1968: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1971: ldc_w 3714
    //   1974: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1977: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1980: ldc_w 3765
    //   1983: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1986: aload_0
    //   1987: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   1990: ldc_w 3718
    //   1993: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   1996: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1999: ldc_w 1426
    //   2002: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2005: ldc_w 922
    //   2008: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2011: aload_0
    //   2012: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2015: ldc_w 3722
    //   2018: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2021: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2024: ldc_w 3765
    //   2027: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2030: aload_0
    //   2031: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2034: ldc_w 3726
    //   2037: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2040: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2043: ldc_w 3765
    //   2046: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2049: aload_0
    //   2050: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2053: ldc_w 3730
    //   2056: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2059: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2062: ldc_w 3765
    //   2065: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2068: aload_0
    //   2069: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2072: ldc_w 3734
    //   2075: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2078: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2081: ldc_w 1426
    //   2084: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2087: ldc_w 922
    //   2090: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2093: aload_0
    //   2094: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2097: ldc_w 3737
    //   2100: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2103: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2106: ldc_w 3765
    //   2109: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2112: aload_0
    //   2113: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2116: ldc_w 3740
    //   2119: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2122: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2125: ldc_w 3765
    //   2128: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2131: aload_0
    //   2132: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2135: ldc_w 3744
    //   2138: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2141: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2144: ldc_w 3765
    //   2147: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2150: aload_0
    //   2151: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2154: ldc_w 3748
    //   2157: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2160: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2163: ldc_w 3765
    //   2166: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2169: aload_0
    //   2170: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2173: ldc_w 3752
    //   2176: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2179: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2182: ldc_w 1777
    //   2185: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2188: invokevirtual 629	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   2191: astore 7
    //   2193: aload_0
    //   2194: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2197: ldc_w 3688
    //   2200: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2203: invokestatic 474	weaver/general/Util:null2String	(Ljava/lang/String;)Ljava/lang/String;
    //   2206: astore 8
    //   2208: aload_0
    //   2209: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2212: aload 7
    //   2214: invokevirtual 943	weaver/conn/RecordSet:executeSql	(Ljava/lang/String;)Z
    //   2217: pop
    //   2218: ldc_w 683
    //   2221: aload_0
    //   2222: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2225: invokevirtual 680	weaver/conn/RecordSet:getDBType	()Ljava/lang/String;
    //   2228: invokevirtual 685	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   2231: ifeq +234 -> 2465
    //   2234: new 604	java/lang/StringBuilder
    //   2237: dup
    //   2238: ldc_w 3779
    //   2241: invokespecial 620	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   2244: aload_0
    //   2245: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2248: ldc_w 3044
    //   2251: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2254: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2257: ldc_w 3781
    //   2260: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2263: aload_0
    //   2264: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2267: ldc_w 3045
    //   2270: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2273: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2276: invokevirtual 629	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   2279: astore 9
    //   2281: aload_0
    //   2282: getfield 639	weaver/workflow/request/RequestManager:rs1	Lweaver/conn/RecordSet;
    //   2285: aload 9
    //   2287: invokevirtual 943	weaver/conn/RecordSet:executeSql	(Ljava/lang/String;)Z
    //   2290: pop
    //   2291: aload_0
    //   2292: getfield 639	weaver/workflow/request/RequestManager:rs1	Lweaver/conn/RecordSet;
    //   2295: invokevirtual 653	weaver/conn/RecordSet:next	()Z
    //   2298: ifeq +384 -> 2682
    //   2301: aload_0
    //   2302: getfield 639	weaver/workflow/request/RequestManager:rs1	Lweaver/conn/RecordSet;
    //   2305: ldc_w 3783
    //   2308: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2311: invokestatic 474	weaver/general/Util:null2String	(Ljava/lang/String;)Ljava/lang/String;
    //   2314: astore 10
    //   2316: ldc_w 284
    //   2319: aload 10
    //   2321: invokevirtual 685	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   2324: ifne +358 -> 2682
    //   2327: new 604	java/lang/StringBuilder
    //   2330: dup
    //   2331: ldc_w 3759
    //   2334: invokespecial 620	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   2337: aload 10
    //   2339: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2342: ldc_w 922
    //   2345: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2348: invokevirtual 629	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   2351: astore 11
    //   2353: aconst_null
    //   2354: astore 12
    //   2356: new 1637	weaver/conn/ConnStatement
    //   2359: dup
    //   2360: invokespecial 1639	weaver/conn/ConnStatement:<init>	()V
    //   2363: astore 12
    //   2365: aload 12
    //   2367: aload 11
    //   2369: invokevirtual 1640	weaver/conn/ConnStatement:setStatementSql	(Ljava/lang/String;)V
    //   2372: aload 12
    //   2374: iconst_1
    //   2375: aload_0
    //   2376: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2379: ldc_w 2573
    //   2382: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2385: invokevirtual 1643	weaver/conn/ConnStatement:setString	(ILjava/lang/String;)V
    //   2388: aload 12
    //   2390: iconst_2
    //   2391: aload 8
    //   2393: invokevirtual 1643	weaver/conn/ConnStatement:setString	(ILjava/lang/String;)V
    //   2396: aload 12
    //   2398: invokevirtual 1647	weaver/conn/ConnStatement:executeUpdate	()I
    //   2401: pop
    //   2402: goto +39 -> 2441
    //   2405: astore 13
    //   2407: aload_0
    //   2408: aload 13
    //   2410: invokevirtual 1268	weaver/workflow/request/RequestManager:writeLog	(Ljava/lang/Object;)V
    //   2413: aload 12
    //   2415: ifnull +267 -> 2682
    //   2418: aload 12
    //   2420: invokevirtual 1650	weaver/conn/ConnStatement:close	()V
    //   2423: goto +259 -> 2682
    //   2426: astore 14
    //   2428: aload 12
    //   2430: ifnull +8 -> 2438
    //   2433: aload 12
    //   2435: invokevirtual 1650	weaver/conn/ConnStatement:close	()V
    //   2438: aload 14
    //   2440: athrow
    //   2441: aload 12
    //   2443: ifnull +239 -> 2682
    //   2446: aload 12
    //   2448: invokevirtual 1650	weaver/conn/ConnStatement:close	()V
    //   2451: goto +231 -> 2682
    //   2454: astore 9
    //   2456: aload_0
    //   2457: aload 9
    //   2459: invokevirtual 1268	weaver/workflow/request/RequestManager:writeLog	(Ljava/lang/Object;)V
    //   2462: goto +220 -> 2682
    //   2465: new 604	java/lang/StringBuilder
    //   2468: dup
    //   2469: ldc_w 3779
    //   2472: invokespecial 620	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   2475: aload_0
    //   2476: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2479: ldc_w 3044
    //   2482: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2485: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2488: ldc_w 3781
    //   2491: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2494: aload_0
    //   2495: getfield 637	weaver/workflow/request/RequestManager:rs	Lweaver/conn/RecordSet;
    //   2498: ldc_w 3045
    //   2501: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2504: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2507: invokevirtual 629	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   2510: astore 9
    //   2512: aload_0
    //   2513: getfield 639	weaver/workflow/request/RequestManager:rs1	Lweaver/conn/RecordSet;
    //   2516: aload 9
    //   2518: invokevirtual 943	weaver/conn/RecordSet:executeSql	(Ljava/lang/String;)Z
    //   2521: pop
    //   2522: aload_0
    //   2523: getfield 639	weaver/workflow/request/RequestManager:rs1	Lweaver/conn/RecordSet;
    //   2526: invokevirtual 653	weaver/conn/RecordSet:next	()Z
    //   2529: ifeq +153 -> 2682
    //   2532: aload_0
    //   2533: getfield 639	weaver/workflow/request/RequestManager:rs1	Lweaver/conn/RecordSet;
    //   2536: ldc_w 3783
    //   2539: invokevirtual 659	weaver/conn/RecordSet:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   2542: invokestatic 474	weaver/general/Util:null2String	(Ljava/lang/String;)Ljava/lang/String;
    //   2545: astore 10
    //   2547: ldc_w 284
    //   2550: aload 10
    //   2552: invokevirtual 685	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   2555: ifne +127 -> 2682
    //   2558: new 604	java/lang/StringBuilder
    //   2561: dup
    //   2562: ldc_w 3785
    //   2565: invokespecial 620	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
    //   2568: aload 10
    //   2570: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2573: ldc_w 922
    //   2576: invokevirtual 625	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2579: invokevirtual 629	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   2582: astore 11
    //   2584: aconst_null
    //   2585: astore 12
    //   2587: new 1637	weaver/conn/ConnStatement
    //   2590: dup
    //   2591: invokespecial 1639	weaver/conn/ConnStatement:<init>	()V
    //   2594: astore 12
    //   2596: aload 12
    //   2598: aload 11
    //   2600: invokevirtual 1640	weaver/conn/ConnStatement:setStatementSql	(Ljava/lang/String;)V
    //   2603: aload 12
    //   2605: iconst_1
    //   2606: aload 8
    //   2608: invokevirtual 1643	weaver/conn/ConnStatement:setString	(ILjava/lang/String;)V
    //   2611: aload 12
    //   2613: invokevirtual 1647	weaver/conn/ConnStatement:executeUpdate	()I
    //   2616: pop
    //   2617: goto +39 -> 2656
    //   2620: astore 13
    //   2622: aload_0
    //   2623: aload 13
    //   2625: invokevirtual 1268	weaver/workflow/request/RequestManager:writeLog	(Ljava/lang/Object;)V
    //   2628: aload 12
    //   2630: ifnull +52 -> 2682
    //   2633: aload 12
    //   2635: invokevirtual 1650	weaver/conn/ConnStatement:close	()V
    //   2638: goto +44 -> 2682
    //   2641: astore 14
    //   2643: aload 12
    //   2645: ifnull +8 -> 2653
    //   2648: aload 12
    //   2650: invokevirtual 1650	weaver/conn/ConnStatement:close	()V
    //   2653: aload 14
    //   2655: athrow
    //   2656: aload 12
    //   2658: ifnull +24 -> 2682
    //   2661: aload 12
    //   2663: invokevirtual 1650	weaver/conn/ConnStatement:close	()V
    //   2666: goto +16 -> 2682
    //   2669: astore 9
    //   2671: aload_0
    //   2672: aload 9
    //   2674: invokevirtual 1268	weaver/workflow/request/RequestManager:writeLog	(Ljava/lang/Object;)V
    //   2677: goto +5 -> 2682
    //   2680: astore 7
    //   2682: return
    //
    // Exception table:
    //   from	to	target	type
    //   551	597	600	java/lang/Exception
    //   551	608	621	finally
    //   523	646	649	java/lang/Exception
    //   1088	1134	1137	java/lang/Exception
    //   1088	1145	1158	finally
    //   1060	1183	1186	java/lang/Exception
    //   2356	2402	2405	java/lang/Exception
    //   2356	2413	2426	finally
    //   2234	2451	2454	java/lang/Exception
    //   2587	2617	2620	java/lang/Exception
    //   2587	2628	2641	finally
    //   2465	2666	2669	java/lang/Exception
    //   1250	2677	2680	java/lang/Exception } 
  private boolean doOutWork(int workflowid, int requestid) { String outWorkFileName = "requestmapping";
    String className = "";
    RequestOutWork work = null;
    try {
      String hasOutWork = getPropValue(GCONST.getConfigFile(), "hasOutWork");
      if ((hasOutWork != null) && (hasOutWork.equals("true"))) {
        className = getPropValue(outWorkFileName, workflowid);
        if (className != null) {
          Class cl = Class.forName(className);

          work = (RequestOutWork)cl.newInstance();
          HashMap dataInfo = new HashMap();
          dataInfo.put("workflowid", this.workflowid);
          dataInfo.put("userid", this.userid);
          dataInfo.put("usertype", this.usertype);
          dataInfo.put("src", this.src);
          dataInfo.put("iscreate", this.iscreate);
          dataInfo.put("requestlevel", this.requestlevel);
          dataInfo.put("requestmark", this.requestmark);
          dataInfo.put("nextnodetype", getNextNodetype());
          return work.execute(requestid, dataInfo);
        }
      }
    }
    catch (ClassNotFoundException e)
    {
      writeLog("get class instance error!");
    } catch (InstantiationException e) {
      writeLog("get class instance error!");
    } catch (IllegalAccessException e) {
      writeLog("get class instance error!");
    } catch (RequestOutWorkException e) {
      writeLog(e.getMessage());
    }
    return true;
  }

  public boolean saveRequestLog()
  {
    return this.islogsuccess;
  }

  public boolean saveRequestLog2()
  {
    if (this.src.equals("save")) {
      if ((this.isremark == 1) || (this.isremark == 9) || ((this.isremark == 7) && ("submit".equals(this.tempsrc))))
        saveRequestLog("9");
      else
        saveRequestLog("1");
    } else if (this.src.equals("submit")) {
      FlowExceptionHandle flowExceptionHandle = new FlowExceptionHandle();
      ArrayList _alluserids = new ArrayList();
      for (int n = 0; n < this.nextnodeids.size(); ++n) {
        this.nextnodeid = Util.getIntValue((String)this.nextnodeids.get(n), 0);
        this.nextnodeattr = Util.getIntValue((String)this.nextnodeattrs.get(n), 0);
        try {
          Hashtable _operatorsht = (Hashtable)this.operatorshts.get(n);
          Enumeration tempKeys = this.operatorsht.keys();
          while (tempKeys.hasMoreElements()) {
            String tempKey = (String)tempKeys.nextElement();
            ArrayList tempoperators = (ArrayList)this.operatorsht.get(tempKey);
            _alluserids.addAll(tempoperators);
          }
        }
        catch (Exception localException)
        {
        }
        if ((this.isremark == 7) && (this.coadsigntype.equals("2"))) {
          saveRequestLog("9");
        }
        else if (this.nodetype.equals("1"))
          saveRequestLog("0");
        else {
          saveRequestLog("2");
        }

        int requestexceptiontype = 0;
        if (this.requestexceptiontypes.size() > n)
          requestexceptiontype = Util.getIntValue((String)this.requestexceptiontypes.get(n));
        flowExceptionHandle.saveRequestExceptionFlowInfo(this.requestid, this.nodeid, this.nextnodeid, requestexceptiontype, this.eh_operatorMap);
      }
    }
    else
    {
      int n;
      if (this.src.equals("reject"))
      {
        ArrayList _alluserids = new ArrayList();
        for (n = 0; n < this.nextnodeids.size(); ++n) {
          this.nextnodeid = Util.getIntValue((String)this.nextnodeids.get(n), 0);
          this.nextnodeattr = Util.getIntValue((String)this.nextnodeattrs.get(n), 0);
          try {
            Hashtable _operatorsht = (Hashtable)this.operatorshts.get(n);
            Enumeration tempKeys = this.operatorsht.keys();
            while (tempKeys.hasMoreElements()) {
              String tempKey = (String)tempKeys.nextElement();
              ArrayList tempoperators = (ArrayList)this.operatorsht.get(tempKey);
              _alluserids.addAll(tempoperators);
            }
          }
          catch (Exception localException1) {
          }
          saveRequestLog("3");
        }
      } else if (this.src.equals("reopen")) {
        saveRequestLog("4");
      } else if (this.src.equals("delete")) {
        saveRequestLog("5");
      } else if (this.src.equals("active")) {
        saveRequestLog("6");
      } else if (this.src.equals("supervise")) {
        ArrayList nownodelist = Util.TokenizerString(this.wflinkinfo.getNowNodeids(this.requestid), ",");
        for (n = 0; n < nownodelist.size(); ++n) {
          int nownodeid = Util.getIntValue((String)nownodelist.get(n), 0);
          if (nownodeid > 0)
            saveRequestLog("s", nownodeid, nownodeid);
        }
      } else if (this.src.equals("intervenor")) {
        saveRequestLog("i");
      }
    }
    return true;
  }

  private String remarkBeforeSaveHandle(String remarkstr)
  {
    String result = "";
    StringBuffer rksb = new StringBuffer();
    Pattern ptrn = Pattern.compile("requestid=\\{#\\[currentRequestid\\]#\\}");
    result = ptrn.matcher(remarkstr).replaceAll("requestid=" + this.requestid);

    return result;
  }

  private void saveRequestLog(String logtype)
  {
    if (("true".equals(this.isFromEditDocument)) && ("".equals(this.remark)) && ("".equals(this.signdocids)) && ("".equals(this.signworkflowids))) {
      return;
    }

    if ("1".equals(this.iscreate)) {
      this.remark = remarkBeforeSaveHandle(this.remark);
    }

    String ismode = "";
    this.rs.executeSql("select ismode from workflow_flownode where workflowid=" + this.workflowid + " and nodeid=" + this.nodeid);
    if (this.rs.next()) {
      ismode = Util.null2String(this.rs.getString("ismode"));
    }

    int currentid = 0;
    this.rs.executeSql("select id from workflow_currentoperator where requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + " and islasttimes=1 ");
    if (this.rs.next()) {
      currentid = Util.getIntValue(this.rs.getString("id"), 0);
    }
    String annexdocids = "";

    if (WorkflowSpeechAppend.isFromMobile(this.clientType)) {
      annexdocids = this.signatureAppendfix;
    }
    else if ((!(ismode.equals("1"))) || (this.src.equals("supervise")) || (this.src.equals("intervenor"))) {
      RequestAnnexUpload rau = new RequestAnnexUpload();
      rau.setRequest(this.fu);
      rau.setUser(this.user);
      annexdocids = rau.AnnexUpload();
    } else {
      String hasSign = "0";
      this.rs.executeSql("select * from workflow_modeview where formid=" + this.formid + " and nodeid=" + this.nodeid + " and fieldid=-4");
      if (this.rs.next())
        hasSign = "1";
      if ("1".equals(hasSign)) {
        if (this.isRequest) {
          if (this.request != null)
            annexdocids = Util.null2String(this.request.getParameter("qianzi"));
        }
        else if (this.fu != null)
          annexdocids = Util.null2String(this.fu.getParameter("qianzi"));
      }
      else {
        RequestAnnexUpload rau = new RequestAnnexUpload();
        rau.setRequest(this.fu);
        rau.setUser(this.user);
        annexdocids = rau.AnnexUpload();
      }

    }

    if (this.isRequest) {
      if (this.request != null) {
        this.signdocids = Util.null2String(this.request.getParameter("signdocids"));
        this.signworkflowids = Util.null2String(this.request.getParameter("signworkflowids"));
      }
    }
    else if (this.fu != null) {
      this.signdocids = Util.null2String(this.fu.getParameter("signdocids"));
      this.signworkflowids = Util.null2String(this.fu.getParameter("signworkflowids"));
    }

    String clientip = "";
    if (this.isRequest) {
      if (this.request != null)
        clientip = Util.null2String(this.request.getRemoteAddr());
    }
    else if (this.fu != null) {
      clientip = Util.null2String(this.fu.getRemoteAddr());
    }

    int requestLogId = 0;
    if (this.isRequest) {
      if (this.request != null)
        requestLogId = Util.getIntValue(this.request.getParameter("workflowRequestLogId"), 0);
    }
    else if (this.fu != null) {
      requestLogId = Util.getIntValue(this.fu.getParameter("workflowRequestLogId"), 0);
    }

    if (this.nextnodeid == 0)
      this.nextnodeid = this.nodeid;
    String personStr = "";
    String receivedPersonids = "";
    String personStr1 = "";
    String receivedPersonids1 = "";
    String isremarkStr_t = "";
    if (("0".equals(logtype)) || ("2".equals(logtype)) || ("3".equals(logtype))) {
      isremarkStr_t = " and isremark='2' ";
    }

    if (isOldOrNewFlag(this.requestid)) {
      this.rs.executeSql("select userid,usertype from workflow_currentoperator where isremark = 0 and requestid = " + this.requestid);
      while (this.rs.next()) {
        if ("0".equals(this.rs.getString("usertype"))) {
          personStr = personStr + Util.toScreen(this.resourceComInfo.getResourcename(this.rs.getString("userid")), this.user.getLanguage()) + ",";
          receivedPersonids = receivedPersonids + Util.null2String(this.rs.getString("userid")) + ",";
        } else {
          personStr = personStr + Util.toScreen(this.customerInfoComInfo.getCustomerInfoname(this.rs.getString("userid")), this.user.getLanguage()) + ",";
          receivedPersonids = receivedPersonids + Util.null2String(this.rs.getString("userid")) + ",";
        }
      }

      this.Procpara = this.requestid + this.flag + this.workflowid + this.flag + this.nodeid + this.flag + logtype + this.flag + this.logdate + this.flag + this.logtime + this.flag + this.userid + this.flag + clientip + this.flag + this.usertype + this.flag + this.nextnodeid + this.flag + personStr.trim() + this.flag + -1 + this.flag + "0" + this.flag + -1 + this.flag + annexdocids + this.flag + requestLogId + this.flag + this.signdocids + this.flag + this.signworkflowids + this.flag + this.clientType + this.flag + this.speechAttachment + this.flag + this.handWrittenSign + this.flag + receivedPersonids + this.flag + this.remarkLocation;

      if (this.logdate.equals("")) {
        String currentString = execRequestlog(this.Procpara, this.rs, this.flag, this.remark);
        if ((!("".equals(currentString))) && (currentString.indexOf("~~current~~") > -1)) {
          String[] arraycurrent = Util.TokenizerString2(currentString, "~~current~~");
          this.logdate = arraycurrent[0];
          this.logtime = arraycurrent[1];
        }

      }
      else
      {
        execRequestlog(this.Procpara, this.rs, this.flag, this.remark);
      }
    } else {
      String tempPersonStr;
      String currentString;
      String[] arraycurrent;
      String tempSQL = "";
      int agentorbyagentid = -1;
      int agenttype = 0;
      int showorder = 1;

      ResourceComInfo resourceComInfo = null;
      try {
        resourceComInfo = new ResourceComInfo();
      }
      catch (Exception localException) {
      }
      String tempisremark = "isremark in ('0','4','f')";

      this.rs.executeSql("select signorder,signtype from workflow_groupdetail where type=42 and groupid in (select id from workflow_nodegroup where nodeid=" + this.nodeid + ")");
      if (this.rs.next()) {
        int signorder = this.rs.getInt("signorder");
        int signtype = this.rs.getInt("signtype");
        if ((signtype != 2) && (((signorder == 0) || (signorder == 1) || (signorder == 2) || ((signorder == 0) && (this.src.equals("reject")))))) {
          tempisremark = "isremark in ('0','4','7','f')";
        }
      }
      this.rs.executeSql("select userid,usertype,agentorbyagentid, agenttype,isremark,nodeid from workflow_currentoperator where " + tempisremark + " and requestid = " + this.requestid + " and nodeid in(" + this.wflinkinfo.getBrancheNode(this.nextnodeid, this.workflowid, new StringBuilder().append(this.nextnodeid).toString(), this.requestid) + ") order by showorder asc");
      String useridStr = "";
      while (this.rs.next())
      {
        String tempUseridStr;
        if (("submit".equals(this.src)) && ("f".equals(this.rs.getString("isremark"))) && (this.nodeid == this.rs.getInt("nodeid")) && (this.user.getUID() == this.rs.getInt("userid"))) {
          continue;
        }
        if ("0".equals(this.rs.getString("usertype"))) {
          if (this.rs.getInt("agenttype") == 0) {
            tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(this.rs.getString("userid")), this.user.getLanguage());
            tempUseridStr = Util.null2String(this.rs.getString("userid"));
            if (((personStr.indexOf("," + tempPersonStr + ",") == -1) && (personStr.indexOf(tempPersonStr + ",") == -1)) || ((useridStr.indexOf("," + tempUseridStr + ",") == -1) && (useridStr.indexOf(tempUseridStr + ",") == -1))) {
              personStr = personStr + tempPersonStr + ",";
              useridStr = useridStr + tempUseridStr + ",";
              receivedPersonids = receivedPersonids + tempUseridStr + ",";
            }
          } else if (this.rs.getInt("agenttype") == 2) {
            tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(this.rs.getString("agentorbyagentid")), this.user.getLanguage()) + "->" + Util.toScreen(resourceComInfo.getResourcename(this.rs.getString("userid")), this.user.getLanguage());
            tempUseridStr = Util.null2String(this.rs.getString("agentorbyagentid"));
            if (((personStr.indexOf("," + tempPersonStr + ",") == -1) && (personStr.indexOf(tempPersonStr + ",") == -1)) || ((useridStr.indexOf("," + tempUseridStr + ",") == -1) && (useridStr.indexOf(tempUseridStr + ",") == -1))) {
              personStr = personStr + tempPersonStr + ",";
              useridStr = useridStr + tempUseridStr + ",";
              receivedPersonids = receivedPersonids + tempUseridStr + ",";
            }
          }
        }
        else {
          tempPersonStr = Util.toScreen(this.customerInfoComInfo.getCustomerInfoname(this.rs.getString("userid")), this.user.getLanguage());
          tempUseridStr = Util.null2String(this.rs.getString("userid"));
          if (((personStr.indexOf("," + tempPersonStr + ",") == -1) && (personStr.indexOf(tempPersonStr + ",") == -1)) || ((useridStr.indexOf("," + tempUseridStr + ",") == -1) && (useridStr.indexOf(tempUseridStr + ",") == -1))) {
            personStr = personStr + tempPersonStr + ",";
            useridStr = useridStr + tempUseridStr + ",";
            receivedPersonids = receivedPersonids + tempUseridStr + ",";
          }
        }
      }

      if ((((logtype.equals("0")) || (logtype.equals("2")))) && (this.showcoadjutant))
      {
        this.sql = "select userid,usertype,agentorbyagentid, agenttype from workflow_currentoperator where isremark ='7' and nodeid in(" + this.wflinkinfo.getBrancheNode(this.nextnodeid, this.workflowid, new StringBuilder().append(this.nextnodeid).toString(), this.requestid) + ") and requestid = " + this.requestid;
        RequestManager tmp2747_2746 = this; tmp2747_2746.sql = tmp2747_2746.sql + " and id>=" + this.currentopratorInsFirstid;
        this.sql += " order by id desc";
        this.rs.executeSql(this.sql);

        while (this.rs.next()) {
          if ("0".equals(this.rs.getString("usertype"))) {
            if (this.rs.getInt("agenttype") == 0) {
              tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(this.rs.getString("userid")), this.user.getLanguage());
              if ((personStr1.indexOf("," + tempPersonStr + ",") == -1) && (personStr1.indexOf(tempPersonStr + ",") == -1)) {
                personStr1 = personStr1 + tempPersonStr + ",";
                receivedPersonids1 = receivedPersonids1 + Util.null2String(this.rs.getString("userid")) + ",";
              }
            } else if (this.rs.getInt("agenttype") == 2) {
              tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(this.rs.getString("agentorbyagentid")), this.user.getLanguage()) + "->" + Util.toScreen(resourceComInfo.getResourcename(this.rs.getString("userid")), this.user.getLanguage());
              if ((personStr1.indexOf("," + tempPersonStr + ",") == -1) && (personStr1.indexOf(tempPersonStr + ",") == -1)) {
                personStr1 = personStr1 + tempPersonStr + ",";
                receivedPersonids1 = receivedPersonids1 + Util.null2String(this.rs.getString("userid")) + ",";
              }
            }
          }
          else {
            tempPersonStr = Util.toScreen(this.customerInfoComInfo.getCustomerInfoname(this.rs.getString("userid")), this.user.getLanguage());
            if ((personStr1.indexOf("," + tempPersonStr + ",") == -1) && (personStr1.indexOf(tempPersonStr + ",") == -1)) {
              personStr1 = personStr1 + tempPersonStr + ",";
              receivedPersonids1 = receivedPersonids1 + Util.null2String(this.rs.getString("userid")) + ",";
            }
          }

        }

        if ((this.operator89mp != null) && (!(this.operator89mp.isEmpty()))) {
          this.operator89List = ((ArrayList)this.operator89mp.get(String.valueOf(this.nextnodeid)));
          this.operatortype89List = ((ArrayList)this.operatortype89mp.get(String.valueOf(this.nextnodeid)));
          this.agentoperator89List = ((ArrayList)this.agentoperator89mp.get(String.valueOf(this.nextnodeid)));

          if (this.operator89List == null) this.operator89List = new ArrayList();
          if (this.operatortype89List == null) this.operatortype89List = new ArrayList();
          if (this.agentoperator89List == null) this.agentoperator89List = new ArrayList();
        }

        for (int cc = 0; cc < this.operator89List.size(); ++cc) {
          String tempPersonStr;
          int operatortype_tmp = Util.getIntValue((String)this.operatortype89List.get(cc), 0);
          int operator_tmp = Util.getIntValue((String)this.operator89List.get(cc), 0);
          int agentoperator_tmp = Util.getIntValue((String)this.agentoperator89List.get(cc), 0);
          if (operatortype_tmp == 0) {
            if (agentoperator_tmp == 0) {
              tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(operator_tmp), this.user.getLanguage());
              if ((personStr1.indexOf("," + tempPersonStr + ",") == -1) && (personStr1.indexOf(tempPersonStr + ",") == -1)) {
                personStr1 = personStr1 + tempPersonStr + ",";
                receivedPersonids1 = receivedPersonids1 + Util.null2String(this.operator89List.get(cc)) + ",";
              }
            } else if (agentoperator_tmp > 0) {
              tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(new StringBuilder().append(operator_tmp).toString()), this.user.getLanguage()) + "->" + Util.toScreen(resourceComInfo.getResourcename(new StringBuilder().append(agentoperator_tmp).toString()), this.user.getLanguage());
              if ((personStr1.indexOf("," + tempPersonStr + ",") == -1) && (personStr1.indexOf(tempPersonStr + ",") == -1)) {
                personStr1 = personStr1 + tempPersonStr + ",";
                receivedPersonids1 = receivedPersonids1 + Util.null2String(this.operator89List.get(cc)) + ",";
              }
            }
          }
          else {
            tempPersonStr = Util.toScreen(this.customerInfoComInfo.getCustomerInfoname(operator_tmp), this.user.getLanguage());
            if ((personStr1.indexOf("," + tempPersonStr + ",") == -1) && (personStr1.indexOf(tempPersonStr + ",") == -1)) {
              personStr1 = personStr1 + tempPersonStr + ",";
              receivedPersonids1 = receivedPersonids1 + Util.null2String(this.operator89List.get(cc)) + ",";
            }
          }
        }
      }
      if ((!(this.canflowtonextnode)) && (((this.nextnodeattr == 3) || (this.nextnodeattr == 4) || (this.nextnodeattr == 5)))) {
        personStr = SystemEnv.getHtmlLabelName(21399, this.user.getLanguage()) + ",";
      }
      tempSQL = "select agentorbyagentid, agenttype, showorder from workflow_currentoperator where nodeid = " + this.nodeid + " and requestid = " + this.requestid + " and userid = " + this.userid + isremarkStr_t + " order by operatedate desc,operatetime desc, id desc";
      this.rs.executeSql(tempSQL);
      if (this.rs.next()) {
        agentorbyagentid = this.rs.getInt("agentorbyagentid");
        agenttype = this.rs.getInt("agenttype");
        showorder = this.rs.getInt("showorder");
      }
      if ((logtype.equals("9")) || (logtype.equals("7")))
      {
        this.rs.executeSql("select max(showorder) as maxshow from workflow_requestlog where requestid = " + this.requestid + " and nodeid = " + this.nodeid + " and (logtype = '9' or logtype = '7')");
        this.rs.next();
        if (this.rs.getInt("maxshow") != -1)
          showorder = this.rs.getInt("maxshow") + 1;
        else {
          showorder = 10000;
        }
        this.rs.executeSql("select receivedpersons,receivedpersonids from workflow_requestlog where requestid = " + this.requestid + " and nodeid = " + this.nodeid + " and (logtype = '2' or logtype = '0' or logtype = '3') order by operatedate desc,operatetime desc");
        if (this.rs.next()) {
          personStr = Util.null2String(this.rs.getString(1));
          receivedPersonids = Util.null2String(this.rs.getString(2));
        }
      }

      this.Procpara = this.requestid + this.flag + this.workflowid + this.flag + this.nodeid + this.flag + logtype + this.flag + this.logdate + this.flag + this.logtime + this.flag + this.userid + this.flag + clientip + this.flag + this.usertype + this.flag + this.nextnodeid + this.flag + personStr.trim() + this.flag + agentorbyagentid + this.flag + agenttype + this.flag + showorder + this.flag + annexdocids + this.flag + requestLogId + this.flag + this.signdocids + this.flag + this.signworkflowids + this.flag + this.clientType + this.flag + this.speechAttachment + this.flag + this.handWrittenSign + this.flag + receivedPersonids + this.flag + this.remarkLocation;

      if (this.logdate.equals(""))
      {
        currentString = execRequestlog(this.Procpara, this.rs, this.flag, this.remark);
        if ((!("".equals(currentString))) && (currentString.indexOf("~~current~~") > -1)) {
          arraycurrent = Util.TokenizerString2(currentString, "~~current~~");
          this.logdate = arraycurrent[0];
          this.logtime = arraycurrent[1];
        }
      }
      else {
        execRequestlog(this.Procpara, this.rs, this.flag, this.remark);
      }

      if (!(personStr1.trim().equals("")))
      {
        this.Procpara = this.requestid + this.flag + this.workflowid + this.flag + this.nodeid + this.flag + "t" + this.flag + this.logdate + this.flag + this.logtime + this.flag + this.userid + this.flag + clientip + this.flag + this.usertype + this.flag + this.nextnodeid + this.flag + personStr1.trim() + this.flag + agentorbyagentid + this.flag + agenttype + this.flag + showorder + this.flag + this.flag + requestLogId + this.flag + this.flag + this.flag + this.clientType + this.flag + this.speechAttachment + this.flag + this.handWrittenSign + this.flag + receivedPersonids1 + this.flag + this.remarkLocation;
        if (this.logdate.equals(""))
        {
          currentString = execRequestlog(this.Procpara, this.rs, this.flag, "");
          if ((!("".equals(currentString))) && (currentString.indexOf("~~current~~") > -1)) {
            arraycurrent = Util.TokenizerString2(currentString, "~~current~~");
            this.logdate = arraycurrent[0];
            this.logtime = arraycurrent[1];
          }
        }
        else {
          execRequestlog(this.Procpara, this.rs, this.flag, "");
        }
      }
    }

    RequestSignRelevanceWithMe reqsignwm = new RequestSignRelevanceWithMe();
    reqsignwm.inertRelevanceInfo(this.workflowid, this.requestid, this.nodeid, logtype, this.logdate, this.logtime, this.userid, this.remark);
  }

  private void saveRequestLog(String logtype, int nownodeid, int nownextnodeid)
  {
    String ismode = "";
    this.rs.executeSql("select ismode from workflow_flownode where workflowid=" + this.workflowid + " and nodeid=" + nownodeid);
    if (this.rs.next()) {
      ismode = Util.null2String(this.rs.getString("ismode"));
    }

    String annexdocids = "";

    if (WorkflowSpeechAppend.isFromMobile(this.clientType)) {
      annexdocids = this.signatureAppendfix;
    }
    else if ((!(ismode.equals("1"))) || (this.src.equals("supervise")) || (this.src.equals("intervenor"))) {
      RequestAnnexUpload rau = new RequestAnnexUpload();
      rau.setRequest(this.fu);
      rau.setUser(this.user);
      annexdocids = rau.AnnexUpload();
    }
    else if (this.isRequest) {
      if (this.request != null)
        annexdocids = Util.null2String(this.request.getParameter("qianzi"));
    }
    else if (this.fu != null) {
      annexdocids = Util.null2String(this.fu.getParameter("qianzi"));
    }

    if (this.isRequest) {
      if (this.request != null) {
        this.signdocids = Util.null2String(this.request.getParameter("signdocids"));
        this.signworkflowids = Util.null2String(this.request.getParameter("signworkflowids"));
      }
    }
    else if (this.fu != null) {
      this.signdocids = Util.null2String(this.fu.getParameter("signdocids"));
      this.signworkflowids = Util.null2String(this.fu.getParameter("signworkflowids"));
    }

    String clientip = "";
    if (this.isRequest) {
      if (this.request != null)
        clientip = Util.null2String(this.request.getRemoteAddr());
    }
    else if (this.fu != null) {
      clientip = Util.null2String(this.fu.getRemoteAddr());
    }

    int requestLogId = 0;
    if (this.isRequest) {
      if (this.request != null)
        requestLogId = Util.getIntValue(this.request.getParameter("workflowRequestLogId"), 0);
    }
    else if (this.fu != null) {
      requestLogId = Util.getIntValue(this.fu.getParameter("workflowRequestLogId"), 0);
    }

    if (nownextnodeid == 0)
      nownextnodeid = nownodeid;
    String personStr = "";
    String personStr1 = "";
    String receivedPersonids = "";
    String receivedPersonids1 = "";
    String isremarkStr_t = "";
    if (("0".equals(logtype)) || ("2".equals(logtype)) || ("3".equals(logtype)) || ("h".equals(logtype))) {
      isremarkStr_t = " and isremark='2' ";
    }

    if (isOldOrNewFlag(this.requestid)) {
      this.rs.executeSql("select userid,usertype from workflow_currentoperator where isremark = 0 and requestid = " + this.requestid);
      while (this.rs.next()) {
        if ("0".equals(this.rs.getString("usertype"))) {
          personStr = personStr + Util.toScreen(this.resourceComInfo.getResourcename(this.rs.getString("userid")), this.user.getLanguage()) + ",";
          receivedPersonids = receivedPersonids + Util.null2String(this.rs.getString("userid")) + ",";
        } else {
          personStr = personStr + Util.toScreen(this.customerInfoComInfo.getCustomerInfoname(this.rs.getString("userid")), this.user.getLanguage()) + ",";
          receivedPersonids = receivedPersonids + Util.null2String(this.rs.getString("userid")) + ",";
        }
      }

      this.Procpara = this.requestid + this.flag + this.workflowid + this.flag + nownodeid + this.flag + logtype + this.flag + this.logdate + this.flag + this.logtime + this.flag + this.userid + this.flag + clientip + this.flag + this.usertype + this.flag + nownextnodeid + this.flag + personStr.trim() + this.flag + -1 + this.flag + "0" + this.flag + -1 + this.flag + annexdocids + this.flag + requestLogId + this.flag + this.signdocids + this.flag + this.signworkflowids + this.flag + this.clientType + this.flag + this.speechAttachment + this.flag + this.handWrittenSign + this.flag + receivedPersonids + this.flag + this.remarkLocation;

      if (this.logdate.equals("")) {
        String currentString = execRequestlog(this.Procpara, this.rs, this.flag, this.remark);
        if ((!("".equals(currentString))) && (currentString.indexOf("~~current~~") > -1)) {
          String[] arraycurrent = Util.TokenizerString2(currentString, "~~current~~");
          this.logdate = arraycurrent[0];
          this.logtime = arraycurrent[1];
        }

      }
      else
      {
        execRequestlog(this.Procpara, this.rs, this.flag, this.remark);
      }
    } else {
      String tempPersonStr;
      String currentString;
      String[] arraycurrent;
      String tempSQL = "";
      int agentorbyagentid = -1;
      int agenttype = 0;
      int showorder = 1;

      ResourceComInfo resourceComInfo = null;
      try {
        resourceComInfo = new ResourceComInfo();
      }
      catch (Exception localException) {
      }
      this.rs.executeSql("select userid,usertype,agentorbyagentid, agenttype from workflow_currentoperator where isremark in ('0','4') and requestid = " + this.requestid + " and nodeid in(" + this.wflinkinfo.getBrancheNode(nownextnodeid, this.workflowid, new StringBuilder().append(nownextnodeid).toString(), this.requestid) + ") order by showorder asc");
      String _useridStr = "";
      while (this.rs.next())
      {
        String tempUseridStr;
        if ("0".equals(this.rs.getString("usertype"))) {
          if (this.rs.getInt("agenttype") == 0) {
            tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(this.rs.getString("userid")), this.user.getLanguage());
            tempUseridStr = Util.null2String(this.rs.getString("userid"));
            if (((personStr.indexOf("," + tempPersonStr + ",") == -1) && (personStr.indexOf(tempPersonStr + ",") == -1)) || ((_useridStr.indexOf("," + tempUseridStr + ",") == -1) && (_useridStr.indexOf(tempUseridStr + ",") == -1))) {
              personStr = personStr + tempPersonStr + ",";
              _useridStr = _useridStr + tempUseridStr + ",";
              receivedPersonids = receivedPersonids + tempUseridStr + ",";
            }
          } else if (this.rs.getInt("agenttype") == 2) {
            tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(this.rs.getString("agentorbyagentid")), this.user.getLanguage()) + "->" + Util.toScreen(resourceComInfo.getResourcename(this.rs.getString("userid")), this.user.getLanguage());
            tempUseridStr = Util.null2String(this.rs.getString("agentorbyagentid"));
            if (((personStr.indexOf("," + tempPersonStr + ",") == -1) && (personStr.indexOf(tempPersonStr + ",") == -1)) || ((_useridStr.indexOf("," + tempUseridStr + ",") == -1) && (_useridStr.indexOf(tempUseridStr + ",") == -1))) {
              personStr = personStr + tempPersonStr + ",";
              _useridStr = _useridStr + tempUseridStr + ",";
              receivedPersonids = receivedPersonids + tempUseridStr + ",";
            }
          }
        }
        else {
          tempPersonStr = Util.toScreen(this.customerInfoComInfo.getCustomerInfoname(this.rs.getString("userid")), this.user.getLanguage());
          tempUseridStr = Util.null2String(this.rs.getString("userid"));
          if (((personStr.indexOf("," + tempPersonStr + ",") == -1) && (personStr.indexOf(tempPersonStr + ",") == -1)) || ((_useridStr.indexOf("," + tempUseridStr + ",") == -1) && (_useridStr.indexOf(tempUseridStr + ",") == -1))) {
            personStr = personStr + tempPersonStr + ",";
            _useridStr = _useridStr + tempUseridStr + ",";
            receivedPersonids = receivedPersonids + tempUseridStr + ",";
          }
        }
      }

      if ((logtype.equals("0")) || (logtype.equals("2")))
      {
        this.sql = "select userid,usertype,agentorbyagentid, agenttype from workflow_currentoperator where isremark ='7' and nodeid = " + nownextnodeid + " and requestid = " + this.requestid;
        RequestManager tmp2215_2214 = this; tmp2215_2214.sql = tmp2215_2214.sql + " and id>=" + this.currentopratorInsFirstid;
        this.sql += " order by id desc";
        this.rs.executeSql(this.sql);

        if (this.rs.next()) {
          if ("0".equals(this.rs.getString("usertype"))) {
            if (this.rs.getInt("agenttype") == 0) {
              tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(this.rs.getString("userid")), this.user.getLanguage());
              if ((personStr1.indexOf("," + tempPersonStr + ",") == -1) && (personStr1.indexOf(tempPersonStr + ",") == -1)) {
                personStr1 = personStr1 + tempPersonStr + ",";
                receivedPersonids1 = receivedPersonids1 + Util.null2String(this.rs.getString("userid")) + ",";
              }
            } else if (this.rs.getInt("agenttype") == 2) {
              tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(this.rs.getString("agentorbyagentid")), this.user.getLanguage()) + "->" + Util.toScreen(resourceComInfo.getResourcename(this.rs.getString("userid")), this.user.getLanguage());
              if ((personStr1.indexOf("," + tempPersonStr + ",") == -1) && (personStr1.indexOf(tempPersonStr + ",") == -1)) {
                personStr1 = personStr1 + tempPersonStr + ",";
                receivedPersonids1 = receivedPersonids1 + Util.null2String(this.rs.getString("userid")) + ",";
              }
            }
          }
          else {
            tempPersonStr = Util.toScreen(this.customerInfoComInfo.getCustomerInfoname(this.rs.getString("userid")), this.user.getLanguage());
            if ((personStr1.indexOf("," + tempPersonStr + ",") == -1) && (personStr1.indexOf(tempPersonStr + ",") == -1)) {
              personStr1 = personStr1 + tempPersonStr + ",";
              receivedPersonids1 = receivedPersonids1 + Util.null2String(this.rs.getString("userid")) + ",";
            }
          }

        }

        if ((this.operator89mp != null) && (!(this.operator89mp.isEmpty()))) {
          this.operator89List = ((ArrayList)this.operator89mp.get(String.valueOf(this.nextnodeid)));
          this.operatortype89List = ((ArrayList)this.operatortype89mp.get(String.valueOf(this.nextnodeid)));
          this.agentoperator89List = ((ArrayList)this.agentoperator89mp.get(String.valueOf(this.nextnodeid)));

          if (this.operator89List == null) this.operator89List = new ArrayList();
          if (this.operatortype89List == null) this.operatortype89List = new ArrayList();
          if (this.agentoperator89List == null) this.agentoperator89List = new ArrayList();
        }

        for (int cc = 0; cc < this.operator89List.size(); ++cc) {
          String tempPersonStr;
          int operatortype_tmp = Util.getIntValue((String)this.operatortype89List.get(cc), 0);
          int operator_tmp = Util.getIntValue((String)this.operator89List.get(cc), 0);
          int agentoperator_tmp = Util.getIntValue((String)this.agentoperator89List.get(cc), 0);
          if (operatortype_tmp == 0) {
            if (agentoperator_tmp == 0) {
              tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(operator_tmp), this.user.getLanguage());
              if ((personStr1.indexOf("," + tempPersonStr + ",") == -1) && (personStr1.indexOf(tempPersonStr + ",") == -1)) {
                personStr1 = personStr1 + tempPersonStr + ",";
                receivedPersonids1 = receivedPersonids1 + Util.null2String(this.operator89List.get(cc)) + ",";
              }
            } else if (agentoperator_tmp > 0) {
              tempPersonStr = Util.toScreen(resourceComInfo.getResourcename(new StringBuilder().append(operator_tmp).toString()), this.user.getLanguage()) + "->" + Util.toScreen(resourceComInfo.getResourcename(new StringBuilder().append(agentoperator_tmp).toString()), this.user.getLanguage());
              if ((personStr1.indexOf("," + tempPersonStr + ",") == -1) && (personStr1.indexOf(tempPersonStr + ",") == -1)) {
                personStr1 = personStr1 + tempPersonStr + ",";
                receivedPersonids1 = receivedPersonids1 + Util.null2String(this.operator89List.get(cc)) + ",";
              }
            }
          }
          else {
            tempPersonStr = Util.toScreen(this.customerInfoComInfo.getCustomerInfoname(operator_tmp), this.user.getLanguage());
            if ((personStr1.indexOf("," + tempPersonStr + ",") == -1) && (personStr1.indexOf(tempPersonStr + ",") == -1)) {
              personStr1 = personStr1 + tempPersonStr + ",";
              receivedPersonids1 = receivedPersonids1 + Util.null2String(this.operator89List.get(cc)) + ",";
            }
          }
        }
      }
      if ((!(this.canflowtonextnode)) && (((this.nextnodeattr == 3) || (this.nextnodeattr == 4) || (this.nextnodeattr == 5)))) {
        personStr = SystemEnv.getHtmlLabelName(21399, this.user.getLanguage()) + ",";
      }
      tempSQL = "select agentorbyagentid, agenttype, showorder from workflow_currentoperator where nodeid = " + nownodeid + " and requestid = " + this.requestid + " and userid = " + this.userid + isremarkStr_t + " order by operatedate desc,operatetime desc, id desc";
      this.rs.executeSql(tempSQL);
      if (this.rs.next()) {
        agentorbyagentid = this.rs.getInt("agentorbyagentid");
        agenttype = this.rs.getInt("agenttype");
        showorder = this.rs.getInt("showorder");
      }
      if ((logtype.equals("9")) || (logtype.equals("7")))
      {
        this.rs.executeSql("select max(showorder) as maxshow from workflow_requestlog where requestid = " + this.requestid + " and nodeid = " + nownodeid + " and (logtype = '9' or logtype = '7')");
        this.rs.next();
        if (this.rs.getInt("maxshow") != -1)
          showorder = this.rs.getInt("maxshow") + 1;
        else {
          showorder = 10000;
        }
        this.rs.executeSql("select receivedpersons,receivedPersonids from workflow_requestlog where requestid = " + this.requestid + " and nodeid = " + nownodeid + " and (logtype = '2' or logtype = '0' or logtype = '3') order by operatedate desc,operatetime desc");
        if (this.rs.next()) {
          personStr = Util.null2String(this.rs.getString(1));
          receivedPersonids = Util.null2String(this.rs.getString(2));
        }
      }

      this.Procpara = this.requestid + this.flag + this.workflowid + this.flag + nownodeid + this.flag + logtype + this.flag + this.logdate + this.flag + this.logtime + this.flag + this.userid + this.flag + clientip + this.flag + this.usertype + this.flag + nownextnodeid + this.flag + personStr.trim() + this.flag + agentorbyagentid + this.flag + agenttype + this.flag + showorder + this.flag + annexdocids + this.flag + requestLogId + this.flag + this.signdocids + this.flag + this.signworkflowids + this.flag + this.clientType + this.flag + this.speechAttachment + this.flag + this.handWrittenSign + this.flag + receivedPersonids + this.flag + this.remarkLocation;

      if (this.logdate.equals(""))
      {
        currentString = execRequestlog(this.Procpara, this.rs, this.flag, this.remark);
        if ((!("".equals(currentString))) && (currentString.indexOf("~~current~~") > -1)) {
          arraycurrent = Util.TokenizerString2(currentString, "~~current~~");
          this.logdate = arraycurrent[0];
          this.logtime = arraycurrent[1];
        }
      }
      else {
        execRequestlog(this.Procpara, this.rs, this.flag, this.remark);
      }

      if (personStr1.trim().equals("")) {
        return;
      }
      this.Procpara = this.requestid + this.flag + this.workflowid + this.flag + nownodeid + this.flag + "t" + this.flag + this.logdate + this.flag + this.logtime + this.flag + this.userid + this.flag + clientip + this.flag + this.usertype + this.flag + nownextnodeid + this.flag + personStr1.trim() + this.flag + agentorbyagentid + this.flag + agenttype + this.flag + showorder + this.flag + this.flag + requestLogId + this.flag + this.flag + this.flag + this.clientType + this.flag + this.speechAttachment + this.flag + this.handWrittenSign + this.flag + receivedPersonids1 + this.flag + this.remarkLocation;

      if (this.logdate.equals(""))
      {
        currentString = execRequestlog(this.Procpara, this.rs, this.flag, "");
        if ((!("".equals(currentString))) && (currentString.indexOf("~~current~~") > -1)) {
          arraycurrent = Util.TokenizerString2(currentString, "~~current~~");
          this.logdate = arraycurrent[0];
          this.logtime = arraycurrent[1];
        }
      }
      else {
        execRequestlog(this.Procpara, this.rs, this.flag, "");
      }
    }
  }

  private String doRequestMark()
  {
    RecordSetTrans rst1 = new RecordSetTrans();
    rst1.setAutoCommit(false);
    try {
      String needmark = "";
      rst1.executeSql(" select needmark from workflow_base where id = " + this.workflowid);
      if (rst1.next()) needmark = Util.null2String(this.rs.getString("needmark"));

      if (needmark.equals("1")) {
        String newcurrentdate = Util.StringReplace(this.currentdate, "-", "");
        rst1.executeSql("select requestmark from workflow_requestmark where markdate = '" + this.currentdate + "' ");
        if (rst1.next()) {
          int requestmark = Util.getIntValue(this.rs.getString("requestmark"), 0);
          rst1.executeSql("update workflow_requestmark set requestmark = requestmark+1 where markdate = '" + this.currentdate + "' ");
          return newcurrentdate + Util.add0(requestmark + 1, 4);
        }
        rst1.executeSql("insert into workflow_requestmark values('" + this.currentdate + "',1) ");
        return newcurrentdate + Util.add0(1, 4);
      }

      rst1.commit();
    } catch (Exception exception) {
      exception.printStackTrace();
      rst1.rollback();
      return "";
    }
    return "";
  }

  public String getMessageType() {
    return this.messageType;
  }

  public void setMessageType(String messageType) {
    if ((messageType == null) || (messageType.equals(""))) {
      messageType = "0";
    }
    this.messageType = messageType;
  }

  public boolean isOldOrNewFlagTrans(int requestid, RecordSetTrans rst)
    throws Exception
  {
    return false;
  }

  public boolean isOldOrNewFlag(int requestid)
  {
    boolean isOldWf = false;
    RecordSet rs_ = new RecordSet();
    rs_.executeSql("select nodeid from workflow_currentoperator where requestid = " + requestid);
    while (rs_.next()) {
      if ((rs_.getString("nodeid") == null) || ("".equals(rs_.getString("nodeid"))) || ("-1".equals(rs_.getString("nodeid")))) {
        isOldWf = true;
      }
    }
    return isOldWf;
  }

  public void setOperator(RecordSetTrans rst)
    throws Exception
  {
    WFAutoApproveUtils.processApproveLog(rst, this);
    wfAgentCondition wfAgentCondition = new wfAgentCondition();
    ArrayList poppuplist = new ArrayList();
    WFCoadjutantManager coadjutantmanager = new WFCoadjutantManager();
    WFAutoApproveUtils wfautoApproveUtil = new WFAutoApproveUtils();
    int organizedid = 0;
    rst.executeSql("select nodeattribute from workflow_nodebase where id=" + this.nodeid);
    rst.next();
    int nodeattr = rst.getInt(1);

    String prevOperatorid = "";
    int prevOperatortype = 0;
    int prevOperatorisremark = 0;
    int prevOperatornodeid = 0;
    int prevgroupdetailid = 0;

    Date d2 = null;
    ArrayList operatorsWfNew = new ArrayList();
    ArrayList operatorsWfEnd = new ArrayList();
    for (int n = 0; n < this.nextnodeids.size(); ++n) {
      this.nextnodeid = Util.getIntValue((String)this.nextnodeids.get(n), 0);
      this.operatorsht = ((Hashtable)this.operatorshts.get(n));
      this.nextnodetype = Util.null2String((String)this.nextnodetypes.get(n));
      this.nextnodeattr = Util.getIntValue((String)this.nextnodeattrs.get(n), 0);

      this.operator89List = new ArrayList();
      this.operatortype89List = new ArrayList();
      this.agentoperator89List = new ArrayList();

      int showorder = 0;
      TreeMap map = new TreeMap(new ComparatorUtilBean());
      Enumeration tempKeys = this.operatorsht.keys();
      while (tempKeys.hasMoreElements()) {
        String tempKey = (String)tempKeys.nextElement();
        ArrayList tempoperators = (ArrayList)this.operatorsht.get(tempKey);
        map.put(tempKey, tempoperators);
      }
      try
      {
        int verSuccussPerson = 0;

        Map operatorAgentInfo = wfAgentCondition.getAgentInfoByResouce(
          String.valueOf(this.workflowid), 
          wfAgentCondition.getAlloperator(this.operatorsht, map), 
          String.valueOf(this.requestid));

        Iterator iterator = map.keySet().iterator();

        List insertOperatorParamsList = new ArrayList();
        String operatornames = "";
        while (iterator.hasNext()) {
          String operatorgroup = (String)iterator.next();
          ArrayList operators = (ArrayList)this.operatorsht.get(operatorgroup);

          for (int i = 0; i < operators.size(); ++i) {
            ++showorder;
            String operatorandtype = (String)operators.get(i);
            String[] operatorandtypes = Util.TokenizerString2(operatorandtype, "_");
            String opertor = operatorandtypes[0];
            String opertortype = operatorandtypes[1];
            this.groupdetailid = Util.getIntValue(operatorandtypes[2], -1);
            this.typeid = Util.getIntValue(operatorandtypes[3], 0);
            if ((!(this.canflowtonextnode)) && (this.typeid != -3) && (this.typeid != -4) && (this.typeid != -5))
              continue;
            boolean isbeAgent = false;
            String agenterId = "";

            if (opertortype.equals("0"))
            {
              agenterId = (String)operatorAgentInfo.get(opertor);
              if ((agenterId != null) && (!(agenterId.equals("")))) {
                isbeAgent = true;
              }
            }

            String hrmstatusSql = "";
            String tempStatus = "";
            String operatorname = "";
            if (!(isbeAgent))
            {
              if (!("3".equals(this.nextnodetype)))
              {
                if (opertortype.equals("0")) {
                  hrmstatusSql = "select status from hrmresource where id=" + opertor;
                  tempStatus = this.resourceComInfo.getStatus(opertor);
                  operatorname = this.resourceComInfo.getLastname(opertor);
                } else {
                  hrmstatusSql = "select status,name from CRM_CustomerInfo where id=" + opertor;
                  rst.execute(hrmstatusSql);
                  if (rst.next()) {
                    tempStatus = rst.getString("status");
                    operatornames = Util.null2String(rst.getString("name"));
                  }

                }

                if ((opertortype.equals("0")) && 
                  (!("".equals(tempStatus))) && ((
                  ("5".equals(tempStatus)) || ("4".equals(tempStatus)) || ("6".equals(tempStatus))))) {
                  if ((this.typeid == -3) || (this.typeid == -4) || (this.typeid == -5)) continue;
                  operatornames = operatornames + operatorname + ",";
                }
              }
            }
            else
            {
              Map popmap;
              if (!("3".equals(this.nextnodetype)))
              {
                hrmstatusSql = "select status,lastname from hrmresource where id=" + agenterId;
                rst.execute(hrmstatusSql);
                if (rst.next()) {
                  tempStatus = rst.getString("status");
                  operatorname = Util.null2String(rst.getString("lastname"));
                }

                if ((opertortype.equals("0")) && 
                  (!("".equals(tempStatus))) && ((
                  ("5".equals(tempStatus)) || ("4".equals(tempStatus)) || ("6".equals(tempStatus))))) {
                  if ((this.typeid != -3) && (this.typeid != -4) && (this.typeid != -5)) {
                    operatornames = operatornames + operatorname + ",";
                  }
                  isbeAgent = false;
                }

              }

              if ((this.typeid != -3) && (this.typeid != -4) && (this.typeid != -5)) ++verSuccussPerson;

              String Procpara1 = "";

              int tempremark = 0;
              if (this.typeid == -3) {
                tempremark = 8;
              }
              if (this.typeid == -4) {
                tempremark = 9;
              }
              if (this.typeid == -5) {
                tempremark = 7;
                rst.executeSql("update workflow_CurrentOperator set isremark='2' where isremark='7' and requestid=" + this.requestid + " and userid=" + opertor + " and usertype=" + opertortype + " and nodeid=" + this.nodeid);
              }

              boolean isMergeNode = (this.nextnodeattr == 3) || (this.nextnodeattr == 4) || (this.nextnodeattr == 5);

              boolean isCopyOperation = (tempremark == 7) || (tempremark == 8) || (tempremark == 9);

              if ((isMergeNode) && (isCopyOperation))
              {
                boolean canFlowToNextNode = this.wflinkinfo.FlowToNextNode(this.requestid, this.nodeid, this.nextnodeid, String.valueOf(this.nextnodeattr), Util.getIntValue((String)this.nextnodepassnums.get(n)), Util.getIntValue((String)this.linkismustpasss.get(n)), rst);

                if (canFlowToNextNode)
                {
                  if (isOldOrNewFlagTrans(this.requestid, rst)) {
                    if (isbeAgent)
                    {
                      this.Procpara = this.requestid + this.flag + opertor + this.flag + operatorgroup + this.flag + 
                        this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + "2" + this.flag + -1 + 
                        this.flag + -1 + this.flag + "0" + this.flag + -1 + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
                      rst.executeProc("workflow_CurrentOperator_I2", this.Procpara);

                      Procpara1 = this.requestid + this.flag + agenterId + this.flag + operatorgroup + this.flag + 
                        this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + tempremark + this.flag + -1 + 
                        this.flag + -1 + this.flag + "0" + this.flag + -1 + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
                      rst.executeProc("workflow_CurrentOperator_I2", Procpara1);
                      if ((tempremark == 8) || (tempremark == 9)) {
                        this.operator89List.add(opertor);
                        this.operatortype89List.add("0");
                        this.agentoperator89List.add(agenterId);
                      }
                    } else {
                      this.Procpara = this.requestid + this.flag + opertor + this.flag + operatorgroup + this.flag + 
                        this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + tempremark + this.flag + -1 + 
                        this.flag + -1 + this.flag + "0" + this.flag + -1 + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
                      rst.executeProc("workflow_CurrentOperator_I2", this.Procpara);
                      if ((tempremark == 8) || (tempremark == 9)) {
                        this.operator89List.add(opertor);
                        this.operatortype89List.add(opertortype);
                        this.agentoperator89List.add("0");
                      }
                    }
                  }
                  else if (isbeAgent)
                  {
                    this.Procpara = this.requestid + this.flag + opertor + this.flag + operatorgroup + this.flag + 
                      this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + "2" + this.flag + this.nextnodeid + 
                      this.flag + agenterId + this.flag + "1" + this.flag + showorder + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
                    rst.executeProc("workflow_CurrentOperator_I2", this.Procpara);

                    Procpara1 = this.requestid + this.flag + agenterId + this.flag + operatorgroup + this.flag + 
                      this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + tempremark + this.flag + this.nextnodeid + 
                      this.flag + opertor + this.flag + "2" + this.flag + showorder + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
                    rst.executeProc("workflow_CurrentOperator_I2", Procpara1);
                    if ((tempremark == 8) || (tempremark == 9)) {
                      this.operator89List.add(opertor);
                      this.operatortype89List.add("0");
                      this.agentoperator89List.add(agenterId);
                    }
                  } else {
                    this.Procpara = this.requestid + this.flag + opertor + this.flag + operatorgroup + this.flag + 
                      this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + tempremark + this.flag + this.nextnodeid + 
                      this.flag + -1 + this.flag + "0" + this.flag + showorder + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
                    rst.executeProc("workflow_CurrentOperator_I2", this.Procpara);
                    if ((tempremark == 8) || (tempremark == 9)) {
                      this.operator89List.add(opertor);
                      this.operatortype89List.add(opertortype);
                      this.agentoperator89List.add("0");
                    }

                  }

                  if (this.currentopratorInsFirstid == 0) {
                    if (isbeAgent)
                      this.sql = "select id from workflow_CurrentOperator where requestid=" + this.requestid + " and userid=" + agenterId + " and usertype=" + this.usertype + " and isremark='" + tempremark + "' and nodeid=" + this.nextnodeid + " order by id desc";
                    else {
                      this.sql = "select id from workflow_CurrentOperator where requestid=" + this.requestid + " and userid=" + opertor + " and usertype=" + this.usertype + " and isremark='" + tempremark + "' and nodeid=" + this.nextnodeid + " order by id desc";
                    }
                    rst.execute(this.sql);
                    if (rst.next()) {
                      this.currentopratorInsFirstid = rst.getInt("id");
                    }

                  }

                  if (tempremark == 7) {
                    this.sql = "select id from workflow_CurrentOperator where requestid=" + this.requestid + " and userid=" + prevOperatorid + " and usertype=" + prevOperatortype + " and isremark='" + prevOperatorisremark + "' and nodeid=" + prevOperatornodeid + " order by id desc";
                    rst.execute(this.sql);
                    if (rst.next()) {
                      organizedid = rst.getInt("id");
                    }

                    if (isbeAgent)
                      this.sql = "select id from workflow_CurrentOperator where requestid=" + this.requestid + " and userid=" + agenterId + " and usertype=" + this.usertype + " and isremark='" + tempremark + "' and nodeid=" + this.nextnodeid + " order by id desc";
                    else {
                      this.sql = "select id from workflow_CurrentOperator where requestid=" + this.requestid + " and userid=" + opertor + " and usertype=" + this.usertype + " and isremark='" + tempremark + "' and nodeid=" + this.nextnodeid + " order by id desc";
                    }
                    rst.execute(this.sql);
                    if (rst.next()) {
                      int currentid = rst.getInt("id");
                      coadjutantmanager.getCoadjutantRights(this.groupdetailid);
                      coadjutantmanager.SaveCoadjutantRights(this.requestid, organizedid, currentid, rst);
                    }
                  }
                  else {
                    if (isbeAgent)
                      prevOperatorid = agenterId;
                    else {
                      prevOperatorid = opertor;
                    }
                    prevOperatortype = this.usertype;
                    prevOperatorisremark = tempremark;
                    prevOperatornodeid = this.nextnodeid;
                  }

                }

              }
              else
              {
                if (isOldOrNewFlagTrans(this.requestid, rst)) {
                  if (isbeAgent)
                  {
                    this.Procpara = this.requestid + this.flag + opertor + this.flag + operatorgroup + this.flag + 
                      this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + "2" + this.flag + -1 + 
                      this.flag + -1 + this.flag + "0" + this.flag + -1 + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
                    rst.executeProc("workflow_CurrentOperator_I2", this.Procpara);

                    Procpara1 = this.requestid + this.flag + agenterId + this.flag + operatorgroup + this.flag + 
                      this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + tempremark + this.flag + -1 + 
                      this.flag + -1 + this.flag + "0" + this.flag + -1 + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
                    rst.executeProc("workflow_CurrentOperator_I2", Procpara1);
                    if ((tempremark == 8) || (tempremark == 9)) {
                      this.operator89List.add(opertor);
                      this.operatortype89List.add("0");
                      this.agentoperator89List.add(agenterId);
                    }
                  } else {
                    this.Procpara = this.requestid + this.flag + opertor + this.flag + operatorgroup + this.flag + 
                      this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + tempremark + this.flag + -1 + 
                      this.flag + -1 + this.flag + "0" + this.flag + -1 + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
                    rst.executeProc("workflow_CurrentOperator_I2", this.Procpara);
                    if ((tempremark == 8) || (tempremark == 9)) {
                      this.operator89List.add(opertor);
                      this.operatortype89List.add(opertortype);
                      this.agentoperator89List.add("0");
                    }
                  }
                }
                else if ((this.hasCoadjutant) || (this.currentopratorInsFirstid == 0)) {
                  if (isbeAgent)
                  {
                    if (!(wfCurrentOperatorAgent(rst, opertor, operatorgroup, opertortype, agenterId, showorder))) {
                      this.Procpara = this.requestid + this.flag + opertor + this.flag + operatorgroup + this.flag + 
                        this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + "2" + this.flag + this.nextnodeid + 
                        this.flag + agenterId + this.flag + "1" + this.flag + showorder + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
                      rst.executeProc("workflow_CurrentOperator_I2", this.Procpara);
                    }

                    Procpara1 = this.requestid + this.flag + agenterId + this.flag + operatorgroup + this.flag + 
                      this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + tempremark + this.flag + this.nextnodeid + 
                      this.flag + opertor + this.flag + "2" + this.flag + showorder + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
                    rst.executeProc("workflow_CurrentOperator_I2", Procpara1);
                    if ((tempremark == 8) || (tempremark == 9)) {
                      this.operator89List.add(opertor);
                      this.operatortype89List.add("0");
                      this.agentoperator89List.add(agenterId);
                    }
                  } else {
                    this.Procpara = this.requestid + this.flag + opertor + this.flag + operatorgroup + this.flag + 
                      this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + tempremark + this.flag + this.nextnodeid + 
                      this.flag + -1 + this.flag + "0" + this.flag + showorder + this.flag + this.groupdetailid + this.flag + this.currentdate + this.flag + this.currenttime;
                    rst.executeProc("workflow_CurrentOperator_I2", this.Procpara);
                    if ((tempremark == 8) || (tempremark == 9)) {
                      this.operator89List.add(opertor);
                      this.operatortype89List.add(opertortype);
                      this.agentoperator89List.add("0");
                    }
                  }
                }
                else
                {
                  List insertSqlParamList;
                  if (isbeAgent) {
                    insertSqlParamList = new ArrayList();

                    insertSqlParamList.add(opertor);
                    insertSqlParamList.add(operatorgroup);

                    insertSqlParamList.add(opertortype);
                    insertSqlParamList.add("2");
                    insertSqlParamList.add(Integer.valueOf(this.nextnodeid));
                    insertSqlParamList.add(agenterId);
                    insertSqlParamList.add(Integer.valueOf(1));
                    insertSqlParamList.add(Integer.valueOf(showorder));

                    insertSqlParamList.add(this.currentdate);
                    insertSqlParamList.add(this.currenttime);
                    insertSqlParamList.add(Integer.valueOf(this.groupdetailid));
                    insertSqlParamList.add("2");
                    insertOperatorParamsList.add(insertSqlParamList);
                    insertSqlParamList = new ArrayList();

                    insertSqlParamList.add(agenterId);
                    insertSqlParamList.add(operatorgroup);

                    insertSqlParamList.add(opertortype);
                    insertSqlParamList.add(Integer.valueOf(tempremark));
                    insertSqlParamList.add(Integer.valueOf(this.nextnodeid));
                    insertSqlParamList.add(opertor);
                    insertSqlParamList.add(Integer.valueOf(2));
                    insertSqlParamList.add(Integer.valueOf(showorder));

                    insertSqlParamList.add(this.currentdate);
                    insertSqlParamList.add(this.currenttime);
                    insertSqlParamList.add(Integer.valueOf(this.groupdetailid));
                    insertSqlParamList.add(Integer.valueOf(tempremark));
                    insertOperatorParamsList.add(insertSqlParamList);

                    if ((tempremark == 8) || (tempremark == 9)) {
                      this.operator89List.add(opertor);
                      this.operatortype89List.add("0");
                      this.agentoperator89List.add(agenterId);
                    }
                  } else {
                    insertSqlParamList = new ArrayList();

                    insertSqlParamList.add(opertor);
                    insertSqlParamList.add(operatorgroup);

                    insertSqlParamList.add(opertortype);
                    insertSqlParamList.add(Integer.valueOf(tempremark));
                    insertSqlParamList.add(Integer.valueOf(this.nextnodeid));
                    insertSqlParamList.add(Integer.valueOf(-1));
                    insertSqlParamList.add(Integer.valueOf(0));
                    insertSqlParamList.add(Integer.valueOf(showorder));

                    insertSqlParamList.add(this.currentdate);
                    insertSqlParamList.add(this.currenttime);
                    insertSqlParamList.add(Integer.valueOf(this.groupdetailid));
                    insertSqlParamList.add(Integer.valueOf(tempremark));
                    insertOperatorParamsList.add(insertSqlParamList);

                    if ((tempremark == 8) || (tempremark == 9)) {
                      this.operator89List.add(opertor);
                      this.operatortype89List.add(opertortype);
                      this.agentoperator89List.add("0");
                    }

                  }

                }

                if (this.currentopratorInsFirstid == 0) {
                  if (isbeAgent)
                    this.sql = "select id from workflow_CurrentOperator where requestid=" + this.requestid + " and userid=" + agenterId + " and usertype=" + this.usertype + " and isremark='" + tempremark + "' and nodeid=" + this.nextnodeid + " order by id desc";
                  else {
                    this.sql = "select id from workflow_CurrentOperator where requestid=" + this.requestid + " and userid=" + opertor + " and usertype=" + this.usertype + " and isremark='" + tempremark + "' and nodeid=" + this.nextnodeid + " order by id desc";
                  }
                  rst.execute(this.sql);
                  if (rst.next()) {
                    this.currentopratorInsFirstid = rst.getInt("id");
                  }

                }

                if (tempremark == 7) {
                  this.sql = "select id from workflow_CurrentOperator where requestid=" + this.requestid + " and userid=" + prevOperatorid + " and usertype=" + prevOperatortype + " and isremark='" + prevOperatorisremark + "' and nodeid=" + prevOperatornodeid + " order by id desc";
                  rst.execute(this.sql);
                  if (rst.next()) {
                    organizedid = rst.getInt("id");
                  }

                  if (isbeAgent)
                    this.sql = "select id from workflow_CurrentOperator where requestid=" + this.requestid + " and userid=" + agenterId + " and usertype=" + this.usertype + " and isremark='" + tempremark + "' and nodeid=" + this.nextnodeid + " order by id desc";
                  else {
                    this.sql = "select id from workflow_CurrentOperator where requestid=" + this.requestid + " and userid=" + opertor + " and usertype=" + this.usertype + " and isremark='" + tempremark + "' and nodeid=" + this.nextnodeid + " order by id desc";
                  }
                  rst.execute(this.sql);
                  if (rst.next()) {
                    int currentid = rst.getInt("id");
                    coadjutantmanager.getCoadjutantRights(this.groupdetailid);
                    coadjutantmanager.SaveCoadjutantRights(this.requestid, organizedid, currentid, rst);
                  }
                }
                else {
                  if (isbeAgent)
                    prevOperatorid = agenterId;
                  else {
                    prevOperatorid = opertor;
                  }
                  prevOperatortype = this.usertype;
                  prevOperatorisremark = tempremark;
                  prevOperatornodeid = this.nextnodeid;
                }

              }

              this.Procpara = opertor + this.flag + opertortype + this.flag + this.requestid;

              if (this.nextnodetype.equals("3")) {
                if (isbeAgent)
                {
                  if ((operatorsWfEnd.contains(agenterId + "_" + opertortype)) || (this.userid + "_" + this.usertype.equals(agenterId + "_" + opertortype)))
                    continue;
                  popmap = new HashMap();
                  popmap.put("userid", Integer.parseInt(agenterId));
                  popmap.put("type", "1");
                  popmap.put("logintype", opertortype);
                  popmap.put("requestid", this.requestid);
                  popmap.put("requestname", this.requestname);
                  popmap.put("workflowid", this.workflowid);
                  popmap.put("creater", this.creater);
                  poppuplist.add(popmap);
                  operatorsWfEnd.add(agenterId + "_" + opertortype);
                }
                else
                {
                  if ((operatorsWfEnd.contains(opertor + "_" + opertortype)) || (this.userid + "_" + this.usertype.equals(opertor + "_" + opertortype)))
                    continue;
                  popmap = new HashMap();
                  popmap.put("userid", Integer.parseInt(opertor));
                  popmap.put("type", "1");
                  popmap.put("logintype", opertortype);
                  popmap.put("requestid", this.requestid);
                  popmap.put("requestname", this.requestname);
                  popmap.put("workflowid", this.workflowid);
                  popmap.put("creater", this.creater);
                  poppuplist.add(popmap);
                  operatorsWfEnd.add(opertor + "_" + opertortype);
                }

              }
              else if (isbeAgent) {
                if (operatorsWfNew.contains(agenterId + "_" + opertortype))
                  continue;
                popmap = new HashMap();
                popmap.put("userid", Integer.parseInt(agenterId));
                popmap.put("type", "0");
                popmap.put("logintype", opertortype);
                popmap.put("requestid", this.requestid);
                popmap.put("requestname", this.requestname);
                popmap.put("workflowid", this.workflowid);
                popmap.put("creater", this.creater);
                poppuplist.add(popmap);
                operatorsWfNew.add(agenterId + "_" + opertortype);
              }
              else {
                if (operatorsWfNew.contains(opertor + "_" + opertortype))
                  continue;
                popmap = new HashMap();
                popmap.put("userid", Integer.parseInt(opertor));
                popmap.put("type", "0");
                popmap.put("logintype", opertortype);
                popmap.put("requestid", this.requestid);
                popmap.put("requestname", this.requestname);
                popmap.put("workflowid", this.workflowid);
                popmap.put("creater", this.creater);
                poppuplist.add(popmap);
                operatorsWfNew.add(opertor + "_" + opertortype);
              }
            }
          }

        }

        if ((insertOperatorParamsList != null) && (!(insertOperatorParamsList.isEmpty())))
        {
          if ("".equals(Util.null2String(this.workflowtype))) {
            WorkflowAllComInfo workflowComInfo = new WorkflowAllComInfo();
            this.workflowtype = workflowComInfo.getWorkflowtype(this.workflowid);
          }

          rst.executeBatchSql("INSERT INTO workflow_currentoperator ( requestid, userid, groupid, workflowid, workflowtype, usertype, isremark, nodeid, agentorbyagentid, agenttype, showorder, receivedate, receivetime, viewtype, iscomplete, islasttimes, groupdetailid, preisremark, needwfback )  VALUES (" + 
            this.requestid + ", ?, ?, " + this.workflowid + ", " + this.workflowtype + ", ?, ?, ?, ?, ?, " + 
            " ?, ?, ?, 0,0,1, ?, ?, '1' )", insertOperatorParamsList);
        }

        String tempNextNodeId = String.valueOf(this.nextnodeid);
        this.operator89mp.put(tempNextNodeId, this.operator89List);
        this.operatortype89mp.put(tempNextNodeId, this.operatortype89List);
        this.agentoperator89mp.put(tempNextNodeId, this.agentoperator89List);

        if ((verSuccussPerson == 0) && (this.nextnodeattr != 3) && (this.nextnodeattr != 4) && (this.nextnodeattr != 5) && (this.canflowtonextnode)) {
          setMessage("126221");
          if (StringUtil.isNotNull(operatornames)) {
            operatornames = operatornames.substring(0, operatornames.length() - 1);
          }
          setMessagecontent(WorkflowRequestMessage.getRMOperatorOutInfo(this.nextnodeid, this.userlanguage, this.workflowid, operatornames));
          throw new Exception();
        }

        if (this.canflowtonextnode) {
          if ((nodeattr == 2) && (this.nextnodeattr != 2))
          {
            rst.executeSql("update workflow_currentoperator set isremark=2 ,operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where nodeid!=" + this.nextnodeid + " and isremark=0 and requestid=" + this.requestid + " and preisremark=0");
            this.rs.executeSql("select id,groupid,nodeid from workflow_currentoperator  where nodeid!=" + this.nextnodeid + " and (isremark=2 and ( operatedate is null or operatedate='')) and requestid=" + this.requestid + " and preisremark=0");
            while (this.rs.next()) {
              String _tempid = this.rs.getString(1);
              String _tempgroupid = this.rs.getString(2);
              String _tempnodeid = this.rs.getString(3);
              rst.executeSql("select operatedate,operatetime from workflow_currentoperator where requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and isremark=2 and preisremark=0 and groupid=" + _tempgroupid);
              if (rst.next()) {
                String _tempdate = this.rs.getString(1);
                String _temptime = this.rs.getString(2);
                rst.executeSql("update workflow_currentoperator set operatedate='" + _tempdate + "',operatetime='" + _temptime + "' where id=" + _tempid);
              }
            }
            rst.executeSql("delete from workflow_nownode where requestid=" + this.requestid);
          }
          try {
            if ((this.innodeids.equals("")) || (this.innodeids.equals("0"))) {
              this.innodeids = this.nodeid;
            }
            rst.executeSql("delete from workflow_nownode where nownodeid in(" + this.innodeids + ") and requestid=" + this.requestid);
            rst.executeSql("insert into workflow_nownode(requestid,nownodeid,nownodetype,nownodeattribute) values(" + this.requestid + "," + this.nextnodeid + "," + this.nextnodetype + "," + this.nextnodeattr + ")");
          } catch (Exception e) {
            System.out.println(e.getMessage());
          }
        }
      } catch (Exception exception) {
        writeLog(exception);
        throw exception;
      }
    }
    try {
      boolean isautoApprove = wfautoApproveUtil.isAutoApprove(this, rst, this.nodeInfoCache, poppuplist, this.nextnodeattr);
      if (isautoApprove)
        return;
      this.poppupRemindInfoUtil.setPoppuplist(poppuplist);
    }
    catch (Exception exception2)
    {
      throw exception2;
    }
  }

  public void setOperatorByremark5(ArrayList operators, ArrayList operatorsType, RecordSetTrans rst)
    throws Exception
  {
    WFAutoApproveUtils.processApproveLog(rst, this);
    ArrayList poppuplist = new ArrayList();
    ArrayList operatorsWfNew = new ArrayList();
    ArrayList operatorsWfEnd = new ArrayList();
    for (int n = 0; n < this.nextnodeids.size(); ++n) {
      this.nextnodeid = Util.getIntValue((String)this.nextnodeids.get(n), 0);
      this.nextnodetype = Util.null2String((String)this.nextnodetypes.get(n));
      this.nextnodeattr = Util.getIntValue((String)this.nextnodeattrs.get(n), 0);
      wfAgentCondition wfAgentCondition = new wfAgentCondition();

      int showorder = 0;
      String opertortype = "0";
      this.groupdetailid = 0;
      try {
        for (int i = 0; i < operators.size(); ++i) {
          if (this.SignType == 2)
            if ((this.nextnodetype.equals("3")) || (this.nextnodetype.equals("3")) || (i <= 0)) {
              this.operatorgroup = -2;
              this.groupdetailid = -2;
              String tempStr = "";
              String tempStrType = "";
              for (int k = i + 1; k < operators.size(); ++k) {
                tempStr = tempStr + operators.get(k) + ",";
              }
              rst.executeProc("workflow_groupdetail_Insert", this.operatorgroup + this.flag + 5 + this.flag + this.operatorgroup + this.flag + 0 + this.flag + -1 + this.flag + this.flag + this.flag + 0 + this.flag + 2 + this.flag + this.flag + this.flag + this.flag + this.flag + this.flag + this.flag + this.flag + this.flag + this.flag + this.flag);
              if (rst.next()) {
                this.groupdetailid = rst.getInt(1);
              }
              rst.executeSql("delete from workflow_agentpersons where requestid=" + this.requestid + " and  (groupdetailid in(select b.id from workflow_nodegroup a,workflow_groupdetail b where a.id=b.groupid and a.nodeid in(" + this.nodeid + "," + this.nextnodeid + ")) or groupdetailid is null)");
              rst.executeSql("insert into workflow_agentpersons values (" + this.requestid + ",'" + tempStr.trim() + "'," + this.groupdetailid + ",'')");
            }
          ++showorder;
          int opertor = Util.getIntValue((String)operators.get(i));

          if ((operatorsType != null) && (i < operatorsType.size()))
            opertortype = (String)operatorsType.get(i);
          else {
            opertortype = "0";
          }

          if (opertor > 0)
          {
            Map popmap;
            boolean isbeAgent = false;
            String agenterId = "";

            if (opertortype.equals("0")) {
              agenterId = wfAgentCondition.getAgentid(this.workflowid, opertor, this.requestid);
              if (!(agenterId.equals(""))) {
                isbeAgent = true;
              }
            }

            String Procpara1 = "";

            if (isOldOrNewFlagTrans(this.requestid, rst)) {
              if (isbeAgent)
              {
                this.Procpara = this.requestid + this.flag + opertor + this.flag + this.operatorgroup + this.flag + 
                  this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + "2" + this.flag + -1 + 
                  this.flag + -1 + this.flag + "0" + this.flag + -1 + this.flag + this.groupdetailid;
                rst.executeProc("workflow_CurrentOperator_I", this.Procpara);

                Procpara1 = this.requestid + this.flag + agenterId + this.flag + this.operatorgroup + this.flag + 
                  this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + "0" + this.flag + -1 + 
                  this.flag + -1 + this.flag + "0" + this.flag + -1 + this.flag + this.groupdetailid;
                rst.executeProc("workflow_CurrentOperator_I", Procpara1);
              } else {
                this.Procpara = this.requestid + this.flag + opertor + this.flag + this.operatorgroup + this.flag + 
                  this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + "0" + this.flag + -1 + 
                  this.flag + -1 + this.flag + "0" + this.flag + -1 + this.flag + this.groupdetailid;
                rst.executeProc("workflow_CurrentOperator_I", this.Procpara);
              }
            }
            else if (isbeAgent)
            {
              this.Procpara = this.requestid + this.flag + opertor + this.flag + this.operatorgroup + this.flag + 
                this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + "2" + this.flag + this.nextnodeid + 
                this.flag + agenterId + this.flag + "1" + this.flag + showorder + this.flag + this.groupdetailid;
              rst.executeProc("workflow_CurrentOperator_I", this.Procpara);

              Procpara1 = this.requestid + this.flag + agenterId + this.flag + this.operatorgroup + this.flag + 
                this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + "0" + this.flag + this.nextnodeid + 
                this.flag + opertor + this.flag + "2" + this.flag + showorder + this.flag + this.groupdetailid;
              rst.executeProc("workflow_CurrentOperator_I", Procpara1);
            } else {
              this.Procpara = this.requestid + this.flag + opertor + this.flag + this.operatorgroup + this.flag + 
                this.workflowid + this.flag + this.workflowtype + this.flag + opertortype + this.flag + "0" + this.flag + this.nextnodeid + 
                this.flag + -1 + this.flag + "0" + this.flag + showorder + this.flag + this.groupdetailid;
              rst.executeProc("workflow_CurrentOperator_I", this.Procpara);
            }

            this.Procpara = (opertor + this.flag) + opertortype + this.flag + this.requestid;

            if (this.nextnodetype.equals("3")) {
              if (isbeAgent)
              {
                if ((!(operatorsWfEnd.contains(agenterId + "_" + opertortype))) && (!(this.userid + "_" + this.usertype.equals(agenterId + "_" + opertortype))))
                {
                  popmap = new HashMap();
                  popmap.put("userid", Integer.parseInt(agenterId));
                  popmap.put("type", "1");
                  popmap.put("logintype", opertortype);
                  popmap.put("requestid", this.requestid);
                  popmap.put("requestname", this.requestname);
                  popmap.put("workflowid", this.workflowid);
                  popmap.put("creater", this.creater);
                  poppuplist.add(popmap);
                  operatorsWfEnd.add(agenterId + "_" + opertortype);
                }

              }
              else if ((!(operatorsWfEnd.contains(opertor + "_" + opertortype))) && (!(this.userid + "_" + this.usertype.equals(opertor + "_" + opertortype))))
              {
                popmap = new HashMap();
                popmap.put("userid", opertor);
                popmap.put("type", "1");
                popmap.put("logintype", opertortype);
                popmap.put("requestid", this.requestid);
                popmap.put("requestname", this.requestname);
                popmap.put("workflowid", this.workflowid);
                popmap.put("creater", this.creater);
                poppuplist.add(popmap);
                operatorsWfEnd.add(opertor + "_" + opertortype);
              }

            }
            else if (isbeAgent) {
              if (!(operatorsWfNew.contains(agenterId + "_" + opertortype)))
              {
                popmap = new HashMap();
                popmap.put("userid", Integer.parseInt(agenterId));
                popmap.put("type", "0");
                popmap.put("logintype", opertortype);
                popmap.put("requestid", this.requestid);
                popmap.put("requestname", this.requestname);
                popmap.put("workflowid", this.workflowid);
                popmap.put("creater", this.creater);
                poppuplist.add(popmap);
                operatorsWfNew.add(agenterId + "_" + opertortype);
              }
            }
            else if (!(operatorsWfNew.contains(opertor + "_" + opertortype)))
            {
              popmap = new HashMap();
              popmap.put("userid", opertor);
              popmap.put("type", "0");
              popmap.put("logintype", opertortype);
              popmap.put("requestid", this.requestid);
              popmap.put("requestname", this.requestname);
              popmap.put("workflowid", this.workflowid);
              popmap.put("creater", this.creater);
              poppuplist.add(popmap);
              operatorsWfNew.add(opertor + "_" + opertortype);
            }

            if (this.SignType == 1) {
              if (this.nextnodeattr == 2)
                this.operatorgroup -= 1;
              else {
                this.operatorgroup += 1;
              }

            }

          }

          if (this.canflowtonextnode) {
            if (this.nextnodeattr != 2) {
              rst.executeSql("delete from workflow_nownode where requestid=" + this.requestid);
            }
            if ((this.innodeids.equals("")) || (this.innodeids.equals("0"))) this.innodeids = this.nodeid;
            rst.executeSql("delete from workflow_nownode where nownodeid in(" + this.innodeids + ") and requestid=" + this.requestid);
            rst.executeSql("delete from workflow_nownode where nownodeid=" + this.nodeid + " and requestid=" + this.requestid);
            rst.executeSql("insert into workflow_nownode(requestid,nownodeid,nownodetype,nownodeattribute) values(" + this.requestid + "," + this.nextnodeid + "," + this.nextnodetype + "," + this.nextnodeattr + ")");
          }
        }
      } catch (Exception exception) {
        throw exception;
      }
    }
    try {
      this.poppupRemindInfoUtil.insertPoppupRemindInfo(poppuplist);
    } catch (Exception exception2) {
      throw exception2;
    }
  }

  private int getNewRequestLogId(int userId, int requestid)
  {
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

  private int loadRequestLogId(String workflowid, int requestid, int requestLogId)
  {
    int logtype = 0;
    StringBuffer sb = new StringBuffer().append("SELECT LOGTYPE FROM ")
      .append("WFOpinionFieldData" + workflowid).append(" WHERE REQUESTID=")
      .append(requestid).append(" AND REQUESTLOGID=").append(
      requestLogId);
    RecordSet rs = new RecordSet();
    rs.executeSql(sb.toString());
    if (rs.next()) {
      logtype = rs.getInt("LOGTYPE");
    }
    return logtype;
  }

  private int getLogType()
  {
    int logtype = 0;
    if (this.src.equals("save")) {
      if (this.isremark == 1)
        logtype = 9;
      else if (this.isremark == -1)
        logtype = 2;
      else
        logtype = 1;
    }
    else if (this.src.equals("submit")) {
      if (this.isremark == 7)
        logtype = 9;
      else {
        logtype = 2;
      }
    }
    return logtype;
  }

  private boolean isExistTable(String tablename)
  {
    StringBuffer sb = new StringBuffer().append(
      "SELECT ID FROM WFOpinionTableNames ").append(" WHERE NAME='")
      .append(tablename).append("'");
    RecordSet rs = new RecordSet();
    rs.executeSql(sb.toString());

    return (!(rs.next()));
  }

  public String getUpLoadTypeForSelect(int workflowid)
  {
    int type = 0;
    String selectedCateLog = "";
    String result = "";
    StringBuffer sb = new StringBuffer().append(
      "select * from workflow_base ").append(" WHERE id=")
      .append(workflowid);
    RecordSet rs = new RecordSet();
    rs.executeSql(sb.toString());
    if (rs.next()) {
      type = Util.getIntValue(rs.getString("catelogType"), 0);

      selectedCateLog = Util.null2String(rs.getString("selectedCateLog"));
      result = selectedCateLog + "," + type;
    }
    return result;
  }

  public int getUpLoadType(int workflowid)
  {
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

  public boolean hasUsedType(int workflowid)
  {
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

  private List getFieldsName()
  {
    StringBuffer s = new StringBuffer();
    if (this.isbill == 1) {
      s.append("select t2.id, t2.fieldname, t2.fielddbtype, t2.fieldhtmltype, t2.type, t2.fieldlabel ");
      s.append("from  workflow_bill t1, workflow_billfield t2, workflow_form t3 ");
      s.append("where t1.id=t3.billformid and t2.billid = t1.id ");
      s.append("and t2.viewtype=0 and t3.requestid =" + this.requestid);
    }
    else {
      s.append("select t1.id, t1.fieldname, t1.fielddbtype, t1.fieldhtmltype, t1.type,");
      s.append("(select fieldlable from workflow_fieldlable t where t.langurageid = 7 and t.fieldid = t2.fieldid and t.formid = t2.formid) fieldNameCn,");
      s.append("(select fieldlable from workflow_fieldlable t where t.langurageid = 8 and t.fieldid = t2.fieldid and t.formid = t2.formid) fieldNameEn, ");
      s.append("(select fieldlable from workflow_fieldlable t where t.langurageid = 9 and t.fieldid = t2.fieldid and t.formid = t2.formid) fieldNameTw ");
      s.append("from workflow_formdict t1, workflow_formfield t2, workflow_form t3,workflow_fieldlable t4 ");
      s.append("where t1.id=t2.fieldid and t2.formid=t3.billformid and t4.langurageid = " + this.userlanguage + " and t4.fieldid = t2.fieldid and t4.formid = t2.formid and t3.requestid =" + this.requestid);
    }

    this.executesuccess = this.rs.executeSql(s.toString());
    if (!(this.executesuccess)) {
      writeLog(s.toString());
      saveRequestLog("1");
    }

    List fields = new ArrayList();
    while (this.rs.next()) {
      Track t = new Track();
      t.setFieldName(this.rs.getString("fieldname"));
      t.setFieldType(this.rs.getString("type"));
      t.setFieldHtmlType(this.rs.getString("fieldhtmltype"));
      t.setFieldId(this.rs.getInt("id"));

      if (this.isbill != 1) {
        t.setFieldNameCn(this.rs.getString("fieldNameCn"));
        t.setFieldNameEn(this.rs.getString("fieldNameEn"));
        t.setFieldNameTw(this.rs.getString("fieldNameTw"));
      }
      t.setNodeId(this.nodeid);
      t.setRequestId(this.requestid);
      t.setIsBill(this.isbill);

      if (this.isbill == 1)
        t.setFieldLableId(this.rs.getInt("fieldlabel"));
      t.setModifierIP(getIp());
      t.setOptKind(this.src);
      fields.add(t);
    }
    return fields;
  }

  public String getIp()
  {
    String ip = "";
    if (this.isRequest) {
      if (this.request != null)
        ip = Util.null2String(this.request.getRemoteAddr());
    }
    else if (this.fu != null) {
      ip = Util.null2String(this.fu.getRemoteAddr());
    }
    return ip;
  }

  private void SendRemindMail(String mailtoaddress, String mailobject, String mailrequestname)
  {
    try
    {
      mailtoaddress = mailtoaddress.substring(0, mailtoaddress.length() - 1);
      SendMail sm = new SendMail();
      SystemComInfo systemComInfo = new SystemComInfo();
      String defmailserver = systemComInfo.getDefmailserver();
      String defneedauth = systemComInfo.getDefneedauth();
      String defmailuser = systemComInfo.getDefmailuser();
      String defmailpassword = systemComInfo.getDefmailpassword();
      String defmailfrom = systemComInfo.getDefmailfrom();
      sm.setMailServer(defmailserver);
      if (defneedauth.equals("1")) {
        sm.setNeedauthsend(true);
        sm.setUsername(defmailuser);
        sm.setPassword(defmailpassword);
      } else {
        sm.setNeedauthsend(false);
      }

      sm.sendhtml(defmailfrom, mailtoaddress, null, null, mailobject, mailrequestname, 3, "3");
    } catch (Exception e) {
      writeLog(e);
    }
  }

  public void getBillId()
  {
    if (this.isbill == 1) {
      this.rs.executeSql("select * from workflow_bill where id = " + this.formid);
      this.rs.next();
      String tablename = Util.null2String(this.rs.getString("tablename"));
      if (!(tablename.equals(""))) {
        this.rs.executeSql("select * from " + tablename + " where requestid = " + this.requestid);
        if (this.rs.next())
          this.billid = this.rs.getInt("id");
      }
    }
  }

  public boolean getModeid(int curworkflowid, int curnodeid, int curformid, int curisbill)
  {
    RecordSet currs = new RecordSet();
    String ismode = "";
    int modeid = 0;
    int showdes = 0;
    currs.executeSql("select ismode,showdes from workflow_flownode where workflowid=" + curworkflowid + " and nodeid=" + curnodeid);
    if (currs.next()) {
      ismode = Util.null2String(currs.getString("ismode"));
      showdes = Util.getIntValue(Util.null2String(currs.getString("showdes")), 0);
    }
    if ((ismode.equals("1")) && (showdes != 1)) {
      currs.executeSql("select id from workflow_nodemode where isprint='0' and workflowid=" + curworkflowid + " and nodeid=" + curnodeid);
      if (currs.next()) {
        modeid = currs.getInt("id");
      } else {
        currs.executeSql("select id from workflow_formmode where isprint='0' and formid=" + curformid + " and isbill='" + curisbill + "'");
        if (currs.next()) {
          modeid = currs.getInt("id");
        }
      }
    }
    return (modeid > 0);
  }

  public String getDocrowindex()
  {
    return this.docrowindex;
  }

  public void setDocrowindex(String docrowindex)
  {
    this.docrowindex = docrowindex;
  }

  private void updatePoppupRemindInfoThisJava(RecordSetTrans rst, int requestId) throws Exception {
    String remindusers = "";
    String usertypes = "";
    rst.executeSql("select wfreminduser,wfusertypes from workflow_currentoperator where (isremark='0' or isremark='7') and requestid=" + this.requestid);
    if (rst.next()) {
      remindusers = rst.getString("wfreminduser");
      usertypes = rst.getString("wfusertypes");
    }
    ArrayList wfremindusers = Util.TokenizerString(remindusers, ",");
    ArrayList wfusertypes = Util.TokenizerString(usertypes, ",");
    rst.executeSql("select userid,usertype from workflow_currentoperator where requestid=" + this.requestid + " group by userid,usertype");
    while (rst.next()) {
      String tempuserid = rst.getString("userid");
      String tempusertype = rst.getString("usertype");
      if (wfremindusers.indexOf(tempuserid) < 0) {
        wfremindusers.add(tempuserid);
        wfusertypes.add(tempusertype);
      }
    }
    for (int i = 0; i < wfremindusers.size(); ++i) {
      this.poppupRemindInfoUtil.updatePoppupRemindInfo(Util.getIntValue((String)wfremindusers.get(i)), 10, (String)wfusertypes.get(i), this.requestid);
      this.poppupRemindInfoUtil.updatePoppupRemindInfo(Util.getIntValue((String)wfremindusers.get(i)), 0, (String)wfusertypes.get(i), this.requestid);
    }
  }

  private void updateworkflowcurrentoperator(RecordSetTrans rst, ArrayList submituserids, ArrayList submitusertypes, String finishndgpids) throws Exception {
    rst.executeSql("select distinct userid,usertype from workflow_currentoperator where isremark = '" + this.isremark + "' and requestid=" + this.requestid + "and nodeid=" + this.nodeid + " and groupid in(select distinct groupid from workflow_currentoperator where isremark = '" + this.isremark + "' and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + " and nodeid=" + this.nodeid + ")");
    while (rst.next()) {
      submituserids.add(rst.getString("userid"));
      submitusertypes.add(rst.getString("usertype"));
    }

    List tmpgroupidList = new ArrayList();

    if (!("".equals(finishndgpids)))
      rst.executeSql("select distinct groupid from workflow_currentoperator where isremark = '" + this.isremark + "' and requestid=" + this.requestid + " and nodeid=" + this.nodeid + " and (groupdetailid in (SELECT id FROM workflow_groupdetail WHERE groupid in (" + finishndgpids + ")) OR (userid=" + this.userid + " and usertype=" + this.usertype + "))");
    else {
      rst.executeSql("select distinct groupid from workflow_currentoperator where isremark = '" + this.isremark + "' and requestid=" + this.requestid + " and userid=" + this.userid + " and usertype=" + this.usertype + " and nodeid=" + this.nodeid);
    }

    while (rst.next()) {
      tmpgroupidList.add(Util.getIntValue(rst.getString(1), 0));
    }
    for (int i = 0; i < tmpgroupidList.size(); ++i) {
      int tmpgroupid = Util.getIntValue((String)tmpgroupidList.get(i), 0);
      if (!("0".equals(this.needwfback)))
      {
        workflow_CurOpe_UpdatebySubmit(rst, this.requestid, this.nodeid, this.userid, this.currentdate, this.currenttime, tmpgroupid, this.isremark);
        rst.executeSql("update workflow_currentoperator set operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where (isremark = '5' or isremark='0' or isremark='1' or isremark='8' or isremark='9' or isremark='7') and requestid =" + this.requestid + " and userid=" + this.userid + " and nodeid=" + this.nodeid + " and groupid=" + tmpgroupid);
        updateworkflowcurrenttakingopsoperator(rst, tmpgroupid);
      }
      else {
        workflow_CurOpe_UbySubmitNB(rst, this.requestid, this.nodeid, this.userid, this.currentdate, this.currenttime, tmpgroupid, this.isremark);
        rst.executeSql("update workflow_currentoperator set operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where (isremark = '5' or isremark='0' or isremark='1' or isremark='8' or isremark='9' or isremark='7') and requestid =" + this.requestid + " and userid=" + this.userid + " and nodeid=" + this.nodeid + " and groupid=" + tmpgroupid);
        updateworkflowcurrenttakingopsoperator(rst, tmpgroupid);
      }
    }
  }

  private void updateworkflowcurrenttakingopsoperator(RecordSetTrans rst, int tmpgroupid) throws Exception
  {
    rst.executeSql("update workflow_currentoperator set viewtype=-2 , isremark=2 ,  operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where (isremark = '1' and takisremark='2') and requestid =" + this.requestid + " and nodeid=" + this.nodeid + " and groupid=" + tmpgroupid);
  }

  private void updateworkflowcurrenttakingopsoperator(RecordSetTrans rst, String nodeids, int tmpgroupid)
    throws Exception
  {
    rst.executeSql("update workflow_currentoperator set viewtype=-2 , isremark=2 ,  operatedate='" + this.currentdate + "',operatetime='" + this.currenttime + "' where (isremark = '1' and takisremark='2') and requestid =" + this.requestid + " and nodeid in (" + nodeids + ") and groupid=" + tmpgroupid);
  }

  private static void workflow_CurOpe_UpdatebySubmit(RecordSetTrans rst, int requestid, int nodeid, int userid, String currentdate, String currenttime, int tmpgroupid, int isremark)
    throws Exception
  {
    String upSql = "update workflow_currentoperator set operatedate ='" + currentdate + "', operatetime='" + currenttime + "', viewtype=-2 " + 
      " where requestid=" + requestid + 
      " and userid=" + userid + " and isremark='" + isremark + "' " + " and groupid=" + tmpgroupid + " and nodeid=" + nodeid;
    rst.executeSql(upSql);

    upSql = "update workflow_currentoperator set isremark = '2' where requestid = " + requestid + " and isremark='" + isremark + "' and groupid=" + tmpgroupid + " and nodeid=" + nodeid;
    rst.executeSql(upSql);
    WFLinkInfo wflinkinfo = new WFLinkInfo();
    int nodeattr = wflinkinfo.getNodeAttribute(nodeid);

    if (nodeattr == 2)
      upSql = " update workflow_currentoperator set isremark = '2'  where requestid = " + 
        requestid + 
        " and (isremark = '5' or isremark = '8' or isremark = '9') and userid = " + userid + " and nodeid =" + nodeid;
    else {
      upSql = "update workflow_currentoperator set isremark = '2' where requestid = " + 
        requestid + " and (isremark = '5' or isremark = '8' or isremark = '9') and userid=" + userid;
    }
    rst.executeSql(upSql);
  }

  private static void workflow_CurOpe_UbySubmitNB(RecordSetTrans rst, int requestid, int nodeid, int userid, String currentdate, String currenttime, int tmpgroupid, int isremark)
    throws Exception
  {
    String upSql = "update workflow_currentoperator set operatedate ='" + currentdate + "', operatetime ='" + currenttime + "', viewtype=-2" + 
      " where requestid=" + requestid + 
      " and userid = " + userid + 
      " and isremark = '" + isremark + "' " + 
      " and groupid = " + tmpgroupid + 
      " and nodeid = " + nodeid;
    rst.executeSql(upSql);

    upSql = "update workflow_currentoperator set isremark = '2', needwfback = '0' where requestid = " + 
      requestid + 
      " and isremark = '" + isremark + "' " + 
      " and groupid = " + tmpgroupid + 
      " and nodeid = " + nodeid;
    rst.executeSql(upSql);

    WFLinkInfo wflinkinfo = new WFLinkInfo();
    int nodeattr = wflinkinfo.getNodeAttribute(nodeid);

    if (nodeattr == 2)
      upSql = " update workflow_currentoperator set isremark = '2', needwfback = '0' where requestid = " + 
        requestid + 
        " and (isremark = '5' or isremark = '8' or isremark = '9') " + 
        " and userid = " + userid + 
        " and nodeid = " + nodeid;
    else {
      upSql = " update workflow_currentoperator set isremark = '2', needwfback = '0' where requestid = " + 
        requestid + 
        " and (isremark = '5' or isremark = '8' or isremark = '9')" + 
        " and userid = " + userid;
    }
    rst.executeSql(upSql);
  }

  private static void workflow_CurOpe_UbyForward(RecordSetTrans rst, int requestid, int userid, int usertype, String currentdate, String currenttime)
    throws Exception
  {
    String upSql = "update workflow_currentoperator set isremark = 2, operatedate = '" + currentdate + "', operatetime = '" + currenttime + "'" + 
      " where requestid = " + requestid + " and userid =" + userid + 
      " and usertype = " + usertype + " and (isremark = 1 or isremark = 8 or isremark = 9)";
    rst.executeSql(upSql);
  }

  private static void workflow_CurOpe_UbyForwardNB(RecordSetTrans rst, int requestid, int userid, int usertype, String currentdate, String currenttime)
    throws Exception
  {
    String upSql = " update workflow_currentoperator set isremark='2', operatedate = '" + currentdate + "', operatetime = '" + currenttime + "', needwfback  = '0'" + 
      " where requestid = " + requestid + 
      " and userid = " + userid + 
      " and usertype = " + usertype + 
      " and (isremark = '1' or isremark = '8' or isremark = '9')";

    rst.executeSql(upSql); }

  public void setRejectToNodeid(int rejectToNodeid) {
    this.RejectToNodeid = rejectToNodeid;
  }

  public void setRejectToType(int RejectToType) {
    this.RejectToType = RejectToType;
  }

  public void setSubmitToNodeid(int submitToNodeid) {
    this.SubmitToNodeid = submitToNodeid;
  }

  public String getClientType() {
    return this.clientType;
  }

  public void setClientType(String clientType) {
    this.clientType = clientType;
  }

  public int getHandWrittenSign() {
    return this.handWrittenSign;
  }

  public void setHandWrittenSign(int handWrittenSign) {
    this.handWrittenSign = handWrittenSign;
  }

  public int getSpeechAttachment() {
    return this.speechAttachment;
  }

  public void setSpeechAttachment(int speechAttachment) {
    this.speechAttachment = speechAttachment; }

  public int getEnableIntervenor() {
    return this.enableIntervenor;
  }

  public void setEnableIntervenor(int enableIntervenor) {
    this.enableIntervenor = enableIntervenor;
  }

  public String fillFullNull(String obj)
  {
    if ("".equals(obj.trim())) {
      return "";
    }
    return obj;
  }

  public String getSignatureAppendfix()
  {
    return this.signatureAppendfix;
  }

  public void setSignatureAppendfix(String signatureAppendfix) {
    this.signatureAppendfix = signatureAppendfix; }

  public String getChatsType() {
    return this.chatsType;
  }

  public void setChatsType(String chatsType) {
    if ((chatsType == null) || (chatsType.equals(""))) {
      chatsType = "0";
    }
    this.chatsType = chatsType;
  }

  private String[] parseArgument(String s, char separator)
  {
    int i = 0;
    int j = 0;
    if (s.trim().equals("")) {
      args = new String[0];
      return args;
    }
    for (j = 0; j < s.length(); ++j) {
      if (s.charAt(j) == separator)
        ++i;
    }
    String[] args = new String[i + 1];
    j = 0;
    i = 0;
    while ((j = s.indexOf(separator)) != -1)
    {
      args[(i++)] = s.substring(0, j);
      s = s.substring(j + 1);
    }
    args[i] = s;
    return args;
  }

  public String execRequestlog(String rstlogpara, RecordSet rslog, char flag, String remarknew)
  {
    String returnStr = "";
    if (!("".equals(rstlogpara)))
    {
      String[] arraypara = rstlogpara.split(String.valueOf(flag));
      if (arraypara.length < 22) {
        arraypara = parseArgument(rstlogpara, flag);
      }

      String requestidlog = arraypara[0];
      String workflowidlog = arraypara[1];
      String nodeidlog = arraypara[2];
      String logtypelog = arraypara[3];
      String logdatelog = arraypara[4];
      String logtimelog = arraypara[5];
      String useridlog = arraypara[6];

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

      String receivedPersonids = "";
      String remarkLocation = "";
      if (arraypara.length == 23) {
        receivedPersonids = arraypara[21];
        remarkLocation = arraypara[22];
      } else if (arraypara.length == 22) {
        receivedPersonids = arraypara[21];
      }

      int count2 = 0;
      Date date = new Date();
      SimpleDateFormat sdfdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      String currentStr = sdfdate.format(date);
      String currentdate = "";
      String currenttime = "";
      if (logdatelog.equals("")) {
        currentdate = currentStr.substring(0, 10).trim();
        currenttime = currentStr.substring(10).trim();
      } else {
        currentdate = logdatelog;
        currenttime = logtimelog;
      }
      returnStr = currentdate + "~~current~~" + currenttime;
      int operatorDept1 = Util.getIntValue(this.resourceComInfo.getDepartmentID(useridlog), 0);
      ConnStatement logstatement = null;
      try {
        if (("0".equals(usertypelog)) || ("1".equals(usertypelog))) {
          if ("1".equals(logtypelog)) {
            String logsql = " SELECT 1 FROM workflow_requestlog WHERE requestid=" + requestidlog + 
              " AND nodeid=" + nodeidlog + " AND logtype='" + logtypelog + "' AND OPERATOR = " + useridlog + 
              " AND operatortype = " + usertypelog;
            rslog.executeSql(logsql);
            int count1 = rslog.getCounts();
            if (count1 > 0) {
              String updatelogsql = " UPDATE workflow_requestlog SET operatedate= ?,operatetime= ?, remark= ?, clientip= ?, destnodeid= ?, annexdocids= ?,  requestLogId= ?, signdocids= ?, signworkflowids= ?, isMobile= ?, SpeechAttachment= ?, HandWrittenSign= ?, remarkLocation=?  where requestid=" + 
                requestidlog + 
                " AND nodeid=" + nodeidlog + " AND logtype='" + logtypelog + "' AND OPERATOR = " + useridlog + 
                " AND operatortype = " + usertypelog;
              logstatement = new ConnStatement();
              logstatement.setStatementSql(updatelogsql);
              logstatement.setString(1, currentdate);
              logstatement.setString(2, currenttime);
              logstatement.setString(3, remarknew);
              logstatement.setString(4, clientiplog);
              logstatement.setInt(5, Integer.parseInt(nodeidlog));
              logstatement.setString(6, annexdocidslog);
              logstatement.setInt(7, Util.getIntValue(requestLogIdlog, 0));
              logstatement.setString(8, signdocidslog);
              logstatement.setString(9, signworkflowidslog);
              logstatement.setString(10, clientTypelog);
              logstatement.setInt(11, Util.getIntValue(speechAttachmentlog, 0));
              logstatement.setInt(12, Util.getIntValue(handWrittenSignlog, 0));
              logstatement.setString(13, remarkLocation);
              logstatement.executeUpdate();
            } else {
              String insertlogsql = " INSERT INTO workflow_requestlog (requestid,workflowid,nodeid,logtype, operatedate,  operatetime,OPERATOR, remark,clientip,operatortype,destnodeid,receivedPersons,  agentorbyagentid,agenttype,showorder,annexdocids,requestLogId,operatorDept,  signdocids,signworkflowids,isMobile,HandWrittenSign,SpeechAttachment,receivedPersonids,remarkLocation)  VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

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
              logstatement.setInt(17, Util.getIntValue(requestLogIdlog, 0));
              logstatement.setString(18, String.valueOf(operatorDept1));
              logstatement.setString(19, signdocidslog);
              logstatement.setString(20, signworkflowidslog);
              logstatement.setString(21, clientTypelog);
              logstatement.setInt(22, Util.getIntValue(handWrittenSignlog, 0));
              logstatement.setInt(23, Util.getIntValue(speechAttachmentlog, 0));
              logstatement.setString(24, receivedPersonids);
              logstatement.setString(25, remarkLocation);
              logstatement.executeUpdate();
            }
          } else {
            String deletelogsql = " DELETE workflow_requestlog WHERE requestid=" + requestidlog + " AND nodeid=" + nodeidlog + 
              " AND (logtype='1') AND OPERATOR = " + useridlog + " AND operatortype = " + usertypelog;
            rslog.executeSql(deletelogsql);
            String insertlog = " INSERT INTO workflow_requestlog (requestid,workflowid,nodeid,logtype, operatedate,  operatetime,OPERATOR, remark,clientip,operatortype,destnodeid,receivedPersons,  agentorbyagentid,agenttype,showorder,annexdocids,requestLogId,operatorDept,  signdocids,signworkflowids,isMobile,HandWrittenSign,SpeechAttachment,receivedPersonids,remarkLocation)  VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

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
            logstatement.setInt(17, Util.getIntValue(requestLogIdlog, 0));
            logstatement.setString(18, String.valueOf(operatorDept1));
            logstatement.setString(19, signdocidslog);
            logstatement.setString(20, signworkflowidslog);
            logstatement.setString(21, clientTypelog);
            logstatement.setInt(22, Util.getIntValue(handWrittenSignlog, 0));
            logstatement.setInt(23, Util.getIntValue(speechAttachmentlog, 0));
            logstatement.setString(24, receivedPersonids);
            logstatement.setString(25, remarkLocation);
            logstatement.executeUpdate();
          }

        }

        if ((!("s".equals(logtypelog))) && (!("9".equals(logtypelog))) && 
          (!("2".equals(logtypelog))) && (!("3".equals(logtypelog))) && 
          (!("4".equals(logtypelog))) && (!("5".equals(logtypelog))) && 
          (!("6".equals(logtypelog))) && (!("e".equals(logtypelog))) && 
          (!("0".equals(logtypelog))) && (!("t".equals(logtypelog))) && 
          (!("i".equals(logtypelog))) && (!("7".equals(logtypelog))) && 
          (!("h".equals(logtypelog))) && (!("a".equals(logtypelog))) && 
          (!("b".equals(logtypelog))) && (!("j".equals(logtypelog))))
          break label1913;
        String rsql = "select currentnodeid,currentnodetype from workflow_requestbase where requestid = " + requestidlog;
        rslog.executeSql(rsql);
        String curnodeid = "";
        String curnodetype = "";
        if (rslog.next()) {
          curnodeid = rslog.getString("currentnodeid");
          curnodetype = rslog.getString("currentnodetype");
        }
        int nodeattr = this.wflinkinfo.getNodeAttribute(Util.getIntValue(curnodeid, -1));
        Set branchNodeSet = new HashSet();
        if (nodeattr == 2) {
          String branchnodes = "";
          branchnodes = this.wflinkinfo.getNowNodeids(Util.getIntValue(requestidlog, -1));
          if (!("".equals(branchnodes))) {
            String[] strs = branchnodes.split(",");
            for (int k = 0; k < strs.length; ++k) {
              String nodestr = strs[k];
              if (!("-1".equals(nodestr))) {
                branchNodeSet.add(nodestr);
              }
            }
          }

        }

        if (("3".equals(curnodetype)) || ((!(curnodeid.equals(nodeidlog))) && (!(branchNodeSet.contains(nodeidlog))))) break label1913;
        logstatement.close();
        String logsql = " select logid from workflow_requestlog where workflowid = " + 
          workflowidlog + 
          " and nodeid = " + 
          nodeidlog + 
          " and logtype = '" + 
          logtypelog + 
          "' and requestid = " + 
          requestidlog + 
          " and operatedate = '" + 
          currentdate + 
          "' and operatetime = '" + 
          currenttime + 
          "' and operator = " + useridlog;

        rslog.executeSql(logsql);
        int logid = -1;
        if (rslog.next()) {
          logid = rslog.getInt("logid");
        }

        int loguserid = Util.getIntValue(useridlog, 0);
        if ("s".equals(logtypelog)) {
          loguserid = -1;
        }

        String rightSql = "insert into workflow_logviewusers (logid,userid) values (? , ?)";
        logstatement.close();
        logstatement = new ConnStatement();
        logstatement.setStatementSql(rightSql);
        logstatement.setInt(1, logid);
        logstatement.setInt(2, loguserid);
        label1913: logstatement.executeUpdate();
      }
      catch (Exception e)
      {
        writeLog(e);
      } finally {
        if (logstatement != null) logstatement.close();
      }
    }
    return returnStr;
  }

  private String resourceAuthorityFilter(String fieldhtmltype, String fieldtype, String resstr)
  {
    String result = resstr;

    if ("".equals(resstr)) {
      return result;
    }

    if ("3".equals(fieldhtmltype)) {
      if ((fieldtype.equals("9")) || (fieldtype.equals("37")))
      {
        new DocShareUtil(); result = DocShareUtil.docRightFilterForRequestId(this.user, resstr, this.requestid);
      } else if ((fieldtype.equals("8")) || (fieldtype.equals("135"))) {
        result = new CommonShareManager().getPrjFilterids(resstr, this.user);
      } else if (fieldtype.equals("23")) {
        result = new CommonShareManager().getCptFilterids(resstr, this.user);
      } else if ((fieldtype.equals("7")) || (fieldtype.equals("18"))) {
        result = CustomerShareUtil.customerRightFilter(String.valueOf(this.user.getUID()), resstr);
      }
    }
    return result;
  }

  public static String filterClause(String causes) {
    String returnval = "";
    Map tempMap = new HashMap();
    StringTokenizer tokens = new StringTokenizer(causes, ",");
    while (tokens.hasMoreElements()) {
      String setKV = tokens.nextToken();
      if (tempMap.get(setKV) == null)
        returnval = returnval + setKV + ",";
      tempMap.put(setKV, setKV);
    }
    return returnval.substring(0, returnval.length() - 1);
  }

  private String rePlaceWordMark(String content)
  {
    String text = content;
    String mark = "xml:namespace";
    int xmlIdx_b = text.indexOf(mark);
    if (xmlIdx_b > -1) {
      String str_f = text.substring(0, xmlIdx_b);
      str_f = str_f.substring(0, str_f.lastIndexOf("<"));
      String str_e = text.substring(xmlIdx_b + mark.length());
      int xmlIdx_e = str_e.indexOf(mark);
      if (xmlIdx_e > -1) {
        str_e = str_e.substring(xmlIdx_e + mark.length());
        str_e = str_e.substring(str_e.indexOf(">") + 1);
        text = str_f + str_e;
      }
    }
    return text;
  }

  public void createEh_operatorMap_pc()
  {
    String eh_setoperator = "";
    if ((this.isRequest) && (this.request != null))
      eh_setoperator = Util.null2String(this.request.getParameter("eh_setoperator"));
    else if (this.fu != null)
      eh_setoperator = Util.null2String(this.fu.getParameter("eh_setoperator"));
    this.eh_operatorMap.put("eh_setoperator", eh_setoperator);
    String eh_relationship = "";
    if ((this.isRequest) && (this.request != null))
      eh_relationship = Util.null2String(this.request.getParameter("eh_relationship"));
    else if (this.fu != null)
      eh_relationship = Util.null2String(this.fu.getParameter("eh_relationship"));
    this.eh_operatorMap.put("eh_relationship", eh_relationship);
    String eh_operators = "";
    if ((this.isRequest) && (this.request != null))
      eh_operators = Util.null2String(this.request.getParameter("eh_operators"));
    else if (this.fu != null)
      eh_operators = Util.null2String(this.fu.getParameter("eh_operators"));
    this.eh_operatorMap.put("eh_operators", eh_operators);
  }

  public boolean checkNodeOperatorComment(int requestid, int userid, int nodeid)
  {
    return false;
  }

  public void createEh_operatorMap_mobile(Map<String, Object> _eh_operatorMap)
  {
    this.eh_operatorMap = _eh_operatorMap;
  }

  public String getIsFirstSubmit() {
    return this.isFirstSubmit;
  }

  public void setIsFirstSubmit(String isFirstSubmit) {
    this.isFirstSubmit = isFirstSubmit;
  }

  public String getRemarkLocation() {
    return this.remarkLocation;
  }

  public void setRemarkLocation(String remarkLocation) {
    this.remarkLocation = remarkLocation;
  }

  public void removeErrorMsg()
  {
    if (!(this.isRequest)) {
      this.request = this.fu.getRequest();
    }
    if (this.request != null) {
      HttpSession session = this.request.getSession(false);
      session.removeAttribute("errormsg_" + this.user.getUID() + "_" + this.requestid);
      session.removeAttribute("errormsgid_" + this.user.getUID() + "_" + this.requestid);
    }
  }

  private boolean wfCurrentOperatorAgent(RecordSetTrans rst, String opertor, String operatorgroup, String opertortype, String agenterId, int showorder)
  {
    boolean returnFlag = false;
    try
    {
      if ((this.requestid > 0) && (!("".equals(opertor))))
      {
        String execSql = " select * from workflow_currentoperator where requestid = ? ";
        execSql = execSql + " AND userid = ? ";
        execSql = execSql + " AND usertype = ? ";
        execSql = execSql + " AND nodeid = ? ";
        execSql = execSql + " AND isremark = 0 ";
        execSql = execSql + " AND agenttype = 2 ";
        rst.executeQuery(execSql, new Object[] { Integer.valueOf(this.requestid), opertor, opertortype, Integer.valueOf(this.nextnodeid) });
        if (rst.next()) {
          String workflowtype2 = "0";

          if ((this.workflowtype == null) || (this.workflowtype.equals(""))) {
            execSql = "SELECT workflowtype FROM workflow_base WHERE id = ? ";
            rst.executeQuery(execSql, new Object[] { Integer.valueOf(this.workflowid) });
            if (rst.next())
              workflowtype2 = rst.getString("workflowtype");
          }
          else {
            workflowtype2 = this.workflowtype;
          }

          execSql = "";
          execSql = execSql + "        INSERT INTO workflow_currentoperator  ";
          execSql = execSql + "          (requestid,    ";
          execSql = execSql + "           userid,   ";
          execSql = execSql + "           groupid,  ";
          execSql = execSql + "           workflowid,   ";
          execSql = execSql + "           workflowtype,     ";
          execSql = execSql + "           usertype,     ";
          execSql = execSql + "           isremark,     ";
          execSql = execSql + "           nodeid,   ";
          execSql = execSql + "           agentorbyagentid,     ";
          execSql = execSql + "           agenttype,    ";
          execSql = execSql + "           showorder,    ";
          execSql = execSql + "           receivedate,  ";
          execSql = execSql + "           receivetime,  ";
          execSql = execSql + "           viewtype,     ";
          execSql = execSql + "           iscomplete,   ";
          execSql = execSql + "           islasttimes,  ";
          execSql = execSql + "           groupdetailid,    ";
          execSql = execSql + "           preisremark,  ";
          execSql = execSql + "           needwfback)   ";
          execSql = execSql + "        VALUES   ";
          execSql = execSql + "          (" + this.requestid + ",   ";
          execSql = execSql + "           " + opertor + ",  ";
          execSql = execSql + "           " + operatorgroup + ",     ";
          execSql = execSql + "           " + this.workflowid + ",  ";
          execSql = execSql + "           " + workflowtype2 + ",    ";
          execSql = execSql + "           " + opertortype + ",    ";
          execSql = execSql + "           2,    ";
          execSql = execSql + "           " + this.nextnodeid + ",   ";
          execSql = execSql + "           " + agenterId + ",     ";
          execSql = execSql + "           1,    ";
          execSql = execSql + "           " + showorder + ",    ";
          execSql = execSql + "           '" + this.currentdate + "',  ";
          execSql = execSql + "           '" + this.currenttime + "',  ";
          execSql = execSql + "           0,    ";
          execSql = execSql + "           0,    ";
          execSql = execSql + "           0,    ";
          execSql = execSql + "           " + this.groupdetailid + ",  ";
          execSql = execSql + "           2,    ";
          execSql = execSql + "           '1')     ";
          rst.executeSql(execSql);
          returnFlag = true;
          break label1351: }
        return false;
      }

      return false;
    }
    catch (Exception e) {
      return false;
    }
    label1351: return returnFlag;
  }

  public void setMakeOperateLog(boolean isMakeOperateLog) {
    this.isMakeOperateLog = isMakeOperateLog;
  }

  public void CheckUserIsLasttimes(int requestid, int currentnodeid, User user) {
    RecordSet rs = new RecordSet();
    try {
      String sql = "select id from workflow_currentoperator where requestid=" + requestid + " and nodeid=" + currentnodeid + " and userid=" + user.getUID() + " and isremark=2 and islasttimes=1 and preisremark=0 ";
      System.out.println(sql);
      rs.executeSql(sql);
      if (rs.next()) {
        int tempid = rs.getInt("id");
        int otempid = 0;
        sql = "select c.id from workflow_currentoperator c where c.requestid=" + requestid + " and c.nodeid!=" + currentnodeid + " and c.nodeid in (select nownodeid from workflow_nownode where requestid=c.requestid) and userid=" + user.getUID() + " and c.isremark=0 and c.islasttimes=0 and c.preisremark=0 order by c.id";
        rs.executeSql(sql);
        if (rs.next()) {
          otempid = rs.getInt("id");
        }
        System.out.println("tempid = " + tempid + " otempid=" + otempid);
        if (otempid > 0) {
          rs.executeSql("update workflow_currentoperator set islasttimes=0 where requestid=" + requestid + " and id=" + tempid);
          rs.executeSql("update workflow_currentoperator set islasttimes=1 where requestid=" + requestid + " and id=" + otempid);
        }
      }
    }
    catch (Exception localException) {
    }
  }

  public Map<String, Integer> getNewAddDetailRowPerInfo() {
    return this.newAddDetailRowPerInfo;
  }

  private static void init()
  {
    try
    {
      isExeOldFlowlogic = Util.getIntValue(new BaseBean().getPropValue("workflowFlowLogicConfig", "oldlogic"), 0) == 1;
    }
    catch (Exception localException)
    {
    }
  }
}