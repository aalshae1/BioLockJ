/**
 * @UNCC Fodor Lab
 *  
 * @author Michael Sioda
 * @date Jan 23, 2017
 */
package bioLockJ;
 
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ConfigReader;

/**
 *
 */
public class ScriptBuilder 
{
	
    protected static final Logger log = LoggerFactory.getLogger(ScriptBuilder.class);

	public static final String SCRIPT_FAILED = "_FAIL";
	public static final String SCRIPT_SUCCEEDED = "_SUCCESS";
	public static final String INDENT = "    ";
	public static final String RUN_BIOLOCK_J = "#RUN_BIOLOCK_J";
	
	public static void buildScripts(BioLockJExecutor blje, ArrayList<ArrayList<String>> data) throws Exception
	{
		boolean needMultipleScripts = true;
		int numJobsPerCore = 0;
		try{
			numJobsPerCore = BioLockJUtils.requirePositiveInteger (blje.getConfig(), ConfigReader.NUMBER_OF_JOBS_PER_CORE);
		}catch(Exception ex){
			log.warn("NUMBER_OF_JOBS_PER_CORE not defined, only one script will be created");
			needMultipleScripts = false;
		}
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(blje.getRunAllFile(), true));
		int countNum = 0;
		int numToDo = numJobsPerCore;
		File subScript = null;
		BufferedWriter aWriter = null;
		boolean scriptOpen = false;
		boolean exitOnError = BioLockJUtils.getBoolean(blje.getConfig(), ConfigReader.EXIT_ON_ERROR_FLAG, false);
		
		for(ArrayList<String> lines : data)
		{
			if(subScript == null || needNewScript(numToDo, numJobsPerCore))
			{
				if(needMultipleScripts || subScript == null )
				{
					subScript = createSubScript(blje, allWriter, countNum++);
					aWriter = new BufferedWriter(new FileWriter(subScript, true));
					scriptOpen = true;
				}
			}
			
			addDependantLinesToScript(aWriter, lines, exitOnError);
			
			if( needMultipleScripts && --numToDo == 0 )
			{
				numToDo = numJobsPerCore;
				closeSubScript(aWriter, subScript);
				scriptOpen = false;
			}		
		}
		
		if(scriptOpen) closeSubScript(aWriter, subScript);
		closeRunAllFile(allWriter, blje.getRunAllFile().getAbsolutePath());
	}
	
	protected static File createRunAllFile(String scriptDir) throws Exception
	{
		File f = new File(scriptDir + File.separator + "runAll.sh");
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		writer.write("### This script submits subscripts for parallel processing ### \n" );
		writer.write("okToContinue=true \n" );
		writer.flush(); writer.close();
		return f;
	}
	
	protected static File createSubScript(BioLockJExecutor blje, BufferedWriter allWriter, int countNum) throws Exception
	{
		String num = BioLockJUtils.formatInt(countNum, 3);
		File script = new File(blje.getScriptDir().getAbsolutePath() + File.separator + "run_" + num  + ".sh");

		BufferedWriter writer = new BufferedWriter(new FileWriter(script));
		writer.write("### Subscript #" + num + " for parallel processing ### \n" );
		writer.write("failureDetected=false \n");
		writer.flush(); writer.close();

		allWriter.write("if [[ $okToContinue == true ]]; then \n" );
		
		String clusterParams = blje.getConfig().getAProperty(ConfigReader.CLUSTER_PARAMS);
		String clusterCommand = blje.getConfig().getAProperty(ConfigReader.CLUSTER_BATCH_COMMAND);
		allWriter.write(INDENT + (clusterCommand == null ?  "": clusterCommand + " " ) + script.getAbsolutePath() + 
				" " + (clusterParams == null ?  "": clusterParams ) +   "\n"  );
		
		allWriter.write(INDENT + "if [ \"$?\" -ne \"0\" ]; then \n");
		allWriter.write(INDENT + INDENT +"okToContinue=false \n" );
		allWriter.write(INDENT + "fi \n");
		allWriter.write("fi \n");
		allWriter.flush();
		blje.addScriptFile(script);
		return script;
	} 
	
	
	protected static void closeRunAllFile(BufferedWriter writer, String runAllFilePath) throws Exception
	{
		writer.write("if [[ $okToContinue == true ]]; then \n" );
		writer.write(INDENT + "touch " + runAllFilePath + SCRIPT_SUCCEEDED + " \n" );
		writer.write("else \n" );
		writer.write(INDENT + "touch " + runAllFilePath + SCRIPT_FAILED + " \n" );
		writer.write(INDENT + "exit 1 \n");
		writer.write("fi \n");
		writer.flush();  writer.close();
	}
 
	
	protected static void closeSubScript(BufferedWriter writer, File script) throws Exception
	{
		writer.write("if [[ $failureDetected == false ]]; then \n" );
		writer.write(INDENT + "touch " + script.getAbsolutePath() + SCRIPT_SUCCEEDED + " \n" );
		writer.write("else \n" );
		writer.write(INDENT + "touch " + script.getAbsolutePath() + SCRIPT_FAILED + " \n" );
		writer.write(INDENT + "exit 1 \n");
		writer.write("fi \n");
		writer.flush();  writer.close();
	}
	
	
	protected static void addDependantLinesToScript(BufferedWriter writer, 
			ArrayList<String> lines, boolean exitOnError) throws Exception
	{
		Iterator<String> it = lines.iterator();
		writer.write("okToContinue=true \n" );

		while(it.hasNext())
		{ 
			if(exitOnError)
			{
				writer.write("if [[ $okToContinue == true && $failureDetected == false ]]; then \n" );
			}
			else
			{
				writer.write("if [[ $okToContinue == true ]]; then \n" );
			}
			
			writer.write(INDENT + it.next() + "\n" );
			writer.write(INDENT + "if [ \"$?\" -ne \"0\" ]; then \n");
			writer.write(INDENT + INDENT + "okToContinue=false \n");
			writer.write(INDENT + INDENT + "failureDetected=true \n");
			writer.write(INDENT + "fi \n");
			writer.write("fi \n");
		}
	}
	
	
	private static boolean needNewScript(int numToDo, int numJobsPerCore)
	{
		if(numToDo == numJobsPerCore) return true;
		return false;
	}

}
