package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.Colony;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for all buildings which require a filterable list of allowed items.
 */
public abstract class AbstractFilterableListBuilding extends AbstractBuildingWorker
{
    /**
     * Tag to store the item list.
     */
    private static final String TAG_ITEMLIST = "itemList";

    /**
     * List of allowed items.
     */
    private final List<ItemStorage> itemsAllowed = new ArrayList<>();

    /**
     * The constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public AbstractFilterableListBuilding(@NotNull final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public void deserializeNBT(final NBTTagCompound compound)
    {
        super.deserializeNBT(compound);

        final NBTTagList itemsToCompost = compound.getTagList(TAG_ITEMLIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < itemsToCompost.tagCount(); ++i)
        {
            try
            {
                itemsAllowed.add(new ItemStorage(new ItemStack(itemsToCompost.getCompoundTagAt(i))));
            }
            catch (Exception e)
            {
                Log.getLogger().info("Removing incompatible stack");
            }
        }
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compound = super.serializeNBT();
        @NotNull final NBTTagList itemsToCompost = new NBTTagList();
        for(@NotNull final ItemStorage entry : itemsAllowed)
        {
            @NotNull final NBTTagCompound itemCompound = new NBTTagCompound();
            entry.getItemStack().writeToNBT(itemCompound);
            itemsToCompost.appendTag(itemCompound);
        }
        compound.setTag(TAG_ITEMLIST, itemsToCompost);
        return compound;
    }

    /**
     * Add a compostable item to the list.
     * @param item the item to add.
     */
    public void addItem(final ItemStorage item)
    {
        if(!itemsAllowed.contains(item))
        {
            itemsAllowed.add(item);
        }
    }

    /**
     * Check if the item is an allowed item.
     * @param item the item to check.
     * @return true if so.
     */
    public boolean isAllowedItem(final ItemStorage item)
    {
        return itemsAllowed.contains(item);
    }

    /**
     * Remove a compostable item from the list.
     * @param item the item to remove.
     */
    public void removeItem(final ItemStorage item)
    {
        itemsAllowed.remove(item);
    }

    /**
     * Getter of copy of all allowed items.
     * @return a copy.
     */
    public List<ItemStorage> getCopyOfAllowedItems()
    {
        return new ArrayList<>(itemsAllowed);
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeInt(itemsAllowed.size());
        for (final ItemStorage item : itemsAllowed)
        {
            ByteBufUtils.writeItemStack(buf, item.getItemStack());
        }
    }
}
