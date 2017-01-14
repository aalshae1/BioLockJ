package homologySearch.blast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import bioLockJ.BioLockJUtils;
import bioLockJ.BioLockJExecutor;
import utils.ConfigReader;

public class FormatSingleBlastDatabase extends BioLockJExecutor
{
	/**
	 * Takes in FASTA_FILE_TO_FORMAT_FOR_BLAST_DB
	 * Writes a single script to SCRIPTS_DIR_FOR_BLAST_FORMAT
	 * 
	 * will issue BLAST_PRELIMINARY_STRING if defined
	 * requires BLAST_BIN_DIR to be defined
	 * 
	 * CLUSTER_BATCH_COMMAND must be defined (e.g. qsub -q "viper" ) where viper is the name of the cluster
	 */
	
	private File runAllFile = null;
	private List<File> scripts = null;
	
	@Override
	public void checkDependencies(ConfigReader cReader) throws Exception
	{
		BioLockJUtils.requireString(cReader, ConfigReader.BLAST_BINARY_DIR);
		BioLockJUtils.requireExistingFile(
						cReader, ConfigReader.FASTA_FILE_TO_FORMAT_FOR_BLAST_DB);
		
		BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.SCRIPTS_DIR_FOR_BLAST_FORMAT);
		BioLockJUtils.requireString(cReader, ConfigReader.CLUSTER_BATCH_COMMAND);
		BioLockJUtils.requireDBType(cReader);
	}
	
	@Override
	public void executeProjectFile(ConfigReader cReader) throws Exception
	{
		this.scripts = new ArrayList<File>();
		String blastBinDin = BioLockJUtils.requireString(cReader, ConfigReader.BLAST_BINARY_DIR);
		File fastaFileToFormat= BioLockJUtils.requireExistingFile(
						cReader, ConfigReader.FASTA_FILE_TO_FORMAT_FOR_BLAST_DB);
		
		File scriptDir = BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.SCRIPTS_DIR_FOR_BLAST_FORMAT);
		
		String clusterBatchCommand = BioLockJUtils.requireString(cReader, ConfigReader.CLUSTER_BATCH_COMMAND);
		String dbType = BioLockJUtils.requireDBType(cReader);
		
		this.runAllFile = createRunAllFile(cReader, scriptDir.getAbsolutePath());
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(this.runAllFile));
		
		File script = new File(
						scriptDir.getAbsolutePath() + File.separator + "run_" + System.currentTimeMillis()  + ".sh");
		BufferedWriter writer = new BufferedWriter(new FileWriter(script));
				
		String prelimString = cReader.getAProperty(ConfigReader.BLAST_PRELIMINARY_STRING);
				
		if( prelimString != null)
			writer.write(prelimString + "\n");
				
		writer.write(blastBinDin + "/makeblastdb -dbtype " + dbType +  
								" -in " + fastaFileToFormat.getAbsolutePath() + "\n");
				
		File touchFile = new File(script.getAbsolutePath() + FINISHED_SUFFIX );
				
		if( touchFile.exists())
			touchFile.delete();
				
		writer.write("touch " + touchFile.getAbsolutePath() + "\n");
				
		writer.flush();  writer.close();
		this.scripts.add(script);
				
		allWriter.write(clusterBatchCommand + " " + script.getAbsolutePath() + "\n");
		allWriter.flush();
		
		allWriter.flush();  allWriter.close();
	}
	
	@Override
	public File getRunAllFile()
	{
		return runAllFile;
	}
	
	@Override
	public List<File> getScriptFiles()
	{
		return scripts;
	}
	
}
