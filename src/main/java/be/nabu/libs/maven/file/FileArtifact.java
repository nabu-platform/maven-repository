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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import be.nabu.libs.maven.BaseArtifact;

public class FileArtifact extends BaseArtifact {
	
	private File file;
	
	public FileArtifact(File file) throws IOException {
		this.file = file;
		parseProperties();
	}
	
	public Date getLastModified() {
		return new Date(file.lastModified());
	}

	@Override
	public FileInputStream getContent() throws FileNotFoundException {
		return new FileInputStream(file);
	}

	@Override
	protected String getArtifactName() {
		return file.getName();
	}
}
