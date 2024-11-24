package com.minecolonies.core.colony.buildings.modules;

import com.google.common.collect.ImmutableSet;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
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
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.*;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.settings.CrafterRecipeSetting;
import com.minecolonies.core.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.core.colony.crafting.CustomRecipe;
import com.minecolonies.core.colony.crafting.CustomRecipeManager;
import com.minecolonies.core.colony.jobs.AbstractJobCrafter;
import com.minecolonies.core.colony.requestsystem.resolvers.PublicWorkerCraftingProductionResolver;
import com.minecolonies.core.colony.requestsystem.resolvers.PublicWorkerCraftingRequestResolver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.minecolonies.api.research.util.ResearchConstants.RECIPES;
import static com.minecolonies.api.util.constant.BuildingConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_DISABLED_RECIPES;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_RECIPES;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_REDUCEABLE;
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
     * The list of recipes the worker knows, correspond to a subset of the recipes in the colony.
     */
    protected final List<IToken<?>> recipes = new ArrayList<>();

    /**
     * The list of disabled recipes.
     */
    protected final List<IToken<?>> disabledRecipes = new ArrayList<>();

    /**
     * The job entry that works at this module.
     */
    protected final JobEntry jobEntry;

    /**
     * Specific crafting building.
     */
    protected AbstractBuilding building;

    /**
     * Dirty flag for recipes
     */
    private boolean recipesDirty = true;

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
        double increase = 1 + building.getColony().getResearchManager().getResearchEffects().getEffectStrength(RECIPES);
        if (canLearnManyRecipes())
        {
            increase *= EXTRA_RECIPE_MULTIPLIER;
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
    protected boolean isPreTaughtRecipe(
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
    public void serializeNBT(@NotNull final CompoundTag compound)
    {
        @NotNull final ListTag recipesTagList = recipes.stream()
                                                  .map(iToken -> StandardFactoryController.getInstance().serialize(iToken))
                                                  .collect(NBTUtils.toListNBT());
        compound.put(TAG_RECIPES, recipesTagList);

        @NotNull final ListTag disabledRecipesTag = new ListTag();
        for (@NotNull final IToken<?> recipe : disabledRecipes)
        {
            if (disabledRecipes.contains(recipe))
            {
                disabledRecipesTag.add(StandardFactoryController.getInstance().serialize(recipe));
            }
        }
        compound.put(TAG_DISABLED_RECIPES, disabledRecipesTag);
    }

    @Override
    public void deserializeNBT(CompoundTag compound)
    {
        if (compound.contains(getId()))
        {
            compound = compound.getCompound(getId());
        }

        ListTag recipesTags = new ListTag();
        if (compound.contains(TAG_RECIPES))
        {
            recipesTags = compound.getList(TAG_RECIPES, Tag.TAG_COMPOUND);
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

        if (compound.contains(TAG_DISABLED_RECIPES))
        {
            final ListTag disabledRecipeTag = compound.getList(TAG_DISABLED_RECIPES, Tag.TAG_COMPOUND);
            for (int i = 0; i < disabledRecipeTag.size(); i++)
            {
                final IToken<?> token = StandardFactoryController.getInstance().deserialize(disabledRecipeTag.getCompound(i));
                if (!disabledRecipes.contains(token))
                {
                    disabledRecipes.add(token);
                }
            }
        }
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf, final boolean fullSync)
    {
        if (jobEntry != null)
        {
            buf.writeBoolean(true);
            buf.writeRegistryId(IMinecoloniesAPI.getInstance().getJobRegistry(), jobEntry);
        }
        else
        {
            buf.writeBoolean(false);
        }

        final Set<CraftingType> craftingTypes = this.getSupportedCraftingTypes();
        buf.writeVarInt(craftingTypes.size());
        for (final CraftingType type : craftingTypes)
        {
            buf.writeRegistryIdUnsafe(MinecoloniesAPIProxy.getInstance().getCraftingTypeRegistry(), type);
        }

        buf.writeBoolean(recipesDirty || fullSync);
        if (recipesDirty || fullSync)
        {
            final List<IRecipeStorage> storages = new ArrayList<>();
            final List<IRecipeStorage> disabledStorages = new ArrayList<>();
            final Map<ResourceLocation, CustomRecipe> crafterRecipes = CustomRecipeManager.getInstance().getAllRecipes().getOrDefault(getCustomRecipeKey(), Collections.emptyMap());
            for (final IToken<?> token : new ArrayList<>(recipes))
            {
                final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);

                if (storage == null || (storage.getRecipeSource() != null && !crafterRecipes.containsKey(storage.getRecipeSource())) || (
                  !isRecipeCompatibleWithCraftingModule(token) && !isPreTaughtRecipe(storage, crafterRecipes)))
                {
                    removeRecipe(token);
                }
                else
                {
                    storages.add(storage);
                    if (disabledRecipes.contains(token))
                    {
                        disabledStorages.add(storage);
                    }
                }
            }

            buf.writeInt(storages.size());
            for (final IRecipeStorage storage : storages)
            {
                buf.writeNbt(StandardFactoryController.getInstance().serialize(storage));
            }

            buf.writeInt(disabledStorages.size());
            for (final IRecipeStorage storage : disabledStorages)
            {
                buf.writeNbt(StandardFactoryController.getInstance().serialize(storage));
            }
        }

        recipesDirty = false;

        buf.writeInt(getMaxRecipes());
        buf.writeUtf(getId());
        buf.writeBoolean(isVisible());
    }

    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<ItemStorage, Tuple<Integer, Boolean>> requiredItems = new HashMap<>();
        for (final Tuple<IRecipeStorage, Integer> recipeStorage : getPendingRequestQueueExcluding(null))
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
    public Map<ItemStorage, Integer> reservedStacksExcluding(@Nullable final IRequest<? extends IDeliverable> request)
    {
        final Map<ItemStorage, Integer> recipeOutputs = new HashMap<>();
        for (final Tuple<IRecipeStorage, Integer> recipeStorage : getPendingRequestQueueExcluding(request))
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
     * @param excluded ignore this request (and its parents).
     * @return the list.
     */
    private List<Tuple<IRecipeStorage, Integer>> getPendingRequestQueueExcluding(@Nullable final IRequest<? extends IDeliverable> excluded)
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
                    if (request == null || (excluded != null && anyChildRequestIs(building.getColony().getRequestManager(), request, excluded)))
                    {
                        continue;
                    }

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

    private static boolean anyChildRequestIs(@NotNull final IRequestManager requestManager,
                                             @NotNull final IRequest<?> parent,
                                             @NotNull final IRequest<?> target)
    {
        return parent.getChildren().stream().anyMatch(childToken ->
        {
            final IRequest<?> childRequest = requestManager.getRequestForToken(childToken);
            if (childRequest == target)
            {
                return true;
            }
            else if (childRequest != null)
            {
                return anyChildRequestIs(requestManager, childRequest, target);
            }
            return false;
        });
    }

    @Override
    public boolean isVisible()
    {
        return !getSupportedCraftingTypes().isEmpty() || !recipes.isEmpty();
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
                if (recipeStorage.getAlternateOutputs().isEmpty())
                {
                    building.getColony().getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable iDeliverable && iDeliverable.matches(recipeStorage.getPrimaryOutput()));
                    return true;
                }

                final List<ItemStack> allOutputs = Stream.concat(Stream.of(recipeStorage.getPrimaryOutput()),
                    recipeStorage.getAlternateOutputs().stream()).filter(stack -> !stack.isEmpty()).toList();

                building.getColony().getRequestManager().onColonyUpdate(request ->
                                                                          request.getRequest() instanceof IDeliverable delivery && allOutputs.stream().anyMatch(i -> delivery.matches(i)));
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
                    building.getColony().getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable iDeliverable && iDeliverable.matches(recipeStorage.getPrimaryOutput()));
                    markDirty();
                }
                else if((forceReplace || newRecipe.getMustExist()) && !(duplicateFound.equals(recipeToken)))
                {
                    //We found the base recipe for a multi-recipe, replace it with the multi-recipe
                    replaceRecipe(duplicateFound, recipeToken);
                    building.getColony().getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable iDeliverable && iDeliverable.matches(recipeStorage.getPrimaryOutput()));

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
                    building.getColony().getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable iDeliverable && iDeliverable.matches(recipeStorage.getPrimaryOutput()));
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
        recipesDirty = true;
    }

    @Override
    public void improveRecipe(IRecipeStorage recipe, int count, ICitizenData citizen)
    {
        final List<ItemStorage> inputs = recipe.getCleanedInput().stream().sorted(Comparator.comparingInt(ItemStorage::getAmount).reversed()).collect(Collectors.toList());


        final double actualChance = Math.min(5.0, (BASE_CHANCE * count) + (BASE_CHANCE * citizen.getCitizenSkillHandler().getLevel(building.getModuleMatching(CraftingWorkerBuildingModule.class, m -> m.getJobEntry() == jobEntry).getRecipeImprovementSkill())));
        final double roll = citizen.getRandom().nextDouble() * 100;

        ItemStorage reducedItem = null;

        if(roll <= actualChance && ModTags.crafterProductExclusions.containsKey(CRAFTING_REDUCEABLE) && !recipe.getPrimaryOutput().is(ModTags.crafterProductExclusions.get(CRAFTING_REDUCEABLE)))
        {
            final ArrayList<ItemStorage> newRecipe = new ArrayList<>();
            boolean didReduction = false;
            for(ItemStorage input : inputs)
            {
                // Check against excluded products
                if (input.getAmount() > 1 && ModTags.crafterIngredient.containsKey(CRAFTING_REDUCEABLE) && input.getItemStack().is(ModTags.crafterIngredient.get(CRAFTING_REDUCEABLE)))
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
                  recipe.getIntermediate(),
                  null,     // improved recipes have no source (expected by checkForWorkerSpecificRecipes)
                  recipe.getRecipeType().getId(),
                  recipe.getAlternateOutputs(),
                  recipe.getSecondaryOutputs(),
                  recipe.getLootTable(),
                  recipe.getRequiredTool());

                final IToken<?> token = IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(storage);
                if (isRecipeCompatibleWithCraftingModule(token))
                {
                    replaceRecipe(recipe.getToken(), token);

                    // Expected parameters for RECIPE_IMPROVED are Job, Result, Ingredient, Citizen
                    Component jobComponent = MessageUtils.format(citizen.getJob().getJobRegistryEntry().getTranslationKey()).create();
                    MessageUtils.format(RECIPE_IMPROVED + citizen.getRandom().nextInt(3),
                      jobComponent,
                      recipe.getPrimaryOutput().getHoverName(),
                      reducedItem.getItemStack().getHoverName(),
                      citizen.getName()
                    ).sendTo(building.getColony()).forAllPlayers();
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
            if (disabledRecipes.contains(token))
            {
                continue;
            }
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if (storage != null && (stackPredicate.test(storage.getPrimaryOutput()) || InventoryUtils.getFirstMatch(storage.getAlternateOutputs(), stackPredicate) != null))
            {
                if (storage.getRecipeType() instanceof MultiOutputRecipe && storage.getClassicForMultiOutput(stackPredicate) == null)
                {
                    continue;
                }

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
        if (disabledRecipes.contains(token))
        {
            return false;
        }

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
            if (disabledRecipes.contains(token))
            {
                continue;
            }

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

        LootParams.Builder builder =  (new LootParams.Builder((ServerLevel) building.getColony().getWorld())
                                          .withParameter(LootContextParams.ORIGIN, worker.position())
                                          .withParameter(LootContextParams.THIS_ENTITY, worker)
                                          .withParameter(LootContextParams.TOOL, getCraftingTool(worker))
                                          .withLuck(getCraftingLuck(worker)));

        return storage.fullfillRecipe(builder.create(RecipeStorage.recipeLootParameters), handlers);
    }

    @Override 
    public ItemStack getCraftingTool(final AbstractEntityCitizen worker)
    {
        return worker != null ? worker.getMainHandItem() : ItemStack.EMPTY;
    }

    @Override
    public float getCraftingLuck(final AbstractEntityCitizen worker)
    {
        if (worker != null)
        {
            WorkerBuildingModule workerModule = building.getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == jobEntry);
            final int primarySkill =worker.getCitizenData().getCitizenSkillHandler().getLevel(workerModule.getPrimarySkill());
            return (int)(((primarySkill + 1) * 2) - Math.pow((primarySkill + 1 ) / 10.0, 2));
        }
        return 0;
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
            if (disabledRecipes.contains(token))
            {
                continue;
            }

            final IRecipeStorage recipeStorage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if (recipeStorage != null)
            {
                building.getColony().getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable iDeliverable && iDeliverable.matches(recipeStorage.getPrimaryOutput()));
            }
        }
    }

    @Override
    public void replaceRecipe(final IToken<?> oldRecipe, final IToken<?> newRecipe)
    {
        if (recipes.contains(oldRecipe))
        {
            recipesDirty = true;
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
            recipesDirty = true;
            disabledRecipes.remove(token);
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
            recipesDirty = true;
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
    public void switchOrder(final int i, final int j, final boolean fullMove)
    {
        recipesDirty = true;
        if (fullMove)
        {
            if (i > j)
            {
                recipes.add(0, recipes.remove(i));
            }
            else
            {
                recipes.add(recipes.remove(i));
            }
        }
        else if (i < recipes.size() && j < recipes.size() && i >= 0 && j >= 0)
        {
            final IToken<?> storage = recipes.get(i);
            recipes.set(i, recipes.get(j));
            recipes.set(j, storage);
            markDirty();
        }
    }

    @Override
    public void toggle(int recipeLocation)
    {
        final IToken<?> key = recipes.get(recipeLocation);
        if (disabledRecipes.contains(key))
        {
            disabledRecipes.remove(key);

            final IRecipeStorage recipeStorage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(key);
            if (recipeStorage != null)
            {
                building.getColony().getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable iDeliverable && iDeliverable.matches(recipeStorage.getPrimaryOutput()));
            }
        }
        else
        {
            disabledRecipes.add(key);
        }
        markDirty();
    }

    @NotNull
    @Override
    public List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly(@NotNull final Level world)
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
    @Deprecated
    public abstract String getId();

    @NotNull
    @Override
    public String getCustomRecipeKey()
    {
        if (jobEntry == null)
        {
            return "";
        }
        return jobEntry.getKey().getPath() + "_" + getId();
    }

    @NotNull
    @Override
    public OptionalPredicate<ItemStack> getIngredientValidator()
    {
        return stack -> Optional.empty();
    }

    @Override
    public boolean canLearnManyRecipes()
    {
        return true;
    }

    @Override
    public boolean isDisabled(final IToken<?> token)
    {
        return disabledRecipes.contains(token);
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
        public Set<CraftingType> getSupportedCraftingTypes()
        {
            return Set.of(ModCraftingTypes.SMALL_CRAFTING.get(), ModCraftingTypes.LARGE_CRAFTING.get());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            return canLearn(ModCraftingTypes.SMALL_CRAFTING.get()) &&
                    recipe.getIntermediate() == Blocks.AIR;
        }

        /**
         * Get a string identifier to this.
         * @return the id.
         */
        @NotNull
        public String getId()
        {
            return MODULE_CRAFTING;
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
        public Set<CraftingType> getSupportedCraftingTypes()
        {
            return Set.of(ModCraftingTypes.SMELTING.get());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            return canLearn(ModCraftingTypes.SMELTING.get()) &&
                     recipe.getIntermediate() == Blocks.FURNACE;
        }

        /**
         * Get a string identifier to this.
         * @return the id.
         */
        @NotNull
        public String getId()
        {
            return MODULE_SMELTING;
        }
    }

    /** this module is for brewing-only users */
    public abstract static class Brewing extends AbstractCraftingBuildingModule
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public Brewing(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @Override
        public Set<CraftingType> getSupportedCraftingTypes()
        {
            return ImmutableSet.of(ModCraftingTypes.BREWING.get());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            return canLearn(ModCraftingTypes.BREWING.get()) &&
                     recipe.getIntermediate() == Blocks.BREWING_STAND;
        }

        /**
         * Get a string identifier to this.
         * @return the id.
         */
        @NotNull
        public String getId()
        {
            return MODULE_BREWING;
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
        public Set<CraftingType> getSupportedCraftingTypes()
        {
            return Set.of();
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe) { return false; }

        /**
         * Get a string identifier to this.
         * @return the id.
         */
        @NotNull
        public String getId()
        {
            return MODULE_CUSTOM;
        }
    }
}
