package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.lib.Constants;
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
import java.util.stream.Stream;

import java.util.concurrent.ThreadLocalRandom;


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

    private static final String                    SCHEMATIC_EXTENSION        = ".nbt";
    private static final String                    SCHEMATICS_ASSET_PATH      = "/assets/minecolonies/schematics/";
    public  static final String                    SCHEMATICS_HUTS            = "huts";
    public  static final String                    SCHEMATICS_DECORATIONS     = "decorations";
    public  static final String                    SCHEMATICS_CACHE           = "cache";
    public  static final String                    SCHEMATICS_CUSTOM          = "custom";
    //Hut, Levels
    @NotNull
    private static       Map<String, Map<String, Map<String, String>>> schematicsMap = new HashMap<>();

    /* md5 hash for the schematics
     * format is:
     * huts/stone/builder1.nbt -> hash
     * decorations/decoration/Well.nbt -> hash
     */
    @NotNull
    private static       Map<String, String>       md5Map                = new HashMap<>();

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
            final Path basePath;
            if ("jar".equals(uri.getScheme()))
            {
                try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap()))
                {
                    basePath = fileSystem.getPath(SCHEMATICS_ASSET_PATH);
                    loadStyleMaps(basePath);
                }
            }
            else
            {
                basePath = Paths.get(uri);
                loadStyleMaps(basePath);
            }

            File schematicsFolder = Structure.getSchematicsFolder();

            if (schematicsFolder != null)
            {
                loadStyleMaps(schematicsFolder.toPath());
            }

        }
        catch (@NotNull IOException | URISyntaxException e)
        {
            //Silently ignore
        }

        if (schematicsMap.size()==0)
        {
            Log.getLogger().error("Error loading StructureProxy directory. Things will break!");
        }
    }

    /**
     * Load all style maps from a certain path.
     *
     * @param basePath the base path.
     * @throws IOException if nothing found.
     */
    public static void loadStyleMaps(final Path basePath) throws IOException
    {
        try (Stream<Path> walk = Files.walk(basePath))
        {
            final Iterator<Path> it = walk.iterator();

            while (it.hasNext())
            {
                final Path path = it.next();

                if (path.toString().endsWith(SCHEMATIC_EXTENSION))
                {
                    Log.getLogger().info("path = " + path);
                    String style = "";
                    String schematicCategory = "";
                    if (path.getParent().toString().startsWith(basePath.toString()))
                    {
                        style = path.getParent().toString().substring(basePath.toString().length());
                        if (style.startsWith("/"))
                        {
                            style = style.substring(1);
                        }
                        final int indexSeparator = style.indexOf('/');
                        if (indexSeparator!=-1)
                        {
                            schematicCategory = style.substring(0,indexSeparator);
                            style = style.substring(indexSeparator+1);
                        }
                    }

                    //Don't treat generic schematics as decorations or huts - ex: supply ship
                    if (NULL_STYLE.equals(style) || MINER_STYLE.equals(style))
                    {
                        continue;
                    }

                    String relativePath = path.toString().substring(basePath.toString().length()).split("\\.nbt")[0];
                    if (relativePath.startsWith("/"))
                    {
                        relativePath = relativePath.substring(1);
                    }

                    final String md5 = Structure.calculateMD5(Structure.getStream(relativePath));
                    final StructureName structureName = new StructureName(relativePath);

                    if (md5Map.containsKey(structureName.toString()))
                    {
                        Log.getLogger().info("Override " + structureName + " md5:" + md5 + " (was " + md5Map.containsKey(structureName.toString()) + ")");
                    }
                    md5Map.put(structureName.toString(), md5);

                    if (!structureName.getPrefix().equals(SCHEMATICS_CACHE))
                    {
                        addMenuEntry(structureName, path.toString());
                    }
                }
            }
        }
    }

    private static void addMenuEntry(@NotNull StructureName structureName, String fileName)
    {
        if (structureName.getPrefix().equals(SCHEMATICS_CACHE))
        {
            return;
        }

        if (!schematicsMap.containsKey(structureName.getSection()))
        {
            Log.getLogger().warn("Can not add " + structureName + " to the menu, section " + structureName.getSection() + " does not exist" );
            //return;
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

    public static void loadCustomStyleMaps()
    {
        File schematicsFolder = Structure.getCustomSchematicsFolder();

        if (schematicsFolder == null)
        {
            Log.getLogger().info("could not find custom schematic folder");
            return;
        }
        final Path basePath = schematicsFolder.toPath();
        try (Stream<Path> walk = Files.walk(basePath))
        {
            final Iterator<Path> it = walk.iterator();

            while (it.hasNext())
            {
                final Path path = it.next();

                if (path.toString().endsWith(SCHEMATIC_EXTENSION))
                {
                    String style = "";
                    String schematicCategory = "";
                    if (path.getParent().toString().startsWith(basePath.toString()))
                    {
                        style = path.getParent().toString().substring(basePath.toString().length());
                        if (style.startsWith("/"))
                        {
                            style = style.substring(1);
                        }
                        final int indexSeparator = style.indexOf('/');
                        if (indexSeparator!=-1)
                        {
                            schematicCategory = style.substring(0,indexSeparator);
                            style = style.substring(indexSeparator+1);
                        }
                    }



//                    final String filename = path.getFileName().toString().split("\\.nbt")[0];
                    String relativePath = path.toString().substring(basePath.toString().length()).split("\\.nbt")[0];
                    if (relativePath.startsWith("/"))
                    {
                        relativePath = relativePath.substring(1);
                    }

                    final FileInputStream fis =  new FileInputStream(path.toString());
                    final String md5 = Structure.calculateMD5(fis);
                    final StructureName structureName = new StructureName("custom/" + relativePath);
                    if (md5Map.containsKey(structureName.toString()))
                    {
                        Log.getLogger().info("Override " + structureName + " md5:" + md5 + " (was " + md5Map.containsKey(structureName.toString()) + ")");
                    }
                    md5Map.put(structureName.toString(), md5);

                    if (!structureName.getPrefix().equals(SCHEMATICS_CACHE))
                    {
                        addMenuEntry(structureName, path.toString());
                    }
                }
            }
        }
        catch (final IOException e)
        {
        }
    }

    private static boolean isSchematicHut(final String name)
    {
        return Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + name) != null;
    }

    /**
     * Get the list of Sections.
     * Builder, Citizen, Farmer ... + decorations and custom.
     * @return list of sections.
     */
    @NotNull
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
     * Returns a map of styles for one specific decoration.
     *
     * @param decoration Decoration to get styles for.
     * @return List of styles.
     */
    public static Map<String, Map<String, Map<String, String>>> getSchematics()
    {
        return Structures.schematicsMap;
    }

    public static class StructureName
    {
        private String section;
        private String prefix;
        private String style;
        private String schematic;
        private String hut = null;

        public StructureName(@NotNull final String structureName)
        {
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

        public boolean isHut()
        {
            return hut != null;
        }

        public String getHutName()
        {
            return hut;
        }

        public String getSection()
        {
            return section;
        }

        public String getPrefix()
        {
            return section;
        }

        public String getStyle()
        {
            return style;
        }

        public String getSchematic()
        {
            return schematic;
        }

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
    public static boolean hasStructureName(@NotNull final StructureName structureName)
    {
        if (!schematicsMap.containsKey(structureName.getSection()))
        {
            return false;
        }

        final Map<String, Map<String, String>> sectionMap = schematicsMap.get(structureName.getSection());
        if (!sectionMap.containsKey(structureName.getStyle()))
        {
            return false;
        }

        return sectionMap.get(structureName.getStyle()).containsKey(structureName.getSchematic());
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

        for (Map.Entry<String, Map<String, Map<String, String>>> sectionEntry : schematicsMap.entrySet())
        {
            for (Map.Entry<String, Map<String, String>> styleEntry : sectionEntry.getValue().entrySet())
            {
                for (Map.Entry<String, String> schematicEntry : styleEntry.getValue().entrySet())
                {
                    if (schematicEntry.getValue().compareTo(md5) == 0)
                    {
                        return new StructureName(sectionEntry.getKey() + '/' + styleEntry.getKey() + '/'+ schematicEntry.getKey());
                    }
                }
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
     * For use on client side by the ColonyStylesMessage.
     *
     * @param md5s        new md5Map.
     */
    @SideOnly(Side.CLIENT)
    public static void setMD5s(final Map<String, String> md5Map)
    {
        Log.getLogger().info("Structures.setMD5s");

        //We don't want to overide it all (we need to key custom)
        Structures.md5Map.putAll(md5Map);
    }


    /**
     * For use on client side by the ColonyStylesMessage.
     *
     * @param md5s        new md5Map.
     */
    @SideOnly(Side.CLIENT)
    public static void setSchematics(final Map<String, Map<String, Map<String, String>>> schematicsMap)
    {
        // We don't want to overide "Custom"
        for (Map.Entry<String, Map<String, Map<String, String>>> sectionEntry : schematicsMap.entrySet())
        {
            if (!sectionEntry.getKey().equals(SCHEMATICS_CUSTOM))
            {
                Structures.schematicsMap.put(sectionEntry.getKey(),sectionEntry.getValue());
            }
        }
    }
}
