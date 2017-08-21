package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.util.RecipeStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.ToolLevelConstants.*;


/**
 * The abstract class for each worker building.
 */
public abstract class AbstractBuildingWorker extends AbstractBuildingHut
{
    /**
     * Minimal level to ask for wood tools. (WOOD_HUT_LEVEL + 1 == stone)
     */
    public static final int WOOD_HUT_LEVEL = 0;

    /**
     * Tag used to store the worker to nbt.
     */
    private static final String TAG_WORKER = "worker";

    /**
     * NBTTag to store the recipes list.
     */
    private static final String TAG_RECIPES = "recipes";

    /**
     * Tag to serialize ITokens.
     */
    private static final String TAG_TOKEN = "tokenTag";

    /**
     * The list of recipes the worker knows, correspond to a subset of the recipes in the colony.
     */
    private final List<IToken> recipes = new ArrayList<>();

    /**
     * The citizenData of the assigned worker.
     */
    private CitizenData worker;

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public AbstractBuildingWorker(@NotNull final Colony c, final BlockPos l)
    {
        super(c, l);
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
     * Returns the worker of the current building.
     *
     * @return {@link CitizenData} of the current building
     */
    public CitizenData getWorker()
    {
        return worker;
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
     * Get a fullfillable recipe to execute.
     * @param tempStack the stack which should be crafted.
     * @return the recipe or null.
     */
    public RecipeStorage getFirstFullFillableRecipe(final ItemStack tempStack)
    {
        for(final IToken token : recipes)
        {
            final RecipeStorage storage = ColonyManager.getRecipes().get(token);
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
    public boolean fullFillRecipe(final RecipeStorage storage)
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
        if(this.getWorkerEntity() == null || colony == null || colony.getWorld() == null)
        {
            return Collections.emptyList();
        }

        final EntityCitizen workerEntity = this.getWorkerEntity();
        final List<IItemHandler> handlers = new ArrayList<>();
        handlers.add(new InvWrapper(workerEntity.getInventoryCitizen()));
        handlers.add(new InvWrapper(getTileEntity()));

        for (final BlockPos pos : getAdditionalCountainers())
        {
            final TileEntity entity = colony.getWorld().getTileEntity(pos);
            if (entity instanceof TileEntityChest)
            {
                handlers.add(new InvWrapper((TileEntityChest) entity));
            }
        }
        return handlers;
    }

    /**
     * Set the worker of the current building.
     *
     * @param citizen {@link CitizenData} of the worker
     */
    public void setWorker(final CitizenData citizen)
    {
        if (worker == citizen)
        {
            return;
        }

        // If we have a worker, it no longer works here
        if (worker != null)
        {
            final EntityCitizen tempCitizen = worker.getCitizenEntity();
            worker.setWorkBuilding(null);
            if (tempCitizen != null)
            {
                tempCitizen.setLastJob(getJobName());
            }
            setNeedsTool(ToolType.NONE, TOOL_LEVEL_HAND);
        }

        worker = citizen;

        // If we set a worker, inform it of such
        if (worker != null)
        {
            final EntityCitizen tempCitizen = citizen.getCitizenEntity();
            if (tempCitizen != null && !tempCitizen.getLastJob().equals(getJobName()))
            {
                citizen.resetExperienceAndLevel();
            }
            worker.setWorkBuilding(this);
        }

        markDirty();
    }

    /**
     * Override this method if you want to keep some items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @param stack the stack to decide on
     * @return true if the stack should remain in inventory
     */
    public boolean neededForWorker(@Nullable final ItemStack stack)
    {
        return false;
    }

    /**
     * Returns the {@link net.minecraft.entity.Entity} of the worker.
     *
     * @return {@link net.minecraft.entity.Entity} of the worker
     */
    @Nullable
    public EntityCitizen getWorkerEntity()
    {
        if (worker == null)
        {
            return null;
        }
        return worker.getCitizenEntity();
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey(TAG_WORKER))
        {
            // Bypass setWorker, which marks dirty
            worker = getColony().getCitizen(compound.getInteger(TAG_WORKER));
            if (worker != null)
            {
                worker.setWorkBuilding(this);
            }
        }

        recipes.clear();
        final NBTTagList recipesTags = compound.getTagList(TAG_RECIPES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < recipesTags.tagCount(); ++i)
        {
            final NBTTagCompound recipeTag = recipesTags.getCompoundTagAt(i);
            final IToken token = StandardFactoryController.getInstance().deserialize(recipeTag.getCompoundTag(
                    TAG_TOKEN));
            recipes.add(token);
        }
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (worker != null)
        {
            compound.setInteger(TAG_WORKER, worker.getId());
        }

        @NotNull final NBTTagList recipesTagList = new NBTTagList();
        for (@NotNull final IToken token : recipes)
        {
            @NotNull final NBTTagCompound recipeTagCompound = new NBTTagCompound();
            recipeTagCompound.setTag(TAG_TOKEN , StandardFactoryController.getInstance().serialize(token));
            recipesTagList.appendTag(recipeTagCompound);
        }
        compound.setTag(TAG_RECIPES, recipesTagList);
    }

    @Override
    public void onDestroyed()
    {
        if (hasWorker())
        {
            // EntityCitizen will detect the workplace is gone and fix up it's
            // Entity properly
            removeCitizen(worker);
        }

        super.onDestroyed();
    }

    /**
     * executed when a new day start.
     */
    public void onWakeUp()
    {
    }


    /**
     * Returns whether or not the building has a worker.
     *
     * @return true if building has worker, otherwise false.
     */
    public boolean hasWorker()
    {
        return worker != null;
    }

    /**
     * Returns if the {@link CitizenData} is the same as {@link #worker}.
     *
     * @param citizen {@link CitizenData} you want to compare
     * @return true if same citizen, otherwise false
     */
    public boolean isWorker(final CitizenData citizen)
    {
        return citizen == worker;
    }

    /**
     * The abstract method which returns the name of the job.
     *
     * @return the job name.
     */
    @NotNull
    public abstract String getJobName();

    @Override
    public void removeCitizen(final CitizenData citizen)
    {
        if (isWorker(citizen))
        {
            setWorker(null);
        }
    }

    /**
     * Add a recipe to the building.
     * @param token the id of the recipe.
     */
    public void addRecipe(final IToken token)
    {
        if(canRecipeBeAdded() && Math.pow(2, getBuildingLevel()) >= recipes.size())
        {
            recipes.add(token);
        }
    }

    /**
     * Remove a recipe of the building.
     * @param token the id of the recipe.
     */
    public void removeRecipe(final IToken token)
    {
        recipes.remove(token);
    }

    /**
     * Get the max tool level useable by the worker.
     *
     * @return the integer.
     */
    public int getMaxToolLevel()
    {
        if (getBuildingLevel()>=getMaxBuildingLevel())
        {
            return TOOL_LEVEL_MAXIMUM;
        }
        else if (getBuildingLevel() <= WOOD_HUT_LEVEL)
        {
            return TOOL_LEVEL_WOOD_OR_GOLD;
        }
        return getBuildingLevel()-WOOD_HUT_LEVEL;
    }

    /**
     * @see AbstractBuilding#onUpgradeComplete(int)
     */
    @Override
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        super.onWorldTick(event);

        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        // If we have no active worker, grab one from the Colony
        // TODO Maybe the Colony should assign jobs out, instead?
        if (!hasWorker() && (getBuildingLevel() > 0 || this instanceof BuildingBuilder)
              && !this.getColony().isManualHiring())
        {
            final CitizenData joblessCitizen = getColony().getJoblessCitizen();
            if (joblessCitizen != null)
            {
                setWorker(joblessCitizen);
            }
        }
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);

        buf.writeInt(worker == null ? 0 : worker.getId());

        buf.writeInt(recipes.size());
        for(final IToken token: recipes)
        {
            ColonyManager.getRecipes().get(token).writeToBuffer(buf);
        }
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

    /**
     * AbstractBuildingWorker View for clients.
     */
    public static class View extends AbstractBuildingHut.View
    {
        /**
         * WorkerId of the building
         */
        private int workerId;

        /**
         * List of recipes.
         */
        private final List<RecipeStorage> recipes = new ArrayList<>();

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
        public int getWorkerId()
        {
            return workerId;
        }

        /**
         * Sets the id of the worker.
         *
         * @param workerId the id to set.
         */
        public void setWorkerId(final int workerId)
        {
            this.workerId = workerId;
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);

            workerId = buf.readInt();

            recipes.clear();

            final int recipesSize = buf.readInt();
            for(int i = 0; i < recipesSize; i++)
            {
                recipes.add(RecipeStorage.createFromByteBuffer(buf));
            }
        }

        /**
         * Get the list of recipes.
         * @return copy of the list.
         */
        public List<RecipeStorage> getRecipes()
        {
            return new ArrayList<>(recipes);
        }

        /**
         * Remove a recipe from the list.
         * @param i the index to remove.
         */
        public void removeRecipe(final int i)
        {
            if(i < recipes.size() && i > 0)
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
                final RecipeStorage storage = recipes.get(i);
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
    }
}
