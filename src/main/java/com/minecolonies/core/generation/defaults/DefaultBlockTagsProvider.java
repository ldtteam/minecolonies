package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

@SuppressWarnings({"ConstantConditions", "unchecked"})
public class DefaultBlockTagsProvider extends BlockTagsProvider
{

    public DefaultBlockTagsProvider(
      final PackOutput output,
      final CompletableFuture<HolderLookup.Provider> lookupProvider,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(final HolderLookup.Provider holder)
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
                .add(ModBlocks.blockWoodenGate)
                .add(ModBlocks.blockIronGate)
                .addTag(BlockTags.BANNERS)
                .addTag(BlockTags.SIGNS)
                .addTag(BlockTags.CAMPFIRES);

        tag(ModTags.concreteBlocks)
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

        tag(ModTags.concretePowderBlocks)
          .add(Blocks.WHITE_CONCRETE_POWDER)
          .add(Blocks.ORANGE_CONCRETE_POWDER)
          .add(Blocks.MAGENTA_CONCRETE_POWDER)
          .add(Blocks.LIGHT_BLUE_CONCRETE_POWDER)
          .add(Blocks.YELLOW_CONCRETE_POWDER)
          .add(Blocks.LIME_CONCRETE_POWDER)
          .add(Blocks.PINK_CONCRETE_POWDER)
          .add(Blocks.GRAY_CONCRETE_POWDER)
          .add(Blocks.LIGHT_GRAY_CONCRETE_POWDER)
          .add(Blocks.CYAN_CONCRETE_POWDER)
          .add(Blocks.PURPLE_CONCRETE_POWDER)
          .add(Blocks.BLUE_CONCRETE_POWDER)
          .add(Blocks.BROWN_CONCRETE_POWDER)
          .add(Blocks.GREEN_CONCRETE_POWDER)
          .add(Blocks.RED_CONCRETE_POWDER)
          .add(Blocks.BLACK_CONCRETE_POWDER);

        tag(ModTags.pathingBlocks)
                .addTag(ModTags.concreteBlocks)
                .addTag(BlockTags.STONE_BRICKS)
            .addTag(BlockTags.PLANKS)
            .addTag(BlockTags.WOODEN_SLABS)
            .addTag(BlockTags.WOOL_CARPETS)
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
                .add(Blocks.DIRT_PATH)
                .add(Blocks.MUD_BRICKS)
                .add(Blocks.MUD_BRICK_SLAB)
                .add(Blocks.MUD_BRICK_STAIRS)
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
                .add(Blocks.END_STONE_BRICK_STAIRS)
                .add(Blocks.POLISHED_DEEPSLATE)
                .add(Blocks.POLISHED_DEEPSLATE_SLAB)
                .add(Blocks.POLISHED_DEEPSLATE_STAIRS)
                .add(Blocks.DEEPSLATE_BRICKS)
                .add(Blocks.DEEPSLATE_BRICK_SLAB)
                .add(Blocks.DEEPSLATE_BRICK_STAIRS)
                .add(Blocks.DEEPSLATE_TILES)
                .add(Blocks.DEEPSLATE_TILE_SLAB)
                .add(Blocks.DEEPSLATE_TILE_STAIRS)
                .add(com.ldtteam.domumornamentum.block.ModBlocks.getInstance().getAllBrickBlocks().toArray(new Block[0]))
                .add(com.ldtteam.domumornamentum.block.ModBlocks.getInstance().getAllBrickStairBlocks().toArray(new Block[0]))
                .addTag(com.ldtteam.domumornamentum.tag.ModTags.BRICKS);

        tag(ModTags.dangerousBlocks);

        tag(ModTags.freeClimbBlocks)
                .add(Blocks.LADDER)
                .add(Blocks.SCAFFOLDING);

        tag(ModTags.mangroveTree)
                .add(Blocks.MANGROVE_LOG)
                .add(Blocks.MANGROVE_ROOTS);

        // sadly forge doesn't provide the block form of this tag, despite providing an item tag
        tag(ModTags.mushroomBlocks)
                .add(Blocks.BROWN_MUSHROOM)
                .add(Blocks.RED_MUSHROOM);

        tag(ModTags.hugeMushroomBlocks)
                .add(Blocks.BROWN_MUSHROOM_BLOCK)
                .add(Blocks.RED_MUSHROOM_BLOCK);

        tag(ModTags.fungiBlocks)
                .add(Blocks.WARPED_FUNGUS)
                .add(Blocks.CRIMSON_FUNGUS);

        tag(ModTags.tree)
                .addTag(BlockTags.LOGS)
                .addTag(ModTags.mangroveTree)
                .add(Blocks.MUSHROOM_STEM)
                .addOptionalTag(new ResourceLocation("productivebees", "nests/wood_nests"));

        tag(ModTags.colonyProtectionException)
                .addOptionalTag(new ResourceLocation("waystones", "waystones"));

        tag(ModTags.indestructible).add(Blocks.BEDROCK);
        tag(ModTags.oreChanceBlocks)
                .addTags(Tags.Blocks.STONE)
                .addTags(BlockTags.BASE_STONE_OVERWORLD, BlockTags.BASE_STONE_NETHER);

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.blockIronGate);

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.blockBarrel)
                .add(ModBlocks.blockRack)
                .add(ModBlocks.blockWoodenGate)
                .add(ModBlocks.blockScarecrow)
                .add(ModBlocks.blockDecorationPlaceholder)
                .add(ModBlocks.blockColonyBanner)
                .add(ModBlocks.blockColonyWallBanner)
                .add(ModBlocks.blockPostBox)
                .add(ModBlocks.blockStash)
                .add(ModBlocks.blockPlantationField)
                .add(ModBlocks.getHuts());

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.blockCompostedDirt)
                .add(ModBlocks.blockGrave)
                .add(ModBlocks.blockNamedGrave);
        tag(ModTags.validSpawn)
          .add(Blocks.AIR, Blocks.CAVE_AIR, Blocks.SNOW, Blocks.TALL_GRASS, Blocks.GRASS, Blocks.FERN, Blocks.TORCH)
          .addTags(BlockTags.BUTTONS)
          .addTags(BlockTags.RAILS)
          .addTags(BlockTags.WOOL_CARPETS);
    }
}
