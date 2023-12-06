package com.minecolonies.api.colony.buildings.registry;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Entry for the {@link IBuilding} registry. Makes it possible to create a single registry for a {@link IBuilding}. Used to lookup how to create {@link IBuilding} and {@link
 * IBuildingView}. Also links a given {@link IBuilding} to a given {@link AbstractBlockHut}.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass") //Use the builder to create one.
public class BuildingEntry
{
    private final AbstractBlockHut<?> buildingBlock;

    private final BiFunction<IColony, BlockPos, IBuilding> buildingProducer;
    private final ResourceLocation registryName;

    private List<ModuleProducer> buildingModuleProducers;

    /**
     * A builder class for {@link BuildingEntry}.
     */
    public static final class Builder
    {
        private AbstractBlockHut<?>                                        buildingBlock;
        private BiFunction<IColony, BlockPos, IBuilding>                   buildingProducer;
        private Supplier<BiFunction<IColonyView, BlockPos, IBuildingView>> buildingViewProducer;
        private List<ModuleProducer>                                       buildingModuleProducers = new ArrayList<>();
        private ResourceLocation                                           registryName;

        /**
         * Sets the block that represents this building.
         *
         * @param buildingBlock The block.
         * @return The builder.
         */
        public Builder setBuildingBlock(final AbstractBlockHut<?> buildingBlock)
        {
            this.buildingBlock = buildingBlock;
            return this;
        }

        /**
         * Sets the callback that is used to create the {@link IBuilding} from the {@link IColony} and its position in the world.
         *
         * @param buildingProducer The callback used to create the {@link IBuilding}.
         * @return The builder.
         */
        public Builder setBuildingProducer(final BiFunction<IColony, BlockPos, IBuilding> buildingProducer)
        {
            this.buildingProducer = buildingProducer;
            return this;
        }

        /**
         * Sets the callback that is used to create the {@link IBuildingView} from the {@link IColonyView} and its position in the world.
         *
         * @param buildingViewProducer The callback used to create the {@link IBuildingView}.
         * @return The builder.
         */
        public Builder setBuildingViewProducer(final Supplier<BiFunction<IColonyView, BlockPos, IBuildingView>> buildingViewProducer)
        {
            this.buildingViewProducer = buildingViewProducer;
            return this;
        }

        /**
         * Sets the registry name for the new building entry.
         *
         * @param registryName The name for the registry entry.
         * @return The builder.
         */
        public Builder setRegistryName(final ResourceLocation registryName)
        {
            this.registryName = registryName;
            return this;
        }

        /**
         * Method used to create the entry.
         *
         * @return The entry.
         */
        @SuppressWarnings("PMD.AccessorClassGeneration") //The builder explicitly allowed to create an instance.
        public BuildingEntry createBuildingEntry()
        {
            Validate.notNull(buildingBlock);
            Validate.notNull(buildingProducer);
            Validate.notNull(buildingViewProducer);
            Validate.notNull(registryName);

            return new BuildingEntry(registryName, buildingBlock, buildingProducer, buildingViewProducer, buildingModuleProducers);
        }

        /**
         * Add a building module producer.
         * @return the builder again.
         */
        public Builder addBuildingModuleProducer(final ModuleProducer moduleSet)
        {
            buildingModuleProducers.add(moduleSet);
            return this;
        }
    }

    public String getTranslationKey()
    {
        return "com." + registryName.getNamespace() + ".building." + registryName.getPath();
    }

    private final Supplier<BiFunction<IColonyView, BlockPos, IBuildingView>> buildingViewProducer;

    public AbstractBlockHut<?> getBuildingBlock()
    {
        return buildingBlock;
    }

    public IBuilding produceBuilding(final BlockPos position, final IColony colony)
    {
        final IBuilding building = buildingProducer.apply(colony, position);
        for (final ModuleProducer<IBuildingModule, IBuildingModuleView> moduleTuple : buildingModuleProducers)
        {
            if (moduleTuple.moduleProducer != null)
            {
                building.registerModule(moduleTuple.moduleProducer.get().setBuilding(building).setProducer(moduleTuple));
            }
        }
        building.setBuildingType(this);
        return building;
    }

    public IBuildingView produceBuildingView(final BlockPos position, final IColonyView colony)
    {
        final IBuildingView buildingView = buildingViewProducer.get().apply(colony, position);
        buildingView.setBuildingType(this);
        for (final ModuleProducer<IBuildingModule,IBuildingModuleView> moduleSet : buildingModuleProducers)
        {
            if (moduleSet.viewProducer != null)
            {
                buildingView.registerModule(moduleSet.viewProducer.get().get().setProducer(moduleSet));
            }
        }

        return buildingView;
    }

    public List<ModuleProducer> getModuleProducers() { return buildingModuleProducers;}

    private BuildingEntry(
      final ResourceLocation registryName,
      final AbstractBlockHut<?> buildingBlock,
      final BiFunction<IColony, BlockPos, IBuilding> buildingProducer,
      final Supplier<BiFunction<IColonyView, BlockPos, IBuildingView>> buildingViewProducer,
      List<ModuleProducer> buildingModuleProducers)
    {
        super();
        this.registryName = registryName;
        this.buildingBlock = buildingBlock;
        this.buildingProducer = buildingProducer;
        this.buildingViewProducer = buildingViewProducer;
        this.buildingModuleProducers = buildingModuleProducers;
    }

    /**
     * Get the assigned registry name.
     * @return
     */
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    /**
     * Set of servermodule and its view with ID, module or view might be null
     */
    public static class ModuleProducer<MODULECLASS extends IBuildingModule, VIEWCLASS extends IBuildingModuleView>
    {
        /**
         * Map of all modules, by serialization key
         */
        private final static Map<String, ModuleProducer<? extends IBuildingModule, ? extends IBuildingModuleView>> ALL_MODULES = new HashMap<>();

        /**
         * Static supplier of runtime int ID's, used for networking
         */
        private static int runtimeIdGenerator = 0;

        public ModuleProducer(
          final String key, final Supplier<IBuildingModule> moduleProducer,
          final Supplier<Supplier<IBuildingModuleView>> viewProducer)
        {
            this.key = key;
            this.id = ++runtimeIdGenerator;
            this.viewProducer = viewProducer;
            this.moduleProducer = moduleProducer;

            ModuleProducer previous = ALL_MODULES.put(key,this);
            if (previous != null)
            {
                throw new RuntimeException("Tried to register existing module: "+key+" again!");
            }
        }

        /**
         * Internal temporary ID, used for sync, not guaranteed to persist between updates
         */
        private final int                                     id;

        /**
         * Saving and loading ID, should never change
         */
        public final String key;

        /**
         * View producers
         */
        private final Supplier<Supplier<IBuildingModuleView>> viewProducer;

        /**
         * Server module producers
         */
        private final Supplier<IBuildingModule>               moduleProducer;

        /**
         * Internal temporary ID, used for sync, not guaranteed to persist between updates
         */
        public int getRuntimeID()
        {
            return id;
        }

        /**
         * Get if a view exists
         * @return
         */
        public boolean hasView()
        {
            return viewProducer != null;
        }

        /**
         * Get if a server module exists
         * @return
         */
        public boolean hasServerModule()
        {
            return moduleProducer != null;
        }

        @Override
        public int hashCode()
        {
            return id;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            final ModuleProducer that = (ModuleProducer) o;
            return id == that.id;
        }
    }

    /**
     * Produces a module without a respective building, mostly used for display or other purposes
     *
     * @param key
     * @return
     */
    @Nullable
    public static IBuildingModule produceModuleWithoutBuilding(final String key)
    {
        final var producer = ModuleProducer.ALL_MODULES.get(key);
        if (producer.hasServerModule())
        {
            return producer.moduleProducer.get();
        }

        return null;
    }

    /**
     * Produces a module view without a respective building, mostly used for display or other purposes
     *
     * @param key
     * @return
     */
    @Nullable
    public static IBuildingModuleView produceViewWithoutBuilding(final String key)
    {
        final var producer = ModuleProducer.ALL_MODULES.get(key);
        if (producer.hasView())
        {
            return producer.viewProducer.get().get();
        }

        return null;
    }

    /**
     * Retreives all existing module producers and their IDs
     *
     * @return
     */
    public static Map<String, ModuleProducer> getALlModuleProducers()
    {
        return ImmutableMap.copyOf(ModuleProducer.ALL_MODULES);
    }
}
