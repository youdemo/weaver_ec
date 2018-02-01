package weaver.file;

import gvo.passwd.GvoServiceFile;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipInputStream;
import javax.servlet.http.HttpServletRequest;
import weaver.alioss.AliOSSObjectManager;
import weaver.conn.RecordSet;
import weaver.docs.docs.ImageFileIdUpdate;
import weaver.file.multipart.DefaultFileRenamePolicy;
import weaver.file.multipart.FileRenamePolicy;
import weaver.file.multipart.MultipartRequest;
import weaver.file.multipart.UploadedFile;
import weaver.file.util.PicCompression;
import weaver.filter.XssUtil;
import weaver.general.BaseBean;
import weaver.general.GCONST;
import weaver.general.StaticObj;
import weaver.general.Util;
import weaver.system.SystemComInfo;

public class FileUploadold extends BaseBean
{
  private static ImageFileIdUpdate imageFileIdUpdate = new ImageFileIdUpdate();
  private RecordSet rs;
  private MultipartRequest mpdata = null;
  HttpServletRequest request = null;
  private String[] filenames = null;
  private InputStream source = null;
  private ArrayList filesizes = new ArrayList();
  private ArrayList imagewidth = new ArrayList();
  private ArrayList imageheight = new ArrayList();
  private ArrayList delfilerealpaths = new ArrayList();
  private int mailid;
  private boolean needimagewidth = false;
  private boolean needzip = false;
  private boolean needzipencrypt = false;
  private String isaesencrypt = "0";
  private String aescode = "";
  private String remoteAddr = "";
  private XssUtil xss = null;

  public FileUpload(HttpServletRequest paramHttpServletRequest) {
    this.remoteAddr = paramHttpServletRequest.getRemoteAddr();
    if (isMultipartData(paramHttpServletRequest)) this.mpdata = getAttachment(paramHttpServletRequest);
    this.request = paramHttpServletRequest;
    this.xss = new XssUtil();
  }

  public FileUpload(HttpServletRequest paramHttpServletRequest, String paramString) {
    this.remoteAddr = paramHttpServletRequest.getRemoteAddr();
    if (isMultipartData(paramHttpServletRequest)) this.mpdata = getAttachment(paramHttpServletRequest, paramString);
    this.request = paramHttpServletRequest;
    this.xss = new XssUtil();
  }

  public FileUpload(HttpServletRequest paramHttpServletRequest, String paramString1, boolean paramBoolean, String paramString2) {
    this.remoteAddr = paramHttpServletRequest.getRemoteAddr();
    if ((isMultipartData(paramHttpServletRequest)) && ("1".equals(paramString2))) this.mpdata = getEmailAttachment(paramHttpServletRequest, paramString1, paramBoolean);
    this.request = paramHttpServletRequest;
    this.xss = new XssUtil();
  }

  public FileUpload(HttpServletRequest paramHttpServletRequest, String paramString, boolean paramBoolean) {
    this.remoteAddr = paramHttpServletRequest.getRemoteAddr();
    if (isMultipartData(paramHttpServletRequest)) this.mpdata = getAttachment(paramHttpServletRequest, paramString, paramBoolean);
    this.request = paramHttpServletRequest;
    this.xss = new XssUtil(); }

  public FileUpload(HttpServletRequest paramHttpServletRequest, String paramString, boolean paramBoolean1, boolean paramBoolean2) {
    this.remoteAddr = paramHttpServletRequest.getRemoteAddr();
    if (isMultipartData(paramHttpServletRequest)) this.mpdata = getAttachment(paramHttpServletRequest, paramString, paramBoolean1, paramBoolean2);
    this.request = paramHttpServletRequest;
    this.xss = new XssUtil();
  }

  public HttpServletRequest getRequest() {
    return this.request;
  }

  public FileUpload(HttpServletRequest paramHttpServletRequest, boolean paramBoolean)
  {
    this.remoteAddr = paramHttpServletRequest.getRemoteAddr();
    if (isMultipartData(paramHttpServletRequest)) this.mpdata = getAttachment(paramHttpServletRequest, paramBoolean);
    this.request = paramHttpServletRequest;
  }

  public FileUpload(HttpServletRequest paramHttpServletRequest, boolean paramBoolean1, boolean paramBoolean2) {
    this.remoteAddr = paramHttpServletRequest.getRemoteAddr();
    if (isMultipartData(paramHttpServletRequest)) this.mpdata = getAttachment(paramHttpServletRequest, paramBoolean1, paramBoolean2);
    this.request = paramHttpServletRequest;
  }

  public FileUpload(HttpServletRequest paramHttpServletRequest, boolean paramBoolean, String paramString)
  {
    this.remoteAddr = paramHttpServletRequest.getRemoteAddr();
    if (isMultipartData(paramHttpServletRequest)) this.mpdata = getAttachment(paramHttpServletRequest, paramBoolean, paramString);
    this.request = paramHttpServletRequest;
  }

  public FileUpload(HttpServletRequest paramHttpServletRequest, boolean paramBoolean1, boolean paramBoolean2, String paramString)
  {
    this.remoteAddr = Util.getIpAddr(paramHttpServletRequest);
    if (isMultipartData(paramHttpServletRequest)) this.mpdata = getAttachment(paramHttpServletRequest, paramBoolean1, paramBoolean2, paramString);
    this.request = paramHttpServletRequest;
  }

  public Hashtable getUploadImgNames() {
    String str1 = ""; String str2 = ""; String str3 = "";
    Hashtable localHashtable = new Hashtable();
    for (Enumeration localEnumeration = this.mpdata.getFileUploadNames(); localEnumeration.hasMoreElements(); ) {
      str1 = (String)localEnumeration.nextElement();
      if (str1.indexOf("docimages_") != -1);
      str2 = Util.null2String(this.mpdata.getFilePath(str1));
      str3 = Util.null2String(this.mpdata.getFileName(str1));
      if (str2.equals("")) continue; if (!(str3.equals("")));
      String str4 = str1.substring(str1.indexOf("_") + 1, str1.length());
      localHashtable.put(str4, str2 + str3);
    }

    return localHashtable;
  }

  public Hashtable getUploadFileNames() {
    String str1 = ""; String str2 = ""; String str3 = "";
    Hashtable localHashtable = new Hashtable();
    for (Enumeration localEnumeration = this.mpdata.getFileUploadNames(); localEnumeration.hasMoreElements(); ) {
      str1 = (String)localEnumeration.nextElement();
      UploadedFile localUploadedFile = this.mpdata.getUploadedFile(str1);
      localHashtable.put(str1, localUploadedFile);
    }
    return localHashtable;
  }

  public String getRemoteAddr()
  {
    return this.remoteAddr;
  }

  public String getParameter(String paramString) throws RuntimeException {
    if (!(isMultipartData(this.request))) return Util.null2String(this.request.getParameter(paramString));
    if (this.mpdata == null) return "";

    try
    {
      String str = Util.null2String(this.mpdata.getParameter(paramString));
      if ((!(str.equals(""))) && 
        (str.startsWith("__random__")))
      {
        return this.xss.get(str);
      }

      return new String(str.getBytes("ISO8859_1"), "UTF-8"); } catch (Exception localException) {
    }
    return "";
  }

  public String getParameter2(String paramString)
  {
    if (!(isMultipartData(this.request))) return Util.null2String(this.request.getParameter(paramString));
    if (this.mpdata == null) return "";
    String str = Util.null2String(this.mpdata.getParameter(paramString));
    return str;
  }

  public String getParameter3(String paramString)
  {
    if (!(isMultipartData(this.request))) {
      return this.request.getParameter(paramString);
    }
    if (this.mpdata == null) {
      return null;
    }

    String str = this.mpdata.getParameter(paramString);
    if (str == null) {
      return null;
    }
    try
    {
      return new String(str.getBytes("ISO8859_1"), "UTF-8"); } catch (Exception localException) {
    }
    return str;
  }

  public String[] getParameters(String paramString)
  {
    if (!(isMultipartData(this.request))) return this.request.getParameterValues(paramString);
    if (this.mpdata == null) return null;
    String[] arrayOfString = this.mpdata.getParameterValues(paramString);
    return arrayOfString;
  }

  public Enumeration getParameterNames() {
    if (!(isMultipartData(this.request))) return this.request.getParameterNames();
    if (this.mpdata == null) return null;
    return this.mpdata.getParameterNames();
  }

  public String[] getParameterValues(String paramString) {
    if (!(isMultipartData(this.request))) return this.request.getParameterValues(paramString);
    if (this.mpdata == null) return null;
    String[] arrayOfString = this.mpdata.getParameterValues(paramString);
    return arrayOfString;
  }

  public String[] getParameterValues2(String paramString) {
    String[] arrayOfString1 = null;
    if (!(isMultipartData(this.request))) {
      arrayOfString1 = this.request.getParameterValues(paramString);
      arrayOfString2 = new String[arrayOfString1.length];
      try {
        for (int i = 0; i < arrayOfString1.length; ++i)
          arrayOfString2[i] = new String(Util.null2String(arrayOfString1[i]).getBytes("ISO8859_1"), "UTF-8");
      }
      catch (UnsupportedEncodingException localUnsupportedEncodingException1) {
        localUnsupportedEncodingException1.printStackTrace();
      }
      return arrayOfString2;
    }
    if (this.mpdata == null) return null;
    arrayOfString1 = this.mpdata.getParameterValues(paramString);
    String[] arrayOfString2 = new String[arrayOfString1.length];
    try {
      for (int j = 0; j < arrayOfString1.length; ++j)
        arrayOfString2[j] = new String(Util.null2String(arrayOfString1[j]).getBytes("ISO8859_1"), "UTF-8");
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException2) {
      localUnsupportedEncodingException2.printStackTrace();
    }
    return arrayOfString2;
  }

  public String getFileName()
  {
    return this.filenames[0];
  }

  public String[] getFileNames() {
    return this.filenames;
  }

  public void setFileNames(String[] paramArrayOfString) {
    this.filenames = paramArrayOfString;
  }

  public int getFileSize() {
    return Util.getIntValue((String)this.filesizes.get(0));
  }

  public int[] getFileSizes() {
    int[] arrayOfInt = new int[this.filesizes.size()];
    for (int i = 0; i < this.filesizes.size(); ++i) arrayOfInt[i] = Util.getIntValue((String)this.filesizes.get(i));
    return arrayOfInt;
  }

  public void setFileSizes(ArrayList paramArrayList) {
    this.filesizes = paramArrayList;
  }

  public int getWidth() {
    return Util.getIntValue((String)this.imagewidth.get(0));
  }

  public int getHeight() {
    return Util.getIntValue((String)this.imageheight.get(0));
  }

  public int[] getWidths() {
    int[] arrayOfInt = new int[this.imagewidth.size()];
    for (int i = 0; i < this.imagewidth.size(); ++i) arrayOfInt[i] = Util.getIntValue((String)this.imagewidth.get(i));
    return arrayOfInt;
  }

  public int[] getHeights() {
    int[] arrayOfInt = new int[this.imageheight.size()];
    for (int i = 0; i < this.imageheight.size(); ++i) arrayOfInt[i] = Util.getIntValue((String)this.imageheight.get(i));
    return arrayOfInt;
  }

  public void setMailid(int paramInt) {
    this.mailid = paramInt;
  }

  public int getMailid() {
    return this.mailid;
  }

  public void needImagewidth(boolean paramBoolean) {
    this.needimagewidth = paramBoolean;
  }

  public ArrayList getDelFilerealpaths() {
    return this.delfilerealpaths;
  }

  public String uploadFiles(String paramString)
  {
    String[] arrayOfString1 = new String[1];
    arrayOfString1[0] = paramString;
    String[] arrayOfString2 = uploadFiles(arrayOfString1);
    return arrayOfString2[0];
  }

  public String[] uploadFiles(String[] paramArrayOfString) {
    if (this.mpdata == null) return null;
    int i = paramArrayOfString.length;
    String[] arrayOfString = new String[i];
    this.filenames = new String[i];
    for (int j = 0; j < i; ++j) {
      this.filenames[j] = this.mpdata.getOriginalFileName(paramArrayOfString[j]);
      if ((this.filenames[j] != null) && (!(this.filenames[j].equals("")))) {
        arrayOfString[j] = saveFile(paramArrayOfString[j], this.mpdata);
      }
    }
    return arrayOfString;
  }

  public String uploadFilesToEmail(String paramString) {
    String[] arrayOfString1 = new String[1];
    arrayOfString1[0] = paramString;
    String[] arrayOfString2 = uploadFilesToEmail(arrayOfString1);
    return arrayOfString2[0];
  }

  public String[] uploadFilesToEmail(String[] paramArrayOfString) {
    if (this.mpdata == null) return null;
    int i = paramArrayOfString.length;
    String[] arrayOfString = new String[i];
    this.filenames = new String[i];
    for (int j = 0; j < i; ++j) {
      this.filenames[j] = this.mpdata.getOriginalFileName(paramArrayOfString[j]);
      if ((this.filenames[j] != null) && (!(this.filenames[j].equals("")))) {
        arrayOfString[j] = saveFileToEmail(paramArrayOfString[j], this.mpdata);
      }
    }
    return arrayOfString;
  }

  private synchronized String saveFileToEmail(String paramString, MultipartRequest paramMultipartRequest)
  {
    String str1 = "";
    String str2 = paramMultipartRequest.getFilePath(paramString);
    String str3 = paramMultipartRequest.getFileName(paramString);
    String str4 = paramMultipartRequest.getOriginalFileName(paramString);
    String str5 = paramMultipartRequest.getContentType(paramString);
    long l = paramMultipartRequest.getFileSize(paramString);
    String str6 = str2 + str3;

    String str7 = "1";
    String str8 = "";
    String str9 = "";

    String str10 = "0";
    String str11 = "0";
    if (this.needzip) str10 = "1";
    if (this.needzipencrypt) str11 = "1";

    this.rs = new RecordSet();
    char c = Util.getSeparator();
    String str12 = System.currentTimeMillis() + "";
    String str13 = "" + c + str4 + c + str5 + c + str6 + c + str10 + c + str11 + c + str7 + c + str8 + c + str9 + c + String.valueOf(l) + c + str12;

    this.rs.executeProc("MailResourceFile_Insert", str13);

    this.rs.execute("select id from MailResourceFile where timeMillis = '" + str12 + "'");
    if (this.rs.next()) {
      str1 = this.rs.getString("id");
    }

    this.rs.execute("update MailResourceFile set isaesencrypt=" + this.isaesencrypt + ", aescode='" + this.aescode + "' where id=" + str1);

    return str1;
  }

  public ArrayList uploadFilesToMail(String[] paramArrayOfString, String paramString)
  {
    if (this.mpdata == null) return null;
    int i = paramArrayOfString.length;
    ArrayList localArrayList = new ArrayList();
    this.filenames = new String[i];
    for (int j = 0; j < i; ++j) {
      String str = this.mpdata.getOriginalFileName(paramArrayOfString[j]);
      if ((str != null) && (!(str.equals("")))) {
        this.filenames[j] = str;
        localArrayList.add(getFileContent(paramArrayOfString[j], this.mpdata, paramString));
      }
    }
    return localArrayList;
  }

  private MultipartRequest getAttachment(HttpServletRequest paramHttpServletRequest)
  {
    if (isMultipartData(paramHttpServletRequest))
      try {
        DefaultFileRenamePolicy localDefaultFileRenamePolicy = new DefaultFileRenamePolicy();
        SystemComInfo localSystemComInfo = new SystemComInfo();
        String str = getCreateDir(localSystemComInfo.getFilesystem());
        this.isaesencrypt = localSystemComInfo.getIsaesencrypt();
        this.aescode = Util.getRandomString(13);
        if (localSystemComInfo.getNeedzip().equals("1")) this.needzip = true;

        return new MultipartRequest(paramHttpServletRequest, str, paramHttpServletRequest.getContentLength(), localDefaultFileRenamePolicy, this.needzip, this.needzipencrypt, "", this.isaesencrypt, this.aescode); } catch (Exception localException) {
        writeLog(localException); return null; }
    return null;
  }

  private MultipartRequest getAttachment(HttpServletRequest paramHttpServletRequest, String paramString) {
    if (isMultipartData(paramHttpServletRequest))
      try {
        DefaultFileRenamePolicy localDefaultFileRenamePolicy = new DefaultFileRenamePolicy();
        SystemComInfo localSystemComInfo = new SystemComInfo();
        String str = getCreateDir(localSystemComInfo.getFilesystem());
        this.isaesencrypt = localSystemComInfo.getIsaesencrypt();
        this.aescode = Util.getRandomString(13);
        if (localSystemComInfo.getNeedzip().equals("1")) this.needzip = true;

        return new MultipartRequest(paramHttpServletRequest, str, paramHttpServletRequest.getContentLength(), localDefaultFileRenamePolicy, this.needzip, this.needzipencrypt, paramString, this.isaesencrypt, this.aescode); } catch (Exception localException) {
        writeLog(localException); return null; }
    return null;
  }

  private MultipartRequest getEmailAttachment(HttpServletRequest paramHttpServletRequest, String paramString, boolean paramBoolean) {
    if (isMultipartData(paramHttpServletRequest))
      try {
        DefaultFileRenamePolicy localDefaultFileRenamePolicy = new DefaultFileRenamePolicy();
        SystemComInfo localSystemComInfo = new SystemComInfo();

        String str1 = GCONST.getRootPath() + "filesystem" + File.separatorChar;
        str1 = getCreateDir(str1 + File.separatorChar);
        RecordSet localRecordSet = new RecordSet();
        localRecordSet.execute("select filePath from MailConfigureInfo");
        while (localRecordSet.next()) {
          String str2 = localRecordSet.getString("filePath");
          if (!("".equals(str2))) {
            str1 = getCreateDir(str2 + File.separatorChar);
          }
        }

        this.isaesencrypt = localSystemComInfo.getIsaesencrypt();
        this.aescode = Util.getRandomString(13);
        this.needzip = paramBoolean;

        return new MultipartRequest(paramHttpServletRequest, str1, paramHttpServletRequest.getContentLength(), localDefaultFileRenamePolicy, this.needzip, this.needzipencrypt, paramString, this.isaesencrypt, this.aescode); } catch (Exception localException) {
        writeLog(localException); return null; }
    return null;
  }

  protected MultipartRequest getAttachment(HttpServletRequest paramHttpServletRequest, String paramString, boolean paramBoolean) {
    if (isMultipartData(paramHttpServletRequest))
      try {
        DefaultFileRenamePolicy localDefaultFileRenamePolicy = new DefaultFileRenamePolicy();
        SystemComInfo localSystemComInfo = new SystemComInfo();
        String str = getCreateDir(localSystemComInfo.getFilesystem());
        this.isaesencrypt = localSystemComInfo.getIsaesencrypt();
        this.aescode = Util.getRandomString(13);
        this.needzip = paramBoolean;

        return new MultipartRequest(paramHttpServletRequest, str, paramHttpServletRequest.getContentLength(), localDefaultFileRenamePolicy, this.needzip, this.needzipencrypt, paramString, this.isaesencrypt, this.aescode); } catch (Exception localException) {
        writeLog(localException); return null; }
    return null; }

  private MultipartRequest getAttachment(HttpServletRequest paramHttpServletRequest, String paramString, boolean paramBoolean1, boolean paramBoolean2) {
    if (isMultipartData(paramHttpServletRequest))
      try {
        FileRenamePolicy localFileRenamePolicy = null;
        SystemComInfo localSystemComInfo = new SystemComInfo();
        String str = getCreateDir(localSystemComInfo.getFilesystem());
        this.isaesencrypt = "0";
        this.aescode = Util.getRandomString(13);
        this.needzip = false;
        if (!(paramBoolean2))
        {
          this.isaesencrypt = localSystemComInfo.getIsaesencrypt();
          this.aescode = Util.getRandomString(13);
          this.needzip = paramBoolean1;
        }

        return new MultipartRequest(paramHttpServletRequest, str, paramHttpServletRequest.getContentLength(), localFileRenamePolicy, this.needzip, this.needzipencrypt, paramString, this.isaesencrypt, this.aescode); } catch (Exception localException) {
        writeLog(localException); return null; }
    return null;
  }

  private MultipartRequest getAttachment(HttpServletRequest paramHttpServletRequest, boolean paramBoolean) {
    if (isMultipartData(paramHttpServletRequest))
      try {
        DefaultFileRenamePolicy localDefaultFileRenamePolicy = new DefaultFileRenamePolicy();
        SystemComInfo localSystemComInfo = new SystemComInfo();
        String str = getCreateDir(localSystemComInfo.getFilesystem());
        this.isaesencrypt = localSystemComInfo.getIsaesencrypt();
        this.aescode = Util.getRandomString(13);
        if (localSystemComInfo.getNeedzip().equals("1")) this.needzip = true;

        if (!(paramBoolean)) this.needzip = false;
        return new MultipartRequest(paramHttpServletRequest, str, paramHttpServletRequest.getContentLength(), localDefaultFileRenamePolicy, this.needzip, this.needzipencrypt, "", this.isaesencrypt, this.aescode); } catch (Exception localException) {
        writeLog(localException); return null; }
    return null;
  }

  private MultipartRequest getAttachment(HttpServletRequest paramHttpServletRequest, boolean paramBoolean1, boolean paramBoolean2) {
    if (isMultipartData(paramHttpServletRequest))
      try {
        DefaultFileRenamePolicy localDefaultFileRenamePolicy = new DefaultFileRenamePolicy();
        SystemComInfo localSystemComInfo = new SystemComInfo();
        String str = getCreateDir(localSystemComInfo.getFilesystem());
        if (paramBoolean2) {
          this.isaesencrypt = localSystemComInfo.getIsaesencrypt();
          this.aescode = Util.getRandomString(13);
        }
        if (localSystemComInfo.getNeedzip().equals("1")) this.needzip = true;

        if (!(paramBoolean1)) this.needzip = false;
        return new MultipartRequest(paramHttpServletRequest, str, paramHttpServletRequest.getContentLength(), localDefaultFileRenamePolicy, this.needzip, this.needzipencrypt, "", this.isaesencrypt, this.aescode); } catch (Exception localException) {
        writeLog(localException); return null; }
    return null;
  }

  public List getFiles()
  {
    if (this.mpdata == null) return null;
    return this.mpdata.getFiles();
  }

  private MultipartRequest getAttachment(HttpServletRequest paramHttpServletRequest, boolean paramBoolean, String paramString) {
    if (isMultipartData(paramHttpServletRequest))
      try {
        DefaultFileRenamePolicy localDefaultFileRenamePolicy = new DefaultFileRenamePolicy();
        SystemComInfo localSystemComInfo = new SystemComInfo();

        this.isaesencrypt = localSystemComInfo.getIsaesencrypt();
        this.aescode = Util.getRandomString(13);
        String str = getCreateDir(GCONST.getRootPath() + paramString);
        if (localSystemComInfo.getNeedzip().equals("1")) this.needzip = true;

        if (!(paramBoolean)) this.needzip = false;
        return new MultipartRequest(paramHttpServletRequest, str, paramHttpServletRequest.getContentLength(), localDefaultFileRenamePolicy, this.needzip, this.needzipencrypt, "", this.isaesencrypt, this.aescode); } catch (Exception localException) {
        writeLog(localException); return null; }
    return null;
  }

  private MultipartRequest getAttachment(HttpServletRequest paramHttpServletRequest, boolean paramBoolean1, boolean paramBoolean2, String paramString)
  {
    if (isMultipartData(paramHttpServletRequest))
      try {
        DefaultFileRenamePolicy localDefaultFileRenamePolicy = new DefaultFileRenamePolicy();
        SystemComInfo localSystemComInfo = new SystemComInfo();

        this.isaesencrypt = localSystemComInfo.getIsaesencrypt();
        if (!(paramBoolean2)) this.isaesencrypt = "0";
        this.aescode = Util.getRandomString(13);
        String str = getCreateDir(GCONST.getRootPath() + paramString);
        if (localSystemComInfo.getNeedzip().equals("1")) this.needzip = true;

        if (!(paramBoolean1)) this.needzip = false;
        return new MultipartRequest(paramHttpServletRequest, str, paramHttpServletRequest.getContentLength(), localDefaultFileRenamePolicy, this.needzip, this.needzipencrypt, "", this.isaesencrypt, this.aescode); } catch (Exception localException) {
        writeLog(localException); return null; }
    return null;
  }

  private InputStream getFileContent(String paramString1, MultipartRequest paramMultipartRequest, String paramString2)
  {
    Object localObject1;
    Object localObject2;
    String str1;
    if ((paramString2.equals("1")) || (paramString2.equals("2")))
    {
      localObject1 = paramMultipartRequest.getFilePath(paramString1);
      localObject2 = paramMultipartRequest.getFileName(paramString1);
      str1 = paramMultipartRequest.getOriginalFileName(paramString1);
      String str2 = paramMultipartRequest.getContentType(paramString1);
      long l = paramMultipartRequest.getFileSize(paramString1);
      String str3 = ((String)localObject1) + ((String)localObject2);

      String str4 = "1";
      String str5 = "";
      String str6 = "";

      String str7 = "0";
      String str8 = "0";
      if (this.needzip) str7 = "1";
      if (this.needzipencrypt) str8 = "1";

      this.rs = new RecordSet();
      char c = Util.getSeparator();

      String str9 = "" + this.mailid + c + str1 + c + str2 + c + str3 + c + str7 + c + str8 + c + str4 + c + str5 + c + str6 + c + String.valueOf(l);

      this.rs.executeProc("MailResourceFile_Insert", str9);
    }
    else {
      localObject1 = paramMultipartRequest.getFilePath(paramString1);
      localObject2 = paramMultipartRequest.getFileName(paramString1);
      str1 = ((String)localObject1) + ((String)localObject2);
      this.delfilerealpaths.add(str1);
    }
    try
    {
      localObject1 = paramMultipartRequest.getFile(paramString1);
      if (this.needzip) {
        localObject2 = new ZipInputStream(new FileInputStream((File)localObject1));
        if (((ZipInputStream)localObject2).getNextEntry() != null) this.source = new BufferedInputStream((InputStream)localObject2);
      } else {
        this.source = new BufferedInputStream(new FileInputStream((File)localObject1)); } } catch (Exception localException) {
      writeLog(localException);
    }
    return ((InputStream)(InputStream)this.source);
  }

  private synchronized String saveFile(String paramString, MultipartRequest paramMultipartRequest)
  {
    Object localObject2;
    BaseBean localBaseBean = new BaseBean();
    int i = 0;
    String str1 = paramMultipartRequest.getFilePath(paramString);
    String str2 = paramMultipartRequest.getFileName(paramString);
    String str3 = paramMultipartRequest.getOriginalFileName(paramString);
    String str4 = paramMultipartRequest.getContentType(paramString);
    long l1 = paramMultipartRequest.getFileSize(paramString);

    String str5 = str1 + str2;

    String str6 = (String)this.request.getAttribute("needCompressionPic");
    if (("1".equals(str6)) && (!(this.needzip))) {
      localObject1 = new PicCompression();
      str7 = ((PicCompression)localObject1).compress(str5, 1280, 1024, 1.0F);
    }

    Object localObject1 = "1";
    String str7 = "0";
    String str8 = "0";
    if (this.needzip) str7 = "1";
    if (this.needzipencrypt) str8 = "1";

    this.rs = new RecordSet();
    char c = Util.getSeparator();

    i = imageFileIdUpdate.getImageFileNewId();

    String str9 = "" + i + c + str3 + c + str4 + c + ((String)localObject1) + c + str5 + c + str7 + c + str8 + c + l1;

    this.rs.executeProc("ImageFile_Insert", str9);
    AliOSSObjectManager localAliOSSObjectManager = new AliOSSObjectManager();
    String str10 = AliOSSObjectManager.getTokenKeyByFileRealPath(str5);

    this.rs.execute("update imagefile set isaesencrypt=" + this.isaesencrypt + ", aescode='" + this.aescode + "',TokenKey='" + str10 + "' where imagefileid=" + i);

    localAliOSSObjectManager.uploadFile(str5, str3, str7, this.isaesencrypt, this.aescode);
    try
    {
      if ((str4.indexOf("image") != -1) && (this.needimagewidth)) {
        File localFile1 = paramMultipartRequest.getFile(paramString);
        long l2 = localFile1.length();
        this.filesizes.add("" + l2);

        if (this.needzip) {
          localObject2 = new ZipInputStream(new FileInputStream(localFile1));
          if (((ZipInputStream)localObject2).getNextEntry() != null) this.source = new BufferedInputStream((InputStream)localObject2);
        } else {
          this.source = new BufferedInputStream(new FileInputStream(localFile1));
        }
        if (this.isaesencrypt.equals("1")) {
          this.source = AESCoder.decrypt(this.source, this.aescode);
        }

        localObject2 = new ImageInfo();
        ((ImageInfo)localObject2).setInput(this.source);
        if (!(((ImageInfo)localObject2).check())) {
          this.imagewidth.add("0");
          this.imageheight.add("0");
        } else {
          this.imagewidth.add("" + ((ImageInfo)localObject2).getWidth());
          this.imageheight.add("" + ((ImageInfo)localObject2).getHeight());
        }
      } else {
        this.imagewidth.add("0");
        this.imageheight.add("0");
        this.filesizes.add("0");
      }
    }
    catch (Exception localException1) {
      this.imagewidth.add("0");
      this.imageheight.add("0");
      this.filesizes.add("0");
    }

    File localFile2 = new File(str5);

    BufferedInputStream localBufferedInputStream = null;
    ZipInputStream localZipInputStream = null;
    try {
      if (str7.equals("1")) {
        localZipInputStream = new ZipInputStream(new FileInputStream(localFile2));
        if (localZipInputStream.getNextEntry() != null) localBufferedInputStream = new BufferedInputStream(localZipInputStream);
      } else {
        localBufferedInputStream = new BufferedInputStream(new FileInputStream(localFile2));
      }
      localObject2 = new StringBuffer();
      ((StringBuffer)localObject2).append("参数[ {imageid:"); ((StringBuffer)localObject2).append(i);
      ((StringBuffer)localObject2).append(",filename:"); ((StringBuffer)localObject2).append(str3);
      ((StringBuffer)localObject2).append(",filerealpath:"); ((StringBuffer)localObject2).append(str5);
      ((StringBuffer)localObject2).append("}]");

      GvoServiceFile localGvoServiceFile = new GvoServiceFile();
      String str11 = localGvoServiceFile.gvo_isEncrypt(((StringBuffer)localObject2).toString(), localBufferedInputStream);
      if (!("".equals(str11)))
      {
        localGvoServiceFile.changeFile(str11, str5, str7, str4);

        this.rs.execute("update imagefile set gvo_encrypt=1 where imagefileid=" + i);
      }
    } catch (Exception localException2) {
      localBaseBean.writeLog("上传加密处理异常！");
    }

    return ((String)(String)i + "");
  }

  private boolean isMultipartData(HttpServletRequest paramHttpServletRequest)
  {
    return Util.null2String(paramHttpServletRequest.getContentType()).toLowerCase().startsWith("multipart/form-data");
  }

  public static String getCreateDir(String paramString)
  {
    if (paramString == null) {
      localObject1 = StaticObj.getInstance();
      ((StaticObj)localObject1).removeObject("SystemInfo");
      localObject2 = new SystemComInfo();
      paramString = ((SystemComInfo)localObject2).getFilesystem();
    }

    if ((paramString == null) || (paramString.equals(""))) { paramString = GCONST.getSysFilePath();
    } else {
      paramString = Util.StringReplace(paramString, "\\", "#$^123");
      paramString = Util.StringReplace(paramString, "/", "#$^123");
      paramString = Util.StringReplace(paramString, "#$^123", File.separator);

      if (!(paramString.endsWith(File.separator))) {
        paramString = paramString + File.separator;
      }
    }

    Object localObject1 = Calendar.getInstance();
    Object localObject2 = Util.add0(((Calendar)localObject1).get(1), 4);
    String str1 = Util.add0(((Calendar)localObject1).get(2) + 1, 2);

    Random localRandom = new Random();
    int i = 1 + localRandom.nextInt(26);
    String str2 = Util.getCharString(i);

    paramString = paramString + ((String)localObject2) + str1 + File.separatorChar + str2 + File.separatorChar;
    String str3 = System.getProperty("os.arch");
    String str4 = System.getProperty("os.name").toLowerCase();

    if (!(str4.startsWith("windows")))
      try {
        if (!(paramString.substring(0, 1).equals(File.separator))) {
          new BaseBean().writeLog("WRAN................File path=[" + paramString + "]   os=[" + str3 + "]");
          paramString = File.separator + paramString;
          new BaseBean().writeLog("WRAN................Changed path=[" + paramString + "]   os=[" + str3 + "]");
        }
      }
      catch (Exception localException)
      {
      }
    return ((String)(String)paramString);
  }

  public String getFileOriginalFileName(String paramString)
  {
    return this.mpdata.getOriginalFileName(paramString);
  }

  public File getFile(String paramString) {
    return this.mpdata.getFile(paramString);
  }
}