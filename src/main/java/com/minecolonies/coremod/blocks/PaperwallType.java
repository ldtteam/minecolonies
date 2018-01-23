package com.minecolonies.coremod.blocks;

import net.minecraft.block.material.MapColor;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Types that the {@link BlockPaperwall} supports
 */
public enum PaperwallType implements IStringSerializable
{
    OAK(0, "oak", MapColor.WOOD),
    SPRUCE(1, "spruce", MapColor.OBSIDIAN),
    BIRCH(2, "birch", MapColor.SAND),
    JUNGLE(3, "jungle", MapColor.DIRT);

    private static final PaperwallType[] META_LOOKUP = new PaperwallType[values().length];
    static
    {
        for (final PaperwallType enumtype : values())
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

    PaperwallType(final int metaIn, final String nameIn, final MapColor mapColorIn)
    {
        this(metaIn, nameIn, nameIn, mapColorIn);
    }

    PaperwallType(final int metaIn, final String nameIn, final String unlocalizedNameIn, final MapColor mapColorIn)
    {
        this.meta = metaIn;
        this.name = nameIn;
        this.unlocalizedName = unlocalizedNameIn;
        this.mapColor = mapColorIn;
    }

    public static PaperwallType byMetadata(final int meta)
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
