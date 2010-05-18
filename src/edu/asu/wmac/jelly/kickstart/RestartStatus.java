package edu.asu.wmac.jelly.kickstart;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author alwold
 * 
 * @version $Revision: 1.3 $
 */
public class RestartStatus {
   public static final int UNKNOWN = -1;
   public static final int NOTRESTARTED = 1;
   public static final int STOPPED = 2;
   public static final int STOP_ERROR = 3;
   public static final int STARTING = 4;
   public static final int START_ERROR = 5;
   public static final int RESTARTED = 6;
   
   private Map status;
   private Map messages;
   
   private List serverList;
   private boolean done;
   
   public RestartStatus(List serverList) {
      this.serverList = serverList;
      status = new Hashtable();
      messages = new Hashtable();
      for (Iterator i = serverList.iterator(); i.hasNext(); ) {
         String server = (String)i.next();
         status.put(server, new Hashtable());
         messages.put(server, new Hashtable());
      }
      this.done = false;
   }
   
   public int getStatus(String server, int instance) {
      Map instances = (Map)status.get(server);
      if (instances == null) {
         return -1;
      } else {
         Integer statusValue = (Integer)instances.get(new Integer(instance));
         if (statusValue == null) {
            return -1;
         }
         return statusValue.intValue();
      }
   }
   
   public void setStatus(String server, int instance, int statusValue) {
      Map instances = (Map)status.get(server);
      // TODO deal with a null instances
      instances.put(new Integer(instance), new Integer(statusValue));
   }
   
   public String getMessage(String server, int instance) {
      Map instances = (Map)messages.get(server);
      if (instances == null) {
         return null;
      } else {
         String messageValue = (String)instances.get(new Integer(instance));
         return messageValue;
      }
   }
   
   public void setMessage(String server, int instance, String messageValue) {
      Map instances = (Map)messages.get(server);
      // TODO deal with a null instances
      instances.put(new Integer(instance), messageValue);
   }
   
   public List getServerList() {
      return serverList;
   }
   
   public static String getStatusString(int status) {
      switch(status) {
         case UNKNOWN:
            return "Unknown";
         case NOTRESTARTED:
            return "Not yet restarted";
         case STOPPED:
            return "Stopped";
         case STOP_ERROR:
            return "Error stopping";
         case STARTING:
            return "Starting";
         case START_ERROR:
            return "Error starting";
         case RESTARTED:
            return "Restart completed";
         default:
            return "Unknown";
      }
   }

   public void setDone(boolean done) {
      this.done = done;
   }
   
   public boolean isDone() {
      return done;
   }
}
