package com.minecolonies.tileentities;

import com.minecolonies.inventory.InventoryCitizen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

public abstract class TileEntityHut extends TileEntityBuildable
{
    private int maxInhabitants;
    private int maleInhabitants;
    private int femaleInhabitants;

    public abstract void breakBlock();

    public int getMaxInhabitants()
    {
        return maxInhabitants;
    }

    public void setMaxInhabitants(int maxInhabitants)
    {
        this.maxInhabitants = maxInhabitants;
    }

    public int getMaleInhabitants()
    {
        return maleInhabitants;
    }

    public void setMaleInhabitants(int maleInhabitants)
    {
        this.maleInhabitants = maleInhabitants;
    }

    public int getFemaleInhabitants()
    {
        return femaleInhabitants;
    }

    public void setFemaleInhabitants(int femaleInhabitants)
    {
        this.femaleInhabitants = femaleInhabitants;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return super.isUseableByPlayer(player) && this.isPlayerOwner(player);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        this.readFromNBT(packet.func_148857_g());
    }

    @Override
    public S35PacketUpdateTileEntity getDescriptionPacket()
    {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        this.writeToNBT(nbtTagCompound);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbtTagCompound);
    }
}
