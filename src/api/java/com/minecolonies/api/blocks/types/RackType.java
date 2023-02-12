package com.minecolonies.api.blocks.types;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the types of Racks that the {@link AbstractBlockMinecoloniesRack} supports.
 */
public enum RackType implements StringRepresentable
{
    DEFAULT("blockrackemptysingle", "emptysingle", false),
    FULL( "blockrackfullsingle", "fullsingle", false),
    DEFAULTDOUBLE( "blockrackempty", "empty", true),
    FULLDOUBLE( "blockrackfull", "full", true),
    EMPTYAIR( "blockrackair", "dontrender", true);

    private final String name;
    private final String unlocalizedName;

    private boolean doubleVariant = false;

    RackType(final String name, final String unlocalizedName, final boolean doubleVariant)
    {
        this.name = name;
        this.unlocalizedName = unlocalizedName;
        this.doubleVariant = doubleVariant;
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

    public boolean isDoubleVariant()
    {
        return doubleVariant;
    }

    public RackType getInvBasedVariant(final boolean empty)
    {
        switch (this)
        {
            case FULL, DEFAULT ->
            {
                return empty ? DEFAULT : FULL;
            }
            case DEFAULTDOUBLE, FULLDOUBLE ->
            {
                return empty ? DEFAULTDOUBLE : FULLDOUBLE;
            }
            default ->
            {
                return EMPTYAIR;
            }
        }
    }

    @NotNull
    @Override
    public String getSerializedName()
    {
        return getName();
    }
}
