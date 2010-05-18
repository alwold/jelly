package edu.asu.wmac.jelly;

import java.io.*;
import java.util.*;


public class CarManager {
  
   private ArrayList carFiles;
   private String tempDir = "/tmp/jelly/";

   public CarManager( String tempDir) throws JellyException {
     this.tempDir = tempDir;
     updateCarList();
   }


   protected void updateCarList() throws JellyException
   {
     StringWriter sw = new StringWriter();
     File f = new File(this.tempDir + "/uportal-cars");
     if (!f.exists()) {
       Util.cvsExport("uportal-cars", "HEAD", new PrintWriter(sw), this.tempDir);
     }
     File[] files = f.listFiles();
     this.carFiles = new ArrayList();
     for (int i = 0; i<files.length ; i++) {
      carFiles.add(files[i]);
     }
   }

   public ArrayList getCarFiles()
     { return carFiles; } 


}
