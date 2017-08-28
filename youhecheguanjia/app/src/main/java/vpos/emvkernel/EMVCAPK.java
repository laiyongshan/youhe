package vpos.emvkernel;

public class EMVCAPK {
	private byte[] RID = new byte[5]; // 应用注册服务商ID
	private byte KeyID; // 密钥索引
	private byte HashInd; // HASH算法标志
	private byte ArithInd; // RSA算法标志
	private byte ModulLen; // 模长
	private byte[] Modul = new byte[248]; //�?
	private byte ExponentLen; // 指数长度
	private byte[] Exponent = new byte[3]; // 指数
	private byte[] ExpDate = new byte[3]; // 有效YYMMDD)
	private byte[] CheckSum = new byte[20]; // 密钥校验

	public EMVCAPK() {

	}

	public EMVCAPK(byte[] rID, byte keyID, byte hashInd, byte arithInd,
			byte modulLen, byte[] modul, byte exponentLen, byte[] exponent,
			byte[] expDate, byte[] checkSum) {
		super();
		RID = rID;
		KeyID = keyID;
		HashInd = hashInd;
		ArithInd = arithInd;
		ModulLen = modulLen;
		Modul = modul;
		ExponentLen = exponentLen;
		Exponent = exponent;
		ExpDate = expDate;
		CheckSum = checkSum;
	}

	public byte[] getRID() {
		return RID;
	}

	public void setRID(byte[] rID) {
		RID = rID;
	}

	public byte getKeyID() {
		return KeyID;
	}

	public void setKeyID(byte keyID) {
		KeyID = keyID;
	}

	public byte getHashInd() {
		return HashInd;
	}

	public void setHashInd(byte hashInd) {
		HashInd = hashInd;
	}

	public byte getArithInd() {
		return ArithInd;
	}

	public void setArithInd(byte arithInd) {
		ArithInd = arithInd;
	}

	public byte getModulLen() {
		return ModulLen;
	}

	public void setModulLen(byte modulLen) {
		ModulLen = modulLen;
	}

	public byte[] getModul() {
		return Modul;
	}

	public void setModul(byte[] modul) {
		Modul = modul;
	}

	public byte getExponentLen() {
		return ExponentLen;
	}

	public void setExponentLen(byte exponentLen) {
		ExponentLen = exponentLen;
	}

	public byte[] getExponent() {
		return Exponent;
	}

	public void setExponent(byte[] exponent) {
		Exponent = exponent;
	}

	public byte[] getExpDate() {
		return ExpDate;
	}

	public void setExpDate(byte[] expDate) {
		ExpDate = expDate;
	}

	public byte[] getCheckSum() {
		return CheckSum;
	}

	public void setCheckSum(byte[] checkSum) {
		CheckSum = checkSum;
	}

	
}
