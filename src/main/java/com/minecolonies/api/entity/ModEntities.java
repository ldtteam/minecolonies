package com.minecolonies.api.entity;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesRaider;
import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazonRaider;
import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarianRaider;
import com.minecolonies.api.entity.mobs.drownedpirate.AbstractDrownedEntityPirate;
import com.minecolonies.api.entity.mobs.drownedpirate.AbstractDrownedEntityPirateRaider;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptianRaider;
import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirateRaider;
import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen;
import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemenRaider;
import com.minecolonies.api.entity.other.MinecoloniesMinecart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.projectile.*;

import java.util.List;

public class ModEntities
{
    public static EntityType<? extends AbstractEntityCitizen> CITIZEN;

    public static EntityType<? extends AbstractEntityCitizen> VISITOR;

    public static EntityType<? extends Projectile> FISHHOOK;

    public static EntityType<? extends PathfinderMob> MERCENARY;

    public static EntityType<? extends AbstractEntityBarbarianRaider> BARBARIAN;

    public static EntityType<? extends AbstractEntityBarbarianRaider> ARCHERBARBARIAN;

    public static EntityType<? extends AbstractEntityBarbarianRaider> CHIEFBARBARIAN;

    public static EntityType<? extends AbstractEntityPirateRaider> PIRATE;

    public static EntityType<? extends AbstractEntityPirateRaider> CHIEFPIRATE;

    public static EntityType<? extends AbstractEntityPirateRaider> ARCHERPIRATE;

    public static EntityType<? extends Entity> SITTINGENTITY;

    public static EntityType<? extends AbstractEntityEgyptianRaider> MUMMY;

    public static EntityType<? extends AbstractEntityEgyptianRaider> PHARAO;

    public static EntityType<? extends AbstractEntityEgyptianRaider> ARCHERMUMMY;

    public static EntityType<? extends AbstractEntityNorsemenRaider> NORSEMEN_ARCHER;

    public static EntityType<? extends AbstractEntityNorsemenRaider> SHIELDMAIDEN;

    public static EntityType<? extends AbstractEntityNorsemenRaider> NORSEMEN_CHIEF;

    public static EntityType<? extends AbstractEntityAmazonRaider> AMAZON;

    public static EntityType<? extends AbstractEntityAmazonRaider> AMAZONSPEARMAN;

    public static EntityType<? extends AbstractEntityAmazonRaider> AMAZONCHIEF;

    public static EntityType<MinecoloniesMinecart> MINECART;

    public static EntityType<? extends AbstractArrow> FIREARROW;

    public static EntityType<? extends Arrow> MC_NORMAL_ARROW;

    public static EntityType<? extends ThrownPotion> DRUID_POTION;

    public static EntityType<? extends ThrownTrident> SPEAR;

    public static EntityType<? extends AbstractDrownedEntityPirateRaider> DROWNED_PIRATE;

    public static EntityType<? extends AbstractDrownedEntityPirateRaider> DROWNED_CHIEFPIRATE;

    public static EntityType<? extends AbstractDrownedEntityPirateRaider> DROWNED_ARCHERPIRATE;

    // Camp Raiders

    public static EntityType<? extends AbstractEntityBarbarian> CAMP_BARBARIAN;

    public static EntityType<? extends AbstractEntityBarbarian> CAMP_ARCHERBARBARIAN;

    public static EntityType<? extends AbstractEntityBarbarian> CAMP_CHIEFBARBARIAN;

    public static EntityType<? extends AbstractEntityPirate> CAMP_PIRATE;

    public static EntityType<? extends AbstractEntityPirate> CAMP_CHIEFPIRATE;

    public static EntityType<? extends AbstractEntityPirate> CAMP_ARCHERPIRATE;

    public static EntityType<? extends AbstractEntityAmazon> CAMP_AMAZON;

    public static EntityType<? extends AbstractEntityAmazon> CAMP_AMAZONSPEARMAN;

    public static EntityType<? extends AbstractEntityAmazon> CAMP_AMAZONCHIEF;

    public static EntityType<? extends AbstractEntityEgyptian> CAMP_MUMMY;

    public static EntityType<? extends AbstractEntityEgyptian> CAMP_PHARAO;

    public static EntityType<? extends AbstractEntityEgyptian> CAMP_ARCHERMUMMY;

    public static EntityType<? extends AbstractEntityNorsemen> CAMP_NORSEMEN_ARCHER;

    public static EntityType<? extends AbstractEntityNorsemen> CAMP_SHIELDMAIDEN;

    public static EntityType<? extends AbstractEntityNorsemen> CAMP_NORSEMEN_CHIEF;

    public static EntityType<? extends AbstractDrownedEntityPirate> CAMP_DROWNED_PIRATE;

    public static EntityType<? extends AbstractDrownedEntityPirate> CAMP_DROWNED_CHIEFPIRATE;

    public static EntityType<? extends AbstractDrownedEntityPirate> CAMP_DROWNED_ARCHERPIRATE;

    public static List<EntityType<? extends AbstractEntityMinecoloniesRaider>> getRaiders()
    {
        return List.of(
          BARBARIAN,
          ARCHERBARBARIAN,
          CHIEFBARBARIAN,
          AMAZON,
          AMAZONSPEARMAN,
          AMAZONCHIEF,
          MUMMY,
          ARCHERMUMMY,
          PHARAO,
          PIRATE,
          ARCHERPIRATE,
          CHIEFPIRATE,
          SHIELDMAIDEN,
          NORSEMEN_ARCHER,
          NORSEMEN_CHIEF,
          DROWNED_PIRATE,
          DROWNED_ARCHERPIRATE,
          DROWNED_CHIEFPIRATE
        );
    }
}
