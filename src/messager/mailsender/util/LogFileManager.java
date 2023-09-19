package messager.mailsender.util;

import java.io.*;
import java.text.*;
import java.util.*;

import messager.mailsender.config.*;

public class LogFileManager
    extends Thread
{
    SimpleDateFormat day;
    String daily;

    public LogFileManager() {

    }

    public void run() {
        while (true) {
            String[] delFileList = getFileList();
            int listCount = delFileList.length;
            int delDay = 0;

            try {
                day = new SimpleDateFormat("dd", Locale.US);
                daily = day.format(new Date());
                 for (int k = 0; k < listCount; k++) {

                    delDay = Integer.parseInt(delFileList[k].substring(8, 10));
      
                    if (delDay < Integer.parseInt(daily)) {

                    	File tempFile = new File(new StringBuffer()
                                                 .append(ConfigLoader.TRANSFER_ROOT_DIR)
                                                 .append("log/")
                                                 .append(delFileList[k])
                                                 .toString());
                        if (tempFile.delete()) {
                        	
                            LogWriter.writeError("LogFileManager", "run()", "로그 파일 삭제 하였습니다",
                                                 delFileList[k]);
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            try {
                super.sleep(Integer.parseInt(ConfigLoader.DELETE_PERIOD.trim()));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String[] getFileList() {
        String[] fileList = null;
        try {
//			File root = new File(ConfigLoader.TRANSFER_ROOT_DIR + "log/");
            fileList = new java.io.File(ConfigLoader.TRANSFER_ROOT_DIR + "log/").list();
            return fileList;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
    }
}
