package com.minecolonies.api.tileentities;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_SIZE;

public abstract class AbstractTileEntityGrave extends TileEntity implements INamedContainerProvider
{
    /**
     * The inventory of the tileEntity.
     */
    protected ItemStackHandler inventory;

    public AbstractTileEntityGrave(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
        inventory = createInventory(DEFAULT_SIZE);
    }

    /**
     * Grave inventory type.
     */
    public class GraveInventory extends ItemStackHandler
    {
        public GraveInventory(final int defaultSize)
        {
            super(defaultSize);
        }
    }

    /**
     * Create the inventory that belongs to the grave.
     * @param slots the number of slots.
     * @return the created inventory,
     */
    public abstract ItemStackHandler createInventory(final int slots);

    /**
     * Check if a similar/same item as the stack is in the inventory. This method checks the content list, it is therefore extremely fast.
     *
     * @param stack             the stack to check.
     * @param count             the min count it should have.
     * @param ignoreDamageValue ignore the damage value.
     * @return true if so.
     */
    public abstract boolean hasItemStack(ItemStack stack, final int count, boolean ignoreDamageValue);

    /**
     * Check if a similar/same item as the stack is in the inventory. And return the count if so.
     *
     * @param stack             the stack to check.
     * @param ignoreDamageValue ignore the damage value.
     * @param ignoreNBT           if nbt should be ignored.
     * @return the quantity or 0.
     */
    public abstract int getCount(ItemStack stack, boolean ignoreDamageValue, final boolean ignoreNBT);

    /**
     * Check if a similar/same item as the stack is in the inventory. This method checks the content list, it is therefore extremely fast.
     *
     * @param itemStackSelectionPredicate the predicate to test the stack against.
     * @return true if so.
     */
    public abstract boolean hasItemStack(@NotNull Predicate<ItemStack> itemStackSelectionPredicate);

    /**
     * Check if a similar stack is in the grave.
     *
     * @param stack stack to check.
     * @return a set of different results depending on the similarity metric.
     */
    public abstract boolean hasSimilarStack(@NotNull ItemStack stack);

    /* Get the amount of items matching a predicate in the inventory.
     * @param predicate the predicate.
     * @return the total count.
     */
    public abstract int getItemCount(Predicate<ItemStack> predicate);

    /**
     * Get all the content stack  in the grave.
     *
     * @return the map of content.
     */
    public abstract Map<ItemStorage, Integer> getAllContent();

    /**
     * Checks if the chest is empty. This method checks the content list, it is therefore extremely fast.
     *
     * @return true if so.
     */
    public abstract boolean isEmpty();

    public IItemHandlerModifiable getInventory()
    {
        return inventory;
    }
}
