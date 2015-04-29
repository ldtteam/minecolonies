package com.minecolonies.colony.buildings;

import com.blockout.views.Window;
import com.minecolonies.client.gui.WindowHutFarmer;
import com.minecolonies.client.gui.WindowHutMiner;
import com.minecolonies.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobFarmer;
import com.minecolonies.colony.jobs.JobPlaceholder;
import com.minecolonies.lib.EnumGUI;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

public class BuildingFarmer extends BuildingWorker
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

    public BuildingFarmer(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName(){ return "Farmer"; }

    @Override
    public int getMaxBuildingLevel(){ return 3; }

    @Override
    public String getJobName(){ return "Farmer"; }

    @Override
    public Job createJob(CitizenData citizen)
    {
        return new JobFarmer(citizen); //TODO Implement Later
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        NBTTagCompound deliveryCompound = compound.getCompoundTag("farmer");

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

        compound.setTag("farmer", deliveryCompound);
    }

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
            if (guiId == EnumGUI.FARMER.getID())
            {
                return new WindowHutFarmer(this);
            }

            return null;
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


    public int getFarmRadius()
    {
        return getBuildingLevel()+3;
    }

    @Override
    public int getGuiId(){ return EnumGUI.FARMER.getID(); }

}
