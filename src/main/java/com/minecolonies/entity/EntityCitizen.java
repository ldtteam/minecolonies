package com.minecolonies.entity;

import com.minecolonies.client.gui.GuiEntityCitizen;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.ai.EntityAIGoHome;
import com.minecolonies.entity.ai.EntityAISleep;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.GuiHandler;
import com.minecolonies.tileentities.TileEntityHutCitizen;
import com.minecolonies.tileentities.TileEntityHutWorker;
import com.minecolonies.tileentities.TileEntityTownHall;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import com.minecolonies.util.Vec3Utils;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.ArrayList;

import static net.minecraftforge.common.util.Constants.NBT;

public class EntityCitizen extends EntityAgeable implements IInvBasic, INpc
{
    public static final int SEX_MALE   = 0;
    public static final int SEX_FEMALE = 1;

    public int strength, stamina, wisdom, intelligence, charisma;
    public  ResourceLocation texture;
    private String           job;
    private InventoryCitizen inventory;

    private TileEntityTownHall   tileEntityTownHall;
    private Vec3                 townPos;
    private TileEntityHutWorker  tileEntityWorkHut;
    private Vec3                 workPos;
    private TileEntityHutCitizen tileEntityHomeHut;
    private Vec3                 homePos;

    protected Status status = Status.IDLE;

    public EntityCitizen(World world)
    {
        super(world);
        setSize(0.6F, 1.8F);
        this.func_110163_bv();//Set persistenceRequired = true;
        this.job = initJob();
        setTexture();
        this.setCustomNameTag(generateName());
        this.setAlwaysRenderNameTag(true);//TODO: configurable
        this.inventory = new InventoryCitizen("Minecolonies Inventory", false, 27, this);
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
        this.tasks.addTask(2, new EntityAISleep(this));
        this.tasks.addTask(3, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(4, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(5, new EntityAIWatchClosest2(this, EntityCitizen.class, 5.0F, 0.02F));
        this.tasks.addTask(6, new EntityAIWander(this, 0.6D));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityLiving.class, 6.0F));
    }

    protected String initJob()
    {
        return "Citizen";
    }

    public void setTexture()
    {
        String textureBase = "textures/entity/";
        if(this.getJob().equals("Citizen"))
        {
            switch(getLevel())
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
            textureBase += this.getJob();
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
        updateTileEntities();
        super.onLivingUpdate();
    }

    private void updateTileEntities()
    {
        if(tileEntityTownHall == null && townPos != null)
        {
            tileEntityTownHall = (TileEntityTownHall) Vec3Utils.getTileEntityFromVec(worldObj, townPos);
        }
        if(tileEntityWorkHut == null && workPos != null)
        {
            tileEntityWorkHut = (TileEntityHutWorker) Vec3Utils.getTileEntityFromVec(worldObj, workPos);
        }
        if(tileEntityHomeHut == null && homePos != null)
        {
            tileEntityHomeHut = (TileEntityHutCitizen) Vec3Utils.getTileEntityFromVec(worldObj, homePos);
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
        GuiHandler.showGuiScreen(new GuiEntityCitizen(this, player, worldObj));
        return true;
    }

    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        if(this.getTownHall() != null && this.getTownHall().getCitizens().contains(this.getUniqueID()))
        {
            LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(worldObj, tileEntityTownHall.getOwners()), "tile.blockHutTownhall.messageColonistDead");

            tileEntityTownHall.removeCitizen(this);
        }
        if(this.getHomeHut() != null)
        {
            this.getHomeHut().removeCitizen(this);
        }
        if(this.getWorkHut() != null)
        {
            this.getWorkHut().unbindWorker(this);
        }

        setCurrentItemOrArmor(0, null);
        for(int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);
            if(itemstack != null && itemstack.stackSize > 0)
            {
                entityDropItem(itemstack, getEyeHeight() - 0.3F);
            }
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
        this.job = job;
        this.tileEntityWorkHut = (TileEntityHutWorker) tileEntity;
    }

    public TileEntityTownHall getTownHall()
    {
        return tileEntityTownHall;
    }

    public TileEntityHutCitizen getHomeHut()
    {
        return tileEntityHomeHut;
    }

    public TileEntityHutWorker getWorkHut()
    {
        return tileEntityWorkHut;
    }

    public void setTownHall(TileEntityTownHall tileEntityTownHall)
    {
        this.tileEntityTownHall = tileEntityTownHall;
    }

    public void setHomeHut(TileEntityHutCitizen home)
    {
        this.tileEntityHomeHut = home;
    }

    public void setWorkHut(TileEntityHutWorker work)
    {
        this.tileEntityWorkHut = work;
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
        compound.setString("job", job);
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

        if(tileEntityTownHall != null)
        {
            Vec3Utils.writeVecToNBT(compound, "townhall", tileEntityTownHall.getPosition());
        }
        if(tileEntityWorkHut != null)
        {
            Vec3Utils.writeVecToNBT(compound, "workhut", tileEntityWorkHut.getPosition());
        }
        if(tileEntityHomeHut != null)
        {
            Vec3Utils.writeVecToNBT(compound, "homehut", tileEntityHomeHut.getPosition());
        }
        NBTTagList inventoryList = new NBTTagList();
        for(int i = 0; i < inventory.getSizeInventory(); i++)
        {
            if(inventory.getStackInSlot(i) != null)
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

        this.job = compound.getString("job");
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

        if(compound.hasKey("townhall"))
        {
            townPos = Vec3Utils.readVecFromNBT(compound, "townhall");
        }
        if(compound.hasKey("workhut"))
        {
            workPos = Vec3Utils.readVecFromNBT(compound, "workhut");
        }
        if(compound.hasKey("homehut"))
        {
            homePos = Vec3Utils.readVecFromNBT(compound, "homehut");
        }
        NBTTagList nbttaglist = compound.getTagList("Inventory", NBT.TAG_COMPOUND);
        for(int i = 0; i < nbttaglist.tagCount(); i++)
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

    @Override
    public void onInventoryChanged(InventoryBasic inventoryBasic){}

    public InventoryCitizen getInventory()
    {
        return inventory;
    }

    public void setInventorySize(int newSize, boolean dropLeftovers)
    {
        if(!worldObj.isRemote)
        {
            InventoryCitizen newInventory = new InventoryCitizen(inventory.getInventoryName(), inventory.hasCustomInventoryName(), newSize, this);
            ArrayList<ItemStack> leftovers = new ArrayList<ItemStack>();
            for(int i = 0; i < inventory.getSizeInventory(); i++)
            {
                ItemStack itemstack = inventory.getStackInSlot(i);
                if(i < newInventory.getSizeInventory())
                {
                    newInventory.setInventorySlotContents(i, itemstack);
                }
                else
                {
                    if(itemstack != null) leftovers.add(itemstack);
                }
            }
            inventory = newInventory;
            inventory.addIInvBasic(this);
            if(dropLeftovers)
            {
                for(ItemStack leftover : leftovers)
                {
                    if(leftover.stackSize > 0)
                    {
                        entityDropItem(leftover, getEyeHeight() - 0.3F);
                    }
                }
            }
        }
    }

    public void addToWorkHut(TileEntityHutWorker tileEntityHutWorker)
    {
        setJob(tileEntityHutWorker.getJobName(), tileEntityHutWorker);

        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        getTownHall().removeCitizen(this);
        this.setDead();

        EntityCitizen worker = tileEntityHutWorker.createWorker();
        worker.readFromNBT(nbt);
        getTownHall().addCitizenToTownhall(worker);
        tileEntityHutWorker.bindWorker(worker);
        worldObj.spawnEntityInWorld(worker);
        Vec3Utils.tryMoveLivingToXYZ(worker, tileEntityHutWorker.getPosition());
    }

    public void removeFromWorkHut()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        nbt.removeTag("workhut");
        getTownHall().removeCitizen(this);
        getWorkHut().unbindWorker(this);
        this.setDead();

        EntityCitizen citizen = new EntityCitizen(worldObj);
        citizen.readFromNBT(nbt);
        citizen.setJob("Citizen", null);
        getTownHall().addCitizenToTownhall(citizen);
        worldObj.spawnEntityInWorld(citizen);
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
