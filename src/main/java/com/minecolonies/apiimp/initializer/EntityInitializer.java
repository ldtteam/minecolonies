package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.entity.other.MinecoloniesMinecart;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.mobs.camp.amazons.EntityAmazonChief;
import com.minecolonies.core.entity.mobs.camp.amazons.EntityAmazonSpearman;
import com.minecolonies.core.entity.mobs.camp.amazons.EntityArcherAmazon;
import com.minecolonies.core.entity.mobs.camp.barbarians.EntityArcherBarbarian;
import com.minecolonies.core.entity.mobs.camp.barbarians.EntityBarbarian;
import com.minecolonies.core.entity.mobs.camp.barbarians.EntityChiefBarbarian;
import com.minecolonies.core.entity.mobs.camp.drownedpirates.EntityDrownedArcherPirate;
import com.minecolonies.core.entity.mobs.camp.drownedpirates.EntityDrownedCaptainPirate;
import com.minecolonies.core.entity.mobs.camp.drownedpirates.EntityDrownedPirate;
import com.minecolonies.core.entity.mobs.camp.egyptians.EntityArcherMummy;
import com.minecolonies.core.entity.mobs.camp.egyptians.EntityMummy;
import com.minecolonies.core.entity.mobs.camp.egyptians.EntityPharao;
import com.minecolonies.core.entity.mobs.camp.norsemen.EntityNorsemenArcher;
import com.minecolonies.core.entity.mobs.camp.norsemen.EntityNorsemenChief;
import com.minecolonies.core.entity.mobs.camp.norsemen.EntityShieldmaiden;
import com.minecolonies.core.entity.mobs.camp.pirates.EntityArcherPirate;
import com.minecolonies.core.entity.mobs.camp.pirates.EntityCaptainPirate;
import com.minecolonies.core.entity.mobs.camp.pirates.EntityPirate;
import com.minecolonies.core.entity.mobs.raider.drownedpirates.EntityDrownedArcherPirateRaider;
import com.minecolonies.core.entity.mobs.raider.drownedpirates.EntityDrownedCaptainPirateRaider;
import com.minecolonies.core.entity.mobs.raider.drownedpirates.EntityDrownedPirateRaider;
import com.minecolonies.core.entity.visitor.VisitorCitizen;
import com.minecolonies.core.entity.mobs.EntityMercenary;
import com.minecolonies.core.entity.mobs.raider.amazons.EntityAmazonChiefRaider;
import com.minecolonies.core.entity.mobs.raider.amazons.EntityAmazonSpearmanRaider;
import com.minecolonies.core.entity.mobs.raider.amazons.EntityArcherAmazonRaider;
import com.minecolonies.core.entity.mobs.raider.barbarians.EntityArcherBarbarianRaider;
import com.minecolonies.core.entity.mobs.raider.barbarians.EntityBarbarianRaider;
import com.minecolonies.core.entity.mobs.raider.barbarians.EntityChiefBarbarianRaider;
import com.minecolonies.core.entity.mobs.raider.egyptians.EntityArcherMummyRaider;
import com.minecolonies.core.entity.mobs.raider.egyptians.EntityMummyRaider;
import com.minecolonies.core.entity.mobs.raider.egyptians.EntityPharaoRaider;
import com.minecolonies.core.entity.mobs.raider.norsemen.EntityNorsemenArcherRaider;
import com.minecolonies.core.entity.mobs.raider.norsemen.EntityNorsemenChiefRaider;
import com.minecolonies.core.entity.mobs.raider.norsemen.EntityShieldmaidenRaider;
import com.minecolonies.core.entity.mobs.raider.pirates.EntityArcherPirateRaider;
import com.minecolonies.core.entity.mobs.raider.pirates.EntityCaptainPirateRaider;
import com.minecolonies.core.entity.mobs.raider.pirates.EntityPirateRaider;
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

            ModEntities.VISITOR = build(registry, "visitor", EntityType.Builder.of(VisitorCitizen::new, MobCategory.CREATURE)
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
              EntityType.Builder.of(EntityBarbarianRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.ARCHERBARBARIAN = build(registry, "archerbarbarian",
              EntityType.Builder.of(EntityArcherBarbarianRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CHIEFBARBARIAN = build(registry, "chiefbarbarian",
              EntityType.Builder.of(EntityChiefBarbarianRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.PIRATE = build(registry, "pirate",
              EntityType.Builder.of(EntityPirateRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.ARCHERPIRATE = build(registry, "archerpirate",
              EntityType.Builder.of(EntityArcherPirateRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CHIEFPIRATE = build(registry, "chiefpirate",
              EntityType.Builder.of(EntityCaptainPirateRaider::new, MobCategory.MONSTER)
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
              EntityType.Builder.of(EntityMummyRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.ARCHERMUMMY = build(registry, "archermummy",
              EntityType.Builder.of(EntityArcherMummyRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.PHARAO = build(registry, "pharao",
              EntityType.Builder.of(EntityPharaoRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.AMAZON = build(registry, "amazon",
              EntityType.Builder.of(EntityArcherAmazonRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.AMAZONSPEARMAN = build(registry, "amazonspearman",
              EntityType.Builder.of(EntityAmazonSpearmanRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.AMAZONCHIEF = build(registry, "amazonchief",
              EntityType.Builder.of(EntityAmazonChiefRaider::new, MobCategory.MONSTER)
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
              EntityType.Builder.of(EntityShieldmaidenRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.NORSEMEN_ARCHER = build(registry, "norsemenarcher",
              EntityType.Builder.of(EntityNorsemenArcherRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.NORSEMEN_CHIEF = build(registry, "norsemenchief",
              EntityType.Builder.of(EntityNorsemenChiefRaider::new, MobCategory.MONSTER)
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
              EntityType.Builder.of(EntityDrownedPirateRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.DROWNED_ARCHERPIRATE = build(registry, "drownedarcherpirate",
              EntityType.Builder.of(EntityDrownedArcherPirateRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.DROWNED_CHIEFPIRATE = build(registry, "drownedchiefpirate",
              EntityType.Builder.of(EntityDrownedCaptainPirateRaider::new, MobCategory.MONSTER)
                .setTrackingRange(ENTITY_TRACKING_RANGE)
                .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            // Camp Raiders

            ModEntities.CAMP_BARBARIAN = build(registry, "campbarbarian",
                    EntityType.Builder.of(EntityBarbarian::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_ARCHERBARBARIAN = build(registry, "camparcherbarbarian",
                    EntityType.Builder.of(EntityArcherBarbarian::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_CHIEFBARBARIAN = build(registry, "campchiefbarbarian",
                    EntityType.Builder.of(EntityChiefBarbarian::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_PIRATE = build(registry, "camppirate",
                    EntityType.Builder.of(EntityPirate::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_ARCHERPIRATE = build(registry, "camparcherpirate",
                    EntityType.Builder.of(EntityArcherPirate::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_CHIEFPIRATE = build(registry, "campchiefpirate",
                    EntityType.Builder.of(EntityCaptainPirate::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_AMAZON = build(registry, "campamazon",
                    EntityType.Builder.of(EntityArcherAmazon::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_AMAZONSPEARMAN = build(registry, "campamazonspearman",
                    EntityType.Builder.of(EntityAmazonSpearman::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_AMAZONCHIEF = build(registry, "campamazonchief",
                    EntityType.Builder.of(EntityAmazonChief::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_MUMMY = build(registry, "campmummy",
                    EntityType.Builder.of(EntityMummy::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_ARCHERMUMMY = build(registry, "camparchermummy",
                    EntityType.Builder.of(EntityArcherMummy::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_PHARAO = build(registry, "camppharao",
                    EntityType.Builder.of(EntityPharao::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_SHIELDMAIDEN = build(registry, "campshieldmaiden",
                    EntityType.Builder.of(EntityShieldmaiden::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_NORSEMEN_ARCHER = build(registry, "campnorsemenarcher",
                    EntityType.Builder.of(EntityNorsemenArcher::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_NORSEMEN_CHIEF = build(registry, "campnorsemenchief",
                    EntityType.Builder.of(EntityNorsemenChief::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_DROWNED_PIRATE = build(registry, "campdrownedpirate",
                    EntityType.Builder.of(EntityDrownedPirate::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_DROWNED_ARCHERPIRATE = build(registry, "campdrownedarcherpirate",
                    EntityType.Builder.of(EntityDrownedArcherPirate::new, MobCategory.MONSTER)
                            .setTrackingRange(ENTITY_TRACKING_RANGE)
                            .setUpdateInterval(ENTITY_UPDATE_FREQUENCY)
                            .sized((float) CITIZEN_WIDTH, (float) CITIZEN_HEIGHT));

            ModEntities.CAMP_DROWNED_CHIEFPIRATE = build(registry, "campdrownedchiefpirate",
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
