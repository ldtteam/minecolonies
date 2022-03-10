package com.minecolonies.api.entity.mobs.amazons;

import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

/**
 * A tagging interface for Amazon Entities implements IMob, ICommandSource and ICapabilitySerializable<CompoundNBT>
 */
public interface IAmazonEntity extends Enemy, ICapabilitySerializable<CompoundTag>
{

}
