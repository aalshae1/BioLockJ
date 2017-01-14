package homologySearch.blast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import bioLockJ.BioLockJUtils;
import bioLockJ.BioLockJExecutor;
import utils.ConfigReader;

public class FormatMultipleBlastDatabases extends BioLockJExecutor
{
	/*
	 * Takes in FASTA_DIR_TO_FORMAT which should only contain fasta files
	 * (sub-directories are allowed but will be ignored)
	 * 
	 * Writes scripts to SCRIPTS_DIR/SCRIPTS_DIR_FOR_BLAST_FORMAT
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
		BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.FASTA_DIR_TO_FORMAT);
		BioLockJUtils.requireString(cReader, ConfigReader.CLUSTER_BATCH_COMMAND);
	}
	
	@Override
	public void executeProjectFile(ConfigReader cReader) throws Exception
	{
		this.scripts = new ArrayList<File>();
		String blastBinDin = BioLockJUtils.requireString(cReader, ConfigReader.BLAST_BINARY_DIR);
		File fastaDirToFormat = BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.FASTA_DIR_TO_FORMAT);
		String clusterBatchCommand = BioLockJUtils.requireString(cReader, ConfigReader.CLUSTER_BATCH_COMMAND);

		File scriptDir = getScriptDir(cReader, "formatBlastDB");
		
		int index =1;
		this.runAllFile = createRunAllFile(cReader, scriptDir.getAbsolutePath());
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(this.runAllFile));
		
		String[] filesToFormat = fastaDirToFormat.list();
		
		for( String s : filesToFormat)
		{
			File fastaFile = new File(fastaDirToFormat.getAbsolutePath() + File.separator + s);
			
			if( ! fastaFile.isDirectory())
			{
				File script = new File(
						scriptDir.getAbsolutePath() + File.separator + "run_" + index + "_" +
								getTimeStamp(cReader) + 	"_.sh");
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(script));
				
				String prelimString = cReader.getAProperty(ConfigReader.BLAST_PRELIMINARY_STRING);
				
				if( prelimString != null)
					writer.write(prelimString + "\n");
				
				writer.write(blastBinDin + "/makeblastdb -dbtype nucl " + 
								"-in " + fastaFile.getAbsolutePath() + "\n");
				
				File touchFile = new File(script.getAbsolutePath() + FINISHED_SUFFIX );
				
				if( touchFile.exists())
					touchFile.delete();
				
				writer.write("touch " + touchFile.getAbsolutePath() + "\n");
				
				writer.flush();  writer.close();
				this.scripts.add(script);
				
				allWriter.write(clusterBatchCommand + " " + script.getAbsolutePath() + "\n");
				allWriter.flush();
				
				index++;
			}
		}
		
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
