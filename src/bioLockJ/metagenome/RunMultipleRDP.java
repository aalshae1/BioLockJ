package bioLockJ.metagenome;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import utils.ConfigReader;

/**
 * 
 * Use this to run one core per RDP parser job
 */
public class RunMultipleRDP extends BioLockJExecutor
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
	}
	
	@Override
	public void executeProjectFile(ConfigReader cReader) throws Exception
	{
		String clusterCommand = BioLockJUtils.requireString(cReader, ConfigReader.CLUSTER_BATCH_COMMAND);
		String clusterParams = BioLockJUtils.getStringOrNull(cReader, ConfigReader.CLUSTER_PARAMS);
	 	File fastaInDir =  BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		File rdpBinary =  BioLockJUtils.requireExistingFile(cReader, ConfigReader.PATH_TO_RDP_JAR);
		File rdpOutDir =  getOutputDir(cReader);
		File rdpScriptDir =  getScriptDir(cReader);
		
		log.debug("Cluster parms = " + clusterParams);
		
		String[] files = fastaInDir.list();
	
		this.runAllFile = createRunAllFile(cReader, rdpScriptDir.getAbsolutePath());
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(runAllFile));
		
		int countNum=0;
		for(String s : files)
		{
			countNum++;
			File fastaFile = new File(fastaInDir.getAbsolutePath() + File.separator + s);
			
			File rdpOutFile = new File(rdpOutDir.getAbsolutePath() + File.separator + 
					s  + "toRDP.txt");
			
			File runFile = new File(rdpScriptDir.getAbsoluteFile() + File.separator + "run_" + 
						countNum + "_" + System.currentTimeMillis() +  ".sh");
			
			this.scriptFiles.add(runFile);
			
			BufferedWriter writer = new BufferedWriter( new FileWriter(runFile));
			
			writer.write("java -jar "  + rdpBinary.getAbsolutePath() + " " +  
					"-o \"" + rdpOutFile.getAbsolutePath()  + "\" -q \"" + fastaFile+ "\"\n" );
			
			writer.write("gzip " + rdpOutFile.getAbsolutePath() + " \n");
			
			File touchFile = new File(runFile.getAbsolutePath() + FINISHED_SUFFIX );
			
			if( touchFile.exists())
				touchFile.delete();
			
			writer.write("touch " + touchFile.getAbsolutePath() + "\n");
			
			writer.flush();  writer.close();
			
			allWriter.write(clusterCommand + " " +  runFile.getAbsolutePath() + 
					" " + (clusterParams == null ? "" : clusterParams) +  "\n"  );
			allWriter.flush();
		}
		
		allWriter.flush();  allWriter.close();
	}
}
