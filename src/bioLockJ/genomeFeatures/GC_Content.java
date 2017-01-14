package bioLockJ.genomeFeatures;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import parsers.FastaSequence;
import utils.ConfigReader;

public class GC_Content extends BioLockJExecutor
{
	@Override
	public void checkDependencies(ConfigReader cReader) throws Exception
	{
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.REFERENCE_GENOME);
		BioLockJUtils.requireString(cReader, ConfigReader.GC_CONTENT_IGV_OUTPUT_FILE);
		BioLockJUtils.requirePositiveInteger(cReader, ConfigReader.GC_CONTENT_WINDOW_SIZE);
		BioLockJUtils.requirePositiveInteger(cReader, ConfigReader.GC_CONTENT_STEP_SIZE);
	}
	
	private float getGCContent(String s )
	{
		double num=0;
		double gc =0;
		
		for( int x=0;x  < s.length(); x++)
		{
			char c = s.charAt(x);
			
			if( c == 'A' || c == 'C' || c == 'G' || c == 'T')
			{
				num++;
				
				if( c == 'C' || c == 'G')
					gc++;
			}
		}
		
		if ( num ==0)
			return 0;
		
		return (float) (gc / num);
	}
	
	@Override
	public void executeProjectFile(ConfigReader cReader) throws Exception
	{
		File referenceGenome = BioLockJUtils.requireExistingFile(cReader, ConfigReader.REFERENCE_GENOME);
		File outputFile =new File( BioLockJUtils.requireString(cReader, ConfigReader.GC_CONTENT_IGV_OUTPUT_FILE));
		int windowSize =
				BioLockJUtils.requirePositiveInteger(cReader, ConfigReader.GC_CONTENT_WINDOW_SIZE);
		int stepSize = 
				BioLockJUtils.requirePositiveInteger(cReader, ConfigReader.GC_CONTENT_STEP_SIZE);
		
		List<FastaSequence> list = FastaSequence.readFastaFile(referenceGenome);
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		writer.write("Chromosome\tStart\tEnd\tFeature\tgcContent\n");
		
		for(FastaSequence fs : list)
		{
			String seq = fs.getSequence().toUpperCase();
			String chr = fs.getFirstTokenOfHeader();
			
			for( int x=0; x < seq.length() - windowSize -1 ; x += stepSize)
			{
				String subSeq = seq.substring(x, x + windowSize );
				writer.write(chr + "\t");
				writer.write( (x + 1) + "\t");
				writer.write(( x + windowSize ) + "\t");
				writer.write("gc\t");
				writer.write(getGCContent(subSeq) + "\n");
			}
		}
		
		writer.flush();  writer.close();
	}
}
