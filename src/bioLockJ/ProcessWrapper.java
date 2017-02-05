package bioLockJ;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessWrapper
{
	protected static final Logger log = LoggerFactory.getLogger(ProcessWrapper.class);
	
	public ProcessWrapper( String[] cmdArgs ) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		for ( int x=0; x < cmdArgs.length; x++ )
				sb.append(cmdArgs[x] + " " );
				
		log.info("EXECUTE COMMAND: " + sb.toString());	

		Runtime r = Runtime.getRuntime();
		Process p = r.exec(cmdArgs);
		BufferedReader br = new BufferedReader (new InputStreamReader(p.getInputStream()));	
		String s;	
		while ((s = br.readLine ())!= null)
		{
    		log.info (s);
		}

		p.waitFor();
		p.destroy();
	}
}
