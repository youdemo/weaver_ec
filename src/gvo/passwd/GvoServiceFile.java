package gvo.passwd;

import ext.zmm.validator.DesInterface;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import weaver.file.multipart.MacBinaryDecoderOutputStream;
import weaver.general.BaseBean;

public class GvoServiceFile
{
  private DesInterface dif = null;
  private BaseBean bb = new BaseBean();

  public GvoServiceFile() {
    this.dif = new DesInterface();
  }

  public static void main(String[] args) throws Exception
  {
  }

  public static void test2()
  {
    String oldPath = "d:\\123\\beijing.jpg";
    System.out.println(new GvoServiceFile().gvo_isEncrypt("", oldPath));
  }

  public static void test1() throws Exception
  {
    String iszip = "1";
    String filerealpath = "C:\\Users\\Administrator\\Desktop\\WEAVER\\file\\file\\201412\\R\\1688096794.zip";
    File thefile_gvo = new File(filerealpath);
    String filerealpath_1 = "d:\\123\\2014-12\\1688096794.zip";
    InputStream imagefile_gvo = null;
    ZipInputStream zin_gvo = null;
    if (iszip.equals("1")) {
      zin_gvo = new ZipInputStream(new FileInputStream(thefile_gvo));
      if (zin_gvo.getNextEntry() != null)
        imagefile_gvo = new BufferedInputStream(zin_gvo);
    } else {
      imagefile_gvo = new BufferedInputStream(
        new FileInputStream(thefile_gvo));
    }

    GvoServiceFile gsf = new GvoServiceFile();
    String filename_gvo = gsf.gvo_isEncrypt("", imagefile_gvo);

    if (!("".equals(filename_gvo)))
      return;
    gsf.changeFile(filename_gvo, filerealpath_1, iszip, "123");
  }

  public void getSourceFile(String filePath, String souceFile, String isZip)
    throws Exception
  {
    File thefile_gvo = new File(filePath);
    InputStream imagefile_gvo = null;
    ZipInputStream zin_gvo = null;
    if (isZip.equals("1")) {
      zin_gvo = new ZipInputStream(new FileInputStream(thefile_gvo));
      if (zin_gvo.getNextEntry() != null)
        imagefile_gvo = new BufferedInputStream(zin_gvo);
    } else {
      imagefile_gvo = new BufferedInputStream(
        new FileInputStream(thefile_gvo));
    }

    FileOutputStream fos = null;
    BufferedInputStream bis = null;
    int BUFFER_SIZE = 1024;
    byte[] buf = new byte[BUFFER_SIZE];
    int size = 0;
    bis = new BufferedInputStream(imagefile_gvo);
    fos = new FileOutputStream(souceFile, true);
    while ((size = bis.read(buf)) != -1) {
      fos.write(buf, 0, size);
    }
    if (fos != null)
      fos.close();
    if (bis != null)
      bis.close();
    if (zin_gvo != null)
      zin_gvo.close();
    if (imagefile_gvo != null)
      imagefile_gvo.close();
  }

  public void changeFile(String filePath, String lastPath, String isZip, String contentType)
    throws Exception
  {
    int read;
    InputStream in = new FileInputStream(new File(filePath));
    OutputStream fileOut = null;
    if ("1".equals(isZip)) {
      ZipOutputStream filezipOut = new ZipOutputStream(
        new BufferedOutputStream(new FileOutputStream(lastPath)));
      filezipOut.setMethod(8);
      filezipOut.putNextEntry(
        new ZipEntry(new String(getPreName(lastPath).getBytes("GBK"), "ISO8859_1")));
      fileOut = filezipOut;
    } else {
      fileOut = new BufferedOutputStream(new FileOutputStream(lastPath)); }
    if (contentType.equals("application/x-macbinary")) {
      fileOut = new MacBinaryDecoderOutputStream(fileOut);
    }
    byte[] buf = new byte[8192];
    while ((read = in.read(buf)) != -1) {
      fileOut.write(buf, 0, read);
    }
    if (fileOut != null)
      fileOut.close();
    if (in != null)
      in.close();
  }

  private String getPreName(String filePath) {
    String filename = "";
    if ((filePath != null) && (filePath.length() > 0)) {
      int len = filePath.lastIndexOf("/") + 1;
      if (len <= 0)
        len = filePath.lastIndexOf("\\") + 1;
      int last = filePath.indexOf(".zip");
      if (last > len)
        filename = filePath.substring(len, last);
    }
    return filename;
  }

  public boolean gvo_isEncrypt(String log_filename, String pfile)
  {
    boolean state = this.dif.Filestate(log_filename, pfile);

    this.bb.writeLog("加密判断完成！文件：" + pfile + ", 系统原文件：" + log_filename + " 为" + ((state) ? "加密状态" : "不加密状态"));
    return state;
  }

  public String gvo_isEncrypt(String log_filename, InputStream inputStream)
  {
    String path = getPath();

    String fileName = path + getFileName();

    FileOutputStream fos = null;
    BufferedInputStream bis = null;
    int BUFFER_SIZE = 1024;
    byte[] buf = new byte[BUFFER_SIZE];
    int size = 0;
    try {
      bis = new BufferedInputStream(inputStream);
      fos = new FileOutputStream(fileName, true);
      while ((size = bis.read(buf)) != -1)
        fos.write(buf, 0, size);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (fos != null)
          fos.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      try {
        if (bis != null)
          bis.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }

    }

    boolean isEncrypt = gvo_isEncrypt(log_filename, fileName);
    this.bb.writeLog("(GvoServiceFile:241)加密判断完成！文件：" + fileName + " 为" + ((isEncrypt) ? "不加密状态" : "加密状态"));
    if (isEncrypt) {
      String now_fileName = fileName + "_1";
      if (gvo_serviceFile(log_filename, fileName, now_fileName, false))
        return now_fileName;
    }
    return "";
  }

  public boolean gvo_serviceFile(String log_filename, String sourceFile, String nowFile, boolean isencfile)
  {
    boolean result = this.dif.Encode(log_filename, sourceFile, nowFile, isencfile);

    return result;
  }

  public InputStream getGvoInputStream(String log_filename, InputStream inputStream, boolean isPass)
  {
    String path = getPath();

    String fileName = path + getFileName();

    FileOutputStream fos = null;
    BufferedInputStream bis = null;
    int BUFFER_SIZE = 1024;
    byte[] buf = new byte[BUFFER_SIZE];
    int size = 0;
    try {
      bis = new BufferedInputStream(inputStream);
      fos = new FileOutputStream(fileName, true);
      while ((size = bis.read(buf)) != -1)
        fos.write(buf, 0, size);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        fos.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      try {
        bis.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }

    String pfile = fileName + "_1";

    boolean result = gvo_serviceFile(log_filename, fileName, pfile, isPass);

    InputStream result_in = null;
    if (result) {
      try {
        result_in = new FileInputStream(new File(pfile));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }

    }

    return result_in;
  }

  public String getFileName()
  {
    Format ft = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    char[] x = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 
      'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 
      'X', 'Y', 'Z' };

    String str = ft.format(new Date());
    int max_length = x.length;
    Random rm = new Random();

    for (int i = 0; i < 4; ++i) {
      str = str + x[rm.nextInt(max_length)];
    }

    return str;
  }

  public String getPath() {
    Format ft = new SimpleDateFormat("yyyy-MM");

    String path = "D:\\ecologyTempCache\\" + ft.format(new Date()) + "\\";

    File f_apth = new File(path);
    if (!(f_apth.exists())) {
      f_apth.mkdir();
    }
    return path;
  }
}