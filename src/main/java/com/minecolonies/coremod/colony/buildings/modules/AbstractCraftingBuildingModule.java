package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.*;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.settings.CrafterRecipeSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.colony.crafting.CustomRecipe;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingRequestResolver;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.research.util.ResearchConstants.RECIPES;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_RECIPES;
import static com.minecolonies.api.util.constant.TranslationConstants.RECIPE_IMPROVED;

/**
 * Basic implementation of a crafting module.
 *
 * Typically you should not directly extend this module; instead you should extend one of the
 * "policy classes" (inner classes) to specify the type of crafting supported.  The policy
 * classes don't provide any "real" implementation, they just configure this one.
 */
public abstract class AbstractCraftingBuildingModule extends AbstractBuildingModule implements ICraftingBuildingModule, IPersistentModule, ICreatesResolversModule, IHasRequiredItemsModule, ITickingModule
{
    /**
     * The recipemode of the crafter (either priority based, or warehouse stock baseD).
     */
    public static final ISettingKey<CrafterRecipeSetting> RECIPE_MODE = new SettingKey<>(CrafterRecipeSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "recipemode"));

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
     * The job entry that works at this module.
     */
    protected final JobEntry jobEntry;

    /**
     * Specific crafting building.
     */
    protected AbstractBuilding building;

    /**
     * Create a new module.
     * @param jobEntry the entry of the job.
     */
    public AbstractCraftingBuildingModule(final JobEntry jobEntry)
    {
        this.jobEntry = jobEntry;
    }

    @Override
    public List<IToken<?>> getRecipes()
    {
        return recipes;
    }

    @Override
    public IBuildingModule setBuilding(final IBuilding building)
    {
        this.building = (AbstractBuilding) building;
        return super.setBuilding(building);
    }

    @Override
    public boolean canRecipeBeAdded(final IToken<?> token)
    {
        return hasSpaceForMoreRecipes() && isRecipeCompatibleWithCraftingModule(token);
    }

    /**
     * Check if the worker has more space for recipes.
     *
     * @return true if so.
     */
    private boolean hasSpaceForMoreRecipes()
    {
        return getMaxRecipes() > recipes.size();
    }

    /**
     * Gets the maximum number of recipes a building may have at the current time.
     */
    protected int getMaxRecipes()
    {
        final double increase;
        if(canLearnLargeRecipes() || canLearnFurnaceRecipes())
        {
            increase = (1 + building.getColony().getResearchManager().getResearchEffects().getEffectStrength(RECIPES)) * EXTRA_RECIPE_MULTIPLIER;
        }
        else
        {
            increase = 1 + building.getColony().getResearchManager().getResearchEffects().getEffectStrength(RECIPES);
        }
        return (int) (Math.pow(2, building.getBuildingLevel()) * increase);
    }

    /**
     * @param token the recipe token
     * @return whether the recipe can be added according to the crafting module (or false if there's no module)
     */
    protected boolean isRecipeCompatibleWithCraftingModule(final IToken<?> token)
    {
        final IGenericRecipe recipe = GenericRecipe.of(token);
        if (recipe == null) return false;
        return isRecipeCompatible(recipe);
    }

    /**
     * Check if the recipe is a pre-taught recipe through datapack.
     * @param storage the recipe to check.
     * @param crafterRecipes the list of custom recipes.
     * @return true if so.
     */
    private boolean isPreTaughtRecipe(
      final IRecipeStorage storage,
      final Map<ResourceLocation, CustomRecipe> crafterRecipes)
    {
        final ItemStack one = storage.getPrimaryOutput();
        for (final CustomRecipe rec : crafterRecipes.values())
        {
            final ItemStack two = rec.getRecipeStorage().getPrimaryOutput();
            if (ItemStackUtils.compareItemStacksIgnoreStackSize(one, two) && one.getCount() == two.getCount())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void serializeNBT(@NotNull final CompoundNBT compound)
    {
        final CompoundNBT moduleCompound = new CompoundNBT();
        @NotNull final ListNBT recipesTagList = recipes.stream()
                                                  .map(iToken -> StandardFactoryController.getInstance().serialize(iToken))
                                                  .collect(NBTUtils.toListNBT());
        moduleCompound.put(TAG_RECIPES, recipesTagList);
        compound.put(getId(), moduleCompound);
    }

    @Override
    public void deserializeNBT(CompoundNBT compound)
    {
        final ListNBT recipesTags;
        if (compound.contains(TAG_RECIPES))
        {
            recipesTags = compound.getList(TAG_RECIPES, Constants.NBT.TAG_COMPOUND);
        }
        else
        {
            final CompoundNBT compoundNBT = compound.getCompound(getId());
            recipesTags = compoundNBT.getList(TAG_RECIPES, Constants.NBT.TAG_COMPOUND);
        }

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
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        if (jobEntry != null)
        {
            buf.writeBoolean(true);
            buf.writeRegistryId(jobEntry);
        }
        else
        {
            buf.writeBoolean(false);
        }
        buf.writeBoolean(this.canLearnCraftingRecipes());
        buf.writeBoolean(this.canLearnFurnaceRecipes());
        buf.writeBoolean(this.canLearnLargeRecipes());

        final List<IRecipeStorage> storages = new ArrayList<>();
        final Map<ResourceLocation, CustomRecipe> crafterRecipes = CustomRecipeManager.getInstance().getAllRecipes().getOrDefault(getCustomRecipeKey(), Collections.emptyMap());
        for (final IToken<?> token : new ArrayList<>(recipes))
        {
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);

            if (storage == null || (storage.getRecipeSource() != null && !crafterRecipes.containsKey(storage.getRecipeSource())) || (!isRecipeCompatibleWithCraftingModule(token) && !isPreTaughtRecipe(storage, crafterRecipes)))
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
            buf.writeNbt(StandardFactoryController.getInstance().serialize(storage));
        }
        buf.writeInt(getMaxRecipes());
        buf.writeUtf(getId());
        buf.writeBoolean(isVisible());
    }

    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<ItemStorage, Tuple<Integer, Boolean>> requiredItems = new HashMap<>();
        for (final Tuple<IRecipeStorage, Integer> recipeStorage : getPendingRequestQueue())
        {
            for (final ItemStorage itemStorage : recipeStorage.getA().getCleanedInput())
            {
                int amount = itemStorage.getAmount() * recipeStorage.getB();
                if (requiredItems.containsKey(itemStorage))
                {
                    amount += requiredItems.get(itemStorage).getA();
                }
                requiredItems.put(itemStorage, new Tuple<>(amount, false));
            }

            final ItemStorage output = new ItemStorage(recipeStorage.getA().getPrimaryOutput());
            int amount = output.getAmount() * recipeStorage.getB();
            if (requiredItems.containsKey(output))
            {
                amount += requiredItems.get(output).getA();
            }
            requiredItems.put(output, new Tuple<>(amount, false));
        }

        return new HashMap<>(requiredItems.entrySet()
                               .stream()
                               .collect(Collectors.toMap(key -> (stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, key.getKey().getItemStack(), false, true)), Map.Entry::getValue)));
    }

    @Override
    public Map<ItemStorage, Integer> reservedStacks()
    {
        final Map<ItemStorage, Integer> recipeOutputs = new HashMap<>();
        for (final Tuple<IRecipeStorage, Integer> recipeStorage : getPendingRequestQueue())
        {
            for (final ItemStorage itemStorage : recipeStorage.getA().getCleanedInput())
            {
                int amount = itemStorage.getAmount() * recipeStorage.getB();
                if (recipeOutputs.containsKey(itemStorage))
                {
                    amount += recipeOutputs.get(itemStorage);
                }
                recipeOutputs.put(itemStorage, amount);
            }
        }
        return recipeOutputs;
    }

    /**
     * Get a list of all recipeStorages of the pending requests in the crafters queues.
     * @return the list.
     */
    private List<Tuple<IRecipeStorage, Integer>> getPendingRequestQueue()
    {
        final List<Tuple<IRecipeStorage, Integer>> recipes = new ArrayList<>();
        for (final ICitizenData citizen : building.getAllAssignedCitizen())
        {
            if (citizen.getJob() instanceof AbstractJobCrafter)
            {
                final List<IToken<?>> assignedTasks = new ArrayList<>(citizen.getJob(AbstractJobCrafter.class).getAssignedTasks());
                assignedTasks.addAll(citizen.getJob(AbstractJobCrafter.class).getTaskQueue());

                for (final IToken<?> taskToken : assignedTasks)
                {
                    final IRequest<? extends PublicCrafting> request = (IRequest<? extends PublicCrafting>) building.getColony().getRequestManager().getRequestForToken(taskToken);
                    final IRecipeStorage recipeStorage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(request.getRequest().getRecipeID());
                    if (holdsRecipe(request.getRequest().getRecipeID()) && recipeStorage != null)
                    {
                        recipes.add(new Tuple<>(recipeStorage, request.getRequest().getCount()));
                    }
                }
            }
        }
        return recipes;
    }

    @Override
    public boolean isVisible()
    {
        return true;
    }

    @Override
    public boolean addRecipe(final IToken<?> token)
    {
        if (canRecipeBeAdded(token))
        {
            addRecipeToList(token, false);
            markDirty();

            if (building.getAllAssignedCitizen().isEmpty())
            {
                return true;
            }

            final IRecipeStorage recipeStorage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if (recipeStorage != null)
            {
                building.getColony().getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable && ((IDeliverable) request.getRequest()).matches(recipeStorage.getPrimaryOutput()));
            }
            return true;
        }
        return false;
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        checkForWorkerSpecificRecipes();
    }

    @Override
    public void checkForWorkerSpecificRecipes()
    {
        final IRecipeManager recipeManager = IColonyManager.getInstance().getRecipeManager();
        for(final CustomRecipe newRecipe : CustomRecipeManager.getInstance().getRecipes(getCustomRecipeKey()))
        {
            final IRecipeStorage recipeStorage = newRecipe.getRecipeStorage();
            final IToken<?> recipeToken = recipeManager.checkOrAddRecipe(recipeStorage);

            if(newRecipe.isValidForBuilding(building))
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
                    final IRecipeStorage storage = recipeManager.getRecipes().get(token);

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
                    building.getColony().getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable && ((IDeliverable) request.getRequest()).matches(recipeStorage.getPrimaryOutput()));
                    markDirty();
                }
                else if((forceReplace || newRecipe.getMustExist()) && !(duplicateFound.equals(recipeToken)))
                {
                    //We found the base recipe for a multi-recipe, replace it with the multi-recipe
                    replaceRecipe(duplicateFound, recipeToken);
                    building.getColony().getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable && ((IDeliverable) request.getRequest()).matches(recipeStorage.getPrimaryOutput()));

                    //Clean up old 'classic' recipes that the new multi-recipe replaces
                    final List<ItemStack> alternates = recipeStorage.getAlternateOutputs();
                    for(IToken<?> token : recipes)
                    {
                        final IRecipeStorage storage = recipeManager.getRecipes().get(token);
                        if(storage.getRecipeType() instanceof ClassicRecipe && ItemStackUtils.compareItemStackListIgnoreStackSize(alternates, storage.getPrimaryOutput(), false, true))
                        {
                            removeRecipe(token);
                        }
                    }
                    building.getColony().getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable && recipeStorage.getAlternateOutputs().stream().anyMatch(i -> ((IDeliverable) request.getRequest()).matches(i)));
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

    @Override
    public void clearRecipes()
    {
        recipes.clear();
    }

    @Override
    public void improveRecipe(IRecipeStorage recipe, int count, ICitizenData citizen)
    {
        final List<ItemStorage> inputs = recipe.getCleanedInput().stream().sorted(Comparator.comparingInt(ItemStorage::getAmount).reversed()).collect(Collectors.toList());


        final double actualChance = Math.min(5.0, (BASE_CHANCE * count) + (BASE_CHANCE * citizen.getCitizenSkillHandler().getLevel(building.getModuleMatching(CraftingWorkerBuildingModule.class, m -> m.getJobEntry() == jobEntry).getRecipeImprovementSkill())));
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
                  new TranslationTextComponent(citizen.getJob().getJobRegistryEntry().getTranslationKey().toLowerCase()),
                  recipe.getPrimaryOutput().getHoverName(),
                  reducedItem.getItemStack().getHoverName(),
                  citizen.getName());

                for(PlayerEntity player : building.getColony().getMessagePlayerEntities())
                {
                    player.sendMessage(message, player.getUUID());
                }
            }
        }
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
            if (storage != null && (stackPredicate.test(storage.getPrimaryOutput()) || storage.getAlternateOutputs().stream().anyMatch(stackPredicate::test)))
            {
                if(foundRecipe == null)
                {
                    foundRecipe = storage;
                }
                candidates.put(storage, 0);
            }
        }

        //If we have more than one possible recipe, let's choose the one with the most stock in the warehouses
        if(candidates.size() > 1 && building.hasModule(ISettingsModule.class) && building.getSetting(RECIPE_MODE).getValue().equals(CrafterRecipeSetting.MAX_STOCK))
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
            IToken<?> token = IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(foundRecipe.getClassicForMultiOutput(stackPredicate));
            foundRecipe = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
        }

        return foundRecipe;
    }

    @Override
    public boolean holdsRecipe(final IToken<?> token)
    {
        if (recipes.contains(token))
        {
            return true;
        }

        final IRecipeStorage storageIn = IColonyManager.getInstance().getRecipeManager().getRecipe(token);
        if (storageIn == null)
        {
            return false;
        }

        for (final IToken<?> localToken : recipes)
        {
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipe(localToken);
            if (storage != null && storage.getRecipeType() instanceof MultiOutputRecipe)
            {
                if (storageIn.equals(storage.getClassicForMultiOutput(storageIn.getPrimaryOutput())))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get the count of items in all the warehouses
     */
    protected int getWarehouseCount(ItemStorage item)
    {
        int count = 0;
        final List<IWareHouse> wareHouses = building.getColony().getBuildingManager().getWareHouses();

        for(IWareHouse wareHouse: wareHouses)
        {
            count += InventoryUtils.getCountFromBuilding(wareHouse, item);
        }
        return count;
    }

    @Override
    public IRecipeStorage getFirstFulfillableRecipe(final Predicate<ItemStack> stackPredicate, final int count, final boolean considerReservation)
    {
        for (final IToken<?> token : recipes)
        {
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if (storage != null && (stackPredicate.test(storage.getPrimaryOutput()) || storage.getAlternateOutputs().stream().anyMatch(i -> stackPredicate.test(i))))
            {
                final Set<IItemHandler> handlers = new HashSet<>();
                for (final ICitizenData workerEntity : building.getAllAssignedCitizen())
                {
                    handlers.add(workerEntity.getInventory());
                }
                IRecipeStorage toTest = storage.getRecipeType() instanceof MultiOutputRecipe ? storage.getClassicForMultiOutput(stackPredicate) : storage;
                if (toTest.canFullFillRecipe(count, considerReservation ? reservedStacks() : Collections.emptyMap(), new ArrayList<>(handlers), building))
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
        final List<IItemHandler> handlers = building.getHandlers();
        final ICitizenData data = building.getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == jobEntry).getFirstCitizen();

        if (data == null || !data.getEntity().isPresent())
        {
            // we shouldn't hit this case, but just in case...
            return storage.fullfillRecipe(building.getColony().getWorld(), handlers);
        }
        final AbstractEntityCitizen worker = data.getEntity().get();
        final int primarySkill =worker.getCitizenData().getCitizenSkillHandler().getLevel(building.getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == jobEntry).getPrimarySkill());
        final int luck = (int)(((primarySkill + 1) * 2) - Math.pow((primarySkill + 1 ) / 10.0, 2));

        LootContext.Builder builder =  (new LootContext.Builder((ServerWorld) building.getColony().getWorld())
                                          .withParameter(LootParameters.ORIGIN, worker.position())
                                          .withParameter(LootParameters.THIS_ENTITY, worker)
                                          .withParameter(LootParameters.TOOL, worker.getMainHandItem())
                                          .withRandom(worker.getRandom())
                                          .withLuck((float) luck));

        return storage.fullfillRecipe(builder.create(RecipeStorage.recipeLootParameters), handlers);
    }

    @Nullable
    @Override
    public IJob<?> getCraftingJob()
    {
        if (jobEntry == null)
        {
            return null;
        }
        return jobEntry.produceJob(null);
    }

    @Override
    public void updateWorkerAvailableForRecipes()
    {
        for (final IToken<?> token : recipes)
        {
            final IRecipeStorage recipeStorage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if (recipeStorage != null)
            {
                building.getColony().getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable && ((IDeliverable) request.getRequest()).matches(recipeStorage.getPrimaryOutput()));
            }
        }
    }

    @Override
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

    @Override
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
    public void switchOrder(final int i, final int j)
    {
        if (i < recipes.size() && j < recipes.size() && i >= 0 && j >= 0)
        {
            final IToken<?> storage = recipes.get(i);
            recipes.set(i, recipes.get(j));
            recipes.set(j, storage);
            markDirty();
        }
    }

    @NotNull
    @Override
    public List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly()
    {
        return Collections.emptyList();
    }

    @Override
    public List<IRequestResolver<?>> createResolvers()
    {
        final List<IRequestResolver<?>> resolvers = new ArrayList<>();
        resolvers.add(new PublicWorkerCraftingRequestResolver(building.getRequester().getLocation(),
          building.getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN), jobEntry));
        resolvers.add(new PublicWorkerCraftingProductionResolver(building.getRequester().getLocation(),
          building.getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN), jobEntry));

        return resolvers;
    }

    @NotNull
    public abstract String getId();

    @NotNull
    @Override
    public String getCustomRecipeKey()
    {
        if (jobEntry == null)
        {
            return "";
        }
        return jobEntry.getRegistryName().getPath() + "_" + getId();
    }

    /** This module is for standard crafters (3x3 by default) */
    public abstract static class Crafting extends AbstractCraftingBuildingModule
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public Crafting(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @Override
        public boolean canLearnCraftingRecipes() { return true; }

        @Override
        public boolean canLearnFurnaceRecipes() { return false; }

        @Override
        public boolean canLearnLargeRecipes() { return true; }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            return canLearnCraftingRecipes() &&
                    recipe.getIntermediate() == Blocks.AIR;
        }

        /**
         * Get a string identifier to this.
         * @return the id.
         */
        @NotNull
        public String getId()
        {
            return "crafting";
        }
    }

    /** this module is for furnace-only users */
    public abstract static class Smelting extends AbstractCraftingBuildingModule
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public Smelting(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @Override
        public boolean canLearnCraftingRecipes() { return false; }

        @Override
        public boolean canLearnFurnaceRecipes() { return true; }

        @Override
        public boolean canLearnLargeRecipes() { return false; }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            return canLearnFurnaceRecipes() &&
                    recipe.getIntermediate() == Blocks.FURNACE;
        }

        /**
         * Get a string identifier to this.
         * @return the id.
         */
        @NotNull
        public String getId()
        {
            return "smelting";
        }
    }

    /** this module is for those who can't be taught recipes but can still use custom recipes */
    public abstract static class Custom extends AbstractCraftingBuildingModule
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public Custom(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @Override
        public boolean canLearnCraftingRecipes() { return false; }

        @Override
        public boolean canLearnFurnaceRecipes() { return false; }

        @Override
        public boolean canLearnLargeRecipes() { return false; }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe) { return false; }

        /**
         * Get a string identifier to this.
         * @return the id.
         */
        @NotNull
        public String getId()
        {
            return "custom";
        }
    }
}
