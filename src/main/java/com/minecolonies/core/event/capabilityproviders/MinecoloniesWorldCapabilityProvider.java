package com.minecolonies.core.event.capabilityproviders;

import com.minecolonies.api.colony.IChunkmanagerCapability;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ICapabilitySerializable;
import net.neoforged.neoforge.common.util.LazyOptional;
import javax.annotation.Nonnull;

import static com.minecolonies.core.MineColonies.CHUNK_STORAGE_UPDATE_CAP;

/**
 * Capability provider for the world capability of Minecolonies.
 */
public class MinecoloniesWorldCapabilityProvider implements ICapabilitySerializable<Tag>
{
    /**
     * The chunk map capability.
     */
    private final IChunkmanagerCapability chunkMap;

    /**
     * The chunk map capability optional.
     */
    private final LazyOptional<IChunkmanagerCapability> chunkMapOptional;

    /**
     * Constructor of the provider.
     */
    public MinecoloniesWorldCapabilityProvider()
    {
        this.chunkMap = new IChunkmanagerCapability.Impl();
        this.chunkMapOptional = LazyOptional.of(() -> chunkMap);
    }

    @Override
    public Tag serializeNBT()
    {
        return IChunkmanagerCapability.Storage.writeNBT(CHUNK_STORAGE_UPDATE_CAP, chunkMap, null);
    }

    @Override
    public void deserializeNBT(final Tag nbt)
    {
        IChunkmanagerCapability.Storage.readNBT(CHUNK_STORAGE_UPDATE_CAP, chunkMap, null, nbt);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final Direction direction)
    {
        return cap == CHUNK_STORAGE_UPDATE_CAP ? chunkMapOptional.cast() : LazyOptional.empty();
    }
}