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
