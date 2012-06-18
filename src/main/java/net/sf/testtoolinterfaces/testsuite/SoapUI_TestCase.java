package net.sf.testtoolinterfaces.testsuite;

import java.util.ArrayList;
import java.util.Hashtable;

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
		       new TestStepSequence(),
		       new Hashtable<String, String>(),
		       new Hashtable<String, String>() );
		
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
