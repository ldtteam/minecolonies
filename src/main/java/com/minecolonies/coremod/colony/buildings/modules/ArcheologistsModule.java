package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.coremod.entity.ai.citizen.archeologist.StructureTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;

import java.sql.Struct;

public class ArcheologistsModule extends AbstractBuildingModule implements IPersistentModule
{

    private StructureTarget target;

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
                  NbtUtils.readBlockPos(structureTarget.getCompound(NbtTagConstants.TAG_STRUCTURE_POS))
                );
            }
            else
            {
                target = null;
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
            moduleTag.put(NbtTagConstants.TAG_STRUCTURE_TARGET, tag);
        }

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
        }
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
}
