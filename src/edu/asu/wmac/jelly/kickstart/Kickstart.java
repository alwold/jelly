package edu.asu.wmac.jelly.kickstart;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import edu.asu.wmac.jelly.Util;

/**
 * @author alwold
 * 
 * @version $Revision: 1.3 $
 */
public class Kickstart {
   /**
    * Stop a tomcat instance
    * 
    * @param host The hostname to stop on
    * @param instance The number of the instance to stop
    * @param wait Whether or not to wait for the clone to fully stop
    * @throws IOException
    */
   public static void stopTomcat(String host, int instance, boolean wait) throws KickstartException {
      try {
         // run the kickstart command
         if (Util.execute(new String[]{"ssh", "nsadmin@"+host, "/usr/local/sbin/kickstart "+(1+instance)}, new String[0], new PrintWriter(new StringWriter())) != 0) {
            throw new KickstartException("Unable to stop tomcat on "+host+", instance "+instance);
         }
      } catch (IOException ioe) {
         throw new KickstartException("Unable to stop tomcat on "+host+", instance "+instance+" ("+ioe.getMessage()+")");
      }
      if (wait) {
         // now check if it is down until it is
         boolean refused = false;
         Socket s = new Socket();
         SocketAddress sa = new InetSocketAddress(host, 8079+instance);
         int count = 0;
         while(!refused && count < 4) {
            count++;
            try {
               s.connect(sa, 2000);
            } catch (IOException e) {
               refused = true;
               break;
            }
            try {
               Thread.sleep(2000);
            } catch (InterruptedException e1) {
               throw new KickstartException("Interrupted");
            }
         }
         if (!refused) {
            throw new KickstartException("Clone on "+host+", instance "+instance+" appears to be hung");
         }
      }
   }
   
   public static void startTomcat(String host, int instance, boolean wait) throws KickstartException {
      try {
         // run the kickstart command
         if (Util.execute(new String[]{"ssh", "nsadmin@"+host, "/usr/local/sbin/kickstart "+((-1)+instance)}, new String[0], new PrintWriter(new StringWriter())) != 0) {
            throw new KickstartException("Unable to start tomcat on "+host+", instance "+instance);
         }
      } catch (IOException ioe) {
         throw new KickstartException("Unable to start tomcat on "+host+", instance "+instance+" ("+ioe.getMessage()+")");
      }
      if (wait) {
         boolean running = false;
         int count = 0;
         while(!running && count < 6) {
            try {
               count++;
               HttpURLConnection.setFollowRedirects(false);
               HttpURLConnection con = (HttpURLConnection)new URL("http://"+host+":"+(8079+instance)+"/uPortal/").openConnection();
               if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                  running = true;
                  break;
               }
            } catch (MalformedURLException e) {
               throw new KickstartException("Internal error (Malformed URL: "+e.getMessage()+")");
            } catch (IOException ignore) {}
            try {
               Thread.sleep(5000);
            } catch (InterruptedException e1) {
               throw new KickstartException("Interrupted");
            }
         }
         if (!running) {
            throw new KickstartException("Tomcat on "+host+", instance "+instance+" did not start within 30s.");
         }
      }
   }
   
   public static RestartStatus startRipple(List servers) {
      RestartStatus status = new RestartStatus(servers);
      new RippleThread(status, servers).start();
      return status;
   }
   
   public static void main(String[] args) throws KickstartException, InterruptedException {
      System.out.println("restarting lion");
      List servers = Arrays.asList(new String[]{"lion.xxx"});
      RestartStatus status = startRipple(servers);
      while(true) {
         for (Iterator i = servers.iterator(); i.hasNext(); ) {
            String server = (String)i.next();
            System.out.println("clone 1: "+status.getStatus(server, 1)+" "+status.getMessage(server, 1));
            System.out.println("clone 2: "+status.getStatus(server, 2)+" "+status.getMessage(server, 2));
            Thread.sleep(1000);
         }
      }
   }
   
   public static class RippleThread extends Thread {
      private RestartStatus status;
      private List servers;
      
      public RippleThread(RestartStatus status, List servers) {
         this.status = status;
         this.servers = servers;
      }
      
      public void run() {
         List threads = new ArrayList();
         for (Iterator i = servers.iterator(); i.hasNext(); ) {
            Thread thread = new RestartThread((String)i.next(), status);
            thread.start();
            threads.add(thread);
         }
         for (Iterator i = threads.iterator(); i.hasNext(); ) {
            Thread thread = (Thread)i.next();
            try {
               thread.join();
            } catch (InterruptedException e) {
            }
         }
         status.setDone(true);
      }
   }
   
   public static class RestartThread extends Thread {
      private String server;
      private RestartStatus status;
      
      public RestartThread(String server, RestartStatus status) {
         super();
         this.server = server;
         this.status = status;
      }
      
      public void run() {
         status.setStatus(server, 1, RestartStatus.NOTRESTARTED);
         status.setStatus(server, 2, RestartStatus.NOTRESTARTED);
         // stop the first tomcat and if it stops, restart it
         // if the first gets restarted, do the same for the second
         try {
            Kickstart.stopTomcat(server, 1, true);
            status.setStatus(server, 1, RestartStatus.STOPPED);
         } catch (KickstartException ke) {
            status.setMessage(server, 1, ke.getMessage());
            status.setStatus(server, 1, RestartStatus.STOP_ERROR);
         }
         if (status.getStatus(server, 1) == RestartStatus.STOPPED) {
            try {
               status.setStatus(server, 1, RestartStatus.STARTING);
               Kickstart.startTomcat(server, 1, true);
               status.setStatus(server, 1, RestartStatus.RESTARTED);
            } catch (KickstartException ke) {
               status.setMessage(server, 1, ke.getMessage());
               status.setStatus(server, 1, RestartStatus.START_ERROR);
            }
         }
         if (status.getStatus(server, 1) == RestartStatus.RESTARTED) {
            try {
               Kickstart.stopTomcat(server, 2, true);
               status.setStatus(server, 2, RestartStatus.STOPPED);
            } catch (KickstartException ke) {
               status.setMessage(server, 2, ke.getMessage());
               status.setStatus(server, 2, RestartStatus.STOP_ERROR);
            }
            if (status.getStatus(server, 2) == RestartStatus.STOPPED) {
               try {
                  status.setStatus(server, 2, RestartStatus.STARTING);
                  Kickstart.startTomcat(server, 2, true);
                  status.setStatus(server, 2, RestartStatus.RESTARTED);
               } catch (KickstartException ke) {
                  status.setMessage(server, 2, ke.getMessage());
                  status.setStatus(server, 2, RestartStatus.START_ERROR);
               }
            }
         }
      }
   }
}
