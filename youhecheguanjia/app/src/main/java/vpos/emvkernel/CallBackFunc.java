package vpos.emvkernel;

import android.util.Log;

public class CallBackFunc {

	public static int cEmvLib_WaitAppSel(int TryCnt, String[] Appname, int AppNum)
	{
		if(Appname == null)
		{
			return (-1);
		}
		Log.d("-------CallBackFunc-------","cEmvLib_WaitAppSel--AppNum="+AppNum+"Appname_len="+Appname.length);
		int ret,i;

		for(i = 0; i < AppNum; i++)
		{
			Log.d("-------Appname["+i+"]",Appname[i]);
			Log.d("--EmvLib_GetApp---4--appname=%s", Appname[i]);
		}
	
		return 0;  //选择第一个APP
	}
	
	public static int cEmvLib_GetHolderPwd(int TryCnt,int RemainCnt,  byte[] pin)
	{
		Log.d("-------CallBackFunc-------","cEmvLib_GetHolderPwd---pin.length"+new String(pin).length());
		Log.d("-------CallBackFunc-------","cEmvLib_GetHolderPwd---TryCnt"+TryCnt);
		Log.d("-------CallBackFunc-------","cEmvLib_GetHolderPwd---RemainCnt"+RemainCnt);
		int ret,i;
		for(i = 0; i < pin.length; i++)
		{
			Log.d("-------CallBackFunc-------","--cEmvLib_GetHolderPwd---4--pin="+pin[i]);
		}
		pin[0] =  0x33;
		pin[1] =  0x34;
		pin[2] =  0x35;
		pin[3] =  0x36;
		pin[4] =  0x37;
	
		return 0;  //0 返回正常  
	}
}
