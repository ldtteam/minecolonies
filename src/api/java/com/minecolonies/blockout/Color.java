package com.minecolonies.blockout;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Color utility methods.
 */
public final class Color
{
    private static final Map<String, Integer> nameToColorMap = new HashMap<>();
    static
    {
        //  Would love to load these from a file
        nameToColorMap.put("aqua", 0x00FFFF);
        nameToColorMap.put("black", 0x000000);
        nameToColorMap.put("blue", 0x0000FF);
        nameToColorMap.put("cyan", 0x00FFFF);
        nameToColorMap.put("fuchsia", 0xFF00FF);
        nameToColorMap.put("green", 0x008000);
        nameToColorMap.put("ivory", 0xFFFFF0);
        nameToColorMap.put("lime", 0x00FF00);
        nameToColorMap.put("magenta", 0xFF00FF);
        nameToColorMap.put("orange", 0xFFA500);
        nameToColorMap.put("orangered", 0xFF4500);
        nameToColorMap.put("purple", 0x800080);
        nameToColorMap.put("red", 0xFF0000);
        nameToColorMap.put("white", 0xFFFFFF);
        nameToColorMap.put("yellow", 0xFFFF00);
        nameToColorMap.put("gray", 0x808080);
        nameToColorMap.put("darkgray", 0xA9A9A9);
        nameToColorMap.put("dimgray", 0x696969);
        nameToColorMap.put("lightgray", 0xD3D3D3);
        nameToColorMap.put("slategray", 0x708090);
        nameToColorMap.put("darkgreen", 0x006400);
    }
    private Color()
    {
        // Hides default constructor.
    }

    /**
     * Get a color integer from its name.
     *
     * @param name name of the color.
     * @param def  default to use if the name doesn't exist.
     * @return the color as an integer.
     */
    public static int getByName(final String name, final int def)
    {
        final Integer i = nameToColorMap.get(name.toLowerCase(Locale.ENGLISH));
        return i != null ? i : def;
    }
}
