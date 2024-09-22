package com.minecolonies.core.tileentities.storageblocks;

import com.minecolonies.api.tileentities.storageblocks.IStorageBlockInterface;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class AbstractStorageBlockInterface implements IStorageBlockInterface {
    protected final BlockEntity targetBlockEntity;

    public AbstractStorageBlockInterface(BlockEntity targetBlockEntity) {
        this.targetBlockEntity = targetBlockEntity;
    }
}
