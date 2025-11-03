package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.core.tileentities.TileEntityEnchanter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockHutEnchanter extends AbstractBlockHut
{
    public BlockHutEnchanter(final Properties properties)
    {
        super(properties);
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.enchanter.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos blockPos, @NotNull final BlockState blockState)
    {
        final TileEntityEnchanter building = new TileEntityEnchanter(blockPos, blockState);
        building.registryName = this.getBuildingEntry().getRegistryName();
        return building;
    }
}
