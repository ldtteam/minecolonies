package com.minecolonies.coremod.client.render;

import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesBlockHutField;
import com.minecolonies.api.tileentities.ScareCrowType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.huts.BlockHutField;
import com.minecolonies.coremod.client.model.ModelScarecrowBoth;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

/**
 * Class to render the scarecrow.
 */
@SideOnly(Side.CLIENT)
public class TileEntityScarecrowRenderer extends TileEntitySpecialRenderer<ScarecrowTileEntity>
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
    private static final double SIZERATIO       = .0625;
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

    /**
     * The public constructor for the renderer.
     */
    public TileEntityScarecrowRenderer()
    {
        super();
        this.model = new ModelScarecrowBoth();
    }

    @Override
    public void render(final ScarecrowTileEntity te, final double x, final double y, final double z, final float partialTicks, final int destroyStage, final float alpha)
    {
        //Store the transformation
        GlStateManager.pushMatrix();
        //Set viewport to tile entity position to render it
        GlStateManager.translate(x + BLOCK_MIDDLE, y + YOFFSET, z + BLOCK_MIDDLE);

        this.bindTexture(getResourceLocation(te));

        GlStateManager.rotate(ROTATION, XROTATIONOFFSET, YROTATIONOFFSET, ZROTATIONOFFSET);

        //In the case of worldLags tileEntities may sometimes disappear.
        if (getWorld().getBlockState(te.getPos()).getBlock() instanceof BlockHutField)
        {
            final Direction facing = getWorld().getBlockState(te.getPos()).getValue(AbstractBlockMinecoloniesBlockHutField.FACING);
            switch (facing)
            {
                case EAST:
                    GlStateManager.rotate((float) (BASIC_ROTATION * ROTATE_EAST), 0, 1, 0);
                    break;
                case SOUTH:
                    GlStateManager.rotate((float) (BASIC_ROTATION * ROTATE_SOUTH), 0, 1, 0);
                    break;
                case WEST:
                    GlStateManager.rotate((float) (BASIC_ROTATION * ROTATE_WEST), 0, 1, 0);
                    break;
                default:
                    //don't rotate at all.
            }
        }

        this.model.render((float) SIZERATIO);

        /* ============ Rendering Code stops here =========== */
        //Restore the transformation, so other renderer's are not messed up.
        GlStateManager.popMatrix();
    }

    /**
     * Returns the ResourceLocation of the scarecrow texture.
     *
     * @param tileEntity the tileEntity of the scarecrow.
     * @return the location.
     */
    @NotNull
    private static ResourceLocation getResourceLocation(@NotNull final ScarecrowTileEntity tileEntity)
    {
        final String loc;

        if (tileEntity.getType() == ScareCrowType.PUMPKINHEAD)
        {
            loc = "textures/blocks/blockscarecrowpumpkin.png";
        }
        else
        {
            loc = "textures/blocks/blockscarecrownormal.png";
        }

        return new ResourceLocation(Constants.MOD_ID + ":" + loc);
    }
}
