package com.minecolonies.api.entity.mobs.vikings;

import net.minecraft.commands.CommandSource;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public interface INorsemenEntity extends Enemy, CommandSource, ICapabilitySerializable<CompoundTag>
{

}
