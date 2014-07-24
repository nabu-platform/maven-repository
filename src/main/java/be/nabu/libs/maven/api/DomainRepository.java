package be.nabu.libs.maven.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface DomainRepository extends Repository {
	public List<String> getDomains();
	public boolean isInternal(Artifact artifact);
	public Set<Artifact> getInternalArtifacts() throws IOException;
}
