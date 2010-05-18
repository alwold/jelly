/*
 * Created on Jan 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.asu.wmac.jelly.config;


import javax.xml.bind.*;
/**
 * @author bsamson
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Configuration {

	private static Configuration instance;
	private JellyConfiguration jc;
	private static String xmlFile ="/edu/asu/wmac/jelly/config/jellyConfiguration.xml";
	
	public static Configuration getInstance() {
		   if (instance == null) {
		      instance = new Configuration();
		   }
		   return instance;
	}
	
	
	private Configuration() {
		
		//initalize Configuration instance here from xml
		
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance("edu.asu.wmac.jelly.config");
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			this.jc = (JellyConfiguration) unmarshaller.unmarshal(this.getClass().getResourceAsStream(xmlFile));
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Configuration.instance=null;
			e.printStackTrace();
		}
		
	}

	 public JellyConfiguration getJellyConfiguration() {
		return this.jc;
	}
	
}


