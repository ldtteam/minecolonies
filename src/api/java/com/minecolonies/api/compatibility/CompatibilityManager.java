package com.minecolonies.api.compatibility;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.configuration.Configurations;
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
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;
import static com.minecolonies.api.util.constant.Constants.ORE_STRING;
import static com.minecolonies.api.util.constant.Constants.SAPLINGS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_SAP_LEAF;

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
     * List of all the items that can be composted
     */
    private final List<ItemStorage> compostableItems = new ArrayList<>();

    /**
     * List of lucky ores which get dropped by the miner.
     */
    private final List<ItemStorage> luckyOres = new ArrayList<>();

    /**
     * If discovery is finished already.
     */
    private boolean discoveredAlready = false;

    /**
     * Random obj.
     */
    private static final Random random = new Random();

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
        for (final String string : OreDictionary.getOreNames())
        {
            if (string.contains(ORE_STRING))
            {
                discoverOres(string);
            }
        }
        Log.getLogger().info("Finished discovering ores");
        discoverCompostableItems();
        discoverLuckyOres();

        discoveredAlready = true;
    }

    @Override
    public boolean isCompost(final ItemStack itemStack)
    {
        if (itemStack.isEmpty())
        {
            return false;
        }

        for (final String string : Configurations.gameplay.listOfCompostableItems)
        {
            if (itemStack.getItem().getRegistryName().toString().equals(string))
            {
                return true;
            }
            for (final int id : OreDictionary.getOreIDs(itemStack))
            {
                if (OreDictionary.getOreName(id).equals(string))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isLuckyBlock(final ItemStack itemStack)
    {
        if (itemStack.isEmpty())
        {
            return false;
        }

        for (final String string : Configurations.gameplay.luckyBlocks)
        {
            if (itemStack.getItem().getRegistryName().toString().equals(string))
            {
                return true;
            }
            for (final int id : OreDictionary.getOreIDs(itemStack))
            {
                if (OreDictionary.getOreName(id).equals(string))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public IBlockState getLeafForSapling(final ItemStack stack)
    {
        if (leavesToSaplingMap.inverse().containsKey(new ItemStorage(stack, false, true)))
        {
            return leavesToSaplingMap.inverse().get(new ItemStorage(stack, false, true));
        }
        return null;
    }

    @Override
    public ItemStack getSaplingForLeaf(final IBlockState block)
    {
        final ItemStack stack = new ItemStack(block.getBlock(), 1, block.getBlock().getMetaFromState(block));
        final IBlockState tempLeaf = BlockLeaves.getBlockFromItem(stack.getItem()).getStateFromMeta(stack.getMetadata());
        if (leavesToSaplingMap.containsKey(tempLeaf))
        {
            return leavesToSaplingMap.get(tempLeaf).getItemStack();
        }
        return null;
    }

    @Override
    public List<ItemStorage> getCopyOfSaplings()
    {
        return new ArrayList<>(saplings);
    }

    @Override
    public List<ItemStorage> getCopyOfCompostableItems()
    {
        return ImmutableList.copyOf(compostableItems);
    }

    @Override
    public boolean isOre(final IBlockState block)
    {
        if (block.getBlock() instanceof BlockOre || block.getBlock() instanceof BlockRedstoneOre)
        {
            return true;
        }

        return ores.contains(block.getBlock());
    }

    @Override
    public boolean isOre(@NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return false;
        }
        final int[] ids = OreDictionary.getOreIDs(stack);
        for (final int id : ids)
        {
            if (OreDictionary.getOreName(id).contains(ORE_STRING))
            {
                return !FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty();
            }
        }
        return false;
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        @NotNull final NBTTagList saplingsLeavesTagList =
          leavesToSaplingMap.entrySet().stream().map(entry -> writeLeafSaplingEntryToNBT(entry.getKey(), entry.getValue())).collect(NBTUtils.toNBTTagList());
        compound.setTag(TAG_SAP_LEAF, saplingsLeavesTagList);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        NBTUtils.streamCompound(compound.getTagList(TAG_SAP_LEAF, Constants.NBT.TAG_COMPOUND))
          .map(CompatibilityManager::readLeafSaplingEntryFromNBT)
          .filter(key -> !leavesToSaplingMap.containsKey(key.getFirst()) && !leavesToSaplingMap.containsValue(key.getSecond()))
          .forEach(key -> leavesToSaplingMap.put(key.getFirst(), key.getSecond()));
    }

    @Override
    public void connectLeafToSapling(final IBlockState leaf, final ItemStack stack)
    {
        final ItemStack tempStack = new ItemStack(leaf.getBlock(), 1, leaf.getBlock().getMetaFromState(leaf));
        final IBlockState tempLeaf = BlockLeaves.getBlockFromItem(tempStack.getItem()).getStateFromMeta(tempStack.getMetadata());
        if (!leavesToSaplingMap.containsKey(tempLeaf) && !leavesToSaplingMap.containsValue(new ItemStorage(stack, false, true)))
        {
            leavesToSaplingMap.put(tempLeaf, new ItemStorage(stack, false, true));
        }
    }

    @Override
    public boolean isDiscoveredAlready()
    {
        return discoveredAlready;
    }

    @Override
    public ItemStack getRandomLuckyOre()
    {
        if (random.nextInt(ONE_HUNDRED_PERCENT) <= Configurations.gameplay.luckyBlockChance)
        {
            Collections.shuffle(luckyOres);
            return luckyOres.get(0).getItemStack().copy();
        }
        return ItemStack.EMPTY;
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
                        if (!ores.contains(block))
                        {
                            ores.add(block);
                        }
                    }
                }
            }
        }

        for (final String oreString : Configurations.gameplay.extraOres)
        {
            final Block block = Block.getBlockFromName(oreString);
            if (!(block == null || ores.contains(block)))
            {
                ores.add(block);
            }
        }
    }

    private void discoverSaplings()
    {
        for (final ItemStack saps : OreDictionary.getOres(SAPLINGS))
        {
            if (saps.getHasSubtypes())
            {
                for (final CreativeTabs tabs : CreativeTabs.CREATIVE_TAB_ARRAY)
                {
                    final NonNullList<ItemStack> list = NonNullList.create();
                    saps.getItem().getSubItems(tabs, list);
                    for (final ItemStack stack : list)
                    {
                        //Just put it in if not in there already, don't mind the leaf yet.
                        if (!ItemStackUtils.isEmpty(stack) && !leavesToSaplingMap.containsValue(new ItemStorage(stack, false, true)) && !saplings.contains(new ItemStorage(stack,
                          false,
                          true)))
                        {
                            saplings.add(new ItemStorage(stack, false, true));
                        }
                    }
                }
            }
            else
            {
                // Dynamictree's saplings dont have sub types
                if (Compatibility.isDynamicTreeSapling(saps) && !ItemStackUtils.isEmpty(saps) && !leavesToSaplingMap.containsValue(new ItemStorage(saps, false, true))
                      && !saplings.contains(new ItemStorage(saps, false, true)))
                {
                    saplings.add(new ItemStorage(saps, false, true));
                }
            }
        }
        Log.getLogger().info("Finished discovering saplings");
    }

    private void discoverCompostableItems()
    {
        if (compostableItems.isEmpty())
        {
            compostableItems.addAll(
              ImmutableList.copyOf(StreamSupport.stream(Spliterators.spliteratorUnknownSize(Item.REGISTRY.iterator(), Spliterator.ORDERED), false).flatMap(item -> {
                  final NonNullList<ItemStack> stacks = NonNullList.create();
                  try
                  {
                      item.getSubItems(CreativeTabs.SEARCH, stacks);
                  }
                  catch (Exception ex)
                  {
                      Log.getLogger().warn("Failed to get sub items from: " + item.getRegistryName());
                  }


                  return stacks.stream().filter(this::isCompost);
              }).map(ItemStorage::new).collect(Collectors.toList())));
        }
        Log.getLogger().info("Finished discovering compostables");
    }

    /**
     * Run through all blocks and check if they match one of our lucky ores.
     */
    private void discoverLuckyOres()
    {
        if (luckyOres.isEmpty())
        {
            for (final String ore : Configurations.gameplay.luckyOres)
            {
                final String[] split = ore.split("!");
                if (split.length < 2)
                {
                    Log.getLogger().warn("Wrong configured ore: " +  ore);
                    continue;
                }

                int meta = 0;
                if (split.length == 3)
                {
                    try
                    {
                        meta = Integer.parseInt(split[1]);
                    }
                    catch (final NumberFormatException ex)
                    {
                        Log.getLogger().warn("Ore has invalid metadata: " + ore);
                    }
                }

                final Item item = Item.getByNameOrId(split[0]);
                if (item == null || item == Items.AIR)
                {
                    Log.getLogger().warn("Invalid lucky block: " + ore);
                    continue;
                }

                final ItemStack stack = new ItemStack(item, 1, meta);
                try
                {
                    final int rarity = Integer.parseInt(split[split.length - 1]);
                    for (int i = 0; i < rarity; i++)
                    {
                        luckyOres.add(new ItemStorage(stack));
                    }
                }
                catch (final NumberFormatException ex)
                {
                    Log.getLogger().warn("Ore has invalid rarity: " + ore);
                }
            }
        }
        Log.getLogger().info("Finished discovering lucky ores");
    }

    private static NBTTagCompound writeLeafSaplingEntryToNBT(final IBlockState state, final ItemStorage storage)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        NBTUtil.writeBlockState(compound, state);
        storage.getItemStack().writeToNBT(compound);
        return compound;
    }

    private static Tuple<IBlockState, ItemStorage> readLeafSaplingEntryFromNBT(final NBTTagCompound compound)
    {
        return new Tuple<>(NBTUtil.readBlockState(compound), new ItemStorage(new ItemStack(compound), false, true));
    }
}
