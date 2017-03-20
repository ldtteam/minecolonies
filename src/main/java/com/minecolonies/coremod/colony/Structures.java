package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.workorders.AbstractWorkOrder;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.lib.Constants;
import com.minecolonies.coremod.util.LanguageHandler;
import com.minecolonies.coremod.util.Log;
import com.minecolonies.structures.helpers.Structure;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.Collectors;


/**
 * StructureProxy class.
 */
public final class Structures
{
    /**
     * Ignore the styles from the generic folder.
     */
    private static final String NULL_STYLE = "";

    /**
     * Ignore the styles from the miner folder.
     */
    private static final String MINER_STYLE = "miner";

    public static final String                     SCHEMATIC_EXTENSION        = ".nbt";
    private static final String                    SCHEMATICS_ASSET_PATH      = "/assets/minecolonies/schematics/";
    public  static final String                    SCHEMATICS_HUTS            = "huts";
    public  static final String                    SCHEMATICS_DECORATIONS     = "decorations";
    public  static final String                    SCHEMATICS_CACHE           = "cache";
    public  static final String                    SCHEMATICS_CUSTOM          = "custom";
    /**
     * Hut/Decoration, Styles, Levels.
     * This is populated on the client side only
     * Examples:
     *  - huts/stone/Builder1 => Builder -> stone -> Level 1 , huts/stone/Builder1
     *  - decorations/walls/stone/Gate => decorations -> walls/stone -> Gate , decorations/walls/stone/Gate
     *  - custom/scan/458764687564687654 => custom -> scan -> 458764687564687654 , custom/scan/458764687564687654
     */
    @NotNull
    private static       Map<String, Map<String, Map<String, String>>> schematicsMap = new HashMap<>();

    /**
     * md5 hash for the schematics.
     * format is:
     * huts/stone/builder1 -> hash
     * decorations/decoration/Well -> hash
     */
    @NotNull
    private static       Map<String, String>       md5Map                = new HashMap<>();

    private static boolean dirty = false;

    /*
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
     * Puts these in {@link #hutStyleMap}, with key being the name of the hutDec (E.G. Lumberjack).
     * and the value is a list of styles. Puts decorations in {@link #decorationStyleMap}.
     */
    private static void loadStyleMaps()
    {
        try
        {
            @NotNull final URI uri = ColonyManager.class.getResource(SCHEMATICS_ASSET_PATH).toURI();
            final FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            final Path basePath = fileSystem.getPath(SCHEMATICS_ASSET_PATH);
            loadSchematicsForSection(basePath, SCHEMATICS_HUTS);
            loadSchematicsForSection(basePath, SCHEMATICS_DECORATIONS);
        }
        catch (@NotNull IOException | URISyntaxException e)
        {
            //Silently ignore
            Log.getLogger().warn("loadStyleMaps: Could not load the schematics from the jar");
        }

        File schematicsFolder = Structure.getSchematicsFolder();
        if (schematicsFolder != null)
        {
            checkDirectory(schematicsFolder.toPath().resolve(SCHEMATICS_HUTS).toFile());
            loadSchematicsForSection(schematicsFolder.toPath(), SCHEMATICS_HUTS);
            checkDirectory(schematicsFolder.toPath().resolve(SCHEMATICS_DECORATIONS).toFile());
            loadSchematicsForSection(schematicsFolder.toPath(), SCHEMATICS_DECORATIONS);
            checkDirectory(schematicsFolder.toPath().resolve(SCHEMATICS_CACHE).toFile());
            loadSchematicsForSection(schematicsFolder.toPath(), SCHEMATICS_CACHE);
        }

        if (md5Map.size()==0)
        {
            Log.getLogger().error("Error loading StructureProxy directory. Things will break!");
        }
    }

    /**
     * Load all schematic in the custom folder.
     */
    @SideOnly(Side.CLIENT)
    public static void loadCustomStyleMaps()
    {
        if (!allowPlayerSchematics && FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
            return;
        }


        schematicsMap.remove(SCHEMATICS_CUSTOM);
        final File schematicsFolder = Structure.getClientSchematicsFolder();
        checkDirectory(schematicsFolder.toPath().resolve(SCHEMATICS_CUSTOM).toFile());
        loadSchematicsForSection(schematicsFolder.toPath(), SCHEMATICS_CUSTOM);
    }

    /**
     * Load all style maps from a certain path.
     * load all the schematics inside the forlder path/section
     * and add them in the section of schematicsMap
     * @param basePath the base path.
     * @param section
     * @throws IOException if nothing found.
     */
    private static void loadSchematicsForSection(@NotNull final Path basePath, @NotNull final String section)
    {
        try (Stream<Path> walk = Files.walk(basePath.resolve(section)))
        {
            final Iterator<Path> it = walk.iterator();

            while (it.hasNext())
            {
                final Path path = it.next();

                if (path.toString().endsWith(SCHEMATIC_EXTENSION))
                {
                    String relativePath = path.toString().substring(basePath.toString().length()).split("\\"+SCHEMATIC_EXTENSION)[0];
                    if (relativePath.startsWith("/"))
                    {
                        relativePath = relativePath.substring(1);
                    }

                    final StructureName structureName = new StructureName(relativePath);
                    final String md5 = Structure.calculateMD5(Structure.getStream(relativePath));
                    if (md5Map.containsKey(structureName.toString()))
                    {
                        Log.getLogger().info("Override " + structureName + " md5:" + md5 + " (was " + md5Map.containsKey(structureName.toString()) + ")");
                    }
                    else
                    {
                        Log.getLogger().info("Add " + structureName + " md5:" + md5);
                    }
                    md5Map.put(structureName.toString(), md5);

                    if (MineColonies.isClient())
                    {
                        addSchematic(structureName);
                    }
                }
            }
        }
        catch (@NotNull IOException e)
        {
            Log.getLogger().warn("loadSchematicsForSection: Could not load schematics from " + basePath.resolve(section));
        }
    }

    /**
     * return true if the schematics list have changed.
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
     * @return True if the server accept schematics otherwise False
     */
    @SideOnly(Side.CLIENT)
    public static boolean isPlayerSchematicsAllowed()
    {
        return allowPlayerSchematics;
    }

    /**
     * Set if the server allow player schematics
     * @param allowed True if the server allow it otherwise False
     */
    @SideOnly(Side.CLIENT)
    public static void setAllowPlayerSchematics(boolean allowed)
    {
        allowPlayerSchematics = allowed;
    }

    /**
     * add a schematic in the schematicsMap.
     * @param structureName the structure to add
     */
    @SideOnly(Side.CLIENT)
    private static void addSchematic(@NotNull StructureName structureName)
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
     * rename a custom structure.
     * rename the file and the md5 entry
     * @param structureName the structure to add
     * @return the new structureName
     */
    @SideOnly(Side.CLIENT)
    public static Structures.StructureName renameCustomStructure(@NotNull final StructureName structureName, @NotNull final String name)
    {
        Log.getLogger().warn("renameCustomStructure(" + structureName + ", " + name + ")");
        if (!SCHEMATICS_CUSTOM.equals(structureName.getPrefix()))
        {
            Log.getLogger().warn("Renamed failed: Invalid name " + structureName);
            return null;
        }

        if (!hasMD5(structureName))
        {
            Log.getLogger().warn("Renamed failed: No MD5 hash found for " + structureName);
            return null;
        }

        final StructureName newStructureName = new StructureName(SCHEMATICS_CUSTOM + '/' + name);

        if (!hasMD5(structureName))
        {
            Log.getLogger().warn("Renamed failed: File already exist " + newStructureName);
            return null;
        }

        final File structureFile = Structure.getClientSchematicsFolder().toPath().resolve(structureName.toString()+SCHEMATIC_EXTENSION).toFile();
        final File newStructureFile = Structure.getClientSchematicsFolder().toPath().resolve(newStructureName.toString()+SCHEMATIC_EXTENSION).toFile();
        checkDirectory(newStructureFile.getParentFile());
        if (structureFile.renameTo(newStructureFile))
        {
            final String md5 = getMD5(structureName);
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
     * delete a custom structure.
     * delete the file and the md5 entry
     * @param structureName the structure to delete
     * @return True if the structure have been deleted, False otherwise
     */
    @SideOnly(Side.CLIENT)
    public static boolean deleteCustomStructure(@NotNull final StructureName structureName)
    {
        Log.getLogger().warn("deleteCustomStructure(" + structureName + ")");
        if (!SCHEMATICS_CUSTOM.equals(structureName.getPrefix()))
        {
            Log.getLogger().warn("Delete failed: Invalid name " + structureName);
            return false;
        }

        if (!hasMD5(structureName))
        {
            Log.getLogger().warn("Delete failed: No MD5 hash found for " + structureName);
            return false;
        }

        final File structureFile = Structure.getClientSchematicsFolder().toPath().resolve(structureName.toString()+SCHEMATIC_EXTENSION).toFile();
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
     * Builder, Citizen, Farmer ... + decorations and custom.
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
     * @param section such as Builder, decorations...
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
        private final static Pattern levelPattern = Pattern.compile("[^0-9]+([0-9]+)$");
        private final static String LOCALIZED_SCHEMATIC_LEVEL = "com.minecolonies.coremod.gui.buildtool.hut.level";
        private String section;
        private String prefix;
        private String style;
        private String schematic;
        private String hut = null;

        /**
         * Create a StructureName object from a schematic name.
         * @param structureName as huts/stone/Builder1 or decorations/Walls/Gate
         */
        public StructureName(@NotNull final String structureName)
        {
            init(structureName);
        }

        /**
         * Create a StructureName
         * @param section should be huts, decorations, custom of cache.
         * @param style ex: wood, stone, walls/stone
         * @param schematic as in Builde1, Gate, without the nbt extension.
         */
        public StructureName(@NotNull final String section, final String style, @NotNull final String schematic)
        {
            String name = section;
            if (style != null && !style.isEmpty())
            {
                name = name + '/' + style;
            }
            name = name + '/' + schematic;
            init(name);
        }


        private void init(@NotNull final String structureName)
        {
            if (structureName == null || structureName.isEmpty())
            {
                return;
            }
            final int firstSeparator = structureName.indexOf('/');
            final int lastSeparator = structureName.lastIndexOf('/');
            if (firstSeparator != -1 || lastSeparator != -1)
            {
                prefix = structureName.substring(0,firstSeparator);
                if (firstSeparator == lastSeparator)
                {
                    style = "";
                }
                else
                {
                    style = structureName.substring(firstSeparator+1, lastSeparator);
                }
                schematic = structureName.substring(lastSeparator+1);
            }

            if (prefix.equals(SCHEMATICS_HUTS))
            {
                final String name = schematic.split("\\d+")[0];

                if (Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + name) != null)
                {
                    hut = name;
                }
            }

            if (hut == null)
            {
                section = prefix;
            }
            else
            {
                section = hut;
            }

            if (toString().compareTo(structureName) != 0)
            {
                Log.getLogger().error("Structure " + structureName + " parsing failed");
                Log.getLogger().error("=> " + toString());
                Log.getLogger().error("section = " + section);
                Log.getLogger().error("prefix = " + prefix);
                Log.getLogger().error("style = " + style);
                Log.getLogger().error("schematic = " + schematic);
                Log.getLogger().error("firstSeparator = " + firstSeparator);
                Log.getLogger().error("lastSeparator = " + lastSeparator);


            }
        }

        /**
         * Whether the schematic is a huit or not.
         * This is done using the naming convention only, should start by huts/
         * and a minecolonies block name should exist.
         * @return True is it is a hut otherwise False
         */
        public boolean isHut()
        {
            return hut != null;
        }

        /**
         * get the hut name.
         * such as Builder, Citizen, ...
         * @return the name of the hut.
         */
        public String getHutName()
        {
            return hut;
        }

        /**
         * Get the section for the schematic.
         * it should be huts, custom, the block name (if isHut)
         * @return the section the schematic belong to.
         */
        public String getSection()
        {
            return section;
        }

        /**
         * Get the prefix for the schematics
         * @return huts, decorations, cache or custom.
         */
        public String getPrefix()
        {
            return section;
        }

        /**
         * get the style for the schematic.
         * @return the style of the schematic.
         */
        public String getStyle()
        {
            return style;
        }
        /**
         * Get the name of the schematic.
         * For Builder's hut, it would be Builder1 (or Builder2. or ...)
         * @return the schematic name
         */
        public String getSchematic()
        {
            return schematic;
        }

        /**
         * Get the localized name.
         * Examples:
         * - huts/stone/Builder1 => Level 1
         * - decorations/walls/Gate => Gate
         */
        public String getLocalizedName()
        {
            if (isHut())
            {
                Matcher matcher = levelPattern.matcher(schematic);
                if (matcher.find())
                {
                    final int level = Integer.parseInt(matcher.group(1));
                    return LanguageHandler.format(LOCALIZED_SCHEMATIC_LEVEL, level);
                }
            }
            return schematic;
        }

        /**
         * Get the full name of the scematic.
         * Examples: huts/stone/Builder4 or custom/test/myown
         * This is what Structure.getStream use as a parameter.
         * @return the full name of the schematics
         */
        public String toString()
        {
            if (style.isEmpty())
            {
                return prefix + '/' + schematic;
            }
            return prefix + '/' + style + '/' + schematic;
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
        return md5Map.containsKey(structureName.toString());
    }


    /**
     * get the md5 hash for a structure name.
     *
     * @param structureName name of the structure as 'hut/wooden/Builder1'
     * @return the md5 hash String or null if not found
     */
    public static void addMD5ToCache(@NotNull String md5)
    {
        markDirty();
        md5Map.put(Structures.SCHEMATICS_CACHE + '/' + md5, md5);
    }


    /**
     * get the md5 hash for a structure name.
     *
     * @param structureName name of the structure as 'hut/wooden/Builder1'
     * @return the md5 hash String or null if not found
     */
    public static String getMD5(@NotNull final StructureName structureName)
    {
        if (!md5Map.containsKey(structureName.toString()))
        {
            return null;
        }

        return md5Map.get(structureName.toString());
    }


    /**
     * get a structure name for a give md5 hash.
     *
     * @param md5 hash identifying the schematic
     * @return the structure name as 'hut/wooden/Builder1' or an empty String if not found
     */
    public static StructureName getStructureNameByMD5(final String md5)
    {
        if (md5==null)
        {
            return null;
        }

        for (Map.Entry<String, String> md5Entry : md5Map.entrySet())
        {
            if (md5Entry.getValue().equals(md5))
            {
                return new StructureName(md5Entry.getKey());
            }
        }

        return null;
    }

    /**
     * Returns a map of styles for one specific decoration.
     *
     * @param decoration Decoration to get styles for.
     * @return List of styles.
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
        final Set<String> md5Set =  new HashSet<>();
        for (Map.Entry<String, String> md5 : md5Map.entrySet())
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
     * delete a custom structure.
     * delete the file and the md5 entry
     * @param structureName the structure to delete
     * @return True if the structure have been deleted, False otherwise
     */
    private static boolean deleteCachedStructure(@NotNull final StructureName structureName)
    {
        Log.getLogger().warn("deleteCustomStructure(" + structureName + ")");
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

        final File structureFile = Structure.getSchematicsFolder().toPath().resolve(structureName.toString()+SCHEMATIC_EXTENSION).toFile();
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
     * @return true if we can store more schematics
     */
    private static boolean canStoreNewSchematic()
    {
        if (MineColonies.isClient())
        {
            return true;
        }
        if (!Configurations.allowPlayerSchematics)
        {
            return false;
        }

        final Set<String> md5Set = getCachedMD5s();
        Log.getLogger().info("Server has " + md5Set.size() + " cached schematics");
        if (md5Set.size() < Configurations.maxCachedSchematics)
        {
            return true;
        }


        int countInUseStructures = 0;
        for(final Colony c : ColonyManager.getColonies())
        {
            Log.getLogger().info("Looking a workorder in Colony " + c.getName());
	        for(final AbstractWorkOrder workOrder : c.getWorkManager().getWorkOrders().values())
            {
                if (workOrder instanceof WorkOrderBuildDecoration)
                {
                    final String schematicName = ((WorkOrderBuildDecoration)workOrder).getStructureName();
                    Log.getLogger().info("Looking a workorder with structure " + schematicName);
                    if (md5Set.contains(schematicName))
                    {
                        Log.getLogger().info("Colony " + c.getName() + " use cached schematic " + schematicName);
                        md5Set.remove(schematicName);
                        countInUseStructures++;
                    }
                }
            }
        }
        Log.getLogger().info("Server has " + countInUseStructures + " used cached schematics ");
        Log.getLogger().info("Server has " + md5Set.size() + " not used cached schematics ");
        //md5Set containd only the unused one
        Iterator<String> iterator = md5Set.iterator();
        while(iterator.hasNext() && md5Set.size() + countInUseStructures >= Configurations.maxCachedSchematics)
        {
            final StructureName sn = new StructureName(iterator.next());
            if (deleteCachedStructure(sn))
            {
                Log.getLogger().info("Removing cached schematic " + sn);
                iterator.remove();
            }
        }

        return md5Set.size() + countInUseStructures < Configurations.maxCachedSchematics;
    }

    /**
     * Save a schematic in the cache.
     * This method is valid on the client and server
     * @param bytes representing the schematic
     */
    public static boolean handleSaveSchematicMessage(final byte[] bytes)
    {
        final File schematicsFolder = Structure.getCachedSchematicsFolder();
        final String md5 = Structure.calculateMD5(bytes);

        if (!canStoreNewSchematic())
        {
            Log.getLogger().warn("Could not store schematic in cache");
            return false;
        }

        if (md5 != null)
        {
            Log.getLogger().info("Structures.handleSaveSchematicMessage: received new schematic md5:" + md5);
            final File schematicFile = schematicsFolder.toPath().resolve(md5 + SCHEMATIC_EXTENSION).toFile();
            checkDirectory(schematicFile.getParentFile());
            try (OutputStream outputstream = new FileOutputStream(schematicFile))
            {
                outputstream.write(bytes);
                Structures.addMD5ToCache(md5);
            }
            catch (final IOException e)
            {
                Log.getLogger().warn("Exception while trying to save a schematic.", e);
                return false;
            }
        }
        else
        {
           Log.getLogger().info("Structures.handleSaveSchematicMessage: Could not calculate the MD5 hash");
           return false ;
        }

        //Let the gui know we just save a schematic
        ColonyManager.setSchematicDownloaded(true);
        return true;
    }


    /**
     * For use on client side by the ColonyStylesMessage.
     *
     * @param md5s        new md5Map.
     */
    @SideOnly(Side.CLIENT)
    public static void setMD5s(final Map<String, String> md5s)
    {
        // First clear all section except custom
        schematicsMap.entrySet().removeIf(entry -> !entry.getKey().equals(SCHEMATICS_CUSTOM));

        // Then we update all mdp hash and fill the schematicsMap
        for (Map.Entry<String, String> md5 : md5s.entrySet())
        {
            final StructureName sn = new StructureName(md5.getKey());
            if (!sn.getSection().equals(SCHEMATICS_CUSTOM))
            {
                md5Map.put(md5.getKey(),md5.getValue());
                addSchematic(sn);
            }
        }
    }
}
