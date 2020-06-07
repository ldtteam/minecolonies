package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.entity.MinecoloniesMinecart;
import com.minecolonies.api.entity.ModEntities;
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
import com.minecolonies.coremod.entity.mobs.norsemen.EntityNorsemenArcher;
import com.minecolonies.coremod.entity.mobs.norsemen.EntityNorsemenChief;
import com.minecolonies.coremod.entity.mobs.norsemen.EntityShieldmaiden;
import com.minecolonies.coremod.entity.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityCaptainPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityPirate;
import net.minecraft.entity.Entity;
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
    public static void setupEntities()
    {
        ModEntities.CITIZEN = build("citizen",
            EntityType.Builder.create(EntityCitizen::new, EntityClassification.CREATURE)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                .setShouldReceiveVelocityUpdates(true));

        ModEntities.FISHHOOK = build("fishhook",
            EntityType.Builder.<NewBobberEntity> create(NewBobberEntity::new, EntityClassification.MISC)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
                .size(0.25F, 0.25F)
                .setShouldReceiveVelocityUpdates(true)
                .setCustomClientFactory(NewBobberEntity::new));

        ModEntities.MERCENARY = build("mercenary",
            EntityType.Builder.create(EntityMercenary::new, EntityClassification.CREATURE)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.BARBARIAN = build("barbarian",
            EntityType.Builder.create(EntityBarbarian::new, EntityClassification.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.ARCHERBARBARIAN = build("archerbarbarian",
            EntityType.Builder.create(EntityArcherBarbarian::new, EntityClassification.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.CHIEFBARBARIAN = build("chiefbarbarian",
            EntityType.Builder.create(EntityChiefBarbarian::new, EntityClassification.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.PIRATE = build("pirate",
            EntityType.Builder.create(EntityPirate::new, EntityClassification.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.ARCHERPIRATE = build("archerpirate",
            EntityType.Builder.create(EntityArcherPirate::new, EntityClassification.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.CHIEFPIRATE = build("chiefpirate",
            EntityType.Builder.create(EntityCaptainPirate::new, EntityClassification.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.SITTINGENTITY = build("sittingentity",
            EntityType.Builder.<SittingEntity> create(SittingEntity::new, EntityClassification.AMBIENT)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size(0F, 0.5F));

        ModEntities.MINECART = build("mcminecart",
            EntityType.Builder.create(MinecoloniesMinecart::new, EntityClassification.MISC)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size(0.98F, 0.7F));

        ModEntities.MUMMY = build("mummy",
            EntityType.Builder.create(EntityMummy::new, EntityClassification.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.ARCHERMUMMY = build("archermummy",
            EntityType.Builder.create(EntityArcherMummy::new, EntityClassification.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.PHARAO = build("pharao",
            EntityType.Builder.create(EntityPharao::new, EntityClassification.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.AMAZON = build("amazon",
            EntityType.Builder.create(EntityArcherAmazon::new, EntityClassification.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.AMAZONCHIEF = build("amazonchief",
            EntityType.Builder.create(EntityAmazonChief::new, EntityClassification.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.FIREARROW = build("firearrow",
            EntityType.Builder.create(FireArrowEntity::new, EntityClassification.MISC)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
                .size(0.5F, 0.5F)
                .setShouldReceiveVelocityUpdates(true));
            
        ModEntities.SHIELDMAIDEN = build("shieldmaiden",
            EntityType.Builder.create(EntityShieldmaiden::new, EntityClassification.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.NORSEMEN_ARCHER = build("norsemenarcher",
            EntityType.Builder.create(EntityNorsemenArcher::new, EntityClassification.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

        ModEntities.NORSEMEN_CHIEF = build("norsemenchief",
            EntityType.Builder.create(EntityNorsemenChief::new, EntityClassification.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .size((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));
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
            ModEntities.AMAZONCHIEF,
            ModEntities.FIREARROW,
            ModEntities.SHIELDMAIDEN,
            ModEntities.NORSEMEN_ARCHER,
            ModEntities.NORSEMEN_CHIEF);
    }
}
