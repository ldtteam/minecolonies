package com.minecolonies.colony;

import com.minecolonies.lib.Constants;
import com.minecolonies.util.Log;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by chris on 10/19/15.
 * StructureProxy class
 */
public final class Structures
{
    /**
     * Ignore the styles from the generic folder.
     */
    private static final String                    NULL_STYLE            = "schematics";

    /**
     * Ignore the styles from the miner folder.
     */
    private static final String                    MINER_STYLE            = "miner";

    private static final String                    SCHEMATIC_EXTENSION   = ".nbt";
    private static final String                    SCHEMATICS_ASSET_PATH = "/assets/minecolonies/schematics/";
    //Hut, Styles
    private static       Map<String, List<String>> hutStyleMap           = new HashMap<>();
    //Hut, Levels
    @NotNull
    private static       Map<String, Integer>      hutLevelsMap          = new HashMap<>();
    //Decoration, Style
    private static       Map<String, List<String>> decorationStyleMap    = new HashMap<>();

    /**
     * Private constructor so Structures objects can't be made.
     */
    private Structures()
    {
        //Hide implicit public constructor.
    }

    /**
     * Calls {@link #loadStyleMaps()}
     */
    public static void init()
    {
        loadStyleMaps();
    }

    /**
     * Loads all styles saved in ["/assets/minecolonies/schematics/"]
     * Puts these in {@link #hutStyleMap}, with key being the name of the hutDec (E.G. Lumberjack)
     * and the value is a list of styles. Puts decorations in {@link #decorationStyleMap}.
     */
    private static void loadStyleMaps()
    {
        try
        {
            @NotNull URI uri = ColonyManager.class.getResource(SCHEMATICS_ASSET_PATH).toURI();
            Path basePath;

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

            File decorationFolder;

            if(FMLCommonHandler.instance().getMinecraftServerInstance() == null)
            {
                 decorationFolder = new File(Minecraft.getMinecraft().mcDataDir, "minecolonies/decorations");
            }
            else
            {
                decorationFolder = new File(FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory(), "minecolonies/decorations");
            }

            if(!decorationFolder.exists() && !decorationFolder.mkdirs())
            {
                Log.getLogger().warn("Failed to create directories for dynamic decorations.");
            }
            loadStyleMaps(decorationFolder.toPath());
        }
        catch (@NotNull IOException | URISyntaxException e)
        {
            Log.getLogger().error("Error loading StructureProxy directory. Things will break!", e);
        }
    }

    public static void loadStyleMaps(Path basePath) throws IOException
    {
        try (Stream<Path> walk = Files.walk(basePath))
        {
            Iterator<Path> it = walk.iterator();

            while (it.hasNext())
            {
                Path path = it.next();

                String style = path.getParent().getFileName().toString();

                //Don't treat generic schematics as decorations or huts - ex: supply ship
                if (NULL_STYLE.equals(style) || MINER_STYLE.equals(style))
                {
                    continue;
                }

                if (path.toString().endsWith(SCHEMATIC_EXTENSION))
                {
                    String filename = path.getFileName().toString().split("\\.nbt")[0];
                    String hut = filename.split("\\d+")[0];

                    if (isSchematicHut(hut))
                    {
                        addHutStyle(hut, style);
                        incrementHutMaxLevel(hut);
                    }
                    else
                    {
                        addDecorationStyle(filename, style);
                    }
                }
            }
        }
    }

    private static boolean isSchematicHut(String name)
    {
        return Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + name) != null;
    }

    private static void addHutStyle(String hut, String style)
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

    private static void incrementHutMaxLevel(String hut)
    {
        //Only count the number of huts in 1 style.
        if (getStylesForHut(hut).size() > 1)
        {
            return;
        }

        Integer level = hutLevelsMap.getOrDefault(hut, 0);
        hutLevelsMap.put(hut, level + 1);
    }

    private static void addDecorationStyle(String decoration, String style)
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
    public static List<String> getStylesForHut(String hut)
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
    public static int getMaxLevelForHut(String hut)
    {
        return hutLevelsMap.getOrDefault(hut, 0);
    }

    /**
     * Returns a set of decorations.
     * This is the key set of {@link #decorationStyleMap}
     *
     * @return Set of decorations with a schematic
     */
    public static Set<String> getDecorations()
    {
        return decorationStyleMap.keySet();
    }

    /**
     * Returns a list of styles for one specific decoration
     *
     * @param decoration Decoration to get styles for
     * @return List of styles
     */
    public static List<String> getStylesForDecoration(String decoration)
    {
        return decorationStyleMap.get(decoration);
    }

    /**
     * For use on client side by the ColonyStylesMessage
     *
     * @param hutStyleMap        new hutStyleMap
     * @param decorationStyleMap new decorationStyleMap
     */
    @SideOnly(Side.CLIENT)
    public static void setStyles(Map<String, List<String>> hutStyleMap, Map<String, List<String>> decorationStyleMap)
    {
        Structures.hutStyleMap = hutStyleMap;
        Structures.decorationStyleMap = decorationStyleMap;
    }
}
