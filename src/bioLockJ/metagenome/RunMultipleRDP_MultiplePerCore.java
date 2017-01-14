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
		BioLockJUtils.requireString(cReader, ConfigReader.CLUSTER_PARAMS);
		BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_RDP_JAR);
		BioLockJUtils.requirePositiveInteger(cReader, ConfigReader.NUMBER_OF_JOBS_PER_CORE);
	}
	
	
	@Override
	public void executeProjectFile(ConfigReader cReader) throws Exception
	{
	 	File fastaInDir =  BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		File rdpBinary =  BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_RDP_JAR);
		int numJobsPerCore = BioLockJUtils.requirePositiveInteger(cReader, ConfigReader.NUMBER_OF_JOBS_PER_CORE);
		File rdpOutDir =  getOutputDir(cReader);
		File scriptDir =  getScriptDir(cReader);
		String[] files = fastaInDir.list();
	
		this.runAllFile = createRunAllFile(cReader, scriptDir.getAbsolutePath());
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(runAllFile));
		
		int countNum=0;
		int numToDo = numJobsPerCore;
		File runFile = BioLockJUtils.makeNewRunFile(cReader, 
				scriptDir.getAbsolutePath(), allWriter, countNum);
		this.scriptFiles.add(runFile);
		BufferedWriter aWriter = new BufferedWriter(new FileWriter(runFile));
		
		for(String s : files)
		{
			countNum++;
			File fastaFile = new File(fastaInDir.getAbsolutePath() + File.separator + s);
			File rdpOutFile = new File(rdpOutDir.getAbsolutePath() + File.separator + 
					s  + "toRDP.txt");
			
			aWriter.write("java -jar "  + rdpBinary.getAbsolutePath() + " " +  
					"-o \"" + rdpOutFile.getAbsolutePath()  + "\" -q \"" + fastaFile + "\"\n" );
			
			aWriter.write("gzip \"" + rdpOutFile.getAbsolutePath() + "\" \n");
			
			numToDo--;
			
			if( numToDo == 0 )
			{
				numToDo = numJobsPerCore;
				BioLockJUtils.closeRunFile(aWriter, runFile);
				runFile = BioLockJUtils.makeNewRunFile(cReader, 
						scriptDir.getAbsolutePath(), allWriter, countNum);
				this.scriptFiles.add(runFile);
				aWriter = new BufferedWriter(new FileWriter(runFile));
			}
		}

		BioLockJUtils.closeRunFile(aWriter, runFile);
		allWriter.flush();  allWriter.close();
	}
}
