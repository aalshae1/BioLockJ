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
	public static final String ERROR_DETECTED = "errorDetected";
	public static final String ERROR_ON_PREVIOUS_LINE = "errorOnPreviousLine";
	
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
				closeScript(aWriter, subScript.getAbsolutePath());
				scriptOpen = false;
			}		
		}
		
		if(scriptOpen) closeScript(aWriter, subScript.getAbsolutePath());
		closeScript(allWriter, blje.getRunAllFile().getAbsolutePath());
	}
	
	protected static File createRunAllFile(String scriptDir) throws Exception
	{ 
		File f = new File(scriptDir + File.separator + "runAll.sh");
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		writer.write("### This script submits subscripts for parallel processing ### \n" );
		
		File qsubOutput = new File(f.getParentFile().getParentFile().getAbsolutePath() + 
				File.separator + "qsub");
		qsubOutput.mkdirs();

		log.info("Create Qsub Directory: " + qsubOutput.getAbsolutePath());
		writer.write("cd " + qsubOutput.getAbsolutePath() + " \n" );
		writer.write(ERROR_DETECTED + "=false \n" );
		writer.flush(); writer.close();
		return f;
	}
	
	protected static File createSubScript(BioLockJExecutor blje, BufferedWriter allWriter, int countNum) throws Exception
	{
		String num = BioLockJUtils.formatInt(countNum, 3);
		File script = new File(blje.getScriptDir().getAbsolutePath() + File.separator + "run_" + num  + ".sh");
		log.info(blje.getClass().getSimpleName() + " Create Sub Script: " + script.getAbsolutePath());
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(script));
		String clusterParams = blje.getConfig().getAProperty(ConfigReader.CLUSTER_PARAMS);
		writer.write(clusterParams == null ?  "": clusterParams + "\n" );
		writer.write(ERROR_DETECTED + "=false \n");
		writer.flush(); writer.close();

		allWriter.write("if [[ $" + ERROR_DETECTED + " == false ]]; then \n" );

		String clusterCommand = blje.getConfig().getAProperty(ConfigReader.CLUSTER_BATCH_COMMAND);
		allWriter.write(INDENT + (clusterCommand == null ?  "": clusterCommand + " " ) + 
				script.getAbsolutePath() + "\n"  );
		
		allWriter.write(INDENT + "if [ \"$?\" -ne \"0\" ]; then \n");
		allWriter.write(INDENT + INDENT + ERROR_DETECTED + "=true \n" );
		allWriter.write(INDENT + "fi \n");
		allWriter.write("fi \n");
		allWriter.flush();
		blje.addScriptFile(script);
		return script;
	} 
	
	
	protected static void closeScript(BufferedWriter writer, String script) throws Exception
	{
		writer.write("if [[ $" + ERROR_DETECTED + " == false ]]; then \n" );
		writer.write(INDENT + "touch " + script + SCRIPT_SUCCEEDED + " \n" );
		writer.write("else \n" );
		writer.write(INDENT + "touch " + script + SCRIPT_FAILED + " \n" );
		writer.write(INDENT + "exit 1 \n");
		writer.write("fi \n");
		writer.flush();  writer.close();
	}
	


	protected static void addDependantLinesToScript(BufferedWriter writer, 
			ArrayList<String> lines, boolean exitOnError) throws Exception
	{
		Iterator<String> it = lines.iterator();
		writer.write(ERROR_ON_PREVIOUS_LINE + "=false \n" );
		boolean firstLine = true;
		boolean indent = false;
		while(it.hasNext())
		{ 
			if( exitOnError )
			{
				indent = true;
				writer.write("if [[ " + (firstLine ?  "": "$" + ERROR_ON_PREVIOUS_LINE + 
						" == false && ") + "$" + ERROR_DETECTED + " == false ]]; then \n" );
			}
			else if( !firstLine )
			{
				indent = true; 
				writer.write("if [[ $" + ERROR_ON_PREVIOUS_LINE + " == false ]]; then \n" );
			}
			
			writer.write((indent ? INDENT: "") + it.next() + "\n" );
			writer.write((indent ? INDENT: "") + "if [ \"$?\" -ne \"0\" ]; then \n");
			writer.write((indent ? INDENT: "") + INDENT + ERROR_ON_PREVIOUS_LINE + "=true \n");
			writer.write((indent ? INDENT: "") + INDENT + ERROR_DETECTED + "=true \n");
			writer.write((indent ? INDENT: "") + "fi \n");
			writer.write(indent ? "fi \n": "");
			firstLine = false;
		}
	}
	
	
	private static boolean needNewScript(int numToDo, int numJobsPerCore)
	{
		if(numToDo == numJobsPerCore) return true;
		return false;
	}

}
