package com.minecolonies.api.compatibility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.*;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.loot.LootPredicateManager;
import net.minecraft.loot.LootTableManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.resources.VanillaPack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.Property;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.packs.ModFileResourcePack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;
import static com.minecolonies.api.util.constant.Constants.ORE_STRING;
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
     * Free block positions everyone can interact with.
     */
    private Set<Block> freeBlocks = new HashSet<>();

    /**
     * Free positions everyone can interact with.
     */
    private Set<BlockPos> freePositions = new HashSet<>();

    private LootTableManager lootTableManager;

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
    public void discover(final World world)
    {
        discoverAllItems();

        discoverSaplings();
        discoverOres();
        discoverCompostRecipes(world);
        discoverPlantables();
        discoverLuckyOres();
        discoverRecruitCosts();
        discoverDiseases();
        discoverFood();
        discoverFuel();
        discoverEnchantments();
        discoverFreeBlocksAndPos();

        discoveredAlready = true;
    }

    /**
     * Create complete list of all existing items, client side only.
     */
    private void discoverAllItems()
    {
        final List<ItemStack> stacks = StreamSupport.stream(Spliterators.spliteratorUnknownSize(ForgeRegistries.ITEMS.iterator(), Spliterator.ORDERED), true)
                                           .flatMap(item ->
                                             {
                                                 final NonNullList<ItemStack> list = NonNullList.create();
                                                 item.fillItemGroup(ItemGroup.SEARCH, list);
                                                 return list.stream();
                                             })
                                           .collect(Collectors.toList());

        allItems = ImmutableList.copyOf(stacks);
    }

    /**
     * Getter for the list.
     *
     * @return the list of itemStacks.
     */
    @Override
    public List<ItemStack> getListOfAllItems()
    {
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
    public Map<Item, CompostRecipe> getCopyOfCompostRecipes()
    {
        return ImmutableMap.copyOf(compostRecipes);
    }

    @Override
    public List<ItemStorage> getCopyOfPlantables()
    {
        return ImmutableList.copyOf(plantables);
    }

    @Override
    public Map<Integer, List<Tuple<String, Integer>>> getCopyOfEnchantments() { return ImmutableMap.copyOf(enchantments); }

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

    //------------------------------- Private Utility Methods -------------------------------//

    private void discoverOres()
    {
        if (smeltableOres.isEmpty())
        {
            smeltableOres.addAll(ImmutableList.copyOf(allItems.stream().filter(this::isOre).map(ItemStorage::new).collect(Collectors.toList())));
        }

        if (oreBlocks.isEmpty())
        {
            oreBlocks.addAll(ImmutableList.copyOf(allItems.stream().filter(this::isMineableOre)
                                                    .filter(stack -> !isEmpty(stack) && stack.getItem() instanceof BlockItem)
                                                    .map(stack -> ((BlockItem) stack.getItem()).getBlock())
                                                    .collect(Collectors.toList())));
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
     * Create complete list of compost recipes.
     */
    @SuppressWarnings("ConditionalExpression")
    private void discoverCompostRecipes(final World world)
    {
        if (compostRecipes.isEmpty())
        {
            for (final IRecipe<?> r : world.getRecipeManager().getRecipes(CompostRecipe.TYPE).values())
            {
                final CompostRecipe recipe = (CompostRecipe) r;
                for (final ItemStack stack : recipe.getInput().getMatchingStacks())
                {
                    // there can be duplicates due to overlapping tags.  weakest one wins.
                    compostRecipes.merge(stack.getItem(), recipe,
                            (r1, r2) -> r1.getStrength() < r2.getStrength() ? r1 : r2);
                }
            }
        }
        Log.getLogger().info("Finished discovering compostables");
    }

    /**
     * Create complete list of plantable items.
     */
    private void discoverPlantables()
    {
        if (plantables.isEmpty())
        {
            plantables.addAll(ImmutableList.copyOf(allItems.stream()
                                                     .filter(this::isPlantable)
                                                     .map(ItemStorage::new)
                                                     .collect(Collectors.toList())));
        }
        Log.getLogger().info("Finished discovering plantables");
    }

    /**
     * Create complete list of fuel items.
     */
    private void discoverFuel()
    {
        if (fuel.isEmpty())
        {
            fuel.addAll(ImmutableList.copyOf(allItems.stream().filter(FurnaceTileEntity::isFuel).map(ItemStorage::new).collect(Collectors.toList())));
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
            food.addAll(ImmutableList.copyOf(allItems.stream()
                    .filter(ISFOOD.or(ISCOOKABLE))
                    .map(ItemStorage::new)
                    .collect(Collectors.toList())));
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
}
