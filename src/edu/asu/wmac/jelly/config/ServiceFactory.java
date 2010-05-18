package edu.asu.wmac.jelly.config;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import edu.asu.wmac.jelly.build.IBuilder;
import edu.asu.wmac.jelly.deploy.IDeployer;
import edu.asu.wmac.jelly.source.ISource;
/**
 * @author alwold
 * 
 * @version $Revision: 1.4 $
 */
public class ServiceFactory {
   private Hashtable builders;
   private Hashtable sources;
   private Hashtable deployers;
   private static ServiceFactory instance;
   
   private ServiceFactory() {
      builders = new Hashtable();
      sources = new Hashtable();
      deployers = new Hashtable();
      

      
      List projectList = Configuration.getInstance().getJellyConfiguration().getProject();
      Iterator i = projectList.iterator();
      while (i.hasNext()){
      	//Iterate through all available project defined in JellyConfiguration.xml
        ProjectType p = (ProjectType) i.next();
        
        try {
			String cnSource = p.getSourceConfiguration().getDriverClassName();
			sources.put(p.getName(), Class.forName(cnSource).newInstance());
			
			String cnBuild = p.getBuildConfiguration().getDriverClassName();
			builders.put(p.getName(), Class.forName(cnBuild).newInstance());
     
			String cnDeploy = p.getDeployConfiguration().getDriverClassName();
			deployers.put(p.getName(), Class.forName(cnDeploy).newInstance());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
        //TODO: make room in ISource, IDeploy, etc for parameter storage
        //Then populate it. 
      }
   }
   
   public static synchronized ServiceFactory getInstance() {
      if (instance == null) {
         instance = new ServiceFactory();
      }
      return instance;
   }
   
   public ISource getSource(String appName) {
      return (ISource)sources.get(appName);
   }

   public IBuilder getBuilder(String appName) {
      return (IBuilder)builders.get(appName);
   }
   
   public IDeployer getDeployer(String appName) {
      return (IDeployer)deployers.get(appName);
   }
}
