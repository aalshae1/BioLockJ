package bioLockJ.metagenome;

import java.io.*;
import java.util.*;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import utils.ConfigReader;

/**
 * 
 * Use this to have multiple kraken jobs per core
 */
public class RunMultipleKraken_MultiplePerCore extends BioLockJExecutor
{
	
	
	@Override
	public void checkDependencies() throws Exception
	{	
		BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.PATH_TO_KRAKEN_BINARY);
		BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.PATH_TO_KRAKEN_DATABASE);
		BioLockJUtils.requirePositiveInteger(getConfig(), ConfigReader.NUMBER_OF_JOBS_PER_CORE);
	}
	
	private static boolean needNewScript(int numToDo, int numJobsPerCore)
	{
		if(numToDo == numJobsPerCore) return true;
		return false;
	}
	
	
	@Override
	public void executeProjectFile() throws Exception
	{

		File fastaInDir =  BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		File krakenBinary =  BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.PATH_TO_KRAKEN_BINARY);
		File krakenDatabase = BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.PATH_TO_KRAKEN_DATABASE);
		int numJobsPerCore = BioLockJUtils.requirePositiveInteger(getConfig(), ConfigReader.NUMBER_OF_JOBS_PER_CORE);

		String[] files = BioLockJUtils.getFilePaths(fastaInDir);
		log.debug("Number of valid kraken FAST files found: " + files.length);
		setInputDir(fastaInDir);
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(getRunAllFile(), true));
		int countNum = 0;
		int numToDo = numJobsPerCore;
		File subScript = null;
		BufferedWriter aWriter = null;
		boolean scriptOpen = false;
		for(String s : files)
		{
			log.debug("Add input file to script = " + s);
			if(needNewScript(numToDo, numJobsPerCore))
			{
				subScript = createSubScript(allWriter, countNum++);
				aWriter = new BufferedWriter(new FileWriter(subScript, true));
				scriptOpen = true;
			}
			
			File inputFile = new File(fastaInDir.getAbsolutePath() + File.separator + s);
			String krakenOutput = getOutputDir().getAbsolutePath() + File.separator + s + "_toKraken.txt";
			String krakenTranslate = getOutputDir().getAbsolutePath() + File.separator + s + "_toKrakenTranslate.txt";
			String filePath = getOutputDir().getAbsolutePath() + File.separator + s;
			
			String exitOnErrorFlag = BioLockJUtils.getStringOrNull(getConfig(), ConfigReader.EXIT_ON_ERROR);
			boolean exitOnError = exitOnErrorFlag!=null && exitOnErrorFlag.equals("Y"); 
			
			String firstLine = krakenBinary.getAbsolutePath() + " --db " + krakenDatabase.getAbsolutePath()  + 
					" --output " + krakenOutput + " " + inputFile;
			
			String nextLine = krakenBinary.getAbsolutePath() + "-translate --db " +  
					krakenDatabase.getAbsolutePath()  + " " + krakenOutput + " > " + krakenTranslate;
			
			ArrayList<String> lines = new ArrayList<String>();
			lines.add(firstLine);
			lines.add(nextLine);
			BioLockJUtils.addDependantLinesToScript(aWriter, filePath, lines, exitOnError);

			if( --numToDo == 0 )
			{
				numToDo = numJobsPerCore;
				BioLockJUtils.closeSubScript(aWriter, subScript);
				scriptOpen = false;
			}
		}

		if(scriptOpen) BioLockJUtils.closeSubScript(aWriter, subScript);
		BioLockJUtils.closeRunAllFile(allWriter, getRunAllFile().getAbsolutePath());
	}
	
}