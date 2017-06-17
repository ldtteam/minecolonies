package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.blocks.*;
import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.colony.buildings.views.BuildingBuilderView;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_HAND;

/**
 * Base building class, has all the foundation for what a building stores and does.
 */
public abstract class AbstractBuilding
{
    /**
     * Tag used to store the containers to NBT.
     */
    private static final String TAG_CONTAINERS = "Containers";

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
     * The tag to store the md5 hash of the schematic.
     */
    private static final String TAG_SCHEMATIC_MD5 = "schematicMD5";

    /**
     * The tag to store the mirror of the building.
     */
    private static final String TAG_MIRROR = "mirror";

    /**
     * The tag to store the style of the building.
     */
    private static final String TAG_STYLE = "style";
    private static final int NO_WORK_ORDER = 0;

    /**
     * A list which contains the position of all containers which belong to the worker building.
     */
    private final List<BlockPos> containerList = new ArrayList<>();

    /**
     * Map to resolve names to class.
     */
    @NotNull
    private static final Map<String, Class<?>>   nameToClassMap               = new TreeMap<>();

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
    private static final Map<Integer, Class<?>>  classNameHashToViewClassMap  = new HashMap<>();
    /*
     * Add all the mappings.
     */
    static
    {
        addMapping("Baker", BuildingBaker.class, BuildingBaker.View.class, BlockHutBaker.class);
        addMapping("Blacksmith", BuildingBlacksmith.class, BuildingBlacksmith.View.class, BlockHutBlacksmith.class);
        addMapping("Builder", BuildingBuilder.class, BuildingBuilderView.class, BlockHutBuilder.class);
        addMapping("Home", BuildingHome.class, BuildingHome.View.class, BlockHutCitizen.class);
        addMapping("Farmer", BuildingFarmer.class, BuildingFarmer.View.class, BlockHutFarmer.class);
        addMapping("Lumberjack", BuildingLumberjack.class, BuildingLumberjack.View.class, BlockHutLumberjack.class);
        addMapping("Miner", BuildingMiner.class, BuildingMiner.View.class, BlockHutMiner.class);
        addMapping("Stonemason", BuildingStonemason.class, BuildingStonemason.View.class, BlockHutStonemason.class);
        addMapping("TownHall", BuildingTownHall.class, BuildingTownHall.View.class, BlockHutTownHall.class);
        addMapping("Deliveryman", BuildingDeliveryman.class, BuildingDeliveryman.View.class, BlockHutDeliveryman.class);
        addMapping("Fisherman", BuildingFisherman.class, BuildingFisherman.View.class, BlockHutFisherman.class);
        addMapping("GuardTower", BuildingGuardTower.class, BuildingGuardTower.View.class, BlockHutGuardTower.class);
        addMapping("WareHouse", BuildingWareHouse.class, BuildingWareHouse.View.class, BlockHutWareHouse.class);
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
     * A list of ItemStacks with needed items and their quantity.
     * This list is a diff between itemsNeeded in AbstractEntityAiBasic and
     * the players inventory and their hut combined.
     * So look here for what is currently still needed
     * to fulfill the workers needs.
     * <p>
     * Will be cleared on restart, be aware!
     */
    @NotNull
    private List<ItemStack> itemsCurrentlyNeeded = new ArrayList<>();
    /**
     * the tool currenly needed by the worker.
     */
    private IToolType        neededTool          = ToolType.NONE;
    /**
     * The minimum tool level we need to fulfill the tool request.
     */
    private int              needsToolLevel      = TOOL_LEVEL_HAND;
    /**
     * Checks if there is a ongoing delivery for the currentItem.
     */
    private boolean         onGoingDelivery      = false;
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
     * The mirror of the building.
     */
    private boolean isMirrored = false;

    /**
     * The building style.
     */
    private String style = "wooden";

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
    }

    /**
     * Add build to a mapping.
     * <code>buildingClass</code> needs to extend {@link AbstractBuilding}.
     * <code>parentBlock</code> needs to extend {@link AbstractBlockHut}.
     *
     * @param name          name of building.
     * @param buildingClass subclass of AbstractBuilding, located in {@link com.minecolonies.coremod.colony.buildings}.
     * @param viewClass     subclass of AbstractBuilding.View.
     * @param parentBlock   subclass of Block, located in {@link com.minecolonies.coremod.blocks}.
     */
    private static void addMapping(
            final String name,
            @NotNull final Class<? extends AbstractBuilding> buildingClass,
            @NotNull final Class<? extends AbstractBuilding.View> viewClass,
            @NotNull final Class<? extends AbstractBlockHut> parentBlock)
    {
        final int buildingHashCode = buildingClass.getName().hashCode();

        if (nameToClassMap.containsKey(name) || classNameHashToViewClassMap.containsKey(buildingHashCode))
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
                    classNameHashToViewClassMap.put(buildingHashCode, viewClass);
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
            return null;
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

        final String md5 = compound.getString(TAG_SCHEMATIC_MD5);
        final int testLevel = buildingLevel == 0 ? 1 : buildingLevel;
        final Structures.StructureName sn = new Structures.StructureName(Structures.SCHEMATICS_PREFIX, style, this.getSchematicName() + testLevel);

        if (!Structures.hasMD5(sn))
        {
            final Structures.StructureName newStructureName = Structures.getStructureNameByMD5(md5);
            if (newStructureName!= null
                && newStructureName.getPrefix().equals(sn.getPrefix())
                && newStructureName.getSchematic().equals(sn.getSchematic()))
            {
                //We found the new location for the schematic, update the style accordingly
                style = newStructureName.getStyle();
                Log.getLogger().warn("AbstractBuilding.readFromNBT: " + sn + " have been moved to " + newStructureName);
            }
        }

        if (style.isEmpty())
        {
            Log.getLogger().warn("Loaded empty style, setting to wooden");
            style = "wooden";
        }

        final NBTTagList containerTagList = compound.getTagList(TAG_CONTAINERS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < containerTagList.tagCount(); ++i)
        {
            final NBTTagCompound containerCompound = containerTagList.getCompoundTagAt(i);
            containerList.add(NBTUtil.getPosFromTag(containerCompound));
        }
        isMirrored = compound.getBoolean(TAG_MIRROR);
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
                return null;
            }

            final BlockPos loc = parent.getPosition();
            final Constructor<?> constructor = oclass.getDeclaredConstructor(Colony.class, BlockPos.class);
            building = (AbstractBuilding) constructor.newInstance(colony, loc);
        }
        catch (@NotNull NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException exception)
        {
            Log.getLogger().error(String.format("Unknown Building type '%s' or missing constructor of proper format.", parent.getClass().getName()), exception);
        }

        if(building != null && parent.getWorld() != null)
        {
            ConstructionTapeHelper.placeConstructionTape(building, parent.getWorld());
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
            oclass = classNameHashToViewClassMap.get(typeHash);

            if (oclass != null)
            {
                final Constructor<?> constructor = oclass.getDeclaredConstructor(ColonyView.class, BlockPos.class);
                view = (View) constructor.newInstance(colony, id);
            }
        }
        catch (@NotNull NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException exception)
        {
            Log.getLogger().error(exception);
        }

        if (view == null)
        {
            Log.getLogger().warn("Unknown AbstractBuilding type, missing View subclass, or missing constructor of proper format.");
            return null;
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
            return null;
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
            final Structures.StructureName  structureName = new Structures.StructureName(Structures.SCHEMATICS_PREFIX, style, this.getSchematicName() + buildingLevel);
            if (Structures.hasMD5(structureName))
            {
                compound.setString(TAG_SCHEMATIC_MD5, Structures.getMD5(structureName.toString()));
            }
        }

        compound.setInteger(TAG_BUILDING_LEVEL, buildingLevel);
        compound.setInteger(TAG_ROTATION, rotation);
        compound.setString(TAG_STYLE, style);


        @NotNull final NBTTagList containerTagList = new NBTTagList();
        for (@NotNull final BlockPos pos: containerList)
        {
            containerTagList.appendTag(NBTUtil.createPosTag(pos));
        }
        compound.setTag(TAG_CONTAINERS, containerTagList);
        compound.setBoolean(TAG_MIRROR, isMirrored);
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
            ConstructionTapeHelper.removeConstructionTape(this, world);
        }
        ConstructionTapeHelper.removeConstructionTape(this, world);
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
     * Get the current level of the work order.
     *
     * @return NO_WORK_ORDER if not current work otherwise the level requested.
     */
    private int getCurrentWorkOrderLevel()
    {
        for (@NotNull final WorkOrderBuild o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuild.class))
        {
            if (o.getBuildingLocation().equals(getID()))
            {
                return o.getUpgradeLevel();
            }
        }

        return NO_WORK_ORDER;
    }

    /**
     * Checks if this building have a work order.
     *
     * @return true if the building is building, upgrading or repairing.
     */
    public boolean hasWorkOrder()
    {
        return getCurrentWorkOrderLevel() != NO_WORK_ORDER;
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
        LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), "com.minecolonies.coremod.workOrderAdded");
        markDirty();
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
     * Remove the work order for the building.
     *
     * Remove either the upgrade or repair work order
     */
    public void removeWorkOrder()
    {
        for (@NotNull final WorkOrderBuild o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuild.class))
        {
            if (o.getBuildingLocation().equals(getID()))
            {
                colony.getWorkManager().removeWorkOrder(o.getID());
                markDirty();
                return;
            }
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
        buf.writeInt(getCurrentWorkOrderLevel());
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
        if(colony != null)
        {
            colony.markBuildingsDirty();
        }
    }

    /**
     * register a block and position.
     *
     * @param block to be registered
     * @param pos of the block
     */
    public void registerBlockPosition(@NotNull Block block, @NotNull final BlockPos pos)
    {
        if (block instanceof BlockContainer)
        {
            addContainerPosition(pos);
        }
    }

    /**
     * Add a new container to the building.
     * @param pos position to add.
     */
    public void addContainerPosition(@NotNull final BlockPos pos)
    {
        if (!containerList.contains(pos))
        {
            containerList.add(pos);
        }
    }

    /**
     * Remove a container from the building.
     * @param pos position to remove.
     */
    public void removeContainerPosition(BlockPos pos)
    {
        containerList.remove(pos);
    }

    /**
     * Get all additional containers which belong to the building.
     * @return a copy of the list to avoid currentModification exception.
     */
    public List<BlockPos> getAdditionalCountainers()
    {
        return new ArrayList<>(containerList);
    }

    //------------------------- Starting Required Tools/Item handling -------------------------//

    /**
     * Override this method if you want to keep an amount of items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    public Map<ItemStorage, Integer> getRequiredItemsAndAmount()
    {
        return Collections.emptyMap();
    }

    /**
     * Check if the building is receiving the required items.
     * @return true if so.
     */
    public boolean hasOnGoingDelivery()
    {
        return onGoingDelivery;
    }

    /**
     * Check if the building is receiving the required items.
     * @param valueToSet true or false
     */
    public void setOnGoingDelivery(boolean valueToSet)
    {
        this.onGoingDelivery = valueToSet;
    }

    /**
     * Check if the worker needs anything. Tool or item.
     * @return true if so.
     */
    public boolean needsAnything()
    {
        return !itemsCurrentlyNeeded.isEmpty() || neededTool != ToolType.NONE;
    }

    /**
     * Check if any items are needed at the moment.
     * @return true if so.
     */
    public boolean areItemsNeeded()
    {
        return !itemsCurrentlyNeeded.isEmpty();
    }

    /**
     * Check if the worker requires a specific tool.
     *
     * @param toolType type of tool to check for
     * @return true if so.
     */
    public boolean needsTool(final IToolType toolType)
    {
        return neededTool.equals(toolType);
    }

    /**
     * Set which tool the worker needs.
     *
     * @param neededTool    which tool is needed
     * @param minimalLevel which minimal level for the tool
     */
    public void setNeedsTool(final IToolType neededTool, final int minimalLevel)
    {
        this.neededTool     = neededTool;
        this.needsToolLevel = minimalLevel;
    }

    @Override
    public boolean equals(final Object o)
    {
        return o instanceof AbstractBuilding && ((AbstractBuilding) o).getID().equals(this.getID());
    }

    @Override
    public int hashCode()
    {
        return (int) (31 * this.getID().toLong());
    }

    /**
     * Get which tool the worker needs.
     *
     * @return which tool is needed
     */
    public IToolType getNeedsTool()
    {
        return neededTool;
    }

    /**
     * Get which tool level the worker needs.
     *
     * @return the tool level needed by the worker
     */
    public int getNeededToolLevel()
    {
        return needsToolLevel;
    }

    /**
     * Add a neededItem to the currentlyNeededItem list.
     * @param stack the stack to add.
     */
    public void addNeededItems(@Nullable ItemStack stack)
    {
        if(!ItemStackUtils.isEmpty(stack))
        {
            itemsCurrentlyNeeded.add(stack);
        }
    }

    /**
     * This method makes a copy of the itemsCurrentlyNeeded.
     * Currently every call to this method needs a copy, if for some reason an outside
     * class needs this list and a copy isn't desired, a new method should be created,
     * and this doc should be changed to point to that new method.
     *
     * @return a copy of the itemsCurrentlyNeeded list.
     */
    public List<ItemStack> getCopyOfNeededItems()
    {
        return new ArrayList<>(itemsCurrentlyNeeded);
    }

    /**
     * Getter for the first of the currentlyNeededItems.
     * @return copy of the itemStack.
     */
    @Nullable
    public ItemStack getFirstNeededItem()
    {
        if(itemsCurrentlyNeeded.isEmpty())
        {
            return null;
        }
        return itemsCurrentlyNeeded.get(0).copy();
    }

    /**
     * Clear the currentlyNeededItem list.
     */
    public void clearNeededItems()
    {
        itemsCurrentlyNeeded.clear();
    }

    /**
     * Overwrite the itemsCurrentlyNeededList with a new one.
     * @param newList the new list to set.
     */
    public void setItemsCurrentlyNeeded(@NotNull List<ItemStack> newList)
    {
        this.itemsCurrentlyNeeded = new ArrayList<>(newList);
    }

    /**
     * Try to transfer a stack to one of the inventories of the building.
     * @param stack the stack to transfer.
     * @param world the world to do it in.
     * @return The {@link ItemStack} as that is left over, might be {@link ItemStackUtils#EMPTY} if the stack was completely accepted
     */
    public ItemStack transferStack(@NotNull final ItemStack stack, @NotNull final World world)
    {
        if (tileEntity == null || InventoryUtils.isProviderFull(tileEntity))
        {
            Iterator<BlockPos> posIterator = containerList.iterator();
            @NotNull ItemStack resultStack = stack.copy();

            while (posIterator.hasNext() && !ItemStackUtils.isEmpty(resultStack))
            {
                final BlockPos pos = posIterator.next();
                final TileEntity tempTileEntity = world.getTileEntity(pos);
                if (tempTileEntity instanceof TileEntityChest && !InventoryUtils.isProviderFull(tempTileEntity))
                {
                    resultStack = InventoryUtils.addItemStackToProviderWithResult(tempTileEntity, stack);
                }
            }

            return resultStack;
        }
        else
        {
            return InventoryUtils.addItemStackToProviderWithResult(tileEntity, stack);
        }
    }

    /**
     * Try to transfer a stack to one of the inventories of the building and force the transfer.
     * @param stack the stack to transfer.
     * @param world the world to do it in.
     * @return the itemStack which has been replaced or the itemStack which could not be transfered
     */
    @Nullable
    public ItemStack forceTransferStack(final ItemStack stack, final World world)
    {
        if(tileEntity == null)
        {
            for(final BlockPos pos: containerList)
            {
                final TileEntity tempTileEntity = world.getTileEntity(pos);
                if(tempTileEntity instanceof TileEntityChest && !InventoryUtils.isProviderFull(tempTileEntity))
                {
                    return forceItemStackToProvider(tempTileEntity, stack);
                }
            }
        }
        else
        {
            return forceItemStackToProvider(tileEntity, stack);
        }
        return stack;
    }

    /**
     * Returns the mirror of the current building.
     *
     * @return boolean value of the mirror.
     */
    public boolean isMirrored()
    {
        return isMirrored;
    }

    /**
     * Sets the mirror of the current building.
     */
    public void setMirror()
    {
        this.isMirrored = !isMirrored;
    }

    //------------------------- Ending Required Tools/Item handling -------------------------//

    @Nullable
    private ItemStack forceItemStackToProvider(@NotNull final ICapabilityProvider provider, @NotNull final ItemStack itemStack)
    {
        final List<ItemStorage> localAlreadyKept = new ArrayList<>();
        return InventoryUtils.forceItemStackToProvider(provider, itemStack, (ItemStack stack) -> EntityAIWorkDeliveryman.workerRequiresItem(this, stack, localAlreadyKept));
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
        private int workOrderLevel   = NO_WORK_ORDER;

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
            workOrderLevel = buf.readInt();
        }
    }
}
