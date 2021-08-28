package com.minecolonies.api.blocks.types;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesGrave;
import net.minecraft.util.StringRepresentable;

/**
 * Defines the types of Grave that the {@link AbstractBlockMinecoloniesGrave} supports.
 */
public enum GraveType implements StringRepresentable
{
    DEFAULT(0, "blockgravedefault", "default"),
    DECAYED(1, "blockgravedecayed", "decayed");

    private static final GraveType[] META_LOOKUP = new GraveType[values().length];
    static
    {
        for (final GraveType blockGrave : values())
        {
            META_LOOKUP[blockGrave.getMetadata()] = blockGrave;
        }
    }
    private final int    meta;
    private final String name;
    private final String unlocalizedName;

    GraveType(final int meta, final String name, final String unlocalizedName)
    {
        this.meta = meta;
        this.name = name;
        this.unlocalizedName = unlocalizedName;
    }

    public static GraveType byMetadata(final int meta)
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

    @Override
    public String toString()
    {
        return this.name;
    }

    public String getName()
    {
        return this.name;
    }

    public String getTranslationKey()
    {
        return this.unlocalizedName;
    }

    @Override
    public String getSerializedName()
    {
        return getName();
    }
}
