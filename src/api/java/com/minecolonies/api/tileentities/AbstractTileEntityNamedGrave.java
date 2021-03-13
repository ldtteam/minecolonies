package com.minecolonies.api.tileentities;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.state.DirectionProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class AbstractTileEntityNamedGrave extends TileEntity
{
    /**
     * The position it faces.
     */
    public static final DirectionProperty FACING       = HorizontalBlock.HORIZONTAL_FACING;

    private String citizenName;

    public AbstractTileEntityNamedGrave(TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    public String getCitizenName()
    {
        return citizenName;
    }

    public void setCitizenName(final String citizenName)
    {
        this.citizenName = citizenName;
    }
}
