package com.minecolonies.api.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.modules.IModuleContainer;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.EquipmentLevelConstants.BASIC_TOOL_LEVEL;
import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_MAXIMUM;
import static com.minecolonies.api.util.constant.Suppression.GENERIC_WILDCARD;

public interface IBuilding extends IBuildingContainer, IModuleContainer<IBuildingModule>, IRequestResolverProvider, IRequester, ISchematicProvider
{
    /**
     * Minimal level to ask for wood tools. (WOOD_HUT_LEVEL + 1 == stone)
     */
    int WOOD_HUT_LEVEL = 0;

    /**
     * Getter for the custom name of a building.
     *
     * @return the custom name.
     */
    @NotNull
    String getCustomName();

    /**
     * Getter for the display name of a building.
     * Returns either the custom name (if any) or the schematic name.
     *
     * @return the display name.
     */
    @NotNull
    String getBuildingDisplayName();

    /**
     * Executed when a new day start.
     */
    default void onWakeUp() { }

    /**
     * Executed every time when citizen finish inventory cleanup called after citizen got paused. Use for cleaning a state only.
     *
     * @param citizen cleanup for citizen.
     */
    void onCleanUp(ICitizenData citizen);

    /**
     * Executed when RestartCitizenMessage is called and worker is paused. Use for reseting, onCleanUp is called before this
     *
     * @param citizen the citizen assigned to the building.
     */
    void onRestart(ICitizenData citizen);

    /**
     * Called when the building is placed in the world.
     */
    void onPlacement();

    /**
     * Called when a player comes close to the building.
     *
     * @param player entering player
     */
    default void onPlayerEnterNearby(final Player player) {}

    /**
     * Called when a player enters the building area
     *
     * @param player entering player
     */
    default void onPlayerEnterBuilding(final Player player) {}

    /**
     * Checks if a block matches the current object.
     *
     * @param block Block you want to know whether it matches this class or not.
     * @return True if the block matches this class, otherwise false.
     */
    boolean isMatchingBlock(@NotNull Block block);

    /**
     * Destroys the block. Calls {@link #onDestroyed()}.
     */
    void destroy();

    void onDestroyed();

    /**
     * Get the colony from a building.
     * @return the colony it belongs to.
     */
    IColony getColony();

    /**
     * Method to define if a builder can build this although the builder is not level 1 yet.
     *
     * @param newLevel the new level of the building.
     * @return true if so.
     */
    boolean canBeBuiltByBuilder(int newLevel);

    @Override
    void markDirty();

    /**
     * Checks if this building have a work order.
     *
     * @return true if the building is building, upgrading or repairing.
     */
    boolean hasWorkOrder();

    /**
     * Remove the work order for the building.
     * <p>
     * Remove either the upgrade or repair work order
     */
    void removeWorkOrder();

    /**
     * Method to calculate the radius to be claimed by this building depending on the level.
     *
     * @param buildingLevel the building level.
     * @return the radius.
     */
    int getClaimRadius(int buildingLevel);

    /**
     * Serializes to view.
     *
     * @param buf      FriendlyByteBuf to write to.
     * @param fullSync Whether it's a full sync
     */
    void serializeToView(@NotNull FriendlyByteBuf buf, final boolean fullSync);

    /**
     * Set the custom building name of the building.
     *
     * @param name the name to set.
     */
    void setCustomBuildingName(String name);

    /**
     * Check if the building should be gathered by the dman.
     *
     * @return true if so.
     */
    boolean canBeGathered();

    /**
     * Requests an upgrade for the current building.
     *
     * @param player  the requesting player.
     * @param builder the assigned builder.
     */
    void requestUpgrade(Player player, BlockPos builder);

    /**
     * Requests a removal for the current building.
     *
     * @param player  the requesting player.
     * @param builder the assigned builder.
     */
    void requestRemoval(Player player, BlockPos builder);

    /**
     * Requests a repair for the current building.
     *
     * @param builder the assigned builder.
     */
    void requestRepair(BlockPos builder);

    /**
     * Check if the building was built already.
     *
     * @return true if so.
     */
    boolean isBuilt();

    /**
     * Deconstruct the building on destroyed.
     */
    void deconstruct();

    /**
     * Called upon completion of an upgrade process. We suppress this warning since this parameter will be used in child classes which override this method.
     *
     * @param newLevel The new level.
     */
    void onUpgradeComplete(int newLevel);

    /**
     * Whether this building has a guard building nearby
     *
     * @return true/false
     */
    boolean isGuardBuildingNear();

    /**
     * Requests recalculation of whether this building has a guard building nearby
     */
    void resetGuardBuildingNear();

    /**
     * Check if the worker requires a certain amount of that item and the alreadykept list contains it.
     * Always leave one stack behind if the worker requires a certain amount of it. Just to be sure.
     *
     * @param stack            the stack to check it with.
     * @param localAlreadyKept already kept items.
     * @param inventory        if it should be in the inventory or in the building.
     * @param jobEntry the job entry trying to dump.
     * @return the amount which can get dumped or 0 if not.
     */
    int buildingRequiresCertainAmountOfItem(ItemStack stack, List<ItemStorage> localAlreadyKept, boolean inventory, @Nullable final JobEntry jobEntry);

    /**
     * Check if the building requires a certain amount of that item and the alreadykept list contains it.
     * Always leave one stack behind if the worker requires a certain amount of it. Just to be sure.
     *
     * @param stack            the stack to check it with.
     * @param localAlreadyKept already kept items.
     * @param inventory        if it should be in the inventory or in the building.
     * @return the amount which can get dumped or 0 if not.
     */
    default int buildingRequiresCertainAmountOfItem(ItemStack stack, List<ItemStorage> localAlreadyKept, boolean inventory)
    {
        return buildingRequiresCertainAmountOfItem(stack, localAlreadyKept, inventory, null);
    }

    /**
     * Override this method if you want to keep an amount of items in inventory. When the inventory is full, everything get's dumped into the building chest. But you can use this
     * method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount();

    /**
     * Try to transfer a stack to one of the inventories of the building and force the transfer.
     *
     * @param stack the stack to transfer.
     * @param world the world to do it in.
     * @return the itemStack which has been replaced or the itemStack which could not be transfered
     */
    @Nullable
    ItemStack forceTransferStack(ItemStack stack, Level world);

    /**
     * Create a request for a citizen.
     *
     * @param citizenData the data of the citizen.
     * @param requested   the request to create.
     * @param async       if async or not.
     * @param <R>         the type of the request.
     * @return the Token of the request.
     */
    <R extends IRequestable> IToken<?> createRequest(@NotNull ICitizenData citizenData, @NotNull R requested, boolean async);

    /**
     * Create a request for the building.
     *
     * @param requested the request to create.
     * @param async     if async or not.
     * @param <R>       the type of the request.
     * @return the Token of the request.
     */
    <R extends IRequestable> IToken<?> createRequest(@NotNull R requested, boolean async);

    boolean hasWorkerOpenRequests(final int citizenid);

    Collection<IRequest<?>> getOpenRequests(final int citizenid);

    boolean hasWorkerOpenRequestsFiltered(final int citizenid, @NotNull Predicate<IRequest<?>> selectionPredicate);

    /**
     * Checks whether the citizen has an open sync request, preventing it from working
     *
     * @param citizen citizen data to check
     * @return true if an open non async request exists
     */
    boolean hasOpenSyncRequest(@NotNull ICitizenData citizen);

    <R> boolean hasWorkerOpenRequestsOfType(final int citizenid, TypeToken<R> requestType);

    @SuppressWarnings(GENERIC_WILDCARD)
    <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfType(
      final int citizenid,
      TypeToken<R> requestType);

    boolean hasCitizenCompletedRequests(@NotNull ICitizenData data);

    boolean hasCitizenCompletedRequestsToPickup(@NotNull ICitizenData data);

    Collection<IRequest<?>> getCompletedRequests(@NotNull ICitizenData data);

    @SuppressWarnings(GENERIC_WILDCARD)
    <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfType(@NotNull ICitizenData citizenData, TypeToken<R> requestType);

    @SuppressWarnings(GENERIC_WILDCARD)
    <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfTypeFiltered(
      @NotNull ICitizenData citizenData,
      TypeToken<R> requestType,
      Predicate<IRequest<? extends R>> filter);

    void markRequestAsAccepted(@NotNull ICitizenData data, @NotNull IToken<?> token);

    void cancelAllRequestsOfCitizen(@NotNull ICitizenData data);

    /**
     * Overrule the next open request with a give stack.
     * <p>
     * We squid:s135 which takes care that there are not too many continue statements in a loop since it makes sense here out of performance reasons.
     *
     * @param stack the stack.
     */
    @SuppressWarnings("squid:S135")
    void overruleNextOpenRequestWithStack(@NotNull ItemStack stack);

    @SuppressWarnings(GENERIC_WILDCARD)
    <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfTypeFiltered(
      @NotNull ICitizenData citizenData,
      TypeToken<R> requestType,
      Predicate<IRequest<? extends R>> filter);

    boolean overruleNextOpenRequestOfCitizenWithStack(@NotNull ICitizenData citizenData, @NotNull ItemStack stack);

    /**
     * Creates a pickup request for the building. It will make sure that only one pickup request exists per building, so it's safe to call multiple times. The call will return
     * false if a pickup request already exists, or if the priority is not within the proper range, or if the pickup priority is set to NEVER (0).
     *
     * @param pickUpPrio The priority of the pickup request.
     * @return true if a pickup request could be created, false if not.
     */
    boolean createPickupRequest(final int pickUpPrio);

    @Override
    ImmutableCollection<IRequestResolver<?>> getResolvers();

    ImmutableCollection<IRequestResolver<?>> createResolvers();

    IRequester getRequester();

    Optional<ICitizenData> getCitizenForRequest(@NotNull IToken<?> token);

    /**
     * Calculate the number of reserved stacks the resolver can't touch.
     * @param request ignore reservations that would exist for the current request.
     * @return a list of itemstorages.
     */
    Map<ItemStorage, Integer> reservedStacksExcluding(@NotNull final IRequest<? extends IDeliverable> request);

    /**
     * Process time the colony was offline.
     * @param time the time in seconds.
     */
    void processOfflineTime(long time);

    /**
     * Calculate all building corners from the schematic data.
     */
    void calculateCorners();

    /**
     * Check if a certain vec is within this building.
     * @param pos the pos to check.
     * @return true if so.
     */
    boolean isInBuilding(@NotNull final BlockPos pos);

    /**
     * Upgrades the buildings level to fit its schematic data
     */
    void upgradeBuildingLevelToSchematicData();

    /**
     * Get a map of all open requests by type.
     * @return the map.
     */
    Map<TypeToken<?>, Collection<IToken<?>>> getOpenRequestsByRequestableType();

    /**
     * Pickup the building including the level and put it in the players inv.
     * @param player the player picking it up.
     */
    void pickUp(final Player player);

    /**
     * Get the Building type
     *
     * @return building type
     */
    BuildingEntry getBuildingType();

    /**
     * Set the building type
     *
     * @param buildingType
     */
    void setBuildingType(BuildingEntry buildingType);

    /**
     * On tick of the colony.
     */
    void onColonyTick(IColony colony);

    /**
     * Check if a certain ItemStack is in the request of a worker.
     *
     * @param stack the stack to chest.
     * @return true if so.
     */
    boolean isItemStackInRequest(@Nullable ItemStack stack);

    /**
     * Get the max equipment level useable by the worker.
     *
     * @return the integer.
     */
    default int getMaxEquipmentLevel()
    {
        if (getBuildingLevel() >= getMaxBuildingLevel())
        {
            return TOOL_LEVEL_MAXIMUM;
        }
        else if (getBuildingLevel() <= WOOD_HUT_LEVEL)
        {
            return BASIC_TOOL_LEVEL;
        }
        return getBuildingLevel() - WOOD_HUT_LEVEL;
    }

    /**
     * Check if this building is sufficiently built to be able to assign workers.
     * @return true if so.
     */
    default boolean canAssignCitizens()
    {
        return getBuildingLevel() > 0 && isBuilt();
    }

    /**
     * Get the set of all assigned citizens in the colony.
     * @return the list
     */
    Set<ICitizenData> getAllAssignedCitizen();

    /**
     * Get all handlers associated with this building.
     *
     * @return the handlers of the building + citizen.
     */
    List<IItemHandler> getHandlers();

    /**
     * Get setting for key. Utility function.
     * @param key the key.
     * @param <T> the key type.
     * @return the setting.
     */
    <T extends ISetting<S>, S> T getSetting(@NotNull final ISettingKey<T> key);

    /**
     * Get setting value for key or some default. Utility function.
     * @param key the key.
     * @param <T> the key type.
     * @param def the default.
     * @return the setting.
     */
    <T extends ISetting<S>, S> S getSettingValueOrDefault(@NotNull final ISettingKey<T> key, @NotNull final S def);

    /**
     * Check if the assigned citizens are allowed to eat the following stack.
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    default boolean canEat(final ItemStack stack)
    {
        return true;
    }

    /**
     * Get the standing position for a building.
     * @return the standing pos.
     */
    BlockPos getStandingPosition();
}
