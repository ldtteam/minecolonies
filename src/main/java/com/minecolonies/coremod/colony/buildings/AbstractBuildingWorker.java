package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.IBuildingWorkerView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.inventory.container.ContainerCrafting;
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
import com.minecolonies.coremod.network.messages.server.colony.building.worker.BuildingHiringModeMessage;
import com.minecolonies.coremod.research.MultiplierModifierResearchEffect;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.research.util.ResearchConstants.RECIPES;
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
    protected final List<IToken<?>> recipes = new ArrayList<>();

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
        toKeep.put(ItemStackUtils.CAN_EAT, new Tuple<>(getBuildingLevel() * 2, true));
        return toKeep;
    }

    @Override
    @Nullable
    public IRecipeStorage getFirstRecipe(final ItemStack stack)
    {
        return getFirstRecipe(itemStack -> !itemStack.isEmpty() && itemStack.isItemEqual(stack));
    }

    @Override
    @Nullable
    public IRecipeStorage getFirstRecipe(final Predicate<ItemStack> stackPredicate)
    {
        for (final IToken<?> token : recipes)
        {
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if (storage != null && stackPredicate.test(storage.getPrimaryOutput()))
            {
                return storage;
            }
        }
        return null;
    }

    @Override
    public IRecipeStorage getFirstFullFillableRecipe(final ItemStack tempStack)
    {
        return getFirstFullFillableRecipe(tempStack, tempStack.getCount());
    }

    @Override
    public IRecipeStorage getFirstFullFillableRecipe(final ItemStack tempStack, int count)
    {
        return getFirstFullFillableRecipe(itemStack -> !itemStack.isEmpty() && itemStack.isItemEqual(tempStack), count * tempStack.getCount());
    }

    @Override
    public IRecipeStorage getFirstFullFillableRecipe(final Predicate<ItemStack> stackPredicate, final int count)
    {
        for (final IToken<?> token : recipes)
        {
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if (storage != null && stackPredicate.test(storage.getPrimaryOutput()))
            {
                final List<IItemHandler> handlers = getHandlers();
                if (storage.canFullFillRecipe(count, handlers.toArray(new IItemHandler[0])))
                {
                    return storage;
                }
            }
        }
        return null;
    }

    @Override
    public boolean fullFillRecipe(final IRecipeStorage storage)
    {
        final List<IItemHandler> handlers = getHandlers();
        return storage.fullfillRecipe(handlers);
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
    protected Optional<Boolean> canRecipeBeAddedBasedOnTags(final IToken token)
    {

        ResourceLocation products = new ResourceLocation("minecolonies", this.getJobName().toLowerCase().concat("_product"));
        ResourceLocation ingredients = new ResourceLocation("minecolonies", this.getJobName().toLowerCase().concat("_ingredient"));
        ResourceLocation productsExcluded = new ResourceLocation("minecolonies", this.getJobName().toLowerCase().concat("_product_excluded"));
        ResourceLocation ingredientsExcluded = new ResourceLocation("minecolonies", this.getJobName().toLowerCase().concat("_ingredient_excluded"));

        final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
        if (storage == null)
        {
            return Optional.of(false);
        }

        // Check against excluded products
        if (ItemTags.getCollection().getOrCreate(productsExcluded).contains(storage.getPrimaryOutput().getItem()))
        {
            return Optional.of(false);
        }

        // Check against excluded ingredients
        for (final ItemStack stack : storage.getInput())
        {
            if (ItemTags.getCollection().getOrCreate(ingredientsExcluded).contains(stack.getItem()))
            {
                return Optional.of(false);
            }
        }

        // Check against allowed products
        if (ItemTags.getCollection().getOrCreate(products).contains(storage.getPrimaryOutput().getItem()))
        {
            return Optional.of(true);
        }

        // Check against allowed ingredients
        for (final ItemStack stack : storage.getInput())
        {
            if (ItemTags.getCollection().getOrCreate(ingredients).contains(stack.getItem()))
            {
                return Optional.of(true);
            }
        }

        return Optional.empty();
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
        double increase = 1;
        final MultiplierModifierResearchEffect effect = colony.getResearchManager().getResearchEffects().getEffect(RECIPES, MultiplierModifierResearchEffect.class);
        if (effect != null)
        {
            increase = 1 + effect.getEffect();
        }
        return Math.pow(2, getBuildingLevel()) * increase > getRecipes().size() + 1;
    }

    /**
     *
     */

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
        handlers.add(getTileEntity().getInventory());

        for (final BlockPos pos : getAdditionalCountainers())
        {
            final TileEntity entity = colony.getWorld().getTileEntity(pos);
            if (entity != null)
            {
                handlers.addAll(InventoryUtils.getItemHandlersFromProvider(entity));
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

        final ListNBT recipesTags = compound.getList(TAG_RECIPES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < recipesTags.size(); i++)
        {
            final IToken<?> token = StandardFactoryController.getInstance().deserialize(recipesTags.getCompound(i));
            if (!recipes.contains(token))
            {
                recipes.add(token);
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
            addRecipeToList(token);
            markDirty();
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
     * Add a recipe to the list of recipes.
     * @param token the token to add.
     */
    public void addRecipeToList(final IToken<?> token)
    {
        if (!recipes.contains(token))
        {
            recipes.add(token);
        }
    }

    @Override
    public void removeRecipe(final IToken<?> token)
    {
        recipes.remove(token);
        markDirty();
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
        for (final IToken<?> token : new ArrayList<>(recipes))
        {
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            if (storage == null)
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
        buf.writeBoolean(hasSpaceForMoreRecipes());
        buf.writeBoolean(isRecipeAlterationAllowed());
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
                final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                buffer.writeBoolean(canCraftComplexRecipes());
                buffer.writeBlockPos(getID());
                return new ContainerCrafting(id, inv, buffer);
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
        return true;
    }

    /**
     * Check for worker specific recipes and add them if necessary.
     */
    public void checkForWorkerSpecificRecipes()
    {
        // Override if necessary.
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
         * If the building can add more recipes.
         */
        private boolean canAddMoreRecipes;

        /**
         * If the building allows altering of recipes
         */
        private boolean isRecipeAlterationAllowed;

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
            this.canAddMoreRecipes = buf.readBoolean();
            this.isRecipeAlterationAllowed = buf.readBoolean();
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
            return canAddMoreRecipes;
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
    }
}
