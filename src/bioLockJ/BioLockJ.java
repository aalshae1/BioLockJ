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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.MailUtil;

/** 
 * To run BioLockJ program, from project root directory ($BLJ) run:
 * 
 *  nohup java -cp ./lib/*:./bin bioLockJ.BioLockJ ./resources/localKraken.properties emailPassword &	
 *  java -cp $BLJ/lib/*:$BLJ/bin bioLockJ.BioLockJ ./resources/somePropFile.prop
 *  
 *  Include 2nd param "emailPassword" to receive email notification when job is complete.
 *  
 *  BioLockJ is designed to run on any platform.  
 *  Each time BioLockJ runs, a new project specific directory is created in ./projects.
 *   
 *  PROJECT_NAME is read from propFile (required).  
 *  NUMBER_OF_JOBS_PER_CORE = # commands/subscript for cluster compute node (optional).
 *  
 *  Project Structure
 *  -----------------------------------------------------------------------------------
 *  ./projects
 *  	> PROJECT_NAME_%timestamp% (PROJECT_NAME = required property)
 *  		> 00_#RUN_BIOLOCK_J<BioLockJExecutor> (#RUN_BIOLOCK_J = required property)
 *  			> failures  
 *  			> input - if COPY_INPUT_FLAG=TRUE (optional property)
 *  			> output 
 *  			> qsub 
 *  			> scripts 
 *  				-runAll.sh (calls all run_###.sh scripts)
 *  				-run_000.sh (at least one numbered subscript is created)
 *  				-run_XXX.sh (as many as required byas per NUMBER_OF_JOBS_PER_CORE)
 *  		> 01_#RUN_BIOLOCK_J<BioLockJExecutor> (additional #RUN_BIOLOCK_J = optional)
 * 				* No input directory, uses 00_#RUN_BIOLOCK_J/output as input.
 * 				> failures
 * 				> output
 * 				> qsub (if needed)
 * 				> scripts (if needed)
 * 			> XX_#RUN_BIOLOCK_J<BioLockJExecutor> (additional #RUN_BIOLOCK_J = optional)
 */
public class BioLockJ
{
	// wait to initialize until after ConfigReader names log file.
	protected static Logger log;

	/**
	 * The main method is the first method called when BioLockJ is run.  Here we
	 * read property file, copy it to project directory, initialize ConfigReader 
	 * and call runProgram(cReader).
	 * 
	 * @param args - args[0] path to property file
	 * 				 args[1] email password (optional)
	 */
	public static void main( String[] args )
	{
		System.out.println( "START PROGRAM" );
		ConfigReader cReader = null;
		try
		{
			if( args.length < 1 || args.length > 2 )
			{
				System.out
						.println( "Usage " + BioLockJ.class.getName() + " <PROP FILE PATH> <OPTIONAL EMAIL PASSWORD>" );
				System.out.println( "TERMINATE PROGRAM" );
				System.exit( 1 );
			}

			File propFile = new File( args[0] );
			if( !propFile.exists() || propFile.isDirectory() )
				throw new Exception( propFile.getAbsolutePath() + " is not a valid file" );

			if( args.length == 2 )
			{
				cReader = new ConfigReader( propFile, args[1] );
			}
			else
			{
				cReader = new ConfigReader( propFile );
			}

			log = LoggerFactory.getLogger( BioLockJ.class );
			log.info( "Num Java run parameters args[ ] = " + args.length );
			String projectDir = BioLockJUtils.requireString( cReader, ConfigReader.PATH_TO_PROJECT_DIR );
			BioLockJUtils.logConfigFileSettings( cReader );
			log.info( "Create Project Directory: " + projectDir );
			BioLockJUtils.copyFile( propFile, projectDir );

			if( cReader.getMetaData() != null )
			{
				log.debug( "Testing Metadata Code" );
				ArrayList<String> attNames = cReader.getMetaData().getAttributeNames();
				Set<String> fileNames = cReader.getMetaData().getFileNames();
				String testFile = fileNames.iterator().next();
				String testAtt = attNames.get( 2 );

				log.debug( "Meta Attributes: " + attNames );
				log.debug( "Meta File Names: " + fileNames );
				log.debug(
						"Meta " + testAtt + " Descriptor: " + cReader.getMetaData().getAttributeDescriptor( testAtt ) );
				log.debug( "Meta " + testFile + " Att Values: " + cReader.getMetaData().getAttributes( testFile ) );
				log.debug( "Meta " + testFile + "/" + testAtt + " = "
						+ cReader.getMetaData().getAttribute( testFile, testAtt ) );

				String metadataPath = cReader.getMetaData().getMetadataPath();
				String descriptorPath = cReader.getMetaData().getDescriptorPath();
				BioLockJUtils.copyFile( new File( metadataPath ), projectDir );
				BioLockJUtils.copyFile( new File( descriptorPath ), projectDir );
			}

			runProgram( cReader );
		}
		catch( Exception ex )
		{
			log.error( ex.getMessage(), ex );
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				MailUtil.sendEmailNotification( cReader );
			}
			catch( Exception ex )
			{
				if( log != null )
				{
					log.error( "Unable to send notification email." );
					log.error( ex.getMessage(), ex );
				}
			}

			if( log != null )
			{
				log.info( BioLockJUtils.LOG_SPACER );
				log.info( "MAIN PROGRAM COMPLETE" );
				log.info( BioLockJUtils.LOG_SPACER );
			}
			else
			{
				System.out.println( "MAIN PROGRAM COMPLETE" );
			}
		}
	}

	protected static void runProgram( ConfigReader cReader ) throws Exception
	{
		List<BioLockJExecutor> list = getListToRun( cReader );

		for( BioLockJExecutor e : list )
			e.checkDependencies();

		for( BioLockJExecutor e : list )
		{
			BioLockJUtils.noteStartToLogWriter( e );
			executeAndWaitForScriptsIfAny( e );
			BioLockJUtils.noteEndToLogWriter( e );
		}
	}

	protected static void executeAndWaitForScriptsIfAny( BioLockJExecutor invoker ) throws Exception
	{
		invoker.executeProjectFile();
		if( invoker.hasScripts() )
		{
			int pollTime = 15;
			try
			{
				pollTime = BioLockJUtils.requirePositiveInteger( invoker.getConfig(), ConfigReader.POLL_TIME );
			}
			catch( Exception ex )
			{
				BioLockJExecutor.log.warn(
						"Could not set " + ConfigReader.POLL_TIME + ".  Setting poll time to " + pollTime + " seconds ",
						ex );
			}

			executeCHMOD_ifDefined( invoker.getConfig(), invoker.getScriptDir() );
			executeFile( invoker.getRunAllFile() );
			pollAndSpin( invoker, pollTime );
		}
	}

	protected static void executeCHMOD_ifDefined( ConfigReader cReader, File scriptDir ) throws Exception
	{
		String chmod = cReader.getAProperty( ConfigReader.CHMOD_STRING );
		if( chmod != null )
		{
			File[] listOfFiles = scriptDir.listFiles();
			for( File file : listOfFiles )
			{
				if( !file.getName().startsWith( "." ) )
				{
					new ProcessWrapper( getArgs( chmod, file.getAbsolutePath() ) );
				}
			}
		}
	}

	protected static void pollAndSpin( BioLockJExecutor invoker, int pollTime ) throws Exception
	{
		boolean finished = false;
		while( !finished )
		{
			finished = poll( invoker );

			if( !finished )
			{
				Thread.sleep( pollTime * 1000 );
			}
		}
	}

	protected static boolean poll( BioLockJExecutor invoker ) throws Exception
	{
		List<File> scriptFiles = invoker.getScriptFiles();
		int numSuccess = 0;
		int numFailed = 0;

		for( File f : scriptFiles )
		{
			File testSuccess = new File( f.getAbsolutePath() + BashScriptBuilder.SCRIPT_SUCCEEDED );

			if( testSuccess.exists() )
			{
				numSuccess++;
			}
			else
			{
				File testFailure = new File( f.getAbsolutePath() + BashScriptBuilder.SCRIPT_FAILED );
				if( testFailure.exists() )
				{
					numFailed++;
				}
				else
				{
					log.info( f.getAbsolutePath() + " not finished " );
				}
			}
		}

		File runAllFailed = new File( invoker.getRunAllFile().getAbsolutePath() + BashScriptBuilder.SCRIPT_FAILED );
		if( runAllFailed.exists() )
		{
			throw new Exception( "CANCEL SCRIPT EXECUTION: ERROR IN...runAll.sh" );
		}

		log.info(
				"Script Status (Total=" + scriptFiles.size() + "): Success=" + numSuccess + "; Failure=" + numFailed );
		return ( numSuccess + numFailed ) == scriptFiles.size();
	}

	protected static void executeFile( File f ) throws Exception
	{
		String[] cmd = new String[ 1 ];
		cmd[0] = f.getAbsolutePath();
		new ProcessWrapper( cmd );
	}

	protected static List<BioLockJExecutor> getListToRun( ConfigReader cReader ) throws Exception
	{
		List<BioLockJExecutor> list = new ArrayList<BioLockJExecutor>();
		BufferedReader reader = new BufferedReader( new FileReader( cReader.getPropertiesFile() ) );
		try
		{
			int count = 0;
			BioLockJExecutor bljePrevious = null;
			for( String s = reader.readLine(); s != null; s = reader.readLine() )
			{
				if( s.startsWith( BashScriptBuilder.RUN_BIOLOCK_J ) )
				{
					StringTokenizer sToken = new StringTokenizer( s );
					sToken.nextToken();
					if( !sToken.hasMoreTokens() )
						throw new Exception( "Lines starting with " + BashScriptBuilder.RUN_BIOLOCK_J
								+ " must be followed by a Java class that is a BioLockJExecutor" );

					String fullClassName = sToken.nextToken();
					BioLockJExecutor blje = (BioLockJExecutor) Class.forName( fullClassName ).newInstance();
					blje.setConfig( cReader );
					blje.setExecutorDir( BioLockJUtils.getSimpleClassName( fullClassName ), count++ );
					if( bljePrevious != null )
					{
						blje.setInputDir( bljePrevious.getOutputDir() );
					}
					else
					{
						blje.setInputFiles( null );
					}

					bljePrevious = blje;
					list.add( blje );
					if( sToken.hasMoreTokens() )
						throw new Exception( "Lines starting with " + BashScriptBuilder.RUN_BIOLOCK_J
								+ " must be followed by a Java class that is a BioLockJExecutor with no parameters" );
				}
			}
		}
		finally
		{
			if( reader != null )
				reader.close();
		}

		return list;
	}

	private static String[] getArgs( String command, String filePath )
	{
		StringTokenizer sToken = new StringTokenizer( command + " " + filePath );
		List<String> list = new ArrayList<String>();
		while( sToken.hasMoreTokens() )
			list.add( sToken.nextToken() );

		String[] args = new String[ list.size() ];

		for( int x = 0; x < list.size(); x++ )
			args[x] = list.get( x );;

		return args;
	}
}
