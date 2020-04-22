package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingDeliveryman;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.tileentities.*;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.client.gui.WindowHutWareHouse;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.requestsystem.resolvers.WarehouseRequestResolver;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import com.minecolonies.coremod.util.SortingUtils;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ContainerBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.ldtteam.structurize.placementhandlers.PlacementHandlers.handleTileEntityPlacement;

/**
 * Class of the warehouse building.
 */
public class BuildingWareHouse extends AbstractBuilding implements IWareHouse
{
    /**
     * String describing the Warehouse.
     */
    private static final String WAREHOUSE = "warehouse";

    /**
     * Tag to store the deliverymen.
     */
    private static final String TAG_DELIVERYMAN = "deliveryman";

    /**
     * The storage tag for the storage capacity.
     */
    private static final String TAG_STORAGE = "tagStorage";

    /**
     * Minimum stock it can hold per level.
     */
    private static final int STOCK_PER_LEVEL = 5;

    /**
     * The minimum stock tag.
     */
    private static final String TAG_MINIMUM_STOCK = "minstock";

    /**
     * The list of deliverymen registered to this building.
     */
    private final Set<Vec3d> registeredDeliverymen = new HashSet<>();

    /**
     * Max level of the building.
     */
    private static final int MAX_LEVEL = 5;

    /**
     * Max storage upgrades.
     */
    private static final int MAX_STORAGE_UPGRADE = 3;

    /**
     * Storage upgrade level.
     */
    private int storageUpgrade = 0;

    /**
     * Instantiates a new warehouse building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingWareHouse(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public void requestRepair(final BlockPos builder)
    {
        //To ensure that the racks are all set to in the warehouse when repaired.
        for (final BlockPos pos : containerList)
        {
            if (getColony().getWorld() != null)
            {
                final TileEntity entity = getColony().getWorld().getTileEntity(pos);
                if (entity instanceof TileEntityRack)
                {
                    ((AbstractTileEntityRack) entity).setInWarehouse(true);
                }
            }
        }

        super.requestRepair(builder);
    }

    /**
     * Register deliveryman with the warehouse.
     *
     * @param buildingWorker the building of the worker.
     * @return true if able to register or already registered
     */
    @Override
    public boolean registerWithWareHouse(final IBuildingDeliveryman buildingWorker)
    {
        if (registeredDeliverymen.contains(new Vec3d(buildingWorker.getID())))
        {
            return true;
        }

        if (registeredDeliverymen.size() >= getBuildingLevel())
        {
            if (!registeredDeliverymen.isEmpty())
            {
                checkForRegisteredDeliverymen();
            }

            if (registeredDeliverymen.size() >= getBuildingLevel())
            {
                return false;
            }
        }

        registeredDeliverymen.add(new Vec3d(buildingWorker.getID()));
        return true;
    }

    /**
     * Check the registered deliverymen and see if one of their huts got destroyed.
     */
    private void checkForRegisteredDeliverymen()
    {
        for (final Vec3d pos : new ArrayList<>(registeredDeliverymen))
        {
            final IColony colony = getColony();
            final IBuilding building = colony.getBuildingManager().getBuilding(new BlockPos(pos));
            if (!(building instanceof BuildingDeliveryman) || !building.hasAssignedCitizen())
            {
                registeredDeliverymen.remove(pos);
            }
        }
    }

    /**
     * Check if deliveryman is allowed to access warehouse.
     *
     * @param buildingWorker the building of the deliveryman.
     * @return true if able to.
     */
    @Override
    public boolean canAccessWareHouse(final IBuildingDeliveryman buildingWorker)
    {
        return registeredDeliverymen.contains(new Vec3d(buildingWorker.getID()));
    }

    /**
     * Get the deliverymen connected with this building.
     *
     * @return the unmodifiable List of positions of them.
     */
    @Override
    public Set<Vec3d> getRegisteredDeliverymen()
    {
        return new HashSet<>(Collections.unmodifiableSet(registeredDeliverymen));
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        registeredDeliverymen.clear();

        final ListNBT deliverymanTagList = compound.getList(TAG_DELIVERYMAN, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < deliverymanTagList.size(); i++)
        {
            final BlockPos pos = NBTUtil.readBlockPos(deliverymanTagList.getCompound(i));
            if (getColony() != null && getColony().getBuildingManager().getBuilding(pos) instanceof AbstractBuildingWorker) {
                registeredDeliverymen.add(new Vec3d(pos));
            }
        }
        storageUpgrade = compound.getInt(TAG_STORAGE);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        @NotNull final ListNBT levelTagList = new ListNBT();
        for (@NotNull final Vec3d deliverymanBuilding : registeredDeliverymen)
        {
            levelTagList.add(NBTUtil.writeBlockPos(new BlockPos(deliverymanBuilding)));
        }
        compound.put(TAG_DELIVERYMAN, levelTagList);
        compound.putInt(TAG_STORAGE, storageUpgrade);

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

    @NotNull
    @Override
    public String getSchematicName()
    {
        return WAREHOUSE;
    }

    /**
     * Returns the tile entity that belongs to the colony building.
     *
     * @return {@link TileEntityColonyBuilding} object of the building.
     */
    @Override
    public AbstractTileEntityWareHouse getTileEntity()
    {
        final AbstractTileEntityColonyBuilding entity = super.getTileEntity();
        return !(entity instanceof TileEntityWareHouse) ? null : (AbstractTileEntityWareHouse) entity;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_LEVEL;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(storageUpgrade < MAX_STORAGE_UPGRADE);
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        if ((block instanceof ContainerBlock || block instanceof BlockMinecoloniesRack) && (!(block instanceof AbstractSignBlock)))
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof ChestTileEntity)
            {
                handleBuildingOverChest(pos, (ChestTileEntity) entity, world, null);
            }
            if (entity instanceof TileEntityRack)
            {
                ((AbstractTileEntityRack) entity).setInWarehouse(true);
            }
            addContainerPosition(pos);
        }
    }

    /**
     * Handles the chest placement.
     *
     * @param pos            at pos.
     * @param chest          the entity.
     * @param world          the world.
     * @param tileEntityData the rack te data.
     */
    public static void handleBuildingOverChest(@NotNull final BlockPos pos, final ChestTileEntity chest, final World world, @Nullable final CompoundNBT tileEntityData)
    {
        final List<ItemStack> inventory = new ArrayList<>();
        final int size = chest.getSizeInventory();
        for (int slot = 0; slot < size; slot++)
        {
            final ItemStack stack = chest.getStackInSlot(slot);
            if (!ItemStackUtils.isEmpty(stack))
            {
                inventory.add(stack.copy());
            }
            chest.removeStackFromSlot(slot);
        }

        world.setBlockState(pos, ModBlocks.blockRack.getDefaultState(), 0x03);
        if (tileEntityData != null)
        {
            handleTileEntityPlacement(tileEntityData, world, pos);
        }
        final TileEntity entity = world.getTileEntity(pos);
        if (entity instanceof TileEntityRack)
        {
            ((AbstractTileEntityRack) entity).setInWarehouse(true);
            for (final ItemStack stack : inventory)
            {
                if (!ItemStackUtils.isEmpty(stack))
                {
                    InventoryUtils.addItemStackToItemHandler(((AbstractTileEntityRack) entity).getInventory(), stack);
                }
            }
        }
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        final ImmutableCollection<IRequestResolver<?>> supers = super.createResolvers();
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();

        builder.addAll(supers);
        builder.add(new WarehouseRequestResolver(getRequester().getLocation(),
          getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));

        return builder.build();
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.wareHouse;
    }

    /**
     * Upgrade all containers by 9 slots.
     *
     * @param world the world object.
     */
    @Override
    public void upgradeContainers(final World world)
    {
        if (storageUpgrade < MAX_STORAGE_UPGRADE)
        {
            for (final BlockPos pos : getAdditionalCountainers())
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (entity instanceof TileEntityRack)
                {
                    ((AbstractTileEntityRack) entity).upgradeItemStorage();
                }
            }
            storageUpgrade++;
        }
        markDirty();
    }

    @Override
    public boolean canBeGathered()
    {
        return false;
    }

    /**
     * Add the minimum stock of the warehouse to this building.
     *
     * @param itemStack the itemStack to add.
     * @param quantity  the quantity.
     */
    public void addMinimumStock(final ItemStack itemStack, final int quantity)
    {
        if (minimumStock.containsKey(new ItemStorage(itemStack)) || minimumStock.size() < minimumStockSize())
        {
            minimumStock.put(new ItemStorage(itemStack), quantity);
            markDirty();
        }
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

    /**
     * Remove the minimum stock.
     *
     * @param itemStack the stack to remove.
     */
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
     * Regularly tick this building and check if we  got the minimum stock(like once a minute is still fine) - If not: Check if there is a request for this already. -> If not:
     * Create a request. - If so: Check if there is a request for this still. -> If so: cancel it.
     */
    @Override
    public void onColonyTick(final IColony colony)
    {
        super.onColonyTick(colony);
        final Collection<IToken<?>> list = getOpenRequestsByRequestableType().getOrDefault(TypeToken.of(Stack.class), new ArrayList<>());

        for (final Map.Entry<ItemStorage, Integer> entry : minimumStock.entrySet())
        {
            final ItemStack itemStack = entry.getKey().getItemStack().copy();
            final int count = getTileEntity().getItemCount(stack -> !stack.isEmpty() && stack.isItemEqual(itemStack));
            final int delta = entry.getValue() * itemStack.getMaxStackSize() - count;
            final IToken<?> request = getMatchingRequest(itemStack, list);
            if (delta > 0)
            {
                if (request == null)
                {
                    itemStack.setCount(Math.min(itemStack.getMaxStackSize(), delta));
                    final Stack stack = new Stack(itemStack);
                    createRequest(stack, false);
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
     * BuildWarehouse View.
     */
    public static class View extends AbstractBuildingView
    {
        /**
         * Should the building allow further storage upgrades.
         */
        private boolean allowUpgrade = true;

        /**
         * The minimum stock.
         */
        private List<Tuple<ItemStorage, Integer>> minimumStock = new ArrayList<>();

        /**
         * If the warehouse reached the minimum stock limit.
         */
        private boolean reachedLimit = false;

        private List<BlockPos> storageracks = new ArrayList<>();

        /**
         * Instantiate the warehouse view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWareHouse(this);
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);
            allowUpgrade = buf.readBoolean();
            final int size = buf.readInt();
            for (int i = 0; i < size; i++)
            {
                minimumStock.add(new Tuple<>(new ItemStorage(buf.readItemStack()), buf.readInt()));
            }
            reachedLimit = buf.readBoolean();
        }

        /**
         * Check if the warehouse building storage can be upgraded further.
         *
         * @return true if so.
         */
        public boolean canUpgradeStorage()
        {
            return allowUpgrade;
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
    }
}
