package com.schematica.reference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reference {
    public static final String MODID = "Schematica";
    public static final String VERSION = "${version}";

    public static Logger logger = LogManager.getLogger(Reference.MODID);
}
