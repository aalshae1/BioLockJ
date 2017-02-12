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
import bioLockJ.BashScriptBuilder;
import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;

/**
 * 
 * Use this to run one core per RDP parser job
 */
public class RdpClassifier extends BioLockJExecutor
{

	@Override
	public void checkDependencies( ) throws Exception
	{
		BioLockJUtils.getExistingDirectories( getConfig(), ConfigReader.INPUT_DIRS, true );
		BioLockJUtils.requireExistingFile( getConfig(), ConfigReader.PATH_TO_RDP_JAR );
	}

	@Override
	public void executeProjectFile( ) throws Exception
	{
		File rdpBinary = BioLockJUtils.requireExistingFile( getConfig(), ConfigReader.PATH_TO_RDP_JAR );

		// how do I print the version?
		//BioLockJUtils.logVersion( rdpBinary.getAbsolutePath() );
		ArrayList<File> files = getInputFiles();
		log.info( "Number of input files to add to RDP scripts: " + files.size() );

		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		for( File file : files )
		{
			String rdpOutFile = getOutputDir().getAbsolutePath() + File.separator + file.getName() + "toRDP.txt";

			String firstLine = "java -jar " + rdpBinary.getAbsolutePath() + " " + "-o \"" + rdpOutFile + "\" -q \""
					+ file.getAbsolutePath() + "\"";

			String nextLine = "gzip " + rdpOutFile;

			ArrayList<String> lines = new ArrayList<String>( 2 );
			lines.add( firstLine );
			lines.add( nextLine );
			data.add( lines );

		}

		BashScriptBuilder.buildScripts( this, data, files );
	}
}
