package net.sf.testtoolinterfaces.testsuite;

import java.util.ArrayList;

import org.testtoolinterfaces.testsuite.TestCaseImpl;
import org.testtoolinterfaces.testsuite.TestStepSequence;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;


/**
 * A Wrapper for SoapUI Test Cases
 * 
 * @author Arjan Kranenburg
 *
 */
public class SoapUI_TestCase extends TestCaseImpl
{
	private WsdlTestCase mySoapUITestCase;

	public SoapUI_TestCase( WsdlTestCase aSoapUI_TestCase )
	{
		super( aSoapUI_TestCase.getName(),
		       aSoapUI_TestCase.getDescription(),
		       0,
		       new ArrayList<String>(),
		       new TestStepSequence(),
		       new TestStepSequence(),
		       new TestStepSequence() );
		
		mySoapUITestCase = aSoapUI_TestCase;
	}

	/**
	 * @return the soapUI_TestCase
	 */
	public WsdlTestCase getSoapUI_TC()
	{
		return mySoapUITestCase;
	}
}
