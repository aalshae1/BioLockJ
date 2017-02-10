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
import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;
import bioLockJ.BashScriptBuilder;

/**
 * 
 * Use this to run one core per RDP parser job
 */
public class RdpClassifier extends BioLockJExecutor
{

	@Override
	public void checkDependencies( ) throws Exception
	{
		BioLockJUtils.requireExistingDirectory( getConfig(), ConfigReader.PATH_TO_INPUT_DIRECTORY );
		BioLockJUtils.requireExistingFile( getConfig(), ConfigReader.PATH_TO_RDP_JAR );
	}

	@Override
	public void executeProjectFile( ) throws Exception
	{
		File fastaInDir = BioLockJUtils.requireExistingDirectory( getConfig(), ConfigReader.PATH_TO_INPUT_DIRECTORY );
		File rdpBinary = BioLockJUtils.requireExistingFile( getConfig(), ConfigReader.PATH_TO_RDP_JAR );

		String[] files = BioLockJUtils.getFilePaths( fastaInDir );
		log.debug( "Number of valid  files found: " + files.length );
		BioLockJUtils.logVersion( rdpBinary.getAbsolutePath() );
		setInputDir( fastaInDir );

		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		for( String file : files )
		{
			String fastaFile = fastaInDir.getAbsolutePath() + File.separator + file;
			String rdpOutFile = getOutputDir().getAbsolutePath() + File.separator + file + "toRDP.txt";

			String firstLine = "java -jar " + rdpBinary.getAbsolutePath() + " " + "-o \"" + rdpOutFile + "\" -q \""
					+ fastaFile + "\"";

			String nextLine = "gzip " + rdpOutFile;

			ArrayList<String> lines = new ArrayList<String>( 2 );
			lines.add( firstLine );
			lines.add( nextLine );
			data.add( lines );

		}

		BashScriptBuilder.buildScripts( this, data, files );
	}
}
