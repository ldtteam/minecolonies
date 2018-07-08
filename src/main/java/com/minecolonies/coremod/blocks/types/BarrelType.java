package com.minecolonies.coremod.blocks.types;

import net.minecraft.block.material.MapColor;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Types that the {@link BlockPaperwall} supports
 */
public enum BarrelType implements IStringSerializable
{
    ZERO(0, "0perc", MapColor.WOOD),
    TWENTY(1, "20perc", MapColor.WOOD),
    FORTY(2, "40perc", MapColor.WOOD),
    SIXTY(3, "60perc", MapColor.WOOD),
    EIGHTY(4, "80perc", MapColor.WOOD),
    HUNDRED(5, "100perc", MapColor.WOOD),
    WORKING(6, "working", MapColor.WOOD),
    DONE(7, "done", MapColor.WOOD),;

    private static final BarrelType[] META_LOOKUP = new BarrelType[values().length];
    static
    {
        for (final BarrelType enumtype : values())
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

    BarrelType(final int metaIn, final String nameIn, final MapColor mapColorIn)
    {
        this(metaIn, nameIn, nameIn, mapColorIn);
    }

    BarrelType(final int metaIn, final String nameIn, final String unlocalizedNameIn, final MapColor mapColorIn)
    {
        this.meta = metaIn;
        this.name = nameIn;
        this.unlocalizedName = unlocalizedNameIn;
        this.mapColor = mapColorIn;
    }

    public static BarrelType byMetadata(final int meta)
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
