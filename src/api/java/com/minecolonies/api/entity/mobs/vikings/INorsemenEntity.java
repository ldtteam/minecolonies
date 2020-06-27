package com.minecolonies.api.entity.mobs.vikings;

import net.minecraft.command.ICommandSource;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public interface INorsemenEntity extends IMob, ICommandSource, ICapabilitySerializable<CompoundNBT>
{
    
}
