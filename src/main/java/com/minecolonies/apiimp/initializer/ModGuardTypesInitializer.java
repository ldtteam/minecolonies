package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.coremod.colony.jobs.JobKnight;
import com.minecolonies.coremod.colony.jobs.JobRanger;
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
                                 .setJobTranslationKey("com.minecolonies.coremod.job.Knight")
                                 .setButtonTranslationKey("com.minecolonies.coremod.gui.workerHuts.knight")
                                 .setPrimarySkill(IBuildingWorker.Skill.STRENGTH)
                                 .setSecondarySkill(IBuildingWorker.Skill.ENDURANCE)
                                 .setWorkerSoundName("archer")
                                 .setGuardJobProducer(JobKnight::new)
                                 .setRegistryName(ModGuardTypes.KNIGHT_ID)
                                 .createGuardType();

        ModGuardTypes.ranger = new GuardType.Builder()
                                 .setJobTranslationKey("com.minecolonies.coremod.job.Ranger")
                                 .setButtonTranslationKey("com.minecolonies.coremod.gui.workerHuts.ranger")
                                 .setPrimarySkill(IBuildingWorker.Skill.DEXTERITY)
                                 .setSecondarySkill(IBuildingWorker.Skill.STRENGTH)
                                 .setWorkerSoundName("archer")
                                 .setGuardJobProducer(JobRanger::new)
                                 .setRegistryName(ModGuardTypes.RANGER_ID)
                                 .createGuardType();

        reg.register(ModGuardTypes.knight);
        reg.register(ModGuardTypes.ranger);
    }
}
