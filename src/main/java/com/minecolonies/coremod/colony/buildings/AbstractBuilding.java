package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ISchematicProvider;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemBuildingDataStore;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Pickup;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.modules.AbstractAssignedCitizenModule;
import com.minecolonies.coremod.colony.buildings.modules.LivingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.colony.interactionhandling.RequestBasedInteraction;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.requesters.BuildingBasedRequester;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildRemoval;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import com.minecolonies.coremod.util.ChunkDataHelper;
import com.minecolonies.coremod.util.ColonyUtils;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.colony.requestsystem.requestable.deliveryman.AbstractDeliverymanRequestable.getPlayerActionPriority;
import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.BuildingConstants.NO_WORK_ORDER;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.Suppression.GENERIC_WILDCARD;
import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;
import static com.minecolonies.api.util.constant.TranslationConstants.ASYNC_REQUEST;
import static com.minecolonies.api.util.constant.TranslationConstants.NORMAL_REQUEST;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * Base building class, has all the foundation for what a building stores and does.
 * <p>
 * We suppress the warning which warns you about referencing child classes in the parent because that's how we register the instances of the childClasses to their views and
 * blocks.
 */
@SuppressWarnings({"squid:S2390", "PMD.ExcessiveClassLength"})
public abstract class AbstractBuilding extends AbstractBuildingContainer
{
    /**
     * Breeding setting.
     */
    public static final ISettingKey<BoolSetting> BREEDING = new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "breeding"));

    public static final int MAX_BUILD_HEIGHT = 256;
    public static final int MIN_BUILD_HEIGHT = 1;

    /**
     * The data store id for request system related data.
     */
    private IToken<?> rsDataStoreToken;

    /**
     * The ID of the building. Needed in the request system to identify it.
     */
    private IRequester requester;

    /**
     * If the building has been built already.
     */
    private boolean isBuilt = false;

    /**
     * The custom name of the building, empty by default.
     */
    private String customName = "";

    /**
     * Whether a guard building is near
     */
    private boolean guardBuildingNear = false;

    /**
     * Whether we need to recheck if a guard building is near
     */
    private boolean recheckGuardBuildingNear = true;

    /**
     * Made to check if the building has to update the server/client.
     */
    private boolean dirty = false;

    /**
     * Set of building modules this building has.
     */
    protected List<IBuildingModule> modules = new ArrayList<>();

    /**
     * Constructor for a AbstractBuilding.
     *
     * @param colony Colony the building belongs to.
     * @param pos    Location of the building (it's Hut Block).
     */
    protected AbstractBuilding(@NotNull final IColony colony, final BlockPos pos)
    {
        super(pos, colony);

        this.requester = StandardFactoryController.getInstance().getNewInstance(TypeToken.of(BuildingBasedRequester.class), this);
        setupRsDataStore();
    }

    @Override
    public boolean hasModule(final Class<? extends IBuildingModule> clazz)
    {
        for (final IBuildingModule module : modules)
        {
            if (clazz.isInstance(module))
            {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public <T extends IBuildingModule> T getFirstModuleOccurance(final Class<T> clazz)
    {
        for (final IBuildingModule module : modules)
        {
            if (clazz.isInstance(module))
            {
                return (T) module;
            }
        }

        throw new IllegalStateException("The module of class: " + clazz.toString() + "should never be null!");
    }

    @NotNull
    @Override
    public <T extends IBuildingModule> Optional<T> getFirstOptionalModuleOccurance(final Class<T> clazz)
    {
        for (final IBuildingModule module : modules)
        {
            if (clazz.isInstance(module))
            {
                return Optional.of((T) module);
            }
        }
        return Optional.empty();
    }

    @NotNull
    @Override
    public <T extends IBuildingModule> T getModuleMatching(final Class<T> clazz, final Predicate<? super T> modulePredicate)
    {
        for (final IBuildingModule module : modules)
        {
            if (clazz.isInstance(module) && modulePredicate.test((T) module))
            {
                return (T) module;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public <T extends IBuildingModule> List<T> getModules(final Class<T> clazz)
    {
        return this.modules.stream()
                .filter(clazz::isInstance)
                .map(c -> (T) c)
                .collect(Collectors.toList());
    }

    @Override
    public void registerModule(@NotNull final IBuildingModule module)
    {
        this.modules.add(module);
    }

    /**
     * Getter for the custom name of a building.
     *
     * @return the custom name.
     */
    @Override
    @NotNull
    public String getCustomBuildingName()
    {
        if (customName.isEmpty())
        {
            return getSchematicName();
        }
        return this.customName;
    }

    /**
     * Executed when a new day start.
     */
    @Override
    public void onWakeUp()
    {
        getModules(IBuildingEventsModule.class).forEach(IBuildingEventsModule::onWakeUp);
    }

    /**
     * Executed every time when citizen finish inventory cleanup called after citizen got paused. Use for cleaning a state only.
     */
    @Override
    public void onCleanUp(final ICitizenData citizen)
    {
        /*
         * Buildings override this if required.
         */
    }

    /**
     * Executed when RestartCitizenMessage is called and worker is paused. Use for reseting, onCleanUp is called before this
     */
    @Override
    public void onRestart(final ICitizenData citizen)
    {
        // Unpause citizen
        citizen.setPaused(false);
        /*
         * Buildings override this if required.
         */
    }

    @Override
    public void onPlayerEnterBuilding(final PlayerEntity player)
    {
        getModules(IBuildingEventsModule.class).forEach(module -> module.onPlayerEnterBuilding(player));
    }

    @Override
    public void onPlayerEnterNearby(final PlayerEntity player)
    {
        if (getBuildingLevel() == 0 || getSchematicName() == null || getSchematicName().isEmpty())
        {
            return;
        }

        if (isInBuilding(player.blockPosition()))
        {
            onPlayerEnterBuilding(player);
        }
    }

    /**
     * On setting down the building.
     */
    @Override
    public void onPlacement()
    {
        ChunkDataHelper.claimColonyChunks(colony, true, getPosition(), getClaimRadius(getBuildingLevel()));
    }

    /**
     * Checks if a block matches the current object.
     *
     * @param block Block you want to know whether it matches this class or not.
     * @return True if the block matches this class, otherwise false.
     */
    @Override
    public boolean isMatchingBlock(@NotNull final Block block)
    {
        return this.getBuildingType().getBuildingBlock() == block;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        loadRequestSystemFromNBT(compound);
        if (compound.getAllKeys().contains(TAG_IS_BUILT))
        {
            isBuilt = compound.getBoolean(TAG_IS_BUILT);
        }
        else if (getBuildingLevel() > 0)
        {
            isBuilt = true;
        }
        if (compound.getAllKeys().contains(TAG_CUSTOM_NAME))
        {
            this.customName = compound.getString(TAG_CUSTOM_NAME);
        }

        getModules(IPersistentModule.class).forEach(module -> module.deserializeNBT(compound));
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        final ListNBT list = new ListNBT();
        for (final IRequestResolver<?> requestResolver : getResolvers())
        {
            list.add(StandardFactoryController.getInstance().serialize(requestResolver.getId()));
        }
        compound.put(TAG_RESOLVER, list);
        compound.putString(TAG_BUILDING_TYPE, this.getBuildingType().getRegistryName().toString());
        writeRequestSystemToNBT(compound);
        compound.putBoolean(TAG_IS_BUILT, isBuilt);
        compound.putString(TAG_CUSTOM_NAME, customName);

        getModules(IPersistentModule.class).forEach(module -> module.serializeNBT(compound));
        return compound;
    }

    /**
     * Destroys the block. Calls {@link #onDestroyed()}.
     */
    @Override
    public final void destroy()
    {
        onDestroyed();
        colony.getBuildingManager().removeBuilding(this, colony.getPackageManager().getCloseSubscribers());
        colony.getRequestManager().getDataStoreManager().remove(this.rsDataStoreToken);

        for (final BlockPos childpos : getChildren())
        {
            final IBuilding building = colony.getBuildingManager().getBuilding(childpos);
            if (building != null)
            {
                building.destroy();
            }
        }
    }

    @Override
    public void onDestroyed()
    {
        final AbstractTileEntityColonyBuilding tileEntityNew = this.getTileEntity();
        final World world = colony.getWorld();
        final Block block = world.getBlockState(this.getPosition()).getBlock();

        if (tileEntityNew != null)
        {
            InventoryUtils.dropItemHandler(tileEntityNew.getInventory(),
              world,
              tileEntityNew.getPosition().getX(),
              tileEntityNew.getPosition().getY(),
              tileEntityNew.getPosition().getZ());
            world.updateNeighbourForOutputSignal(this.getPosition(), block);
        }

        ChunkDataHelper.claimColonyChunks(colony, false, this.getID(), getClaimRadius(getBuildingLevel()));
        ConstructionTapeHelper.removeConstructionTape(getCorners(), world);

        getModules(IBuildingEventsModule.class).forEach(IBuildingEventsModule::onDestroyed);
    }

    /**
     * Adds work orders to the {@link Colony#getWorkManager()}.
     *
     * @param level   Desired level.
     * @param builder the assigned builder.
     */
    protected void requestWorkOrder(final int level, final BlockPos builder, final boolean removal)
    {
        for (@NotNull final WorkOrderBuildBuilding o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuildBuilding.class))
        {
            if (o.getSchematicLocation().equals(getID()))
            {
                return;
            }
        }

        WorkOrderBuild workOrder;
        if (removal)
        {
            if (!canDeconstruct())
            {
                LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
                  "entity.builder.cantdeconstruct");
                return;
            }
            workOrder = new WorkOrderBuildRemoval(this, level);
        }
        else
        {
            workOrder = new WorkOrderBuildBuilding(this, level);
        }

        if (!removal && !canBeBuiltByBuilder(level) && !workOrder.canBeResolved(colony, level))
        {
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
              "entity.builder.messagebuildernecessary", Integer.toString(level));
            return;
        }

        if (workOrder.tooFarFromAnyBuilder(colony, level) && builder.equals(BlockPos.ZERO))
        {
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
              "entity.builder.messagebuilderstoofar");
            return;
        }

        if (getCorners().getA().getY() >= MAX_BUILD_HEIGHT || getCorners().getB().getY() >= MAX_BUILD_HEIGHT)
        {
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
              "entity.builder.messagebuildtoohigh");
            return;
        }
        else if (getPosition().getY() <= MIN_BUILD_HEIGHT)
        {
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
              "entity.builder.messagebuildtoolow");
            return;
        }

        if (!builder.equals(BlockPos.ZERO))
        {
            final IBuilding building = colony.getBuildingManager().getBuilding(builder);
            if (building instanceof AbstractBuildingStructureBuilder && (building.getBuildingLevel() >= level || canBeBuiltByBuilder(level)))
            {
                workOrder.setClaimedBy(builder);
            }
            else
            {
                LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
                  "entity.builder.messagebuildernecessary", Integer.toString(level));
                return;
            }
        }

        colony.getWorkManager().addWorkOrder(workOrder, false);
        colony.getProgressManager().progressWorkOrderPlacement(workOrder);

        if (workOrder.getID() != 0)
        {
            LanguageHandler.sendPlayersMessage(colony.getImportantMessageEntityPlayers(), "com.minecolonies.coremod.workorderadded", workOrder.getDisplayName(), colony.getName(), workOrder.getSchematicLocation().getX(), workOrder.getSchematicLocation().getY(), workOrder.getSchematicLocation().getZ());
        }
        markDirty();
    }

    /**
     * Check if this particular building can be deconstructed.
     * @return true if so.
     */
    public boolean canDeconstruct()
    {
        return true;
    }

    /**
     * Method to define if a builder can build this although the builder is not level 1 yet.
     *
     * @return true if so.
     */
    @Override
    public boolean canBeBuiltByBuilder(final int newLevel)
    {
        return false;
    }

    @Override
    public final void markDirty()
    {
        dirty = true;
        if (colony != null)
        {
            colony.getBuildingManager().markBuildingsDirty();
        }
    }

    @Override
    public final boolean isDirty()
    {
        for (final IBuildingModule module : modules)
        {
            if (module.checkDirty())
            {
                return true;
            }
        }
        return dirty;
    }

    @Override
    public final void clearDirty()
    {
        dirty = false;
        for (final IBuildingModule module : modules)
        {
            module.clearDirty();
        }
    }

    @Override
    public void processOfflineTime(final long time)
    {
        // Do nothing.
    }

    /**
     * Checks if this building have a work order.
     *
     * @return true if the building is building, upgrading or repairing.
     */
    @Override
    public boolean hasWorkOrder()
    {
        return getCurrentWorkOrderLevel() != NO_WORK_ORDER;
    }

    /**
     * Get the current level of the work order.
     *
     * @return NO_WORK_ORDER if not current work otherwise the level requested.
     */
    private int getCurrentWorkOrderLevel()
    {
        for (@NotNull final WorkOrderBuildBuilding o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuildBuilding.class))
        {
            if (o.getSchematicLocation().equals(getID()))
            {
                return o.getUpgradeLevel();
            }
        }

        for (@NotNull final WorkOrderBuildRemoval o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuildRemoval.class))
        {
            if (o.getSchematicLocation().equals(getID()))
            {
                return 0;
            }
        }

        return NO_WORK_ORDER;
    }

    /**
     * Remove the work order for the building.
     * <p>
     * Remove either the upgrade or repair work order
     */
    @Override
    public void removeWorkOrder()
    {
        for (@NotNull final WorkOrderBuild o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuild.class))
        {
            if (o.getSchematicLocation().equals(getID()) && (o instanceof WorkOrderBuildBuilding || o instanceof WorkOrderBuildRemoval))
            {
                colony.getWorkManager().removeWorkOrder(o.getID());
                markDirty();

                final BlockPos buildingPos = o.getClaimedBy();
                final IBuilding building = colony.getBuildingManager().getBuilding(buildingPos);
                if (building != null)
                {
                    for (final AbstractAssignedCitizenModule module : building.getModules(AbstractAssignedCitizenModule.class))
                    {
                        for (final ICitizenData citizen : module.getAssignedCitizen())
                        {
                            building.cancelAllRequestsOfCitizen(citizen);
                        }
                    }
                }
                return;
            }
        }
    }

    @Override
    public Set<ICitizenData> getAllAssignedCitizen()
    {
        final Set<ICitizenData> citizens = new HashSet<>();
        for (final AbstractAssignedCitizenModule module : getModules(AbstractAssignedCitizenModule.class))
        {
            citizens.addAll(module.getAssignedCitizen());
        }
        return citizens;
    }

    /**
     * Method to calculate the radius to be claimed by this building depending on the level.
     *
     * @param newLevel the new level of the building.
     * @return the radius.
     */
    @Override
    public int getClaimRadius(final int newLevel)
    {
        switch (newLevel)
        {
            case 1:
            case 2:
            case 3:
                return 1;
            case 4:
            case 5:
                return 2;
            default:
                return 0;
        }
    }

    /**
     * Serializes to view.
     *
     * @param buf PacketBuffer to write to.
     */
    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        buf.writeUtf(this.getBuildingType().getRegistryName().toString());
        buf.writeInt(getBuildingLevel());
        buf.writeInt(getMaxBuildingLevel());
        buf.writeInt(getPickUpPriority());
        buf.writeInt(getCurrentWorkOrderLevel());
        buf.writeUtf(getStyle());
        buf.writeUtf(this.getSchematicName());
        buf.writeBlockPos(getParent());
        buf.writeUtf(this.customName);

        buf.writeInt(getRotation());
        buf.writeBoolean(isMirrored());
        buf.writeInt(getClaimRadius(getBuildingLevel()));

        final CompoundNBT requestSystemCompound = new CompoundNBT();
        writeRequestSystemToNBT(requestSystemCompound);

        final ImmutableCollection<IRequestResolver<?>> resolvers = getResolvers();
        buf.writeInt(resolvers.size());
        for (final IRequestResolver<?> resolver : resolvers)
        {
            buf.writeNbt(StandardFactoryController.getInstance().serialize(resolver.getId()));
        }
        buf.writeNbt(StandardFactoryController.getInstance().serialize(getId()));
        buf.writeInt(containerList.size());
        for (BlockPos blockPos : containerList)
        {
            buf.writeBlockPos(blockPos);
        }
        buf.writeNbt(requestSystemCompound);

        buf.writeBoolean(isDeconstructed());

        getModules(IPersistentModule.class).forEach(module -> module.serializeToView(buf));
    }



    /**
     * Regularly tick this building and check if we  got the minimum stock(like once a minute is still fine) - If not: Check if there is a request for this already. -- If not:
     * Create a request. - If so: Check if there is a request for this still. -- If so: cancel it.
     */
    @Override
    public void onColonyTick(final IColony colony)
    {
        getModules(ITickingModule.class).forEach(module -> module.onColonyTick(colony));
    }

    /**
     * If an incoming request is a minimum stock request.
     *
     * @param request the request to check.
     * @return true if so.
     */
    public boolean isMinimumStockRequest(final IRequest<? extends IDeliverable> request)
    {
        for (final IMinimumStockModule module : getModules(IMinimumStockModule.class))
        {
            if (module.isMinimumStockRequest(request))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Set the custom building name of the building.
     *
     * @param name the name to set.
     */
    @Override
    public void setCustomBuildingName(final String name)
    {
        this.customName = name;
        this.markDirty();
    }

    /**
     * Check if the building should be gathered by the dman.
     *
     * @return true if so.
     */
    @Override
    public boolean canBeGathered()
    {
        return true;
    }

    /**
     * Requests an upgrade for the current building.
     *
     * @param player  the requesting player.
     * @param builder the assigned builder.
     */
    @Override
    public void requestUpgrade(final PlayerEntity player, final BlockPos builder)
    {
        final ResourceLocation hutResearch = colony.getResearchManager().getResearchEffectIdFrom(this.getBuildingType().getBuildingBlock());

        if (MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().hasResearchEffect(hutResearch) && colony.getResearchManager().getResearchEffects().getEffectStrength(hutResearch) < 1)
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.havetounlock"), player.getUUID());
            return;
        }
        if(MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().hasResearchEffect(hutResearch) && (colony.getResearchManager().getResearchEffects().getEffectStrength(hutResearch) <= getBuildingLevel()))
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.unlocktoupgrade"), player.getUUID());
            return;
        }

        final IBuilding parentBuilding = colony.getBuildingManager().getBuilding(getParent());

        if (getBuildingLevel() < getMaxBuildingLevel() && (parentBuilding == null || getBuildingLevel() < parentBuilding.getBuildingLevel()))
        {
            requestWorkOrder(getBuildingLevel() + 1, builder, false);
        }
        else
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.worker.noupgrade"), player.getUUID());
        }
    }

    @Override
    public void requestRemoval(final PlayerEntity player, final BlockPos builder)
    {
        if (this.isDeconstructed())
        {
            pickUp(player);
        }
        else
        {
            requestWorkOrder(getBuildingLevel(), builder, true);
        }
    }

    @Override
    public void pickUp(final PlayerEntity player)
    {
        if (hasParent())
        {
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.gui.workerhuts.pickup.denied");
            return;
        }

        final ItemStack stack = new ItemStack(colony.getWorld().getBlockState(getPosition()).getBlock(), 1);
        final CompoundNBT compoundNBT = new CompoundNBT();
        compoundNBT.putInt(TAG_COLONY_ID, this.colony.getID());
        compoundNBT.putInt(TAG_OTHER_LEVEL, this.getBuildingLevel());
        stack.setTag(compoundNBT);
        if (InventoryUtils.addItemStackToProvider(player, stack))
        {
            this.destroy();
            colony.getWorld().destroyBlock(this.getPosition(), false);
        }
        else
        {
            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.playerinvfull");
        }
    }

    /**
     * Requests a repair for the current building.
     *
     * @param builder the assigned builder.
     */
    @Override
    public void requestRepair(final BlockPos builder)
    {
        if (getBuildingLevel() > 0)
        {
            requestWorkOrder(getBuildingLevel(), builder, false);
        }
    }

    /**
     * Check if the building was built already.
     *
     * @return true if so.
     */
    @Override
    public boolean isBuilt()
    {
        return isBuilt;
    }

    /**
     * Deconstruct the building on destroyed.
     */
    @Override
    public void deconstruct()
    {
        final Tuple<BlockPos, BlockPos> tuple = getCorners();
        for (int x = tuple.getA().getX(); x < tuple.getB().getX(); x++)
        {
            for (int z = tuple.getA().getZ(); z < tuple.getB().getZ(); z++)
            {
                for (int y = tuple.getA().getY(); y < tuple.getB().getY(); y++)
                {
                    colony.getWorld().destroyBlock(new BlockPos(x, y, z), false);
                }
            }
        }
    }

    @Override
    public AbstractTileEntityColonyBuilding getTileEntity()
    {
        if (tileEntity != null && tileEntity.isRemoved())
        {
            tileEntity = null;
        }

        if ((tileEntity == null)
              && colony != null
              && colony.getWorld() != null
              && getPosition() != null
              && WorldUtil.isBlockLoaded(colony.getWorld(), getPosition())
              && !(colony.getWorld().getBlockState(getPosition()).getBlock() instanceof AirBlock)
              && colony.getWorld().getBlockState(this.getPosition()).getBlock() instanceof AbstractBlockHut)
        {
            final TileEntity te = colony.getWorld().getBlockEntity(getPosition());
            if (te instanceof TileEntityColonyBuilding)
            {
                tileEntity = (TileEntityColonyBuilding) te;
                if (tileEntity.getBuilding() == null)
                {
                    tileEntity.setColony(colony);
                    tileEntity.setBuilding(this);
                }
            }
            else
            {
                Log.getLogger().error("Somehow the wrong TileEntity is at the location where the building should be!", new Exception());
                Log.getLogger().error("Trying to restore order!");

                final AbstractTileEntityColonyBuilding tileEntityColonyBuilding = new TileEntityColonyBuilding(MinecoloniesTileEntities.BUILDING);
                colony.getWorld().setBlockEntity(getPosition(), tileEntityColonyBuilding);
                this.tileEntity = tileEntityColonyBuilding;
            }
        }

        return tileEntity;
    }

    /**
     * Called upon completion of an upgrade process. We suppress this warning since this parameter will be used in child classes which override this method.
     *
     * @param newLevel The new level.
     */
    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        cachedRotation = -1;
        ChunkDataHelper.claimColonyChunks(colony, true, this.getID(), this.getClaimRadius(newLevel));
        recheckGuardBuildingNear = true;

        ConstructionTapeHelper.removeConstructionTape(getCorners(), colony.getWorld());
        colony.getProgressManager().progressBuildBuilding(this,
          colony.getBuildingManager().getBuildings().values().stream()
            .filter(building -> building.hasModule(WorkerBuildingModule.class)).mapToInt(ISchematicProvider::getBuildingLevel).sum(),
          colony.getBuildingManager().getBuildings().values().stream()
            .filter(building -> building.hasModule(LivingBuildingModule.class)).mapToInt(ISchematicProvider::getBuildingLevel).sum()
        );
        calculateCorners();
        this.isBuilt = true;

        if (newLevel > getBuildingLevel())
        {
            FireworkUtils.spawnFireworksAtAABBCorners(getCorners(), colony.getWorld(), newLevel);
        }

        getModules(IBuildingEventsModule.class).forEach(module -> module.onUpgradeComplete(newLevel));
        colony.getResearchManager().checkAutoStartResearch();
    }

    @Override
    public void calculateCorners()
    {
        final AbstractTileEntityColonyBuilding te = getTileEntity();
        if (te != null && !te.getSchematicName().isEmpty())
        {
            setCorners(te.getInWorldCorners().getA(), te.getInWorldCorners().getB());
            return;
        }

        final WorkOrderBuildBuilding workOrder = new WorkOrderBuildBuilding(this, Math.max(1, getBuildingLevel()));
        final LoadOnlyStructureHandler wrapper = new LoadOnlyStructureHandler(colony.getWorld(), getPosition(), workOrder.getStructureName(), new PlacementSettings(), true);
        if (!wrapper.hasBluePrint())
        {
            setCorners(getPosition(), getPosition());
            return;
        }

        final Tuple<BlockPos, BlockPos> corners
          = ColonyUtils.calculateCorners(this.getPosition(),
          colony.getWorld(),
          wrapper.getBluePrint(),
          workOrder.getRotation(colony.getWorld()),
          workOrder.isMirrored());
        this.setCorners(corners.getA(), corners.getB());
    }

    @Override
    public boolean isGuardBuildingNear()
    {
        if (recheckGuardBuildingNear)
        {
            guardBuildingNear = colony.getBuildingManager().hasGuardBuildingNear(this);
            recheckGuardBuildingNear = false;
        }
        return guardBuildingNear;
    }

    @Override
    public void resetGuardBuildingNear()
    {
        this.recheckGuardBuildingNear = true;
    }

    //------------------------- Starting Required Tools/Item handling -------------------------//

    /**
     * Check if the worker requires a certain amount of that item and the alreadykept list contains it. Always leave one stack behind if the worker requires a certain amount of it.
     * Just to be sure.
     *
     * @param stack            the stack to check it with.
     * @param localAlreadyKept already kept items.
     * @param inventory        if it should be in the inventory or in the building.
     * @return the amount which can get dumped or 0 if not.
     */
    @Override
    public int buildingRequiresCertainAmountOfItem(final ItemStack stack, final List<ItemStorage> localAlreadyKept, final boolean inventory)
    {
        for (final Map.Entry<Predicate<ItemStack>, Tuple<Integer, Boolean>> entry : getRequiredItemsAndAmount().entrySet())
        {
            if (inventory && !entry.getValue().getB())
            {
                continue;
            }

            if (entry.getKey().test(stack))
            {
                final ItemStorage kept = ItemStorage.getItemStackOfListMatchingPredicate(localAlreadyKept, entry.getKey());
                final int toKeep = entry.getValue().getA();
                int rest = stack.getCount() - toKeep;
                if (kept != null)
                {
                    if (kept.getAmount() >= toKeep && !ItemStackUtils.isBetterTool(stack, kept.getItemStack()))
                    {
                        return stack.getCount();
                    }

                    rest = kept.getAmount() + stack.getCount() - toKeep;

                    localAlreadyKept.remove(kept);
                    kept.setAmount(kept.getAmount() + ItemStackUtils.getSize(stack) - Math.max(0, rest));
                    localAlreadyKept.add(kept);
                }
                else
                {
                    final ItemStorage newStorage = new ItemStorage(stack);
                    newStorage.setAmount(ItemStackUtils.getSize(stack) - Math.max(0, rest));
                    localAlreadyKept.add(newStorage);
                }

                if (rest <= 0)
                {
                    return 0;
                }

                return Math.min(rest, ItemStackUtils.getSize(stack));
            }
        }
        return stack.getCount();
    }

    /**
     * Override this method if you want to keep an amount of items in inventory. When the inventory is full, everything get's dumped into the building chest. But you can use this
     * method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = new HashMap<>(keepX);

        final Map<ItemStorage, Tuple<Integer, Boolean>> requiredItems = new HashMap<>();
        final Collection<IRequestResolver<?>> resolvers = getResolvers();
        for (final IRequestResolver<?> resolver : resolvers)
        {
            final IStandardRequestManager requestManager = (IStandardRequestManager) colony.getRequestManager();
            final List<IRequest<? extends IDeliverable>> deliverableRequests =
                requestManager.getRequestHandler().getRequestsMadeByRequester(resolver)
                    .stream()
                    .filter(iRequest -> iRequest.getRequest() instanceof IDeliverable)
                    .map(iRequest -> (IRequest<? extends IDeliverable>) iRequest)
                    .collect(Collectors.toList());
            for(IRequest<? extends IDeliverable> request: deliverableRequests)
            {
                for(ItemStack item : request.getDeliveries())
                {
                    final ItemStorage output = new ItemStorage(item);
                    int amount = output.getAmount();
                    if (requiredItems.containsKey(output))
                    {
                        amount += requiredItems.get(output).getA();
                    }
                    requiredItems.put(output, new Tuple<>(amount, false));
                }
            }
        }
        toKeep.putAll(requiredItems.entrySet().stream().collect(Collectors.toMap(key -> (stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, key.getKey().getItemStack())), Map.Entry::getValue)));

        if (keepFood())
        {
            toKeep.put(stack -> ItemStackUtils.CAN_EAT.test(stack) && canEat(stack), new Tuple<>(getBuildingLevel() * 2, true));
        }
        for (final IHasRequiredItemsModule module : getModules(IHasRequiredItemsModule.class))
        {
            toKeep.putAll(module.getRequiredItemsAndAmount());
        }

        getModules(IAltersRequiredItems.class).forEach(module -> module.alterItemsToBeKept((stack, qty, inv) -> toKeep.put(stack, new Tuple<>(qty,inv))));
        return toKeep;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        return stack.getItem().getFoodProperties().getNutrition() >= getBuildingLevel();
    }


    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public List<IItemHandler> getHandlers()
    {
        if (this.getAllAssignedCitizen().isEmpty() || colony == null || colony.getWorld() == null)
        {
            return Collections.emptyList();
        }

        final Set<IItemHandler> handlers = new HashSet<>();
        for (final ICitizenData workerEntity : this.getAllAssignedCitizen())
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

    /**
     * If the worker should keep some food in the inventory/building.
     * @return true if so.
     */
    protected boolean keepFood()
    {
        return true;
    }

    /**
     * Try to transfer a stack to one of the inventories of the building and force the transfer.
     *
     * @param stack the stack to transfer.
     * @param world the world to do it in.
     * @return the itemStack which has been replaced or the itemStack which could not be transfered
     */
    @Override
    @Nullable
    public ItemStack forceTransferStack(final ItemStack stack, final World world)
    {
        if (getTileEntity() == null)
        {
            for (final BlockPos pos : containerList)
            {
                final TileEntity tempTileEntity = world.getBlockEntity(pos);
                if (tempTileEntity instanceof ChestTileEntity && !InventoryUtils.isProviderFull(tempTileEntity))
                {
                    return forceItemStackToProvider(tempTileEntity, stack);
                }
            }
        }
        else
        {
            return forceItemStackToProvider(getTileEntity(), stack);
        }
        return stack;
    }

    @Nullable
    private ItemStack forceItemStackToProvider(@NotNull final ICapabilityProvider provider, @NotNull final ItemStack itemStack)
    {
        final List<ItemStorage> localAlreadyKept = new ArrayList<>();
        return InventoryUtils.forceItemStackToProvider(provider,
          itemStack,
          (ItemStack stack) -> EntityAIWorkDeliveryman.workerRequiresItem(this, stack, localAlreadyKept) != stack.getCount());
    }

    //------------------------- Ending Required Tools/Item handling -------------------------//

    //------------------------- !START! RequestSystem handling for minecolonies buildings -------------------------//

    @Override
    public boolean isItemStackInRequest(@Nullable final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return false;
        }

        for (final AbstractAssignedCitizenModule module : getModules(AbstractAssignedCitizenModule.class))
        {
            for (final ICitizenData citizen : module.getAssignedCitizen())
            {
                for (final IRequest<?> request : getOpenRequests(citizen.getId()))
                {
                    for (final ItemStack deliveryStack : request.getDeliveries())
                    {
                        if (ItemStackUtils.compareItemStacksIgnoreStackSize(deliveryStack, stack, false, true))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    protected void writeRequestSystemToNBT(final CompoundNBT compound)
    {
        compound.put(TAG_REQUESTOR_ID, StandardFactoryController.getInstance().serialize(requester));
        compound.put(TAG_RS_BUILDING_DATASTORE, StandardFactoryController.getInstance().serialize(rsDataStoreToken));
    }

    protected void setupRsDataStore()
    {
        this.rsDataStoreToken = colony.getRequestManager()
                                  .getDataStoreManager()
                                  .get(
                                    StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                                    TypeConstants.REQUEST_SYSTEM_BUILDING_DATA_STORE
                                  )
                                  .getId();
    }

    private void loadRequestSystemFromNBT(final CompoundNBT compound)
    {
        if (compound.getAllKeys().contains(TAG_REQUESTOR_ID))
        {
            this.requester = StandardFactoryController.getInstance().deserialize(compound.getCompound(TAG_REQUESTOR_ID));
        }
        else
        {
            this.requester = StandardFactoryController.getInstance().getNewInstance(TypeToken.of(BuildingBasedRequester.class), this);
        }

        if (compound.getAllKeys().contains(TAG_RS_BUILDING_DATASTORE))
        {
            this.rsDataStoreToken = StandardFactoryController.getInstance().deserialize(compound.getCompound(TAG_RS_BUILDING_DATASTORE));
        }
        else
        {
            setupRsDataStore();
        }
    }

    private IRequestSystemBuildingDataStore getDataStore()
    {
        return colony.getRequestManager().getDataStoreManager().get(rsDataStoreToken, TypeConstants.REQUEST_SYSTEM_BUILDING_DATA_STORE);
    }

    @Override
    public Map<TypeToken<?>, Collection<IToken<?>>> getOpenRequestsByRequestableType()
    {
        return getDataStore().getOpenRequestsByRequestableType();
    }

    protected Map<Integer, Collection<IToken<?>>> getOpenRequestsByCitizen()
    {
        return getDataStore().getOpenRequestsByCitizen();
    }

    private Map<Integer, Collection<IToken<?>>> getCompletedRequestsByCitizen()
    {
        return getDataStore().getCompletedRequestsByCitizen();
    }

    private Map<IToken<?>, Integer> getCitizensByRequest()
    {
        return getDataStore().getCitizensByRequest();
    }

    /**
     * Create a request for a citizen.
     *
     * @param citizenData the data of the citizen.
     * @param requested   the request to create.
     * @param async       if async or not.
     * @param <R>         the type of the request.
     * @return the Token of the request.
     */
    @Override
    public <R extends IRequestable> IToken<?> createRequest(@NotNull final ICitizenData citizenData, @NotNull final R requested, final boolean async)
    {
        final IToken<?> requestToken = colony.getRequestManager().createRequest(requester, requested);
        final IRequest<?> request = colony.getRequestManager().getRequestForToken(requestToken);

        if (async)
        {
            citizenData.getJob().getAsyncRequests().add(requestToken);
            citizenData.triggerInteraction(new RequestBasedInteraction(new TranslationTextComponent(ASYNC_REQUEST,
              request.getShortDisplayString()), ChatPriority.PENDING, new TranslationTextComponent(NORMAL_REQUEST), request.getId()));
        }
        else
        {
            citizenData.triggerInteraction(new RequestBasedInteraction(new TranslationTextComponent(NORMAL_REQUEST,
              request.getShortDisplayString()), ChatPriority.BLOCKING, new TranslationTextComponent(NORMAL_REQUEST), request.getId()));
        }

        addRequestToMaps(citizenData.getId(), requestToken, TypeToken.of(requested.getClass()));

        colony.getRequestManager().assignRequest(requestToken);

        markDirty();

        return requestToken;
    }

    /**
     * Create a request for the building.
     *
     * @param requested the request to create.
     * @param async     if async or not.
     * @param <R>       the type of the request.
     * @return the Token of the request.
     */
    @Override
    public <R extends IRequestable> IToken<?> createRequest(@NotNull final R requested, final boolean async)
    {
        final IToken<?> requestToken = colony.getRequestManager().createRequest(requester, requested);
        addRequestToMaps(-1, requestToken, TypeToken.of(requested.getClass()));

        colony.getRequestManager().assignRequest(requestToken);

        markDirty();

        return requestToken;
    }

    @Override
    public void registerBlockPosition(@NotNull final BlockState blockState, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(blockState, pos, world);
        getModules(IModuleWithExternalBlocks.class).forEach(module -> module.onBlockPlacedInBuilding(blockState, pos, world));
    }

    /**
     * Internal method used to register a new Request to the request maps. Helper method.
     *
     * @param citizenId    The id of the citizen.
     * @param requestToken The {@link IToken} that is used to represent the request.
     * @param requested    The class of the type that has been requested eg. {@code ItemStack.class}
     */
    private void addRequestToMaps(final int citizenId, @NotNull final IToken<?> requestToken, @NotNull final TypeToken<?> requested)
    {
        if (!getOpenRequestsByRequestableType().containsKey(requested))
        {
            getOpenRequestsByRequestableType().put(requested, new ArrayList<>());
        }
        getOpenRequestsByRequestableType().get(requested).add(requestToken);

        getCitizensByRequest().put(requestToken, citizenId);

        if (!getOpenRequestsByCitizen().containsKey(citizenId))
        {
            getOpenRequestsByCitizen().put(citizenId, new ArrayList<>());
        }
        getOpenRequestsByCitizen().get(citizenId).add(requestToken);
    }

    @Override
    public boolean hasWorkerOpenRequests(final int citizenId)
    {
        return getOpenRequestsByCitizen().containsKey(citizenId) && !getOpenRequestsByCitizen().get(citizenId).isEmpty();
    }

    @Override
    public Collection<IRequest<?>> getOpenRequests(final int citizenId)
    {
        if (!getOpenRequestsByCitizen().containsKey(citizenId))
        {
            return ImmutableList.of();
        }

        final Collection<IToken<?>> tokens = getOpenRequestsByCitizen().get(citizenId);
        final List<IRequest<?>> requests = new ArrayList<>(tokens.size());

        for (final IToken<?> token : tokens)
        {
            final IRequest<?> request = colony.getRequestManager().getRequestForToken(token);
            if (request != null)
            {
                requests.add(request);
            }
        }

        return Collections.unmodifiableList(requests);
    }

    @Override
    public boolean hasWorkerOpenRequestsFiltered(final int citizenId, @NotNull final Predicate<IRequest<?>> selectionPredicate)
    {
        return getOpenRequests(citizenId).stream().anyMatch(selectionPredicate);
    }

    @Override
    public boolean hasOpenSyncRequest(@NotNull final ICitizenData citizen)
    {
        if (!hasWorkerOpenRequests(citizen.getId()))
        {
            return false;
        }

        for (final IToken<?> token : getOpenRequestsByCitizen().get(citizen.getId()))
        {
            if (!citizen.isRequestAsync(token))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public <R> boolean hasWorkerOpenRequestsOfType(final int citizenId, final TypeToken<R> requestType)
    {
        return !getOpenRequestsOfType(citizenId, requestType).isEmpty();
    }

    @Override
    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfType(
      final int citizenId,
      final TypeToken<R> requestType)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenId).stream()
                                      .filter(request ->  request.getType().isSubtypeOf(requestType))
                                      .map(request -> (IRequest<? extends R>) request)
                                      .iterator());
    }

    @Override
    public boolean createPickupRequest(final int scaledPriority)
    {
        if (scaledPriority < 0 || scaledPriority > getPlayerActionPriority(true))
        {
            return false;
        }

        final List<IToken<?>> reqs = new ArrayList<>(getOpenRequestsByRequestableType().getOrDefault(TypeConstants.PICKUP, Collections.emptyList()));
        if (!reqs.isEmpty())
        {
            for (final IToken<?> req : reqs)
            {
                final IRequest<?> request = colony.getRequestManager().getRequestForToken(req);
                if (request != null && request.getState() == RequestState.IN_PROGRESS)
                {
                    final IRequestResolver<?> resolver = colony.getRequestManager().getResolverForRequest(req);
                    if (resolver instanceof IPlayerRequestResolver || resolver instanceof IRetryingRequestResolver)
                    {
                        colony.getRequestManager().reassignRequest(req, Collections.emptyList());
                    }
                }
            }
            return false;
        }

        createRequest(new Pickup(scaledPriority), true);
        return true;
    }

    @Override
    public boolean hasCitizenCompletedRequests(@NotNull final ICitizenData data)
    {
        return getCompletedRequestsByCitizen().containsKey(data.getId()) && !getCompletedRequestsByCitizen().get(data.getId()).isEmpty();
    }

    @Override
    public boolean hasCitizenCompletedRequestsToPickup(@NotNull final ICitizenData data)
    {
        if (!getCompletedRequestsByCitizen().containsKey(data.getId()))
        {
            return false;
        }

        for (IToken<?> token : getCompletedRequestsByCitizen().get(data.getId()))
        {
            if (!data.isRequestAsync(token))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public Collection<IRequest<?>> getCompletedRequests(@NotNull final ICitizenData data)
    {
        final Collection<IToken<?>> tokens = getCompletedRequestsByCitizen().get(data.getId());
        if (tokens == null || tokens.isEmpty())
        {
            return ImmutableList.of();
        }

        final List<IRequest<?>> requests = new ArrayList<>(tokens.size());

        for (final IToken<?> token : tokens)
        {
            final IRequest<?> request = colony.getRequestManager().getRequestForToken(token);
            if (request != null)
            {
                requests.add(request);
            }
            else
            {
                getCompletedRequestsByCitizen().get(data.getId()).remove(token);
                if (getCompletedRequestsByCitizen().get(data.getId()).isEmpty())
                {
                    getCompletedRequestsByCitizen().remove(data.getId());
                }
            }
        }

        return Collections.unmodifiableList(requests);
    }

    @Override
    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfType(@NotNull final ICitizenData citizenData, final TypeToken<R> requestType)
    {
        return ImmutableList.copyOf(getCompletedRequests(citizenData).stream()
                                      .filter(request ->  request.getType().isSubtypeOf(requestType))
                                      .map(request -> (IRequest<? extends R>) request)
                                      .iterator());
    }

    @Override
    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfTypeFiltered(
      @NotNull final ICitizenData citizenData,
      final TypeToken<R> requestType,
      final Predicate<IRequest<? extends R>> filter)
    {
        return ImmutableList.copyOf(getCompletedRequests(citizenData).stream()
                                      .filter(request ->  request.getType().isSubtypeOf(requestType))
                                      .map(request -> (IRequest<? extends R>) request)
                                      .filter(filter)
                                      .iterator());
    }

    @Override
    public void markRequestAsAccepted(@NotNull final ICitizenData data, @NotNull final IToken<?> token)
    {
        if (!getCompletedRequestsByCitizen().containsKey(data.getId()) || !getCompletedRequestsByCitizen().get(data.getId()).contains(token))
        {
            throw new IllegalArgumentException("The given token " + token + " is not known as a completed request waiting for acceptance by the citizen.");
        }

        getCompletedRequestsByCitizen().get(data.getId()).remove(token);
        if (getCompletedRequestsByCitizen().get(data.getId()).isEmpty())
        {
            getCompletedRequestsByCitizen().remove(data.getId());
        }

        colony.getRequestManager().updateRequestState(token, RequestState.RECEIVED);
        markDirty();
    }

    @Override
    public void cancelAllRequestsOfCitizen(@NotNull final ICitizenData data)
    {
        getOpenRequests(data.getId()).forEach(request ->
        {
            colony.getRequestManager().updateRequestState(request.getId(), RequestState.CANCELLED);

            if (getOpenRequestsByRequestableType().containsKey(TypeToken.of(request.getRequest().getClass())))
            {
                getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).remove(request.getId());
                if (getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).isEmpty())
                {
                    getOpenRequestsByRequestableType().remove(TypeToken.of(request.getRequest().getClass()));
                }
            }

            getCitizensByRequest().remove(request.getId());
        });

        getCompletedRequests(data).forEach(request -> colony.getRequestManager().updateRequestState(request.getId(), RequestState.RECEIVED));

        getOpenRequestsByCitizen().remove(data.getId());

        getCompletedRequestsByCitizen().remove(data.getId());

        markDirty();
    }

    /**
     * Overrule the next open request with a give stack.
     * <p>
     * We squid:s135 which takes care that there are not too many continue statements in a loop since it makes sense here out of performance reasons.
     *
     * @param stack the stack.
     */
    @Override
    @SuppressWarnings("squid:S135")
    public void overruleNextOpenRequestWithStack(@NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return;
        }

        final Collection<IRequestResolver<?>> resolvers = getResolvers();
        final List<IToken<?>> playerRequests = colony.getRequestManager().getPlayerResolver().getAllAssignedRequests();
        final List<IToken<?>> retryingRequests = colony.getRequestManager().getRetryingRequestResolver().getAllAssignedRequests();


        for (final IRequestResolver<?> resolver : resolvers)
        {
            final IStandardRequestManager requestManager = (IStandardRequestManager) colony.getRequestManager();

            final List<IRequest<? extends IDeliverable>> deliverableRequests =
              requestManager.getRequestHandler().getRequestsMadeByRequester(resolver)
                .stream()
                .filter(iRequest -> iRequest.getRequest() instanceof IDeliverable)
                .map(iRequest -> (IRequest<? extends IDeliverable>) iRequest)
                .collect(Collectors.toList());

            final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(deliverableRequests, stack);

            if (target == null || !isRequestStuck(target, playerRequests, retryingRequests))
            {
                continue;
            }

            colony.getRequestManager().overruleRequest(target.getId(), stack.copy());
            return;
        }

        final Set<Integer> citizenIdsWithRequests = getOpenRequestsByCitizen().keySet();
        for (final int citizenId : citizenIdsWithRequests)
        {
            final ICitizenData data = colony.getCitizenManager().getCivilian(citizenId);

            if (data == null)
            {
                continue;
            }

            final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(getOpenRequestsOfType(data.getId(), TypeConstants.DELIVERABLE), stack);

            if (target == null || !isRequestStuck(target, playerRequests, retryingRequests) )
            {
                continue;
            }

            colony.getRequestManager().overruleRequest(target.getId(), stack.copy());
            return;
        }
    }

    /**
     * Check if the request or one of the child requests is stuck in the retrying resolver.
     * @param target the request to check.
     * @return true if stuck.
     */
    private boolean isRequestStuck(final IRequest<?> target, final List<IToken<?>> playerResolverRequests, final List<IToken<?>> retryingRequests)
    {
        if (playerResolverRequests.contains(target.getId())
              || retryingRequests.contains(target.getId()))
        {
            return true;
        }

        for (final IToken<?> child : target.getChildren())
        {
            if (isRequestStuck(colony.getRequestManager().getRequestForToken(child), playerResolverRequests, retryingRequests))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfTypeFiltered(
      @NotNull final ICitizenData citizenData,
      final TypeToken<R> requestType,
      final Predicate<IRequest<? extends R>> filter)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData.getId()).stream()
                                      .filter(request ->  request.getType().isSubtypeOf(requestType))
                                      .map(request -> (IRequest<? extends R>) request)
                                      .filter(filter)
                                      .iterator());
    }

    @Override
    public boolean overruleNextOpenRequestOfCitizenWithStack(@NotNull final ICitizenData citizenData, @NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return false;
        }

        final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(getOpenRequestsOfType(citizenData.getId(), TypeConstants.DELIVERABLE), stack);

        if (target == null)
        {
            if (citizenData.getJob() instanceof AbstractJobCrafter)
            {
                final AbstractJobCrafter<?, ?> crafterJob = citizenData.getJob(AbstractJobCrafter.class);

                if (!crafterJob.getAssignedTasks().isEmpty())
                {
                    final List<IToken<?>> assignedTasks = crafterJob.getAssignedTasks();
                    final IRequest<? extends IDeliverable> deliverableChildRequest = assignedTasks
                                                                                       .stream()
                                                                                       .map(colony.getRequestManager()::getRequestForToken)
                                                                                       .map(IRequest::getChildren)
                                                                                       .flatMap(Collection::stream)
                                                                                       .map(colony.getRequestManager()::getRequestForToken)
                                                                                       .filter(iRequest -> iRequest.getRequest() instanceof IDeliverable)
                                                                                       .filter(iRequest -> ((IRequest<? extends IDeliverable>) iRequest).getRequest()
                                                                                                             .matches(stack))
                                                                                       .findFirst()
                                                                                       .map(iRequest -> (IRequest<? extends IDeliverable>) iRequest)
                                                                                       .orElse(null);

                    if (deliverableChildRequest != null)
                    {
                        deliverableChildRequest.overrideCurrentDeliveries(ImmutableList.of(stack));
                        colony.getRequestManager().overruleRequest(deliverableChildRequest.getId(), stack.copy());
                        return true;
                    }
                }
            }

            return false;
        }

        try
        {
            target.overrideCurrentDeliveries(ImmutableList.of(stack));
            colony.getRequestManager().overruleRequest(target.getId(), stack.copy());
        }
        catch (final Exception ex)
        {
            Log.getLogger().error("Error during overruling", ex);
            Log.getLogger().error(target.getId().toString() + " " + target.getState().name() + " " + target.getShortDisplayString().toString());
            return false;
        }

        return true;
    }

    private IRequest<? extends IDeliverable> getFirstOverullingRequestFromInputList(
      @NotNull final Collection<IRequest<? extends IDeliverable>> queue,
      @NotNull final ItemStack stack)
    {
        if (queue.isEmpty())
        {
            return null;
        }

        List<IToken<?>> validRequesterTokens = Lists.newArrayList();
        validRequesterTokens.add(this.getId());
        this.getResolvers().forEach(iRequestResolver -> validRequesterTokens.add(iRequestResolver.getId()));

        return queue
                 .stream()
                 .filter(request -> request.getState() == RequestState.IN_PROGRESS && validRequesterTokens.contains(request.getRequester().getId()) && request.getRequest()
                                                                                                                                                         .matches(stack))
                 .findFirst()
                 .orElse(null);
    }

    /*
    private Collection<IRequest<? extends IDeliverable>> flattenDeliverableChildRequests(@NotNull final IRequest<? extends IDeliverable> request)
    {
        if (!request.hasChildren())
        {
            return ImmutableList.of();
        }

        return request.getChildren()
                 .stream()
                 .map(colony.getRequestManager()::getRequestForToken)
                 .filter(Objects::nonNull)
                 .filter(request1 -> request1.getRequest() instanceof IDeliverable)
                 .map(request1 -> (IRequest<? extends IDeliverable>) request1)
                 .collect(Collectors.toList());
    }
    */

    @Override
    public IToken<?> getId()
    {
        return requester.getId();
    }

    @Override
    public final ImmutableCollection<IRequestResolver<?>> getResolvers()
    {
        final IStandardRequestManager requestManager = (IStandardRequestManager) colony.getRequestManager();
        if (!requestManager.getProviderHandler().getRegisteredResolvers(this).isEmpty())
        {
            return ImmutableList.copyOf(requestManager.getProviderHandler().getRegisteredResolvers(this)
                                          .stream()
                                          .map(token -> requestManager.getResolverHandler().getResolver(token))
                                          .collect(Collectors.toList()));
        }

        return createResolvers();
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();

        for (final ICreatesResolversModule module : getModules(ICreatesResolversModule.class))
        {
            builder.addAll(module.createResolvers());
        }
        builder.add(new BuildingRequestResolver(getRequester().getLocation(), colony.getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));
        return builder.build();
    }

    @Override
    public IRequester getRequester()
    {
        return requester;
    }

    @NotNull
    @Override
    public ILocation getLocation()
    {
        return getRequester().getLocation();
    }

    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        if (!getCitizensByRequest().containsKey(request.getId()))
        {
            return;
        }
        
        final int citizenThatRequested = getCitizensByRequest().remove(request.getId());

        if (getOpenRequestsByCitizen().containsKey(citizenThatRequested))
        {
            getOpenRequestsByCitizen().get(citizenThatRequested).remove(request.getId());
            if (getOpenRequestsByCitizen().get(citizenThatRequested).isEmpty())
            {
                getOpenRequestsByCitizen().remove(citizenThatRequested);
            }
        }

        getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).remove(request.getId());

        if (getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).isEmpty())
        {
            getOpenRequestsByRequestableType().remove(TypeToken.of(request.getRequest().getClass()));
        }

        if (citizenThatRequested >= 0)
        {
            getCompletedRequestsByCitizen().computeIfAbsent(citizenThatRequested, ArrayList::new).add(request.getId());
        }
        else
        {
            colony.getRequestManager().updateRequestState(request.getId(), RequestState.RECEIVED);
        }

        markDirty();
    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        final int citizenThatRequested = getCitizensByRequest().remove(request.getId());
        final Map<Integer, Collection<IToken<?>>> openRequestsByCitizen = getOpenRequestsByCitizen();
        final Collection<IToken<?>> byCitizenList = openRequestsByCitizen.get(citizenThatRequested);
        if (byCitizenList != null)
        {
            byCitizenList.remove(request.getId());
            if (byCitizenList.isEmpty())
            {
                openRequestsByCitizen.remove(citizenThatRequested);
            }
        }

        if (getOpenRequestsByRequestableType().containsKey(TypeToken.of(request.getRequest().getClass())))
        {
            getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).remove(request.getId());
            if (getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).isEmpty())
            {
                getOpenRequestsByRequestableType().remove(TypeToken.of(request.getRequest().getClass()));
            }
        }

        //Check if the citizen did not die.
        if (colony.getCitizenManager().getCivilian(citizenThatRequested) != null)
        {
            colony.getCitizenManager().getCivilian(citizenThatRequested).onRequestCancelled(request.getId());
        }
        markDirty();
    }

    @NotNull
    @Override
    public IFormattableTextComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        final int citizenId = getCitizensByRequest().get(request.getId());
        if (!getCitizensByRequest().containsKey(citizenId))
        {
            return new StringTextComponent("<UNKNOWN>");
        }

        final ICitizenData citizenData = colony.getCitizenManager().getCivilian(citizenId);
        if (citizenData.getJob() == null)
        {
            return new StringTextComponent(citizenData.getName());
        }
        final IFormattableTextComponent jobName =  new TranslationTextComponent(citizenData.getJob().getJobRegistryEntry().getTranslationKey().toLowerCase());
        return jobName.append(new StringTextComponent(" " + citizenData.getName()));
    }

    @Override
    public Optional<ICitizenData> getCitizenForRequest(@NotNull final IToken<?> token)
    {
        if (!getCitizensByRequest().containsKey(token) || colony == null)
        {
            return Optional.empty();
        }

        final int citizenID = getCitizensByRequest().get(token);
        if (citizenID == -1 || colony.getCitizenManager().getCivilian(citizenID) == null)
        {
            return Optional.empty();
        }

        return Optional.of(colony.getCitizenManager().getCivilian(citizenID));
    }

    @Override
    public Map<ItemStorage, Integer> reservedStacks()
    {
        final Map<ItemStorage, Integer> map = new HashMap<>();
        for (final IHasRequiredItemsModule module : getModules(IHasRequiredItemsModule.class))
        {
            for (final Map.Entry<ItemStorage, Integer> content : module.reservedStacks().entrySet())
            {
                final int current = map.getOrDefault(content.getKey(), 0);
                map.put(content.getKey(), current + content.getValue());
            }
        }
        return map;
    }

    //------------------------- !END! RequestSystem handling for minecolonies buildings -------------------------//
}
