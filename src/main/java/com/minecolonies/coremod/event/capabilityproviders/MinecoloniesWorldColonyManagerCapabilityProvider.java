package com.minecolonies.coremod.event.capabilityproviders;

import com.minecolonies.coremod.colony.IColonyManagerCapability;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.minecolonies.coremod.MineColonies.COLONY_MANAGER_CAP;

/**
 * Capability provider for the world capability of Minecolonies.
 */
public class MinecoloniesWorldColonyManagerCapabilityProvider implements ICapabilitySerializable<INBT>
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
    public INBT serializeNBT()
    {
        return COLONY_MANAGER_CAP.getStorage().writeNBT(COLONY_MANAGER_CAP, colonyManager, null);
    }

    @Override
    public void deserializeNBT(final INBT nbt)
    {
        COLONY_MANAGER_CAP.getStorage().readNBT(COLONY_MANAGER_CAP, colonyManager, null, nbt);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final Direction dir)
    {
        return cap == COLONY_MANAGER_CAP ? LazyOptional.of(() -> (T) colonyManager) : LazyOptional.empty();
    }
}