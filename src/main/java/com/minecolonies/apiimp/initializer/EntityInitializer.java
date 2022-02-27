package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.entity.MinecoloniesMinecart;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.SpearEntity;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.entity.*;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.citizen.VisitorCitizen;
import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import com.minecolonies.coremod.entity.mobs.amazons.EntityAmazonChief;
import com.minecolonies.coremod.entity.mobs.amazons.EntityAmazonSpearman;
import com.minecolonies.coremod.entity.mobs.amazons.EntityArcherAmazon;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityChiefBarbarian;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityArcherMummy;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityMummy;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityPharao;
import com.minecolonies.coremod.entity.mobs.norsemen.EntityNorsemenArcher;
import com.minecolonies.coremod.entity.mobs.norsemen.EntityNorsemenChief;
import com.minecolonies.coremod.entity.mobs.norsemen.EntityShieldmaiden;
import com.minecolonies.coremod.entity.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityCaptainPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityPirate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_HEIGHT;
import static com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_WIDTH;
import static com.minecolonies.api.util.constant.Constants.*;

@ObjectHolder(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityInitializer
{
    public static void setupEntities()
    {
        ModEntities.CITIZEN = build("citizen",
          EntityType.Builder.of(EntityCitizen::new, EntityClassification.CREATURE)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
            .setShouldReceiveVelocityUpdates(true));

        ModEntities.FISHHOOK = build("fishhook",
          EntityType.Builder.<NewBobberEntity>of(NewBobberEntity::new, EntityClassification.MISC)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
            .sized(0.25F, 0.25F)
            .setShouldReceiveVelocityUpdates(true)
            .setCustomClientFactory(NewBobberEntity::new));

        ModEntities.VISITOR = build("visitor", EntityType.Builder.of(VisitorCitizen::new, EntityClassification.CREATURE)
                                                 .setTrackingRange(ENTITY_TRACKING_RANGE)
                                                 .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                                 .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                                 .setShouldReceiveVelocityUpdates(true));

        ModEntities.MERCENARY = build("mercenary",
          EntityType.Builder.of(EntityMercenary::new, EntityClassification.CREATURE)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.BARBARIAN = build("barbarian",
          EntityType.Builder.of(EntityBarbarian::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.ARCHERBARBARIAN = build("archerbarbarian",
          EntityType.Builder.of(EntityArcherBarbarian::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.CHIEFBARBARIAN = build("chiefbarbarian",
          EntityType.Builder.of(EntityChiefBarbarian::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.PIRATE = build("pirate",
          EntityType.Builder.of(EntityPirate::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.ARCHERPIRATE = build("archerpirate",
          EntityType.Builder.of(EntityArcherPirate::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.CHIEFPIRATE = build("chiefpirate",
          EntityType.Builder.of(EntityCaptainPirate::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.SITTINGENTITY = build("sittingentity",
          EntityType.Builder.<SittingEntity>of(SittingEntity::new, EntityClassification.MISC)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized(0F, 0.5F));

        ModEntities.MINECART = build("mcminecart",
          EntityType.Builder.of(MinecoloniesMinecart::new, EntityClassification.MISC)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized(0.98F, 0.7F));

        ModEntities.MUMMY = build("mummy",
          EntityType.Builder.of(EntityMummy::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.ARCHERMUMMY = build("archermummy",
          EntityType.Builder.of(EntityArcherMummy::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.PHARAO = build("pharao",
          EntityType.Builder.of(EntityPharao::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.AMAZON = build("amazon",
          EntityType.Builder.of(EntityArcherAmazon::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.AMAZONSPEARMAN = build("amazonspearman",
          EntityType.Builder.of(EntityAmazonSpearman::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.AMAZONCHIEF = build("amazonchief",
          EntityType.Builder.of(EntityAmazonChief::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.FIREARROW = build("firearrow",
          EntityType.Builder.of(FireArrowEntity::new, EntityClassification.MISC)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
            .sized(0.5F, 0.5F)
            .setShouldReceiveVelocityUpdates(true));

        ModEntities.MC_NORMAL_ARROW = build("mcnormalarrow",
          EntityType.Builder.of(CustomArrowEntity::new, EntityClassification.MISC)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
            .sized(0.5F, 0.5F)
            .setShouldReceiveVelocityUpdates(true));

        ModEntities.DRUID_POTION = build("druidpotion",
          EntityType.Builder.<DruidPotionEntity>of(DruidPotionEntity::new, EntityClassification.MISC)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
            .sized(0.25F, 0.25F)
            .setShouldReceiveVelocityUpdates(true));

        ModEntities.SHIELDMAIDEN = build("shieldmaiden",
          EntityType.Builder.of(EntityShieldmaiden::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.NORSEMEN_ARCHER = build("norsemenarcher",
          EntityType.Builder.of(EntityNorsemenArcher::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.NORSEMEN_CHIEF = build("norsemenchief",
          EntityType.Builder.of(EntityNorsemenChief::new, EntityClassification.MONSTER)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.SPEAR = build("spear",
          EntityType.Builder.<SpearEntity>of(SpearEntity::new, EntityClassification.MISC)
            .setTrackingRange(ENTITY_TRACKING_RANGE)
            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
            .sized(0.5F, 0.5F)
            .setShouldReceiveVelocityUpdates(true));
    }

    private static <T extends Entity> EntityType<T> build(final String key, final EntityType.Builder<T> builder)
    {
        final EntityType<T> entityType = builder.build(Constants.MOD_ID + ":" + key);
        // TODO: next major mc release, setRegistryName should have complete RL as param
        entityType.setRegistryName(key);
        return entityType;
    }

    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
    {
        event.getRegistry()
          .registerAll(ModEntities.CITIZEN,
            ModEntities.VISITOR,
            ModEntities.PIRATE,
            ModEntities.ARCHERPIRATE,
            ModEntities.CHIEFPIRATE,
            ModEntities.MERCENARY,
            ModEntities.ARCHERBARBARIAN,
            ModEntities.BARBARIAN,
            ModEntities.CHIEFBARBARIAN,
            ModEntities.FISHHOOK,
            ModEntities.SITTINGENTITY,
            ModEntities.MINECART,
            ModEntities.MUMMY,
            ModEntities.ARCHERMUMMY,
            ModEntities.PHARAO,
            ModEntities.AMAZON,
            ModEntities.AMAZONSPEARMAN,
            ModEntities.AMAZONCHIEF,
            ModEntities.FIREARROW,
            ModEntities.MC_NORMAL_ARROW,
            ModEntities.SHIELDMAIDEN,
            ModEntities.NORSEMEN_ARCHER,
            ModEntities.NORSEMEN_CHIEF,
            ModEntities.SPEAR,
            ModEntities.DRUID_POTION);
    }
}
