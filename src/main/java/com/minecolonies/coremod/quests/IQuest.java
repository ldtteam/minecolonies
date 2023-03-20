package com.minecolonies.coremod.quests;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.quests.type.sideeffects.IQuestSideEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Quest instance
 */
public interface IQuest extends INBTSerializable<CompoundTag>
{
    /**
     * Triggered when the quest is accepted
     *
     * @param player player accepting
     */
    void onStart(final Player player, final IColony colony);

    /**
     * Get the id of the quest giver.
     *
     * @return the id of the quest giver.
     */
    int getQuestGiver();

    /**
     * Check if the expiration date relative to the colony day count was reached.
     * @param colony the colony to check the validity for.
     * @return true if so.
     */
    boolean isValid(IColony colony);

    /**
     * This is where we actually check if the nbt of the colony fulfills the quest requirements.
     * @param colonyTag the colony tag to check.
     * @return true if so.
     */
    boolean canStart(CompoundTag colonyTag);
}
