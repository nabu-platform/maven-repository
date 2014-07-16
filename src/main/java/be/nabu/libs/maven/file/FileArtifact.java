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
