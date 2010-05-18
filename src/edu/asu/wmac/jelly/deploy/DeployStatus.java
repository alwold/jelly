package edu.asu.wmac.jelly.deploy;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author alwold
 * 
 * @version $Revision: 1.1 $
 */
public class DeployStatus {
   private StringWriter output;
   private boolean success;
   private PrintWriter writer;
   private String host;
   
   public DeployStatus(String host) {
      this.host = host;
      output = new StringWriter();
      writer = new PrintWriter(output);
   }
   
   public PrintWriter getWriter() {
      return writer;
   }
   public boolean isSuccess() {
      return success;
   }
   public void setSuccess(boolean success) {
      this.success = success;
   }
   public String getHost() {
      return host;
   }
   public String getOutput() {
      return output.toString();
   }
}
