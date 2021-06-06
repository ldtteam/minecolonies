package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.IBuildingWorkerView;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ClassicRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.MultiOutputRecipe;
import com.minecolonies.api.crafting.RecipeStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.crafting.CustomRecipe;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingRequestResolver;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.BuildingHiringModeMessage;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.research.util.ResearchConstants.RECIPES;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_MAXIMUM;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.TranslationConstants.RECIPE_IMPROVED;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * The abstract class for each worker building.
 */
public abstract class AbstractBuildingWorker extends AbstractBuilding implements IBuildingWorker
{
    /**
     * Breeding setting.
     */
    public static final ISettingKey<BoolSetting> BREEDING = new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "breeding"));

    /**
     * The base chance for a recipe to be improved. This is modified by worker skill and the number of items crafted
     */
    private static final double BASE_CHANCE = 0.0625;

    /**
     * Extra amount of recipes the crafters can learn.
     */
    private static final int EXTRA_RECIPE_MULTIPLIER = 5;

    /**
     * The name of the tag for improving recipes
     */
    private static final String REDUCEABLE = "reduceable";

    /**
     * The list of recipes the worker knows, correspond to a subset of the recipes in the colony.
     */
    protected final List<IToken<?>> recipes = new ArrayList<>();

    /**
     * The hiring mode of this particular building, by default overriden by colony mode.
     */
    private HiringMode hiringMode = HiringMode.DEFAULT;

    /**
     * The display name of the job - post localization
     */
    private String jobDisplayName = "";

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public AbstractBuildingWorker(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public boolean isItemStackInRequest(@Nullable final ItemStack stack)
    {
        if (stack == null || stack.getItem() == null)
        {
            return false;
        }

        for (final ICitizenData data : getAssignedCitizen())
        {
            for (final IRequest<?> request : getOpenRequests(data))
            {
                for (final ItemStack deliveryStack : request.getDeliveries())
                {
                    if (deliveryStack.isItemEqualIgnoreDurability(stack))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void setHiringMode(final HiringMode hiringMode)
    {
        this.hiringMode = hiringMode;
        this.markDirty();
    }

    @Override
    public HiringMode getHiringMode()
    {
        return hiringMode;
    }

    /**
     * Override this method if you want to keep an amount of items in inventory. When the inventory is full, everything get's dumped into the building chest. But you can use this
     * method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = new HashMap<>(super.getRequiredItemsAndAmount());
        if (keepFood())
        {
            toKeep.put(stack -> ItemStackUtils.CAN_EAT.test(stack) && canEat(stack), new Tuple<>(getBuildingLevel() * 2, true));
        }
        return toKeep;
    }

    @Override
    @Nullable
    public IRecipeStorage getFirstRecipe(final ItemStack stack)
    {
        return getFirstRecipe(itemStack -> !itemStack.isEmpty() && ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, stack, true, true));
    }

    @Override
    @Nullable
    public IRecipeStorage getFirstRecipe(final Predicate<ItemStack> stackPredicate)
    {
        IRecipeStorage foundRecipe = null;
        final HashMap<IRecipeStorage, Integer> candidates = new HashMap<>();

        //Scan through and collect all possible recipes that could fulfill this, taking special note of the first one
        for (final IToken<?> token : recipes)
        {
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if (storage != null && (stackPredicate.test(storage.getPrimaryOutput()) || storage.getAlternateOutputs().stream().anyMatch(i -> stackPredicate.test(i))))
            {
                if(foundRecipe == null)
                {
                    foundRecipe = storage;
                }
                candidates.put(storage, 0);
            }
        }

        //If we have more than one possible recipe, let's choose the one with the most stock in the warehouses
        if(candidates.size() > 1)
        {
            for(Map.Entry<IRecipeStorage, Integer> foo : candidates.entrySet())
            {
                final ItemStorage checkItem = foo.getKey().getCleanedInput().stream().max(Comparator.comparingInt(ItemStorage::getAmount)).get();
                candidates.put(foo.getKey(), getWarehouseCount(checkItem));
            }
            foundRecipe = candidates.entrySet().stream().min(Map.Entry.comparingByValue(Comparator.reverseOrder())).get().getKey();
        }

        if(foundRecipe != null && foundRecipe.getRecipeType() instanceof MultiOutputRecipe)
        {
            foundRecipe = foundRecipe.getClassicForMultiOutput(stackPredicate);
        }

        return foundRecipe;
    }

    /**
     * Get the count of items in all the warehouses
     */
    protected int getWarehouseCount(ItemStorage item)
    {
        int count = 0;
        final List<IWareHouse> wareHouses = colony.getBuildingManager().getWareHouses();

        for(IWareHouse wareHouse: wareHouses)
        {
            count += InventoryUtils.getCountFromBuilding(wareHouse, item);
        }
        return count;
    }

    @Override
    public IRecipeStorage getFirstFullFillableRecipe(final ItemStack tempStack, final boolean considerReservation)
    {
        return getFirstFullFillableRecipe(tempStack, tempStack.getCount(), considerReservation);
    }

    @Override
    public IRecipeStorage getFirstFullFillableRecipe(final ItemStack tempStack, int count, final boolean considerReservation)
    {
        return getFirstFullFillableRecipe(itemStack -> !itemStack.isEmpty() && ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, tempStack, true, true), count * tempStack.getCount(), considerReservation);
    }

    @Override
    public IRecipeStorage getFirstFullFillableRecipe(final Predicate<ItemStack> stackPredicate, final int count, final boolean considerReservation)
    {
        for (final IToken<?> token : recipes)
        {
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if (storage != null && (stackPredicate.test(storage.getPrimaryOutput()) || storage.getAlternateOutputs().stream().anyMatch(i -> stackPredicate.test(i))))
            {
                final List<IItemHandler> handlers = getHandlers();
                IRecipeStorage toTest = storage.getRecipeType() instanceof MultiOutputRecipe ? storage.getClassicForMultiOutput(stackPredicate) : storage;
                if (toTest.canFullFillRecipe(count, Collections.emptyMap(), handlers.toArray(new IItemHandler[0])))
                {
                    return toTest;
                }
            }
        }
        return null;
    }

    @Override
    public boolean fullFillRecipe(final IRecipeStorage storage)
    {
        final List<IItemHandler> handlers = getHandlers();

        final AbstractEntityCitizen worker = this.getMainCitizen().getEntity().orElse(null);

        if(worker == null)
        {
            // we shouldn't hit this case, but just in case...
            return storage.fullfillRecipe(this.getColony().getWorld(), handlers);
        }

        final int primarySkill =worker.getCitizenData().getCitizenSkillHandler().getLevel(this.getPrimarySkill());
        final int luck = (int)(((primarySkill + 1) * 2) - Math.pow((primarySkill + 1 ) / 10.0, 2));

        LootContext.Builder builder =  (new LootContext.Builder((ServerWorld) this.getColony().getWorld())
        .withParameter(LootParameters.field_237457_g_, worker.getPositionVec())
        .withParameter(LootParameters.THIS_ENTITY, worker)
        .withParameter(LootParameters.TOOL, worker.getHeldItemMainhand())
        .withRandom(worker.getRandom())
        .withLuck((float) luck));

        return storage.fullfillRecipe(builder.build(RecipeStorage.recipeLootParameters), handlers);
    }

    @Override
    public void switchIndex(final int i, final int j)
    {
        if (i < recipes.size() && j < recipes.size() && i >= 0 && j >= 0)
        {
            final IToken<?> storage = recipes.get(i);
            recipes.set(i, recipes.get(j));
            recipes.set(j, storage);
        }
    }

    @Override
    public boolean canRecipeBeAdded(final IToken<?> ignored)
    {
        return hasSpaceForMoreRecipes();
    }

    /**
     * @param token
     * @return whether the recipe can bee added based on tokens.
     */
    protected Optional<Boolean> canRecipeBeAddedBasedOnTags(final IToken<?> token)
    {
        final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
        return canRecipeBeAddedBasedOnTags(storage);
    }

    /**
     * @param storage
     * @return whether the recipe can bee added based on tokens.
     */
    protected Optional<Boolean> canRecipeBeAddedBasedOnTags(final IRecipeStorage storage)
    {
        if (storage == null)
        {
            return Optional.of(false);
        }

        final String crafterName = this.getJobName().toLowerCase();

        // Check against excluded products
        if (ModTags.crafterProductExclusions.containsKey(crafterName) && ModTags.crafterProductExclusions.get(crafterName).contains(storage.getPrimaryOutput().getItem()))
        {
            return Optional.of(false);
        }

        // Check against allowed products
        if (ModTags.crafterProduct.containsKey(crafterName) && ModTags.crafterProduct.get(crafterName).contains(storage.getPrimaryOutput().getItem()))
        {
            return Optional.of(true);
        }

        // Check against excluded ingredients
        for (final ItemStorage stack : storage.getInput())
        {
            if (ModTags.crafterIngredientExclusions.containsKey(crafterName) && ModTags.crafterIngredientExclusions.get(crafterName).contains(stack.getItem()))
            {
                return Optional.of(false);
            }
        }

        // Check against allowed ingredients
        for (final ItemStorage stack : storage.getInput())
        {
            if (ModTags.crafterIngredient.containsKey(crafterName) && ModTags.crafterIngredient.get(crafterName).contains(stack.getItem()))
            {
                return Optional.of(true);
            }
        }

        return Optional.empty();
    }

    /**
     * Has a chance to reduce the resource requirements for the recipe in this building
     * 
     * @param recipe the recipe we're possibly improving
     * @param count the number of items (chances)
     * @param citizen The citizen, as the primary skill can improve the chances
     */
    public void improveRecipe(IRecipeStorage recipe, int count, ICitizenData citizen)
    {
        final List<ItemStorage> inputs = recipe.getCleanedInput().stream().sorted(Comparator.comparingInt(ItemStorage::getAmount).reversed()).collect(Collectors.toList());

        final double actualChance = Math.min(5.0, (BASE_CHANCE * count) + (BASE_CHANCE * citizen.getCitizenSkillHandler().getLevel(getRecipeImprovementSkill())));
        final double roll = citizen.getRandom().nextDouble() * 100;

        ItemStorage reducedItem = null;

        if(roll <= actualChance && ModTags.crafterProductExclusions.containsKey(REDUCEABLE) && !ModTags.crafterProductExclusions.get(REDUCEABLE).contains(recipe.getPrimaryOutput().getItem()))
        {
            final ArrayList<ItemStorage> newRecipe = new ArrayList<>();
            boolean didReduction = false;
            for(ItemStorage input : inputs)
            {
                // Check against excluded products
                if (input.getAmount() > 1 && ModTags.crafterIngredient.containsKey(REDUCEABLE) && ModTags.crafterIngredient.get(REDUCEABLE).contains(input.getItem()))
                {
                    reducedItem = input.copy();
                    reducedItem.setAmount(input.getAmount() - 1);
                    newRecipe.add(reducedItem.toImmutable());
                    didReduction = true;
                }
                else
                {
                    newRecipe.add(input.copy().toImmutable());
                }
            }

            if (didReduction)
            {
                final IRecipeStorage storage = StandardFactoryController.getInstance().getNewInstance(
                    TypeConstants.RECIPE,
                    StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                    newRecipe,
                    1,
                    recipe.getPrimaryOutput(),
                    Blocks.AIR);

                replaceRecipe(recipe.getToken(), IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(storage));

                // Expected parameters for RECIPE_IMPROVED are Job, Result, Ingredient, Citizen
                final TranslationTextComponent message = new TranslationTextComponent(RECIPE_IMPROVED + citizen.getRandom().nextInt(3),
                    new TranslationTextComponent(citizen.getJob().getName().toLowerCase()),
                    recipe.getPrimaryOutput().getDisplayName(),
                    reducedItem.getItemStack().getDisplayName(),
                    citizen.getName());

                for(PlayerEntity player :colony.getMessagePlayerEntities())
                {
                    player.sendMessage(message, player.getUniqueID());
                }
            }
        }
    }

    @Override
    @NotNull
    public Skill getRecipeImprovementSkill()
    {
        return getSecondarySkill();
    }

    @Override
    public boolean isRecipeAlterationAllowed()
    {
        return true;
    }

    /**
     * Check if the worker has more space for recipes.
     *
     * @return true if so.
     */
    private boolean hasSpaceForMoreRecipes()
    {
        return getMaxRecipes() > getRecipes().size();
    }

    /**
     * Gets the maximum number of recipes a building may have at the current time.
     */
    protected int getMaxRecipes()
    {
        final double increase;
        if(canCraftComplexRecipes())
        {
            increase = (1 + colony.getResearchManager().getResearchEffects().getEffectStrength(RECIPES)) * EXTRA_RECIPE_MULTIPLIER;
        }
        else
        {
            increase = 1 + colony.getResearchManager().getResearchEffects().getEffectStrength(RECIPES);
        }
        return (int) (Math.pow(2, getBuildingLevel()) * increase);
    }

    @Override
    public List<IToken<?>> getRecipes()
    {
        return new ArrayList<>(recipes);
    }

    @Override
    public List<IItemHandler> getHandlers()
    {
        final IColony colony = getColony();
        if (this.getAssignedEntities().isEmpty() || colony == null || colony.getWorld() == null)
        {
            return Collections.emptyList();
        }

        final Set<IItemHandler> handlers = new HashSet<>();
        for (final ICitizenData workerEntity : this.getAssignedCitizen())
        {
            handlers.add(workerEntity.getInventory());
        }

        for (final BlockPos pos : getContainers())
        {
            final TileEntity entity = colony.getWorld().getTileEntity(pos);
            if (entity != null)
            {
                final LazyOptional<IItemHandler> handler = entity.getCapability(ITEM_HANDLER_CAPABILITY, null);
                handler.ifPresent(handlers::add);
            }
        }
        return ImmutableList.copyOf(handlers);
    }

    @Override
    public boolean assignCitizen(final ICitizenData citizen)
    {
        if (citizen.getWorkBuilding() != null)
        {
            citizen.getWorkBuilding().removeCitizen(citizen);
        }

        if (!super.assignCitizen(citizen))
        {
            Log.getLogger().warn("Unable to assign citizen:" + citizen.getName() + " to building:" + this.getSchematicName() + " jobname:" + this.getJobName());
            return false;
        }

        // If we set a worker, inform it of such
        if (citizen != null)
        {
            citizen.setWorkBuilding(this);
            citizen.getJob().onLevelUp();
            colony.getProgressManager()
              .progressEmploy(colony.getCitizenManager().getCitizens().stream().filter(citizenData -> citizenData.getJob() != null).collect(Collectors.toList()).size());
        }

        updateWorkerAvailableForRecipes();
        return true;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        if (compound.keySet().contains(TAG_WORKER))
        {
            try
            {
                final ListNBT workersTagList = compound.getList(TAG_WORKER, Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < workersTagList.size(); ++i)
                {
                    final ICitizenData data;
                    if (workersTagList.getCompound(i).keySet().contains(TAG_ID))
                    {
                        data = getColony().getCitizenManager().getCivilian(workersTagList.getCompound(i).getInt(TAG_ID));
                    }
                    else if (workersTagList.getCompound(i).keySet().contains(TAG_WORKER_ID))
                    {
                        data = getColony().getCitizenManager().getCivilian(workersTagList.getCompound(i).getInt(TAG_WORKER_ID));
                    }
                    else
                    {
                        data = null;
                    }

                    if (data != null)
                    {
                        assignCitizen(data);
                    }
                }
            }
            catch (final Exception e)
            {
                Log.getLogger().warn("Warning: Updating data structures:", e);
                final ICitizenData worker = getColony().getCitizenManager().getCivilian(compound.getInt(TAG_WORKER));
                assignCitizen(worker);
            }
        }

        this.hiringMode = HiringMode.values()[compound.getInt(TAG_HIRING_MODE)];

        final ListNBT recipesTags = compound.getList(TAG_RECIPES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < recipesTags.size(); i++)
        {
            final IToken<?> token = StandardFactoryController.getInstance().deserialize(recipesTags.getCompound(i));
            if (!recipes.contains(token))
            {
                recipes.add(token);
                IColonyManager.getInstance().getRecipeManager().registerUse(token);
            }
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        @NotNull final ListNBT workersTagList = new ListNBT();
        for (@NotNull final ICitizenData data : getAssignedCitizen())
        {
            if (data != null)
            {
                final CompoundNBT idCompound = new CompoundNBT();
                idCompound.putInt(TAG_WORKER_ID, data.getId());
                workersTagList.add(idCompound);
            }
        }
        compound.put(TAG_WORKER, workersTagList);

        compound.putInt(TAG_HIRING_MODE, this.hiringMode.ordinal());
        @NotNull final ListNBT recipesTagList = recipes.stream()
            .map(iToken -> StandardFactoryController.getInstance().serialize(iToken))
            .collect(NBTUtils.toListNBT());
        compound.put(TAG_RECIPES, recipesTagList);
        return compound;
    }

    @Override
    public void onWakeUp()
    {

    }

    @Override
    public boolean addRecipe(final IToken<?> token)
    {
        if (canRecipeBeAdded(token))
        {
            addRecipeToList(token, false);
            markDirty();

            if (getAssignedCitizen().isEmpty())
            {
                return true;
            }

            final IRecipeStorage recipeStorage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if (recipeStorage != null)
            {
                colony.getRequestManager()
                    .onColonyUpdate(request -> request.getRequest() instanceof IDeliverable && ((IDeliverable) request.getRequest()).matches(recipeStorage.getPrimaryOutput()));
            }
            return true;
        }
        return false;
    }

    /**
     * Updates existing requests, if they match the recipes available at this worker
     */
    private void updateWorkerAvailableForRecipes()
    {
        for (final IToken<?> token : recipes)
        {
            final IRecipeStorage recipeStorage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if (recipeStorage != null)
            {
                colony.getRequestManager()
                  .onColonyUpdate(request -> request.getRequest() instanceof IDeliverable && ((IDeliverable) request.getRequest()).matches(recipeStorage.getPrimaryOutput()));
            }
        }
    }

    /**
     * Add a recipe to the list of recipes.
     *
     * @param token the token to add.
     */
    public void addRecipeToList(final IToken<?> token, boolean atTop)
    {
        if (!recipes.contains(token))
        {
            if(atTop)
            {
                recipes.add(0, token);
            }
            else
            {
                recipes.add(token);
            }
        }
    }

    @Override
    public void removeRecipe(final IToken<?> token)
    {
        if(recipes.remove(token))
        {
            markDirty();
        }
        else
        {
            Log.getLogger().warn("Failure to remove recipe, please tell the mod authors about this");
            recipes.clear();
        }
    }

    /**
     * Replace one current recipe with a new one
     * @param oldRecipe the recipe to replace
     * @param newRecipe the new version
     */
    public void replaceRecipe(final IToken<?> oldRecipe, final IToken<?> newRecipe)
    {
        if (recipes.contains(oldRecipe))
        {
            int oldIndex = recipes.indexOf(oldRecipe);
            recipes.add(oldIndex, newRecipe);
            recipes.remove(oldRecipe);
            markDirty();
        }
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        super.onColonyTick(colony);
        checkForWorkerSpecificRecipes();

        // If we have no active worker, grab one from the Colony
        if (!isFull() && ((getBuildingLevel() > 0 && isBuilt()) || this instanceof BuildingBuilder)
            && (this.hiringMode == HiringMode.DEFAULT && !this.getColony().isManualHiring() || this.hiringMode == HiringMode.AUTO))
        {
            final ICitizenData joblessCitizen = getColony().getCitizenManager().getJoblessCitizen();
            if (joblessCitizen != null)
            {
                assignCitizen(joblessCitizen);
            }
        }
    }

    @Override
    public void removeCitizen(final ICitizenData citizen)
    {
        if (isCitizenAssigned(citizen))
        {
            super.removeCitizen(citizen);
            citizen.setWorkBuilding(null);
            cancelAllRequestsOfCitizen(citizen);
            citizen.setVisibleStatus(null);
        }
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);

        buf.writeInt(getAssignedCitizen().size());
        for (final ICitizenData data : getAssignedCitizen())
        {
            buf.writeInt(data == null ? 0 : data.getId());
        }
        final List<IRecipeStorage> storages = new ArrayList<>();
        Map<ResourceLocation, CustomRecipe> crafterRecipes = CustomRecipeManager.getInstance().getAllRecipes().get(getJobName());
        for (final IToken<?> token : new ArrayList<>(recipes))
        {
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if (storage == null || (storage.getRecipeSource() != null && !crafterRecipes.containsKey(storage.getRecipeSource())))
            {
                removeRecipe(token);
            }
            else
            {
                storages.add(storage);
            }
        }

        buf.writeInt(storages.size());
        for (final IRecipeStorage storage : storages)
        {
            buf.writeCompoundTag(StandardFactoryController.getInstance().serialize(storage));
        }

        buf.writeBoolean(canCraftComplexRecipes());
        buf.writeInt(hiringMode.ordinal());
        buf.writeString(this.getJobName());
        buf.writeInt(getMaxInhabitants());
        buf.writeInt(getPrimarySkill().ordinal());
        buf.writeInt(getSecondarySkill().ordinal());
        buf.writeInt(getMaxInhabitants());
        buf.writeBoolean(isRecipeAlterationAllowed());
        buf.writeString(getJobDisplayName());
        buf.writeInt(getMaxRecipes());
    }

    @Override
    public int getMaxToolLevel()
    {
        if (getBuildingLevel() >= getMaxBuildingLevel())
        {
            return TOOL_LEVEL_MAXIMUM;
        }
        else if (getBuildingLevel() <= WOOD_HUT_LEVEL)
        {
            return TOOL_LEVEL_WOOD_OR_GOLD;
        }
        return getBuildingLevel() - WOOD_HUT_LEVEL;
    }

    @Override
    public boolean canWorkDuringTheRain()
    {
        return getBuildingLevel() >= getMaxBuildingLevel();
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        return ImmutableList.of(
            new BuildingRequestResolver(getRequester().getLocation(), getColony().getRequestManager()
                                                                        .getFactoryController().getNewInstance(TypeConstants.ITOKEN)),
            new PrivateWorkerCraftingRequestResolver(getRequester().getLocation(), getColony().getRequestManager()
                                                                        .getFactoryController().getNewInstance(TypeConstants.ITOKEN)),
            new PrivateWorkerCraftingProductionResolver(getRequester().getLocation(), getColony().getRequestManager()
                                                                        .getFactoryController().getNewInstance(TypeConstants.ITOKEN)));
    }

    @Override
    public boolean canCraftComplexRecipes()
    {
        return false;
    }

    @Override
    public void openCraftingContainer(final ServerPlayerEntity player)
    {
        NetworkHooks.openGui(player, new INamedContainerProvider()
        {
            @Override
            public ITextComponent getDisplayName()
            {
                return new StringTextComponent("Crafting GUI");
            }

            @NotNull
            @Override
            public Container createMenu(final int id, @NotNull final PlayerInventory inv, @NotNull final PlayerEntity player)
            {
                return new ContainerCrafting(id, inv, canCraftComplexRecipes(), getID());
            }
        }, buffer -> new PacketBuffer(buffer.writeBoolean(canCraftComplexRecipes())).writeBlockPos(getID()));
    }

    @Override
    public void onBuildingMove(final IBuilding oldBuilding)
    {
        super.onBuildingMove(oldBuilding);
        final List<ICitizenData> workers = oldBuilding.getAssignedCitizen();
        for (final ICitizenData citizen : workers)
        {
            citizen.setWorkBuilding(null);
            citizen.setWorkBuilding(this);
            this.assignCitizen(citizen);
        }
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        if (stack.getItem().getFood().getHealing() >= getBuildingLevel())
        {
            return true;
        }
        return false;
    }

    /**
     * Check for worker specific recipes and add them if necessary.
     */
    public void checkForWorkerSpecificRecipes()
    {
        for(final CustomRecipe newRecipe : CustomRecipeManager.getInstance().getRecipes(getJobName()))
        {
            final IRecipeStorage recipeStorage = newRecipe.getRecipeStorage();
            final IToken<?> recipeToken = IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(recipeStorage);

            if(newRecipe.isValidForBuilding(this))
            {
                IToken<?> duplicateFound = null;
                boolean forceReplace = false;
                for(IToken<?> token : recipes)
                {
                    if(token == recipeToken)
                    {
                        duplicateFound = token;
                        break;
                    }
                    final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);

                    //Let's verify that this recipe doesn't exist in an improved form
                    if(storage != null && storage.getPrimaryOutput().equals(recipeStorage.getPrimaryOutput(), true))
                    {
                        List<ItemStorage> recipeInput1 = storage.getCleanedInput();
                        List<ItemStorage> recipeInput2 = recipeStorage.getCleanedInput();

                        if(recipeInput1.size() != recipeInput2.size())
                        {
                            continue;
                        }

                        if(recipeInput1.size() > 1)
                        {
                            recipeInput1.sort(Comparator.comparing(item -> Objects.hash(item.hashCode(), item.getAmount())));
                            recipeInput2.sort(Comparator.comparing(item -> Objects.hash(item.hashCode(), item.getAmount())));
                        }

                        boolean allMatch = true;
                        for(int i=0; i<recipeInput1.size(); i++)
                        {
                            if(!recipeInput1.get(i).getItem().equals(recipeInput2.get(i).getItem()))
                            {
                                allMatch = false;
                                break;
                            }
                        }
                        if(allMatch)
                        {
                            duplicateFound = token;
                            if(storage.getRecipeType() instanceof ClassicRecipe && recipeStorage.getRecipeType() instanceof MultiOutputRecipe)
                            {
                                //This catches the old custom recipes without a RecipeSource
                                forceReplace = true;
                            }
                            if(storage.getRecipeSource() != null && storage.getRecipeSource().equals(recipeStorage.getRecipeSource()))
                            {
                                //This will only happen if the tokens don't match, aka: the recipe has changed.
                                forceReplace = true;
                            }
                            break;
                        }
                    }
                }
                if(duplicateFound == null)
                {
                    addRecipeToList(recipeToken, true);
                    colony.getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable && ((IDeliverable) request.getRequest()).matches(recipeStorage.getPrimaryOutput()));
                    markDirty();
                }
                else if((forceReplace || newRecipe.getMustExist()) && !(duplicateFound.equals(recipeToken)))
                {
                    //We found the base recipe for a multi-recipe, replace it with the multi-recipe
                    replaceRecipe(duplicateFound, recipeToken);
                    colony.getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable && ((IDeliverable) request.getRequest()).matches(recipeStorage.getPrimaryOutput()));

                    //Clean up old 'classic' recipes that the new multi-recipe replaces
                    final List<ItemStack> alternates = recipeStorage.getAlternateOutputs();
                    for(IToken<?> token : this.getRecipes())
                    {
                        final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
                        if(storage.getRecipeType() instanceof ClassicRecipe && ItemStackUtils.compareItemStackListIgnoreStackSize(alternates, storage.getPrimaryOutput(), false, true))
                        {
                            removeRecipe(token);
                        }
                    }
                    colony.getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable && recipeStorage.getAlternateOutputs().stream().anyMatch(i -> ((IDeliverable) request.getRequest()).matches(i)));
                    markDirty();
                }
            }
            else
            {
                if(recipes.contains(recipeToken))
                {
                    removeRecipe(recipeToken);
                    markDirty();
                }
            }
        }
    }

    /**
     * Get the Job DisplayName
     */
    public String getJobDisplayName()
    {
        if (jobDisplayName.isEmpty())
        {
            jobDisplayName = createJob(null).getName();
        }
        return jobDisplayName;
    }

    /**
     * Get setting for key. Utility function.
     * @param key the key.
     * @param <T> the key type.
     * @return the optional wrapping the value.
     */
    public <T extends ISetting> T getSetting(@NotNull final ISettingKey<T> key)
    {
        return getFirstModuleOccurance(ISettingsModule .class).getSetting(key);
    }

    /**
     * AbstractBuildingWorker View for clients.
     */
    public static class View extends AbstractBuildingView implements IBuildingWorkerView
    {
        /**
         * List of the worker ids.
         */
        private final Set<Integer> workerIDs = new HashSet<>();

        /**
         * List of recipes.
         */
        private final List<IRecipeStorage> recipes = new ArrayList<>();

        /**
         * Variable defining if the building owner can craft complex 3x3 recipes.
         */
        private boolean canCraftComplexRecipes;

        /**
         * The hiring mode of the building.
         */
        private HiringMode hiringMode;

        /**
         * The name of the job.
         */
        private String jobName;

        /**
         * The max amount of inhabitants
         */
        private int maxInhabitants = 1;

        /**
         * The primary skill.
         */
        private Skill primary = Skill.Intelligence;

        /**
         * The secondary skill.
         */
        private Skill secondary = Skill.Intelligence;

        /**
         * The maximum number of recipes this building can have currently
         */
        private int maxRecipes;

        /**
         * If the building allows altering of recipes
         */
        private boolean isRecipeAlterationAllowed;

        /**
         * The job display name
         */
        private String jobDisplayName;

        /**
         * Creates the view representation of the building.
         *
         * @param c the colony.
         * @param l the location.
         */
        public View(final IColonyView c, @NotNull final BlockPos l)
        {
            super(c, l);
        }

        @Override
        public List<Integer> getWorkerId()
        {
            return new ArrayList<>(workerIDs);
        }

        @Override
        public void addWorkerId(final int workerId)
        {
            workerIDs.add(workerId);
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);
            final int size = buf.readInt();
            workerIDs.clear();
            for (int i = 0; i < size; i++)
            {
                workerIDs.add(buf.readInt());
            }

            recipes.clear();

            final int recipesSize = buf.readInt();
            for (int i = 0; i < recipesSize; i++)
            {
                final IRecipeStorage storage = StandardFactoryController.getInstance().deserialize(buf.readCompoundTag());
                if (storage != null)
                {
                    recipes.add(storage);
                }
            }
            this.canCraftComplexRecipes = buf.readBoolean();
            this.hiringMode = HiringMode.values()[buf.readInt()];
            this.jobName = buf.readString(32767);
            this.maxInhabitants = buf.readInt();
            this.primary = Skill.values()[buf.readInt()];
            this.secondary = Skill.values()[buf.readInt()];
            this.maxInhabitants = buf.readInt();
            this.isRecipeAlterationAllowed = buf.readBoolean();
            this.jobDisplayName = buf.readString();
            this.maxRecipes=buf.readInt();
        }

        @Override
        public List<IRecipeStorage> getRecipes()
        {
            return new ArrayList<>(recipes);
        }

        @Override
        public void removeRecipe(final int i)
        {
            if (i < recipes.size() && i >= 0)
            {
                recipes.remove(i);
            }
        }

        @Override
        public void switchIndex(final int i, final int j)
        {
            if (i < recipes.size() && j < recipes.size() && i >= 0 && j >= 0)
            {
                final IRecipeStorage storage = recipes.get(i);
                recipes.set(i, recipes.get(j));
                recipes.set(j, storage);
            }
        }

        @Override
        @NotNull
        public Skill getPrimarySkill()
        {
            return primary;
        }

        @Override
        @NotNull
        public Skill getSecondarySkill()
        {
            return secondary;
        }

        @Override
        public void removeWorkerId(final int id)
        {
            workerIDs.remove(id);
        }

        @Override
        public boolean hasEnoughWorkers()
        {
            return !workerIDs.isEmpty();
        }

        @Override
        public boolean canCraftComplexRecipes()
        {
            return this.canCraftComplexRecipes;
        }

        @Override
        public boolean canRecipeBeAdded()
        {
            return getMaxRecipes() > getRecipes().size();
        }
        
        public int getMaxRecipes()
        {
            return maxRecipes;
        }

        @Override
        public boolean isRecipeAlterationAllowed()
        {
            return isRecipeAlterationAllowed;
        }

        @Override
        public HiringMode getHiringMode()
        {
            return hiringMode;
        }

        @Override
        public void setHiringMode(final HiringMode hiringMode)
        {
            this.hiringMode = hiringMode;
            Network.getNetwork().sendToServer(new BuildingHiringModeMessage(this, hiringMode));
        }

        @Override
        public String getJobName()
        {
            return this.jobName;
        }

        /**
         * Check if it is possible to assign the citizen to this building.
         *
         * @param citizenDataView the citizen to test.
         * @return true if so.
         */
        public boolean canAssign(final ICitizenDataView citizenDataView)
        {
            return true;
        }

        /**
         * Get the max number of inhabitants
         *
         * @return max inhabitants
         */
        public int getMaxInhabitants()
        {
            return this.maxInhabitants;
        }

        @Override
        public String getJobDisplayName()
        {
            return jobDisplayName;
        }
    }
}
