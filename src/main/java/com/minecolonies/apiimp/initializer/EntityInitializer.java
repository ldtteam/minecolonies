package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.entity.EntityFishHook;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityChiefBarbarian;
import com.minecolonies.coremod.entity.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityCaptainPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityPirate;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
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
    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
    {
        ModEntities.CITIZEN = EntityType.Builder.create(EntityCitizen::new, EntityClassification.CREATURE)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                .setShouldReceiveVelocityUpdates(true)
                                .build(Constants.MOD_ID + ":citizen")
                                .setRegistryName("citizen");

        ModEntities.FISHHOOK = EntityType.Builder.create(EntityFishHook::new, EntityClassification.MISC)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
                                .size(0.25F, 0.25F)
                                .setShouldReceiveVelocityUpdates(true)
                                .build(Constants.MOD_ID + ":fishhook")
                                .setRegistryName("fishhook");

        ModEntities.MERCENARY = EntityType.Builder.create(EntityMercenary::new, EntityClassification.CREATURE)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                  .size(1.0f, 2.0f)
                                .build(Constants.MOD_ID + ":mercenary")
                                .setRegistryName("mercenary");

        ModEntities.BARBARIAN = EntityType.Builder.create(EntityBarbarian::new, EntityClassification.MONSTER)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                  .size(1.0f, 2.0f)
                                .build(Constants.MOD_ID + ":barbarian")
                                .setRegistryName("barbarian");

        ModEntities.ARCHERBARBARIAN = EntityType.Builder.create(EntityArcherBarbarian::new, EntityClassification.MONSTER)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                        .size(1.0f, 2.0f)
                                .build(Constants.MOD_ID + ":archerbarbarian")
                                .setRegistryName("archerbarbarian");

        ModEntities.CHIEFBARBARIAN = EntityType.Builder.create(EntityChiefBarbarian::new, EntityClassification.MONSTER)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                       .size(1.0f, 2.2f)
                                .build(Constants.MOD_ID + ":chiefbarbarian")
                                .setRegistryName("chiefbarbarian");

        ModEntities.PIRATE = EntityType.Builder.create(EntityPirate::new, EntityClassification.MONSTER)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                               .size(1.0f, 2.0f)
                                .build(Constants.MOD_ID + ":pirate")
                                .setRegistryName("pirate");

        ModEntities.ARCHERPIRATE = EntityType.Builder.create(EntityArcherPirate::new, EntityClassification.MONSTER)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                     .size(1.0f, 2.0f)
                                .build(Constants.MOD_ID + ":archerpirate")
                                .setRegistryName("archerpirate");

        ModEntities.CHIEFPIRATE = EntityType.Builder.create(EntityCaptainPirate::new, EntityClassification.MONSTER)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                .size(1.0f, 2.2f)
                                .build(Constants.MOD_ID + ":chiefpirate")
                                .setRegistryName("chiefpirate");

        event.getRegistry().registerAll(ModEntities.CITIZEN);
    }
}
