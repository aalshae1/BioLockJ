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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BioLockJExecutor
{

	public abstract void executeProjectFile( ) throws Exception;

	public abstract void checkDependencies( ) throws Exception;

	protected static final Logger log = LoggerFactory.getLogger( BioLockJExecutor.class );

	private List<File> scriptFiles = new ArrayList<File>();
	private ConfigReader config;
	private File runAllFile;
	private File projectDir;
	private File executorDir;
	private File inputDir;
	private File scriptsDir;
	private File outputDir;

	public boolean hasScripts( )
	{
		if( scriptsDir == null )
			return false;
		return true;
	}

	public boolean poll( )
	{
		return true;
	}

	public void setConfig( ConfigReader cReader )
	{
		config = cReader;
	}

	public ConfigReader getConfig( )
	{
		return config;
	}

	public void setExecutorDir( String name, int index ) throws Exception
	{
		String fullPath = getProjectDir().getAbsolutePath() + File.separator + BioLockJUtils.formatInt( index, 2 ) + "_"
				+ name;
		File dir = new File( fullPath );
		if( !dir.mkdirs() )
		{
			throw new Exception( "ERROR: Unable to create: " + fullPath );
		}

		log.info( this.getClass().getSimpleName() + " Create Executor Directory: " + fullPath );
		executorDir = dir;
	}

	public File getExecutorDir( )
	{
		return executorDir;
	}

	public List<File> getScriptFiles( )
	{
		return scriptFiles;
	}

	public void addScriptFile( File f )
	{
		scriptFiles.add( f );
	}

	public File getRunAllFile( ) throws Exception
	{
		if( runAllFile != null )
		{
			return runAllFile;
		}
		runAllFile = ScriptBuilder.createRunAllFile( getScriptDir().getAbsolutePath() );
		log.info( this.getClass().getSimpleName() + " Create RunAllFile: " + runAllFile.getAbsolutePath() );
		return runAllFile;
	}

	public File getProjectDir( ) throws Exception
	{
		if( projectDir != null )
		{
			return projectDir;
		}
		projectDir = BioLockJUtils.requireExistingDirectory( getConfig(), ConfigReader.PATH_TO_PROJECT_DIR );
		log.info( this.getClass().getSimpleName() + " Project Directory: " + projectDir.getAbsolutePath() );
		return projectDir;
	}

	public void setInputDir( File inDir ) throws Exception
	{
		if( !inDir.getAbsolutePath().contains( getProjectDir().getName() ) )
		{
			if( BioLockJUtils.getBoolean( getConfig(), ConfigReader.COPY_INPUT_FLAG, false ) )
			{
				log.info( "Copy input data to " + getExecutorDir().getAbsolutePath() + File.separator + "input" );
				FileUtils.copyDirectory( inDir, getInputDir() );
			}
		}

		inputDir = inDir;
		log.info( this.getClass().getSimpleName() + " Set Input Directory: " + inputDir.getAbsolutePath() );
	}

	public File getInputDir( ) throws Exception
	{
		if( inputDir != null )
		{
			return inputDir;
		}
		inputDir = createSubDir( "input" );
		return inputDir;
	}

	public File getScriptDir( ) throws Exception
	{
		if( scriptsDir != null )
		{
			return scriptsDir;
		}
		scriptsDir = createSubDir( "scripts" );
		log.info( this.getClass().getSimpleName() + " Create Script Directory: " + scriptsDir.getAbsolutePath() );
		return scriptsDir;
	}

	public File getOutputDir( ) throws Exception
	{
		if( outputDir != null )
		{
			return outputDir;
		}
		outputDir = createSubDir( "output" );
		log.info( this.getClass().getSimpleName() + " Create Output Directory: " + outputDir.getAbsolutePath() );
		return outputDir;
	}

	private File createSubDir( String subDirPath ) throws Exception
	{
		String subDir = getExecutorDir().getAbsolutePath() + File.separator + subDirPath;
		File dir = new File( subDir );
		if( !dir.mkdir() )
		{
			throw new Exception( "ERROR: Unable to create: " + dir );
		}
		return dir;
	}

}
