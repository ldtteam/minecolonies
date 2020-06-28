package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.block.Block;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;

/**
 * Abstract Class for all furnace users.
 */
@SuppressWarnings(OVERRIDE_EQUALS)
public abstract class AbstractBuildingFurnaceUser extends AbstractFilterableListBuilding
{
    /**
     * Tag to store the furnace position.
     */
    private static final String TAG_POS = "pos";

    /**
     * Tag to store the furnace list.
     */
    private static final String TAG_FURNACES = "furnaces";

    /**
     * The list of fuel.
     */
    public static final String FUEL_LIST = "fuel";

    /**
     * List of registered furnaces.
     */
    private final List<BlockPos> furnaces = new ArrayList<>();

    /**
     * Instantiates a new cook building.
     *
     * @param c the colony.
     * @param l the location
     */
    public AbstractBuildingFurnaceUser(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Return a list of furnaces assigned to this hut.
     *
     * @return copy of the list
     */
    public List<BlockPos> getFurnaces()
    {
        return new ArrayList<>(furnaces);
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        final ListNBT furnaceTagList = compound.getList(TAG_FURNACES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < furnaceTagList.size(); ++i)
        {
            furnaces.add(NBTUtil.readBlockPos(furnaceTagList.getCompound(i).getCompound(TAG_POS)));
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        @NotNull final ListNBT furnacesTagList = new ListNBT();
        for (@NotNull final BlockPos entry : furnaces)
        {
            @NotNull final CompoundNBT furnaceCompound = new CompoundNBT();
            furnaceCompound.put(TAG_POS, NBTUtil.writeBlockPos(entry));
            furnacesTagList.add(furnaceCompound);
        }
        compound.put(TAG_FURNACES, furnacesTagList);

        return compound;
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block instanceof FurnaceBlock && !furnaces.contains(pos))
        {
            furnaces.add(pos);
        }
        markDirty();
    }

    /**
     * Remove a furnace from the building.
     *
     * @param pos the position of it.
     */
    public void removeFromFurnaces(final BlockPos pos)
    {
        furnaces.remove(pos);
    }

    /**
     * Getter for all allowed fuel from the building.
     * @return the list of itemStacks.
     */
    public List<ItemStack> getAllowedFuel()
    {
        return getCopyOfAllowedItems().containsKey(FUEL_LIST) ? getCopyOfAllowedItems().get(FUEL_LIST).stream().map(ItemStorage::getItemStack).peek(stack -> stack.setCount(stack.getMaxStackSize())).collect(Collectors.toList()) : Collections.emptyList();
    }

    /**
     * Check if an ItemStack is one of the accepted fuel items.
     *
     * @param stack the itemStack to check.
     * @return true if so.
     */
    public boolean isAllowedFuel(final ItemStack stack)
    {
        return getCopyOfAllowedItems().get(FUEL_LIST).stream().anyMatch(itemStack -> stack.isItemEqual(itemStack.getItemStack()));
    }
}
