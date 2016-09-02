package be.nabu.libs.maven;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import be.nabu.libs.maven.api.Artifact;

abstract public class BaseArtifact implements Artifact {

	private String groupId, artifactId, version, packaging;
	
	@Override
	public InputStream getPom() throws IOException {
		if (getPackaging().equalsIgnoreCase("pom")) {
			return getContent();
		}
		else {
			InputStream input = getContent();
			try {
				ZipInputStream zip = new ZipInputStream(input);
				ZipEntry entry;
				while((entry = zip.getNextEntry()) != null) {
					if (entry.getName().endsWith("/pom.xml"))
						return zip;
				}
				// if no pom.xml was found, generate it
				return new ByteArrayInputStream(generatePom().getBytes("UTF-8"));
			}
			catch (IOException e) {
				input.close();
				throw e;
			}
		}
	}
	
	private String generatePom() {
		String pom = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n";
		pom += "\t<modelVersion>4.0.0</modelVersion>\n";
		pom += "\t<groupId>" + getGroupId() + "</groupId>\n";
		pom += "\t<artifactId>" + getArtifactId() + "</artifactId>\n";
		pom += "\t<packaging>" + getPackaging() + "</packaging>\n";
		pom += "\t<version>" + getVersion() + "</version>\n";
		pom += "\t<name>" + getArtifactId()+ "</name>\n";
		pom += "\t<url>http://maven.apache.org</url>\n";
		pom += "</project>";
		return pom;
	}
	
	protected void parseProperties() throws IOException {
		InputStream input = getContent();
		Properties properties;
		try {
			if (getPackaging().equalsIgnoreCase("pom")) {
				properties = RepositoryUtils.getPropertiesFromXML(input);
			}
			else {
				properties = RepositoryUtils.getPropertiesFromZip(input);
			}
		}
		finally {
			input.close();
		}
		if (properties != null) {
			setVersion(properties.getProperty("version"));
			setArtifactId(properties.getProperty("artifactId"));
			setGroupId(properties.getProperty("groupId"));
			packaging = properties.getProperty("packaging");
		}
		else {
			String groupId = getArtifactName().replaceAll("^([^-]+).*", "$1");
			setGroupId(groupId.equals(getArtifactName()) ? "com.example" : groupId.replace("__", "-"));
			String version = getArtifactName().replaceAll(".*?-([^-]+)\\.[^.]+$", "$1");
			// the replacement makes sure we can still add "-" to versions in the file name
			setVersion(version.equals(getArtifactName()) ? "1.0" : version.replace("__", "-"));
			String name = getArtifactName().replaceAll("^(.+)\\.[^.]+$", "$1")
				.replaceAll("^" + groupId + "-", "")
				.replaceAll("-" + version, "");
			setArtifactId(name);
		}
	}
	
	abstract protected String getArtifactName();

	@Override
	public String getGroupId() {
		return groupId;
	}

	protected void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	@Override
	public String getArtifactId() {
		return artifactId;
	}

	protected void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	@Override
	public String getVersion() {
		return version;
	}

	protected void setVersion(String version) {
		this.version = version;
	}

	private String getExtension() {
		return getArtifactName().replaceAll(".*?([^.]+)$", "$1");
	}
	
	@Override
	public String getPackaging() {
		if (packaging == null) {
			packaging = getExtension().toLowerCase();
		}
		return packaging;
	}

	@Override
	public boolean isTest() {
		return getArtifactName().matches(".*-tests\\.[^.]+$");
	}

}
