package com.minecolonies.core.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableSet;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.core.colony.buildings.modules.CraftingWorkerBuildingModule;
import com.minecolonies.core.colony.buildings.modules.ItemListModule;
import com.minecolonies.core.colony.buildings.modules.MinimumStockModule;
import com.minecolonies.core.colony.jobs.AbstractJobCrafter;
import com.minecolonies.core.util.FurnaceRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.ItemStackUtils.ISFOOD;
import static com.minecolonies.api.util.constant.BuildingConstants.FUEL_LIST;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_SITTING;
import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_COOK;

/**
 * Class of the cook building.
 */
@SuppressWarnings(OVERRIDE_EQUALS)
public class BuildingCook extends AbstractBuilding
{
    /**
     * The cook string.
     */
    private static final String COOK_DESC = "cook";

    /**
     * Exclusion list id.
     */
    public static final String FOOD_EXCLUSION_LIST = "food";

    /**
     * Max building level of the cook.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * If the assistant is fulfilling a recipe
     * This blocks the auto-smelting of the cook
     */
    private boolean isCooking = false;

    /**
     * Failsafe for isCooking. Number of Colony Ticks before setting isCooking false. 
     */
    private int isCookingTimeout = 0;

    /**
     * Whether we did init tags
     */
    private boolean initTags = false;

    /**
     * Sitting positions
     */
    private List<BlockPos> sitPositions;

    /**
     * Current sitting index
     */
    private int lastSitting = 0;

    /**
     * Instantiates a new cook building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingCook(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(this::isAllowedFood, new Tuple<>(STACKSIZE, true));
        keepX.put(stack -> !ItemStackUtils.isEmpty(stack.getCraftingRemainingItem()) && !stack.getCraftingRemainingItem().getItem().equals(Items.BUCKET), new Tuple<>(STACKSIZE, false));
    }

    /**
     * Return whether the given stack is allowed food
     * @param stack the stack
     * @return true if so
     */
    public boolean isAllowedFood(ItemStack stack)
    {
        ItemListModule listModule = this.getModuleMatching(ItemListModule.class, m -> m.getId().equals(FOOD_EXCLUSION_LIST));
        return ISFOOD.test(stack) && !listModule.isItemInList(new ItemStorage(stack))
          && !listModule.isItemInList(new ItemStorage(MinecoloniesAPIProxy.getInstance().getFurnaceRecipes().getSmeltingResult(stack)));
    }

    /**
     * Reads the tag positions
     */
    public void initTagPositions()
    {
        if (initTags)
        {
            return;
        }

        sitPositions = getLocationsFromTag(TAG_SITTING);
        initTags = !sitPositions.isEmpty();
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
        initTags = false;
    }

    /**
     * On initial construction or reset request, excludes the tagged food by default.
     *
     * @param listModule The food exclusion module.
     */
    public static void onResetFoodExclusionList(final ItemListModule listModule)
    {
        listModule.clearItems();
        for (final Item item : ForgeRegistries.ITEMS.tags().getTag(ModTags.excludedFood))
        {
            listModule.addItem(new ItemStorage(new ItemStack(item)));
        }
    }

    /**
     * Gets the next sitting position to use for eating, just keeps iterating the aviable positions, so we do not have to keep track of who is where.
     *
     * @return eating position to sit at
     */
    public BlockPos getNextSittingPosition()
    {
        initTagPositions();

        if (sitPositions.isEmpty())
        {
            return null;
        }

        lastSitting++;

        if (lastSitting >= sitPositions.size())
        {
            lastSitting = 0;
        }

        return sitPositions.get(lastSitting);
    }

    /**
     * Get the status of the assistant processing requests
     * @return true if currently crafting
     */
    public boolean getIsCooking()
    {
        final ICitizenData citizen = this.getModuleMatching(CraftingWorkerBuildingModule.class, m -> m.getJobEntry() == ModJobs.cookassistant.get()).getFirstCitizen();
        return citizen != null && isCooking && isCookingTimeout > 0;
    }

    /**
     * Record the state of the assistant processing requests
     * @param cookingState true if currently crafting
     */
    public void setIsCooking(final boolean cookingState)
    {
        isCooking = cookingState;
        if(cookingState)
        {
            //Wait ~32 minutes before timing out. 
            isCookingTimeout = 75;
        }
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return COOK_DESC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public boolean canBeGathered()
    {
        return super.canBeGathered() &&
                 this.getModuleMatching(CraftingWorkerBuildingModule.class, m -> m.getJobEntry() == ModJobs.cookassistant.get()).getAssignedCitizen().stream()
                   .map(c -> c.getJob(AbstractJobCrafter.class))
                   .filter(Objects::nonNull)
                   .allMatch(AbstractJobCrafter::hasTask);
    }

    @Override
    public int buildingRequiresCertainAmountOfItem(final ItemStack stack, final List<ItemStorage> localAlreadyKept, final boolean inventory, final JobEntry jobEntry)
    {
        if (stack.isEmpty())
        {
            return 0;
        }

        if (inventory && getFirstModuleOccurance(MinimumStockModule.class).isStocked(stack))
        {
            return stack.getCount();
        }

        // Make the assistant cook drop everything. We don't want them to keep food.
        // Neither like the cook does here, nor how the average worker does in the super call.
        if (jobEntry == ModJobs.cookassistant.get())
        {
            return stack.getCount();
        }

        if (isAllowedFood(stack) && (localAlreadyKept.stream().filter(storage -> ISFOOD.test(storage.getItemStack())).mapToInt(ItemStorage::getAmount).sum() < STACKSIZE || !inventory))
        {
            final ItemStorage kept = new ItemStorage(stack);
            if (localAlreadyKept.contains(kept))
            {
                kept.setAmount(localAlreadyKept.remove(localAlreadyKept.indexOf(kept)).getAmount());
            }
            localAlreadyKept.add(kept);
            return 0;
        }

        final Predicate<ItemStack> allowedFuel = theStack -> getModuleMatching(ItemListModule.class, m -> m.getId().equals(FUEL_LIST)).isItemInList(new ItemStorage(theStack));
        if (allowedFuel.test(stack) && (localAlreadyKept.stream().filter(storage -> allowedFuel.test(storage.getItemStack())).mapToInt(ItemStorage::getAmount).sum() < STACKSIZE
              || !inventory))
        {
            final ItemStorage kept = new ItemStorage(stack);
            if (localAlreadyKept.contains(kept))
            {
                kept.setAmount(localAlreadyKept.remove(localAlreadyKept.indexOf(kept)).getAmount());
            }
            localAlreadyKept.add(kept);
            return 0;
        }

        return super.buildingRequiresCertainAmountOfItem(stack, localAlreadyKept, inventory, jobEntry);
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        super.onColonyTick(colony);
        if(isCookingTimeout > 0)
        {
            isCookingTimeout = isCookingTimeout - 1;
        }
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public CraftingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @NotNull
        @Override
        public OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_COOK)
                    .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;

            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_COOK);
            if (isRecipeAllowed.isPresent()) return isRecipeAllowed.get();

            final ItemStack output = recipe.getPrimaryOutput();
            return ItemStackUtils.CAN_EAT.test(output)
                    || ItemStackUtils.CAN_EAT.test(FurnaceRecipes.getInstance()
                    .getSmeltingResult(output));
        }

        @Override
        public Set<CraftingType> getSupportedCraftingTypes()
        {
            return (building == null || building.getBuildingLevel() >= 3)
                    ? super.getSupportedCraftingTypes()
                    : ImmutableSet.of();
        }


        @Override
        public boolean isVisible()
        {
            return building.getBuildingLevel() >= 3;
        }

        @Override
        public boolean canRecipeBeAdded(@NotNull final IToken<?> token)
        {
            if(building.getBuildingLevel() < 3)
            {
                return false;
            }

            return super.canRecipeBeAdded(token);
        }
    }

    public static class SmeltingModule extends AbstractCraftingBuildingModule.Smelting
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public SmeltingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @NotNull
        @Override
        public OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_COOK)
                    .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_COOK).orElse(ItemStackUtils.CAN_EAT.test(recipe.getPrimaryOutput()));
        }

        @Override
        @Nullable
        public IRecipeStorage getFirstRecipe(final Predicate<ItemStack> stackPredicate)
        {
            if (building.getBuildingLevel() < 3 || building.getModuleMatching(CraftingWorkerBuildingModule.class, m -> m.getJobEntry() == jobEntry).getAssignedCitizen().isEmpty())
            {
                return null;
            }

            //First, do the normal check against taught recipes, and return those if found
            IRecipeStorage storage = super.getFirstRecipe(stackPredicate);
            if(storage != null)
            {
                return storage;
            }


            //If we didn't have a stored recipe, see if there is a smelting recipe that is also a food output, and use it.
            storage = FurnaceRecipes.getInstance().getFirstSmeltingRecipeByResult(stackPredicate);
            if(storage != null && storage.getRecipeSource() != null && ISFOOD.test(storage.getPrimaryOutput()) && isRecipeCompatible(GenericRecipe.of(storage)))
            {
                return storage;
            }

            return null;
        }

        @Override
        public IRecipeStorage getFirstFulfillableRecipe(final Predicate<ItemStack> stackPredicate, final int count, final boolean considerReservation)
        {
            //Try to fulfill normally
            IRecipeStorage storage = super.getFirstFulfillableRecipe(stackPredicate, count, considerReservation);

            //Couldn't fulfill normally, let's try to fulfill with a temporary smelting recipe.
            if(storage == null)
            {
                storage = FurnaceRecipes.getInstance().getFirstSmeltingRecipeByResult(stackPredicate);
                if (storage != null)
                {
                    final Set<IItemHandler> handlers = new HashSet<>();
                    for (final ICitizenData workerEntity :  building.getModuleMatching(CraftingWorkerBuildingModule.class, m -> m.getJobEntry() == jobEntry).getAssignedCitizen())
                    {
                        handlers.add(workerEntity.getInventory());
                    }

                    if (!storage.canFullFillRecipe(count, Collections.emptyMap(), new ArrayList<>(handlers), building))
                    {
                        return null;
                    }
                }
            }

            return storage;
        }
    }
}
