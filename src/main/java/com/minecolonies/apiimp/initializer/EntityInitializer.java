package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
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
                                .build(Constants.MOD_ID + ":citizen")
                                .setRegistryName("citizen");

        ModEntities.CITIZEN = EntityType.Builder.create(EntityCitizen::new, EntityClassification.MISC)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
                                .size((float) 0.25F, (float) 0.25F)
                                .build(Constants.MOD_ID + ":fishhook")
                                .setRegistryName("fishhook");

        ModEntities.CITIZEN = EntityType.Builder.create(EntityCitizen::new, EntityClassification.CREATURE)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                .build(Constants.MOD_ID + ":citizen")
                                .setRegistryName("citizen");

        ModEntities.CITIZEN = EntityType.Builder.create(EntityCitizen::new, EntityClassification.CREATURE)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                .build(Constants.MOD_ID + ":citizen")
                                .setRegistryName("citizen");

        ModEntities.CITIZEN = EntityType.Builder.create(EntityCitizen::new, EntityClassification.CREATURE)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                .build(Constants.MOD_ID + ":citizen")
                                .setRegistryName("citizen");

        ModEntities.CITIZEN = EntityType.Builder.create(EntityCitizen::new, EntityClassification.CREATURE)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                .build(Constants.MOD_ID + ":citizen")
                                .setRegistryName("citizen");

        ModEntities.CITIZEN = EntityType.Builder.create(EntityCitizen::new, EntityClassification.CREATURE)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                .build(Constants.MOD_ID + ":citizen")
                                .setRegistryName("citizen");

        ModEntities.CITIZEN = EntityType.Builder.create(EntityCitizen::new, EntityClassification.CREATURE)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                .build(Constants.MOD_ID + ":citizen")
                                .setRegistryName("citizen");

        ModEntities.CITIZEN = EntityType.Builder.create(EntityCitizen::new, EntityClassification.CREATURE)
                                .setTrackingRange(ENTITY_TRACKING_RANGE)
                                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                                .build(Constants.MOD_ID + ":citizen")
                                .setRegistryName("citizen");

        event.getRegistry().registerAll(ModEntities.CITIZEN);
    }

    // Half as much tracking range and same update frequency as a player
    // See EntityTracker.addEntityToTracker for more default values

        EntityRegistry.registerModEntity(locationFishHook,
    EntityFishHook .class,
                           "Fishhook",
    getNextEntityId(),
    MineColonies.instance,
    ENTITY_TRACKING_RANGE,
    com.minecolonies.api.util.constant.Constants.ENTITY_UPDATE_FREQUENCY_FISHHOOK,
                                                                                   true);
        EntityRegistry.registerModEntity(BARBARIAN,
    EntityBarbarian .class,
                            "Barbarian",
    getNextEntityId(),
    MineColonies.instance,
    ENTITY_TRACKING_RANGE,
    ENTITY_UPDATE_FREQUENCY,
                                                                          true);
        EntityRegistry.registerModEntity(MERCENARY,
    EntityMercenary .class,
                            "Mercenary",
    getNextEntityId(),
    MineColonies.instance,
    ENTITY_TRACKING_RANGE,
    ENTITY_UPDATE_FREQUENCY,
                                                                          true);
        EntityRegistry.registerModEntity(ARCHER,
    EntityArcherBarbarian .class,
                                  "ArcherBarbarian",
    getNextEntityId(),
    MineColonies.instance,
    ENTITY_TRACKING_RANGE,
    ENTITY_UPDATE_FREQUENCY,
                                                                          true);
        EntityRegistry.registerModEntity(CHIEF,
    EntityChiefBarbarian .class,
                                 "ChiefBarbarian",
    getNextEntityId(),
    MineColonies.instance,
    ENTITY_TRACKING_RANGE,
    ENTITY_UPDATE_FREQUENCY,
                                                                          true);

        EntityRegistry.registerModEntity(PIRATE,
    EntityPirate .class,
                         "Pirate",
    getNextEntityId(),
    MineColonies.instance,
    ENTITY_TRACKING_RANGE,
    ENTITY_UPDATE_FREQUENCY,
                                                                          true);
        EntityRegistry.registerModEntity(PIRATE_ARCHER,
    EntityArcherPirate .class,
                               "ArcherPirate",
    getNextEntityId(),
    MineColonies.instance,
    ENTITY_TRACKING_RANGE,
    ENTITY_UPDATE_FREQUENCY,
                                                                          true);
        EntityRegistry.registerModEntity(PIRATE_CHIEF,
    EntityCaptainPirate .class,
                                "ChiefPirate",
    getNextEntityId(),
    MineColonies.instance,
    ENTITY_TRACKING_RANGE,
    ENTITY_UPDATE_FREQUENCY,
                                                                          true);
}
