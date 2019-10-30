package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.tileentities.TileEntityEnchanter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BlockHutEnchanter extends AbstractBlockHut<BlockHutEnchanter>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutenchanter";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.enchanter;
    }

    @NotNull
    @Override
    public TileEntity createNewTileEntity(final World world, final int meta)
    {
        //Creates a tile entity for our building
        return new TileEntityEnchanter(getBuildingEntry().getRegistryName());
    }

}
