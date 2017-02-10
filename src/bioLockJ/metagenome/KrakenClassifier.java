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
package bioLockJ.metagenome;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;
import bioLockJ.ScriptBuilder;

/**
 * 
 * Use this to have multiple kraken jobs per core
 */
public class KrakenClassifier extends BioLockJExecutor
{

	@Override
	public void checkDependencies( ) throws Exception
	{
		BioLockJUtils.requireExistingDirectory( getConfig(), ConfigReader.PATH_TO_INPUT_DIRECTORY );
		BioLockJUtils.requireExistingFile( getConfig(), ConfigReader.PATH_TO_KRAKEN_BINARY );
		BioLockJUtils.requireExistingFile( getConfig(), ConfigReader.PATH_TO_KRAKEN_DATABASE );
		BioLockJUtils.requirePositiveInteger( getConfig(), ConfigReader.NUMBER_OF_JOBS_PER_CORE );
	}

	@Override
	public void executeProjectFile( ) throws Exception
	{

		File fastaInDir = BioLockJUtils.requireExistingDirectory( getConfig(), ConfigReader.PATH_TO_INPUT_DIRECTORY );
		File krakenBinary = BioLockJUtils.requireExistingFile( getConfig(), ConfigReader.PATH_TO_KRAKEN_BINARY );
		File krakenDatabase = BioLockJUtils.requireExistingFile( getConfig(), ConfigReader.PATH_TO_KRAKEN_DATABASE );
		boolean mpaFormat = BioLockJUtils.getBoolean( getConfig(), ConfigReader.MPA_FORMAT, false );

		
		
		String[] files = BioLockJUtils.getFilePaths( fastaInDir );
		log.debug( "Number of valid  files found: " + files.length );
		BioLockJUtils.logVersion( krakenBinary.getAbsolutePath() );
		setInputDir( fastaInDir );

		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		for( String file : files )
		{

			String inputFile = fastaInDir.getAbsolutePath() + File.separator + file;
			String krakenOutput = getOutputDir().getAbsolutePath() + File.separator + file + "_toKraken.txt";
			String krakenTranslate = getOutputDir().getAbsolutePath() + File.separator + file
					+ "_toKrakenTranslate.txt";

			String firstLine = krakenBinary.getAbsolutePath() + " --db " + krakenDatabase.getAbsolutePath()
					+ " --output " + krakenOutput + getKrakenSwitches() + inputFile;

			String nextLine = krakenBinary.getAbsolutePath() + "-translate --db " + krakenDatabase.getAbsolutePath()
					+ (mpaFormat ? " --mpa-format " : " ") + krakenOutput + " > " + krakenTranslate;

			ArrayList<String> lines = new ArrayList<String>( 2 );
			lines.add( firstLine );
			lines.add( nextLine );
			data.add( lines );
		}

		ScriptBuilder.buildScripts( this, data, files );
	}

	protected String getKrakenSwitches( ) throws Exception
	{
		String formattedSwitches = " ";
		ArrayList<String> switches = getConfig().getPropertyAsList( ConfigReader.KRAKEN_SWITCHES );
		Iterator<String> it = switches.iterator();
		while( it.hasNext() )
		{
			formattedSwitches += "--" + it.next() + " ";
		}

		return formattedSwitches;
	}
}