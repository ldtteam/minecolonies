package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.IBuildingWorkerView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingRequestResolver;
import com.minecolonies.coremod.network.messages.BuildingHiringModeMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.CitizenConstants.BONUS_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_MAXIMUM;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * The abstract class for each worker building.
 */
public abstract class AbstractBuildingWorker extends AbstractBuilding implements IBuildingWorker
{

    /**
     * The list of recipes the worker knows, correspond to a subset of the recipes in the colony.
     */
    protected final List<IToken> recipes = new ArrayList<>();

    /**
     * The hiring mode of this particular building, by default overriden by colony mode.
     */
    private HiringMode hiringMode = HiringMode.DEFAULT;

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

    /**
     * Check if a certain ItemStack is in the request of a worker.
     *
     * @param stack the stack to chest.
     * @return true if so.
     */
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
                for(final ItemStack deliveryStack : request.getDeliveries())
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

    /**
     * Set a new hiring mode in the building.
     * @param hiringMode the mode to set.
     */
    @Override
    public void setHiringMode(final HiringMode hiringMode)
    {
        this.hiringMode = hiringMode;
        this.markDirty();
    }

    /**
     * Get the current hiring mode of this building.
     * @return the current mode.
     */
    @Override
    public HiringMode getHiringMode()
    {
        return hiringMode;
    }

    /**
     * Override this method if you want to keep an amount of items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = new HashMap<>(super.getRequiredItemsAndAmount());
        toKeep.put(ItemStackUtils.CAN_EAT, new Tuple<>(getBuildingLevel() * 2, true));
        return toKeep;
    }

    /**
     * Check if is the worker has the knowledge to craft something.
     * @param stack the stack to craft.
     * @return the recipe storage if so.
     */
    @Override
    @Nullable
    public IRecipeStorage getFirstRecipe(final ItemStack stack)
    {
        return getFirstRecipe(itemStack -> !itemStack.isEmpty() && itemStack.isItemEqual(stack));
    }

    /**
     * Check if is the worker has the knowledge to craft something.
     * @param stackPredicate the predicate to check for fullfillment.
     * @return the recipe storage if so.
     */
    @Override
    @Nullable
    public IRecipeStorage getFirstRecipe(final Predicate<ItemStack> stackPredicate)
    {
        for(final IToken token : recipes)
        {
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if (storage != null && stackPredicate.test(storage.getPrimaryOutput()))
            {
                return storage;
            }
        }
        return null;
    }

    /**
     * Get a fullfillable recipe to execute.
     * @param tempStack the stack which should be crafted.
     * @return the recipe or null.
     */
    @Override
    public IRecipeStorage getFirstFullFillableRecipe(final ItemStack tempStack)
    {
        return getFirstFullFillableRecipe(tempStack, tempStack.getCount());
    }

    /**
     * Get a fullfillable recipe to execute, with at least a given count.
     *
     * @param tempStack The temp stack to match.
     * @param count     The count to craft.
     * @return The recipe or null.
     */
    @Override
    public IRecipeStorage getFirstFullFillableRecipe(final ItemStack tempStack, int count)
    {
        return getFirstFullFillableRecipe(itemStack -> !itemStack.isEmpty() && itemStack.isItemEqual(tempStack), count);
    }

    @Override
    public IRecipeStorage getFirstFullFillableRecipe(final Predicate<ItemStack> stackPredicate, final int count)
    {
        for(final IToken token : recipes)
        {
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if(storage != null && stackPredicate.test(storage.getPrimaryOutput()))
            {
                final List<IItemHandler> handlers = getHandlers();
                if(storage.canFullFillRecipe(count, handlers.toArray(new IItemHandler[0])))
                {
                    return storage;
                }
            }
        }
        return null;
    }

    /**
     * Try to fullfill a recipe.
     * @param storage with the storage.
     * @return true if successful.
     */
    @Override
    public boolean fullFillRecipe(final IRecipeStorage storage)
    {
        final List<IItemHandler> handlers = getHandlers();
        return storage.fullfillRecipe(handlers);
    }

    /**
     * Switch indices of two recipes because of the priority.
     * @param i the first index.
     * @param j the second index.
     */
    @Override
    public void switchIndex(final int i, final int j)
    {
        if(i < recipes.size() && j < recipes.size() && i >= 0 && j >= 0)
        {
            final IToken storage = recipes.get(i);
            recipes.set(i, recipes.get(j));
            recipes.set(j, storage);
        }
    }

    /**
     * Check if a recipe can be added.
     * This is only important for 3x3 crafting.
     * Workers shall override this if necessary.
     * @param ignored the token of the recipe.
     * @return true if so.
     */
    @Override
    public boolean canRecipeBeAdded(final IToken ignored)
    {
        return IBuildingWorker.canBuildingCanLearnMoreRecipes (getBuildingLevel(), getRecipes().size());
    }

    /**
     * Get the list of all recipes the worker can learn.
     * @return a copy of the tokens of the recipes.
     */
    @Override
    public List<IToken> getRecipes()
    {
        return new ArrayList<>(recipes);
    }

    /**
     * Get all handlers accociated with this building.
     * @return the handlers of the building + citizen.
     */
    @Override
    public List<IItemHandler> getHandlers()
    {
        final IColony colony = getColony();
        if(this.getAssignedEntities().isEmpty() || colony == null || colony.getWorld() == null)
        {
            return Collections.emptyList();
        }

        final Set<IItemHandler> handlers = new HashSet<>();
        for(final ICitizenData workerEntity: this.getAssignedCitizen())
        {
            handlers.add(workerEntity.getInventory());
        }
        handlers.add(getTileEntity().getInventory());
        
        for (final BlockPos pos : getAdditionalCountainers())
        {
            final TileEntity entity = colony.getWorld().getTileEntity(pos);
            if(entity != null)
            {
                handlers.addAll(InventoryUtils.getItemHandlersFromProvider(entity));
            }
        }
        return ImmutableList.copyOf(handlers);
    }

    @Override
    public boolean assignCitizen(final ICitizenData citizen)
    {
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
            colony.getProgressManager().progressEmploy(colony.getCitizenManager().getCitizens().stream().filter(citizenData -> citizenData.getJob() != null).collect(Collectors.toList()).size());
        }
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
                        data = getColony().getCitizenManager().getCitizen(workersTagList.getCompound(i).getInt(TAG_ID));
                    }
                    else if (workersTagList.getCompound(i).keySet().contains(TAG_WORKER_ID))
                    {
                        data = getColony().getCitizenManager().getCitizen(workersTagList.getCompound(i).getInt(TAG_WORKER_ID));
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
                final ICitizenData worker = getColony().getCitizenManager().getCitizen(compound.getInt(TAG_WORKER));
                assignCitizen(worker);
            }
        }

        this.hiringMode = HiringMode.values()[compound.getInt(TAG_HIRING_MODE)];

        recipes.clear();
        final ListNBT recipesTags = compound.getList(TAG_RECIPES, Constants.NBT.TAG_COMPOUND);
        recipes.addAll(NBTUtils.streamCompound(recipesTags)
                         .map(recipeCompound -> (IToken) StandardFactoryController.getInstance().deserialize(recipeCompound))
                         .collect(Collectors.toList()));
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

    /**
     * Executed when a new day start.
     */
    @Override
    public void onWakeUp()
    {

    }

    /**
     * Add a recipe to the building.
     * @param token the id of the recipe.
     */
    @Override
    public boolean addRecipe(final IToken token)
    {
        if(canRecipeBeAdded(token))
        {
            recipes.add(token);
            markDirty();
            return true;
        }
        return false;
    }

    /**
     * Remove a recipe of the building.
     * @param token the id of the recipe.
     */
    @Override
    public void removeRecipe(final IToken token)
    {
        recipes.remove(token);
        markDirty();
    }

    /**
     * Auto assigns workers on a tick from the colony.
     * @param colony the colony being ticked
     */
    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        super.onColonyTick(colony);

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
        for(final IToken token: new ArrayList<>(recipes))
        {
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if(storage == null)
            {
                removeRecipe(token);
            }
            else
            {
                storages.add(storage);
            }
        }

        buf.writeInt(storages.size());
        for(final IRecipeStorage storage: storages)
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
    }

    /**
     * Get the max tool level useable by the worker.
     *
     * @return the integer.
     */
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

    /**
     * Method which defines if a worker should be allowed to work during the rain.
     * @return true if so.
     */
    @Override
    public boolean canWorkDuringTheRain()
    {
        return getBuildingLevel() >= BONUS_BUILDING_LEVEL;
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

    /**
     * Check if a building can craft complex recipes.
     * @return true if so.
     */
    @Override
    public boolean canCraftComplexRecipes()
    {
        return false;
    }

    /**
     * AbstractBuildingWorker View for clients.
     */
    public static class View extends AbstractBuildingView implements IBuildingWorkerView
    {
        /**
         * List of the worker ids.
         */
        private final List<Integer> workerIDs = new ArrayList<>();

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
         * Creates the view representation of the building.
         *
         * @param c the colony.
         * @param l the location.
         */
        public View(final IColonyView c, @NotNull final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Returns the id of the worker.
         *
         * @return 0 if there is no worker else the correct citizen id.
         */
        @Override
        public List<Integer> getWorkerId()
        {
            return new ArrayList<>(workerIDs);
        }

        /**
         * Sets the id of the worker.
         *
         * @param workerId the id to set.
         */
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
            for(int i = 0; i < recipesSize; i++)
            {
                final IRecipeStorage storage = StandardFactoryController.getInstance().deserialize(buf.readCompoundTag());
                if(storage != null)
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
        }

        /**
         * Get the list of recipes.
         * @return copy of the list.
         */
        @Override
        public List<IRecipeStorage> getRecipes()
        {
            return new ArrayList<>(recipes);
        }

        /**
         * Remove a recipe from the list.
         * @param i the index to remove.
         */
        @Override
        public void removeRecipe(final int i)
        {
            if(i < recipes.size() && i >= 0)
            {
                recipes.remove(i);
            }
        }

        /**
         * Switch the indices of two recipes.
         * @param i the first.
         * @param j the second.
         */
        @Override
        public void switchIndex(final int i, final int j)
        {
            if(i < recipes.size() && j < recipes.size() && i >= 0 && j >= 0)
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

        /**
         * Remove a worker from the list.
         *
         * @param id the id to remove.
         */
        @Override
        public void removeWorkerId(final int id)
        {
            for (int i = 0; i < workerIDs.size(); i++)
            {
                final int workerId = workerIDs.get(i);
                if (workerId == id)
                {
                    workerIDs.remove(i);
                }
            }
        }

        /**
         * Check if it has enough worker.
         *
         * @return true if so.
         */
        @Override
        public boolean hasEnoughWorkers()
        {
            return !workerIDs.isEmpty();
        }

        /**
         * Check if a building can craft complex recipes.
         * @return true if so.
         */
        @Override
        public boolean canCraftComplexRecipes()
        {
            return this.canCraftComplexRecipes;
        }

        /**
         * Check if an additional recipe can be added.
         * @return true if so.
         */
        @Override
        public boolean canRecipeBeAdded()
        {
            return IBuildingWorker.canBuildingCanLearnMoreRecipes(getBuildingLevel(), getRecipes().size());
        }

        /**
         * Get the hiring mode of the building.
         * @return the mode.
         */
        @Override
        public HiringMode getHiringMode()
        {
            return hiringMode;
        }

        /**
         * Set the hiring mode and sync to the server.
         * @param hiringMode the mode to set.
         */
        @Override
        public void setHiringMode(final HiringMode hiringMode)
        {
            this.hiringMode = hiringMode;
            Network.getNetwork().sendToServer(new BuildingHiringModeMessage(this, hiringMode));
        }

        /**
         * Get the name of the job.
         * @return job name.
         */
        @Override
        public String getJobName()
        {
            return this.jobName;
        }

        /**
         * Check if it is possible to assign the citizen to this building.
         * @param citizenDataView the citizen to test.
         * @return true if so.
         */
        public boolean canAssign(final ICitizenDataView citizenDataView)
        {
            return true;
        }

        /**
         * Get the max number of inhabitants
         * @return max inhabitants
         */
        public int getMaxInhabitants()
        {
            return this.maxInhabitants;
        }
    }
}
