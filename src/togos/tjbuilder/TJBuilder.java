package togos.tjbuilder;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

public class TJBuilder
{
	protected static long getLatestModified( File f ) {
		if( f.isDirectory() ) {
			File[] entries = f.listFiles();
			if( entries == null ) return Long.MIN_VALUE;
			long t = f.lastModified();
			for( File e : entries ) {
				long et = getLatestModified(e);
				if( et > t ) t = et;
			}
			return t;
		} else if( f.exists() ) {
			return f.lastModified();
		} else {
			return Long.MIN_VALUE;
		}
	}
	
	protected static int touch( File f ) {
		long lm = getLatestModified(f);
		if( lm > Long.MIN_VALUE ) {
			return 0;
		} else {
			System.err.println("Couldn't determine last modification time of "+f);
			return 1;
		}
	}
	
	public static int touchetteMain( Iterator<String> argi ) {
		ArrayList<File> toBeTouched = new ArrayList<File>();
		
		boolean timeSpecified = false;
		long mtime = Long.MIN_VALUE;
		
		while( argi.hasNext() ) {
			String arg = argi.next();
			if( !arg.startsWith("-") ) {
				toBeTouched.add(new File(arg));
			} else if( "-latest-within".equals(arg) ) {
				mtime = Math.max(mtime, getLatestModified(new File(argi.next())));
				timeSpecified = true;
			} else {
				System.err.println("Error: unrecognized argument: '"+arg+"'");
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
			return 1;
		}
	}
	
	public static void main( String[] args ) {
		System.exit( main( Arrays.asList(args).iterator() ) );
	}
}
