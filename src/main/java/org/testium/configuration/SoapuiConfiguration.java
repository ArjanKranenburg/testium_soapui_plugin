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
	private File myProject;
	private String mySoapInterface;
	private File mySoapUILibsDir;
	private File myLog4jFile;

	/**
	 * @param aSoapuiExecutable
	 * @param aSoapuiProject
	 */
	public SoapuiConfiguration( //File aSoapuiExecutable,
							    File aSoapuiProject,
							    String aSoapInterface,
							    File aSoapUILibsDir,
							    File aLog4jFile )
	{
	    Trace.println(Trace.CONSTRUCTOR);

	    myProject = aSoapuiProject;
	    mySoapInterface = aSoapInterface;
	    mySoapUILibsDir = aSoapUILibsDir;
	    myLog4jFile = aLog4jFile;
	}

	/**
	 * @return the soapUILibsDir
	 */
	public File getSoapUILibsDir()
	{
		return mySoapUILibsDir;
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

	/**
	 * @return the Log4j File
	 */
	public File getLog4jFile()
	{
	    Trace.println(Trace.GETTER);
		return myLog4jFile;
	}
}
