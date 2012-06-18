package net.sf.testium.executor.soapui;

/**
 * 
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.*;
import org.testtoolinterfaces.utils.Trace;

import com.eviware.soapui.impl.support.http.HttpRequestTestStep;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlRunTestCaseTestStep;
import com.eviware.soapui.model.support.PropertiesMap;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestRunner.Status;

import junit.framework.TestCase;


public class SoapUiTester extends TestCase 
{
	@Test
	public void testcase_soapUiSingleTest()
	{
    	String tcId = "TestCase_1_1";
		Trace.println(Trace.EXEC, "execute( " + tcId + " )", true );

System.out.println( "Loading log4j config" );
File file  = new File("C:\\Temp\\sysout.log");
PrintStream oldOutStrem = System.out;
try
{
	PrintStream printStream = new PrintStream(new FileOutputStream(file));
	System.setOut(printStream);
}
catch (FileNotFoundException e1)
{
	// No action. Print to stdout.
	e1.printStackTrace();
}

		PropertyConfigurator.configure( "C:\\Users\\Arjan\\Projects\\FT_Testium\\FunctionTest\\config\\soapui-log4j.xml" );
//		PropertyConfigurator.configureAndWatch( "C:\\Temp\\log4j.xml" );
//		PropertyConfigurator.configure(properties)
//		DOMConfigurator.configureAndWatch( "C:\\Users\\Arjan\\Projects\\FT_Testium\\FunctionTest\\config\\soapui-log4j.xml", 5000 );
		try
		{
//			WsdlProject project = new WsdlProject( "C:\\Users\\Arjan\\Projects\\Tekelec\\TG_511_SPF_SOAP\\SPF_soapui-project" );
//			WsdlProject project = new WsdlProject( "C:\\Users\\Arjan\\Projects\\Tekelec\\Trial\\bu_tessie-soapui-project.xml" );
			WsdlProject project = new WsdlProjectPro( "C:\\Users\\Arjan\\Projects\\Tekelec\\Trial\\tessie-soapui-project" );
System.setOut(oldOutStrem);
System.out.println( "Project loaded" );
			TestSuite testSuite = project.getTestSuiteByName( "TestSuite_1" );
			WsdlTestCase testCase = (WsdlTestCase) testSuite.getTestCaseByName( "TestCase_1_1" );

			setEndPoint(testCase);

System.out.println( "Changed endpoints" );
			for( TestStep testStep : testCase.getTestStepList() )
			{
				if ( HttpRequestTestStep.class.isInstance( testStep ) )
				{
					HttpRequestTestStep httpRequestTS = (HttpRequestTestStep) testStep;
System.out.println( "EndPoint: " + httpRequestTS.getHttpRequest().getEndpoint() );
				}
			}
			TestRunner runner = testCase.run( new PropertiesMap(), false );
			assertEquals( Status.FINISHED, runner.getStatus() ); 
		}
		catch (Exception e)
		{
			fail( e.getLocalizedMessage() );
			e.printStackTrace();
		}
	}

	/**
	 * @param aTestCase
	 */
	private void setEndPoint(WsdlTestCase aTestCase)
	{
		List<TestStep> testSteps = aTestCase.getTestStepList();
		for( TestStep testStep : testSteps )
		{
System.out.println( "TestStep: " + testStep.getId() );
System.out.println( "TestStep type: " + testStep.getClass() );
			if ( HttpRequestTestStep.class.isInstance( testStep ) )
			{
				HttpRequestTestStep httpRequestTS = (HttpRequestTestStep) testStep;
System.out.println( "EndPoint: " + httpRequestTS.getHttpRequest().getEndpoint() );
				httpRequestTS.getHttpRequest().setEndpoint( "http://localhost:8080/SPF" );
			}
			else if ( WsdlRunTestCaseTestStep.class.isInstance(testStep) )
			{
				WsdlRunTestCaseTestStep wsdlRunTestCaseTS = (WsdlRunTestCaseTestStep) testStep;
				WsdlTestCase subTC = wsdlRunTestCaseTS.getTargetTestCase();
				setEndPoint( subTC );
			}
		}
	}
}

