package bioLockJ.metagenome;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import utils.ConfigReader;

/**
 * 
 * Use this to run one core per RDP parser job
 */
public class RunMultipleRDP extends BioLockJExecutor
{
	
	@Override
	public void checkDependencies() throws Exception
	{	
		BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.PATH_TO_RDP_JAR);
	}
	
	@Override
	public void executeProjectFile() throws Exception
	{
	 	File fastaInDir =  BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		File rdpBinary =  BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.PATH_TO_RDP_JAR);

		String[] files = fastaInDir.list();
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(getRunAllFile()));
		
		int countNum = 0;
		for(String s : files)
		{

			File fastaFile = new File(fastaInDir.getAbsolutePath() + File.separator + s);
			
			File rdpOutFile = new File(getOutputDir().getAbsolutePath() + File.separator + 
					s  + "toRDP.txt");
			
			File runFile = makeNewRunFile(allWriter, countNum++);

			BufferedWriter writer = new BufferedWriter( new FileWriter(runFile));
			
			writer.write("java -jar "  + rdpBinary.getAbsolutePath() + " " +  
					"-o \"" + rdpOutFile.getAbsolutePath()  + "\" -q \"" + fastaFile+ "\"\n" );
			
			writer.write("gzip " + rdpOutFile.getAbsolutePath() + " \n");
			
			BioLockJUtils.closeRunFile(writer, runFile);
		}
		
		BioLockJUtils.closeRunFile(allWriter, getRunAllFile());
	}
}
