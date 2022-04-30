package com.minecolonies.api.loot;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.function.Supplier;

public class ModLootContextProviders
{

    private static final    Object       playerDamageSourceSync = new Object();
    private static volatile DamageSource playerDamageSource;
    private ModLootContextProviders()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModLootContextProviders. This is a utility class");
    }

    /**
     * Returns a supplier which can provide a simple loot context for drop handling. Includes all the required information on the citizen that is retrieving the drop.
     *
     * @return The provider for the loot context.
     */
    public static Supplier<LootContext> getCitizenLootContext(AbstractEntityCitizen citizen)
    {
        return getPotentiallyKillingCitizenLootContext(citizen, false);
    }

    /**
     * Returns a supplier which can provide a simple loot context for drop handling. Includes all the required information on the citizen that is retrieving the drop.
     *
     * @param includeCitizenAsKiller True if the citizen should be included as the killer.
     * @return The provider for the loot context.
     */
    public static Supplier<LootContext> getPotentiallyKillingCitizenLootContext(AbstractEntityCitizen citizen, boolean includeCitizenAsKiller)
    {
        return () -> {
            //We are potentially in a multi-threaded environment, so we need to synchronize.
            if (playerDamageSource == null)
            {
                synchronized (playerDamageSourceSync)
                {
                    //Double null check, it might have been filled while we waited for the mutex lock to allow us in this critical section.
                    if (playerDamageSource == null)
                    {
                        FakePlayer fp = FakePlayerFactory.getMinecraft((ServerLevel) citizen.getLevel());
                        playerDamageSource = DamageSource.playerAttack(fp);
                    }
                }
            }

            LootContext.Builder builder = (new LootContext.Builder((ServerLevel) citizen.getLevel()))
              .withParameter(LootContextParams.ORIGIN, citizen.position())
              .withParameter(LootContextParams.THIS_ENTITY, citizen)
              .withParameter(LootContextParams.TOOL, citizen.getMainHandItem())
              .withParameter(ModLootContextParams.CITIZEN_PRIMARY_SKILL, citizen.getPrimarySkillLevel())
              .withParameter(ModLootContextParams.CITIZEN_SECONDARY_SKILL, citizen.getSecondarySkillLevel())
              .withRandom(citizen.getRandom())
              .withLuck(citizen.getLuckiness());

            if (includeCitizenAsKiller)
            {
                builder = builder
                  .withParameter(LootContextParams.DAMAGE_SOURCE, playerDamageSource)
                  .withOptionalParameter(LootContextParams.KILLER_ENTITY, playerDamageSource.getEntity())
                  .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, playerDamageSource.getDirectEntity());
            }

            return builder.create(ModLootContextParamSets.CITIZEN_PERFORMS_LOOTING);
        };
    }
}
