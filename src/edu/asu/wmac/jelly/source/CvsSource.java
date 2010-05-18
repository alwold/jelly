package edu.asu.wmac.jelly.source;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.status.StatusInformation;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.connection.Scrambler;
import org.netbeans.lib.cvsclient.connection.StandardScrambler;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;

import edu.asu.wmac.jelly.options.BooleanOption;
import edu.asu.wmac.jelly.options.Option;

/**
 * @author alwold
 * 
 * @version $Revision: 1.4 $
 */
public class CvsSource extends BaseSource {
   private PServerConnection cvsconn;
   private GlobalOptions globalOptions;
   
   public CvsSource() {
      super();
      options.add(new BooleanOption("p-patches", "Include -p patches"));
      options.add(new BooleanOption("q-patches", "Include -q patches"));
   }
   
   public List listOptions(String module) {
      Option tag = new Option("tag", "CVS Version tag");
      List myOptions = new ArrayList();
      myOptions.addAll(options);
      myOptions.add(tag);
      return myOptions;
   }
   
   public void retrieveSource(String module, String destinationDir, Map options) throws SourceException {
      ResourceBundle rb = ResourceBundle.getBundle("edu.asu.wmac.jelly.jelly");
      String cvsRoot = rb.getString("cvsRoot");
      String cvsPassword = rb.getString("cvsPassword");
      
      globalOptions = new GlobalOptions();
      globalOptions.setCVSRoot(cvsRoot);

      CVSRoot cvsroot = CVSRoot.parse(cvsRoot);
      cvsconn = new PServerConnection(cvsroot);

      //this is dumb
      Scrambler scram = StandardScrambler.getInstance();
      String scramPW = scram.scramble(cvsPassword);
      cvsconn.setEncodedPassword(scramPW);
      
      StandardAdminHandler handler = new StandardAdminHandler();
      Client client = new Client(cvsconn, handler);
      client.setLocalPath(destinationDir);
      boolean checkout = true;
      try {
         if (handler.getAllFiles(new File(destinationDir)).size() > 0) {
            checkout = false;
         }
      } catch (IOException e1) {}
      
      Command cmd;
      if (checkout) {
         CheckoutCommand ccmd = new CheckoutCommand();
         ccmd.setCheckoutByRevision((String)options.get("tag"));
         // TODO get module name from config
         ccmd.setModule("uPortal");
         cmd = ccmd;
      } else {
         UpdateCommand ucmd = new UpdateCommand();
         ucmd.setUpdateByRevision((String)options.get("tag"));
         cmd = ucmd;
      }
      try {
         client.executeCommand(cmd, globalOptions);
      } catch (Exception e) {
         throw new SourceException("Unable to check out source", e);
      }
   }
   
   public static class StatusListener extends CVSAdapter {
      List responses;
      public StatusListener() {
         responses = new ArrayList();
      }
      
      public void fileInfoGenerated(FileInfoEvent e) {
         System.out.println("response: "+e.getInfoContainer());
         if (e.getInfoContainer() instanceof StatusInformation) {
            responses.add(e.getInfoContainer());
         }
      }
      
      public List getResponses() {
         return responses;
      }
   }

}
