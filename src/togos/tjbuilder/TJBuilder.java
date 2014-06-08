package togos.tjbuilder;

import java.util.Arrays;
import java.util.Iterator;

public class TJBuilder
{
	public static boolean isHelpRequestArgument( String arg ) {
		return "-?".equals(arg) || "-h".equals(arg) || "--help".equals(arg);
	}
	
	public static int main( Iterator<String> argi ) throws Exception {
		if( !argi.hasNext() ) {
			System.err.println("Need to specify a sub-command.");
			return 1;
		}
		
		String subCommand = argi.next();
		if( "touch".equals(subCommand) ) {
			return TouchCommand.main(argi);
		} else if( "update-libraries".equals(subCommand) ) {
			return UpdateLibrariesCommand.main(argi);
		} else {
			System.err.println("Unrecognized sub-command: '"+subCommand+"'");
			System.err.println("Available sub-commands: touch, update-libraries");
			return 1;
		}
	}
	
	public static void main( String[] args ) throws Exception {
		System.exit( main( Arrays.asList(args).iterator() ) );
	}
}
