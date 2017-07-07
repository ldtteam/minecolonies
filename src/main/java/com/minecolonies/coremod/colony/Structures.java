package com.minecolonies.coremod.colony;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.workorders.AbstractWorkOrder;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.structures.helpers.Structure;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.minecolonies.api.util.constant.Suppression.EXCEPTION_HANDLERS_SHOULD_PRESERVE_THE_ORIGINAL_EXCEPTIONS;

/**
 * StructureProxy class.
 */
public final class Structures
{
    /**
     * Extension used by the schematic files.
     */
    public static final  String                                        SCHEMATIC_EXTENSION   = ".nbt";

    /**
     * Schematic's path in the jar file.
     */
    private static final String                                        SCHEMATICS_ASSET_PATH = "/assets/minecolonies/";

    /**
     * Schematic's path separator.
     */
    private static final String                                        SCHEMATICS_SEPARATOR = "/";

    /**
     * Storage location for the "normal" schematics.
     * In the jar file or on the local hard drive
     */
    public static final  String                                        SCHEMATICS_PREFIX     = "schematics";

    /**
     * Storage location for the cached schematics.
     */
    public static final  String                                        SCHEMATICS_CACHE      = "cache";

    /**
     * Storage location for the player's schematics.
     */
    public static final  String                                        SCHEMATICS_SCAN       = "scans";

    /**
     * Maximum size for a compressed schematic.
     */
    private static final int MAX_TOTAL_SIZE = 32_767;

    /**
     * Hut/Decoration, Styles, Levels.
     * This is populated on the client side only
     * Examples:
     * - schematics/stone/Builder1 => Builder -> stone -> Level 1 , huts/stone/Builder1
     * - schematics/walls/stone/Gate => decorations -> walls/stone -> Gate , decorations/walls/stone/Gate
     * - scans/458764687564687654 => scans -> <none> -> 458764687564687654 , scan/458764687564687654
     */
    @NotNull
    private static       Map<String, Map<String, Map<String, String>>> schematicsMap         = new HashMap<>();

    /**
     * md5 hash for the schematics.
     * format is:
     * schematics/stone/builder1 -> hash
     * schematics/decoration/Well -> hash
     * scans/test/buidling -> hash
     * cache/458764687564687654 => 458764687564687654
     */
    @NotNull
    private static Map<String, String> md5Map = new HashMap<>();

    /**
     * Whether or not the schematics list have changed.
     */
    private static boolean dirty = false;

    /**
     * Wether or not the server allow player Schematics.
     */
    private static boolean allowPlayerSchematics = false;

    /**
     * Private constructor so Structures objects can't be made.
     */
    private Structures()
    {
        //Hide implicit public constructor.
    }

    /**
     * Calls {@link #loadStyleMaps()}.
     */
    public static void init()
    {
        loadStyleMaps();
    }

    /**
     * Loads all styles saved in ["/assets/minecolonies/schematics/"].
     * Puts these in {@link #md5Map}, with key being the fullname of the structure (schematics/stone/Builder1).
     */
    // The same exception will be triggered in the 2nd catch with logging this time.
    @SuppressWarnings(EXCEPTION_HANDLERS_SHOULD_PRESERVE_THE_ORIGINAL_EXCEPTIONS)
    private static void loadStyleMaps()
    {
        if (!Configurations.gameplay.ignoreSchematicsFromJar)
        {
            loadStyleMapsJar();
        }

        final File schematicsFolder = MineColonies.proxy.getSchematicsFolder();
        if (schematicsFolder != null)
        {
            Log.getLogger().info("Load additionnal huts or decorations from " + schematicsFolder + SCHEMATICS_SEPARATOR + SCHEMATICS_PREFIX);
            checkDirectory(schematicsFolder.toPath().resolve(SCHEMATICS_PREFIX).toFile());
            loadSchematicsForPrefix(schematicsFolder.toPath(), SCHEMATICS_PREFIX);
        }

        final File cacheSchematicFolder = Structure.getCachedSchematicsFolder();
        if (cacheSchematicFolder != null)
        {
            checkDirectory(cacheSchematicFolder);
            Log.getLogger().info("Load cached schematic from " + cacheSchematicFolder + SCHEMATICS_SEPARATOR + SCHEMATICS_CACHE);
            checkDirectory(cacheSchematicFolder.toPath().resolve(SCHEMATICS_CACHE).toFile());
            loadSchematicsForPrefix(cacheSchematicFolder.toPath(), SCHEMATICS_CACHE);
        }

        if (md5Map.size() == 0)
        {
            Log.getLogger().error("Error loading StructureProxy directory. Things will break!");
        }
    }

    /**
     * load the schematics from the jar.
     */
    private static void loadStyleMapsJar()
    {
        URI uri = null;
        try
        {
            uri = ColonyManager.class.getResource(SCHEMATICS_ASSET_PATH).toURI();
        }
        catch (@NotNull final URISyntaxException e)
        {
            Log.getLogger().error("loadStyleMaps : ",e);
            return;
        }

        if ("jar".equals(uri.getScheme()))
        {
            try (FileSystem fileSystem = FileSystems.getFileSystem(uri))
            {
                final Path basePath = fileSystem.getPath(SCHEMATICS_ASSET_PATH);
                Log.getLogger().info("Load huts or decorations from jar");
                loadSchematicsForPrefix(basePath, SCHEMATICS_PREFIX);
            }
            catch (@NotNull IOException | FileSystemNotFoundException e1)
            {
                try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap()))
                {
                    final Path basePath = fileSystem.getPath(SCHEMATICS_ASSET_PATH);
                    Log.getLogger().info("Load huts or decorations from jar");
                    loadSchematicsForPrefix(basePath, SCHEMATICS_PREFIX);
                }
                catch (@NotNull final IOException e2)
                {
                    Log.getLogger().warn("loadStyleMaps: Could not load the schematics from the jar.", e2);
                }
            }
        }
        else
        {
            final Path basePath = Paths.get(uri);
            Log.getLogger().info("Load huts or decorations from uri");
            loadSchematicsForPrefix(basePath, SCHEMATICS_PREFIX);
        }
    }

    /**
     * Load all schematics from the scan folder.
     */
    @SideOnly(Side.CLIENT)
    public static void loadScannedStyleMaps()
    {
        if (!allowPlayerSchematics && FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
            return;
        }


        schematicsMap.remove(SCHEMATICS_SCAN);
        final File schematicsFolder = Structure.getClientSchematicsFolder();
        checkDirectory(schematicsFolder.toPath().resolve(SCHEMATICS_SCAN).toFile());
        loadSchematicsForPrefix(schematicsFolder.toPath(), SCHEMATICS_SCAN);
    }

    /**
     * Load all style maps from a certain path.
     * load all the schematics inside the folder path/prefix
     * and add them in the md5Map
     *
     * @param basePath the base path.
     * @param prefix   either schematics, scans, cache
     */
    private static void loadSchematicsForPrefix(@NotNull final Path basePath, @NotNull final String prefix)
    {
        try (Stream<Path> walk = Files.walk(basePath.resolve(prefix)))
        {
            final Iterator<Path> it = walk.iterator();
            while (it.hasNext())
            {
                final Path path = it.next();
                if (path.toString().endsWith(SCHEMATIC_EXTENSION))
                {
                    String relativePath = path.toString().substring(basePath.toString().length()).split("\\" + SCHEMATIC_EXTENSION)[0];
                    if (!SCHEMATICS_SEPARATOR.equals(path.getFileSystem().getSeparator()))
                    {
                        relativePath = relativePath.replace(path.getFileSystem().getSeparator(), SCHEMATICS_SEPARATOR);
                    }
                    if (relativePath.startsWith(SCHEMATICS_SEPARATOR))
                    {
                        relativePath = relativePath.substring(1);
                    }

                    final StructureName structureName = new StructureName(relativePath);
                    final String md5 = Structure.calculateMD5(Structure.getStream(relativePath));
                    if (md5 == null)
                    {
                        Log.getLogger().error("Structures: " + structureName + " with md5 null.");
                    }
                    else if (isSchematicSizeValid(structureName.toString()))
                    {
                        md5Map.put(structureName.toString(), md5);
                        if (MineColonies.isClient())
                        {
                            addSchematic(structureName);
                        }
                    }
                }
            }
        }
        catch (@NotNull final IOException e)
        {
            Log.getLogger().warn("loadSchematicsForPrefix: Could not load schematics from " + basePath.resolve(prefix), e);
        }
    }

    /**
     * check that a schematic is not too big to be sent.
     *
     * @param structureName name of the structure to check for.
     * @return True when the schematic is not too big.
     */
    private static boolean isSchematicSizeValid(@NotNull final String structureName)
    {
        final int maxSize = MAX_TOTAL_SIZE - Integer.SIZE / Byte.SIZE;
        final byte[] data = Structure.getStreamAsByteArray(Structure.getStream(structureName));
        final byte[] compressed = Structure.compress(data);

        if (compressed != null && compressed.length > maxSize)
        {
            Log.getLogger().warn("Structure " + structureName + " is " + compressed.length + " bytes when compress, maximum allowed is " + maxSize + " bytes.");
            return false;
        }
        return true;
    }

    /**
     * return true if the schematics list have changed.
     *
     * @return True if dirty, otherwise false
     */
    public static boolean isDirty()
    {
        return dirty;
    }

    /**
     * mark Structures as not dirty.
     */
    public static void clearDirty()
    {
        dirty = false;
    }

    /**
     * mark Structures as dirty.
     */
    private static void markDirty()
    {
        dirty = true;
    }

    /**
     * Whether ot not the server allow players schematics.
     *
     * @return True if the server accept schematics otherwise False
     */
    @SideOnly(Side.CLIENT)
    public static boolean isPlayerSchematicsAllowed()
    {
        return allowPlayerSchematics;
    }

    /**
     * Set if the server allow player schematics
     *
     * @param allowed True if the server allow it otherwise False
     */
    @SideOnly(Side.CLIENT)
    public static void setAllowPlayerSchematics(final boolean allowed)
    {
        allowPlayerSchematics = allowed;
    }

    /**
     * add a schematic in the schematicsMap.
     *
     * @param structureName the structure to add
     */
    @SideOnly(Side.CLIENT)
    private static void addSchematic(@NotNull final StructureName structureName)
    {
        if (structureName.getPrefix().equals(SCHEMATICS_CACHE))
        {
            return;
        }

        if (!schematicsMap.containsKey(structureName.getSection()))
        {
            schematicsMap.put(structureName.getSection(), new HashMap<>());
        }

        final Map<String, Map<String, String>> sectionMap = schematicsMap.get(structureName.getSection());
        if (!sectionMap.containsKey(structureName.getStyle()))
        {
            sectionMap.put(structureName.getStyle(), new TreeMap<>());
        }

        final Map<String, String> styleMap = sectionMap.get(structureName.getStyle());
        styleMap.put(structureName.getSchematic(), structureName.toString());
    }

    /**
     * rename a scanned structure.
     * rename the file and the md5 entry
     *
     * @param structureName the structure to rename
     * @param name New name for the schematic as in style/schematicname
     * @return the new structureName
     */
    @SideOnly(Side.CLIENT)
    public static Structures.StructureName renameScannedStructure(@NotNull final StructureName structureName, @NotNull final String name)
    {
        if (!SCHEMATICS_SCAN.equals(structureName.getPrefix()))
        {
            Log.getLogger().warn("Renamed failed: Invalid name " + structureName);
            return null;
        }

        if (!hasMD5(structureName))
        {
            Log.getLogger().warn("Renamed failed: No MD5 hash found for " + structureName);
            return null;
        }

        final StructureName newStructureName = new StructureName(SCHEMATICS_SCAN + SCHEMATICS_SEPARATOR + name);

        if (!hasMD5(structureName))
        {
            Log.getLogger().warn("Renamed failed: File already exist " + newStructureName);
            return null;
        }

        final File structureFile = Structure.getClientSchematicsFolder().toPath().resolve(structureName.toString() + SCHEMATIC_EXTENSION).toFile();
        final File newStructureFile = Structure.getClientSchematicsFolder().toPath().resolve(newStructureName.toString() + SCHEMATIC_EXTENSION).toFile();
        checkDirectory(newStructureFile.getParentFile());
        if (structureFile.renameTo(newStructureFile))
        {
            final String md5 = getMD5(structureName.toString());
            md5Map.put(newStructureName.toString(), md5);
            md5Map.remove(structureName.toString());
            Log.getLogger().info("Structure " + structureName + " have been renamed " + newStructureName);
            return newStructureName;
        }
        else
        {
            Log.getLogger().warn("Failed to rename structure from " + structureName + " to " + newStructureName);
            Log.getLogger().warn("Failed to rename structure from " + structureFile + " to " + newStructureFile);
        }
        return null;
    }

    /**
     * delete a scanned structure.
     * delete the file and the md5 entry
     *
     * @param structureName the structure to delete
     * @return True if the structure have been deleted, False otherwise
     */
    @SideOnly(Side.CLIENT)
    public static boolean deleteScannedStructure(@NotNull final StructureName structureName)
    {
        if (!SCHEMATICS_SCAN.equals(structureName.getPrefix()))
        {
            Log.getLogger().warn("Delete failed: Invalid name " + structureName);
            return false;
        }

        if (!hasMD5(structureName))
        {
            Log.getLogger().warn("Delete failed: No MD5 hash found for " + structureName);
            return false;
        }

        final File structureFile = Structure.getClientSchematicsFolder().toPath().resolve(structureName.toString() + SCHEMATIC_EXTENSION).toFile();
        if (structureFile.delete())
        {
            md5Map.remove(structureName.toString());
            Log.getLogger().info("Structures: " + structureName + " deleted successfully");
            return true;
        }
        else
        {
            Log.getLogger().warn("Failed to delete structure " + structureName);
        }
        return false;
    }

    /**
     * check/create a directory and its parents.
     *
     * @param directory to be created
     */
    private static void checkDirectory(@NotNull final File directory)
    {
        if (!directory.exists() && !directory.mkdirs())
        {
            Log.getLogger().error("Directory doesn't exist and failed to be created: " + directory.toString());
        }
    }

    /**
     * Get the list of Sections.
     * Builder, Citizen, Farmer ... + decorations and scans.
     *
     * @return list of sections.
     */
    @NotNull
    @SideOnly(Side.CLIENT)
    public static List<String> getSections()
    {
        final ArrayList<String> list = new ArrayList<>(schematicsMap.keySet());
        Collections.sort(list);
        return list;
    }

    /**
     * Get the list of styles for a given section.
     *
     * @param section such as decorations, Builder ...
     * @return the list of style for that section.
     */
    @NotNull
    @SideOnly(Side.CLIENT)
    public static List<String> getStylesFor(final String section)
    {
        if (schematicsMap.containsKey(section))
        {
            final Map<String, Map<String, String>> sectionMap = schematicsMap.get(section);
            final ArrayList<String> list = new ArrayList<>(sectionMap.keySet());
            Collections.sort(list);
            return list;
        }
        return new ArrayList<>();
    }

    /**
     * Get a list of schematics for this section and style.
     *
     * @param section such as Builder, schematics. scans ...
     * @param style limit the list for schematics to this style.
     * @return the list of schematics
     */
    @NotNull
    @SideOnly(Side.CLIENT)
    public static List<String> getSchematicsFor(final String section, final String style)
    {
        if (schematicsMap.containsKey(section))
        {
            final Map<String, Map<String, String>> sectionMap = schematicsMap.get(section);
            if (sectionMap.containsKey(style))
            {
                final ArrayList<String> list = new ArrayList<>(sectionMap.get(style).values());
                Collections.sort(list);
                return list;
            }
        }
        return new ArrayList<>();
    }

    /**
     * Class to handle schematic naming.
     * It does extract information from a schematic using its name.
     */
    public static class StructureName
    {
        private static final Pattern levelPattern              = Pattern.compile("[^0-9]+([0-9]+)$");
        private static final String  LOCALIZED_SCHEMATIC_LEVEL = "com.minecolonies.coremod.gui.buildtool.hut.level";
        /**
         * as in Builder, Citizen, TownHall, ... and decorations
         */
        private              String  section                   = "";
        private              String  prefix                    = "";
        private              String  style                     = "";
        private              String  schematic                 = "";
        private              String  hut                       = "";

        /**
         * Create a StructureName object from a schematic name.
         *
         * @param structureName as huts/stone/Builder1 or decorations/Walls/Gate
         */
        public StructureName(@NotNull final String structureName)
        {
            init(structureName);
        }

        /**
         * Create a StructureName
         *
         * @param prefix    should be schematics, scan or cache.
         * @param style     ex: wood, stone, walls/stone
         * @param schematic as in Builder1, Gate, without the nbt extension.
         */
        public StructureName(@NotNull final String prefix, final String style, @NotNull final String schematic)
        {
            String name = prefix;
            if (style != null && !style.isEmpty())
            {
                name = name + SCHEMATICS_SEPARATOR + style;
            }
            name = name + SCHEMATICS_SEPARATOR + schematic;
            init(name);
        }

        /**
         * fill the StructureName property by parsing the string.
         */
        private void init(@NotNull final String structureName)
        {
            if (structureName.isEmpty())
            {
                return;
            }

            String name = structureName;

            if (name.startsWith(SCHEMATICS_SCAN + SCHEMATICS_SEPARATOR))
            {
                prefix = SCHEMATICS_SCAN;
            }
            else if (name.startsWith(SCHEMATICS_CACHE + SCHEMATICS_SEPARATOR))
            {
                prefix = SCHEMATICS_CACHE;
            }
            else
            {
                if (!name.startsWith(SCHEMATICS_PREFIX + SCHEMATICS_SEPARATOR))
                {
                    name = SCHEMATICS_PREFIX + SCHEMATICS_SEPARATOR + name;
                }
                prefix = SCHEMATICS_PREFIX;
            }

            name = name.substring(prefix.length() + 1);
            final int lastSeparator = name.lastIndexOf(SCHEMATICS_SEPARATOR);
            if (lastSeparator == -1)
            {
                schematic = name;
            }
            else
            {
                style = name.substring(0, lastSeparator);
                schematic = name.substring(lastSeparator + 1);
            }

            //The section is the prefix, except fot hut
            section = prefix;
            if (prefix.equals(SCHEMATICS_PREFIX))
            {
                hut = schematic.split("\\d+")[0];
                section = SCHEMATICS_PREFIX;

                if (Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + hut) != null)
                {
                    section = hut;
                }
                else
                {
                    hut = "";
                }
            }
        }

        /**
         * Whether the schematic is a huit or not.
         * This is done using the naming convention only, should start by huts/
         * and a minecolonies block name should exist.
         *
         * @return True is it is a hut otherwise False
         */
        public boolean isHut()
        {
            return !hut.isEmpty();
        }

        /**
         * get the hut name.
         * such as Builder, Citizen, ...
         *
         * @return the name of the hut.
         */
        public String getHutName()
        {
            return hut;
        }

        /**
         * Get the section for the schematic.
         * it should be huts, scan, the block name (if isHut)
         *
         * @return the section the schematic belong to.
         */
        public String getSection()
        {
            return section;
        }

        /**
         * Get the prefix for the schematics
         *
         * @return huts, decorations, cache or scan.
         */
        public String getPrefix()
        {
            return prefix;
        }

        /**
         * get the style for the schematic.
         *
         * @return the style of the schematic.
         */
        public String getStyle()
        {
            return style;
        }

        /**
         * Get the name of the schematic.
         * For Builder's hut, it would be Builder1 (or Builder2. or ...)
         *
         * @return the schematic name
         */
        public String getSchematic()
        {
            return schematic;
        }

        /**
         * Get the localized name.
         * Examples:
         * - schematics/stone/Builder1 return Level 1
         * - schematics/walls/Gate return Gate
         * @return the localized name of the schematic
         */
        public String getLocalizedName()
        {
            if (isHut())
            {
                final Matcher matcher = levelPattern.matcher(schematic);
                if (matcher.find())
                {
                    final int level = Integer.parseInt(matcher.group(1));
                    return LanguageHandler.format(LOCALIZED_SCHEMATIC_LEVEL, level);
                }
            }
            return schematic;
        }

        /**
         * Get the full name of the schematic.
         * Examples: schematics/stone/Builder4 or scans/test/myown
         * This is what Structure.getStream use as a parameter.
         *
         * @return the full name of the schematics
         */
        public String toString()
        {
            if (style == null || style.isEmpty())
            {
                return prefix + SCHEMATICS_SEPARATOR + schematic;
            }
            return prefix + SCHEMATICS_SEPARATOR + style + SCHEMATICS_SEPARATOR + schematic;
        }
    }

    /**
     * check if a structure exist.
     *
     * @param structureName name of the structure as 'hut/wooden/Builder1'
     * @return the md5 hash or and empty String if not found
     */
    public static boolean hasMD5(@NotNull final StructureName structureName)
    {
        return hasMD5(structureName.toString());
    }

    public static boolean hasMD5(@NotNull final String structureName)
    {
        return md5Map.containsKey(structureName);
    }

    /**
     * add the md5 as a known structure in cache.
     *
     * @param md5 hash of the structure
     */
    public static void addMD5ToCache(@NotNull final String md5)
    {
        markDirty();
        md5Map.put(Structures.SCHEMATICS_CACHE + SCHEMATICS_SEPARATOR + md5, md5);
    }

    /**
     * get the md5 hash for a structure name.
     *
     * @param structureName name of the structure as 'hut/wooden/Builder1'
     * @return the md5 hash String or null if not found
     */
    public static String getMD5(@NotNull final String structureName)
    {
        if (!md5Map.containsKey(structureName))
        {
            return null;
        }

        return md5Map.get(structureName);
    }

    /**
     * get a structure name for a give md5 hash.
     *
     * @param md5 hash identifying the schematic
     * @return the structure name as 'schematics/wooden/Builder1' or an empty String if not found
     */
    public static StructureName getStructureNameByMD5(final String md5)
    {
        if (md5 != null)
        {
            for (final Map.Entry<String, String> md5Entry : md5Map.entrySet())
            {
                if (md5Entry.getValue().equals(md5))
                {
                    return new StructureName(md5Entry.getKey());
                }
            }
        }

        return null;
    }

    /**
     * Returns a map of all the structures.
     *
     * @return List of structure with their md5 hash.
     */
    public static Map<String, String> getMD5s()
    {
        return Structures.md5Map;
    }

    /**
     * get the set of cached schematic.
     */
    private static Set<String> getCachedMD5s()
    {
        final Set<String> md5Set = new HashSet<>();
        for (final Map.Entry<String, String> md5 : md5Map.entrySet())
        {
            final StructureName sn = new StructureName(md5.getKey());
            if (sn.getSection().equals(SCHEMATICS_CACHE))
            {
                md5Set.add(md5.getKey());
            }
        }
        return md5Set;
    }

    /**
     * delete a cached structure.
     * delete the file and the md5 entry
     *
     * @param structureName the structure to delete
     * @return True if the structure have been deleted, False otherwise
     */
    private static boolean deleteCachedStructure(@NotNull final StructureName structureName)
    {
        if (!SCHEMATICS_CACHE.equals(structureName.getPrefix()))
        {
            Log.getLogger().warn("Delete failed: Invalid name " + structureName);
            return false;
        }

        if (!hasMD5(structureName))
        {
            Log.getLogger().warn("Delete failed: No MD5 hash found for " + structureName);
            return false;
        }

        final File structureFile = MineColonies.proxy.getSchematicsFolder().toPath().resolve(structureName.toString() + SCHEMATIC_EXTENSION).toFile();
        if (structureFile.delete())
        {
            md5Map.remove(structureName.toString());
            return true;
        }
        else
        {
            Log.getLogger().warn("Failed to delete structure " + structureName);
        }
        return false;
    }

    /**
     * check that we can store the schematic.
     * According to the total number of schematic allowed on the server
     *
     * @return true if we can store more schematics
     */
    private static boolean canStoreNewSchematic()
    {
        if (MineColonies.isClient())
        {
            return true;
        }
        if (!Configurations.gameplay.allowPlayerSchematics)
        {
            return false;
        }

        final Set<String> md5Set = getCachedMD5s();
        if (md5Set.size() < Configurations.gameplay.maxCachedSchematics)
        {
            return true;
        }


        int countInUseStructures = 0;
        for (final Colony c : ColonyManager.getColonies())
        {
            for (final AbstractWorkOrder workOrder : c.getWorkManager().getWorkOrders().values())
            {
                if (workOrder instanceof WorkOrderBuildDecoration)
                {
                    final String schematicName = ((WorkOrderBuildDecoration) workOrder).getStructureName();
                    if (md5Set.contains(schematicName))
                    {
                        md5Set.remove(schematicName);
                        countInUseStructures++;
                    }
                }
            }
        }

        //md5Set containd only the unused one
        final Iterator<String> iterator = md5Set.iterator();
        while (iterator.hasNext() && md5Set.size() + countInUseStructures >= Configurations.gameplay.maxCachedSchematics)
        {
            final StructureName sn = new StructureName(iterator.next());
            if (deleteCachedStructure(sn))
            {
                iterator.remove();
            }
        }

        return md5Set.size() + countInUseStructures < Configurations.gameplay.maxCachedSchematics;
    }

    /**
     * Save a schematic in the cache.
     * This method is valid on the client and server
     * The schematic will be save under the cache directory using is md5 hash as a name.
     *
     * @param bytes representing the schematic.
     * @return True is the schematic have been saved successfully.
     */
    public static boolean handleSaveSchematicMessage(final byte[] bytes)
    {
        if (!canStoreNewSchematic())
        {
            Log.getLogger().warn("Could not store schematic in cache");
            return false;
        }

        final String md5 = Structure.calculateMD5(bytes);
        if (md5 != null)
        {
            Log.getLogger().info("Structures.handleSaveSchematicMessage: received new schematic md5:" + md5);
            final File schematicsFolder = Structure.getCachedSchematicsFolder();
            final File schematicFile = schematicsFolder.toPath().resolve(SCHEMATICS_CACHE + SCHEMATICS_SEPARATOR +md5 + SCHEMATIC_EXTENSION).toFile();
            checkDirectory(schematicFile.getParentFile());
            try (OutputStream outputstream = new FileOutputStream(schematicFile))
            {
                outputstream.write(bytes);
                Structures.addMD5ToCache(md5);
            }
            catch (@NotNull final IOException e)
            {
                Log.getLogger().warn("Exception while trying to save a schematic.", e);
                return false;
            }
        }
        else
        {
            Log.getLogger().info("Structures.handleSaveSchematicMessage: Could not calculate the MD5 hash");
            return false;
        }

        //Let the gui know we just save a schematic
        ColonyManager.setSchematicDownloaded(true);
        return true;
    }

    /**
     * For use on client side by the ColonyStylesMessage.
     *
     * @param md5s new md5Map.
     */
    @SideOnly(Side.CLIENT)
    public static void setMD5s(final Map<String, String> md5s)
    {
        // First clear all section except scans
        schematicsMap.entrySet().removeIf(entry -> !entry.getKey().equals(SCHEMATICS_SCAN));

        // Then we update all mdp hash and fill the schematicsMap
        for (final Map.Entry<String, String> md5 : md5s.entrySet())
        {
            final StructureName sn = new StructureName(md5.getKey());
            if (!sn.getSection().equals(SCHEMATICS_SCAN))
            {
                md5Map.put(md5.getKey(), md5.getValue());
                addSchematic(sn);
            }
        }
    }
}
