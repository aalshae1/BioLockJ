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
		BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_KRAKEN_BINARY);
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_KRAKEN_DATABASE);
		BioLockJUtils.requirePositiveInteger(cReader, ConfigReader.NUMBER_OF_JOBS_PER_CORE);
	}
	
	private File makeNewRunFile(String scriptDir, BufferedWriter allWriter, 
			String clusterCommand, int countNum) throws Exception
	{
		File runFile = new File(scriptDir + "run_" + countNum + "_" + System.currentTimeMillis() +  ".sh");
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
	public void executeProjectFile(ConfigReader cReader, BufferedWriter logWriter) throws Exception
	{
		String clusterCommand = BioLockJUtils.requireString(cReader, ConfigReader.CLUSTER_BATCH_COMMAND);
	 	File fastaInDir =  BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		File krakenBinary =  BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_KRAKEN_BINARY);
		File krakenDatabase = BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_KRAKEN_DATABASE);
		int numJobsPerCore = BioLockJUtils.requirePositiveInteger(cReader, ConfigReader.NUMBER_OF_JOBS_PER_CORE);
		String clusterParams = BioLockJUtils.getStringOrNull(cReader, ConfigReader.CLUSTER_PARAMS);
		
		//File krakenScriptDir =  BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_KRAKEN_SCRIPT_DIR);
		//File krakenOutDir =  BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_KRAKEN_OUTPUT_DIRECTORY);
		
		String[] files = fastaInDir.list();

		this.runAllFile = new File(getScriptDir(cReader) + File.separator + "runAll.sh");
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(runAllFile));
		int countNum=0;
		int numToDo = numJobsPerCore;
		File runFile = makeNewRunFile(getScriptDir(cReader), allWriter, clusterCommand, countNum);
		BufferedWriter aWriter = new BufferedWriter(new FileWriter(runFile));
		
		for(String s : files)
		{
			countNum++;
			File fastaFile = new File(fastaInDir.getAbsolutePath() + File.separator + s);
			
			//File krakenOutFile = new File(krakenOutDir.getAbsolutePath() + File.separator + s  + "toKraken.txt");
			//File krakenTranslateFile = new File(krakenOutDir.getAbsolutePath() + File.separator + s  + "toKrakenTranslate.txt");
			
			String krakenOutput = getOutputDir(cReader) + s + "toKraken.txt";
			String krakenTranslate = getOutputDir(cReader) + s + "toKrakenTranslate.txt";
			
			if( clusterParams != null)
				aWriter.write(clusterParams + "\n");
			
			aWriter.write(krakenBinary.getAbsolutePath() + " --db " +  krakenDatabase.getAbsolutePath()  + 
					" --output " + krakenOutput + " " +  fastaFile + "\n" );
			aWriter.write("if [ $? –eq 0 ]; then \n" );
			aWriter.write("    " + krakenBinary.getAbsolutePath() + "-translate " + " --db " +  
					krakenDatabase.getAbsolutePath()  + " " + krakenOutput + " > " + krakenTranslate + "\n" );
			aWriter.write("else touch " + getOutputDir(cReader) + s + FAILED_TO_PROCESS + " fi \n" );
			
			//numToDo--; MS move "--" into in conditional before param to subtract before check			
			if( --numToDo == 0 )
			{
				numToDo = numJobsPerCore;
				closeARunFile(aWriter, runFile);
				runFile = makeNewRunFile(getScriptDir(cReader), allWriter, clusterCommand, countNum);
				aWriter = new BufferedWriter(new FileWriter(runFile));
			}
		}

		closeARunFile(aWriter, runFile);
		allWriter.flush();  allWriter.close();
	}
}