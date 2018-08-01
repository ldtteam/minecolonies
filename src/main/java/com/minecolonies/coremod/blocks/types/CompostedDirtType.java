package com.minecolonies.coremod.blocks.types;

import net.minecraft.block.material.MapColor;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

public enum CompostedDirtType implements IStringSerializable
{
    NOT_COMPOSTED(0, "notcomposted", MapColor.DIRT),
    COMPOSTED(1, "composted", MapColor.DIRT);

    private static final CompostedDirtType[] META_LOOKUP = new CompostedDirtType[values().length];
    static
    {
        for (final CompostedDirtType enumtype : values())
        {
            META_LOOKUP[enumtype.getMetadata()] = enumtype;
        }
    }
    private final int      meta;
    private final String   name;
    private final String   unlocalizedName;

    private final MapColor mapColor;

    /***
     * Constructor for the BarrelType
     * @param metaIn the metadata
     * @param nameIn the name
     * @param mapColorIn the color
     */
    CompostedDirtType(final int metaIn, final String nameIn, final MapColor mapColorIn)
    {
        this(metaIn, nameIn, nameIn, mapColorIn);
    }

    /***
     * Constructor for the BarrelType
     * @param metaIn the metadata
     * @param nameIn the name
     * @param unlocalizedNameIn the unlocalized name
     * @param mapColorIn the color
     */
    CompostedDirtType(final int metaIn, final String nameIn, final String unlocalizedNameIn, final MapColor mapColorIn)
    {
        this.meta = metaIn;
        this.name = nameIn;
        this.unlocalizedName = unlocalizedNameIn;
        this.mapColor = mapColorIn;
    }

    /**
     * Returns a type by a given metadata
     * @param meta the metadata
     * @return the type
     */
    public static CompostedDirtType byMetadata(final int meta)
    {
        int tempMeta = meta;
        if (tempMeta < 0 || tempMeta >= META_LOOKUP.length)
        {
            tempMeta = 0;
        }

        return META_LOOKUP[tempMeta];
    }

    /***
     * Returns the metadata
     * @return the metadata of the type
     */
    public int getMetadata()
    {
        return this.meta;
    }

    /***
     * Returns the color that represents the entry on the map
     * @return the color
     */
    public MapColor getMapColor()
    {
        return this.mapColor;
    }

    /***
     * Override for the toString method
     * @return the name of the type
     */
    @Override
    public String toString()
    {
        return this.name;
    }

    /***
     * Returns the name
     * @return the name of the type
     */
    @NotNull
    public String getName()
    {
        return this.name;
    }

    /***
     * Returns the unlocalized name
     * @return the unlocalized name of the type
     */
    public String getUnlocalizedName()
    {
        return this.unlocalizedName;
    }
}
