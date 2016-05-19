package com.minecolonies.colony;

import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.Log;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.Random;

/**
 * Extra data for Citizens
 */
public class CitizenData
{
    //  Attributes
    private final           int             id;

    /**
     * Max level of an attribute a citizen may initially have.
     */
    private static final    int             LEVEL_CAP               = 5;

    private                 String          name;
    private                 boolean         isFemale;
    private                 int             textureId;

    private                 Colony          colony;
    private                 BuildingHome    homeBuilding;
    private                 BuildingWorker  workBuilding;
    private                 Job             job;

    private                 boolean         isDirty;

    //  Citizen
    private                 EntityCitizen   entity;

    /**
     *  Attributes, which influence the workers behaviour.
     *  May be added more later
    */
    private                 int             strength;
    private                 int             endurance;
    private                 int             charisma;
    private                 int             intelligence;
    private                 int             dexterity;

    private                 float           health;
    private                 float           maxHealth;

    /**
     * Tags
     */
    private static final    String          TAG_ID                  = "id";
    private static final    String          TAG_NAME                = "name";
    private static final    String          TAG_FEMALE              = "female";
    private static final    String          TAG_TEXTURE             = "texture";
    private static final    String          TAG_LEVEL               = "level";
    private static final    String          TAG_EXPERIENCE          = "experience";
    private static final    String          TAG_HEALTH              = "health";
    private static final    String          TAG_MAX_HEALTH          = "maxHealth";

    private static final    String          TAG_ENTITY_ID           = "entity";
    private static final    String          TAG_HOME_BUILDING       = "homeBuilding";
    private static final    String          TAG_WORK_BUILDING       = "workBuilding";

    private static final    String          TAG_SKILLS              = "skills";
    private static final    String          TAG_SKILL_STRENGTH      = "strength";
    private static final    String          TAG_SKILL_STAMINA       = "endurance";
    private static final    String          TAG_SKILL_SPEED         = "charisma";
    private static final    String          TAG_SKILL_INTELLIGENCE  = "intelligence";
    private static final    String          TAG_SKILL_DEXTERITY     = "dexterity";

    private static final    String          TAG_JOB                 = "job";
    /**
     * The number to calculate the experienceLevel of the citizen
     */
    private static final int EXPERIENCE_MULTIPLIER = 100;
    /**
    * The current experience level the citizen is on.
    */
    private                 int     level                           = 0;

    /**
    * The total amount of experience the citizen has.
    * This also includes the amount of experience within their Experience Bar.
    */
    private                 double     experience;

    /**
     * Create a CitizenData given an ID
     * Used as a super-constructor or during loading
     *
     * @param id     ID of the Citizen
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
     * @param entity   Entity to initialize from
     */
    public void initializeFromEntity(EntityCitizen entity)
    {
        Random rand = entity.getRNG();

        this.entity = entity;

        isFemale = rand.nextBoolean();   //  Gender before name
        name = generateName(rand);

        textureId = entity.worldObj.rand.nextInt(Integer.MAX_VALUE);
        health = entity.getHealth();
        maxHealth = entity.getMaxHealth();
        experience = 0;
        level = 0;
        Random random = new Random();

        //Initialize the citizen skills and make sure they are never 0
        intelligence = random.nextInt(LEVEL_CAP - 1) + 1;
        charisma = random.nextInt(LEVEL_CAP - 1) + 1;
        strength = random.nextInt(LEVEL_CAP - 1) + 1;
        endurance = random.nextInt(LEVEL_CAP - 1) + 1;
        dexterity = random.nextInt(LEVEL_CAP - 1) + 1;

        markDirty();
    }

    /**
     * Creates CitizenData from tag compound
     *
     * @param compound  NBT compound to build from
     * @param colony    Colony of the citizen
     * @return          CitizenData
     */
    public static CitizenData createFromNBT(NBTTagCompound compound, Colony colony)
    {
        int id = compound.getInteger(TAG_ID);
        CitizenData citizen = new CitizenData(id, colony);
        citizen.readFromNBT(compound);
        return citizen;
    }

    /**
     * Calculates the xp needed for the next level
     * @return the xp in double
     */
    public double getXpForNextLvl()
    {
        return EXPERIENCE_MULTIPLIER * (getLevel()+1) * (getLevel()+1);
    }
    /**
     * Returns the id of the citizen
     *
     * @return      id of the citizen
     */
    public int getId()
    {
        return id;
    }

    /**
     * Returns the colony of the citizen
     *
     * @return      colony of the citizen
     */
    public Colony getColony()
    {
        return colony;
    }

    /**
     * Returns the name of the citizen
     *
     * @return      name of the citizen
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns true if citizen is female, false for male
     *
     * @return      true for female, false for male
     */
    public boolean isFemale()
    {
        return isFemale;
    }

    /**
     * Returns the texture id for the citizen
     *
     * @return     texture ID
     */
    public int getTextureId()
    {
        return textureId;
    }

    /**
     * Returns the level of the citizen
     *
     * @return     level of the citizen
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * Returns the experience of the citizen
     *
     * @return     experience of the citizen
     */
    public double getExperience()
    {
        return experience;
    }

    /**
     * Sets the experience of the citizen
     */
    public void setExperience(double xp)
    {
        this.experience = xp;
    }

    /**
     * Adds experience of the citizen
     */
    public void addExperience(double xp)
    {
        this.experience += xp;
    }

    /**
     * Sets the level of the citizen
     */
    public void setLevel(int lvl)
    {
        this.level = lvl;
    }

    /**
     * Sets the level of the citizen
     */
    public void increaseLevel()
    {
        this.level+=1;
    }

    /**
     * Returns whether or not the instance is dirty
     *
     * @return      true when dirty, otherwise false
     */
    public boolean isDirty()
    {
        return isDirty;
    }

    /**
     * Marks the instance dirty
     */
    public void markDirty()
    {
        isDirty = true;
        colony.markCitizensDirty();
    }

    /**
     * Markt the instance not dirty
     */
    public void clearDirty()
    {
        isDirty = false;
    }

    /**
     * Returns the home building of the citizen
     *
     * @return      home building
     */
    public BuildingHome getHomeBuilding()
    {
        return homeBuilding;
    }

    /**
     * Sets the home of the citizen
     *
     * @param building  home building
     */
    public void setHomeBuilding(BuildingHome building)
    {
        if(homeBuilding != null && building != null && homeBuilding != building)
        {
            throw new IllegalStateException("CitizenData.setHomeBuilding() - already assigned a home building when setting a new home building");
        } else if(homeBuilding != building)
        {
            homeBuilding = building;
            markDirty();
        }
    }

    /**
     * Returns the work building of a citizen
     *
     * @return      home building of a citizen
     */
    public BuildingWorker getWorkBuilding(){ return workBuilding; }

    //TODO: inspect if we can use this to make worker buildings nicer
    public <BUILDING extends BuildingWorker> BUILDING getWorkBuilding(Class<BUILDING> type)
    {
        try
        {
            return type.cast(workBuilding);
        }
        catch(ClassCastException exc)
        {
            return null;
        }
    }

    /**
     * Sets the work building of a citizen
     *
     * @param building  work building
     */
    public void setWorkBuilding(BuildingWorker building)
    {
        if(workBuilding != null && building != null && workBuilding != building)
        {
            throw new IllegalStateException("CitizenData.setWorkBuilding() - already assigned a work building when setting a new work building");
        } else if(workBuilding != building)
        {
            workBuilding = building;

            if(workBuilding != null)
            {
                //  We have a place to work, do we have the assigned Job?
                if(job == null)
                {
                    //  No job, create one!
                    setJob(workBuilding.createJob(this));
                    colony.getWorkManager().clearWorkForCitizen(this);
                }
            } else if(job != null)
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
     * @param building building that is destroyed
     */
    public void onRemoveBuilding(Building building)
    {
        if(getHomeBuilding() == building)
        {
            setHomeBuilding(null);
        }

        if(getWorkBuilding() == building)
        {
            setWorkBuilding(null);
        }
    }

    /**
     * return the entity instance of the citizen data
     *
     * @return          {@link EntityCitizen} of the citizen data
     */
    public EntityCitizen getCitizenEntity()
    {
        return entity; /*(entity != null) ? entity.get() : null;*/
    }

    /**
     * Sets the entity of the citizen data
     *
     * @param citizen   {@link EntityCitizen} instance of the citizen data
     */
    public void setCitizenEntity(EntityCitizen citizen)
    {
        entity = citizen;
        markDirty();
    }

    /**
     * Sets {@link EntityCitizen} to null for the instance
     */
    public void clearCitizenEntity()
    {
        entity = null;
    }

    /**
     * Returns the job of the citizen
     *
     * @return          Job of the citizen
     */
    public Job getJob()
    {
        return job;
    }


    /**
     * Returns the job subclass needed. Returns null on type mismatch.
     * @param type the type of job wanted.
     * @param <JOB> The job type returned
     * @return the job this citizen has
     */
    public <JOB extends Job> JOB getJob(Class<JOB> type)
    {
        try
        {
            return type.cast(job);
        }
        catch(ClassCastException exc)
        {
            return null;
        }
    }

    /**
     * Sets the job of this citizen
     *
     * @param job   Job of the citizen
     */
    public void setJob(Job job)
    {
        this.job = job;

        EntityCitizen entity = getCitizenEntity();
        if(entity != null)
        {
            entity.onJobChanged(job);
        }

        markDirty();
    }

    /**
     * Writes the citiizen data to an NBT-compound
     *
     * @param compound  NBT-Tag compound
     */
    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setInteger(TAG_ID, id);
        compound.setString(TAG_NAME, name);
        compound.setBoolean(TAG_FEMALE, isFemale);
        compound.setInteger(TAG_TEXTURE, textureId);

        //  Attributes
        compound.setInteger(TAG_LEVEL, level);
        compound.setDouble(TAG_EXPERIENCE, experience);
        compound.setFloat(TAG_HEALTH, health);
        compound.setFloat(TAG_MAX_HEALTH, maxHealth);


        NBTTagCompound nbtTagSkillsCompound = new NBTTagCompound();
        nbtTagSkillsCompound.setInteger(TAG_SKILL_STRENGTH, strength);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_STAMINA, endurance);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_SPEED, charisma);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_INTELLIGENCE, intelligence);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_DEXTERITY, dexterity);
        compound.setTag(TAG_SKILLS, nbtTagSkillsCompound);

        if(job != null)
        {
            NBTTagCompound jobCompound = new NBTTagCompound();
            job.writeToNBT(jobCompound);
            compound.setTag("job", jobCompound);
        }
    }

    /**
     * Reads data from NBT-tag compound
     *
     * @param compound  NBT-Tag compound
     */
    public void readFromNBT(NBTTagCompound compound)
    {
        name = compound.getString(TAG_NAME);
        isFemale = compound.getBoolean(TAG_FEMALE);
        textureId = compound.getInteger(TAG_TEXTURE);

        //  Attributes
        level = compound.getInteger(TAG_LEVEL);
        experience = compound.getInteger(TAG_EXPERIENCE);
        health = compound.getFloat(TAG_HEALTH);
        maxHealth = compound.getFloat(TAG_MAX_HEALTH);


        NBTTagCompound nbtTagSkillsCompound = compound.getCompoundTag("skills");
        strength = nbtTagSkillsCompound.getInteger("strength");
        endurance = nbtTagSkillsCompound.getInteger("endurance");
        charisma = nbtTagSkillsCompound.getInteger("charisma");
        intelligence = nbtTagSkillsCompound.getInteger("intelligence");
        dexterity = nbtTagSkillsCompound.getInteger("dexterity");

        if(compound.hasKey("job"))
        {
            setJob(Job.createFromNBT(this, compound.getCompoundTag("job")));
        }
    }

    /**
     * Generates a random name from a set of names
     *
     * @param rand  Random object
     * @return      Name of the citizen
     */
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

    /**
     * Returns a random element in a list
     *
     * @param rand      Random object
     * @param array     Array to select from
     * @return          Random element from array
     */
    private String getRandomElement(Random rand, String[] array)
    {
        return array[rand.nextInt(array.length)];
    }

    /**
     * Returns a random capital letter from the alphabet
     *
     * @param rand      Random object
     * @return          Random capital letter
     */
    private char getRandomLetter(Random rand)
    {
        return (char) (rand.nextInt(26) + 'A');
    }

    /**
     * Strength getter
     * @return citizen Strength value
     */
    public int getStrength()
    {
        return strength;
    }

    /**
     * Endurance getter
     * @return citizen Endurance value
     */
    public int getEndurance()
    {
        return endurance;
    }

    /**
     * Charisma getter
     * @return citizen Charisma value
     */
    public int getCharisma()
    {
        return charisma;
    }

    /**
     * Intelligence getter
     * @return citizen Intelligence value
     */
    public int getIntelligence()
    {
        return intelligence;
    }

    /**
     * Dexterity getter
     * @return citizen Dexterity value
     */
    public int getDexterity()
    {
        return dexterity;
    }

    /**
     * The CitizenData View is the client-side representation of a CitizenData.
     * Views contain the CitizenData's data that is relevant to a Client, in a more client-friendly form
     * Mutable operations on a View result in a message to the server to perform the operation
     */
    public static class View
    {
        /**
         * Attributes
         */
        private final int       id;
        private       int       entityId;
        private       String    name;
        private       boolean   isFemale;

        //  Placeholder skills
        private       int       level;
        private       double       experience;
        public        float     health;
        public        float     maxHealth;
        public        int       strength, endurance, charisma, intelligence, dexterity;

        /**
         * Job identifier
         */
        private String job;

        /**
         * Working and home position
         */
        private BlockPos homeBuilding;
        private BlockPos workBuilding;

        /**
         * Set View id
         * @param id the id to set
         */
        protected View(int id)
        {
            this.id = id;
        }

        /**
         * Id getter
         * @return view Id
         */
        public int getID()
        {
            return id;
        }

        /**
         * Entity Id getter
         * @return entity id
         */
        public int getEntityId()
        {
            return entityId;
        }

        /**
         * Entity name getter
         * @return entity name
         */
        public String getName()
        {
            return name;
        }

        /**
         * Check entity sex
         * @return true if entity is female
         */
        public boolean isFemale()
        {
            return isFemale;
        }

        /**
         * Entity level getter
         * @return the citizens level
         */
        public int getLevel()
        {
            return level;
        }

        /**
         * Entity experience getter
         * @return it's experience
         */
        public double getExperience()
        {
            return experience;
        }

        /**
         * Calculates the xp needed for the next level
         * @return the xp in double
         */
        public double getXpForNextLvl()
        {
            return EXPERIENCE_MULTIPLIER * (getLevel()+1) * (getLevel()+1);
        }

        /**
         * Entity job getter
         * @return the job as a string
         */
        public String getJob()
        {
            return job;
        }

        /**
         * Get the entities home building
         * @return the home coordinates
         */
        public BlockPos getHomeBuilding()
        {
            return homeBuilding;
        }

        /**
         * Get the entities work building
         * @return the work coordinates
         */
        public BlockPos getWorkBuilding()
        {
            return workBuilding;
        }

        /**
         * Deserialize the attributes and variables from transition
         * @param buf Byte buffer to deserialize
         */
        public void deserialize(ByteBuf buf)
        {
            name = ByteBufUtils.readUTF8String(buf);
            isFemale = buf.readBoolean();
            entityId = buf.readInt();

            homeBuilding = buf.readBoolean() ? BlockPosUtil.readFromByteBuf(buf) : null;
            workBuilding = buf.readBoolean() ? BlockPosUtil.readFromByteBuf(buf) : null;

            //  Attributes
            level = buf.readInt();
            experience = buf.readDouble();
            health = buf.readFloat();
            maxHealth = buf.readFloat();

            strength = buf.readInt();
            endurance = buf.readInt();
            charisma = buf.readInt();
            intelligence = buf.readInt();
            dexterity = buf.readInt();

            job = ByteBufUtils.readUTF8String(buf);
        }
    }

    /**
     * Writes the citizen data to a byte buf for transition.
     *
     * @param buf   Buffer to write to
     */
    public void serializeViewNetworkData(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeBoolean(isFemale);

        EntityCitizen entity = getCitizenEntity();
        buf.writeInt(entity != null ? entity.getEntityId() : -1);

        buf.writeBoolean(homeBuilding != null);
        if(homeBuilding != null)
        {
            BlockPosUtil.writeToByteBuf(buf, homeBuilding.getID());
        }

        buf.writeBoolean(workBuilding != null);
        if(workBuilding != null)
        {
            BlockPosUtil.writeToByteBuf(buf, workBuilding.getID());
        }

        //  Attributes
        buf.writeInt(getLevel());
        buf.writeDouble(getExperience());

        //If entity is null assume the standard values as health
        if(entity==null)
        {
            buf.writeFloat(20.0F);
            buf.writeFloat(20.0F);
        }
        else
        {
            buf.writeFloat(entity.getHealth());
            buf.writeFloat(entity.getMaxHealth());
        }

        buf.writeInt(getStrength());
        buf.writeInt(getEndurance());
        buf.writeInt(getCharisma());
        buf.writeInt(getIntelligence());
        buf.writeInt(getDexterity());

        ByteBufUtils.writeUTF8String(buf, (job != null) ? job.getName() : "");
    }

    /**
     * Create a CitizenData View given it's saved NBTTagCompound
     *
     * @param id    The citizen's id
     * @param buf   The network data
     * @return      View object of the citizen
     */
    public static View createCitizenDataView(int id, ByteBuf buf)
    {
        View view = new View(id);

        try
        {
            view.deserialize(buf);
        }
        catch(Exception ex)
        {
            Log.logger.error(String.format("A CitizenData.View for #%d has thrown an exception during loading, its state cannot be restored. Report this to the mod author", view.getID()), ex);
            view = null;
        }

        return view;
    }
}
