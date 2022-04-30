package com.minecolonies.coremod.colony.buildings.modules;

import com.google.common.collect.Sets;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.loot.ModLootTables;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.coremod.entity.ai.citizen.archeologist.StructureTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.sql.Struct;
import java.util.Set;

public class ArcheologistsModule extends AbstractBuildingWithLootTableModule implements IPersistentModule
{

    private StructureTarget target;

    private final Set<BlockPos> previouslyVisitedStructures = Sets.newHashSet();

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        if (compound.contains(NbtTagConstants.TAG_ARCHEOLOGISTS_MODULE)) {
            final CompoundTag tag = compound.getCompound(NbtTagConstants.TAG_ARCHEOLOGISTS_MODULE);
            if (tag.contains(NbtTagConstants.TAG_STRUCTURE_TARGET))
            {
                final CompoundTag structureTarget = tag.getCompound(NbtTagConstants.TAG_STRUCTURE_TARGET);
                target = new StructureTarget(
                  NbtUtils.readBlockPos(structureTarget.getCompound(NbtTagConstants.TAG_SPAWN_TARGET)),
                  NbtUtils.readBlockPos(structureTarget.getCompound(NbtTagConstants.TAG_STRUCTURE_POS)),
                  new ResourceLocation(structureTarget.getString(NbtTagConstants.TAG_STRUCTURE_TYPE))
                );
            }
            else
            {
                target = null;
            }

            previouslyVisitedStructures.clear();
            if (compound.contains(NbtTagConstants.TAG_PREVIOUSLY_VISITED_STRUCTURES)) {
                final ListTag previouslyVisitedStructureData = compound.getList(NbtTagConstants.TAG_PREVIOUSLY_VISITED_STRUCTURES, Tag.TAG_COMPOUND);
                previouslyVisitedStructureData.stream()
                  .filter(CompoundTag.class::isInstance)
                  .map(CompoundTag.class::cast)
                  .map(NbtUtils::readBlockPos)
                  .forEach(previouslyVisitedStructures::add);
            }
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        IPersistentModule.super.serializeNBT(compound);

        final CompoundTag moduleTag = new CompoundTag();

        if (target != null)
        {
            final CompoundTag tag = new CompoundTag();
            tag.put(NbtTagConstants.TAG_SPAWN_TARGET, NbtUtils.writeBlockPos(target.workspaceSpawnTarget()));
            tag.put(NbtTagConstants.TAG_STRUCTURE_POS, NbtUtils.writeBlockPos(target.structureCenter()));
            tag.putString(NbtTagConstants.TAG_STRUCTURE_TYPE, target.name().toString());
            moduleTag.put(NbtTagConstants.TAG_STRUCTURE_TARGET, tag);
        }

        final ListTag previouslyVisitedStructureData = new ListTag();
        previouslyVisitedStructures
          .stream()
            .map(NbtUtils::writeBlockPos)
              .forEach(previouslyVisitedStructureData::add);

        moduleTag.put(NbtTagConstants.TAG_PREVIOUSLY_VISITED_STRUCTURES, previouslyVisitedStructureData);

        compound.put(NbtTagConstants.TAG_ARCHEOLOGISTS_MODULE, moduleTag);
    }

    @Override
    public void serializeToView(final FriendlyByteBuf buf)
    {
        buf.writeBoolean(target != null);
        if (target != null)
        {
            buf.writeBlockPos(target.workspaceSpawnTarget());
            buf.writeBlockPos(target.structureCenter());
            buf.writeResourceLocation(target.name());
        }
        buf.writeInt(previouslyVisitedStructures.size());
        previouslyVisitedStructures.forEach(buf::writeBlockPos);
    }

    public StructureTarget getTarget()
    {
        return target;
    }

    public void setTarget(final StructureTarget target)
    {
        this.target = target;
        this.markDirty();
    }

    public Set<BlockPos> getPreviouslyVisitedStructures()
    {
        return previouslyVisitedStructures;
    }

    public void addPreviousTarget(final BlockPos pos) {
        previouslyVisitedStructures.add(pos);
        this.markDirty();
    }

    public void addCurrentTargetToPreviousTargetsAndClearCurrentTarget() {
        if (target != null) {
            previouslyVisitedStructures.add(target.workspaceSpawnTarget());
            this.markDirty();
        }
        target = null;
    }

    public boolean hasVisitedBefore(final StructureTarget target) {
        return previouslyVisitedStructures.contains(target.workspaceSpawnTarget());
    }

    @Override
    public @NotNull ResourceLocation getDefaultLootTable()
    {
        return ModLootTables.ARCHEOLOGISTS_DEFAULT_LOOT_TABLE;
    }
}
