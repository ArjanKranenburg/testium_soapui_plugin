package org.testium.configuration;
/**
 * 
 */

import java.io.File;

import org.testtoolinterfaces.utils.Trace;

/**
 * @author Arjan Kranenburg
 *
 */
public class SoapuiConfiguration
{
	private File myExecutor;
	private File myProject;
	private String mySoapInterface;

	/**
	 * @param aSoapuiExecutable
	 * @param aSoapuiProject
	 */
	public SoapuiConfiguration( File aSoapuiExecutable,
							    File aSoapuiProject,
							    String aSoapInterface )
	{
	    Trace.println(Trace.CONSTRUCTOR);

	    myExecutor = aSoapuiExecutable;
	    myProject = aSoapuiProject;
	    mySoapInterface = aSoapInterface;
	}

	/**
	 * @return the SoapUI Test Executor
	 */
	public File getExecutor()
	{
	    Trace.println(Trace.GETTER);
		return myExecutor;
	}

	/**
	 * @return the SoapUI Project
	 */
	public File getProject()
	{
	    Trace.println(Trace.GETTER);
		return myProject;
	}

	/**
	 * @return the Soap Interface
	 */
	public String getSoapInterface()
	{
	    Trace.println(Trace.GETTER);
		return mySoapInterface;
	}
}
