package com.minecolonies.coremod.colony.buildings.views;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemBuildingDataStore;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.colony.OpenInventoryMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.HutRenameMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.BuildingConstants.NO_WORK_ORDER;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_RS_BUILDING_DATASTORE;
import static com.minecolonies.api.util.constant.Suppression.*;

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
    @NotNull
    private IToken<?> rsDataStoreToken;

    /**
     * The Schematic name of the building.
     */
    private String schematicName;

    /**
     * The style of the building.
     */
    private String style;

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

    private List<BlockPos> containerlist = new ArrayList<>();

    /**
     * The minimum stock.
     */
    private List<Tuple<ItemStorage, Integer>> minimumStock = new ArrayList<>();

    /**
     * If the stock limit was reached.
     */
    private boolean reachedLimit = false;

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
    public String getSchematicName()
    {
        return schematicName;
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
    public String getStyle()
    {
        return style;
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

    /**
     * Check if the building is current being built.
     *
     * @return true if so.
     */
    @Override
    public boolean isBuilding()
    {
        return workOrderLevel != NO_WORK_ORDER && workOrderLevel > buildingLevel;
    }

    /**
     * Check if the building is currently being repaired.
     *
     * @return true if so.
     */
    @Override
    public boolean isRepairing()
    {
        return workOrderLevel != NO_WORK_ORDER && workOrderLevel == buildingLevel;
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
    public List<BlockPos> getContainerList()
    {
        return containerlist;
    }

    /**
     * Open the associated BlockOut window for this building. If the player is sneaking open the inventory else open the GUI directly.
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
            @Nullable final Window window = getWindow();
            if (window != null)
            {
                window.open();
            }
        }
    }

    /**
     * Will return the window if this building has an associated BlockOut window.
     *
     * @return BlockOut window.
     */
    @Override
    @Nullable
    public Window getWindow()
    {
        return null;
    }

    /**
     * Read this view from a {@link PacketBuffer}.
     *
     * @param buf The buffer to read this view from.
     */
    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        buildingLevel = buf.readInt();
        buildingMaxLevel = buf.readInt();
        buildingDmPrio = buf.readInt();
        workOrderLevel = buf.readInt();
        style = buf.readString(32767);
        schematicName = buf.readString(32767);
        customName = buf.readString(32767);

        rotation = buf.readInt();
        isBuildingMirrored = buf.readBoolean();
        claimRadius = buf.readInt();

        final List<IToken<?>> list = new ArrayList<>();
        final int resolverSize = buf.readInt();
        for (int i = 0; i < resolverSize; i++)
        {
            final CompoundNBT compound = buf.readCompoundTag();
            if (compound != null)
            {
                list.add(StandardFactoryController.getInstance().deserialize(compound));
            }
        }

        resolvers = ImmutableList.copyOf(list);
        final CompoundNBT compound = buf.readCompoundTag();
        if (compound != null)
        {
            requesterId = StandardFactoryController.getInstance().deserialize(compound);
        }
        containerlist.clear();
        final int racks = buf.readInt();
        for (int i = 0; i < racks; i++)
        {
            containerlist.add(buf.readBlockPos());
        }
        loadRequestSystemFromNBT(buf.readCompoundTag());

        minimumStock.clear();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            minimumStock.add(new Tuple<>(new ItemStorage(buf.readItemStack()), buf.readInt()));
        }
        reachedLimit = buf.readBoolean();
    }

    /**
     * The minimum stock.
     *
     * @return the stock.
     */
    public List<Tuple<ItemStorage, Integer>> getStock()
    {
        return minimumStock;
    }

    /**
     * Check if the warehouse has reached the limit.
     *
     * @return true if so.
     */
    public boolean hasReachedLimit()
    {
        return reachedLimit;
    }

    private void loadRequestSystemFromNBT(final CompoundNBT compound)
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
    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED, RAWTYPES})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfType(@NotNull final ICitizenDataView citizenData, final Class<R> requestType)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                                      .filter(request -> {
                                          final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getType());
                                          return requestTypes.contains(TypeToken.of(requestType));
                                      })
                                      .map(request -> (IRequest<? extends R>) request)
                                      .iterator());
    }

    @Override
    @SuppressWarnings(RAWTYPES)
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
    @SuppressWarnings(RAWTYPES)
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
    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED, RAWTYPES})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfTypeFiltered(
      @NotNull final ICitizenDataView citizenData,
      final Class<R> requestType,
      final Predicate<IRequest<? extends R>> filter)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                                      .filter(request -> {
                                          final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getType());
                                          return requestTypes.contains(TypeToken.of(requestType));
                                      })
                                      .map(request -> (IRequest<? extends R>) request)
                                      .filter(filter)
                                      .iterator());
    }

    @Override
    public IToken<?> getId()
    {
        return requesterId;
    }

    @NotNull
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

    @NotNull
    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        final Integer citizenThatRequested = getCitizensByRequest().remove(request.getId());
        getOpenRequestsByCitizen().get(citizenThatRequested).remove(request.getId());

        if (getOpenRequestsByCitizen().get(citizenThatRequested).isEmpty())
        {
            getOpenRequestsByCitizen().remove(citizenThatRequested);
        }
    }

    @NotNull
    @Override
    public ITextComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        try
        {
            if (getColony() == null || !getCitizensByRequest().containsKey(request.getId()) || getColony().getCitizen(getCitizensByRequest().get(request.getId())) == null)
            {
                return new TranslationTextComponent(this.getCustomName().isEmpty() ? this.getSchematicName() : this.getCustomName());
            }

            return new StringTextComponent(getColony().getCitizen(getCitizensByRequest().get(request.getId())).getName());
        }
        catch (final Exception ex)
        {
            Log.getLogger().warn(ex);
            return new StringTextComponent("");
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
}
