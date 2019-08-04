package com.minecolonies.coremod.colony.jobs.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.JobConstants;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.coremod.colony.jobs.*;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

/**
 * Class taking care of registering the jobs.
 */
public class JobRegistry implements IJobRegistry
{
    /**
     * Map to resolve names to class.
     */
    @NotNull
    private final BiMap<String, Function<ICitizenData, IJob<?>>> nameToConstructorMap = HashBiMap.create();
    @NotNull
    private final BiMap<Class<?>, String>                        classToNameMap       = HashBiMap.create();

    public JobRegistry()
    {
        registerNewJobMapping(JobConstants.MAPPING_PLACEHOLDER, JobPlaceholder.class, JobPlaceholder::new);
        registerNewJobMapping(JobConstants.MAPPING_BUILDER, JobBuilder.class, JobBuilder::new);
        registerNewJobMapping(JobConstants.MAPPING_DELIVERY, JobDeliveryman.class, JobDeliveryman::new);
        registerNewJobMapping(JobConstants.MAPPING_MINER, JobMiner.class, JobMiner::new);
        registerNewJobMapping(JobConstants.MAPPING_LUMBERJACK, JobLumberjack.class, JobLumberjack::new);
        registerNewJobMapping(JobConstants.MAPPING_FARMER, JobFarmer.class, JobFarmer::new);
        registerNewJobMapping(JobConstants.MAPPING_FISHERMAN, JobFisherman.class, JobFisherman::new);
        registerNewJobMapping(JobConstants.MAPPING_BAKER, JobBaker.class, JobBaker::new);
        registerNewJobMapping(JobConstants.MAPPING_COOK, JobCook.class, JobCook::new);
        registerNewJobMapping(JobConstants.MAPPING_SHEPHERD, JobShepherd.class, JobShepherd::new);
        registerNewJobMapping(JobConstants.MAPPING_COWBOY, JobCowboy.class, JobCowboy::new);
        registerNewJobMapping(JobConstants.MAPPING_SWINE_HERDER, JobSwineHerder.class, JobSwineHerder::new);
        registerNewJobMapping(JobConstants.MAPPING_CHICKEN_HERDER, JobChickenHerder.class, JobChickenHerder::new);
        registerNewJobMapping(JobConstants.MAPPING_SMELTER, JobSmelter.class, JobSmelter::new);
        registerNewJobMapping(JobConstants.MAPPING_RANGER, JobRanger.class, JobRanger::new);
        registerNewJobMapping(JobConstants.MAPPING_KNIGHT, JobKnight.class, JobKnight::new);
        registerNewJobMapping(JobConstants.MAPPING_COMPOSTER, JobComposter.class, JobComposter::new);
        registerNewJobMapping(JobConstants.MAPPING_STUDENT, JobStudent.class, JobStudent::new);
        registerNewJobMapping(JobConstants.MAPPING_ARCHER, JobArcherTraining.class, JobArcherTraining::new);
        registerNewJobMapping(JobConstants.MAPPING_COMBAT, JobCombatTraining.class, JobCombatTraining::new);
        registerNewJobMapping(JobConstants.MAPPING_SAWMILL, JobSawmill.class, JobSawmill::new);
        registerNewJobMapping(JobConstants.MAPPING_BLACKSMITH, JobBlacksmith.class, JobBlacksmith::new);
        registerNewJobMapping(JobConstants.MAPPING_STONEMASON, JobStonemason.class, JobStonemason::new);
        registerNewJobMapping(JobConstants.MAPPING_STONE_SMELTERY, JobStoneSmeltery.class, JobStoneSmeltery::new);
        registerNewJobMapping(JobConstants.MAPPING_CRUSHER, JobCrusher.class, JobCrusher::new);
        registerNewJobMapping(JobConstants.MAPPING_SIFTER, JobSifter.class, JobSifter::new);
    }

    /**
     * Add a given Job mapping.
     *
     * @param name     name of job class.
     * @param jobFunction class of job.
     */
    @Override
    public void registerNewJobMapping(final String name, @NotNull final Class<?> jobClass, @NotNull final Function<ICitizenData, IJob<?>> jobFunction)
    {
        if (nameToConstructorMap.containsKey(name))
        {
            throw new IllegalArgumentException("Duplicate type '" + name + "' when adding Job class mapping");
        }
        nameToConstructorMap.put(name, jobFunction);
        classToNameMap.put(jobClass, name);
    }

    /**
     * Create a Job from saved NBTTagCompound data.
     *
     * @param citizen  The citizen that owns the Job.
     * @param compound The NBTTagCompound containing the saved Job data.
     * @return New Job created from the data, or null.
     */
    @Override
    @Nullable
    public IJob createFromNBT(final ICitizenData citizen, @NotNull final NBTTagCompound compound)
    {
        final String jobType = compound.hasKey(NbtTagConstants.TAG_JOB_TYPE) ? compound.getString(NbtTagConstants.TAG_JOB_TYPE) : JobConstants.MAPPING_PLACEHOLDER;
        final IJob job = nameToConstructorMap.get(jobType).apply(citizen);

        if (job != null)
        {
            try
            {
                job.deserializeNBT(compound);
            }
            catch (final RuntimeException ex)
            {
                Log.getLogger().error(String.format("A Job %s has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                  jobType), ex);
                return null;
            }
        }
        else
        {
            Log.getLogger().warn(String.format("Unknown Job type '%s' or missing constructor of proper format.", jobType));
        }

        return job;
    }

    @Override
    @NotNull
    public Map<Class<?>, String> getClassToNameMap()
    {
        return classToNameMap;
    }
}
