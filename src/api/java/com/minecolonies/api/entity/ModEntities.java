package com.minecolonies.api.entity;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
public class ModEntities
{
    @ObjectHolder("citizen")
    public static EntityType<? extends AbstractEntityCitizen> CITIZEN;

    @ObjectHolder("fishhook")
    public static EntityType<?> FISHHOOK;

    @ObjectHolder("barbarian")
    public static EntityType<?> BARBARIAN;

    @ObjectHolder("mercenary")
    public static EntityType<? extends CreatureEntity> MERCENARY;

    @ObjectHolder("archerbarbarian")
    public static EntityType<?> ARCHERBARBARIAN;

    @ObjectHolder("chiefbarbarian")
    public static EntityType<?> CHIEFBARBARIAN;

    @ObjectHolder("pirate")
    public static EntityType<?> PIRATE;

    @ObjectHolder("chiefpirate")
    public static EntityType<?> CHIEFPIRATE;

    @ObjectHolder("archerpirate")
    public static EntityType<?> ARCHERPIRATE;

    @ObjectHolder("sittingentity")
    public static EntityType<?> SITTINGENTITY;

    @ObjectHolder("mummy")
    public static EntityType<?> MUMMY;

    @ObjectHolder("pharao")
    public static EntityType<?> PHARAO;

    @ObjectHolder("archermummy")
    public static EntityType<?> ARCHERMUMMY;

    @ObjectHolder("minecart")
    public static EntityType<AbstractMinecartEntity> MINECART;
}
