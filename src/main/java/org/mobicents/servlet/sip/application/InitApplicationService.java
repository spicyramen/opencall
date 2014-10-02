package org.mobicents.servlet.sip.application;

import org.apache.log4j.Logger;

public class InitApplicationService {

	private static final Logger logger = Logger.getLogger(InitApplicationService.class);
	private static boolean initError = true;
	//private String configFile = "../standalone/configuration/opencall/opencall.ini";
	
	public void startService() throws Exception {
		
		logger.debug("InitAppService - starting!");
		Thread intializationThread = new Thread(new RunInitialization());

		intializationThread.start();
		intializationThread.join();
		
		if (!initError) {

			logger.debug("Sending done notification to barrier");

			
			//long now = System.currentTimeMillis();
			

			logger.debug("InitAppServiceMBean - started!");

		}

		else {

			logger.error("InitAppServiceMBean Error!");

		}

		
		
	}
	
	public void stopService() throws Exception {

		logger.debug("InitAppService - stopping!");
		logger.debug("Service stopped - stopped!");

	}
	
	private class RunInitialization implements Runnable {

		public void run() {

			synchronized (this) {

				try {

					logger.debug("Starting processing in Opencall Engine Init-App service");
					logger.debug("Starting pre-population in Opencall Engine Init-App Service");
					//general initializations
					// Read system configuration
					//engine-only initializations
					initError = false;

				} catch (Exception e) {

					initError = true;

					logger.error("The Configuration was not populated correctly", e);

				}

			}

		}

	}

	
}
