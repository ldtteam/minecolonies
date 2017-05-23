package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.ColonyManager;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Utils;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.blocks.*;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.colony.buildings.views.BuildingBuilderView;
import com.minecolonies.coremod.colony.requestsystem.locations.StaticLocation;
import com.minecolonies.coremod.colony.requestsystem.requestable.Tool;
import com.minecolonies.coremod.colony.workorders.AbstractWorkOrderBuild;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.util.LanguageHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Base building class, has all the foundation for what a building stores and does.
 */
public abstract class AbstractBuilding implements IBuilding
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

    /**
     * The tag to store the ID of the building in NBT. Will be regenerated when it does not exist.
     */
    private static final String TAG_ID = "id";

    /**
     * The tag to store the open requests made through this building.
     */
    private static final String TAG_REQUESTS_BY_CITIZENS = "requests";

    /**
     * The tag to store the completed requests made through this building.
     */
    private static final String TAG_COMPLETED_REQUESTS_BY_CITIZEN = "completed_requests";

    /**
     * The tag to store who created the request
     */
    private static final String TAG_REQUESTER = "requester";

    /**
     * The tag to store the id of the request.
     */
    private static final String TAG_REQUEST_TOKEN = "token";

    private static final int NO_WORK_ORDER = 0;

    /**
     * List of all open requests made by this building.
     *
     * The key in this map is the class for the request type.
     * The value is a list of tokens that represent the open requests inside the colony.
     */
    @NotNull
    private final Map<Class, List<IToken>> openRequests = new HashMap<>();

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
    }
    /**
     * A list which contains the position of all containers which belong to the
     * worker building.
     */
    private final List<BlockPos> containerList = new ArrayList<>();
    /**
     * The location of the building.
     */
    private final StaticLocation           location;
    /**
     * The colony the building belongs to.
     */
    @NotNull
    private final Colony                   colony;
    /**
     * The tileEntity of the building.
     */
    private       TileEntityColonyBuilding tileEntity;

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
     * The ID of the building. Needed in the request system to identify it.
     */
    private IToken id;

    /**
     * Keeps track of which citizen created what request. Citizen -> Request direction.
     */
    private HashMap<Integer, Collection<IToken>> citizensByRequests = new HashMap<>();

    /**
     * Keeps track of which citizen has completed requests. Citizen -> Request direction.
     */
    private HashMap<Integer, Collection<IToken>> citizensByCompletedRequests = new HashMap<>();

    /**
     * Keeps track of which citizen created what request. Request -> Citizen direction.
     */
    private HashMap<IToken, Integer> requestsByCitizen = new HashMap<>();

    /**
     * Constructor for a AbstractBuilding.
     *
     * @param colony Colony the building belongs to.
     * @param pos    Location of the building (it's Hut Block).
     */
    protected AbstractBuilding(@NotNull final Colony colony, final BlockPos pos)
    {
        location = new StaticLocation(pos, colony.getDimension());
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
                if (buildingClass.getDeclaredConstructor(Colony.class, BlockPos.class) != null
                      && viewClass.getDeclaredConstructor(ColonyView.class, BlockPos.class, IToken.class) != null)
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
        isMirrored = compound.getBoolean(TAG_MIRROR);

        //Check if this building has an ID already. Else create a new one.
        if (compound.hasKey(TAG_ID))
        {
            //Existing ID
            id = getColony().getFactoryController().deserialize(compound.getCompoundTag(TAG_ID));
        }
        else
        {
            //Not existing.
            id = getColony().getFactoryController().getNewInstance(UUID.randomUUID());
            Log.getLogger().info("Loaded Building without ID. Assigned new one.");
        }

        openRequests.clear();
        requestsByCitizen.clear();
        citizensByRequests.clear();
        citizensByCompletedRequests.clear();

        //Check for open requests.
        if (compound.hasKey(TAG_REQUESTS_BY_CITIZENS))
        {
            //Requests exist. Get the list.
            NBTTagList requests = compound.getTagList(TAG_REQUESTS_BY_CITIZENS, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < requests.tagCount(); i++)
            {
                //For each request attempt the deserialization.
                try
                {
                    NBTTagCompound requestCompound = requests.getCompoundTagAt(i);
                    Integer citizenId = requestCompound.getInteger(TAG_REQUESTER);

                    //Use the factory to get the ID of the request from NBT.
                    IToken requestToken = getColony().getFactoryController().deserialize(requestCompound.getCompoundTag(TAG_REQUEST_TOKEN));

                    //Get the request from the colony. (Attention, request system needs to be deserialised before the Buildings!
                    IRequest request = getColony().getRequestManager().getRequestForToken(requestToken);

                    //Add the request to the map.
                    addRequestToMaps(citizenId, requestToken, request.getRequestType());
                }
                catch (Exception ex)
                {
                    //Failure to deserialize and store the request.
                    //TODO: Keep this in mind when we get the complete callback. Some request registration might have failed.
                    //TODO: If so how to proceed? Probably discard. Citizen will have recreated a request or so. DMan needs to pick it up.
                    Log.getLogger().warn("Failed to deserialize a Request. Somethings might look a bit weird, as it is being skipped.");
                }
            }
        }

        if (compound.hasKey(TAG_COMPLETED_REQUESTS_BY_CITIZEN))
        {
            //Requests exist. Get the list.
            NBTTagList requests = compound.getTagList(TAG_REQUESTS_BY_CITIZENS, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < requests.tagCount(); i++)
            {
                //For each request attempt the deserialization.
                try
                {
                    NBTTagCompound requestCompound = requests.getCompoundTagAt(i);
                    Integer citizenId = requestCompound.getInteger(TAG_REQUESTER);

                    //Use the factory to get the ID of the request from NBT.
                    IToken requestToken = getColony().getFactoryController().deserialize(requestCompound.getCompoundTag(TAG_REQUEST_TOKEN));

                    if (!citizensByCompletedRequests.containsKey(citizenId))
                    {
                        citizensByCompletedRequests.put(citizenId, new ArrayList<>());
                    }
                    citizensByCompletedRequests.get(citizenId).add(requestToken);
                }
                catch (Exception ex)
                {
                    //Failure to deserialize and store the request.
                    //TODO: Keep this in mind when we get the complete callback. Some request registration might have failed.
                    //TODO: If so how to proceed? Probably discard. Citizen will have recreated a request or so. DMan needs to pick it up.
                    Log.getLogger().warn("Failed to deserialize a Request. Somethings might look a bit weird, as it is being skipped.");
                }
            }
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
            building.id = building.getColony().getFactoryController().getNewInstance(UUID.randomUUID());
        }
        catch (@NotNull NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException exception)
        {
            Log.getLogger().error(String.format("Unknown Building type '%s' or missing constructor of proper format.", parent.getClass().getName()), exception);
        }

        if (building != null && parent.getWorld() != null)
        {
            ConstructionTapeHelper.placeConstructionTape(building, parent.getWorld());
        }
        return building;
    }

    /**
     * Create a AbstractBuilding View given it's saved NBTTagCompound.
     *
     * @param colony The owning colony.
     * @param location     Chunk coordinate of the block a view is created for.
     * @param buf    The network data.
     * @return {@link AbstractBuilding.View} created from reading the buf.
     */
    @Nullable
    public static View createBuildingView(final ColonyView colony, final BlockPos location, final IToken id, @NotNull final ByteBuf buf)
    {
        @Nullable View view = null;
        @Nullable Class<?> oclass = null;

        try
        {
            final int typeHash = buf.readInt();
            oclass = classNameHashToViewClassMap.get(typeHash);

            if (oclass != null)
            {
                final Constructor<?> constructor = oclass.getDeclaredConstructor(ColonyView.class, BlockPos.class, IToken.class);
                view = (View) constructor.newInstance(colony, location, id);
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
            BlockPosUtil.writeToNBT(compound, TAG_LOCATION, getLocation().getInDimensionLocation());
            final Structures.StructureName structureName = new Structures.StructureName(Structures.SCHEMATICS_PREFIX, style, this.getSchematicName() + buildingLevel);
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
        compound.setBoolean(TAG_MIRROR, isMirrored);

        NBTTagList requests = new NBTTagList();
        requestsByCitizen.forEach((IToken token, Integer citizenId) ->
        {
            NBTTagCompound requestCompound = new NBTTagCompound();
            requestCompound.setInteger(TAG_REQUESTER, citizenId);
            requestCompound.setTag(TAG_REQUEST_TOKEN, getColony().getFactoryController().serialize(token));

            requests.appendTag(requestCompound);
        });

        compound.setTag(TAG_REQUESTS_BY_CITIZENS, requests);

        NBTTagList completedRequests = new NBTTagList();
        citizensByCompletedRequests.forEach((Integer citizenId, Collection<IToken> tokens) ->
        {
            tokens.forEach((IToken token) ->
            {
                NBTTagCompound requestCompound = new NBTTagCompound();
                requestCompound.setInteger(TAG_REQUESTER, citizenId);
                requestCompound.setTag(TAG_REQUEST_TOKEN, getColony().getFactoryController().serialize(token));

                requests.appendTag(requestCompound);
            });
        });

        compound.setTag(TAG_COMPLETED_REQUESTS_BY_CITIZEN, completedRequests);
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    @Override
    public StaticLocation getLocation()
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
        final Block block = world.getBlockState(this.location.getInDimensionLocation()).getBlock();

        if (tileEntityNew != null)
        {
            InventoryHelper.dropInventoryItems(world, this.location.getInDimensionLocation(), tileEntityNew);
            world.updateComparatorOutputLevel(this.location.getInDimensionLocation(), block);
        }
        ConstructionTapeHelper.removeConstructionTape(this, world);
    }

    @Override
    public TileEntityColonyBuilding getTileEntity()
    {
        if ((tileEntity == null || tileEntity.isInvalid()) && colony.getWorld().getBlockState(location.getInDimensionLocation()).getBlock() != null)
        {
            final TileEntity te = getColony().getWorld().getTileEntity(location.getInDimensionLocation());
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
    @Override
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
        for (@NotNull final AbstractWorkOrderBuild o : colony.getWorkManager().getWorkOrdersOfType(AbstractWorkOrderBuild.class))
        {
            if (o.getBuildingLocation().equals(getID()))
            {
                return;
            }
        }

        colony.getWorkManager().addWorkOrder(new AbstractWorkOrderBuild(this, level));
        LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), "com.minecolonies.coremod.workOrderAdded");
        markDirty();
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    @Override
    public IToken getID()
    {
        return id;
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
        for (@NotNull final AbstractWorkOrderBuild o : colony.getWorkManager().getWorkOrdersOfType(AbstractWorkOrderBuild.class))
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
        for (@NotNull final AbstractWorkOrderBuild o : colony.getWorkManager().getWorkOrdersOfType(AbstractWorkOrderBuild.class))
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

    @Override
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
    @Override
    public int getBuildingLevel()
    {
        return buildingLevel;
    }

    @Override
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
     * Add a new container to the building.
     *
     * @param pos position to add.
     */
    public void addContainerPosition(BlockPos pos)
    {
        containerList.add(pos);
    }

    /**
     * Remove a container from the building.
     *
     * @param pos position to remove.
     */
    public void removeContainerPosition(BlockPos pos)
    {
        containerList.remove(pos);
    }

    @Override
    public List<BlockPos> getAdditionalContainers()
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

    @Override
    public boolean needsAnything()
    {
        return !openRequests.isEmpty();
    }

    @Override
    public boolean areItemsNeeded()
    {
        return !openRequests.get(ItemStack.class).isEmpty();
    }

    @Override
    public boolean requiresTool(String toolClass) {
        if (openRequests.get(Tool.class).isEmpty())
            return false;

        List<IToken> tokenList = openRequests.get(Tool.class);
        for(IToken token : tokenList) {
            IRequest<Tool> toolIRequest = colony.getRequestManager().getRequestForToken(token);

            if (toolIRequest.getRequest().getToolClass().equals(toolClass))
                return true;
        }

        return false;
    }

    /**
     * Check if the worker requires a shovel.
     *
     * @return true if so.
     */
    public boolean needsShovel() { return requiresTool(Utils.SHOVEL); }

    /**
     * Check if the worker requires a axe.
     *
     * @return true if so.
     */
    public boolean needsAxe()
    {
        return requiresTool(Utils.AXE);
    }

    /**
     * Check if the worker requires a hoe.
     *
     * @return true if so.
     */
    public boolean needsHoe()
    {
        return requiresTool(Utils.HOE);
    }

    /**
     * Check if the worker requires a pickaxe.
     *
     * @return true if so.
     */
    public boolean needsPickaxe()
    {
        return requiresTool(Utils.PICKAXE);
    }

    /**
     * Check if the worker requires a weapon.
     *
     * @return true if so.
     */
    public boolean needsWeapon()
    {
        return requiresTool(Utils.WEAPON);
    }

    @Nullable
    public Tool getRequestedToolForClass(String toolClass) {
        if (!requiresTool(toolClass))
            return null;

        List<IToken> tokenList = openRequests.get(Tool.class);
        for(IToken token : tokenList) {
            IRequest<Tool> toolIRequest = colony.getRequestManager().getRequestForToken(token);

            if (toolIRequest.getRequest().getToolClass().equals(toolClass))
                return toolIRequest.getRequest();
        }

        return null;
    }

    /**
     * Check the required pickaxe level..
     *
     * @return the mining level of the pickaxe.
     */
    public int getNeededPickaxeLevel()
    {
        Tool pickaxeRequets = getRequestedToolForClass(Utils.PICKAXE);
        if (pickaxeRequets == null)
        {
            return 0;
        }

        return getRequestedToolForClass(Utils.PICKAXE).getMinLevel();
    }

    /**
     * Check for the required tool and return the describing string.
     *
     * @return the string of the required tool.
     */
    public String getRequiredTool()
    {
        if (!openRequests.containsKey(Tool.class))
        {
            return "";
        }

        IToken firstOpenToolRequestToken = openRequests.get(Tool.class).get(0);
        IRequest<Tool> firstOpenToolRequest = getColony().getRequestManager().getRequestForToken(firstOpenToolRequestToken);

        return firstOpenToolRequest.getRequest().getToolClass();
    }

    /**
     * Try to transfer a stack to one of the inventories of the building.
     *
     * @param stack the stack to transfer.
     * @param world the world to do it in.
     * @return The {@link ItemStack} as that is left over, might be {@link InventoryUtils#EMPTY} if the stack was completely accepted
     */
    public ItemStack transferStack(@NotNull final ItemStack stack, @NotNull final World world)
    {
        if (tileEntity == null || InventoryUtils.isProviderFull(tileEntity))
        {
            Iterator<BlockPos> posIterator = containerList.iterator();
            @NotNull ItemStack resultStack = stack.copy();

            while (posIterator.hasNext() && !InventoryUtils.isItemStackEmpty(resultStack))
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

    /**
     * Returns the mirror of the current building.
     *
     * @return boolean value of the mirror.
     */
    public boolean isMirrored()
    {
        return isMirrored;
    }

    //------------------------- Ending Required Tools/Item handling -------------------------//

    /**
     * Sets the mirror of the current building.
     */
    public void setMirror()
    {
        this.isMirrored = !isMirrored;
    }

    @Override
    public <Request> void createRequest(@NotNull ICitizenData citizenData, @NotNull Request requested)
    {
        IToken requestToken = colony.getRequestManager().createAndAssignRequest(this, requested);

        addRequestToMaps(citizenData.getId(), requestToken, requested.getClass());
    }

    /**
     * Internal method used to register a new Request to the request maps.
     * Helper method.
     *
     * @param citizenId    The id of the citizen.
     * @param requestToken The {@link IToken} that is used to represent the request.
     * @param requested    The class of the type that has been requested eg. {@code ItemStack.class}
     */
    private void addRequestToMaps(@NotNull Integer citizenId, @NotNull IToken requestToken, @NotNull Class requested)
    {
        if (!openRequests.containsKey(requested))
        {
            openRequests.put(requested, new ArrayList<>());
        }
        openRequests.get(requested).add(requestToken);

        requestsByCitizen.put(requestToken, citizenId);

        if (!citizensByRequests.containsKey(citizenId))
        {
            citizensByRequests.put(citizenId, new ArrayList<>());
        }
        citizensByRequests.get(citizenId).add(requestToken);
    }

    @NotNull
    @Override
    public void onRequestComplete(@NotNull final IToken token)
    {
        Integer citizenThatRequested = requestsByCitizen.remove(token);
        citizensByRequests.get(citizenThatRequested).remove(token);

        if (citizensByRequests.get(citizenThatRequested).isEmpty())
        {
            citizensByRequests.remove(citizenThatRequested);
        }

        IRequest requestThatCompleted = getColony().getRequestManager().getRequestForToken(token);
        openRequests.get(requestThatCompleted.getRequestType()).remove(token);

        if (openRequests.get(requestThatCompleted.getRequestType()).isEmpty())
        {
            openRequests.remove(requestThatCompleted.getRequestType());
        }

        if (!citizensByCompletedRequests.containsKey(citizenThatRequested))
        {
            citizensByCompletedRequests.put(citizenThatRequested, new ArrayList<>());
        }
        citizensByCompletedRequests.get(citizenThatRequested).add(token);

        getColony().getCitizen(citizenThatRequested);
    }

    @Override
    public boolean hasWorkerOpenRequests(@NotNull ICitizenData citizen)
    {
        return !getOpenRequests(citizen).isEmpty();
    }

    @Override
    public <Request> boolean hasWorkerOpenRequestsOfType(@NotNull final ICitizenData citizenData, final Class<Request> requestType)
    {
        return getOpenRequests(citizenData).stream()
                 .map(getColony().getRequestManager()::getRequestForToken)
                 .anyMatch(request -> request.getRequestType().equals(requestType));
    }

    @Override
    public ImmutableList<IToken> getOpenRequests(@NotNull final ICitizenData data)
    {
        if (!citizensByRequests.containsKey(data.getId()))
        {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(citizensByRequests.get(data.getId()));
    }

    @Override
    public <Request> ImmutableList<IRequest<Request>> getOpenRequestsOfType(@NotNull final ICitizenData citizenData, final Class<Request> requestType)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                                      .map(getColony().getRequestManager()::getRequestForToken)
                                      .filter(request -> request.getRequestType().equals(requestType))
                                      .map(request -> (IRequest<Request>) request)
                                      .iterator());
    }

    @Override
    public ImmutableList<IToken> getCompletedRequestsForCitizen(@NotNull final ICitizenData data)
    {
        if (!citizensByCompletedRequests.containsKey(data.getId()))
        {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(citizensByCompletedRequests.get(data.getId()));
    }

    @Override
    public void markRequestAsAccepted(@NotNull final ICitizenData data, @NotNull final IToken token) throws IllegalArgumentException
    {
        if (!citizensByCompletedRequests.containsKey(data.getId()) || !citizensByCompletedRequests.get(data).contains(token))
        {
            throw new IllegalArgumentException("The given token " + token + " is not known as a completed request waiting for acceptance by the citizen.");
        }

        citizensByCompletedRequests.get(data.getId()).remove(token);
        if (citizensByCompletedRequests.get(data.getId()).isEmpty())
        {
            citizensByCompletedRequests.remove(data.getId());
        }
    }

    /**
     * The AbstractBuilding View is the client-side representation of a AbstractBuilding.
     * Views contain the AbstractBuilding's data that is relevant to a Client, in a more client-friendly form.
     * Mutable operations on a View result in a message to the server to perform the operation.
     */
    public static class View implements IBuilding
    {
        private final ColonyView     colony;
        @NotNull
        private final StaticLocation location;
        @NotNull
        private final IToken         id;

        private int buildingLevel    = 0;
        private int buildingMaxLevel = 0;
        private int workOrderLevel   = NO_WORK_ORDER;

        /**
         * Creates a building view.
         *
         * @param c ColonyView the building is in.
         * @param l The location of the building.
         */
        protected View(final ColonyView c, @NotNull final BlockPos l, @NotNull final IToken id)
        {
            colony = c;
            location = new StaticLocation(l, c.getDimension());
            this.id = id;
        }

        /**
         * Gets the id for this building.
         *
         * @return A BlockPos because the building ID is its location.
         */
        @NotNull
        public IToken getID()
        {
            // Location doubles as ID
            return id;
        }

        @Override
        public void onUpgradeComplete(final int newLevel)
        {
            //Noop
        }

        /**
         * Gets the location of this building.
         *
         * @return A BlockPos, where this building is.
         */
        @NotNull
        public StaticLocation getLocation()
        {
            return location;
        }

        @NotNull
        @Override
        public void onRequestComplete(@NotNull final IToken token)
        {
            throw new IllegalStateException("Request complete called on the CLIENT side.");
            //NOOP: On the client side this should never be called.
        }

        @Override
        public TileEntity getTileEntity()
        {
            return null;
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

        @Override
        public void setBuildingLevel(final int level)
        {
            throw new IllegalStateException("Cannot set building level on the client side.");
        }

        @Override
        public List<BlockPos> getAdditionalContainers()
        {
            return null;
        }

        @Override
        public boolean needsAnything()
        {
            return false;
        }

        @Override
        public boolean areItemsNeeded()
        {
            return false;
        }

        @Override
        public boolean requiresTool(final String toolClass)
        {
            return false;
        }

        @Override
        public <Request> void createRequest(@NotNull final ICitizenData citizenData, @NotNull final Request requested)
        {
            throw new IllegalStateException("Requests cannot be created on the client side.");
        }

        @Override
        public boolean hasWorkerOpenRequests(@NotNull final ICitizenData citizen)
        {
            //For now returns always false until getOpenRequests is populated properly (if ever)
            return !getOpenRequests(citizen).isEmpty();
        }

        @Override
        public <Request> boolean hasWorkerOpenRequestsOfType(@NotNull final ICitizenData citizenData, final Class<Request> requestType)
        {
            return getOpenRequests(citizenData).stream()
                     .map(getColony().getRequestManager()::getRequestForToken)
                     .anyMatch(request -> request.getRequestType().equals(requestType));
        }

        @Override
        public ImmutableList<IToken> getOpenRequests(@NotNull final ICitizenData data)
        {
            //TODO: Fill properly from Server.
            return ImmutableList.of();
        }

        @Override
        public <Request> ImmutableList<IRequest<Request>> getOpenRequestsOfType(@NotNull final ICitizenData citizenData, final Class<Request> requestType)
        {
            return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                                          .map(getColony().getRequestManager()::getRequestForToken)
                                          .filter(request -> request.getRequestType().equals(requestType))
                                          .map(request -> (IRequest<Request>) request)
                                          .iterator());
        }

        @Override
        public ImmutableList<IToken> getCompletedRequestsForCitizen(@NotNull final ICitizenData data)
        {
            //TODO: Fill properly from Server.
            return ImmutableList.of();
        }

        @Override
        public void markRequestAsAccepted(@NotNull final ICitizenData data, @NotNull final IToken token) throws IllegalArgumentException
        {
            throw new IllegalStateException("Requests cannot be marked as accepted on the client side");
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

        /**
         * Children must return their max building level.
         *
         * @return Max building level.
         */
        @Override
        public int getMaxBuildingLevel() {
            return buildingMaxLevel;
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
        @SideOnly(Side.CLIENT)
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
        @SideOnly(Side.CLIENT)
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
