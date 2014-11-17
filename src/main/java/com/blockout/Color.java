package com.blockout;

import java.util.HashMap;
import java.util.Map;

public class Color
{
    private static Map<String, Integer> nameToColorMap = new HashMap<String, Integer>();

    public static int getByName(String name, int def)
    {
        Integer i = nameToColorMap.get(name.toLowerCase());
        return i != null ? i : def;
    }

    static
    {
        //  Would love to load these from a file
        nameToColorMap.put("aqua",      0x00FFFF);
        nameToColorMap.put("black",     0x000000);
        nameToColorMap.put("blue",      0x0000FF);
        nameToColorMap.put("cyan",      0x00FFFF);
        nameToColorMap.put("fuchsia",   0xFF00FF);
        nameToColorMap.put("green",     0x008000);
        nameToColorMap.put("ivory",     0xFFFFF0);
        nameToColorMap.put("lime",      0x00FF00);
        nameToColorMap.put("magenta",   0xFF00FF);
        nameToColorMap.put("orange",    0xFFA500);
        nameToColorMap.put("orangered", 0xFF4500);
        nameToColorMap.put("purple",    0x800080);
        nameToColorMap.put("red",       0xFF0000);
        nameToColorMap.put("white",     0xFFFFFF);
        nameToColorMap.put("yellow",    0xFFFF00);
    }
}
