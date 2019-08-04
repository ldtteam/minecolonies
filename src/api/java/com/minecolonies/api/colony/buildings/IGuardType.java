package com.minecolonies.api.colony.buildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import net.minecraft.util.ResourceLocation;

public interface IGuardType
{
    IJob getGuardJob(ICitizenData citizen);

    String getJobName();

    String getButtonName();

    ResourceLocation getRegistryName();

    IBuildingWorker.Skill getPrimarySkill();

    IBuildingWorker.Skill getSecondarySkill();

    String getWorkerSoundName();
}
