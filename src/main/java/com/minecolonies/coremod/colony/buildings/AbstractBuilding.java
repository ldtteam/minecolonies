package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.blocks.*;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.materials.MaterialStore;
import com.minecolonies.coremod.colony.materials.MaterialSystem;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.util.BlockPosUtil;
import com.minecolonies.coremod.util.LanguageHandler;
import com.minecolonies.coremod.util.Log;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Base building class, has all the foundation for what a building stores and does.
 */
public abstract class AbstractBuilding
{
    /**
     * The tag to store the building type.
     */
    private static final String TAG_BUILDING_TYPE = "type";

    /**
     * The tag to store the building location.
     * Location is unique (within a Colony) and so can double as the Id.
     */
    private static final String TAG_LOCATION = "location";

    /**
     * The tag to store the level of the building.
     */
    private static final String TAG_BUILDING_LEVEL = "level";

    /**
     * The tag to store the rotation of the building.
     */
    private static final String TAG_ROTATION = "rotation";

    /**
     * The tag to store the style of the building.
     */
    private static final String TAG_STYLE = "style";

    /**
     * Map to resolve names to class.
     */
    @NotNull
    private static final Map<String, Class<?>> nameToClassMap = new HashMap<>();

    /**
     * Map to resolve classes to name.
     */
    @NotNull
    private static final Map<Class<?>, String> classToNameMap = new HashMap<>();

    /**
     * Map to resolve block to building class.
     */
    @NotNull
    private static final Map<Class<?>, Class<?>> blockClassToBuildingClassMap = new HashMap<>();

    /**
     * Map to resolve classNameHash to class.
     */
    @NotNull
    private static final Map<Integer, Class<?>> classNameHashToClassMap = new HashMap<>();
    /*
     * Add all the mappings.
     */
    static
    {
        addMapping("Baker", BuildingBaker.class, BlockHutBaker.class);
        addMapping("Blacksmith", BuildingBlacksmith.class, BlockHutBlacksmith.class);
        addMapping("Builder", BuildingBuilder.class, BlockHutBuilder.class);
        addMapping("Home", BuildingHome.class, BlockHutCitizen.class);
        addMapping("Farmer", BuildingFarmer.class, BlockHutFarmer.class);
        addMapping("Lumberjack", BuildingLumberjack.class, BlockHutLumberjack.class);
        addMapping("Miner", BuildingMiner.class, BlockHutMiner.class);
        addMapping("Stonemason", BuildingStonemason.class, BlockHutStonemason.class);
        addMapping("TownHall", BuildingTownHall.class, BlockHutTownHall.class);
        addMapping("Warehouse", BuildingDeliveryman.class, BlockHutDeliveryman.class);
        addMapping("Fisherman", BuildingFisherman.class, BlockHutFisherman.class);
        addMapping("GuardTower", BuildingGuardTower.class, BlockHutGuardTower.class);
    }

    /**
     * The location of the building.
     */
    private final BlockPos location;

    /**
     * The colony the building belongs to.
     */
    @NotNull
    private final Colony colony;

    /**
     * The material store of the colony (WIP).
     */
    private final MaterialStore materialStore;

    /**
     * The tileEntity of the building.
     */
    private TileEntityColonyBuilding tileEntity;

    /**
     * The level of the building.
     */
    private int buildingLevel = 0;

    /**
     * The rotation of the building.
     */
    private int rotation = 0;

    /**
     * The building style.
     */
    private String style = "default";

    /**
     * Made to check if the building has to update the server/client.
     */
    private boolean dirty = false;

    /**
     * Constructor for a AbstractBuilding.
     *
     * @param colony Colony the building belongs to.
     * @param pos    Location of the building (it's Hut Block).
     */
    protected AbstractBuilding(@NotNull final Colony colony, final BlockPos pos)
    {
        location = pos;
        this.colony = colony;
        materialStore = new MaterialStore(MaterialStore.Type.CHEST, colony.getMaterialSystem());
    }

    /**
     * Add build to a mapping.
     * <code>buildingClass</code> needs to extend {@link AbstractBuilding}.
     * <code>parentBlock</code> needs to extend {@link AbstractBlockHut}.
     *
     * @param name          name of building.
     * @param buildingClass subclass of AbstractBuilding, located in {@link com.minecolonies.coremod.colony.buildings}.
     * @param parentBlock   subclass of Block, located in {@link com.minecolonies.coremod.blocks}.
     */
    private static void addMapping(final String name, @NotNull final Class<? extends AbstractBuilding> buildingClass, @NotNull final Class<? extends AbstractBlockHut> parentBlock)
    {
        if (nameToClassMap.containsKey(name) || classNameHashToClassMap.containsKey(buildingClass.getName().hashCode()))
        {
            throw new IllegalArgumentException("Duplicate type '" + name + "' when adding AbstractBuilding class mapping");
        }
        else
        {
            try
            {
                /*
                If a constructor exist for the building, put the building in the lists.
                 */
                if (buildingClass.getDeclaredConstructor(Colony.class, BlockPos.class) != null)
                {
                    nameToClassMap.put(name, buildingClass);
                    classToNameMap.put(buildingClass, name);
                    classNameHashToClassMap.put(buildingClass.getName().hashCode(), buildingClass);
                }
            }
            catch (final NoSuchMethodException exception)
            {
                throw new IllegalArgumentException("Missing constructor for type '" + name + "' when adding AbstractBuilding class mapping", exception);
            }
        }

        if (blockClassToBuildingClassMap.containsKey(parentBlock))
        {
            throw new IllegalArgumentException("AbstractBuilding type '" + name + "' uses TileEntity '" + parentBlock.getClass().getName() + "' which is already in use.");
        }
        else
        {
            blockClassToBuildingClassMap.put(parentBlock, buildingClass);
        }
    }

    /**
     * Create and load a AbstractBuilding given it's saved NBTTagCompound.
     * Calls {@link #readFromNBT(net.minecraft.nbt.NBTTagCompound)}.
     *
     * @param colony   The owning colony.
     * @param compound The saved data.
     * @return {@link AbstractBuilding} created from the compound.
     */
    @Nullable
    public static AbstractBuilding createFromNBT(final Colony colony, @NotNull final NBTTagCompound compound)
    {
        @Nullable AbstractBuilding building = null;
        @Nullable Class<?> oclass = null;

        try
        {
            oclass = nameToClassMap.get(compound.getString(TAG_BUILDING_TYPE));

            if (oclass != null)
            {
                @NotNull final BlockPos pos = BlockPosUtil.readFromNBT(compound, TAG_LOCATION);
                final Constructor<?> constructor = oclass.getDeclaredConstructor(Colony.class, BlockPos.class);
                building = (AbstractBuilding) constructor.newInstance(colony, pos);
            }
        }
        catch (@NotNull NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException exception)
        {
            Log.getLogger().error(exception);
        }

        if (building == null)
        {
            Log.getLogger().warn(String.format("Unknown Building type '%s' or missing constructor of proper format.", compound.getString(TAG_BUILDING_TYPE)));
            return building;
        }

        try
        {
            building.readFromNBT(compound);
        }
        catch (final RuntimeException ex)
        {
            Log.getLogger().error(String.format("A Building %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
              compound.getString(TAG_BUILDING_TYPE), oclass.getName()), ex);
            building = null;
        }

        return building;
    }

    /**
     * Load data from NBT compound.
     * Writes to {@link #buildingLevel}, {@link #rotation} and {@link #style}.
     *
     * @param compound {@link net.minecraft.nbt.NBTTagCompound} to read data from.
     */
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        buildingLevel = compound.getInteger(TAG_BUILDING_LEVEL);

        rotation = compound.getInteger(TAG_ROTATION);
        style = compound.getString(TAG_STYLE);
        if ("".equals(style))
        {
            Log.getLogger().warn("Loaded empty style, setting to default");
            style = "default";
        }

        if (MaterialSystem.isEnabled)
        {
            materialStore.readFromNBT(compound);
        }
    }

    /**
     * Create a Building given it's TileEntity.
     *
     * @param colony The owning colony.
     * @param parent The Tile Entity the building belongs to.
     * @return {@link AbstractBuilding} instance, without NBTTags applied.
     */
    @Nullable
    public static AbstractBuilding create(final Colony colony, @NotNull final TileEntityColonyBuilding parent)
    {
        @Nullable AbstractBuilding building = null;
        final Class<?> oclass;

        try
        {
            oclass = blockClassToBuildingClassMap.get(parent.getBlockType().getClass());

            if (oclass == null)
            {
                Log.getLogger().error(String.format("TileEntity %s does not have an associated Building.", parent.getClass().getName()));
                return building;
            }

            final BlockPos loc = parent.getPosition();
            final Constructor<?> constructor = oclass.getDeclaredConstructor(Colony.class, BlockPos.class);
            building = (AbstractBuilding) constructor.newInstance(colony, loc);
        }
        catch (@NotNull NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException exception)
        {
            Log.getLogger().error(String.format("Unknown Building type '%s' or missing constructor of proper format.", parent.getClass().getName()), exception);
        }

        return building;
    }

    /**
     * Create a AbstractBuilding View given it's saved NBTTagCompound.
     *
     * @param colony The owning colony.
     * @param id     Chunk coordinate of the block a view is created for.
     * @param buf    The network data.
     * @return {@link AbstractBuilding.View} created from reading the buf.
     */
    @Nullable
    public static View createBuildingView(final ColonyView colony, final BlockPos id, @NotNull final ByteBuf buf)
    {
        @Nullable View view = null;
        @Nullable Class<?> oclass = null;

        try
        {
            final int typeHash = buf.readInt();
            oclass = classNameHashToClassMap.get(typeHash);

            if (oclass != null)
            {
                for (@NotNull final Class<?> c : oclass.getDeclaredClasses())
                {
                    if (c.getName().endsWith("$View"))
                    {
                        final Constructor<?> constructor = c.getDeclaredConstructor(ColonyView.class, BlockPos.class);
                        view = (View) constructor.newInstance(colony, id);
                        break;
                    }
                }
            }
        }
        catch (@NotNull NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException exception)
        {
            Log.getLogger().error(exception);
        }

        if (view == null)
        {
            Log.getLogger().warn("Unknown AbstractBuilding type, missing View subclass, or missing constructor of proper format.");
            return view;
        }

        try
        {
            view.deserialize(buf);
        }
        catch (final IndexOutOfBoundsException ex)
        {
            Log.getLogger().error(
              String.format("A AbstractBuilding View (%s) has thrown an exception during deserializing, its state cannot be restored. Report this to the mod author",
                oclass.getName()), ex);
            view = null;
        }

        return view;
    }

    /**
     * Children must return the name of their structure.
     *
     * @return StructureProxy name.
     */
    public abstract String getSchematicName();

    /**
     * Checks if a block matches the current object.
     *
     * @param block Block you want to know whether it matches this class or not.
     * @return True if the block matches this class, otherwise false.
     */
    public boolean isMatchingBlock(@NotNull final Block block)
    {
        final Class<?> c = blockClassToBuildingClassMap.get(block.getClass());
        return getClass().equals(c);
    }

    /**
     * Save data to NBT compound.
     * Writes the {@link #buildingLevel}, {@link #rotation}, {@link #style}, {@link #location}, and {@link #getClass()} value.
     *
     * @param compound {@link net.minecraft.nbt.NBTTagCompound} to write data to.
     */
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        final String s = classToNameMap.get(this.getClass());

        if (s == null)
        {
            throw new IllegalStateException(this.getClass() + " is missing a mapping! This is a bug!");
        }
        else
        {
            compound.setString(TAG_BUILDING_TYPE, s);
            BlockPosUtil.writeToNBT(compound, TAG_LOCATION, location);
        }

        compound.setInteger(TAG_BUILDING_LEVEL, buildingLevel);
        compound.setInteger(TAG_ROTATION, rotation);
        compound.setString(TAG_STYLE, style);

        if (MaterialSystem.isEnabled)
        {
            materialStore.writeToNBT(compound);
        }
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    public BlockPos getLocation()
    {
        return location;
    }

    /**
     * Returns whether the instance is dirty or not.
     *
     * @return true if dirty, false if not.
     */
    public final boolean isDirty()
    {
        return dirty;
    }

    /**
     * Sets {@link #dirty} to false, meaning that the instance is up to date.
     */
    public final void clearDirty()
    {
        dirty = false;
    }

    /**
     * Destroys the block.
     * Calls {@link #onDestroyed()}.
     */
    public final void destroy()
    {
        onDestroyed();
        colony.removeBuilding(this);
    }

    /**
     * Method to do things when a block is destroyed.
     */
    public void onDestroyed()
    {
        final TileEntityColonyBuilding tileEntityNew = this.getTileEntity();
        final World world = colony.getWorld();
        final Block block = world.getBlockState(this.location).getBlock();

        if(tileEntityNew != null)
        {
            InventoryHelper.dropInventoryItems(world, this.location, (IInventory) tileEntityNew);
            world.updateComparatorOutputLevel(this.location, block);
        }

        if (MaterialSystem.isEnabled)
        {
            materialStore.destroy();
        }
    }

    /**
     * Returns the tile entity that belongs to the colony building.
     *
     * @return {@link TileEntityColonyBuilding} object of the building.
     */
    public TileEntityColonyBuilding getTileEntity()
    {
        if ((tileEntity == null || tileEntity.isInvalid()) && colony.getWorld().getBlockState(location).getBlock() != null)
        {
            final TileEntity te = getColony().getWorld().getTileEntity(location);
            if (te instanceof TileEntityColonyBuilding)
            {
                tileEntity = (TileEntityColonyBuilding) te;
                if (tileEntity.getBuilding() == null)
                {
                    tileEntity.setColony(colony);
                    tileEntity.setBuilding(this);
                }
            }
        }

        return tileEntity;
    }

    /**
     * Sets the tile entity for the building.
     *
     * @param te {@link TileEntityColonyBuilding} that will fill the {@link #tileEntity} field.
     */
    public void setTileEntity(final TileEntityColonyBuilding te)
    {
        tileEntity = te;
    }

    /**
     * Returns the colony of the building.
     *
     * @return {@link com.minecolonies.coremod.colony.Colony} of the current object.
     */
    @NotNull
    public Colony getColony()
    {
        return colony;
    }

    /**
     * Method to remove a citizen.
     *
     * @param citizen Citizen to be removed.
     */
    public void removeCitizen(final CitizenData citizen)
    {
        // Can be overridden by other buildings.
    }

    /**
     * On tick of the server.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent}
     */
    public void onServerTick(final TickEvent.ServerTickEvent event)
    {
        // Can be overridden by other buildings.
    }

    /**
     * On tick of the world.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent}
     */
    public void onWorldTick(final TickEvent.WorldTickEvent event)
    {
        // Can be overridden by other buildings.
    }

    /**
     * Requests an upgrade for the current building.
     */
    public void requestUpgrade()
    {
        if (buildingLevel < getMaxBuildingLevel())
        {
            requestWorkOrder(buildingLevel + 1);
        }
    }

    /**
     * Children must return their max building level.
     *
     * @return Max building level.
     */
    public abstract int getMaxBuildingLevel();

    /**
     * Adds work orders to the {@link Colony#workManager}.
     *
     * @param level Desired level.
     */
    private void requestWorkOrder(final int level)
    {
        for (@NotNull final WorkOrderBuild o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuild.class))
        {
            if (o.getBuildingLocation().equals(getID()))
            {
                return;
            }
        }

        colony.getWorkManager().addWorkOrder(new WorkOrderBuild(this, level));
        LanguageHandler.sendPlayersLocalizedMessage(colony.getMessageEntityPlayers(), "com.minecolonies.coremod.workOrderAdded");
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    public BlockPos getID()
    {
        // Location doubles as ID.
        return location;
    }

    /**
     * Requests a repair for the current building.
     */
    public void requestRepair()
    {
        if (buildingLevel > 0)
        {
            requestWorkOrder(buildingLevel);
        }
    }

    /**
     * Returns the rotation of the current building.
     *
     * @return integer value of the rotation.
     */
    public int getRotation()
    {
        return rotation;
    }

    /**
     * Sets the rotation of the current building.
     *
     * @param rotation integer value of the rotation.
     */
    public void setRotation(final int rotation)
    {
        this.rotation = rotation;
    }

    /**
     * Returns the style of the current building.
     *
     * @return String representation of the current building-style
     */
    public String getStyle()
    {
        return style;
    }

    /**
     * Sets the style of the building.
     *
     * @param style String value of the style.
     */
    public void setStyle(final String style)
    {
        this.style = style;
    }

    /**
     * Gets the MaterialStore for this building.
     *
     * @return The MaterialStore that tracks this building's inventory.
     */
    public MaterialStore getMaterialStore()
    {
        return materialStore;
    }

    /**
     * Called upon completion of an upgrade process.
     *
     * @param newLevel The new level.
     */
    public void onUpgradeComplete(final int newLevel)
    {
        // Does nothing here
    }

    /**
     * Serializes to view.
     * Sends 3 integers.
     * 1) hashcode of the name of the class.
     * 2) building level.
     * 3) max building level.
     *
     * @param buf ByteBuf to write to.
     */
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        buf.writeInt(this.getClass().getName().hashCode());
        buf.writeInt(getBuildingLevel());
        buf.writeInt(getMaxBuildingLevel());
    }

    /**
     * Returns the level of the current object.
     *
     * @return Level of the current object.
     */
    public int getBuildingLevel()
    {
        return buildingLevel;
    }

    /**
     * Sets the current level of the building.
     *
     * @param level Level of the building.
     */
    public void setBuildingLevel(final int level)
    {
        if (level > getMaxBuildingLevel())
        {
            return;
        }

        buildingLevel = level;
        markDirty();
        ColonyManager.markDirty();
    }

    /**
     * Marks the instance and the building dirty.
     */
    public final void markDirty()
    {
        dirty = true;
        colony.markBuildingsDirty();
    }

    /**
     * The AbstractBuilding View is the client-side representation of a AbstractBuilding.
     * Views contain the AbstractBuilding's data that is relevant to a Client, in a more client-friendly form.
     * Mutable operations on a View result in a message to the server to perform the operation.
     */
    public static class View
    {
        private final ColonyView colony;
        @NotNull
        private final BlockPos   location;

        private int buildingLevel    = 0;
        private int buildingMaxLevel = 0;

        /**
         * Creates a building view.
         *
         * @param c ColonyView the building is in.
         * @param l The location of the building.
         */
        protected View(final ColonyView c, @NotNull final BlockPos l)
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
         * Gets the ColonyView that this building belongs to.
         *
         * @return ColonyView, client side interpretations of Colony.
         */
        public ColonyView getColony()
        {
            return colony;
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
         * Open the associated BlockOut window for this building.
         */
        public void openGui()
        {
            @Nullable final Window window = getWindow();
            if (window != null)
            {
                window.open();
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
        }
    }
}
