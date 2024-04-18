package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.entity.other.MinecoloniesMinecart;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.mobs.drownedpirates.EntityDrownedArcherPirate;
import com.minecolonies.core.entity.mobs.drownedpirates.EntityDrownedCaptainPirate;
import com.minecolonies.core.entity.mobs.drownedpirates.EntityDrownedPirate;
import com.minecolonies.core.entity.visitor.VisitorCitizen;
import com.minecolonies.core.entity.mobs.EntityMercenary;
import com.minecolonies.core.entity.mobs.amazons.EntityAmazonChief;
import com.minecolonies.core.entity.mobs.amazons.EntityAmazonSpearman;
import com.minecolonies.core.entity.mobs.amazons.EntityArcherAmazon;
import com.minecolonies.core.entity.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.core.entity.mobs.barbarians.EntityBarbarian;
import com.minecolonies.core.entity.mobs.barbarians.EntityChiefBarbarian;
import com.minecolonies.core.entity.mobs.egyptians.EntityArcherMummy;
import com.minecolonies.core.entity.mobs.egyptians.EntityMummy;
import com.minecolonies.core.entity.mobs.egyptians.EntityPharao;
import com.minecolonies.core.entity.mobs.norsemen.EntityNorsemenArcher;
import com.minecolonies.core.entity.mobs.norsemen.EntityNorsemenChief;
import com.minecolonies.core.entity.mobs.norsemen.EntityShieldmaiden;
import com.minecolonies.core.entity.mobs.pirates.EntityArcherPirate;
import com.minecolonies.core.entity.mobs.pirates.EntityCaptainPirate;
import com.minecolonies.core.entity.mobs.pirates.EntityPirate;
import com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType;
import com.minecolonies.core.entity.visitor.RegularVisitorType;
import com.minecolonies.core.entity.other.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_HEIGHT;
import static com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_WIDTH;
import static com.minecolonies.api.util.constant.Constants.*;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityInitializer
{
    public static void setupEntities(RegisterEvent event)
    {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.ENTITY_TYPES))
        {
            final @Nullable IForgeRegistry<EntityType<?>> registry = event.getForgeRegistry();

            ModEntities.CITIZEN = build(registry, "citizen",
              EntityType.Builder.of(EntityCitizen::new, MobCategory.CREATURE)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                .setShouldReceiveVelocityUpdates(true));

            ModEntities.FISHHOOK = build(registry, "fishhook",
              EntityType.Builder.<NewBobberEntity>of(NewBobberEntity::new, MobCategory.MISC)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
                .sized(0.25F, 0.25F)
                .setShouldReceiveVelocityUpdates(true)
                .setCustomClientFactory(NewBobberEntity::new));

            ModEntities.VISITOR = build(registry, "visitor",
              EntityType.Builder.of(VisitorCitizen.forVisitorType(new RegularVisitorType()), MobCategory.CREATURE)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                .setShouldReceiveVelocityUpdates(true));

            ModEntities.EXPEDITIONARY = build(registry, "expeditionary",
              EntityType.Builder.of(VisitorCitizen.forVisitorType(new ExpeditionaryVisitorType()), MobCategory.CREATURE)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT)
                .setShouldReceiveVelocityUpdates(true));

            ModEntities.MERCENARY = build(registry, "mercenary",
              EntityType.Builder.of(EntityMercenary::new, MobCategory.CREATURE)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.BARBARIAN = build(registry, "barbarian",
              EntityType.Builder.of(EntityBarbarian::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.ARCHERBARBARIAN = build(registry, "archerbarbarian",
              EntityType.Builder.of(EntityArcherBarbarian::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CHIEFBARBARIAN = build(registry, "chiefbarbarian",
              EntityType.Builder.of(EntityChiefBarbarian::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.PIRATE = build(registry, "pirate",
              EntityType.Builder.of(EntityPirate::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.ARCHERPIRATE = build(registry, "archerpirate",
              EntityType.Builder.of(EntityArcherPirate::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CHIEFPIRATE = build(registry, "chiefpirate",
              EntityType.Builder.of(EntityCaptainPirate::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.SITTINGENTITY = build(registry, "sittingentity",
              EntityType.Builder.<SittingEntity>of(SittingEntity::new, MobCategory.MISC)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized(0F, 0.5F));

            ModEntities.MINECART = build(registry, "mcminecart",
              EntityType.Builder.of(MinecoloniesMinecart::new, MobCategory.MISC)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized(0.98F, 0.7F));

            ModEntities.MUMMY = build(registry, "mummy",
              EntityType.Builder.of(EntityMummy::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.ARCHERMUMMY = build(registry, "archermummy",
              EntityType.Builder.of(EntityArcherMummy::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.PHARAO = build(registry, "pharao",
              EntityType.Builder.of(EntityPharao::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.AMAZON = build(registry, "amazon",
              EntityType.Builder.of(EntityArcherAmazon::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.AMAZONSPEARMAN = build(registry, "amazonspearman",
              EntityType.Builder.of(EntityAmazonSpearman::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.AMAZONCHIEF = build(registry, "amazonchief",
              EntityType.Builder.of(EntityAmazonChief::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.FIREARROW = build(registry, "firearrow",
              EntityType.Builder.of(FireArrowEntity::new, MobCategory.MISC)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
                .sized(0.5F, 0.5F)
                .setShouldReceiveVelocityUpdates(true));

            ModEntities.MC_NORMAL_ARROW = build(registry, "mcnormalarrow",
              EntityType.Builder.of(CustomArrowEntity::new, MobCategory.MISC)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
                .sized(0.5F, 0.5F)
                .setShouldReceiveVelocityUpdates(true));

            ModEntities.DRUID_POTION = build(registry, "druidpotion",
              EntityType.Builder.<DruidPotionEntity>of(DruidPotionEntity::new, MobCategory.MISC)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
                .sized(0.25F, 0.25F)
                .setShouldReceiveVelocityUpdates(true));

            ModEntities.SHIELDMAIDEN = build(registry, "shieldmaiden",
              EntityType.Builder.of(EntityShieldmaiden::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.NORSEMEN_ARCHER = build(registry, "norsemenarcher",
              EntityType.Builder.of(EntityNorsemenArcher::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.NORSEMEN_CHIEF = build(registry, "norsemenchief",
              EntityType.Builder.of(EntityNorsemenChief::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.SPEAR = build(registry, "spear",
              EntityType.Builder.<SpearEntity>of(SpearEntity::new, MobCategory.MISC)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY_FISHHOOK)
                .sized(0.5F, 0.5F)
                .setShouldReceiveVelocityUpdates(true));

            ModEntities.DROWNED_PIRATE = build(registry, "drownedpirate",
              EntityType.Builder.of(EntityDrownedPirate::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.DROWNED_ARCHERPIRATE = build(registry, "drownedarcherpirate",
              EntityType.Builder.of(EntityDrownedArcherPirate::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.DROWNED_CHIEFPIRATE = build(registry, "drownedchiefpirate",
              EntityType.Builder.of(EntityDrownedCaptainPirate::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));
        }
    }

    private static <T extends Entity> EntityType<T> build(IForgeRegistry<EntityType<?>> registry, final String key, final EntityType.Builder<T> builder)
    {
        EntityType<T> entity = builder.build(Constants.MOD_ID + ":" + key);
        registry.register(new ResourceLocation(Constants.MOD_ID + ":" + key), entity);
        return entity;
    }

    @SubscribeEvent
    public static void registerEntities(final RegisterEvent event)
    {
        setupEntities(event);
    }
}
