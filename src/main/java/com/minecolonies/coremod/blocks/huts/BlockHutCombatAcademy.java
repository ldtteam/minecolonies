package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Block of the combat academy camp.
 */
public class BlockHutCombatAcademy extends AbstractBlockHut<BlockHutCombatAcademy>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockHutCombatAcademy";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.combatAcademy;
    }

    /**
     * This returns a complete list of items dropped from this block.
     * @deprecated but we still need it.
     * @param world   The current world
     * @param pos     Block position in world
     * @param state   Current state
     * @param fortune Breakers fortune level
     * @return A ArrayList containing all items this block drops
     */
    @NotNull
    @Override
    @Deprecated
    public List<ItemStack> getDrops(@NotNull final IBlockAccess world, @NotNull final BlockPos pos, @NotNull final BlockState state, final int fortune)
    {
        return Collections.emptyList();
    }
}
