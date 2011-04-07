/**
 * 
 */
package org.testium.executor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.util.ArrayList;
import java.util.Hashtable;

import org.testium.configuration.SoapuiConfiguration;
import org.testtoolinterfaces.testresult.TestCaseResult;
import org.testtoolinterfaces.testresult.TestCaseResultLink;
import org.testtoolinterfaces.testresult.TestResult.VERDICT;
import org.testtoolinterfaces.testresultinterface.TestCaseResultWriter;
import org.testtoolinterfaces.testsuite.Parameter;
import org.testtoolinterfaces.testsuite.ParameterArrayList;
import org.testtoolinterfaces.testsuite.TestCase;
import org.testtoolinterfaces.testsuite.TestCaseImpl;
import org.testtoolinterfaces.testsuite.TestCaseLink;
import org.testtoolinterfaces.testsuite.TestStepArrayList;
import org.testtoolinterfaces.utils.Trace;

/**
 * @author Arjan Kranenburg
 *
 */
public class TestCaseSoapuiExecutor implements TestCaseExecutor
{
	public static final String TYPE = "soapui";

	private SoapuiConfiguration myConfiguration;
	private TestCaseResultWriter myTestCaseResultWriter;
	
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

	public TestCaseResultLink execute(TestCaseLink aTestCaseLink, File aLogDir)
	{
    	String tcId = aTestCaseLink.getId();
		Trace.println(Trace.EXEC, "execute( "
						+ tcId + ", "
			            + aLogDir.getPath() + " )", true );

		if ( !aLogDir.isDirectory() )
		{
			FileNotFoundException exc = new FileNotFoundException("Directory does not exist: " + aLogDir.getAbsolutePath());
			throw new IOError( exc );
		}

    	File executable = myConfiguration.getExecutor();

		String description = ""; // TODO try to get a description from the shell script
		ArrayList<String> requirements = new ArrayList<String>(); // TODO try to get a requirements from the shell script

		File caseLogDir = new File( aLogDir, tcId );
		caseLogDir.mkdir();
		File resultFile = new File( caseLogDir, tcId + ".xml" );


		TestCase testCase = new TestCaseImpl( tcId,
		                                      new Hashtable<String, String>(),
		                                      description,
		                                      requirements,
		                                      new TestStepArrayList(),
		                                      new TestStepArrayList(),
		                                      new TestStepArrayList(),
		                                      new Hashtable<String, String>());
		TestCaseResult result = new TestCaseResult( testCase );
		File logFile = new File( caseLogDir, tcId + "_run.log" );

		myTestCaseResultWriter.write( result, resultFile );

    	ParameterArrayList params = new ParameterArrayList();

    	// Specifies which TC to run
    	Parameter paramSoapIf = new Parameter( "-e", myConfiguration.getSoapInterface() );
    	paramSoapIf.setIndex(0);
		params.add(paramSoapIf);

		Parameter paramTc = new Parameter( "-c", tcId );
		paramTc.setIndex(1);
		params.add(paramTc);

    	Parameter param_r = new Parameter( "-r", "" );
    	param_r.setIndex(2);
		params.add(param_r);
    	
    	Parameter param_A = new Parameter( "-A", "" );
    	param_A.setIndex(3);
		params.add(param_A);
    	
    	Parameter param_j = new Parameter( "-j", "" );
    	param_j.setIndex(4);
		params.add(param_j);
    	
    	Parameter paramLogDir = new Parameter( "-f", caseLogDir.getAbsolutePath() );
    	paramLogDir.setIndex(5);
		params.add(paramLogDir);
    	
    	Parameter paramSuite = new Parameter( "-s", aTestCaseLink.getLink().getAbsolutePath() );
    	paramSuite.setIndex(6);
		params.add(paramSuite);

    	Parameter paramProject = new Parameter( myConfiguration.getProject().getAbsolutePath(), "" );
    	paramProject.setIndex(7);
		params.add(paramProject);

       	try
		{
			result.setResult( ShellScript.execute(executable, params, logFile) );
		}
		catch (FileNotFoundException e)
		{
			result.addComment( "Execution Failed: " + e.getMessage() );
			result.setResult( VERDICT.ERROR );
		}
		result.addTestLog("log", logFile.getPath());

		return new TestCaseResultLink( aTestCaseLink,
		                               result.getResult(),
		                               resultFile );
	}

	@Override
	public String getType()
	{
		return TYPE;
	}
}
