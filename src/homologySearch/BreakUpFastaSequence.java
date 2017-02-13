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
package homologySearch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;
import parsers.FastaSequence;
import parsers.FastaSequenceOneAtATime;

/*
 * Takes in FASTA_TO_SPLIT_PATH
 * Writes out to SPLIT_FASTA_DIR
 * Splits into NUMBER_OF_JOBS_PER_CORE individual files
 */
public class BreakUpFastaSequence extends BioLockJExecutor
{
	public static final String NEW_SUFFIX = "PART.fasta";

	private static void breakUpSequences( File inFile, File outFile, int numClusters ) throws Exception
	{
		HashMap<Integer, BufferedWriter> writers = new HashMap<Integer, BufferedWriter>();

		for( int x = 0; x < numClusters; x++ )
		{
			BufferedWriter writer = new BufferedWriter(
					new FileWriter( new File( outFile.getAbsolutePath() + File.separator
							+ inFile.getName().replace( "fasta", "" ).replace( "FASTA", "" ).replaceAll( " ", "_" )
							+ "_" + x + "_" + NEW_SUFFIX ) ) );

			writers.put( x, writer );
		}

		FastaSequenceOneAtATime fsoat = new FastaSequenceOneAtATime( inFile );

		int index = 0;
		for( FastaSequence fs = fsoat.getNextSequence(); fs != null; fs = fsoat.getNextSequence() )
		{

			BufferedWriter writer = writers.get( index );

			writer.write( fs.getHeader() + "\n" );
			writer.write( fs.getSequence() + "\n" );

			if( ++index == numClusters )
				index = 0;
		}

		for( BufferedWriter writer: writers.values() )
		{
			writer.flush();
			writer.close();
		}

	}

	@Override
	public void checkDependencies() throws Exception
	{
		BioLockJUtils.requireExistingDirectory( getConfig(), ConfigReader.SPLIT_FASTA_DIR );
		BioLockJUtils.requireExistingFile( getConfig(), ConfigReader.FASTA_TO_SPLIT_PATH );
		BioLockJUtils.requirePositiveInteger( getConfig(), ConfigReader.NUMBER_OF_JOBS_PER_CORE );
	}

	@Override
	public void executeProjectFile() throws Exception
	{
		File outputDir = BioLockJUtils.requireExistingDirectory( getConfig(), ConfigReader.SPLIT_FASTA_DIR );
		File fileToParse = BioLockJUtils.requireExistingFile( getConfig(), ConfigReader.FASTA_TO_SPLIT_PATH );
		int numChunks = BioLockJUtils.requirePositiveInteger( getConfig(), ConfigReader.NUMBER_OF_JOBS_PER_CORE );

		breakUpSequences( fileToParse, outputDir, numChunks );
	}
}
