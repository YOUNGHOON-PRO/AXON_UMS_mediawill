package messager.generator.config;

import java.io.*;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generator의 환경 설정 파일을 로딩한다.
 */
public class ConfigLoader
{
	private static final Logger LOGGER = LogManager.getLogger(ConfigLoader.class.getName());
	
    private static Properties props;
    static {
        props = new Properties();
    }

    public static void load() {
        InputStream is = null;
        String user_dir = System.getProperty("user.dir");
        if (user_dir == null) {
            user_dir = ".";
        }
        String configPath = "conf" + File.separator + "generator.properties";
        File configFile = new File(user_dir, configPath);
        try {
            is = new FileInputStream(configFile);
            props.load(is);
        }
        catch (IOException ex) {
        	LOGGER.error(ex);
            //ex.printStackTrace();
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException ex) {
                	LOGGER.error(ex);
                }
            }
        }
    }

    public static String getProperty(String name) {
        if (name != null) {
            return props.getProperty(name);
        }
        return null;
    }

    public static String getString(String key, String defaultValue) {
        String value = defaultValue;
        if (key != null) {
            value = props.getProperty(key);
            if (value == null || value.length() == 0) {
                value = defaultValue;
            }
        }
        return value;
    }

    public static int getInt(String key, int defaultValue) {
        int ivalue = defaultValue;
        if (key != null) {
            String value = props.getProperty(key);
            if (value != null && value.length() > 0) {
                try {
                    ivalue = Integer.parseInt(value);
                }
                catch (NumberFormatException ex) {
                	LOGGER.error(ex);
                }
            }
        }
        return ivalue;
    }

    public static boolean getBool(String key, boolean defaultValue) {
        boolean value = defaultValue;
        if (key != null) {
            String sValue = props.getProperty(key);
            if (sValue != null) {
                value = Boolean.getBoolean(sValue.toLowerCase());
            }
        }
        return value;
    }
}
