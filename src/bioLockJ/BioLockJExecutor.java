package bioLockJ;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.*;

public abstract class BioLockJExecutor
{

	public abstract void executeProjectFile() throws Exception;
	public abstract void checkDependencies() throws Exception;
	
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
	
	public boolean hasScripts()
	{
		if( scriptsDir==null ) return false;
		return true;
	}
	
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
				BioLockJUtils.formatInt(index, 2) + "_" + name;
		File dir = new File(fullPath);
		if(!dir.mkdirs())
		{
			throw new Exception("ERROR: Unable to create: " + fullPath);
		}
		executorDir = dir;
	}
	
	
	public File getExecutorDir()
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
		runAllFile = ScriptBuilder.createRunAllFile(getScriptDir().getAbsolutePath());
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
 

	public void setInputDir(File inDir) throws Exception
	{
		if( !inDir.getAbsolutePath().contains(getProjectDir().getName()) )
		{
			if (BioLockJUtils.getBoolean(getConfig(), ConfigReader.COPY_INPUT_FLAG, false))
			{
				log.info("Copy input data to " + getExecutorDir().getAbsolutePath() +File.separator + "input");
				FileUtils.copyDirectory(inDir, getInputDir());
			}
		}
		this.inputDir = inDir;
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
