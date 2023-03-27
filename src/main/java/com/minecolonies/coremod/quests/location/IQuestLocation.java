package com.minecolonies.coremod.quests.location;

import com.minecolonies.api.colony.IColony;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Conditions for appearing at a certain place
 */
public interface IQuestLocation extends INBTSerializable<CompoundTag>
{
    /**
     * Check if the colony now has a fitting location to put the quest on, if so attach the quest to questgiver
     */
   // IQuestGiver getQuestGiverForColony(final IColony colony);

    /**
     * Set the building location of the quest giver to attach, gets removed when the building is no longer found
     *
     * @param pos building pos
     */
    void setBuildingLocation(final BlockPos pos);
}
