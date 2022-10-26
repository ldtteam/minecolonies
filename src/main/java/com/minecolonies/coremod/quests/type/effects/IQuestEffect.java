package com.minecolonies.coremod.quests.type.effects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

public interface IQuestEffect extends INBTSerializable<CompoundTag>
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
    default void deserializeNBT(final CompoundTag nbt)
    {

    }

    /**
     * @return
     */
    default CompoundTag serializeNBT()
    {
        return new CompoundTag();
    }
}
