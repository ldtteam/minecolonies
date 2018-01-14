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
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.ORES;
import static com.minecolonies.api.util.constant.Constants.SAPLINGS;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * CompatabilityManager handlign certain list and maps of itemStacks of certain types.
 */
public class CompatabilityManager implements ICompatabilityManager
{
    /**
     * BiMap of saplings and leaves.
     */
    private final BiMap<IBlockState, ItemStorage> leavesToSaplingMap = HashBiMap.create();

    /**
     * List of saplings.
     */
    private final List<ItemStorage> saplings = new ArrayList<>();

    /**
     * List of all ore-like blocks.
     */
    private final List<IBlockState> ores = new ArrayList<>();

    /**
     * The oredict entry of an ore.
     */
    public static final String ORE_STRING = "ore";

    @Override
    public void discover(final World world)
    {
        discoverSaplings();
        for (final String string : OreDictionary.getOreNames())
        {
            if(string.contains(ORE_STRING))
            {
                discoverOres(world, string);
            }
        }
    }

    @Override
    public IBlockState getLeaveForSapling(final ItemStack stack)
    {
        if (leavesToSaplingMap.inverse().containsKey(new ItemStorage(stack)))
        {
            return leavesToSaplingMap.inverse().get(new ItemStorage(stack));
        }
        return null;
    }

    @Override
    public ItemStack getSaplingForLeave(final IBlockState block)
    {
        final ItemStack stack = new ItemStack(block.getBlock(), 1, block.getBlock().getMetaFromState(block));
        if(!ItemStackUtils.isEmpty(stack))
        {
            final IBlockState tempLeave = BlockLeaves.getBlockFromItem(stack.getItem()).getStateFromMeta(stack.getMetadata());
            if (leavesToSaplingMap.containsKey(tempLeave))
            {
                return leavesToSaplingMap.get(tempLeave).getItemStack();
            }
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
        final ItemStack tempStack = new ItemStack(block.getBlock(), 1, block.getBlock().getMetaFromState(block));

        if(!ItemStackUtils.isEmpty(tempStack))
        {
            final Block tempBlock = BlockLeaves.getBlockFromItem(tempStack.getItem());
            if(tempBlock != null)
            {
                final IBlockState temp = tempBlock.getStateFromMeta(tempStack.getMetadata());
                return ores.contains(temp);
            }
        }
        return false;
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
                leavesToSaplingMap.entrySet().stream().map(entry -> writeLeaveSaplingEntryToNBT(entry.getKey(), entry.getValue())).collect(NBTUtils.toNBTTagList());
        compound.setTag(TAG_SAP_LEAVE, saplingsLeavesTagList);

        @NotNull final NBTTagList saplingTagList =
                saplings.stream().map(sap -> sap.getItemStack().writeToNBT(new NBTTagCompound())).collect(NBTUtils.toNBTTagList());
        compound.setTag(TAG_SAPLINGS, saplingTagList);

        @NotNull final NBTTagList oresTagList = ores.stream().map(ore -> NBTUtil.writeBlockState(new NBTTagCompound(), ore)).collect(NBTUtils.toNBTTagList());
        compound.setTag(TAG_ORES, oresTagList);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        leavesToSaplingMap.putAll(NBTUtils.streamCompound(compound.getTagList(TAG_SAP_LEAVE, Constants.NBT.TAG_COMPOUND))
                .map(CompatabilityManager::readLeaveSaplingEntryFromNBT)
                .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond)));
        saplings.addAll(NBTUtils.streamCompound(compound.getTagList(TAG_SAPLINGS, Constants.NBT.TAG_COMPOUND))
                .map(tempCompound -> new ItemStorage(ItemStack.loadItemStackFromNBT(tempCompound)))
                .collect(Collectors.toList()));

        ores.addAll(NBTUtils.streamCompound(compound.getTagList(TAG_ORES, Constants.NBT.TAG_COMPOUND))
                .map(NBTUtil::readBlockState)
                .collect(Collectors.toList()));
    }

    @Override
    public void connectLeaveToSapling(final IBlockState leave, final ItemStack stack)
    {
        final ItemStack tempStack = new ItemStack(leave.getBlock(), 1, leave.getBlock().getMetaFromState(leave));
        final IBlockState tempLeave = BlockLeaves.getBlockFromItem(tempStack.getItem()).getStateFromMeta(tempStack.getMetadata());
        if (!leavesToSaplingMap.containsKey(tempLeave) && !leavesToSaplingMap.containsValue(new ItemStorage(stack)))
        {
            leavesToSaplingMap.put(tempLeave, new ItemStorage(stack));
            if(!saplings.contains(new ItemStorage(stack)))
            {
                saplings.add(new ItemStorage(stack));
            }
        }
    }

    //------------------------------- Private Utility Methods -------------------------------//

    private void discoverOres(final World world, final String string)
    {
        for (final ItemStack stack : OreDictionary.getOres(string))
        {
            if (!ItemStackUtils.isEmpty(stack) && stack.getItem() instanceof ItemBlock)
            {
                final IBlockState state = ((ItemBlock) stack.getItem()).getBlock()
                        .getStateForPlacement(world, BlockPos.ORIGIN, EnumFacing.NORTH, 0, 0, 0, stack.getMetadata(), null);
                if (!ores.contains(state))
                {
                    ores.add(state);
                }
            }
        }
        Log.getLogger().info("Finished discovering ores");
    }

    private void discoverSaplings()
    {
        for (final ItemStack saps : OreDictionary.getOres(SAPLINGS))
        {
            //Just put it in if not in there already, don't mind the leave yet.
            if (!ItemStackUtils.isEmpty(saps) && !leavesToSaplingMap.containsValue(new ItemStorage(saps)) && !saplings.contains(saps))
            {
                saplings.add(new ItemStorage(saps));
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
        return new Tuple<>(NBTUtil.readBlockState(compound), new ItemStorage(ItemStack.loadItemStackFromNBT(compound)));
    }
}
