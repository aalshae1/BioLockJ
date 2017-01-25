package bioLockJ.genomeFeatures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;
import bioLockJ.gwas.WriteKmerInclusionFile;
import bitManipulations.Encode;
import parsers.FastaSequence;
import utils.Translate;

public class WriteConservedKMersForReference extends BioLockJExecutor
{

	@Override
	public void checkDependencies() throws Exception
	{
		BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.REFERENCE_GENOME);
		BioLockJUtils.requireString(getConfig(), ConfigReader.KMER_TO_HAS_GENOME_FILE);
		BioLockJUtils.requireString(getConfig(), ConfigReader.CONSERVED_KMER_FOR_REFERENCE_OUPUT_FILE);
	}
	
	@Override
	public void executeProjectFile() throws Exception
	{
		File refGenome = BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.REFERENCE_GENOME);
		File kmerFile = BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.KMER_TO_HAS_GENOME_FILE);
		File outFile = new File(
				BioLockJUtils.requireString(getConfig(), ConfigReader.CONSERVED_KMER_FOR_REFERENCE_OUPUT_FILE));
		
		HashMap<Long, Float> conservationMap = getConservationMap(kmerFile);
		
		List<FastaSequence> list = FastaSequence.readFastaFile(refGenome);
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

		log.info("Writing results: " + outFile.getAbsolutePath());
		writer.write("Chromosome\tStart\tEnd\tFeature\tratioConsereved\n");
		
		for(FastaSequence fs : list)
		{
			String seq = fs.getSequence().toUpperCase();
			String chr = fs.getFirstTokenOfHeader();
			
			for( int x=0; x < seq.length()- WriteKmerInclusionFile.KMER_SIZE-1; x++)
			{
				String subSeq = seq.substring(x, x + WriteKmerInclusionFile.KMER_SIZE);
				writeIfPresent(writer, x, subSeq, conservationMap, chr);
				subSeq = Translate.safeReverseTranscribe(subSeq);
				writeIfPresent(writer, x, subSeq, conservationMap, chr);
			}
		}
		
		writer.flush();  writer.close();
	
	}
	
	private static void writeIfPresent(BufferedWriter writer, int startPos, String aString,
				HashMap<Long, Float> map, String chr) throws Exception
	{
		Long aVal = Encode.makeLong(aString);
		Float ratioPresent = map.get(aVal);
		
		if( ratioPresent != null)
		{
			writer.write(chr + "\t");
			writer.write( (startPos + 1) + "\t");
			writer.write(( startPos + 1 + WriteKmerInclusionFile.KMER_SIZE) + "\t");
			writer.write("num\t");
			writer.write(ratioPresent+ "\n");
		}
		
	}
	
	public static HashMap<Long, Float> getConservationMap(File inKmerFile) throws Exception
	{
		HashMap<Long, Float> map = new HashMap<Long,Float>();
		
		BufferedReader reader = new BufferedReader(new FileReader(inKmerFile));
		
		for(String s = reader.readLine(); s!=null; s = reader.readLine())
		{
			String[] splits = s.split("\t");
			
			if( splits.length != 2)
				throw new Exception("Parsing error");
			
			float numOnes = 0;
			String bitMap = splits[1];
			
			for( int x=0; x < bitMap.length(); x++)
				if( bitMap.charAt(x) == '1' )
					numOnes++;
			
			map.put(Long.parseLong(splits[0]), numOnes/bitMap.length());
			
			if( map.size() %1000000 == 0)
				log.info(String.valueOf(map.size()));
		}
		
		return map;
		
	}
}
