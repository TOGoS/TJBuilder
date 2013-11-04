package togos.tjbuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitUtil
{
	static final String REF_NONE = "0000000000000000000000000000000000000000";
	
	private static List<Repository> readRepositoryList( BufferedReader r, String filename )
		throws IOException
	{
		ArrayList<Repository> repoList = new ArrayList<Repository>();
		String line;
		int lineNum = 0;
		while( (line = r.readLine()) != null ) {
			++lineNum;
			line = line.trim();
			if( line.startsWith("#") || line.isEmpty() ) continue;
			
			String[] nameUriPair = line.split("\\s+", 2);
			if( nameUriPair.length < 2 ) {
				throw new IOException("Bad {name \\s+ uri} line at "+filename+":"+lineNum+": '"+line+"'");
			}
			repoList.add( new Repository(nameUriPair[0], nameUriPair[1]) );
		}
		return repoList;
	}
	
	public static List<Repository> readRepositoryList( File f )
		throws IOException
	{
		BufferedReader r = new BufferedReader(new FileReader(f));
		try {
			return readRepositoryList(r, f.getPath());
		} finally {
			r.close();
		}
	}
	
	public static String readFirstNonEmptyLine( BufferedReader r, String filename )
		throws IOException
	{
		String line;
		while( (line = r.readLine()) != null ) {
			line = line.trim();
			if( line.startsWith("#") || line.isEmpty() ) continue;
			
			return line;
		}
		throw new IOException("No data found in "+filename);
	}
	
	public static String readRef( File f )
		throws IOException
	{
		BufferedReader r = new BufferedReader(new FileReader(f));
		try {
			return readFirstNonEmptyLine(r, f.getPath());
		} finally {
			r.close();
		}
	}
	
	public String getHeadRef( File projectDir )
		throws IOException
	{
		File headFile = new File(projectDir, ".git/refs/head/master");
		if( headFile.exists() ) {
			return readRef(headFile);
		}
		return REF_NONE;
	}
}
