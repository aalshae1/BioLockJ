package bioLockJ.gwas;

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
import bitManipulations.Encode;
import parsers.FastaSequence;
import utils.Translate;

public class PivotPValuesToReferenceGenome extends BioLockJExecutor
{
	@Override
	public void checkDependencies() throws Exception
	{
		BioLockJUtils.requireString(getConfig(), ConfigReader.FISHER_PVALUES_OUTPUT_FILE);
		BioLockJUtils.requireString(getConfig(), ConfigReader.FISHER_GTF_OUTPUT_FILE);
		BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.REFERENCE_GENOME);	
	}
	
	public HashMap<Long, Double> getPValueMap( File inFile ) throws Exception
	{
		HashMap<Long, Double> map = new HashMap<Long,Double>();
		
		BufferedReader reader = new BufferedReader(new FileReader(inFile));
		
		reader.readLine();
		
		for(String s = reader.readLine(); s != null; s= reader.readLine())
		{
			String[] splits = s.split("\t");
			map.put(Long.parseLong(splits[0]),  Double.parseDouble(splits[5]));
		}
		
		reader.close();
		
		return map;
	}
	
	@Override
	public void executeProjectFile() throws Exception
	{
		File inFile =  BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.FISHER_PVALUES_OUTPUT_FILE);
		File outFile = new File( BioLockJUtils.requireString(getConfig(), ConfigReader.FISHER_GTF_OUTPUT_FILE));
		File refGenome =  BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.REFERENCE_GENOME);	
		
		HashMap<Long, Double> pValueMap = getPValueMap(inFile);
		
		BufferedWriter writer =new BufferedWriter(new FileWriter(outFile));
			
		writer.write("CHR\tBP\tSNP\tP\n");
		
		List<FastaSequence> list = FastaSequence.readFastaFile(refGenome);
		
		int index=0;
		for( FastaSequence fs : list)
		{
				String seq = fs.getSequence();
				String chr = fs.getFirstTokenOfHeader();
				
				for( int x=0; x < seq.length() - WriteKmerInclusionFile.KMER_SIZE - 1; x++)
				{
					String kmer = seq.substring(x, x + WriteKmerInclusionFile.KMER_SIZE);
					Long val = Encode.makeLong(kmer);
					
					if( val != null)
					{
						Double pValue = pValueMap.get(val);
						
						if( pValue != null)
						{
							writeOne(writer, chr, x, index, pValue);
							index++;
						}
					}
					
					kmer = Translate.safeReverseTranscribe(kmer);
					val = Encode.makeLong(kmer);
					
					if( val != null)
					{
						Double pValue = pValueMap.get(val);
						
						if( pValue != null)
						{
							writeOne(writer, chr, x, index, pValue);
							index++;
						}
					}
				}
			}
			
		
		writer.flush();  writer.close();
	}
	
	private static void writeOne(BufferedWriter writer, String chr, int position, int index,
			double pValue
	) throws Exception
	{
		writer.write(chr + "\t");
		writer.write( position + "\t");
		writer.write( "SNP_"  + index+ "\t"  );
		writer.write( pValue + "\n");
		writer.flush();
	}
}
