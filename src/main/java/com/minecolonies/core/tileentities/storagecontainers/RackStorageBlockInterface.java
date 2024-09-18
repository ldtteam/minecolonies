package com.minecolonies.core.tileentities.storagecontainers;

import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.storageblocks.IStorageBlockInterface;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RackStorageBlockInterface implements IStorageBlockInterface
{
    @Override
    public boolean automaticallyAddToBuilding()
    {
        return true;
    }

    @Override
    public void setInWarehouse(BlockEntity blockEntity, final boolean inWarehouse)
    {
        getRack(blockEntity).setInWarehouse(inWarehouse);
    }

    @Override
    public int getUpgradeLevel(BlockEntity blockEntity)
    {
        return getRack(blockEntity).getUpgradeSize();
    }

    @Override
    public void increaseUpgradeLevel(BlockEntity blockEntity)
    {
        getRack(blockEntity).upgradeRackSize();
    }

    private AbstractTileEntityRack getRack(BlockEntity blockEntity) {
        return (AbstractTileEntityRack) blockEntity;
    }
}
