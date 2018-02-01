package weaver.file;

/**
 * Title:
 * Description:  ,2004-6-25:增加了是否记录下载次数到数据库
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author     ,Charoes Huang
 * @version 1.0,2004-6-25
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;

import weaver.WorkPlan.WorkPlanService;
import weaver.alioss.AliOSSObjectManager;
import weaver.blog.BlogDao;
import weaver.conn.RecordSet;
import weaver.cowork.CoworkDAO;
import weaver.docs.category.SecCategoryComInfo;
import weaver.docs.docs.DocManager;
import weaver.general.BaseBean;
import weaver.general.GCONST;
import weaver.general.Util;
import weaver.file.constant.FileConstant;
import weaver.file.right.FileRightManager;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import weaver.hrm.resource.ResourceComInfo;
import weaver.meeting.MeetingUtil;
import weaver.splitepage.operate.SpopForDoc;
import weaver.system.SystemComInfo;
import weaver.systeminfo.SystemEnv;
import weaver.voting.VotingManager;
import weaver.workflow.request.RequestAnnexUpload;
import weaver.workflow.request.WFUrgerManager;
import weaver.worktask.worktask.WTRequestUtil;
import DBstep.iMsgServer2000;
import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipOutputStream;
import weaver.crm.CrmShareBase;
import weaver.social.service.SocialIMService;
import gvo.passwd.GvoServiceFile;
import com.weaver.formmodel.util.StringHelper;
import weaver.formmode.setup.ModeRightInfo;

public class FileDownload-old extends HttpServlet {
    /**
     * 是否需要记录下载次数
     */
    private boolean isCountDownloads = false;
    private String agent = "";
	private  List fileNameEcoding ;
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {    
        String type = Util.null2String(req.getParameter("type"));
		GvoServiceFile gsf = new GvoServiceFile();
        if(!"netimag".equals(type)){
        String clientEcoding = "GBK";
        try
        {
            String acceptlanguage = req.getHeader("Accept-Language");
            if(!"".equals(acceptlanguage))
                acceptlanguage = acceptlanguage.toLowerCase();
            if(acceptlanguage.indexOf("zh-tw")>-1||acceptlanguage.indexOf("zh-hk")>-1)
            {
                clientEcoding = "BIG5";
            }
            else
            {
                clientEcoding = "GBK";
            }
        }
        catch(Exception e)
        {
            
        }
        
        agent = req.getHeader("user-agent");
        
        String downloadBatch = Util.null2String(req.getParameter("downloadBatch"));
        //只下载附件，而不是下载文档的附件
        String onlydownloadfj = Util.null2String(req.getParameter("onlydownloadfj"));
        /*==============td26423 流程中批量下载     start=========================================================*/
        if(downloadBatch!=null &&"1".equals(downloadBatch)){//批量下载---传入的是文档的ids
            //req.setCharacterEncoding("ISO8859-1");     
		String ipstring = req.getRemoteAddr();
        String currentDateTime= new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());          
            String docids = Util.null2String(req.getParameter("fieldvalue"));//docids 以逗号隔开的
            String displayUsage = Util.null2String(req.getParameter("displayUsage"));
            String tempFlag1="";
            if(docids!=null&&!"".equals(docids)){
                tempFlag1=docids.substring(docids.length()-1);
            }
            if(tempFlag1.equals(",")) docids=docids.substring(0,docids.length()-1);
            String delImgIds=Util.null2String(req.getParameter("delImgIds"));//imgeFileids 以逗号隔开的
            String tempFlag="";
            if(delImgIds!=null &&!"".equals(delImgIds)){
                tempFlag=delImgIds.substring(delImgIds.length()-1);
            }
            if(tempFlag.equals(",")) delImgIds=delImgIds.substring(0,delImgIds.length()-1);
            
            String docSearchFlag=Util.null2String(req.getParameter("docSearchFlag"));//docSearchFlag ='1-标记为从查询文档中来批量下载多文档的多附件'
            
            String requestname ="";
            //String requestname = Util.null2String(req.getParameter("requestname"));//requestname 流程标题
            //requestname =new String(requestname.getBytes("UTF-8"));
            RecordSet rsimagefileid =new RecordSet();
            String requestid = Util.null2String(req.getParameter("requestid"));//requestname 流程标题
            String sqlrequestname="select requestname from workflow_requestbase where requestid='"+requestid+"'";
            rsimagefileid.executeSql(sqlrequestname);
            if(rsimagefileid.next()){
                requestname=Util.null2String(rsimagefileid.getString("requestname"));
            }
            requestname = Util.StringReplace(requestname,"/","／");
            String download = Util.null2String(req.getParameter("download"));
            
            User loginuser = (User)req.getSession(true).getAttribute("weaver_user@bean") ;
            String userid=  Util.null2String(req.getParameter("f_weaver_belongto_userid"));
            if("".equals(userid) || userid == null){
             userid=loginuser.getUID()+"";
            }
            //String loginid=loginuser.getLoginid();
            //String[] docidsList = Util.TokenizerString2(docids, ",");
            //List docImagefileidList= new ArrayList();
            String docImagefileids="";
            String sqlimagefileid="select a.id,a.docsubject,b.imagefileid,b.imagefilename from DocDetail a,DocImageFile b where a.id=b.docid and a.id in ("+docids+") and b.versionId = (select MAX(c.versionId) from DocImageFile c where c.id=b.id ) order by a.id asc";
            
            /*
            String sqlimagefileid="";
            if("1".equals(docSearchFlag)){//处理从查询文档中 来的文档附件批量下载时只下载最新版本问题
                sqlimagefileid="select a.id,a.docsubject,b.imagefileid,b.imagefilename from DocDetail a,DocImageFile b where a.id=b.docid and a.id in ("+docids+") and b.versionId = (select MAX(c.versionId) from DocImageFile c where c.id=b.id ) order by a.id asc";
            }else{
                sqlimagefileid="select a.id,a.docsubject,b.imagefileid,b.imagefilename from DocDetail a,DocImageFile b where a.id=b.docid and a.id in ("+docids+") order by a.id asc";
            }
            */
            String docsubject="";
            rsimagefileid.executeSql(sqlimagefileid);
            int counts=rsimagefileid.getCounts();
            String urlType = Util.null2String(req.getParameter("urlType"));
            if(counts<=0){
               if("1".equals(docSearchFlag)){
                   if(urlType.equals("10")){
                       res.sendRedirect("/docs/search/DocCommonContent.jsp?urlType=10&displayUsage="+displayUsage);
                   }else{
                    res.sendRedirect("/docs/search/DocCommonContent.jsp?urlType=6&fromUrlType=1&displayUsage="+displayUsage);
                   }
                return;
               }else{
                res.sendRedirect("/login/BatchDownloadsEror.jsp"); 
                return;
               }    
            }
            int num=0;
            while(rsimagefileid.next()){
                num++;
                if(num==counts){
                    docImagefileids += Util.null2String(rsimagefileid.getString("imagefileid"));
                }else{
                    docImagefileids += Util.null2String(rsimagefileid.getString("imagefileid"))+",";
                }
                docsubject=Util.null2String(rsimagefileid.getString("docsubject"));
            }
            List filenameList = new ArrayList();
			fileNameEcoding = new ArrayList();
            List filerealpathList = new ArrayList();
            List filerealpathTempList = new ArrayList();
            //List filerealpathTempParentList = new ArrayList();
            File fileTemp=null;
            String sqlfilerealpath="";
			String gvo_encrypt = "";
            ImageFileManager imageFileManager=new ImageFileManager();
            if(delImgIds!=null &&!"".equals(delImgIds)){
                //sqlfilerealpath="select imagefilename,filerealpath,iszip,isencrypt,imagefiletype , imagefileid, imagefile from ImageFile where imagefileid in ("+delImgIds+")";
                sqlfilerealpath = "select t1.isaesencrypt,t1.aescode,t1.imagefilename,t1.filerealpath,t1.iszip,t1.isencrypt,t1.imagefiletype , t1.imagefileid, t1.imagefile,t2.imagefilename as realname,t1.gvo_encrypt from ImageFile t1 left join DocImageFile t2 on t1.imagefileid = t2.imagefileid where t1.imagefileid in ("+delImgIds+") "       +(docids.equals("")?"":" and docid in("+docids+") ");
            }else{
                //sqlfilerealpath="select imagefilename,filerealpath,iszip,isencrypt,imagefiletype , imagefileid, imagefile from ImageFile where imagefileid in ("+docImagefileids+")";
                sqlfilerealpath = "select t1.isaesencrypt,t1.aescode,t1.imagefilename,t1.filerealpath,t1.iszip,t1.isencrypt,t1.imagefiletype , t1.imagefileid, t1.imagefile,t2.imagefilename as realname,t1.gvo_encrypt from ImageFile t1 left join DocImageFile t2 on t1.imagefileid = t2.imagefileid where t1.imagefileid in ("+docImagefileids+") " +(docids.equals("")?"":" and docid in("+docids+") ");
            }
            
            rsimagefileid.executeSql(sqlfilerealpath);
            while(rsimagefileid.next()){
                try{
                    String imagefileid = Util.null2String(rsimagefileid.getString("imagefileid"));
                    String filename = Util.null2String(rsimagefileid.getString("realname"));
                    if(filename.equals("")){
                        filename = Util.null2String(rsimagefileid.getString("imagefilename"));
                    }
                    String filerealpath = Util.null2String(rsimagefileid.getString("filerealpath"));
                    String iszip = Util.null2String(rsimagefileid.getString("iszip"));
                    String isencrypt = Util.null2String(rsimagefileid.getString("isencrypt"));
                    String isaesencrypt = Util.null2String(rsimagefileid.getString("isaesencrypt"));
                    String aescode = Util.null2String(rsimagefileid.getString("aescode"));
                	gvo_encrypt = Util.null2String(rsimagefileid.getString("gvo_encrypt"));
                    filenameList.add(filename);
                    filerealpathList.add(filerealpath);
                        //是否需要记录日志
                       if(addDownLoadLogByimageId(Util.getIntValue(imagefileid))){
                        // 记录下载日志 begin
                           
                           String userType = loginuser.getLogintype();
                           if ("1".equals(userType)) { // 如果是内部用户　名称就是　lastName
                                                       // 外部则入在　firstName里面
                               downloadLog(loginuser.getUID(), loginuser.getLastname(), Util.getIntValue(imagefileid),filename, ipstring);
                           } else {
                               downloadLog(loginuser.getUID(), loginuser.getFirstname(), Util.getIntValue(imagefileid),filename, ipstring);
                           }
                  
                        // 记录下载日志 end
                       }
                    String extName = "";
                    int byteread;
                    byte data[] = new byte[1024];
                    if(filename.indexOf(".") > -1){
                        int bx = filename.lastIndexOf(".");
                        if(bx>=0){
                            extName = filename.substring(bx+1, filename.length());
                        }
                    }
                    
                    InputStream imagefile = null;
                    if("1".equals(gvo_encrypt)&&"1".equals(download)){
						//
						StringBuffer log_buff = new StringBuffer();
						log_buff.append("参数[ {imageid:");log_buff.append(imagefileid);
						log_buff.append(",filename:");log_buff.append(filename);
						log_buff.append(",filerealpath:");log_buff.append(filerealpath);
						log_buff.append("}]");
						// 加密
						// boolean isencfile = true;  //false 解密 true 加密
						imagefile = gsf.getGvoInputStream(log_buff.toString(),imagefile,true);
					}else {
                    	imageFileManager.getImageFileInfoById(Util.getIntValue(imagefileid));
                    	imagefile=imageFileManager.getInputStream();
                    }
                    if(download.equals("1") && ("xls".equalsIgnoreCase(extName) || "doc".equalsIgnoreCase(extName)||"wps".equalsIgnoreCase(extName)||"ppt".equalsIgnoreCase(extName))&&isMsgObjToDocument()) {
                        //正文的处理
                        ByteArrayOutputStream bout = null;
                        try {
                            bout = new ByteArrayOutputStream() ;
                            while((byteread = imagefile.read(data)) != -1) {
                                bout.write(data, 0, byteread) ;
                                bout.flush() ;
                            }
                            byte[] fileBody = bout.toByteArray();
                            iMsgServer2000 MsgObj = new DBstep.iMsgServer2000();
                            MsgObj.MsgFileBody(fileBody);           //将文件信息打包
                            fileBody = MsgObj.ToDocument(MsgObj.MsgFileBody());    //通过iMsgServer200 将pgf文件流转化为普通Office文件流
                            imagefile = new ByteArrayInputStream(fileBody);
                            bout.close();
                        }
                        catch(Exception e) {
                            if(bout!=null) bout.close();
                        }
                    }       
                    
                    FileOutputStream out = null;
                    try {
                        
                        SystemComInfo syscominfo = new SystemComInfo();
                        String fileFoder= syscominfo.getFilesystem();
                        String fileFoderCompare= syscominfo.getFilesystem();
                        if("".equals(fileFoder)){
                            fileFoder = GCONST.getRootPath();
                            fileFoder = fileFoder + "filesystem" + File.separatorChar+ "downloadBatchTemp"+File.separatorChar+userid+currentDateTime+File.separatorChar;
                        }else{
                            if(fileFoder.endsWith(File.separator) ){
                                fileFoder += "downloadBatchTemp"+File.separatorChar+userid+currentDateTime+File.separatorChar;
                            }else{
                                fileFoder += File.separator+ "downloadBatchTemp"+File.separatorChar+userid+currentDateTime+File.separatorChar;
                            }
                        }   
                        
                        if("".equals(fileFoderCompare)){
                            fileFoderCompare = GCONST.getRootPath();
                            fileFoderCompare = fileFoderCompare + "filesystem" + File.separatorChar+ "downloadBatchTemp"+File.separatorChar+userid+currentDateTime+File.separatorChar+filename;
                        }else{
                            if(fileFoderCompare.endsWith(File.separator)){
                                fileFoderCompare += "downloadBatchTemp"+File.separatorChar+userid+currentDateTime+File.separatorChar+filename;
                            }else{
                                fileFoderCompare += File.separator+ "downloadBatchTemp"+File.separatorChar+userid+currentDateTime+File.separatorChar+filename;
                            }
                        }                   
                        
                        //String fileFoder=GCONST.getRootPath()+ "downloadBatchTemp"+File.separatorChar+userid+File.separatorChar;//获取系统的运行目录 如：d:\ecology\
                        //附件文件重名加数字后缀区别:因为不加以区别的话,压缩到压缩包时只取一个文件(压缩包中不能有同名文件) 加唯一标识'_imagefileid'
                        //String fileFoderCompare=GCONST.getRootPath()+ "downloadBatchTemp"+File.separatorChar+userid+File.separatorChar+filename;
                        String newFileName=filename;
                        for(int j=0;j<filerealpathTempList.size();j++){
                            if(fileFoderCompare.equals(filerealpathTempList.get(j))){
                                int lastindex=filename.lastIndexOf(".");
                                if(lastindex>=0){
                                    newFileName=filename.substring(0, lastindex)+"_"+imagefileid+filename.substring(lastindex);                                 
                                }else{
                                    newFileName=filename+"_"+imagefileid;                                   
                                }
                            }
                        }
						fileNameEcoding.add(newFileName);
						newFileName=userid+currentDateTime+"_"+imagefileid;
                        fileTemp=this.fileCreate(fileFoder, newFileName);//在系统的根目录下创建了一个文件夹(downloadBatchTemp)及附件文件
                        out = new FileOutputStream(fileTemp);
                        while ((byteread = imagefile.read(data)) != -1) {
                           out.write(data, 0, byteread);                    
                           out.flush();
                        }
                        String filerealpathTemp=fileTemp.getPath();
                        //String filerealpathTempParent=fileTemp.getParent();
                        //filerealpathTempParentList.add(filerealpathTempParent);
                        //filerealpathTempList.add(filerealpathTemp);
                        filerealpathTempList.add(fileFoder+newFileName);                        
                    }catch(Exception e) {
                        //do nothing
                    }finally {
                        if(imagefile!=null) imagefile.close();
                        if(out!=null) out.flush();
                        if(out!=null) out.close();
                    }                   
                }catch(Exception e){
                    BaseBean basebean = new BaseBean();
                    basebean.writeLog(e);
                    continue;
                }
            }
            
            

            //存储下载的文件路径   把临时存放的附件 打压缩包 提供给用户下载
            File[] files=new File[filerealpathList.size()];
            for(int i=0;i<filerealpathTempList.size();i++){
                  String path=(String)filerealpathTempList.get(i); 
                  files[i]=new File(path); 
            } 
            byte[] bt=new byte[8192];
            
            SystemComInfo syscominfo2 = new SystemComInfo();
            String fileFoder2= syscominfo2.getFilesystem();
            if("".equals(fileFoder2)){
                fileFoder2 = GCONST.getRootPath();
                fileFoder2 = fileFoder2 + "filesystem" + File.separatorChar+ "downloadBatch"+File.separatorChar;
            }else{
                if(fileFoder2.endsWith(File.separator)){
                    fileFoder2 += "downloadBatch"+File.separatorChar;
                    
                }else{
                    fileFoder2 += File.separator+ "downloadBatch"+File.separatorChar;
                }
            }           
            
            //String fileFoder=GCONST.getRootPath()+ "downloadBatch"+File.separatorChar;//获取系统的运行目录 如：d:\ecology\
            //String strs=requestname+"_"+userid+".zip";
            String strs="";
            String tmptfilename=userid+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".zip";
            
            if(requestname!=null&&!"".equals(requestname)){//附件从流程中来 文件名称：流程名称_用户userid.zip
                strs=requestname+"_"+userid+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".zip";
            }else if("1".equals(docSearchFlag)){//从查询文档中来批量下载多文档多附件 文件名称：用户userid.zip
                strs=userid+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".zip";
            }else{//附件从文档中来 文件名称：文档名称_用户userid.zip
                strs=docsubject+"_"+userid+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".zip";
            }
            File file=this.fileCreate(fileFoder2, tmptfilename);//在系统的根目录下创建了一个文件夹(downloadBatch)及zip文件
            String strzipName=file.getPath();
            //String fileTempParent=fileTemp.getParent();
            String fileTempParent=null;
            if(fileTemp!=null){
                fileTempParent=fileTemp.getParent();
            }
            ZipOutputStream zout =null;
            FileInputStream is=null;
            try {  
                zout = new ZipOutputStream(new FileOutputStream(strzipName), "UTF-8");  
                //循环下载每个文件并读取内容 
                for(int j=0;j<files.length;j++){
                    try{
                         is=new FileInputStream(files[j]); 
                         //System.out.println("第["+j+"]个文件的名称为："+files[j].getName());
                         ZipEntry ze=new ZipEntry((String)fileNameEcoding.get(j));//设置文件编码格式 
                         zout.putNextEntry(ze); 
                         int len; 
                         //读取下载文件内容 
                         while((len=is.read(bt))>0){ 
                          zout.write(bt, 0, len); 
                         } 
                         zout.closeEntry(); 
                         is.close();                    
                    }catch(Exception e){
                        BaseBean basebean = new BaseBean();
                        basebean.writeLog(e);
                        continue;
                    } 
                } 
                zout.close(); 
                this.toUpload(res, strs, tmptfilename);//调用下载方法
                //String fileTempParent=fileTemp.getParent();
                //this.deleteFile(fileTempParent);//下载完成后调用删除文件的方法删除downloadBatchTemp文件夹下的子文件夹
                /*
                for(int i=0;i<filerealpathTempList.size();i++){
                    String delPathTemp=(String)filerealpathTempList.get(i);
                    this.deleteFile(delPathTemp);//下载完成后调用删除方法删除downloadBatchTemp临时文件夹中存放的附件文件
                }*/
                //this.deleteFile(strzipName);//下载完成后调用删除文件的方法删除downloadBatch文件夹中的.zip文件
         } catch (FileNotFoundException e) { 
                e.printStackTrace(); 
         }catch (IOException e) { 
                e.printStackTrace(); 
         }finally {
             if(is!=null) {
                is.close(); 
             }
             if(zout!=null){
                zout.close();
             }
             this.deleteFile(strzipName);//下载完成后调用删除文件的方法删除downloadBatch文件夹中的.zip文件              
             //this.deleteFile(fileTempParent);//下载完成后调用删除文件的方法删除downloadBatchTemp文件夹下的子文件夹
             if(fileTempParent!=null){
                 //new weaver.filter.XssUtil().writeLog("=====L402------"+fileTempParent);
                 this.deleteFile(fileTempParent);//下载完成后调用删除文件的方法删除downloadBatchTemp文件夹下的子文件夹
             }
         } 
         
         
         
       /*==============td26423 流程中批量下载     end============================================================*/                
            
        }else if(onlydownloadfj!=null &&"1".equals(onlydownloadfj)){//批量下载--来自于证照，只批量下载附件，传入的是附件的ids
             /*============== 证照批量下载附件     start============================================================*/              
            String currentDateTime= new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());          
            String fieldids = Util.null2String(req.getParameter("fieldids"));//fieldids 以逗号隔开的
            int labelid = Util.getIntValue(Util.null2String(req.getParameter("labelid")),0);
            String download = Util.null2String(req.getParameter("download"));
            User loginuser = (User)req.getSession(true).getAttribute("weaver_user@bean") ;
            String userid=loginuser.getUID()+"";
            File fileTemp=null;
            if(!"".equals(fieldids)&&fieldids.length()>0){
                fieldids = fieldids.substring(0,fieldids.length()-1);
            }
            List filenameList = new ArrayList();
            List filerealpathList = new ArrayList();
            List filerealpathTempList = new ArrayList();
            String sqlfilerealpath="";
            ImageFileManager imageFileManager=new ImageFileManager();
            sqlfilerealpath = "select * from imagefile where imagefileid in("+fieldids+") ";
            RecordSet rsimagefileid =new RecordSet();
            rsimagefileid.executeSql(sqlfilerealpath);
            while(rsimagefileid.next()){
                try{
                    String imagefileid = Util.null2String(rsimagefileid.getString("imagefileid"));
                    String filename = Util.null2String(rsimagefileid.getString("realname"));
                    if(filename.equals("")){
                        filename = Util.null2String(rsimagefileid.getString("imagefilename"));
                    }
                    String filerealpath = Util.null2String(rsimagefileid.getString("filerealpath"));
                    String iszip = Util.null2String(rsimagefileid.getString("iszip"));
                    String isencrypt = Util.null2String(rsimagefileid.getString("isencrypt"));
                    String isaesencrypt = Util.null2String(rsimagefileid.getString("isaesencrypt"));
                    String aescode = Util.null2String(rsimagefileid.getString("aescode"));
                    String gvo_encrypt = Util.null2String(rsimagefileid.getString("gvo_encrypt"));

                    filenameList.add(filename);
                    filerealpathList.add(filerealpath);
                    String extName = "";
                    int byteread;
                    byte data[] = new byte[1024];
                    if(filename.indexOf(".") > -1){
                        int bx = filename.lastIndexOf(".");
                        if(bx>=0){
                            extName = filename.substring(bx+1, filename.length());
                        }
                    }
                    
                    InputStream imagefile = null;
					if("1".equals(gvo_encrypt)&&"1".equals(download)){
						StringBuffer log_buff = new StringBuffer();
						log_buff.append("参数[ {imageid:");log_buff.append(imagefileid);
						log_buff.append(",filename:");log_buff.append(filename);
						log_buff.append(",filerealpath:");log_buff.append(filerealpath);
						log_buff.append("}]");
						// 加密
						// boolean isencfile = true;  //false 解密 true 加密
						imagefile = gsf.getGvoInputStream(log_buff.toString(),imagefile,true);
					}else {
                    	imageFileManager.getImageFileInfoById(Util.getIntValue(imagefileid));
                    	imagefile=imageFileManager.getInputStream();
                    }
                    if(download.equals("1") && ("xls".equalsIgnoreCase(extName) || "doc".equalsIgnoreCase(extName)||"wps".equalsIgnoreCase(extName)||"ppt".equalsIgnoreCase(extName))&&isMsgObjToDocument()) {
                        //正文的处理
                        ByteArrayOutputStream bout = null;
                        try {
                            bout = new ByteArrayOutputStream() ;
                            while((byteread = imagefile.read(data)) != -1) {
                                bout.write(data, 0, byteread) ;
                                bout.flush() ;
                            }
                            byte[] fileBody = bout.toByteArray();
                            iMsgServer2000 MsgObj = new DBstep.iMsgServer2000();
                            MsgObj.MsgFileBody(fileBody);           //将文件信息打包
                            fileBody = MsgObj.ToDocument(MsgObj.MsgFileBody());    //通过iMsgServer200 将pgf文件流转化为普通Office文件流
                            imagefile = new ByteArrayInputStream(fileBody);
                            bout.close();
                        }
                        catch(Exception e) {
                            if(bout!=null) bout.close();
                        }
                    }       
                    
                    FileOutputStream out = null;
                    try {
                        
                        SystemComInfo syscominfo = new SystemComInfo();
                        String fileFoder= syscominfo.getFilesystem();
                        String fileFoderCompare= syscominfo.getFilesystem();
                        if("".equals(fileFoder)){
                            fileFoder = GCONST.getRootPath();
                            fileFoder = fileFoder + "filesystem" + File.separatorChar+ "downloadBatchTemp"+File.separatorChar+userid+currentDateTime+File.separatorChar;
                        }else{
                            if(fileFoder.endsWith(File.separator)){
                                fileFoder += "downloadBatchTemp"+File.separatorChar+userid+currentDateTime+File.separatorChar;
                            }else{
                                fileFoder += File.separator+ "downloadBatchTemp"+File.separatorChar+userid+currentDateTime+File.separatorChar;
                            }
                        }   
                        
                        if("".equals(fileFoderCompare)){
                            fileFoderCompare = GCONST.getRootPath();
                            fileFoderCompare = fileFoderCompare + "filesystem" + File.separatorChar+ "downloadBatchTemp"+File.separatorChar+userid+currentDateTime+File.separatorChar+filename;
                        }else{
                            if(fileFoderCompare.endsWith(File.separator)){
                                fileFoderCompare += "downloadBatchTemp"+File.separatorChar+userid+currentDateTime+File.separatorChar+filename;
                            }else{
                                fileFoderCompare += File.separator+ "downloadBatchTemp"+File.separatorChar+userid+currentDateTime+File.separatorChar+filename;
                            }
                        }                   
                        
                        //String fileFoder=GCONST.getRootPath()+ "downloadBatchTemp"+File.separatorChar+userid+File.separatorChar;//获取系统的运行目录 如：d:\ecology\
                        //附件文件重名加数字后缀区别:因为不加以区别的话,压缩到压缩包时只取一个文件(压缩包中不能有同名文件) 加唯一标识'_imagefileid'
                        //String fileFoderCompare=GCONST.getRootPath()+ "downloadBatchTemp"+File.separatorChar+userid+File.separatorChar+filename;
                        String newFileName=filename;
                        for(int j=0;j<filerealpathTempList.size();j++){
                            if(fileFoderCompare.equals(filerealpathTempList.get(j))){
                                int lastindex=filename.lastIndexOf(".");
                                if(lastindex>=0){
                                    newFileName=filename.substring(0, lastindex)+"_"+imagefileid+filename.substring(lastindex);                                 
                                }else{
                                    newFileName=filename+"_"+imagefileid;                                   
                                }
                            }
                        }
                        fileTemp=this.fileCreate(fileFoder, newFileName);//在系统的根目录下创建了一个文件夹(downloadBatchTemp)及附件文件
                        out = new FileOutputStream(fileTemp);
                        while ((byteread = imagefile.read(data)) != -1) {
                           out.write(data, 0, byteread);                    
                           out.flush();
                        }
                        String filerealpathTemp=fileTemp.getPath();
                        //String filerealpathTempParent=fileTemp.getParent();
                        //filerealpathTempParentList.add(filerealpathTempParent);
                        //filerealpathTempList.add(filerealpathTemp);
                        filerealpathTempList.add(fileFoder+newFileName);
                    }catch(Exception e) {
                        //do nothing
                        e.printStackTrace();
                    }finally {
                        if(imagefile!=null) imagefile.close();
                        if(out!=null) out.flush();
                        if(out!=null) out.close();
                    }                   
                }catch(Exception e){
                    BaseBean basebean = new BaseBean();
                    basebean.writeLog(e);
                    continue;
                }
            }
            //存储下载的文件路径   把临时存放的附件 打压缩包 提供给用户下载
            File[] files=new File[filerealpathList.size()];
            for(int i=0;i<filerealpathTempList.size();i++){
                  String path=(String)filerealpathTempList.get(i); 
                  files[i]=new File(path); 
            } 
            byte[] bt=new byte[8192];
            
            SystemComInfo syscominfo2 = new SystemComInfo();
            String fileFoder2= syscominfo2.getFilesystem();
            if("".equals(fileFoder2)){
                fileFoder2 = GCONST.getRootPath();
                fileFoder2 = fileFoder2 + "filesystem" + File.separatorChar+ "downloadBatch"+File.separatorChar;
            }else{
                if(fileFoder2.endsWith(File.separator)){
                    fileFoder2 += "downloadBatch"+File.separatorChar;
                }else{
                    fileFoder2 += File.separator+ "downloadBatch"+File.separatorChar;
                }
            }           
            //String fileFoder=GCONST.getRootPath()+ "downloadBatch"+File.separatorChar;//获取系统的运行目录 如：d:\ecology\
            //String strs=requestname+"_"+userid+".zip";
            //附件的名字
            String strs=SystemEnv.getHtmlLabelName(labelid,loginuser.getLanguage())+"_"+userid+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".zip";
            File file=this.fileCreate(fileFoder2, strs);//在系统的根目录下创建了一个文件夹(downloadBatch)及zip文件
            String strzipName=file.getPath();
            //String fileTempParent=fileTemp.getParent();
            String fileTempParent=null;
            if(fileTemp!=null){
                fileTempParent=fileTemp.getParent();
            }
            ZipOutputStream zout =null;
            FileInputStream is=null;
            try {  
                zout = new ZipOutputStream(new FileOutputStream(strzipName), "UTF-8");  
                //循环下载每个文件并读取内容 
                for(int j=0;j<files.length;j++){
                    try{
                     is=new FileInputStream(files[j]); 
                     //System.out.println("第["+j+"]个文件的名称为："+files[j].getName());
                     ZipEntry ze=new ZipEntry(files[j].getName());//设置文件编码格式 
                     zout.putNextEntry(ze); 
                     int len; 
                     //读取下载文件内容 
                     while((len=is.read(bt))>0){ 
                      zout.write(bt, 0, len); 
                     } 
                     zout.closeEntry(); 
                     is.close();                        
                    }catch(Exception e){
                        BaseBean basebean = new BaseBean();
                        basebean.writeLog(e);
                        continue;
                    }
                } 
                zout.close(); 
                this.toUpload(res, strs);//调用下载方法
                //String fileTempParent=fileTemp.getParent();
                //this.deleteFile(fileTempParent);//下载完成后调用删除文件的方法删除downloadBatchTemp文件夹下的子文件夹
                /*
                for(int i=0;i<filerealpathTempList.size();i++){
                    String delPathTemp=(String)filerealpathTempList.get(i);
                    this.deleteFile(delPathTemp);//下载完成后调用删除方法删除downloadBatchTemp临时文件夹中存放的附件文件
                }*/
                //this.deleteFile(strzipName);//下载完成后调用删除文件的方法删除downloadBatch文件夹中的.zip文件
         } catch (FileNotFoundException e) { 
                e.printStackTrace(); 
         }catch (IOException e) { 
                e.printStackTrace(); 
         }finally {
             if(is!=null) {
                is.close(); 
             }
             if(zout!=null){
                zout.close();
             }
             this.deleteFile(strzipName);//下载完成后调用删除文件的方法删除downloadBatch文件夹中的.zip文件              
             //this.deleteFile(fileTempParent);//下载完成后调用删除文件的方法删除downloadBatchTemp文件夹下的子文件夹
             if(fileTempParent!=null){
                // new weaver.filter.XssUtil().writeLog("=====L622------"+strzipName);
                 this.deleteFile(fileTempParent);//下载完成后调用删除文件的方法删除downloadBatchTemp文件夹下的子文件夹
             }
         } 
            /*============== 证照批量下载附件     end============================================================*/             
    } else{
        
        int fileid = Util.getIntValue(req.getParameter("fileid"), -1);
        String coworkid = Util.getFileidIn(Util.null2String(req.getParameter("coworkid")));
        int requestid = Util.getIntValue(req.getParameter("requestid"));
        String ipstring = req.getRemoteAddr();
        if(fileid <= 0){//转化为int型，防止SQL注入
            res.sendRedirect("/notice/noright.jsp");                                
            return;
        }
        //String strSql="select docpublishtype from docdetail where id in (select docid from docimagefile where imagefileid="+fileid+") and ishistory <> 1";    
        RecordSet statement = new RecordSet();   //by ben  开启连接后知道文件下载才关闭，数据库连接时间太长
        //RecordSet rs =new RecordSet();
        User user=null;
        try {
            //解决问题：一个附件或图片被一篇内部文档和外部新闻同时引用时，外部新闻可能查看不到附件或图片。 update by fanggsh fot TD5478  begin            
            //调整为如下：
            //默认需要用户登录信息,不需要登录信息的情形如下：
            //1、非登录用户查看外部新闻
            //boolean needUser= false;  
            boolean needUser= true; 
            int docId=0;
            String docIdsForOuterNews="";
            String strSql="select id from DocDetail where exists (select 1 from docimagefile where imagefileid="+fileid+" and docId=DocDetail.id) and ishistory <> 1 and (docPublishType='2' or docPublishType='3')";
            RecordSet rs =new RecordSet();
            rs.executeSql(strSql);
            while(rs.next()){
                docId=rs.getInt("id");
                if(docId>0){
                    docIdsForOuterNews+=","+docId;
                }
            }
            
            if(!docIdsForOuterNews.equals("")){
                docIdsForOuterNews=docIdsForOuterNews.substring(1);
            }
            
            if(!docIdsForOuterNews.equals("")){
                String newsClause="";
                String sqlDocExist=" select 1 from DocDetail where id in("+docIdsForOuterNews+") "; 
                String sqlNewsClauseOr="";
                boolean hasOuterNews=false;
                
                rs.executeSql("select newsClause from DocFrontPage where publishType='0'");
                while(rs.next()){
                    hasOuterNews=true;
                    newsClause=Util.null2String(rs.getString("newsClause"));
                    if (newsClause.equals(""))
                    {
                        //newsClause=" 1=1 ";
                        needUser=false;
                        break;
                    }
                    if(!newsClause.trim().equals("")){
                        sqlNewsClauseOr+=" ^_^ ("+newsClause+")";
                    }
                }
                ArrayList newsArr = new ArrayList();
                if(!sqlNewsClauseOr.equals("")&&needUser){
                    //sqlNewsClauseOr=sqlNewsClauseOr.substring(sqlNewsClauseOr.indexOf("("));
                    //sqlDocExist+=" and ("+sqlNewsClauseOr+") ";
                    String[] newsPage = Util.TokenizerString2(sqlNewsClauseOr,"^_^");
                    int i = 0;
                    String newsWhere = "";                  
                    for(;i<newsPage.length;i++){
                        if(i%10==0){
                            newsArr.add(newsWhere);
                            newsWhere="";
                            newsWhere+=newsPage[i];
                        }else
                            newsWhere+=" or "+newsPage[i];  
                    }
                    newsArr.add(newsWhere);
                }
                //System.out.print(sqlDocExist);
                if(hasOuterNews&&needUser){
                    for(int j=1;j<newsArr.size();j++){  
                        String newsp = newsArr.get(j).toString();                       
                        if(j==1)
                            newsp = newsp.substring(newsp.indexOf("or")+2);
                        sqlDocExist+="and("+newsp+")";
                        rs.executeSql(sqlDocExist);
                        sqlDocExist = " select 1 from DocDetail where id in("+docIdsForOuterNews+") "; 
                        if(rs.next()){
                            needUser=false;
                            break;
                        }
                    }
                }
            }           


            //处理外网查看默认图片
                rs.executeSql("SELECT * FROM DocPicUpload  WHERE  Imagefileid="+fileid);
                if(rs.next()){
                        needUser=false;
                    }

            if(needUser){
                boolean hasRight=false;
                try{
                    
                    ResourceComInfo  comInfo = new ResourceComInfo();
                    String fromhrmcontract = Util.null2String(req.getParameter("fromhrmcontract"));
                    if(!"".equals(fromhrmcontract)){
                        String contractman ="";
                        String contractdocid ="";
                        String contSql = "select contractman,contractdocid from HrmContract where id ="+fromhrmcontract;
                        rs.executeSql(contSql);
                        if(rs.next()){
                            contractman = rs.getString("contractman");
                            contractdocid = rs.getString("contractdocid");
                        }
                        
                        contSql = "select * from DocImageFile where docid="+contractdocid+" and imagefileid="+fileid;
                        rs.executeSql(contSql);
                        if(rs.next()){
                            String f_weaver_belongto_userid=Util.null2String(req.getParameter("f_weaver_belongto_userid"));//需要增加的代码
                            String f_weaver_belongto_usertype=Util.null2String(req.getParameter("f_weaver_belongto_usertype"));//需要增加的代码
                            user = HrmUserVarify.getUser(req, res, f_weaver_belongto_userid, f_weaver_belongto_usertype) ;//需要增加的代码
                            //user = HrmUserVarify.getUser (req ,res) ;
                            int hrmid = user.getUID();
                            boolean ism = comInfo.isManager(hrmid,contractman); //上级
                            boolean ishe = (hrmid == Util.getIntValue(contractman)); //本人
                            boolean ishr = (HrmUserVarify.checkUserRight("HrmContractAdd:Add",user));//人力资源管理员
                            if(ism || ishe || ishr) hasRight = true;
                        }
                    }else{
                        hasRight=getWhetherHasRight(""+fileid,req,res,requestid);
                    }
                }catch(Exception ex){
                    BaseBean basebean = new BaseBean();
                    basebean.writeLog(ex);
                    hasRight=false;
                }

                if(!hasRight){//
                    res.sendRedirect("/notice/noright.jsp");                                
                    return;
    
                  
                }
                //end by cyril on 2008-08-01 for td:9133
            }

            //解决问题：一个附件或图片被一篇内部文档和外部新闻同时引用时，外部新闻可能查看不到附件或图片。 update by fanggsh fot TD5478  end          
            //statement.close();
            String download = Util.null2String(req.getParameter("download"));
            String contenttype = "";
            String filename = "";
            String filerealpath = "";
            String iszip = "";
            String isencrypt = "";
			String gvo_encrypt = "";
            String isaesencrypt="";
            String aescode = "";
            String tokenKey="";
            String storageStatus = "";
            String comefrom="";

            if ("1".equals(req.getParameter("countdownloads"))) {
                isCountDownloads = true;
            }
            int byteread;
            byte data[] = new byte[1024];
           

            
            //String sql = "select imagefilename,filerealpath,iszip,isencrypt,imagefiletype , imagefile from ImageFile where imagefileid = " + fileid;
            String sql = "select t1.imagefilename,t1.filerealpath,t1.iszip,t1.isencrypt,t1.imagefiletype , t1.imagefileid, t1.imagefile,t1.isaesencrypt,t1.aescode,t2.imagefilename as realname,t1.gvo_encrypt from ImageFile t1 left join DocImageFile t2 on t1.imagefileid = t2.imagefileid where t1.imagefileid = "+fileid;
            boolean isoracle = (statement.getDBType()).equals("oracle");
            
            String extName = "";
            
            statement.execute(sql);
            //statement.executeQuery();
            if (statement.next()) {
                filename = Util.null2String(statement.getString("realname"));
                if(filename.equals("")){
                    filename = Util.null2String(statement.getString("imagefilename"));
                }
                if(filename.toLowerCase().endsWith(".pdf")){
                    int decryptPdfImageFileId=0;
                    rs.executeSql("select decryptPdfImageFileId from workflow_texttopdf where pdfImageFileId="+fileid);
                    if(rs.next()){
                        decryptPdfImageFileId=Util.getIntValue(rs.getString("decryptPdfImageFileId"),-1);
                    }
                    if(decryptPdfImageFileId>0){
                        sql = "select t1.imagefilename,t1.filerealpath,t1.iszip,t1.isencrypt,t1.imagefiletype , t1.imagefileid, t1.imagefile,t1.isaesencrypt,t1.aescode,t2.imagefilename as realname,t1.TokenKey,t1.StorageStatus,t1.comefrom from ImageFile t1 left join DocImageFile t2 on t1.imagefileid = t2.imagefileid where t1.imagefileid = "+decryptPdfImageFileId;
                        statement.execute(sql);
                        if(!statement.next()){
                            return ;
                        }
                    }
                }
                filerealpath = Util.null2String(statement.getString("filerealpath"));
                iszip = Util.null2String(statement.getString("iszip"));
				gvo_encrypt = Util.null2String(statement.getString("gvo_encrypt"));
                isencrypt = Util.null2String(statement.getString("isencrypt"));
                isaesencrypt = Util.null2o(statement.getString("isaesencrypt"));
                aescode = Util.null2String(statement.getString("aescode"));
                tokenKey = Util.null2String(statement.getString("TokenKey"));
                storageStatus = Util.null2String(statement.getString("StorageStatus"));
                comefrom = Util.null2String(statement.getString("comefrom"));
                                
                if(filename.indexOf(".") > -1){
                    int bx = filename.lastIndexOf(".");
                    if(bx>=0){
                        extName = filename.substring(bx+1, filename.length());                      
                    }
                }               

                boolean isInline=false;
                String cacheContorl="";
                boolean isEnableForDsp=false;
                if(!tokenKey.equals("")&&storageStatus.equals("1")&&AliOSSObjectManager.isEnableForDsp(req)){
                    isEnableForDsp=true;
                }
                boolean isPic=false;
                String lowerfilename = filename!=null ? filename.toLowerCase() : "";
                boolean ishtmlfile = false;
				if(lowerfilename.endsWith(".html")||lowerfilename.endsWith(".htm")){
					RecordSet rs_tmp = new RecordSet();
					rs_tmp.executeQuery("select 1 from DocPreviewHtml where htmlfileid = ?",fileid);
					if(rs_tmp.next()){
						ishtmlfile = true;
					}
				}
				if (download.equals("")&&((!lowerfilename.endsWith(".ppt"))&&(!lowerfilename.endsWith(".pptx"))&&!lowerfilename.endsWith(".xps") && !lowerfilename.endsWith(".js"))||ishtmlfile){
                    if(filename.toLowerCase().endsWith(".doc")) contenttype = "application/msword";
                    else if(filename.toLowerCase().endsWith(".xls")) contenttype = "application/vnd.ms-excel";
                    else if(filename.toLowerCase().endsWith(".gif")) {
                        contenttype = "image/gif";  
                        res.addHeader("Cache-Control", "private, max-age=8640000"); 
                        isPic=true;
                    }else if(filename.toLowerCase().endsWith(".png")) {
                        contenttype = "image/png";
                        res.addHeader("Cache-Control", "private, max-age=8640000"); 
                        isPic=true;
                    }else if(filename.toLowerCase().endsWith(".jpg")) {
                        contenttype = "image/jpg";
                        res.addHeader("Cache-Control", "private, max-age=8640000"); 
                        isPic=true;
                    }else if(filename.toLowerCase().endsWith(".bmp")) {
                        contenttype = "image/bmp";
                        res.addHeader("Cache-Control", "private, max-age=8640000"); 
                        isPic=true;
                    }
                    else if(filename.toLowerCase().endsWith(".txt")) contenttype = "text/plain";
                    else if(filename.toLowerCase().endsWith(".pdf")) contenttype = "application/pdf";
                    else if(filename.toLowerCase().endsWith(".html")||filename.toLowerCase().endsWith(".htm")) contenttype = "text/html";               
                    else {                  
                        contenttype = statement.getString("imagefiletype");
                    }
                    try {
                        if((agent.contains("Firefox")||agent.contains(" Chrome")||agent.contains("Safari") )&& !agent.contains("Edge")){
                            res.setHeader("content-disposition", "inline; filename=\"" +  new String(filename.replaceAll("<", "").replaceAll(">", "").replaceAll("&lt;", "").replaceAll("&gt;", "").getBytes("UTF-8"),"ISO-8859-1")+"\"");
                        }else{
                            res.setHeader("content-disposition", "inline; filename=\"" + URLEncoder.encode(filename.replaceAll("<", "").replaceAll(">", "").replaceAll("&lt;", "").replaceAll("&gt;", ""),"UTF-8").replaceAll("\\+", "%20")+"\"");
                        }
                    } catch (Exception ecode) {
                    }
                    isInline=true;
                }else {
                    contenttype = "application/octet-stream";
                    try {
                        //System.out.println(new String(new String(filename.getBytes(clientEcoding), "ISO8859_1").getBytes("ISO8859_1"),"utf-8"));
                        //System.out.println(new String(filename.getBytes(clientEcoding)));
                        if((agent.contains("Firefox")||agent.contains(" Chrome")||agent.contains("Safari") )&& !agent.contains("Edge")){
                            res.setHeader("content-disposition", "attachment; filename=\"" +  new String(filename.replaceAll("<", "").replaceAll(">", "").replaceAll("&lt;", "").replaceAll("&gt;", "").getBytes("UTF-8"),"ISO-8859-1")+"\"");
                        }else{
                            res.setHeader("content-disposition", "attachment; filename=\"" + URLEncoder.encode(filename.replaceAll("<", "").replaceAll(">", "").replaceAll("&lt;", "").replaceAll("&gt;", ""),"UTF-8").replaceAll("\\+", "%20")+"\"");
                        }
                    } catch (Exception ecode) {
                    }
                }
                
                if(isEnableForDsp){
                    
                    boolean  isAliOSSToServer=AliOSSObjectManager.isAliOSSToServer(comefrom);
                    if(isAliOSSToServer||isPic){
                        InputStream imagefile = null;
                        ServletOutputStream out = null;
                        try {
                            imagefile=weaver.alioss.AliOSSObjectUtil.downloadFile(tokenKey);
                            
                            out = res.getOutputStream();
                            res.setContentType(contenttype);
                                        
                            while ((byteread = imagefile.read(data)) != -1) {
                                out.write(data, 0, byteread);                   
                                out.flush();
                            }
                        }
                        catch(Exception e) {
                            //do nothing
                        }
                        finally {
                            if(imagefile!=null) imagefile.close();
                            if(out!=null) out.flush();
                            if(out!=null) out.close();
                        }                           
                        
                        try{
                            if(needUser) {
                                //记录下载日志 begin
                                HttpSession session = req.getSession(false);
                                if (session != null) {
                                    user = (User) session.getAttribute("weaver_user@bean");
                                    if (user != null) {
                                        //董平修改　文档下载日志只记录了内部员工的名字，如果是客户门户来下载，则没有记录　for TD:1644
                                        String userType = user.getLogintype();
                                        if ("1".equals(userType)) {   //如果是内部用户　名称就是　lastName 外部则入在　firstName里面
                                            downloadLog(user.getUID(), user.getLastname(), fileid, filename,ipstring);
                                        } else {
                                            downloadLog(user.getUID(), user.getFirstname(), fileid, filename,ipstring);
                                        }
    
                                    }
                                }
                                //记录下载日志 end
                            }
    
                            countDownloads(""+fileid);                              
                        }catch(Exception ex){
                            
                        }                           
                        return ;
                    }else{
                        boolean  isSafari=AliOSSObjectManager.isSafari(req);
                        
                        String urlString=weaver.alioss.AliOSSObjectUtil.generatePresignedUrl(tokenKey,filename,contenttype,isInline,cacheContorl,isSafari);
                        if(urlString!=null){
                            try{
                                if(needUser) {
                                    //记录下载日志 begin
                                    HttpSession session = req.getSession(false);
                                    if (session != null) {
                                        user = (User) session.getAttribute("weaver_user@bean");
                                        if (user != null) {
                                            //董平修改　文档下载日志只记录了内部员工的名字，如果是客户门户来下载，则没有记录　for TD:1644
                                            String userType = user.getLogintype();
                                            if ("1".equals(userType)) {   //如果是内部用户　名称就是　lastName 外部则入在　firstName里面
                                                downloadLog(user.getUID(), user.getLastname(), fileid, filename,ipstring);
                                            } else {
                                                downloadLog(user.getUID(), user.getFirstname(), fileid, filename,ipstring);
                                            }
        
                                        }
                                    }
                                    //记录下载日志 end
                                }
        
                                countDownloads(""+fileid);                              
                            }catch(Exception ex){
                                
                            }
                            urlString=urlString+"&fileid="+fileid;
                            res.sendRedirect(urlString); 
                            return;                             
                        }                           
                    }
                }
                
                InputStream imagefile = null;               
                ZipInputStream zin = null;
                /*if (filerealpath.equals("")) {         // 旧的文件放在数据库中的方式
                    if (isoracle)
                        imagefile = new BufferedInputStream(statement.getBlobBinary("imagefile"));
                    else
                        imagefile = new BufferedInputStream(statement.getBinaryStream("imagefile"));
                } else*/       //目前已经不可能将文件存放在数据库中了
                
                    File thefile = new File(filerealpath);
                    if (iszip.equals("1")) {
                        zin = new ZipInputStream(new FileInputStream(thefile));
                        if (zin.getNextEntry() != null) imagefile = new BufferedInputStream(zin);
                    } else{
                        imagefile = new BufferedInputStream(new FileInputStream(thefile));
                    }
					if("1".equals(download)&&"1".equals(gvo_encrypt)){
					StringBuffer log_buff = new StringBuffer();
					log_buff.append("参数[ {imageid:");log_buff.append(fileid);
					log_buff.append(",filename:");log_buff.append(filename);
					log_buff.append(",filerealpath:");log_buff.append(filerealpath);
					log_buff.append("}]");
					// 加密
					// boolean isencfile = true;  //false 解密 true 加密
					imagefile = gsf.getGvoInputStream(log_buff.toString(),imagefile,true);
				}
                    if(download.equals("1") && ("xls".equalsIgnoreCase(extName) || "doc".equalsIgnoreCase(extName)||"wps".equalsIgnoreCase(extName)||"ppt".equalsIgnoreCase(extName))&&isMsgObjToDocument()) {
                        //正文的处理
                        ByteArrayOutputStream bout = null;
                        try {
                            bout = new ByteArrayOutputStream() ;
                            while((byteread = imagefile.read(data)) != -1) {
                                bout.write(data, 0, byteread) ;
                                bout.flush() ;
                            }
                            byte[] fileBody = bout.toByteArray();
                            iMsgServer2000 MsgObj = new DBstep.iMsgServer2000();
                            MsgObj.MsgFileBody(fileBody);           //将文件信息打包
                            fileBody = MsgObj.ToDocument(MsgObj.MsgFileBody());    //通过iMsgServer200 将pgf文件流转化为普通Office文件流
                            imagefile = new ByteArrayInputStream(fileBody);
                            bout.close();
                        }
                        catch(Exception e) {
                            if(bout!=null) bout.close();
                        }
                    }           
                


                ServletOutputStream out = null;
                try {
                    out = res.getOutputStream();
                    res.setContentType(contenttype);
    
                    if(isaesencrypt.equals("1")){
                        imagefile = AESCoder.decrypt(imagefile, aescode); 
                    }
                    
                    while ((byteread = imagefile.read(data)) != -1) {
                        out.write(data, 0, byteread);                   
                        out.flush();
                    }
                }
                catch(Exception e) {
                    //do nothing
                }
                finally {
                    if(imagefile!=null) imagefile.close();
                    if(zin!=null) zin.close();
                    if(out!=null) out.flush();
                    if(out!=null) out.close();
                }
                
                if(needUser) {
                    //记录下载日志 begin
                    HttpSession session = req.getSession(false);
                    if (session != null) {
                        user = (User) session.getAttribute("weaver_user@bean");
                        if (user != null) {
                            //董平修改　文档下载日志只记录了内部员工的名字，如果是客户门户来下载，则没有记录　for TD:1644
                            String userType = user.getLogintype();
                            if ("1".equals(userType)) {   //如果是内部用户　名称就是　lastName 外部则入在　firstName里面
                                downloadLog(user.getUID(), user.getLastname(), fileid, filename,ipstring);
                            } else {
                                downloadLog(user.getUID(), user.getFirstname(), fileid, filename,ipstring);
                            }

                        }
                    }
                    //记录下载日志 end
                }


                countDownloads(""+fileid);
            }
        } catch (Exception e) {
            BaseBean basebean = new BaseBean();
            basebean.writeLog(e);
        } //错误处理
        }
        }else{
            String urlstr = req.getParameter("urlstr");
            res.addHeader("Cache-Control", "private, max-age=8640000");
            
            InputStream imagefile = null;
            URL url = new URL(urlstr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            imagefile = conn.getInputStream();
            
            ServletOutputStream outputStream = res.getOutputStream();

            byte data[] = new byte[1024];
            int byteread;
            
            while ((byteread = imagefile.read(data)) != -1) {
                outputStream.write(data, 0, byteread);                  
                outputStream.flush();
            }
            
            outputStream.close();
            imagefile.close();
        }
    }

    /**
     * 记录下载次数，根据传入的参数 countdownloads == 1
     *
     */
    private void countDownloads(String fileid) {
        if (this.isCountDownloads) {

            RecordSet rs = new RecordSet();
            String sqlStr = "UPDATE ImageFile Set downloads=downloads+1 WHERE imagefileid = " + fileid;
            //System.out.println("sqlStr ==" + sqlStr);
            rs.execute(sqlStr);
        }
    }

    /**
     * 记录下载日志
     * @param userid 下载的用户id
     * @param userName 下载的用户名称
     * @param imageid 下载的文档id
     * @param imageName 下载的文档文件名称
     */
    private void downloadLog(int userid, String userName, int imageid, String imageName,String ipstring) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(new Date());  
       
        RecordSet rs = new RecordSet();
        String sql = "select t2.id,t2.docsubject from DocImageFile t1, DocDetail t2 where t1.docid=t2.id and t1.docfiletype<>1 and t1.imagefileid = "+imageid;
        int docid = -1;
        String docName = "";
        rs.executeSql(sql);
        if(rs.next()){
            docid = rs.getInt(1);
            docName = rs.getString(2);
            sql = "insert into DownloadLog(userid, username, downloadtime, imageid, imagename, docid, docname,clientaddress) values(" + userid + ",'" + Util.toHtml100(userName) + "','" + time + "'," + imageid + ",'" + Util.toHtml100(imageName) + "',"+docid+",'"+Util.toHtml100(docName)+"','"+ipstring+"')";
            rs.executeSql(sql);
        }

    }
    
    /**
     * 判断是否有下载特定文件的权限
     * @param fileId 文件id
     * @param req 请求
     * @param res 响应
     * 
     * @return boolean  true:有下载的权限  false:没有下载的权限
     */
    private boolean getWhetherHasRight(String fileId,HttpServletRequest req, HttpServletResponse res,int requestid) throws Exception  {

        //安全性检查
        if(fileId==null||fileId.trim().equals("")){
            return false;
        }

        RecordSet rs = new RecordSet();
        //是否必须授权     1：是   0或其他：否
        String mustAuth=Util.null2String(rs.getPropValue("FileDownload","mustAuth"));           
        //boolean hasRight=false;
        boolean hasRight=true; 
        if(mustAuth.equals("1")){
            hasRight=false;
        }       
        boolean isDocFile=false;
        
        //文档模块  附件查看权限控制  开始
        String docId=null;
        List docIdList=new ArrayList();
		RecordSet rs2 = new RecordSet();
		rs2.executeSql("select  imagefilename from ImageFile where imageFileId="+fileId);
		String extName="";
		boolean isExtfile=false;
		if(rs2.next()){
    		 String  filename = Util.null2String(rs2.getString("imagefilename"));			       	    	
				if(filename.indexOf(".") > -1){
					int bx = filename.lastIndexOf(".");
					if(bx>=0){
						extName = filename.substring(bx+1, filename.length());						
					}
				}						
		}

		if( "xls".equalsIgnoreCase(extName)||"xlsx".equalsIgnoreCase(extName) || "doc".equalsIgnoreCase(extName)|| "docx".equalsIgnoreCase(extName)||"wps".equalsIgnoreCase(extName)||"ppt".equalsIgnoreCase(extName)||"pptx".equalsIgnoreCase(extName)) {		
		isExtfile=true;
		}
        rs.executeSql("select  docId from docImageFile where imageFileId="+fileId);
        while(rs.next()){
            hasRight=false;
            docId=rs.getString(1);
            if(docId!=null&&!docId.equals("")){
                docIdList.add(docId);
            }
        }
        String comefrom="";     
        if(docIdList.size()==0){
            int fileId_related=0;
            int docId_related=0;
            boolean hasDocId=false;
            rs.executeSql("select comefrom from ImageFile where imageFileId="+fileId);
            if(rs.next()){
                comefrom=Util.null2String(rs.getString("comefrom"));
            }   
            String comefrom_noNeedLogin=Util.null2String(rs.getPropValue("FileDownload","comefrom_noNeedLogin"));           
            if((","+comefrom_noNeedLogin+",").indexOf(","+comefrom+",")>=0){
                hasRight=true;
                return hasRight;
            }   
            
            if(comefrom.equals("DocPreview")||comefrom.equals("DocPreviewHistory")){
                rs.executeSql("select imageFileId,docId from "+comefrom+"  where (pdfFileId="+fileId+" or swfFileId="+fileId+") order by id desc");
                if(rs.next()){
                    fileId_related=Util.getIntValue(rs.getString("imageFileId"),0);
                    docId_related=Util.getIntValue(rs.getString("docId"),0);                
                }
                if(docId_related>0){
                    docIdList.add(""+docId_related);
                    hasDocId=true;
                }
            }else if(comefrom.equals("DocPreviewHtml")||comefrom.equals("DocPreviewHtmlHistory")){
                rs.executeSql("select imageFileId,docId from "+comefrom+"  where  htmlFileId="+fileId+" order by id desc");
                if(rs.next()){
                    fileId_related=Util.getIntValue(rs.getString("imageFileId"),0);
                    docId_related=Util.getIntValue(rs.getString("docId"),0);                
                }
                if(docId_related>0){
                    docIdList.add(""+docId_related);
                    hasDocId=true;
                }               
            }else if(comefrom.equals("DocPreviewHtmlImage")){
                rs.executeSql("select imageFileId,docId from DocPreviewHtmlImage  where  picFileId="+fileId+"  order by id desc");
                if(rs.next()){
                    fileId_related=Util.getIntValue(rs.getString("imageFileId"),0);
                    docId_related=Util.getIntValue(rs.getString("docId"),0);                
                }
                if(docId_related>0){
                    docIdList.add(""+docId_related);
                    hasDocId=true;
                }                   
            }
            if(!hasDocId&&fileId_related>0){
                rs.executeSql("select  docId from docImageFile where imageFileId="+fileId_related);
                while(rs.next()){
                    docId=rs.getString(1);
                    if(docId!=null&&!docId.equals("")){
                        docIdList.add(docId);
                    }
                }               
            }

        }
        if(docIdList.size()>0){
            hasRight=false;         
        }
        String mustLogin=Util.null2String(rs.getPropValue("FileDownload","mustLogin"));
        int votingId=Util.getIntValue(req.getParameter("votingId"),0);
        User user = (User)req.getSession(true).getAttribute("weaver_user@bean") ;
        if(user==null){
            if(mustLogin.equals("1")){
                hasRight=false; 
            }
            return hasRight;
        }           
        String f_weaver_belongto_userid=Util.null2String(req.getParameter("f_weaver_belongto_userid"));//需要增加的代码
        String f_weaver_belongto_usertype=Util.null2String(req.getParameter("f_weaver_belongto_usertype"));//需要增加的代码
        user = HrmUserVarify.getUser(req, res, f_weaver_belongto_userid, f_weaver_belongto_usertype) ;//需要增加的代码
        String workplanid=Util.null2String(req.getParameter("workplanid"));
        if(user==null){
            user = (User)req.getSession(true).getAttribute("weaver_user@bean") ;
        }
        if(user==null){
            if(mustLogin.equals("1")){
                hasRight=false; 
            }           
            return hasRight;
        }
        String comefrom_noNeedAuth=Util.null2String(rs.getPropValue("FileDownload","comefrom_noNeedAuth"));
        if((","+comefrom_noNeedAuth+",").indexOf(","+comefrom+",")>=0){
            hasRight=true;
            return hasRight;
        }       
        
        DocManager docManager=new DocManager();
        String docStatus="";
        int isHistory=0;
        int secCategory=0;
        String docPublishType="";//文档发布类型  1:正常(不发布)  2:新闻  3:标题新闻

        for(int i=0;i<docIdList.size()&&!hasRight;i++){
            docId=(String)docIdList.get(i);

            isDocFile=true;
            
            if(docId==null||docId.trim().equals("")){
                continue;
            }
            docManager.resetParameter();
            docManager.setId(Integer.parseInt(docId));
            docManager.getDocInfoById();
            
            docStatus=docManager.getDocstatus();            
            isHistory = docManager.getIsHistory();
            secCategory=docManager.getSeccategory();    
            docPublishType=docManager.getDocpublishtype();
            
            if(docPublishType!=null&&(docPublishType.equals("2")||docPublishType.equals("3"))){
                String newsClause="";
                String sqlDocExist=" select 1 from DocDetail where id="+docId+" "; 
                String sqlNewsClauseOr="";
                boolean hasOuterNews=false;             
                
                rs.executeSql("select newsClause from DocFrontPage where publishType='0'");
                while(rs.next()){
                    hasOuterNews=true;                  
                    newsClause=Util.null2String(rs.getString("newsClause"));
                    if (newsClause.equals(""))
                    {
                        //newsClause=" 1=1 ";
                        hasRight=true;
                        break;
                    }
                    if(!newsClause.trim().equals("")){
                        //sqlDocExist+=" and "+newsClause;
                        sqlNewsClauseOr+=" ^_^ ("+newsClause+")";                       
                    }
                }
                ArrayList newsArr = new ArrayList();
                if(!sqlNewsClauseOr.equals("")&&!hasRight){
                    //sqlNewsClauseOr=sqlNewsClauseOr.substring(sqlNewsClauseOr.indexOf("("));
                    //sqlDocExist+=" and ("+sqlNewsClauseOr+") ";
                    String[] newsPage = Util.TokenizerString2(sqlNewsClauseOr,"^_^");
                    int k = 0;
                    String newsWhere = "";                  
                    for(;k<newsPage.length;k++){
                        if(k%10==0){
                            newsArr.add(newsWhere);
                            newsWhere="";
                            newsWhere+=newsPage[k];
                        }else
                            newsWhere+=" or "+newsPage[k];  
                    }
                    newsArr.add(newsWhere);
                }
                //System.out.print(sqlDocExist);
                if(hasOuterNews&&!hasRight){
                    for(int j=1;j<newsArr.size();j++){  
                        String newsp = newsArr.get(j).toString();                       
                        if(j==1)
                            newsp = newsp.substring(newsp.indexOf("or")+2);
                        sqlDocExist+="and("+newsp+")";
                        rs.executeSql(sqlDocExist);
                        sqlDocExist = " select 1 from DocDetail where id="+docId+" "; 
                        if(rs.next()){
                            hasRight=false;
                            break;
                        }
                    }
                }               
                
            }
            if(user==null){
                continue;
            }
            String userId=""+user.getUID();
            String loginType = user.getLogintype();
            String userSeclevel = user.getSeclevel();
            String userType = ""+user.getType();
            String userDepartment = ""+user.getUserDepartment();
            String userSubComany = ""+user.getUserSubCompany1();
            
            String userInfo=loginType+"_"+userId+"_"+userSeclevel+"_"+userType+"_"+userDepartment+"_"+userSubComany;
            
            ArrayList PdocList = null;

            SpopForDoc  spopForDoc=new SpopForDoc();
            PdocList = spopForDoc.getDocOpratePopedom(""+docId,userInfo);               

                
            SecCategoryComInfo secCategoryComInfo=new SecCategoryComInfo();
            
            //0:查看  
            boolean canReader = false;
            //1:编辑
    		boolean canEdit = false;
    		//5:下载
            if (((String)PdocList.get(0)).equals("true")) {canReader = true ;}
    		if (((String)PdocList.get(1)).equals("true")) {canEdit = true ;}
    		if (((String)PdocList.get(5)).equals("true")) {hasRight = true ;}//TD12005
            String readerCanViewHistoryEdition=secCategoryComInfo.isReaderCanViewHistoryEdition(secCategory)?"1":"0";
            
            if(canReader && ((!docStatus.equals("7")&&!docStatus.equals("8")) 
                    ||(docStatus.equals("7")&&isHistory==1&&readerCanViewHistoryEdition.equals("1"))
                  )){
                canReader = true;
            }else{
                canReader = false;
            }
            
            if(isHistory==1) {
                if(secCategoryComInfo.isReaderCanViewHistoryEdition(secCategory)){
                    if(canReader && !canEdit) canReader = true;
                } else {
                    if(canReader && !canEdit) canReader = false;
                }
            }   
            
            if(canEdit && ((docStatus.equals("3") || docStatus.equals("5") || docStatus.equals("6") || docStatus.equals("7")) || isHistory==1)) {
                canEdit = false;
                canReader = true;
            }
            
            if(canEdit && (docStatus.equals("0") || docStatus.equals("1") || docStatus.equals("2") || docStatus.equals("7")) && (isHistory!=1))
                canEdit = true;
            else
                canEdit = false;
    		if(!isExtfile&&canReader){
                hasRight=true;
            }
            if(!canReader)  {//如果没有查看权限，判断是否通过协作区赋权
                int desrequestid = Util.getIntValue(req.getParameter("desrequestid"));
                int wfdesrequestid = Util.getIntValue(String.valueOf(req.getSession().getAttribute("desrequestid")),0);
                //System.out.println("wfdesrequestid = "+wfdesrequestid);
                int coworkid = Util.getIntValue(req.getParameter("coworkid"));
                CoworkDAO coworkDAO=new CoworkDAO(coworkid);
                VotingManager votingManager=new VotingManager();
                Map parameterMap=new HashMap();
                    parameterMap.put("docId",Util.getIntValue(docId));
                    parameterMap.put("votingId",votingId);
                    parameterMap.put("userId",user.getUID());
                //微博下载权限 
                BlogDao blogDao=new BlogDao();
                int blogDiscussid = Util.getIntValue(req.getParameter("blogDiscussid"),0);
                WFUrgerManager wfum=new WFUrgerManager();
                RequestAnnexUpload rau=new RequestAnnexUpload();
                if (!wfum.OperHaveDocViewRight(requestid,user.getUID(),Util.getIntValue(loginType,1),""+docId)&&!coworkDAO.haveRightToViewDoc(userId,docId)&&!votingManager.haveViewVotingDocRight(parameterMap)
                  &&!wfum.OperHaveDocViewRight(requestid,desrequestid,Util.getIntValue(userId),Util.getIntValue(loginType),""+docId)                        
                  &&!wfum.getWFShareDesRight(requestid,wfdesrequestid,user,Util.getIntValue(loginType),""+docId)
                  &&!wfum.UrgerHaveDocViewRight(requestid,Util.getIntValue(userId),Util.getIntValue(loginType),""+docId)
                  &&!wfum.getMonitorViewObjRight(requestid,Util.getIntValue(userId),""+docId,"0")
                  &&!wfum.getWFShareViewObjRight(requestid,user,""+docId,"0")
                  &&!rau.HaveAnnexDocViewRight(requestid,Util.getIntValue(userId),Util.getIntValue(loginType),Util.getIntValue(docId))&&!blogDao.appViewRight("doc",userId,Util.getIntValue(docId,0),blogDiscussid)){
                    hasRight=false;
                } else {
                    hasRight = true ;
    			}
    		}
            if(!canReader&&!hasRight)  {//如果没有查看权限，判断是否通过会议赋权
                MeetingUtil MeetingUtil = new MeetingUtil();
                if (!MeetingUtil.UrgerHaveMeetingDocViewRight(user,Util.getIntValue(loginType),""+docId)){
                    hasRight=false;
                } else {
                    hasRight = true ;
    			}
    		}
            
            if(!canReader&&!hasRight)  {//如果没有查看权限，判断是否通过日程赋权
                WorkPlanService workPlanService = new WorkPlanService();
                if (!workPlanService.UrgerHaveWorkplanDocViewRight(workplanid,user,Util.getIntValue(loginType),""+docId)){
                    hasRight=false;
                } else {
                    hasRight = true ;
    			}
    		}
            //判断是否计划任务赋权
            String fromworktask = Util.getFileidIn(Util.null2String(req.getParameter("fromworktask")));
            String operatorid = Util.getFileidIn(Util.null2String(req.getParameter("operatorid")));
            if("1".equals(fromworktask)) {
                WTRequestUtil WTRequestUtil = new WTRequestUtil();
                if(!canReader&&!hasRight)  {
                    if(!WTRequestUtil.UrgerHaveWorktaskDocViewRight(requestid,Util.getIntValue(userId),Util.getIntValue(docId,0),Util.getIntValue(operatorid,0))) {
                        hasRight=false;
                    } else {
                        hasRight = true ;
                    }
                } else {
                    hasRight=true;
                }
            }
            //如果没有权限，看到是否具有客户联系查看权限
            if(!canReader&&!hasRight)  {//如果没有查看权限，判断是否通过会议赋权
                CrmShareBase crmshare = new CrmShareBase();
                String crmid = Util.null2String(req.getParameter("crmid"));
                int sharetype = crmshare.getRightLevelForCRM(userId+"",crmid,loginType);
                String crmtype = Util.null2String(req.getParameter("crmtype"));
                if(crmtype == null || "".equals(crmtype))crmtype = "0";
                if (sharetype > 0 && crmshare.checkCrmFileExist(crmid, fileId, crmtype)){
                    CoworkDAO coworkDAO = new CoworkDAO();
                    coworkDAO.shareCoworkRelateddoc(Util.getIntValue(loginType),Util.getIntValue(docId,0),Util.getIntValue(userId));
                    hasRight= true;
                } else {
                    hasRight = false;
    			}
    		}
        }
        //文档模块  附件查看权限控制  结束        
        
        //检查社交平台附加权限
        hasRight=SocialIMService.checkFileRight(user, fileId, isDocFile, hasRight);
    	if(!hasRight) {//查看是否有建模关联授权
    		//表单建模判断关联授权
    		String formmodeflag = StringHelper.null2String(req.getParameter("formmode_authorize"));
    		Map<String,String> formmodeAuthorizeInfo = new HashMap<String,String>();
    		//formmodeparas+="&formmode_authorize="+formmodeflag;
    		if(formmodeflag.equals("formmode_authorize")){
    			int modeId = 0;
    			int formmodebillId = 0;
    			int fieldid = 0;
    			int formModeReplyid = 0;
    			modeId = Util.getIntValue(req.getParameter("authorizemodeId"),0);
    			formmodebillId = Util.getIntValue(req.getParameter("authorizeformmodebillId"),0);
    			fieldid = Util.getIntValue(req.getParameter("authorizefieldid"),0);
    			formModeReplyid = Util.getIntValue(req.getParameter("authorizeformModeReplyid"),0);
    			String fMReplyFName = Util.null2String(req.getParameter("authorizefMReplyFName"));
    			ModeRightInfo modeRightInfo = new ModeRightInfo();
    			modeRightInfo.setUser(user);
    			if(formModeReplyid!=0){
    				formmodeAuthorizeInfo = modeRightInfo.isFormModeAuthorize(formmodeflag, modeId, formmodebillId, fieldid, Util.getIntValue(docId), formModeReplyid,fMReplyFName);
    			}else{
    				formmodeAuthorizeInfo = modeRightInfo.isFormModeAuthorize(formmodeflag, modeId, formmodebillId, fieldid, Util.getIntValue(docId));
    			}
    		}
    		
    		if("1".equals(formmodeAuthorizeInfo.get("AuthorizeFlag"))){//如果是表单建模的关联授权，那么直接有查看权限
    			hasRight = true;
    		}
    	}
        return hasRight;
    }    
    
    //  下载公用方法 
  
  public void toUpload(HttpServletResponse response,String str){
    toUpload(response,str,str); 
  }  
    
    public void toUpload(HttpServletResponse response,String str, String tmptfilename){ 
       try { 
        
            SystemComInfo syscominfo = new SystemComInfo();
            String path= syscominfo.getFilesystem();
            if("".equals(path)){
                path = GCONST.getRootPath();
                path = path + "filesystem" + File.separatorChar+ "downloadBatch"+File.separatorChar+tmptfilename;
            }else{
                if(path.endsWith(File.separator)){
                    path += "downloadBatch"+File.separatorChar+File.separatorChar+tmptfilename;
                    
                }else{
                    path += File.separator+ "downloadBatch"+File.separatorChar+File.separatorChar+tmptfilename;
                }
            }       
        
        //String path=GCONST.getRootPath()+ "downloadBatch"+File.separatorChar+str; 
        //String path="E:/bjls_ecology4.1_5.0/ecology/downloadBatch/"+str; 
        if(!"".equals(path)){ 
         File file=new File(path); 
         if(file.exists()){ 
          InputStream ins=null;
          BufferedInputStream bins=null;
          OutputStream outs=null;
          BufferedOutputStream bouts=null;
          try
          {
            
          ins=new FileInputStream(path); 
          bins=new BufferedInputStream(ins);//放到缓冲流里面 
          outs=response.getOutputStream();//获取文件输出IO流 
          bouts=new BufferedOutputStream(outs); 
               response.setContentType("application/x-download");//设置response内容的类型 
               if((agent.contains("Firefox")||agent.contains(" Chrome")||agent.contains("Safari") )&& !agent.contains("Edge")){
                   response.setHeader("content-disposition", "attachment; filename=\"" +  new String(str.replaceAll("<", "").replaceAll(">", "").replaceAll("&lt;", "").replaceAll("&gt;", "").getBytes("UTF-8"),"ISO-8859-1")+"\"");
               }else{
                   response.setHeader("content-disposition", "attachment; filename=\"" + URLEncoder.encode(str.replaceAll("<", "").replaceAll(">", "").replaceAll("&lt;", "").replaceAll("&gt;", ""),"UTF-8").replaceAll("\\+", "%20")+"\"");
               }
               int bytesRead = 0; 
               byte[] buffer = new byte[8192]; 
               //开始向网络传输文件流 
               while ((bytesRead = bins.read(buffer, 0, 8192)) != -1) { 
                   bouts.write(buffer, 0, bytesRead); 
               } 
               bouts.flush();//这里一定要调用flush()方法 
               ins.close(); 
               bins.close(); 
               outs.close(); 
               bouts.close(); 
           }
          catch (Exception ef)
          {
              new weaver.filter.XssUtil().writeError(ef);
          }
          finally {
                        
                if(ins!=null) ins.close();
                if(bins!=null) bins.close();
                if(outs!=null) outs.close();
                if(bouts!=null) bouts.close();
                    }

         } 
         else{ 
          response.sendRedirect("/login/BatchDownloadsEror.jsp"); 
         } 
        } 
        else{ 
         response.sendRedirect("/login/BatchDownloadsEror.jsp"); 

    //注;这里面不要用到PrintWriter out=response.getWriter();这里调用了response对象，后面下载调用时就会出错。这里要是想都用，希望大家找到解决办法。 
        } 
       } catch (IOException e) { 
        e.printStackTrace(); 
       } 
    } 
    
    /**  
     *3：打包完成后删除原来的目中的文件 
     * @param 
     * @param   
     * @throws Exception  
     */ 
  	public  void deleteFile(String targetPath) throws IOException {   
		 File targetFile = new File(targetPath);   
		 if (targetFile.isDirectory()) { 
			if(targetPath.indexOf("downloadBatchTemp")>-1){
		      FileUtils.deleteDirectory(targetFile);   
			}
		 } else if (targetFile.isFile()) {   
		  targetFile.delete();   
		 }   
		}  
    /*
     * 
     */
    public boolean addDownLoadLogByimageId( int fileid){
        boolean needUser = true;
        int docId = 0;
        String docIdsForOuterNews = "";
        String strSql = "select id from DocDetail where exists (select 1 from docimagefile where imagefileid=" + fileid + " and docId=DocDetail.id) and ishistory <> 1 and (docPublishType='2' or docPublishType='3')";
        RecordSet rs = new RecordSet();
        rs.executeSql(strSql);
        while (rs.next()) {
            docId = rs.getInt("id");
            if (docId > 0) {
                docIdsForOuterNews += "," + docId;
            }
        }

        if (!docIdsForOuterNews.equals("")) {
            docIdsForOuterNews = docIdsForOuterNews.substring(1);
        }

        if (!docIdsForOuterNews.equals("")) {
            String newsClause = "";
            String sqlDocExist = " select 1 from DocDetail where id in(" + docIdsForOuterNews + ") ";
            String sqlNewsClauseOr = "";
            boolean hasOuterNews = false;

            rs.executeSql("select newsClause from DocFrontPage where publishType='0'");
            while (rs.next()) {
                hasOuterNews = true;
                newsClause = Util.null2String(rs.getString("newsClause"));
                if (newsClause.equals("")) {
                    // newsClause=" 1=1 ";
                    needUser = false;
                    break;
                }
                if (!newsClause.trim().equals("")) {
                    sqlNewsClauseOr += " ^_^ (" + newsClause + ")";
                }
            }
            ArrayList newsArr = new ArrayList();
            if (!sqlNewsClauseOr.equals("") && needUser) {
                // sqlNewsClauseOr=sqlNewsClauseOr.substring(sqlNewsClauseOr.indexOf("("));
                // sqlDocExist+=" and ("+sqlNewsClauseOr+") ";
                String[] newsPage = Util.TokenizerString2(sqlNewsClauseOr, "^_^");
                int i = 0;
                String newsWhere = "";
                for (; i < newsPage.length; i++) {
                    if (i % 10 == 0) {
                        newsArr.add(newsWhere);
                        newsWhere = "";
                        newsWhere += newsPage[i];
                    } else
                        newsWhere += " or " + newsPage[i];
                }
                newsArr.add(newsWhere);
            }
            // System.out.print(sqlDocExist);
            if (hasOuterNews && needUser) {
                for (int j = 1; j < newsArr.size(); j++) {
                    String newsp = newsArr.get(j).toString();
                    if (j == 1)
                        newsp = newsp.substring(newsp.indexOf("or") + 2);
                    sqlDocExist += "and(" + newsp + ")";
                    rs.executeSql(sqlDocExist);
                    sqlDocExist = " select 1 from DocDetail where id in(" + docIdsForOuterNews + ") ";
                    if (rs.next()) {
                        needUser = false;
                        break;
                    }
                }
            }
        }

        // 处理外网查看默认图片
        rs.executeSql("SELECT * FROM DocPicUpload  WHERE  Imagefileid=" + fileid);
        if (rs.next()) {
            needUser = false;
        }

        return needUser;
    }
	/**
     * creat Folder and File.
     * @param String fileFoder, String fileName
     * Copyright (c) 2010
     * Company weaver
     * Create Time 2010-11-25
     * @author caizhijun001
     * @return File
     */
    public File fileCreate(String fileFoder, String fileName){
          File foder = new File(fileFoder);//E:\dlxdq_ecology4.5\ecology\filesystem\201012\S  1450623393_.xls
          File file = new File(fileFoder+fileName);
                //如果文件夹不存在，则创建文件夹
          if(foder.exists()==false){
                 foder.mkdirs();//多级目录
                   //foder.mkdir();//只创建一级目录
           }
          
          if(file.exists()==true){//删除以前同名的txt文件
             try{
                 file.delete();
            }catch(Exception e){
                   e.printStackTrace();
           }
          }
          //如果文件不存在，则创建文件
          if(file.exists()==false){
             try{
                       file.createNewFile();
                }catch(IOException e){
                       e.printStackTrace();
               }
          }
          return file;
  }
    
    private boolean isMsgObjToDocument(){
        boolean isMsgObjToDocument=true;
        
        BaseBean basebean = new BaseBean();
        String mClientName=Util.null2String(basebean.getPropValue("weaver_obj","iWebOfficeClientName"));
        boolean isIWebOffice2003 = (mClientName.indexOf("iWebOffice2003")>-1)?true:false;
        String isHandWriteForIWebOffice2009=Util.null2String(basebean.getPropValue("weaver_obj","isHandWriteForIWebOffice2009"));
        if(isIWebOffice2003||isHandWriteForIWebOffice2009.equals("0")){
            isMsgObjToDocument=false;
        }
        
        return isMsgObjToDocument;
    }
}
