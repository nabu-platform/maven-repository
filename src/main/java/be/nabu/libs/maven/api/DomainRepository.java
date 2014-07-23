package be.nabu.libs.maven.api;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

public interface DomainRepository extends Repository {
	public List<String> getDomains();
	public boolean isInternal(Artifact artifact);
	public SortedSet<Artifact> getInternalArtifacts() throws IOException;
}
