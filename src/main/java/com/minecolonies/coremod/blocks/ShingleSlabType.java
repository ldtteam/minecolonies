package com.minecolonies.coremod.blocks;

import net.minecraft.block.material.MapColor;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Types that the {@link BlockShingleSlab} supports
 */
public enum ShingleSlabType implements IStringSerializable
{
    TOP(0, "top", MapColor.WOOD),
    ONE_WAY(1, "oneway", MapColor.OBSIDIAN),
    TWO_WAY(2, "twoway", MapColor.SAND),
    THREE_WAY(3, "threeway", MapColor.GOLD),
    FOUR_WAY(4, "fourway", MapColor.BROWN),
    CURVED(5, "curved", MapColor.DIRT);

    private static final ShingleSlabType[] META_LOOKUP = new ShingleSlabType[values().length];
    static
    {
        for (final ShingleSlabType enumtype : values())
        {
            META_LOOKUP[enumtype.getMetadata()] = enumtype;
        }
    }
    private final int      meta;
    private final String   name;
    private final String   unlocalizedName;
    /**
     * The color that represents this entry on a map.
     */
    private final MapColor mapColor;

    ShingleSlabType(final int metaIn, final String nameIn, final MapColor mapColorIn)
    {
        this(metaIn, nameIn, nameIn, mapColorIn);
    }

    ShingleSlabType(final int metaIn, final String nameIn, final String unlocalizedNameIn, final MapColor mapColorIn)
    {
        this.meta = metaIn;
        this.name = nameIn;
        this.unlocalizedName = unlocalizedNameIn;
        this.mapColor = mapColorIn;
    }

    public static ShingleSlabType byMetadata(final int meta)
    {
        int tempMeta = meta;
        if (tempMeta < 0 || tempMeta >= META_LOOKUP.length)
        {
            tempMeta = 0;
        }

        return META_LOOKUP[tempMeta];
    }

    public int getMetadata()
    {
        return this.meta;
    }

    /**
     * The color which represents this entry on a map.
     */
    public MapColor getMapColor()
    {
        return this.mapColor;
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    @NotNull
    public String getName()
    {
        return this.name;
    }

    public String getUnlocalizedName()
    {
        return this.unlocalizedName;
    }
}
