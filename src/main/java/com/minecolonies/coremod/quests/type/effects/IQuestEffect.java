package com.minecolonies.coremod.quests.type.effects;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

public interface IQuestEffect extends INBTSerializable<CompoundNBT>
{
    /**
     * Gets the quest effects ID
     *
     * @return res location id
     */
    ResourceLocation getID();

    /**
     * Called on quest start
     */
    default void onStart() {}

    /**
     * Called on quest completion
     */
    default void onFinish() {}

    /**
     * Called on quest cancellation
     */
    default void onCancel() {}

    /**
     * @param nbt
     */
    default void deserializeNBT(final CompoundNBT nbt)
    {

    }

    /**
     * @return
     */
    default CompoundNBT serializeNBT()
    {
        return new CompoundNBT();
    }
}
