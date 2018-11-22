package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingRequestResolver;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.ColonyConstants.ONWORLD_TICK_AVERAGE;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_MAXIMUM;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * The abstract class for each worker building.
 */
public abstract class AbstractBuildingWorker extends AbstractBuilding
{
    /**
     * Minimal level to ask for wood tools. (WOOD_HUT_LEVEL + 1 == stone)
     */
    public static final int WOOD_HUT_LEVEL = 0;

    /**
     * The list of recipes the worker knows, correspond to a subset of the recipes in the colony.
     */
    private final List<IToken> recipes = new ArrayList<>();

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public AbstractBuildingWorker(@NotNull final Colony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof ItemFood, getBuildingLevel() * 2);
    }

    /**
     * The abstract method which creates a job for the building.
     *
     * @param citizen the citizen to take the job.
     * @return the Job.
     */
    @NotNull
    public abstract AbstractJob createJob(CitizenData citizen);

    /**
     * Check if a certain ItemStack is in the request of a worker.
     *
     * @param stack the stack to chest.
     * @return true if so.
     */
    public boolean isItemStackInRequest(@Nullable final ItemStack stack)
    {
        if (stack == null || stack.getItem() == null)
        {
            return false;
        }

        for (final CitizenData data : getAssignedCitizen())
        {
            for (final IRequest request : getOpenRequests(data))
            {
                if (request.getDelivery().isItemEqualIgnoreDurability(stack))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method to check if the worker assigned to this building can craft an input stack.
     * Checks a) if he knows the recipe and
     *        b) if he has the required items in his inventory or in the hut.
     * @param stack the stack which shall be crafted.
     * @return true if possible.
     */
    public boolean canCraft(final ItemStack stack)
    {
        final Colony colony = getColony();

        if(colony == null)
        {
            return false;
        }

        return getFirstFullFillableRecipe(stack) != null;
    }

    /**
     * Check if is the worker has the knowledge to craft something.
     * @param stack the stack to craft.
     * @return the recipe storage if so.
     */
    @Nullable
    public IRecipeStorage getFirstRecipe(final ItemStack stack)
    {
        for(final IToken token : recipes)
        {
            final IRecipeStorage storage = ColonyManager.getRecipeManager().getRecipes().get(token);
            if (storage != null && storage.getPrimaryOutput().isItemEqual(stack))
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
    public IRecipeStorage getFirstFullFillableRecipe(final ItemStack tempStack)
    {
        for(final IToken token : recipes)
        {
            final IRecipeStorage storage = ColonyManager.getRecipeManager().getRecipes().get(token);
            if(storage != null && storage.getPrimaryOutput().isItemEqual(tempStack))
            {
                final List<IItemHandler> handlers = getHandlers();
                if(storage.canFullFillRecipe(handlers.toArray(new IItemHandler[handlers.size()])))
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
     * @return true if so.
     */
    public boolean canRecipeBeAdded()
    {
        return true;
    }


    /**
     * Get all handlers accociated with this building.
     * @return the handlers of the building + citizen.
     */
    public List<IItemHandler> getHandlers()
    {
        final Colony colony = getColony();
        if(this.getAssignedEntities().isEmpty() || colony == null || colony.getWorld() == null)
        {
            return Collections.emptyList();
        }

        final List<IItemHandler> handlers = new ArrayList<>();
        for(final CitizenData workerEntity: this.getAssignedCitizen())
        {
            handlers.add(new InvWrapper(workerEntity.getInventory()));
        }
        handlers.add(new InvWrapper(getTileEntity()));
        
        for (final BlockPos pos : getAdditionalCountainers())
        {
            final TileEntity entity = colony.getWorld().getTileEntity(pos);
            if(entity != null)
            {
                handlers.addAll(InventoryUtils.getItemHandlersFromProvider(entity));
            }
        }
        return handlers;
    }

    @Override
    public boolean assignCitizen(final CitizenData citizen)
    {
        if (!super.assignCitizen(citizen))
        {
            Log.getLogger().warn("Ohoohohoohoh, wasn't abel to assign citizen to work building!!!");
            return false;
        }

        // If we set a worker, inform it of such
        if (citizen != null)
        {
            citizen.getCitizenEntity().ifPresent(tempCitizen -> {
                if(!tempCitizen.getCitizenJobHandler().getLastJob().isEmpty()
                     && !tempCitizen.getCitizenJobHandler().getLastJob().equals(getJobName())
                     && !tempCitizen.getCitizenJobHandler().getLastJob().contains("student")
                     && !getJobName().contains("student"))
                {
                    citizen.resetExperienceAndLevel();
                }
                tempCitizen.getCitizenJobHandler().setLastJob(getJobName());
            });

            citizen.setWorkBuilding(this);
            colony.getProgressManager().progressEmploy(colony.getCitizenManager().getCitizens().stream().filter(citizenData -> citizenData.getJob() != null).collect(Collectors.toList()).size());
        }
        return true;
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey(TAG_WORKER))
        {
            try
            {
                final NBTTagList workersTagList = compound.getTagList(TAG_WORKER, Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < workersTagList.tagCount(); ++i)
                {
                    final CitizenData data;
                    if (workersTagList.getCompoundTagAt(i).hasKey(TAG_ID))
                    {
                        data = getColony().getCitizenManager().getCitizen(workersTagList.getCompoundTagAt(i).getInteger(TAG_ID));
                    }
                    else if (workersTagList.getCompoundTagAt(i).hasKey(TAG_WORKER_ID))
                    {
                        data = getColony().getCitizenManager().getCitizen(workersTagList.getCompoundTagAt(i).getInteger(TAG_WORKER_ID));
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
                MineColonies.getLogger().warn("Warning: Updating data structures:", e);
                final CitizenData worker = getColony().getCitizenManager().getCitizen(compound.getInteger(TAG_WORKER));
                assignCitizen(worker);
            }
        }

        recipes.clear();
        final NBTTagList recipesTags = compound.getTagList(TAG_RECIPES, Constants.NBT.TAG_COMPOUND);
        recipes.addAll(NBTUtils.streamCompound(recipesTags)
                .map(recipeCompound -> (IToken) StandardFactoryController.getInstance().deserialize(recipeCompound))
                .collect(Collectors.toList()));
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList workersTagList = new NBTTagList();
        for (@NotNull final CitizenData data : getAssignedCitizen())
        {
            if (data != null)
            {
                final NBTTagCompound idCompound = new NBTTagCompound();
                idCompound.setInteger(TAG_WORKER_ID, data.getId());
                workersTagList.appendTag(idCompound);
            }
        }
        compound.setTag(TAG_WORKER, workersTagList);

        @NotNull final NBTTagList recipesTagList = recipes.stream()
                                                     .map(iToken -> StandardFactoryController.getInstance().serialize(iToken))
                                                     .collect(NBTUtils.toNBTTagList());
        compound.setTag(TAG_RECIPES, recipesTagList);
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
    public void addRecipe(final IToken token)
    {
        if(canRecipeBeAdded() && Math.pow(2, getBuildingLevel()) >= (recipes.size() + 1))
        {
            recipes.add(token);
            markDirty();
        }
    }

    /**
     * Remove a recipe of the building.
     * @param token the id of the recipe.
     */
    public void removeRecipe(final IToken token)
    {
        recipes.remove(token);
        markDirty();
    }

    /**
     * @see AbstractBuilding#onUpgradeComplete(int)
     */
    @Override
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        super.onWorldTick(event);

        //
        // Code below this check won't lag each tick anymore
        //
        if (!Colony.shallUpdate(event.world, ONWORLD_TICK_AVERAGE))
        {
            return;
        }

        // If we have no active worker, grab one from the Colony
        // TODO Maybe the Colony should assign jobs out, instead?
        if (!hasAssignedCitizen()
              && ((getBuildingLevel() > 0 && isBuilt()) || this instanceof BuildingBuilder)
              && !this.getColony().isManualHiring())
        {
            final CitizenData joblessCitizen = getColony().getCitizenManager().getJoblessCitizen();
            if (joblessCitizen != null)
            {
                assignCitizen(joblessCitizen);
            }
        }
    }

    @Override
    public void removeCitizen(final CitizenData citizen)
    {
        if (isCitizenAssigned(citizen))
        {
            super.removeCitizen(citizen);
            citizen.setWorkBuilding(null);
        }
    }

    /**
     * The abstract method which returns the name of the job.
     *
     * @return the job name.
     */
    @NotNull
    public abstract String getJobName();

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);

        buf.writeInt(getAssignedCitizen().size());
        for (final CitizenData data : getAssignedCitizen())
        {
            buf.writeInt(data == null ? 0 : data.getId());
        }

        final List<IRecipeStorage> storages = new ArrayList<>();
        for(final IToken token: new ArrayList<>(recipes))
        {
            final IRecipeStorage storage = ColonyManager.getRecipeManager().getRecipes().get(token);
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
            ByteBufUtils.writeTag(buf, StandardFactoryController.getInstance().serialize(storage));
        }

        buf.writeBoolean(canCraftComplexRecipes());
    }

    /**
     * Get the max tool level useable by the worker.
     *
     * @return the integer.
     */
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
     * Available skills of the citizens.
     */
    public enum Skill
    {
        STRENGTH,
        ENDURANCE,
        CHARISMA,
        INTELLIGENCE,
        DEXTERITY,
        PLACEHOLDER
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> getResolvers()
    {
        return ImmutableList.of(
                new BuildingRequestResolver(getRequester().getRequesterLocation(), getColony().getRequestManager()
                        .getFactoryController().getNewInstance(TypeConstants.ITOKEN)),
                new PrivateWorkerCraftingRequestResolver(getRequester().getRequesterLocation(), getColony().getRequestManager()
                        .getFactoryController().getNewInstance(TypeConstants.ITOKEN)));
    }

    /**
     * Check if a building can craft complex recipes.
     * @return true if so.
     */
    public boolean canCraftComplexRecipes()
    {
        return false;
    }

    /**
     * AbstractBuildingWorker View for clients.
     */
    public static class View extends AbstractBuildingView
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
         * Creates the view representation of the building.
         *
         * @param c the colony.
         * @param l the location.
         */
        public View(final ColonyView c, @NotNull final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Returns the id of the worker.
         *
         * @return 0 if there is no worker else the correct citizen id.
         */
        public List<Integer> getWorkerId()
        {
            return new ArrayList<>(workerIDs);
        }

        /**
         * Sets the id of the worker.
         *
         * @param workerId the id to set.
         */
        public void addWorkerId(final int workerId)
        {
            workerIDs.add(workerId);
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
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
                final IRecipeStorage storage = StandardFactoryController.getInstance().deserialize(ByteBufUtils.readTag(buf));
                if(storage != null)
                {
                    recipes.add(storage);
                }
            }
            this.canCraftComplexRecipes = buf.readBoolean();
        }

        /**
         * Get the list of recipes.
         * @return copy of the list.
         */
        public List<IRecipeStorage> getRecipes()
        {
            return new ArrayList<>(recipes);
        }

        /**
         * Remove a recipe from the list.
         * @param i the index to remove.
         */
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
        public void switchIndex(final int i, final int j)
        {
            if(i < recipes.size() && j < recipes.size() && i >= 0 && j >= 0)
            {
                final IRecipeStorage storage = recipes.get(i);
                recipes.set(i, recipes.get(j));
                recipes.set(j, storage);
            }
        }

        @NotNull
        public Skill getPrimarySkill()
        {
            return Skill.PLACEHOLDER;
        }

        @NotNull
        public Skill getSecondarySkill()
        {
            return Skill.PLACEHOLDER;
        }

        /**
         * Remove a worker from the list.
         *
         * @param id the id to remove.
         */
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
        public boolean hasEnoughWorkers()
        {
            return !workerIDs.isEmpty();
        }

        /**
         * Check if a building can craft complex recipes.
         * @return true if so.
         */
        public boolean canCraftComplexRecipes()
        {
            return this.canCraftComplexRecipes;
        }
    }
}
