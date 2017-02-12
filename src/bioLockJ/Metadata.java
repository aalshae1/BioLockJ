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

import java.io.File;
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

	static final char SP = ' ';
	static final char PIPE = '|';
	static final char COMMA = ',';
	static final char BACKSLASH = '\\';
	static final char TAB = '\t';

	private HashMap<String, ArrayList<String>> metadataMap;
	private HashMap<String, String> descriptorMap;
	private ArrayList<String> attributeNames;
	private String metadataPath;
	private String descriptorPath;

	public Metadata( ArrayList<ArrayList<String>> metadata, String metadataFileName,
			ArrayList<ArrayList<String>> descriptor, String descriptorFileName )
	{
		metadataPath = metadataFileName;
		descriptorPath = descriptorFileName;
		init( metadata, descriptor );
	}

	public static Metadata loadMetadata( ConfigReader cReader )
	{
		String metadata = null;
		String commentChar = null;
		String delim = null;
		String descriptor = null;
		String nullChar = null;
		char delimChar = TAB;

		try
		{
			metadata = getFilePath( cReader, ConfigReader.METADATA_FILE );
			descriptor = getFilePath( cReader, ConfigReader.METADATA_DESCRIPTOR );
			commentChar = cReader.getAProperty( ConfigReader.METADATA_COMMENT );
			delim = cReader.getAProperty( ConfigReader.METADATA_DELIMITER );
			nullChar = cReader.getAProperty( ConfigReader.METADATA_NULL_VALUE );

			boolean delimNotFound = true;
			if( delim != null && delim.trim().length() == 1 )
			{
				delimChar = delim.charAt( 0 );
				delimNotFound = false;
			}
			else if( delim != null && delim.trim().length() == 2 )
			{
				if( delim.startsWith( "\\" ) )
				{
					if( delim.endsWith( "\\" ) )
					{
						delimChar = BACKSLASH;
						delimNotFound = false;
					}
					else if( delim.endsWith( "t" ) )
					{
						delimChar = TAB;
						delimNotFound = false;
					}
				}
			}

			if( delimNotFound )
			{
				throw new Exception( "Metadata delimiter not found: " + delim );
			}
		}
		catch( Exception ex )
		{
			log.error( ex.getMessage(), ex );
			return null;
		}

		if( metadata == null || metadata.trim().length() < 1 )
		{
			log.info( "Metadata file not configured: " + metadata );
			return null;
		}

		if( descriptor == null || descriptor.trim().length() < 1 )
		{
			log.info( "Metadata descriptor file not configured: " + descriptor );
			return null;
		}

		log.info( BioLockJUtils.LOG_SPACER );
		log.info( "Loading metadata file: " + metadata );
		log.info( "Metadata commentChar: " + commentChar );
		log.info( "Metadata delimeter: " + String.valueOf( delimChar ) );
		log.info( "Metadata descriptor: " + descriptor );
		log.info( "Metadata nullChar: " + nullChar );
		log.info( BioLockJUtils.LOG_SPACER );

		return new Metadata( processFile( metadata, delimChar ), metadata, processFile( descriptor, delimChar ),
				descriptor );
	}

	public static ArrayList<ArrayList<String>> processFile( String fileName, char delimChar )
	{
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		FileReader fileReader = null;
		CSVParser csvFileParser = null;
		try
		{
			fileReader = new FileReader( fileName );
			csvFileParser = new CSVParser( fileReader, CSVFormat.DEFAULT.withDelimiter( delimChar ) );

			Iterator<CSVRecord> csvRecords = csvFileParser.getRecords().iterator();
			while( csvRecords.hasNext() )
			{
				ArrayList<String> record = new ArrayList<String>();
				CSVRecord csvRow = csvRecords.next();
				csvRow.getRecordNumber();
				Iterator<String> it = csvRow.iterator();
				while( it.hasNext() )
				{
					record.add( it.next() );
				}

				data.add( record );
			}
		}
		catch( Exception ex )
		{
			log.error( "Error in CsvFileReader  !!!", ex );
			return null;
		}
		finally
		{
			try
			{
				fileReader.close();
				csvFileParser.close();
			}
			catch( IOException ex )
			{
				log.error( "Error while closing fileReader/csvFileParser !!!", ex );
			}
		}

		return data;
	}

	public static String getFilePath( ConfigReader cReader, String propName ) throws Exception
	{
		String prop = cReader.getAProperty( propName );
		if( prop == null || prop.length() < 1 )
		{
			return null;
		}

		return cReader.getResourcesDir() + "metadata" + File.separator + prop;
	}

	public String getMetadataPath( )
	{
		return metadataPath;
	}

	public String getDescriptorPath( )
	{
		return descriptorPath;
	}

	public ArrayList<String> getAttributeNames( )
	{
		return attributeNames;
	}

	public Set<String> getFileNames( )
	{
		return metadataMap.keySet();
	}

	public ArrayList<String> getAttributes( String fileName )
	{
		return metadataMap.get( fileName );
	}

	public String getAttributeDescriptor( String attribute )
	{
		return descriptorMap.get( attribute );
	}

	public String getAttribute( String fileName, String attribute )
	{
		return metadataMap.get( fileName ).get( attributeNames.indexOf( attribute ) );
	}

	protected void init( ArrayList<ArrayList<String>> metadata, ArrayList<ArrayList<String>> descriptor )
	{
		processDescriptor( descriptor );
		processMetadata( metadata );
	}

	protected void processDescriptor( ArrayList<ArrayList<String>> descriptor )
	{
		log.info( "Number of descriptor rows (including header): " + descriptor.size() );
		descriptorMap = new HashMap<String, String>();
		boolean firstRow = true;
		int rowNum = 0;
		Iterator<ArrayList<String>> rows = descriptor.iterator();
		while( rows.hasNext() )
		{
			ArrayList<String> row = getFormattedRow( rows.next() );
			if( firstRow )
			{
				firstRow = false;
				log.info( "Descriptor Column Names: " + row );
			}
			else
			{
				log.info( "Row[" + BioLockJUtils.formatInt( rowNum++, 2 ) + "], key[" + row.get( 0 ) + "], values"
						+ row );
				descriptorMap.put( row.get( 0 ), row.get( 1 ) );
			}
		}
	}

	protected void processMetadata( ArrayList<ArrayList<String>> metadata )
	{
		log.info( "Number of metadata rows (including header): " + metadata.size() );
		metadataMap = new HashMap<String, ArrayList<String>>();
		boolean firstRow = true;
		int rowNum = 0;
		Iterator<ArrayList<String>> rows = metadata.iterator();
		while( rows.hasNext() )
		{
			ArrayList<String> row = getFormattedRow( rows.next() );
			String name = row.get( 0 );
			row.remove( 0 );
			if( firstRow )
			{
				firstRow = false;
				attributeNames = row;
				log.info( "Metadata Attributes: " + row );
			}
			else
			{
				log.info( "Row[" + BioLockJUtils.formatInt( rowNum++, 3 ) + "], key[" + name + "], #atts[" + row.size()
						+ "], values" + row );
				metadataMap.put( name, row );
			}
		}
	}

	private ArrayList<String> getFormattedRow( ArrayList<String> row )
	{
		ArrayList<String> formattedRow = new ArrayList<String>();
		Iterator<String> it = row.iterator();
		while( it.hasNext() )
		{
			formattedRow.add( it.next().trim() );
		}

		return formattedRow;
	}
}