package com.minecolonies.api.compatibility;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.ORE_STRING;
import static com.minecolonies.api.util.constant.Constants.SAPLINGS;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * CompatibilityManager handling certain list and maps of itemStacks of certain types.
 */
public class CompatibilityManager implements ICompatibilityManager
{
    /**
     * BiMap of saplings and leaves.
     */
    private final BiMap<IBlockState, ItemStorage> leavesToSaplingMap = HashBiMap.create();

    /**
     * List of saplings.
     * Works on client and server-side.
     */
    private final List<ItemStorage> saplings = new ArrayList<>();

    /**
     * List of all ore-like blocks.
     * Works on client and server-side.
     */
    private final List<Block> ores = new ArrayList<>();

    /**
     * Instantiates the compatibilityManager.
     */
    public CompatibilityManager()
    {

    }

    @Override
    public void discover()
    {
        discoverSaplings();
        for(final String string: OreDictionary.getOreNames())
        {
            if(string.contains(ORE_STRING))
            {
                discoverOres(string);
            }
        }
    }

    @Override
    public IBlockState getLeaveForSapling(final ItemStack stack)
    {
        if(leavesToSaplingMap.inverse().containsKey(new ItemStorage(stack, false, true)))
        {
            return leavesToSaplingMap.inverse().get(new ItemStorage(stack, false, true));
        }
        return null;
    }

    @Override
    public ItemStack getSaplingForLeave(final IBlockState block)
    {
        final ItemStack stack = new ItemStack(block.getBlock(), 1, block.getBlock().getMetaFromState(block));
        final IBlockState tempLeave = BlockLeaves.getBlockFromItem(stack.getItem()).getStateFromMeta(stack.getMetadata());
        if(leavesToSaplingMap.containsKey(tempLeave))
        {
            return leavesToSaplingMap.get(tempLeave).getItemStack();
        }
        return null;
    }

    @Override
    public List<ItemStorage> getCopyOfSaplings()
    {
        return new ArrayList<>(saplings);
    }

    @Override
    public boolean isOre(final IBlockState block)
    {
        if(block.getBlock() instanceof BlockOre || block.getBlock() instanceof BlockRedstoneOre)
        {
            return true;
        }

        return ores.contains(block.getBlock());
    }

    @Override
    public boolean isOre(@NotNull final ItemStack stack)
    {
        if(ItemStackUtils.isEmpty(stack))
        {
            return false;
        }
        final int[] ids = OreDictionary.getOreIDs(stack);
        for(final int id : ids)
        {
            if(OreDictionary.getOreName(id).contains(ORE_STRING))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        @NotNull final NBTTagList saplingsLeavesTagList =
                leavesToSaplingMap.entrySet().stream().map(entry ->  writeLeaveSaplingEntryToNBT(entry.getKey(), entry.getValue())).collect(NBTUtils.toNBTTagList());
        compound.setTag(TAG_SAP_LEAVE, saplingsLeavesTagList);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        NBTUtils.streamCompound(compound.getTagList(TAG_SAP_LEAVE, Constants.NBT.TAG_COMPOUND))
                .map(CompatibilityManager::readLeaveSaplingEntryFromNBT)
                .filter(key -> !leavesToSaplingMap.containsKey(key.getFirst()) && !leavesToSaplingMap.containsValue(key.getSecond()))
                .forEach(key -> leavesToSaplingMap.put(key.getFirst(), key.getSecond()));
    }

    @Override
    public void connectLeaveToSapling(final IBlockState leave, final ItemStack stack)
    {
        final ItemStack tempStack = new ItemStack(leave.getBlock(), 1, leave.getBlock().getMetaFromState(leave));
        final IBlockState tempLeave = BlockLeaves.getBlockFromItem(tempStack.getItem()).getStateFromMeta(tempStack.getMetadata());
        if(!leavesToSaplingMap.containsKey(tempLeave) && !leavesToSaplingMap.containsValue(new ItemStorage(stack, false, true)))
        {
            leavesToSaplingMap.put(tempLeave, new ItemStorage(stack, false, true));
        }
    }

    //------------------------------- Private Utility Methods -------------------------------//

    private void discoverOres(final String string)
    {
        for (final ItemStack ore : OreDictionary.getOres(string))
        {
            for (final CreativeTabs tabs : CreativeTabs.CREATIVE_TAB_ARRAY)
            {
                final NonNullList<ItemStack> list = NonNullList.create();
                ore.getItem().getSubItems(tabs, list);
                for (final ItemStack stack : list)
                {
                    if (!ItemStackUtils.isEmpty(stack) && stack.getItem() instanceof ItemBlock)
                    {
                        final Block block = ((ItemBlock) stack.getItem()).getBlock();
                        if(!ores.contains(block))
                        {
                            ores.add(block);
                        }
                    }
                }
            }
        }
        Log.getLogger().info("Finished discovering ores");
    }

    private void discoverSaplings()
    {
        for (final ItemStack saps : OreDictionary.getOres(SAPLINGS))
        {
            if (saps.getHasSubtypes())
            {
                for(final CreativeTabs tabs: CreativeTabs.CREATIVE_TAB_ARRAY)
                {
                    final NonNullList<ItemStack> list = NonNullList.create();
                    saps.getItem().getSubItems(tabs, list);
                    for (final ItemStack stack : list)
                    {
                        //Just put it in if not in there already, don't mind the leave yet.
                        if(!ItemStackUtils.isEmpty(stack) && !leavesToSaplingMap.containsValue(new ItemStorage(stack, false, true)) && !saplings.contains(new ItemStorage(stack, false, true)))
                        {
                            saplings.add(new ItemStorage(stack, false, true));
                        }
                    }
                }
            }
        }
        Log.getLogger().info("Finished discovering saplings");
    }

    private static NBTTagCompound writeLeaveSaplingEntryToNBT(final IBlockState state, final ItemStorage storage)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        NBTUtil.writeBlockState(compound, state);
        storage.getItemStack().writeToNBT(compound);
        return compound;
    }

    private static Tuple<IBlockState, ItemStorage> readLeaveSaplingEntryFromNBT(final NBTTagCompound compound)
    {
        return new Tuple<>(NBTUtil.readBlockState(compound), new ItemStorage(new ItemStack(compound), false, true));
    }
}
