package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.modules.ICreatesResolversModule;
import com.minecolonies.api.colony.buildings.modules.IHasRequiredItemsModule;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingRequestResolver;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_MAXIMUM;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
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
        if (ItemStackUtils.isEmpty(stack))
        {
            return false;
        }

        for (final ICitizenData data : getAssignedCitizen())
        {
            for (final IRequest<?> request : getOpenRequests(data.getId()))
            {
                for (final ItemStack deliveryStack : request.getDeliveries())
                {
                    if (deliveryStack.sameItemStackIgnoreDurability(stack))
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
        for (final IHasRequiredItemsModule module : getModules(IHasRequiredItemsModule.class))
        {
            toKeep.putAll(module.getRequiredItemsAndAmount());
        }
        return toKeep;
    }

    @NotNull
    @Override
    public Skill getRecipeImprovementSkill()
    {
        return getPrimarySkill();
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

        final TileEntity entity = colony.getWorld().getBlockEntity(getID());
        if (entity != null)
        {
            final LazyOptional<IItemHandler> handler = entity.getCapability(ITEM_HANDLER_CAPABILITY, null);
            handler.ifPresent(handlers::add);
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

        if (compound.getAllKeys().contains(TAG_WORKER))
        {
            try
            {
                final ListNBT workersTagList = compound.getList(TAG_WORKER, Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < workersTagList.size(); ++i)
                {
                    final ICitizenData data;
                    if (workersTagList.getCompound(i).getAllKeys().contains(TAG_ID))
                    {
                        data = getColony().getCitizenManager().getCivilian(workersTagList.getCompound(i).getInt(TAG_ID));
                    }
                    else if (workersTagList.getCompound(i).getAllKeys().contains(TAG_WORKER_ID))
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
        return compound;
    }

    /**
     * Updates existing requests, if they match the recipes available at this worker
     */
    private void updateWorkerAvailableForRecipes()
    {
        for (final AbstractCraftingBuildingModule module : getModules(AbstractCraftingBuildingModule.class))
        {
            module.updateWorkerAvailableForRecipes();
        }
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        super.onColonyTick(colony);
        for (final ICraftingBuildingModule module : this.getModules(ICraftingBuildingModule.class))
        {
            module.checkForWorkerSpecificRecipes();
        }

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

        buf.writeInt(hiringMode.ordinal());
        buf.writeUtf(this.getJobName());
        buf.writeInt(getMaxInhabitants());
        buf.writeInt(getPrimarySkill().ordinal());
        buf.writeInt(getSecondarySkill().ordinal());
        buf.writeInt(getMaxInhabitants());
        buf.writeUtf(getJobDisplayName());
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
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();
        builder.add(new BuildingRequestResolver(getRequester().getLocation(), getColony().getRequestManager()
                                                                        .getFactoryController().getNewInstance(TypeConstants.ITOKEN)),
            new PrivateWorkerCraftingRequestResolver(getRequester().getLocation(), getColony().getRequestManager()
                                                                        .getFactoryController().getNewInstance(TypeConstants.ITOKEN)),
            new PrivateWorkerCraftingProductionResolver(getRequester().getLocation(), getColony().getRequestManager()
                                                                        .getFactoryController().getNewInstance(TypeConstants.ITOKEN)));

        for (final ICreatesResolversModule module : getModules(ICreatesResolversModule.class))
        {
            builder.addAll(module.createResolvers());
        }

        return builder.build();
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
        return stack.getItem().getFoodProperties().getNutrition() >= getBuildingLevel();
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

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    /**
     * Get setting for key. Utility function.
     * @param key the key.
     * @param <T> the key type.
     * @return the optional wrapping the value.
     */
    public <T extends ISetting> T getSetting(@NotNull final ISettingKey<T> key)
    {
        return getFirstModuleOccurance(ISettingsModule.class).getSetting(key);
    }

    /**
     * Get the right module for the recipe.
     * @param token the recipe trying to be fulfilled.
     * @return the matching module.
     */
    public ICraftingBuildingModule getCraftingModuleForRecipe(final IToken<?> token)
    {
        for (final ICraftingBuildingModule module: getModules(ICraftingBuildingModule.class))
        {
            if (module.holdsRecipe(token))
            {
                return module;
            }
        }
        return null;
    }
}
