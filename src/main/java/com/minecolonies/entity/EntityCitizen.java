package com.minecolonies.entity;

import com.minecolonies.MineColonies;
import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.ai.*;
import com.minecolonies.entity.pathfinding.PathNavigate;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.BlockParticleEffectMessage;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.world.World;
import scala.util.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Random;

import static net.minecraftforge.common.util.Constants.NBT;

public class EntityCitizen extends EntityAgeable implements IInvBasic, INpc
{
    private static final int DATA_TEXTURE         = 13;
    private static final int DATA_LEVEL           = 14;
    private static final int DATA_IS_FEMALE       = 15;
    private static final int DATA_COLONY_ID       = 16;
    private static final int DATA_CITIZEN_ID      = 17;  //  Because Entity UniqueIDs are not identical between client and server
    private static final int DATA_MODEL           = 18;
    private static final int DATA_RENDER_METADATA = 19;
    private static final int LEVEL_CAP = 10;
    private static Field navigatorField;
    protected Status status = Status.IDLE;
    private boolean isFemale;
    private RenderBipedCitizen.Model modelId = RenderBipedCitizen.Model.SETTLER;
    private String renderMetadata;
    private ResourceLocation texture;
    private InventoryCitizen inventory;
    private int              colonyId;
    private int citizenId = 0;
    private Colony      colony;
    private CitizenData citizenData;
    private int         level;
    private int         textureId;
    private Map<String, Integer> statusMessages = new HashMap<>();
    private PathNavigate newNavigator;
    //Connects the fishingHook with the citizen
    private EntityFishHook fishEntity;
    //Will be set true when the citizen caught a fish (to reset the fisherman)
    private boolean caughtFish=false;
    //Skills, may be added more later
    private int intelligence;
    private int speed;
    private int strength;
    private int stamina;
    private int diligence;

    //The current experience level the citizen is on.
    private int experienceLevel=1;
    /*The total amount of experience the citizen has.
    This also includes the amount of experience within their Experience Bar.*/
    private int experienceTotal;
    //The current amount of experience the citizen has within their Experience Bar.
    private double experience;
    //Something with ticks which I didn't understand yet!
    private int nOTicks;

    //Creates a citizen entity
    public EntityCitizen(World world)
    {
        super(world);
        setSize(0.6F, 1.8F);
        this.func_110163_bv();//Set persistenceRequired = true;
        this.setAlwaysRenderNameTag(Configurations.alwaysRenderNameTag);
        this.inventory = new InventoryCitizen("Minecolonies Inventory", false, 27);
        this.inventory.addIInvBasic(this);
        Random random = new Random();

        intelligence = random.nextInt(LEVEL_CAP-1) + 1;
        speed        = random.nextInt(LEVEL_CAP-1) + 1;
        strength     = random.nextInt(LEVEL_CAP-1) + 1;
        stamina      = random.nextInt(LEVEL_CAP-1) + 1;
        diligence    = random.nextInt(LEVEL_CAP-1) + 1;

        this.renderDistanceWeight = 2.0D;
        this.newNavigator = new PathNavigate(this, world);

        if (navigatorField == null) {
            Field[] fields = EntityLiving.class.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(net.minecraft.pathfinding.PathNavigate.class)) {
                    field.setAccessible(true);
                    navigatorField = field;
                    break;
                }
            }
        }

        try {
            navigatorField.set(this, this.newNavigator);
        } catch (IllegalAccessException e) {
            e.printStackTrace(); //TODO: use a logger
        }

        this.getNavigator().setAvoidsWater(true);
        this.getNavigator().setCanSwim(true);
        this.getNavigator().setEnterDoors(true);
        this.getNavigator().setBreakDoors(true);

        initTasks();
    }

    public boolean isWorkerAtSiteWithMove(ChunkCoordinates site, int range)
    {
        return Utils.isWorkerAtSiteWithMove(this, site.posX, site.posY, site.posZ, range)
                //Fix for getting stuck sometimes
                || Utils.isWorkerAtSite(this, site.posX, site.posY, site.posZ, range + 1);
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(DATA_COLONY_ID, colonyId);
        dataWatcher.addObject(DATA_CITIZEN_ID, citizenId);
        dataWatcher.addObject(DATA_TEXTURE, 0);
        dataWatcher.addObject(DATA_LEVEL, 0);
        dataWatcher.addObject(DATA_IS_FEMALE, 0);
        dataWatcher.addObject(DATA_MODEL, RenderBipedCitizen.Model.SETTLER.name());
        dataWatcher.addObject(DATA_RENDER_METADATA, "");
    }

    private void initTasks()
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAICitizenAvoidEntity(this, EntityMob.class, 8.0F, 0.6D, 1.6D));
        this.tasks.addTask(2, new EntityAIGoHome(this));
        this.tasks.addTask(3, new EntityAISleep(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(6, new EntityAIWatchClosest2(this, EntityCitizen.class, 5.0F, 0.02F));
        this.tasks.addTask(7, new EntityAICitizenWander(this, 0.6D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityLiving.class, 6.0F));

        onJobChanged(getColonyJob());
    }

    private Job getColonyJob()
    {
        return citizenData != null ? citizenData.getJob() : null;
    }

    public <JOB extends Job> JOB getColonyJob(Class<JOB> type)
    {
        return citizenData != null ? citizenData.getJob(type) : null;
    }

    /**
     * Arguments: current rotation, intended rotation, max increment.
     */
    private double updateRotation(double currentRotation, double intentedRotation, double maxIncrement)
    {
        double wrappedAngle = MathHelper.wrapAngleTo180_double(intentedRotation - currentRotation);

        if (wrappedAngle > maxIncrement)
        {
            wrappedAngle = maxIncrement;
        }

        if (wrappedAngle < -maxIncrement)
        {
            wrappedAngle = -maxIncrement;
        }

        return currentRotation + wrappedAngle;
    }

    public void faceBlock(ChunkCoordinates block)
    {

        double xDifference = block.posX - this.posX;
        double zDifference = block.posZ - this.posZ;
        double yDifference = block.posY - (this.posY + (double)this.getEyeHeight()-0.5);

        double squareDifference = Math.sqrt(xDifference * xDifference + zDifference * zDifference);
        double intendedRotationYaw = (Math.atan2(zDifference, xDifference) * 180.0D / Math.PI) - 90.0;
        double intendedRotationPitch = (-(Math.atan2(yDifference, squareDifference) * 180.0D / Math.PI));
        this.rotationPitch = (float)this.updateRotation(this.rotationPitch, intendedRotationPitch, 30);
        this.rotationYaw = (float)this.updateRotation(this.rotationYaw, intendedRotationYaw, 30);
    }

    public void onJobChanged(Job job)
    {
        //  Model
        if(job != null)
        {
            modelId = job.getModel();
        }
        else
        {
            switch(getLevel())
            {
                default:
                    modelId = RenderBipedCitizen.Model.SETTLER;
                    break;
                case 1:
                    modelId = RenderBipedCitizen.Model.CITIZEN;
                    break;
                case 2:
                    modelId = RenderBipedCitizen.Model.NOBLE;
                    break;
                case 3:
                    modelId = RenderBipedCitizen.Model.ARISTOCRAT;
                    break;
            }
        }

        dataWatcher.updateObject(DATA_MODEL, modelId.name());
        setRenderMetadata("");


        //  AI Tasks
        Object currentTasks[] = this.tasks.taskEntries.toArray();
        for(Object task : currentTasks)
        {
            if(((EntityAITasks.EntityAITaskEntry) task).action instanceof AbstractEntityAIWork)
            {
                this.tasks.removeTask(((EntityAITasks.EntityAITaskEntry) task).action);
            }
        }

        if(job != null)
        {
            job.addTasks(this.tasks);
            if(ticksExisted > 0)
            {
                ChunkCoordUtils.tryMoveLivingToXYZ(this, getWorkBuilding().getLocation());
            }
        }
    }

    public void gatherXp()
    {
        for (EntityXPOrb orb : getXPOrbsOnGrid())
        {
            addExperience(orb.getXpValue());
            orb.setDead();
        }
    }

    private void setTexture()
    {
        if(!worldObj.isRemote)
        {
            return;
        }

        RenderBipedCitizen.Model model = getModelID();

        String textureBase = "textures/entity/";
        textureBase += model.textureBase;
        textureBase += isFemale ? "Female" : "Male";

        int moddedTextureId = (textureId % model.numTextures) + 1;
        texture = new ResourceLocation(Constants.MOD_ID, textureBase + moddedTextureId + renderMetadata + ".png");
    }

    public void setRenderMetadata(String metadata)
    {
        renderMetadata = metadata;
        dataWatcher.updateObject(DATA_RENDER_METADATA, renderMetadata);
        //Display some debug info always available while testing
        //TODO: remove this when in Beta!
        //Will help track down some hard to find bugs (Pathfinding etc.)
        if(citizenData != null)
        {
            if (this.getColonyJob() != null && Configurations.enableInDevelopmentFeatures)
            {
                setCustomNameTag(citizenData.getName() + " (" + getStatus() + ")[" + this.getColonyJob()
                                                                                        .getNameTagDescription()+"]");
            }
            else
            {
                setCustomNameTag(citizenData.getName());
            }
        }
    }

    @Override
    public boolean isAIEnabled()
    {
        return true;
    }

    @Override
    public void onLivingUpdate()
    {
        if(worldObj.isRemote){ updateColonyClient();}
        else
        {
            pickupItems();
            cleanupChatMessages();
            updateColonyServer();
        }

        super.onLivingUpdate();
    }

    /**
     * Entities treat being on ladders as not on ground; this breaks navigation logic
     */
    @Override
    protected void updateFallState(double y, boolean onGround)
    {
        if(!onGround)
        {
            int px = MathHelper.floor_double(posX);
            int py = (int) posY;
            int pz = MathHelper.floor_double(posZ);

            this.onGround = worldObj.getBlock(px, py, pz).isLadder(worldObj, px, py, pz, this);
        }

        super.updateFallState(y, this.onGround);
    }

    private void updateColonyClient()
    {
        if(dataWatcher.hasChanges())
        {
            if(colonyId == 0)
            {
                colonyId = dataWatcher.getWatchableObjectInt(DATA_COLONY_ID);
            }

            if(citizenId == 0)
            {
                citizenId = dataWatcher.getWatchableObjectInt(DATA_CITIZEN_ID);
            }

            isFemale = dataWatcher.getWatchableObjectInt(DATA_IS_FEMALE) != 0;
            level = dataWatcher.getWatchableObjectInt(DATA_LEVEL);
            modelId = RenderBipedCitizen.Model.valueOf(dataWatcher.getWatchableObjectString(DATA_MODEL));
            textureId = dataWatcher.getWatchableObjectInt(DATA_TEXTURE);
            renderMetadata = dataWatcher.getWatchableObjectString(DATA_RENDER_METADATA);

            setTexture();

            dataWatcher.func_111144_e(); // clear hasChanges
        }

        updateArmSwingProgress();
    }

    /**
     * Server-specific update for the EntityCitizen
     */
    public void updateColonyServer()
    {
        if(colonyId == 0)
        {
            setDead();
            return;
        }

        if(colony == null)
        {
            Colony c = ColonyManager.getColony(colonyId);

            if(c == null)
            {
                MineColonies.logger.warn(String.format("EntityCitizen '%s' unable to find Colony #%d", getUniqueID(), colonyId));
                setDead();
                return;
            }

            CitizenData data = c.getCitizen(citizenId);
            if(data == null)
            {
                //  Citizen does not exist in the Colony
                MineColonies.logger.warn(String.format("EntityCitizen '%s' attempting to register with Colony #%d as Citizen %d, but not known to colony", getUniqueID(), colonyId, citizenId));
                setDead();
                return;
            }

            EntityCitizen existingCitizen = data.getCitizenEntity();
            if(existingCitizen != null && existingCitizen != this)
            {
                // This Citizen already has a different Entity registered to it
                MineColonies.logger.warn(String.format("EntityCitizen '%s' attempting to register with Colony #%d as Citizen #%d, but already have a citizen ('%s')", getUniqueID(), colonyId, citizenId, existingCitizen.getUniqueID()));
                if(!existingCitizen.getUniqueID().equals(this.getUniqueID()))
                {
                    setDead();
                }
                else
                {
                    data.setCitizenEntity(this);
                }
                return;
            }

            setColony(c, data);
        }
    }

    private void cleanupChatMessages()
    {
        if(statusMessages.size() > 0 && ticksExisted % 20 == 0)//Only check if there are messages and once a second
        {
            Iterator<Map.Entry<String, Integer>> it = statusMessages.entrySet().iterator();
            while(it.hasNext())
            {
                if(ticksExisted - it.next().getValue() > 20 * Configurations.chatFrequency)
                {
                    it.remove();
                }
            }
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
        if(worldObj.isRemote)
        {
            CitizenData.View view = getCitizenDataView();
            if(view != null)
            {
                MineColonies.proxy.showCitizenWindow(view);
            }
        }
        return true;
    }

    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        int i;

        if (!this.worldObj.isRemote && (this.recentlyHit > 0 || this.isPlayer()) && this.func_146066_aG() && this.worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))
        {
            i = this.getExperiencePoints(this.attackingPlayer);

            while (i > 0)
            {
                int j = EntityXPOrb.getXPSplit(i);
                i -= j;
                this.worldObj.spawnEntityInWorld(new EntityXPOrb(this.worldObj, this.posX, this.posY, this.posZ, j));
            }
        }

        this.setDead();

        for (i = 0; i < 20; ++i)
        {
            double d2 = this.rand.nextGaussian() * 0.02D;
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            this.worldObj.spawnParticle("explode", this.posX + (this.rand.nextDouble() * this.width * 2.0F) - (double)this.width, this.posY + (this.rand.nextDouble() * this.height), this.posZ + (this.rand.nextDouble() * this.width * 2.0F) - (double)this.width, d2, d0, d1);
        }

        if(colony != null)
        {
            LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(worldObj, colony.getPermissions().getMessagePlayers()), "tile.blockHutTownHall.messageColonistDead", citizenData.getName());
            colony.removeCitizen(getCitizenData());
        }
        super.onDeath(par1DamageSource);
    }

    @Override
    public PathNavigate getNavigator()
    {
        return newNavigator;
    }

    public ResourceLocation getTexture()
    {
        return texture;
    }

    public RenderBipedCitizen.Model getModelID()
    {
        return modelId;
    }

    public int getLevel()
    {
        return level;
    }

    private void updateLevel()
    {
        level = citizenData != null ? citizenData.getLevel() : 0;
        dataWatcher.updateObject(DATA_LEVEL, level);
    }

    public boolean isFemale()
    {
        return isFemale;
    }

    public CitizenData getCitizenData()
    {
        return citizenData;
    }

    private CitizenData.View getCitizenDataView()
    {
        if(colonyId != 0 && citizenId != 0)
        {
            ColonyView colonyView = ColonyManager.getColonyView(colonyId);
            if(colonyView != null)
            {
                return colonyView.getCitizen(citizenId);
            }
        }

        return null;
    }

    public Colony getColony()
    {
        return colony;
    }

    public void clearColony()
    {
        setColony(null, null);
    }

    public void setColony(Colony c, CitizenData data)
    {
        if(c == null)
        {
            colony = null;
            colonyId = 0;
            citizenId = 0;
            citizenData = null;
            setDead();
            return;
        }

        colony = c;
        colonyId = colony.getID();
        citizenId = data.getId();
        citizenData = data;

        setCustomNameTag(citizenData.getName());

        isFemale = citizenData.isFemale();
        textureId = citizenData.getTextureId();

        dataWatcher.updateObject(DATA_COLONY_ID, colonyId);
        dataWatcher.updateObject(DATA_CITIZEN_ID, citizenId);
        dataWatcher.updateObject(DATA_IS_FEMALE, isFemale ? 1 : 0);
        dataWatcher.updateObject(DATA_TEXTURE, textureId);
        updateLevel();

        citizenData.setCitizenEntity(this);

        onJobChanged(getColonyJob());
    }

    private BuildingHome getHomeBuilding()
    {
        return (citizenData != null) ? citizenData.getHomeBuilding() : null;
    }

    public ChunkCoordinates getHomePosition()
    {
        BuildingHome homeBuilding = getHomeBuilding();
        if(homeBuilding != null)
        {
            return homeBuilding.getLocation();
        }
        else if(getColony() != null && getColony().getTownhall() != null)
        {
            return getColony().getTownhall().getLocation();
        }

        return null;
    }

    public boolean isAtHome()
    {
        ChunkCoordinates homePosition = getHomePosition();
        return homePosition != null && homePosition.getDistanceSquared((int)Math.floor(posX), (int) posY, (int)Math.floor(posZ)) <= 16;
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
        compound.setDouble("xp",experience);
        compound.setInteger("xpLevel",experienceLevel);
        compound.setInteger("xpTotal",experienceTotal);
        if(colony != null && citizenData != null)
        {
            compound.setInteger("colony", colony.getID());
            compound.setInteger("citizen", citizenData.getId());
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
        compound.setInteger("HeldItemSlot", inventory.getHeldItemSlot());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);

        status = Status.values()[compound.getInteger("status")];
        experience = compound.getDouble("xp");
        experienceLevel = compound.getInteger("xpLevel");
        experienceTotal = compound.getInteger("xpTotal");
        colonyId = compound.getInteger("colony");
        citizenId = compound.getInteger("citizen");

        NBTTagList nbttaglist = compound.getTagList("Inventory", NBT.TAG_COMPOUND);
        for(int i = 0; i < nbttaglist.tagCount(); i++)
        {
            NBTTagCompound tag = nbttaglist.getCompoundTagAt(i);
            int slot = tag.getInteger("slot");
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(tag);
            inventory.setInventorySlotContents(slot, itemstack);
        }
        inventory.setHeldItem(compound.getInteger("HeldItemSlot"));
    }

    public int getOffsetTicks()
    {
        return this.ticksExisted + 7 * this.getEntityId();
    }

    public boolean isInventoryFull()
    {
        return InventoryUtils.getOpenSlot(getInventory()) == -1;
    }

    public DesiredActivity getDesiredActivity()
    {
        if(!worldObj.isDaytime())
        {
            return DesiredActivity.SLEEP;
        }
        else if(worldObj.isRaining())
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
        return entityDropItem(itemstack, 0.0F);
    }

    @Override
    protected void dropEquipment(boolean par1, int par2)
    {
        for(int i = 0; i < getLastActiveItems().length; i++){ setCurrentItemOrArmor(i, null);}
        for(int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);
            if(itemstack != null && itemstack.stackSize > 0)
            {
                entityDropItem(itemstack);
            }
        }
    }

    @Override
    public void onInventoryChanged(InventoryBasic inventoryBasic)
    {
        //TODO use in future for lumberjack rendering logs, etc
        setCurrentItemOrArmor(0, inventory.getHeldItem());
    }

    //Add experience points to citizen.
    private void addExperience(int xp)
    {
        int j = Integer.MAX_VALUE - this.experienceTotal;
        int localXp = xp;
        if (localXp > j)
        {
            localXp = j;
        }

        this.experience += localXp / this.xpBarCap();

        for (this.experienceTotal += localXp; this.experience >= 1.0F; this.experience /= this.xpBarCap())
        {
            this.experience = (this.experience - 1.0) * this.xpBarCap();
            this.addExperienceLevel(1);
        }
    }

    //Add experience levels to this citizen.
    private void addExperienceLevel(int nOLevels)
    {
        this.experienceLevel += nOLevels;

        if (this.experienceLevel < 0)
        {
            this.experienceLevel = 0;
            this.experience = 0.0;
            this.experienceTotal = 0;
        }

        if (nOLevels > 0 && this.experienceLevel % 5 == 0 && this.nOTicks < this.ticksExisted - 100.0)
        {
            double f = this.experienceLevel > 30 ? 1.0 : this.experienceLevel / 30.0;
            this.worldObj.playSoundAtEntity(this, "random.levelup", (float)f * 0.75F, 1.0F);
            this.nOTicks = this.ticksExisted;
        }
    }

    /**
     * This method returns the cap amount of experience that the experience bar can hold. With each level, the
     * experience cap on the citizen's experience bar is raised by 10.
     */
    private int xpBarCap()
    {
        return (this.experienceLevel >= 30 ? 62 + (this.experienceLevel - 30) * 7 : (this.experienceLevel >= 15 ? 17 + (this.experienceLevel - 15) * 3 : 17));
    }

    /**
     * Get the experience points the entity currently has.
     */
    protected int getExperiencePoints()
    {
        if (this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory"))
        {
            return 0;
        }
        else
        {
            int i = this.experienceLevel * 7;
            return i > 100 ? 100 : i;
        }
    }

    public InventoryCitizen getInventory()
    {
        return inventory;
    }

    public int findFirstSlotInInventoryWith(Item targetItem)
    {
        return InventoryUtils.findFirstSlotInInventoryWith(getInventory(), targetItem);
    }

    public int findFirstSlotInInventoryWith(Block block)
    {
        return InventoryUtils.findFirstSlotInInventoryWith(getInventory(), block);
    }

    public int getItemCountInInventory(Block block)
    {
        return InventoryUtils.getItemCountInInventory(getInventory(), block);
    }

    public int getItemCountInInventory(Item targetitem)
    {
        return InventoryUtils.getItemCountInInventory(getInventory(), targetitem);
    }

    public boolean hasItemInInventory(Block block)
    {
        return InventoryUtils.hasitemInInventory(getInventory(), block);
    }

    public boolean hasItemInInventory(Item item)
    {
        return InventoryUtils.hasitemInInventory(getInventory(), item);
    }


    //Defines the area in which the citizen automatically gathers experience
    @SuppressWarnings("unchecked")
    private List<EntityXPOrb> getXPOrbsOnGrid() {
        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(posX-2, posY-2, posZ-2, posX + 2, posY + 2, posZ + 2);
        return worldObj.getEntitiesWithinAABB(EntityXPOrb.class, bb);
    }

    public void setInventorySize(int newSize, boolean dropLeftovers)
    {
        if(!worldObj.isRemote)
        {
            InventoryCitizen newInventory = new InventoryCitizen(inventory.getInventoryName(), inventory.hasCustomInventoryName(), newSize);
            ArrayList<ItemStack> leftOvers = new ArrayList<>();
            for(int i = 0; i < inventory.getSizeInventory(); i++)
            {
                ItemStack itemstack = inventory.getStackInSlot(i);
                if(i < newInventory.getSizeInventory())
                {
                    newInventory.setInventorySlotContents(i, itemstack);
                }
                else
                {
                    if(itemstack != null) leftOvers.add(itemstack);
                }
            }
            inventory = newInventory;
            inventory.addIInvBasic(this);
            if(dropLeftovers)
            {
                leftOvers.stream().filter(leftover -> leftover.stackSize > 0).forEach(this::entityDropItem);
            }
        }
    }

    private void pickupItems()
    {
        @SuppressWarnings("unchecked") List<EntityItem> list = worldObj.getEntitiesWithinAABB(EntityItem.class, boundingBox.expand(2.0, 0.0, 2.0));//TODO change range

        list.stream().filter(item -> item != null && !item.isDead && canPickUpLoot()).forEach(this::tryPickupEntityItem);
    }

    private void tryPickupEntityItem(EntityItem entityItem)
    {
        if(!this.worldObj.isRemote)
        {
            if(entityItem.delayBeforeCanPickup > 0)
            {
                return;
            }

            ItemStack itemStack = entityItem.getEntityItem();
            int i = itemStack.stackSize;

            if(i <= 0 || InventoryUtils.addItemStackToInventory(this.getInventory(), itemStack))
            {
                this.worldObj.playSoundAtEntity(this, "random.pop", 0.2f,
                                                (float) (((this.rand.nextDouble() - this.rand.nextDouble()) * 0.7D + 1.0D) * 2.0D));
                this.onItemPickup(this, i);

                if(itemStack.stackSize <= 0)
                {
                    entityItem.setDead();
                }
            }
        }
    }

    public void setHeldItem(int slot)
    {
        inventory.setHeldItem(slot);
        setCurrentItemOrArmor(0, inventory.getStackInSlot(slot));
    }

    /**
     * Swing entity arm, create sound and particle effects. if breakBlock is true then it will break the block (different sound and particles),
     * and damage the tool in the citizens hand.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    private void hitBlockWithToolInHand(int x, int y, int z, boolean breakBlock)
    {
        this.swingItem();

        Block block = worldObj.getBlock(x, y, z);
        if(breakBlock)
        {
            if(!worldObj.isRemote)
            {
                MineColonies.getNetwork().sendToAllAround(
                        new BlockParticleEffectMessage(x, y, z, block, worldObj.getBlockMetadata(x, y, z), BlockParticleEffectMessage.BREAK_BLOCK),
                        new NetworkRegistry.TargetPoint(worldObj.provider.dimensionId, x, y, z, 16.0D));
            }
            worldObj.playSoundEffect((float)(x + 0.5D), (float)(y + 0.5D), (float)(z + 0.5D), block.stepSound.getBreakSound(), block.stepSound.getVolume(), block.stepSound.getPitch());
            worldObj.setBlockToAir(x, y, z);

            damageItemInHand(1);
        }
        else
        {
            if(!worldObj.isRemote)//TODO might remove this
            {
                MineColonies.getNetwork().sendToAllAround(
                        new BlockParticleEffectMessage(x, y, z, block, worldObj.getBlockMetadata(x, y, z), 1),//TODO correct side
                        new NetworkRegistry.TargetPoint(worldObj.provider.dimensionId, x, y, z, 16.0D));
            }
            worldObj.playSoundEffect((float)(x + 0.5D), (float)(y + 0.5D), (float)(z + 0.5D), block.stepSound.getStepResourcePath(),

                                     (float) ((block.stepSound.getVolume() + 1.0D) / 8.0D),
                                     (float) (block.stepSound.getPitch() * 0.5D));
        }
    }

    public void hitBlockWithToolInHand(ChunkCoordinates block)
    {
        if(block == null) return;
        hitBlockWithToolInHand(block.posX, block.posY, block.posZ, false);
    }

    public void hitBlockWithToolInHand(int x, int y, int z)
    {
        hitBlockWithToolInHand(x, y, z, false);
    }

    public void breakBlockWithToolInHand(ChunkCoordinates block)
    {
        if(block == null) return;
        hitBlockWithToolInHand(block.posX, block.posY, block.posZ, true);
    }

    public void damageItemInHand(int damage)
    {
        getInventory().getHeldItem().damageItem(damage, this);

        if(this.getInventory().getHeldItem().stackSize < 1)//if tool breaks
        {
            this.setCurrentItemOrArmor(0, null);
            getInventory().setInventorySlotContents(getInventory().getHeldItemSlot(), null);
        }
    }

    public void sendChat(String msg)
    {
        if(msg == null || msg.length() == 0 || statusMessages.containsKey(msg))
        {
            return;
        }

        statusMessages.put(msg, ticksExisted);

        LanguageHandler.sendPlayersMessage(Utils.getPlayersFromUUID(worldObj, getColony().getPermissions().getMessagePlayers()), LanguageHandler.format(this.getColonyJob().getName()) + " " + this.getCustomNameTag() + ": " + msg);
    }

    public void sendLocalizedChat(String key, Object... args)
    {
        sendChat(LanguageHandler.format(key, args));
    }

    public enum DesiredActivity
    {
        SLEEP,
        IDLE,
        WORK
    }

    /**
     * Used for chat messages, sounds, and other need based interactions
     * Created: June 20, 2014
     *
     * @author Colton
     */
    public enum Status
    {
        IDLE, SLEEPING, WORKING, GETTING_ITEMS, NEED_ASSISTANCE, PATHFINDING_ERROR
    }

    public int getIntelligence() {
        return intelligence;
    }

    public int getSpeed() {
        return speed;
    }

    public int getStrength() {
        return strength;
    }

    public int getStamina() {
        return stamina;
    }

    public int getDiligence() {
        return diligence;
    }

    public EntityFishHook getFishEntity() {
        return fishEntity;
    }

    public void setFishEntity(EntityFishHook fishEntity) {
        this.fishEntity = fishEntity;
    }

    public boolean isCaughtFish() {
        return caughtFish;
    }

    public void setCaughtFish(boolean caughtFish) {
        this.caughtFish = caughtFish;
    }

    public int getExperienceLevel() {return experienceLevel; }
}
