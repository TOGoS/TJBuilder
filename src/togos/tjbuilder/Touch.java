package togos.tjbuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class Touch
{
	protected static long getLatestModification( File f ) {
		if( f.isDirectory() ) {
			long t = f.lastModified();
			File[] entries = f.listFiles();
			if( entries != null ) for( File e : entries ) {
				t = Math.max(t, getLatestModification(e));
			}
			return t;
		} else if( f.exists() ) {
			return f.lastModified();
		} else {
			return Long.MIN_VALUE;
		}
	}
	
	protected static final String USAGE =
		"Usage: tjb touch [<options>] <file> ...\n" +
		"\n" +
		"Options:\n" +
		"  --latest-within=<file/dir>\n" +
		"\n" +
		"If --latest-within isn't specified, the current time will be used.";
	
	public static int main( Iterator<String> argi ) {
		ArrayList<File> toBeTouched = new ArrayList<File>();
		
		boolean timeSpecified = false;
		long mtime = Long.MIN_VALUE;
		
		while( argi.hasNext() ) {
			String arg = argi.next();
			if( !arg.startsWith("-") ) {
				toBeTouched.add(new File(arg));
			} else if( "-latest-within".equals(arg) ) {
				mtime = Math.max(mtime, getLatestModification(new File(argi.next())));
				timeSpecified = true;
			} else if( arg.startsWith("--latest-within=") ) {
				mtime = Math.max(mtime, getLatestModification(new File(arg.substring(16))));
				timeSpecified = true;
			} else if( "-?".equals(arg) || "-h".equals(arg) || "--help".equals(arg) ) {
				System.out.println(USAGE);
				return 0;
			} else {
				System.err.println("Error: unrecognized argument: '"+arg+"'\n\n"+USAGE);
				return 1;
			}
		}
		
		if( !timeSpecified ) mtime = System.currentTimeMillis();
		
		if( mtime == Long.MIN_VALUE ) {
			System.err.println("Couldn't determine modification time");
			return 1;
		}
		
		int err = 0;
		for( File f : toBeTouched ) {
			f.setLastModified(mtime);
		}
		return err;
	}
}
