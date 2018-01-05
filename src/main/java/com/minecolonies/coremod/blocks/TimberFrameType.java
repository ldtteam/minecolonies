package com.minecolonies.coremod.blocks;

import net.minecraft.block.material.MapColor;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

//types that TimberFrame uses

public enum TimberFrameType implements IStringSerializable
{
    PLAIN(0, "plain", MapColor.WOOD),
    DOUBLECROSSED(1, "doublecrossed", MapColor.WOOD),
    FRAMED(2, "framed", MapColor.WOOD),
    SIDEFRAMED(3, "sideframed", MapColor.WOOD),
    GATEFRAMED(4, "gateframed", MapColor.WOOD),
    ONECROSSEDLR(5, "onecrossedlr", MapColor.WOOD),
    ONECROSSEDRL(6, "onecrossedrl", MapColor.WOOD),
    DOWNGATED(7, "downgated", MapColor.WOOD),
    HORIZONTALPLAIN(8, "horizontalplain", MapColor.WOOD),
    HORIZONTALNOCAP(9, "horizontalnocap", MapColor.WOOD);
    private static final TimberFrameType[] META_LOOKUP = new TimberFrameType[values().length];
    static
    {
        for (TimberFrameType enumtype : values())
        {
            META_LOOKUP[enumtype.getMetadata()] = enumtype;
        }
    }
    private final int      meta;
    private final String   name;
    private final String   unlocalizedName;
    private final MapColor mapColor;

    TimberFrameType(final int metaIn, final String nameIn, final MapColor mapColorIn)
    {
        this(metaIn, nameIn, nameIn, mapColorIn);
    }
    TimberFrameType(final int metaIn, final String nameIn, final String unlocalizedNameIn, final MapColor mapColorIn)
    {
        this.meta = metaIn;
        this.name = nameIn;
        this.unlocalizedName = unlocalizedNameIn;
        this.mapColor = mapColorIn;
    }
    public static TimberFrameType byMetadata(int meta)
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
