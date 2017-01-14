package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessWrapper
{
	protected static Logger LOG = LoggerFactory.getLogger(ProcessWrapper.class);
	
	public ProcessWrapper( String[] cmdArgs ) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		for ( int x=0; x < cmdArgs.length; x++ )
				sb.append(cmdArgs[x] + " " );
				
		LOG.info(sb.toString());	

		Runtime r = Runtime.getRuntime();
		Process p = r.exec(cmdArgs);
		
		BufferedReader br = new BufferedReader (new InputStreamReader(p.getInputStream ()));
		
		String s;
		
		while ((s = br.readLine ())!= null)
		{
    		LOG.info (s);
		}
				
		p.waitFor();
		p.destroy();
	}
	
	public static void main(String[] args) throws Exception
	{
		String[] a= new String[2];
		a[0] = "c:\\temp\\myScript.bat";
		a[1] = "locationOfConfigFile";
		new ProcessWrapper(a);
	}
}
