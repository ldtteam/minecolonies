package com.minecolonies.api.blocks.types;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the types of Racks that the {@link AbstractBlockMinecoloniesRack} supports.
 */
public enum RackType implements StringRepresentable
{
    EMPTY("blockrackemptysingle", "emptysingle", false),
    FULL( "blockrackfullsingle", "fullsingle", false),
    EMPTY_DOUBLE("blockrackempty", "empty", true),
    FULL_DOUBLE("blockrackfull", "full", true),
    NO_RENDER("blockrackair", "dontrender", true);

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
            case FULL, EMPTY ->
            {
                return empty ? EMPTY : FULL;
            }
            case EMPTY_DOUBLE, FULL_DOUBLE ->
            {
                return empty ? EMPTY_DOUBLE : FULL_DOUBLE;
            }
            default ->
            {
                return NO_RENDER;
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
