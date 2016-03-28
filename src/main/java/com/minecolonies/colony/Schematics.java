package com.minecolonies.colony;

import com.minecolonies.MineColonies;
import com.minecolonies.lib.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by chris on 10/19/15.
 */
public class Schematics
{
    private static Map<String, List<String>> hutStyleMap = new HashMap<>();//Hut,Styles

    /**
     * Calls {@link #loadHutStyleMap()}
     */
    public static void init()
    {
        loadHutStyleMap();
    }

    /**
     * Loads all styles saved in ["/assets/minecolonies/schematics/"]
     * Puts these in {@link #hutStyleMap}, with key being the name of the hut (E.G. Lumberjack)
     *  and the value is a list of styles
     */
    private static void loadHutStyleMap()
    {
        try
        {
            URI uri = ColonyManager.class.getResource("/assets/minecolonies/schematics/").toURI();
            System.out.println(uri.toString());     //todo why do we print this
            Path basePath;

            if (uri.getScheme().equals("jar"))
            {
                basePath = FileSystems.newFileSystem(uri, Collections.emptyMap()).getPath(
                        "/assets/minecolonies/schematics/");
            }
            else
            {
                basePath = Paths.get(uri);
            }
            try (Stream<Path> walk = Files.walk(basePath))
            {

                Iterator<Path> it = walk.iterator();

                while (it.hasNext())
                {
                    Path path = it.next();

                    if (path.toString().endsWith("1.schematic"))
                    {
                        String hutpath = path.getFileName().toString();
                        String hut = hutpath.substring(0, hutpath.length() - 11);
                        String style = path.getParent().getFileName().toString();

                        if (Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + hut) == null)
                        {
                            MineColonies.logger.warn(String.format("Malformed schematic name: %s/%s ignoring file",
                                                                   style,
                                                                   hut));
                            continue;
                        }

                        if (!hutStyleMap.containsKey(hut))
                        {
                            hutStyleMap.put(hut, new ArrayList<>());
                        }
                        hutStyleMap.get(hut).add(style);
                    }
                }
            }

        }
        catch (IOException | URISyntaxException e)
        {
            MineColonies.logger.error("Error loading Schematic directory. Things will break!");
        }
    }

    /**
     * Returns a set of huts.
     * This is the key set of {@link #hutStyleMap}
     *
     * @return  Set of huts with a schematic
     */
    public static Set<String> getHuts()
    {
        return hutStyleMap.keySet();
    }

    /**
     * Returns a lst of styles for one specific hut
     *
     * @param hut       Hut to get styles for
     * @return          List of styles
     */
    public static List<String> getStylesForHut(String hut)
    {
        return hutStyleMap.get(hut);
    }

    /**
     * For use on client side by the ColonyStylesMessage
     *
     * @param stylesMap     new hutStyleMap
     */
    @SideOnly(Side.CLIENT)
    public static void setStyles(Map<String, List<String>> stylesMap)
    {
        hutStyleMap = stylesMap;
    }
}
