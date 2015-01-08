package com.minecolonies.entity;

import com.minecolonies.MineColonies;
import com.minecolonies.client.gui.WindowCitizen;
import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.ai.EntityAIGoHome;
import com.minecolonies.entity.ai.EntityAISleep;
import com.minecolonies.entity.ai.EntityAIWork;
import com.minecolonies.colony.jobs.Job;
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
    private ResourceLocation texture;
    private InventoryCitizen inventory;

    private UUID        colonyId;   //  Client and Server
    private UUID        citizenId;  //  Client Only

    private Colony      colony;
    private CitizenData citizenData;

    protected Status status = Status.IDLE;

    private static final int DATA_TEXTURE    = 13;
    private static final int DATA_LEVEL      = 14;
    private static final int DATA_IS_FEMALE  = 15;
    private static final int DATA_COLONY_ID  = 16;
    private static final int DATA_CITIZEN_ID = 17;  //  Because Entity UniqueIDs are not identical between client and server
    private static final int DATA_MODEL_ID   = 18;

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
        this.setAlwaysRenderNameTag(Configurations.alwaysRenderNameTag);
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
        dataWatcher.addObject(DATA_TEXTURE, 0);
        dataWatcher.addObject(DATA_LEVEL, 0);
        dataWatcher.addObject(DATA_IS_FEMALE, 0);
        dataWatcher.addObject(DATA_MODEL_ID, RenderBipedCitizen.Model.SETTLER.name());
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

        onJobChanged(getColonyJob());
    }

    public Job getColonyJob(){ return citizenData != null ? citizenData.getJob() : null; }
    public <JOB extends Job> JOB getColonyJob(Class<JOB> type)
    {
        return citizenData != null ? citizenData.getJob(type) : null;
    }

    public void onJobChanged(Job job)
    {
        //  Model
        RenderBipedCitizen.Model model = RenderBipedCitizen.Model.SETTLER;

        if (job != null)
        {
            model = job.getModel();
        }
        else
        {
            switch (getLevel())
            {
                case 1: model = RenderBipedCitizen.Model.CITIZEN;    break;
                case 2: model = RenderBipedCitizen.Model.NOBLE;      break;
                case 3: model = RenderBipedCitizen.Model.ARISTOCRAT; break;
            }
        }

        dataWatcher.updateObject(DATA_MODEL_ID, model.name());

        //  AI Tasks
        Object currentTasks[] = this.tasks.taskEntries.toArray();
        for (Object task : currentTasks)
        {
            if (((EntityAITasks.EntityAITaskEntry)task).action instanceof EntityAIWork)
            {
                this.tasks.removeTask(((EntityAITasks.EntityAITaskEntry) task).action);
            }
        }

        if (job != null)
        {
            job.addTasks(this.tasks);
            ChunkCoordUtils.tryMoveLivingToXYZ(this, getWorkBuilding().getLocation());
        }
    }

    public void setTexture()
    {
        if (!worldObj.isRemote)
        {
            return;
        }

        RenderBipedCitizen.Model model = getModelID();

        String textureBase = "textures/entity/";
        textureBase += model.textureBase;
        textureBase += isFemale() ? "Female" : "Male";

        int textureId = (getTextureID() % model.numTextures) + 1;
        texture = new ResourceLocation(Constants.MOD_ID, textureBase + textureId + ".png");
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
        updateArmSwingProgress();
        updateColony();
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
                MineColonies.logger.warn(String.format("Citizen '%s' attempting to register with colony, but not known to colony", getUniqueID()));
                setDead();
                return;
            }

            EntityCitizen existingCitizen = data.getCitizenEntity();
            if (existingCitizen != null && existingCitizen != this)
            {
                //  This Citizen already has a different Entity registered to it
                MineColonies.logger.warn(String.format("Citizen '%s' attempting to register with colony, but already have a citizen ('%s')", getUniqueID(), existingCitizen.getUniqueID()));
                setDead();
                return;
            }

            setColony(c, data);
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
                GuiHandler.showGuiWindow(new WindowCitizen(view));
            }
        }
        return true;
    }

    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        if (colony != null)
        {
            LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(worldObj, colony.getPermissions().getMessagePlayers()), "tile.blockHutTownhall.messageColonistDead", citizenData.getName());
            colony.removeCitizen(getCitizenData());
        }

        super.onDeath(par1DamageSource);
    }

    public ResourceLocation getTexture()
    {
        return texture;
    }

    public int getTextureID()
    {
        return dataWatcher.getWatchableObjectInt(DATA_TEXTURE);
    }

    public RenderBipedCitizen.Model getModelID()
    {
        return RenderBipedCitizen.Model.valueOf(dataWatcher.getWatchableObjectString(DATA_MODEL_ID));
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

        citizenData.setCitizenEntity(this);

        onJobChanged(getColonyJob());
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

    public enum DesiredActivity
    {
        SLEEP,
        IDLE,
        WORK
    }

    public DesiredActivity getDesiredActivity()
    {
        if (!worldObj.isDaytime())
        {
            return DesiredActivity.SLEEP;
        }
        else if (worldObj.isRaining())
        {
            return DesiredActivity.IDLE;
        }
        else
        {
            return DesiredActivity.WORK;
        }
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
