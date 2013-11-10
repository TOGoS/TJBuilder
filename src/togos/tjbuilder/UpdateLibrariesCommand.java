package togos.tjbuilder;

import static togos.tjbuilder.TJBuilder.isHelpRequestArgument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UpdateLibrariesCommand
{
	protected static final String USAGE =
		"Usage: tjb update-libraries [<options>] <libraries-dir>\n" +
		"\n" +
		"Options:\n" +
		"  --git=<path> ; path to 'git' executable\n" +
		"\n" +
		"Will install libraries with versions given in\n" +
		"<libraries-dir>-refs/<library-name> and repository URLs from\n" +
		"<libraries-dir>-repositories/<library-name>.\n" +
		"\n" +
		"Refs should be text files containing a single hex-encoded Git commit hash.\n" +
		"Repository files contain one repository per line in the form \"<name> <url>\".\n" +
		"Only the first one will be used for automatic updating.";
	
	String gitCommandPath = "git";
	CommandRunner requiredCommandRunner = new CommandRunner(true);
	CommandRunner whateverCommandRunner = new CommandRunner(false);
	
	protected String[] flatten( Object...args ) {
		ArrayList<String> res = new ArrayList<String>();
		for( Object arg : args ) {
			if( arg instanceof String ) res.add((String)arg);
			else if( arg instanceof String[] ) res.addAll(Arrays.asList((String[])arg));
		}
		return res.toArray(new String[res.size()]);
	}
	
	protected void updateLibrary( File libDir, LibraryVersion lib ) throws InterruptedException, IOException {
		if( !libDir.exists() ) libDir.mkdirs();
		File gitDir = new File(libDir, ".git");
		if( !gitDir.exists() ) requiredCommandRunner.sys( libDir, gitCommandPath, "init" );
		for( Repository repo : lib.repositories ) {
			whateverCommandRunner.sys( libDir, gitCommandPath, "remote", "rm", repo.name );
			requiredCommandRunner.sys( libDir, gitCommandPath, "remote", "add", repo.name, repo.uri );
		}
		requiredCommandRunner.sys( libDir, gitCommandPath, "fetch", lib.repositories.get(0).name );
		requiredCommandRunner.sys( libDir, gitCommandPath, "merge", lib.headRef );
	}
	
	protected static int error( String message ) {
		System.err.println("Error: "+message);
		return 1;
	}
	
	public static int main( Iterator<String> argi ) throws InterruptedException, IOException {
		File libsDir = null;
		String gitPath = null;
		
		while( argi.hasNext() ) {
			String arg = argi.next();
			if( arg.startsWith("--git=") ) {
				gitPath = arg.substring(6);
			} else if( !arg.startsWith("-") ) {
				if( libsDir != null ) return error("Library directory already given");
				libsDir = new File(arg);
			} else if( isHelpRequestArgument(arg) ) {
				System.out.println(USAGE);
				return 0;
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
		
		UpdateLibrariesCommand ulc = new UpdateLibrariesCommand();
		if( gitPath != null ) ulc.gitCommandPath = gitPath;
		
		for( Map.Entry<String,LibraryVersion> libE : libs.entrySet() ) {
			File libDir = new File(libsDir, libE.getKey());
			ulc.updateLibrary( libDir, libE.getValue() );
		}
		
		return 0;
	}
}
