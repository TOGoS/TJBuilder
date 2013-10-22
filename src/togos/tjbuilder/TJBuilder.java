package togos.tjbuilder;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

public class TJBuilder
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
	
	protected static final String TOUCH_USAGE =
		"Usage: tjb touch [<options>] <file> ...\n" +
		"\n" +
		"Options:\n" +
		"  --latest-within=<file/dir>\n" +
		"\n" +
		"If --latest-within isn't specified, the current time will be used.";
	
	public static int touchetteMain( Iterator<String> argi ) {
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
				System.out.println(TOUCH_USAGE);
				return 0;
			} else {
				System.err.println("Error: unrecognized argument: '"+arg+"'\n\n"+TOUCH_USAGE);
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
	
	public static int main( Iterator<String> argi ) {
		if( !argi.hasNext() ) {
			System.err.println("Need to specify a sub-command.");
			return 1;
		}
		
		String subCommand = argi.next();
		if( "touch".equals(subCommand) ) {
			return touchetteMain(argi);
		} else {
			System.err.println("Unrecognized sub-command: '"+subCommand+"'");
			System.err.println("Available sub-commands: touch");
			return 1;
		}
	}
	
	public static void main( String[] args ) {
		System.exit( main( Arrays.asList(args).iterator() ) );
	}
}
