package com.minecolonies.api.entity;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
public class ModEntities
{
    @ObjectHolder("citizen")
    public static EntityType<? extends AbstractEntityCitizen> CITIZEN;

    @ObjectHolder("fishhook")
    public static EntityType<? extends Entity> FISHHOOK;

    @ObjectHolder("barbarian")
    public static EntityType<? extends AbstractEntityBarbarian> BARBARIAN;

    @ObjectHolder("mercenary")
    public static EntityType<? extends CreatureEntity> MERCENARY;

    @ObjectHolder("archerbarbarian")
    public static EntityType<? extends AbstractEntityBarbarian> ARCHERBARBARIAN;

    @ObjectHolder("chiefbarbarian")
    public static EntityType<? extends AbstractEntityBarbarian> CHIEFBARBARIAN;

    @ObjectHolder("pirate")
    public static EntityType<? extends AbstractEntityPirate> PIRATE;

    @ObjectHolder("chiefpirate")
    public static EntityType<? extends AbstractEntityPirate> CHIEFPIRATE;

    @ObjectHolder("archerpirate")
    public static EntityType<? extends AbstractEntityPirate> ARCHERPIRATE;

    @ObjectHolder("sittingentity")
    public static EntityType<? extends Entity> SITTINGENTITY;

    @ObjectHolder("mummy")
    public static EntityType<? extends AbstractEntityEgyptian> MUMMY;

    @ObjectHolder("pharao")
    public static EntityType<? extends AbstractEntityEgyptian> PHARAO;

    @ObjectHolder("archermummy")
    public static EntityType<? extends AbstractEntityEgyptian> ARCHERMUMMY;

    @ObjectHolder("amazon")
    public static EntityType<? extends AbstractEntityAmazon> AMAZON;

    @ObjectHolder("amazonchief")
    public static EntityType<? extends AbstractEntityAmazon> AMAZONCHIEF;

    @ObjectHolder("minecart")
    public static EntityType<MinecoloniesMinecart> MINECART;

    @ObjectHolder("firearrow")
    public static EntityType<? extends AbstractArrowEntity> FIREARROW;
}