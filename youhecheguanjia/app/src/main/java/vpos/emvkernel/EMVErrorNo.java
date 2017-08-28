package vpos.emvkernel;

public class EMVErrorNo {

	public static final int EMV_OK = 0; // 成功
	public static final int ERR_EMVRSP = (-1); // 返回码错
	public static final int ERR_APPBLOCK = (-2); // 应用已锁
	public static final int ERR_NOAPP = (-3); // 卡片里没有EMV应用
	public static final int ERR_USERCANCEL = (-4); // 用户取消当前操作或交�?
	public static final int ERR_TIMEOUT = (-5); // 用户操作超时
	public static final int ERR_EMVDATA = (-6); // 卡片数据错误
	public static final int ERR_NOTACCEPT = (-7); // 交易不接�?
	public static final int ERR_EMVDENIAL = (-8); // 交易被拒�?
	public static final int ERR_KEYEXP = (-9); // 密钥过期
	public static final int ERR_NOPINPAD = (-10); // 没有密码键盘或键盘不可用
	public static final int ERR_NOPIN = (-11); // 没有密码或用户忽略了密码输入
	public static final int ERR_CAPKCHECKSUM = (-12); // 认证中心密钥校验和错�?
	public static final int ERR_NOTFOUND = (-13); // 没有找到指定的数据或元素
	public static final int ERR_NODATA = (-14); // 指定的数据元素没有数�?
	public static final int ERR_OVERFLOW = (-15); // 内存溢出
	public static final int ERR_NOTRANSLOG = (-16); // 无交易日�?
	public static final int ERR_NORECORD = (-17); // 无记�?
	public static final int ERR_NOLOGITEM = (-18); // 目志项目错误
	public static final int ERR_ICCRESET = (-19); // IC卡夁位失�?
	public static final int ERR_ICCCMD = (-20); // IC命令失败
	public static final int ERR_ICCBLOCK = (-21); // IC卡锁�?
	public static final int ERR_ICCNORECORD = (-22); // IC卡无记录
	public static final int ERR_GENAC1_6985 = (-23); // GEN AC命令返回6985
	public static final int ERR_USECONTACT = (-24); // 非接失败，改用接触界�?
	public static final int ERR_APPEXP = (-25); // qPBOC卡应用过�?
	public static final int ERR_BLACKLIST = (-26); // qPBOC黑名单卡
	public static final int ERR_GPORSP = (-27);
	public static final int ERR_USEMAG = (-28);
	public static final int ERR_LASTREAD = (-29);
	public static final int ERR_TRANSEXCEEDED = (-30);
	public static final int ERR_QPBOCFDDAFAIL = (-31);
	public static final int ERR_NULL = (-33);
	public static final int ERR_PINBLOCK = (-38);
	
}
