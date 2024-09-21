package com.minecolonies.core.colony.buildings.modules;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorkerModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingEventsModule;
import com.minecolonies.api.colony.buildings.modules.ICreatesResolversModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.modules.ITickingModule;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Assignment module for crafting workers.
 */
public class ConsensusBuildingModule extends WorkerBuildingModule implements IBuildingEventsModule, ITickingModule, IPersistentModule, IBuildingWorkerModule, ICreatesResolversModule
{
    private int blockIndex = 0;

    public ConsensusBuildingModule(final JobEntry entry,
      final Skill primary,
      final Skill secondary,
      final boolean canWorkingDuringRain,
      final Function<IBuilding, Integer> sizeLimit)
    {
        super(entry, primary, secondary, canWorkingDuringRain, sizeLimit);
    }

    @Override
    public void onColonyTick(final @NotNull IColony colony)
    {
        // Don't auto hire.
        return;
    }

    public int getBlockIndex()
    {
        return blockIndex;
    }

    public void incrementBlockIndex()
    {
        blockIndex++;
    }

    @Override
    public void serializeNBT(final HolderLookup.@NotNull Provider provider, final CompoundTag compound)
    {
        super.serializeNBT(provider, compound);
    }

    @Override
    public void deserializeNBT(final HolderLookup.@NotNull Provider provider, final CompoundTag compound)
    {
        super.deserializeNBT(provider, compound);
    }
}
