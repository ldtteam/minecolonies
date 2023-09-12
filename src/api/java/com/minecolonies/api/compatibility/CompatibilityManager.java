package com.minecolonies.api.compatibility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.compatibility.dynamictrees.DynamicTreeCompat;
import com.minecolonies.api.compatibility.resourcefulbees.ResourcefulBeesCompat;
import com.minecolonies.api.compatibility.tinkers.SlimeTreeCheck;
import com.minecolonies.api.compatibility.tinkers.TinkersToolHelper;
import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.registry.ModRecipeSerializer;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;
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
    private final Map<Integer, List<ItemStorage>> luckyOres = new HashMap<>();

    /**
     * The items and weights of the recruitment.
     */
    private final List<Tuple<Item, Integer>> recruitmentCostsWeights = new ArrayList<>();

    /**
     * Random obj.
     */
    private static final Random random = new Random();


    /**
     * List of all blocks.
     */
    private static ImmutableList<ItemStack> allItems = ImmutableList.of();

    /**
     * Set of all items.  Uses ItemStorage mostly for better equals/hash.
     */
    private static ImmutableSet<ItemStorage> allItemsSet = ImmutableSet.of();

    /**
     * Free block positions everyone can interact with.
     */
    private final Set<Block> freeBlocks = new HashSet<>();

    /**
     * Free positions everyone can interact with.
     */
    private final Set<BlockPos> freePositions = new HashSet<>();

    /**
     * Hashmap of mobs we may or may not attack.
     */
    private ImmutableSet<ResourceLocation> monsters = ImmutableSet.of();

    /**
     * List of all the items that can be used by the apiary.
     */
    private ImmutableSet<ItemStorage> flowers = ImmutableSet.of();

    /**
     * Instantiates the compatibilityManager.
     */
    public CompatibilityManager()
    {
        /*
         * Intentionally left empty.
         */
    }

    private void clear()
    {
        saplings.clear();
        oreBlocks.clear();
        smeltableOres.clear();
        plantables.clear();
        flowers = ImmutableSet.of();

        food.clear();
        edibles.clear();
        fuel.clear();
        compostRecipes.clear();

        luckyOres.clear();
        recruitmentCostsWeights.clear();
        diseases.clear();
        diseaseList.clear();
        freeBlocks.clear();
        freePositions.clear();
        monsters = ImmutableSet.of();
    }

    /**
     * Called server-side *only* to calculate the various lists of items from the registry, recipes, and tags.
     * @param recipeManager The vanilla recipe manager.
     */
    @Override
    public void discover(@NotNull final RecipeManager recipeManager)
    {
        clear();
        discoverAllItems();

        discoverSaplings();
        discoverOres();
        discoverPlantables();
        discoverFlowers();

        discoverFood();
        discoverFuel();
        discoverMobs();
        discoverCompostRecipes(recipeManager);

        discoverLuckyOres();
        discoverRecruitCosts();
        discoverDiseases();
        discoverFreeBlocksAndPos();
        discoverModCompat();
    }

    /**
     * Transfer server-discovered item lists to client, to avoid double-handling (and
     * potentially getting different answers).
     *
     * @param buf serialization buffer
     */
    public void serialize(@NotNull final FriendlyByteBuf buf)
    {
        serializeItemStorageList(buf, saplings);
        serializeBlockList(buf, oreBlocks);
        serializeItemStorageList(buf, smeltableOres);
        serializeItemStorageList(buf, plantables);
        serializeItemStorageList(buf, flowers);

        serializeItemStorageList(buf, food);
        serializeItemStorageList(buf, edibles);
        serializeItemStorageList(buf, fuel);
        serializeRegistryIds(buf, ForgeRegistries.ENTITY_TYPES, monsters);

        serializeCompostRecipes(buf, compostRecipes);
    }

    /**
     * Receive and update lists based on incoming server discovery data.
     *
     * Note: anything based purely on the registries and configs can be safely recalculated here.
     *       But anything based on tags or recipes must be updated purely via the packet,
     *       because this can be called before the client has the latest tags/recipes.
     *
     * @param buf deserialization buffer
     */
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        clear();
        discoverAllItems();

        saplings.addAll(deserializeItemStorageList(buf));
        oreBlocks.addAll(deserializeBlockList(buf));
        smeltableOres.addAll(deserializeItemStorageList(buf));
        plantables.addAll(deserializeItemStorageList(buf));
        flowers = ImmutableSet.copyOf(deserializeItemStorageList(buf));

        food.addAll(deserializeItemStorageList(buf));
        edibles.addAll(deserializeItemStorageList(buf));
        fuel.addAll(deserializeItemStorageList(buf));
        monsters = ImmutableSet.copyOf(deserializeRegistryIds(buf, ForgeRegistries.ENTITY_TYPES));

        Log.getLogger().info("Synchronized {} saplings", saplings.size());
        Log.getLogger().info("Synchronized {} ore blocks with {} smeltable ores", oreBlocks.size(), smeltableOres.size());
        Log.getLogger().info("Synchronized {} plantables", plantables.size());
        Log.getLogger().info("Synchronized {} flowers", flowers.size());

        Log.getLogger().info("Synchronized {} food types with {} edible", food.size(), edibles.size());
        Log.getLogger().info("Synchronized {} fuel types", fuel.size());
        Log.getLogger().info("Synchronized {} monsters", monsters.size());

        discoverCompostRecipes(deserializeCompostRecipes(buf));

        // the below are loaded from config files, which have been synched already by this point
        discoverLuckyOres();
        discoverRecruitCosts();
        discoverDiseases();
        discoverFreeBlocksAndPos();
        discoverModCompat();
    }

    private static void serializeItemStorageList(@NotNull final FriendlyByteBuf buf,
                                                 @NotNull final Collection<ItemStorage> list)
    {
        buf.writeCollection(list, StandardFactoryController.getInstance()::serialize);
    }

    @NotNull
    private static List<ItemStorage> deserializeItemStorageList(@NotNull final FriendlyByteBuf buf)
    {
        return buf.readList(StandardFactoryController.getInstance()::deserialize);
    }

    private static void serializeBlockList(@NotNull final FriendlyByteBuf buf,
                                           @NotNull final Collection<Block> list)
    {
        buf.writeCollection(list.stream().map(ItemStack::new).toList(), FriendlyByteBuf::writeItem);
    }

    @NotNull
    private static List<Block> deserializeBlockList(@NotNull final FriendlyByteBuf buf)
    {
        final List<ItemStack> stacks = buf.readList(FriendlyByteBuf::readItem);
        return stacks.stream()
                .flatMap(stack -> stack.getItem() instanceof BlockItem blockItem
                        ? Stream.of(blockItem.getBlock()) : Stream.empty())
                .toList();
    }

    private static void serializeRegistryIds(@NotNull final FriendlyByteBuf buf,
                                             @NotNull final IForgeRegistry<?> registry,
                                             @NotNull final Collection<ResourceLocation> ids)
    {
        buf.writeCollection(ids, (b, id) -> b.writeRegistryIdUnsafe(registry, id));
    }

    @NotNull
    private static <T> List<ResourceLocation>
            deserializeRegistryIds(@NotNull final FriendlyByteBuf buf,
                                   @NotNull final IForgeRegistry<T> registry)
    {
        return buf.readList(b -> b.readRegistryIdUnsafe(registry)).stream()
                .flatMap(item -> Stream.ofNullable(registry.getKey(item)))
                .toList();
    }

    private static void serializeCompostRecipes(@NotNull final FriendlyByteBuf buf,
                                                @NotNull final Map<Item, CompostRecipe> compostRecipes)
    {
        final List<CompostRecipe> recipes = compostRecipes.values().stream().distinct().toList();
        buf.writeCollection(recipes, ModRecipeSerializer.CompostRecipeSerializer.get()::toNetwork);
    }

    @NotNull
    private static List<CompostRecipe> deserializeCompostRecipes(@NotNull final FriendlyByteBuf buf)
    {
        final CompostRecipe.Serializer serializer = ModRecipeSerializer.CompostRecipeSerializer.get();
        final ResourceLocation empty = new ResourceLocation("");
        return buf.readList(b -> serializer.fromNetwork(empty, b));
    }

    /**
     * Getter for the list.
     *
     * @return the list of itemStacks.
     */
    @Override
    public List<ItemStack> getListOfAllItems()
    {
        if (allItems.isEmpty()) Log.getLogger().error("getListOfAllItems when empty");
        return allItems;
    }

    @Override
    public Set<ItemStorage> getSetOfAllItems()
    {
        if (allItemsSet.isEmpty()) Log.getLogger().error("getSetOfAllItems when empty");
        return allItemsSet;
    }

    @Override
    public boolean isPlantable(final ItemStack itemStack)
    {
        return !itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem && itemStack.is(ModTags.floristFlowers);
    }

    @Override
    public boolean isLuckyBlock(final Block block)
    {
        return block.defaultBlockState().is(ModTags.oreChanceBlocks);
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
        if (saplings.isEmpty()) Log.getLogger().error("getCopyOfSaplings when empty");
        return new HashSet<>(saplings);
    }

    @Override
    public Set<ItemStorage> getFuel()
    {
        if (fuel.isEmpty()) Log.getLogger().error("getFuel when empty");
        return fuel;
    }

    @Override
    public Set<ItemStorage> getFood()
    {
        if (food.isEmpty()) Log.getLogger().error("getFood when empty");
        return food;
    }

    @Override
    public Set<ItemStorage> getEdibles(final int minNutrition)
    {
        if (edibles.isEmpty()) Log.getLogger().error("getEdibles when empty");
        final Set<ItemStorage> filteredEdibles = new HashSet<>();
        for (final ItemStorage storage : edibles)
        {
            if ((storage.getItemStack().getFoodProperties(null) != null && storage.getItemStack().getFoodProperties(null).getNutrition() >= minNutrition))
            {
                filteredEdibles.add(storage);
            }
        }
        return filteredEdibles;
    }

    @Override
    public Set<ItemStorage> getSmeltableOres()
    {
        if (smeltableOres.isEmpty()) Log.getLogger().error("getSmeltableOres when empty");
        return smeltableOres;
    }

    @Override
    public Map<Item, CompostRecipe> getCopyOfCompostRecipes()
    {
        if (compostRecipes.isEmpty()) Log.getLogger().error("getCopyOfCompostRecipes when empty");
        return ImmutableMap.copyOf(compostRecipes);
    }

    @Override
    public Set<ItemStorage> getCompostInputs()
    {
        if (compostRecipes.isEmpty()) Log.getLogger().error("getCompostInputs when empty");
        return compostRecipes.keySet().stream()
                .map(item -> new ItemStorage(new ItemStack(item)))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ItemStorage> getCopyOfPlantables()
    {
        if (plantables.isEmpty()) Log.getLogger().error("getCopyOfPlantables when empty");
        return new HashSet<>(plantables);
    }

    @Override
    public Set<ItemStorage> getImmutableFlowers()
    {
        if (flowers.isEmpty()) Log.getLogger().error("getImmutableFlowers when empty");
        return flowers;
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
        if (oreBlocks.isEmpty()) Log.getLogger().error("isOre when empty");

        return oreBlocks.contains(block.getBlock());
    }

    @Override
    public boolean isOre(@NotNull final ItemStack stack)
    {
        if (isMineableOre(stack) || stack.is(ModTags.raw_ore) || stack.is(ModTags.breakable_ore) )
        {
            ItemStack smeltingResult = MinecoloniesAPIProxy.getInstance().getFurnaceRecipes().getSmeltingResult(stack);
            return stack.is(ModTags.breakable_ore) || !smeltingResult.isEmpty();
        }

        return false;
    }

    @Override
    public boolean isMineableOre(@NotNull final ItemStack stack)
    {
        return !isEmpty(stack) && stack.is(Tags.Items.ORES);
    }

    @Override
    public void write(@NotNull final CompoundTag compound)
    {
        @NotNull final ListTag saplingsLeavesTagList =
                leavesToSaplingMap.entrySet()
                        .stream()
                        .filter(entry -> entry.getKey() != null)
                        .map(entry -> writeLeafSaplingEntryToNBT(entry.getKey().getState(), entry.getValue()))
                        .collect(NBTUtils.toListNBT());
        compound.put(TAG_SAP_LEAF, saplingsLeavesTagList);
    }

    @Override
    public void read(@NotNull final CompoundTag compound)
    {
        NBTUtils.streamCompound(compound.getList(TAG_SAP_LEAF, Tag.TAG_COMPOUND))
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
    public ItemStack getRandomLuckyOre(final double chanceBonus, final int buildingLevel)
    {
        if (random.nextDouble() * ONE_HUNDRED_PERCENT <= MinecoloniesAPIProxy.getInstance().getConfig().getServer().luckyBlockChance.get() * chanceBonus)
        {
            // fetch default config for all level
            // override it if specific config for this level is available.
            List<ItemStorage> luckyOresInLevel = luckyOres.get(0);
            if (luckyOres.containsKey(buildingLevel)) {
                luckyOresInLevel = luckyOres.get(buildingLevel);
            }

            return luckyOresInLevel.get(random.nextInt(luckyOres.size())).getItemStack().copy();
        }
        return ItemStack.EMPTY;
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
    public ImmutableSet<ResourceLocation> getAllMonsters()
    {
        if (monsters.isEmpty()) Log.getLogger().error("getAllMonsters when empty");
        return monsters;
    }

    //------------------------------- Private Utility Methods -------------------------------//

    /**
     * Calculate all monsters.
     */
    private void discoverMobs()
    {
        Set<ResourceLocation> monsterSet = new HashSet<>();

        for (final Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : ForgeRegistries.ENTITY_TYPES.getEntries())
        {
            if (entry.getValue().getCategory() == MobCategory.MONSTER)
            {
                monsterSet.add(entry.getKey().location());
            }
            else if (entry.getValue().is(ModTags.hostile))
            {
                monsterSet.add(entry.getKey().location());
            }

        }

        monsters = ImmutableSet.copyOf(monsterSet);
    }

    /**
     * Create complete list of all existing items.
     */
    private void discoverAllItems()
    {
        final ImmutableList.Builder<ItemStack> listBuilder = new ImmutableList.Builder<>();
        final ImmutableSet.Builder<ItemStorage> setBuilder = new ImmutableSet.Builder<>();

        for (final Item item : ForgeRegistries.ITEMS.getValues())
        {
            final NonNullList<ItemStack> list = NonNullList.create();
            try
            {
                item.fillItemCategory(CreativeModeTab.TAB_SEARCH, list);
            }
            catch (Exception e)
            {
                Log.getLogger().warn("Error populating items for " + ForgeRegistries.ITEMS.getKey(item) + "; using fallback", e);
                list.add(new ItemStack(item));
            }
            listBuilder.addAll(list);

            for (final ItemStack stack : list)
            {
                setBuilder.add(new ItemStorage(stack, true));
            }
        }

        allItems = listBuilder.build();
        allItemsSet = setBuilder.build();
    }

    /**
     * Discover ores for the Smelter and Miners.
     */
    private void discoverOres()
    {
        if (smeltableOres.isEmpty())
        {
            Set<Item> m = Stream.of(Tags.Items.ORES, ModTags.breakable_ore, ModTags.raw_ore)
                    .flatMap(tag -> ForgeRegistries.ITEMS.tags().getTag(tag).stream())
                    .collect(Collectors.toSet());

            for (Item item : m)
            {
                final NonNullList<ItemStack> list = NonNullList.create();
                item.fillItemCategory(CreativeModeTab.TAB_SEARCH, list);

                for (final ItemStack stack : list)
                {
                    if (stack.getItem() instanceof BlockItem)
                    {
                        oreBlocks.add(((BlockItem) stack.getItem()).getBlock());
                    }
                    if (!MinecoloniesAPIProxy.getInstance().getFurnaceRecipes().getSmeltingResult(stack).isEmpty())
                    {
                        smeltableOres.add(new ItemStorage(stack));
                    }
                }
            }
        }
        Log.getLogger().info("Finished discovering Ores " + oreBlocks.size() + " " + smeltableOres.size());
    }

    /**
     * Discover saplings from the vanilla Saplings tag, used for the Forester
     */
    private void discoverSaplings()
    {
        for (final Item item : ForgeRegistries.ITEMS.tags().getTag(ItemTags.SAPLINGS))
        {
            final NonNullList<ItemStack> list = NonNullList.create();
            item.fillItemCategory(CreativeModeTab.TAB_SEARCH, list);
            for (final ItemStack stack : list)
            {
                saplings.add(new ItemStorage(stack, false, true));
            }
        }
        Log.getLogger().info("Finished discovering saplings " + saplings.size());
    }

    /**
     * Create complete list of compost recipes.
     * @param recipeManager recipe manager
     */
    private void discoverCompostRecipes(@NotNull final RecipeManager recipeManager)
    {
        if (compostRecipes.isEmpty())
        {
            discoverCompostRecipes(recipeManager.byType(ModRecipeSerializer.CompostRecipeType.get()).values().stream()
                    .map(r -> (CompostRecipe) r).toList());
            Log.getLogger().info("Finished discovering compostables " + compostRecipes.size());
        }
    }

    private void discoverCompostRecipes(@NotNull final List<CompostRecipe> recipes)
    {
        for (final CompostRecipe recipe : recipes)
        {
            for (final ItemStack stack : recipe.getInput().getItems())
            {
                // there can be duplicates due to overlapping tags.  weakest one wins.
                compostRecipes.merge(stack.getItem(), recipe,
                        (r1, r2) -> r1.getStrength() < r2.getStrength() ? r1 : r2);
            }
        }
    }

    /**
     * Create complete list of plantable items, from the "minecolonies:florist_flowers" tag, for the Florist.
     */
    private void discoverPlantables()
    {
        if (plantables.isEmpty())
        {
            for (final Item item : ForgeRegistries.ITEMS.tags().getTag(ModTags.floristFlowers))
            {
                final NonNullList<ItemStack> list = NonNullList.create();
                item.fillItemCategory(CreativeModeTab.TAB_SEARCH, list);
                for (final ItemStack stack : list)
                {
                    if (stack.getItem() instanceof BlockItem)
                    {
                        plantables.add(new ItemStorage(stack));
                    }
                }
            }
        }
        Log.getLogger().info("Finished discovering plantables " + plantables.size());
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
                if(FurnaceBlockEntity.isFuel(item))
                {
                    fuel.add(new ItemStorage(item));
                }
            }
        }
        Log.getLogger().info("Finished discovering fuel " + fuel.size());
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
        Log.getLogger().info("Finished discovering food " + edibles.size() + " " + food.size());
    }

    /**
     * Run through all blocks and check if they match one of our lucky oreBlocks.
     */
    private void discoverLuckyOres()
    {
        if (luckyOres.isEmpty())
        {
            final int defaultMineLevelName = 0;
            for (final String ore : MinecoloniesAPIProxy.getInstance().getConfig().getServer().luckyOres.get())
            {
                final String pattern = "([\\w:]+)!(\\d+)(?:#(\\d+))?";
                final Pattern r = Pattern.compile(pattern);
                final Matcher m = r.matcher(ore);
                if (!m.find())
                {
                    Log.getLogger().warn("Wrong configured ore: " + ore);
                    continue;
                }

                final String oreName = m.group(1);
                final String chance = m.group(2);
                final String detectedLevelName = m.group(3);

                String mineLevelName = defaultMineLevelName + "";
                if (m.groupCount() == 3 && null != detectedLevelName) {
                    mineLevelName = m.group(3);
                }

                discoverLuckyOre(ore, oreName, chance, mineLevelName);
            }

            List<ItemStorage> alternative = null;
            int mineMaxLevel = 5;
            for (int levelToTest = 0; levelToTest <= mineMaxLevel; levelToTest++) {
                if (luckyOres.containsKey(levelToTest) && !luckyOres.get(levelToTest).isEmpty()) {
                    alternative = luckyOres.get(levelToTest);
                }
            }

            for (int levelToReplace = 0; levelToReplace <= mineMaxLevel; levelToReplace++) {
                luckyOres.putIfAbsent(levelToReplace, alternative);
            }

        }
        Log.getLogger().info("Finished discovering lucky oreBlocks " + luckyOres.size());
    }

    private void discoverLuckyOre(String ore, String oreName, String chance, String mineLevelName) {
        final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(oreName));
        if (item == null || item == Items.AIR) {
            Log.getLogger().warn("Invalid lucky block: " + ore);
            return;
        }

        final ItemStack stack = new ItemStack(item, 1);
        try {
            final int rarity = Integer.parseInt(chance);
            final int buildingLevel = Integer.parseInt(mineLevelName);
            luckyOres.putIfAbsent(buildingLevel, new ArrayList<>());

            for (int i = 0; i < rarity; i++) {
                List<ItemStorage> luckyOreOnLevel = luckyOres.get(buildingLevel);
                luckyOreOnLevel.add(new ItemStorage(stack));
            }
        } catch (final NumberFormatException ex) {
            Log.getLogger().warn("Ore has invalid rarity or building level: " + ore);
        }
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

    private static CompoundTag writeLeafSaplingEntryToNBT(final BlockState state, final ItemStorage storage)
    {
        final CompoundTag compound = NbtUtils.writeBlockState(state);
        storage.getItemStack().save(compound);
        return compound;
    }

    private static Tuple<BlockState, ItemStorage> readLeafSaplingEntryFromNBT(final CompoundTag compound)
    {
        return new Tuple<>(NbtUtils.readBlockState(compound), new ItemStorage(ItemStack.of(compound), false, true));
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
        if (ModList.get().isLoaded("tconstruct"))
        {
            Compatibility.tinkersCompat = new TinkersToolHelper();
            Compatibility.tinkersSlimeCompat = new SlimeTreeCheck();
        }
        if (ModList.get().isLoaded("dynamictrees"))
        {
            Compatibility.dynamicTreesCompat = new DynamicTreeCompat();
        }
    }

    /**
     * Discover all the possible flowers for the beekeeper.
     *
     */
    private void discoverFlowers()
    {
        if (flowers.isEmpty())
        {
            final HashSet<ItemStorage> tempFlowers = new HashSet<ItemStorage>();
            for (final Item item : ForgeRegistries.ITEMS.tags().getTag(ItemTags.FLOWERS))
            {
                final NonNullList<ItemStack> list = NonNullList.create();
                item.fillItemCategory(CreativeModeTab.TAB_SEARCH, list);
                for (final ItemStack stack : list)
                {
                    tempFlowers.add(new ItemStorage(stack));
                }
            }
            flowers = ImmutableSet.copyOf(tempFlowers);
        }
    }
}
