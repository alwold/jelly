package edu.asu.wmac.jelly.deploy;

import java.util.List;
import java.util.ResourceBundle;

/**
 * @author alwold
 * 
 * @version $Revision: 1.2 $
 */
public abstract class BaseDeployer implements IDeployer {
   public List deploy(String sourcePath, String environment) {
      ResourceBundle rb = ResourceBundle.getBundle("edu.asu.wmac.jelly.jelly");
      String[] hosts = rb.getString(environment+"Hosts").split(",");
      String deployPath = rb.getString(environment+"DestPath");
      return deploy(sourcePath, hosts, deployPath);
   }
   
   public abstract List deploy(String sourcePath, String[] hosts, String deployPath);

}
