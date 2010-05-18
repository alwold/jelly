package edu.asu.wmac.jelly;

import java.io.*;
import java.util.*;
import java.lang.reflect.Array;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.command.export.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.admin.*;

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.*;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.publickey.*;

public abstract class Util {

   public static int execute (String[] cmd, String[] env, PrintWriter out)
      throws IOException
   {
      int retVal = -1;
      System.out.println("executing: " + cmd[0] );
      for ( int i=0; i < Array.getLength(cmd) ; i++ ) {
      	System.out.print(cmd[i] + ' ');
      }
      System.out.println(" ");

      
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
          out.print((char)bes.read());

        out.flush();

        try {
          retVal = p.exitValue();   //this will throw an exception if the process isn't done yet
          //System.out.println("exitValue is: " + retVal + " buffers are: " + bis.available() + ":" + bes.available());
          //if (bis.available() == 0 & bes.available() == 0)
          keepGoing = 0;
        }catch (IllegalThreadStateException itse) {
        }
      }
      bis.close();
      bes.close();

      System.out.println("done exec: " + retVal);
      return retVal;
   }

   public static File findFile(String filename, String startDir) { 
      System.out.println("in FindFile");
      String files[];
      Vector agenda = new Vector();
      File root = new File (startDir);
      System.out.println("starting with " + startDir);
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
            else if (f.getName().equals(filename)) { // test for target
               System.out.println(f);     // print out the full path
               return(f);
            }
         }
      }
      System.out.println("Leaving FindFile(failed)");
      return null;
   } 

   public static void copyFile(String src, String dest) throws JellyException
   {
    try{
      File s = new File(src);
      File d = new File(dest);

      if (!d.getParentFile().exists()){
        d.getParentFile().mkdirs();
      }

      InputStream in = new FileInputStream(s);
      OutputStream out = new FileOutputStream(d);
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0 ) {
         out.write(buf,0,len);
      }
      in.close();
      out.close();
    } catch (Exception e) {
      throw new JellyException("File copy error: " + e);
    }
   }

   public static void rmDir(File path)
     throws IOException
   {
     File[] files = path.listFiles();

     for(int i=0; i<files.length; ++i)
     {
        if(files[i].isDirectory())
           rmDir(files[i]);

        files[i].delete();
     }
   }

   public static File createTempDir() throws JellyException
   {
     try { 
       File f = File.createTempFile("jelly", "temp");
       System.out.println("created tempfile:" + f.getPath());
       f.delete(); 
       f.mkdir();
       System.out.println("created tempdie:" + f.getPath()); 
       return f;
     } catch (Exception e) {
       throw new JellyException("Couldn't create temp dir: " + e);
     }
   }

   public static void cvsExport(String module, String tag, PrintWriter out, String tempDir) throws JellyException
   {
     ResourceBundle rb = ResourceBundle.getBundle("edu.asu.wmac.jelly.jelly");
     String cvsRoot = rb.getString("cvsRoot");
     String cvsPassword = rb.getString("cvsPassword");


      try {
         out.println("<div>Using Tag: " + tag + "</div>");
         out.println("<div>Setting up CVS Connection</div>");
         out.flush();

         GlobalOptions globalOptions = new GlobalOptions();
         globalOptions.setCVSRoot(cvsRoot);

         System.setProperty("user.dir", tempDir);  //Warning this may be ghetto code

         CVSRoot cvsroot = CVSRoot.parse(cvsRoot);
         PServerConnection c = new PServerConnection(cvsroot);

         //this is dumb
         Scrambler scram = StandardScrambler.getInstance();
         String scramPW = scram.scramble(cvsPassword);
         //System.out.println("JELLYBOT SCRAMPASS=" + scramPW);
         c.setEncodedPassword(scramPW);

         c.open();
         out.println("<div>Connected to CVS.  Checking out sources...</div>");
         out.flush();
         Client cvs = new Client(c, new StandardAdminHandler());
         cvs.setLocalPath(tempDir);
         cvs.getEventManager().addCVSListener(new BasicListener());

         ExportCommand cmd = new ExportCommand();
         cmd.setBuilder(null);
         cmd.setModules( new String[] { module } );
         cmd.setExportByRevision(tag);

         out.println("<!--Executing the equivalent of " + cmd.getCVSCommand() + "-->");
         cvs.executeCommand(cmd,globalOptions);
         out.println("<div>Done checking out</div>");
         out.flush();
         c.close();
     }catch (Exception e) {
        throw new JellyException("Error in CVS Export: " + e);
     }
   }
   public static void cvsCheckout(String module, String tag, PrintWriter out, String tempDir) throws JellyException
   {
     ResourceBundle rb = ResourceBundle.getBundle("edu.asu.wmac.jelly.jelly");
     String cvsRoot = rb.getString("cvsRoot");
     String cvsPassword = rb.getString("cvsPassword");


      try {
         out.println("<div>Using Tag: " + tag + "</div>");
         out.println("<div>Setting up CVS Connection</div>");
         out.flush();

         GlobalOptions globalOptions = new GlobalOptions();
         globalOptions.setCVSRoot(cvsRoot);

         System.setProperty("user.dir", tempDir);  //Warning this may be ghetto code

         CVSRoot cvsroot = CVSRoot.parse(cvsRoot);
         PServerConnection c = new PServerConnection(cvsroot);

         //this is dumb
         Scrambler scram = StandardScrambler.getInstance();
         String scramPW = scram.scramble(cvsPassword);
         //System.out.println("JELLYBOT SCRAMPASS=" + scramPW);
         c.setEncodedPassword(scramPW);

         c.open();
         out.println("<div>Connected to CVS.  Checking out sources...</div>");
         out.flush();
         Client cvs = new Client(c, new StandardAdminHandler());
         cvs.setLocalPath(tempDir);
         cvs.getEventManager().addCVSListener(new BasicListener());

         ExportCommand cmd = new ExportCommand();
         cmd.setBuilder(null);
         cmd.setModules( new String[] { module } );
         cmd.setExportByRevision(tag);

         out.println("<!--Executing the equivalent of " + cmd.getCVSCommand() + "-->");
         cvs.executeCommand(cmd,globalOptions);
         out.println("<div>Done checking out</div>");
         out.flush();
         c.close();
     }catch (Exception e) {
        throw new JellyException("Error in CVS Export: " + e);
     }
   }


   public static void sshCommand(String cmd, String host) throws JellyException
   {
    try{
     SshClient ssh = new SshClient();
     ssh.connect(host);
    
     PublicKeyAuthenticationClient pk = new PublicKeyAuthenticationClient();
     pk.setUsername("nsadmin");
     SshPrivateKeyFile file = SshPrivateKeyFile.parse(new File("/home/nobody/.ssh/id_dsa.pub"));
     SshPrivateKey key = file.toPrivateKey(null);
     
   
     // Set the key and authenticate
     pk.setKey(key);
     int result = ssh.authenticate(pk);
     if (result==AuthenticationProtocolState.FAILED)
       System.out.println ("The authentication failed");

     if (result==AuthenticationProtocolState.PARTIAL)
        System.out.println ("The authentication succeeded but another authentication is required");

     if (result==AuthenticationProtocolState.COMPLETE)
        System.out.println ("The authentication is complete");

     SessionChannelClient session = ssh.openSessionChannel ();
     session.executeCommand (cmd);

     BufferedReader input = new BufferedReader (new InputStreamReader (session.getInputStream ()));
     String line;
     while ((line = input.readLine ()) != null) {
        System.out.println (line);
     }
     input.close ();
     session.close ();
     ssh.disconnect ();
    }catch (IOException ioe){
     ioe.printStackTrace(System.out); 
     throw new JellyException("ssh command failed:" + ioe);
    }
  }
   
  public static boolean isAuthorized(String u)
  {
  	if (u.equals("xxx") || u.equals("yyy") || u.equals("zzz"))
  		return true;
  	else
  		return false;
  	
  }

}
