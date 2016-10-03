package com.jlgm.structurepreview.fake;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class FakeEntity extends Entity{

	public FakeEntity(World worldIn){
		super(worldIn);
		this.setPosition(0, 0, 0);
	}

	@Override
	protected void entityInit(){
		//Not needed
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound){
		//Not needed
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound){
		//Not needed
	}

}
