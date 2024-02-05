package com.minecolonies.api.entity.mobs.vikings;

import net.minecraft.commands.CommandSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.neoforge.common.capabilities.ICapabilitySerializable;

public interface INorsemenEntity extends Enemy, CommandSource, ICapabilitySerializable<CompoundTag>
{

}
