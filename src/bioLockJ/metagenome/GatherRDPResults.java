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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;
import bioLockJ.BashScriptBuilder;
import parsers.NewRDPNode;
import parsers.NewRDPParserFileLine;
import parsers.OtuWrapper;

public class GatherRDPResults extends BioLockJExecutor
{
	public static final String THREE_COL_SUFFIX = "_SparseThreeCol.txt";

	@Override
	public void checkDependencies( ) throws Exception
	{
		BioLockJUtils.requirePositiveInteger( getConfig(), ConfigReader.RDP_THRESHOLD );
	}

	@Override
	public void executeProjectFile( ) throws Exception
	{
		int rdpThreshold = BioLockJUtils.requirePositiveInteger( getConfig(), ConfigReader.RDP_THRESHOLD );

		HashMap<String, BufferedWriter> taxaWriters = new HashMap<String, BufferedWriter>();

		for( int x = 1; x < NewRDPParserFileLine.TAXA_ARRAY.length; x++ )
		{
			BufferedWriter writer = new BufferedWriter( new FileWriter( new File( getOutputDir().getAbsolutePath()
					+ File.separator + NewRDPParserFileLine.TAXA_ARRAY[x] + THREE_COL_SUFFIX ) ) );
			taxaWriters.put( NewRDPParserFileLine.TAXA_ARRAY[x], writer );
		}

		int fileCount = 0;
		for( String s : getOutputDir().list() )
		{
			log.info( "RDP OUTPUT FILE # (" + String.valueOf( fileCount++ ) + "):  " + s );
			List<NewRDPParserFileLine> list = NewRDPParserFileLine
					.getRdpListSingleThread( getOutputDir().getAbsoluteFile() + File.separator + s );

			for( int x = 1; x < NewRDPParserFileLine.TAXA_ARRAY.length; x++ )
			{
				HashMap<String, Integer> countMap = getCount( NewRDPParserFileLine.TAXA_ARRAY[x], list, rdpThreshold );

				BufferedWriter writer = taxaWriters.get( NewRDPParserFileLine.TAXA_ARRAY[x] );

				for( String key : countMap.keySet() )
				{
					if( s.contains( BashScriptBuilder.SCRIPT_SUCCEEDED ) )
					{
						writer.write( s.replaceAll( BashScriptBuilder.SCRIPT_SUCCEEDED, "" ) + "\t" + key + "\t"
								+ countMap.get( key ) + "\n" );
					}
					if( s.contains( BashScriptBuilder.SCRIPT_FAILED ) )
					{
						writer.write( s.replaceAll( BashScriptBuilder.SCRIPT_FAILED, "" ) + "\t" + key + "\t"
								+ countMap.get( key ) + "\n" );
					}

				}

				writer.flush();
			}
		}

		for( BufferedWriter writer : taxaWriters.values() )
		{
			writer.flush();
			writer.close();
		}

		for( int x = 1; x < NewRDPParserFileLine.TAXA_ARRAY.length; x++ )
		{
			File file = new File( getOutputDir().getAbsolutePath() + File.separator + NewRDPParserFileLine.TAXA_ARRAY[x]
					+ THREE_COL_SUFFIX );

			HashMap<String, HashMap<String, Integer>> map = getMapFromFile( file.getAbsolutePath() );
			File outFile = new File( getOutputDir().getAbsolutePath() + File.separator
					+ NewRDPParserFileLine.TAXA_ARRAY[x] + "_taxaAsColumns.txt" );
			writeResults( map, outFile.getAbsolutePath() );
			OtuWrapper wrapper = new OtuWrapper( outFile );
			wrapper.writeNormalizedLoggedDataToFile( getOutputDir().getAbsolutePath() + File.separator + "pivoted_"
					+ NewRDPParserFileLine.TAXA_ARRAY[x] + "asColumnsLogNormal.txt" );
		}
	}

	private static List<String> getOTUSAtThreshold( HashMap<String, HashMap<String, Integer>> map, int threshold )
	{

		HashMap<String, Integer> countMap = new HashMap<String, Integer>();

		for( String s : map.keySet() )
		{
			HashMap<String, Integer> innerMap = map.get( s );

			for( String possibleOtu : innerMap.keySet() )
			{
				Integer oldCount = countMap.get( possibleOtu );

				if( oldCount == null )
					oldCount = 0;

				oldCount += innerMap.get( possibleOtu );

				countMap.put( possibleOtu, oldCount );
			}
		}

		List<String> otuList = new ArrayList<String>();

		for( String s : countMap.keySet() )
			if( countMap.get( s ) >= threshold )
				otuList.add( s );

		return otuList;

	}

	public static void writeResults( HashMap<String, HashMap<String, Integer>> map, String filepath ) throws Exception
	{
		BufferedWriter writer = new BufferedWriter( new FileWriter( new File( filepath ) ) );

		writer.write( "sample" );
		List<String> otuList = getOTUSAtThreshold( map, 0 );
		Collections.sort( otuList );

		for( String s : otuList )
			writer.write( "\t" + s );

		writer.write( "\n" );

		List<String> samples = new ArrayList<String>();

		for( String s : map.keySet() )
			samples.add( s );

		Collections.sort( samples );

		for( String s : samples )
		{
			//String expandedString = PivotRDPs.getExpandedString( s);
			//writer.write( expandedString );
			writer.write( s );

			HashMap<String, Integer> innerMap = map.get( s );

			for( String otu : otuList )
			{
				Integer aVal = innerMap.get( otu );

				if( aVal == null )
					aVal = 0;

				writer.write( "\t" + aVal );
			}

			writer.write( "\n" );
		}

		writer.flush();
		writer.close();
	}

	private static HashMap<String, HashMap<String, Integer>> getMapFromFile( String filePath ) throws Exception
	{
		HashMap<String, HashMap<String, Integer>> map = new HashMap<String, HashMap<String, Integer>>();

		BufferedReader reader = filePath.toLowerCase().endsWith( "gz" )
				? new BufferedReader( new InputStreamReader( new GZIPInputStream( new FileInputStream( filePath ) ) ) )
				: new BufferedReader( new FileReader( new File( filePath ) ) );

		String nextLine = reader.readLine();

		int numDone = 0;
		while( nextLine != null )
		{
			StringTokenizer sToken = new StringTokenizer( nextLine, "\t" );
			String sample = sToken.nextToken().replace( ".extendedFrags.fastatoRDP.txt.gz", "" );
			String taxa = sToken.nextToken();
			int count = Integer.parseInt( sToken.nextToken() );

			if( sToken.hasMoreTokens() )
				throw new Exception( "No" );

			HashMap<String, Integer> innerMap = map.get( sample );
			if( innerMap == null )
			{
				innerMap = new HashMap<String, Integer>();
				map.put( sample, innerMap );
			}

			if( innerMap.containsKey( taxa ) )
				throw new Exception( "parsing error " + taxa );

			innerMap.put( taxa, count );
			nextLine = reader.readLine();

			if( ++numDone % 1000000 == 0 )
				log.info( "RDP numDone# " + numDone );
		}

		return map;
	}

	private static HashMap<String, Integer> getCount( String level, List<NewRDPParserFileLine> rdpList, int threshold )
	{
		HashMap<String, Integer> map = new HashMap<String, Integer>();

		for( NewRDPParserFileLine rdp : rdpList )
		{
			NewRDPNode node = rdp.getTaxaMap().get( level );

			if( node != null && node.getScore() >= threshold )
			{
				Integer count = map.get( node.getTaxaName() );

				if( count == null )
					count = 0;

				count++;

				map.put( node.getTaxaName(), count );
			}
		}

		return map;
	}
}
