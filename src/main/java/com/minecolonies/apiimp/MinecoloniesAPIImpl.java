package com.minecolonies.apiimp;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.ICitizenDataManager;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.buildings.registry.IGuardTypeRegistry;
import com.minecolonies.api.colony.jobs.registry.IJobRegistry;
import com.minecolonies.api.entity.ai.registry.IEntityAIRegistry;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.CitizenDataManager;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.registry.BuildingDataManager;
import com.minecolonies.coremod.colony.buildings.registry.GuardTypeRegistry;
import com.minecolonies.coremod.colony.jobs.registry.JobRegistry;
import com.minecolonies.coremod.entity.ai.registry.EntityAiRegistry;
import com.minecolonies.coremod.entity.pathfinding.registry.PathNavigateRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.jetbrains.annotations.NotNull;

public class MinecoloniesAPIImpl implements IMinecoloniesAPI
{
    private static MinecoloniesAPIImpl  ourInstance         = new MinecoloniesAPIImpl();
    private final  IBuildingDataManager buildingDataManager = new BuildingDataManager();

    private final IColonyManager                colonyManager        = new ColonyManager();
    private final ICitizenDataManager           citizenDataManager   = new CitizenDataManager();
    private final IEntityAIRegistry             entityAIRegistry     = new EntityAiRegistry();
    private final IGuardTypeRegistry            guardTypeRegistry    = new GuardTypeRegistry();
    private final IPathNavigateRegistry         pathNavigateRegistry = new PathNavigateRegistry();
    private final IJobRegistry                  jobRegistry          = new JobRegistry();
    private       IForgeRegistry<BuildingEntry> buildingRegistry;

    private MinecoloniesAPIImpl()
    {
    }

    public static MinecoloniesAPIImpl getInstance()
    {
        return ourInstance;
    }

    @Override
    @NotNull
    public IColonyManager getColonyManager()
    {
        return colonyManager;
    }

    @Override
    @NotNull
    public ICitizenDataManager getCitizenDataManager()
    {
        return citizenDataManager;
    }

    @Override
    @NotNull
    public IEntityAIRegistry getEntityAIRegistry()
    {
        return entityAIRegistry;
    }

    @Override
    @NotNull
    public IGuardTypeRegistry getGuardTypeRegistry()
    {
        return guardTypeRegistry;
    }

    @Override
    @NotNull
    public IPathNavigateRegistry getPathNavigateRegistry()
    {
        return pathNavigateRegistry;
    }

    @Override
    @NotNull
    public IJobRegistry getJobRegistry()
    {
        return jobRegistry;
    }

    @Override
    @NotNull
    public IBuildingDataManager getBuildingDataManager()
    {
        return buildingDataManager;
    }

    @Override
    @NotNull
    public IForgeRegistry<BuildingEntry> getBuildingRegistry()
    {
        return buildingRegistry;
    }

    public void onRegistryNewRegistry(final RegistryEvent.NewRegistry event)
    {
        buildingRegistry = new RegistryBuilder<BuildingEntry>()
                             .setName(new ResourceLocation(Constants.MOD_ID, "buildings"))
                             .setDefaultKey(new ResourceLocation(Constants.MOD_ID, "null"))
                             .disableSaving()
                             .setType(BuildingEntry.class)
                             .setIDRange(0, Integer.MAX_VALUE - 1)
                             .create();
    }
}

