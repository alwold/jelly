/*
 * Created on Sep 2, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.asu.wmac.jelly.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.asu.wmac.jelly.Util;
import edu.asu.wmac.jelly.build.IBuilder;
import edu.asu.wmac.jelly.config.Configuration;
import edu.asu.wmac.jelly.config.ProjectType;
import edu.asu.wmac.jelly.config.ServiceFactory;
import edu.asu.wmac.jelly.deploy.IDeployer;
import edu.asu.wmac.jelly.options.BooleanOption;
import edu.asu.wmac.jelly.options.ListOption;
import edu.asu.wmac.jelly.options.Option;
import edu.asu.wmac.jelly.source.ISource;
import edu.asu.wmac.jelly.source.SourceException;
import edu.asu.wmac.webauth.WebAuthServlet;
import edu.asu.wmac.webauth.WebUser;


/**
 * @author bsamson
 *
 * The Main servlet that will provide all the functionality of Jelly
 *
 */
public class Jelly extends WebAuthServlet{
   public void doRequest(HttpServletRequest req, HttpServletResponse res, WebUser user) throws ServletException, IOException {
      res.setContentType("text/html");
      PrintWriter out = res.getWriter();
      Document doc = null;
      try {
         doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      } catch (ParserConfigurationException pce) {
         out.println("Parser Configuration Exception: "+pce.getMessage());
         out.println("<!--");
         pce.printStackTrace(out);
         out.println("-->");
      }

   	//Make sure this person is authorized
   	if (!Util.isAuthorized(user.getUserID()))
   	{  
   		res.setContentType("text/html");
           out.println("<title>JELlY</title>");
   		out.println("You are not authorized to use Jelly.  Please talk to Brian Samson if" +
   			"you believe this message is in error");
          return;
   	}
      
      if (req.getParameter("action") == null) {
         // main page, show list of modules
         Element projects = doc.createElement("projects");
         List projectList = Configuration.getInstance().getJellyConfiguration().getProject();
         for (Iterator i = projectList.iterator(); i.hasNext(); ) {
            ProjectType project = (ProjectType)i.next();
            Element projectElement = doc.createElement("project");
            projectElement.setAttribute("name", project.getName());
            projects.appendChild(projectElement);
         }
         doc.appendChild(projects);
      } else if (req.getParameter("action").equals("selectproject")) {
         String project = req.getParameter("project");
         Element optionsPage = doc.createElement("optionsPage");
         Element projectElement = doc.createElement("project");
         projectElement.setAttribute("name", project);
         optionsPage.appendChild(projectElement);

         ServiceFactory serviceFactory = ServiceFactory.getInstance();
         
         ISource source = serviceFactory.getSource(project);
         Element sourceElement = doc.createElement("source");
         sourceElement.setAttribute("class", source.getClass().getName());
         sourceElement.appendChild(getOptions(source, project, doc));
         optionsPage.appendChild(sourceElement);
         
         
         IBuilder builder = serviceFactory.getBuilder(project);
         Element builderElement = doc.createElement("builder");
         builderElement.setAttribute("class", builder.getClass().getName());
         builderElement.appendChild(getOptions(builder, project, doc));
         optionsPage.appendChild(builderElement);
         
         IDeployer deployer = serviceFactory.getDeployer(project);
         Element deployerElement = doc.createElement("deployer");
         deployerElement.setAttribute("class", deployer.getClass().getName());
         deployerElement.appendChild(getOptions(deployer, project, doc));
         optionsPage.appendChild(deployerElement);
         
         doc.appendChild(optionsPage);
      } else if (req.getParameter("action").equals("go")) {
         String project = req.getParameter("project");
         ServiceFactory serviceFactory = ServiceFactory.getInstance();

         String sourceDir = "/tmp/jelly."+project+"/src";
         String destDir = "/tmp/jelly."+project+"/dest";

         try {
            Element go = doc.createElement("go");

            ISource source = serviceFactory.getSource(project);
            Map options = getOptions("source", req);
            source.retrieveSource(project, sourceDir, options);
            
            IBuilder builder = serviceFactory.getBuilder(project);
            options = getOptions("builder", req);
            String buildOutput = builder.build(project, sourceDir+File.separator+project, destDir, options);
            Element build = doc.createElement("build");
            build.appendChild(doc.createTextNode(buildOutput));
            go.appendChild(build);
            
            IDeployer deployer = serviceFactory.getDeployer(project);
            options = getOptions("deployer", req);
            deployer.deploy(destDir, IDeployer.ENV_QA);
            
            doc.appendChild(go);
         } catch (SourceException e) {
            Element error = doc.createElement("error");
            error.setAttribute("message", e.getMessage());
            doc.appendChild(error);
         }
      }
   	
      try {
         URL url = this.getClass().getResource("/edu/asu/wmac/jelly/servlets/jelly.xsl");
         StreamSource stylesheet = new StreamSource(url.openConnection().getInputStream());
         stylesheet.setSystemId(url.toString());
         Transformer trans = TransformerFactory.newInstance().newTransformer(stylesheet);
         trans.setParameter("requestURL", req.getRequestURI());
         trans.transform(new DOMSource(doc), new StreamResult(out));
      } catch (TransformerException tce) {
         out.println("Transformer exception: "+tce.getMessage());
         out.println("<!--");
         tce.printStackTrace(out);
         out.println("-->");
      }
   }

   private Element getOptions(Object service, String project, Document doc) {
      List options;
      if (service instanceof ISource) {
         options = ((ISource)service).listOptions(project);
      } else if (service instanceof IBuilder) {
         options = ((IBuilder)service).getOptions();
      } else if (service instanceof IDeployer) {
         options = Collections.EMPTY_LIST;
      } else {
         return null;
      }
      Element optionsElement = doc.createElement("options");
      for (Iterator i = options.iterator(); i.hasNext(); ) {
         Option o = (Option)i.next();
         Element option = doc.createElement("option");;
         if (o instanceof BooleanOption) {
            option.setAttribute("type", "boolean");
         } else if (o instanceof ListOption) {
            option.setAttribute("type", "list");
            for (Iterator j = ((ListOption)o).getChoices().iterator(); j.hasNext(); ) {
               Element choice = doc.createElement("choice");
               choice.setAttribute("value", (String)j.next());
               option.appendChild(choice);
            }
         }
         option.setAttribute("name", o.getName());
         option.setAttribute("label", o.getLabel());
         optionsElement.appendChild(option);
      }
      return optionsElement;
   }
   
   private Map getOptions(String prefix, HttpServletRequest req) {
      Map options = new HashMap();
      for (Enumeration e = req.getParameterNames(); e.hasMoreElements(); ) {
         String key = (String)e.nextElement();
         if (key.startsWith(prefix+"-")) {
            String value = req.getParameter(key);
            key = key.substring(key.indexOf('-')+1);
            options.put(key, value);
         }
      }
      return options;
   }
}
