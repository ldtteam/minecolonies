package com.minecolonies.coremod.generation.defaults;

import com.ldtteam.structurize.api.blocks.BlockType;
import com.ldtteam.structurize.blocks.types.BrickType;
import com.minecolonies.api.items.ModTags;
import net.minecraft.block.Blocks;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

@SuppressWarnings({"ConstantConditions", "unchecked"})
public class DefaultBlockTagsProvider extends BlockTagsProvider
{
    public DefaultBlockTagsProvider(@NotNull final DataGenerator generator,
                                    @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(generator, MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
        tag(ModTags.decorationItems)
                .add(Blocks.DEAD_BRAIN_CORAL_BLOCK)
                .add(Blocks.DEAD_BUBBLE_CORAL_BLOCK)
                .add(Blocks.DEAD_FIRE_CORAL_BLOCK)
                .add(Blocks.DEAD_HORN_CORAL_BLOCK)
                .add(Blocks.DEAD_TUBE_CORAL_BLOCK)
                .add(Blocks.BRAIN_CORAL_BLOCK)
                .add(Blocks.BUBBLE_CORAL_BLOCK)
                .add(Blocks.FIRE_CORAL_BLOCK)
                .add(Blocks.HORN_CORAL_BLOCK)
                .add(Blocks.TUBE_CORAL_BLOCK)
                .add(Blocks.BELL)
                .add(Blocks.LANTERN)
                .addTag(BlockTags.BANNERS)
                .addTag(BlockTags.SIGNS)
                .addTag(BlockTags.CAMPFIRES);

        tag(ModTags.concreteBlock)
                .add(Blocks.WHITE_CONCRETE)
                .add(Blocks.ORANGE_CONCRETE)
                .add(Blocks.MAGENTA_CONCRETE)
                .add(Blocks.LIGHT_BLUE_CONCRETE)
                .add(Blocks.YELLOW_CONCRETE)
                .add(Blocks.LIME_CONCRETE)
                .add(Blocks.PINK_CONCRETE)
                .add(Blocks.GRAY_CONCRETE)
                .add(Blocks.LIGHT_GRAY_CONCRETE)
                .add(Blocks.CYAN_CONCRETE)
                .add(Blocks.PURPLE_CONCRETE)
                .add(Blocks.BLUE_CONCRETE)
                .add(Blocks.BROWN_CONCRETE)
                .add(Blocks.GREEN_CONCRETE)
                .add(Blocks.RED_CONCRETE)
                .add(Blocks.BLACK_CONCRETE);

        tag(ModTags.pathingBlocks)
                .addTag(ModTags.concreteBlock)
                .addTag(BlockTags.STONE_BRICKS)
                .add(Blocks.STONE_BRICK_STAIRS)
                .add(Blocks.STONE_BRICK_SLAB)
                .add(Blocks.MOSSY_STONE_BRICK_SLAB)
                .add(Blocks.MOSSY_STONE_BRICK_STAIRS)
                .add(Blocks.POLISHED_ANDESITE)
                .add(Blocks.POLISHED_ANDESITE_SLAB)
                .add(Blocks.POLISHED_ANDESITE_STAIRS)
                .add(Blocks.POLISHED_DIORITE)
                .add(Blocks.POLISHED_DIORITE_SLAB)
                .add(Blocks.POLISHED_DIORITE_STAIRS)
                .add(Blocks.POLISHED_GRANITE)
                .add(Blocks.POLISHED_GRANITE_SLAB)
                .add(Blocks.POLISHED_GRANITE_STAIRS)
                .add(Blocks.BRICKS)
                .add(Blocks.BRICK_SLAB)
                .add(Blocks.BRICK_STAIRS)
                .add(Blocks.NETHER_BRICKS)
                .add(Blocks.NETHER_BRICK_SLAB)
                .add(Blocks.NETHER_BRICK_STAIRS)
                .add(Blocks.RED_NETHER_BRICKS)
                .add(Blocks.RED_NETHER_BRICK_SLAB)
                .add(Blocks.RED_NETHER_BRICK_STAIRS)
                .add(Blocks.CRACKED_NETHER_BRICKS)
                .add(Blocks.CHISELED_NETHER_BRICKS)
                .add(Blocks.GRAVEL)
                .add(Blocks.GRASS_PATH)
                .add(Blocks.SMOOTH_STONE)
                .add(Blocks.SMOOTH_STONE_SLAB)
                .add(Blocks.SMOOTH_SANDSTONE)
                .add(Blocks.SMOOTH_SANDSTONE_SLAB)
                .add(Blocks.SMOOTH_SANDSTONE_STAIRS)
                .add(Blocks.CHISELED_SANDSTONE)
                .add(Blocks.CHISELED_RED_SANDSTONE)
                .add(Blocks.CUT_SANDSTONE)
                .add(Blocks.CUT_SANDSTONE_SLAB)
                .add(Blocks.CUT_RED_SANDSTONE)
                .add(Blocks.CUT_RED_SANDSTONE_SLAB)
                .add(Blocks.SMOOTH_RED_SANDSTONE)
                .add(Blocks.SMOOTH_RED_SANDSTONE_SLAB)
                .add(Blocks.SMOOTH_RED_SANDSTONE_STAIRS)
                .add(Blocks.POLISHED_BLACKSTONE)
                .add(Blocks.POLISHED_BLACKSTONE_STAIRS)
                .add(Blocks.POLISHED_BLACKSTONE_SLAB)
                .add(Blocks.POLISHED_BLACKSTONE_BRICKS)
                .add(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB)
                .add(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)
                .add(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)
                .add(Blocks.CHISELED_POLISHED_BLACKSTONE)
                .add(Blocks.END_STONE_BRICKS)
                .add(Blocks.END_STONE_BRICK_SLAB)
                .add(Blocks.END_STONE_BRICK_STAIRS);
        for (final BrickType type : BrickType.values())
        {
            final ResourceLocation id = type.getMainBlock().getRegistryName();
            tag(ModTags.pathingBlocks)
                    .add(ForgeRegistries.BLOCKS.getValue(id))
                    .add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id.getNamespace(), BlockType.SLAB.withSuffix(type.getName(), type.getPluralName()))))
                    .add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id.getNamespace(), BlockType.STAIRS.withSuffix(type.getName(), type.getPluralName()))));
        }

        tag(ModTags.colonyProtectionException)
                .addOptional(new ResourceLocation("waystones:waystone"))
                .addOptional(new ResourceLocation("waystones:sandy_waystone"))
                .addOptional(new ResourceLocation("waystones:mossy_waystone"));

        tag(ModTags.indestructible).add(Blocks.BEDROCK);
        tag(ModTags.oreChanceBlocks)
                .addTags(Tags.Blocks.STONE, Tags.Blocks.COBBLESTONE)
                .addTags(BlockTags.BASE_STONE_OVERWORLD, BlockTags.BASE_STONE_NETHER);
        tag(ModTags.validSpawn)
          .addTags(BlockTags.BUTTONS)
          .addTags(BlockTags.RAILS)
          .addTags(BlockTags.CARPETS)
          .add(Blocks.AIR, Blocks.CAVE_AIR, Blocks.SNOW, Blocks.TALL_GRASS, Blocks.GRASS, Blocks.FERN, Blocks.TORCH);
    }
}
