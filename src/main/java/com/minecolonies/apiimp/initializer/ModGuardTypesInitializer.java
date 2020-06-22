package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.colony.jobs.JobKnight;
import com.minecolonies.coremod.colony.jobs.JobRanger;
import com.minecolonies.coremod.colony.jobs.JobWitch;
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
                                 .setButtonTranslationKey("com.minecolonies.coremod.gui.workerhuts.knight")
                                 .setPrimarySkill(Skill.Adaptability)
                                 .setSecondarySkill(Skill.Stamina)
                                 .setWorkerSoundName("archer")
                                 .setGuardJobProducer(JobKnight::new)
                                 .setRegistryName(ModGuardTypes.KNIGHT_ID)
                                 .createGuardType();

        ModGuardTypes.ranger = new GuardType.Builder()
                                 .setJobTranslationKey("com.minecolonies.coremod.job.Ranger")
                                 .setButtonTranslationKey("com.minecolonies.coremod.gui.workerhuts.ranger")
                                 .setPrimarySkill(Skill.Agility)
                                 .setSecondarySkill(Skill.Adaptability)
                                 .setWorkerSoundName("archer")
                                 .setGuardJobProducer(JobRanger::new)
                                 .setRegistryName(ModGuardTypes.RANGER_ID)
                                 .createGuardType();

        ModGuardTypes.witch = new GuardType.Builder()
                                .setJobTranslationKey("com.minecolonies.coremod.job.Witch")
                                .setButtonTranslationKey("com.minecolonies.coremod.gui.workerhuts.witch")
                                .setPrimarySkill(Skill.Mana)//TODO
                                .setSecondarySkill(Skill.Intelligence)//TODO
                                .setWorkerSoundName("witch")
                                .setGuardJobProducer(JobWitch::new)
                                .setRegistryName(ModGuardTypes.WITCH_ID)
                                .createGuardType();

        reg.register(ModGuardTypes.knight);
        reg.register(ModGuardTypes.ranger);
        reg.register(ModGuardTypes.witch);
    }
}
