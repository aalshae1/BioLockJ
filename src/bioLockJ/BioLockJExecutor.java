package bioLockJ;

import java.io.BufferedWriter;
import java.io.File;
import java.util.List;

import utils.ConfigReader;

public abstract class BioLockJExecutor
{
	public abstract void executeProjectFile(ConfigReader cReader, BufferedWriter logWriter) 
				throws Exception;
	public abstract void checkDependencies(ConfigReader cReader) throws Exception;
	
	public static final String FINISHED_SUFFIX = "_succesfullyFinished";
	public static final String FAILED_TO_PROCESS = "_failedToProcess";
	public static final String RUN_BIOLOCK_J = "#RUN_BIOLOCK_J";
	private final static String SCRIPT_DIR = "script";
	private final static String SUMMARY_DIR = "summary";
	private final static String OUTPUT_DIR = "output";
	
	private String scriptDir;
	private String summaryDir;
	private String outputDir;
	
	public boolean poll()
	{
		return true;
	}
	public List<File> getScriptFiles() { return null; } 
	public File getRunAllFile() { return null; } 
	

	public String getOutputDir(ConfigReader cReader) throws Exception
	{ 
		if (outputDir==null)
			outputDir = getProjectRoot(cReader) + OUTPUT_DIR + File.separator;
		return outputDir;
	} 
	
	public String getSummaryDir(ConfigReader cReader) throws Exception
	{ 
		if (summaryDir==null)
			summaryDir = getProjectRoot(cReader) + SUMMARY_DIR + File.separator;
		return summaryDir;
	} 
	
	public String getScriptDir(ConfigReader cReader) throws Exception
	{ 
		if (scriptDir==null)
			scriptDir = getProjectRoot(cReader) + SCRIPT_DIR + File.separator;
		return scriptDir;
	} 
	
	public String getProjectRoot(ConfigReader cReader) throws Exception
	{
		String projectDir = BioLockJUtils.requireString(cReader, ConfigReader.BIOLOCKJ_PROJECT_DIR);
		String projectName = BioLockJUtils.requireString(cReader, ConfigReader.PROJECT_NAME);
		return projectDir + File.separator + projectName + File.separator;
	}
}
