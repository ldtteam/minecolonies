package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StructureTagProvider extends ForgeRegistryTagsProvider<StructureFeature<?>>
{
    public StructureTagProvider(
      final DataGenerator generatorIn,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(generatorIn, ForgeRegistries.STRUCTURE_FEATURES, Constants.MOD_ID, existingFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags()
    {
        this.tag(ModTags.archeologist_visitable)
          .add(StructureFeature.DESERT_PYRAMID)
          .add(StructureFeature.JUNGLE_TEMPLE)
          .add(StructureFeature.BASTION_REMNANT);
    }

    @Override
    @NotNull
    public String getName()
    {
        return "StructureFeature Tag Provider";
    }
}
