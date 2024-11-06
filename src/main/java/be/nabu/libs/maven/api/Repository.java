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
