package be.nabu.libs.maven.api;

import java.io.IOException;
import java.io.InputStream;

public interface WritableRepository extends Repository {
	public Artifact create(String groupId, String artifactId, String version, String packaging, InputStream input, boolean isTest) throws IOException;
}
