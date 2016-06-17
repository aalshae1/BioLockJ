package homologySearch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
	public void checkDependencies(ConfigReader cReader) throws Exception
	{
		BioJLockUtils.requireExistingDirectory( cReader, ConfigReader.SPLIT_FASTA_DIR);
		BioJLockUtils.requireExistingFile(cReader, ConfigReader.FASTA_TO_SPLIT_PATH);
		BioJLockUtils.requirePositiveInteger(cReader, ConfigReader.NUMBER_CLUSTERS);
	}
	
	@Override
	public void executeProjectFile(ConfigReader cReader, BufferedWriter logWriter) throws Exception
	{
		File outputDir= 
				BioJLockUtils.requireExistingDirectory( cReader, ConfigReader.SPLIT_FASTA_DIR);
		File fileToParse = 
				BioJLockUtils.requireExistingFile(cReader, ConfigReader.FASTA_TO_SPLIT_PATH);
		int numChunks = BioJLockUtils.requirePositiveInteger(cReader, ConfigReader.NUMBER_CLUSTERS);
		
		breakUpSequences(fileToParse, outputDir, numChunks);
	}
}
