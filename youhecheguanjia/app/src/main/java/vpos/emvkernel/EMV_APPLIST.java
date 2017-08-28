package vpos.emvkernel;

public class EMV_APPLIST {

	
	private byte[] AppName = new byte[33]; 
	private byte[] AID = new byte[17]; 
	private byte AidLen; 
	private byte SelFlag; 
	private byte Priority; 
	private byte TargetPer; 
	private byte MaxTargetPer; 
	private byte FloorLimitCheck; 
	private byte RandTransSel; 
	private byte VelocityCheck;
	private long FloorLimit; 
	private long Threshold;
	private byte[] TACDenial = new byte[6]; 
	private byte[] TACOnline = new byte[6]; 
	private byte[] TACDefault = new byte[6]; 
	private byte[] AcquierId = new byte[7]; 
	private byte[] dDOL = new byte[256]; 
	private byte[] tDOL = new byte[256]; 
	private byte[] Version = new byte[3]; 
	private byte[] RiskManData = new byte[10];
	private byte EC_bTermLimitCheck;
	private long EC_TermLimit;
	private byte CL_bStatusCheck;
	private long CL_FloorLimit;
	private long CL_TransLimit;
	private long CL_CVMLimit;
	private byte TermQuali_byte2;
	
	public EMV_APPLIST() {

	}

	public EMV_APPLIST(byte[] appName, byte[] aID, byte aidLen, byte selFlag,
			byte priority, byte targetPer, byte maxTargetPer,
			byte floorLimitCheck, byte randTransSel, byte velocityCheck,
			long floorLimit, long threshold, byte[] tACDenial,
			byte[] tACOnline, byte[] tACDefault, byte[] acquierId, byte[] dDOL,
			byte[] tDOL, byte[] version, byte[] riskManData,
			byte eC_bTermLimitCheck, long eC_TermLimit, byte cL_bStatusCheck,
			long cL_FloorLimit, long cL_TransLimit, long cL_CVMLimit,
			byte termQuali_byte2) {
		super();
		AppName = appName;
		AID = aID;
		AidLen = aidLen;
		SelFlag = selFlag;
		Priority = priority;
		TargetPer = targetPer;
		MaxTargetPer = maxTargetPer;
		FloorLimitCheck = floorLimitCheck;
		RandTransSel = randTransSel;
		VelocityCheck = velocityCheck;
		FloorLimit = floorLimit;
		Threshold = threshold;
		TACDenial = tACDenial;
		TACOnline = tACOnline;
		TACDefault = tACDefault;
		AcquierId = acquierId;
		this.dDOL = dDOL;
		this.tDOL = tDOL;
		Version = version;
		RiskManData = riskManData;
		EC_bTermLimitCheck = eC_bTermLimitCheck;
		EC_TermLimit = eC_TermLimit;
		CL_bStatusCheck = cL_bStatusCheck;
		CL_FloorLimit = cL_FloorLimit;
		CL_TransLimit = cL_TransLimit;
		CL_CVMLimit = cL_CVMLimit;
		TermQuali_byte2 = termQuali_byte2;
	}

	public byte[] getAppName() {
		return AppName;
	}

	public void setAppName(byte[] appName) {
		AppName = appName;
	}

	public byte[] getAID() {
		return AID;
	}

	public void setAID(byte[] aID) {
		AID = aID;
	}

	public byte getAidLen() {
		return AidLen;
	}

	public void setAidLen(byte aidLen) {
		AidLen = aidLen;
	}

	public byte getSelFlag() {
		return SelFlag;
	}

	public void setSelFlag(byte selFlag) {
		SelFlag = selFlag;
	}

	public byte getPriority() {
		return Priority;
	}

	public void setPriority(byte priority) {
		Priority = priority;
	}

	public byte getTargetPer() {
		return TargetPer;
	}

	public void setTargetPer(byte targetPer) {
		TargetPer = targetPer;
	}

	public byte getMaxTargetPer() {
		return MaxTargetPer;
	}

	public void setMaxTargetPer(byte maxTargetPer) {
		MaxTargetPer = maxTargetPer;
	}

	public byte getFloorLimitCheck() {
		return FloorLimitCheck;
	}

	public void setFloorLimitCheck(byte floorLimitCheck) {
		FloorLimitCheck = floorLimitCheck;
	}

	public byte getRandTransSel() {
		return RandTransSel;
	}

	public void setRandTransSel(byte randTransSel) {
		RandTransSel = randTransSel;
	}

	public byte getVelocityCheck() {
		return VelocityCheck;
	}

	public void setVelocityCheck(byte velocityCheck) {
		VelocityCheck = velocityCheck;
	}

	public long getFloorLimit() {
		return FloorLimit;
	}

	public void setFloorLimit(long floorLimit) {
		FloorLimit = floorLimit;
	}

	public long getThreshold() {
		return Threshold;
	}

	public void setThreshold(long threshold) {
		Threshold = threshold;
	}

	public byte[] getTACDenial() {
		return TACDenial;
	}

	public void setTACDenial(byte[] tACDenial) {
		TACDenial = tACDenial;
	}

	public byte[] getTACOnline() {
		return TACOnline;
	}

	public void setTACOnline(byte[] tACOnline) {
		TACOnline = tACOnline;
	}

	public byte[] getTACDefault() {
		return TACDefault;
	}

	public void setTACDefault(byte[] tACDefault) {
		TACDefault = tACDefault;
	}

	public byte[] getAcquierId() {
		return AcquierId;
	}

	public void setAcquierId(byte[] acquierId) {
		AcquierId = acquierId;
	}

	public byte[] getdDOL() {
		return dDOL;
	}

	public void setdDOL(byte[] dDOL) {
		this.dDOL = dDOL;
	}

	public byte[] gettDOL() {
		return tDOL;
	}

	public void settDOL(byte[] tDOL) {
		this.tDOL = tDOL;
	}

	public byte[] getVersion() {
		return Version;
	}

	public void setVersion(byte[] version) {
		Version = version;
	}

	public byte[] getRiskManData() {
		return RiskManData;
	}

	public void setRiskManData(byte[] riskManData) {
		RiskManData = riskManData;
	}

	public byte getEC_bTermLimitCheck() {
		return EC_bTermLimitCheck;
	}

	public void setEC_bTermLimitCheck(byte eC_bTermLimitCheck) {
		EC_bTermLimitCheck = eC_bTermLimitCheck;
	}

	public long getEC_TermLimit() {
		return EC_TermLimit;
	}

	public void setEC_TermLimit(long eC_TermLimit) {
		EC_TermLimit = eC_TermLimit;
	}

	public byte getCL_bStatusCheck() {
		return CL_bStatusCheck;
	}

	public void setCL_bStatusCheck(byte cL_bStatusCheck) {
		CL_bStatusCheck = cL_bStatusCheck;
	}

	public long getCL_FloorLimit() {
		return CL_FloorLimit;
	}

	public void setCL_FloorLimit(long cL_FloorLimit) {
		CL_FloorLimit = cL_FloorLimit;
	}

	public long getCL_TransLimit() {
		return CL_TransLimit;
	}

	public void setCL_TransLimit(long cL_TransLimit) {
		CL_TransLimit = cL_TransLimit;
	}

	public long getCL_CVMLimit() {
		return CL_CVMLimit;
	}

	public void setCL_CVMLimit(long cL_CVMLimit) {
		CL_CVMLimit = cL_CVMLimit;
	}

	public byte getTermQuali_byte2() {
		return TermQuali_byte2;
	}

	public void setTermQuali_byte2(byte termQuali_byte2) {
		TermQuali_byte2 = termQuali_byte2;
	}
	


}
