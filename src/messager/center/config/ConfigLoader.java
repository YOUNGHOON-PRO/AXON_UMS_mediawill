package messager.center.config;

import java.io.*;
import java.util.Properties;

/**
 * 환경 설정 파일를 로드한다.
 */
public class ConfigLoader
{
    private final static String CENTER_MAIN_CONFIG = "center.properties";
    private final static String DATABASE_CONFIG = "database.properties";
    private static String conf_dir;
    private static Properties props;

    static {
        String user_dir = System.getProperty("user.dir");
        if (user_dir == null) {
            user_dir = ".";
        }

        File userDirFile = new File(user_dir, "conf");
        conf_dir = userDirFile.getAbsolutePath();
    }

    /**
     * conf/center.properties 파일 로드
     */
    public static void load() {
        InputStream is = null;
        synchronized (ConfigLoader.class) {
            if (props != null) {
                return;
            }
            props = new Properties();
            File configFile = new File(conf_dir, CENTER_MAIN_CONFIG);
            try {
                is = new FileInputStream(configFile);
                props.load(is);
            }
            catch (IOException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch (IOException ex) {
                    }
                }
            }
        }
    }

    /**
     * 파일로 부터 읽혀진 Property 리스트에서 지정된 키의 Property를 얻는다.
     *
     * @param name 프로퍼티 키
     * @return 지정된 키의 Property 값
     */
    public static String getProperty(String name) {
        if (props == null) {
            load();
        }
        if (name != null) {
            return props.getProperty(name);
        }
        return null;
    }

    /**
     * 지정된 키의 Property를 int형으로 얻는다.
     *
     * @param name Property의 키
     * @param defaultValue 존재하지 않는 값일 경우 리턴할 값
     * @return 지정된 키의 int형으로 변환한 값, 존재하지 않을 경우 defaultValue
     */
    public static int getInt(String name, int defaultValue) {
        if (props == null) {
            load();
        }
        int value = defaultValue;

        if (name != null) {
            String sValue = props.getProperty(name);
            if (sValue != null) {
                try {
                    value = Integer.parseInt(sValue);
                }
                catch (Exception ex) {
                }
            }
        }
        return value;
    }

    /**
     * 지정된 키의 Property를 boolean형으로 얻는다.
     *
     * @param name 키의 이름.
     * @param defaultValue 해당 키의 값이 저장되어있지 않을 때 기본 반환값.
     * @return 키에 해당하는 boolean값
     */
    public static boolean getBool(String name, boolean defaultValue) {
        if (props == null) {
            load();
        }
        boolean value = defaultValue;

        if (name != null) {
            String sValue = props.getProperty(name);
            if (sValue != null) {
                value = Boolean.getBoolean(sValue.toLowerCase());
            }
        }
        return value;
    }

    /**
     * work DB 의 접속 정보 파일 로드
     * @return 환경설정 Properties 객체
     */
    public static Properties getDBProperties() {
        InputStream is = null;
        File configFile = new File(conf_dir, DATABASE_CONFIG);
        Properties properties = new Properties();

        try {
            is = new FileInputStream(configFile);
            properties.load(is);
        }
        catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException ex) {
                }
                is = null;
            }
        }

        return properties;
    }

    public static String getString(String keyName, String defaultValue) {
        String value = null;
        if (keyName != null) {
            value = props.getProperty(keyName);
        }

        if (value == null) {
            value = defaultValue;
        }
        return value;
    }
}
