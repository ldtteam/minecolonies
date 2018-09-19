package com.minecolonies.coremod.event.capabilityproviders;

import com.minecolonies.coremod.colony.IServerCapability;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.minecolonies.coremod.MineColonies.SERVER_CAPABILITY;

/**
 * Capability provider for the server capability
 */
public class MinecoloniesServerCapabilityProvider implements ICapabilitySerializable<NBTBase>
{
    /**
     * The server capability.
     */
    private final IServerCapability serverCapability;

    /**
     * Constructor of the provider.
     */
    public MinecoloniesServerCapabilityProvider()
    {
        this.serverCapability = new IServerCapability.Impl();
    }

    @Override
    public NBTBase serializeNBT()
    {
        return SERVER_CAPABILITY.getStorage().writeNBT(SERVER_CAPABILITY, serverCapability, null);
    }

    @Override
    public void deserializeNBT(final NBTBase nbt)
    {
        SERVER_CAPABILITY.getStorage().readNBT(SERVER_CAPABILITY, serverCapability, null, nbt);
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing)
    {
        return capability == SERVER_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing)
    {
        return capability == SERVER_CAPABILITY ? SERVER_CAPABILITY.cast(serverCapability) : null;
    }
}