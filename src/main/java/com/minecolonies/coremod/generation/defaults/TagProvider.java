package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.blocks.*;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TagProvider extends BlockTagsProvider
{
    public TagProvider(
      final DataGenerator generatorIn,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(generatorIn, Constants.MOD_ID, existingFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags()
    {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
          .add(ModBlocks.blockIronGate);

        this.tag(BlockTags.MINEABLE_WITH_AXE)
          .add(ModBlocks.blockBarrel,
            ModBlocks.blockRack,
            ModBlocks.blockWoodenGate,
            ModBlocks.blockScarecrow,
            ModBlocks.blockDecorationPlaceholder,
            ModBlocks.blockColonyBanner,
            ModBlocks.blockColonyWallBanner,
            ModBlocks.blockPostBox,
            ModBlocks.blockStash)
          .add(ModBlocks.getHuts());

        this.tag(BlockTags.MINEABLE_WITH_SHOVEL)
          .add(ModBlocks.blockCompostedDirt,
            ModBlocks.blockGrave,
            ModBlocks.blockNamedGrave);
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Global Tag Provider";
    }
}
