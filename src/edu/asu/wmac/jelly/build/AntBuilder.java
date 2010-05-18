package edu.asu.wmac.jelly.build;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.asu.wmac.jelly.Util;

/**
 * @author alwold
 * 
 * @version $Revision: 1.2 $
 */
public class AntBuilder implements IBuilder {
   private String antPath;

   public AntBuilder() {
      // TODO load from config
      antPath = "/usr/local/ant/bin/ant";
   }
   public List getOptions() {
      return new ArrayList();
   }

   public String build(String module, String sourceDir, String destinationDir, Map options) {
      StringWriter output = new StringWriter();
      PrintWriter out = new PrintWriter(output);
      
      // TODO load from config
      String target = "dist";
      String buildFile = "build.xml";
      
      // TODO deal with non-zero return/exception
      try {
         Util.execute(new String[]{antPath, "-f", sourceDir+File.separator+buildFile, target}, null, out);
      } catch (IOException e) {
         e.printStackTrace();
      }
      
      // TODO copy build artifacts to the destination directory
      
      return output.toString();
   }

}
