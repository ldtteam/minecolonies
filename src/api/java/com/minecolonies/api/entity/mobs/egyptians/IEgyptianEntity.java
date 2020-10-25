package com.minecolonies.api.entity.mobs.egyptians;

import net.minecraft.command.ICommandSource;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public interface IEgyptianEntity extends IMob, ICommandSource, ICapabilitySerializable<CompoundNBT>
{
}
