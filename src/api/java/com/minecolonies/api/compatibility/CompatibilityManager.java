package com.minecolonies.api.compatibility;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
     * What the crusher can work on.
     */
    private final Map<ItemStorage, ItemStorage> crusherModes = new HashMap<>();

    /**
     * The meshes the sifter is going to be able to use.
     */
    private final List<Tuple<ItemStorage, Double>> sifterMeshes = new ArrayList<>();

    /**
     * The blocks which can be sifted.
     */
    private final List<ItemStorage> sievableBlocks = new ArrayList<>();

    /**
     * Map of mash -> block -> sieveResult
     */
    private final Map<ItemStorage, Map<ItemStorage, List<ItemStorage>>> sieveResult = new HashMap<>();

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
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public List<Tuple<ItemStorage, Double>> getMeshes()
    {
        return new ArrayList<>(this.sifterMeshes);
    }

    @Override
    public ArrayList<ItemStorage> getSievableBlock()
    {
        return new ArrayList<>(this.sievableBlocks);
    }

    @Override
    public ItemStack getRandomSieveResultForMeshAndBlock(final ItemStorage mesh, final ItemStorage block)
    {
        if (this.sieveResult.containsKey(mesh) && this.sieveResult.get(mesh).containsKey(block))
        {
            final List<ItemStorage> drops = this.sieveResult.get(mesh).get(block);
            Collections.shuffle(drops);
            return drops.get(0).getItemStack();
        }
        return ItemStack.EMPTY;
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
        discoverCrusherModes();
        discoverSifting();

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

    /**
     * Getter for all the crusher modes.
     * @return an immutable copy of the map.
     */
    @Override
    public Map<ItemStorage, ItemStorage> getCrusherModes()
    {
        return ImmutableMap.<ItemStorage, ItemStorage>builder().putAll(this.crusherModes).build();
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

    /**
     * Method discovering and loading from the config all materials the sifter needs.
     */
    private void discoverSifting()
    {
        for (final String string : Configurations.gameplay.sifterMeshes)
        {
            final String[] mesh = string.split(",");

            if (mesh.length != 2)
            {
                Log.getLogger().warn("Couldn't parse the mesh: " +  string);
                continue;
            }

            try
            {
                final double probability = Double.parseDouble(mesh[1]);

                final String[] item = mesh[0].split(":");
                final String itemName = item[0] + ":" + item[1];
                final Item theItem = Item.getByNameOrId(itemName);

                if (theItem == null)
                {
                    Log.getLogger().warn("Couldn't find item for mesh: " + string);
                    continue;
                }

                final ItemStack stack = new ItemStack(theItem, 1, item.length > 2 ? Integer.parseInt(item[2]) : 0);
                sifterMeshes.add(new Tuple<>(new ItemStorage(stack), probability));
            }
            catch (final NumberFormatException ex)
            {
                Log.getLogger().warn("Couldn't retrieve probability for mesh: " + string, ex);
            }
        }

        for (final String string : Configurations.gameplay.siftableBlocks)
        {
            try
            {
                final String[] item = string.split(":");
                final String itemName = item[0] + ":" + item[1];
                final Item theItem = Item.getByNameOrId(itemName);

                if (theItem == null)
                {
                    Log.getLogger().warn("Couldn't find item for siftable block: " + string);
                    continue;
                }

                final ItemStack stack = new ItemStack(theItem, 1, item.length > 2 ? Integer.parseInt(item[2]) : 0);
                sievableBlocks.add(new ItemStorage(stack));
            }
            catch (final NumberFormatException ex)
            {
                Log.getLogger().warn("Couldn't retrieve the metadata for siftable block: " + string, ex);
            }
        }

        final Map<ItemStorage, Map<ItemStorage, Map<ItemStorage, Double>>> tempDrops = new HashMap<>();
        for (final String string : Configurations.gameplay.sifterDrops)
        {
            final String[] drop = string.split(",");
            if (drop.length != 4)
            {
                Log.getLogger().warn("Required Parameters: keyBlock, keyMesh, item, probability, not met in: " + string);
                continue;
            }

            try
            {
                final int block = Integer.parseInt(drop[0]);

                if (sievableBlocks.size() < block)
                {
                    Log.getLogger().warn("Trying to add siftresult for not configured block.");
                    continue;
                }

                final ItemStorage blockStorage = sievableBlocks.get(block);

                final int mesh = Integer.parseInt(drop[1]);

                if (sifterMeshes.size() < mesh)
                {
                    Log.getLogger().warn("Trying to add siftresult for not configured mesh.");
                    continue;
                }

                final ItemStorage meshStorage = sifterMeshes.get(mesh).getFirst();

                final String[] item = drop[2].split(":");
                final String itemName = item[0] + ":" + item[1];
                final Item theItem = Item.getByNameOrId(itemName);

                if (theItem == null)
                {
                    Log.getLogger().warn("Couldn't find item for siftable block: " + string);
                    continue;
                }

                final ItemStack stack = new ItemStack(theItem, 1, item.length > 2 ? Integer.parseInt(item[2]) : 0);

                final double probability = Double.parseDouble(drop[3]);

                final Map<ItemStorage, Map<ItemStorage, Double>> map;
                if (tempDrops.containsKey(meshStorage))
                {
                    map = tempDrops.get(meshStorage);
                }
                else
                {
                    map = new HashMap<>();
                }

                final Map<ItemStorage, Double> drops;
                if (map.containsKey(blockStorage))
                {
                    drops = map.get(blockStorage);
                }
                else
                {
                    drops = new HashMap<>();
                }

                drops.put(new ItemStorage(stack), probability);
                map.put(blockStorage, drops);
                tempDrops.put(meshStorage, map);
            }
            catch (final NumberFormatException ex)
            {
                Log.getLogger().warn("Couldn't retrieve block or mesh for drop: " + string, ex);
            }
        }

        for (final Map.Entry<ItemStorage, Map<ItemStorage, Map<ItemStorage, Double>>> meshEntry : tempDrops.entrySet())
        {
            for (final Map.Entry<ItemStorage, Map<ItemStorage, Double>> blockEntry : meshEntry.getValue().entrySet())
            {
                final List<ItemStorage> theDrops = new ArrayList<>();
                double probabilitySum = 0;
                for (final Map.Entry<ItemStorage, Double> drops : blockEntry.getValue().entrySet())
                {
                    final ItemStorage storage = drops.getKey();
                    final double probability = drops.getValue();
                    probabilitySum+=probability;
                    for (int i = 0; i < probability; i++)
                    {
                        theDrops.add(storage);
                    }
                }

                final ItemStorage airStorage = new ItemStorage(ItemStack.EMPTY);
                for (int i = 0; i < 100 - probabilitySum; i++)
                {
                    theDrops.add(airStorage);
                }

                final Map<ItemStorage, List<ItemStorage>> map;
                if (this.sieveResult.containsKey(meshEntry.getKey()))
                {
                    map = this.sieveResult.get(meshEntry.getKey());
                }
                else
                {
                    map = new HashMap<>();
                }

                map.put(blockEntry.getKey(), theDrops);
                this.sieveResult.put(meshEntry.getKey(), map);
            }
        }
        Log.getLogger().info("Finished initiating sifter config");
    }

    /**
     * Calculate the crusher modes from the config file.
     */
    private void discoverCrusherModes()
    {
        for (final String string : Configurations.gameplay.crusherProduction)
        {
            final String[] split = string.split("!");
            if (split.length != 2)
            {
                Log.getLogger().warn("Invalid crusher mode setting: " + string);
                continue;
            }

            final String[] firstItem = split[0].split(":");
            final String[] secondItem = split[1].split(":");

            final Item item1 = Item.getByNameOrId(firstItem[0] + ":" + firstItem[1]);
            final Item item2 = Item.getByNameOrId(secondItem[0] + ":" + secondItem[1]);

            try
            {
                final int meta1 = firstItem.length > 2 ? Integer.parseInt(firstItem[2]) : 0;
                final int meta2 = secondItem.length > 2 ? Integer.parseInt(secondItem[2]) : 0;

                if (item1 == null || item2 == null)
                {
                    Log.getLogger().warn("Invalid crusher mode setting: " + string);
                    continue;
                }

                final ItemStorage storage1 = new ItemStorage(new ItemStack(item1, 2, meta1));
                final ItemStorage storage2 = new ItemStorage(new ItemStack(item2, 1, meta2));
                crusherModes.put(storage1, storage2);
            }
            catch (final NumberFormatException ex)
            {
                Log.getLogger().warn("Error getting metaData", ex);
            }
        }
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
