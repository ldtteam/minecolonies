package com.minecolonies.coremod.client.render;

import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.api.tileentities.AbstractTileEntityScarecrow;
import com.minecolonies.api.tileentities.ScareCrowType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.BlockScarecrow;
import com.minecolonies.coremod.client.model.ScarecrowModel;
import com.minecolonies.coremod.event.ClientRegistryHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Vector3f;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Class to render the scarecrow.
 */
@OnlyIn(Dist.CLIENT)
public class TileEntityScarecrowRenderer implements BlockEntityRenderer<AbstractTileEntityScarecrow>
{
    /**
     * Offset to the block middle.
     */
    private static final double BLOCK_MIDDLE = 0.5;

    /**
     * Y-Offset in order to have the scarecrow over ground.
     */
    private static final double YOFFSET = 1.5;

    /**
     * Rotate the model some degrees.
     */
    private static final int ROTATION = 180;

    /**
     * Basic rotation to achieve a certain direction.
     */
    private static final int BASIC_ROTATION = 90;

    /**
     * Rotate by amount to go east.
     */
    private static final int ROTATE_EAST = 1;

    /**
     * Rotate by amount to go south.
     */
    private static final int ROTATE_SOUTH = 2;

    /**
     * Rotate by amount to go west.
     */
    private static final int ROTATE_WEST = 3;

    /**
     * The model of the scarecrow.
     */
    @NotNull
    private ScarecrowModel model;

    public static final Material SCARECROW_A;
    public static final Material       SCARECROW_B;
    static
    {
        SCARECROW_A = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(Constants.MOD_ID, "blocks/blockscarecrowpumpkin"));
        SCARECROW_B = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(Constants.MOD_ID, "blocks/blockscarecrownormal"));
    }
    /**
     * The public constructor for the renderer.
     *
     * @param context the render context.
     */
    public TileEntityScarecrowRenderer(final BlockEntityRendererProvider.Context context)
    {
        super();
        this.model = new ScarecrowModel(context.bakeLayer(ClientRegistryHandler.SCARECROW));
    }

    @Override
    public void render(
      final AbstractTileEntityScarecrow te,
      final float partialTicks,
      final PoseStack matrixStack,
      @NotNull final MultiBufferSource iRenderTypeBuffer,
      final int lightA,
      final int lightB)
    {
        if (te.getBlockState().getValue(BlockScarecrow.HALF) == DoubleBlockHalf.UPPER)
        {
            return;
        }
        //Store the transformation
        matrixStack.pushPose();
        //Set viewport to tile entity position to render it
        matrixStack.translate(BLOCK_MIDDLE, YOFFSET, BLOCK_MIDDLE);
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(ROTATION));

        //In the case of worldLags tileEntities may sometimes disappear.
        if (te.getLevel().getBlockState(te.getBlockPos()).getBlock() instanceof BlockScarecrow)
        {
            final Direction facing = te.getLevel().getBlockState(te.getBlockPos()).getValue(AbstractBlockMinecoloniesDefault.FACING);
            switch (facing)
            {
                case EAST:
                    matrixStack.mulPose(Vector3f.YP.rotationDegrees(BASIC_ROTATION * ROTATE_EAST));
                    break;
                case SOUTH:
                    matrixStack.mulPose(Vector3f.YP.rotationDegrees(BASIC_ROTATION * ROTATE_SOUTH));
                    break;
                case WEST:
                    matrixStack.mulPose(Vector3f.YP.rotationDegrees(BASIC_ROTATION * ROTATE_WEST));
                    break;
                default:
                    //don't rotate at all.
            }
        }

        final VertexConsumer vertexConsumer = getMaterial(te).buffer(iRenderTypeBuffer, RenderType::entitySolid);
        this.model.renderToBuffer(matrixStack, vertexConsumer, lightA, lightB, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.popPose();
    }

    /**
     * Returns the Material of the scarecrow texture.
     *
     * @param tileEntity the tileEntity of the scarecrow.
     * @return the material.
     */
    @NotNull
    private static Material getMaterial(@NotNull final AbstractTileEntityScarecrow tileEntity)
    {
        if (tileEntity.getScarecrowType() == ScareCrowType.PUMPKINHEAD)
        {
            return SCARECROW_A;
        }
        else
        {
            return SCARECROW_B;
        }
    }
}
