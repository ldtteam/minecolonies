package com.minecolonies.util;

import java.nio.file.Path;
import java.util.*;

/**
 * Created by Dars on 9/21/2016.
 */
public class StructureInfo
{

    /**
     * The name this schematic has.
     */
    private String name;

    /**
     * The category  of the structure.
     */
    private String category = "hut";

    private Boolean isHut = false;

    /**
     * style, level, path
     */
    private final Map<String, TreeMap<Integer, Path>> resourceLocationsByStyleAndLevel = new HashMap<>();

    /**
     *
     * @param name the name of the structure (hut or decoration)
     * @param isHut is it a hut- meaning does it have a hut block
     * @param category the category the builders tool should display it under
     */
    public StructureInfo(String name, Boolean isHut, String category)
    {
        this.name = name;
        this.isHut = isHut;
        this.category=category;
    }

    public String getName()
    {
        return this.name;
    }

    public String getCategory()
    {
        return this.category;
    }

    public int getMaxLevel(String style)
    {
        if(!this.resourceLocationsByStyleAndLevel.containsKey(style))
        {
            return 0;
        }
        Integer maxLevel =
          Collections.max(this.resourceLocationsByStyleAndLevel.get(style).keySet());
        return maxLevel;
    }

    public Boolean getIsHut()
    {
        return this.isHut;
    }

    /**
     *
     * @param style the name of the style ...classic
     * @param level the level as taken from the file name or 1
     * @param path the path to the files on the system or in the jar.
     */
    public void addLevel(String style, int level, Path path)
    {
        if (!this.resourceLocationsByStyleAndLevel.containsKey(style))
        {
            this.resourceLocationsByStyleAndLevel.put(style, new TreeMap<>());
        }

        if(!this.resourceLocationsByStyleAndLevel.get(style).containsKey(level)){
            this.resourceLocationsByStyleAndLevel.get(style).put(level,path);
        }
    }

    public List<String> getStyles()
    {
        return new ArrayList<>(this.resourceLocationsByStyleAndLevel.keySet());
    }
}
