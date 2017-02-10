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

public class FormatSingleBlastDatabase extends BioLockJExecutor
{
	/**
	 * Takes in FASTA_FILE_TO_FORMAT_FOR_BLAST_DB
	 * Writes a single script to SCRIPTS_DIR/SCRIPTS_DIR_FOR_BLAST_FORMAT
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
		BioLockJUtils.requireExistingFile( getConfig(), ConfigReader.FASTA_FILE_TO_FORMAT_FOR_BLAST_DB );
		BioLockJUtils.requireDBType( getConfig() );
	}

	@Override
	public void executeProjectFile( ) throws Exception
	{
		String blastBinDin = BioLockJUtils.requireString( getConfig(), ConfigReader.BLAST_BINARY_DIR );
		File fastaFileToFormat = BioLockJUtils.requireExistingFile( getConfig(),
				ConfigReader.FASTA_FILE_TO_FORMAT_FOR_BLAST_DB );

		String dbType = BioLockJUtils.requireDBType( getConfig() );

		//String[] files = BioLockJUtils.getFilePaths(fastaFileToFormat);
		//log.debug("Number of valid  files found: " + files.length);
		//setInputDir(fastaFileToFormat);

		String prelimString = getConfig().getAProperty( ConfigReader.BLAST_PRELIMINARY_STRING );

		ArrayList<String> lines = new ArrayList<String>();
		if( prelimString != null )
			lines.add( prelimString );

		lines.add( blastBinDin + "/makeblastdb -dbtype " + dbType + " -in " + fastaFileToFormat.getAbsolutePath() );

		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		data.add( lines );

		String[] files = new String[ 1 ];
		files[0] = fastaFileToFormat.getName();

		ScriptBuilder.buildScripts( this, data, files );
	}

}
