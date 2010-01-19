/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2010 ETH Zurich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Web:  http://tos-ide.ethz.ch
 * Mail: tos-ide@tik.ee.ethz.ch
 */
package tinyos.yeti.model.standard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.model.ProjectModel;

/**
 * Standard implementation of {@link IStreamProvider}, using one file
 * for each {@link IParseFile}-extension pair.
 * @author Benjamin Sigg
 */
public class StandardStreamProvider extends StreamProvider{
	public StandardStreamProvider( ProjectModel model ){
		super( model );
	}
	
	public InputStream read( IParseFile file, String extension ) throws CoreException{
		IFile cache = getCacheFile( file, extension );
		if( cache == null || !cache.exists() )
			return null;
		
		return cache.getContents();
	}

	public OutputStream write( IParseFile file, String extension ) throws CoreException{
		IFile cache = getCacheFile( file, extension );
		return new DeferredOutputStream( cache );
	}
	
	private class DeferredOutputStream extends ByteArrayOutputStream{
		private IFile file;
		
		public DeferredOutputStream( IFile file ){
			this.file = file;
		}
		
		@Override
		public void close() throws IOException{
			super.close();
			ByteArrayInputStream in = new ByteArrayInputStream( this.toByteArray() );
			try{
				create( file, in, null );
			}
			catch( CoreException e ){
				TinyOSPlugin.log( e );
				throw new IOException( e.getMessage() );
			}
			in.close();
		}
	}
	

    public void clear( IParseFile file, String extension, IProgressMonitor monitor ){
        IFile cache = getCacheFile( file, extension );
        if( cache != null ){
            clearFile( cache, monitor );
        }
    } 
	
	public boolean canRead( IParseFile file, String extension ){
        IFile cache = getCacheFile( file, extension );
        if( cache == null )
            return false;
        
        return cache.exists();
    }

    /**
     * Gets the file which represents <code>file</code>.
     * @param file the file which gets cached
     * @param extension the kind of cache to be accessed
     * @return the cache file for <code>file</code>
     */
    protected IFile getCacheFile( IParseFile file, String extension ){
        return getDerivedFile( file, extension );
    }

    @Override
    protected String derivedFileName( File file, String extension, boolean absolute ){
    	if( absolute )
    		return file.getAbsolutePath() + "." + extension;
	    return file.getName() + "." + extension;
    }
}
