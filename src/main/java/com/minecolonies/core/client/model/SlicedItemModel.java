package com.minecolonies.core.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class SlicedItemModel implements IUnbakedGeometry<SlicedItemModel>
{
    private final BlockModel baseModel;
    private final int slices;

    public SlicedItemModel(@NotNull final BlockModel baseModel, final int slices)
    {
        this.baseModel = baseModel;
        this.slices = slices;
    }

    @Override
    public void resolveParents(
        @NotNull final Function<ResourceLocation, UnbakedModel> modelGetter,
        @NotNull final IGeometryBakingContext context)
    {
        this.baseModel.resolveParents(modelGetter);
    }

    @NotNull
    @Override
    public BakedModel bake(
        @NotNull final IGeometryBakingContext context,
        @NotNull final ModelBaker baker,
        @NotNull final Function<Material, TextureAtlasSprite> spriteGetter,
        @NotNull final ModelState modelState,
        @NotNull final ItemOverrides overrides)
    {
        final ItemModelGenerator generator = new ItemModelGenerator();
        final TextureAtlasSprite[] sprites = new TextureAtlasSprite[slices];
        final BakedModel[] bakedSlices = new BakedModel[slices];
        final List<List<BakedQuad>> bakedSliceQuads = new ArrayList<>();
        final RandomSource random = RandomSource.create();

        sprites[0] = spriteGetter.apply(baseModel.getMaterial("layer0"));
        bakedSlices[0] = baseModel.bake(baker, baseModel, spriteGetter, modelState, context.useBlockLight());
        bakedSliceQuads.add(bakedSlices[0].getQuads(null, null, random, ModelData.EMPTY, null));

        for (int i = 1; i < slices; ++i)
        {
            final int slice = i;

            sprites[i] = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, sprites[0].contents().name().withSuffix(Integer.toString(i))));
            final Function<Material, TextureAtlasSprite> slicer = material ->
                material.equals(baseModel.getMaterial("layer0")) ? sprites[slice] : spriteGetter.apply(material);

            final BlockModel sliceModel = generator.generateBlockModel(slicer, baseModel);
            bakedSlices[i] = sliceModel.bake(baker, sliceModel, slicer, modelState, context.useBlockLight());
            final List<BakedQuad> quads = bakedSlices[i].getQuads(null, null, random, ModelData.EMPTY, null);
            bakedSliceQuads.add(quads);
        }

        final BakedModel[] variants = new BakedModel[64];
        final BakedModel[] guiVariants = new BakedModel[variants.length];

        for (int i = 0; i < variants.length; ++i)
        {
            final int count = i + 1;
            final int sliceIndex = count % sprites.length;
            final int stackCount = Math.min(2, (count - 1) / sprites.length);

            variants[i] = bake(bakedSlices[sliceIndex], bakedSliceQuads.get(sliceIndex), bakedSliceQuads.getFirst(), stackCount, sprites[0], overrides, false);
            guiVariants[i] = bake(bakedSlices[sliceIndex], bakedSliceQuads.get(sliceIndex), bakedSliceQuads.getFirst(), stackCount, sprites[0], overrides, true);
        }

        return new BakedSliceHelper(bakedSlices[1], variants, guiVariants, ItemDisplayContext.GUI);
    }

    private BakedModel bake(
        @NotNull final BakedModel bakedBase,
        @NotNull final List<BakedQuad> sliceQuads,
        @NotNull final List<BakedQuad> fullQuads,
        final int stackCount,
        @NotNull final TextureAtlasSprite particle,
        @NotNull final ItemOverrides overrides,
        final boolean gui)
    {
        final float px = 1f / 16f;
        final SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(baseModel, overrides, bakedBase.isGui3d())
            .particle(particle);

        for (int i = 0; i < stackCount; ++i)
        {
            offsetQuads(builder, fullQuads, gui ? i * 2 * px : 0, i * 2 * px);
        }
        offsetQuads(builder, sliceQuads, gui ? stackCount * 2 * px : 0, stackCount * 2 * px);

        return builder.build(RenderTypeGroup.EMPTY);
    }

    private static void offsetQuads(
        @NotNull final SimpleBakedModel.Builder builder,
        @NotNull final List<BakedQuad> originalQuads,
        final float yOffset,
        final float zOffset)
    {
        for (final BakedQuad quad : originalQuads)
        {
            int[] data = quad.getVertices().clone();

            for (int i = 0; i < 4; ++i)
            {
                int base = i * 8;

                float y = Float.intBitsToFloat(data[base + 1]);
                float z = Float.intBitsToFloat(data[base + 2]);

                data[base + 1] = Float.floatToRawIntBits(y + yOffset);
                data[base + 2] = Float.floatToRawIntBits(z + zOffset);
            }

            builder.addUnculledFace(new BakedQuad(
                data,
                quad.getTintIndex(),
                quad.getDirection(),
                quad.getSprite(),
                quad.isShade()
            ));
        }
    }

    /**
     * Helper to find the "real" baked model based on rendering context and stack size.
     */
    public static class BakedSliceHelper extends BakedModelWrapper<BakedModel>
    {
        private final BakedModel[] variants;
        private final BakedModel[] guiVariants;
        private final ItemDisplayContext context;

        public BakedSliceHelper(
            @NotNull final BakedModel originalModel,
            @NotNull final BakedModel[] variants,
            @NotNull final BakedModel[] guiVariants,
            @NotNull final ItemDisplayContext context)
        {
            super(originalModel);
            this.variants = variants;
            this.guiVariants = guiVariants;
            this.context = context;
        }

        @NotNull
        @Override
        public BakedModel applyTransform(@NotNull final ItemDisplayContext cameraTransformType, @NotNull final PoseStack poseStack, final boolean applyLeftHandTransform)
        {
            // this assumes that our base model doesn't switch models, which is true for standard flat models.
            super.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);   // update pose

            return cameraTransformType == context
                ? this
                : new BakedSliceHelper(originalModel, variants, guiVariants, cameraTransformType);
        }

        @NotNull
        @Override
        public List<BakedModel> getRenderPasses(@NotNull final ItemStack stack, final boolean fabulous)
        {
            final BakedModel[] v = context == ItemDisplayContext.GUI ? guiVariants : variants;

            if (context == ItemDisplayContext.GROUND)
            {
                // since the ground renderer renders >1 stacks as two items, it looks weird if we do our custom render,
                // so instead we just render as a single slice
                return List.of(v[0]);
            }

            // this is our opportunity to inspect the stack and pick a different model to render based on the count
            final int count = stack.getCount();
            final int index = Math.min(v.length - 1, count - 1);
            return List.of(v[index]);
        }
    }

    /**
     * Model loader for minecolonies:sliced_item.
     */
    public static class Loader implements IGeometryLoader<SlicedItemModel>
    {
        public static final Loader INSTANCE = new Loader();
        public static final ResourceLocation ID = new ResourceLocation(MOD_ID, "sliced_item");

        private Loader() {}

        @NotNull
        @Override
        public SlicedItemModel read(
            @NotNull final JsonObject jsonObject,
            @NotNull final JsonDeserializationContext deserializationContext) throws JsonParseException
        {
            final BlockModel baseModel = deserializationContext.deserialize(GsonHelper.getAsJsonObject(jsonObject, "base"), BlockModel.class);
            final int sliceCount = GsonHelper.getAsInt(jsonObject, "slices", 8);
            return new SlicedItemModel(baseModel, sliceCount);
        }
    }
}
