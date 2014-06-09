package togos.tjbuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;

public class ActualCommandRunner implements CommandRunner
{
	PrintStream cmdOut;
	OutputStream outOut;
	OutputStream errOut;
	boolean requireZeroExitStatus;
	
	public ActualCommandRunner( boolean requireZeroExitStatus ) {
		this.requireZeroExitStatus = requireZeroExitStatus;
	}
	
	static Thread pipe( final InputStream is, final OutputStream os, String threadName ) {
		Thread t = new Thread(threadName) {
			@Override public void run() {
				try {
					int r;
					byte[] buffer = new byte[1024];
					while( (r = is.read(buffer)) > 0 ) {
						os.write(buffer, 0, r);
					}
				} catch( IOException e ) {
					System.err.println("Error while piping");
					e.printStackTrace(System.err);
				}
			}
		};
		t.start();
		return t;
	}
	
	protected static final Pattern CLEAN_ARG_PATTERN = Pattern.compile("^[a-zA-Z0-9/:_+\\.\\-]+$");  
	
	public static String toCmdLine( String...args ) {
		String r = "";
		for( String a : args ) {
			if( r.length() > 0 ) r += " ";
			String escaped = CLEAN_ARG_PATTERN.matcher(a).matches() ? a :
				"'"+a.replace("\\","\\\\").replace("'","\\'")+"'"; 
			r += escaped;
		}
		return r;
	}
	
	@Override public void mkdirs(File dir) {
		if( !dir.exists() ) dir.mkdirs();
	}
	
	@Override public int sys( File workingDir, String...command ) throws IOException, InterruptedException {
		if( cmdOut != null ) cmdOut.println(workingDir + "$ " + toCmdLine(command));
		Process proc = new ProcessBuilder().directory(workingDir).command(command).start();
		Thread t1 = outOut == null ? null : pipe( proc.getInputStream(), outOut, "output stream piper" );
		Thread t2 = errOut == null ? null : pipe( proc.getErrorStream(), errOut, "error stream piper" );
		int status = proc.waitFor();
		if( t1 != null ) t1.join();
		if( t2 != null ) t2.join();
		if( requireZeroExitStatus && status != 0 ) {
			throw new RuntimeException("Command returned "+status+": "+toCmdLine(command));
		}
		return status;
	}
}
