package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutFarmer;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobFarmer;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

/*
Class is not used yet
 */
public class BuildingFarmer extends BuildingWorker
{

    public int wheat = 100;
    public int potato = 0;
    public int carrot = 0;
    public int melon = 0;
    public int pumpkin = 0;

    public static final String WHEAT_TAG = "wheat";
    public static final String POTATO_TAG = "potato";
    public static final String CARROT_TAG = "carrot";
    public static final String MELON_TAG = "melon";
    public static final String PUMPKIN_TAG = "pumpkin";

    private static final String FARMER = "Farmer";
    private static final String TAG_FARMER = "farmer";

    public BuildingFarmer(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName(){ return FARMER; }

    @Override
    public int getMaxBuildingLevel(){ return 3; }

    @Override
    public String getJobName(){ return FARMER; }

    @Override
    public Job createJob(CitizenData citizen)
    {
        return new JobFarmer(citizen); //TODO Implement Later
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        NBTTagCompound farmerCompound = compound.getCompoundTag(TAG_FARMER);

        wheat = farmerCompound.getInteger(WHEAT_TAG);
        potato = farmerCompound.getInteger(POTATO_TAG);
        carrot = farmerCompound.getInteger(CARROT_TAG);
        melon = farmerCompound.getInteger(MELON_TAG);
        pumpkin = farmerCompound.getInteger(PUMPKIN_TAG);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        NBTTagCompound farmerCompound = new NBTTagCompound();

        farmerCompound.setInteger(WHEAT_TAG,wheat);
        farmerCompound.setInteger(POTATO_TAG,potato);
        farmerCompound.setInteger(CARROT_TAG,carrot);
        farmerCompound.setInteger(MELON_TAG,melon);
        farmerCompound.setInteger(PUMPKIN_TAG, pumpkin);

        compound.setTag(TAG_FARMER, farmerCompound);
    }

    public static class View extends BuildingWorker.View
    {
        public int wheat = 100,
                potato = 0,
                carrot = 0,
                melon = 0,
                pumpkin = 0;


        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }

        public com.blockout.views.Window getWindow()
        {
            return new WindowHutFarmer(this);
        }

        @Override
        public void deserialize(ByteBuf buf)
        {
            super.deserialize(buf);

            wheat = buf.readInt();
            potato = buf.readInt();
            carrot = buf.readInt();
            melon = buf.readInt();
            pumpkin = buf.readInt();
        }
    }

    @Override
    public void serializeToView(ByteBuf buf)
    {
        super.serializeToView(buf);

        buf.writeInt(wheat);
        buf.writeInt(potato);
        buf.writeInt(carrot);
        buf.writeInt(melon);
        buf.writeInt(pumpkin);
    }

    public int getFarmRadius()
    {
        return getBuildingLevel()+3;
    }
}
