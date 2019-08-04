package com.minecolonies.api.entity.mobs.barbarians;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public interface IBarbarianEntity extends IMob, CommandSource, ICapabilitySerializable<CompoundNBT>
{
}
