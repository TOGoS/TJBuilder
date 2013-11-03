package togos.tjbuilder;

import java.util.Arrays;
import java.util.Iterator;

public class TJBuilder
{
	public static int main( Iterator<String> argi ) {
		if( !argi.hasNext() ) {
			System.err.println("Need to specify a sub-command.");
			return 1;
		}
		
		String subCommand = argi.next();
		if( "touch".equals(subCommand) ) {
			return Touch.main(argi);
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
