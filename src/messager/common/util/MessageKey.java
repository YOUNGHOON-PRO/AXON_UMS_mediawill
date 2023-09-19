package messager.common.util;

import java.util.*;

/**
 * 정의 된 머지키 리스트를 리소스 파일에서 읽는다.
 * 머지키 리소스 파일은 messager/common/resource/message.properties  의 경로의 파일이며
 * jar 내부에 포함되고,
 * Neo_Task와 Neo_SubTask에서 검색된 필드의 명이 어떤 머지키를 사용하는가 정의한다.
 * 대상자 기본 정보가 머지되는 키명을 정의 해서 이 정보로 대상자 기본정보를 얻어온다.
 */
public class MessageKey
{
    /** 리소스 파일 경로 */
    private final static String MESSAGE_RESOURCE =
        "messager.common.resource.message";

    /** ResourceBundle */
    private static ResourceBundle resource;
    /** 리소스 파일의 charactor set */
    private static String fileEncoding;

    /** 대상자의 이름에 대한 머지키 */
    public static String TO_NAME;

    /** 대상자의 이메일 주소에 대한 머지키 */
    public static String TO_EMAIL;

    /** 대상자의 User ID에 대한 머지키 */
    public static String TO_ID;
    
    /** 대상자의 User 보안메일 암호 값 */
    public static String TO_ENCKEY;
    
    /** 대상자의 User EAI연계 메시지 값 */
    public static String TO_BIZKEY;

    static {
        fileEncoding = System.getProperty("file.encoding");

        try {
            resource = ResourceBundle.getBundle(MESSAGE_RESOURCE);
            TO_NAME = getName("TO.USER.NAME");
            TO_EMAIL = getName("TO.USER.EMAIL");
            TO_ID = getName("TO.USER.ID");
            TO_ENCKEY = getName("TO.USER.ENCKEY");
            TO_BIZKEY = getName("TO.USER.BIZKEY");
        }
        catch (MissingResourceException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 머지 항목에 대한 머지 키를 얻는다
     * 머지키는 file.encoding 시스템 프로퍼티에 의해 인코딩 된다.
     */
    public static String getName(String key) {
        String name = null;
        try {
            name = resource.getString(key);
            if (name != null && name.length() == 0) {
                name = null;
            }
            else {
                String localeName = new String(name.getBytes("8859_1"), fileEncoding);
                name = localeName;
            }
        }
        catch (Exception ex) {}

        return name;
    }
}
