package com.minecolonies.coremod.colony;

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
import java.util.*;
import java.util.stream.Stream;


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
    //Hut, Levels
    @NotNull
    private static       Map<String, Integer>      hutLevelsMap          = new HashMap<>();
    //Hut, Styles
    private static       Map<String, List<String>> hutStyleMap           = new HashMap<>();
    //Decoration, Style
    private static       Map<String, List<String>> decorationStyleMap    = new HashMap<>();

    /* md5 hash for the schematics
     * format is:
     * huts/stone/builder1.nbt -> hash
     * decorations/decoration/Well.nbt -> hash
     */
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
        Log.getLogger().info("loadStyleMaps()");
        try
        {
            @NotNull final URI uri = ColonyManager.class.getResource(SCHEMATICS_ASSET_PATH).toURI();
            final Path basePath;
            if ("jar".equals(uri.getScheme()))
            {
                try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap()))
                {
                    Log.getLogger().info("get folder schematic from jar");
                    basePath = fileSystem.getPath(SCHEMATICS_ASSET_PATH);
                    loadStyleMaps(basePath);
                }
            }
            else
            {
                Log.getLogger().info("URI="+uri);
                Log.getLogger().info("get folder schematic from uri");
                basePath = Paths.get(uri);
                loadStyleMaps(basePath);
            }

            Log.getLogger().info("get folder schematic");
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

        if (hutStyleMap.size()==0)
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
        Log.getLogger().info("Structures.loadStyleMaps(" + basePath + ")");

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

                    //Don't treat generic schematics as decorations or huts - ex: supply ship
                    if (NULL_STYLE.equals(style) || MINER_STYLE.equals(style))
                    {
                        continue;
                    }

                    final String filename = path.getFileName().toString().split("\\.nbt")[0];
                    final String hut = filename.split("\\d+")[0];

                    if (SCHEMATICS_HUTS.equals(schematicCategory) && isSchematicHut(hut))
                    {
                        addHutStyle(hut, style);
                        incrementHutMaxLevel(hut);
                    }
                    else if (SCHEMATICS_HUTS.equals(schematicCategory) || SCHEMATICS_DECORATIONS.equals(schematicCategory))
                    {
                        if (isSchematicHut(hut))
                        {
                            Log.getLogger().warn(path + " look like a hut but will not be considered as a hut!");
                        }
                        addDecorationStyle(style, filename);
                    }
                    else
                    {
                        Log.getLogger().error(path + " schematic is not a huts or decorations, ignoring it");
                    }

                    String relativePath = path.toString().substring(basePath.toString().length()).split("\\.nbt")[0];
                    if (relativePath.startsWith("/"))
                    {
                        relativePath = relativePath.substring(1);
                    }

                    final String md5 = Structure.calculateMD5(Structure.getStream(relativePath));
                    Log.getLogger().info("Add schematic "+ relativePath + " (md5:" + md5+")");
                    md5Map.put(relativePath, md5);
                }
            }
        }
    }

    private static boolean isSchematicHut(final String name)
    {
        return Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + name) != null;
    }

    private static void addHutStyle(final String hut, final String style)
    {
        if (!hutStyleMap.containsKey(hut))
        {
            hutStyleMap.put(hut, new ArrayList<>());
        }

        if (!hutStyleMap.get(hut).contains(style))
        {
            hutStyleMap.get(hut).add(style);
        }
    }

    private static void incrementHutMaxLevel(final String hut)
    {
        //Only count the number of huts in 1 style.
        if (getStylesForHut(hut).size() > 1)
        {
            return;
        }

        final Integer level = hutLevelsMap.getOrDefault(hut, 0);
        hutLevelsMap.put(hut, level + 1);
    }

    private static void addDecorationStyle(final String decoration, final String style)
    {
        if (!decorationStyleMap.containsKey(decoration))
        {
            decorationStyleMap.put(decoration, new ArrayList<>());
        }
        decorationStyleMap.get(decoration).add(style);
    }

    /**
     * Returns a list of styles for one specific hut.
     *
     * @param hut Hut to get styles for.
     * @return List of styles.
     */
    public static List<String> getStylesForHut(final String hut)
    {
        return hutStyleMap.get(hut);
    }

    /**
     * Returns a set of huts.
     * This is the key set of {@link #hutStyleMap}.
     *
     * @return Set of huts with a schematic.
     */
    public static Set<String> getHuts()
    {
        return hutStyleMap.keySet();
    }

    /**
     * Returns the max level for the provided hut.
     *
     * @param hut Hut to get max level for.
     * @return Max level of hut.
     */
    public static int getMaxLevelForHut(final String hut)
    {
        return hutLevelsMap.getOrDefault(hut, 0);
    }

    /**
     * Returns a set of decorations.
     * This is the key set of {@link #decorationStyleMap}.
     *
     * @return Set of decorations with a schematic.
     */
    public static Set<String> getDecorations()
    {
        return decorationStyleMap.keySet();
    }

    /**
     * Returns a list of styles for one specific decoration.
     *
     * @param decoration Decoration to get styles for.
     * @return List of styles.
     */
    public static List<String> getStylesForDecoration(final String decoration)
    {
        return decorationStyleMap.get(decoration);
    }

    /**
     * For use on client side by the ColonyStylesMessage.
     *
     * @param hutStyleMap        new hutStyleMap.
     * @param decorationStyleMap new decorationStyleMap.
     */
    @SideOnly(Side.CLIENT)
    public static void setStyles(final Map<String, Integer> hutLevelsMap, final Map<String, List<String>> hutStyleMap, final Map<String, List<String>> decorationStyleMap)
    {
        Structures.hutLevelsMap = hutLevelsMap;
        Structures.hutStyleMap = hutStyleMap;
        Structures.decorationStyleMap = decorationStyleMap;
    }

    /**
     * Returns a map of styles for one specific decoration.
     *
     * @param decoration Decoration to get styles for.
     * @return List of styles.
     */
    public static Map<String, Integer> getHutLevels()
    {
        return Structures.hutLevelsMap;
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
     * get the md5 hash for a structure name.
     *
     * @param structureName name of the structure as 'hut/wooden/Builder1'
     * @return the md5 hash or and empty String if not found
     */
    public static @NotNull String getMD5(final String structureName)
    {
        if (Structures.md5Map.containsKey(structureName))
        {
            return Structures.md5Map.get(structureName);
        }
        return "";
    }

    /**
     * get a structure name for a give md5 hash.
     *
     * @param md5 hash identifying the schematic
     * @return the structure name as 'hut/wooden/Builder1' or an empty String if not found
     */
    public static String getStructureNameByMD5(final String md5)
    {
        if (md5==null)
        {
            return "";
        }
        for (Map.Entry<String, String> entry : md5Map.entrySet())
        {
            if (entry.getValue().compareTo(md5) == 0)
            {
               return entry.getKey();
            }
        }
        return "";
    }


    /**
     * For use on client side by the ColonyStylesMessage.
     *
     * @param md5s        new md5Map.
     */
    @SideOnly(Side.CLIENT)
    public static void setMD5s(final Map<String, String> md5Map)
    {
        Structures.md5Map = md5Map;
    }
}
