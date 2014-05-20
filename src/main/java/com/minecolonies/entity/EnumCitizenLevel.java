package com.minecolonies.entity;

import com.minecolonies.client.model.ModelEntityCitizenFemaleAristocrat;
import com.minecolonies.client.model.ModelEntityCitizenFemaleCitizen;
import com.minecolonies.client.model.ModelEntityCitizenFemaleNoble;
import com.minecolonies.lib.Constants;
import net.minecraft.client.model.ModelBiped;

public enum EnumCitizenLevel
{
    SETTLERMALE(0, "Male", "/textures/entity/EntitySettler"),
    CITIZENMALE(1, "Male", "/textures/entity/EntityCitizen"),
    NOBLEMALE(2, "Male", "/textures/entity/EntityNoble"),
    ARISTOCRATMALE(3, "Male", "/textures/entity/EntityAristocrat"),
    SETTLERFEMALE(0, "Female", "/textures/entity/EntitySettler"),
    CITIZENFEMALE(1, "Female", "/textures/entity/EntityCitizen"),
    NOBLEFEMALE(2, "Female", "/textures/entity/EntityNoble"),
    ARISTOCRATFEMALE(3, "Female", "/textures/entity/EntityAristocrat");

    private final int        level;
    private final String     partialTextureString;
    private final int        sexInt;
    private final String     sexString;

    EnumCitizenLevel(int level, String sex, String textureLocationPart)
    {
        this.level = level;
        this.sexInt = sex.equalsIgnoreCase("Male") ? 0 : 1;
        this.sexString = sex;
        this.partialTextureString = setTexture(textureLocationPart);
    }

    public int getLevel()
    {
        return this.level;
    }

    public int getSexInt()
    {
        return sexInt;
    }

    public String getSexString()
    {
        return sexString;
    }

    public String getTexture()
    {
        return this.partialTextureString;
    }

    public String setTexture(String texture)
    {
        return Constants.MODID + ":" + texture + sexString;
    }
}