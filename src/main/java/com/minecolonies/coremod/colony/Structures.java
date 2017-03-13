package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.configuration.Configurations;
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
                    loadSchematicsForSection(basePath, SCHEMATICS_HUTS);
                    loadSchematicsForSection(basePath, SCHEMATICS_DECORATIONS);
                }
            }
            else
            {
                basePath = Paths.get(uri);
                loadSchematicsForSection(basePath, SCHEMATICS_HUTS);
                loadSchematicsForSection(basePath, SCHEMATICS_DECORATIONS);
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

        }
        catch (@NotNull IOException | URISyntaxException e)
        {
            //Silently ignore
        }

        if (md5Map.size()==0)
        {
            Log.getLogger().error("Error loading StructureProxy directory. Things will break!");
        }
    }

    /**
     * Load all style maps from a certain path.
     *
     * @param basePath the base path.
     * @param section
     * @throws IOException if nothing found.
     */
    public static void loadSchematicsForSection(@NotNull final Path basePath, @NotNull final String section) throws IOException
    {
        try (Stream<Path> walk = Files.walk(basePath.resolve(section)))
        {
            final Iterator<Path> it = walk.iterator();

            while (it.hasNext())
            {
                final Path path = it.next();

                if (path.toString().endsWith(SCHEMATIC_EXTENSION))
                {
                    Log.getLogger().info("path = " + path);
                    String style = "";
                    if (path.getParent().toString().startsWith(basePath.toString()))
                    {
                        style = path.getParent().toString().substring(basePath.toString().length());
                        if (style.startsWith("/"))
                        {
                            style = style.substring(1);
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

                    if (MineColonies.isClient())
                    {
                        addSchematic(structureName);
                    }
                }
            }
        }
    }

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

    public static void loadCustomStyleMaps()
    {
        File schematicsFolder = Structure.getClientSchematicsFolder();
        try
        {
            checkDirectory(schematicsFolder.toPath().resolve(SCHEMATICS_CUSTOM).toFile());
            loadSchematicsForSection(schematicsFolder.toPath(), SCHEMATICS_CUSTOM);
        }
        catch (IOException e)
        {
            Log.getLogger().warn("Could not load the custom folder for schematics " + schematicsFolder.toPath().resolve(SCHEMATICS_CUSTOM));
        }
    }

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
            init(structureName);
        }

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
