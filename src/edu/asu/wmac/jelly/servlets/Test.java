/*
 * Created on Jan 27, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.asu.wmac.jelly.servlets;
import java.io.*;
import java.util.*;
import edu.asu.wmac.webauth.*;
import edu.asu.wmac.jelly.*;
import edu.asu.wmac.jelly.config.*;
import edu.asu.wmac.jelly.config.ProjectType;
import edu.asu.wmac.jelly.config.ProjectType.DeployConfigurationType;
import edu.asu.wmac.jelly.config.ParameterType;
import edu.asu.wmac.jelly.config.DriverConfigurationType;
/**
 * @author bsamson
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Test extends WebAuthServlet {
	  
	public void doRequest(javax.servlet.http.HttpServletRequest req,
            javax.servlet.http.HttpServletResponse res,
            WebUser user)
     throws javax.servlet.ServletException,
	        java.io.IOException
     {
		//Test servlet
		Configuration c = Configuration.getInstance();
		JellyConfiguration conf = c.getJellyConfiguration();
		PrintWriter out = res.getWriter();
		res.setContentType("text/html");
		out.println("<title>Jelly Test Servlet</title>");
		out.println("welcome to the test servlet<br><br>");
		
		out.println("from the xml file, here is some data:<br>");
		
		
		List projList = conf.getProject();
		ProjectType p = (ProjectType) projList.get(0);
		DeployConfigurationType dep = p.getDeployConfiguration();
		out.println("deploy driver class name is: " + dep.getDriverClassName());
		DriverConfigurationType depdrv = dep.getDriverConfiguration();
		
		int parmCount = depdrv.getParameter().size();
		out.println("<br>driver parameters (" + Integer.toString(parmCount) + ")are: ");
		
		List parmlist = depdrv.getParameter();
		for (int i=0; i<parmCount; i++){
			ParameterType parm = (ParameterType) parmlist.get(i); 
			out.println(parm.getName() + " => " + parm.getValue());
		    out.println("<br>");
		}
		
		//{
		//	DriverConfigurationType.ParameterType parm = (DriverConfigurationType.ParameterType) i.next();
			//out.println(parm.getName() + " => " + p.getValue() + "<br>");
		//}
		
		
		
		
	}
	
	
}
