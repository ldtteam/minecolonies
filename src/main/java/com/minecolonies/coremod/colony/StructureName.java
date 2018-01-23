package com.minecolonies.coremod.colony;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to handle schematic naming.
 * It does extract information from a schematic using its name.
 */
public class StructureName
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
        final StringBuilder name = new StringBuilder(prefix);
        if (style != null && !style.isEmpty())
        {
            name.append(Structures.SCHEMATICS_SEPARATOR).append(style);
        }
        name.append(Structures.SCHEMATICS_SEPARATOR).append(schematic);
        init(name.toString());
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

        if (name.startsWith(Structures.SCHEMATICS_SCAN + Structures.SCHEMATICS_SEPARATOR))
        {
            prefix = Structures.SCHEMATICS_SCAN;
        }
        else if (name.startsWith(Structures.SCHEMATICS_CACHE + Structures.SCHEMATICS_SEPARATOR))
        {
            prefix = Structures.SCHEMATICS_CACHE;
        }
        else
        {
            if (!name.startsWith(Structures.SCHEMATICS_PREFIX + Structures.SCHEMATICS_SEPARATOR))
            {
                name = Structures.SCHEMATICS_PREFIX + Structures.SCHEMATICS_SEPARATOR + name;
            }
            prefix = Structures.SCHEMATICS_PREFIX;
        }

        name = name.substring(prefix.length() + 1);
        final int lastSeparator = name.lastIndexOf(Structures.SCHEMATICS_SEPARATOR);
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
        if (prefix.equals(Structures.SCHEMATICS_PREFIX))
        {
            hut = schematic.split("\\d+")[0];
            section = Structures.SCHEMATICS_PREFIX;

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
     *
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
            return prefix + Structures.SCHEMATICS_SEPARATOR + schematic;
        }
        return prefix + Structures.SCHEMATICS_SEPARATOR + style + Structures.SCHEMATICS_SEPARATOR + schematic;
    }
}
