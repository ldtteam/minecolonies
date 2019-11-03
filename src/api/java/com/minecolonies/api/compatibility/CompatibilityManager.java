package com.minecolonies.api.compatibility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockStateStorage;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;
import static com.minecolonies.api.util.constant.Constants.ORE_STRING;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_SAP_LEAF;

/**
 * CompatibilityManager handling certain list and maps of itemStacks of certain types.
 */
public class CompatibilityManager implements ICompatibilityManager
{
    /**
     * BiMap of saplings and leaves.
     */
    private final Map<BlockStateStorage, ItemStorage> leavesToSaplingMap = new HashMap<>();

    /**
     * List of saplings.
     * Works on client and server-side.
     */
    private final List<ItemStorage> saplings = new ArrayList<>();

    /**
     * List of properties we're ignoring when comparing leaves.
     */
    private final List<IProperty> leafCompareWithoutProperties = ImmutableList.of(checkDecay, decayable, DYN_PROP_HYDRO,TREE_DISTANCE);

    /**
     * Properties for leaves we're ignoring upon comparing.
     */
    private static final BooleanProperty checkDecay     = BooleanProperty.create("check_decay");
    private static final BooleanProperty decayable      = BooleanProperty.create("decayable");
    public static final  IntegerProperty DYN_PROP_HYDRO = IntegerProperty.create("hydro", 1, 4);
    public static final  IntegerProperty TREE_DISTANCE = IntegerProperty.create("distance", 1, 7);

    /**
     * List of all ore-like blocks.
     * Works on client and server-side.
     */
    private final Set<Block> oreBlocks = new HashSet<>();

    /**
     * List of all ore-like items.
     */
    private final Set<ItemStorage> smeltableOres = new HashSet<>();

    /**
     * List of all the items that can be composted
     */
    private final Set<ItemStorage> compostableItems = new HashSet<>();

    /**
     * List of all the items that can be composted
     */
    private final Set<ItemStorage> plantables = new HashSet<>();

    /**
     * List of all the items that can be used as fuel
     */
    private final Set<ItemStorage> fuel = new HashSet<>();

    /**
     * List of all the items that can be used as food
     */
    private final Set<ItemStorage> food = new HashSet<>();

    /**
     * List of lucky oreBlocks which get dropped by the miner.
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
     * Map of building level to the lis tof possible enchantments.
     */
    private final Map<Integer, List<Tuple<String, Integer>>> enchantments = new HashMap<>();

    /**
     * If discovery is finished already.
     */
    private boolean discoveredAlready = false;

    /**
     * Random obj.
     */
    private static final Random random = new Random();

    /**
     * List of all blocks.
     */
    private static ImmutableList<ItemStack> allBlocks = ImmutableList.<ItemStack>builder().build();

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
        discoverBlockList();

        discoverSaplings();
        discoverOres();
        Log.getLogger().info("Finished discovering oreBlocks");
        discoverCompostableItems();
        discoverPlantables();
        discoverLuckyOres();
        discoverCrusherModes();
        discoverSifting();
        discoverFood();
        discoverFuel();
        discoverEnchantments();

        discoveredAlready = true;
    }

    /**
     * Create complete list of blocks, client side only.
     */
    private void discoverBlockList()
    {
        allBlocks = ImmutableList.copyOf(StreamSupport.stream(Spliterators.spliteratorUnknownSize(ForgeRegistries.ITEMS.iterator(), Spliterator.ORDERED), false)
                                           .map(ItemStack::new)
                                           .collect(Collectors.toList()));
    }

    /**
     * Getter for the list.
     *
     * @return the list of itemStacks.
     */
    @Override
    public List<ItemStack> getBlockList()
    {
        return allBlocks;
    }

    @Override
    public boolean isCompost(final ItemStack itemStack)
    {
        if (itemStack.isEmpty())
        {
            return false;
        }

        if (itemStack.getItem().isFood() || (itemStack.getItem() instanceof BlockItem && ((BlockItem) itemStack.getItem()).getBlock() instanceof CropsBlock))
        {
            return true;
        }

        for (final String string : MinecoloniesAPIProxy.getInstance().getConfig().getCommon().listOfCompostableItems.get())
        {
            if (itemStack.getItem().getRegistryName().toString().equals(string))
            {
                return true;
            }

            for (final ResourceLocation tag : itemStack.getItem().getTags())
            {
                if (tag.toString().contains(string))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isPlantable(final ItemStack itemStack)
    {
        if (itemStack.isEmpty())
        {
            return false;
        }

        for (final String string : IMinecoloniesAPI.getInstance().getConfig().getCommon().listOfPlantables.get())
        {
            if (itemStack.getItem().getRegistryName().toString().equals(string))
            {
                return true;
            }

            for (final ResourceLocation tag : itemStack.getItem().getTags())
            {
                if (tag.toString().contains(string))
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

        for (final String string : MinecoloniesAPIProxy.getInstance().getConfig().getCommon().luckyBlocks.get())
        {
            if (itemStack.getItem().getRegistryName().toString().equals(string))
            {
                return true;
            }
            for (final ResourceLocation tag : itemStack.getItem().getTags())
            {
                if (tag.getPath().equals(string))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack getSaplingForLeaf(final BlockState block)
    {
        final BlockStateStorage tempLeaf = new BlockStateStorage(block, leafCompareWithoutProperties, true);

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
    public Set<ItemStorage> getFuel()
    {
        return fuel;
    }

    @Override
    public Set<ItemStorage> getFood()
    {
        return food;
    }

    @Override
    public Set<ItemStorage> getSmeltableOres()
    {
        return smeltableOres;
    }

    @Override
    public List<ItemStorage> getCopyOfCompostableItems()
    {
        return ImmutableList.copyOf(compostableItems);
    }

    @Override
    public List<ItemStorage> getCopyOfPlantables()
    {
        return ImmutableList.copyOf(plantables);
    }

    @Override
    public boolean isOre(final BlockState block)
    {
        if (block.getBlock() instanceof OreBlock || block.getBlock() instanceof RedstoneOreBlock)
        {
            return true;
        }

        return oreBlocks.contains(block.getBlock());
    }

    @Override
    public boolean isOre(@NotNull final ItemStack stack)
    {
        if (isEmpty(stack))
        {
            return false;
        }

        if (stack.getItem().isIn(Tags.Items.ORES))
        {
            return !MinecoloniesAPIProxy.getInstance().getFurnaceRecipes().getSmeltingResult(stack).isEmpty();
        }

        for (final ResourceLocation tag : stack.getItem().getTags())
        {
            if (tag.getPath().contains(ORE_STRING))
            {
                return !MinecoloniesAPIProxy.getInstance().getFurnaceRecipes().getSmeltingResult(stack).isEmpty();
            }
        }

        return false;
    }

    @Override
    public boolean isMineableOre(@NotNull final ItemStack stack)
    {
        if (isEmpty(stack))
        {
            return false;
        }

        if (stack.getItem().isIn(Tags.Items.ORES))
        {
            return true;
        }

        for (final ResourceLocation tag : stack.getItem().getTags())
        {
            if (tag.getPath().contains(ORE_STRING))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Getter for all the crusher modes.
     *
     * @return an immutable copy of the map.
     */
    @Override
    public Map<ItemStorage, ItemStorage> getCrusherModes()
    {
        return ImmutableMap.<ItemStorage, ItemStorage>builder().putAll(this.crusherModes).build();
    }

    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        @NotNull final ListNBT saplingsLeavesTagList =
          leavesToSaplingMap.entrySet()
            .stream()
            .filter(entry -> entry.getKey() != null)
            .map(entry -> writeLeafSaplingEntryToNBT(entry.getKey().getState(), entry.getValue()))
            .collect(NBTUtils.toListNBT());
        compound.put(TAG_SAP_LEAF, saplingsLeavesTagList);
    }

    @Override
    public void read(@NotNull final CompoundNBT compound)
    {
        NBTUtils.streamCompound(compound.getList(TAG_SAP_LEAF, Constants.NBT.TAG_COMPOUND))
          .map(CompatibilityManager::readLeafSaplingEntryFromNBT)
          .filter(key -> !leavesToSaplingMap.containsKey(key.getA()) && !leavesToSaplingMap.containsValue(key.getB()))
          .forEach(key -> leavesToSaplingMap.put(new BlockStateStorage(key.getA(), leafCompareWithoutProperties, true), key.getB()));
    }

    @Override
    public void connectLeafToSapling(final BlockState leaf, final ItemStack stack)
    {
        final BlockStateStorage store = new BlockStateStorage(leaf, leafCompareWithoutProperties, true);
        if (!leavesToSaplingMap.containsKey(store))
        {
            leavesToSaplingMap.put(store, new ItemStorage(stack, false, true));
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
        if (random.nextInt(ONE_HUNDRED_PERCENT) <= MinecoloniesAPIProxy.getInstance().getConfig().getCommon().luckyBlockChance.get())
        {
            return luckyOres.get(random.nextInt(luckyOres.size())).getItemStack().copy();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Tuple<String, Integer> getRandomEnchantment(final int buildingLevel)
    {
        final List<Tuple<String, Integer>> list = enchantments.getOrDefault(buildingLevel, new ArrayList<>());
        if (list.isEmpty())
        {
            return new Tuple<>("protection", 1);
        }

        return list.get(random.nextInt(list.size()));
    }

    //------------------------------- Private Utility Methods -------------------------------//

    private void discoverOres()
    {
        if (smeltableOres.isEmpty())
        {
            smeltableOres.addAll(ImmutableList.copyOf(allBlocks.stream().filter(this::isOre).map(ItemStorage::new).collect(Collectors.toList())));
        }

        if (oreBlocks.isEmpty())
        {
            oreBlocks.addAll(ImmutableList.copyOf(allBlocks.stream().filter(this::isMineableOre)
                                                    .filter(stack -> !isEmpty(stack) && stack.getItem() instanceof BlockItem)
                                                    .map(stack -> ((BlockItem) stack.getItem()).getBlock())
                                                    .collect(Collectors.toList())));

            for (final String oreString : MinecoloniesAPIProxy.getInstance().getConfig().getCommon().extraOres.get())
            {
                final String[] split = oreString.split(":");
                final Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[0], split[1]));
                if (!(block == null || oreBlocks.contains(block)))
                {
                    oreBlocks.add(block);
                }
            }
        }
        Log.getLogger().info("Finished discovering Ores");
    }

    private void discoverSaplings()
    {

        for (final Item item : ItemTags.SAPLINGS.getAllElements())
        {
            final ItemStack stack = new ItemStack(item);
            {
                saplings.add(new ItemStorage(stack, false, true));
            }
        }
        Log.getLogger().info("Finished discovering saplings");
    }

    /**
     * Create complete list of compostable items.
     */
    private void discoverCompostableItems()
    {
        if (compostableItems.isEmpty())
        {
            compostableItems.addAll(ImmutableList.copyOf(allBlocks.stream().filter(this::isCompost).map(ItemStorage::new).collect(Collectors.toList())));
        }
        Log.getLogger().info("Finished discovering compostables");
    }

    /**
     * Create complete list of compostable items.
     */
    private void discoverPlantables()
    {
        if (plantables.isEmpty())
        {
            plantables.addAll(ImmutableList.copyOf(allBlocks.stream().filter(this::isPlantable).map(ItemStorage::new).collect(Collectors.toList())));
        }
        Log.getLogger().info("Finished discovering compostables");
    }

    /**
     * Create complete list of fuel items.
     */
    private void discoverFuel()
    {
        if (fuel.isEmpty())
        {
            fuel.addAll(ImmutableList.copyOf(allBlocks.stream().filter(FurnaceTileEntity::isFuel).map(ItemStorage::new).collect(Collectors.toList())));
        }
        Log.getLogger().info("Finished discovering fuel");
    }

    /**
     * Create complete list of food items.
     */
    private void discoverFood()
    {
        if (food.isEmpty())
        {
            food.addAll(ImmutableList.copyOf(allBlocks.stream().filter(ISFOOD.or(ISCOOKABLE)).map(ItemStorage::new).collect(Collectors.toList())));
        }
        Log.getLogger().info("Finished discovering food");
    }

    /**
     * Run through all blocks and check if they match one of our lucky oreBlocks.
     */
    private void discoverLuckyOres()
    {
        if (luckyOres.isEmpty())
        {
            for (final String ore : MinecoloniesAPIProxy.getInstance().getConfig().getCommon().luckyOres.get())
            {
                final String[] split = ore.split("!");
                if (split.length < 2)
                {
                    Log.getLogger().warn("Wrong configured ore: " + ore);
                    continue;
                }

                final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0]));
                if (item == null || item == Items.AIR)
                {
                    Log.getLogger().warn("Invalid lucky block: " + ore);
                    continue;
                }

                final ItemStack stack = new ItemStack(item, 1);
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
        Log.getLogger().info("Finished discovering lucky oreBlocks");
    }

    /**
     * Method discovering and loading from the config all materials the sifter needs.
     */
    private void discoverSifting()
    {
        for (final String string : MinecoloniesAPIProxy.getInstance().getConfig().getCommon().sifterMeshes.get())
        {
            final String[] mesh = string.split(",");

            if (mesh.length != 2)
            {
                Log.getLogger().warn("Couldn't parse the mesh: " + string);
                continue;
            }

            try
            {
                final double probability = Double.parseDouble(mesh[1]);

                final String[] item = mesh[0].split(":");
                final Item theItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item[0], item[1]));

                if (theItem == null)
                {
                    Log.getLogger().warn("Couldn't find item for mesh: " + string);
                    continue;
                }

                final ItemStack stack = new ItemStack(theItem, 1);
                sifterMeshes.add(new Tuple<>(new ItemStorage(stack), probability));
            }
            catch (final NumberFormatException ex)
            {
                Log.getLogger().warn("Couldn't retrieve probability for mesh: " + string, ex);
            }
        }

        for (final String string : MinecoloniesAPIProxy.getInstance().getConfig().getCommon().siftableBlocks.get())
        {
            try
            {
                final String[] item = string.split(":");
                final Item theItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item[0], item[1]));

                if (theItem == null)
                {
                    Log.getLogger().warn("Couldn't find item for siftable block: " + string);
                    continue;
                }

                final ItemStack stack = new ItemStack(theItem, 1);
                sievableBlocks.add(new ItemStorage(stack));
            }
            catch (final NumberFormatException ex)
            {
                Log.getLogger().warn("Couldn't retrieve the metadata for siftable block: " + string, ex);
            }
        }

        final Map<ItemStorage, Map<ItemStorage, Map<ItemStorage, Double>>> tempDrops = new HashMap<>();
        for (final String string : MinecoloniesAPIProxy.getInstance().getConfig().getCommon().sifterDrops.get())
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

                final ItemStorage meshStorage = sifterMeshes.get(mesh).getA();

                final String[] item = drop[2].split(":");
                final Item theItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item[0], item[1]));

                if (theItem == null || theItem == Items.AIR)
                {
                    Log.getLogger().warn("Couldn't find item for siftable block: " + string);
                    continue;
                }

                final ItemStack stack = new ItemStack(theItem, 1);
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
                    probabilitySum += probability;
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
    private void discoverEnchantments()
    {
        for (final String string : MinecoloniesAPIProxy.getInstance().getConfig().getCommon().enchantments.get())
        {
            final String[] split = string.split(":");
            if (split.length != 4)
            {
                Log.getLogger().warn("Invalid enchantment mode setting: " + string);
                continue;
            }

            try
            {
                final String enchantment = split[1];
                if (!Registry.ENCHANTMENT.getValue(new ResourceLocation("minecraft",enchantment)).isPresent())
                {
                    Log.getLogger().warn("Enchantment: " + enchantment + " doesn't exist!");
                    continue;
                }

                final int buildingLevel = Integer.parseInt(split[0]);
                final int enchantmentLevel = Integer.parseInt(split[2]);
                final int numberOfTickets = Integer.parseInt(split[3]);

                for (int level = buildingLevel; level <= 5; level++)
                {
                    final List<Tuple<String, Integer>> list = enchantments.getOrDefault(level, new ArrayList<>());
                    for (int i = 0; i < numberOfTickets; i++)
                    {
                        list.add(new Tuple<>(enchantment, enchantmentLevel));
                    }
                    enchantments.put(level, list);
                }
            }
            catch (final NumberFormatException ex)
            {
                Log.getLogger().warn("Invalid integer at pos 1, 3 or 4");
            }
        }
    }

    /**
     * Calculate the crusher modes from the config file.
     */
    private void discoverCrusherModes()
    {
        for (final String string : MinecoloniesAPIProxy.getInstance().getConfig().getCommon().crusherProduction.get())
        {
            final String[] split = string.split("!");
            if (split.length != 2)
            {
                Log.getLogger().warn("Invalid crusher mode setting: " + string);
                continue;
            }

            final String[] firstItem = split[0].split(":");
            final String[] secondItem = split[1].split(":");

            final Item item1 = ForgeRegistries.ITEMS.getValue(new ResourceLocation(firstItem[0], firstItem[1]));
            final Item item2 = ForgeRegistries.ITEMS.getValue(new ResourceLocation(secondItem[0], secondItem[1]));

            try
            {
                if (item1 == null || item2 == null)
                {
                    Log.getLogger().warn("Invalid crusher mode setting: " + string);
                    continue;
                }

                final ItemStorage storage1 = new ItemStorage(new ItemStack(item1, 2));
                final ItemStorage storage2 = new ItemStorage(new ItemStack(item2, 1));
                crusherModes.put(storage1, storage2);
            }
            catch (final NumberFormatException ex)
            {
                Log.getLogger().warn("Error getting metaData", ex);
            }
        }
    }

    private static CompoundNBT writeLeafSaplingEntryToNBT(final BlockState state, final ItemStorage storage)
    {
        final CompoundNBT compound = NBTUtil.writeBlockState(state);
        storage.getItemStack().write(compound);
        return compound;
    }

    private static Tuple<BlockState, ItemStorage> readLeafSaplingEntryFromNBT(final CompoundNBT compound)
    {
        return new Tuple<>(NBTUtil.readBlockState(compound), new ItemStorage(ItemStack.read(compound), false, true));
    }
}
