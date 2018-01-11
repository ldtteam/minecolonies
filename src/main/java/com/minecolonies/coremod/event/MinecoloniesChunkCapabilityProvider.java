package com.minecolonies.coremod.event;

import com.minecolonies.api.colony.IColonyTagCapability;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

public class MinecoloniesChunkCapabilityProvider implements ICapabilitySerializable<NBTBase>
{
    private final IColonyTagCapability colonyList;

    public MinecoloniesChunkCapabilityProvider()
    {
        this.colonyList = new IColonyTagCapability.Impl();
    }

    @Override
    public NBTBase serializeNBT()
    {
        return CLOSE_COLONY_CAP.getStorage().writeNBT(CLOSE_COLONY_CAP, colonyList, null);
    }

    @Override
    public void deserializeNBT(final NBTBase nbt)
    {
        CLOSE_COLONY_CAP.getStorage().readNBT(CLOSE_COLONY_CAP, colonyList, null, nbt);
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing)
    {
        return capability == CLOSE_COLONY_CAP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing)
    {
        return capability == CLOSE_COLONY_CAP ? CLOSE_COLONY_CAP.cast(colonyList) : null;
    }
}