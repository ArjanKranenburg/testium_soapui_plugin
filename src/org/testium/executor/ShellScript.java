package org.testium.executor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.testtoolinterfaces.testresult.TestResult;
import org.testtoolinterfaces.testsuite.Parameter;
import org.testtoolinterfaces.testsuite.ParameterArrayList;
import org.testtoolinterfaces.utils.StreamGobbler;
import org.testtoolinterfaces.utils.Trace;



public class ShellScript
{
    /**
     * See http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
     * 
	 * @param aScript the script to execute, without parameters
	 * @param aRunLog the file to store the output
	 * 
     * @throws FileNotFoundException when the script or run-log does not
     *         exist or are directories
     */
    public static TestResult.VERDICT execute( File aScript, File aRunLog ) throws FileNotFoundException
    {
    	return execute( aScript, new ParameterArrayList(), aRunLog );
    }

    /**
     * See http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
     * 
	 * @param aScript the script to execute, without parameters
	 * @param aParameters a table of parameters to use
	 * @param aRunLog the file to store the output
	 * 
     * @throws FileNotFoundException when the script or run-log does not
     *         exist or are directories
     */
    public static TestResult.VERDICT execute( File aScript, ParameterArrayList aParameters, File aRunLog ) throws FileNotFoundException
    {
		if ( aScript.isDirectory() )
		{
			throw new FileNotFoundException("Script cannot be a directory: " + aScript.getPath());
		}

		if ( aRunLog.isDirectory() )
		{
			throw new FileNotFoundException("Run log cannot be a Directory: " + aRunLog.getPath());
		}

		Trace.println(Trace.EXEC_PLUS, "execute( "
				+ aScript.getAbsolutePath() + ", "
	            + aRunLog.getAbsolutePath()
	            + " )", true );

        FileOutputStream runLog = new FileOutputStream(aRunLog.getAbsolutePath());
    	if ( !aScript.canExecute() )
    	{
    		PrintWriter pw = new PrintWriter(runLog);
    		pw.println("Cannot execute file:");
    		pw.println(aScript);
            pw.flush();

            return TestResult.ERROR;
    	}

    	String commandString = aScript.getAbsolutePath();
    	ArrayList<Parameter> params = aParameters.sort();
    	for(int i=0; i<params.size(); i++)
    	{
    		commandString += " " + params.get(i).getName() + " " + params.get(i).getValue().toString();
    	}
    	Trace.println(Trace.EXEC_PLUS, "Executing " + commandString);
    	Trace.println(Trace.EXEC_PLUS, "Writing log to " + aRunLog.getAbsolutePath());
 
		File commandLogFile = new File(aRunLog.getParent(), "command.log");
		BufferedWriter commandLog;
		try
		{
			commandLog = new BufferedWriter(new FileWriter(commandLogFile));
			commandLog.write( commandString + "\n" );
			commandLog.close();
		}
		catch (IOException e)
		{
			// Ignored. Then we just don't log the command
		}

//		File errorLogFile = new File(aRunLog.getParent(), "output_error.log");
//        FileOutputStream errorLog = new FileOutputStream(errorLogFile.getAbsolutePath());
    	try
        {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(commandString);

            // any error message?
            StreamGobbler errorGobbler = new 
//                StreamGobbler(proc.getErrorStream(), errorLog);            
            	StreamGobbler(proc.getErrorStream(), runLog);            
            
            // any output?
            StreamGobbler outputGobbler = new 
                StreamGobbler(proc.getInputStream(), runLog);
                
            // kick them off
            errorGobbler.start();
            outputGobbler.start();
                                    
            // Wait for threads to finish
            int exitVal = proc.waitFor();
    		try
    		{
    			Thread.sleep( 20 );
    		}
    		catch (InterruptedException e)
    		{
    			throw new Error( e );
    		}
            runLog.flush();
            runLog.close();

    		Trace.println(Trace.EXEC_PLUS, "Exit value is " + exitVal);
            if ( exitVal > 0 )
            {
                return TestResult.FAILED;
            }
            else
            {
                return TestResult.PASSED;
            }
        }
        catch (Throwable t)
        {
        	Trace.print(Trace.EXEC_PLUS, new Exception(t) );

        	Long logSize = new Long(0);
            if ( aRunLog.exists() && aRunLog.isFile() )
            {
            	logSize = aRunLog.length();
            }

        	if ( logSize.intValue() == 0 )
        	{
        		PrintWriter pw = new PrintWriter(runLog);
        		pw.println("Error while executing SoapUI script:");
        		pw.println(t.getMessage());
                pw.flush();
        	}

        	return TestResult.ERROR;
        }
    }
}
