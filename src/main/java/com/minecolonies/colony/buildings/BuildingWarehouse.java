package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.GuiHutWarehouse;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityDeliveryman;
import com.minecolonies.entity.jobs.ColonyJob;
import com.minecolonies.lib.EnumGUI;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class BuildingWarehouse extends BuildingWorker
{
    public boolean blacksmithGold = false,
            blacksmithDiamond = false,
            stonemasonStone = false,
            stonemasonSand = false,
            stonemasonNetherrack = false,
            stonemasonQuartz = false,
            guardArmor = false,
            guardWeapon = false,
            citizenVisit = false;

    public BuildingWarehouse(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName() { return "Warehouse"; }

    @Override
    public String getJobName() { return "Deliveryman"; }

    //  Classic Style of Jobs
    @Override
    public EntityCitizen createWorker(World world)
    {
        return new EntityDeliveryman(world);
    }

    //  Future Style of Jobs
    @Override
    public Class<ColonyJob> getJobClass()
    {
        return ColonyJob.class; //TODO Implement Later
    }

    @Override
    public int getGuiId() { return EnumGUI.WAREHOUSE.getID(); }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        NBTTagCompound deliveryCompound = compound.getCompoundTag("delivery");

        //  Blacksmith
        NBTTagCompound blacksmithCompound = deliveryCompound.getCompoundTag("blacksmith");
        blacksmithGold = blacksmithCompound.getBoolean("gold");
        blacksmithDiamond = blacksmithCompound.getBoolean("diamond");

        //  Stonemason
        NBTTagCompound stonemasonCompound = deliveryCompound.getCompoundTag("stonemason");
        stonemasonStone = stonemasonCompound.getBoolean("stone");
        stonemasonSand = stonemasonCompound.getBoolean("sand");
        stonemasonNetherrack = stonemasonCompound.getBoolean("netherrack");
        stonemasonQuartz = stonemasonCompound.getBoolean("quartz");

        //  Guard
        NBTTagCompound guardCompound = deliveryCompound.getCompoundTag("guard");
        guardArmor = guardCompound.getBoolean("armor");
        guardWeapon = guardCompound.getBoolean("weapon");

        //  Misc
        citizenVisit = deliveryCompound.getBoolean("citizen");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        NBTTagCompound deliveryCompound = new NBTTagCompound();

        //  Blacksmith
        NBTTagCompound blacksmithCompound = new NBTTagCompound();
        blacksmithCompound.setBoolean("gold", blacksmithGold);
        blacksmithCompound.setBoolean("diamond", blacksmithDiamond);
        deliveryCompound.setTag("blacksmith", blacksmithCompound);

        //  Stonemason
        NBTTagCompound stonemasonCompound = new NBTTagCompound();
        stonemasonCompound.setBoolean("stone", stonemasonStone);
        stonemasonCompound.setBoolean("sand", stonemasonSand);
        stonemasonCompound.setBoolean("netherrack", stonemasonNetherrack);
        stonemasonCompound.setBoolean("quartz", stonemasonQuartz);
        deliveryCompound.setTag("stonemason", stonemasonCompound);

        //  Guard
        NBTTagCompound guardCompound = new NBTTagCompound();
        guardCompound.setBoolean("armor", guardArmor);
        guardCompound.setBoolean("weapon", guardWeapon);
        deliveryCompound.setTag("guard", guardCompound);

        //  Misc
        deliveryCompound.setBoolean("citizen", citizenVisit);

        compound.setTag("delivery", deliveryCompound);
    }

    /**
     * BuildingWarehouse View
     */
    public static class View extends BuildingWorker.View
    {
        public boolean blacksmithGold = false,
                blacksmithDiamond = false,
                stonemasonStone = false,
                stonemasonSand = false,
                stonemasonNetherrack = false,
                stonemasonQuartz = false,
                guardArmor = false,
                guardWeapon = false,
                citizenVisit = false;

        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }

        public GuiScreen getGui(EntityPlayer player, World world, int guiId, int x, int y, int z)
        {
            if (guiId == EnumGUI.WAREHOUSE.getID())
            {
                return new GuiHutWarehouse(this, player, world, x, y, z);
            }

            return null;
        }

        public void parseNetworkData(NBTTagCompound compound)
        {
            //  TODO - Use a PacketBuffer
            super.parseNetworkData(compound);

            NBTTagCompound deliveryCompound = compound.getCompoundTag("delivery");

            //  Blacksmith
            NBTTagCompound blacksmithCompound = deliveryCompound.getCompoundTag("blacksmith");
            blacksmithGold = blacksmithCompound.getBoolean("gold");
            blacksmithDiamond = blacksmithCompound.getBoolean("diamond");

            //  Stonemason
            NBTTagCompound stonemasonCompound = deliveryCompound.getCompoundTag("stonemason");
            stonemasonStone = stonemasonCompound.getBoolean("stone");
            stonemasonSand = stonemasonCompound.getBoolean("sand");
            stonemasonNetherrack = stonemasonCompound.getBoolean("netherrack");
            stonemasonQuartz = stonemasonCompound.getBoolean("quartz");

            //  Guard
            NBTTagCompound guardCompound = deliveryCompound.getCompoundTag("guard");
            guardArmor = guardCompound.getBoolean("armor");
            guardWeapon = guardCompound.getBoolean("weapon");

            //  Misc
            citizenVisit = deliveryCompound.getBoolean("citizen");
        }
    }

    public void createViewNetworkData(NBTTagCompound compound)
    {
        //  TODO - Use a PacketBuffer
        super.createViewNetworkData(compound);

        NBTTagCompound deliveryCompound = new NBTTagCompound();

        //  Blacksmith
        NBTTagCompound blacksmithCompound = new NBTTagCompound();
        blacksmithCompound.setBoolean("gold", blacksmithGold);
        blacksmithCompound.setBoolean("diamond", blacksmithDiamond);
        deliveryCompound.setTag("blacksmith", blacksmithCompound);

        //  Stonemason
        NBTTagCompound stonemasonCompound = new NBTTagCompound();
        stonemasonCompound.setBoolean("stone", stonemasonStone);
        stonemasonCompound.setBoolean("sand", stonemasonSand);
        stonemasonCompound.setBoolean("netherrack", stonemasonNetherrack);
        stonemasonCompound.setBoolean("quartz", stonemasonQuartz);
        deliveryCompound.setTag("stonemason", stonemasonCompound);

        //  Guard
        NBTTagCompound guardCompound = new NBTTagCompound();
        guardCompound.setBoolean("armor", guardArmor);
        guardCompound.setBoolean("weapon", guardWeapon);
        deliveryCompound.setTag("guard", guardCompound);

        //  Misc
        deliveryCompound.setBoolean("citizen", citizenVisit);

        compound.setTag("delivery", deliveryCompound);
    }
}
