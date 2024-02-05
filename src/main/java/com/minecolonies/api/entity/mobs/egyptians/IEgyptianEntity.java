package com.minecolonies.api.entity.mobs.egyptians;

import net.minecraft.commands.CommandSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.neoforge.common.capabilities.ICapabilitySerializable;

public interface IEgyptianEntity extends Enemy, CommandSource, ICapabilitySerializable<CompoundTag>
{
}
