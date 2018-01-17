package com.minecolonies.coremod.colony.buildings.views;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.network.messages.OpenInventoryMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CITIZEN_BY_REQUEST;
import static com.minecolonies.api.util.constant.Suppression.*;
import static com.minecolonies.coremod.colony.buildings.AbstractBuilding.NO_WORK_ORDER;
import static com.minecolonies.coremod.colony.buildings.AbstractBuilding.processIntegerKeyTokenList;

/**
 * The AbstractBuilding View is the client-side representation of a AbstractBuilding.
 * Views contain the AbstractBuilding's data that is relevant to a Client, in a more client-friendly form.
 * Mutable operations on a View result in a message to the server to perform the operation.
 */
public abstract class AbstractBuildingView implements IRequester
{
    private final ColonyView colony;
    @NotNull
    private final BlockPos   location;
    /**
     * Keeps track of which citizen created what request. Request -> Citizen direction.
     */
    private final HashMap<IToken<?>, Integer> requestsByCitizen = new HashMap<>();
    private int buildingLevel    = 0;
    private int buildingMaxLevel = 0;
    private int buildingDmPrio   = 1;
    private int workOrderLevel   = NO_WORK_ORDER;
    /**
     * Keeps track of which citizen created what request. Citizen -> Request direction.
     */
    private HashMap<Integer, Collection<IToken<?>>> citizensByRequests = new HashMap<>();

    /**
     * Creates a building view.
     *
     * @param c ColonyView the building is in.
     * @param l The location of the building.
     */
    protected AbstractBuildingView(final ColonyView c, @NotNull final BlockPos l)
    {
        colony = c;
        location = new BlockPos(l);
    }

    /**
     * Gets the id for this building.
     *
     * @return A BlockPos because the building ID is its location.
     */
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
    @NotNull
    public BlockPos getLocation()
    {
        return location;
    }

    /**
     * Get the current level of the building.
     *
     * @return AbstractBuilding current level.
     */
    public int getBuildingLevel()
    {
        return buildingLevel;
    }

    /**
     * Get the max level of the building.
     *
     * @return AbstractBuilding max level.
     */
    public int getBuildingMaxLevel()
    {
        return buildingMaxLevel;
    }

    /**
     * Checks if this building is at its max level.
     *
     * @return true if the building is at its max level.
     */
    public boolean isBuildingMaxLevel()
    {
        return buildingLevel >= buildingMaxLevel;
    }

    /**
     * Get the current work order level.
     *
     * @return 0 if none, othewise the current level worked on
     */
    public int getCurrentWorkOrderLevel()
    {
        return workOrderLevel;
    }

    /**
     * Get the current work order level.
     *
     * @return 0 if none, othewise the current level worked on
     */
    public boolean hasWorkOrder()
    {
        return workOrderLevel != NO_WORK_ORDER;
    }

    public boolean isBuilding()
    {
        return workOrderLevel != NO_WORK_ORDER && workOrderLevel > buildingLevel;
    }

    public boolean isRepairing()
    {
        return workOrderLevel != NO_WORK_ORDER && workOrderLevel == buildingLevel;
    }

    /**
     * Open the associated BlockOut window for this building.
     * If the player is sneaking open the inventory else open the GUI directly.
     * @param shouldOpenInv if the player is sneaking.
     */
    public void openGui(final boolean shouldOpenInv)
    {
        if(shouldOpenInv)
        {
            MineColonies.getNetwork().sendToServer(new OpenInventoryMessage(getID()));
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
    @Nullable
    public Window getWindow()
    {
        return null;
    }

    /**
     * Read this view from a {@link ByteBuf}.
     *
     * @param buf The buffer to read this view from.
     */
    public void deserialize(@NotNull final ByteBuf buf)
    {
        buildingLevel = buf.readInt();
        buildingMaxLevel = buf.readInt();
        buildingDmPrio = buf.readInt();
        workOrderLevel = buf.readInt();

        loadRequestSystemFromNBT(ByteBufUtils.readTag(buf));
    }

    private void loadRequestSystemFromNBT(final NBTTagCompound compound)
    {
        this.citizensByRequests.clear();
        if (compound.hasKey(TAG_CITIZEN_BY_REQUEST))
        {
            final NBTTagList citizensByRequestList = compound.getTagList(TAG_CITIZEN_BY_REQUEST, Constants.NBT.TAG_COMPOUND);
            NBTUtils.streamCompound(citizensByRequestList).forEach(cbrc -> processIntegerKeyTokenList(cbrc, citizensByRequests));
        }


        this.requestsByCitizen.clear();
        this.citizensByRequests.keySet().forEach(citizen -> this.citizensByRequests.get(citizen).forEach(requestToken -> this.requestsByCitizen.put(requestToken, citizen)));
    }

    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED, RAWTYPES})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfType(@NotNull final CitizenDataView citizenData, final Class<R> requestType)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                                      .filter(request -> {
                                          Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                                          //TODO: Check on the types of this, it is TypeToken and Class, possible mismatch
                                          return requestTypes.contains(requestType);
                                      })
                                      .map(request -> (IRequest<? extends R>) request)
                                      .iterator());
    }

    @SuppressWarnings(RAWTYPES)
    public ImmutableList<IRequest> getOpenRequests(@NotNull final CitizenDataView data)
    {
        if (!citizensByRequests.containsKey(data.getId()))
        {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(citizensByRequests.get(data.getId()).stream().map(getColony().getRequestManager()::getRequestForToken).filter(Objects::nonNull).iterator());
    }

    /**
     * Gets the ColonyView that this building belongs to.
     *
     * @return ColonyView, client side interpretations of Colony.
     */
    public ColonyView getColony()
    {
        return colony;
    }

    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED, RAWTYPES})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfTypeFiltered(
                                                                                               @NotNull final CitizenDataView citizenData,
                                                                                               final Class<R> requestType,
                                                                                               final Predicate<IRequest<? extends R>> filter)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                                      .filter(request -> {
                                          Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                                          //TODO: Check on the types of this, it is TypeToken and Class, possible mismatch
                                          return requestTypes.contains(requestType);
                                      })
                                      .map(request -> (IRequest<? extends R>) request)
                                      .filter(filter)
                                      .iterator());
    }

    @Override
    public IToken<?> getRequesterId()
    {
        //NOOP; Is Client side view.
        return null;
    }

    @NotNull
    @Override
    public ILocation getRequesterLocation()
    {
        //NOOP; Is Client side view.
        return null;
    }

    @NotNull
    @Override
    public void onRequestComplete(@NotNull final IToken<?> token)
    {
        //NOOP; Is Client side view.
    }

    @NotNull
    @Override
    public void onRequestCancelled(@NotNull final IToken<?> token)
    {
        //NOOP; Is Client side view.
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IToken<?> token)
    {
        if (!requestsByCitizen.containsKey(token))
        {
            return new TextComponentString("<UNKNOWN>");
        }

        return new TextComponentString(getColony().getCitizen(requestsByCitizen.get(token)).getName());
    }

    public int getBuildingDmPrio()
    {
        return buildingDmPrio;
    }
}
