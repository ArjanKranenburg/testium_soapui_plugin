package org.testium.configuration;

import java.io.File;
import java.util.ArrayList;

import org.testtoolinterfaces.utils.GenericTagAndStringXmlHandler;
import org.testtoolinterfaces.utils.RunTimeData;
import org.testtoolinterfaces.utils.Trace;
import org.testtoolinterfaces.utils.XmlHandler;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;


public class SoapuiConfigurationXmlHandler extends XmlHandler
{
	private static final String START_ELEMENT = "SoapuiConfiguration";

	private static final String	CFG_TEST_EXECUTOR	= "testExecutor";
	private static final String	CFG_PROJECT			= "project";
	private static final String	CFG_SOAP_INTERFACE	= "soapInterface";

	private File myTempExecutor;
	private File myTempProject;
	private String myTempSoapInterface;

	private RunTimeData myRunTimeData;

	public SoapuiConfigurationXmlHandler(XMLReader anXmlReader, RunTimeData anRtData)
	{
	    super(anXmlReader, START_ELEMENT);
	    Trace.println(Trace.LEVEL.CONSTRUCTOR);
	    reset();

		myRunTimeData = anRtData;

	    ArrayList<XmlHandler> xmlHandlers = new ArrayList<XmlHandler>();
	    xmlHandlers.add(new GenericTagAndStringXmlHandler(anXmlReader, CFG_TEST_EXECUTOR));
	    xmlHandlers.add(new GenericTagAndStringXmlHandler(anXmlReader, CFG_PROJECT));
	    xmlHandlers.add(new GenericTagAndStringXmlHandler(anXmlReader, CFG_SOAP_INTERFACE));

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
	    Trace.println(Trace.LEVEL.UTIL, "handleReturnFromChildElement( " + 
	    	      aQualifiedName + " )", true);
	    
		if (aQualifiedName.equalsIgnoreCase(CFG_TEST_EXECUTOR))
    	{
			String executorFileName = myRunTimeData.substituteVars( aChildXmlHandler.getValue() );
			myTempExecutor = new File( executorFileName );
			aChildXmlHandler.reset();
    	}
		else if (aQualifiedName.equalsIgnoreCase(CFG_PROJECT))
    	{
			String projectName = myRunTimeData.substituteVars( aChildXmlHandler.getValue() );
			myTempProject = new File( projectName );
			aChildXmlHandler.reset();
    	}
		else if (aQualifiedName.equalsIgnoreCase(CFG_SOAP_INTERFACE))
    	{
			myTempSoapInterface = myRunTimeData.substituteVars( aChildXmlHandler.getValue() );
			aChildXmlHandler.reset();
    	}

   		aChildXmlHandler.reset();
	}
	
	public SoapuiConfiguration getConfiguration() throws ConfigurationException
	{
		return new SoapuiConfiguration( myTempExecutor,
										myTempProject,
										myTempSoapInterface );
	}
	
	public void reset()
	{
		myTempExecutor = null;
		myTempProject = null;
		myTempSoapInterface = "";
	}

}
