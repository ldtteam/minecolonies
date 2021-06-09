package com.minecolonies.api.blocks.types;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the types of Racks that the {@link AbstractBlockMinecoloniesRack} supports.
 */
public enum RackType implements IStringSerializable
{
    DEFAULT("blockrackemptysingle", "emptysingle"),
    FULL( "blockrackfullsingle", "fullsingle"),
    DEFAULTDOUBLE( "blockrackempty", "empty"),
    FULLDOUBLE( "blockrackfull", "full"),
    EMPTYAIR( "blockrackair", "dontrender");

    private final String name;
    private final String unlocalizedName;

    RackType(final String name, final String unlocalizedName)
    {
        this.name = name;
        this.unlocalizedName = unlocalizedName;
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

    @NotNull
    @Override
    public String getSerializedName()
    {
        return getName();
    }
}
