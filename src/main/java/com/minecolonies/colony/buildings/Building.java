package com.minecolonies.colony.buildings;

import com.minecolonies.blocks.*;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.materials.MaterialStore;
import com.minecolonies.colony.materials.MaterialSystem;
import com.minecolonies.colony.workorders.WorkOrderBuild;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.Log;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public abstract class Building
{
    private         final       BlockPos                    location;
    private         final       Colony                      colony;

    private MaterialStore materialStore;

    private TileEntityColonyBuilding tileEntity;

    //  Attributes
    private                     int                         buildingLevel                   = 0;
    private                     int                         rotation                        = 0;
    private                     String                      style                           = "classic";

    //  State
    private                     boolean                     isDirty                         = false;

    //  Building and View Class Mapping
    private static              Map<String,      Class<?>>  nameToClassMap                  = new HashMap<>();
    private static              Map<Class<?>,    String>    classToNameMap                  = new HashMap<>();
    private static              Map<Class<?>,    Class<?>>  blockClassToBuildingClassMap    = new HashMap<>();
    private static              Map<Integer,     Class<?>>  classNameHashToClassMap         = new HashMap<>();

    private static  final       String                      TAG_BUILDING_TYPE               = "type";
    //    pr         ivate final static String TAG_ID              = "id";      //  CJJ - We are using the Location as the Id as it is unique enough
    private static  final       String                      TAG_LOCATION                    = "location";  //  Location is unique (within a Colony) and so can double as the Id
    private static  final       String                      TAG_BUILDING_LEVEL              = "level";
    private static  final       String                      TAG_ROTATION                    = "rotation";
    private static  final       String                      TAG_STYLE                       = "style";

    public abstract             String                      getSchematicName();
    public abstract             int                         getMaxBuildingLevel();

    /**
     * Add build to a mapping.
     * <code>buildingClass</code> needs to extend {@link com.minecolonies.colony.buildings.Building}
     * <code>parentBlock</code> needs to extend {@link AbstractBlockHut}
     *
     * @param name              name of building.
     * @param buildingClass     subclass of Building, located in {@link com.minecolonies.colony.buildings}
     * @param parentBlock       subclass of Block, located in {@link com.minecolonies.blocks}
     */
    private static void addMapping(String name, Class<? extends Building> buildingClass, Class<? extends AbstractBlockHut> parentBlock)
    {
        if (nameToClassMap.containsKey(name) || classNameHashToClassMap.containsKey(buildingClass.getName().hashCode()))
        {
            throw new IllegalArgumentException("Duplicate type '" + name + "' when adding Building class mapping");
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
            catch (NoSuchMethodException exception)
            {
                throw new IllegalArgumentException("Missing constructor for type '" + name + "' when adding Building class mapping");
            }
        }

        if (blockClassToBuildingClassMap.containsKey(parentBlock))
        {
            throw new IllegalArgumentException("Building type '" + name + "' uses TileEntity '" + parentBlock.getClass().getName() + "' which is already in use.");
        }
        else
        {
            blockClassToBuildingClassMap.put(parentBlock, buildingClass);
        }
    }

    /**
     * Checks if a block matches the current object.
     *
     * @param block     Block you want to know whether it matches this class or not
     * @return          True if the block matches this class, otherwise false
     */
    public boolean isMatchingBlock(Block block)
    {
        Class<?> c = blockClassToBuildingClassMap.get(block.getClass());
        return getClass().equals(c);
    }

    static
    {
        addMapping("Baker",         BuildingBaker.class,         BlockHutBaker.class);
        addMapping("Blacksmith",    BuildingBlacksmith.class,    BlockHutBlacksmith.class);
        addMapping("Builder",       BuildingBuilder.class,       BlockHutBuilder.class);
        addMapping("Home",          BuildingHome.class,          BlockHutCitizen.class);
        addMapping("Farmer",        BuildingFarmer.class,        BlockHutFarmer.class);
        addMapping("Lumberjack",    BuildingLumberjack.class,    BlockHutLumberjack.class);
        addMapping("Miner",         BuildingMiner.class,         BlockHutMiner.class);
        addMapping("Stonemason",    BuildingStonemason.class,    BlockHutStonemason.class);
        addMapping("TownHall",      BuildingTownHall.class,      BlockHutTownHall.class);
        addMapping("Warehouse",     BuildingWarehouse.class,     BlockHutWarehouse.class);
        addMapping("Fisherman",     BuildingFisherman.class,     BlockHutFisherman.class);

    }

    /**
     * Constructor for a Building.
     *
     * @param colony            Colony the building belongs to
     * @param pos  Location of the building (it's Hut Block)
     */
    protected Building(Colony colony, BlockPos pos)
    {
        location = pos;
        this.colony = colony;
        materialStore = new MaterialStore(MaterialStore.Type.CHEST, colony.getMaterialSystem(), this);
    }


    /**
     * Create and load a Building given it's saved NBTTagCompound
     * Calls {@link #readFromNBT(net.minecraft.nbt.NBTTagCompound)}
     *
     * @param colony    The owning colony
     * @param compound  The saved data
     * @return          {@link com.minecolonies.colony.buildings.Building} created from the compound.
     */
    public static Building createFromNBT(Colony colony, NBTTagCompound compound)
    {
        Building building = null;
        Class<?> oclass = null;

        try
        {
            oclass = nameToClassMap.get(compound.getString(TAG_BUILDING_TYPE));

            if (oclass != null)
            {
                //UUID id = UUID.fromString(compound.getString("id"));
                BlockPos pos = BlockPosUtil.readFromNBT(compound, TAG_LOCATION);
                Constructor<?> constructor = oclass.getDeclaredConstructor(Colony.class, BlockPos.class);
                building = (Building)constructor.newInstance(colony, pos);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        if (building != null)
        {
            try
            {
                building.readFromNBT(compound);
            }
            catch (Exception ex)
            {
                Log.logger.error(String.format("A Building %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                                        compound.getString(TAG_BUILDING_TYPE), oclass.getName()), ex);
                building = null;
            }
        }
        else
        {
            Log.logger.warn(String.format("Unknown Building type '%s' or missing constructor of proper format.", compound.getString(TAG_BUILDING_TYPE)));
        }

        return building;
    }

    /**
     * Create a Building given it's TileEntity
     *
     * @param colony    The owning colony
     * @param parent    The Tile Entity the building belongs to.
     * @return          {@link com.minecolonies.colony.buildings.Building} instance, without NBTTags applied.
     */
    public static Building create(Colony colony, TileEntityColonyBuilding parent)
    {
        Building building = null;
        Class<?> oclass;

        try
        {
            oclass = blockClassToBuildingClassMap.get(parent.getBlockType().getClass());

            if (oclass != null)
            {
                //UUID id = UUID.fromString(compound.getString("id"));
                BlockPos loc = parent.getPosition();
                Constructor<?> constructor = oclass.getDeclaredConstructor(Colony.class, BlockPos.class);
                building = (Building)constructor.newInstance(colony, loc);
            }
            else
            {
                Log.logger.error(String.format("TileEntity %s does not have an associated Building.", parent.getClass().getName()));
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            Log.logger.error(String.format("Unknown Building type '%s' or missing constructor of proper format.", parent.getClass().getName()), exception);
        }

        return building;
    }

    /**
     * Load data from NBT compound.
     * Writes to {@link #buildingLevel}, {@link #rotation} and {@link #style}
     *
     * @param compound      {@link net.minecraft.nbt.NBTTagCompound} to read data from
     */
    public void readFromNBT(NBTTagCompound compound)
    {
        buildingLevel = compound.getInteger(TAG_BUILDING_LEVEL);

        rotation = compound.getInteger(TAG_ROTATION);
        style = compound.getString(TAG_STYLE);
        if(style.equals(""))
        {
            Log.logger.warn("Loaded empty style, setting to classic");
            style = "classic";
        }

        if(MaterialSystem.isEnabled)
        {
            if(!materialStore.readFromNBT(compound))
            {
                this.getTileEntity().initMaterialStoreFromInventory();
            }
        }
    }

    /**
     * Save data to NBT compound
     * Writes the {@link #buildingLevel}, {@link #rotation}, {@link #style}, {@link #location}, and {@link #getClass()} value
     *
     * @param compound      {@link net.minecraft.nbt.NBTTagCompound} to write data to
     */
    public void writeToNBT(NBTTagCompound compound)
    {
        String s = classToNameMap.get(this.getClass());

        if (s == null)
        {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        }
        else
        {
            compound.setString(TAG_BUILDING_TYPE, s);
            BlockPosUtil.writeToNBT(compound, TAG_LOCATION, location);
        }

        compound.setInteger(TAG_BUILDING_LEVEL, buildingLevel);
        compound.setInteger(TAG_ROTATION, rotation);
        compound.setString(TAG_STYLE, style);

        if(MaterialSystem.isEnabled)
        {
            materialStore.writeToNBT(compound);
        }
    }

    /**
     * Returns the colony of the building
     *
     * @return          {@link com.minecolonies.colony.Colony} of the current object
     */
    public Colony getColony()
    {
        return colony;
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID
     *
     * @return          {@link BlockPos} of the current object
     */
    public BlockPos getID()
    {
        return location; //  Location doubles as ID
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID
     *
     * @return          {@link BlockPos} of the current object
     */
    public BlockPos getLocation()
    {
        return location;
    }

    /**
     * Returns the level of the current object
     *
     * @return          Level of the current object
     */
    public int getBuildingLevel()
    {
        return buildingLevel;
    }

    /**
     * Sets the current level of the building
     *
     * @param level     Level of the building
     */
    public void setBuildingLevel(int level)
    //todo: Since this is public, dont we want to do some security checks? mw
    {
        buildingLevel = level;
        markDirty();
        ColonyManager.markDirty();
    }

    /**
     * Sets the tile entity field to  {@param te}
     *
     * @param te    {@link TileEntityColonyBuilding} that will fill the {@link #tileEntity} field
     */
    public void setTileEntity(TileEntityColonyBuilding te)
    {
        tileEntity = te;
    }

    /**
     * Returns the tile entity that belongs to the colony building
     *
     * @return      {@link TileEntityColonyBuilding} object of the building
     */
    public TileEntityColonyBuilding getTileEntity()
    {
        if (tileEntity == null)
        {
            //  Lazy evaluation
            if (colony.getWorld().getBlockState(location).getBlock() != null) //todo check (mw, transsition 1.8)
            {
                TileEntity te = getColony().getWorld().getTileEntity(location);
                if (te instanceof TileEntityColonyBuilding)
                {
                    tileEntity = (TileEntityColonyBuilding)te;
                    if (tileEntity.getBuilding() == null)
                    {
                        tileEntity.setColony(colony);
                        tileEntity.setBuilding(this);
                    }
                }
            }
        }

        return tileEntity;
    }

    /**
     * Returns whether the instance is dirty or not.
     *
     * @return          true if dirty, false if not.
     */
    public final boolean isDirty()
    {
        return isDirty;
    }

    /**
     * Sets {@link #isDirty} to false, meaning that the instance is up to date
     */
    public final void clearDirty()
    {
        isDirty = false;
    }

    /**
     * Marks the instance and the building dirty
     */
    public final void markDirty()
    {
        isDirty = true;
        colony.markBuildingsDirty();
    }

    /**
     * Method to do things when a block is destroyed.
     */
    public void onDestroyed()
    {
        if(MaterialSystem.isEnabled)
        {
            materialStore.destroy();
        }
    }

    /**
     * Destroys the block.
     * Calls {@link #onDestroyed()}
     */
    public final void destroy()
    {
        onDestroyed();
        colony.removeBuilding(this);
    }

    /**
     * Method to remove a citizen.
     *
     * @param citizen       Citizen to be removed
     */
    public void removeCitizen(CitizenData citizen) {}

    /**
     * On tick of the server
     *
     * @param event         {@link net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent}
     */
    public void onServerTick(TickEvent.ServerTickEvent event) {}

    /**
     * On tick of the world
     *
     * @param event         {@link net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent}
     */
    public void onWorldTick(TickEvent.WorldTickEvent event) {}

    /**
     * Adds work orders to the {@link Colony#workManager}
     *
     * @param level         Desired level
     */
    private void requestWorkOrder(int level)
    {
        for (WorkOrderBuild o : colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuild.class))

        {
            if (o.getBuildingId().equals(getID()))
            {
                return;
            }
        }

        colony.getWorkManager().addWorkOrder(new WorkOrderBuild(this, level));
    }

    /**
     * Requests an upgrade for the current building
     */
    public void requestUpgrade()
    {
        if (buildingLevel < getMaxBuildingLevel())
        {
            requestWorkOrder(buildingLevel + 1);
        }
    }

    /**
     * Requests a repair for the current building
     */
    public void requestRepair()
    {
        if (buildingLevel > 0)
        {
            requestWorkOrder(buildingLevel);
        }
    }

    //todo 0 north, 1 east .. ?
    /**
     * Sets the rotation of the current building
     *
     * @param rotation      integer value of the rotation
     */
    public void setRotation(int rotation)
    {
        this.rotation = rotation;
    }

    /**
     * Returns the rotation of the current building
     *
     * @return              integer value of the rotation
     */
    public int getRotation()
    {
        return rotation;
    }

    //todo possible values?
    /**
     * Sets the style of the building
     *
     * @param style         String value of the style
     */
    public void setStyle(String style)
    {
        this.style = style;
    }

    /**
     * Returns the style of the current building
     *
     * @return              String representation of the current building-style
     */
    public String getStyle()
    {
        return style;
    }

    public MaterialStore getMaterialStore()
    {
        return materialStore;
    }

    /**
     * The Building View is the client-side representation of a Building.
     * Views contain the Building's data that is relevant to a Client, in a more client-friendly form
     * Mutable operations on a View result in a message to the server to perform the operation
     */
    public static class View
    {
        private final   ColonyView       colony;
        private final   BlockPos location;

        private int     buildingLevel       = 0;
        private int     buildingMaxLevel    = 0;

        protected View(ColonyView c, BlockPos l)
        {
            colony = c;
            location = new BlockPos(l);
        }

        public BlockPos getID()
        {
            return location; //  Location doubles as ID
        }

        public BlockPos getLocation()
        {
            return location;
        }

        public ColonyView getColony()
        {
            return colony;
        }

        public int getBuildingLevel()
        {
            return buildingLevel;
        }

        public int getBuildingMaxLevel()
        {
            return buildingMaxLevel;
        }

        public boolean isBuildingMaxLevel()
        {
            return buildingLevel >= buildingMaxLevel;
        }

        public void openGui()
        {
            com.blockout.views.Window window = getWindow();
            if (window != null)
            {
                window.open();
            }
        }

        public com.blockout.views.Window getWindow()
        {
            return null;
        }

        public void deserialize(ByteBuf buf)
        {
            buildingLevel = buf.readInt();
            buildingMaxLevel = buf.readInt();
        }
    }

    /**
     * Serializes to view.
     * Sends 3 integers.
     *      1) hashcode of the name of the class
     *      2) building level
     *      3) max building level
     *
     * @param buf   ByteBuf to write to
     */
    public void serializeToView(ByteBuf buf)
    {
        buf.writeInt(this.getClass().getName().hashCode());
        buf.writeInt(getBuildingLevel());
        buf.writeInt(getMaxBuildingLevel());
    }

    /**
     * Create a Building View given it's saved NBTTagCompound
     *
     * @param       colony The owning colony
     * @param       id     Chunk coordinate of the block a view is created for.
     * @param       buf    The network data
     * @return      {@link com.minecolonies.colony.buildings.Building.View} created from reading the buf
     */
    public static View createBuildingView(ColonyView colony, BlockPos id, ByteBuf buf)
    {
        View view = null;
        Class<?> oclass = null;

        try
        {
            int typeHash = buf.readInt();
            oclass = classNameHashToClassMap.get(typeHash);

            if (oclass != null)
            {
                for (Class<?> c : oclass.getDeclaredClasses())
                {
                    if (c.getName().endsWith("$View"))
                    {
                        Constructor<?> constructor = c.getDeclaredConstructor(ColonyView.class, BlockPos.class);
                        view = (View)constructor.newInstance(colony, id);
                        break;
                    }
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        if (view != null)
        {
            try
            {
                view.deserialize(buf);
            }
            catch (Exception ex)
            {
                Log.logger.error(String.format("A Building View (%s) has thrown an exception during deserializing, its state cannot be restored. Report this to the mod author", oclass.getName()), ex);
                view = null;
            }
        }
        else
        {
            Log.logger.warn("Unknown Building type, missing View subclass, or missing constructor of proper format.");
        }

        return view;
    }
}
