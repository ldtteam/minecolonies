package com.minecolonies.api.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Suppression.*;

public interface IBuilding extends ISchematicProvider, ICitizenAssignable, IBuildingContainer, IRequestResolverProvider, IRequester
{
    /**
     * Getter for the custom name of a building.
     * @return the custom name.
     */
    @NotNull
    String getCustomBuildingName();

    /**
     * Executed when a new day start.
     */
    void onWakeUp();

    /**
     * Executed every time when citizen finish inventory cleanup called after citizen got paused.
     * Use for cleaning a state only.
     */
    void onCleanUp(ICitizenData citizen);

    /**
     * Executed when RestartCitizenMessage is called and worker is paused.
     * Use for reseting, onCleanUp is called before this
     */
    void onRestart(ICitizenData citizen);

    /**
     * Called when the building is placed in the world.
     */
    void onPlacement();

    /**
     * Checks if a block matches the current object.
     *
     * @param block Block you want to know whether it matches this class or not.
     * @return True if the block matches this class, otherwise false.
     */
    boolean isMatchingBlock(@NotNull Block block);

    /**
     * Destroys the block.
     * Calls {@link #onDestroyed()}.
     */
    void destroy();

    @Override
    void onDestroyed();

    /**
     * Method to define if a builder can build this although the builder is not level 1 yet.
     *
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
     * @return the radius.
     */
    int getClaimRadius(int buildingLevel);

    /**
     * Serializes to view.
     * Sends 3 integers.
     * 1) hashcode of the name of the class.
     * 2) building level.
     * 3) max building level.
     *
     * @param buf ByteBuf to write to.
     */
    void serializeToView(@NotNull ByteBuf buf);

    /**
     * Check if a building is being gathered.
     *
     * @return true if so.
     */
    boolean isBeingGathered();

    /**
     * Set the custom building name of the building.
     * @param name the name to set.
     */
    void setCustomBuildingName(String name);

    /**
     * Check if the building should be gathered by the dman.
     * @return true if so.
     */
    boolean canBeGathered();

    /**
     * Set if a building is being gathered.
     *
     * @param gathering value to set.
     */
    void setBeingGathered(boolean gathering);

    /**
     * Requests an upgrade for the current building.
     *
     * @param player the requesting player.
     * @param builder the assigned builder.
     */
    void requestUpgrade(EntityPlayer player, BlockPos builder);

    /**
     * Requests a repair for the current building.
     * @param builder
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
     * Called upon completion of an upgrade process.
     * We suppress this warning since this parameter will be used in child classes which override this method.
     *
     * @param newLevel The new level.
     */
    void onUpgradeComplete(int newLevel);

    /**
     * Check if the worker requires a certain amount of that item and the alreadykept list contains it.
     * Always leave one stack behind if the worker requires a certain amount of it. Just to be sure.
     *
     * @param stack            the stack to check it with.
     * @param localAlreadyKept already kept items.
     * @param inventory        if it should be in the inventory or in the building.
     * @return the amount which can get dumped or 0 if not.
     */
    int buildingRequiresCertainAmountOfItem(ItemStack stack, List<ItemStorage> localAlreadyKept, boolean inventory);

    /**
     * Override this method if you want to keep an amount of items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
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
     * @param requested   the request to create.
     * @param async       if async or not.
     * @param <R>         the type of the request.
     * @return the Token of the request.
     */
    <R extends IRequestable> IToken<?> createRequest(@NotNull R requested, boolean async);

    boolean hasWorkerOpenRequests(@NotNull ICitizenData citizen);

    @SuppressWarnings(RAWTYPES)
    ImmutableList<IRequest> getOpenRequests(@NotNull ICitizenData data);

    @SuppressWarnings(RAWTYPES)
    boolean hasWorkerOpenRequestsFiltered(@NotNull ICitizenData citizen, @NotNull Predicate<IRequest> selectionPredicate);

    <R> boolean hasWorkerOpenRequestsOfType(@NotNull ICitizenData citizenData, TypeToken<R> requestType);

    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED, RAWTYPES})
    <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfType(
      @NotNull ICitizenData citizenData,
      TypeToken<R> requestType);

    boolean hasCitizenCompletedRequests(@NotNull ICitizenData data);

    @SuppressWarnings(RAWTYPES)
    ImmutableList<IRequest> getCompletedRequests(@NotNull ICitizenData data);

    @SuppressWarnings({GENERIC_WILDCARD, RAWTYPES, UNCHECKED})
    <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfType(@NotNull ICitizenData citizenData, TypeToken<R> requestType);

    @SuppressWarnings({GENERIC_WILDCARD, RAWTYPES, UNCHECKED})
    <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfTypeFiltered(
      @NotNull ICitizenData citizenData,
      TypeToken<R> requestType,
      Predicate<IRequest<? extends R>> filter);

    void markRequestAsAccepted(@NotNull ICitizenData data, @NotNull IToken<?> token);

    void cancelAllRequestsOfCitizen(@NotNull ICitizenData data);

    /**
     * Overrule the next open request with a give stack.
     * <p>
     * We squid:s135 which takes care that there are not too many continue statements in a loop since it makes sense here
     * out of performance reasons.
     *
     * @param stack the stack.
     */
    @SuppressWarnings("squid:S135")
    void overruleNextOpenRequestWithStack(@NotNull ItemStack stack);

    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED, RAWTYPES})
    <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfTypeFiltered(
      @NotNull ICitizenData citizenData,
      TypeToken<R> requestType,
      Predicate<IRequest<? extends R>> filter);

    boolean overruleNextOpenRequestOfCitizenWithStack(@NotNull ICitizenData citizenData, @NotNull ItemStack stack);

    @Override
    ImmutableCollection<IRequestResolver<?>> getResolvers();

    ImmutableCollection<IRequestResolver<?>> createResolvers();

    IRequester getRequester();

    Optional<ICitizenData> getCitizenForRequest(@NotNull IToken token);

    BuildingEntry getBuildingRegistryEntry();
}
