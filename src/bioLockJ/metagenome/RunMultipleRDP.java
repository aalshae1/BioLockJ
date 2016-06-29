package bioLockJ.metagenome;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import utils.ConfigReader;

public class RunMultipleRDP extends BioLockJExecutor
{

	@Override
	public void checkDependencies(ConfigReader cReader) throws Exception
	{	
		BioLockJUtils.requireString(cReader, ConfigReader.CLUSTER_BATCH_COMMAND);
		BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_OUTPUT_RDP_DIRECTORY);
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_RDP_JAR);
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.RDP_SCRIPT_DIR);
	}
	
	@Override
	public void executeProjectFile(ConfigReader cReader, BufferedWriter logWriter) throws Exception
	{
		String clusterCommand = BioLockJUtils.requireString(cReader, ConfigReader.CLUSTER_BATCH_COMMAND);
	 	File fastaInDir =  BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		File rdpOutDir =  BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_OUTPUT_RDP_DIRECTORY);
		File rdpBinary =  BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_RDP_JAR);
		File rdpScriptDir =  BioLockJUtils.requireExistingFile(cReader, ConfigReader.RDP_SCRIPT_DIR);
		
		String[] files = fastaInDir.list();
	
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(new File(
			rdpScriptDir.getAbsoluteFile() + File.separator + "runAll.sh")));
		

		int countNum=0;
		for(String s : files)
		{
			countNum++;
			File fastaFile = new File(fastaInDir.getAbsolutePath() + File.separator + s);
			
			File rdpOutFile = new File(rdpOutDir.getAbsolutePath() + File.separator + 
					s  + "toRDP.txt");
			
			File runFile = new File(rdpScriptDir.getAbsoluteFile() + File.separator + "run_" + 
						countNum + "_" + System.currentTimeMillis() +  ".sh");
			
			BufferedWriter writer = new BufferedWriter( new FileWriter(runFile));
			
			writer.write("java -jar "  + rdpBinary.getAbsolutePath() + " " +  
					"-o \"" + rdpOutFile.getAbsolutePath()  + "\" -q \"" + fastaFile+ "\"\n" );
			
			writer.write("gzip " + rdpOutFile.getAbsolutePath() + " \n");
			
			File touchFile = new File(runFile.getAbsolutePath() + FINISHED_SUFFIX );
			
			if( touchFile.exists())
				touchFile.delete();
			
			writer.write("touch " + touchFile.getAbsolutePath() + "\n");
			
			writer.flush();  writer.close();
			
			allWriter.write(clusterCommand + " " +  runFile.getAbsolutePath() +  "\n"  );
			allWriter.flush();
		}
		
		allWriter.flush();  allWriter.close();
	}
}
