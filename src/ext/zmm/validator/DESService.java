package ext.zmm.validator;

public class DESService {
	public native boolean setServerMessage(String serverip,String messageip);
	
	public native boolean fileOperation(int tag,String username,String password,String sfile,String pfile,boolean isenfile);
	
	public native boolean writeLog(String username,String password,String time,String operation,String content,String result,String cause,String remark);
	
	public native int getLastErrorID();
	
	public native void setSoftOperationTag(String tag);
	
	public native boolean addEmail(String userID,String email,int emailType);
	
	public native boolean delEmail(String userID,String email,int emailType);
	
	public native boolean getEmail(String userID,String email,int bufferSize,int emailType);
	
	public native boolean publishedEmail(String userID,int emailType);
	
	
	public native boolean createAuthKey(char[] key);
	
	public native boolean createAuthMessage(char[] key,char[] auth);
	

	public native boolean checkAuthMessage(char[] message);

	public native void SetLinkID(int ID);

	public native void SetLinkType(int type);

	public native void DisConnectServer();

	public native int[] FileIsEncode(String username,String password,String sfile);

	public native int[] FileIsEncodeFile(String sfile);
	public native boolean initSystemFile(String username,String password);
	public native boolean fileOperationFile(String sfile,String pfile,boolean isenfile);
	
	
	static{
		System.out.println(System.getProperty("java.library.path"));
		System.loadLibrary("JNL_FileOP");
		
//		System.load("c:\\Windows\\System32\\JNL_FileOP.dll");
	}
}
