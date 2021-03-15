package com.minecolonies.api.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Suppression.GENERIC_WILDCARD;

public interface IBuilding extends IBuildingContainer, IRequestResolverProvider, IRequester
{
    /**
     * Check if the building has a particular module.
     * @param clazz the class or interface of the module to check.
     * @return true if so.
     */
    boolean hasModule(final Class<? extends IBuildingModule> clazz);

    /**
     * Get the first module with a particular class or interface.
     * @param clazz the module's class or interface.
     * @return the module or empty if not existent.
     */
    @NotNull
    <T extends IBuildingModule> Optional<T> getFirstModuleOccurance(Class<T> clazz);

    /**
     * Get all modules with a particular class or interface.
     * @param clazz the module's interface (or class, but prefer getModule in that case)
     * @return the list of modules or empty if none match.
     */
    @NotNull
    <T extends IBuildingModule> List<T> getModules(Class<T> clazz);

    /**
     * Register a specific module to the building.
     * @param module the module to register.
     */
    void registerModule(@NotNull final IBuildingModule module);

    /**
     * Getter for the custom name of a building.
     *
     * @return the custom name.
     */
    @NotNull
    String getCustomBuildingName();

    /**
     * Executed when a new day start.
     */
    void onWakeUp();

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
    default void onPlayerEnterNearby(final PlayerEntity player) {}

    /**
     * Called when a player enters the building area
     *
     * @param player entering player
     */
    default void onPlayerEnterBuilding(final PlayerEntity player) {}

    /**
     * Checks if a block matches the current object.
     *
     * @param block Block you want to know whether it matches this class or not.
     * @return True if the block matches this class, otherwise false.
     */
    boolean isMatchingBlock(@NotNull Block block);

    /**
     * When the building is repositioned.
     *
     * @param oldBuilding the moved building.
     */
    void onBuildingMove(final IBuilding oldBuilding);

    /**
     * Destroys the block. Calls {@link #onDestroyed()}.
     */
    void destroy();

    @Override
    void onDestroyed();

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
     * Serializes to view. Sends 3 integers. 1) hashcode of the name of the class. 2) building level. 3) max building level.
     *
     * @param buf PacketBuffer to write to.
     */
    void serializeToView(@NotNull PacketBuffer buf);

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
    void requestUpgrade(PlayerEntity player, BlockPos builder);

    /**
     * Requests a removal for the current building.
     *
     * @param player  the requesting player.
     * @param builder the assigned builder.
     */
    void requestRemoval(PlayerEntity player, BlockPos builder);

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
     * Sets whether this building has a guard building nearby
     * @param guardBuildingNear
     */
    void setGuardBuildingNear(boolean guardBuildingNear);

    /**
     * Check if the worker requires a certain amount of that item and the alreadykept list contains it. Always leave one stack behind if the worker requires a certain amount of it.
     * Just to be sure.
     *
     * @param stack            the stack to check it with.
     * @param localAlreadyKept already kept items.
     * @param inventory        if it should be in the inventory or in the building.
     * @return the amount which can get dumped or 0 if not.
     */
    int buildingRequiresCertainAmountOfItem(ItemStack stack, List<ItemStorage> localAlreadyKept, boolean inventory);

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
    ItemStack forceTransferStack(ItemStack stack, World world);

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

    boolean hasWorkerOpenRequests(@NotNull ICitizenData citizen);

    Collection<IRequest<?>> getOpenRequests(@NotNull ICitizenData data);

    boolean hasWorkerOpenRequestsFiltered(@NotNull ICitizenData citizen, @NotNull Predicate<IRequest<?>> selectionPredicate);

    /**
     * Checks whether the citizen has an open sync request, preventing it from working
     *
     * @param citizen citizen data to check
     * @return true if an open non async request exists
     */
    boolean hasOpenSyncRequest(@NotNull ICitizenData citizen);

    <R> boolean hasWorkerOpenRequestsOfType(@NotNull ICitizenData citizenData, TypeToken<R> requestType);

    @SuppressWarnings(GENERIC_WILDCARD)
    <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfType(
      @NotNull ICitizenData citizenData,
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
     * @param scaledPriority The priority of the pickup request. This value is considered already scaled!
     * @return true if a pickup request could be created, false if not.
     */
    boolean createPickupRequest(final int scaledPriority);

    @Override
    ImmutableCollection<IRequestResolver<?>> getResolvers();

    ImmutableCollection<IRequestResolver<?>> createResolvers();

    IRequester getRequester();

    Optional<ICitizenData> getCitizenForRequest(@NotNull IToken<?> token);

    BuildingEntry getBuildingRegistryEntry();

    /**
     * Remove the minimum stock.
     *
     * @param itemStack the stack to remove.
     */
    void removeMinimumStock(final ItemStack itemStack);

    /**
     * Add the minimum stock of the warehouse to this building.
     *
     * @param itemStack the itemStack to add.
     * @param quantity  the quantity.
     */
    void addMinimumStock(final ItemStack itemStack, final int quantity);

    /**
     * Calculate the number of reserved stacks the resolver can't touch.
     * @return a list of itemstorages.
     */
    Map<ItemStorage, Integer> reservedStacks();

    /**
     * Open the right crafting container.
     *
     * @param player the player opening it.
     */
    void openCraftingContainer(final ServerPlayerEntity player);

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
     * @param positionVec the vec to check.
     * @return true if so.
     */
    boolean isInBuilding(Vector3d positionVec);
}
