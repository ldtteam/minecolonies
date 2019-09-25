package com.minecolonies.api.entity.mobs.pirates;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public interface IPirateEntity extends IMob, ICommandSender, ICapabilitySerializable<NBTTagCompound>
{
}
