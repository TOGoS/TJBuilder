package togos.tjbuilder;

import java.io.File;
import java.io.IOException;

public interface CommandRunner
{
	public void mkdirs( File dir );
	
	public abstract int sys(File workingDir, String... command) throws IOException, InterruptedException;
}