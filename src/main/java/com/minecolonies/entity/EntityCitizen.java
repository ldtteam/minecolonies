package com.minecolonies.entity;

import com.minecolonies.client.gui.GuiEntityCitizen;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.ai.EntityAIGoHome;
import com.minecolonies.entity.ai.EntityAISleep;
import com.minecolonies.entity.jobs.ColonyJob;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.GuiHandler;
import com.minecolonies.tileentities.TileEntityHutCitizen;
import com.minecolonies.tileentities.TileEntityHutWorker;
import com.minecolonies.tileentities.TileEntityTownHall;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.minecraftforge.common.util.Constants.NBT;

public class EntityCitizen extends EntityAgeable implements IInvBasic, INpc
{
    public static final int SEX_MALE   = 0;
    public static final int SEX_FEMALE = 1;

    public int strength, stamina, wisdom, intelligence, charisma;
    public  ResourceLocation texture;
    private String           job;
    private InventoryCitizen inventory;

    private UUID                            colonyId;
    private WeakReference<Colony>           colony;
    private ChunkCoordinates                homeBuildingId;
    private WeakReference<BuildingHome>     homeBuilding;
    private ChunkCoordinates                workBuildingId;
    private WeakReference<BuildingWorker>   workBuilding;

    private ColonyJob                       colonyJob;

    //  OLD CODE
//    private TileEntityTownHall   tileEntityTownHall;
//    private ChunkCoordinates     townPos;
//    private TileEntityHutWorker  tileEntityWorkHut;
//    private ChunkCoordinates     workPos;
//    private TileEntityHutCitizen tileEntityHomeHut;
//    private ChunkCoordinates     homePos;
    //  END OLD CODE

    protected Status status = Status.IDLE;

    public EntityCitizen(World world, Colony colony)
    {
        this(world);
        this.colony = new WeakReference<Colony>(colony);
        this.colonyId = colony.getID();
    }

    public EntityCitizen(World world)
    {
        super(world);
        setSize(0.6F, 1.8F);
        this.func_110163_bv();//Set persistenceRequired = true;
        this.job = initJob();
        setTexture();
        this.setCustomNameTag(generateName());
        this.setAlwaysRenderNameTag(true);//TODO: configurable
        this.inventory = new InventoryCitizen("Minecolonies Inventory", false, 27);
        this.inventory.addIInvBasic(this);

        this.strength = worldObj.rand.nextInt(10) + 1;
        this.stamina = worldObj.rand.nextInt(10) + 1;
        this.wisdom = worldObj.rand.nextInt(10) + 1;
        this.intelligence = worldObj.rand.nextInt(10) + 1;
        this.charisma = worldObj.rand.nextInt(10) + 1;

        this.getNavigator().setAvoidsWater(true);
        this.getNavigator().setCanSwim(true);
        this.getNavigator().setEnterDoors(true);
        initTasks();
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(13, worldObj.rand.nextInt(3) + 1);//textureID
        dataWatcher.addObject(14, 0);//level
        dataWatcher.addObject(15, worldObj.rand.nextInt(2));//sex
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

    public ColonyJob getColonyJob() { return colonyJob; }
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

    protected String initJob()
    {
        return "Citizen";
    }

    public void setTexture()
    {
        String textureBase = "textures/entity/";
        if (colonyJob == null)
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
            textureBase += colonyJob.getName();
        }

        textureBase += getSex() == SEX_MALE ? "Male" : "Female";

        texture = new ResourceLocation(Constants.MODID, textureBase + getTextureID() + ".png");
    }

    private String generateName()
    {
        String firstName;
        if(getSex() == 0)
        {
            firstName = getRandomElement(Configurations.maleFirstNames);
        }
        else
        {
            firstName = getRandomElement(Configurations.femaleFirstNames);
        }
        return String.format("%s %s. %s", firstName, getRandomLetter(), getRandomElement(Configurations.lastNames));
    }

    private String getRandomElement(String[] array)
    {
        return array[rand.nextInt(array.length)];
    }

    private char getRandomLetter()
    {
        return (char) (rand.nextInt(26) + 'A');
    }

    @Override
    public boolean isAIEnabled()
    {
        return true;
    }

    @Override
    public void onLivingUpdate()
    {
        this.setTexture();
        //updateTileEntities();
        updateColony();
        super.onLivingUpdate();
    }

    //  OLD CODE
//    private void updateTileEntities()
//    {
//        if(tileEntityTownHall == null && townPos != null)
//        {
//            tileEntityTownHall = (TileEntityTownHall) ChunkCoordUtils.getTileEntity(worldObj, townPos);
//        }
//        if(tileEntityWorkHut == null && workPos != null)
//        {
//            tileEntityWorkHut = (TileEntityHutWorker) ChunkCoordUtils.getTileEntity(worldObj, workPos);
//        }
//        if(tileEntityHomeHut == null && homePos != null)
//        {
//            tileEntityHomeHut = (TileEntityHutCitizen) ChunkCoordUtils.getTileEntity(worldObj, homePos);
//        }
//    }
    //  END OLD CODE

    private void updateColony()
    {
        if (worldObj.isRemote)
        {
            return;
        }

        if (colony == null && colonyId != null)
        {
            Colony c = ColonyManager.getColonyById(colonyId);
            if (c != null)
            {
                if (!c.registerCitizen(this))
                {
                    //  Failed to register citizen to the Colony, it must not actually be a citizen of the colony anymore
                    setDead();
                    c = null;
                }
            }

            colony = new WeakReference<Colony>(c);
        }

        if (homeBuilding == null && homeBuildingId != null && colony != null)
        {
            Colony c = colony.get();
            Building b = (c != null) ? c.getBuilding(homeBuildingId) : null;
            BuildingHome bCitizen = (b instanceof BuildingHome) ? (BuildingHome)b : null;
            homeBuilding = new WeakReference<BuildingHome>(bCitizen);
        }

        if (workBuilding == null && workBuildingId != null && colony != null)
        {
            Colony c = colony.get();
            Building b = (c != null) ? c.getBuilding(workBuildingId) : null;
            BuildingWorker bWorker = (b instanceof BuildingWorker) ? (BuildingWorker)b : null;
            workBuilding = new WeakReference<BuildingWorker>(bWorker);
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
        //  OLD CODE
//        if (this.getTownHall() != null && this.getTownHall().getCitizens().contains(this.getUniqueID()))
//        {
//            LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(worldObj, tileEntityTownHall.getOwners()), "tile.blockHutTownhall.messageColonistDead");
//
//            tileEntityTownHall.removeCitizen(this);
//        }
//        if (this.getHomeHut() != null)
//        {
//            this.getHomeHut().removeCitizen(this);
//        }
//        if (this.getWorkHut() != null)
//        {
//            this.getWorkHut().unbindWorker(this);
//        }
        //  END OLD CODE

        Colony c = (colony != null) ? colony.get() : null;
        if (c != null)
        {
            LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(worldObj, new ArrayList<UUID>(c.getOwners())), "tile.blockHutTownhall.messageColonistDead");
            c.removeCitizen(this);
        }

        BuildingHome bHome = (homeBuilding != null) ? homeBuilding.get() : null;
        if (bHome != null)
        {
            bHome.removeCitizen(this);
        }

        BuildingWorker bWorker = (workBuilding != null) ? workBuilding.get() : null;
        if (bWorker != null)
        {
            bWorker.unbindWorker(this);
        }

        super.onDeath(par1DamageSource);
    }

    public int getTextureID()
    {
        return dataWatcher.getWatchableObjectInt(13);
    }

    public void setTextureID(int textureID)
    {
        dataWatcher.updateObject(13, textureID);
    }

    public int getLevel()
    {
        return dataWatcher.getWatchableObjectInt(14);
    }

    public void setLevel(int level)
    {
        dataWatcher.updateObject(14, level);
    }

    public int getSex()
    {
        return dataWatcher.getWatchableObjectInt(15);
    }

    public void setSex(int sex)
    {
        dataWatcher.updateObject(15, sex);
    }

    public String getJob()
    {
        return job;
    }

    public void setJob(String job, TileEntity tileEntity)
    {
//        this.job = job;
//        this.tileEntityWorkHut = (TileEntityHutWorker) tileEntity;
    }

    public TileEntityTownHall getTownHall()
    {
        return null; //tileEntityTownHall;
    }

    public TileEntityHutCitizen getHomeHut()
    {
        return null; //tileEntityHomeHut;
    }

    public TileEntityHutWorker getWorkHut()
    {
        return null; //tileEntityWorkHut;
    }

    public void setTownHall(TileEntityTownHall tileEntityTownHall)
    {
//        this.tileEntityTownHall = tileEntityTownHall;
    }

    public void setHomeHut(TileEntityHutCitizen home)
    {
//        this.tileEntityHomeHut = home;
    }

    public void setWorkHut(TileEntityHutWorker work)
    {
//        this.tileEntityWorkHut = work;
    }

    public Colony getColony() { return (colony != null) ? colony.get() : null; }
    public void setColony(Colony c)
    {
        //  Only for use by Colony when spawning the EntityCitizen for the first time
        colony = new WeakReference<Colony>(c);
        colonyId = c.getID();
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
        this.setDead();

        EntityCitizen worker = building.createWorker(worldObj);
        worker.readFromNBT(nbt);
        building.bindWorker(worker);
        worldObj.spawnEntityInWorld(worker);
        ChunkCoordUtils.tryMoveLivingToXYZ(worker, building.getLocation());

        if (getColony() != null)
        {
            getColony().registerCitizen(worker);
            worker.setColony(getColony());
        }

        BuildingHome home = getHomeBuilding();
        if (home != null)
        {
            home.replaceCitizen(this, worker);
        }
    }

    public void removeFromWorkBuilding()
    {
        setWorkBuilding(null);

        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        this.setDead();

        EntityCitizen citizen = new EntityCitizen(worldObj);
        citizen.readFromNBT(nbt);
        worldObj.spawnEntityInWorld(citizen);

        if (getColony() != null)
        {
            getColony().registerCitizen(citizen);
            citizen.setColony(getColony());
        }

        BuildingHome home = getHomeBuilding();
        if (home != null)
        {
            home.replaceCitizen(this, citizen);
        }
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
        //compound.setString("job", job);
        compound.setInteger("status", status.ordinal());
        compound.setInteger("level", getLevel());
        compound.setInteger("textureID", getTextureID());
        compound.setInteger("sex", getSex());

        NBTTagCompound nbtTagSkillsCompound = new NBTTagCompound();
        nbtTagSkillsCompound.setInteger("strength", strength);
        nbtTagSkillsCompound.setInteger("stamina", stamina);
        nbtTagSkillsCompound.setInteger("wisdom", wisdom);
        nbtTagSkillsCompound.setInteger("intelligence", intelligence);
        nbtTagSkillsCompound.setInteger("charisma", charisma);
        compound.setTag("skills", nbtTagSkillsCompound);

        //  OLD CODE
//        if (tileEntityTownHall != null)
//        {
//            ChunkCoordUtils.writeToNBT(compound, "townhall", tileEntityTownHall.getPosition());
//        }
//        if (tileEntityWorkHut != null)
//        {
//            ChunkCoordUtils.writeToNBT(compound, "workhut", tileEntityWorkHut.getPosition());
//        }
//        if (tileEntityHomeHut != null)
//        {
//            ChunkCoordUtils.writeToNBT(compound, "homehut", tileEntityHomeHut.getPosition());
//        }
        //  END OLD CODE

        if (colonyId != null)
        {
            compound.setString("colony", colonyId.toString());
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

        //this.job = compound.getString("job");
        status = Status.values()[compound.getInteger("status")];

        setTextureID(compound.getInteger("textureID"));
        setLevel(compound.hasKey("level") ? compound.getInteger("level") : this.getLevel());
        setSex(compound.hasKey("sex") ? compound.getInteger("sex") : this.getSex());
        setTexture();

        NBTTagCompound nbtTagSkillsCompound = compound.getCompoundTag("skills");
        strength = nbtTagSkillsCompound.getInteger("strength");
        stamina = nbtTagSkillsCompound.getInteger("stamina");
        wisdom = nbtTagSkillsCompound.getInteger("wisdom");
        intelligence = nbtTagSkillsCompound.getInteger("intelligence");
        charisma = nbtTagSkillsCompound.getInteger("charisma");

        //  OLD CODE
//        if (compound.hasKey("townhall"))
//        {
//            townPos = ChunkCoordUtils.readFromNBT(compound, "townhall");
//        }
//        if (compound.hasKey("workhut"))
//        {
//            workPos = ChunkCoordUtils.readFromNBT(compound, "workhut");
//        }
//        if (compound.hasKey("homehut"))
//        {
//            homePos = ChunkCoordUtils.readFromNBT(compound, "homehut");
//        }
        //  END OLD CODE

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

    public void addToWorkHut(TileEntityHutWorker tileEntityHutWorker)
    {
//        setJob(tileEntityHutWorker.getJobName(), tileEntityHutWorker);
//
//        NBTTagCompound nbt = new NBTTagCompound();
//        this.writeToNBT(nbt);
//        getTownHall().removeCitizen(this);
//        this.setDead();
//
//        EntityCitizen worker = tileEntityHutWorker.createWorker();
//        worker.readFromNBT(nbt);
//        getTownHall().addCitizenToTownhall(worker);
//        tileEntityHutWorker.bindWorker(worker);
//        worldObj.spawnEntityInWorld(worker);
//        ChunkCoordUtils.tryMoveLivingToXYZ(worker, tileEntityHutWorker.getPosition());
    }

    public void removeFromWorkHut()
    {
//        NBTTagCompound nbt = new NBTTagCompound();
//        this.writeToNBT(nbt);
//        nbt.removeTag("workhut");
//        getTownHall().removeCitizen(this);
//        getWorkHut().unbindWorker(this);
//        this.setDead();
//
//        EntityCitizen citizen = new EntityCitizen(worldObj);
//        citizen.readFromNBT(nbt);
//        citizen.setJob("Citizen", null);
//        getTownHall().addCitizenToTownhall(citizen);
//        worldObj.spawnEntityInWorld(citizen);
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
