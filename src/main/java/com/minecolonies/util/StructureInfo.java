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
    private final Map<String, TreeMap<Integer, Path>> resourceLocationsByLevel = new HashMap<>();

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

    public int getMaxLevel()
    {
        final Comparator<Map<Integer,Path>> valueComparator =
          new Comparator<Map<Integer,Path>>() {
              public int compare(Map<Integer,Path> k1, Map<Integer,Path> k2) {
                  int compare =
                    ((Integer)k1.size()).compareTo((k2.size()));
                  if (compare == 0)
                      return 1;
                  else
                      return compare;
              }
          };

          Optional<TreeMap<Integer,Path>> levelControl= this.resourceLocationsByLevel.values().stream()
                       .max(valueComparator);
        if(levelControl.isPresent())
        {
            return levelControl.get().size();
        }
        return 0;
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
        if (!this.resourceLocationsByLevel.containsKey(style))
        {
            this.resourceLocationsByLevel.put(style, new TreeMap<>());
        }

        if(!this.resourceLocationsByLevel.get(style).containsKey(level)){
            this.resourceLocationsByLevel.get(style).put(level,path);
        }
    }

    public List<String> getStyles()
    {
        return new ArrayList<>(this.resourceLocationsByLevel.keySet());
    }
}
