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
 * Use this to have multiple kraken jobs per core
 */
public class RunMultipleKraken_MultiplePerCore extends BioLockJExecutor
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
		BioLockJUtils.requireString(cReader, ConfigReader.CLUSTER_PARAMS);
		BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_KRAKEN_BINARY);
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_KRAKEN_DATABASE);
		BioLockJUtils.requirePositiveInteger(cReader, ConfigReader.NUMBER_OF_JOBS_PER_CORE);
	}
	
	private File makeNewRunFile(ConfigReader cReader, String scriptDir, BufferedWriter allWriter, 
			String clusterCommand, int countNum) throws Exception
	{
		File runFile = new File(scriptDir + "run_" + countNum + "_" + getTimeStamp(cReader) +  ".sh");
		this.scriptFiles.add(runFile);
		allWriter.write(clusterCommand + " " +  runFile.getAbsolutePath() + "\n"  );
		allWriter.flush();
		return runFile;
	}
	
	private void closeARunFile(BufferedWriter aWriter , File runFile)
		throws Exception
	{
		File touchFile = new File(runFile.getAbsolutePath() + FINISHED_SUFFIX );
		if( touchFile.exists()) touchFile.delete();
		aWriter.write("touch " + touchFile.getAbsolutePath() + "\n");
		aWriter.flush();  aWriter.close();
	}
	
	@Override
	public void executeProjectFile(ConfigReader cReader) throws Exception
	{
	 	File fastaInDir =  BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		File krakenBinary =  BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_KRAKEN_BINARY);
		File krakenDatabase = BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_KRAKEN_DATABASE);
		int numJobsPerCore = BioLockJUtils.requirePositiveInteger(cReader, ConfigReader.NUMBER_OF_JOBS_PER_CORE);
		
		String outputDir = BioLockJUtils.requireString(cReader, ConfigReader.PATH_TO_OUTPUT_DIR);
		String scriptDir = BioLockJUtils.requireString(cReader, ConfigReader.PATH_TO_SCRIPT_DIR);
		
		String[] files = fastaInDir.list();
		
		this.runAllFile = createRunAllFile(cReader, scriptDir);
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(runAllFile));
		int countNum=0;
		int numToDo = numJobsPerCore;

		File runFile = BioLockJUtils.makeNewRunFile(cReader, scriptDir, allWriter, countNum);
		this.scriptFiles.add(runFile);
		
		
		BufferedWriter aWriter = new BufferedWriter(new FileWriter(runFile));
		
		for(String s : files)
		{
			countNum++;
			File fastaFile = new File(fastaInDir.getAbsolutePath() + File.separator + s);
			
			String krakenOutput = outputDir + s + "toKraken.txt";
			String krakenTranslate = outputDir + s + "toKrakenTranslate.txt";
			
			aWriter.write(krakenBinary.getAbsolutePath() + " --db " +  krakenDatabase.getAbsolutePath()  + 
					" --output " + krakenOutput + " " +  fastaFile + "\n" );
			aWriter.write("if [ $? –eq 0 ]; then \n" );
			aWriter.write("    " + krakenBinary.getAbsolutePath() + "-translate " + " --db " +  
					krakenDatabase.getAbsolutePath()  + " " + krakenOutput + " > " + krakenTranslate + "\n" );
			aWriter.write("else touch " + outputDir + s + FAILED_TO_PROCESS + " fi \n" );
					
			if( --numToDo == 0 )
			{
				numToDo = numJobsPerCore;
				closeARunFile(aWriter, runFile);
				runFile = BioLockJUtils.makeNewRunFile(cReader, scriptDir, allWriter, countNum);
				this.scriptFiles.add(runFile);
				aWriter = new BufferedWriter(new FileWriter(runFile));
			}
		}

		closeARunFile(aWriter, runFile);
		allWriter.flush();  allWriter.close();
	}
}