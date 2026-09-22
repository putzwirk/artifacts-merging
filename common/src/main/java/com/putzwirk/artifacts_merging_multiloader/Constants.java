package com.putzwirk.artifacts_merging_multiloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	public static final String MOD_ID = "artifactsmerging";
	public static final String MOD_NAME = "Artifacts Merging";
	public static final String CONFIG_DIR_NAME = "artifactsmerging";
	public static final String CONFIG_CHANNEL_PATH = "config_sync";
	public static final int CONFIG_PAYLOAD_MAX_CHARS = 1 << 20;
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
}
