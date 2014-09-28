package com.minecolonies.colony;

import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import java.util.Random;
import java.util.UUID;

/**
 * Extra data for Citizens
 *
 */
public class CitizenData
{
    public static final int SEX_MALE   = 0;
    public static final int SEX_FEMALE = 1;

    //  Attributes
    private final UUID   id;
    private String       name;
    private int          gender;
    private int          textureId;

    private BuildingHome   homeBuilding;
    private BuildingWorker workBuilding;

    //  Citizen
    public WeakReference<EntityCitizen> entity;

    //  Placeholder skills
    private int level;
    public  int strength, stamina, wisdom, intelligence, charisma;

    private static final String TAG_ID      = "id";
    private static final String TAG_NAME    = "name";
    private static final String TAG_GENDER  = "gender";
    private static final String TAG_TEXTURE = "texture";
    private static final String TAG_LEVEL   = "level";

    private static final String TAG_HOME_BUILDING = "homebuilding";
    private static final String TAG_WORK_BUILDING = "workbuilding";

    private static final String TAG_SKILLS         = "skills";
    private static final String TAG_SKILL_STRENGTH = "strength";
    private static final String TAG_SKILL_STAMINA  = "stamina";
    private static final String TAG_SKILL_WISDOM   = "wisdom";
    private static final String TAG_SKILL_INTELLIGENCE = "intelligence";
    private static final String TAG_SKILL_CHARISMA = "charisma";

    private CitizenData(UUID id)
    {
        this.id = id;
    }

    private CitizenData(EntityCitizen entity)
    {
        this.id = entity.getUniqueID();

        Random rand = entity.getRNG();

        this.entity = new WeakReference<EntityCitizen>(entity);

        gender = rand.nextInt(2);   //  Gender before name
        name = generateName(rand);
        textureId = entity.getTextureID();

        strength = rand.nextInt(10) + 1;
        stamina = rand.nextInt(10) + 1;
        wisdom = rand.nextInt(10) + 1;
        intelligence = rand.nextInt(10) + 1;
        charisma = rand.nextInt(10) + 1;
    }

    public static CitizenData createFromNBT(NBTTagCompound compound)
    {
        UUID id = UUID.fromString(compound.getString(TAG_ID));
        CitizenData citizen = new CitizenData(id);
        citizen.readFromNBT(compound);
        return citizen;
    }

    public static CitizenData createFromEntity(EntityCitizen entity)
    {
        CitizenData citizen = new CitizenData(entity);
        return citizen;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getSex() { return gender; }
    public int getTextureId() { return textureId; }
    public int getLevel() { return level; }

    public BuildingHome getHomeBuilding() { return homeBuilding; }
    public void setHomeBuilding(BuildingHome building)
    {
        if (homeBuilding != null && building != null && homeBuilding != building)
        {
            throw new IllegalStateException("CitizenData.setHomeBuilding() - already assigned a hom building when setting a new home building");
        }
        homeBuilding = building;
    }

    public BuildingWorker getWorkBuilding() { return workBuilding; }
    public void setWorkBuilding(BuildingWorker building)
    {
        if (workBuilding != null && building != null && workBuilding != building)
        {
            throw new IllegalStateException("CitizenData.setWorkBuilding() - already assigned a work building when setting a new work building");
        }
        workBuilding = building;
    }

    public void onRemoveBuilding(Building building)
    {
        if (getHomeBuilding() == building)
        {
            homeBuilding = null;
        }

        if (getWorkBuilding() == building)
        {
            workBuilding = null;
        }
    }

    public EntityCitizen getCitizenEntity() { return (entity != null) ? entity.get() : null; }
    public void registerCitizenEntity(EntityCitizen citizen)
    {
        if (!citizen.getUniqueID().equals(id))
        {
            throw new IllegalArgumentException(String.format("Mismatch citizen '%s' registered to CitizenData for '%s'", citizen.getUniqueID().toString(), id.toString()));
        }

        entity = new WeakReference<EntityCitizen>(citizen);
    }
    public void clearCitizenEntity()
    {
        entity = null;
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString(TAG_ID, id.toString());
        compound.setString(TAG_NAME, name);
        compound.setInteger(TAG_GENDER, gender);
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
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        name = compound.getString(TAG_NAME);
        gender = compound.getInteger(TAG_GENDER);
        textureId = compound.getInteger(TAG_TEXTURE);

        //  Attributes
        level = compound.getInteger(TAG_LEVEL);

        NBTTagCompound nbtTagSkillsCompound = compound.getCompoundTag("skills");
        strength = nbtTagSkillsCompound.getInteger("strength");
        stamina = nbtTagSkillsCompound.getInteger("stamina");
        wisdom = nbtTagSkillsCompound.getInteger("wisdom");
        intelligence = nbtTagSkillsCompound.getInteger("intelligence");
        charisma = nbtTagSkillsCompound.getInteger("charisma");
    }

    private String generateName(Random rand)
    {
        String firstName;
        if(getSex() == SEX_MALE)
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
}
