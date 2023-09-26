package messager.response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class DemonCheck_Response extends Thread{
	
	private static final Logger LOGGER = LogManager.getLogger(DemonCheck_Response.class.getName());
	
	private String task="";
	
	public DemonCheck_Response(String task) {
		
		this.task=task;
		LOGGER.info("["+task+"] Start!!");
	}
	
	public void run() {
		
		while(true) {
			try {
				LOGGER.info("["+task+"] Alive..");
				sleep(15000);
			}catch (InterruptedException e) {
				LOGGER.error(e);
			}
		}
		
	}
}
