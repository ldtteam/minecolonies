package com.minecolonies.core.quests.sideeffects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

public interface IQuestSideEffect extends INBTSerializable<CompoundTag>
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
     * Deserialize the quest side effect.
     * @param nbt the nbt to deserialize it from.
     */
    default void deserializeNBT(final CompoundTag nbt)
    {
        // noop
    }

    /**
     * Serialize the side effect to nbt.
     * @return the nbt to serialize it to.
     */
    default CompoundTag serializeNBT()
    {
        return new CompoundTag();
    }
}
