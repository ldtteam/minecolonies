package com.minecolonies.core.colony.buildings.views;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemBuildingDataStore;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.tileentities.storageblocks.AbstractStorageBlock;
import com.minecolonies.api.tileentities.storageblocks.ModStorageBlocks;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.WindowHutMinPlaceholder;
import com.minecolonies.core.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.core.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.core.network.messages.server.colony.OpenInventoryMessage;
import com.minecolonies.core.network.messages.server.colony.building.HutRenameMessage;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.NO_WORK_ORDER;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_RS_BUILDING_DATASTORE;
import static com.minecolonies.api.util.constant.Suppression.GENERIC_WILDCARD;
import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;

/**
 * The AbstractBuilding View is the client-side representation of a AbstractBuilding. Views contain the AbstractBuilding's data that is relevant to a Client, in a more
 * client-friendly form. Mutable operations on a View result in a message to the server to perform the operation.
 */
public abstract class AbstractBuildingView implements IBuildingView
{
    /**
     * The colony of the building.
     */
    private final IColonyView colony;

    /**
     * It's location.
     */
    @NotNull
    private final BlockPos location;

    /**
     * Parent building location.
     */
    @NotNull
    private BlockPos parent = BlockPos.ZERO;

    /**
     * The building level.
     */
    private int buildingLevel = 0;

    /**
     * The max building level.
     */
    private int buildingMaxLevel = 0;

    /**
     * The dm priority.
     */
    private int buildingDmPrio = 1;

    /**
     * Rotation of the building.
     */
    private int rotation;

    /**
     * Mirror of the building.
     */
    private boolean isBuildingMirrored;

    /**
     * The workOrderLevel.
     */
    private int workOrderLevel = NO_WORK_ORDER;

    /**
     * Resolver collection.
     */
    private ImmutableCollection<IToken<?>> resolvers;

    /**
     * Requester ID.
     */
    private IToken<?> requesterId;

    /**
     * The data store id for request system related data.
     */
    private IToken<?> rsDataStoreToken;

    /**
     * The Schematic blueprint path of the building.
     */
    private String path;

    /**
     * The structure pack of the building.
     */
    private String pack;

    /**
     * The custom name of the building.
     */
    private String customName = "";

    /**
     * The claim radius.
     */
    private int claimRadius = 0;

    /**
     * The BlockPos list of all Containers
     */

    private List<AbstractStorageBlock> containerlist = new ArrayList<>();

    /**
     * If building is deconstructed.
     */
    private boolean isDeconstructed;

    /**
     * If citizen assignment is permitted.
     */
    private boolean isAssignmentAllowed;

    /**
     * Set of building modules this building has.
     */
    protected Int2ObjectLinkedOpenHashMap<IBuildingModuleView> moduleViews = new Int2ObjectLinkedOpenHashMap<>();

    /**
     * Building type
     */
    private BuildingEntry buildingType;

    /**
     * Creates a building view.
     *
     * @param c ColonyView the building is in.
     * @param l The location of the building.
     */
    protected AbstractBuildingView(final IColonyView c, @NotNull final BlockPos l)
    {
        colony = c;
        location = new BlockPos(l);
    }

    /**
     * Gets the id for this building.
     *
     * @return A BlockPos because the building ID is its location.
     */
    @Override
    @NotNull
    public BlockPos getID()
    {
        // Location doubles as ID
        return location;
    }

    /**
     * Gets the location of this building.
     *
     * @return A BlockPos, where this building is.
     */
    @Override
    @NotNull
    public BlockPos getPosition()
    {
        return location;
    }

    @Override
    @NotNull
    public BlockPos getParent()
    {
        return parent;
    }

    /**
     * Get the current level of the building.
     *
     * @return AbstractBuilding current level.
     */
    @Override
    public int getBuildingLevel()
    {
        return buildingLevel;
    }

    /**
     * Get the max level of the building.
     *
     * @return AbstractBuilding max level.
     */
    @Override
    public int getBuildingMaxLevel()
    {
        return buildingMaxLevel;
    }

    /**
     * Checks if this building is at its max level.
     *
     * @return true if the building is at its max level.
     */
    @Override
    public boolean isBuildingMaxLevel()
    {
        return buildingLevel >= buildingMaxLevel;
    }

    /**
     * Get the current work order level.
     *
     * @return 0 if none, othewise the current level worked on
     */
    @Override
    public int getCurrentWorkOrderLevel()
    {
        return workOrderLevel;
    }

    /**
     * Getter for the schematic name.
     *
     * @return the schematic name.
     */
    @Override
    public String getStructurePath()
    {
        return path;
    }

    /**
     * Getter for the custom building name.
     *
     * @return the name.
     */
    @Override
    public String getCustomName()
    {
        return this.customName;
    }

    /**
     * Getter for the style.
     *
     * @return the style string.
     */
    @Override
    public String getStructurePack()
    {
        return pack;
    }

    /**
     * Getter for the rotation.
     *
     * @return the rotation.
     */
    @Override
    public int getRotation()
    {
        return rotation;
    }

    /**
     * Getter for the mirror.
     *
     * @return true if mirrored.
     */
    @Override
    public boolean isMirrored()
    {
        return isBuildingMirrored;
    }

    /**
     * Get the current work order level.
     *
     * @return 0 if none, othewise the current level worked on
     */
    @Override
    public boolean hasWorkOrder()
    {
        return workOrderLevel != NO_WORK_ORDER;
    }

    @Override
    public boolean isBuilding()
    {
        return workOrderLevel != 0 && workOrderLevel != NO_WORK_ORDER && workOrderLevel > buildingLevel;
    }

    @Override
    public boolean isRepairing()
    {
        return workOrderLevel != 0 && workOrderLevel != NO_WORK_ORDER && workOrderLevel == buildingLevel;
    }

    @Override
    public boolean isDeconstructing()
    {
        return workOrderLevel == 0;
    }

    /**
     * Get the claim radius for the building.
     *
     * @return the radius.
     */
    @Override
    public int getClaimRadius()
    {
        return this.claimRadius;
    }

    /**
     * Returns the Container List
     */
    @Override
    public List<AbstractStorageBlock> getContainerList()
    {
        return new ArrayList<>(containerlist);
    }

    /**
     * Open the associated blockui window for this building. If the player is sneaking open the inventory else open the GUI directly.
     *
     * @param shouldOpenInv if the player is sneaking.
     */
    @Override
    public void openGui(final boolean shouldOpenInv)
    {
        if (shouldOpenInv)
        {
            Network.getNetwork().sendToServer(new OpenInventoryMessage(this));
        }
        else
        {
            @Nullable final BOWindow window = getWindow();
            if (window != null)
            {
                window.open();
            }
        }
    }

    /**
     * Will return the window if this building has an associated blockui window.
     *
     * @return blockui window.
     */
    @Override
    @Nullable
    public BOWindow getWindow()
    {
        if (!getModuleViews(WorkerBuildingModuleView.class).isEmpty())
        {
            return new WindowHutWorkerModulePlaceholder<>(this);
        }
        return new WindowHutMinPlaceholder<>(this);
    }

    /**
     * Read this view from a {@link FriendlyByteBuf}.
     *
     * @param buf The buffer to read this view from.
     */
    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        buildingLevel = buf.readInt();
        buildingMaxLevel = buf.readInt();
        buildingDmPrio = buf.readInt();
        workOrderLevel = buf.readInt();
        pack = buf.readUtf(32767);
        path = buf.readUtf(32767);
        parent = buf.readBlockPos();
        customName = buf.readUtf(32767);

        rotation = buf.readInt();
        isBuildingMirrored = buf.readBoolean();
        claimRadius = buf.readInt();

        final List<IToken<?>> list = new ArrayList<>();
        final int resolverSize = buf.readInt();
        for (int i = 0; i < resolverSize; i++)
        {
            final CompoundTag compound = buf.readNbt();
            if (compound != null)
            {
                list.add(StandardFactoryController.getInstance().deserialize(compound));
            }
        }

        resolvers = ImmutableList.copyOf(list);
        final CompoundTag compound = buf.readNbt();
        if (compound != null)
        {
            requesterId = StandardFactoryController.getInstance().deserialize(compound);
        }
        containerlist.clear();
        final int racks = buf.readInt();
        for (int i = 0; i < racks; i++)
        {
            AbstractStorageBlock storageInterface = ModStorageBlocks.getStorageBlockInterface(getColony().getWorld(), buf.readBlockPos());
            if (storageInterface != null)
            {
                containerlist.add(storageInterface);
            }
        }
        loadRequestSystemFromNBT(buf.readNbt());
        isDeconstructed = buf.readBoolean();
        isAssignmentAllowed = buf.readBoolean();

        for (int i = 0, size = buf.readInt(); i < size; i++)
        {
            int id = buf.readInt();
            final IBuildingModuleView moduleView = moduleViews.get(id);

            if (moduleView == null)
            {
                Log.getLogger().error("Problem during sync: Client side does not have matching module views to sent module data, missing:" + BuildingEntry.getProducer(id).key);
                return;
            }

            moduleView.deserialize(buf);
        }
    }

    private void loadRequestSystemFromNBT(final CompoundTag compound)
    {
        this.rsDataStoreToken = StandardFactoryController.getInstance().deserialize(compound.getCompound(TAG_RS_BUILDING_DATASTORE));
    }

    private IRequestSystemBuildingDataStore getDataStore()
    {
        return colony.getRequestManager().getDataStoreManager().get(rsDataStoreToken, TypeConstants.REQUEST_SYSTEM_BUILDING_DATA_STORE);
    }

    @Override
    public Map<Integer, Collection<IToken<?>>> getOpenRequestsByCitizen()
    {
        return getDataStore().getOpenRequestsByCitizen();
    }

    protected Map<IToken<?>, Integer> getCitizensByRequest()
    {
        return getDataStore().getCitizensByRequest();
    }

    @Override
    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfType(@NotNull final ICitizenDataView citizenData, final Class<R> requestType)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                                      .filter(request ->  request.getType().isSubtypeOf(requestType))
                                      .map(request -> (IRequest<? extends R>) request)
                                      .iterator());
    }

    @Override
    public ImmutableList<IRequest<?>> getOpenRequests(@NotNull final ICitizenDataView data)
    {
        if (data == null || getColony() == null || getColony().getRequestManager() == null)
        {
            return ImmutableList.of();
        }

        if (!getOpenRequestsByCitizen().containsKey(data.getId()))
        {
            return ImmutableList.of();
        }

        final Collection<IToken<?>> list = getOpenRequestsByCitizen().get(data.getId());

        if (list == null || list.isEmpty())
        {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(list
                                      .stream().filter(Objects::nonNull)
                                      .map(getColony().getRequestManager()::getRequestForToken)
                                      .filter(Objects::nonNull).iterator());
    }

    @Override
    public ImmutableList<IRequest<?>> getOpenRequestsOfBuilding()
    {
        return ImmutableList.copyOf(getOpenRequestsByCitizen().values().stream().flatMap(Collection::stream)
                                      .filter(Objects::nonNull)
                                      .map(getColony().getRequestManager()::getRequestForToken)
                                      .filter(Objects::nonNull).iterator());
    }

    /**
     * Gets the ColonyView that this building belongs to.
     *
     * @return ColonyView, client side interpretations of Colony.
     */
    @Override
    public IColonyView getColony()
    {
        return colony;
    }

    @Override
    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfTypeFiltered(
      @NotNull final ICitizenDataView citizenData,
      final Class<R> requestType,
      final Predicate<IRequest<? extends R>> filter)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                                      .filter(request ->  request.getType().isSubtypeOf(requestType))
                                      .map(request -> (IRequest<? extends R>) request)
                                      .filter(filter)
                                      .iterator());
    }

    @Override
    public IToken<?> getId()
    {
        return requesterId;
    }

    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        final Integer citizenThatRequested = getCitizensByRequest().remove(request.getId());
        getOpenRequestsByCitizen().get(citizenThatRequested).remove(request.getId());

        if (getOpenRequestsByCitizen().get(citizenThatRequested).isEmpty())
        {
            getOpenRequestsByCitizen().remove(citizenThatRequested);
        }
    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        if (getOpenRequestsOfBuilding().contains(request))
        {
            final Integer citizenThatRequested = getCitizensByRequest().remove(request.getId());
            getOpenRequestsByCitizen().get(citizenThatRequested).remove(request.getId());

            if (getOpenRequestsByCitizen().get(citizenThatRequested).isEmpty())
            {
                getOpenRequestsByCitizen().remove(citizenThatRequested);
            }
        }
    }

    @NotNull
    @Override
    public MutableComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        try
        {
            final MutableComponent component = Component.literal("");
            component.append(Component.translatable(this.getCustomName().isEmpty() ? this.getBuildingType().getTranslationKey() : this.getCustomName()));
            if (getColony() == null || !getCitizensByRequest().containsKey(request.getId()))
            {
                return component;
            }

            final int citizenId = getCitizensByRequest().get(request.getId());
            if (citizenId == -1 || getColony().getCitizen(citizenId) == null)
            {
                return component;
            }

            component.append(Component.literal(": "));
            component.append(Component.literal(getColony().getCitizen(getCitizensByRequest().get(request.getId())).getName()));
            return component;
        }
        catch (final Exception ex)
        {
            Log.getLogger().warn(ex);
            return Component.literal("");
        }
    }

    @NotNull
    @Override
    public ILocation getLocation()
    {
        return null;
    }

    @Override
    public int getBuildingDmPrio()
    {
        return buildingDmPrio;
    }

    @Override
    public ImmutableCollection<IToken<?>> getResolverIds()
    {
        return resolvers;
    }

    @Override
    public void setCustomName(final String name)
    {
        this.customName = name;
        Network.getNetwork().sendToServer(new HutRenameMessage(this, name));
    }

    @Override
    public boolean isDeconstructed()
    {
        return isDeconstructed;
    }

    @Override
    public IBuildingModuleView getModuleView(final int id)
    {
        return moduleViews.get(id);
    }

    @Override
    public <M extends IBuildingModule, V extends IBuildingModuleView> V getModuleView(final BuildingEntry.ModuleProducer<M, V> producer)
    {
        return (V) moduleViews.get(producer.getRuntimeID());
    }

    @Override
    public boolean hasModuleView(final BuildingEntry.ModuleProducer producer)
    {
        return moduleViews.containsKey(producer.getRuntimeID());
    }

    @NotNull
    @Override
    public <T extends IBuildingModuleView> T getModuleViewByType(final Class<T> clazz)
    {
        for (final IBuildingModuleView view : moduleViews.values())
        {
            if (clazz.isInstance(view))
            {
                return (T) view;
            }
        }
        return null;
    }

    @Override
    public <T extends IBuildingModuleView> T getModuleViewMatching(final Class<T> clazz, final Predicate<? super T> modulePredicate)
    {
        for (final IBuildingModuleView module : moduleViews.values())
        {
            if (clazz.isInstance(module) && modulePredicate.test(clazz.cast(module)))
            {
                return (T) module;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public <T extends IBuildingModuleView> List<T> getModuleViews(final Class<T> clazz)
    {
        return this.moduleViews.values().stream()
                 .filter(clazz::isInstance)
                 .map(c -> (T) c)
                 .collect(Collectors.toList());
    }

    @Override
    public void registerModule(final IBuildingModuleView iModuleView)
    {
        iModuleView.setBuildingView(this);
        this.moduleViews.put(iModuleView.getProducer().getRuntimeID(),iModuleView);
    }

    @Override
    public List<IBuildingModuleView> getAllModuleViews()
    {
        return Collections.unmodifiableList(new ArrayList<>(this.moduleViews.values()));
    }

    @Override
    public final BuildingEntry getBuildingType()
    {
        return buildingType;
    }

    @Override
    public void setBuildingType(final BuildingEntry buildingType)
    {
        this.buildingType = buildingType;
    }

    @Override
    public Set<Integer> getAllAssignedCitizens()
    {
        final Set<Integer> assignees = new HashSet<>();
        for (final WorkerBuildingModuleView view : getModuleViews(WorkerBuildingModuleView.class))
        {
            assignees.addAll(view.getAssignedCitizens());
        }
        return assignees;
    }

    @Override
    public boolean allowsAssignment()
    {
        return isAssignmentAllowed;
    }
}
