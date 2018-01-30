package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemBuildingDataStore;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.blocks.*;
import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.views.BuildingBuilderView;
import com.minecolonies.coremod.colony.requestsystem.requesters.BuildingBasedRequester;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import com.minecolonies.coremod.inventory.api.CombinedItemHandler;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.tileentities.TileEntityRack;
import com.minecolonies.coremod.util.BuildingUtils;
import com.minecolonies.coremod.util.ColonyUtils;
import com.minecolonies.coremod.util.StructureWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.Suppression.*;

/**
 * Base building class, has all the foundation for what a building stores and does.
 * <p>
 * We suppress the warning which warns you about referencing child classes in the parent because that's how we register the instances of the childClasses
 * to their views and blocks.
 */
@SuppressWarnings("squid:S2390")
public abstract class AbstractBuilding implements IRequestResolverProvider, IRequester, ICapabilityProvider
{

    protected static final int CONST_DEFAULT_MAX_BUILDING_LEVEL = 5;

    /**
     * Tag if the building has no workOrder.
     */
    public static final int NO_WORK_ORDER = 0;

    /**
     * Max priority of a building.
     */
    private static final int MAX_PRIO = 10;

    /**
     * Map to resolve names to class.
     */
    @NotNull
    private static final Map<String, Class<?>>   nameToClassMap               = new TreeMap<>();
    /**
     * Map to resolve classes to name.
     */
    @NotNull
    private static final Map<Class<?>, String>   classToNameMap               = new HashMap<>();
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
        addMapping("Cook", BuildingCook.class, BuildingCook.View.class, BlockHutCook.class);
        addMapping("Barracks", BuildingBarracks.class, BuildingBarracks.View.class, BlockHutBarracks.class);
        addMapping("BarracksTower", BuildingBarracksTower.class, BuildingBarracksTower.View.class, BlockHutBarracksTower.class);
        addMapping("Shepherd", BuildingShepherd.class, BuildingShepherd.View.class, BlockHutShepherd.class);
        addMapping("Cowboy", BuildingCowboy.class, BuildingCowboy.View.class, BlockHutCowboy.class);
        addMapping("SwingHerder", BuildingSwineHerder.class, BuildingSwineHerder.View.class, BlockHutSwineHerder.class);
        addMapping("ChickenHerder", BuildingChickenHerder.class, BuildingChickenHerder.View.class, BlockHutChickenHerder.class);
        addMapping("Smeltery", BuildingSmeltery.class, BuildingSmeltery.View.class, BlockHutSmeltery.class);
    }
    /**
     * List of items the worker should keep.
     */
    protected final Map<Predicate<ItemStack>, Integer> keepX = new HashMap<>();
    /**
     * A list which contains the position of all containers which belong to the
     * worker building.
     */
    private final List<BlockPos> containerList = new ArrayList<>();
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
     * The data store id for request system related data.
     */
    @NotNull
    private IToken<?> rsDataStoreToken;

    /**
     * The ID of the building. Needed in the request system to identify it.
     */
    private IRequester requester;
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
    private boolean isBuildingMirrored = false;
    /**
     * The building style.
     */
    private String style = "wooden";
    /**
     * Made to check if the building has to update the server/client.
     */
    private boolean dirty = false;
    /**
     * Corners of the building.
     */
    private int cornerX1;
    private int cornerX2;
    private int cornerZ1;
    private int cornerZ2;
    /**
     * Priority of the building in the pickUpList.
     */
    private int pickUpPriority = 1;
    /**
     * Is being gathered right now
     */
    private boolean beingGathered = false;
    /**
     * Height of the building.
     */
    private int height;

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

        this.requester = StandardFactoryController.getInstance().getNewInstance(TypeToken.of(BuildingBasedRequester.class), this);
        setupRsDataStore();
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
      @NotNull final Class<? extends AbstractBuildingView> viewClass,
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
        final StructureName sn = new StructureName(Structures.SCHEMATICS_PREFIX, style, this.getSchematicName() + testLevel);

        if (!Structures.hasMD5(sn))
        {
            final StructureName newStructureName = Structures.getStructureNameByMD5(md5);
            if (newStructureName != null
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
        isBuildingMirrored = compound.getBoolean(TAG_MIRROR);

        if (compound.hasKey(TAG_CORNER1))
        {
            this.cornerX1 = compound.getInteger(TAG_CORNER1);
            this.cornerX2 = compound.getInteger(TAG_CORNER2);
            this.cornerZ1 = compound.getInteger(TAG_CORNER3);
            this.cornerZ2 = compound.getInteger(TAG_CORNER4);
        }

        if (compound.hasKey(TAG_HEIGHT))
        {
            this.height = compound.getInteger(TAG_HEIGHT);
        }

        loadRequestSystemFromNBT(compound);

        if (compound.hasKey(TAG_PRIO))
        {
            this.pickUpPriority = compound.getInteger(TAG_PRIO);
        }
    }

    /**
     * Children must return the name of their structure.
     *
     * @return StructureProxy name.
     */
    public abstract String getSchematicName();

    private void loadRequestSystemFromNBT(final NBTTagCompound compound)
    {
        if (compound.hasKey(TAG_REQUESTOR_ID))
        {
            this.requester = StandardFactoryController.getInstance().deserialize(compound.getCompoundTag(TAG_REQUESTOR_ID));
        }
        else
        {
            this.requester = StandardFactoryController.getInstance().getNewInstance(TypeToken.of(BuildingBasedRequester.class), this);
        }

        if (compound.hasKey(TAG_RS_BUILDING_DATASTORE))
        {
            this.rsDataStoreToken = StandardFactoryController.getInstance().deserialize(compound.getCompoundTag(TAG_RS_BUILDING_DATASTORE));
        }
        else
        {
            setupRsDataStore();
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

        if (building != null && parent.getWorld() != null)
        {
            final WorkOrderBuild workOrder = new WorkOrderBuild(building, 1);
            final StructureWrapper wrapper = new StructureWrapper(parent.getWorld(), workOrder.getStructureName());
            final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners
              = ColonyUtils.calculateCorners(building.getLocation(),
              parent.getWorld(),
              wrapper,
              workOrder.getRotation(parent.getWorld()),
              workOrder.isMirrored());
            building.setCorners(corners.getFirst().getFirst(), corners.getFirst().getSecond(), corners.getSecond().getFirst(), corners.getSecond().getSecond());
            building.setHeight(wrapper.getHeight());
            ConstructionTapeHelper.placeConstructionTape(building.getLocation(), corners, parent.getWorld());
        }
        return building;
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
     * Sets the corners of the building based on the schematic.
     *
     * @param x1 the first x corner.
     * @param x2 the second x corner.
     * @param z1 the first z corner.
     * @param z2 the second z corner.
     */
    public void setCorners(final int x1, final int x2, final int z1, final int z2)
    {
        this.cornerX1 = x1;
        this.cornerX2 = x2;
        this.cornerZ1 = z1;
        this.cornerZ2 = z2;
    }

    /**
     * Set the height of the building.
     *
     * @param height the height to set.
     */
    public void setHeight(final int height)
    {
        this.height = height;
    }

    /**
     * Create a AbstractBuilding View given it's saved NBTTagCompound.
     *
     * @param colony The owning colony.
     * @param id     Chunk coordinate of the block a view is created for.
     * @param buf    The network data.
     * @return {@link AbstractBuildingView} created from reading the buf.
     */
    @Nullable
    public static AbstractBuildingView createBuildingView(final ColonyView colony, final BlockPos id, @NotNull final ByteBuf buf)
    {
        @Nullable AbstractBuildingView view = null;
        @Nullable Class<?> oclass = null;

        try
        {
            final int typeHash = buf.readInt();
            oclass = classNameHashToViewClassMap.get(typeHash);

            if (oclass != null)
            {
                final Constructor<?> constructor = oclass.getDeclaredConstructor(ColonyView.class, BlockPos.class);
                view = (AbstractBuildingView) constructor.newInstance(colony, id);
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
     * executed when a new day start.
     */
    public void onWakeUp()
    {
        /**
         * Buildings override this if required.
         */
    }

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
            final StructureName structureName = new StructureName(Structures.SCHEMATICS_PREFIX, style, this.getSchematicName() + buildingLevel);
            if (Structures.hasMD5(structureName))
            {
                compound.setString(TAG_SCHEMATIC_MD5, Structures.getMD5(structureName.toString()));
            }
        }

        compound.setInteger(TAG_BUILDING_LEVEL, buildingLevel);
        compound.setInteger(TAG_ROTATION, rotation);
        compound.setString(TAG_STYLE, style);


        @NotNull final NBTTagList containerTagList = new NBTTagList();
        for (@NotNull final BlockPos pos : containerList)
        {
            containerTagList.appendTag(NBTUtil.createPosTag(pos));
        }
        compound.setTag(TAG_CONTAINERS, containerTagList);
        compound.setBoolean(TAG_MIRROR, isBuildingMirrored);

        compound.setInteger(TAG_CORNER1, this.cornerX1);
        compound.setInteger(TAG_CORNER2, this.cornerX2);
        compound.setInteger(TAG_CORNER3, this.cornerZ1);
        compound.setInteger(TAG_CORNER4, this.cornerZ2);

        compound.setInteger(TAG_HEIGHT, this.height);

        writeRequestSystemToNBT(compound);

        compound.setInteger(TAG_PRIO, this.pickUpPriority);
    }

    private void writeRequestSystemToNBT(final NBTTagCompound compound)
    {
        compound.setTag(TAG_RS_BUILDING_DATASTORE, StandardFactoryController.getInstance().serialize(rsDataStoreToken));
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
        colony.getBuildingManager().removeBuilding(this, colony.getPackageManager().getSubscribers());
    }

    /**
     * Method to do things when a block is destroyed.
     */
    public void onDestroyed()
    {
        final TileEntityColonyBuilding tileEntityNew = this.getTileEntity();
        final World world = colony.getWorld();
        final Block block = world.getBlockState(this.location).getBlock();

        if (tileEntityNew != null)
        {
            InventoryHelper.dropInventoryItems(world, this.location, (IInventory) tileEntityNew);
            world.updateComparatorOutputLevel(this.location, block);
        }

        ConstructionTapeHelper.removeConstructionTape(getCorners(), world);
    }

    /**
     * Returns the tile entity that belongs to the colony building.
     *
     * @return {@link TileEntityColonyBuilding} object of the building.
     */
    public TileEntityColonyBuilding getTileEntity()
    {
        if ((tileEntity == null || tileEntity.isInvalid())
              && colony != null
              && colony.getWorld() != null
              && getLocation() != null
              && colony.getWorld().getBlockState(getLocation())
                   != null && colony.getWorld().getBlockState(this.getLocation()).getBlock() instanceof AbstractBlockHut)
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
     * Get all the corners of the building based on the schematic.
     *
     * @return the corners.
     */
    public Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> getCorners()
    {
        return new Tuple<>(new Tuple<>(cornerX1, cornerX2), new Tuple<>(cornerZ1, cornerZ2));
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
     * Sets the tile entity for the building.
     *
     * @param te {@link TileEntityColonyBuilding} that will fill the {@link #tileEntity} field.
     */
    public void setTileEntity(final TileEntityColonyBuilding te)
    {
        tileEntity = te;
    }

    /**
     * Deconstruct the building on destroyed.
     */
    public void deconstruct()
    {
        for (int x = cornerX1; x < cornerX2; x++)
        {
            for (int z = cornerZ1; z < cornerZ2; z++)
            {
                for (int y = getLocation().getY() - 1; y < getLocation().getY() + this.height; y++)
                {
                    getColony().getWorld().destroyBlock(new BlockPos(x, y, z), false);
                }
            }
        }
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
     *
     * @param player the requesting player.
     */
    public void requestUpgrade(final EntityPlayer player)
    {
        if (buildingLevel < getMaxBuildingLevel())
        {
            requestWorkOrder(buildingLevel + 1);
        }
        else
        {
            player.sendMessage(new TextComponentTranslation("com.minecolonies.coremod.worker.noUpgrade"));
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
    protected void requestWorkOrder(final int level)
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
     * Marks the instance and the building dirty.
     */
    public final void markDirty()
    {
        dirty = true;
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
     * <p>
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

                final int citizenThatIsBuilding = o.getClaimedBy();
                final CitizenData data = colony.getCitizenManager().getCitizen(citizenThatIsBuilding);
                if (data != null && data.getWorkBuilding() != null)
                {
                    data.getWorkBuilding().cancelAllRequestsOfCitizen(data);
                }
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
        this.markDirty();
    }

    /**
     * Get the height of the building.
     *
     * @return the height..
     */
    public int getHeight()
    {
        return this.height;
    }

    /**
     * Called upon completion of an upgrade process.
     * We suppress this warning since this parameter will be used in child classes which override this method.
     *
     * @param newLevel The new level.
     */
    @SuppressWarnings("squid:S1172")
    public void onUpgradeComplete(final int newLevel)
    {
        final WorkOrderBuild workOrder = new WorkOrderBuild(this, newLevel);
        final StructureWrapper wrapper = new StructureWrapper(colony.getWorld(), workOrder.getStructureName());
        final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners
          = ColonyUtils.calculateCorners(this.getLocation(),
          colony.getWorld(),
          wrapper,
          workOrder.getRotation(colony.getWorld()),
          workOrder.isMirrored());
        this.height = wrapper.getHeight();
        this.setCorners(corners.getFirst().getFirst(), corners.getFirst().getSecond(), corners.getSecond().getFirst(), corners.getSecond().getSecond());
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
        buf.writeInt(getPickUpPriority());
        buf.writeInt(getCurrentWorkOrderLevel());
        ByteBufUtils.writeUTF8String(buf, style);
        ByteBufUtils.writeUTF8String(buf, this.getSchematicName());
        buf.writeInt(rotation);
        buf.writeBoolean(isBuildingMirrored);
        final NBTTagCompound requestSystemCompound = new NBTTagCompound();
        writeRequestSystemToNBT(requestSystemCompound);

        ByteBufUtils.writeTag(buf, requestSystemCompound);
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
     * Get the pick up priority of the building.
     *
     * @return the priority, an integer.
     */
    public int getPickUpPriority()
    {
        return this.pickUpPriority;
    }

    /**
     * Returns the mirror of the current building.
     *
     * @return boolean value of the mirror.
     */
    public boolean isMirrored()
    {
        return isBuildingMirrored;
    }

    /**
     * Register a block and position.
     * We suppress this warning since this parameter will be used in child classes which override this method.
     *
     * @param block to be registered
     * @param pos   of the block
     */
    @SuppressWarnings("squid:S1172")
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        if (block instanceof BlockContainer || block instanceof BlockMinecoloniesRack)
        {
            addContainerPosition(pos);
        }
    }

    /**
     * Add a new container to the building.
     *
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
     *
     * @param pos position to remove.
     */
    public void removeContainerPosition(final BlockPos pos)
    {
        containerList.remove(pos);
    }

    /**
     * Get all additional containers which belong to the building.
     *
     * @return a copy of the list to avoid currentModification exception.
     */
    public List<BlockPos> getAdditionalCountainers()
    {
        return new ArrayList<>(containerList);
    }

    /**
     * Increase or decrease the current pickup priority.
     *
     * @param value the new prio to add to.
     */
    public void alterPickUpPriority(final int value)
    {
        if (this.pickUpPriority + value < 1)
        {
            this.pickUpPriority = 1;
        }
        else if (this.pickUpPriority + value > MAX_PRIO)
        {
            this.pickUpPriority = MAX_PRIO;
        }
        else
        {
            this.pickUpPriority += value;
        }
    }

    /**
     * Check if a building is being gathered.
     *
     * @return true if so.
     */
    public boolean isBeingGathered()
    {
        return this.beingGathered;
    }

    /**
     * Set if a building is being gathered.
     *
     * @param gathering value to set.
     */
    public void setBeingGathered(final boolean gathering)
    {
        this.beingGathered = gathering;
    }

    /**
     * Calculates the area of the building.
     *
     * @param world the world.
     * @return the AxisAlignedBB.
     */
    public AxisAlignedBB getTargetableArea(final World world)
    {
        return BuildingUtils.getTargetAbleArea(world, this);
    }

    //------------------------- Starting Required Tools/Item handling -------------------------//

    @Override
    public int hashCode()
    {
        return (int) (31 * this.getID().toLong());
    }

    @Override
    public boolean equals(final Object o)
    {
        return o instanceof AbstractBuilding && ((AbstractBuilding) o).getID().equals(this.getID());
    }

    /**
     * Try to transfer a stack to one of the inventories of the building.
     *
     * @param stack the stack to transfer.
     * @param world the world to do it in.
     * @return The {@link ItemStack} as that is left over, might be {@link ItemStackUtils#EMPTY} if the stack was completely accepted
     */
    public ItemStack transferStack(@NotNull final ItemStack stack, @NotNull final World world)
    {
        if (tileEntity == null || InventoryUtils.isProviderFull(tileEntity))
        {
            final Iterator<BlockPos> posIterator = containerList.iterator();
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
     * Check if the worker requires a certain amount of that item and the alreadykept list contains it.
     * Always leave one stack behind if the worker requires a certain amount of it. Just to be sure.
     *
     * @param stack            the stack to check it with.
     * @param localAlreadyKept already kept items.
     * @return true if it should be leave it behind.
     */
    public boolean buildingRequiresCertainAmountOfItem(final ItemStack stack, final List<ItemStorage> localAlreadyKept)
    {
        for (final Map.Entry<Predicate<ItemStack>, Integer> entry : getRequiredItemsAndAmount().entrySet())
        {
            if (entry.getKey().test(stack))
            {
                final ItemStorage kept = ItemStorage.getItemStackOfListMatchingPredicate(localAlreadyKept, entry.getKey());
                if (kept != null)
                {
                    if (kept.getAmount() >= entry.getValue())
                    {
                        return false;
                    }

                    localAlreadyKept.remove(kept);
                    kept.setAmount(kept.getAmount() + ItemStackUtils.getSize(stack));
                    localAlreadyKept.add(kept);
                    return true;
                }

                localAlreadyKept.add(new ItemStorage(stack));
                return true;
            }
        }
        return false;
    }

    /**
     * Override this method if you want to keep an amount of items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    public Map<Predicate<ItemStack>, Integer> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Integer> toKeep = new HashMap<>();
        toKeep.putAll(keepX);
        final IRequestManager manager = colony.getRequestManager();
        toKeep.put(stack -> this.getOpenRequestsByCitizen().values().stream()
                .anyMatch(list -> list.stream()
                        .anyMatch(token -> manager.getRequestForToken(token) instanceof IDeliverable
                                && ((IDeliverable) manager.getRequestForToken(token).getRequest()).matches(stack))), Integer.MAX_VALUE);

        return toKeep;
    }

    /**
     * Try to transfer a stack to one of the inventories of the building and force the transfer.
     *
     * @param stack the stack to transfer.
     * @param world the world to do it in.
     * @return the itemStack which has been replaced or the itemStack which could not be transfered
     */
    @Nullable
    public ItemStack forceTransferStack(final ItemStack stack, final World world)
    {
        if (tileEntity == null)
        {
            for (final BlockPos pos : containerList)
            {
                final TileEntity tempTileEntity = world.getTileEntity(pos);
                if (tempTileEntity instanceof TileEntityChest && !InventoryUtils.isProviderFull(tempTileEntity))
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

    @Nullable
    private ItemStack forceItemStackToProvider(@NotNull final ICapabilityProvider provider, @NotNull final ItemStack itemStack)
    {
        final List<ItemStorage> localAlreadyKept = new ArrayList<>();
        return InventoryUtils.forceItemStackToProvider(provider, itemStack, (ItemStack stack) -> EntityAIWorkDeliveryman.workerRequiresItem(this, stack, localAlreadyKept));
    }

    //------------------------- Ending Required Tools/Item handling -------------------------//

    /**
     * Sets the mirror of the current building.
     */
    public void invertMirror()
    {
        this.isBuildingMirrored = !isBuildingMirrored;
    }

    //------------------------- !START! RequestSystem handling for minecolonies buildings -------------------------//

    private IRequestSystemBuildingDataStore getDataStore()
    {
        return colony.getRequestManager().getDataStoreManager().get(rsDataStoreToken, TypeConstants.REQUEST_SYSTEM_BUILDING_DATA_STORE);
    }

    private Map<TypeToken<?>, Collection<IToken<?>>> getOpenRequestsByRequestableType()
    {
        return getDataStore().getOpenRequestsByRequestableType();
    }

    private Map<Integer, Collection<IToken<?>>> getOpenRequestsByCitizen()
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

    public <R extends IRequestable> IToken<?> createRequest(@NotNull final CitizenData citizenData, @NotNull final R requested)
    {
        IToken requestToken = colony.getRequestManager().createRequest(requester, requested);

        addRequestToMaps(citizenData.getId(), requestToken, TypeToken.of(requested.getClass()));

        colony.getRequestManager().assignRequest(requestToken);

        markDirty();

        return requestToken;
    }

    /**
     * Internal method used to register a new Request to the request maps.
     * Helper method.
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

    public boolean hasWorkerOpenRequests(@NotNull final CitizenData citizen)
    {
        return !getOpenRequests(citizen).isEmpty();
    }

    @SuppressWarnings(RAWTYPES)
    public ImmutableList<IRequest> getOpenRequests(@NotNull final CitizenData data)
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

    @SuppressWarnings(RAWTYPES)
    public boolean hasWorkerOpenRequestsFiltered(@NotNull final CitizenData citizen, @NotNull final Predicate<IRequest> selectionPredicate)
    {
        return getOpenRequests(citizen).stream().anyMatch(selectionPredicate);
    }

    public <R> boolean hasWorkerOpenRequestsOfType(@NotNull final CitizenData citizenData, final TypeToken<R> requestType)
    {
        return !getOpenRequestsOfType(citizenData, requestType).isEmpty();
    }

    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED, RAWTYPES})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfType(
      @NotNull final CitizenData citizenData,
      final TypeToken<R> requestType)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                                      .filter(request -> {
                                          final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                                          return requestTypes.contains(requestType);
                                      })
                                      .map(request -> (IRequest<? extends R>) request)
                                      .iterator());
    }

    public boolean hasCitizenCompletedRequests(@NotNull final CitizenData data)
    {
        return !getCompletedRequests(data).isEmpty();
    }

    @SuppressWarnings(RAWTYPES)
    public ImmutableList<IRequest> getCompletedRequests(@NotNull final CitizenData data)
    {
        if (!getCompletedRequestsByCitizen().containsKey(data.getId()))
        {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(getCompletedRequestsByCitizen().get(data.getId()).stream()
                                      .map(getColony().getRequestManager()::getRequestForToken).filter(Objects::nonNull).iterator());
    }

    @SuppressWarnings({GENERIC_WILDCARD, RAWTYPES, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfType(@NotNull final CitizenData citizenData, final TypeToken<R> requestType)
    {
        return ImmutableList.copyOf(getCompletedRequests(citizenData).stream()
                                      .filter(request -> {
                                          final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                                          return requestTypes.contains(requestType);
                                      })
                                      .map(request -> (IRequest<? extends R>) request)
                                      .iterator());
    }

    @SuppressWarnings({GENERIC_WILDCARD, RAWTYPES, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfTypeFiltered(
      @NotNull final CitizenData citizenData,
      final TypeToken<R> requestType,
      final Predicate<IRequest<? extends R>> filter)
    {
        return ImmutableList.copyOf(getCompletedRequests(citizenData).stream()
                                      .filter(request -> {
                                          final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                                          return requestTypes.contains(requestType);
                                      })
                                      .map(request -> (IRequest<? extends R>) request)
                                      .filter(filter)
                                      .iterator());
    }

    public void markRequestAsAccepted(@NotNull final CitizenData data, @NotNull final IToken<?> token)
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

    public void cancelAllRequestsOfCitizen(@NotNull final CitizenData data)
    {
        getOpenRequests(data).forEach(request ->
        {
            getColony().getRequestManager().updateRequestState(request.getToken(), RequestState.CANCELLED);

            if (getOpenRequestsByRequestableType().containsKey(TypeToken.of(request.getRequest().getClass())))
            {
                getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).remove(request.getToken());
                if (getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).isEmpty())
                {
                    getOpenRequestsByRequestableType().remove(TypeToken.of(request.getRequest().getClass()));
                }
            }

            getCitizensByRequest().remove(request.getToken());
        });

        getCompletedRequests(data).forEach(request -> getColony().getRequestManager().updateRequestState(request.getToken(), RequestState.RECEIVED));

        if (getOpenRequestsByCitizen().containsKey(data.getId()))
        {
            getOpenRequestsByCitizen().remove(data.getId());
        }

        if (getCompletedRequestsByCitizen().containsKey(data.getId()))
        {
            getCompletedRequestsByCitizen().remove(data.getId());
        }

        markDirty();
    }

    /**
     * Overrule the next open request with a give stack.
     * <p>
     * We squid:s135 which takes care that there are not too many continue statements in a loop since it makes sense here
     * out of performance reasons.
     *
     * @param stack the stack.
     */
    @SuppressWarnings("squid:S135")
    public void overruleNextOpenRequestWithStack(@NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return;
        }

        for (final int citizenId : getOpenRequestsByCitizen().keySet())
        {
            final CitizenData data = getColony().getCitizenManager().getCitizen(citizenId);

            if (data == null)
            {
                continue;
            }

            final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(getOpenRequestsOfType(data, TypeConstants.DELIVERABLE), stack);

            if (target == null)
            {
                continue;
            }

            getColony().getRequestManager().overruleRequest(target.getToken(), stack.copy());
            return;
        }
    }

    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED, RAWTYPES})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfTypeFiltered(
      @NotNull final CitizenData citizenData,
      final TypeToken<R> requestType,
      final Predicate<IRequest<? extends R>> filter)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                                      .filter(request -> {
                                          final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                                          return requestTypes.contains(requestType);
                                      })
                                      .map(request -> (IRequest<? extends R>) request)
                                      .filter(filter)
                                      .iterator());
    }

    public boolean overruleNextOpenRequestOfCitizenWithStack(@NotNull final CitizenData citizenData, @NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return false;
        }

        final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(getOpenRequestsOfType(citizenData, TypeConstants.DELIVERABLE),stack);

        if (target == null)
        {
            return false;
        }

        getColony().getRequestManager().overruleRequest(target.getToken(), stack.copy());
        return true;
    }

    private IRequest<? extends IDeliverable> getFirstOverullingRequestFromInputList(@NotNull Collection<IRequest<? extends IDeliverable>> queue, @NotNull final ItemStack stack)
    {
        if (queue.isEmpty())
        {
            return null;
        }

        return queue
                 .stream()
                 .filter(request -> request.getRequest().matches(stack))
                 .findFirst()
                 .orElseGet(() ->
                              getFirstOverullingRequestFromInputList(queue
                                                                       .stream()
                                                                       .flatMap(r -> flattenDeliverableChildRequests(r).stream())
                                                                       .collect(Collectors.toList()),
                                stack));
    }

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

    @Override
    public IToken<?> getRequesterId()
    {
        return getToken();
    }

    @Override
    public IToken<?> getToken()
    {
        return requester.getRequesterId();
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> getResolvers()
    {
        return ImmutableList.of(new BuildingRequestResolver(getRequester().getRequesterLocation(), getColony().getRequestManager().getFactoryController().getNewInstance(
          TypeConstants.ITOKEN)));
    }

    public IRequester getRequester()
    {
        return requester;
    }

    @NotNull
    @Override
    public ILocation getRequesterLocation()
    {
        return getRequester().getRequesterLocation();
    }

    @Override
    public void onRequestComplete(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        final Integer citizenThatRequested = getCitizensByRequest().remove(token);
        getOpenRequestsByCitizen().get(citizenThatRequested).remove(token);

        if (getOpenRequestsByCitizen().get(citizenThatRequested).isEmpty())
        {
            getOpenRequestsByCitizen().remove(citizenThatRequested);
        }

        final IRequest<?> requestThatCompleted = getColony().getRequestManager().getRequestForToken(token);
        getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).remove(token);

        if (getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).isEmpty())
        {
            getOpenRequestsByRequestableType().remove(TypeToken.of(requestThatCompleted.getRequest().getClass()));
        }

        if (!getCompletedRequestsByCitizen().containsKey(citizenThatRequested))
        {
            getCompletedRequestsByCitizen().put(citizenThatRequested, new ArrayList<>());
        }
        getCompletedRequestsByCitizen().get(citizenThatRequested).add(token);

        markDirty();
    }

    @Override
    @NotNull
    public void onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IToken token)
    {
        final int citizenThatRequested = getCitizensByRequest().remove(token);
        getOpenRequestsByCitizen().get(citizenThatRequested).remove(token);

        if (getOpenRequestsByCitizen().get(citizenThatRequested).isEmpty())
        {
            getOpenRequestsByCitizen().remove(citizenThatRequested);
        }

        final IRequest<?> requestThatCompleted = getColony().getRequestManager().getRequestForToken(token);
        if (requestThatCompleted != null)
        {
            if (getOpenRequestsByRequestableType().containsKey(TypeToken.of(requestThatCompleted.getRequest().getClass())))
            {
                getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).remove(token);

                if (getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).isEmpty())
                {
                    getOpenRequestsByRequestableType().remove(TypeToken.of(requestThatCompleted.getRequest().getClass()));
                }
            }
        }

        //Check if the citizen did not die.
        if (getColony().getCitizenManager().getCitizen(citizenThatRequested) != null)
        {
            getColony().getCitizenManager().getCitizen(citizenThatRequested).onRequestCancelled(token);
        }
        markDirty();
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        if (!getCitizensByRequest().containsKey(token))
        {
            return new TextComponentString("<UNKNOWN>");
        }

        final Integer citizenData = getCitizensByRequest().get(token);
        return new TextComponentString(this.getSchematicName() + " " + getColony().getCitizenManager().getCitizen(citizenData).getName());
    }

    public Optional<CitizenData> getCitizenForRequest(@NotNull final IToken token)
    {
        if (!getCitizensByRequest().containsKey(token) || getColony() == null)
        {
            return Optional.empty();
        }

        final int citizenID = getCitizensByRequest().get(token);
        if(getColony().getCitizenManager().getCitizen(citizenID) == null)
        {
            return Optional.empty();
        }

        return Optional.of(getColony().getCitizenManager().getCitizen(citizenID));
    }


    //------------------------- !END! RequestSystem handling for minecolonies buildings -------------------------//

    //------------------------- !Start! Capabilities handling for minecolonies buildings -------------------------//

    @Override
    public boolean hasCapability(
      @Nonnull final Capability<?> capability, @Nullable final EnumFacing facing)
    {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing == null;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing == null)
        {
            final Set<ICapabilityProvider> providers = new HashSet<>();

            //Add myself
            providers.add(getTileEntity());

            //Add additional containers
            providers.addAll(getAdditionalCountainers().stream()
                               .map(getTileEntity().getWorld()::getTileEntity)
                               .filter(entity -> (entity instanceof TileEntityChest) || (entity instanceof TileEntityRack))
                               .collect(Collectors.toSet()));
            providers.removeIf(Objects::isNull);

            //Map all providers to IItemHandlers.
            final Set<IItemHandlerModifiable> modifiables = providers
                                                              .stream()
                                                              .flatMap(provider -> InventoryUtils.getItemHandlersFromProvider(provider).stream())
                                                              .filter(handler -> handler instanceof IItemHandlerModifiable)
                                                              .map(handler -> (IItemHandlerModifiable) handler)
                                                              .collect(Collectors.toSet());

            return (T) new CombinedItemHandler(getSchematicName(), modifiables.toArray(new IItemHandlerModifiable[modifiables.size()]));
        }

        return null;
    }

    //------------------------- !End! Capabilities handling for minecolonies buildings -------------------------//
}
