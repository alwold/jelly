package edu.asu.wmac.jelly.options;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alwold
 * 
 * @version $Revision: 1.2 $
 */
public class ListOption extends Option {
   private List choices;
   
   public ListOption(String name, String label) {
      super(name, label);
   }
   
   public ListOption(String name, String label, List choices) {
      super(name, label);
      this.choices = choices;
   }

   public List getChoices() {
      return choices;
   }

   public void setChoices(List choices) {
      this.choices = choices;
   }
   
   public void addChoice(String choice) {
      if (choices == null) {
         choices = new ArrayList();
      }
      choices.add(choice);
   }
   
   
}
