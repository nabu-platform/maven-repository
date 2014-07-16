package be.nabu.libs.maven;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RepositoryUtils {
	
	public enum HashAlgorithm {
		MD5("MD5"), SHA1("SHA-1");
		
		private String name;
		
		private HashAlgorithm(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	/**
	 * Generates a hash of the input stream
	 * @param input
	 * @param algorithm
	 * @return
	 * @throws IOException
	 */
	public static String hash(InputStream input, HashAlgorithm algorithm) throws IOException {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm.getName());
			byte [] buffer = new byte[102400];
			int read = 0;
			while ((read = input.read(buffer)) != -1)
				digest.update(buffer, 0, read);
			byte [] hash = digest.digest();
			StringBuffer string = new StringBuffer();
			for (int i = 0; i < hash.length; ++i)
				string.append(Integer.toHexString((hash[i] & 0xFF) | 0x100).substring(1,3));
			return string.toString();
		}
		catch (NoSuchAlgorithmException e) {
			// should not occur due to enum
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Retrieves the properties stored in the "pom.properties" file in the zip
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static Properties getPropertiesFromZip(InputStream input) throws IOException {
		ZipInputStream zip = new ZipInputStream(input);
		ZipEntry entry;
		while((entry = zip.getNextEntry()) != null) {
			if (entry.getName().endsWith("/pom.properties")) {
				Properties properties = new Properties();
				properties.load(zip);
				return properties;
			}
		}
		return null;
	}
	
	public static Properties getPropertiesFromXML(InputStream input) throws IOException {
		Properties properties = new Properties();
		try {
			// going for regex instead of dom document parsing due to overhead for latter
			// this may change in future releases
			String content = toString(input);
			// remove any parent reference which would contain its own group id etc
			content = content.replaceAll("(?s)<parent>.*?</parent>", "");
			// remove any dependencies reference
			content = content.replaceAll("(?s)<dependencies>.*?</dependencies>", "");
			properties.put("groupId", content.replaceAll("(?s).*?<groupId>([^<]+).*", "$1"));
			properties.put("artifactId", content.replaceAll("(?s).*?<artifactId>([^<]+).*", "$1"));
			properties.put("version", content.replaceAll("(?s).*?<version>([^<]+).*", "$1"));
		}
		finally {
			input.close();
		}
		return properties;
	}
	
	public static String toString(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output);
		return new String(output.toByteArray(), "UTF-8");
	}

	public static void copy(InputStream input, OutputStream output) throws IOException {
		int read = 0;
		byte [] buffer = new byte[102400];
		while ( (read = input.read(buffer)) != -1)
			output.write(buffer, 0, read);
	}
}
