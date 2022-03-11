package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;

public class ArcheologistsModule extends AbstractBuildingModule implements IPersistentModule
{
    private static final String TAG_ARCHEOLOGISTS_MODULE = "ArcheologistsModule";
    private static final String TAG_TARGET = "target";

    private BlockPos target;

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        if (compound.contains(TAG_ARCHEOLOGISTS_MODULE)) {
            final CompoundTag tag = compound.getCompound(TAG_ARCHEOLOGISTS_MODULE);
            if (tag.contains(TAG_TARGET))
            {
                target = NbtUtils.readBlockPos(tag.getCompound(TAG_TARGET));
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
            moduleTag.put(TAG_TARGET, NbtUtils.writeBlockPos(target));
        }

        compound.put(TAG_ARCHEOLOGISTS_MODULE, moduleTag);
    }

    @Override
    public void serializeToView(final FriendlyByteBuf buf)
    {
        buf.writeBoolean(getTarget() != null);
        if (getTarget() != null)
        {
            buf.writeBlockPos(getTarget());
        }
    }

    public BlockPos getTarget()
    {
        return target;
    }

    public void setTarget(final BlockPos target)
    {
        this.target = target;
        this.markDirty();
    }
}
