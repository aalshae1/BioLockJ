package bioLockJ;

import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.*;

import bioLockJ.BioLockJUtils;
import utils.ConfigReader;

public abstract class BioLockJExecutor
{

	public abstract void executeProjectFile() throws Exception;
	public abstract void checkDependencies() throws Exception;
	
	public static final String FAILED_TO_PROCESS = "_failedToProcess";
	public static final String RUN_BIOLOCK_J = "#RUN_BIOLOCK_J";
	
	protected static final Logger log = LoggerFactory.getLogger(BioLockJExecutor.class);
	
	private List<File> scriptFiles = new ArrayList<File>();
	private ConfigReader config;
	
	private File runAllFile;
	private File projectDir;
	private File executorDir;
	private File inputDir;
	private File scriptsDir;
	private File summaryDir;
	private File outputDir;
	
	
	public boolean poll(){
		return true;
	}
	
	public void setConfig(ConfigReader cReader)
	{
		config = cReader;
	}
	
	public ConfigReader getConfig()
	{
		return config;
	}
	
	public void setExecutorDir(String name, int index) throws Exception
	{
		String fullPath = getProjectDir().getAbsolutePath() + File.separator + 
				BioLockJUtils.formatInt(index) + "_" + name;
		File dir = new File(fullPath);
		if(!dir.mkdir())
		{
			throw new Exception("ERROR: Unable to create: " + fullPath);
		}
		executorDir = dir;
	}
	
	public File makeNewRunFile(BufferedWriter allWriter, int countNum) throws Exception
	{
		File runFile = new File(getScriptDir().getAbsolutePath() + 
				File.separator + "run_" + BioLockJUtils.formatInt(countNum)  + ".sh");
		addScriptFile(runFile);
		String clusterParams = getConfig().getAProperty(ConfigReader.CLUSTER_PARAMS);
		String clusterCommand = getConfig().getAProperty(ConfigReader.CLUSTER_BATCH_COMMAND);

		allWriter.write((clusterCommand == null ?  "": clusterCommand + " " ) + runFile.getAbsolutePath() + 
				" " + (clusterParams == null ?  "": clusterParams ) +   "\n"  );
		
		allWriter.flush();
		return runFile;
	}
	
	
	protected File getExecutorDir()
	{
		return executorDir;
	}
	
	public List<File> getScriptFiles() 
	{ 
		return scriptFiles;
	} 
	
	public void addScriptFile(File f) 
	{ 
		scriptFiles.add(f);
	} 
	
	
	public File getRunAllFile() throws Exception
	{  
		if(runAllFile!=null)
		{
			return runAllFile;
		}
		runAllFile = new File(getScriptDir().getAbsolutePath() + File.separator + "runAll.sh");
		if(!runAllFile.mkdir())
		{
			throw new Exception("ERROR: Unable to create: " + runAllFile);
		}
		return runAllFile;	
	} 
	

	public String getTimeStamp()
	{
		return getConfig().getAProperty(ConfigReader.RUN_TIMESTAMP);
	}
	
	
	public File getProjectDir() throws Exception
	{
		if(projectDir!=null)
		{
			return projectDir;
		}
		projectDir = BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.PATH_TO_PROJECT_DIR);
		return projectDir; 
	}


	
	public File getInputDir() throws Exception
	{
		if(inputDir!=null)
		{
			return inputDir;
		}
		inputDir = createSubDir("input");
		return inputDir;	
	}
	
	public File getScriptDir() throws Exception
	{
		if(scriptsDir!=null)
		{
			return scriptsDir;
		}
		scriptsDir = createSubDir("scripts");
		return scriptsDir;	
	}
	
	public File getSummaryDir() throws Exception
	{
		if(summaryDir!=null)
		{
			return summaryDir;
		}
		summaryDir = createSubDir("summary");
		return summaryDir;	
	}
	
	public File getOutputDir() throws Exception
	{
		if(outputDir!=null)
		{
			return outputDir;
		}
		outputDir = createSubDir("output");
		return outputDir;	
	}
	
	
	private File createSubDir(String subDirPath) throws Exception
	{ 
		String subDir = getExecutorDir().getAbsolutePath() + File.separator + subDirPath;
		File dir = new File(subDir);
		if(!dir.mkdir())
		{
			throw new Exception("ERROR: Unable to create: " + dir);
		}
		return dir;
	} 

	
}
