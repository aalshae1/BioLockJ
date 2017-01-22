package bioLockJ;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
	private static final String INDENT = "    ";
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
	
	public File createSubScript(BufferedWriter allWriter, int countNum) throws Exception
	{
		String num = BioLockJUtils.formatInt(countNum, 3);
		File script = new File(getScriptDir().getAbsolutePath() + 
				File.separator + "run_" + num  + ".sh");

		BufferedWriter writer = new BufferedWriter(new FileWriter(script));
		writer.write("### Subscript #" + num + " for parallel processing ### \n" );
		//writer.append("echo initialize_script_" + num + " \n");
		writer.flush(); writer.close();

		allWriter.write("if [[ $okToContinue == true ]]; then \n" );
		
		String clusterParams = getConfig().getAProperty(ConfigReader.CLUSTER_PARAMS);
		String clusterCommand = getConfig().getAProperty(ConfigReader.CLUSTER_BATCH_COMMAND);
		allWriter.write(INDENT + (clusterCommand == null ?  "": clusterCommand + " " ) + script.getAbsolutePath() + 
				" " + (clusterParams == null ?  "": clusterParams ) +   "\n"  );
		
		allWriter.write(INDENT + "if [ $? –ne 0 ]; then \n");
		allWriter.write(INDENT + INDENT +"okToContinue=false \n" );
		allWriter.write(INDENT + "fi \n");
		allWriter.write("fi \n");
		allWriter.flush();
		addScriptFile(script);
		return script;
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
		runAllFile = createRunAllFile();
		return runAllFile;	
	} 
	
	
	public File createRunAllFile() throws Exception
	{
		File f = new File(getScriptDir().getAbsolutePath() + File.separator + "runAll.sh");
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		writer.write("### This script submits subscripts for parallel processing ### \n" );
		writer.write("okToContinue=true \n" );
		writer.flush(); writer.close();
		return f;
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
		log.debug("InputDir: " + inDir.getAbsolutePath());
		log.debug("getProjectDir().getName(): " + getProjectDir().getName());

		if( !inDir.getAbsolutePath().contains(getProjectDir().getName()) )
		{
			log.debug("Parent Dir is not from within the Project (Alien Input): ");
			String copy = getConfig().getAProperty(ConfigReader.COPY_INPUT_FLAG);
			if (copy!=null) log.debug("copy flag = " + copy);
			if (copy!=null && copy.equals("Y"))
			{
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
