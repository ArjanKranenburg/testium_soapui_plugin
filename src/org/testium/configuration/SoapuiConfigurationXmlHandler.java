package org.testium.configuration;

import java.io.File;
import java.util.ArrayList;

import org.testium.Testium;
import org.testtoolinterfaces.utils.GenericTagAndStringXmlHandler;
import org.testtoolinterfaces.utils.RunTimeData;
import org.testtoolinterfaces.utils.Trace;
import org.testtoolinterfaces.utils.XmlHandler;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;


public class SoapuiConfigurationXmlHandler extends XmlHandler
{
	private static final String START_ELEMENT = "SoapuiConfiguration";

	private static final String	CFG_PROJECT		    = "project";
	private static final String	CFG_SOAP_INTERFACE	= "soapInterface";
	private static final String CFG_SOAPUI_LIBS_DIR = "soapUILibsDir";
	private static final String CFG_LOG4J_FILE      = "log4jFile";

	private File myProject;
	private String mySoapInterface;
	private File mySoapUILibsDir;
	private File myLog4jFile;

	private RunTimeData myRunTimeData;

	public SoapuiConfigurationXmlHandler(XMLReader anXmlReader, RunTimeData anRtData)
	{
	    super(anXmlReader, START_ELEMENT);
	    Trace.println(Trace.CONSTRUCTOR);
		myRunTimeData = anRtData;

		reset();

	    ArrayList<XmlHandler> xmlHandlers = new ArrayList<XmlHandler>();
	    xmlHandlers.add(new GenericTagAndStringXmlHandler(anXmlReader, CFG_PROJECT));
	    xmlHandlers.add(new GenericTagAndStringXmlHandler(anXmlReader, CFG_SOAP_INTERFACE));
	    xmlHandlers.add(new GenericTagAndStringXmlHandler(anXmlReader, CFG_SOAPUI_LIBS_DIR));
	    xmlHandlers.add(new GenericTagAndStringXmlHandler(anXmlReader, CFG_LOG4J_FILE));

	    for (XmlHandler handler : xmlHandlers)
	    {
			this.addStartElementHandler(handler.getStartElement(), handler);
			handler.addEndElementHandler(handler.getStartElement(), this);
	    }
	}

	@Override
	public void handleStartElement(String aQualifiedName)
	{
		// nop
	}

	@Override
	public void handleCharacters(String aValue)
	{
		// nop
	}

	@Override
	public void handleEndElement(String aQualifiedName)
	{
		// nop
	}

	@Override
	public void processElementAttributes(String aQualifiedName, Attributes att)
	{
		// nop
	}

	@Override
	public void handleGoToChildElement(String aQualifiedName)
	{
		// nop
	}

	@Override
	public void handleReturnFromChildElement(String aQualifiedName, XmlHandler aChildXmlHandler)
	{
	    Trace.println(Trace.UTIL, "handleReturnFromChildElement( " + 
	    	      aQualifiedName + " )", true);
	    
		if (aQualifiedName.equalsIgnoreCase(CFG_PROJECT))
    	{
			String projectName = myRunTimeData.substituteVars( aChildXmlHandler.getValue() );
			myProject = new File( projectName );
			aChildXmlHandler.reset();
    	}
		else if (aQualifiedName.equalsIgnoreCase(CFG_SOAP_INTERFACE))
    	{
			mySoapInterface = myRunTimeData.substituteVars( aChildXmlHandler.getValue() );
			aChildXmlHandler.reset();
    	}
		else if (aQualifiedName.equalsIgnoreCase(CFG_SOAPUI_LIBS_DIR))
    	{
			String SoapUILibsDirName = aChildXmlHandler.getValue();
			SoapUILibsDirName = myRunTimeData.substituteVars(SoapUILibsDirName);
			mySoapUILibsDir = new File( SoapUILibsDirName );

			aChildXmlHandler.reset();
    	}
		else if (aQualifiedName.equalsIgnoreCase(CFG_LOG4J_FILE))
    	{
			String log4jFileName = aChildXmlHandler.getValue();
			log4jFileName = myRunTimeData.substituteVars(log4jFileName);
			myLog4jFile = new File( log4jFileName );

			aChildXmlHandler.reset();
    	}

   		aChildXmlHandler.reset();
	}
	
	public SoapuiConfiguration getConfiguration() throws ConfigurationException
	{
		return new SoapuiConfiguration( myProject,
										mySoapInterface,
										mySoapUILibsDir,
										myLog4jFile );
	}
	
	public void reset()
	{
		myProject = null;
		mySoapInterface = "";
		mySoapUILibsDir = getDefaultSoapUILibsDir( myRunTimeData );
		myLog4jFile = getDefaultLog4jFile( myRunTimeData );;
	}

	static public File getDefaultSoapUILibsDir( RunTimeData aRTData)
	{
		File pluginsDir = aRTData.getValueAsFile(Testium.PLUGINSDIR);
		File soapUILibsDir = new File( pluginsDir, "SoapUILibs" );
		
		return soapUILibsDir;
	}

	static public File getDefaultLog4jFile( RunTimeData aRTData)
	{
		File configDir = aRTData.getValueAsFile(Testium.CONFIGDIR);
		File log4jFile = new File( configDir, "log4j.xml" );
		
		return log4jFile;
	}
}
