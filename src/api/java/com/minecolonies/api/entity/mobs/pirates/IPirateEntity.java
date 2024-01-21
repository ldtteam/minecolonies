package com.minecolonies.api.entity.mobs.pirates;

import net.minecraft.commands.CommandSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public interface IPirateEntity extends Enemy, CommandSource, ICapabilitySerializable<CompoundTag>
{
}
