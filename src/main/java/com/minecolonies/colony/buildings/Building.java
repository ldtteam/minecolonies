package com.minecolonies.colony.buildings;

import com.minecolonies.MineColonies;
import com.minecolonies.blocks.*;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.tileentities.*;
import com.minecolonies.util.ChunkCoordUtils;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public abstract class Building
{
    private final ChunkCoordinates location;
    private final Colony           colony;

    private WeakReference<TileEntityColonyBuilding> tileEntity;

    //  Attributes
    private int buildingLevel = 0;

    //  State
    private boolean isDirty = false;

    //  Building and View Class Mapping
    private static Map<String, Class<?>>   nameToClassMap               = new HashMap<String, Class<?>>();
    private static Map<Class<?>, String>   classToNameMap               = new HashMap<Class<?>, String>();
    private static Map<Class<?>, Class<?>> blockClassToBuildingClassMap = new HashMap<Class<?>, Class<?>>();
    private static Map<Integer, Class<?>>  classNameHashToClassMap      = new HashMap<Integer, Class<?>>();

    private final static String TAG_TYPE           = "type";
    //    private final static String TAG_ID              = "id";      //  CJJ - We are using the Location as the Id as it is unique enough
    private final static String TAG_LOCATION       = "location";  //  Location is unique (within a Colony) and so can double as the Id
    private final static String TAG_BUILDING_LEVEL = "level";

    /**
     * Add a given Building mapping
     *
     * @param name name of object
     * @param buildingClass subclass of Building
     * @param parentBlock subclass of Block
     */
    private static void addMapping(String name, Class<? extends Building> buildingClass, Class<? extends BlockHut> parentBlock)
    {
        if (nameToClassMap.containsKey(name) || classNameHashToClassMap.containsKey(buildingClass.getName().hashCode()))
        {
            throw new IllegalArgumentException("Duplicate type '" + name + "' when adding Building class mapping");
        }
        else
        {
            try
            {
                if (buildingClass.getDeclaredConstructor(Colony.class, ChunkCoordinates.class) != null)
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

    public boolean isMatchingBlock(Block block)
    {
        Class<?> c = blockClassToBuildingClassMap.get(block.getClass());
        return getClass().equals(c);
    }

    /**
     * Set up mappings of name->Building and TileEntity->Building
     */
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
        addMapping("Townhall",      BuildingTownHall.class,      BlockHutTownHall.class);
        addMapping("Warehouse",     BuildingWarehouse.class,     BlockHutWarehouse.class);
    }

    /**
     * Constructor for a Building.
     *
     * @param c Colony the building belongs to
     * @param l Location of the building (it's Hut Block)
     */
    protected Building(Colony c, ChunkCoordinates l)
    {
        location = new ChunkCoordinates(l);
        colony = c;
    }

    /**
     * Create and load a Building given it's saved NBTTagCompound
     *
     * @param colony   The owning colony
     * @param compound The saved data
     * @return
     */
    public static Building createFromNBT(Colony colony, NBTTagCompound compound)
    {
        Building building = null;
        Class<?> oclass = null;

        try
        {
            oclass = nameToClassMap.get(compound.getString(TAG_TYPE));

            if (oclass != null)
            {
                //UUID id = UUID.fromString(compound.getString("id"));
                ChunkCoordinates loc = ChunkCoordUtils.readFromNBT(compound, TAG_LOCATION);
                Constructor<?> constructor = oclass.getDeclaredConstructor(Colony.class, ChunkCoordinates.class);
                building = (Building)constructor.newInstance(colony, loc);
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
                MineColonies.logger.error(
                        String.format("A Building %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                            compound.getString(TAG_TYPE), oclass.getName()), ex);
                building = null;
            }
        }
        else
        {
            MineColonies.logger.warn(
                    String.format("Unknown Building type '%s' or missing constructor of proper format.", compound.getString(TAG_TYPE)));
        }

        return building;
    }


    /**
     * Create a Building given it's TileEntity
     *
     * @param colony    The owning colony
     * @param parent    The Tile Entity the building belongs to.
     * @return
     */
    public static Building create(Colony colony, TileEntityColonyBuilding parent)
    {
        Building building = null;
        Class<?> oclass = null;

        try
        {
            oclass = blockClassToBuildingClassMap.get(parent.getBlockType().getClass());

            if (oclass != null)
            {
                //UUID id = UUID.fromString(compound.getString("id"));
                ChunkCoordinates loc = parent.getPosition();
                Constructor<?> constructor = oclass.getDeclaredConstructor(Colony.class, ChunkCoordinates.class);
                building = (Building)constructor.newInstance(colony, loc);
            }
            else
            {
                MineColonies.logger.error(
                        String.format("TileEntity %s does not have an associated Building.", parent.getClass().getName()));
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            MineColonies.logger.error(String.format("Unknown Building type '%s' or missing constructor of proper format.", parent.getClass().getName()), exception);
        }

        return building;
    }

    /**
     * Load data from NBT compound
     *
     * @param compound
     */
    public void readFromNBT(NBTTagCompound compound)
    {
        buildingLevel = compound.getInteger(TAG_BUILDING_LEVEL);
    }

    /**
     * Save data to NBT compound
     *
     * @param compound
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
            compound.setString(TAG_TYPE, s);
            ChunkCoordUtils.writeToNBT(compound, TAG_LOCATION, location);
        }

        compound.setInteger(TAG_BUILDING_LEVEL, buildingLevel);
    }

    public Colony getColony() { return colony; }
    public ChunkCoordinates getID() { return location; }    //  Location doubles as ID
    public ChunkCoordinates getLocation() { return location; }
    public int getBuildingLevel() { return buildingLevel; }
    public void setBuildingLevel(int level) { buildingLevel = level; markDirty(); }

    public abstract String getSchematicName();

    public void setTileEntity(TileEntityColonyBuilding te) { tileEntity = new WeakReference<TileEntityColonyBuilding>(te); }
    public TileEntityColonyBuilding getTileEntity()
    {
        return (tileEntity != null) ? tileEntity.get() : null;
    }

    public final boolean isDirty() { return isDirty; }
    public final void clearDirty() { isDirty = false; }
    public final void markDirty()
    {
        isDirty = true;
        colony.markBuildingsDirty();
    }

    public void onDestroyed() {}
    public final void destroy()
    {
        onDestroyed();
        colony.removeBuilding(this);
    }

    public void removeCitizen(CitizenData citizen) {}

    public int getGuiId() { return 0; }

    public void onServerTick(TickEvent.ServerTickEvent event) {}
    public void onWorldTick(TickEvent.WorldTickEvent event) {}

    public void requestUpgrade()
    {
        if (buildingLevel < 3) //  TODO maxLevel
        {
            colony.addBuildingForUpgrade(this, buildingLevel + 1);
        }
    }

    public void requestRepair()
    {
        if (buildingLevel > 0)
        {
            colony.addBuildingForUpgrade(this, buildingLevel);
        }
    }

    public void openGui(EntityPlayer player)
    {
        player.openGui(MineColonies.instance, getGuiId(), getColony().getWorld(), location.posX, location.posY, location.posZ);
    }

    /**
     * The Building View is the client-side representation of a Building.
     * Views contain the Building's data that is relevant to a Client, in a more client-friendly form
     * Mutable operations on a View result in a message to the server to perform the operation
     */
    public static class View
    {
        private final ColonyView       colony;
        private final ChunkCoordinates location;

        private int buildingLevel = 0;

        protected View(ColonyView c, ChunkCoordinates l)
        {
            colony = c;
            location = new ChunkCoordinates(l);
        }

        public ChunkCoordinates getID() { return location; }    //  Location doubles as ID
        public ChunkCoordinates getLocation() { return location; }
        public ColonyView getColony() { return colony; }
        public int getBuildingLevel() { return buildingLevel; }

        public void openGui(EnumGUI gui)
        {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            player.openGui(MineColonies.instance, gui.getID(), player.worldObj, location.posX, location.posY, location.posZ);
        }

        public com.blockout.views.Window getWindow(int guiId)
        {
            return null;
        }

        public void parseNetworkData(NBTTagCompound compound)
        {
            //  TODO - Use a PacketBuffer
            buildingLevel = compound.getInteger(TAG_BUILDING_LEVEL);
        }
    }

    public void createViewNetworkData(NBTTagCompound compound)
    {
        //  TODO - Use a PacketBuffer
        //String s = classToNameMap.get(this.getClass());
        compound.setInteger(TAG_TYPE, this.getClass().getName().hashCode());
        compound.setInteger(TAG_BUILDING_LEVEL, buildingLevel);
    }

    /**
     * Create a Building View given it's saved NBTTagCompound
     * TODO - Use a PacketBuffer
     *
     * @param colony   The owning colony
     * @param compound The network data
     * @return
     */
    public static View createBuildingView(ColonyView colony, ChunkCoordinates id, NBTTagCompound compound)
    {
        View view = null;
        Class<?> oclass = null;

        try
        {
            int typeHash = compound.getInteger(TAG_TYPE);
            oclass = classNameHashToClassMap.get(typeHash);

            if (oclass != null)
            {
                for (Class<?> c : oclass.getDeclaredClasses())
                {
                    if (c.getName().endsWith("$View"))
                    {
                        Constructor<?> constructor = c.getDeclaredConstructor(ColonyView.class, ChunkCoordinates.class);
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
                view.parseNetworkData(compound);
            }
            catch (Exception ex)
            {
                MineColonies.logger.error(String.format("A Building View %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", compound.getString(TAG_TYPE), oclass.getName()), ex);
                view = null;
            }
        }
        else
        {
            MineColonies.logger.warn(String.format("Unknown Building type '%s', missing View subclass, or missing constructor of proper format.", compound.getString(TAG_TYPE)));
        }

        return view;
    }
}
