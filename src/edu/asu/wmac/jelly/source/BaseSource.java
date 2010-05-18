package edu.asu.wmac.jelly.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.asu.wmac.jelly.options.BooleanOption;

/**
 * @author alwold
 * 
 * @version $Revision: 1.3 $
 */
public abstract class BaseSource implements ISource {
   protected List options;
   
   public BaseSource() {
      options = new ArrayList();
      options.add(new BooleanOption("clean", "Get fresh copy of source"));
   }
   
   public List listOptions(String module) {
      return options;
   }

   public abstract void retrieveSource(String module, String destinationDir, Map options) throws SourceException;

}
