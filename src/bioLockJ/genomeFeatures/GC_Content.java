/** 
 * @UNCC Fodor Lab
 * @author Anthony Fodor
 * @email anthony.fodor@gmail.com 
 * @date Feb 9, 2017
 * @disclaimer 	This code is free software; you can redistribute it and/or
 * 				modify it under the terms of the GNU General Public License
 * 				as published by the Free Software Foundation; either version 2
 * 				of the License, or (at your option) any later version,
 * 				provided that any use properly credits the author.
 * 				This program is distributed in the hope that it will be useful,
 * 				but WITHOUT ANY WARRANTY; without even the implied warranty of
 * 				MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * 				GNU General Public License for more details at http://www.gnu.org * 
 */
package bioLockJ.genomeFeatures;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;
import parsers.FastaSequence;

public class GC_Content extends BioLockJExecutor
{
	@Override
	public void checkDependencies( ) throws Exception
	{
		BioLockJUtils.requireExistingFile( getConfig(), ConfigReader.REFERENCE_GENOME );
		BioLockJUtils.requireString( getConfig(), ConfigReader.GC_CONTENT_IGV_OUTPUT_FILE );
		BioLockJUtils.requirePositiveInteger( getConfig(), ConfigReader.GC_CONTENT_WINDOW_SIZE );
		BioLockJUtils.requirePositiveInteger( getConfig(), ConfigReader.GC_CONTENT_STEP_SIZE );
	}

	private float getGCContent( String s )
	{
		double num = 0;
		double gc = 0;

		for( int x = 0; x < s.length(); x++ )
		{
			char c = s.charAt( x );

			if( c == 'A' || c == 'C' || c == 'G' || c == 'T' )
			{
				num++;

				if( c == 'C' || c == 'G' )
					gc++;
			}
		}

		if( num == 0 )
			return 0;

		return (float) ( gc / num );
	}

	@Override
	public void executeProjectFile( ) throws Exception
	{
		File referenceGenome = BioLockJUtils.requireExistingFile( getConfig(), ConfigReader.REFERENCE_GENOME );
		File outputFile = new File(
				BioLockJUtils.requireString( getConfig(), ConfigReader.GC_CONTENT_IGV_OUTPUT_FILE ) );
		int windowSize = BioLockJUtils.requirePositiveInteger( getConfig(), ConfigReader.GC_CONTENT_WINDOW_SIZE );
		int stepSize = BioLockJUtils.requirePositiveInteger( getConfig(), ConfigReader.GC_CONTENT_STEP_SIZE );

		List<FastaSequence> list = FastaSequence.readFastaFile( referenceGenome );

		BufferedWriter writer = new BufferedWriter( new FileWriter( outputFile ) );
		writer.write( "Chromosome\tStart\tEnd\tFeature\tgcContent\n" );

		for( FastaSequence fs : list )
		{
			String seq = fs.getSequence().toUpperCase();
			String chr = fs.getFirstTokenOfHeader();

			for( int x = 0; x < seq.length() - windowSize - 1; x += stepSize )
			{
				String subSeq = seq.substring( x, x + windowSize );
				writer.write( chr + "\t" );
				writer.write( ( x + 1 ) + "\t" );
				writer.write( ( x + windowSize ) + "\t" );
				writer.write( "gc\t" );
				writer.write( getGCContent( subSeq ) + "\n" );
			}
		}

		writer.flush();
		writer.close();
	}
}
