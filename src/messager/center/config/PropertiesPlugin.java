package messager.center.config;

import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * log4j2.xml, log4j2.properties에서 VM arguments -Droot.dir=...의 값을 사용할때 ${sys:root.dir}를 사용하는데,
 * Application *.properties에서도 동일한 기능을 사용하게 해줌
 * VM arguments -Droot.dir=...로 지정하고 사용하는 properties에서는 ${sys:root.dir}처럼 사용 한다.
 */
public class PropertiesPlugin {
	private static final Logger LOGGER = LogManager.getLogger(PropertiesPlugin.class);
	private static String[] systemProperty = {"root.dir"};
	
	public static void repalceSystemProperty(Properties props) {
        Set<String> propertyKeys = props.stringPropertyNames();
        for (String key : propertyKeys) {
            String value = props.getProperty(key);
            for(int i = 0; i < systemProperty.length; i++) {
	            if(value.indexOf("${sys:"+systemProperty[i]+"}") >= 0) {
	            	LOGGER.info("REPLACE System Property {}", systemProperty[i]);
	            	LOGGER.info("  - {}={}", key, value);
	            	value = value.replace("${sys:"+systemProperty[i]+"}", System.getProperty(systemProperty[i]));
	            	props.setProperty(key, value);
	            	LOGGER.info("  - {}={}", key, value);
	            }
            }
        }
	}
}
