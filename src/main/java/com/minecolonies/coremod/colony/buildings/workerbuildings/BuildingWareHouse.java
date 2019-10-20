package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
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
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.blocks.huts.BlockHutDeliveryman;
import com.minecolonies.coremod.client.gui.WindowHutWareHouse;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.requestsystem.resolvers.WarehouseRequestResolver;
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
public class BuildingWareHouse extends AbstractBuilding implements IWareHouse
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
    private static final Set<Vec3d> registeredDeliverymen = new HashSet<>();

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
    public void deserializeNBT(final NBTTagCompound compound)
    {
        super.deserializeNBT(compound);

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

    @Override
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compound = super.serializeNBT();
        @NotNull final NBTTagList levelTagList = new NBTTagList();
        for (@NotNull final Vec3d deliverymanBuilding : registeredDeliverymen)
        {
            levelTagList.appendTag(NBTUtil.createPosTag(new BlockPos(deliverymanBuilding)));
        }
        compound.setTag(TAG_DELIVERYMAN, levelTagList);
        compound.setInteger(TAG_STORAGE, storageUpgrade);

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
                ((AbstractTileEntityRack) entity).setInWarehouse(true);
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
