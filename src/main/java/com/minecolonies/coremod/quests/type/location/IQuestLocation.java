package com.minecolonies.coremod.quests.type.location;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IQuestGiver;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Conditions for appearing at a certain place
 */
public interface IQuestLocation extends INBTSerializable<CompoundNBT>
{
    /**
     * Check if the colony now has a fitting location to put the quest on, if so attach the quest to questgiver
     */
    IQuestGiver getQuestGiverForColony(final IColony colony);

    /**
     * Set the building location of the quest giver to attach, gets removed when the building is no longer found
     *
     * @param pos building pos
     */
    void setBuildingLocation(final BlockPos pos);
}
