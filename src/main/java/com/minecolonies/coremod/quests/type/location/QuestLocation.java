package com.minecolonies.coremod.quests.type.location;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IQuestGiver;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

/**
 * Determines where a quest should appear
 */
public class QuestLocation implements IQuestLocation
{
    BlockPos buildingPos = BlockPos.ZERO;

    @Override
    public IQuestGiver getQuestGiverForColony(final IColony colony)
    {
        if (buildingPos != BlockPos.ZERO)
        {
            final IBuilding building = colony.getBuildingManager().getBuilding(buildingPos);
            if (building == null)
            {
                buildingPos = BlockPos.ZERO;
                // Cancel quest?
            }
        }

        return null;
    }

    @Override
    public void setBuildingLocation(final BlockPos pos)
    {
        buildingPos = pos;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = new CompoundNBT();
        BlockPosUtil.write(nbt, "pos", buildingPos);
        return null;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        buildingPos = BlockPosUtil.read(nbt, "pos");
    }
}
