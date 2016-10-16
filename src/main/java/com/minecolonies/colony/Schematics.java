package com.minecolonies.colony;

import com.minecolonies.lib.Constants;
import com.minecolonies.util.Log;
import com.minecolonies.util.StructureInfo;
import net.minecraft.block.Block;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by chris on 10/19/15.
 * Schematic class
 */
public final class Schematics
{
    /**
     * the root directory for schematics
     */
    private static final String SCHEMATIC_ROOT = "schematics";

    /**
     * File extension for schematic files
     */
    private static final String SCHEMATIC_EXTENSION = ".schematic";

    /**
     * path to the assets within the jar
     */
    private static final String SCHEMATICS_ASSET_PATH = "/assets/minecolonies/schematics/";

    /**
     * relative file path to the schematics folder in the Minecraft instance
     */
    private static final String SCHEMATICS_FILE_PATH = "minecolonies/schematics/";

    /**
     * Name,style, category,
     */
    private static Map<String, StructureInfo> structureMap = new HashMap<>();

    /**
     * Private constructor so Schematics objects can't be made.
     */
    private Schematics()
    {
        //Hide implicit public constructor.
    }

    /**
     * Calls {@link #loadStyleMaps(Boolean)}
     * Calls {@Link #copySchematicsToFileSystem()}
     *
     * @param useNewDirectoryStructure toggles if the schematics should be loaded from the files steam or the jar.
     */
    public static void init(final boolean useNewDirectoryStructure)
    {
        if (useNewDirectoryStructure)
        {
            copySchematicsToFileSystem(SCHEMATICS_FILE_PATH);
        }
        else
        {
            loadStyleMaps(useNewDirectoryStructure);
        }
    }

    private static void copySchematicsToFileSystem(String basePath)
    {
        final java.io.File folder = new java.io.File(basePath);
        verifySchematicsFolder(folder);
    }

    private static void verifySchematicsFolder(final java.io.File folder)
    {
        if (!folder.exists())
        {
            if (folder.getParentFile() != null)
            {
                folder.getParentFile().mkdirs();
            }
        }

        if (!folder.exists() && !folder.mkdir())
        {
            return;
        }
    }

    /**
     * Loads all styles saved in ["/assets/minecolonies/schematics/"]
     * Puts these in {@link #structureMap}, with key being the name of the hutDec (E.G. Lumberjack)
     * and the value is a list of styles.
     * */

    private static void loadStyleMaps(Boolean useNewDirectoryStructure)
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
                    loadStyleMaps(basePath, useNewDirectoryStructure);
                }
            }
            else
            {
                basePath = Paths.get(uri);
                loadStyleMaps(basePath, useNewDirectoryStructure);
            }
        }
        catch (@NotNull IOException | URISyntaxException e)
        {
            Log.getLogger().error("Error loading Schematic directory. Things will break!", e);
        }
    }

    private static void loadStyleMaps(Path basePath, Boolean useNewDirectoryStructure) throws IOException
    {
        try (Stream<Path> walk = Files.walk(basePath))
        {
            Iterator<Path> it = walk.iterator();

            while (it.hasNext())
            {
                Path path = it.next();

                String style = getDirectoryFromPathAfterADirectory(path, SCHEMATIC_ROOT);

                //Don't treat generic schematics as decorations or huts - ex: supply ship
                if (SCHEMATIC_ROOT.equals(style) || style.startsWith("_"))
                {
                    continue;
                }

                String category = getDirectoryFromPathAfterADirectory(path, style);

                if (path.toString().endsWith(SCHEMATIC_EXTENSION))
                {
                    String filename = path.getFileName().toString().split("\\.schematic")[0];
                    String hut = filename.split("\\d+")[0];

                    StructureInfo info = null;

                    if (!structureMap.containsKey(hut))
                    {
                        Boolean isHut = isSchematicHut(hut);
                        if (!useNewDirectoryStructure)
                        {
                            if (isHut)
                            {
                                category = "hut";
                            }
                            else
                            {
                                category = "decoration";
                            }
                        }

                        info = new StructureInfo(hut, isHut, category);
                        structureMap.put(hut, info);
                    }
                    else
                    {
                        info = structureMap.get(hut);
                    }
                    info.addLevel(style, extractInt(filename), path);
                }
            }
        }
    }

    private static int extractInt(String fileName)
    {
        int result = 1;
        Matcher matcher = Pattern.compile("\\d+").matcher(fileName);
        if (matcher.find())
        {
            result = Integer.parseInt(matcher.group());
        }
        return result;
    }

    private static String getDirectoryFromPathAfterADirectory(@NotNull Path path, String startingDirecotry)
    {
        Path parent = path;
        while (parent.getParent() != null
                 && !startingDirecotry.equalsIgnoreCase(parent.getFileName().toString()) //stop at the root
                 && !startingDirecotry.equalsIgnoreCase(parent.getParent().getFileName().toString())) //stop if the parent is the root
        {
            parent = parent.getParent();
        }
        return parent.getFileName().toString();
    }

    private static boolean isSchematicHut(String name)
    {
        return Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + name) != null;
    }

    /**
     * Returns a list of styles for one specific hut.
     *
     * @param hut Hut to get styles for.
     * @return List of styles.
     */
    public static List<String> getStylesForHut(String hut)
    {
        if (!structureMap.containsKey(hut))
        {
            return new ArrayList<>();
        }
        return structureMap.get(hut).getStyles();
    }

    /**
     * Returns a set of huts.
     * This is the list of huts.
     *
     * @return Set of huts with a schematic.
     */
    public static List<String> getHuts()
    {
        return structureMap.values().stream()
                 .filter(StructureInfo::getIsHut)
                 .map(StructureInfo::getName).collect(Collectors.toList());
    }

    /**
     * Returns the max level for the provided hut.
     *
     * @param hut Hut to get max level for.
     * @return Max level of hut.
     */
    public static int getMaxLevelForHut(String hut, String style)
    {
        if (!structureMap.containsKey(hut))
        {
            return 0;
        }
        return structureMap.get(hut).getMaxLevel(style);
    }

    /**
     * Returns a set of decorations.
     *
     * @return Set of decorations with a schematic
     */
    public static List<String> getDecorations()
    {

        return structureMap.values().stream()
                                .filter(b -> !b.getIsHut())
                                .map(StructureInfo::getName)
                                .collect(Collectors.toList());

    }

    /**
     * Returns a list of styles for one specific decoration
     *
     * @param decoration Decoration to get styles for
     * @return List of styles
     */
    public static List<String> getStylesForDecoration(String decoration)
    {
        return getStylesForHut(decoration);
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
        /*todo this isnt right.  we need to look at the build tool
        and this methods interaction in order to correct it.

         */

        //Schematics.hutStyleMap = hutStyleMap;
        //Schematics.decorationStyleMap = decorationStyleMap;
    }
}
