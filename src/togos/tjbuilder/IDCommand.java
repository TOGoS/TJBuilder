package togos.tjbuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.bitpedia.util.TigerTree;

public class IDCommand
{
	public static int main( Iterator<String> argi ) {
		ArrayList<String> resourcesToHash = new ArrayList<String>();  
		while( argi.hasNext() ) {
			String arg = argi.next();
			if( arg.startsWith("-") ) {
				System.err.println("Unrecognized argument: '"+arg+"'");
				System.err.println("Usage: tjbuilder id <resource> ...");
				return 1;
			} else {
				resourcesToHash.add(arg);
			}
		}
		
		BitprintDigest digester = new BitprintDigest();
		
		int errorCount = 0;
		for( String uri : resourcesToHash ) {
			File file = new File(uri);
			if( !file.exists() ) {
				System.out.println("(could not resolve '"+uri+"')");
				++errorCount;
				continue;
			}
			try {
				digester.reset();
				FileInputStream fis = new FileInputStream(file);
				byte[] buffer = new byte[65536];
				int read;
				while( (read = fis.read(buffer)) != -1 ) {
					digester.update(buffer, 0, read);
				}
				fis.close();
				System.out.println("urn:bitprint:"+BitprintDigest.format(digester.digest()));
			} catch( IOException e ) {
				System.out.println("(could not hash '"+file+"')");
				++errorCount;
			}
		}
		
		if( errorCount > 0 ) {
			System.err.println(errorCount+" resources could not be processed");
		}
		return errorCount;
	}
}
