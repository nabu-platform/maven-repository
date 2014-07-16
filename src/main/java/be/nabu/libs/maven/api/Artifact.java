package be.nabu.libs.maven.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public interface Artifact {
	public InputStream getPom() throws IOException;
	/**
	 * e.g. jar, war, ear,..
	 */
	public String getPackaging();
	public String getGroupId();
	public String getVersion();
	public String getArtifactId();
	public Date getLastModified();
	public InputStream getContent() throws IOException;

	/**
	 * Whether or not this is a test artifact
	 * @return
	 */
	public boolean isTest();
}
