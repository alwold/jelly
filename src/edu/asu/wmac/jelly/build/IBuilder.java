package edu.asu.wmac.jelly.build;

import java.util.List;
import java.util.Map;

/**
 * @author alwold
 * 
 * @version $Revision: 1.2 $
 */
public interface IBuilder {
   public List getOptions();
   public String build(String module, String sourceDir, String destinationDir, Map options);
}
