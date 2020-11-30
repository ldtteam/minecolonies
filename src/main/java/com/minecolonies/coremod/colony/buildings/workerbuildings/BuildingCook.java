package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.client.gui.WindowHutCook;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingSmelterCrafter;
import com.minecolonies.coremod.colony.buildings.views.AbstractFilterableListsView;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.jobs.JobCook;
import com.minecolonies.coremod.colony.jobs.JobCookAssistant;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingRequestResolver;
import com.minecolonies.coremod.util.FurnaceRecipes;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.ItemStackUtils.ISFOOD;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;

/**
 * Class of the cook building.
 */
@SuppressWarnings(OVERRIDE_EQUALS)
public class BuildingCook extends AbstractBuildingSmelterCrafter
{
    /**
     * The cook string.
     */
    private static final String COOK_DESC = "cook";

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
     * Instantiates a new cook building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingCook(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(ItemStackUtils.ISFOOD, new Tuple<>(STACKSIZE, true));
        keepX.put(ItemStackUtils.ISCOOKABLE, new Tuple<>(STACKSIZE, true));
        keepX.put(stack -> isAllowedFuel(stack), new Tuple<>(STACKSIZE, true));
        keepX.put(stack -> !ItemStackUtils.isEmpty(stack.getContainerItem()) && !stack.getContainerItem().getItem().equals(Items.BUCKET), new Tuple<>(STACKSIZE, false));
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
        for (final ICitizenData leadCitizen : getAssignedCitizen())
        {
            if (leadCitizen.getJob() instanceof JobCook)   
            {
                assistant = citizen;
                return new JobCookAssistant(citizen);
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
    public boolean isRecipeAlterationAllowed()
    {
        if(getBuildingLevel() < 3)
        {
            return false;
        }
        return true;
    }

    @Override
    public boolean canCraftComplexRecipes()
    {
        return true;
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

    @Override
    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        final Collection<IRequestResolver<?>> supers =
          super.createResolvers().stream()
            .filter(r -> !(r instanceof PrivateWorkerCraftingProductionResolver || r instanceof PrivateWorkerCraftingRequestResolver))
            .collect(Collectors.toList());
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();

        builder.addAll(supers);
        builder.add(new PublicWorkerCraftingRequestResolver(getRequester().getLocation(),
          getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));
        builder.add(new PublicWorkerCraftingProductionResolver(getRequester().getLocation(),
          getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));

        return builder.build();
    }

    @Override
    @Nullable
    public IRecipeStorage getFirstRecipe(final Predicate<ItemStack> stackPredicate)
    {
        if (getBuildingLevel() < 3 || getAssistant() == null)
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
        Optional<Boolean> validRecipe =  canRecipeBeAddedBasedOnTags(storage);
        if(storage != null && ISFOOD.test(storage.getPrimaryOutput().getStack()) && (!validRecipe.isPresent() || validRecipe.get()))
        {
            return storage;
        }

        return null;
    }

    @Override
    public IRecipeStorage getFirstFullFillableRecipe(final Predicate<ItemStack> stackPredicate, final int count, final boolean considerReservation)
    {
        //Try to fulfill normally
        IRecipeStorage storage = super.getFirstFullFillableRecipe(stackPredicate, count, considerReservation);

        //Couldn't fulfill normally, let's try to fulfill with a temporary smelting recipe. 
        if(storage == null)
        {
            storage = FurnaceRecipes.getInstance().getFirstSmeltingRecipeByResult(stackPredicate);
            if (storage != null)
            {
                final List<IItemHandler> handlers = getHandlers();
                if (!storage.canFullFillRecipe(count, Collections.emptyMap(), handlers.toArray(new IItemHandler[0])))
                {
                    return null;
                }
            }                
        }

        return storage;
    }

    /**
     * Get the list of items that the assistant needs to craft the currently queued tasks
     */
    public Set<ItemStack> getAssistantItems()
    {
        final Set<ItemStack> recipeOutputs = new HashSet<>();
        for (final ICitizenData citizen : getAssignedCitizen())
        {
            if (citizen.getJob() instanceof AbstractJobCrafter)
            {
                final List<IToken<?>> assignedTasks = citizen.getJob(AbstractJobCrafter.class).getAssignedTasks();
                for (final IToken<?> taskToken : assignedTasks)
                {
                    final IRequest<? extends PublicCrafting> request = (IRequest<? extends PublicCrafting>) colony.getRequestManager().getRequestForToken(taskToken);
                    final IRecipeStorage recipeStorage = getFirstRecipe(request.getRequest().getStack());
                    if (recipeStorage != null)
                    {
                        for (final ItemStorage itemStorage : recipeStorage.getCleanedInput())
                        {
                            recipeOutputs.add(itemStorage.getItemStack());
                        }
                    }
                }
            }
        }
        return recipeOutputs;
    }

    @Override
    public boolean canRecipeBeAdded(final IToken<?> token)
    {
        if(getBuildingLevel() < 3)
        {
            return false;
        }

        Optional<Boolean> isRecipeAllowed;

        if (!super.canRecipeBeAdded(token))
        {
            return false;
        }

        isRecipeAllowed = super.canRecipeBeAddedBasedOnTags(token);
        if (isRecipeAllowed.isPresent())
        {
            return isRecipeAllowed.get();
        }
        else
        {
            // Additional recipe rules

            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);

            return ItemStackUtils.CAN_EAT.test(storage.getPrimaryOutput())
                     || ItemStackUtils.CAN_EAT.test(FurnaceRecipes.getInstance()
                                                      .getSmeltingResult(storage.getPrimaryOutput()));

            // End Additional recipe rules
        }
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

        if (inventory && minimumStock.containsKey(new ItemStorage(stack)))
        {
            return stack.getCount();
        }

        if (ISFOOD.test(stack) && (localAlreadyKept.stream().filter(storage -> ISFOOD.test(storage.getItemStack())).mapToInt(ItemStorage::getAmount).sum() < STACKSIZE || !inventory))
        {
            final ItemStorage kept = new ItemStorage(stack);
            if (localAlreadyKept.contains(kept))
            {
                kept.setAmount(localAlreadyKept.remove(localAlreadyKept.indexOf(kept)).getAmount());
            }
            localAlreadyKept.add(kept);
            return 0;
        }

        final Predicate<ItemStack> allowedFuel = theStack -> getAllowedFuel().stream().anyMatch(fuelStack -> fuelStack.isItemEqual(theStack));
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
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.cook;
    }

    @Override
    public void onColonyTick(final IColony colony)
    {
        // TODO: Request on tick if no food, so that foo gets distributed from the warehouse even when there is currently no cook
        super.onColonyTick(colony);
        if(isCookingTimeout > 0)
        {
            isCookingTimeout = isCookingTimeout - 1;
        }
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
                final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                buffer.writeBoolean(canCraftComplexRecipes());
                buffer.writeBlockPos(getID());
                return new ContainerCrafting(id, inv, buffer);
            }
        }, buffer -> new PacketBuffer(buffer.writeBoolean(canCraftComplexRecipes())).writeBlockPos(getID()));
    }


    /**
     * BuildingCook View.
     */
    public static class View extends AbstractFilterableListsView
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
            return new WindowHutCook(this);
        }
    }
}
