package com.minecolonies.entity;

import com.minecolonies.tileentities.TileEntityHutWorker;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.Random;

public class EntityCitizen extends EntityAgeable
{
    public ResourceLocation texture;
    public EnumCitizenLevel level;
    Random random = new Random();
    private String job;
    private TileEntityHutWorker tileEntityHutWorker;

    public EntityCitizen(World world)
    {
        super(world);
        setSize(.6f, 1.8f);
        this.level = random.nextBoolean() ? EnumCitizenLevel.CITIZENMALE : EnumCitizenLevel.CITIZENFEMALE;
        setTexture();
        job = "Citizen";
    }

    @Override
    public EntityAgeable createChild(EntityAgeable var1)
    {
        //TODO ???
        return null;
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0d);
    }

    public void setTexture()
    {
        texture = new ResourceLocation(level.getTexture() + (random.nextInt(3) + 1) + ".png");
    }

    public String getJob()
    {
        return job;
    }

    public void setJob(String job, TileEntity tileEntity)
    {
        this.job = job;
        this.tileEntityHutWorker = (TileEntityHutWorker)tileEntity;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeEntityToNBT(nbtTagCompound);
        nbtTagCompound.setString("job", job);
        nbtTagCompound.setInteger("level", level.getLevel());
        nbtTagCompound.setInteger("sex", level.getSexInt());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readEntityFromNBT(nbtTagCompound);
        this.job = nbtTagCompound.getString("job");

        int level = nbtTagCompound.hasKey("level") ? nbtTagCompound.getInteger("level") : this.level.getLevel();
        int sex = nbtTagCompound.hasKey("sex") ? nbtTagCompound.getInteger("sex") : this.level.getSexInt();

        EnumCitizenLevel[] levels = EnumCitizenLevel.values();
        for (int i = 0; i < levels.length; i++) {
            if (levels[i].getLevel() == level && levels[i].getSexInt() == sex) {
                this.level = levels[i];
            }
        }
    }
}
