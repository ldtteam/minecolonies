package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.DamageSourceKeys;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.JsonCodecProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class DefaultDamageTypeProvider extends JsonCodecProvider<DamageType> {
    public DefaultDamageTypeProvider(@NotNull final PackOutput packOutput,
                                     final CompletableFuture<Provider> lookupProvider) {
        super(packOutput, Target.DATA_PACK, "damage_type", DamageType.DIRECT_CODEC, lookupProvider, MOD_ID);
    }

    @Override
    protected void gather() {
        unconditional(DamageSourceKeys.CONSOLE.identifier(), damage("console"));
        unconditional(DamageSourceKeys.DEFAULT.identifier(), damage("default"));
        unconditional(DamageSourceKeys.DESPAWN.identifier(), damage("despawn"));
        unconditional(DamageSourceKeys.NETHER.identifier(), damage("nether"));
        unconditional(DamageSourceKeys.GUARD.identifier(), damage("entity.minecolonies.guard"));
        unconditional(DamageSourceKeys.GUARD_PVP.identifier(), damage("entity.minecolonies.guardpvp"));
        unconditional(DamageSourceKeys.SLAP.identifier(), damage("entity.minecolonies.slap"));
        unconditional(DamageSourceKeys.STUCK_DAMAGE.identifier(), damage("entity.minecolonies.stuckdamage"));
        unconditional(DamageSourceKeys.TRAINING.identifier(), damage("entity.minecolonies.training"));
        unconditional(DamageSourceKeys.WAKEY.identifier(), damage("entity.minecolonies.wakeywakey"));
        unconditional(DamageSourceKeys.PIERCE.identifier(), damage("entity.minecolonies.pierce"));

        unconditional(DamageSourceKeys.AMAZON.identifier(), entityDamage(ModEntities.AMAZON));
        unconditional(DamageSourceKeys.AMAZONCHIEF.identifier(), entityDamage(ModEntities.AMAZONCHIEF));
        unconditional(DamageSourceKeys.AMAZONSPEARMAN.identifier(), entityDamage(ModEntities.AMAZONSPEARMAN));
        unconditional(DamageSourceKeys.ARCHERBARBARIAN.identifier(), entityDamage(ModEntities.ARCHERBARBARIAN));
        unconditional(DamageSourceKeys.ARCHERMUMMY.identifier(), entityDamage(ModEntities.ARCHERMUMMY));
        unconditional(DamageSourceKeys.ARCHERPIRATE.identifier(), entityDamage(ModEntities.ARCHERPIRATE));
        unconditional(DamageSourceKeys.BARBARIAN.identifier(), entityDamage(ModEntities.BARBARIAN));
        unconditional(DamageSourceKeys.CHIEFBARBARIAN.identifier(), entityDamage(ModEntities.CHIEFBARBARIAN));
        unconditional(DamageSourceKeys.CHIEFPIRATE.identifier(), entityDamage(ModEntities.CHIEFPIRATE));
        unconditional(DamageSourceKeys.MERCENARY.identifier(), entityDamage(ModEntities.MERCENARY));
        unconditional(DamageSourceKeys.MUMMY.identifier(), entityDamage(ModEntities.MUMMY));
        unconditional(DamageSourceKeys.NORSEMENARCHER.identifier(), entityDamage(ModEntities.NORSEMEN_ARCHER));
        unconditional(DamageSourceKeys.NORSEMENCHIEF.identifier(), entityDamage(ModEntities.NORSEMEN_CHIEF));
        unconditional(DamageSourceKeys.PHARAO.identifier(), entityDamage(ModEntities.PHARAO));
        unconditional(DamageSourceKeys.PIRATE.identifier(), entityDamage(ModEntities.PIRATE));
        unconditional(DamageSourceKeys.SHIELDMAIDEN.identifier(), entityDamage(ModEntities.SHIELDMAIDEN));
        unconditional(DamageSourceKeys.SPEAR.identifier(), entityDamage(ModEntities.SPEAR));
        unconditional(DamageSourceKeys.VISITOR.identifier(), entityDamage(ModEntities.VISITOR));
        unconditional(DamageSourceKeys.DROWNED_PIRATE.identifier(), entityDamage(ModEntities.DROWNED_PIRATE));
        unconditional(DamageSourceKeys.DROWNED_ARCHERPIRATE.identifier(), entityDamage(ModEntities.DROWNED_ARCHERPIRATE));
        unconditional(DamageSourceKeys.DROWNED_CHIEFPIRATE.identifier(), entityDamage(ModEntities.DROWNED_CHIEFPIRATE));

        unconditional(DamageSourceKeys.CAMP_AMAZON.identifier(), entityDamage(ModEntities.CAMP_AMAZON));
        unconditional(DamageSourceKeys.CAMP_AMAZONCHIEF.identifier(), entityDamage(ModEntities.CAMP_AMAZONCHIEF));
        unconditional(DamageSourceKeys.CAMP_AMAZONSPEARMAN.identifier(), entityDamage(ModEntities.CAMP_AMAZONSPEARMAN));

        unconditional(DamageSourceKeys.CAMP_BARBARIAN.identifier(), entityDamage(ModEntities.CAMP_BARBARIAN));
        unconditional(DamageSourceKeys.CAMP_CHIEFBARBARIAN.identifier(), entityDamage(ModEntities.CAMP_CHIEFBARBARIAN));
        unconditional(DamageSourceKeys.CAMP_ARCHERBARBARIAN.identifier(), entityDamage(ModEntities.CAMP_ARCHERBARBARIAN));

        unconditional(DamageSourceKeys.CAMP_MUMMY.identifier(), entityDamage(ModEntities.CAMP_MUMMY));
        unconditional(DamageSourceKeys.CAMP_ARCHERMUMMY.identifier(), entityDamage(ModEntities.CAMP_ARCHERMUMMY));
        unconditional(DamageSourceKeys.CAMP_PHARAO.identifier(), entityDamage(ModEntities.CAMP_PHARAO));

        unconditional(DamageSourceKeys.CAMP_PIRATE.identifier(), entityDamage(ModEntities.CAMP_PIRATE));
        unconditional(DamageSourceKeys.CAMP_ARCHERPIRATE.identifier(), entityDamage(ModEntities.CAMP_ARCHERPIRATE));
        unconditional(DamageSourceKeys.CAMP_CHIEFPIRATE.identifier(), entityDamage(ModEntities.CAMP_CHIEFPIRATE));

        unconditional(DamageSourceKeys.CAMP_NORSEMENARCHER.identifier(), entityDamage(ModEntities.CAMP_NORSEMEN_ARCHER));
        unconditional(DamageSourceKeys.CAMP_NORSEMENCHIEF.identifier(), entityDamage(ModEntities.CAMP_NORSEMEN_CHIEF));
        unconditional(DamageSourceKeys.CAMP_SHIELDMAIDEN.identifier(), entityDamage(ModEntities.CAMP_SHIELDMAIDEN));
    }

    @NotNull
    private static DamageType entityDamage(@NotNull final EntityType<?> entityType) {
        return damage(entityType.getDescriptionId());
    }

    @NotNull
    private static DamageType damage(@NotNull final String msgId) {
        return new DamageType(msgId, DamageScaling.ALWAYS, 0.1F);
    }
}
