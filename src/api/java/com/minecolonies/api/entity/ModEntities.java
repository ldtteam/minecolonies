package com.minecolonies.api.entity;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.entity.FireArrowEntity;
import com.minecolonies.coremod.entity.NewBobberEntity;
import com.minecolonies.coremod.entity.SittingEntity;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import com.minecolonies.coremod.entity.mobs.amazons.EntityAmazonChief;
import com.minecolonies.coremod.entity.mobs.amazons.EntityArcherAmazon;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityChiefBarbarian;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityArcherMummy;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityMummy;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityPharao;
import com.minecolonies.coremod.entity.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityCaptainPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityPirate;
import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
public class ModEntities
{
    @ObjectHolder("citizen")
    public static EntityType<EntityCitizen> CITIZEN;

    @ObjectHolder("fishhook")
    public static EntityType<NewBobberEntity> FISHHOOK;

    @ObjectHolder("barbarian")
    public static EntityType<EntityBarbarian> BARBARIAN;

    @ObjectHolder("mercenary")
    public static EntityType<EntityMercenary> MERCENARY;

    @ObjectHolder("archerbarbarian")
    public static EntityType<EntityArcherBarbarian> ARCHERBARBARIAN;

    @ObjectHolder("chiefbarbarian")
    public static EntityType<EntityChiefBarbarian> CHIEFBARBARIAN;

    @ObjectHolder("pirate")
    public static EntityType<EntityPirate> PIRATE;

    @ObjectHolder("chiefpirate")
    public static EntityType<EntityCaptainPirate> CHIEFPIRATE;

    @ObjectHolder("archerpirate")
    public static EntityType<EntityArcherPirate> ARCHERPIRATE;

    @ObjectHolder("sittingentity")
    public static EntityType<SittingEntity> SITTINGENTITY;

    @ObjectHolder("mummy")
    public static EntityType<EntityMummy> MUMMY;

    @ObjectHolder("pharao")
    public static EntityType<EntityPharao> PHARAO;

    @ObjectHolder("archermummy")
    public static EntityType<EntityArcherMummy> ARCHERMUMMY;

    @ObjectHolder("amazon")
    public static EntityType<EntityArcherAmazon> AMAZON;

    @ObjectHolder("amazonchief")
    public static EntityType<EntityAmazonChief> AMAZONCHIEF;

    @ObjectHolder("minecart")
    public static EntityType<MinecoloniesMinecart> MINECART;

    @ObjectHolder("firearrow")
    public static EntityType<FireArrowEntity> FIREARROW;
}
