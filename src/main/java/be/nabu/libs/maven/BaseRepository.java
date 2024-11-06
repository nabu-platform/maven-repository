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

package be.nabu.libs.maven;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.nabu.libs.maven.api.Artifact;
import be.nabu.libs.maven.api.DomainRepository;

abstract public class BaseRepository implements DomainRepository {

	/**
	 * Set it to "1.0.0" (default) or "1.1.0"
	 * If set to 1.1.0 it will list the snapshot versions that are available
	 */
	public static final String PROPERTY_MODEL_VERSION = "be.nabu.mvn.repo.modelVersion";
	
	abstract protected List<? extends Artifact> getArtifacts();
	
	private List<String> domains = new ArrayList<String>();
	
	/**
	 * Allows you to format the resulting file name using variables:
	 * - $groupId
	 * - $artifactId
	 * - $version
	 * - $extension
	 * - $domain ('internal' or 'external')
	 * - $type ('snapshots' or 'releases')
	 * - $exploded: the groupId but exploded
	 */
	private String fileNameFormat = "$artifactId-$version.$extension";
	
	@Override
	public InputStream getMetaData(String groupId, String artifactId) throws IOException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String xml = "<metadata>" +
			"<groupId>" + groupId + "</groupId>" +
			"<artifactId>" + artifactId + "</artifactId>" +
			"<version>$latest</version>" +
			"<versioning>" +
			"	<latest>$latest</latest>" +
			"	<versions>";
		
		Artifact lastArtifact = null;
		for (Artifact other : getArtifacts()) {
			if (other.getGroupId().equals(groupId) && other.getArtifactId().equals(artifactId)) {
				xml += "<version>" + other.getVersion() + "</version>";
				if (lastArtifact == null || other.getVersion().compareTo(lastArtifact.getVersion()) == 1)
					lastArtifact = other;
			}
		}
		
		if (lastArtifact == null)
			return null;
		
		xml += "	</versions>" +
				"	<lastUpdated>" + formatter.format(lastArtifact.getLastModified()) + "</lastUpdated>" +
				"</versioning></metadata>";
		
		xml = xml.replaceAll(Pattern.quote("$latest"), lastArtifact.getVersion());

		return new ByteArrayInputStream(xml.getBytes("UTF-8"));
	}

	@Override
	public Set<Artifact> getInternalArtifacts() throws IOException {
		Set<Artifact> internal = new HashSet<Artifact>(); 
		for (String groupId : getGroups()) {
			if (isInternal(groupId)) {
				for (String artifactId : getArtifacts(groupId)) {
					List<String> versions = new ArrayList<String>(getVersions(groupId, artifactId));
					Artifact artifact = getArtifact(groupId, artifactId, versions.get(versions.size() - 1), false);
					if (artifact != null) {
						internal.add(artifact);
					}
				}
			}
		}
		return internal;
	}
	
	@Override
	public InputStream getMetaData(Artifact artifact) throws IOException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		
		// 1.1.0 is for maven 3. note that the snapshotVersions is not supported in maven 2 and may cause errors. However the "old" metadata seems to work just fine in maven 3 so might as well use that for now
		String modelVersion = System.getProperty(PROPERTY_MODEL_VERSION, "1.0.0");

		String xml = "<metadata modelVersion=\"" + modelVersion + "\">" +
				"<groupId>" + artifact.getGroupId() + "</groupId>" +
				"<artifactId>" + artifact.getArtifactId() + "</artifactId>" +
				"<version>" + artifact.getVersion() + "</version>";
		
		// it's a snapshot
		if (artifact.getVersion().endsWith("-SNAPSHOT")) {
			int buildNumber = (int) (artifact.getLastModified().getTime() / 1000);
			String lastModified = formatter.format(artifact.getLastModified());
			xml += "<versioning>" +
					"	<snapshot>" +
					"		<timestamp>" + lastModified.replaceAll("^([0-9]{8})", "$1.") + "</timestamp>" +
					"		<buildNumber>" + buildNumber + "</buildNumber>" +
					"	</snapshot>" +
					"	<lastUpdated>" + lastModified + "</lastUpdated>";
			
			// only add this for maven 3.x
			// note that when enabling this, maven does not always update snapshots from the repo (only updates the metadata). So likely there is still a bug in it
			if (modelVersion.equals("1.1.0")) {
				xml += "<snapshotVersions>" + 
						"	<snapshotVersion>" +
						"		<extension>" + artifact.getArtifactId().replaceAll("^.*?\\.([^.]+)$", "$1") + "</extension>" +
						"		<value>" + artifact.getVersion().replaceAll("-SNAPSHOT", "-" + formatter.format(artifact.getLastModified()).replaceAll("^([0-9]{8})([0-9]{6})$", "$1.$2-" + buildNumber)) + "</value>" +
						"		<updated>" + lastModified + "</updated>" +
						"	</snapshotVersion>" +
						"</snapshotVersions>";
			}
			
			xml += "</versioning>";
		}
		xml += "</metadata>";
		return new ByteArrayInputStream(xml.getBytes("UTF-8"));
	}

	@Override
	public SortedSet<String> getGroups() throws IOException {
		SortedSet<String> groups = new TreeSet<String>();
		for (Artifact artifact : getArtifacts()) {
			groups.add(artifact.getGroupId());
		}
		return groups;
	}

	@Override
	public SortedSet<String> getArtifacts(String groupId) throws IOException {
		SortedSet<String> artifacts = new TreeSet<String>();
		for (Artifact artifact : getArtifacts()) {
			if (artifact.getGroupId().equals(groupId)) {
				artifacts.add(artifact.getArtifactId());
			}
		}
		return artifacts;
	}

	@Override
	public SortedSet<String> getVersions(String groupId, String artifactId) throws IOException {
		SortedSet<String> versions = new TreeSet<String>();
		for (Artifact artifact : getArtifacts()) {
			if (artifact.getGroupId().equals(groupId) && artifact.getArtifactId().equals(artifactId)) {
				versions.add(artifact.getVersion());
			}
		}
		return versions;
	}

	@Override
	public Artifact getArtifact(String groupId, String artifactId, String version, boolean isTest) {
		for (Artifact artifact : getArtifacts()) {
			if (artifact.getGroupId().equals(groupId) && artifact.getArtifactId().equals(artifactId) && artifact.getVersion().equals(version) && artifact.isTest() == isTest) {
				return artifact;
			}
		}
		return null;
	}

	@Override
	public List<String> getDomains() {
		return domains;
	}
	
	protected boolean isInternal(String groupId) {
		for (String domain : domains) {
			if (groupId.equals(domain) || groupId.startsWith(domain + "."))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean isInternal(Artifact artifact) {
		return isInternal(artifact.getGroupId());
	}
	
	protected String formatFileName(String groupId, String artifactId, String version, String packaging) {
		return fileNameFormat
			.replaceAll("\\$domain", isInternal(groupId) ? "internal" : "external")
			.replaceAll("\\$type", version.endsWith("-SNAPSHOT") ? "snapshots" : "releases")
			.replaceAll("\\$groupId", groupId)
			.replaceAll("\\$exploded", groupId.replaceAll("\\.", "/"))
			.replaceAll("\\$artifactId", Matcher.quoteReplacement(artifactId))
			.replaceAll("\\$version", Matcher.quoteReplacement(version))
			.replaceAll("\\$extension", Matcher.quoteReplacement(packaging));
	}
	
	public String getFileNameFormat() {
		return fileNameFormat;
	}
	public void setFileNameFormat(String format) {
		this.fileNameFormat = format;
	}
}
