package com.minecolonies.api.entity;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraftforge.registries.ObjectHolder;

public class ModEntities
{
    public static EntityType<? extends AbstractEntityCitizen> CITIZEN;

    public static EntityType<? extends AbstractEntityCitizen> VISITOR;

    public static EntityType<? extends Entity> FISHHOOK;

    public static EntityType<? extends AbstractEntityBarbarian> BARBARIAN;

    public static EntityType<? extends CreatureEntity> MERCENARY;

    public static EntityType<? extends AbstractEntityBarbarian> ARCHERBARBARIAN;

    public static EntityType<? extends AbstractEntityBarbarian> CHIEFBARBARIAN;

    public static EntityType<? extends AbstractEntityPirate> PIRATE;

    public static EntityType<? extends AbstractEntityPirate> CHIEFPIRATE;

    public static EntityType<? extends AbstractEntityPirate> ARCHERPIRATE;

    public static EntityType<? extends Entity> SITTINGENTITY;

    public static EntityType<? extends AbstractEntityEgyptian> MUMMY;

    public static EntityType<? extends AbstractEntityEgyptian> PHARAO;

    public static EntityType<? extends AbstractEntityEgyptian> ARCHERMUMMY;

    public static EntityType<? extends AbstractEntityNorsemen> NORSEMEN_ARCHER;

    public static EntityType<? extends AbstractEntityNorsemen> SHIELDMAIDEN;

    public static EntityType<? extends AbstractEntityNorsemen> NORSEMEN_CHIEF;

    public static EntityType<? extends AbstractEntityAmazon> AMAZON;

    public static EntityType<? extends AbstractEntityAmazon> AMAZONSPEARMAN;

    public static EntityType<? extends AbstractEntityAmazon> AMAZONCHIEF;

    public static EntityType<MinecoloniesMinecart> MINECART;

    public static EntityType<? extends AbstractArrowEntity> FIREARROW;

    @ObjectHolder("spear")
    public static EntityType<SpearEntity> SPEAR;

    public static EntityType<? extends ArrowEntity> MC_NORMAL_ARROW;

    public static EntityType<? extends PotionEntity> DRUID_POTION;
}