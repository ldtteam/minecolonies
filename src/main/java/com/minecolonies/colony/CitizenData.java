package com.minecolonies.colony;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

import java.util.Random;

/**
 * Extra data for Citizens
 *
 */
public class CitizenData
{
    //  Attributes
    private final int     id;
    private       String  name;
    private       boolean isFemale;
    private       int     textureId;

    private Colony         colony;
    private BuildingHome   homeBuilding;
    private BuildingWorker workBuilding;
    private Job            job;

    private boolean isDirty;

    //  Citizen
    public EntityCitizen entity;

    //  Placeholder skills
    private int level;
    public  int strength, stamina, wisdom, intelligence, charisma;

    private static final String TAG_ID      = "id";
    private static final String TAG_NAME    = "name";
    private static final String TAG_FEMALE  = "female";
    private static final String TAG_TEXTURE = "texture";
    private static final String TAG_LEVEL   = "level";

    private static final String TAG_ENTITY_ID     = "entity";
    private static final String TAG_HOME_BUILDING = "homeBuilding";
    private static final String TAG_WORK_BUILDING = "workBuilding";

    private static final String TAG_SKILLS         = "skills";
    private static final String TAG_SKILL_STRENGTH = "strength";
    private static final String TAG_SKILL_STAMINA  = "stamina";
    private static final String TAG_SKILL_WISDOM   = "wisdom";
    private static final String TAG_SKILL_INTELLIGENCE = "intelligence";
    private static final String TAG_SKILL_CHARISMA = "charisma";

    private static final String TAG_JOB            = "job";

    /**
     * Create a CitizenData given an ID
     * Used as a super-constructor or during loading
     *
     * @param id ID of the Citizen
     * @param colony Colony the Citizen belongs to
     */
    public CitizenData(int id, Colony colony)
    {
        this.id = id;
        this.colony = colony;
    }

    /**
     * Create a CitizenData given a CitizenEntity
     *
     * @param entity
     */
    public void initializeFromEntity(EntityCitizen entity)
    {
        Random rand = entity.getRNG();

        this.entity = entity;

        isFemale = rand.nextBoolean();   //  Gender before name
        name = generateName(rand);
        textureId = Math.abs(entity.worldObj.rand.nextInt());

        strength = rand.nextInt(10) + 1;
        stamina = rand.nextInt(10) + 1;
        wisdom = rand.nextInt(10) + 1;
        intelligence = rand.nextInt(10) + 1;
        charisma = rand.nextInt(10) + 1;

        markDirty();
    }

    public static CitizenData createFromNBT(NBTTagCompound compound, Colony colony)
    {
        int id = compound.getInteger(TAG_ID);
        CitizenData citizen = new CitizenData(id, colony);
        citizen.readFromNBT(compound);
        return citizen;
    }

    public int getId() { return id; }
    public Colony getColony() { return colony; }
    public String getName() { return name; }
    public boolean isFemale() { return isFemale; }
    public int getTextureId() { return textureId; }
    public int getLevel() { return level; }

    public boolean isDirty() { return isDirty; }
    public void markDirty()
    {
        isDirty = true;
        colony.markCitizensDirty();
    }
    public void clearDirty() { isDirty = false; }

    public BuildingHome getHomeBuilding() { return homeBuilding; }
    public void setHomeBuilding(BuildingHome building)
    {
        if (homeBuilding != null && building != null && homeBuilding != building)
        {
            throw new IllegalStateException("CitizenData.setHomeBuilding() - already assigned a home building when setting a new home building");
        }
        else if (homeBuilding != building)
        {
            homeBuilding = building;
            markDirty();
        }
    }

    public BuildingWorker getWorkBuilding() { return workBuilding; }

    public <BUILDING extends BuildingWorker> BUILDING getWorkBuilding(Class<BUILDING> type)
    {
        try
        {
            return type.cast(workBuilding);
        }
        catch (ClassCastException exc)
        {
        }

        return null;
    }

    public void setWorkBuilding(BuildingWorker building)
    {
        if (workBuilding != null && building != null && workBuilding != building)
        {
            throw new IllegalStateException("CitizenData.setWorkBuilding() - already assigned a work building when setting a new work building");
        }
        else if (workBuilding != building)
        {
            workBuilding = building;

            if (workBuilding != null)
            {
                //  We have a place to work, do we have the assigned Job?
                if (job == null)
                {
                    //  No job, create one!
                    setJob(workBuilding.createJob(this));
                    colony.getWorkManager().clearWorkForCitizen(this);
                }
            }
            else if (job != null)
            {
                //  No place of employment, get rid of our job
                setJob(null);
                colony.getWorkManager().clearWorkForCitizen(this);
            }

            markDirty();
        }
    }

    /**
     * When a building is destroyed, inform the citizen so it can do any cleanup of associations that the building's
     * own Building.onDestroyed did not do.
     *
     * @param building
     */
    public void onRemoveBuilding(Building building)
    {
        if (getHomeBuilding() == building)
        {
            setHomeBuilding(null);
        }

        if (getWorkBuilding() == building)
        {
            setWorkBuilding(null);
        }
    }

    public EntityCitizen getCitizenEntity() { return entity; /*(entity != null) ? entity.get() : null;*/ }
    public void setCitizenEntity(EntityCitizen citizen)
    {
        entity = citizen;
        markDirty();
    }
    public void clearCitizenEntity()
    {
        entity = null;
    }


    public Job getJob(){ return job; }
    public <JOB extends Job> JOB getJob(Class<JOB> type)
    {
        try
        {
            return type.cast(job);
        }
        catch (ClassCastException exc)
        {
        }

        return null;
    }

    public void setJob(Job j)
    {
        job = j;

        EntityCitizen entity = getCitizenEntity();
        if (entity != null)
        {
            entity.onJobChanged(job);
        }

        markDirty();
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setInteger(TAG_ID, id);
        compound.setString(TAG_NAME, name);
        compound.setBoolean(TAG_FEMALE, isFemale);
        compound.setInteger(TAG_TEXTURE, textureId);

        //  Attributes
        compound.setInteger(TAG_LEVEL, level);

        NBTTagCompound nbtTagSkillsCompound = new NBTTagCompound();
        nbtTagSkillsCompound.setInteger(TAG_SKILL_STRENGTH, strength);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_STAMINA, stamina);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_WISDOM, wisdom);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_INTELLIGENCE, intelligence);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_CHARISMA, charisma);
        compound.setTag(TAG_SKILLS, nbtTagSkillsCompound);

        if (job != null)
        {
            NBTTagCompound jobCompound = new NBTTagCompound();
            job.writeToNBT(jobCompound);
            compound.setTag("job", jobCompound);
        }
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        name = compound.getString(TAG_NAME);
        isFemale = compound.getBoolean(TAG_FEMALE);
        textureId = compound.getInteger(TAG_TEXTURE);

        //  Attributes
        level = compound.getInteger(TAG_LEVEL);

        NBTTagCompound nbtTagSkillsCompound = compound.getCompoundTag("skills");
        strength = nbtTagSkillsCompound.getInteger("strength");
        stamina = nbtTagSkillsCompound.getInteger("stamina");
        wisdom = nbtTagSkillsCompound.getInteger("wisdom");
        intelligence = nbtTagSkillsCompound.getInteger("intelligence");
        charisma = nbtTagSkillsCompound.getInteger("charisma");

        if (compound.hasKey("job"))
        {
            setJob(Job.createFromNBT(this, compound.getCompoundTag("job")));
        }
    }

    private String generateName(Random rand)
    {
        String firstName;
        if(!isFemale)
        {
            firstName = getRandomElement(rand, Configurations.maleFirstNames);
        }
        else
        {
            firstName = getRandomElement(rand, Configurations.femaleFirstNames);
        }
        return String.format("%s %s. %s", firstName, getRandomLetter(rand), getRandomElement(rand, Configurations.lastNames));
    }

    private String getRandomElement(Random rand, String[] array)
    {
        return array[rand.nextInt(array.length)];
    }

    private char getRandomLetter(Random rand)
    {
        return (char) (rand.nextInt(26) + 'A');
    }


    /**
     * The Building View is the client-side representation of a Building.
     * Views contain the Building's data that is relevant to a Client, in a more client-friendly form
     * Mutable operations on a View result in a message to the server to perform the operation
     */
    public static class View
    {
        private final int id;
        private int       entityId;
        private String    name;
        private boolean   isFemale;

        //  Placeholder skills
        private int level;
        public  int strength, stamina, wisdom, intelligence, charisma;

        private String job;

        private ChunkCoordinates homeBuilding;
        private ChunkCoordinates workBuilding;

        protected View(int id)
        {
            this.id = id;
        }

        public int getID(){ return id; }

        public int getEntityId(){ return entityId; }

        public String getName(){ return name; }

        public boolean isFemale(){ return isFemale; }

        public int getLevel(){ return level; }

        public String getJob(){ return job; }

        public ChunkCoordinates getHomeBuilding(){ return homeBuilding; }

        public ChunkCoordinates getWorkBuilding(){ return workBuilding; }

        public void deserialize(ByteBuf buf)
        {
            name = ByteBufUtils.readUTF8String(buf);
            isFemale = buf.readBoolean();
            entityId = buf.readInt();

            homeBuilding = buf.readBoolean() ? ChunkCoordUtils.readFromByteBuf(buf) : null;
            workBuilding = buf.readBoolean() ? ChunkCoordUtils.readFromByteBuf(buf) : null;

            //  Attributes
            level = buf.readInt();

            strength = buf.readInt();
            stamina = buf.readInt();
            wisdom = buf.readInt();
            intelligence = buf.readInt();
            charisma = buf.readInt();

            job = ByteBufUtils.readUTF8String(buf);
        }
    }

    public void serializeViewNetworkData(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeBoolean(isFemale);

        EntityCitizen entity = getCitizenEntity();
        buf.writeInt(entity != null ? entity.getEntityId() : -1);

        buf.writeBoolean(homeBuilding != null);
        if (homeBuilding != null)
        {
            ChunkCoordUtils.writeToByteBuf(buf, homeBuilding.getID());
        }

        buf.writeBoolean(workBuilding != null);
        if (workBuilding != null)
        {
            ChunkCoordUtils.writeToByteBuf(buf, workBuilding.getID());
        }

        //  Attributes
        buf.writeInt(level);

        buf.writeInt(strength);
        buf.writeInt(stamina);
        buf.writeInt(wisdom);
        buf.writeInt(intelligence);
        buf.writeInt(charisma);

        ByteBufUtils.writeUTF8String(buf, (job != null) ? job.getName() : "");
    }

    /**
     * Create a CitizenData View given it's saved NBTTagCompound
     *
     * @param id  The citizen's id
     * @param buf The network data
     * @return
     */
    public static View createCitizenDataView(int id, ByteBuf buf)
    {
        View view = new View(id);

        try
        {
            view.deserialize(buf);
        }
        catch (Exception ex)
        {
            MineColonies.logger.error(String.format("A CitizenData.View for #%d has thrown an exception during loading, its state cannot be restored. Report this to the mod author", view.getID()), ex);
            view = null;
        }

        return view;
    }
}
