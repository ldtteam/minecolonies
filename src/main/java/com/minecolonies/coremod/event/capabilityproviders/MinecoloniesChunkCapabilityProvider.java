package com.minecolonies.coremod.event.capabilityproviders;

import com.minecolonies.api.colony.IColonyTagCapability;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;

/**
 * Capability provider for the chunk capability of Minecolonies.
 */
public class MinecoloniesChunkCapabilityProvider implements ICapabilitySerializable<Tag>
{
    /**
     * The colony list capability. (For closest colony and claimed)
     */
    private final IColonyTagCapability tag;

    /**
     * The colony list capability optional.
     */
    private final LazyOptional<IColonyTagCapability> tagOptional;

    /**
     * Constructor of the provider.
     */
    public MinecoloniesChunkCapabilityProvider()
    {
        this.tag = new IColonyTagCapability.Impl();
        this.tagOptional = LazyOptional.of(() -> tag);
    }

    @Override
    public Tag serializeNBT()
    {
        return IColonyTagCapability.Storage.writeNBT(CLOSE_COLONY_CAP, tag, null);
    }

    @Override
    public void deserializeNBT(final Tag nbt)
    {
        IColonyTagCapability.Storage.readNBT(CLOSE_COLONY_CAP, tag, null, nbt);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final Direction direction)
    {
        return cap == CLOSE_COLONY_CAP ? tagOptional.cast() : LazyOptional.empty();
    }
}
