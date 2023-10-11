package com.minecolonies.api.entity;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;

public class ModEntities
{
    public static EntityType<? extends AbstractEntityCitizen> CITIZEN;

    public static EntityType<? extends AbstractEntityCitizen> VISITOR;

    public static EntityType<? extends Entity> FISHHOOK;

    public static EntityType<? extends AbstractEntityBarbarian> BARBARIAN;

    public static EntityType<? extends PathfinderMob> MERCENARY;

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

    public static EntityType<? extends AbstractArrow> FIREARROW;

    public static EntityType<? extends Arrow> MC_NORMAL_ARROW;

    public static EntityType<? extends ThrownPotion> DRUID_POTION;

    public static EntityType<? extends ThrownTrident> SPEAR;
}
