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

import java.io.File;
import java.util.ArrayList;
import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;
import bioLockJ.ScriptBuilder;

public class FormatMultipleBlastDatabases extends BioLockJExecutor
{
	/*
	 * Takes in FASTA_DIR_TO_FORMAT which should only contain fasta files
	 * (sub-directories are allowed but will be ignored)
	 * 
	 * Writes scripts to SCRIPTS_DIR/SCRIPTS_DIR_FOR_BLAST_FORMAT
	 * 
	 * will issue BLAST_PRELIMINARY_STRING if defined
	 * requires BLAST_BIN_DIR to be defined
	 * 
	 * CLUSTER_BATCH_COMMAND must be defined (e.g. qsub -q "viper" ) where viper is the name of the cluster
	 */

	@Override
	public void checkDependencies( ) throws Exception
	{
		BioLockJUtils.requireString( getConfig(), ConfigReader.BLAST_BINARY_DIR );
		BioLockJUtils.requireExistingDirectory( getConfig(), ConfigReader.FASTA_DIR_TO_FORMAT );
	}

	@Override
	public void executeProjectFile( ) throws Exception
	{
		String blastBinDir = BioLockJUtils.requireString( getConfig(), ConfigReader.BLAST_BINARY_DIR );
		File fastaDirToFormat = BioLockJUtils.requireExistingDirectory( getConfig(), ConfigReader.FASTA_DIR_TO_FORMAT );

		String prelimString = getConfig().getAProperty( ConfigReader.BLAST_PRELIMINARY_STRING );

		String[] files = BioLockJUtils.getFilePaths( fastaDirToFormat );
		log.debug( "Number of valid  files found: " + files.length );
		setInputDir( fastaDirToFormat );

		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		for( String file : files )
		{
			ArrayList<String> lines = new ArrayList<String>();
			if( prelimString != null )
				lines.add( prelimString );

			lines.add( blastBinDir + "/makeblastdb -dbtype nucl -in " + file );
			data.add( lines );
		}

		ScriptBuilder.buildScripts( this, data, files );
	}

}
