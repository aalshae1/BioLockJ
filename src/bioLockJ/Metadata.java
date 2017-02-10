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
package bioLockJ;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Metadata
{

	protected static final Logger log = LoggerFactory.getLogger( Metadata.class );

	private ArrayList<String> attributeNames;
	private HashMap<String, ArrayList<String>> map;
	private String filePath;

	public Metadata( ArrayList<ArrayList<String>> csvData, String fileName )
	{
		filePath = fileName;
		init( csvData );
	}

	public static Metadata loadMetadata( String fileName )
	{

		if( fileName == null || fileName.trim().length() < 1 )
		{
			log.info( "Metadata file not configured: " + fileName );
			return null;
		}

		log.info( "Loading metadata file: " + fileName );
		FileReader fileReader = null;
		CSVParser csvFileParser = null;

		ArrayList<ArrayList<String>> csvData = new ArrayList<ArrayList<String>>();

		try
		{

			fileReader = new FileReader( fileName );
			csvFileParser = new CSVParser( fileReader, CSVFormat.TDF );

			Iterator<CSVRecord> csvRecords = csvFileParser.getRecords().iterator();

			int rowNum = 0;
			while( csvRecords.hasNext() )
			{
				ArrayList<String> record = new ArrayList<String>();
				CSVRecord csvRow = csvRecords.next();
				csvRow.getRecordNumber();
				Iterator<String> it = csvRow.iterator();
				log.debug( "Row #" + BioLockJUtils.formatInt( rowNum++, 3 ) + " :: #attributes = " + csvRow.size() );
				while( it.hasNext() )
				{
					record.add( it.next() );
				}

				csvData.add( record );
			}

			log.debug( "CSV Metadata number rows: " + ( csvData.size() - 1 ) );

		}
		catch( Exception e )
		{
			log.error( "Error in CsvFileReader  !!!", e );
			return null;
		}
		finally
		{
			try
			{
				fileReader.close();
				csvFileParser.close();
			}
			catch( IOException e )
			{
				log.error( "Error while closing fileReader/csvFileParser !!!", e );
			}
		}

		log.info( "About to return meta: " );
		return new Metadata( csvData, fileName );
	}

	public String getFilePath( )
	{
		return filePath;
	}

	public ArrayList<String> getAttributeNames( )
	{
		return attributeNames;
	}

	public Set<String> getFileNames( )
	{
		return map.keySet();
	}

	public ArrayList<String> getAttributes( String fileName )
	{
		return map.get( fileName );
	}

	public String getAttribute( String fileName, String attribute )
	{
		return map.get( fileName ).get( attributeNames.indexOf( attribute ) );
	}

	protected void init( ArrayList<ArrayList<String>> csvData )
	{
		log.debug( "Initializing Metadata object" );
		Iterator<ArrayList<String>> rows = csvData.iterator();
		map = new HashMap<String, ArrayList<String>>();
		boolean firstRow = true;
		log.debug( "Number of rows (including header): " + csvData.size() );
		while( rows.hasNext() )
		{
			ArrayList<String> row = rows.next();
			String name = row.get( 0 );
			row.remove( 0 );
			if( firstRow )
			{
				firstRow = false;
				attributeNames = row;
				log.debug( "Setting attribute names: " + row );
			}
			else
			{
				log.debug( "Add row key=" + name + " to map.  Values=" + row );
				map.put( name, row );
			}
		}
	}
}