package com.minecolonies.coremod.colony.buildings.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.blocks.*;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.*;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.*;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.util.ColonyUtils;
import com.minecolonies.coremod.util.StructureWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BUILDING_TYPE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_LOCATION;

/**
 * Class taking care of registering the buildings.
 */
public class BuildingRegistry
{
    /**
     * Map to resolve names to class.
     */
    @NotNull
    private static final BiMap<String, Class<?>> nameToClassMap = HashBiMap.create();

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
     * Add all the mappings of the default buildings..
     */
    static
    {
        addMapping("Baker", BuildingBaker.class, BuildingBaker.View.class, BlockHutBaker.class);
        addMapping("Blacksmith", BuildingBlacksmith.class, BuildingBlacksmith.View.class, BlockHutBlacksmith.class);
        addMapping("Builder", BuildingBuilder.class, BuildingBuilder.View.class, BlockHutBuilder.class);
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
     * Private constructor to hide implicit public one.
     */
    private BuildingRegistry()
    {
        /**
         * Intentionally left empty.
          */
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
            final WorkOrderBuildBuilding workOrder = new WorkOrderBuildBuilding(building, 1);
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

    @NotNull
    public static BiMap<String, Class<?>> getNameToClassMap()
    {
        return nameToClassMap;
    }

    @NotNull
    public static Map<Class<?>, Class<?>> getBlockClassToBuildingClassMap()
    {
        return blockClassToBuildingClassMap;
    }

    @NotNull
    public static Map<Integer, Class<?>> getClassNameHashToViewClassMap()
    {
        return classNameHashToViewClassMap;
    }
}
