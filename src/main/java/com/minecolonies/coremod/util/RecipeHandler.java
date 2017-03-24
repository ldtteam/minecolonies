package com.minecolonies.coremod.util;

import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.items.ModItems;
import com.minecolonies.coremod.lib.Constants;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 * Recipe storage for minecolonies.
 */
public final class RecipeHandler
{
    /**
     * Private constructor to hide the implicit public one.
     */
    private RecipeHandler()
    {
    }

    /**
     * Initialize all recipes for minecolonies.
     *
     * @param enableInDevelopmentFeatures if we want development recipes.
     * @param supplyChests                if we want supply chests or direct town hall crafting.
     */
    public static void init(final boolean enableInDevelopmentFeatures, final boolean supplyChests)
    {
        final String plankWood = "plankWood";
        
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockConstructionTape, 16), "SWS", "S S", "S S",
          'S', Items.STICK, 'W', new ItemStack(Blocks.WOOL, 1, Constants.YELLOW));
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockConstructionTapeCorner, 1),ModBlocks.blockConstructionTape);
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockConstructionTape, 1),ModBlocks.blockConstructionTapeCorner);
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutMiner, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.WOODEN_PICKAXE));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutMiner, 2), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.STONE_PICKAXE));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.WOODEN_AXE));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 2), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.STONE_AXE));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.ACACIA_DOOR));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.BIRCH_DOOR));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.DARK_OAK_DOOR));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.JUNGLE_DOOR));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.OAK_DOOR));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.SPRUCE_DOOR));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutCitizen, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', "torch"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutFisherman, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.FISHING_ROD));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.scanTool, 1), "  I", " S ", "S  ", 'I', Items.IRON_INGOT, 'S', "stickWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.buildTool, 1), "  C", " S ", "S  ", 'C', "cobblestone", 'S', "stickWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockSubstitution, 16), "XXX", "X#X", "XXX", 'X', plankWood, '#', ModItems.scanTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockSolidSubstitution, 16), "XXX", "X#X", "XXX", 'X', "logWood", '#', ModItems.scanTool));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutFarmer, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.WOODEN_HOE));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutFarmer, 2), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.STONE_HOE));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutField, 1), " Y ", "X#X", " X ", 'X', "stickWood", '#', Items.LEATHER, 'Y', Blocks.HAY_BLOCK));
        GameRegistry.addRecipe(new ItemStack(Blocks.WEB, 1), "X X", " X ", "X X", 'X', Items.STRING);
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutGuardTower, 2), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.BOW));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutWareHouse, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', "chest")); // check
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutDeliveryman, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.LEATHER_BOOTS));

        // Disabled for now
        // GameRegistry.addRecipe(new ItemStack(ModBlocks.blockBarrel, 1), "P P", "P P", " S ", 'P', Blocks.planks, 'S', Blocks.wooden_slab);
        if (enableInDevelopmentFeatures)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBaker, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.WHEAT));

            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBlacksmith, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', "ingotIron"));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutStonemason, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', Blocks.STONEBRICK));
        }

        if (supplyChests)
        {
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.ACACIA_BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.BIRCH_BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.DARK_OAK_BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.JUNGLE_BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.SPRUCE_BOAT);
        }
        else
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutTownHall, 1), "XXX", "X#X", "XXX", 'X', plankWood, '#', Items.BOAT));
        }
    }
}
