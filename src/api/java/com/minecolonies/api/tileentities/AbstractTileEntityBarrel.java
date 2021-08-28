package com.minecolonies.api.tileentities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public abstract class AbstractTileEntityBarrel extends BlockEntity implements TickableBlockEntity
{
    /**
     * The number of items it needs to start composting
     */
    public static final int MAX_ITEMS = 64;

    public AbstractTileEntityBarrel(final BlockEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    /**
     * Returns the number of items that the block contains
     *
     * @return the number of items
     */
    public abstract int getItems();

    public abstract boolean isDone();

    public abstract boolean checkIfWorking();

    public abstract boolean addItem(ItemStack item);

    public abstract ItemStack retrieveCompost(double multiplier);
}
