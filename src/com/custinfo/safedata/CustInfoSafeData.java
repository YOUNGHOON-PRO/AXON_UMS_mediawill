package com.custinfo.safedata;

import messager.common.util.EncryptUtil;

public class CustInfoSafeData {
	
	/**
	 * 암호화
	 * @param data
	 * @param type (RNNO/NOT_RNNO)
	 * @return
	 * @throws Exception
	 */
	public synchronized String getEncrypt(String data, String type) throws Exception {
		return EncryptUtil.getJasyptEncryptedFixString("PBEWithMD5AndDES", type, data);
	}
	
	/**
	 * 복호화
	 * @param data
	 * @param type (RNNO/NOT_RNNO)
	 * @return
	 * @throws Exception
	 */
	public synchronized String getDecrypt(String data, String type) throws Exception {
		return EncryptUtil.getJasyptDecryptedFixString("PBEWithMD5AndDES", type, data);
	}
}
