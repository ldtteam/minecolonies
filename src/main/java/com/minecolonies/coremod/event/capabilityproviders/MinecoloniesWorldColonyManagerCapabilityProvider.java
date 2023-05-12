package com.minecolonies.coremod.event.capabilityproviders;

import com.minecolonies.coremod.colony.IColonyManagerCapability;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

import static com.minecolonies.coremod.MineColonies.COLONY_MANAGER_CAP;

/**
 * Capability provider for the world capability of Minecolonies.
 */
public class MinecoloniesWorldColonyManagerCapabilityProvider implements ICapabilitySerializable<Tag>
{
    /**
     * The chunk map capability optional.
     */
    private final LazyOptional<IColonyManagerCapability> colonyManagerOptional;

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
        this.colonyManagerOptional = LazyOptional.of(() -> colonyManager);
    }

    @Override
    public Tag serializeNBT()
    {
        return IColonyManagerCapability.Storage.writeNBT(COLONY_MANAGER_CAP, colonyManager, null);
    }

    @Override
    public void deserializeNBT(final Tag nbt)
    {
        IColonyManagerCapability.Storage.readNBT(COLONY_MANAGER_CAP, colonyManager, null, nbt);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final Direction dir)
    {
        return cap == COLONY_MANAGER_CAP ? colonyManagerOptional.cast() : LazyOptional.empty();
    }
}