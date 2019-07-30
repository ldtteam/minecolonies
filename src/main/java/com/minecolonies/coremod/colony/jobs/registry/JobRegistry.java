package com.minecolonies.coremod.colony.jobs.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.jobs.*;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

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
    private static final BiMap<String, Class<? extends AbstractJob>> nameToClassMap = HashBiMap.create();
    //fix for the annotation
    static
    {
        addMapping(MAPPING_PLACEHOLDER, JobPlaceholder.class);
        addMapping(MAPPING_BUILDER, JobBuilder.class);
        addMapping(MAPPING_DELIVERY, JobDeliveryman.class);
        addMapping(MAPPING_MINER, JobMiner.class);
        addMapping(MAPPING_LUMBERJACK, JobLumberjack.class);
        addMapping(MAPPING_FARMER, JobFarmer.class);
        addMapping(MAPPING_FISHERMAN, JobFisherman.class);
        addMapping(MAPPING_BAKER, JobBaker.class);
        addMapping(MAPPING_COOK, JobCook.class);
        addMapping(MAPPING_SHEPHERD, JobShepherd.class);
        addMapping(MAPPING_COWBOY, JobCowboy.class);
        addMapping(MAPPING_SWINE_HERDER, JobSwineHerder.class);
        addMapping(MAPPING_CHICKEN_HERDER, JobChickenHerder.class);
        addMapping(MAPPING_SMELTER, JobSmelter.class);
        addMapping(MAPPING_RANGER, JobRanger.class);
        addMapping(MAPPING_KNIGHT, JobKnight.class);
        addMapping(MAPPING_COMPOSTER, JobComposter.class);
        addMapping(MAPPING_STUDENT, JobStudent.class);
        addMapping(MAPPING_ARCHER, JobArcherTraining.class);
        addMapping(MAPPING_COMBAT, JobCombatTraining.class);
        addMapping(MAPPING_SAWMILL, JobSawmill.class);
        addMapping(MAPPING_BLACKSMITH, JobBlacksmith.class);
        addMapping(MAPPING_STONEMASON, JobStonemason.class);
        addMapping(MAPPING_STONE_SMELTERY, JobStoneSmeltery.class);
        addMapping(MAPPING_CRUSHER, JobCrusher.class);
        addMapping(MAPPING_SIFTER, JobSifter.class);
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
     * @param jobClass class of job.
     */
    private static void addMapping(final String name, @NotNull final Class<? extends AbstractJob> jobClass)
    {
        if (nameToClassMap.containsKey(name))
        {
            throw new IllegalArgumentException("Duplicate type '" + name + "' when adding Job class mapping");
        }
        try
        {
            if (jobClass.getDeclaredConstructor(CitizenData.class) != null)
            {
                nameToClassMap.put(name, jobClass);
                nameToClassMap.inverse().put(jobClass, name);
            }
        }
        catch (final NoSuchMethodException exception)
        {
            throw new IllegalArgumentException("Missing constructor for type '" + name + "' when adding Job class mapping", exception);
        }
    }

    /**
     * Create a Job from saved NBTTagCompound data.
     *
     * @param citizen  The citizen that owns the Job.
     * @param compound The NBTTagCompound containing the saved Job data.
     * @return New Job created from the data, or null.
     */
    @Nullable
    public static AbstractJob createFromNBT(final CitizenData citizen, @NotNull final NBTTagCompound compound)
    {
        @Nullable AbstractJob job = null;
        @Nullable Class<? extends AbstractJob> oclass = null;

        try
        {
            oclass = nameToClassMap.get(compound.getString(TAG_TYPE));

            if (oclass != null)
            {
                final Constructor<?> constructor = oclass.getDeclaredConstructor(CitizenData.class);
                job = (AbstractJob) constructor.newInstance(citizen);
            }
        }
        catch (@NotNull NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e)
        {
            Log.getLogger().trace(e);
        }

        if (job != null)
        {
            try
            {
                job.readFromNBT(compound);
            }
            catch (final RuntimeException ex)
            {
                Log.getLogger().error(String.format("A Job %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                  compound.getString(TAG_TYPE), oclass.getName()), ex);
                job = null;
            }
        }
        else
        {
            Log.getLogger().warn(String.format("Unknown Job type '%s' or missing constructor of proper format.", compound.getString(TAG_TYPE)));
        }

        return job;
    }

    @NotNull
    public static Map<Class<? extends AbstractJob>, String> getClassToNameMap()
    {
        return nameToClassMap.inverse();
    }
}
