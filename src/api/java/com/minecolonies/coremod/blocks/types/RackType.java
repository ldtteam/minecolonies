package com.minecolonies.coremod.blocks.types;

import com.minecolonies.coremod.blocks.AbstractBlockMinecoloniesRack;
import net.minecraft.util.IStringSerializable;

/**
 * Defines the types of Racks that the {@link AbstractBlockMinecoloniesRack} supports.
 */
public enum RackType implements IStringSerializable
{
    DEFAULT(0, "blockrackemptysingle", "emptysingle"),
    FULL(1, "blockrackfullsingle", "fullsingle"),
    DEFAULTDOUBLE(2, "blockrackempty", "empty"),
    FULLDOUBLE(3, "blockrackfull", "full"),
    EMPTYAIR(4, "blockrackair", "dontrender");

    private static final RackType[] META_LOOKUP = new RackType[values().length];
    static
    {
        for (final RackType blockRack : values())
        {
            META_LOOKUP[blockRack.getMetadata()] = blockRack;
        }
    }
    private final int    meta;
    private final String name;
    private final String unlocalizedName;

    RackType(final int meta, final String name, final String unlocalizedName)
    {
        this.meta = meta;
        this.name = name;
        this.unlocalizedName = unlocalizedName;
    }

    public static RackType byMetadata(final int meta)
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
}
