package com.minecolonies.coremod.colony.jobs.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.colony.jobs.*;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

/**
 * Class taking care of registering the jobs.
 */
public class JobRegistry
{
    public static final String TAG_TYPE = "type";

    private static final String MAPPING_PLACEHOLDER    = "Placeholder";
    private static final String MAPPING_BUILDER        = "Builder";
    private static final String MAPPING_DELIVERY       = "Deliveryman";
    private static final String MAPPING_MINER          = "Miner";
    private static final String MAPPING_LUMBERJACK     = "Lumberjack";
    private static final String MAPPING_FARMER         = "Farmer";
    private static final String MAPPING_FISHERMAN      = "Fisherman";
    private static final String MAPPING_BAKER          = "Baker";
    private static final String MAPPING_COOK           = "Cook";
    private static final String MAPPING_SHEPHERD       = "Shepherd";
    private static final String MAPPING_COWBOY         = "Cowboy";
    private static final String MAPPING_SWINE_HERDER   = "SwineHerder";
    private static final String MAPPING_CHICKEN_HERDER = "ChickenHerder";
    private static final String MAPPING_SMELTER        = "Smelter";
    private static final String MAPPING_RANGER         = "Ranger";
    private static final String MAPPING_KNIGHT         = "Knight";
    private static final String MAPPING_COMPOSTER      = "Composter";
    private static final String MAPPING_STUDENT        = "Student";
    private static final String MAPPING_ARCHER         = "ArcherTraining";
    private static final String MAPPING_COMBAT         = "CombatTraining";
    private static final String MAPPING_SAWMILL        = "Sawmill";
    private static final String MAPPING_BLACKSMITH     = "Blacksmith";
    private static final String MAPPING_STONEMASON     = "Stonemason";
    private static final String MAPPING_STONE_SMELTERY = "StoneSmeltery";
    private static final String MAPPING_CRUSHER        = "Crusher";
    private static final String MAPPING_SIFTER         = "Sifter";

    /**
     * Map to resolve names to class.
     */
    @NotNull
    private static final BiMap<String, Function<ICitizenData, IJob<?>>> nameToConstructorMap = HashBiMap.create();
    @NotNull
    private static final BiMap<Class<?>, String>        classToNameMap       = HashBiMap.create();

    static
    {
        addMapping(MAPPING_PLACEHOLDER, JobPlaceholder.class, JobPlaceholder::new);
        addMapping(MAPPING_BUILDER, JobBuilder.class, JobBuilder::new);
        addMapping(MAPPING_DELIVERY, JobDeliveryman.class, JobDeliveryman::new);
        addMapping(MAPPING_MINER, JobMiner.class, JobMiner::new);
        addMapping(MAPPING_LUMBERJACK, JobLumberjack.class, JobLumberjack::new);
        addMapping(MAPPING_FARMER, JobFarmer.class, JobFarmer::new);
        addMapping(MAPPING_FISHERMAN, JobFisherman.class, JobFisherman::new);
        addMapping(MAPPING_BAKER, JobBaker.class, JobBaker::new);
        addMapping(MAPPING_COOK, JobCook.class, JobCook::new);
        addMapping(MAPPING_SHEPHERD, JobShepherd.class, JobShepherd::new);
        addMapping(MAPPING_COWBOY, JobCowboy.class, JobCowboy::new);
        addMapping(MAPPING_SWINE_HERDER, JobSwineHerder.class, JobSwineHerder::new);
        addMapping(MAPPING_CHICKEN_HERDER, JobChickenHerder.class, JobChickenHerder::new);
        addMapping(MAPPING_SMELTER, JobSmelter.class, JobSmelter::new);
        addMapping(MAPPING_RANGER, JobRanger.class, JobRanger::new);
        addMapping(MAPPING_KNIGHT, JobKnight.class, JobKnight::new);
        addMapping(MAPPING_COMPOSTER, JobComposter.class, JobComposter::new);
        addMapping(MAPPING_STUDENT, JobStudent.class, JobStudent::new);
        addMapping(MAPPING_ARCHER, JobArcherTraining.class, JobArcherTraining::new);
        addMapping(MAPPING_COMBAT, JobCombatTraining.class, JobCombatTraining::new);
        addMapping(MAPPING_SAWMILL, JobSawmill.class, JobSawmill::new);
        addMapping(MAPPING_BLACKSMITH, JobBlacksmith.class, JobBlacksmith::new);
        addMapping(MAPPING_STONEMASON, JobStonemason.class, JobStonemason::new);
        addMapping(MAPPING_STONE_SMELTERY, JobStoneSmeltery.class, JobStoneSmeltery::new);
        addMapping(MAPPING_CRUSHER, JobCrusher.class, JobCrusher::new);
        addMapping(MAPPING_SIFTER, JobSifter.class, JobSifter::new);
    }

    /**
     * Private constructor to hide implicit public one.
     */
    private JobRegistry()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Add a given Job mapping.
     *
     * @param name     name of job class.
     * @param jobFunction class of job.
     */
    private static void addMapping(final String name, @NotNull final Class<?> jobClass, @NotNull final Function<ICitizenData, IJob<?>> jobFunction)
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
    @Nullable
    public static IJob createFromNBT(final ICitizenData citizen, @NotNull final NBTTagCompound compound)
    {
        final String jobType = compound.hasKey(TAG_TYPE) ? compound.getString(TAG_TYPE) : MAPPING_PLACEHOLDER;
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

    @NotNull
    public static Map<Class<?>, String> getClassToNameMap()
    {
        return classToNameMap;
    }
}
