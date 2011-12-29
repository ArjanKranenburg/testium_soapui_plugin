package org.testtoolinterfaces.testsuite;

import java.util.ArrayList;
import java.util.Hashtable;

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
		       new Hashtable<String, String>(),
		       aSoapUI_TestCase.getDescription(),
		       new ArrayList<String>(),
		       new TestStepArrayList(),
		       new TestStepArrayList(),
		       new TestStepArrayList(),
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
