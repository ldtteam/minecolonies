package com.minecolonies.tileentities;

import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class ScarecrowTileEntity extends TileEntity
{
    
    private final Random random = new Random();
    private Boolean TYPE;
    
    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        
        compound.setBoolean("type", this.getType());
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        
        TYPE = compound.getBoolean("type");
    }
    
    public boolean getType() {
        if(this.TYPE == null) {
            this.TYPE = this.random.nextBoolean();
        }
        return this.TYPE;
    }
}