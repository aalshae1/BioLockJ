package homologySearch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.HashMap;

import bioLockJ.BioJLockUtils;
import bioLockJ.BioLockJExecutor;
import parsers.FastaSequence;
import parsers.FastaSequenceOneAtATime;
import utils.ConfigReader;

/*
 * Takes in FASTA_TO_SPLIT_PATH
 * Writes out to SPLIT_FASTA_DIR
 * Splits into NUMBER_CLUSTERS individual files
 */
public class BreakUpFastaSequence extends BioLockJExecutor
{
	public static final String NEW_SUFFIX = "PART.fasta";
	
	public static void main(String[] args) throws Exception
	{
		if( args.length != 1)
		{
			System.out.println("Usage " + BreakUpFastaSequence.class.getName() + " pathToPropertyFile" );
			System.exit(1);
		}
		
		File propFile = BioJLockUtils.findProperyFile(args);
		new BreakUpFastaSequence().executeProjectFile(propFile);
	}
	
	private static void breakUpSequences( File inFile, File outFile, int numClusters) throws Exception
	{
		HashMap<Integer, BufferedWriter> writers= new HashMap<Integer, BufferedWriter>();
		
		for( int x=0; x < numClusters; x++)
		{
			BufferedWriter writer=  new BufferedWriter(new FileWriter(new File(
				outFile.getAbsolutePath() + File.separator + inFile.getName().replace("fasta", "").
				replace("FASTA", "").replaceAll(" ", "_") + "_" + x + "_" + NEW_SUFFIX)));
			
			writers.put(x, writer);
		}
		
		FastaSequenceOneAtATime fsoat = new FastaSequenceOneAtATime(inFile);
		
		int index =0;
		for( FastaSequence fs = fsoat.getNextSequence(); fs != null; fs = fsoat.getNextSequence())
		{
			
			BufferedWriter writer = writers.get(index);
			
			writer.write(fs.getHeader() + "\n");
			writer.write(fs.getSequence() + "\n");
			
			index++;

			if( index == numClusters)
				index = 0;
		}
		
		for(BufferedWriter writer : writers.values())
		{
			writer.flush();  writer.close();
		}
		
	}
	
	@Override
	public void executeProjectFile(File projectFile) throws Exception
	{
		ConfigReader cReader = new ConfigReader(projectFile);
		File outputDir= 
				BioJLockUtils.requireExistingDirectory( cReader, ConfigReader.SPLIT_FASTA_DIR);
		File fileToParse = 
				BioJLockUtils.requireExistingFile(cReader, ConfigReader.FASTA_TO_SPLIT_PATH);
		int numChunks = BioJLockUtils.requirePositiveInteger(cReader, ConfigReader.NUMBER_CLUSTERS);

		File logDir = BioJLockUtils.createLogDirectory(
				outputDir, BreakUpFastaSequence.class.getSimpleName());
		BioJLockUtils.copyPropertiesFile(projectFile, logDir);
		
		BufferedWriter logWriter = new BufferedWriter(new FileWriter(new File(
			logDir.getAbsolutePath() + File.separator + BreakUpFastaSequence.class.getSimpleName() 
			 +"log.txt")));
		
		logWriter.write("starting " + new Date().toString() + "\n");  logWriter.flush();
		
		try
		{
			breakUpSequences(fileToParse, outputDir, numChunks);
		}
		catch(Exception ex)
		{
			BioJLockUtils.logAndRethrow(logWriter, ex);
		}
		
		logWriter.write("successful completion at " + new Date().toString() + "\n"); 
		logWriter.flush(); logWriter.close();
		BioJLockUtils.appendSuccessToPropertyFile(projectFile, this.getClass().getName(), logDir);
	}
}
