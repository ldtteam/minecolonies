package com.minecolonies.colony.buildings;

import com.blockout.views.Window;
import com.minecolonies.client.gui.WindowHutWarehouse;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobDeliveryman;
import com.minecolonies.lib.EnumGUI;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

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
    public String getSchematicName()
    {
        return "Warehouse";
    }

    @Override
    public String getJobName()
    {
        return "Deliveryman";
    }

    @Override
    public Job createJob(CitizenData citizen)
    {
        return new JobDeliveryman(citizen);
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

        public Window getWindow(int guiId)
        {
            if (guiId == EnumGUI.WAREHOUSE.getID())
            {
                return new WindowHutWarehouse(this);
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
