package com.minecolonies.coremod.event.capabilityproviders;

import com.minecolonies.coremod.colony.IColonyManagerCapability;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.minecolonies.coremod.MineColonies.COLONY_MANAGER_CAP;

/**
 * Capability provider for the world capability of Minecolonies.
 */
public class MinecoloniesWorldColonyManagerCapabilityProvider implements ICapabilitySerializable<NBTBase>
{
    /**
     * The chunk map capability.
     */
    private final IColonyManagerCapability colonyManager;

    /**
     * Constructor of the provider.
     */
    public MinecoloniesWorldColonyManagerCapabilityProvider()
    {
        this.colonyManager = new IColonyManagerCapability.Impl();
    }

    @Override
    public NBTBase serializeNBT()
    {
        return COLONY_MANAGER_CAP.getStorage().writeNBT(COLONY_MANAGER_CAP, colonyManager, null);
    }

    @Override
    public void deserializeNBT(final NBTBase nbt)
    {
        COLONY_MANAGER_CAP.getStorage().readNBT(COLONY_MANAGER_CAP, colonyManager, null, nbt);
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing)
    {
        return capability == COLONY_MANAGER_CAP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing)
    {
        return capability == COLONY_MANAGER_CAP ? COLONY_MANAGER_CAP.cast(colonyManager) : null;
    }
}