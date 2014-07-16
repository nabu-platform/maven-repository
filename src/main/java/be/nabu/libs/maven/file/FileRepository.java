package be.nabu.libs.maven.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

	/**
	 * Configure the root directory of the repository
	 */
	public static final String PROPERTY_ROOT = "be.nabu.mvn.file.repository";
	
	/**
	 * The domain that holds your own artifacts (comma separate for multiple)
	 * This is used to distinguish internal and external artifacts
	 */
	public static final String PROPERTY_DOMAIN = "be.nabu.mvn.file.domains";
	
	private File root = new File(System.getProperty(PROPERTY_ROOT, "repository"));
	private List<String> domains = Arrays.asList(System.getProperty(PROPERTY_DOMAIN, "").split("[\\s]*,[\\s]*"));

	private Map<File, Long> lastModified = new HashMap<File, Long>();
	private Map<File, FileArtifact> artifacts = new HashMap<File, FileArtifact>();

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
	
	/**
	 * This regex is let loose upon the resulting file name after the format is applied. This allows you to further tweak the file name
	 * Note that it is replaced with "$1".
	 * This may be deprecated in a next version
	 */
	private String fileNameRegex = null;
	
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

	private boolean isInternal(String groupId) {
		for (String domain : domains) {
			if (groupId.equals(domain) || groupId.startsWith(domain + "."))
				return true;
		}
		return false;
	}
	
	@Override
	public Artifact create(String groupId, String artifactId, String version, String packaging, InputStream input, boolean isTest) throws IOException {
		String fileName = fileNameFormat
			.replaceAll("\\$domain", isInternal(groupId) ? "internal" : "external")
			.replaceAll("\\$type", version.endsWith("-SNAPSHOT") ? "snapshots" : "releases")
			.replaceAll("\\$groupId", groupId)
			.replaceAll("\\$exploded", groupId.replaceAll("\\.", "/"))
			.replaceAll("\\$artifactId", artifactId)
			.replaceAll("\\$version", version)
			.replaceAll("\\$extension", packaging);
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
	
	public String getFileNameFormat() {
		return fileNameFormat;
	}

	public void setFileNameFormat(String format) {
		this.fileNameFormat = format;
	}

	public String getFileNameRegex() {
		return fileNameRegex;
	}

	public void setFileNameRegex(String fileNameRegex) {
		this.fileNameRegex = fileNameRegex;
	}

	public File getRoot() {
		return root;
	}

	public void setRoot(File root) {
		this.root = root;
	}

	public List<String> getDomains() {
		return domains;
	}

	public void setDomains(List<String> domains) {
		this.domains = domains;
	}
}
