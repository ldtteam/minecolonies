package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IAltersRequiredItems;
import com.minecolonies.api.colony.buildings.modules.IModuleWithExternalBlocks;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.BuildingConstants.FUEL_LIST;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Module for all workers that need a furnace.
 */
public class FurnaceUserModule extends AbstractBuildingModule implements IPersistentModule, IModuleWithExternalBlocks, IAltersRequiredItems
{
    /**
     * Tag to store the furnace position.
     */
    private static final String TAG_POS = "pos";

    /**
     * Tag to store the furnace position in compatibility (Baker)
     */
    private static final String TAG_POS_COMPAT = "furnacePos";

    /**
     * Tag to store the furnace list.
     */
    private static final String TAG_FURNACES = "furnaces";

    /**
     * List of registered furnaces.
     */
    private final List<BlockPos> furnaces = new ArrayList<>();

    /**
     * Construct a new furnace user module.
     */
    public FurnaceUserModule()
    {
        super();
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        final ListNBT furnaceTagList = compound.getList(TAG_FURNACES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < furnaceTagList.size(); ++i)
        {
            if(furnaceTagList.getCompound(i).contains(TAG_POS))
            {
                furnaces.add(NBTUtil.readBlockPos(furnaceTagList.getCompound(i).getCompound(TAG_POS)));
            }
            if(furnaceTagList.getCompound(i).contains(TAG_POS_COMPAT))
            {
                furnaces.add(NBTUtil.readBlockPos(furnaceTagList.getCompound(i).getCompound(TAG_POS_COMPAT)));
            }
        }
    }

    @Override
    public void serializeNBT(final CompoundNBT compound)
    {
        @NotNull final ListNBT furnacesTagList = new ListNBT();
        for (@NotNull final BlockPos entry : furnaces)
        {
            @NotNull final CompoundNBT furnaceCompound = new CompoundNBT();
            furnaceCompound.put(TAG_POS, NBTUtil.writeBlockPos(entry));
            furnacesTagList.add(furnaceCompound);
        }
        compound.put(TAG_FURNACES, furnacesTagList);
    }

    @Override
    public void alterItemsToBeKept(final TriConsumer<Predicate<ItemStack>, Integer, Boolean> consumer)
    {
        consumer.accept(this::isAllowedFuel, STACKSIZE * building.getBuildingLevel(), false);

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
     * Check if an ItemStack is one of the accepted fuel items.
     *
     * @param stack the itemStack to check.
     * @return true if so.
     */
    public boolean isAllowedFuel(final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return false;
        }
        return building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(FUEL_LIST)).isItemInList(new ItemStorage(stack));
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
    public void onBlockPlacedInBuilding(@NotNull final BlockState blockState, @NotNull final BlockPos pos, @NotNull final World world)
    {
        if (blockState.getBlock() instanceof FurnaceBlock && !furnaces.contains(pos))
        {
            furnaces.add(pos);
        }
    }

    @Override
    public List<BlockPos> getRegisteredBlocks()
    {
        return new ArrayList<>(furnaces);
    }
}
