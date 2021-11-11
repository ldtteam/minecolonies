package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingPublicCrafter;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.colony.buildings.modules.MinimumStockModule;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.jobs.JobCook;
import com.minecolonies.coremod.colony.jobs.JobCookAssistant;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.ItemStackUtils.ISFOOD;
import static com.minecolonies.api.util.constant.BuildingConstants.FUEL_LIST;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_SITTING;
import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;

/**
 * Class of the cook building.
 */
@SuppressWarnings(OVERRIDE_EQUALS)
public class BuildingCook extends AbstractBuildingWorker implements IBuildingPublicCrafter
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
     * Current Assistant If there is an assistant working
     */
    private ICitizenData assistant = null;

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
        keepX.put(stack -> isAllowedFood(stack), new Tuple<>(STACKSIZE, true));
        keepX.put(stack -> !ItemStackUtils.isEmpty(stack.getContainerItem()) && !stack.getContainerItem().getItem().equals(Items.BUCKET), new Tuple<>(STACKSIZE, false));
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

        final IBlueprintDataProvider te = getTileEntity();
        if (te != null)
        {
            initTags = true;
            sitPositions = new ArrayList<>();
            for (final Map.Entry<BlockPos, List<String>> entry : te.getWorldTagPosMap().entrySet())
            {
                if (entry.getValue().contains(TAG_SITTING))
                {
                    sitPositions.add(entry.getKey());
                }
            }
        }
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
        initTags = false;
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
        ICitizenData citizen = getAssistant();
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

    /**
     * Get the citizen that is currently assigned as the Assistant Cook
     */
    public ICitizenData getAssistant()
    {

        if(getBuildingLevel() <3)
        {
            return null; 
        }

        if(assistant == null)
        {
            for (final ICitizenData citizen : getAssignedCitizen())
            {
                if (citizen.getJob() instanceof JobCookAssistant)   
                {
                    assistant = citizen;
                }
            }
        }
        return assistant;
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

    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        if (citizen != null)
        {
            for (final ICitizenData leadCitizen : getAssignedCitizen())
            {
                if (leadCitizen.getJob() instanceof JobCook)
                {
                    assistant = citizen;
                    return new JobCookAssistant(citizen);
                }
            }
        }
        return new JobCook(citizen);
    }

    @Override
    public void removeCitizen(final ICitizenData citizen)
    {
        if(citizen.getJob() instanceof JobCookAssistant)
        {
            assistant = null;
        }
        super.removeCitizen(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return COOK_DESC;
    }

    @Override
    public int getMaxInhabitants()
    {
        if(getBuildingLevel() < 3)
        {
            return 1;
        }
        return 2;
    }

    @Override
    public boolean canBeGathered()
    {
        return super.canBeGathered() &&
                 this.getAssignedCitizen().stream()
                   .map(c -> c.getJob(AbstractJobCrafter.class))
                   .filter(Objects::nonNull)
                   .allMatch(AbstractJobCrafter::hasTask);
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Adaptability;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Knowledge;
    }

    @Override
    public boolean canWorkDuringTheRain()
    {
        return true;
    }

    @Override
    public int buildingRequiresCertainAmountOfItem(final ItemStack stack, final List<ItemStorage> localAlreadyKept, final boolean inventory)
    {
        if (stack.isEmpty())
        {
            return 0;
        }

        if (inventory && getFirstModuleOccurance(MinimumStockModule.class).isStocked(stack))
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

        return super.buildingRequiresCertainAmountOfItem(stack, localAlreadyKept, inventory);
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        // TODO: Request on tick if no food, so that food gets distributed from the warehouse even when there is currently no cook
        super.onColonyTick(colony);
        if(isCookingTimeout > 0)
        {
            isCookingTimeout = isCookingTimeout - 1;
        }
    }

    @Override
    public Skill getCraftSpeedSkill()
    {
        return getSecondarySkill();
    }

    /**
     * BuildingCook View.
     */
    public static class View extends AbstractBuildingWorkerView
    {
        /**
         * Instantiate the cook view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, COOK_DESC);
        }
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            if (this.building != null)
            {
                final ICitizenData assistant = ((BuildingCook) this.building).getAssistant();
                if (assistant != null)
                {
                    return assistant.getJob();
                }
            }
            return new JobCookAssistant(null);
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;

            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, COOK_DESC);
            if (isRecipeAllowed.isPresent()) return isRecipeAllowed.get();

            final ItemStack output = recipe.getPrimaryOutput();
            return ItemStackUtils.CAN_EAT.test(output)
                    || ItemStackUtils.CAN_EAT.test(FurnaceRecipes.getInstance()
                    .getSmeltingResult(output));
        }

        @Override
        public boolean canLearnCraftingRecipes()
        {
            if (building == null) return true;  // because it can learn at *some* level
            return building.getBuildingLevel() >= 3;
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
        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            return getMainBuildingJob().orElseGet(() -> new JobCook(null));
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, COOK_DESC).orElse(ItemStackUtils.CAN_EAT.test(recipe.getPrimaryOutput()));
        }

        @Override
        @Nullable
        public IRecipeStorage getFirstRecipe(final Predicate<ItemStack> stackPredicate)
        {
            if (building.getBuildingLevel() < 3 || ((BuildingCook) building).getAssistant() == null)
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
            if(storage != null && storage.getRecipeSource() != null && ISFOOD.test(storage.getPrimaryOutput().getStack()) && isRecipeCompatible(GenericRecipe.of(storage)))
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
                    for (final ICitizenData workerEntity : building.getAssignedCitizen())
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
