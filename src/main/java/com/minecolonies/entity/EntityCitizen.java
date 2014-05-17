package com.minecolonies.entity;

import com.minecolonies.tileentities.TileEntityHutWorker;
import com.minecolonies.tileentities.TileEntityTownHall;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class EntityCitizen extends EntityAgeable implements INpc
{
    public ResourceLocation texture;
    public EnumCitizenLevel level;
    Random random = new Random();
    private String job;
    private TileEntityHutWorker tileEntityHutWorker;
    private TileEntityTownHall tileEntityTownHall;
    private int townPosX, townPosY, townPosZ;

    public EntityCitizen(World world)
    {
        super(world);
        setSize(.6f, 1.8f);
        this.level = random.nextBoolean() ? EnumCitizenLevel.CITIZENMALE : EnumCitizenLevel.CITIZENFEMALE;
        setTexture();
        job = "Citizen";
    }

    public void onEntityUpdate()
    {
        super.onEntityUpdate();
        if (tileEntityTownHall == null)
        {
            tileEntityTownHall = (TileEntityTownHall) worldObj.getTileEntity(townPosX, townPosY, townPosZ);
        }
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
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.5d);
    }

    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        if (tileEntityTownHall != null)
        {
            LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(worldObj, tileEntityTownHall.getOwners()), "tile.blockHutTownhall.messageColonistDead");

            tileEntityTownHall.removeCitizen(this);
        }
        super.onDeath(par1DamageSource);

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

    public TileEntityTownHall getTownHall()
    {
        return tileEntityTownHall;
    }

    public void setTownHall(TileEntityTownHall tileEntityTownHall)
    {
        this.tileEntityTownHall = tileEntityTownHall;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeEntityToNBT(nbtTagCompound);
        nbtTagCompound.setString("job", job);
        nbtTagCompound.setInteger("level", level.getLevel());
        nbtTagCompound.setInteger("sex", level.getSexInt());

        if (tileEntityTownHall != null) {
            NBTTagCompound nbtTagTownhallCompound = new NBTTagCompound();
            nbtTagTownhallCompound.setInteger("x", tileEntityTownHall.xCoord);
            nbtTagTownhallCompound.setInteger("y", tileEntityTownHall.yCoord);
            nbtTagTownhallCompound.setInteger("z", tileEntityTownHall.zCoord);
            nbtTagCompound.setTag("townhall", nbtTagTownhallCompound);
        }
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

        if (nbtTagCompound.hasKey("townhall")) {
            NBTTagCompound nbtTagTownhallCompound = nbtTagCompound.getCompoundTag("townhall");
            townPosX = nbtTagTownhallCompound.getInteger("x");
            townPosY = nbtTagTownhallCompound.getInteger("y");
            townPosZ = nbtTagTownhallCompound.getInteger("z");
        }
    }
}
