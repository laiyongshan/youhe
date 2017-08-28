package vpos.emvkernel;

public class EmvKernel {
	static {
		System.loadLibrary("Android");
		System.loadLibrary("AndroidEmvKernel");
		
	}
	
	public static final int TYPE_CASH = 0x01;
	public static final int TYPE_GOODS = 0x02;
	public static final int TYPE_SERVICE = 0x04;
	public static final int TYPE_CASHBACK = 0x08;
	public static final int TYPE_INQUIRY = 0x10;
	public static final int TYPE_PAYMENT = 0x20;
	public static final int TYPE_ADMINSTRATIVE = 0x40;
	public static final int TYPE_TRANSFER = 0x80;

	public static final int MAX_APP_NUM = 32;
	public static final int MAX_CAPK_NUM = 64;
	public static final int MAX_CAPKREVOKE_NUM = 96;

	public static final int PART_MATCH = 0x00;
	public static final int FULL_MATCH = 0x01;

	public static final int ICC_USER = 0;
	public static final int ICC_CL = 1;

	public static final int EMV_OK = 0;
	public static final int ERR_EMVRSP = (-1);
	public static final int ERR_APPBLOCK = (-2);
	public static final int ERR_NOAPP = (-3);
	public static final int ERR_USERCANCEL = (-4);
	public static final int ERR_TIMEOUT = (-5);
	public static final int ERR_EMVDATA = (-6);
	public static final int ERR_NOTACCEPT = (-7);
	public static final int ERR_EMVDENIAL = (-8);
	public static final int ERR_KEYEXP = (-9);
	public static final int ERR_NOPINPAD = (-10);
	public static final int ERR_NOPIN = (-11);
	public static final int ERR_CAPKCHECKSUM = (-12);
	public static final int ERR_NOTFOUND = (-13);
	public static final int ERR_NODATA = (-14);
	public static final int ERR_OVERFLOW = (-15);
	public static final int ERR_NOTRANSLOG = (-16);
	public static final int ERR_NORECORD = (-17);
	public static final int ERR_NOLOGITEM = (-18);
	public static final int ERR_ICCRESET = (-19);
	public static final int ERR_ICCCMD = (-20);
	public static final int ERR_ICCBLOCK = (-21);
	public static final int ERR_ICCNORECORD = (-22);
	public static final int ERR_GENAC1_6985 = (-23);
	public static final int ERR_USECONTACT = (-24);
	public static final int ERR_APPEXP = (-25);
	public static final int ERR_BLACKLIST = (-26);
	public static final int ERR_GPORSP = (-27);
	public static final int ERR_USEMAG = (-28);
	public static final int ERR_LASTREAD = (-29);
	public static final int ERR_TRANSEXCEEDED = (-30);
	public static final int ERR_QPBOCFDDAFAIL = (-31);
	public static final int ERR_NULL = (-32);
	public static final int ERR_NOAMT = (-33);
	public static final int ERR_PINBLOCK = (-34);
	
	public static final int REFER_APPROVE = 0x01;
	public static final int REFER_DENIAL = 0x02;
	public static final int ONLINE_APPROVE = 0x00;
	public static final int ONLINE_FAILED = 0x01;
	public static final int ONLINE_REFER = 0x02;
	public static final int ONLINE_DENIAL = 0x03;
	public static final int ONLINE_ABORT = 0x04;

	public static final int PATH_PBOC = 0x00;
	public static final int PATH_QPBOC = 0x01;
	public static final int PATH_MSD = 0x02;
	public static final int PATH_ECash = 0x03;

	private static EmvKernel kernelInstance = null;

	private EmvKernel() {

	}

	public static EmvKernel getInstance() {
		if (kernelInstance == null) {
			kernelInstance = new EmvKernel();
		}

		return kernelInstance;
	}
	
	public static native int  EmvLib_Init();
	
	public static native void EmvLib_SetFilePath(String path);

	public static native int  EmvLib_GetVer();
	
	public static native byte EmvLib_GetPath();

	public static native void EmvLib_ClearTransLog();
	
	public static native int  EmvLib_GetParam(EMV_PARAM tParam);
	
	public static native int  EmvLib_SetParam(EMV_PARAM tParam);
	
	public static native int  EmvLib_GetTLV(String Tag, byte[] DataOut, int[] outLen);

	public static native int  EmvLib_SetTLV(String Tag, byte[] DataIn, int DataLen);

	public static native int  EmvLib_AddApp(EMV_APPLIST App);

	public static native int  EmvLib_GetApp(int Index, EMV_APPLIST App);
	
	public static native int  EmvLib_DelApp(byte[] AID, int AIDLen);
	
	public static native int  EmvLib_AddCapk(EMVCAPK capk);

	public static native int  EmvLib_GetCapk(int Index, EMVCAPK capk);

	public static native int  EmvLib_DelCapk(byte KeyID, byte[] RID);

	public static native int  EmvLib_CheckCapk(byte KeyID, byte[] RID);
	
	public static native int  EmvLib_GetBalance(String BcdBalance);
	
	public static native int  EmvLib_qPBOCPreProcess(long Amount);
	
	public static native int  EmvLib_AppSel(int slot, long TransNo);
	
	public static native int  EmvLib_ReadAppData();
	
	public static native int  EmvLib_CardAuth();
	
	public static native void EmvLib_ProcRestriction();
	
	public static native int  EmvLib_CardholderVerify();
	  
	public static native int  EmvLib_TermRiskManage();
	
	public static native int  EmvLib_BeforeTrans(long Amount,long BackAmt, byte[] Tdate, byte[] Ttime);
	
	public static native int  EmvLib_ProcCLTransBeforeOnline(int Slot,byte[] bIfGoOnline);
	
	public static native int  EmvLib_ProcTransBeforeOnline(int Slot,byte[] bIfGoOnline); 
	
	public static native int  EmvLib_ProcCLTransComplete(byte Result,byte[] RspCode,byte[] AuthCode,  int AuthCodeLen);
	
	public static native int  EmvLib_ProcTransComplete(byte Result,byte[] RspCode,byte[] AuthCode,  int AuthCodeLen, byte[] IAuthData, int IAuthDataLen, byte[] Script, int ScriptLen); 

	public static native int  EmvLib_GetScriptResult(byte[] Result, int[] RetLen);
}
