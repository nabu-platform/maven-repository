package be.nabu.libs.maven.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.SortedSet;

public interface Repository {
	
	/**
	 * Get all the existing groups
	 */
	public SortedSet<String> getGroups() throws IOException;
	
	/**
	 * Get the artifacts for a specific group
	 * @param groupId
	 * @return
	 * @throws IOException
	 */
	public SortedSet<String> getArtifacts(String groupId) throws IOException;
	
	/**
	 * Get the versions for a specific artifact
	 * @param groupId
	 * @param artifactId
	 * @return
	 * @throws IOException
	 */
	public SortedSet<String> getVersions(String groupId, String artifactId) throws IOException;
	
	/**
	 * Get a specific artifact version
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param isTest Whether or not you want the test version
	 * @return
	 * @throws IOException
	 */
	public Artifact getArtifact(String groupId, String artifactId, String version, boolean isTest) throws IOException;
	
	/**
	 * Get metadata.xml about all the versions for a specific artifact
	 */
	public InputStream getMetaData(String groupId, String artifactId) throws IOException;
	
	/**
	 * Get metadata.xml about a specific version of an artifact
	 */
	public InputStream getMetaData(Artifact artifact) throws IOException;
	
	/**
	 * Force the repository to scan its resources
	 */
	public void scan() throws IOException;
}
