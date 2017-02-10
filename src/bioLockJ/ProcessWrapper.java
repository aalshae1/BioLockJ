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
package bioLockJ;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessWrapper
{
	protected static final Logger log = LoggerFactory.getLogger( ProcessWrapper.class );

	public ProcessWrapper( String[] cmdArgs ) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		for( int x = 0; x < cmdArgs.length; x++ )
			sb.append( cmdArgs[x] + " " );

		log.info( "EXECUTE COMMAND: " + sb.toString() );

		Runtime r = Runtime.getRuntime();
		Process p = r.exec( cmdArgs );
		BufferedReader br = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
		String s;
		while( ( s = br.readLine() ) != null )
		{
			log.info( s );
		}

		p.waitFor();
		p.destroy();
	}
}
