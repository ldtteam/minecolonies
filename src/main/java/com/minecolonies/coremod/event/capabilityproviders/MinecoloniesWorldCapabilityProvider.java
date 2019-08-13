package com.minecolonies.coremod.event.capabilityproviders;

import com.minecolonies.api.colony.IChunkmanagerCapability;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.minecolonies.coremod.MineColonies.CHUNK_STORAGE_UPDATE_CAP;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Capability provider for the world capability of Minecolonies.
 */
public class MinecoloniesWorldCapabilityProvider implements ICapabilitySerializable<INBT>
{
    /**
     * The chunk map capability.
     */
    private final IChunkmanagerCapability chunkMap;

    /**
     * Constructor of the provider.
     */
    public MinecoloniesWorldCapabilityProvider()
    {
        this.chunkMap = new IChunkmanagerCapability.Impl();
    }

    @Override
    public INBT serializeNBT()
    {
        return CHUNK_STORAGE_UPDATE_CAP.getStorage().writeNBT(CHUNK_STORAGE_UPDATE_CAP, chunkMap, null);
    }

    @Override
    public void deserializeNBT(final INBT nbt)
    {
        CHUNK_STORAGE_UPDATE_CAP.getStorage().readNBT(CHUNK_STORAGE_UPDATE_CAP, chunkMap, null, nbt);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final Direction direction)
    {
        return cap == CHUNK_STORAGE_UPDATE_CAP ? LazyOptional.of(() -> (T) chunkMap) : LazyOptional.empty();
    }
}