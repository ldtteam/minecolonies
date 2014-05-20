package com.minecolonies.entity;

import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.tileentities.TileEntityHut;
import com.minecolonies.tileentities.TileEntityHutWorker;
import com.minecolonies.tileentities.TileEntityTownHall;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityCitizen extends EntityAgeable implements IInvBasic, INpc
{
    public  ResourceLocation  texture;
    public  EnumCitizenLevel  level;
    private EnumCitizenAction currentAction;
    private String            job;
    private InventoryCitizen  inventory;

    private TileEntityTownHall tileEntityTownHall;
    private int                townPosX, townPosY, townPosZ;
    private TileEntityHutWorker tileEntityWorkHut;
    private int                 workPosX, workPosY, workPosZ;
    public TileEntityHut tileEntityHomeHut;
    int homePosX, homePosY, homePosZ;

    public EntityCitizen(World world)
    {
        super(world);
        setSize(.6f, 1.8f);
        this.level = worldObj.rand.nextBoolean() ? EnumCitizenLevel.CITIZENMALE : EnumCitizenLevel.CITIZENFEMALE;
        setTexture();
        currentAction = EnumCitizenAction.IDLE;
        job = "Citizen";
    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        if(tileEntityTownHall == null)
        {
            tileEntityTownHall = (TileEntityTownHall) worldObj.getTileEntity(townPosX, townPosY, townPosZ);
        }
        if(tileEntityWorkHut == null)
        {
            tileEntityWorkHut = (TileEntityHutWorker) worldObj.getTileEntity(workPosX, workPosY, workPosZ);
        }
        if(tileEntityHomeHut == null)
        {
            tileEntityHomeHut = (TileEntityHut) worldObj.getTileEntity(homePosX, homePosY, homePosZ);
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
        if(tileEntityTownHall != null)
        {
            LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(worldObj, tileEntityTownHall.getOwners()), "tile.blockHutTownhall.messageColonistDead");

            tileEntityTownHall.removeCitizen(this);
        }
        super.onDeath(par1DamageSource);
    }

    public void setTexture()
    {
        texture = new ResourceLocation(level.getTexture() + (worldObj.rand.nextInt(3) + 1) + ".png");
    }

    public String getJob()
    {
        return job;
    }

    public void setJob(String job, TileEntity tileEntity)
    {
        this.job = job;
        this.tileEntityWorkHut = (TileEntityHutWorker) tileEntity;
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

        if(tileEntityTownHall != null)
        {
            NBTTagCompound nbtTagTownhallCompound = new NBTTagCompound();
            nbtTagTownhallCompound.setInteger("x", tileEntityTownHall.xCoord);
            nbtTagTownhallCompound.setInteger("y", tileEntityTownHall.yCoord);
            nbtTagTownhallCompound.setInteger("z", tileEntityTownHall.zCoord);
            nbtTagCompound.setTag("townhall", nbtTagTownhallCompound);
        }
        if(tileEntityWorkHut != null)
        {
            NBTTagCompound nbtTagWorkHutCompound = new NBTTagCompound();
            nbtTagWorkHutCompound.setInteger("x", tileEntityWorkHut.xCoord);
            nbtTagWorkHutCompound.setInteger("y", tileEntityWorkHut.yCoord);
            nbtTagWorkHutCompound.setInteger("z", tileEntityWorkHut.zCoord);
            nbtTagCompound.setTag("workhut", nbtTagWorkHutCompound);
        }
        if(tileEntityHomeHut != null)
        {
            NBTTagCompound nbtTagHomeHutCompound = new NBTTagCompound();
            nbtTagHomeHutCompound.setInteger("x", tileEntityHomeHut.xCoord);
            nbtTagHomeHutCompound.setInteger("y", tileEntityHomeHut.yCoord);
            nbtTagHomeHutCompound.setInteger("z", tileEntityHomeHut.zCoord);
            nbtTagCompound.setTag("homehut", nbtTagHomeHutCompound);
        }

        nbtTagCompound.setInteger("currentAction", currentAction.getActionID());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readEntityFromNBT(nbtTagCompound);
        this.job = nbtTagCompound.getString("job");

        int level = nbtTagCompound.hasKey("level") ? nbtTagCompound.getInteger("level") : this.level.getLevel();
        int sex = nbtTagCompound.hasKey("sex") ? nbtTagCompound.getInteger("sex") : this.level.getSexInt();

        for(EnumCitizenLevel citizenLevel : EnumCitizenLevel.values())
        {
            if(citizenLevel.getLevel() == level && citizenLevel.getSexInt() == sex)
            {
                this.level = citizenLevel;
            }
        }

        if(nbtTagCompound.hasKey("townhall"))
        {
            NBTTagCompound nbtTagTownhallCompound = nbtTagCompound.getCompoundTag("townhall");
            townPosX = nbtTagTownhallCompound.getInteger("x");
            townPosY = nbtTagTownhallCompound.getInteger("y");
            townPosZ = nbtTagTownhallCompound.getInteger("z");
        }
        if(nbtTagCompound.hasKey("workhut"))
        {
            NBTTagCompound nbtTagWorkHutCompound = nbtTagCompound.getCompoundTag("workhut");
            workPosX = nbtTagWorkHutCompound.getInteger("x");
            workPosY = nbtTagWorkHutCompound.getInteger("y");
            workPosZ = nbtTagWorkHutCompound.getInteger("z");
        }
        if(nbtTagCompound.hasKey("homehut"))
        {
            NBTTagCompound nbtTagHomeHutCompound = nbtTagCompound.getCompoundTag("homehut");
            homePosX = nbtTagHomeHutCompound.getInteger("x");
            homePosY = nbtTagHomeHutCompound.getInteger("y");
            homePosZ = nbtTagHomeHutCompound.getInteger("z");
        }

        currentAction = EnumCitizenAction.getActionById(nbtTagCompound.getInteger("currentAction"));
    }

    @Override
    public void onInventoryChanged(InventoryBasic inventoryBasic){}

    public InventoryCitizen getInventory()
    {
        return inventory;
    }

    /*private static final int DATAWATCHER_CURRENTACTION = 24;

    public EnumCitizenAction getCurrentAction()
    {
        return EnumCitizenAction.getActionById(dataWatcher.getWatchableObjectInt(DATAWATCHER_CURRENTACTION));
    }

    public void setCurrentAction(EnumCitizenAction action)
    {
        this.dataWatcher.updateObject(DATAWATCHER_CURRENTACTION, action.getActionID());
    }

    public void setCurrentAction(int actionID)
    {
        this.dataWatcher.updateObject(DATAWATCHER_CURRENTACTION, actionID);
    }*/
}
