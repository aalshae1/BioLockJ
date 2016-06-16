package homologySearch.blast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bioLockJ.BioJLockUtils;
import bioLockJ.BioLockJExecutor;
import homologySearch.BreakUpFastaSequence;
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
	public void executeProjectFile(File projectFile) throws Exception
	{
		this.scripts = new ArrayList<File>();
		ConfigReader cReader = new ConfigReader(projectFile);
		String blastBinDin = BioJLockUtils.requireString(cReader, ConfigReader.BLAST_BINARY_DIR);
		File fastaFileToFormat= BioJLockUtils.requireExistingFile(
						cReader, ConfigReader.FASTA_FILE_TO_FORMAT_FOR_BLAST_DB);
		
		File scriptDir = BioJLockUtils.requireExistingDirectory(cReader, ConfigReader.SCRIPTS_DIR_FOR_BLAST_FORMAT);
	
		File logDir = BioJLockUtils.createLogDirectory(scriptDir, BreakUpFastaSequence.class.getSimpleName());
		BioJLockUtils.copyPropertiesFile(projectFile, logDir);
		
		BufferedWriter logWriter = new BufferedWriter(new FileWriter(new File(
				logDir.getAbsolutePath() + File.separator + BreakUpFastaSequence.class.getSimpleName() 
				 +"log.txt")));
		
		String clusterBatchCommand = BioJLockUtils.requireString(cReader, ConfigReader.CLUSTER_BATCH_COMMAND);
		
		this.runAllFile = new File(scriptDir.getAbsolutePath() + File.separator + "runAll_" + 
				System.currentTimeMillis() +	".sh");
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(this.runAllFile));
		
		File script = new File(
						scriptDir.getAbsolutePath() + File.separator + "run_" + System.currentTimeMillis()  + ".sh");
		BufferedWriter writer = new BufferedWriter(new FileWriter(script));
				
		String prelimString = cReader.getAProperty(ConfigReader.BLAST_PRELIMINARY_STRING);
				
		if( prelimString != null)
			writer.write(prelimString + "\n");
				
		writer.write(blastBinDin + "/makeblastdb -dbtype nucl " + 
								"-in " + fastaFileToFormat.getAbsolutePath() + "\n");
				
		File touchFile = new File(script.getAbsolutePath() + FINISHED_SUFFIX );
				
		if( touchFile.exists())
			touchFile.delete();
				
		writer.write("touch " + touchFile.getAbsolutePath() + "\n");
				
		writer.flush();  writer.close();
		this.scripts.add(script);
				
		allWriter.write(clusterBatchCommand + " " + script.getAbsolutePath() + "\n");
		allWriter.flush();
		
		allWriter.flush();  allWriter.close();

		logWriter.write("successful completion at " + new Date().toString() + "\n"); 
		logWriter.flush(); logWriter.close();
		BioJLockUtils.appendSuccessToPropertyFile(projectFile, this.getClass().getName(), logDir);
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
	
	public static void main(String[] args) throws Exception
	{
		if( args.length != 1)
		{
			System.out.println("Usage " + FormatSingleBlastDatabase.class.getName() + " pathToPropertyFile" );
			System.exit(1);
		}
		
		File propFile = BioJLockUtils.findProperyFile(args);
		new FormatSingleBlastDatabase().executeProjectFile(propFile);
	}
	
	
}
