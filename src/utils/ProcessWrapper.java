package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessWrapper
{
	protected static final Logger log = LoggerFactory.getLogger(ProcessWrapper.class);
	
	public ProcessWrapper( String[] cmdArgs ) throws Exception
	{
		try{
			StringBuffer sb = new StringBuffer();
			for ( int x=0; x < cmdArgs.length; x++ )
					sb.append(cmdArgs[x] + " " );
					
			log.debug("cmdArgs = " + sb.toString());	
	
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(cmdArgs);
			log.debug("Process Execution Started");	
			BufferedReader br = new BufferedReader (new InputStreamReader(p.getInputStream()));
			log.debug("Open InputStream in BufferedReader");	
			String s;	
			while ((s = br.readLine ())!= null)
			{
	    		log.info (s);
			}
			int exitValue = p.exitValue();
			log.debug("exitValue = " + exitValue);	
			log.debug("All lines have been output, next wait for process to complete...");	
			p.waitFor();
			log.debug("Process Complete");	
			p.destroy();
			log.debug("DESTROY Completed Process Object");
		}finally{
			
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		String[] a= new String[2];
		a[0] = "c:\\temp\\myScript.bat";
		a[1] = "locationOfConfigFile";
		new ProcessWrapper(a);
	}
}
