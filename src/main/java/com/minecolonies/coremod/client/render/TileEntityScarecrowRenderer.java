package com.minecolonies.coremod.client.render;

import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.api.tileentities.AbstractScarescrowTileEntity;
import com.minecolonies.api.tileentities.ScareCrowType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.BlockScarecrow;
import com.minecolonies.coremod.client.model.ModelScarecrowBoth;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Class to render the scarecrow.
 */
@OnlyIn(Dist.CLIENT)
public class TileEntityScarecrowRenderer extends TileEntityRenderer<AbstractScarescrowTileEntity>
{
    /**
     * Offset to the block middle.
     */
    private static final double BLOCK_MIDDLE    = 0.5;
    /**
     * Y-Offset in order to have the scarecrow over ground.
     */
    private static final double YOFFSET         = 1.5;
    /**
     * Which size the scarecrow should have ingame.
     */
    private static final float SIZERATIO       = .0625F;
    /**
     * Rotate the model some degrees.
     */
    private static final int    ROTATION        = 180;
    /**
     * Rotate it on the following x offset.
     */
    private static final float  XROTATIONOFFSET = 0.311F;
    /**
     * Rotate it on the following y offset.
     */
    private static final float  YROTATIONOFFSET = 0.0F;
    /**
     * Rotate it on the following z offset.
     */
    private static final float  ZROTATIONOFFSET = 2.845F;
    /**
     * Basic rotation to achieve a certain direction.
     */
    private static final int    BASIC_ROTATION  = 90;
    /**
     * Rotate by amount to go east.
     */
    private static final int    ROTATE_EAST     = 1;
    /**
     * Rotate by amount to go south.
     */
    private static final int    ROTATE_SOUTH    = 2;
    /**
     * Rotate by amount to go west.
     */
    private static final int    ROTATE_WEST     = 3;
    /**
     * The model of the scarecrow.
     */
    @NotNull
    private final ModelScarecrowBoth model;

    public static final Material SCARECROW_A;
    public static final Material SCARECROW_B;

    static
    {
        SCARECROW_A = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(Constants.MOD_ID, "textures/blocks/blockscarecrowpumpkin.png"));
        SCARECROW_B = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(Constants.MOD_ID, "textures/blocks/blockscarecrownormal.png"));
    }

    /**
     * The public constructor for the renderer.
     */
    public TileEntityScarecrowRenderer(final TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
        this.model = new ModelScarecrowBoth();
    }

    @Override
    public void render(
      final AbstractScarescrowTileEntity te,
      final float ratio,
      final MatrixStack matrixStack,
      @NotNull final IRenderTypeBuffer iRenderTypeBuffer,
      final int lightA,
      final int lightB)
    {
        //Store the transformation
        matrixStack.push();
        //Set viewport to tile entity position to render it
        matrixStack.translate(te.getPos().getX() + BLOCK_MIDDLE, te.getPos().getY() + YOFFSET, te.getPos().getZ() + BLOCK_MIDDLE);
        matrixStack.multiply(new Quaternion(ROTATION, XROTATIONOFFSET, YROTATIONOFFSET, ZROTATIONOFFSET));

        //In the case of worldLags tileEntities may sometimes disappear.
        if (te.getWorld().getBlockState(te.getPos()).getBlock() instanceof BlockScarecrow)
        {
            final Direction facing = te.getWorld().getBlockState(te.getPos()).get(AbstractBlockMinecoloniesDefault.FACING);
            switch (facing)
            {
                case EAST:
                    matrixStack.multiply(new Quaternion((float) (BASIC_ROTATION * ROTATE_EAST), 0, 1, 0));
                    break;
                case SOUTH:
                    matrixStack.multiply(new Quaternion((float) (BASIC_ROTATION * ROTATE_SOUTH), 0, 1, 0));
                    break;
                case WEST:
                    matrixStack.multiply(new Quaternion((float) (BASIC_ROTATION * ROTATE_WEST), 0, 1, 0));
                    break;
                default:
                    //don't rotate at all.
            }
        }

        matrixStack.scale(SIZERATIO, SIZERATIO, SIZERATIO);
        final IVertexBuilder vertexConsumer = getMaterial(te).getVertexConsumer(iRenderTypeBuffer, RenderType::getEntitySolid);
        this.model.render(matrixStack, vertexConsumer, lightA, lightB, 1.0F, 1.0F, 1.0F, 1.0F);

        /* ============ Rendering Code stops here =========== */
        //Restore the transformation, so other renderer's are not messed up.
        matrixStack.pop();
    }

    /**
     * Returns the Material of the scarecrow texture.
     *
     * @param tileEntity the tileEntity of the scarecrow.
     * @return the material.
     */
    @NotNull
    private static Material getMaterial(@NotNull final AbstractScarescrowTileEntity tileEntity)
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
