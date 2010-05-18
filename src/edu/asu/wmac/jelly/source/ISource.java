package edu.asu.wmac.jelly.source;

import java.util.List;
import java.util.Map;

/**
 * @author alwold
 * 
 * @version $Revision: 1.3 $
 */
public interface ISource {
   /**
    * List the options that are available for source retrieval on the specified module.  For example,
    * should an overlay module in CVS be used to add production configuration to the source?
    * 
    * @param module
    * @return
    */
   public List listOptions(String module);
   
   /**
    * Pull the source from its source and put it in the specified directory, theoretically so it
    * can be built by the configured builder.
    * 
    * @param module
    * @param destinationDir Directory to deposit the source into
    * @param options a Map of options for the retrieval (corresponding to listOptions())
    */
   public void retrieveSource(String module, String destinationDir, Map options) throws SourceException;
}
