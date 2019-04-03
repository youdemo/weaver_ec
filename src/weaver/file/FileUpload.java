package weaver.file;


/**
 * Title:        hpsales
 * Description:  for hp sales system
 * Copyright:    Copyright (c) 2001
 * Company:      weaver
 * @author liuyu, Charoes Huang
 * @version 1.0 ,2004-6-25
 */

import gvo.passwd.GvoServiceFile; 
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException; 
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

import weaver.aes.AES;
import weaver.alioss.AliOSSObjectManager;
import weaver.conn.RecordSet;
import weaver.docs.docs.ImageFileIdUpdate;
import weaver.email.MailCommonUtils;
import weaver.email.service.MailAliOSSService;
import weaver.file.multipart.DefaultFileRenamePolicy;
import weaver.file.multipart.MultipartRequest;
import weaver.file.multipart.UploadedFile;
import weaver.file.util.PicCompression;
import weaver.filter.XssUtil;
import weaver.general.BaseBean;
import weaver.general.GCONST;
import weaver.general.StaticObj;
import weaver.general.Util;
import weaver.system.SystemComInfo;

public class FileUpload  extends BaseBean
{
	private static ImageFileIdUpdate imageFileIdUpdate = new ImageFileIdUpdate();	
	
    
    private MultipartRequest mpdata = null ;
    HttpServletRequest request = null ;
    private String[] filenames=null;
    private InputStream source = null;
    private ArrayList filesizes=new ArrayList() ;
    private ArrayList imagewidth=new ArrayList() ;
    private ArrayList imageheight=new ArrayList() ;
    private ArrayList delfilerealpaths=new ArrayList() ;
    private int mailid ;
    private boolean needimagewidth = false ;
    private boolean needzip = false ;
    private boolean needzipencrypt = false ;
    private String isaesencrypt = "0";
    private String aescode ="";
    private String remoteAddr="";
    private XssUtil xss = null;
    private AES aes = null;
    
    public FileUpload(HttpServletRequest request)   {
    	this.remoteAddr=request.getRemoteAddr();
        if (isMultipartData(request)) mpdata = getAttachment(request);
        this.request = request ;
        this.xss = new XssUtil();
        aes = new AES();
    }
    
    public FileUpload(HttpServletRequest request,String encode)   {
    	this.remoteAddr=request.getRemoteAddr();
        if (isMultipartData(request)) mpdata = getAttachment(request,encode);
        this.request = request ;
        this.xss = new XssUtil();
        aes = new AES();
    }
    
    public FileUpload(HttpServletRequest request,String encode,boolean iszip ,String isEmail)   {
    	this.remoteAddr=request.getRemoteAddr();
        if (isMultipartData(request) && "1".equals(isEmail)) mpdata = getEmailAttachment(request,encode,iszip);
        this.request = request ;
        this.xss = new XssUtil();
        aes = new AES();
    }
    
    public FileUpload(HttpServletRequest request,String encode,boolean iszip)   {
    	this.remoteAddr=request.getRemoteAddr();
        if (isMultipartData(request)) mpdata = getAttachment(request,encode,iszip);
        this.request = request ;
        this.xss = new XssUtil();
        aes = new AES();
    }
    public FileUpload(HttpServletRequest request,String encode,boolean iszip,boolean isoriginal)   {
    	this.remoteAddr=request.getRemoteAddr();
        if (isMultipartData(request)) mpdata = getAttachment(request,encode,iszip,isoriginal);
        this.request = request ;
        this.xss = new XssUtil();
        aes = new AES();
    }
    //modify by mackjoe at 2005-11-28 td3282 获得request对象
    public HttpServletRequest getRequest(){
        return request;
    }

//for license upload by chenyingjie 2003-06-26
    public FileUpload(HttpServletRequest request,boolean iszip)   {
    	this.remoteAddr=request.getRemoteAddr();
        if (isMultipartData(request)) mpdata = getAttachment(request,iszip);
        this.request = request ;
        this.xss = new XssUtil();
        aes = new AES();
    }
    
    public FileUpload(HttpServletRequest request,boolean iszip,boolean isaesencrypt)   {
    	this.remoteAddr=request.getRemoteAddr();
        if (isMultipartData(request)) mpdata = getAttachment(request,iszip,isaesencrypt);
        this.request = request ;
        this.xss = new XssUtil();
        aes = new AES();
    }


	//for homepage image upload by dongping 2006-8-24
    public FileUpload(HttpServletRequest request,boolean iszip,String strDirAddr)   {
    	this.remoteAddr=request.getRemoteAddr();
        if (isMultipartData(request)) mpdata = getAttachment(request,iszip,strDirAddr);
        this.request = request ;
        this.xss = new XssUtil();
        aes = new AES();
    }

	//html模板图片保存
	public FileUpload(HttpServletRequest request,boolean iszip,boolean encoding,String strDirAddr)   {
    	this.remoteAddr=Util.getIpAddr(request);
        if (isMultipartData(request)) mpdata = getAttachment(request,iszip,encoding,strDirAddr);
        this.request = request ;
        this.xss = new XssUtil();
        aes = new AES();
    }
    
	public Hashtable getUploadImgNames(){
		String el="",imgpath="",imgname="";
		Hashtable ht = new Hashtable();
		for(Enumeration e=mpdata.getFileUploadNames();e.hasMoreElements();){ 
			el = (String)e.nextElement();
			if(el.indexOf("docimages_")==-1) continue;
			imgpath = Util.null2String(mpdata.getFilePath(el));
			imgname = Util.null2String(mpdata.getFileName(el));
			if(imgpath.equals("") || imgname.equals("")) continue;
			String elNumber = el.substring(el.indexOf("_")+1,el.length());
			ht.put(elNumber, imgpath+imgname);
			
		}
		return ht;
	}
	
	public Hashtable getUploadFileNames() {
		String el="",imgpath="",imgname="";
		Hashtable ht = new Hashtable();
		for(Enumeration e=mpdata.getFileUploadNames();e.hasMoreElements();){ 
			el = (String)e.nextElement();
			UploadedFile uploadedFile = mpdata.getUploadedFile(el);
			ht.put(el, uploadedFile);
		}
		return ht;
	}

    //modify by xhheng @20050315 for 流程附件上传
    public String getRemoteAddr(){
    	return remoteAddr;
    }
    
    public String getParameter(String key) throws RuntimeException   {
        if (!isMultipartData(request)) return Util.null2String(request.getParameter(key)) ;
        if (mpdata == null) return "";
//        return Util.null2String(mpdata.getParameter(key)) ;

        try {
        	String value = Util.null2String(mpdata.getParameter(key));
        	if(!value.equals("")){//特殊参数需要从paramsMap中获取
        		if(value.startsWith(XssUtil.__RANDOM__)){
        			//xssUtil.cacheKey(Util.null2String(Thread.currentThread().hashCode()),value);
        			return xss.get(value);
        		}
        	}
            return aes.decrypt(key, new String ( value.getBytes("ISO8859_1") , "UTF-8"),request);
        }catch(Exception ex) {
        	return "" ;
        }
    }


    public String getParameter2(String key)   {
        if (!isMultipartData(request)) return Util.null2String(request.getParameter(key)) ;
        if (mpdata == null) return "";
        String value = Util.null2String(mpdata.getParameter(key)) ;
        return aes.decrypt(key, value,request);
    }
    
    /**
     * 本方法返回值不会对null进行特殊处理，如果是null则直接返回null，
     * 如果是空字符串("")则直接返回空字符串("")。
     * @param key
     * @return
     */
    public String getParameter3(String key){
      if (!isMultipartData(request)){
        return request.getParameter(key);
      }
      if (mpdata == null){
        return null;
      }

      String value = mpdata.getParameter(key);
      if(value == null){
        return null;
      }
      
      try {
    	  return aes.decrypt(key, new String ( value.getBytes("ISO8859_1") , "UTF-8"),request);
      }catch(Exception ex) {
        return value;
      }
    }

    public String[] getParameters(String key) {
        if (!isMultipartData(request)) return request.getParameterValues(key) ;
        if (mpdata == null) return null;
        String[] values = mpdata.getParameterValues(key);
        if(values==null||values.length==0)return values;
        if(aes.isEnable()){
	        String[] copyValues = new String[values.length];
	        for(int i = 0;i<values.length;i++){
	        	copyValues[i] = aes.decrypt(key,values[i],request);
	        }
	        return copyValues;
        }
        return values;
    }

    public Enumeration getParameterNames() {
    	 if (!isMultipartData(request)) return request.getParameterNames() ;
         if (mpdata == null) return null;
        return mpdata.getParameterNames();      
    }
    
    public String[] getParameterValues(String name) {
    	if (!isMultipartData(request)) return request.getParameterValues(name) ;
        if (mpdata == null) return null;
        String[] values = mpdata.getParameterValues(name);
        if(values==null||values.length==0)return values;
        if(aes.isEnable()){
	        String[] copyValues = new String[values.length];
	        for(int i = 0;i<values.length;i++){
	        	copyValues[i] = aes.decrypt(name, values[i],request);
	        }
	        return copyValues;
        }
        return values;
    }
    
    public String[] getParameterValues2(String name) {
    	String[]  values= null;
		if (!isMultipartData(request)){
		 	values = request.getParameterValues(name) ;
			String[]  multivalues = new String[values.length];
	    	try {
				for(int i=0;i<values.length;i++){
					multivalues[i] =  aes.decrypt(name, new String ( Util.null2String(values[i]).getBytes("ISO8859_1") , "UTF-8"),request);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return multivalues;
		}
        if (mpdata == null) return null;    
		values =  mpdata.getParameterValues(name);      
        String[]  multivalues = new String[values.length];
    	try {
			for(int i=0;i<values.length;i++){
				multivalues[i] =  aes.decrypt(name,new String ( Util.null2String(values[i]).getBytes("ISO8859_1") , "UTF-8"),request);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return multivalues;
    }
    

    public String getFileName() {
        return this.filenames[0].replace("'", "’");
    }

    public String[] getFileNames() {
        return this.filenames;
    }

    public void setFileNames(String[] filenames) {
        this.filenames=filenames;
    }

    public int getFileSize() {
        return Util.getIntValue((String)this.filesizes.get(0));
    }

    public int[] getFileSizes() {
        int[] filesizearr = new int[filesizes.size()];
        for(int i=0 ; i< filesizes.size() ; i++)  filesizearr[i]=Util.getIntValue((String)this.filesizes.get(i));
        return filesizearr;
    }

    public void setFileSizes(ArrayList filesizes) {
        this.filesizes=filesizes;
    }

    public int getWidth() {
        return Util.getIntValue((String)this.imagewidth.get(0));
    }

    public int getHeight() {
        return Util.getIntValue((String)this.imageheight.get(0));
    }

    public int[] getWidths() {
        int[] imagewidtharr = new int[imagewidth.size()];
        for(int i=0 ; i< imagewidth.size() ; i++)  imagewidtharr[i]=Util.getIntValue((String)this.imagewidth.get(i));
        return imagewidtharr;
    }

    public int[] getHeights() {
        int[] imageheightarr = new int[imageheight.size()];
        for(int i=0 ; i< imageheight.size() ; i++)  imageheightarr[i]=Util.getIntValue((String)this.imageheight.get(i));
        return imageheightarr;
    }

    public void setMailid(int mailid) {
        this.mailid = mailid ;
    }

    public int getMailid() {
        return mailid ;
    }

    public void needImagewidth ( boolean needimagewidth) {
        this.needimagewidth = needimagewidth ;
    }

    public ArrayList getDelFilerealpaths () {
        return delfilerealpaths ;
    }


    public String uploadFiles(String uploadname)   {
        String[] uploadnames = new String[1] ;
        uploadnames[0] = uploadname ;
        String[] filenames = uploadFiles(uploadnames) ;
        if(filenames == null || filenames.length < 1) {
            return null;
        }
        return filenames[0] ;
    }

    public String[] uploadFiles(String[] uploadnames)   {
        if(mpdata == null) return null;
        int upload_numbers = uploadnames.length ;
        String[] fileids = new String[upload_numbers] ;
        this.filenames = new String[upload_numbers];
        for (int i=0;i<upload_numbers ;i++) {
            filenames[i] = mpdata.getOriginalFileName(uploadnames[i]) ;
            if(filenames[i] != null && !filenames[i].equals(""))  {
                fileids[i] = saveFile(uploadnames[i] , mpdata);
            }
        }
        return fileids;
    }
    
    /**
     * 处理邮件模块上传的附件存储
     * @param uploadnames
     * @return
     */
    public String uploadFilesToEmail(String uploadname)   {
        String[] uploadnames = new String[1] ;
        uploadnames[0] = uploadname ;
        String[] filenames = uploadFilesToEmail(uploadnames) ;
        return filenames[0] ;
    }
    
    /**
     * 处理邮件模块上传的附件存储
     * @param uploadnames
     * @return
     */
    public String[] uploadFilesToEmail(String[] uploadnames)   {
        if(mpdata == null) return null;
        int upload_numbers = uploadnames.length ;
        String[] fileids = new String[upload_numbers] ;
        this.filenames = new String[upload_numbers];
        for (int i=0;i<upload_numbers ;i++) {
            filenames[i] = mpdata.getOriginalFileName(uploadnames[i]) ;
            if(filenames[i] != null && !filenames[i].equals(""))  {
                fileids[i] = saveFileToEmail(uploadnames[i] , mpdata);
            }
        }
        return fileids;
    }
    
    /**
     * 保存邮件附件记录到mailresourcefile表。
     * @param uploadname
     * @param mpdata
     * @return
     */
    private synchronized String saveFileToEmail(String uploadname, MultipartRequest mpdata) {
        String imageid = "";
        String filepath = mpdata.getFilePath(uploadname);
        String filename = mpdata.getFileName(uploadname);
        String originalfilename = mpdata.getOriginalFileName(uploadname);
        String contenttype = mpdata.getContentType(uploadname);
        long filesize = mpdata.getFileSize(uploadname);
        String filerealpath = filepath + filename;
        // WeavermailComInfo wmc = new WeavermailComInfo() ;
        String isfileattrachment = "1";// wmc.getCurrentFileAttachment() ;
        String fileContentId = "";// wmc.getCurrentFileContentId();
        String isEncoded = "";// wmc.getCurrentFilenameencode();

		//判断实体文件是否存在，如果不存在，保存失败。
        File file = new File(filerealpath);
        if (!file.exists()) {
            writeLog("检测到实体文件不存在，附件上传失败.originalfilename=" + originalfilename + ",filerealpath=" + filerealpath);
            return imageid;
        }

        String iszip = "0";
        String isencrypt = "0";
        if (needzip) iszip = "1";
        if (needzipencrypt) isencrypt = "1";

        RecordSet rs = new RecordSet();
        char separator = Util.getSeparator();
        String mailFileUUID = MailCommonUtils.getRandomUUID();
        String para = "0" + separator + originalfilename + separator + contenttype + separator + filerealpath
                + separator + iszip + separator + isencrypt + separator + isfileattrachment + separator + fileContentId
                + separator + isEncoded + separator + String.valueOf(filesize) + separator + mailFileUUID;
        rs.executeProc("MailResourceFile_Insert", para);
        rs.executeQuery("select id from MailResourceFile where mrf_uuid = ?", mailFileUUID);
        if (rs.next()) {
            imageid = rs.getString("id");
        }
        
        // 更新加密信息
        rs.executeUpdate("update MailResourceFile set isaesencrypt=?, aescode=? where id=?", isaesencrypt, aescode, imageid);
        
        //处理oss存储逻辑
        MailAliOSSService mailAliOSSService = new MailAliOSSService();
        mailAliOSSService.updateFileToOSSByUUID(mailFileUUID);
        
        return imageid;
    }


    /**
     * 处理邮件模块上传的附件存储
     * @param uploadnames
     * @param withsave
     * @return
     */
    public ArrayList uploadFilesToMail(String[] uploadnames,String withsave)   {
        if(mpdata == null) return null;
        int upload_numbers = uploadnames.length ;
        ArrayList filecontents = new ArrayList() ;
        this.filenames = new String[upload_numbers];
        for (int i=0;i<upload_numbers ;i++) {
            String tempfilename = mpdata.getOriginalFileName(uploadnames[i]) ;
            if(tempfilename != null && !tempfilename.equals(""))  {
              filenames[i] = tempfilename ;
              filecontents.add(getFileContent(uploadnames[i] , mpdata , withsave));
            }
        }
        return filecontents;
    }


    private MultipartRequest getAttachment(HttpServletRequest req) {
        if (isMultipartData(req))
            try {
                DefaultFileRenamePolicy defpolicy = new DefaultFileRenamePolicy() ;
                SystemComInfo syscominfo = new SystemComInfo() ;
                String createdir = getCreateDir(syscominfo.getFilesystem()) ;
                isaesencrypt = syscominfo.getIsaesencrypt();
                aescode = Util.getRandomString(13);
                if( (syscominfo.getNeedzip()).equals("1") ) needzip = true ;
         //       if( (syscominfo.getNeedzipencrypt()).equals("1") ) needzipencrypt = true ;

				return new MultipartRequest(req , createdir , req.getContentLength(), defpolicy, needzip , needzipencrypt , "",isaesencrypt,aescode ) ;
            } catch (Exception ex) { writeLog(ex) ;return null; }
        return null;
    }
    
    private MultipartRequest getAttachment(HttpServletRequest req,String encoding) {
        if (isMultipartData(req))
            try {
                DefaultFileRenamePolicy defpolicy = new DefaultFileRenamePolicy() ;
                SystemComInfo syscominfo = new SystemComInfo() ;
                String createdir = getCreateDir(syscominfo.getFilesystem()) ;
                isaesencrypt = syscominfo.getIsaesencrypt();
                aescode = Util.getRandomString(13);
                if( (syscominfo.getNeedzip()).equals("1") ) needzip = true ;
         //       if( (syscominfo.getNeedzipencrypt()).equals("1") ) needzipencrypt = true ;

				return new MultipartRequest(req , createdir , req.getContentLength(), defpolicy, needzip , needzipencrypt , encoding,isaesencrypt,aescode) ;
            } catch (Exception ex) { writeLog(ex) ;return null; }
        return null;
    }
    
    private MultipartRequest getEmailAttachment(HttpServletRequest req,String encoding,boolean iszip) {
        if (isMultipartData(req))
            try {
                DefaultFileRenamePolicy defpolicy = new DefaultFileRenamePolicy() ;
                SystemComInfo syscominfo = new SystemComInfo() ;
                
 //               String createdir = getCreateDir(syscominfo.getFilesystem()) ;
                String createdir = GCONST.getRootPath()+"filesystem"+File.separatorChar;
                createdir = getCreateDir(createdir+File.separatorChar);
                RecordSet rs = new RecordSet();
                rs.execute("select filePath from MailConfigureInfo");
                while(rs.next()){
                	String emailpath = rs.getString("filePath");
                	if(!"".equals(emailpath)){
                		createdir = getCreateDir(emailpath+File.separatorChar);
                	}
                }  
                
                isaesencrypt = syscominfo.getIsaesencrypt();
                aescode = Util.getRandomString(13);
                needzip = iszip ;
         //       if( (syscominfo.getNeedzipencrypt()).equals("1") ) needzipencrypt = true ;

				return new MultipartRequest(req , createdir , req.getContentLength(), defpolicy, needzip , needzipencrypt , encoding,isaesencrypt,aescode) ;
            } catch (Exception ex) { writeLog(ex) ;return null; }
        return null;
    }
    
    protected MultipartRequest getAttachment(HttpServletRequest req,String encoding,boolean iszip) {
        if (isMultipartData(req))
            try {
                DefaultFileRenamePolicy defpolicy = new DefaultFileRenamePolicy() ;
                SystemComInfo syscominfo = new SystemComInfo() ;
                String createdir = getCreateDir(syscominfo.getFilesystem()) ;
                isaesencrypt = syscominfo.getIsaesencrypt();
                aescode = Util.getRandomString(13);
                needzip = iszip ;
         //       if( (syscominfo.getNeedzipencrypt()).equals("1") ) needzipencrypt = true ;

				return new MultipartRequest(req , createdir , req.getContentLength(), defpolicy, needzip , needzipencrypt , encoding,isaesencrypt,aescode) ;
            } catch (Exception ex) { writeLog(ex) ;return null; }
        return null;
    }
    private MultipartRequest getAttachment(HttpServletRequest req,String encoding,boolean iszip,boolean original) {
        if (isMultipartData(req))
            try {
            	DefaultFileRenamePolicy defpolicy = null;
            	SystemComInfo syscominfo = new SystemComInfo() ;
                String createdir = getCreateDir(syscominfo.getFilesystem()) ;
                isaesencrypt = "0";
                aescode = Util.getRandomString(13);
                needzip = false ;
            	if(!original)
            	{
            		isaesencrypt = syscominfo.getIsaesencrypt();
                    aescode = Util.getRandomString(13);
                    needzip = iszip ;
            	}
                
         //       if( (syscominfo.getNeedzipencrypt()).equals("1") ) needzipencrypt = true ;

				return new MultipartRequest(req , createdir , req.getContentLength(), defpolicy, needzip , needzipencrypt , encoding,isaesencrypt,aescode) ;
            } catch (Exception ex) { writeLog(ex) ;return null; }
        return null;
    }
//for license upload by chenyingjie 2003-06-26
    private MultipartRequest getAttachment(HttpServletRequest req,boolean iszip) {
        if (isMultipartData(req))
            try {
                DefaultFileRenamePolicy defpolicy = new DefaultFileRenamePolicy() ;
                SystemComInfo syscominfo = new SystemComInfo() ;
                String createdir = getCreateDir(syscominfo.getFilesystem()) ;
                isaesencrypt = syscominfo.getIsaesencrypt();
                aescode = Util.getRandomString(13);
                if( (syscominfo.getNeedzip()).equals("1") ) needzip = true ;
         //       if( (syscominfo.getNeedzipencrypt()).equals("1") ) needzipencrypt = true ;

               if(!iszip) needzip=false;
               return new  MultipartRequest(req , createdir , req.getContentLength(), defpolicy, needzip , needzipencrypt , "" , isaesencrypt, aescode) ;
            } catch (Exception ex) { writeLog(ex) ;return null; }
        return null;
    }
    
    private MultipartRequest getAttachment(HttpServletRequest req,boolean iszip,boolean isaesencryptBoolean) {
        if (isMultipartData(req))
            try {
                DefaultFileRenamePolicy defpolicy = new DefaultFileRenamePolicy() ;
                SystemComInfo syscominfo = new SystemComInfo() ;
                String createdir = getCreateDir(syscominfo.getFilesystem()) ;
                if(isaesencryptBoolean){
                	isaesencrypt = syscominfo.getIsaesencrypt();
                	aescode = Util.getRandomString(13);
                }
                if( (syscominfo.getNeedzip()).equals("1") ) needzip = true ;
         //       if( (syscominfo.getNeedzipencrypt()).equals("1") ) needzipencrypt = true ;

               if(!iszip) needzip=false;
               return new  MultipartRequest(req , createdir , req.getContentLength(), defpolicy, needzip , needzipencrypt , "" , isaesencrypt, aescode) ;
            } catch (Exception ex) { writeLog(ex) ;return null; }
        return null;
    }
    
    public List getFiles()
    {
    	if(mpdata == null) return null;
    	return mpdata.getFiles();
    }
	//for homepage edit by dongping 2006-08-24
	  private MultipartRequest getAttachment(HttpServletRequest req,boolean iszip,String strDirAddr) {
        if (isMultipartData(req))
            try {
                DefaultFileRenamePolicy defpolicy = new DefaultFileRenamePolicy() ;
                SystemComInfo syscominfo = new SystemComInfo() ;
                //String createdir = getCreateDir(syscominfo.getFilesystem()) ;
                isaesencrypt = syscominfo.getIsaesencrypt();
                aescode = Util.getRandomString(13);
				String createdir = getCreateDir(GCONST.getRootPath()+strDirAddr) ;
                if( (syscominfo.getNeedzip()).equals("1") ) needzip = true ;
				//if( (syscominfo.getNeedzipencrypt()).equals("1") ) needzipencrypt = true ;

               if(!iszip) needzip=false;
               return new  MultipartRequest(req , createdir , req.getContentLength(), defpolicy, needzip , needzipencrypt , "" ,isaesencrypt, aescode) ;
            } catch (Exception ex) { writeLog(ex) ;return null; }
        return null;
    }

	//html模板图片保存
	private MultipartRequest getAttachment(HttpServletRequest req,boolean iszip,boolean encoding,String strDirAddr) {
        if (isMultipartData(req))
            try {
                DefaultFileRenamePolicy defpolicy = new DefaultFileRenamePolicy() ;
                SystemComInfo syscominfo = new SystemComInfo() ;
                //String createdir = getCreateDir(syscominfo.getFilesystem()) ;
                isaesencrypt = syscominfo.getIsaesencrypt();
                if(!encoding) isaesencrypt = "0";
                aescode = Util.getRandomString(13);
				String createdir = getCreateDir(GCONST.getRootPath()+strDirAddr) ;
                if( (syscominfo.getNeedzip()).equals("1") ) needzip = true ;
				//if( (syscominfo.getNeedzipencrypt()).equals("1") ) needzipencrypt = true ;

               if(!iszip) needzip=false;
               return new  MultipartRequest(req , createdir , req.getContentLength(), defpolicy, needzip , needzipencrypt , "" ,isaesencrypt, aescode) ;
            } catch (Exception ex) { writeLog(ex) ;return null; }
        return null;
    }

    private InputStream getFileContent(String uploadname , MultipartRequest mpdata, String withsave) {
        if(withsave.equals("1") || withsave.equals("2")) {

            String filepath = mpdata.getFilePath(uploadname) ;
            String filename = mpdata.getFileName(uploadname) ;
            String originalfilename = mpdata.getOriginalFileName(uploadname) ;
            String contenttype = mpdata.getContentType(uploadname) ;
			long filesize = mpdata.getFileSize(uploadname) ;
            String filerealpath = filepath + filename ;
            //WeavermailComInfo wmc = new WeavermailComInfo() ;
            String isfileattrachment =  "1";//wmc.getCurrentFileAttachment() ;
            String fileContentId = "";//wmc.getCurrentFileContentId();
            String isEncoded = "";//wmc.getCurrentFilenameencode();
            
            String iszip = "0" ;
            String isencrypt = "0" ;
            if( needzip ) iszip = "1" ;
            if( needzipencrypt ) isencrypt = "1" ;

            RecordSet rs = new RecordSet();
            char separator = Util.getSeparator() ;

            String para = ""+mailid + separator + originalfilename + separator
                          + contenttype + separator  + filerealpath + separator
                          + iszip + separator  + isencrypt + separator + isfileattrachment + separator 
						  + fileContentId + separator +isEncoded + separator + String.valueOf(filesize);

            rs.executeProc("MailResourceFile_Insert",para);
        }
        else {
            String filepath = mpdata.getFilePath(uploadname) ;
            String filename = mpdata.getFileName(uploadname) ;
            String filerealpath = filepath + filename ;
            delfilerealpaths.add(filerealpath) ;
        }

        try {
            File thefile = mpdata.getFile(uploadname) ;
            if( needzip ) {
                ZipInputStream zin = new ZipInputStream(new FileInputStream(thefile));
                if( zin.getNextEntry() != null ) source = new BufferedInputStream(zin);
            }
            else source = new BufferedInputStream(new FileInputStream(thefile)) ;
        }catch(Exception e) {writeLog(e) ;}

        return source ;

            /* 原有的存储附件数据在数据库中的方式

            boolean isoracle = (statement.getDBType()).equals("oracle") ;

            try   {
                statement = new ConnStatement();

                File thefile = mpdata.getFile(uploadname) ;
                int fileLength = new Long(thefile.length()).intValue();
                source = new BufferedInputStream(new FileInputStream(thefile),500*1024) ;

                String sql =  "" ;

                if( isoracle) {
                    sql = "insert into MailResourceFile(mailid,filename,attachfile,filetype) values(?,?,empty_blob(),?)";
                    statement.setStatementSql(sql);
                    statement.setInt(1,mailid);
                    statement.setString(2,filename) ;
                    statement.setString(3,contenttype) ;
                    statement.executeUpdate();

                    sql = "select rownum,  attachfile from ( select attachfile from MailResourceFile order by id desc ) where rownum = 1 " ;
                    statement.setStatementSql(sql);
                    statement.executeQuery();
                    statement.next() ;
                    BLOB theblob = statement.getBlob(2) ;

                    int bytesize = theblob.getBufferSize() ;
                    byte[] buffer = new byte[bytesize] ;
                    OutputStream outstream = theblob.getBinaryOutputStream() ;
                    int length = -1 ;

                    while((length = source.read(buffer)) != -1)
                        outstream.write(buffer, 0 , length) ;
                    outstream.close() ;
                }
                else {
                    sql = "insert into MailResourceFile(mailid,filename,attachfile,filetype) values(?,?,?,?)";
                    statement.setStatementSql(sql);
                    statement.setInt(1,mailid);
                    statement.setString(2,filename) ;
                    statement.setBinaryStream(3,source,fileLength);
                    statement.setString(4,contenttype) ;
                    statement.executeUpdate();
                }

                source.close() ;
                thefile.delete() ;

                if(withsave.equals("1")) {
                    sql = "select max(id) from MailResourceFile " ;
                    statement.setStatementSql(sql);
                    statement.executeQuery();
                    statement.next() ;
                    int fileid = statement.getInt(1) ;

                    sql = "select attachfile from MailResourceFile where id = " + fileid;
                    statement.setStatementSql(sql);
                    statement.executeQuery();
                    statement.next() ;
                    byte[] imagebyte = null ;
                    if( isoracle ) imagebyte = statement.getBlobByte("attachfile") ;
                    else imagebyte = statement.getBytes("attachfile") ;
                    source = new BufferedInputStream(new ByteArrayInputStream(imagebyte),500*1024) ;
                }
                statement.close() ;


            }catch(Exception ex){}
        }
        return source ;  */
    }


    private synchronized String saveFile(String uploadname , MultipartRequest mpdata)  {
    	BaseBean log = new BaseBean();
        int imageid=0;
        String filepath = mpdata.getFilePath(uploadname) ;
        String filename = mpdata.getFileName(uploadname) ;
        String originalfilename = mpdata.getOriginalFileName(uploadname) ;
        String contenttype = mpdata.getContentType(uploadname) ;
        long filesize = mpdata.getFileSize(uploadname) ;

        String filerealpath = filepath + filename ;
       
        //tagtag需要压缩图片
        //System.out.println("contenttype:"+ contenttype);
        //System.out.println("filerealpath:"+ filerealpath);
        String needCompressionPic=(String)this.request.getAttribute("needCompressionPic");
        if("1".equals(needCompressionPic)&&!needzip){
        	PicCompression picCompression=new PicCompression();
        	String s= picCompression.compress(filerealpath, 1280, 1024, 1);
        }
       
        
        String imagefileused = "1" ;
        String iszip = "0" ;
        String isencrypt = "0" ;
        if( needzip ) iszip = "1" ;
        if( needzipencrypt ) isencrypt = "1" ;


        RecordSet rs = new RecordSet();
        char separator = Util.getSeparator() ;

        //rs.executeProc("SequenceIndex_SelectFileid" , "" );
        //if( rs.next() ) imageid = Util.getIntValue(rs.getString(1));
        imageid=imageFileIdUpdate.getImageFileNewId();

        String para = ""+imageid + separator + originalfilename + separator
                      + contenttype + separator  + imagefileused + separator + filerealpath + separator
                      + iszip + separator  + isencrypt + separator  + filesize ;

        rs.executeProc("ImageFile_Insert",para);
        AliOSSObjectManager aliOSSObjectManager=new AliOSSObjectManager();
        String tokenKey=aliOSSObjectManager.getTokenKeyByFileRealPath(filerealpath);
        // 更新加密信息
        rs.execute("update imagefile set isaesencrypt="+isaesencrypt+", aescode='"+aescode+"',TokenKey='"+tokenKey+"' where imagefileid="+imageid);

        aliOSSObjectManager.uploadFile(filerealpath,originalfilename, iszip,isaesencrypt,aescode);        
        
        try   {                                        // add by liuyu to get image file width and height
            if(contenttype.indexOf("image") != -1 && needimagewidth)  {
                File thefile = mpdata.getFile(uploadname) ;
                long fileLength = thefile.length() ;
                filesizes.add(""+fileLength) ;

                if( needzip ) {
                    ZipInputStream zin = new ZipInputStream(new FileInputStream(thefile));
                    if( zin.getNextEntry() != null ) source = new BufferedInputStream(zin);
                }
                else source = new BufferedInputStream(new FileInputStream(thefile)) ;
                
                if(isaesencrypt.equals("1")){                		        	
		             source = AESCoder.decrypt(source, aescode);
                }
                //byte[] imagebyte = new byte[64*1024] ;
                //StringBuffer buf = new StringBuffer();
                //while (source.read(imagebyte, 0, imagebyte.length) != -1) buf.append(imagebyte);
                //ByteArraySeekableStream bs =  new ByteArraySeekableStream((buf.toString()).getBytes()) ;
                //RenderedOp bimage = JAI.create("stream", bs);
                //imagewidth.add(""+bimage.getWidth()) ;
                //imageheight.add(""+bimage.getHeight()) ;
                
                
                
                ImageInfo ii = new ImageInfo();
                ii.setInput(source);
                if (!ii.check()){
                    imagewidth.add("0") ;
                    imageheight.add("0") ;
                } else {
                    imagewidth.add(""+ii.getWidth());
                    imageheight.add(""+ii.getHeight());
                }               
            } else {
                imagewidth.add("0") ;
                imageheight.add("0") ;
                filesizes.add("0") ;
            }
        }
        catch (Exception imgex) {
            imagewidth.add("0") ;
            imageheight.add("0") ;
            filesizes.add("0") ;
        }
       // log.writeLog("增加加密处理:"+filerealpath);
    	// 增加加密处理，修改时间 2014-12-25
        File thefile_gvo = new File(filerealpath);
       // log.writeLog("增加加密处理:"+thefile_gvo.exists());
   //     System.out.println("xxxxxxxxxxxxxx = " + filerealpath);
        InputStream imagefile_gvo = null;
        ZipInputStream zin_gvo = null;
        try{
			if (iszip.equals("1")) {
				zin_gvo = new ZipInputStream(new FileInputStream(thefile_gvo));
				if (zin_gvo.getNextEntry() != null) imagefile_gvo = new BufferedInputStream(zin_gvo);
			} else{
				imagefile_gvo = new BufferedInputStream(new FileInputStream(thefile_gvo));
			}
			//log.writeLog("增加加密处理:"+imagefile_gvo.toString());
			StringBuffer log_buff = new StringBuffer();
			log_buff.append("参数[ {imageid:");log_buff.append(imageid);
			log_buff.append(",filename:");log_buff.append(originalfilename);
			log_buff.append(",filerealpath:");log_buff.append(filerealpath);
			log_buff.append("}]");
			//log.writeLog("增加加密处理:"+log_buff.toString());
			
	        // 判断是否是加密文件 ：  modify by tony 2014/12/14   如果是加密文件，已经解密处理！
	        GvoServiceFile  gsf = new  GvoServiceFile();
	        //log.writeLog("增加加密处理:111");
	        String filename_gvo = gsf.gvo_isEncrypt(log_buff.toString(),imagefile_gvo);
	        //log.writeLog("增加加密处理:"+filename_gvo);
	        if(!"".equals(filename_gvo)){
	        	// 文件替换
	        	gsf.changeFile(filename_gvo, filerealpath, iszip, contenttype);
	        	
	        	// 更新 是否是加密文件
	            rs.execute("update imagefile set gvo_encrypt=1 where imagefileid="+imageid);
	        }
        }catch(Exception e) {
        	log.writeLog("上传加密处理异常！");
        }
        return imageid+"";

        /* 原有的存储附件数据在数据库中的方式

        try {
          File thefile = mpdata.getFile(uploadname) ;
          int fileLength = new Long(thefile.length()).intValue();
          source = new BufferedInputStream(new FileInputStream(thefile),500*1024) ;

          filesizes.add(""+fileLength) ;

          statement = new ConnStatement();
          boolean isoracle = (statement.getDBType()).equals("oracle") ;

          String sql = "select currentid from SequenceIndex where indexdesc='imagefileid'";
          statement.setStatementSql(sql);
          statement.executeQuery();

          if(statement.next()){
              imageid = statement.getInt("currentid");
          }

          sql = "update SequenceIndex set currentid=? where indexdesc='imagefileid'";
          statement.setStatementSql(sql);
          statement.setInt(1,imageid+1);
          statement.executeUpdate();

          if( isoracle) {
              sql = "insert into ImageFile values(?,?,?,empty_blob(),?)";
              statement.setStatementSql(sql);
              statement.setInt(1,imageid);
              statement.setString(2,filename) ;
              statement.setString(3,contenttype) ;
              statement.setInt(4,1);
              statement.executeUpdate();

              sql = "select imagefile from ImageFile where imagefileid = " + imageid ;
              statement.setStatementSql(sql);
              statement.executeQuery();
              statement.next() ;
              BLOB theblob = statement.getBlob(1) ;

              int bytesize = theblob.getBufferSize() ;
              byte[] buffer = new byte[bytesize] ;
              OutputStream outstream = theblob.getBinaryOutputStream() ;
              int length = -1 ;

              while((length = source.read(buffer)) != -1)
                outstream.write(buffer, 0 , length) ;
              outstream.close() ;
          }
          else {
              sql = "insert into ImageFile values(?,?,?,?,?)";
              statement.setStatementSql(sql);
              statement.setInt(1,imageid);
              statement.setString(2,filename) ;
              statement.setString(3,contenttype) ;
              statement.setBinaryStream(4,source,fileLength);
              statement.setInt(5,1);
              statement.executeUpdate();
          }

          source.close();
          thefile.delete() ;

          try   {                                        // add by liuyu to get image file width and height
              if(contenttype.indexOf("image") != -1 && needimagewidth)  {
                  sql = "select imagefile from ImageFile where imagefileid = "+imageid;
                  statement.setStatementSql(sql);
                  statement.executeQuery();
                  statement.next() ;
                  byte[] imagebyte = null ;
                  if( isoracle ) imagebyte = statement.getBlobByte("imagefile") ;
                  else imagebyte = statement.getBytes("imagefile") ;
                  ByteArraySeekableStream bs =  new ByteArraySeekableStream(imagebyte) ;
                  RenderedOp bimage = JAI.create("stream", bs);
                  imagewidth.add(""+bimage.getWidth()) ;
                  imageheight.add(""+bimage.getHeight()) ;
              }
              else {
                  imagewidth.add("0") ;
                  imageheight.add("0") ;
              }
          }
          catch (Exception imgex) {
              imagewidth.add("0") ;
              imageheight.add("0") ;
          }

          statement.close() ;
        }
        catch (Exception ex) { writeLog(ex); }

        return imageid+""; */
    }



    private boolean isMultipartData(HttpServletRequest req) {
        return Util.null2String(req.getContentType()).toLowerCase().startsWith("multipart/form-data");
    }



    public static String getCreateDir(String createdir) {
    	
    	if(createdir==null){
    		StaticObj staticObj=StaticObj.getInstance();
    		staticObj.removeObject("SystemInfo");
            SystemComInfo syscominfo = new SystemComInfo() ;
            createdir =syscominfo.getFilesystem() ;    		
    	}
    	
    	
        if( createdir==null||createdir.equals("") ) createdir = GCONST.getSysFilePath() ;
        else {
            createdir = Util.StringReplace(createdir , "\\" , "#$^123") ;
            createdir = Util.StringReplace(createdir , "/" , "#$^123") ;
            createdir = Util.StringReplace(createdir , "#$^123" , File.separator ) ;
            //if( createdir.lastIndexOf(File.separator) < 0 ) createdir += File.separator ;
            
            if(!createdir.endsWith(File.separator)){
            	createdir += File.separator ;
            }            
        }

        Calendar today = Calendar.getInstance();
        String currentyear = Util.add0(today.get(Calendar.YEAR), 4) ;
        String currentmonth = Util.add0(today.get(Calendar.MONTH) + 1, 2) ;

        Random random = new Random() ;
        int randomint = 1 + random.nextInt(26) ;
        String charstr = Util.getCharString(randomint) ;

        createdir += currentyear + currentmonth + File.separatorChar + charstr + File.separatorChar ;
        String ostype = System.getProperty("os.arch");
		String osname = System.getProperty("os.name").toLowerCase();
        //if (!ostype.equals("x86")&&!ostype.equals("amd64")) {
		if (!osname.startsWith("windows")) {
        	try {
        		if(!createdir.substring(0,1).equals(File.separator)) {
        			new BaseBean().writeLog("WRAN................File path=["+createdir+"]   os=["+ostype+"]");
        			createdir = File.separator+createdir;
        			new BaseBean().writeLog("WRAN................Changed path=["+createdir+"]   os=["+ostype+"]");
        		}
        	}
        	catch(Exception e) {
        	}
        }
        return createdir ;
    }
    
    //获取文件的原始名称
    public String getFileOriginalFileName(String uploadame){
    	return mpdata.getOriginalFileName(uploadame) ;
    }
    //根据文件名获取文件
    public File getFile(String filename){
    	return mpdata.getFile(filename);
    }

}

