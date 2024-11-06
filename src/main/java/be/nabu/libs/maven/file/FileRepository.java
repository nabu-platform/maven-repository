/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.libs.maven.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.nabu.libs.maven.BaseRepository;
import be.nabu.libs.maven.RepositoryUtils;
import be.nabu.libs.maven.api.Artifact;
import be.nabu.libs.maven.api.WritableRepository;

/**
 * This maintains a very simple file-based repository
 * 
 * @author alex
 *
 */
public class FileRepository extends BaseRepository implements WritableRepository {

	private File root;

	private Map<File, Long> lastModified = new HashMap<File, Long>();
	private Map<File, FileArtifact> artifacts = new HashMap<File, FileArtifact>();

	/**
	 * This regex is let loose upon the resulting file name after the format is applied. This allows you to further tweak the file name
	 * Note that it is replaced with "$1".
	 * This may be deprecated in a next version
	 */
	private String fileNameRegex = null;
	
	public FileRepository(File root) {
		this.root = root;
	}
	
	@Override
	public void scan() throws IOException {
		scan(root);
	}
	
	private void scan(File dir) throws IOException {
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				if (!lastModified.containsKey(file) || file.lastModified() > lastModified.get(file)) {
					artifacts.put(file, new FileArtifact(file));
					lastModified.put(file, file.lastModified());
				}
			}
			else if (file.isDirectory())
				scan(file);
		}		
	}

	@Override
	protected List<FileArtifact> getArtifacts() {
		return new ArrayList<FileArtifact>(artifacts.values());
	}

	@Override
	public Artifact create(String groupId, String artifactId, String version, String packaging, InputStream input, boolean isTest) throws IOException {
		String fileName = formatFileName(groupId, artifactId, version, packaging);
		
		if (fileNameRegex != null)
			fileName = fileName.replaceAll(fileNameRegex, "$1");
		
		// if it's a test, append that to the filename
		if (isTest)
			fileName = fileName.replaceAll("(.*)(\\.[^.]+)$", "$1-tests$2");
		
		File file = new File(root, fileName);
		
		// create the path to the file if necessary
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		
		FileOutputStream output = new FileOutputStream(file);
		try {
			RepositoryUtils.copy(input, output);
		}
		finally {
			output.close();
		}
		return new FileArtifact(file);
	}
	
	public String getFileNameRegex() {
		return fileNameRegex;
	}

	public void setFileNameRegex(String fileNameRegex) {
		this.fileNameRegex = fileNameRegex;
	}
}
