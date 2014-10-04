package com.minecolonies.entity;

import com.minecolonies.MineColonies;
import com.minecolonies.client.gui.GuiEntityCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.entity.ai.EntityAIGoHome;
import com.minecolonies.entity.ai.EntityAISleep;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.GuiHandler;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.UUID;

import static net.minecraftforge.common.util.Constants.NBT;

public class EntityCitizen extends EntityAgeable implements IInvBasic, INpc
{
    public  ResourceLocation texture;
    private InventoryCitizen inventory;

    private UUID        colonyId;   //  Client and Server
    private UUID        citizenId;  //  Client Only

    private Colony      colony;
    private CitizenData citizenData;

    //private ColonyJob colonyJob;

    protected Status status = Status.IDLE;

    private static final int DATA_TEXTURE    = 13;
    private static final int DATA_LEVEL      = 14;
    private static final int DATA_IS_FEMALE  = 15;
    private static final int DATA_COLONY_ID  = 16;
    private static final int DATA_CITIZEN_ID = 17;  //  Because Entity UniqueIDs are not identical between client and server

    public EntityCitizen(World world, UUID id)
    {
        this(world);
        entityUniqueID = id;
    }

    public EntityCitizen(World world)
    {
        super(world);
        setSize(0.6F, 1.8F);
        this.func_110163_bv();//Set persistenceRequired = true;
        setTexture();
        this.setAlwaysRenderNameTag(true);//TODO: configurable
        this.inventory = new InventoryCitizen("Minecolonies Inventory", false, 27);
        this.inventory.addIInvBasic(this);

        this.renderDistanceWeight = 2.0D;

        this.getNavigator().setAvoidsWater(true);
        this.getNavigator().setCanSwim(true);
        this.getNavigator().setEnterDoors(true);
        initTasks();
    }

    public boolean isWorker(){ return false; }

    @Override
    public void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(DATA_COLONY_ID, "");
        dataWatcher.addObject(DATA_CITIZEN_ID, "");
        dataWatcher.addObject(DATA_TEXTURE, worldObj.rand.nextInt(3) + 1);//textureID
        dataWatcher.addObject(DATA_LEVEL, 0);
        dataWatcher.addObject(DATA_IS_FEMALE, 0);
    }

    protected void initTasks()
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityMob.class, 8.0F, 0.6D, 0.6D));
        this.tasks.addTask(2, new EntityAIGoHome(this));
        this.tasks.addTask(3, new EntityAISleep(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(6, new EntityAIWatchClosest2(this, EntityCitizen.class, 5.0F, 0.02F));
        this.tasks.addTask(7, new EntityAIWander(this, 0.6D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityLiving.class, 6.0F));

//        if (colonyJob != null)
//        {
//            colonyJob.addTasks(this.tasks);
//        }
    }

//    public ColonyJob getColonyJob(){ return colonyJob; }
//    public void setColonyJob(ColonyJob j)
//    {
//        Object currentTasks[] = this.tasks.taskEntries.toArray();
//        for (Object task : currentTasks)
//        {
//            this.tasks.removeTask(((EntityAITasks.EntityAITaskEntry)task).action);
//        }
//
//        colonyJob = j;
//
//        initTasks();
//    }

    protected String getJobName()
    {
        return "Citizen";
    }

    public void setTexture()
    {
        String textureBase = "textures/entity/";
        if (getJobName().equals("Citizen"))
        {
            switch (getLevel())
            {
                case 0:
                    textureBase += "Settler";
                    break;
                case 1:
                    textureBase += "Citizen";
                    break;
                case 2:
                    textureBase += "Noble";
                    break;
                case 3:
                    textureBase += "Aristocrat";
                    break;
            }
        }
        else
        {
            textureBase += getJobName(); //colonyJob.getName();
        }

        textureBase += isFemale() ? "Female" : "Male";

        texture = new ResourceLocation(Constants.MODID, textureBase + getTextureID() + ".png");
    }

    @Override
    public boolean isAIEnabled()
    {
        return true;
    }

    @Override
    public void onLivingUpdate()
    {
        setTexture();
        updateColony();
        updateArmSwingProgress();
        super.onLivingUpdate();
    }

    private void updateColony()
    {
        if (worldObj.isRemote)
        {
            if (colonyId == null)
            {
                String colonyIdString = dataWatcher.getWatchableObjectString(DATA_COLONY_ID);
                if (colonyIdString != null)
                {
                    colonyId = UUID.fromString(colonyIdString);
                }
            }

            if (citizenId == null)
            {
                String citizenIdString = dataWatcher.getWatchableObjectString(DATA_CITIZEN_ID);
                if (citizenIdString != null)
                {
                    citizenId = UUID.fromString(citizenIdString);
                }
            }

            return;
        }

        if (colonyId == null)
        {
            setDead();
            return;
        }

        if (colony == null)
        {
            Colony c = ColonyManager.getColonyById(colonyId);

            if (c == null)
            {
                setDead();
                return;
            }

            CitizenData data = c.getCitizen(getUniqueID());
            if (data == null)
            {
                //  Citizen does not exist in the Colony
                MineColonies.logger.warn(String.format("Citizen '%s' attempting to register with colony, but not known to colony",
                        getUniqueID()));
                setDead();
                return;
            }

            EntityCitizen existingCitizen = data.getCitizenEntity();
            if (existingCitizen != null && existingCitizen != this)
            {
                //  This Citizen already has a different Entity registered to it
                MineColonies.logger.warn(String.format("Citizen '%s' attempting to register with colony, but already have a citizen ('%s')",
                        getUniqueID(), existingCitizen.getUniqueID()));
                setDead();
                return;
            }

            setColony(c, data);
        }

        if (isWorker())
        {
            //  Worker entity subclass, with no work building
            if (citizenData.getWorkBuilding() == null)
            {
                removeFromWorkBuilding();
            }
        }
        else if (citizenData.getWorkBuilding() != null)
        {
            //  Non-Worker entity subclass, with a work building - become that worker subclass
            addToWorkBuilding(citizenData.getWorkBuilding());
        }

//        BuildingWorker b = (workBuilding != null) ? workBuilding.get() : null;
//        if (b != null)
//        {
//            if (colonyJob == null)
//            {
//                ColonyJob newJob = b.createJob(this);
//                if (newJob != null)
//                {
//                    setColonyJob(newJob);
//                }
//            }
//        }
//        else if (colonyJob != null)
//        {
//            setColonyJob(null);
//        }
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
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(100);//path finding search range
    }

    @Override
    public boolean interact(EntityPlayer player)
    {
        if (worldObj.isRemote)
        {
            CitizenData.View view = getCitizenDataView();
            if (view != null)
            {
                GuiHandler.showGuiScreen(new GuiEntityCitizen(this, view, player, worldObj));
            }
        }
        return true;
    }

    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        if (colony != null)
        {
            LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(worldObj, colony.getOwners()), "tile.blockHutTownhall.messageColonistDead", citizenData.getName());
            colony.removeCitizen(this);
        }

        super.onDeath(par1DamageSource);
    }

    public int getTextureID()
    {
        return dataWatcher.getWatchableObjectInt(DATA_TEXTURE);
    }

    public int getLevel()
    {
        return dataWatcher.getWatchableObjectInt(DATA_LEVEL);
    }

    private void updateLevel()
    {
        dataWatcher.updateObject(DATA_LEVEL, citizenData != null ? citizenData.getLevel() : 0);
    }

    public boolean isFemale()
    {
        return (dataWatcher.getWatchableObjectInt(DATA_IS_FEMALE) != 0);
    }

    public CitizenData getCitizenData()
    {
        return citizenData;
    }

    public CitizenData.View getCitizenDataView()
    {
        if (colonyId != null && citizenId != null)
        {
            ColonyView colonyView = ColonyManager.getColonyView(colonyId);
            if (colonyView != null)
            {
                return colonyView.getCitizen(citizenId);
            }
        }

        return null;
    }

    public Colony getColony() { return colony; }
    public void clearColony() { setColony(null, null); }
    public void setColony(Colony c, CitizenData data)
    {
        if (c == null)
        {
            colony = null;
            colonyId = null;
            citizenData = null;
            setDead();
            return;
        }

        colony = c;
        colonyId = colony.getID();
        citizenData = data;

        setCustomNameTag(citizenData.getName());

        dataWatcher.updateObject(DATA_COLONY_ID, colonyId.toString());
        dataWatcher.updateObject(DATA_CITIZEN_ID, citizenData.getId().toString());
        dataWatcher.updateObject(DATA_IS_FEMALE, citizenData.isFemale() ? 1 : 0);
        dataWatcher.updateObject(DATA_TEXTURE, citizenData.getTextureId());
        updateLevel();

        setTexture();

        citizenData.setCitizenEntity(this);
    }

    public BuildingHome getHomeBuilding()
    {
        return (citizenData != null) ? citizenData.getHomeBuilding() : null;
    }

    public ChunkCoordinates getHomePosition()
    {
        BuildingHome homeBuilding = getHomeBuilding();
        if (homeBuilding != null)
        {
            return homeBuilding.getLocation();
        }
        else if (getColony() != null && getColony().getTownhall() != null)
        {
            return getColony().getTownhall().getLocation();
        }

        return null;
    }

    public boolean isAtHome()
    {
        ChunkCoordinates homePosition = getHomePosition();
        return homePosition != null &&
                homePosition.getDistanceSquared((int)posX, (int)posY, (int)posZ) <= 16;
    }

    public BuildingWorker getWorkBuilding()
    {
        return (citizenData != null) ? citizenData.getWorkBuilding() : null;
    }

    public void addToWorkBuilding(BuildingWorker building)
    {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        this.setDead();

        EntityCitizen worker = building.createWorker(worldObj);
        worker.readFromNBT(nbt);
        worker.setColony(colony, citizenData);

        worldObj.spawnEntityInWorld(worker);
        ChunkCoordUtils.tryMoveLivingToXYZ(worker, building.getLocation());

        clearColony();
    }

    public void removeFromWorkBuilding()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        this.setDead();

        EntityCitizen citizen = new EntityCitizen(worldObj);
        citizen.readFromNBT(nbt);
        citizen.setColony(colony, citizenData);

        worldObj.spawnEntityInWorld(citizen);

        clearColony();
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public Vec3 getPosition()
    {
        return Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        compound.setInteger("status", status.ordinal());

        if (colony != null)
        {
            compound.setString("colony", colony.getID().toString());
        }
//        if (colonyJob != null)
//        {
//            NBTTagCompound jobCompound = new NBTTagCompound();
//            colonyJob.writeToNBT(jobCompound);
//            compound.setTag("job", jobCompound);
//        }

        NBTTagList inventoryList = new NBTTagList();
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            if (inventory.getStackInSlot(i) != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("slot", i);
                inventory.getStackInSlot(i).writeToNBT(tag);
                inventoryList.appendTag(tag);
            }
        }
        compound.setTag("Inventory", inventoryList);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);

        status = Status.values()[compound.getInteger("status")];

        if (compound.hasKey("colony"))
        {
            colonyId = UUID.fromString(compound.getString("colony"));
        }
//        if (compound.hasKey("job"))
//        {
//            setColonyJob(ColonyJob.createFromNBT(this, compound.getCompoundTag("job")));
//        }

        NBTTagList nbttaglist = compound.getTagList("Inventory", NBT.TAG_COMPOUND);
        for (int i = 0; i < nbttaglist.tagCount(); i++)
        {
            NBTTagCompound tag = nbttaglist.getCompoundTagAt(i);
            int slot = tag.getInteger("slot");
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(tag);
            inventory.setInventorySlotContents(slot, itemstack);
        }
    }

    public int getOffsetTicks()
    {
        return this.ticksExisted + 7 * this.getEntityId();
    }

    public boolean isWorkTime()
    {
        return worldObj.isDaytime() && !worldObj.isRaining();
    }

    public boolean isSleepTime()
    {
        return !worldObj.isDaytime();
    }

    public EntityItem entityDropItem(ItemStack itemstack)
    {
        return entityDropItem(itemstack, getEyeHeight() - 0.3F);
    }

    @Override
    protected void dropEquipment(boolean par1, int par2)
    {
        for (int i = 0; i < getLastActiveItems().length; i++) setCurrentItemOrArmor(i, null);
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);
            if (itemstack != null && itemstack.stackSize > 0)
            {
                entityDropItem(itemstack);
            }
        }
    }

    @Override
    protected int getExperiencePoints(EntityPlayer par1EntityPlayer)
    {
        return 0;
    }

    @Override
    public void onInventoryChanged(InventoryBasic inventoryBasic){}

    public InventoryCitizen getInventory()
    {
        return inventory;
    }

    public void setInventorySize(int newSize, boolean dropLeftovers)
    {
        if (!worldObj.isRemote)
        {
            InventoryCitizen newInventory = new InventoryCitizen(inventory.getInventoryName(), inventory.hasCustomInventoryName(), newSize);
            ArrayList<ItemStack> leftovers = new ArrayList<ItemStack>();
            for (int i = 0; i < inventory.getSizeInventory(); i++)
            {
                ItemStack itemstack = inventory.getStackInSlot(i);
                if (i < newInventory.getSizeInventory())
                {
                    newInventory.setInventorySlotContents(i, itemstack);
                }
                else
                {
                    if (itemstack != null) leftovers.add(itemstack);
                }
            }
            inventory = newInventory;
            inventory.addIInvBasic(this);
            if (dropLeftovers)
            {
                for (ItemStack leftover : leftovers)
                {
                    if (leftover.stackSize > 0)
                    {
                        entityDropItem(leftover);
                    }
                }
            }
        }
    }

    /**
     * Used for chat messages, sounds, and other need based interactions
     * Created: June 20, 2014
     *
     * @author Colton
     */
    public static enum Status
    {
        IDLE, SLEEPING, WORKING, GETTING_ITEMS, NEED_ASSISTANCE, PATHFINDING_ERROR
    }
}
