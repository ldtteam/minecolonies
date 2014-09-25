package com.minecolonies.entity;

import com.minecolonies.client.gui.GuiEntityCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.entity.ai.EntityAIGoHome;
import com.minecolonies.entity.ai.EntityAISleep;
import com.minecolonies.entity.jobs.ColonyJob;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.UUID;

import static net.minecraftforge.common.util.Constants.NBT;

public class EntityCitizen extends EntityAgeable implements IInvBasic, INpc
{
    public  ResourceLocation texture;
    private InventoryCitizen inventory;

    private UUID                          colonyId;
    private Colony                        colony;
    private CitizenData                   citizenData;
    private ChunkCoordinates              homeBuildingId;
    private WeakReference<BuildingHome>   homeBuilding;
    private ChunkCoordinates              workBuildingId;
    private WeakReference<BuildingWorker> workBuilding;

    private ColonyJob colonyJob;

    protected Status status = Status.IDLE;

    private static final int DATA_TEXTURE = 13;
    private static final int DATA_LEVEL   = 14;
    private static final int DATA_GENDER  = 15;

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

        this.getNavigator().setAvoidsWater(true);
        this.getNavigator().setCanSwim(true);
        this.getNavigator().setEnterDoors(true);
        initTasks();
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(DATA_TEXTURE, worldObj.rand.nextInt(3) + 1);//textureID
        dataWatcher.addObject(DATA_LEVEL, 0);
        dataWatcher.addObject(DATA_GENDER, 0);
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

        if (colonyJob != null)
        {
            colonyJob.addTasks(this.tasks);
        }
    }

    public ColonyJob getColonyJob(){ return colonyJob; }
    public void setColonyJob(ColonyJob j)
    {
//        Object currentTasks[] = this.tasks.taskEntries.toArray();
//        for (Object task : currentTasks)
//        {
//            this.tasks.removeTask(((EntityAITasks.EntityAITaskEntry)task).action);
//        }
//
//        colonyJob = j;
//
//        initTasks();
    }

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

        textureBase += getSex() == CitizenData.SEX_MALE ? "Male" : "Female";

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
        super.onLivingUpdate();
    }

    private void updateColony()
    {
        if (worldObj.isRemote)
        {
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

//            EntityCitizen existingCitizen = colony.getCitizen(getUniqueID());
//            if (existingCitizen != null && existingCitizen != this)
//            {
//                //  There's already an existing registered EntityCitizen with this ID... we should suicide.
//                colony = null;
//                setDead();
//                return;
//            }

            CitizenData data = c.registerCitizen(this);
            if (data == null)
            {
                //  Failed to register citizen to the Colony, it must not actually be a citizen of the colony anymore
                setDead();
                return;
            }

            setColony(c, data);
        }

        if (homeBuilding == null && homeBuildingId != null)
        {
            Building b = colony.getBuilding(homeBuildingId);
            BuildingHome homeBuilding = (b instanceof BuildingHome) ? (BuildingHome)b : null;

            if (homeBuilding.isCitizen(this))
            {
                this.homeBuilding = new WeakReference<BuildingHome>(homeBuilding);
            }
            else
            {
                homeBuildingId = null;
            }
        }

        if (workBuilding == null && workBuildingId != null)
        {
            Building b = colony.getBuilding(workBuildingId);
            BuildingWorker workBuilding = (b instanceof BuildingWorker) ? (BuildingWorker)b : null;

            if (workBuilding.isWorker(this))
            {
                this.workBuilding = new WeakReference<BuildingWorker>(workBuilding);
            }
            else
            {
                workBuildingId = null;
            }
        }

        if (!this.getClass().equals(EntityCitizen.class))
        {
            if (workBuildingId == null ||
                    workBuilding == null ||
                    workBuilding.get() == null)
            {
                removeFromWorkBuilding();
            }
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
        GuiHandler.showGuiScreen(new GuiEntityCitizen(this, player, worldObj));
        return true;
    }

    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        if (colony != null)
        {
            LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(worldObj, colony.getOwners()), "tile.blockHutTownhall.messageColonistDead");
            colony.removeCitizen(this);
        }

        if (getHomeBuilding() != null)
        {
            getHomeBuilding().removeCitizen(getUniqueID());
            setHomeBuilding(null);
        }

        if (getWorkBuilding() != null)
        {
            getWorkBuilding().removeCitizen(getUniqueID());
            setWorkBuilding(null);
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

    public int getSex()
    {
        return dataWatcher.getWatchableObjectInt(DATA_GENDER);
    }

    public CitizenData getCitizenData()
    {
        return citizenData;
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
        }
        else
        {
            colony = c;
            colonyId = colony.getID();
            citizenData = data;

            //  Is this newly created Citizen Data?
            if (citizenData.getId() == null)
            {
                citizenData.setup(this);
            }

            dataWatcher.updateObject(DATA_TEXTURE, citizenData.getTexture());
            //dataWatcher.updateObject(DATA_LEVEL, citizenData.getLevel());
            dataWatcher.updateObject(DATA_GENDER, citizenData.getSex());

            setCustomNameTag(citizenData.getName());
            setTexture();
        }
    }

    public BuildingHome getHomeBuilding()
    {
        return (homeBuilding != null) ? homeBuilding.get() : null;
    }

    public void setHomeBuilding(BuildingHome b)
    {
        homeBuilding = new WeakReference<BuildingHome>(b);
        homeBuildingId = (b != null) ? b.getID() : null;
    }

    public ChunkCoordinates getHomePosition()
    {
        if (getHomeBuilding() != null)
        {
            return getHomeBuilding().getLocation();
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
        return (workBuilding != null) ? workBuilding.get() : null;
    }

    public void setWorkBuilding(BuildingWorker b)
    {
        workBuilding = new WeakReference<BuildingWorker>(b);
        workBuildingId = (b != null) ? b.getID() : null;
    }

    public void addToWorkBuilding(BuildingWorker building)
    {
        setWorkBuilding(building);

        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        setWorkBuilding(null);
        this.setDead();

        EntityCitizen worker = building.createWorker(worldObj);
        worker.readFromNBT(nbt);
        worker.setColony(colony, citizenData);
        building.setWorker(worker);
        worldObj.spawnEntityInWorld(worker);
        ChunkCoordUtils.tryMoveLivingToXYZ(worker, building.getLocation());

        if (colony != null)
        {
            colony.replaceCitizen(this, worker);
        }

        BuildingHome home = getHomeBuilding();
        if (home != null)
        {
            home.replaceCitizen(this, worker);
        }

        this.clearColony();
    }

    public void removeFromWorkBuilding()
    {
        setWorkBuilding(null);

        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        this.setDead();

        EntityCitizen citizen = new EntityCitizen(worldObj);
        citizen.readFromNBT(nbt);
        citizen.setColony(colony, citizenData);
        worldObj.spawnEntityInWorld(citizen);

        if (colony != null)
        {
            colony.replaceCitizen(this, citizen);
        }

        BuildingHome home = getHomeBuilding();
        if (home != null)
        {
            home.replaceCitizen(this, citizen);
        }

        this.clearColony();
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
        if (homeBuildingId != null && homeBuilding != null && homeBuilding.get() != null)
        {
            ChunkCoordUtils.writeToNBT(compound, "homebuilding", homeBuildingId);
        }
        if (workBuildingId != null && workBuilding != null && workBuilding.get() != null)
        {
            ChunkCoordUtils.writeToNBT(compound, "workbuilding", workBuildingId);
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
        if (compound.hasKey("homebuilding"))
        {
            homeBuildingId = ChunkCoordUtils.readFromNBT(compound, "homebuilding");
        }
        if (compound.hasKey("workbuilding"))
        {
            workBuildingId = ChunkCoordUtils.readFromNBT(compound, "workbuilding");
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
