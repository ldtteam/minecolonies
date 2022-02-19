package com.minecolonies.coremod.generation.defaults;

import com.ldtteam.structurize.blocks.types.ShingleFaceType;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.constant.TagConstants;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

@SuppressWarnings("unchecked")
public class DefaultItemTagsProvider extends ItemTagsProvider
{
    public DefaultItemTagsProvider(@NotNull final DataGenerator generator,
                                   @NotNull final BlockTagsProvider blockTagsProvider,
                                   @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(generator, blockTagsProvider, MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
        ModTags.init();     // apparently runData doesn't run work queued in common setup

        tag(ModTags.compostables_poor).addTags(Tags.Items.SEEDS, ItemTags.SAPLINGS);
        tag(ModTags.compostables)
                .addTags(Tags.Items.CROPS, Tags.Items.EGGS, ItemTags.FLOWERS, ItemTags.FISHES, ItemTags.LEAVES, ItemTags.WOOL)
                .add(Items.ROTTEN_FLESH, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM)
                .add(Items.FEATHER, Items.PUMPKIN, Items.CARVED_PUMPKIN)
                .add(Items.GRASS, Items.TALL_GRASS, Items.FERN, Items.LARGE_FERN)
                .add(Items.KELP, Items.SEAGRASS, Items.CACTUS, Items.SUGAR_CANE, Items.VINE)
                .add(Items.COCOA_BEANS, Items.LILY_PAD, Items.SEA_PICKLE)
                .add(Items.BROWN_MUSHROOM_BLOCK, Items.RED_MUSHROOM_BLOCK, Items.MUSHROOM_STEM)
                .add(Items.CAKE, Items.RABBIT_FOOT, Items.FERMENTED_SPIDER_EYE)
                .add(Items.NETHER_WART_BLOCK, Items.WARPED_WART_BLOCK);
        tag(ModTags.compostables_rich).add(Items.PODZOL, ModBlocks.blockCompostedDirt.asItem());
        
        tag(ModTags.concretePowder)
                .add(Items.WHITE_CONCRETE_POWDER)
                .add(Items.ORANGE_CONCRETE_POWDER)
                .add(Items.MAGENTA_CONCRETE_POWDER)
                .add(Items.LIGHT_BLUE_CONCRETE_POWDER)
                .add(Items.YELLOW_CONCRETE_POWDER)
                .add(Items.LIME_CONCRETE_POWDER)
                .add(Items.PINK_CONCRETE_POWDER)
                .add(Items.GRAY_CONCRETE_POWDER)
                .add(Items.LIGHT_GRAY_CONCRETE_POWDER)
                .add(Items.CYAN_CONCRETE_POWDER)
                .add(Items.PURPLE_CONCRETE_POWDER)
                .add(Items.BLUE_CONCRETE_POWDER)
                .add(Items.BROWN_CONCRETE_POWDER)
                .add(Items.GREEN_CONCRETE_POWDER)
                .add(Items.RED_CONCRETE_POWDER)
                .add(Items.BLACK_CONCRETE_POWDER);

        final Tags.IOptionalNamedTag<Item> glazedTerracotta = ItemTags.createOptional(new ResourceLocation(MOD_ID, "glazed_terracotta"));
        tag(glazedTerracotta)
                .add(Items.WHITE_GLAZED_TERRACOTTA)
                .add(Items.ORANGE_GLAZED_TERRACOTTA)
                .add(Items.MAGENTA_GLAZED_TERRACOTTA)
                .add(Items.LIGHT_BLUE_GLAZED_TERRACOTTA)
                .add(Items.YELLOW_GLAZED_TERRACOTTA)
                .add(Items.LIME_GLAZED_TERRACOTTA)
                .add(Items.PINK_GLAZED_TERRACOTTA)
                .add(Items.GRAY_GLAZED_TERRACOTTA)
                .add(Items.LIGHT_GRAY_GLAZED_TERRACOTTA)
                .add(Items.CYAN_GLAZED_TERRACOTTA)
                .add(Items.PURPLE_GLAZED_TERRACOTTA)
                .add(Items.BLUE_GLAZED_TERRACOTTA)
                .add(Items.BROWN_GLAZED_TERRACOTTA)
                .add(Items.GREEN_GLAZED_TERRACOTTA)
                .add(Items.RED_GLAZED_TERRACOTTA)
                .add(Items.BLACK_GLAZED_TERRACOTTA);

        // Be careful adding tags to this, as some, especially #minecraft:small_flowers, have logical impacts that
        // has made them used heavily in mods, including many where high availability has severe balance ramifications.
        tag(ModTags.floristFlowers)
                .add(Items.SUNFLOWER)
                .add(Items.LILAC)
                .add(Items.ROSE_BUSH)
                .add(Items.PEONY)
                .add(Items.TALL_GRASS)
                .add(Items.LARGE_FERN)
                .add(Items.FERN)
                .add(Items.DANDELION)
                .add(Items.POPPY)
                .add(Items.BLUE_ORCHID)
                .add(Items.ALLIUM)
                .add(Items.AZURE_BLUET)
                .add(Items.RED_TULIP)
                .add(Items.ORANGE_TULIP)
                .add(Items.WHITE_TULIP)
                .add(Items.PINK_TULIP)
                .add(Items.OXEYE_DAISY)
                .add(Items.CORNFLOWER)
                .add(Items.LILY_OF_THE_VALLEY);

        tag(ModTags.fungi)
                .add(Items.WARPED_FUNGUS)
                .add(Items.CRIMSON_FUNGUS);

        tag(ModTags.meshes)
                .add(ModItems.sifterMeshString)
                .add(ModItems.sifterMeshFlint)
                .add(ModItems.sifterMeshIron)
                .add(ModItems.sifterMeshDiamond);

        tag(ModTags.excludedFood)
                .add(Items.ENCHANTED_GOLDEN_APPLE)
                .add(Items.POISONOUS_POTATO)
                .add(Items.ROTTEN_FLESH)
                .add(ModItems.chorusBread)
                .add(ModItems.goldenBread);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_BAKER))
                .addTag(Tags.Items.CROPS_WHEAT);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_BAKER));
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_BAKER));
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_BAKER))
                .add(Items.BREAD, Items.CAKE, Items.COOKIE, Items.PUMPKIN_PIE);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_BLACKSMITH))
                .addTags(Tags.Items.NUGGETS, Tags.Items.INGOTS)
                .add(Items.DIAMOND_BLOCK, Items.EMERALD_BLOCK);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_BLACKSMITH))
                .addTag(Tags.Items.CROPS)
                .addTag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_DYER))
                .addTag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_MECHANIC))
                .addTag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_STONEMASON))
                .add(Items.BRICK);
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_BLACKSMITH))
                .addTags(Tags.Items.NUGGETS, Tags.Items.INGOTS)
                .add(Items.SHEARS);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_BLACKSMITH))
                .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_DYER))
                .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_MECHANIC))
                .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_SAWMILL))
                .add(Items.FIREWORK_STAR);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_COOK))
                .addTag(ItemTags.FISHES)
                .add(Items.BEEF)
                .add(Items.MUTTON)
                .add(Items.CHICKEN)
                .add(Items.PORKCHOP)
                .add(Items.RABBIT)
                .add(Items.POTATO)
                .add(Items.KELP)
                .add(Items.DRIED_KELP)
                .add(Items.DRIED_KELP_BLOCK)
                .add(Items.EGG)
                .add(Items.MILK_BUCKET);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_COOK))
                .addTag(Tags.Items.CROPS_WHEAT);
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_COOK));
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_COOK))
                .add(Items.BREAD, Items.CAKE, Items.COOKIE, Items.PUMPKIN_PIE);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_DYER))
                .addTag(Tags.Items.DYES);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_DYER));
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_DYER))
                .addTag(Tags.Items.DYES)
                .add(Items.RED_NETHER_BRICKS);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_DYER))
                .addTags(ModTags.concretePowder);
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_DYER_SMELTING))
                .addTag(Tags.Items.DYES);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_FARMER))
                .add(Items.HAY_BLOCK);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_FARMER));
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_FARMER))
                .add(Items.HAY_BLOCK)
                .addTag(Tags.Items.SEEDS)
                .add(ModBlocks.blockCompostedDirt.asItem())
                .add(Items.MELON)
                .add(Items.COARSE_DIRT);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_FARMER));

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_FLETCHER))
                .addTag(Tags.Items.STRING)
                .addTag(ItemTags.WOOL)
                .add(Items.RABBIT_HIDE)
                .add(Items.LEATHER);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_FLETCHER))
                .addTag(Tags.Items.DYES);
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_FLETCHER))
                .addTag(Tags.Items.STRING);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_FLETCHER))
                .add(Items.BOOK)
                .add(Items.ITEM_FRAME);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_GLASSBLOWER))
                .addTag(Tags.Items.GLASS)
                .addTag(Tags.Items.GLASS_PANES);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_GLASSBLOWER))
                .addTag(Tags.Items.DYES);
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_GLASSBLOWER));
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_GLASSBLOWER));
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_GLASSBLOWER_SMELTING))
                .addTag(Tags.Items.GLASS);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_MECHANIC))
                .addTag(Tags.Items.DUSTS_REDSTONE)
                .addTag(Tags.Items.ORES_REDSTONE)
                .addTag(Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .add(Items.BLAZE_ROD)
                .add(Items.ENDER_PEARL)
                .add(Items.ENDER_EYE)
                .add(Items.REDSTONE_TORCH)
                .add(Items.GLOWSTONE_DUST)
                .add(Items.DRIED_KELP_BLOCK);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_MECHANIC));
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_MECHANIC))
                .addTag(Tags.Items.STORAGE_BLOCKS)
                .addTag(ItemTags.RAILS)
                .addTag(ItemTags.WOODEN_PRESSURE_PLATES)
                .add(Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .add(Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
                .add(Items.POLISHED_BLACKSTONE_PRESSURE_PLATE)
                .add(Items.STONE_PRESSURE_PLATE)
                .add(Items.BLUE_ICE)
                .add(Items.PACKED_ICE)
                .add(Items.BONE_BLOCK)
                .add(Items.DRIED_KELP_BLOCK)
                .add(Items.HONEY_BLOCK)
                .add(Items.HONEYCOMB_BLOCK)
                .add(Items.SNOW_BLOCK)
                .add(Items.DAYLIGHT_DETECTOR)
                .add(Items.LEVER)
                .add(Items.STICKY_PISTON)
                .add(Items.TRIPWIRE_HOOK)
                .add(Items.ENCHANTING_TABLE)
                .add(Items.JACK_O_LANTERN)
                .add(Items.LANTERN)
                .add(Items.SEA_LANTERN)
                .add(Items.SOUL_LANTERN)
                .add(Items.SOUL_TORCH)
                .add(Items.TORCH)
                .add(Items.ENDER_CHEST)
                .add(Items.TRAPPED_CHEST);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_MECHANIC))
                .add(Items.SPECTRAL_ARROW);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_PLANTATION))
                .add(Items.BAMBOO);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_PLANTATION));
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_PLANTATION))
                .add(Items.BOOK)
                .add(Items.PAPER)
                .add(Items.SUGAR)
                .add(Items.WRITABLE_BOOK);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_PLANTATION));

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_SAWMILL))
                .add(Items.CACTUS);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_SAWMILL))
                .addTag(Tags.Items.INGOTS)
                .addTag(Tags.Items.STONE)
                .addTag(Tags.Items.DUSTS_REDSTONE)
                .addTag(Tags.Items.STRING);
        for (final ShingleFaceType type : com.ldtteam.structurize.blocks.ModBlocks.shingles)
        {
            tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_SAWMILL))
                    .addTag(ItemTags.bind("structurize:shingles/" + type.getGroup()))
                    .addTag(ItemTags.bind("structurize:shingle_slabs/" + type.getGroup()));
        }
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_SAWMILL))
                .addTag(ItemTags.bind("structurize:timber_frames/timber_frames"))
                .add(ModBlocks.blockBarrel.asItem());
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_SAWMILL))
                .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_MECHANIC));

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_STONEMASON))
                .addTags(Tags.Items.STONE, Tags.Items.COBBLESTONE, Tags.Items.END_STONES)
                .add(Items.NETHER_BRICK)
                .add(Items.POPPED_CHORUS_FRUIT)
                .add(Items.PURPUR_BLOCK, Items.PURPUR_SLAB)
                .add(Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS)
                .add(Items.BLACKSTONE)
                .add(Items.POLISHED_BLACKSTONE, Items.POLISHED_BLACKSTONE_SLAB, Items.POLISHED_BLACKSTONE_BRICKS)
                .add(Items.BASALT, Items.POLISHED_BASALT)
                .add(Items.BRICKS, Items.STONE_BRICKS);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_STONEMASON))
                .addTags(ItemTags.LOGS, ItemTags.PLANKS)
                .add(Items.STICK)
                .addTag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_MECHANIC))
                .addTag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_DYER));
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_STONEMASON))
                .addTags(Tags.Items.STONE, Tags.Items.COBBLESTONE, Tags.Items.SANDSTONE)
                .addTags(ItemTags.STONE_BRICKS, ItemTags.STAIRS, ItemTags.WALLS)
                .add(Items.BRICKS, Items.NETHER_BRICKS, Items.POLISHED_BLACKSTONE_BRICKS)
                .add(Items.FURNACE)
                .add(Items.FLOWER_POT)
                .add(Items.CHISELED_QUARTZ_BLOCK)
                .add(Items.QUARTZ_PILLAR)
                .add(Items.QUARTZ_BRICKS);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_STONEMASON))
                .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_MECHANIC))
                .addTag(ItemTags.WOODEN_SLABS)
                .addTag(ItemTags.WOODEN_STAIRS);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_STONE_SMELTERY))
                .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_STONEMASON));
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_STONE_SMELTERY));
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_STONE_SMELTERY))
                .add(Items.BRICK)
                .add(Items.COAL)
                .add(Items.CHARCOAL)
                .add(Items.NETHER_BRICK)
                .addTag(Tags.Items.STONE)
                .add(Items.SMOOTH_STONE)
                .add(Items.TERRACOTTA)
                .addTag(glazedTerracotta)
                .addTag(ItemTags.STONE_BRICKS)
                .add(Items.SMOOTH_QUARTZ)
                .add(Items.POPPED_CHORUS_FRUIT)
                .add(Items.SMOOTH_SANDSTONE)
                .add(Items.SMOOTH_RED_SANDSTONE);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_STONE_SMELTERY));

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_REDUCEABLE))
                .addTags(Tags.Items.GLASS, Tags.Items.GLASS_PANES)
                .addTag(Tags.Items.CROPS_WHEAT)
                .addTag(Tags.Items.STRING)
                .addTags(Tags.Items.NUGGETS, Tags.Items.INGOTS)
                .addTags(Tags.Items.STONE, Tags.Items.COBBLESTONE)
                .addTags(Tags.Items.GRAVEL, Tags.Items.SAND)
                .addTags(Tags.Items.DUSTS, Tags.Items.GEMS)
                .add(Items.BOOK, Items.PAPER, Items.SUGAR)
                .addTag(ItemTags.FISHES)
                .add(Items.BEEF)
                .add(Items.MUTTON)
                .add(Items.CHICKEN)
                .add(Items.PORKCHOP)
                .add(Items.RABBIT)
                .add(Items.POTATO)
                .addTag(ItemTags.WOOL)
                .addTags(ItemTags.LOGS, ItemTags.PLANKS, ItemTags.STONE_BRICKS)
                .add(Items.STICK)
                .add(Items.LEATHER)
                .add(Items.RABBIT_HIDE)
                .add(Items.NETHER_BRICK)
                .add(Items.POPPED_CHORUS_FRUIT)
                .add(Items.PRISMARINE_SHARD)
                .add(Items.PRISMARINE_CRYSTALS);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_REDUCEABLE))
                .addTags(Tags.Items.STONE, Tags.Items.COBBLESTONE)
                .addTags(Tags.Items.GRAVEL, Tags.Items.SAND)
                .addTags(Tags.Items.INGOTS, Tags.Items.STORAGE_BLOCKS)
                .add(Items.GLOWSTONE)
                .add(Items.DRIED_KELP_BLOCK)
                .add(Items.HONEY_BLOCK)
                .add(Items.HONEYCOMB_BLOCK)
                .add(Items.BONE_BLOCK)
                .add(Items.HAY_BLOCK)
                .add(ModItems.breadDough)
                .add(ModItems.cookieDough)
                .add(ModItems.rawPumpkinPie)
                .add(ModItems.cakeBatter);

    }
}
