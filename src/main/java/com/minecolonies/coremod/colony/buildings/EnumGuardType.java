package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.IGuardType;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.jobs.JobKnight;
import com.minecolonies.coremod.colony.jobs.JobRanger;
import net.minecraft.util.ResourceLocation;

public enum EnumGuardType implements IGuardType
{
    RANGER("com.minecolonies.coremod.job.Ranger",
      "com.minecolonies.coremod.gui.workerHuts.ranger",
      new ResourceLocation(Constants.MOD_ID, "ranger"),
      IBuildingWorker.Skill.INTELLIGENCE,
      IBuildingWorker.Skill.STRENGTH,
      "archer"),
    KNIGHT("com.minecolonies.coremod.job.Knight",
      "com.minecolonies.coremod.gui.workerHuts.knight",
      new ResourceLocation(Constants.MOD_ID, "knight"),
      IBuildingWorker.Skill.STRENGTH,
      IBuildingWorker.Skill.ENDURANCE,
      "knight");

    private final String                jobName;
    private final String                buttonName;
    private final ResourceLocation      registryName;
    private final IBuildingWorker.Skill primarySkill;
    private final IBuildingWorker.Skill secondarySkill;
    private final String                workerSoundName;

    EnumGuardType(
      final String name,
      final String buttonName,
      final ResourceLocation registryName,
      final IBuildingWorker.Skill primarySkill, final IBuildingWorker.Skill secondarySkill, final String workerSoundName)
    {
        this.jobName = name;
        this.buttonName = buttonName;
        this.registryName = registryName;
        this.primarySkill = primarySkill;
        this.secondarySkill = secondarySkill;
        this.workerSoundName = workerSoundName;
    }

    @Override
    public IJob getGuardJob(final ICitizenData citizen)
    {
        if (this == RANGER)
        {
            return new JobRanger(citizen);
        }
        else if (this == KNIGHT)
        {
            return new JobKnight(citizen);
        }
        return new JobRanger(citizen);
    }

    @Override
    public String getJobName()
    {
        return jobName;
    }

    @Override
    public String getButtonName()
    {
        return buttonName;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    @Override
    public IBuildingWorker.Skill getPrimarySkill()
    {
        return primarySkill;
    }

    public IBuildingWorker.Skill getSecondarySkill()
    {
        return secondarySkill;
    }

    @Override
    public String getWorkerSoundName()
    {
        return workerSoundName;
    }
}
