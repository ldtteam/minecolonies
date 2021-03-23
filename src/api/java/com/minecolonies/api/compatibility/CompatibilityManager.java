package com.minecolonies.api.compatibility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.api.compatibility.resourcefulbees.*;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.*;
import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.tags.*;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_SAP_LEAF;
import static net.minecraft.item.EnchantedBookItem.getEnchantedItemStack;

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
     * List of saplings. Works on client and server-side.
     */
    private final List<ItemStorage> saplings = new ArrayList<>();

    /**
     * List of properties we're ignoring when comparing leaves.
     */
    private final List<Property<?>> leafCompareWithoutProperties = ImmutableList.of(checkDecay, decayable, DYN_PROP_HYDRO, TREE_DISTANCE);

    /**
     * Properties for leaves we're ignoring upon comparing.
     */
    private static final BooleanProperty checkDecay     = BooleanProperty.create("check_decay");
    private static final BooleanProperty decayable      = BooleanProperty.create("decayable");
    public static final  IntegerProperty DYN_PROP_HYDRO = IntegerProperty.create("hydro", 1, 4);
    public static final  IntegerProperty TREE_DISTANCE  = IntegerProperty.create("distance", 1, 7);

    /**
     * List of all ore-like blocks. Works on client and server-side.
     */
    private final Set<Block> oreBlocks = new HashSet<>();

    /**
     * List of all ore-like items.
     */
    private final Set<ItemStorage> smeltableOres = new HashSet<>();

    /**
     * List of all the compost recipes
     */
    private final Map<Item, CompostRecipe> compostRecipes = new HashMap<>();

    /**
     * List of all the items that can be planted.
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
     * List of all the items that can be used as food
     */
    private final Set<ItemStorage> edibles = new HashSet<>();

    /**
     * Set of all possible diseases.
     */
    private final Map<String, Disease> diseases = new HashMap<>();

    /**
     * List of diseases including the random factor.
     */
    private final List<String> diseaseList = new ArrayList<>();

    /**
     * List of lucky oreBlocks which get dropped by the miner.
     */
    private final List<ItemStorage> luckyOres = new ArrayList<>();

    /**
     * The items and weights of the recruitment.
     */
    private final List<Tuple<Item, Integer>> recruitmentCostsWeights = new ArrayList<>();

    /**
     * Map of building level to the list of possible enchantments.
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
    private static ImmutableList<ItemStack> allItems = ImmutableList.<ItemStack>builder().build();

    /**
     * A List of all blocks, calculated using streams.  Not isomorphic to allItems.
     */
    private static ImmutableList<ItemStack> oldItems = ImmutableList.<ItemStack>builder().build();

    /**
     * Free block positions everyone can interact with.
     */
    private Set<Block> freeBlocks = new HashSet<>();

    /**
     * Free positions everyone can interact with.
     */
    private Set<BlockPos> freePositions = new HashSet<>();

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
    public void discover(final boolean serverSide)
    {
        long startTime = System.nanoTime();
        long[] times = new long[12];
        discoverAllItems();
        times[0] = System.nanoTime();

        discoverSaplings();
        times[1] = System.nanoTime();
        discoverOres();
        times[2] = System.nanoTime();
        discoverPlantables();
        times[3] = System.nanoTime();
        discoverFood();
        times[4] = System.nanoTime();
        discoverFuel();
        times[5] = System.nanoTime();

        discoverLuckyOres();
        times[6] = System.nanoTime();
        discoverRecruitCosts();
        times[7] = System.nanoTime();
        discoverDiseases();
        times[8] = System.nanoTime();
        discoverEnchantments();
        times[9] = System.nanoTime();
        discoverFreeBlocksAndPos();
        times[10] = System.nanoTime();
        discoverModCompat();
        times[11] = System.nanoTime();

        Log.getLogger().info("AllItems [" + ((double)(times[0] - startTime) / 1000000) + "] Saplings [" + ((double)(times[1] - times[0]) / 1000000) + "] Ores [" + ((double)(times[2] - times[1]) / 1000000)
                               + "] Plantables [" + ((double)(times[3] - times[2]) / 1000000) + "] Food [" + ((double)(times[4] - times[3]) / 1000000) + "] Fuel [" + ((double)(times[5] - times[4]) / 1000000) +"]");
        Log.getLogger().info("LuckyOres [" + ((double)(times[6] - times[5]) / 1000000) + "] RecruitCosts [" + ((double)(times[7] - times[6]) / 1000000) + "] Diseases [" + ((double)(times[8] - times[7]) / 1000000)
                               + "] Enchantments [" + ((double)(times[9] - times[8]) / 1000000) + "] FreeBlocks [" + ((double)(times[10] - times[9]) / 1000000) + " ModCompat [" + ((double)(times[11] - times[10]) / 1000000) + "]");
        Log.getLogger().info("Total Time [" + (double)((System.nanoTime() - startTime) / 1000000)  + "]");
        discoveredAlready = true;
    }

    /**
     * Create complete list of all existing items, client side only.
     */
    private void discoverAllItems()
    {
        final NonNullList<ItemStack> items = NonNullList.create();
        for(Item item : ForgeRegistries.ITEMS.getValues())
        {
            items.add(new ItemStack(item));
        }
        allItems = ImmutableList.copyOf(items);

        final List<ItemStack> stacks = StreamSupport.stream(Spliterators.spliteratorUnknownSize(ForgeRegistries.ITEMS.iterator(), Spliterator.ORDERED), true)
                                         .flatMap(item ->
                                         {
                                             final NonNullList<ItemStack> list = NonNullList.create();
                                             item.fillItemGroup(ItemGroup.SEARCH, list);
                                             return list.stream();
                                         })
                                         .collect(Collectors.toList());
        oldItems = ImmutableList.copyOf(stacks);
    }

    /**
     * Getter for the list.
     *
     * @return the list of itemStacks.
     */
    @Override
    public List<ItemStack> getListOfAllItems()
    {
        if(allItems.isEmpty())
        {
            discoverAllItems();
        }
        return allItems;
    }

    @Override
    public boolean isPlantable(final ItemStack itemStack)
    {
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof BlockItem) || itemStack.getItem() == ModTags.floristFlowersExcluded)
        {
            return false;
        }

        for (final String string : IMinecoloniesAPI.getInstance().getConfig().getServer().listOfPlantables.get())
        {
            if (itemStack.getItem().getRegistryName().toString().equals(string))
            {
                return true;
            }

            String[] split = string.split(":");
            if (split.length == 2)
            {
                for (final ResourceLocation tag : itemStack.getItem().getTags())
                {
                    if (tag.toString().contains(":" + split[1]) && itemStack.getItem().getRegistryName().getNamespace().equals(split[0]))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isLuckyBlock(final Block block)
    {
        return ModTags.oreChanceBlocks.contains(block);
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
    public Set<ItemStorage> getCopyOfSaplings()
    {
        return new HashSet<>(saplings);
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
    public Set<ItemStorage> getEdibles()
    {
        return edibles;
    }

    @Override
    public Set<ItemStorage> getSmeltableOres()
    {
        return smeltableOres;
    }

    @Override
    public Map<Item, CompostRecipe> getCopyOfCompostRecipes()
    {
        return ImmutableMap.copyOf(compostRecipes);
    }

    @Override
    public Set<ItemStorage> getCompostInputs()
    {
        return compostRecipes.keySet().stream()
                .map(item -> new ItemStorage(new ItemStack(item)))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ItemStorage> getCopyOfPlantables()
    {
        return new HashSet<>(plantables);
    }

    @Override
    public String getRandomDisease()
    {
        return diseaseList.get(random.nextInt(diseaseList.size()));
    }

    @Override
    public Disease getDisease(final String disease)
    {
        return diseases.get(disease);
    }

    @Override
    public List<Disease> getDiseases()
    {
        return new ArrayList<>(diseases.values());
    }

    @Override
    public List<Tuple<Item, Integer>> getRecruitmentCostsWeights()
    {
        return Collections.unmodifiableList(recruitmentCostsWeights);
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
                .filter(key -> !leavesToSaplingMap.containsKey(new BlockStateStorage(key.getA(), leafCompareWithoutProperties, true)) && !leavesToSaplingMap.containsValue(key.getB()))
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
    public ItemStack getRandomLuckyOre(final double chanceBonus)
    {
        if (random.nextDouble() * ONE_HUNDRED_PERCENT <= MinecoloniesAPIProxy.getInstance().getConfig().getServer().luckyBlockChance.get() * chanceBonus)
        {
            return luckyOres.get(random.nextInt(luckyOres.size())).getItemStack().copy();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Tuple<ItemStack, Integer> getRandomEnchantmentBook(final int buildingLevel)
    {
        final List<Tuple<String, Integer>> list = enchantments.getOrDefault(buildingLevel, new ArrayList<>());
        final Tuple<String, Integer> ench;

        if (list.isEmpty())
        {
            ench = new Tuple<>("protection", 1);
        }
        else
        {
            ench = list.get(random.nextInt(list.size()));
        }
        return new Tuple<>(getEnchantedItemStack(new EnchantmentData(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(ench.getA())), ench.getB())),
                ench.getB());
    }

    @Override
    public boolean isFreeBlock(final Block block)
    {
        return freeBlocks.contains(block);
    }

    @Override
    public boolean isFreePos(final BlockPos block)
    {
        return freePositions.contains(block);
    }

    @Override
    public void invalidateRecipes(@NotNull final RecipeManager recipeManager)
    {
        compostRecipes.clear();
        discoverCompostRecipes(recipeManager);
    }

    @Override
    public void invalidateTagsAndConfigs()
    {
        // Kept as a separate function from invalidateRecipes, as invalidateRecipes fires very late for JEI compatibility,
        // will cause extraneous log messages, and significantly add to load times.
        // This does mean data packs which change tags will not filter to the Compatibility Manager until a server restart, but better that than
        // risking irregular disconnects on lower-powered servers.

        // Tag-based configurations may differ between one world and another, or on data pack reload.
        saplings.clear();
        oreBlocks.clear();
        smeltableOres.clear();
        plantables.clear();
        food.clear();
        edibles.clear();
        fuel.clear();

        // Server-config-based files may change between one world and another, or the same world after leaving and re-entering.
        // TODO: Some of these may eventually go to data packs, either for added configurability and/or to reduce the server config size.
        luckyOres.clear();
        recruitmentCostsWeights.clear();
        diseases.clear();
        diseaseList.clear();
        enchantments.clear();
        freeBlocks.clear();
        freePositions.clear();
        discoveredAlready = false;
    }

    //------------------------------- Private Utility Methods -------------------------------//

    private void discoverOres()
    {
        if (smeltableOres.isEmpty())
        {
            for(Item item : Tags.Items.ORES.getAllElements())
            {
                if(item.getItem() instanceof BlockItem)
                {
                    oreBlocks.add(((BlockItem) item.getItem()).getBlock());
                }
                if (!MinecoloniesAPIProxy.getInstance().getFurnaceRecipes().getSmeltingResult(new ItemStack((item))).isEmpty())
                {
                    smeltableOres.add(new ItemStorage(new ItemStack(item)));
                }
            }
        }
        Log.getLogger().info("Finished discovering Ores");
        //Set<ItemStorage> oldSmeltables = new HashSet<>();
        //oldSmeltables.addAll(ImmutableList.copyOf(allItems.stream().filter(this::isOre).map(ItemStorage::new).collect(Collectors.toList())));
        //Set<Block> oldBlocks = new HashSet<>();
        //oldBlocks.addAll(ImmutableList.copyOf(allItems.stream().filter(this::isMineableOre)
        //                                        .filter(stack -> !isEmpty(stack) && stack.getItem() instanceof BlockItem)
        //                                        .map(stack -> ((BlockItem) stack.getItem()).getBlock())
        //                                        .collect(Collectors.toList())));
        //Log.getLogger().info("SmeltableOres = " + smeltableOres.size() + " ; OldCalculations " + oldSmeltables.size());
        //for(ItemStorage item : oldSmeltables)
        //{
        //    if(!smeltableOres.contains(item))
        //    {
        //        Log.getLogger().info("Mismatch: " + item.getItem().getName() + "\\" + item.getItem().getRegistryName());
        //        for(ResourceLocation tag : item.getItem().getTags())
        //        {
        //            Log.getLogger().info(tag);
        //        }
        //    }
        //}
        //for(ItemStorage item : smeltableOres)
        //{
        //    if(!oldSmeltables.contains(item))
        //    {
        //        Log.getLogger().info("Mismatch: " + item.getItem().getName() + "\\" + item.getItem().getRegistryName());
        //        for(ResourceLocation tag : item.getItem().getTags())
        //        {
        //            Log.getLogger().info(tag);
        //        }
        //    }
        //}
        //Log.getLogger().info("OreBlocks = " + oreBlocks.size() + " ; OldCalculations " + oldBlocks.size());
        //for(Block oldBlock : oldBlocks)
        //{
        //    if(!oreBlocks.contains(oldBlock))
        //    {
        //        Log.getLogger().info("Mismatch: " + oldBlock.getTranslatedName() + "\\" + oldBlock.getRegistryName());
        //        for(ResourceLocation tag : oldBlock.getTags())
        //        {
        //            Log.getLogger().info(tag);
        //        }
        //    }
        //}
        //for(Block newBlock : oreBlocks)
        //{
        //    if(!oldBlocks.contains(newBlock))
        //    {
        //        Log.getLogger().info("Mismatch: " + newBlock.getRegistryName());
        //        for(ResourceLocation tag : newBlock.getTags())
        //        {
        //            Log.getLogger().info(tag);
        //        }
        //    }
        //}
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
     * Create complete list of compost recipes.
     * @param recipeManager
     */
    @SuppressWarnings("ConditionalExpression")
    private void discoverCompostRecipes(@NotNull final RecipeManager recipeManager)
    {
        if (compostRecipes.isEmpty())
        {
            for (final IRecipe<?> r : recipeManager.getRecipes(CompostRecipe.TYPE).values())
            {
                final CompostRecipe recipe = (CompostRecipe) r;
                for (final ItemStack stack : recipe.getInput().getMatchingStacks())
                {
                    // there can be duplicates due to overlapping tags.  weakest one wins.
                    compostRecipes.merge(stack.getItem(), recipe,
                            (r1, r2) -> r1.getStrength() < r2.getStrength() ? r1 : r2);
                }
            }
            Log.getLogger().info("Finished discovering compostables");
        }
    }

    /**
     * Create complete list of plantable items.
     */
    private void discoverPlantables()
    {
        if (plantables.isEmpty())
        {
            for (final String string : IMinecoloniesAPI.getInstance().getConfig().getServer().listOfPlantables.get())
            {
                // individual flowers by ResourceLocation.
                if(ForgeRegistries.ITEMS.containsKey(new ResourceLocation(string)))
                {
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(string)).getItem();
                    if(item instanceof BlockItem && !ModTags.floristFlowersExcluded.contains(item) && item != ModTags.floristFlowersExcluded)
                    {
                        plantables.add(new ItemStorage(new ItemStack(item)));
                    }
                }
                // check if a valid tag
                ITag<Item> tag = TagUtils.getItem(new ResourceLocation(string));
                if(!tag.equals(Tag.getEmptyTag()))
                {
                   for(Item item : tag.getAllElements())
                   {
                       if(item instanceof BlockItem && ModTags.floristFlowersExcluded.contains(item) && item != ModTags.floristFlowersExcluded)
                       {
                           continue;
                       }
                       plantables.add(new ItemStorage(new ItemStack(item)));
                   }
                }
            }
        }
        Log.getLogger().info("Finished discovering plantables");
        Set<ItemStorage> oldPlantables = new HashSet<>();
        oldPlantables.addAll(ImmutableList.copyOf(oldItems.stream()
                                                     .filter(this::isPlantable)
                                                     .map(ItemStorage::new)
                                                     .collect(Collectors.toList())));
        Log.getLogger().info("NewCalc: " + plantables.size() + " OldCalc: " + oldPlantables.size());
        for(ItemStorage is : plantables)
        {
            if(!oldPlantables.contains(is))
            {
                Log.getLogger().info("Mismatch at : " + is.getItem().getName() + "\\" + is.getItemStack().getItem().getRegistryName());
                for(ResourceLocation tag : is.getItem().getTags())
                {
                    Log.getLogger().info(tag);
                }
            }
        }
        for(ItemStorage is : oldPlantables)
        {
            if(!plantables.contains(is))
            {
                Log.getLogger().info("Mismatch at : " + is.getItem().getName() + "\\" + is.getItemStack().getItem().getRegistryName());
                for(ResourceLocation tag : is.getItem().getTags())
                {
                    Log.getLogger().info(tag);
                }
            }
        }
    }

    /**
     * Create complete list of fuel items.
     */
    private void discoverFuel()
    {
        if (fuel.isEmpty())
        {
            for(ItemStack item : allItems)
            {
                if(FurnaceTileEntity.isFuel(item))
                {
                    fuel.add(new ItemStorage(item));
                }
            }

        }
        Log.getLogger().info("Finished discovering fuel");
        //Set<ItemStorage> oldFuel = new HashSet<>();
        //oldFuel.addAll(ImmutableList.copyOf(oldItems.stream().filter(FurnaceTileEntity::isFuel).map(ItemStorage::new).collect(Collectors.toList())));
        //Log.getLogger().info("New calcs: " + fuel.size() + " ; Old calcs: " + oldFuel.size());
    }

    /**
     * Create complete list of food items.
     */
    private void discoverFood()
    {
        if (food.isEmpty())
        {
            for(ItemStack item : allItems)
            {
                if(ISFOOD.test(item) || ISCOOKABLE.test(item))
                {
                    food.add(new ItemStorage(item));
                    if(CAN_EAT.test(item))
                    {
                        edibles.add(new ItemStorage(item));
                    }
                }
            }
        }
        Log.getLogger().info("Finished discovering food");
        //Set<ItemStorage> oldFood = new HashSet<>();
        //Set<ItemStorage> oldEdibles = new HashSet<>();
        //oldFood.addAll(ImmutableList.copyOf(oldItems.stream()
        //                                   .filter(ISFOOD.or(ISCOOKABLE))
        //                                   .map(ItemStorage::new)
        //                                   .collect(Collectors.toList())));
        //oldEdibles.addAll(ImmutableList.copyOf(oldFood.stream().filter(storage -> CAN_EAT.test(storage.getItemStack())).collect(Collectors.toList())));
        //Log.getLogger().info("New calcs: " + food.size() + "-" + edibles.size() + " ; " + oldFood.size() + "-" + oldEdibles.size());
        //for(ItemStorage is : food)
        //{
        //    if(!oldFood.contains(is))
        //    {
        //        Log.getLogger().info("Mismatch at : " + is.getItem().getName() + "\\" + is.getItemStack().getItem().getRegistryName());
        //        for (ResourceLocation tag : is.getItem().getTags())
        //        {
        //            Log.getLogger().info(tag);
        //        }
        //    }
        //}
    }

    /**
     * Run through all blocks and check if they match one of our lucky oreBlocks.
     */
    private void discoverLuckyOres()
    {
        if (luckyOres.isEmpty())
        {
            for (final String ore : MinecoloniesAPIProxy.getInstance().getConfig().getServer().luckyOres.get())
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
     * Parses recruitment costs from config
     */
    private void discoverRecruitCosts()
    {
        if (recruitmentCostsWeights.isEmpty())
        {
            for (final String itemString : MinecoloniesAPIProxy.getInstance().getConfig().getServer().configListRecruitmentItems.get())
            {
                final String[] split = itemString.split(";");
                if (split.length < 2)
                {
                    Log.getLogger().warn("Wrong configured recruitment cost: " + itemString);
                    continue;
                }

                final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0]));
                if (item == null || item == Items.AIR)
                {
                    Log.getLogger().warn("Invalid recruitment item: " + item);
                    continue;
                }

                try
                {
                    final int rarity = Integer.parseInt(split[split.length - 1]);
                    recruitmentCostsWeights.add(new Tuple<>(item, rarity));
                }
                catch (final NumberFormatException ex)
                {
                    Log.getLogger().warn("Invalid recruitment weight for: " + item);
                }
            }
        }
        Log.getLogger().info("Finished discovering recruitment costs");
    }

    /**
     * Go through the disease config and setup all possible diseases.
     */
    private void discoverDiseases()
    {
        if (diseases.isEmpty())
        {
            for (final String disease : MinecoloniesAPIProxy.getInstance().getConfig().getServer().diseases.get())
            {
                final String[] split = disease.split(",");
                if (split.length < 3)
                {
                    Log.getLogger().warn("Wrongly configured disease: " + disease);
                    continue;
                }

                try
                {
                    final String name = split[0];
                    final int rarity = Integer.parseInt(split[1]);

                    final List<ItemStack> cure = new ArrayList<>();

                    for (int i = 2; i < split.length; i++)
                    {
                        final String[] theItem = split[i].split(":");
                        final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(theItem[0], theItem[1]));
                        if (item == null || item == Items.AIR)
                        {
                            Log.getLogger().warn("Invalid cure item: " + disease);
                            continue;
                        }

                        final ItemStack stack = new ItemStack(item, 1);
                        cure.add(stack);
                    }
                    diseases.put(name, new Disease(name, rarity, cure));
                    for (int i = 0; i < rarity; i++)
                    {
                        diseaseList.add(name);
                    }
                }
                catch (final NumberFormatException e)
                {
                    Log.getLogger().warn("Wrongly configured disease: " + disease);
                }
            }
        }
        Log.getLogger().info("Finished discovering diseases");
    }

    /**
     * Discover the possible enchantments from file.
     */
    private void discoverEnchantments()
    {
        for (final String string : MinecoloniesAPIProxy.getInstance().getConfig().getServer().enchantments.get())
        {
            final String[] split = string.split(",");
            if (split.length != 4)
            {
                Log.getLogger().warn("Invalid enchantment mode setting: " + string);
                continue;
            }

            try
            {
                final String enchantment = split[1];
                if (ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(enchantment)) == null)
                {
                    Log.getLogger().warn("Enchantment: " + enchantment + " doesn't exist!");
                    continue;
                }

                final int buildingLevel = Integer.parseInt(split[0]);
                final int enchantmentLevel = Integer.parseInt(split[2]);
                final int numberOfTickets = Integer.parseInt(split[3]);

                for (int level = buildingLevel; level <= Math.min(buildingLevel + 2, 5); level++)
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

    /**
     * Load free blocks and pos from the config and add to colony.
     */
    private void discoverFreeBlocksAndPos()
    {
        for (final String s : MinecoloniesAPIProxy.getInstance().getConfig().getServer().freeToInteractBlocks.get())
        {
            try
            {
                final Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(s));
                if (block != null && !(block instanceof AirBlock))
                {
                    freeBlocks.add(block);
                }
            }
            catch (final Exception ex)
            {
                final BlockPos pos = BlockPosUtil.getBlockPosOfString(s);
                if (pos != null)
                {
                    freePositions.add(pos);
                }
            }
        }
    }

    /**
     * Inits compats
     */
    private void discoverModCompat()
    {
        if (ModList.get().isLoaded("resourcefulbees"))
        {
            Compatibility.beeHiveCompat = new ResourcefulBeesCompat();
        }
    }
}