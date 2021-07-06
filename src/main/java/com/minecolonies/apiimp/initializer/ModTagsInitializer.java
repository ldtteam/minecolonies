package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.Log;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.util.ResourceLocation;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.TagConstants.*;

public class ModTagsInitializer
{
    /**
     * The name of the tag (crafter) for improving recipes
     */
    private static final String REDUCEABLE = "reduceable";

    /**
     * Tag specifier for Products to Include
     */
    private static final String PRODUCT = "_product";

    /**
     * Tag specifier for Products to Exclude
     */
    private static final String PRODUCT_EXCLUDED = "_product_excluded";

    /**
     * Tag specifier for Ingredients to include
     */
    private static final String INGREDIENT = "_ingredient";

    /**
     * Tag specifier for Ingredients to exclude
     */
    private static final String INGREDIENT_EXCLUDED = "_ingredient_excluded";

    /**
     * Cached tag supplier from the last successful TagUpdateEvent
     */
    private static ITagCollectionSupplier supplier;

    public static void init(final ITagCollectionSupplier tagSupplier)
    {
        supplier = tagSupplier;

        ModTags.decorationItems = getBlockTags(DECORATION_ITEMS, supplier);
        ModTags.concretePowder = getItemTags(CONCRETE_POWDER, supplier);
        ModTags.concreteBlock = getBlockTags(CONCRETE_BLOCK, supplier);
        ModTags.pathingBlocks = getBlockTags(PATHING_BLOCKS, supplier);
        ModTags.floristFlowers = getItemTags(FLORIST_FLOWERS, supplier);
        ModTags.fungi = getItemTags(FUNGI, supplier);
        ModTags.meshes = getItemTags(MESHES, supplier);
        ModTags.oreChanceBlocks = getBlockTags(ORECHANCEBLOCKS, supplier);
        ModTags.colonyProtectionException = getBlockTags(COLONYPROTECTIONEXCEPTION, supplier);
        ModTags.indestructible = getBlockTags(INDESTRUCTIBLE, supplier);
        ModTags.hostile = getEntityTags(HOSTILE, supplier);

        initCrafterRules("baker");
        initCrafterRules("blacksmith");
        initCrafterRules("chickenherder");
        initCrafterRules("concretemixer");
        initCrafterRules("cook");
        initCrafterRules("cowboy");
        initCrafterRules("crusher");
        initCrafterRules("dyer");
        initCrafterRules("farmer");
        initCrafterRules("fisherman");
        initCrafterRules("fletcher");
        initCrafterRules("glassblower");
        initCrafterRules("glassblower_smelting");
        initCrafterRules("dyer_smelting");
        initCrafterRules("lumberjack");
        initCrafterRules("mechanic");
        initCrafterRules("plantation");
        initCrafterRules("rabbithutch");
        initCrafterRules("sawmill");
        initCrafterRules("shepherd");
        initCrafterRules("smelter");
        initCrafterRules("stonemason");
        initCrafterRules("stonesmeltery");
        initCrafterRules("swineherder");

        initCrafterRules(REDUCEABLE);
        Log.getLogger().info("Tags Loaded");
    }

    /**
     * Initialize the four tags for a particular crafter
     * @param crafterName the string name of the crafter to initialize
     */
    private static void initCrafterRules(final String crafterName)
    {
        final String lowerName = crafterName.toLowerCase();
        final ResourceLocation products = new ResourceLocation(MOD_ID, lowerName.concat(PRODUCT));
        final ResourceLocation ingredients = new ResourceLocation(MOD_ID, lowerName.concat(INGREDIENT));
        final ResourceLocation productsExcluded = new ResourceLocation(MOD_ID, lowerName.concat(PRODUCT_EXCLUDED));
        final ResourceLocation ingredientsExcluded = new ResourceLocation(MOD_ID, lowerName.concat(INGREDIENT_EXCLUDED));


        ModTags.crafterProduct.put(lowerName, getItemTags(products, supplier));
        ModTags.crafterProductExclusions.put(lowerName, getItemTags(productsExcluded, supplier));
        ModTags.crafterIngredient.put(lowerName, getItemTags(ingredients, supplier));
        ModTags.crafterIngredientExclusions.put(lowerName, getItemTags(ingredientsExcluded, supplier));
    }

    /**
     * Get the Tag<Item> from the underlying API
     * @param resourceLocation The resource location specifying the tag ID
     * @param supplier         The tag supplier providing the tag lookup.
     * @return the tag collection
     */
    private static ITag<Item> getItemTags(final ResourceLocation resourceLocation, final ITagCollectionSupplier supplier)
    {
        return supplier.getItems().getTagOrEmpty(resourceLocation);
    }

    /**
     * Get the Tag<Block> from the underlying API
     * @param resourceLocation The resource location specifying the tag ID
     * @param supplier         The tag supplier providing the tag lookup.
     * @return the tag collection
     */
    private static ITag<Block> getBlockTags(final ResourceLocation resourceLocation, final ITagCollectionSupplier supplier)
    {
        return supplier.getBlocks().getTagOrEmpty(resourceLocation);
    }

    /**
     * Get the Tag<EntityType> from the underlying API
     * @param resourceLocation The resource location specifying the tag ID
     * @param supplier         The tag supplier providing the tag lookup.
     * @return the tag collection
     */
    private static ITag<EntityType<?>> getEntityTags(final ResourceLocation resourceLocation, final ITagCollectionSupplier supplier)
    {
        return supplier.getEntityTypes().getTagOrEmpty(resourceLocation);
    }
}
