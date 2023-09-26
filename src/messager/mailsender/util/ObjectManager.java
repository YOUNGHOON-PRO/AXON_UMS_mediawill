package messager.mailsender.util;

import java.io.*;

import messager.common.*;
import messager.mailsender.code.*;
import messager.mailsender.config.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ObjectManager
{
	
	private static final Logger LOGGER = LogManager.getLogger(ObjectManager.class.getName());
	
    private ObjectInputStream ois;
    private SendUnit unit;

    public String readObject(String objName) {
        String objPath = new StringBuffer(ConfigLoader.TRANSFER_REPOSITORY_DIR)
            .append("envelope").append(File.separator).append(objName).toString();

        try {
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(objPath)));
            unit = (SendUnit) ois.readObject();
        }
        catch (Exception e) {
        	LOGGER.error(e);
            LogWriter.writeException("ObjectManager", "readObject()", "오브젝트 파일을 읽는데 오류가 발생 했습니다.", e);
            return ErrorCode.OBJSTREAMEXCEPTION;
        }
        finally {
            closeStream();
        }
        return ErrorCode.SUCCESS;
    }

    public SendUnit getSendUnit() {
        return unit;
    }

    public void closeStream() {
        try {
            if (ois != null) {
                ois.close();
            }
        }
        catch (Exception e) {LOGGER.error(e);}
    }
}
