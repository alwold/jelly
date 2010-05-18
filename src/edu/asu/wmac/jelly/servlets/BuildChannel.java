package edu.asu.wmac.jelly.servlets;
import edu.asu.wmac.jelly.*;

import java.io.*;
import java.util.*;
import edu.asu.wmac.webauth.*;


public class BuildChannel extends WebAuthServlet {
   private String cvsRoot;
   private String frameworkModule;
   private String channelList;
   private String carfileList;
   private String tempDir; 
   private String ant;
   private String javaHome;
   private String finalDir;
   private String cvsPassword;
   private String buildDir;

   public void init() {
      
     ResourceBundle rb = ResourceBundle.getBundle("edu.asu.wmac.jelly.jelly");
     this.frameworkModule = rb.getString("cvsFrameworkModule");
     this.channelList = rb.getString("cvsChannels");
     this.tempDir = rb.getString("tempDir");
     this.cvsRoot = rb.getString("cvsRoot");
     this.ant = rb.getString("ant");
     this.javaHome = rb.getString("JAVA_HOME");
     this.finalDir = rb.getString("finalDir");
     this.carfileList = rb.getString("carFiles");
     this.cvsPassword = rb.getString("cvsPassword");
     this.buildDir = "/usr/local/jelly/build";
   }

   public void doRequest(javax.servlet.http.HttpServletRequest req,
                         javax.servlet.http.HttpServletResponse res,
                         WebUser user)
                  throws javax.servlet.ServletException,
                         java.io.IOException
   {
     //Set up some html stuff
     PrintWriter out = res.getWriter();
     res.setBufferSize(1);
     res.setContentType("text/html");
     out.println("<html><head><title>JELLY</title></head>");
     String baseDir = System.getProperty("java.io.tmpdir");
     String sessid;

     
     if (req.getSession().getAttribute("sessid") != null) {
        sessid = (String) req.getSession().getAttribute("sessid");
        this.tempDir = baseDir + "/" + sessid + "/temp";
     }else{
        try { 
          File dir =  Util.createTempDir();
          sessid = dir.getName();
          File tdir = new File(dir.getPath() + "/temp");
          tdir.mkdir();
          File bdir = new File(dir.getPath() + "/build"); 
          bdir.mkdir();
        } catch (JellyException je) { 
          out.println("Cant create temp dir! " + je);
          return;
        }
        req.getSession().setAttribute("sessid", sessid);
     }
     this.tempDir = baseDir + "/" + sessid + "/temp";
     this.buildDir = baseDir + "/" + sessid + "/build";
     this.finalDir = baseDir + "/" + sessid + "/final-dist";

     out.println("<div><b>Your session ID is: " + sessid + "</b></div>");
     
     char patch = 'n';
     if (req.getParameter("p-patches") != null) 
        patch = 'p';
     if (req.getParameter("q-patches") != null) {
       if (patch == 'p') {
         out.println("Cannot apply both P and Q!!!?!");
         return;
       } else {
         patch = 'q';
       }
     }

     if (req.getParameter("carFile") != null) {
        //manual car file
        String carFile = req.getParameter("carFile");
        out.println("<div>Publishing " + carFile + " to the final distribution space.  You will still need to release the volume. If you want to push to other servers</div>");
        try {
          File cf = new File(carFile);
          Util.copyFile(cf.getPath(), this.finalDir + "/WEB-INF/cars/" + cf.getName());
        } catch (Exception e){
          out.println("Error in copying the carfile:" + e );
        }
        out.println("<div>Success!  Select another channel to build or click <a href=\"Release\">Here</a> to release the volume.</div>"); 
     }
 
     if (req.getParameter("build") != null) {
        
        //build a channel
        String cvsTarget=req.getParameter("build");
        
        out.println("<div>Building " + cvsTarget + "...</div>");
        out.flush();

       if (req.getParameter("tag") != null) {
         String cvsTag = req.getParameter("tag");         //--actual work zone--\\
         String retMsg;
         try {
            if (cvsTag.equals(""))
              throw new JellyException("No CVS Tag");
            buildFromCvs(cvsTarget, cvsTag, patch, out);
            out.println("<div>Completed building " + cvsTarget + ", and it is sitting read to deploy</div>");
         }catch (JellyException je){
            out.println("<div>Things did not finish because of: " + je + "</div>");
         }

         out.println("<a href=\"index.jsp\">Go Back to the menu to build another channel or release</a>");
       } else {
         //prompt for a tag
         out.println("<br><form method=\"post\">Please specify the cvs tag to build from: <input name=\"tag\">");
         //out.println("<br><br>Publish to Development:<input type=\"radio\" name=\"dest\" value=\"dev\" CHECKED=\"CHECKED\">");
         //out.println("<br>Publish to Quality Assurance:<input type=\"radio\" name=\"dest\" value=\"qa\">");
         //out.println("<br>Publish to Production:<input type=\"radio\" name=\"dest\" value=\"prod\">");
         out.println("<br><br>Include " + cvsTarget + "-p patches <input type=checkbox name=\"p-patches\">");
         out.println("<br><br>Include " + cvsTarget + "-q patches <input type=checkbox name=\"q-patches\">");
         out.println("<br><input type=\"hidden\" name=\"build\" value=\"" + cvsTarget + "\"><input type=\"submit\"></form>");
         out.println("<br>P.S. The WMAC convention is to tag the following way (for migrating to production):<br>");
         out.println("<pre>PROD-YYYYMMDD (i.e. PROD-20040802 for August 2nd 2004</pre>");
         out.println("<br>Also note that this tag needs to be created in CVS FIRST before using jelly to migrate");
       }

     }  else  {    
      //prompt for a build parameter 
      try {

        out.println("<h2>Hello please select a channel to build from cvs:</h2>");
        out.println("<ul>");

        StringTokenizer st = new StringTokenizer(channelList, ",");
        while (st.hasMoreTokens()){
          String nt=st.nextToken();
          out.println("<li><a href=\"BuildChannel?build=" + nt + "\">" + nt + "</a></li>");
        }
        out.println("</ul>"); 

        out.println("Or <a href=\"BuildChannel?build=" + frameworkModule + "\">click here to build the framework from cvs</a>");
        out.println("<br><br>Note: if your channel does not appear here please add it to the properties file or talk to Samson"); 
        out.println("<p>Or if you are not in CVS yet, please enter the path(in afs or on local disk) in this box:</p>");
        out.println("<form method=\"post\">Filename:<input name=\"carFile\" size=\"30\"><input type=\"submit\">");
        out.println("</form>");

        out.println("<p>Or push one of the following car files from cvs(uportal-cars)</p>");
/*****
        st = new StringTokenizer(carfileList, ",");
        while (st.hasMoreTokens()){
          String cf = st.nextToken();
          out.println("<form><input type=\"hidden\" name=\"carFile\" value=\"" + cf + "\">");
          out.println("<input type=\"submit\" value=\"" + cf + "\"></form>"); 
        }
******/
       
        CarManager carman = new CarManager(this.tempDir);
        ArrayList cars = carman.getCarFiles();
        Iterator i = cars.iterator();
        while (i.hasNext()) {
          File cf = (File)i.next();
          out.println("<form style=\"display:inline;\"><input type=\"hidden\" name=\"carFile\" value=\"" + cf.getPath() + "\">");
          out.println("<input type=\"submit\" value=\"" + cf.getName() + "\"></form>");
        } 
   
      }catch (Exception ignorefornow){
        ignorefornow.printStackTrace(System.out);
      }

     }  //end if/else
 
   }  

   protected void buildFromCvs(String module, String tag, char patch, PrintWriter out) throws JellyException
   {
      boolean buildingChannel = true;           //Determine if we are building framework or channel so I only need one method
      if (module.equals(this.frameworkModule)) {  
        buildingChannel = false; 
      }
      if (new File(tempDir).exists()) {
        out.println("<div>Temp dir is unclean.....</div>");
        out.flush();
        cleanTempDir();
        out.println("<div>Better now!</div>");
      }
 
      try{
         Util.cvsExport(module,tag,out,this.tempDir);

         if ( patch == 'q' || patch == 'p' ) {
           //apply the production patches before building
           String patchModule;
           out.println("<div>Applying -" + patch + " patches...</div>");
           patchModule=module + "-" + patch;
           out.flush();

           Util.cvsExport(patchModule, "HEAD", out, this.tempDir);
           String[] cpCmd = {"/bin/bash", "-c", "/bin/cp -Rv " + this.tempDir + "/" + patchModule + "/* " + this.tempDir + "/" + module } ;
           StringWriter cpRes = new StringWriter();
           int pExit = Util.execute(cpCmd, new String[] {}, new PrintWriter(cpRes)); 
           if (pExit != 0 ){
             throw new JellyException("There was an error applying the " + patchModule + " patches (during copy: " + pExit + ")");
           }else{
             out.println("<pre>" + cpRes.toString() + "</pre><br>");
             out.println("<p>succesfully applied patches</p>");
             out.flush();
           }
           
         }
          
         //time to run ant
         //First find the build file:
         File buildFile = Util.findFile("build.xml", this.tempDir + "/" + module);
         if (buildFile.getPath().toString().length() != 0){
           out.println("build.xml file found: " + buildFile.getPath());
         }else{
           throw new JellyException("no build.xml file in CVS pls fix");
         }

         String[] antCmd;
         if (buildingChannel) {
           antCmd = new String[] { this.ant, 
                                   "dist",
                                   "-f", buildFile.getPath()};
         }else{
           antCmd = new String[] { this.ant,
                                   "deploy",
                                   "-f", buildFile.getPath(),
                                   "-Ddeploy.home=" + this.finalDir, 
                                   "-Dserver.home=" + this.tempDir + "/server.home",
                                   "-Dbuild.home=" + this.buildDir};
         } 
         out.println("<div>Invoking ant...(This may take a while, be patient please)</div><br><pre>");
         out.flush();
         int antRes = Util.execute(antCmd, new String[] {"JAVA_HOME=" + this.javaHome}, out);
         if (antRes != 0){
           throw new JellyException("Ant failed! I'm not sure why!");
         }
         out.println("</pre><br>Done!");
         if (!buildingChannel){
            out.println("<div>copying source for reference</div>");
            String[] cpSrc =  new String[] { "cp", "-R", this.tempDir+"/" + module, finalDir + "/WEB-INF/src/"};
            Util.execute(cpSrc, new String[] {}, new PrintWriter(System.out) );
         }


         //Move the car file if there is one 
         if (buildingChannel) { 
           File carFile = findCarFile(this.tempDir + "/" + module);
           if (carFile == null) {
             throw new JellyException("car file not found pls fix!");
           }
           out.println("Found car file: " + carFile.getName() + ", Moving to: " + this.finalDir + "/WEB-INF/cars/" + carFile.getName());
           Util.copyFile(carFile.getPath(), this.finalDir  + "/WEB-INF/cars/" + carFile.getName());       
         } 
 
       } catch (Exception e) {
         if (e instanceof JellyException) {
            throw (JellyException) e; 
         }
         e.printStackTrace(System.out);
       }
     
     out.println("<div>Removing temporary directory" + tempDir + "</div>");
     cleanTempDir();
   }

   public File findCarFile(String startDir) { // application's entry point
      System.out.println("in FindCarFile");
      String files[];
      Vector agenda = new Vector();
      //File root = new File(System.getProperty("user.dir"));
      File root = new File(startDir);
      // initialise the agenda to contain just the root directory
      agenda.addElement(root);

      while (agenda.size() > 0) {         // loop until empty
         File dir = (File)agenda.elementAt(0); // get first dir
         // System.out.println(dir);
         agenda.remove(0);       // remove it
         files = dir.list();              // get list of files

         for (int i = 0; i < files.length; i++) { // iterate
            File f = new File(dir, files[i]);
            System.out.println("looking at: " + f.getName());
            if (f.isDirectory()) {        // see if it's a directory
               agenda.insertElementAt(f, 0); } // add dir to start of agenda
            else if (f.getName().endsWith(".car")) { // test for target
               System.out.println(f);     // print out the full path
               return(f);
            }
         }
      }
      System.out.println("Leaving FindCarFile(failed)");
      return null;
   }

   protected void cleanDistDir()
     throws JellyException
   {
     try {
       Util.rmDir (new File(this.finalDir));    
     } catch (IOException ioe) {
       throw new JellyException("Could not clean local Distribution directory" + this.tempDir);
     }
   }

   protected void cleanTempDir()
     throws JellyException
   {
     try {
       Util.rmDir (new File(this.tempDir));    
     } catch (IOException ioe) {
       throw new JellyException("Could not clean temp directory" + this.tempDir);
     }
   }
  
}
