/*
 * Created on Sep 3, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.asu.wmac.jelly;

import java.util.*;
import java.io.*;

/**
 * @author bsamson
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JellyEnvironment {

	private String workingDir;
	
	public JellyEnvironment(String env) throws JellyException 
	{
	   ResourceBundle rb = ResourceBundle.getBundle("edu.asu.wmac.jelly.jelly");	
       this.workingDir = rb.getString("jellyHome") + "/" + env;
	   File lockfile = new File(this.workingDir + "/.locked");
	   if (lockfile.exists()) {
                try{
	   	  FileReader fr = new FileReader(lockfile);
	   	  BufferedReader bf = new BufferedReader(fr);
	   	  String lockedBy = bf.readLine();
	   	  throw new JellyException("Locked by: " + lockedBy);
                }catch (Exception e) {}
	   }
	   
	}
	
	public void lock(String username) throws IOException
	{
	  File lockfile = new File(this.workingDir + "/.locked");
      FileWriter fw = new FileWriter(lockfile);
	  fw.write(username);
	}

	public void unlock() throws IOException
	{
	  File lockfile = new File(this.workingDir + "/.locked");
	  if (lockfile.exists())
	  	 lockfile.delete();
	}

	public void updateSource(String module)
	{
		//File moduleDir = new File(this.workingdir + "/module");
	//	if (moduleDir.exists())
			//Util.cvsUpdate();
	  //      else
                   
	    	//Util.cvsCheckout();
	}
	
	     
		
}
