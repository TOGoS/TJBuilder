package togos.tjbuilder;

import java.util.List;

public class LibraryVersion
{
	public final String headRef;
	public final List<Repository> repositories;
	
	public LibraryVersion( String headRef, List<Repository> repositories ) {
		this.headRef = headRef;
		this.repositories = repositories;
	}
}
