package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.DamageSourceKeys;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class DefaultDamageTypeProvider extends JsonCodecProvider<DamageType>
{
    public DefaultDamageTypeProvider(@NotNull final PackOutput packOutput,
                                     @NotNull final ExistingFileHelper existingFileHelper)
    {
        super(packOutput, existingFileHelper, MOD_ID, JsonOps.INSTANCE, PackType.SERVER_DATA, "damage_type", DamageType.CODEC, getDamageTypes());
    }

    private static Map<ResourceLocation, DamageType> getDamageTypes()
    {
        return Map.ofEntries(
                Map.entry(DamageSourceKeys.CONSOLE.location(), damage("console")),
                Map.entry(DamageSourceKeys.DEFAULT.location(), damage("default")),
                Map.entry(DamageSourceKeys.DESPAWN.location(), damage("despawn")),
                Map.entry(DamageSourceKeys.NETHER.location(), damage("nether")),

                Map.entry(DamageSourceKeys.GUARD.location(), damage("entity.minecolonies.guard")),
                Map.entry(DamageSourceKeys.GUARD_PVP.location(), damage("entity.minecolonies.guardpvp")),
                Map.entry(DamageSourceKeys.SLAP.location(), damage("entity.minecolonies.slap")),
                Map.entry(DamageSourceKeys.STUCK_DAMAGE.location(), damage("entity.minecolonies.stuckdamage")),
                Map.entry(DamageSourceKeys.TRAINING.location(), damage("entity.minecolonies.training")),
                Map.entry(DamageSourceKeys.WAKEY.location(), damage("entity.minecolonies.wakeywakey")),

                Map.entry(DamageSourceKeys.AMAZON.location(), entityDamage(ModEntities.AMAZON)),
                Map.entry(DamageSourceKeys.AMAZONCHIEF.location(), entityDamage(ModEntities.AMAZONCHIEF)),
                Map.entry(DamageSourceKeys.AMAZONSPEARMAN.location(), entityDamage(ModEntities.AMAZONSPEARMAN)),
                Map.entry(DamageSourceKeys.ARCHERBARBARIAN.location(), entityDamage(ModEntities.ARCHERBARBARIAN)),
                Map.entry(DamageSourceKeys.ARCHERMUMMY.location(), entityDamage(ModEntities.ARCHERMUMMY)),
                Map.entry(DamageSourceKeys.ARCHERPIRATE.location(), entityDamage(ModEntities.ARCHERPIRATE)),
                Map.entry(DamageSourceKeys.BARBARIAN.location(), entityDamage(ModEntities.BARBARIAN)),
                Map.entry(DamageSourceKeys.CHIEFBARBARIAN.location(), entityDamage(ModEntities.CHIEFBARBARIAN)),
                Map.entry(DamageSourceKeys.CHIEFPIRATE.location(), entityDamage(ModEntities.CHIEFPIRATE)),
                Map.entry(DamageSourceKeys.MERCENARY.location(), entityDamage(ModEntities.MERCENARY)),
                Map.entry(DamageSourceKeys.MUMMY.location(), entityDamage(ModEntities.MUMMY)),
                Map.entry(DamageSourceKeys.NORSEMENARCHER.location(), entityDamage(ModEntities.NORSEMEN_ARCHER)),
                Map.entry(DamageSourceKeys.NORSEMENCHIEF.location(), entityDamage(ModEntities.NORSEMEN_CHIEF)),
                Map.entry(DamageSourceKeys.PHARAO.location(), entityDamage(ModEntities.PHARAO)),
                Map.entry(DamageSourceKeys.PIRATE.location(), entityDamage(ModEntities.PIRATE)),
                Map.entry(DamageSourceKeys.SHIELDMAIDEN.location(), entityDamage(ModEntities.SHIELDMAIDEN)),
                Map.entry(DamageSourceKeys.SPEAR.location(), entityDamage(ModEntities.SPEAR)),
                Map.entry(DamageSourceKeys.VISITOR.location(), entityDamage(ModEntities.VISITOR)),
                Map.entry(DamageSourceKeys.DROWNED_PIRATE.location(), entityDamage(ModEntities.DROWNED_PIRATE)),
                Map.entry(DamageSourceKeys.DROWNED_ARCHERPIRATE.location(), entityDamage(ModEntities.DROWNED_ARCHERPIRATE)),
                Map.entry(DamageSourceKeys.DROWNED_CHIEFPIRATE.location(), entityDamage(ModEntities.DROWNED_CHIEFPIRATE))
          );
    }

    @NotNull
    private static DamageType entityDamage(@NotNull final EntityType<?> entityType)
    {
        return damage(entityType.getDescriptionId());
    }

    @NotNull
    private static DamageType damage(@NotNull final String msgId)
    {
        return new DamageType(msgId, DamageScaling.ALWAYS, 0.1F);
    }
}