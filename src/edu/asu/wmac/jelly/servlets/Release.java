package edu.asu.wmac.jelly.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;

import edu.asu.wmac.webauth.*;
import edu.asu.wmac.jelly.*;
import edu.asu.wmac.jelly.deploy.DeployStatus;
import edu.asu.wmac.jelly.deploy.RsyncDeployer;

public class Release extends WebAuthServlet {
   private String rsync   = "/usr/bin/rsync";
   private String ssh     = "/usr/bin/ssh";   //hopefully will become obsolete soon   
   private String sshUser = "nsadmin"; 
   
   private String finalDir;
   private String dstPath;
   //private Vector hosts = new Vector();

   public void init() {
     ResourceBundle rb = ResourceBundle.getBundle("edu.asu.wmac.jelly.jelly");
     this.finalDir = rb.getString("finalDir");
   }
  

   public void doRequest(javax.servlet.http.HttpServletRequest req,
                         javax.servlet.http.HttpServletResponse res,
                         WebUser user)
                  throws javax.servlet.ServletException,
                         java.io.IOException

   {
      HttpSession ses = req.getSession();

      res.setBufferSize(0);      
      res.setContentType("text/html");
      PrintWriter out = res.getWriter();

      //out.println("<h2>haha i know who you are because you are: " + user.getUserID() + "</h2><br />");
      if (!user.getUserID().equals("bsamson") && !user.getUserID().equals("alwold") && !user.getUserID().equals("ronpage"))
      {  out.println("<h2>you are not authorized.  please talk to Samson</h2>"); return;}


      out.flush();

      String baseDir = System.getProperty("java.io.tmpdir");
      String sessid;

      if (ses.getAttribute("sessid") != null) {
         sessid = (String) ses.getAttribute("sessid");
      }else{
         out.println("<html><head><title>Jelly</title></head><body>");
         out.println("Hi welcome to Jelly!  This page will let you 'release' the uportal volume<br />"); 
         out.println("There's no temp directory.  Did you build anything yet?");
         return;
      }
      this.finalDir = baseDir + "/" + sessid + "/final-dist";
      out.println("<html><head><title>Jelly</title></head><body>");
      out.println("Hi welcome to Jelly!  This page will let you 'release' the uportal volume<br />"); 
      out.println("<div><b>Your session ID is: " + sessid + "</b></div>");

      Iterator i;

      if (req.getParameter("release") != null){
        String releaseTo = req.getParameter("release");
        if (releaseTo.equals("dev") || releaseTo.equals("qa") || releaseTo.equals("prod")) {
          ResourceBundle rb = ResourceBundle.getBundle("edu.asu.wmac.jelly.jelly");
          Vector hosts = new Vector();
          String dstPath = rb.getString(releaseTo+"DestPath"); 
 
  
          StringTokenizer st = new StringTokenizer(rb.getString(releaseTo + "Hosts"),",");
          while (st.hasMoreTokens())
          {
            String s = st.nextToken();
            System.out.println("JELLYBOT:" + s);
            hosts.addElement(s);
          }
  
          out.println("I will 'release' from " + finalDir + " to local directory " + dstPath + " on the following machines:");
          out.println("<UL>");
  
          i = hosts.iterator();
          while (i.hasNext())
            out.println("<LI>" + i.next() + "</LI>");
        
          out.println("</UL>");
    
          out.println("<form>Click here if you seriously want to release <input type='submit' name='serious' value='serious'>");
          out.println("<input type=\"checkbox\" name=\"delete\"> Clean target directories");
          out.println("<input type='hidden' name='release' value='" + releaseTo + "'></form>");
          
          if ("serious".equals(req.getParameter("serious"))) {
             out.println("deploying...");
             RsyncDeployer deployer = new RsyncDeployer();
             List statusList = deployer.deploy(finalDir, (String[]) hosts.toArray(new String[]{}), dstPath);
             for (Iterator j = statusList.iterator(); j.hasNext(); ) {
                DeployStatus status = (DeployStatus)j.next();
                out.println(status.getHost()+"<br/>");
                out.println("success: "+status.isSuccess()+"<br/>");
                out.println("output:<br/><pre>"+status.getOutput());
                out.println("</pre><p/>");
             }
          }
        } else {
           out.println("quit haxoring and press a button next time");
        }
      } else {   //nothing has been selected for release
         out.println("<form method='post'>Release to Development<input type='submit' name='release' value='dev'></form>");
         out.println("<form method='post'>Release to Quality Assurance<input type='submit' name='release' value='qa'></form>");
         out.println("<form method='post'>Release to Production<input type='submit' name='release' value='prod'></form>");
         out.println("<form method='post'>Clean the distribution directory<input type='submit' name='clean' value='clean'></form>");
      }
 
      if ("clean".equals(req.getParameter("clean"))){
        out.println("<div><font color='red'>cleaning out final distribution directory  (" + this.finalDir + ")</font></div>");
        Util.rmDir(new File(this.finalDir)); 
        out.println("<div><font color='red'>SUCCESS!</font></div>");
      }
 


      out.println("<p>Click <a href=\"BuildChannel\">Here to go to the build page</a></p>");
      out.println("</body></html>");

   }

   protected void execute (String cmd, String[] env, PrintWriter out)
      throws IOException 
   {
      //obfuscate some of the messy stuff
      StringBuffer buf = new StringBuffer();
      Process p = Runtime.getRuntime().exec(cmd,env);
      BufferedInputStream bis = new BufferedInputStream(p.getInputStream());
      BufferedInputStream bes = new BufferedInputStream(p.getErrorStream());
      int keepGoing = 1;
      while (keepGoing != 0) {
        while (bis.available() > 0) 
          out.print((char)bis.read());
        while (bes.available() > 0) 
          out.print((char)bis.read());

        try {
          if (p.exitValue() >= 0) 
             keepGoing = 0;
        }catch (IllegalThreadStateException itse) {
        }
      }
      bis.close();
      bes.close();
 
      return;
   }
   
}
