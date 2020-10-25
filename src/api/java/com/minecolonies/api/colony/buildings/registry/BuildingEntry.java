package com.minecolonies.api.colony.buildings.registry;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.Validate;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Entry for the {@link IBuilding} registry. Makes it possible to create a single registry for a {@link IBuilding}. Used to lookup how to create {@link IBuilding} and {@link
 * IBuildingView}. Also links a given {@link IBuilding} to a given {@link AbstractBlockHut}.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass") //Use the builder to create one.
public class BuildingEntry extends ForgeRegistryEntry<BuildingEntry>
{
    private final AbstractBlockHut<?> buildingBlock;

    private final BiFunction<IColony, BlockPos, IBuilding> buildingProducer;

    /**
     * A builder class for {@link BuildingEntry}.
     */
    public static final class Builder
    {
        private AbstractBlockHut<?>                                        buildingBlock;
        private BiFunction<IColony, BlockPos, IBuilding>                   buildingProducer;
        private Supplier<BiFunction<IColonyView, BlockPos, IBuildingView>> buildingViewProducer;
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

            return new BuildingEntry(buildingBlock, buildingProducer, buildingViewProducer).setRegistryName(registryName);
        }
    }

    private final Supplier<BiFunction<IColonyView, BlockPos, IBuildingView>> buildingViewProducer;

    public AbstractBlockHut<?> getBuildingBlock()
    {
        return buildingBlock;
    }

    public BiFunction<IColony, BlockPos, IBuilding> getBuildingProducer()
    {
        return buildingProducer;
    }

    private BuildingEntry(
      final AbstractBlockHut<?> buildingBlock,
      final BiFunction<IColony, BlockPos, IBuilding> buildingProducer,
      final Supplier<BiFunction<IColonyView, BlockPos, IBuildingView>> buildingViewProducer)
    {
        super();
        this.buildingBlock = buildingBlock;
        this.buildingProducer = buildingProducer;
        this.buildingViewProducer = buildingViewProducer;
    }

    public Supplier<BiFunction<IColonyView, BlockPos, IBuildingView>> getBuildingViewProducer()
    {
        return buildingViewProducer;
    }
}
