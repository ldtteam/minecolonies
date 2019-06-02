package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.blocks.huts.BlockHutDeliveryman;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.client.gui.WindowHutWareHouse;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.requestsystem.resolvers.WarehouseRequestResolver;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.tileentities.TileEntityRack;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Class of the warehouse building.
 */
public class BuildingWareHouse extends AbstractBuilding
{
    /**
     * String describing the Warehouse.
     */
    private static final String WAREHOUSE = "WareHouse";

    /**
     * Tag to store the deliverymen.
     */
    private static final String TAG_DELIVERYMAN = "Deliveryman";

    /**
     * The storage tag for the storage capacity.
     */
    private static final String TAG_STORAGE = "tagStorage";

    /**
     * The list of deliverymen registered to this building.
     */
    private static final List<Vec3d> registeredDeliverymen = new ArrayList<>();

    /**
     * Max level of the building.
     */
    private static final int MAX_LEVEL = 5;

    /**
     * Max storage upgrades.
     */
    private static final int MAX_STORAGE_UPGRADE = 3;

    /**
     * The tileEntity of the building.
     */
    private TileEntityWareHouse tileEntity;

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
    public BuildingWareHouse(final Colony c, final BlockPos l)
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
                    ((TileEntityRack) entity).setInWarehouse(true);
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
    public boolean registerWithWareHouse(final BuildingDeliveryman buildingWorker)
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
            return false;
        }

        registeredDeliverymen.add(new Vec3d(buildingWorker.getID()));
        return true;
    }

    /**
     * Check the registered deliverymen and see if one of their huts got destroyed.
     */
    private void checkForRegisteredDeliverymen()
    {
        final List<Vec3d> registeredDeliverymenCopy = new ArrayList<>(registeredDeliverymen);
        for (final Vec3d pos : registeredDeliverymenCopy)
        {
            final Colony colony = getColony();
            if (colony != null && colony.getWorld() != null
                  && (!(colony.getWorld().getBlockState(new BlockPos(pos)) instanceof BlockHutDeliveryman) || colony.isCoordInColony(colony.getWorld(), new BlockPos(pos))))
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
    public boolean canAccessWareHouse(final BuildingDeliveryman buildingWorker)
    {
        return registeredDeliverymen.contains(new Vec3d(buildingWorker.getID()));
    }

    /**
     * Get the deliverymen connected with this building.
     *
     * @return the unmodifiable List of positions of them.
     */
    public List<Vec3d> getRegisteredDeliverymen()
    {
        return new ArrayList<>(Collections.unmodifiableList(registeredDeliverymen));
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        registeredDeliverymen.clear();
        final NBTTagList deliverymanTagList = compound.getTagList(TAG_DELIVERYMAN, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < deliverymanTagList.tagCount(); i++)
        {
            final BlockPos pos = NBTUtil.getPosFromTag(deliverymanTagList.getCompoundTagAt(i));
            if (getColony() != null && getColony().getBuildingManager().getBuilding(pos) instanceof AbstractBuildingWorker && !registeredDeliverymen.contains(new Vec3d(pos)))
            {
                registeredDeliverymen.add(new Vec3d(pos));
            }
        }
        storageUpgrade = compound.getInteger(TAG_STORAGE);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return WAREHOUSE;
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList levelTagList = new NBTTagList();
        for (@NotNull final Vec3d deliverymanBuilding : registeredDeliverymen)
        {
            levelTagList.appendTag(NBTUtil.createPosTag(new BlockPos(deliverymanBuilding)));
        }
        compound.setTag(TAG_DELIVERYMAN, levelTagList);
        compound.setInteger(TAG_STORAGE, storageUpgrade);
    }

    /**
     * Returns the tile entity that belongs to the colony building.
     *
     * @return {@link TileEntityColonyBuilding} object of the building.
     */
    @Override
    public TileEntityWareHouse getTileEntity()
    {
        final TileEntity entity = super.getTileEntity();
        return entity == null ? null : (TileEntityWareHouse) entity;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_LEVEL;
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(storageUpgrade < MAX_STORAGE_UPGRADE);
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        if (block instanceof BlockContainer || block instanceof BlockMinecoloniesRack)
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityChest)
            {
                handleBuildingOverChest(pos, (TileEntityChest) entity, world);
            }
            if (entity instanceof TileEntityRack)
            {
                ((TileEntityRack) entity).setInWarehouse(true);
            }
            addContainerPosition(pos);
        }
    }

    /**
     * Handles the chest placement.
     *
     * @param pos   at pos.
     * @param chest the entity.
     * @param world the world.
     */
    public static void handleBuildingOverChest(@NotNull final BlockPos pos, final TileEntityChest chest, final World world)
    {
        final List<ItemStack> inventory = new ArrayList<>();
        final int size = chest.getSingleChestHandler().getSlots();
        for (int slot = 0; slot < size; slot++)
        {
            final ItemStack stack = chest.getSingleChestHandler().getStackInSlot(slot);
            if (!ItemStackUtils.isEmpty(stack))
            {
                inventory.add(stack.copy());
            }
            chest.getSingleChestHandler().extractItem(slot, Integer.MAX_VALUE, false);
        }

        world.setBlockState(pos, ModBlocks.blockRack.getDefaultState(), 0x03);
        final TileEntity entity = world.getTileEntity(pos);
        if (entity instanceof TileEntityRack)
        {
            ((TileEntityRack) entity).setInWarehouse(true);
            for (final ItemStack stack : inventory)
            {
                if (!ItemStackUtils.isEmpty(stack))
                {
                    InventoryUtils.addItemStackToItemHandler(((TileEntityRack) entity).getInventory(), stack);
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
        builder.add(new WarehouseRequestResolver(getRequester().getRequesterLocation(),
                                                  getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));

        return builder.build();
    }

    /**
     * Upgrade all containers by 9 slots.
     *
     * @param world the world object.
     */
    public void upgradeContainers(final World world)
    {
        if (storageUpgrade < MAX_STORAGE_UPGRADE)
        {
            for (final BlockPos pos : getAdditionalCountainers())
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (entity instanceof TileEntityRack)
                {
                    ((TileEntityRack) entity).upgradeItemStorage();
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
     * BuildWarehouse View.
     */
    public static class View extends AbstractBuildingView
    {
        /**
         * Should the building allow further storage upgrades.
         */
        private boolean allowUpgrade = true;

        /**
         * Instantiate the warehouse view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final ColonyView c, final BlockPos l)
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
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
            allowUpgrade = buf.readBoolean();
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
    }
}
