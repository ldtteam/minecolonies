package com.minecolonies.coremod.entity;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.entity.ai.mobs.EntityMercenary;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityChiefBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityCaptainPirate;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityPirate;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static com.minecolonies.api.util.constant.ColonyConstants.*;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(Constants.MOD_ID)
public class MinecoloniesEntities
{
    public static final EntityType<EntityCitizen> CITIZEN = EntityType.Builder.<EntityCitizen>create(EntityCitizen::new, EntityClassification.MISC)
                                                        .setTrackingRange(1024)
                                                        .setUpdateInterval(20)
                                                        .setShouldReceiveVelocityUpdates(false)
                                                        .size(1.8f, 4.0f)
                                                        .setCustomClientFactory(EntityCitizen::new)
                                                        .immuneToFire()
                                                        .build(Constants.MOD_ID + ":Citizen");


    @SubscribeEvent
    public static void onRegisterEntities(final RegistryEvent.Register<EntityType<?>> event)
    {
        final ResourceLocation locationCitizen = new ResourceLocation(Constants.MOD_ID, "Citizen");
        final ResourceLocation locationFishHook = new ResourceLocation(Constants.MOD_ID, "Fishhook");

        // Half as much tracking range and same update frequency as a player
        // See EntityTracker.addEntityToTracker for more default values
        EntityRegistry.registerModEntity(locationCitizen,
          EntityCitizen.class,
          "Citizen",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);
        EntityRegistry.registerModEntity(locationFishHook,
          EntityFishHook.class,
          "Fishhook",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY_FISHHOOK,
          true);
        EntityRegistry.registerModEntity(BARBARIAN,
          EntityBarbarian.class,
          "Barbarian",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);
        EntityRegistry.registerModEntity(MERCENARY,
          EntityMercenary.class,
          "Mercenary",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);
        EntityRegistry.registerModEntity(ARCHER,
          EntityArcherBarbarian.class,
          "ArcherBarbarian",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);
        EntityRegistry.registerModEntity(CHIEF,
          EntityChiefBarbarian.class,
          "ChiefBarbarian",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);

        EntityRegistry.registerModEntity(PIRATE,
          EntityPirate.class,
          "Pirate",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);
        EntityRegistry.registerModEntity(PIRATE_ARCHER,
          EntityArcherPirate.class,
          "ArcherPirate",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);
        EntityRegistry.registerModEntity(PIRATE_CHIEF,
          EntityCaptainPirate.class,
          "ChiefPirate",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);

        //Register Barbarian spawn eggs
        EntityRegistry.registerEgg(BARBARIAN, PRIMARY_COLOR_BARBARIAN, SECONDARY_COLOR_BARBARIAN);
        EntityRegistry.registerEgg(ARCHER, PRIMARY_COLOR_BARBARIAN, SECONDARY_COLOR_BARBARIAN);
        EntityRegistry.registerEgg(CHIEF, PRIMARY_COLOR_BARBARIAN, SECONDARY_COLOR_BARBARIAN);

        //Register Pirate spawn eggs
        EntityRegistry.registerEgg(PIRATE, PRIMARY_COLOR_PIRATE, SECONDARY_COLOR_PIRATE);
        EntityRegistry.registerEgg(PIRATE_ARCHER, PRIMARY_COLOR_PIRATE, SECONDARY_COLOR_PIRATE);
        EntityRegistry.registerEgg(PIRATE_CHIEF, PRIMARY_COLOR_PIRATE, SECONDARY_COLOR_PIRATE);
    }
}
