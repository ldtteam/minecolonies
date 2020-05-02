package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.entity.MinecoloniesMinecart;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.entity.NewBobberEntity;
import com.minecolonies.coremod.entity.SittingEntity;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityChiefBarbarian;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityArcherMummy;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityMummy;
import com.minecolonies.coremod.entity.mobs.egyptians.EntityPharao;
import com.minecolonies.coremod.entity.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityCaptainPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityPirate;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
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
        ModEntities.CITIZEN = (EntityType<? extends AbstractEntityCitizen>) EntityType.Builder.create(EntityCitizen::new, EntityClassification.CREATURE)
                                                                      .setTrackingRange(ENTITY_TRACKING_RANGE)
                                                                      .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                                                      .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                                                      .setShouldReceiveVelocityUpdates(true)
                                                                      .build(Constants.MOD_ID + ":citizen")
                                                                      .setRegistryName("citizen");

        ModEntities.FISHHOOK = EntityType.Builder.create(NewBobberEntity::new, EntityClassification.MISC)
                                 .setTrackingRange(ENTITY_TRACKING_RANGE)
                                 .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
                                 .size(0.25F, 0.25F)
                                 .setShouldReceiveVelocityUpdates(true)
                                 .setCustomClientFactory(NewBobberEntity::new)
                                 .build(Constants.MOD_ID + ":fishhook")
                                 .setRegistryName("fishhook");

        ModEntities.MERCENARY = (EntityType<? extends CreatureEntity>) EntityType.Builder.create(EntityMercenary::new, EntityClassification.CREATURE)
                                                                         .setTrackingRange(ENTITY_TRACKING_RANGE)
                                                                         .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                                                         .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                                                         .build(Constants.MOD_ID + ":mercenary")
                                                                         .setRegistryName("mercenary");

        ModEntities.BARBARIAN = EntityType.Builder.create(EntityBarbarian::new, EntityClassification.MONSTER)
                                  .setTrackingRange(ENTITY_TRACKING_RANGE)
                                  .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                  .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                  .build(Constants.MOD_ID + ":barbarian")
                                  .setRegistryName("barbarian");

        ModEntities.ARCHERBARBARIAN = EntityType.Builder.create(EntityArcherBarbarian::new, EntityClassification.MONSTER)
                                        .setTrackingRange(ENTITY_TRACKING_RANGE)
                                        .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                        .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                        .build(Constants.MOD_ID + ":archerbarbarian")
                                        .setRegistryName("archerbarbarian");

        ModEntities.CHIEFBARBARIAN = EntityType.Builder.create(EntityChiefBarbarian::new, EntityClassification.MONSTER)
                                       .setTrackingRange(ENTITY_TRACKING_RANGE)
                                       .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                       .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                       .build(Constants.MOD_ID + ":chiefbarbarian")
                                       .setRegistryName("chiefbarbarian");

        ModEntities.PIRATE = EntityType.Builder.create(EntityPirate::new, EntityClassification.MONSTER)
                               .setTrackingRange(ENTITY_TRACKING_RANGE)
                               .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                               .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                               .build(Constants.MOD_ID + ":pirate")
                               .setRegistryName("pirate");

        ModEntities.ARCHERPIRATE = EntityType.Builder.create(EntityArcherPirate::new, EntityClassification.MONSTER)
                                     .setTrackingRange(ENTITY_TRACKING_RANGE)
                                     .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                     .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                     .build(Constants.MOD_ID + ":archerpirate")
                                     .setRegistryName("archerpirate");

        ModEntities.CHIEFPIRATE = EntityType.Builder.create(EntityCaptainPirate::new, EntityClassification.MONSTER)
                                    .setTrackingRange(ENTITY_TRACKING_RANGE)
                                    .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                    .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                    .build(Constants.MOD_ID + ":chiefpirate")
                                    .setRegistryName("chiefpirate");

        ModEntities.SITTINGENTITY = EntityType.Builder.create(SittingEntity::new, EntityClassification.AMBIENT)
                                      .setTrackingRange(ENTITY_TRACKING_RANGE)
                                      .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                      .size(0F, 0.5F)
                                      .build(Constants.MOD_ID + ":sittingentity")
                                      .setRegistryName("sittingentity");

        ModEntities.MINECART = (EntityType<AbstractMinecartEntity>) EntityType.Builder.create(MinecoloniesMinecart::new, EntityClassification.MISC)
                                      .setTrackingRange(ENTITY_TRACKING_RANGE)
                                      .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                      .size(0.98F, 0.7F)
                                      .build(Constants.MOD_ID + ":mcminecart")
                                      .setRegistryName("mcminecart");

        ModEntities.MUMMY = EntityType.Builder.create(EntityMummy::new, EntityClassification.MONSTER)
                               .setTrackingRange(ENTITY_TRACKING_RANGE)
                               .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                               .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                               .build(Constants.MOD_ID + ":mummy")
                               .setRegistryName("mummy");

        ModEntities.ARCHERMUMMY = EntityType.Builder.create(EntityArcherMummy::new, EntityClassification.MONSTER)
                                     .setTrackingRange(ENTITY_TRACKING_RANGE)
                                     .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                     .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                     .build(Constants.MOD_ID + ":archermummy")
                                     .setRegistryName("archermummy");

        ModEntities.PHARAO = EntityType.Builder.create(EntityPharao::new, EntityClassification.MONSTER)
                                    .setTrackingRange(ENTITY_TRACKING_RANGE)
                                    .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                    .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                    .build(Constants.MOD_ID + ":pharao")
                                    .setRegistryName("pharao");
    }

    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
    {
        event.getRegistry()
          .registerAll(ModEntities.CITIZEN,
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
            ModEntities.PHARAO);
    }
}
