package edu.asu.wmac.jelly.deploy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.asu.wmac.jelly.Util;

/**
 * @author alwold
 * 
 * @version $Revision: 1.5 $
 */
public class RsyncDeployer extends BaseDeployer {
   //TODO: make these settings in the config file
   private final static String rsync   = "/usr/bin/rsync";
   private final static String ssh     = "/usr/bin/ssh";
   private final static String sshUser = "nsadmin";
   private Map options;
   
   public RsyncDeployer() {
      options = new Hashtable();
   }
   
   public List deploy(String sourcePath, String[] hosts, String deployPath) {
      ArrayList baseCmd = new ArrayList();
      baseCmd.add(rsync);
      baseCmd.add("-avv");
      if (options.get("delete") != null && ((Boolean)options.get("delete")).booleanValue()) {
         baseCmd.add("--delete");
      } 
      baseCmd.add(sourcePath + "/");   //The base command is complete, just tack on the hostname next and do it
      List threads = new ArrayList();
      for (int i = 0; i < hosts.length; i++) {
         // start each deploy thread
         Thread thread = new DeployThread(baseCmd, hosts[i], deployPath);
         threads.add(thread);
         thread.start();
      }
      // now wait for them all to finish
      List status = new ArrayList();
      for (Iterator i = threads.iterator(); i.hasNext(); ) {
         DeployThread thread = (DeployThread)i.next();
         try {
            thread.join();
            status.add(thread.getStatus());
         } catch (InterruptedException e) {}
      }
      return status;
   }
   
   public static class DeployThread extends Thread {
      private ArrayList baseCmd;
      private String host;
      private String deployPath;
      private DeployStatus status;
      
      public DeployThread(ArrayList baseCmd, String host, String deployPath) {
         this.baseCmd = baseCmd;
         this.host = host;
         this.deployPath = deployPath;
         this.status = new DeployStatus(host);
      }
      
      public void run() {
         ArrayList cmd = (ArrayList)baseCmd.clone();
         cmd.add(sshUser + "@" + host + ":" + deployPath);
         String env[] = { "RSYNC_RSH=" + ssh };
                  
         try {
            if (Util.execute((String[])cmd.toArray(new String[] {}),env,status.getWriter()) != 0) {
               status.setSuccess(false);
            } else {
               status.setSuccess(true);
            }
         } catch (IOException e) {
            status.getWriter().println("IOException: "+e.getMessage());
            status.setSuccess(false);
         }
      }
      
      public DeployStatus getStatus() {
         return status;
      }
   }
}
