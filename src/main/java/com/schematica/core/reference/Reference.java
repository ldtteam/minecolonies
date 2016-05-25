package com.schematica.core.reference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

public class Reference {
    public static final String MODID = "LunatriusCore";
    public static final String MODID_LOWER = MODID.toLowerCase(Locale.ENGLISH);
    public static final String NAME = "LunatriusCore";
    public static final String VERSION = "${version}";
    public static final String FORGE = "${forgeversion}";
    public static final String MINECRAFT = "${mcversion}";
    public static final String PROXY_SERVER = "com.github.lunatrius.core.proxy.ServerProxy";
    public static final String PROXY_CLIENT = "com.github.lunatrius.core.proxy.ClientProxy";
    public static final String GUI_FACTORY = "com.github.lunatrius.core.client.gui.config.GuiFactory";

    public static Logger logger = LogManager.getLogger(Reference.MODID);
}
