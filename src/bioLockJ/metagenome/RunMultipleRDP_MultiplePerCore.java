package bioLockJ.metagenome;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import utils.ConfigReader;

/**
 * 
 * Use this to have multiple RDP jobs per core
 */
public class RunMultipleRDP_MultiplePerCore extends BioLockJExecutor
{
	private File runAllFile= null;
	private List<File> scriptFiles = new ArrayList<File>();
	
	@Override
	public File getRunAllFile()
	{
		return runAllFile;
	}
	
	@Override
	public List<File> getScriptFiles()
	{
		return scriptFiles;
	}
	
	@Override
	public void checkDependencies(ConfigReader cReader) throws Exception
	{	
		BioLockJUtils.requireString(cReader, ConfigReader.CLUSTER_BATCH_COMMAND);
		BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_RDP_JAR);
		BioLockJUtils.requirePositiveInteger(cReader, ConfigReader.NUMBER_OF_JOBS_PER_CORE);
	}
	
	private File makeNewRunFile( File rdpScriptDir, BufferedWriter allWriter,
					String clusterCommand, String clusterParams, int countNum) throws Exception
	{
		File runFile = new File(rdpScriptDir.getAbsoluteFile() + File.separator + "run_" + 
				countNum + "_" + System.currentTimeMillis() +  ".sh");
	
		this.scriptFiles.add(runFile);
		
		allWriter.write(clusterCommand + " " +  runFile.getAbsolutePath() + 
				" " + (clusterParams == null ?  "": clusterParams ) +   "\n"  );
		allWriter.flush();
	
		return runFile;
	}
	
	private void closeARunFile(BufferedWriter aWriter , File runFile)
		throws Exception
	{
		File touchFile = new File(runFile.getAbsolutePath() + FINISHED_SUFFIX );
		
		if( touchFile.exists())
			touchFile.delete();
		
		aWriter.write("touch " + touchFile.getAbsolutePath() + "\n");
		
		aWriter.flush();  aWriter.close();
		
	}
	
	@Override
	public void executeProjectFile(ConfigReader cReader) throws Exception
	{
		String clusterCommand = BioLockJUtils.requireString(cReader, ConfigReader.CLUSTER_BATCH_COMMAND);
	 	File fastaInDir =  BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		File rdpBinary =  BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_RDP_JAR);
		int numJobsPerCore = BioLockJUtils.requirePositiveInteger(cReader, ConfigReader.NUMBER_OF_JOBS_PER_CORE);
		String clusterParams = BioLockJUtils.getStringOrNull(cReader, ConfigReader.CLUSTER_PARAMS);
		File rdpOutDir =  getOutputDir(cReader);
		File rdpScriptDir =  getScriptDir(cReader);
		String[] files = fastaInDir.list();
	
		this.runAllFile = createRunAllFile(cReader, rdpScriptDir.getAbsolutePath());
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(runAllFile));
		
		
		
		int countNum=0;
		int numToDo = numJobsPerCore;
		File runFile = makeNewRunFile(rdpScriptDir, allWriter, clusterCommand, clusterParams,countNum);
		BufferedWriter aWriter = new BufferedWriter(new FileWriter(runFile));
		
		for(String s : files)
		{
			countNum++;
			File fastaFile = new File(fastaInDir.getAbsolutePath() + File.separator + s);
			
			File rdpOutFile = new File(rdpOutDir.getAbsolutePath() + File.separator + 
					s  + "toRDP.txt");
			
			
			aWriter.write("java -jar "  + rdpBinary.getAbsolutePath() + " " +  
					"-o \"" + rdpOutFile.getAbsolutePath()  + "\" -q \"" + fastaFile+ "\"\n" );
			
			aWriter.write("gzip \"" + rdpOutFile.getAbsolutePath() + "\" \n");
			
			numToDo--;
			
			if( numToDo == 0 )
			{
				numToDo = numJobsPerCore;
				closeARunFile(aWriter, runFile);
				runFile = makeNewRunFile(rdpScriptDir, allWriter, clusterCommand,clusterParams,countNum);
				aWriter = new BufferedWriter(new FileWriter(runFile));
			}
			
		}

		closeARunFile(aWriter, runFile);
		allWriter.flush();  allWriter.close();

	}
}
