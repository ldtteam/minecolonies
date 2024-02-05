package com.minecolonies.api.entity.mobs.amazons;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.neoforge.common.capabilities.ICapabilitySerializable;

/**
 * A tagging interface for Amazon Entities.
 */
public interface IAmazonEntity extends Enemy, ICapabilitySerializable<CompoundTag>
{

}
