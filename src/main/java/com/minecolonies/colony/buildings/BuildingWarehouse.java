package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutWarehouse;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobDeliveryman;
import io.netty.buffer.ByteBuf;
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
    public String getSchematicName(){ return "Warehouse"; }

    @Override
    public int getMaxBuildingLevel(){ return 4; }

    @Override
    public String getJobName(){ return "Deliveryman"; }

    @Override
    public Job createJob(CitizenData citizen){ return new JobDeliveryman(citizen); }

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

        public com.blockout.views.Window getWindow()
        {
            return new WindowHutWarehouse(this);
        }

        @Override
        public void deserialize(ByteBuf buf)
        {
            super.deserialize(buf);

            //  Blacksmith
            blacksmithGold = buf.readBoolean();
            blacksmithDiamond = buf.readBoolean();

            //  Stonemason
            stonemasonStone = buf.readBoolean();
            stonemasonSand = buf.readBoolean();
            stonemasonNetherrack = buf.readBoolean();
            stonemasonQuartz = buf.readBoolean();

            //  Guard
            guardArmor = buf.readBoolean();
            guardWeapon = buf.readBoolean();

            //  Misc
            citizenVisit = buf.readBoolean();
        }
    }

    @Override
    public void serializeToView(ByteBuf buf)
    {
        super.serializeToView(buf);

        //  Blacksmith
        buf.writeBoolean(blacksmithGold);
        buf.writeBoolean(blacksmithDiamond);

        //  Stonemason
        buf.writeBoolean(stonemasonStone);
        buf.writeBoolean(stonemasonSand);
        buf.writeBoolean(stonemasonNetherrack);
        buf.writeBoolean(stonemasonQuartz);

        //  Guard
        buf.writeBoolean(guardArmor);
        buf.writeBoolean(guardWeapon);

        //  Misc
        buf.writeBoolean(citizenVisit);
    }
}
