/** 
 * @UNCC Fodor Lab
 * @author Michael Sioda
 * @email msioda@uncc.edu 
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
package homologySearch.blast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;
import parsers.HitScores;

/**
 * Gathers the top hits in PATH_TO_OUTPUT_DIR.
 * There should be other files in PATH_TO_OUTPUT_DIR (sub-directories are ignored)
 * and writes them all to BLAST_GATHERED_TOP_HITS_FILE
 * 
 * if GTF_GATHERED_TOP_HITS_FILE is defined, then a GTF file is written to that path
 */
public class GatherBlastHits extends BioLockJExecutor
{
	private List<HitScores> getHits( File blastOutputDir ) throws Exception
	{
		List<HitScores> list = new ArrayList<HitScores>();

		for( String s : blastOutputDir.list() )
		{
			File f = new File( blastOutputDir.getAbsolutePath() + File.separator + s );

			if( !f.isDirectory() )
			{
				HashMap<String, HitScores> map = HitScores.getTopHitsAsQueryMap( f.getAbsolutePath() );
				list.addAll( map.values() );
			}
		}

		return list;
	}

	private void writeGTFFile( List<HitScores> list, File outFile, boolean useQueryCoordinates ) throws Exception
	{
		BufferedWriter writer = new BufferedWriter( new FileWriter( outFile ) );

		for( HitScores hs : list )
		{
			writer.write( hs.getTargetId() + "\tblast\t" + hs.getQueryId() + "\t" );

			if( !useQueryCoordinates )
				writer.write( hs.getTargetStart() + "\t" + hs.getTargetEnd() + "\t" );
			else
				writer.write( hs.getQueryStart() + "\t" + hs.getQueryEnd() + "\t" );

			writer.write( hs.getBitScore() + "\t" + "+" + "\t" + "+" + "0\taGene\taGene\n" );
		}

		writer.flush();
		writer.close();
	}

	private void writeResults( List<HitScores> list, File outFile ) throws Exception
	{
		BufferedWriter writer = new BufferedWriter( new FileWriter( outFile ) );

		writer.write( "queryID\ttargetID\ttargetStart\ttargetEnd\tqueryStart\tqueryEnd\tbitScore\teScore\n" );

		for( HitScores hs : list )
		{
			writer.write( hs.getQueryId() + "\t" + hs.getTargetId() + "\t" + hs.getTargetStart() + "\t"
					+ hs.getTargetEnd() + "\t" + hs.getQueryStart() + "\t" + hs.getQueryEnd() + "\t" + hs.getBitScore()
					+ "\t" + hs.getEScore() + "\n" );
		}

		writer.flush();
		writer.close();
	}

	@Override
	public void checkDependencies( ) throws Exception
	{
		BioLockJUtils.requireString( getConfig(), ConfigReader.BLAST_GATHERED_TOP_HITS_FILE );
		BioLockJUtils.getBoolean( getConfig(), ConfigReader.OUTPUT_QUERY_COORDINATES_TO_GTF, true );
	}

	public void executeProjectFile( ) throws Exception
	{

		File topHitsFile = new File(
				BioLockJUtils.requireString( getConfig(), ConfigReader.BLAST_GATHERED_TOP_HITS_FILE ) );
		boolean useQueryCoordiantes = BioLockJUtils.getBoolean( getConfig(),
				ConfigReader.OUTPUT_QUERY_COORDINATES_TO_GTF, true );

		List<HitScores> hits = getHits( getOutputDir() );
		writeResults( hits, topHitsFile );

		if( getConfig().getAProperty( ConfigReader.GTF_GATHERED_TOP_HITS_FILE ) != null )
		{
			writeGTFFile( hits, new File( getConfig().getAProperty( ConfigReader.GTF_GATHERED_TOP_HITS_FILE ) ),
					useQueryCoordiantes );
		}
		else
		{
			log.info( ConfigReader.GTF_GATHERED_TOP_HITS_FILE + " not defined so skipping GTF\n" );
		}
	}

}
