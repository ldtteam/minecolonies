package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

import static com.minecolonies.api.util.constant.translation.JobTranslationConstants.*;

public final class ModGuardTypesInitializer
{
    public final static DeferredRegister<GuardType> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "guardtypes"), Constants.MOD_ID);

    private ModGuardTypesInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModGuardTypesInitializer but this is a Utility class.");
    }

    static
    {
        ModGuardTypes.knight = DEFERRED_REGISTER.register(ModGuardTypes.KNIGHT_ID.getPath(), () -> new GuardType.Builder()
                                 .setJobTranslationKey(JOB_KNIGHT)
                                 .setPrimarySkill(Skill.Adaptability)
                                 .setSecondarySkill(Skill.Stamina)
                                 .setJobEntry(() -> ModJobs.knight.get())
                                 .createGuardType());

        ModGuardTypes.ranger = DEFERRED_REGISTER.register(ModGuardTypes.RANGER_ID.getPath(), () -> new GuardType.Builder()
                                 .setJobTranslationKey(JOB_RANGER)
                                 .setPrimarySkill(Skill.Agility)
                                 .setSecondarySkill(Skill.Adaptability)
                                 .setJobEntry(() -> ModJobs.ranger.get())
                                 .createGuardType());

        ModGuardTypes.druid = DEFERRED_REGISTER.register(ModGuardTypes.DRUID_ID.getPath(), () -> new GuardType.Builder()
          .setJobTranslationKey(JOB_DRUID)
          .setPrimarySkill(Skill.Mana)
          .setSecondarySkill(Skill.Focus)
          .setJobEntry(() -> ModJobs.druid.get())
          .createGuardType());
    }
}
