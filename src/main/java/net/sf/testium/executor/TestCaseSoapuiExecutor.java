/**
 * 
 */
package net.sf.testium.executor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import net.sf.testium.configuration.SoapuiConfiguration;
import net.sf.testtoolinterfaces.testsuite.SoapUI_TestCase;

import org.testtoolinterfaces.testresult.TestCaseResult;
import org.testtoolinterfaces.testresult.TestCaseResultLink;
import org.testtoolinterfaces.testresult.TestResult.VERDICT;
import org.testtoolinterfaces.testresult.impl.TestCaseResultImpl;
import org.testtoolinterfaces.testresult.impl.TestCaseResultLinkImpl;
import org.testtoolinterfaces.testresultinterface.TestCaseResultWriter;
import org.testtoolinterfaces.testsuite.TestCaseLink;
import org.testtoolinterfaces.testsuite.TestSuiteException;
import org.testtoolinterfaces.utils.RunTimeData;
import org.testtoolinterfaces.utils.Trace;
import org.testtoolinterfaces.utils.Trace.LEVEL;

import com.eviware.soapui.impl.support.http.HttpRequestTestStep;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlRunTestCaseTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.PropertiesMap;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;

/**
 * @author Arjan Kranenburg
 *
 */
public class TestCaseSoapuiExecutor implements TestCaseExecutor
{
	public static final String TYPE = "soapui";

	private SoapuiConfiguration myConfiguration;
	private TestCaseResultWriter myTestCaseResultWriter;
	
	private WsdlProject soapUiProject = null;
	private PrintStream myNormalOutStream = System.out;
	private PrintStream myNormalErrStream = System.err;
	
	/**
	 * @param aSoapuiConfiguration 
	 * @param aTestStepExecutors
	 * @param aTestRunResultWriter 
	 */
	public TestCaseSoapuiExecutor( SoapuiConfiguration aSoapuiConfiguration,
	                               TestCaseResultWriter aTestCaseResultWriter )
	{
		Trace.println(Trace.CONSTRUCTOR);

		myConfiguration = aSoapuiConfiguration;
		myTestCaseResultWriter = aTestCaseResultWriter;
	}

	public TestCaseResultLink execute(TestCaseLink aTestCaseLink, File aLogDir, RunTimeData anRTData)
						      throws TestCaseLinkExecutionException
	{
    	try {
			String tcId = aTestCaseLink.getId();
			Trace.println(Trace.EXEC, "execute( "
							+ tcId + ", "
				            + aLogDir.getPath() + ", "
				            + anRTData.size() + " Variables )", true );

System.out.println( "Executing SoapUI TC: " + tcId );
			if ( !aLogDir.isDirectory() )
			{
				FileNotFoundException exc = new FileNotFoundException("Directory does not exist: " + aLogDir.getAbsolutePath());
				throw new IOError( exc );
			}

			File caseLogDir = new File( aLogDir, tcId );
			caseLogDir.mkdir();
			File resultFile = new File( caseLogDir, tcId + ".xml" );

			// StdOut is redirected because SoapUI outputs lots of garbage
			File logFile  = new File( caseLogDir, "soapui.log" );
			redirectStdOut(logFile);
			
			SoapUI_TestCase tc = getTestCase( aTestCaseLink, caseLogDir );

			TestCaseResult result = new TestCaseResultImpl( tc );
			myTestCaseResultWriter.write( result, resultFile );

			// create empty properties and run synchronously
			PropertiesMap soapUiProperties = new PropertiesMap();
			
			TestRunner runner = tc.getSoapUI_TC().run( soapUiProperties, false ); // boolean is for async running
			
			resetStdOut();
			if ( runner.getStatus().equals( Status.FAILED ) )
			{
				result.setResult( VERDICT.FAILED );
				runner.getReason();
				result.addComment("Test Case Failed: " + runner.getReason());
			}
			else if ( runner.getStatus().equals( Status.FINISHED ) )
			{
				result.setResult( VERDICT.PASSED );			
			}
			else
			{
				result.setResult( VERDICT.ERROR );
				runner.getReason();
				result.addComment("Test Case Failed: " + runner.getReason());
			}

			if ( logFile.length() > 0 )
			{
				result.addTestLog("soapui", logFile.getPath());
			}

			return new TestCaseResultLinkImpl( aTestCaseLink,
			                               result.getResult(),
			                               resultFile );
		} catch (Throwable t) {
			resetStdOut();
			t.printStackTrace();

			return new TestCaseResultLinkImpl( aTestCaseLink,
                    VERDICT.ERROR,
                    null );
		}
	}

	/**
	 * 
	 */
	private void resetStdOut()
	{
		if( ! System.out.equals( myNormalOutStream ) )
		{
			System.out.close();
			System.setOut( myNormalOutStream );
		}

		if( ! System.err.equals( myNormalErrStream ) )
		{
			System.err.close();
			System.setErr( myNormalErrStream );
		}
	}

	/**
	 * @param aLogFile
	 */
	private void redirectStdOut(File aLogFile)
	{
		System.out.flush();
		System.err.flush();
		try
		{
			PrintStream printStream = new PrintStream(new FileOutputStream(aLogFile));
			System.setOut(printStream);
			System.setErr(printStream);
		}
		catch (FileNotFoundException e1)
		{
			// No action. Print to stdout.
			Trace.print(LEVEL.EXEC, e1);
		}
	}

	private SoapUI_TestCase getTestCase( TestCaseLink aTestCaseLink,
	                                    File aLogDir )
						   throws TestCaseLinkExecutionException
	{
    	String tcId = aTestCaseLink.getId();
		Trace.println(Trace.EXEC, "getTestCase( " + tcId + " )", true );

		WsdlProject project;
		try
		{
			project = getProject( aLogDir );
		}
		catch (Throwable t)
		{
			resetStdOut();
			throw new TestCaseLinkExecutionException( aTestCaseLink, t );
		}

		TestSuite testSuite = project.getTestSuiteByName( aTestCaseLink.getLink().getName() );
		if ( testSuite == null ) {
			throw new TestCaseLinkExecutionException( aTestCaseLink, "SoapUI TestSuite not found: " + aTestCaseLink.getLink().getName() );
		}
myNormalOutStream.println( "TestSuite " + testSuite.getName() + " loaded with " + testSuite.getTestCaseCount() + " TestCases." );
Iterator<TestCase> testCasesItr = testSuite.getTestCaseList().iterator();
while ( testCasesItr.hasNext() ) {
	myNormalOutStream.println( " | " + testCasesItr.next().getName() );
}

		WsdlTestCase testCase = (WsdlTestCase) testSuite.getTestCaseByName( tcId );
		if ( testCase == null ) {
			throw new TestCaseLinkExecutionException( aTestCaseLink, "SoapUI TestCase not found: " + tcId );
		}
myNormalOutStream.println( "Check 3: " + testCase.getName() );

		setEndPoints(testCase);
myNormalOutStream.println( "Check 4" );

		SoapUI_TestCase soapUI_TC = new SoapUI_TestCase( testCase );
myNormalOutStream.println( "Check 5" );

		return soapUI_TC;
	}

	/**
	 * @param aTestCase
	 */
	private void setEndPoints(WsdlTestCase aTestCase)
	{
myNormalOutStream.println("Setting end-points for " + aTestCase.getName());
		List<TestStep> testSteps = aTestCase.getTestStepList();
		for( TestStep testStep : testSteps )
		{
myNormalOutStream.println("Setting end-points for " + testStep.getName());
			if ( HttpRequestTestStep.class.isInstance( testStep ) )
			{
				HttpRequestTestStep httpRequestTS = (HttpRequestTestStep) testStep;
				httpRequestTS.getHttpRequest().setEndpoint( myConfiguration.getSoapInterface() );
			}
			else if ( WsdlRunTestCaseTestStep.class.isInstance(testStep) )
			{
				WsdlRunTestCaseTestStep wsdlRunTestCaseTS = (WsdlRunTestCaseTestStep) testStep;
				WsdlTestCase subTC = wsdlRunTestCaseTS.getTargetTestCase();
				setEndPoints( subTC );
			}
			// More types?
		}
	}

	private WsdlProject getProject( File aLogDir ) throws TestSuiteException
	{
		if ( soapUiProject == null )
		{
			try
			{
//				soapUiProject = new WsdlProjectPro( myConfiguration.getProject().getAbsolutePath() );
				soapUiProject = new WsdlProject( myConfiguration.getProject().getAbsolutePath() );
//				soapUiProject.setResourceRoot(aLogDir.getAbsolutePath());
			}
			catch (Exception e)
			{
				throw new TestSuiteException( "SoapUI project not found: " + myConfiguration.getProject().getName(), e );
			}
		}

		soapUiProject.setResourceRoot( aLogDir.getAbsolutePath() );

		return soapUiProject;
	}


	public String getType()
	{
		return TYPE;
	}
}
