package togos.tjbuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UpdateLibraries
{
	protected static final String USAGE =
		"Usage: tjb update-libraries <libraries-dir>\n" +
		"\n" +
		"Will install libraries with versions given in\n" +
		"<libraries-dir>-refs/<library-name> and repository URLs from\n" +
		"<libraries-dir>-repositories/<library-name>.\n" +
		"\n" +
		"Refs should be text files containing a single hex-encoded Git commit hash.\n" +
		"Repository files contain one repository per line in the form \"<name> <url>\".\n" +
		"Only the first one will be used for automatic updating.";
	
	protected static int error( String message ) {
		System.err.println("Error: "+message);
		return 1;
	}
	
	protected static String toCmdLine( String...args ) {
		String r = "";
		for( String a : args ) {
			if( r.length() > 0 ) r += " "; 
			r += "'"+a.replace("\\","\\\\").replace("'","\\'")+"'";
		}
		return r;
	}
	
	static Thread pipe( final InputStream is, final OutputStream os ) {
		Thread t = new Thread() {
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
	
	protected static int sys(File workingDir, boolean requireZeroStatus, String...command ) throws InterruptedException, IOException {
		// System.out.println(workingDir + "$ " + toCmdLine(command));
		Process proc = new ProcessBuilder().directory(workingDir).command(command).start();
		Thread t1 = pipe( proc.getInputStream(), System.out );
		Thread t2 = pipe( proc.getErrorStream(), System.err );
		int status = proc.waitFor();
		t1.join();
		t2.join();
		if( requireZeroStatus && status != 0 ) {
			throw new RuntimeException("Command returned "+status+": "+toCmdLine(command));
		}
		return status;
	}
	
	protected static void sys(File workingDir, String...command ) throws InterruptedException, IOException {
		sys( workingDir, true, command );
	}
	
	protected static int sysIgnore(File workingDir, String...command ) throws InterruptedException, IOException {
		return sys( workingDir, false, command );
	}
	
	protected static String[] flatten( Object...args ) {
		ArrayList<String> res = new ArrayList<String>();
		for( Object arg : args ) {
			if( arg instanceof String ) res.add((String)arg);
			else if( arg instanceof String[] ) res.addAll(Arrays.asList((String[])arg));
		}
		return res.toArray(new String[res.size()]);
	}
	
	protected static void updateLibrary( File libDir, LibraryVersion lib ) throws InterruptedException, IOException {
		if( !libDir.exists() ) libDir.mkdirs();
		File gitDir = new File(libDir, ".git");
		if( !gitDir.exists() ) sys( libDir, "git", "init" );
		for( Repository repo : lib.repositories ) {
			sysIgnore( libDir, "git", "remote", "rm", repo.name );
			sys( libDir, "git", "remote", "add", repo.name, repo.uri );
		}
		sys( libDir, "git", "fetch", lib.repositories.get(0).name );
		sys( libDir, "git", "merge", lib.headRef );
	}
	
	public static int main( Iterator<String> argi ) throws InterruptedException, IOException {
		File libsDir = null;
		
		while( argi.hasNext() ) {
			String arg = argi.next();
			if( !arg.startsWith("-") ) {
				if( libsDir != null ) return error("Library directory already given");
				libsDir = new File(arg);
			} else {
				return error("Unrecognized argument: '"+arg+"'\n\n"+USAGE);
			}
		}
		
		File headFileDir = new File(libsDir+"-refs");
		File repoFileDir = new File(libsDir+"-repositories");
		File[] headFileList = headFileDir.listFiles();
		if( headFileList == null ) return error(headFileDir+" could not be read");
		Map<String,LibraryVersion> libs = new HashMap<String,LibraryVersion>();
		for( File headFile : headFileList ) {
			String libName = headFile.getName();
			File repoFile = new File(repoFileDir, libName);
			if( !repoFile.exists() ) return error("Missing "+repoFile);
			List<Repository> repoList = GitUtil.readRepositoryList(repoFile);
			if( repoList.size() == 0 ) {
				return error("No repositories listed for "+libName);
			}
			try {
				libs.put( 
					libName,
					new LibraryVersion(
						GitUtil.readRef(headFile),
						repoList
					)
				);
			} catch( IOException e ) {
				e.printStackTrace();
				return error(e.getMessage());
			}
		}
		
		for( Map.Entry<String,LibraryVersion> libE : libs.entrySet() ) {
			File libDir = new File(libsDir, libE.getKey());
			updateLibrary( libDir, libE.getValue() );
		}
		
		return 0;
	}
}
