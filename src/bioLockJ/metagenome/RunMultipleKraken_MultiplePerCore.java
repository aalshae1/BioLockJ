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
	
	
	@Override
	public void executeProjectFile() throws Exception
	{

		File fastaInDir =  BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		File krakenBinary =  BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.PATH_TO_KRAKEN_BINARY);
		File krakenDatabase = BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.PATH_TO_KRAKEN_DATABASE);
		int numJobsPerCore = BioLockJUtils.requirePositiveInteger(getConfig(), ConfigReader.NUMBER_OF_JOBS_PER_CORE);

		String[] files = getFiles(fastaInDir);
		log.info("Number of files = " + files.length);
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(getRunAllFile()));
		allWriter.write(krakenBinary.getAbsolutePath() + " --version \n");
		int countNum = 0;
		int numToDo = numJobsPerCore;
		File runFile = null;
		BufferedWriter aWriter = null;
		boolean runFileOpen = false;
		for(String s : files)
		{
			runFile = makeNewRunFile(allWriter, countNum++);
			aWriter = new BufferedWriter(new FileWriter(runFile));
			runFileOpen = true;
			File fastaFile = new File(fastaInDir.getAbsolutePath() + File.separator + s);
			
			String krakenOutput = getOutputDir().getAbsolutePath() + File.separator + s + "toKraken.txt";
			String krakenTranslate = getOutputDir().getAbsolutePath() + File.separator + s + "toKrakenTranslate.txt";
			String filePath = getOutputDir().getAbsolutePath() + File.separator + s;
			
			String firstLine = krakenBinary.getAbsolutePath() + " --db " +  krakenDatabase.getAbsolutePath()  + 
					" --output " + krakenOutput + " " +  fastaFile;
			
			String nextLine = krakenBinary.getAbsolutePath() + "-translate " + "--db " +  
					krakenDatabase.getAbsolutePath()  + " " + krakenOutput + " > " + krakenTranslate;
			
			BioLockJUtils.addNextLineToScript(aWriter, filePath, firstLine);
			BioLockJUtils.addNextLineToScript(aWriter, filePath, nextLine);
			
			if( --numToDo == 0 )
			{
				numToDo = numJobsPerCore;
				BioLockJUtils.closeRunFile(aWriter, runFile);
				runFileOpen = false;
			}
		}

		if(runFileOpen) BioLockJUtils.closeRunFile(aWriter, runFile);
		BioLockJUtils.closeRunFile(allWriter, getRunAllFile());
	}
	
	
	private String[] getFiles(File inputDir)
	{
		String[] input = inputDir.list();
		ArrayList<String> list = new ArrayList<String>();
		for(String s : input)
		{
			if(!s.startsWith(".")) // ignore hiddend files
			{
				list.add(s);
			}
		}
		
		String[] files = new String[list.size()];
		int index = 0;
		for(String s : list)
		{
			files[index++] = s;
		}
		
		return files;
	}
}