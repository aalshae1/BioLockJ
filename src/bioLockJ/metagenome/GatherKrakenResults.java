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
package bioLockJ.metagenome;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import bioLockJ.BioLockJExecutor;

public class GatherKrakenResults extends BioLockJExecutor
{
	public static final String[] KRAKEN_TAXONOMY = { "domain", "phylum", "class", "order", "family", "genus",
			"species" };

	public static final String THREE_COL_SUFFIX = "_SparseThreeCol.txt";

	@Override //as required by abstract class
	public void checkDependencies( ) throws Exception
	{
	}

	@Override
	public void executeProjectFile( ) throws Exception
	{
		for( int x = 0; x < KRAKEN_TAXONOMY.length; x++ )
		{
			HashMap<String, HashMap<String, Integer>> map = getAllSamples( x + 2 );
			File summaryFile = new File(
					getOutputDir().getAbsolutePath() + File.separator + "kraken_" + KRAKEN_TAXONOMY[x] + ".txt" );
			GatherRDPResults.writeResults( map, summaryFile.getAbsolutePath() );
		}
	}

	private HashMap<String, HashMap<String, Integer>> getAllSamples( int parseLevel ) throws Exception
	{
		HashMap<String, HashMap<String, Integer>> map = new HashMap<String, HashMap<String, Integer>>();
		setInputFiles( getInputDir() );
		ArrayList<File> inputFiles = getInputFiles();
		for( File file : inputFiles )
		{
			if( file.getName().endsWith( "toKrakenTranslate.txt" ) )
			{
				File inFile = new File( file.getAbsolutePath() );
				HashMap<String, Integer> innerMap = getCounts( inFile, parseLevel );

				long sum = 0;

				for( Integer i : innerMap.values() )
					sum += i;

				if( sum > 0 )
					map.put( inFile.getName().replace( "toKrakenTranslate.txt", "" ), innerMap );
			}
		}

		return map;
	}

	private static HashMap<String, Integer> getCounts( File inFile, int parseNum ) throws Exception
	{
		log.info( "GatherKrakenResults.getCounts from file: " + inFile.getAbsolutePath() );
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader( new FileReader( inFile ) );
		for( String s = reader.readLine(); s != null; s = reader.readLine() )
		{
			StringTokenizer sToken = new StringTokenizer( s, "\t" );
			sToken.nextToken();

			String[] splits = sToken.nextToken().split( ";" );

			if( splits.length - 1 >= parseNum )
			{
				Integer val = map.get( splits[parseNum] );

				if( val == null )
					val = 0;

				val++;

				map.put( new String( splits[parseNum] ), val );
			}
		}

		reader.close();

		return map;
	}
}
