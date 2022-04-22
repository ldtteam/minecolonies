package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.colony.jobs.JobKnight;
import com.minecolonies.coremod.colony.jobs.JobRanger;
import com.minecolonies.coremod.colony.jobs.JobDruid;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

import static com.minecolonies.api.util.constant.translation.JobTranslationConstants.*;

public final class ModGuardTypesInitializer
{

    private ModGuardTypesInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModGuardTypesInitializer but this is a Utility class.");
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final RegistryEvent.Register<GuardType> event)
    {
        final IForgeRegistry<GuardType> reg = event.getRegistry();

        ModGuardTypes.knight = new GuardType.Builder()
                                 .setJobTranslationKey(JOB_KNIGHT)
                                 .setButtonTranslationKey(JOB_KNIGHT_BUTTON)
                                 .setPrimarySkill(Skill.Adaptability)
                                 .setSecondarySkill(Skill.Stamina)
                                 .setWorkerSoundName("archer")
                                 .setJobEntry(() -> ModJobs.knight)
                                 .setRegistryName(ModGuardTypes.KNIGHT_ID)
                                 .setClazz(JobKnight.class)
                                 .createGuardType();

        ModGuardTypes.ranger = new GuardType.Builder()
                                 .setJobTranslationKey(JOB_RANGER)
                                 .setButtonTranslationKey(JOB_RANGER_BUTTON)
                                 .setPrimarySkill(Skill.Agility)
                                 .setSecondarySkill(Skill.Adaptability)
                                 .setWorkerSoundName("archer")
                                 .setJobEntry(() -> ModJobs.ranger)
                                 .setRegistryName(ModGuardTypes.RANGER_ID)
                                 .setClazz(JobRanger.class)
                                 .createGuardType();

        ModGuardTypes.druid = new GuardType.Builder()
          .setJobTranslationKey(JOB_DRUID)
          .setButtonTranslationKey(JOB_DRUID_BUTTON)
          .setPrimarySkill(Skill.Mana)
          .setSecondarySkill(Skill.Focus)
          .setWorkerSoundName("druid")
          .setJobEntry(() -> ModJobs.druid)
          .setRegistryName(ModGuardTypes.DRUID_ID)
          .setClazz(JobDruid.class)
          .createGuardType();

        reg.register(ModGuardTypes.knight);
        reg.register(ModGuardTypes.ranger);
        reg.register(ModGuardTypes.druid);
    }
}
