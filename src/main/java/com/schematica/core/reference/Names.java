package com.schematica.core.reference;

@SuppressWarnings("HardCodedStringLiteral")
public final class Names {
    public static final class Config {
        public static final class Category {
            public static final String VERSION_CHECK = "versioncheck";
            public static final String TWEAKS = "tweaks";
        }

        public static final String CHECK_FOR_UPDATES = "checkForUpdates";
        public static final String CHECK_FOR_UPDATES_DESC = "Should the mod check for updates?";

        public static final String LANG_PREFIX = Reference.MODID_LOWER + ".config";
    }

    public static final class ModId {
        public static final String DYNIOUS_VERSION_CHECKER = "VersionChecker";
    }
}
