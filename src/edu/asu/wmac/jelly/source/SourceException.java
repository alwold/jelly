package edu.asu.wmac.jelly.source;

/**
 * @author alwold
 * 
 * @version $Revision: 1.1 $
 */
public class SourceException extends Exception {
   public SourceException() {
      super();
   }
   public SourceException(String message) {
      super(message);
   }
   public SourceException(String message, Throwable t) {
      super(message, t);
   }
}
