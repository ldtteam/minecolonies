package com.minecolonies.coremod.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Interface of the Quest manager of each colony.
 */
public interface IQuestManager extends INBTSerializable<CompoundTag>
{
    /**
     * All quests that exist.
     */
   Map<UUID, IQuest> GLOBAL_SERVER_QUESTS = new HashMap<>();

    /**
     * On each colony tick.
     */
    void onColonyTick();
}
