package com.minecolonies.core.event.capabilityproviders;

import com.minecolonies.core.colony.IColonyManagerCapability;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ICapabilitySerializable;
import net.neoforged.neoforge.common.util.LazyOptional;
import javax.annotation.Nonnull;

import static com.minecolonies.core.MineColonies.COLONY_MANAGER_CAP;

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
     * Is this the main overworld cap?
     */
    private final boolean overworld;

    /**
     * Constructor of the provider.
     */
    public MinecoloniesWorldColonyManagerCapabilityProvider(final boolean overworld)
    {
        this.colonyManager = new IColonyManagerCapability.Impl();
        this.colonyManagerOptional = LazyOptional.of(() -> colonyManager);
        this.overworld = overworld;
    }

    @Override
    public Tag serializeNBT()
    {
        return IColonyManagerCapability.Storage.writeNBT(COLONY_MANAGER_CAP, colonyManager, overworld);
    }

    @Override
    public void deserializeNBT(final Tag nbt)
    {
        IColonyManagerCapability.Storage.readNBT(COLONY_MANAGER_CAP, colonyManager, overworld, nbt);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final Direction dir)
    {
        return cap == COLONY_MANAGER_CAP ? colonyManagerOptional.cast() : LazyOptional.empty();
    }
}