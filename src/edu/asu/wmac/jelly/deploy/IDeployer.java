package edu.asu.wmac.jelly.deploy;

import java.util.List;

/**
 * @author alwold
 * 
 * @version $Revision: 1.2 $
 */
public interface IDeployer {
   public static final String ENV_DEV = "dev";
   public static final String ENV_QA = "qa";
   public static final String ENV_PROD = "prod";
   
   public List deploy(String sourcePath, String environment);
}
