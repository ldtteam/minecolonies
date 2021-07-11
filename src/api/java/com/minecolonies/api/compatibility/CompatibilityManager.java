package com.minecolonies.api.compatibility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.compatibility.dynamictrees.DynamicTreeCompat;
import com.minecolonies.api.compatibility.resourcefulbees.ResourcefulBeesCompat;
import com.minecolonies.api.compatibility.tinkers.SlimeTreeCheck;
import com.minecolonies.api.compatibility.tinkers.TinkersToolHelper;
import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.*;
import net.minecraft.block.*;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

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
     * Instantiates the compatibilityManager.
     */
    public CompatibilityManager()
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void discover()
    {
        saplings.clear();
        oreBlocks.clear();
        smeltableOres.clear();
        plantables.clear();
        food.clear();
        edibles.clear();
        fuel.clear();

        luckyOres.clear();
        recruitmentCostsWeights.clear();
        diseases.clear();
        diseaseList.clear();
        freeBlocks.clear();
        freePositions.clear();
        monsters.clear();

        discoverAllItems();

        discoverSaplings();
        discoverOres();
        discoverPlantables();
        discoverFood();
        discoverFuel();
        discoverMobs();

        discoverLuckyOres();
        discoverRecruitCosts();
        discoverDiseases();
        discoverFreeBlocksAndPos();
        discoverModCompat();
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
        return !itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem && ModTags.floristFlowers.contains(itemStack.getItem());
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

        if (stack.getItem().is(Tags.Items.ORES))
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

        if (stack.getItem().is(Tags.Items.ORES))
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
    public ItemStack getRandomLuckyOre(final double chanceBonus)
    {
        if (random.nextDouble() * ONE_HUNDRED_PERCENT <= MinecoloniesAPIProxy.getInstance().getConfig().getServer().luckyBlockChance.get() * chanceBonus)
        {
            return luckyOres.get(random.nextInt(luckyOres.size())).getItemStack().copy();
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
    public void invalidateRecipes(@NotNull final RecipeManager recipeManager)
    {
        compostRecipes.clear();
        discoverCompostRecipes(recipeManager);
    }

    @Override
    public ImmutableSet<ResourceLocation> getAllMonsters()
    {
        return monsters;
    }

    //------------------------------- Private Utility Methods -------------------------------//

    /**
     * Calculate all monsters.
     */
    private void discoverMobs()
    {
        Set<ResourceLocation> monsterSet = new HashSet<>();

        for (final Map.Entry<RegistryKey<EntityType<?>>, EntityType<?>> entry : ForgeRegistries.ENTITIES.getEntries())
        {
            if (entry.getValue().getCategory() == EntityClassification.MONSTER)
            {
                monsterSet.add(entry.getKey().location());
            }
            else if (ModTags.hostile.contains(entry.getValue()))
            {
                monsterSet.add(entry.getKey().location());
            }
        }

        monsters = ImmutableSet.copyOf(monsterSet);
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
    }

    /**
     * Discover ores for the Smelter and Miners.
     */
    private void discoverOres()
    {
        if (smeltableOres.isEmpty())
        {
            for(Item item : Tags.Items.ORES.getValues())
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
    }

    /**
     * Discover saplings from the vanilla Saplings tag, used for the Forester
     */
    private void discoverSaplings()
    {
        for (final Item item : ItemTags.SAPLINGS.getValues())
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
            for (final IRecipe<?> r : recipeManager.byType(CompostRecipe.TYPE).values())
            {
                final CompostRecipe recipe = (CompostRecipe) r;
                for (final ItemStack stack : recipe.getInput().getItems())
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
     * Create complete list of plantable items, from the "minecolonies:florist_flowers" tag, for the Florist.
     */
    private void discoverPlantables()
    {
        if (plantables.isEmpty())
        {
            for (Item item : ModTags.floristFlowers.getValues())
            {
                if (item instanceof BlockItem)
                {
                    plantables.add(new ItemStorage(new ItemStack(item)));
                }
            }
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
            for(ItemStack item : allItems)
            {
                if(FurnaceTileEntity.isFuel(item))
                {
                    fuel.add(new ItemStorage(item));
                }
            }
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

    private static CompoundNBT writeLeafSaplingEntryToNBT(final BlockState state, final ItemStorage storage)
    {
        final CompoundNBT compound = NBTUtil.writeBlockState(state);
        storage.getItemStack().save(compound);
        return compound;
    }

    private static Tuple<BlockState, ItemStorage> readLeafSaplingEntryFromNBT(final CompoundNBT compound)
    {
        return new Tuple<>(NBTUtil.readBlockState(compound), new ItemStorage(ItemStack.of(compound), false, true));
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
     * Gets all the possible flowers for the beekeeper.
     * 
     * @return a set of the flowers.
     */
    public static Set<ItemStorage> getAllBeekeeperFlowers()
    {
        Set<ItemStorage> flowers = new HashSet<>();
        ItemTags.FLOWERS.getValues().forEach((item) -> flowers.add(new ItemStorage(new ItemStack(item))));
        return flowers;
    }
}