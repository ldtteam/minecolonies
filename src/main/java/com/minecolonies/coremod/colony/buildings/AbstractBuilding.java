package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ISchematicProvider;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemBuildingDataStore;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Pickup;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHome;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.requesters.BuildingBasedRequester;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildRemoval;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import com.minecolonies.coremod.research.MultiplierModifierResearchEffect;
import com.minecolonies.coremod.util.ChunkDataHelper;
import com.minecolonies.coremod.util.ColonyUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.colony.requestsystem.requestable.deliveryman.AbstractDeliverymanRequestable.getPlayerActionPriority;
import static com.minecolonies.api.research.util.ResearchConstants.MINIMUM_STOCK;
import static com.minecolonies.api.util.constant.BuildingConstants.NO_WORK_ORDER;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.Suppression.*;

/**
 * Base building class, has all the foundation for what a building stores and does.
 * <p>
 * We suppress the warning which warns you about referencing child classes in the parent because that's how we register the instances of the childClasses to their views and
 * blocks.
 */
@SuppressWarnings({"squid:S2390", "PMD.ExcessiveClassLength"})
public abstract class AbstractBuilding extends AbstractBuildingContainer implements IBuilding
{
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
     * Minimum stock it can hold per level.
     */
    private static final int STOCK_PER_LEVEL = 5;

    /**
     * The minimum stock.
     */
    protected final Map<ItemStorage, Integer> minimumStock = new HashMap<>();

    /**
     * The minimum stock tag.
     */
    private static final String TAG_MINIMUM_STOCK = "minstock";

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

    /**
     * Getter for the custom name of a building.
     *
     * @return the custom name.
     */
    @Override
    @NotNull
    public String getCustomBuildingName()
    {
        return this.customName;
    }

    /**
     * Executed when a new day start.
     */
    @Override
    public void onWakeUp()
    {
        /*
         * Buildings override this if required.
         */
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
    public void onBuildingMove(final IBuilding oldBuilding)
    {

    }

    @Override
    public void onPlayerEnterNearby(final PlayerEntity player)
    {
        if (getBuildingLevel() == 0 || getSchematicName() == null || getSchematicName().isEmpty())
        {
            return;
        }

        if (getTargetableArea(colony.getWorld()).contains(player.getPositionVec()))
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
        return getBuildingRegistryEntry().getBuildingBlock() == block;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        loadRequestSystemFromNBT(compound);
        if (compound.keySet().contains(TAG_IS_BUILT))
        {
            isBuilt = compound.getBoolean(TAG_IS_BUILT);
        }
        else if (getBuildingLevel() > 0)
        {
            isBuilt = true;
        }
        if (compound.keySet().contains(TAG_CUSTOM_NAME))
        {
            this.customName = compound.getString(TAG_CUSTOM_NAME);
        }

        minimumStock.clear();
        final ListNBT minimumStockTagList = compound.getList(TAG_MINIMUM_STOCK, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < minimumStockTagList.size(); i++)
        {
            final CompoundNBT compoundNBT = minimumStockTagList.getCompound(i);
            minimumStock.put(new ItemStorage(ItemStack.read(compoundNBT)), compoundNBT.getInt(TAG_QUANTITY));
        }
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
        compound.putString(TAG_BUILDING_TYPE, this.getBuildingRegistryEntry().getRegistryName().toString());
        writeRequestSystemToNBT(compound);
        compound.putBoolean(TAG_IS_BUILT, isBuilt);
        compound.putString(TAG_CUSTOM_NAME, customName);

        @NotNull final ListNBT minimumStockTagList = new ListNBT();
        for (@NotNull final Map.Entry<ItemStorage, Integer> entry : minimumStock.entrySet())
        {
            final CompoundNBT compoundNBT = new CompoundNBT();
            entry.getKey().getItemStack().write(compoundNBT);
            compoundNBT.putInt(TAG_QUANTITY, entry.getValue());
            minimumStockTagList.add(compoundNBT);
        }
        compound.put(TAG_MINIMUM_STOCK, minimumStockTagList);

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
    }

    @Override
    public void onDestroyed()
    {
        super.onDestroyed();
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
            world.updateComparatorOutputLevel(this.getPosition(), block);
        }

        ChunkDataHelper.claimColonyChunks(colony, false, this.getID(), getClaimRadius(getBuildingLevel()));
        ConstructionTapeHelper.removeConstructionTape(getCorners(), world);
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
            if (o.getBuildingLocation().equals(getID()))
            {
                return;
            }
        }

        WorkOrderBuild workOrder;
        if (removal)
        {
            workOrder = new WorkOrderBuildRemoval(this, level);
        }
        else
        {
            workOrder = new WorkOrderBuildBuilding(this, level);
        }

        if (!removal && !canBeBuiltByBuilder(level) && !workOrder.canBeResolved(colony, level))
        {
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
              "entity.builder.messageBuilderNecessary", Integer.toString(level));
            return;
        }

        if (workOrder.tooFarFromAnyBuilder(colony, level) && builder.equals(BlockPos.ZERO))
        {
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
              "entity.builder.messageBuildersTooFar");
            return;
        }

        if (getPosition().getY() + getHeight() >= MAX_BUILD_HEIGHT)
        {
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
              "entity.builder.messageBuildTooHigh");
            return;
        }
        else if (getPosition().getY() <= MIN_BUILD_HEIGHT)
        {
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
              "entity.builder.messageBuildTooLow");
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
                  "entity.builder.messageBuilderNecessary", Integer.toString(level));
                return;
            }
        }

        colony.getWorkManager().addWorkOrder(workOrder, false);
        colony.getProgressManager().progressWorkOrderPlacement(workOrder);

        if (workOrder.getID() != 0)
        {
            LanguageHandler.sendPlayersMessage(colony.getImportantMessageEntityPlayers(), "com.minecolonies.coremod.workOrderAdded");
        }
        markDirty();
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
        super.markDirty();
        if (colony != null)
        {
            colony.getBuildingManager().markBuildingsDirty();
        }
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
            if (o.getBuildingLocation().equals(getID()))
            {
                return o.getUpgradeLevel();
            }
        }

        for (@NotNull final WorkOrderBuildRemoval o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuildRemoval.class))
        {
            if (o.getBuildingLocation().equals(getID()))
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
            if (o.getBuildingLocation().equals(getID()) && (o instanceof WorkOrderBuildBuilding || o instanceof WorkOrderBuildRemoval))
            {
                colony.getWorkManager().removeWorkOrder(o.getID());
                markDirty();

                final BlockPos buildingPos = o.getClaimedBy();
                final IBuilding building = colony.getBuildingManager().getBuilding(buildingPos);
                if (building != null && building.getMainCitizen() != null)
                {
                    building.cancelAllRequestsOfCitizen(building.getMainCitizen());
                }
                return;
            }
        }
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
        buf.writeString(getBuildingRegistryEntry().getRegistryName().toString());
        buf.writeInt(getBuildingLevel());
        buf.writeInt(getMaxBuildingLevel());
        buf.writeInt(getPickUpPriority());
        buf.writeInt(getCurrentWorkOrderLevel());
        buf.writeString(getStyle());
        buf.writeString(this.getSchematicName());
        buf.writeString(this.getCustomBuildingName());

        buf.writeInt(getRotation());
        buf.writeBoolean(isMirrored());
        buf.writeInt(getClaimRadius(getBuildingLevel()));

        final CompoundNBT requestSystemCompound = new CompoundNBT();
        writeRequestSystemToNBT(requestSystemCompound);

        final ImmutableCollection<IRequestResolver<?>> resolvers = getResolvers();
        buf.writeInt(resolvers.size());
        for (final IRequestResolver<?> resolver : resolvers)
        {
            buf.writeCompoundTag(StandardFactoryController.getInstance().serialize(resolver.getId()));
        }
        buf.writeCompoundTag(StandardFactoryController.getInstance().serialize(getId()));
        buf.writeInt(containerList.size());
        for (int i = 0; i < containerList.size(); i++)
        {
            buf.writeBlockPos(containerList.get(i));
        }
        buf.writeCompoundTag(requestSystemCompound);

        buf.writeInt(minimumStock.size());
        for (final Map.Entry<ItemStorage, Integer> entry : minimumStock.entrySet())
        {
            buf.writeItemStack(entry.getKey().getItemStack());
            buf.writeInt(entry.getValue());
        }
        buf.writeBoolean(minimumStock.size() >= minimumStockSize());
        buf.writeBoolean(isDeconstructed());
    }

    /**
     * Calculate the minimum stock size.
     *
     * @return the size.
     */
    private int minimumStockSize()
    {
        double increase = 1;
        final MultiplierModifierResearchEffect effect = colony.getResearchManager().getResearchEffects().getEffect(MINIMUM_STOCK, MultiplierModifierResearchEffect.class);
        if (effect != null)
        {
            increase = 1 + effect.getEffect();
        }

        return (int) (getBuildingLevel() * STOCK_PER_LEVEL * increase);
    }

    @Override
    public void addMinimumStock(final ItemStack itemStack, final int quantity)
    {
        if (minimumStock.containsKey(new ItemStorage(itemStack)) || minimumStock.size() < minimumStockSize())
        {
            minimumStock.put(new ItemStorage(itemStack), quantity);
            markDirty();
        }
    }

    @Override
    public void removeMinimumStock(final ItemStack itemStack)
    {
        minimumStock.remove(new ItemStorage(itemStack));

        final Collection<IToken<?>> list = getOpenRequestsByRequestableType().getOrDefault(TypeToken.of(Stack.class), new ArrayList<>());
        final IToken<?> token = getMatchingRequest(itemStack, list);
        if (token != null)
        {
            getColony().getRequestManager().updateRequestState(token, RequestState.CANCELLED);
        }

        markDirty();
        ;
    }

    /**
     * Regularly tick this building and check if we  got the minimum stock(like once a minute is still fine)
     * - If not: Check if there is a request for this already.
     * -- If not: Create a request.
     * - If so: Check if there is a request for this still.
     * -- If so: cancel it.
     */
    @Override
    public void onColonyTick(final IColony colony)
    {
        super.onColonyTick(colony);
        final Collection<IToken<?>> list = getOpenRequestsByRequestableType().getOrDefault(TypeToken.of(Stack.class), new ArrayList<>());

        for (final Map.Entry<ItemStorage, Integer> entry : minimumStock.entrySet())
        {
            final ItemStack itemStack = entry.getKey().getItemStack().copy();
            final int count = InventoryUtils.getItemCountInProvider(getTileEntity(), stack -> !stack.isEmpty() && stack.isItemEqual(itemStack));
            final int delta = entry.getValue() * itemStack.getMaxStackSize() - count;
            final IToken<?> request = getMatchingRequest(itemStack, list);
            if (delta > 0)
            {
                if (request == null)
                {
                    itemStack.setCount(Math.min(itemStack.getMaxStackSize(), delta));
                    final Stack stack = new Stack(itemStack);
                    if (getMainCitizen() != null)
                    {
                        getMainCitizen().createRequestAsync(stack);
                    }
                    else
                    {
                        createRequest(stack, false);
                    }
                }
            }
            else if (request != null)
            {
                getColony().getRequestManager().updateRequestState(request, RequestState.CANCELLED);
            }
        }
    }

    /**
     * Check if the building is already requesting this stack.
     *
     * @param stack the stack to check.
     * @param list  the list of tokes to check.
     * @return the token if so.
     */
    private IToken<?> getMatchingRequest(final ItemStack stack, final Collection<IToken<?>> list)
    {
        for (final IToken<?> token : list)
        {
            final IRequest<?> iRequest = colony.getRequestManager().getRequestForToken(token);
            if (iRequest != null && iRequest.getRequest() instanceof Stack && ((Stack) iRequest.getRequest()).getStack().isItemEqual(stack))
            {
                return token;
            }
        }
        return null;
    }

    /**
     * If an incoming request is a minimum stock request.
     *
     * @param request the request to check.
     * @return true if so.
     */
    public boolean isMinimumStockRequest(final IRequest<? extends IDeliverable> request)
    {
        for (final Map.Entry<ItemStorage, Integer> entry : minimumStock.entrySet())
        {
            if (request.getRequest() instanceof com.minecolonies.api.colony.requestsystem.requestable.Stack && ((Stack) request.getRequest()).getStack()
                                                                                                                 .isItemEqual(entry.getKey().getItemStack()))
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
        if (getBuildingLevel() < getMaxBuildingLevel())
        {
            requestWorkOrder(getBuildingLevel() + 1, builder, false);
        }
        else
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.worker.noUpgrade"));
        }
    }

    @Override
    public void requestRemoval(final PlayerEntity player, final BlockPos builder)
    {
        if (this.isDeconstructed())
        {
            final ItemStack stack = new ItemStack(colony.getWorld().getBlockState(getPosition()).getBlock(), 1);
            final CompoundNBT compoundNBT = new CompoundNBT();
            compoundNBT.putInt(TAG_COLONY_ID, this.getColony().getID());
            compoundNBT.putInt(TAG_OTHER_LEVEL, this.getBuildingLevel());
            stack.setTag(compoundNBT);
            if (InventoryUtils.addItemStackToProvider(player, stack))
            {
                colony.getWorld().destroyBlock(this.getPosition(), false);
            }
            else
            {
                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.playerinvfull");
            }
        }
        else
        {
            requestWorkOrder(getBuildingLevel(), builder, true);
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
        final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> tuple = getCorners();
        for (int x = tuple.getA().getA(); x < tuple.getA().getB(); x++)
        {
            for (int z = tuple.getB().getA(); z < tuple.getB().getB(); z++)
            {
                for (int y = getPosition().getY() - 1; y < getPosition().getY() + this.getHeight(); y++)
                {
                    getColony().getWorld().destroyBlock(new BlockPos(x, y, z), false);
                }
            }
        }
    }

    @Override
    public AbstractTileEntityColonyBuilding getTileEntity()
    {
        if ((tileEntity == null)
              && colony != null
              && colony.getWorld() != null
              && getPosition() != null
              && !(colony.getWorld().getBlockState(getPosition()).getBlock() instanceof AirBlock)
              && colony.getWorld().getBlockState(this.getPosition()).getBlock() instanceof AbstractBlockHut)
        {
            final TileEntity te = getColony().getWorld().getTileEntity(getPosition());
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
                colony.getWorld().setTileEntity(getPosition(), tileEntityColonyBuilding);
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

        ConstructionTapeHelper.removeConstructionTape(getCorners(), colony.getWorld());
        colony.getProgressManager().progressBuildBuilding(this,
          colony.getBuildingManager().getBuildings().values().stream()
            .filter(building -> building instanceof AbstractBuildingWorker).mapToInt(ISchematicProvider::getBuildingLevel).sum(),
          colony.getBuildingManager().getBuildings().values().stream()
            .filter(building -> building instanceof BuildingHome).mapToInt(ISchematicProvider::getBuildingLevel).sum()
        );
        final WorkOrderBuildBuilding workOrder = new WorkOrderBuildBuilding(this, newLevel);
        final LoadOnlyStructureHandler wrapper = new LoadOnlyStructureHandler(colony.getWorld(), getPosition(), workOrder.getStructureName(), new PlacementSettings(), true);
        final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners
          = ColonyUtils.calculateCorners(this.getPosition(),
          colony.getWorld(),
          wrapper.getBluePrint(),
          workOrder.getRotation(colony.getWorld()),
          workOrder.isMirrored());
        this.setHeight(wrapper.getBluePrint().getSizeY());
        this.setCorners(corners.getA().getA(), corners.getA().getB(), corners.getB().getA(), corners.getB().getB());
        this.isBuilt = true;

        if (newLevel > getBuildingLevel())
        {
            FireworkUtils.spawnFireworksAtAABBCorners(getTargetableArea(colony.getWorld()), colony.getWorld(), newLevel);
        }
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
                    if (kept.getAmount() >= toKeep)
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
        final IRequestManager manager = colony.getRequestManager();
        toKeep.put(stack -> this.getOpenRequestsByCitizen().values().stream()
                              .anyMatch(list -> list.stream().filter(token -> manager.getRequestForToken(token) != null)
                                                  .anyMatch(token -> manager.getRequestForToken(token).getRequest() instanceof IDeliverable
                                                                       && ((IDeliverable) manager.getRequestForToken(token).getRequest()).matches(stack))),
          new Tuple<>(Integer.MAX_VALUE, true));

        return toKeep;
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
                final TileEntity tempTileEntity = world.getTileEntity(pos);
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
        if (compound.keySet().contains(TAG_REQUESTOR_ID))
        {
            this.requester = StandardFactoryController.getInstance().deserialize(compound.getCompound(TAG_REQUESTOR_ID));
        }
        else
        {
            this.requester = StandardFactoryController.getInstance().getNewInstance(TypeToken.of(BuildingBasedRequester.class), this);
        }

        if (compound.keySet().contains(TAG_RS_BUILDING_DATASTORE))
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

    protected Map<TypeToken<?>, Collection<IToken<?>>> getOpenRequestsByRequestableType()
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
        if (async)
        {
            citizenData.getJob().getAsyncRequests().add(requestToken);
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

    /**
     * Internal method used to register a new Request to the request maps. Helper method.
     *
     * @param citizenId    The id of the citizen.
     * @param requestToken The {@link IToken} that is used to represent the request.
     * @param requested    The class of the type that has been requested eg. {@code ItemStack.class}
     */
    private void addRequestToMaps(@NotNull final Integer citizenId, @NotNull final IToken<?> requestToken, @NotNull final TypeToken<?> requested)
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
    public boolean hasWorkerOpenRequests(@NotNull final ICitizenData citizen)
    {
        return !getOpenRequests(citizen).isEmpty();
    }

    @Override
    public ImmutableList<IRequest<?>> getOpenRequests(@NotNull final ICitizenData data)
    {
        if (!getOpenRequestsByCitizen().containsKey(data.getId()))
        {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(getOpenRequestsByCitizen().get(data.getId())
                                      .stream()
                                      .map(getColony().getRequestManager()::getRequestForToken)
                                      .filter(Objects::nonNull)
                                      .iterator());
    }

    @Override
    public boolean hasWorkerOpenRequestsFiltered(@NotNull final ICitizenData citizen, @NotNull final Predicate<IRequest<?>> selectionPredicate)
    {
        return getOpenRequests(citizen).stream().anyMatch(selectionPredicate);
    }

    @Override
    public <R> boolean hasWorkerOpenRequestsOfType(@NotNull final ICitizenData citizenData, final TypeToken<R> requestType)
    {
        return !getOpenRequestsOfType(citizenData, requestType).isEmpty();
    }

    @Override
    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfType(
      @NotNull final ICitizenData citizenData,
      final TypeToken<R> requestType)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                                      .filter(request -> {
                                          final Set<TypeToken<?>> requestTypes = ReflectionUtils.getSuperClasses(request.getType());
                                          return requestTypes.contains(requestType);
                                      })
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

        if (getOpenRequestsByRequestableType().containsKey(TypeConstants.PICKUP))
        {
            return false;
        }

        createRequest(new Pickup(scaledPriority), true);
        return true;
    }

    @Override
    public boolean hasCitizenCompletedRequests(@NotNull final ICitizenData data)
    {
        return !getCompletedRequests(data).isEmpty();
    }

    @Override
    public ImmutableList<IRequest<?>> getCompletedRequests(@NotNull final ICitizenData data)
    {
        if (!getCompletedRequestsByCitizen().containsKey(data.getId()))
        {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(getCompletedRequestsByCitizen().get(data.getId()).stream()
                                      .map(getColony().getRequestManager()::getRequestForToken).filter(Objects::nonNull).iterator());
    }

    @Override
    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfType(@NotNull final ICitizenData citizenData, final TypeToken<R> requestType)
    {
        return ImmutableList.copyOf(getCompletedRequests(citizenData).stream()
                                      .filter(request -> {
                                          final Set<TypeToken<?>> requestTypes = ReflectionUtils.getSuperClasses(request.getType());
                                          return requestTypes.contains(requestType);
                                      })
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
                                      .filter(request -> {
                                          final Set<TypeToken<?>> requestTypes = ReflectionUtils.getSuperClasses(request.getType());
                                          return requestTypes.contains(requestType);
                                      })
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

        getColony().getRequestManager().updateRequestState(token, RequestState.RECEIVED);
        markDirty();
    }

    @Override
    public void cancelAllRequestsOfCitizen(@NotNull final ICitizenData data)
    {
        getOpenRequests(data).forEach(request ->
        {
            getColony().getRequestManager().updateRequestState(request.getId(), RequestState.CANCELLED);

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

        getCompletedRequests(data).forEach(request -> getColony().getRequestManager().updateRequestState(request.getId(), RequestState.RECEIVED));

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

        for (final IRequestResolver<?> resolver : resolvers)
        {
            final IStandardRequestManager requestManager = (IStandardRequestManager) getColony().getRequestManager();

            final List<IRequest<? extends IDeliverable>> deliverableRequests =
              requestManager.getRequestHandler().getRequestsMadeByRequester(resolver)
                .stream()
                .filter(iRequest -> iRequest.getRequest() instanceof IDeliverable)
                .map(iRequest -> (IRequest<? extends IDeliverable>) iRequest)
                .collect(Collectors.toList());

            final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(deliverableRequests, stack);

            if (target == null
                  || (!colony.getRequestManager().getPlayerResolver().getAllAssignedRequests().contains(target.getId())
                        && !colony.getRequestManager().getRetryingRequestResolver().getAllAssignedRequests().contains(target.getId())))
            {
                continue;
            }

            getColony().getRequestManager().overruleRequest(target.getId(), stack.copy());
            return;
        }

        final Set<Integer> citizenIdsWithRequests = getOpenRequestsByCitizen().keySet();
        for (final int citizenId : citizenIdsWithRequests)
        {
            final ICitizenData data = getColony().getCitizenManager().getCitizen(citizenId);

            if (data == null)
            {
                continue;
            }

            final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(getOpenRequestsOfType(data, TypeConstants.DELIVERABLE), stack);

            if (target == null || (!colony.getRequestManager().getPlayerResolver().getAllAssignedRequests().contains(target.getId())
                                     && !colony.getRequestManager().getRetryingRequestResolver().getAllAssignedRequests().contains(target.getId())))
            {
                continue;
            }

            getColony().getRequestManager().overruleRequest(target.getId(), stack.copy());
            return;
        }
    }

    @Override
    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfTypeFiltered(
      @NotNull final ICitizenData citizenData,
      final TypeToken<R> requestType,
      final Predicate<IRequest<? extends R>> filter)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                                      .filter(request -> {
                                          final Set<TypeToken<?>> requestTypes = ReflectionUtils.getSuperClasses(request.getType());
                                          return requestTypes.contains(requestType);
                                      })
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

        final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(getOpenRequestsOfType(citizenData, TypeConstants.DELIVERABLE), stack);

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
                                                                                       .map(getColony().getRequestManager()::getRequestForToken)
                                                                                       .map(IRequest::getChildren)
                                                                                       .flatMap(Collection::stream)
                                                                                       .map(getColony().getRequestManager()::getRequestForToken)
                                                                                       .filter(iRequest -> iRequest.getRequest() instanceof IDeliverable)
                                                                                       .filter(iRequest -> ((IRequest<? extends IDeliverable>) iRequest).getRequest()
                                                                                                             .matches(stack))
                                                                                       .findFirst()
                                                                                       .map(iRequest -> (IRequest<? extends IDeliverable>) iRequest)
                                                                                       .orElse(null);

                    if (deliverableChildRequest != null)
                    {
                        deliverableChildRequest.overrideCurrentDeliveries(ImmutableList.of(stack));
                        getColony().getRequestManager().overruleRequest(deliverableChildRequest.getId(), stack.copy());
                        return true;
                    }
                }
            }

            return false;
        }

        target.overrideCurrentDeliveries(ImmutableList.of(stack));
        getColony().getRequestManager().overruleRequest(target.getId(), stack.copy());
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
                 .map(getColony().getRequestManager()::getRequestForToken)
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
        final IStandardRequestManager requestManager = (IStandardRequestManager) getColony().getRequestManager();
        if (!requestManager.getProviderHandler().getRegisteredResolvers(this).isEmpty())
        {
            return ImmutableList.copyOf(requestManager.getProviderHandler().getRegisteredResolvers(this)
                                          .stream()
                                          .map(token -> requestManager.getResolverHandler().getResolver(token))
                                          .collect(
                                            Collectors.toList()));
        }

        return createResolvers();
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        return ImmutableList.of(new BuildingRequestResolver(getRequester().getLocation(), getColony().getRequestManager().getFactoryController().getNewInstance(
          TypeConstants.ITOKEN)));
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
        final Integer citizenThatRequested = getCitizensByRequest().remove(request.getId());
        getOpenRequestsByCitizen().get(citizenThatRequested).remove(request.getId());

        if (getOpenRequestsByCitizen().get(citizenThatRequested).isEmpty())
        {
            getOpenRequestsByCitizen().remove(citizenThatRequested);
        }

        getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).remove(request.getId());

        if (getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).isEmpty())
        {
            getOpenRequestsByRequestableType().remove(TypeToken.of(request.getRequest().getClass()));
        }

        if (!getCompletedRequestsByCitizen().containsKey(citizenThatRequested))
        {
            getCompletedRequestsByCitizen().put(citizenThatRequested, new ArrayList<>());
        }
        getCompletedRequestsByCitizen().get(citizenThatRequested).add(request.getId());

        markDirty();
    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        final int citizenThatRequested = getCitizensByRequest().remove(request.getId());
        getOpenRequestsByCitizen().get(citizenThatRequested).remove(request.getId());

        if (getOpenRequestsByCitizen().get(citizenThatRequested).isEmpty())
        {
            getOpenRequestsByCitizen().remove(citizenThatRequested);
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
        if (getColony().getCitizenManager().getCitizen(citizenThatRequested) != null)
        {
            getColony().getCitizenManager().getCitizen(citizenThatRequested).onRequestCancelled(request.getId());
        }
        markDirty();
    }

    @NotNull
    @Override
    public ITextComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        if (!getCitizensByRequest().containsKey(request.getId()))
        {
            return new StringTextComponent("<UNKNOWN>");
        }

        final Integer citizenData = getCitizensByRequest().get(request.getId());
        return new StringTextComponent(this.getSchematicName() + " " + getColony().getCitizenManager().getCitizen(citizenData).getName());
    }

    @Override
    public Optional<ICitizenData> getCitizenForRequest(@NotNull final IToken<?> token)
    {
        if (!getCitizensByRequest().containsKey(token) || getColony() == null)
        {
            return Optional.empty();
        }

        final int citizenID = getCitizensByRequest().get(token);
        if (getColony().getCitizenManager().getCitizen(citizenID) == null)
        {
            return Optional.empty();
        }

        return Optional.of(getColony().getCitizenManager().getCitizen(citizenID));
    }

    //------------------------- !END! RequestSystem handling for minecolonies buildings -------------------------//

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
                buffer.writeBoolean(false);
                buffer.writeBlockPos(getID());
                return new ContainerCrafting(id, inv, buffer);
            }
        }, buffer -> new PacketBuffer(buffer.writeBoolean(false)).writeBlockPos(getID()));
    }
}
