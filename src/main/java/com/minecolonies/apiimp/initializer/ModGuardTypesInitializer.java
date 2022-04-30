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
                                 .setJobTranslationKey("com.minecolonies.job.knight")
                                 .setButtonTranslationKey("com.minecolonies.coremod.gui.workerhuts.knight")
                                 .setWorkerSoundName("archer")
                                 .setJobEntry(() -> ModJobs.knight)
                                 .setRegistryName(ModGuardTypes.KNIGHT_ID)
                                 .setClazz(JobKnight.class)
                                 .createGuardType();

        ModGuardTypes.ranger = new GuardType.Builder()
                                 .setJobTranslationKey("com.minecolonies.job.ranger")
                                 .setButtonTranslationKey("com.minecolonies.coremod.gui.workerhuts.ranger")
                                 .setWorkerSoundName("archer")
                                 .setJobEntry(() -> ModJobs.ranger)
                                 .setRegistryName(ModGuardTypes.RANGER_ID)
                                 .setClazz(JobRanger.class)
                                 .createGuardType();

        ModGuardTypes.druid = new GuardType.Builder()
          .setJobTranslationKey("com.minecolonies.job.druid")
          .setButtonTranslationKey("com.minecolonies.coremod.gui.workerhuts.druid")
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
