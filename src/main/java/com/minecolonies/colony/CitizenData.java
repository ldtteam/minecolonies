package com.minecolonies.colony;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import net.minecraft.nbt.NBTTagCompound;
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
    private UUID   id;
    private int    texture;
    private String name;
    private int    gender;

    //  Citizen
    public WeakReference<EntityCitizen> entity;

    //  Placeholder skills
    private int level;
    public int strength, stamina, wisdom, intelligence, charisma;

    private static final String TAG_ID     = "id";
    private static final String TAG_NAME   = "name";
    private static final String TAG_GENDER = "gender";
    private static final String TAG_LEVEL  = "level";
    private static final String TAG_TEXTURE = "texture";

    private static final String TAG_SKILLS         = "skills";
    private static final String TAG_SKILL_STRENGTH = "strength";
    private static final String TAG_SKILL_STAMINA = "stamina";
    private static final String TAG_SKILL_WISDOM = "wisdom";
    private static final String TAG_SKILL_INTELLIGENCE = "intelligence";
    private static final String TAG_SKILL_CHARISMA = "charisma";

    public CitizenData()
    {
    }

    /**
     * Generate random attributes
     *
     * @param citizen
     */
    public void setup(EntityCitizen citizen)
    {
        Random rand = citizen.getRNG();

        entity = new WeakReference<EntityCitizen>(citizen);

        id = citizen.getUniqueID();
        gender = rand.nextInt(2);   //  Gender before name
        name = generateName(rand);
        texture = citizen.getTextureID();

        strength = rand.nextInt(10) + 1;
        stamina = rand.nextInt(10) + 1;
        wisdom = rand.nextInt(10) + 1;
        intelligence = rand.nextInt(10) + 1;
        charisma = rand.nextInt(10) + 1;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getSex() { return gender; }
    public int getTexture() { return texture; }

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
        compound.setInteger(TAG_LEVEL, level);
        compound.setInteger(TAG_TEXTURE, texture);

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
        id = UUID.fromString(compound.getString(TAG_ID));
        name = compound.getString(TAG_NAME);
        gender = compound.getInteger(TAG_GENDER);
        level = compound.getInteger(TAG_LEVEL);
        texture = compound.getInteger(TAG_TEXTURE);

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
