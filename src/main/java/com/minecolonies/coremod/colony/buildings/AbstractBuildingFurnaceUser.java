package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.colony.Colony;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
     * List of registered furnaces.
     */
    private final List<BlockPos> furnaces = new ArrayList<>();

    /**
     * Instantiates a new cook building.
     *
     * @param c the colony.
     * @param l the location
     */
    public AbstractBuildingFurnaceUser(final Colony c, final BlockPos l)
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
    public void deserializeNBT(final NBTTagCompound compound)
    {
        super.deserializeNBT(compound);
        final NBTTagList furnaceTagList = compound.getTagList(TAG_FURNACES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < furnaceTagList.tagCount(); ++i)
        {
            furnaces.add(NBTUtil.getPosFromTag(furnaceTagList.getCompoundTagAt(i).getCompoundTag(TAG_POS)));
        }
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compound = super.serializeNBT();

        @NotNull final NBTTagList furnacesTagList = new NBTTagList();
        for (@NotNull final BlockPos entry : furnaces)
        {
            @NotNull final NBTTagCompound furnaceCompound = new NBTTagCompound();
            furnaceCompound.setTag(TAG_POS, NBTUtil.createPosTag(entry));
            furnacesTagList.appendTag(furnaceCompound);
        }
        compound.setTag(TAG_FURNACES, furnacesTagList);

        return compound;
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block instanceof BlockFurnace && !furnaces.contains(pos))
        {
            furnaces.add(pos);
        }
        markDirty();
    }

    /**
     * Getter for all allowed fuel from the building.
     * @return the list of itemStacks.
     */
    public List<ItemStack> getAllowedFuel()
    {
        return getCopyOfAllowedItems().stream().map(ItemStorage::getItemStack).peek(stack -> stack.setCount(stack.getMaxStackSize())).collect(Collectors.toList());
    }
}
