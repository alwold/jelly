package edu.asu.wmac.jelly.options;

/**
 * @author alwold
 * 
 * @version $Revision: 1.2 $
 */
public class Option {
   private String name;
   private String label;
   
   public Option(String name, String label) {
      this.name = name;
      this.label = label;
   }
   public String getLabel() {
      return label;
   }
   public String getName() {
      return name;
   }
}
