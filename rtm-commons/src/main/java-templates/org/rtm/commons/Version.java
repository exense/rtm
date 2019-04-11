package org.rtm.commons;

import java.io.IOException;

public class Version {

	private static final String VERSION = "${project.version}";
	
	public static String getVersion() throws IOException {
		return VERSION;
	}
}
