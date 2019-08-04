package com.minecolonies.coremod.event.capabilityproviders;

import com.minecolonies.api.colony.IColonyTagCapability;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Capability provider for the chunk capability of Minecolonies.
 */
public class MinecoloniesChunkCapabilityProvider implements ICapabilitySerializable<INBT>
{
    /**
     * The colony list capability. (For closest colony and claimed)
     */
    private final IColonyTagCapability colonyList;

    /**
     * Constructor of the provider.
     */
    public MinecoloniesChunkCapabilityProvider()
    {
        this.colonyList = new IColonyTagCapability.Impl();
    }

    @Override
    public INBT serializeNBT()
    {
        return CLOSE_COLONY_CAP.getStorage().writeNBT(CLOSE_COLONY_CAP, colonyList, null);
    }

    @Override
    public void deserializeNBT(final INBT nbt)
    {
        CLOSE_COLONY_CAP.getStorage().readNBT(CLOSE_COLONY_CAP, colonyList, null, nbt);
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final Direction facing)
    {
        return capability == CLOSE_COLONY_CAP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final Direction facing)
    {
        return capability == CLOSE_COLONY_CAP ? CLOSE_COLONY_CAP.cast(colonyList) : null;
    }
}