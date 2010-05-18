package edu.asu.wmac.jelly.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.asu.wmac.jelly.kickstart.Kickstart;
import edu.asu.wmac.jelly.kickstart.KickstartException;
import edu.asu.wmac.jelly.kickstart.RestartStatus;
import edu.asu.wmac.webauth.WebAuthServlet;
import edu.asu.wmac.webauth.WebUser;

/**
 * @author alwold
 * 
 * @version $Revision: 1.3 $
 */
public class ServerControl extends WebAuthServlet {
   public final static String RIPPLE_STATUS_KEY = "edu.asu.wmac.jelly.servlets.ServerControl.rippleStatus";
   private String[] devServers;
   private String[] qaServers;
   private String[] prodServers;
   
   public void init() throws ServletException {
      super.init();
      ResourceBundle rb = ResourceBundle.getBundle("edu.asu.wmac.jelly.jelly");
      devServers = rb.getString("devHosts").split(",");
      qaServers = rb.getString("qaHosts").split(",");
      prodServers = rb.getString("prodHosts").split(",");
   }

   public void doRequest(HttpServletRequest req, HttpServletResponse res, WebUser user) throws ServletException, IOException {
      res.setContentType("text/html");
      PrintWriter out = res.getWriter();
      if (req.getParameter("cmd") == null) {
         out.println("<h3>Server Control</h3>");
         listServers(out, "dev", devServers, req.getRequestURI());
         listServers(out, "qa", qaServers, req.getRequestURI());
         listServers(out, "prod", prodServers, req.getRequestURI());
      } else if (req.getParameter("cmd").equals("ripple")) {
         List servers;
         if (req.getParameter("env").equals("dev")) {
            servers = Arrays.asList(devServers);
         } else if (req.getParameter("env").equals("qa")) {
            servers = Arrays.asList(qaServers);
         } else if (req.getParameter("env").equals("prod")) {
            servers = Arrays.asList(prodServers);
         } else {
            out.println("take off, you hoser! there's no such environment as "+req.getParameter("env"));
            return;
         }
         RestartStatus status = Kickstart.startRipple(servers);
         req.getSession().setAttribute(RIPPLE_STATUS_KEY, status);
         out.println("<META HTTP-EQUIV=\"Refresh\" CONTENT=\"5; url="+req.getRequestURI()+"?cmd=rippleStatus\" />");
         out.println("rippling...");
      } else if (req.getParameter("cmd").equals("restart")) {
         try {
            Kickstart.stopTomcat(req.getParameter("server"), 1, true);
            Kickstart.startTomcat(req.getParameter("server"), 1, true);
            Kickstart.stopTomcat(req.getParameter("server"), 2, true);
            Kickstart.startTomcat(req.getParameter("server"), 2, true);
            out.println("ok, restarted");
         } catch (KickstartException e) {
            out.println("error:"+e);
         }
      } else if (req.getParameter("cmd").equals("rippleStatus")) {
         RestartStatus status = (RestartStatus)req.getSession().getAttribute(RIPPLE_STATUS_KEY);
         if (!status.isDone()) {
            out.println("<META HTTP-EQUIV=\"Refresh\" CONTENT=\"5; url="+req.getRequestURI()+"?cmd=rippleStatus\" />");
         } else {
            out.println("Done restarting!");
         }
         List servers = status.getServerList();
         for (Iterator i = servers.iterator(); i.hasNext(); ) {
            String server = (String)i.next();
            out.println(server+"(1) : "+RestartStatus.getStatusString(status.getStatus(server, 1)));
            if (status.getMessage(server, 1) != null) {
               out.println(status.getMessage(server, 1));
            }
            out.println("<br/>");
            out.println(server+"(2) : "+RestartStatus.getStatusString(status.getStatus(server, 2)));
            if (status.getMessage(server, 2) != null) {
               out.println(status.getMessage(server, 2));
            }
            out.println("<br/>");
         }
      } else {
         out.println("take off, you hoser! "+req.getParameter("cmd")+" is a bad command");
      }
   }
   
   public void listServers(PrintWriter out, String env, String[] servers, String url) {
      out.println("<h4>"+env+"</h4>");
      out.println("<a href=\""+url+"?cmd=ripple&env="+env+"\">Ripple All Servers</a><p/>");
      for (int i = 0; i < servers.length; i++) {
         out.println(servers[i]+" <a href=\""+url+"?cmd=restart&server="+servers[i]+"\">Restart</a><br/>");
      }
      out.println("<p>");
   }

}
