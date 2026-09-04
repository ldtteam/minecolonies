package com.minecolonies.core.client.render.projectile;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
/**
 * Custom renderer for the fire arrows.
 */
public class FireArrowRenderer extends ArrowRenderer<AbstractArrow, FireArrowRenderer.FireArrowRenderState>
{
    /**
     * Array of different textures.
     */
    private static final Identifier[] RES = new Identifier[]
                                                    {
                                                      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/magicalarrows/magical_arrow1.png"),
                                                      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/magicalarrows/magical_arrow2.png"),
                                                      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/magicalarrows/magical_arrow3.png"),
                                                      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/magicalarrows/magical_arrow4.png"),
                                                      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/magicalarrows/magical_arrow5.png"),
                                                      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/magicalarrows/magical_arrow6.png")
                                                    };
    public FireArrowRenderer(final EntityRendererProvider.Context context)
    {
        super(context);
    }
    @NotNull
    @Override
    protected Identifier getTextureLocation(@NotNull final FireArrowRenderState state)
    {
        return RES[state.animationTick % 6];
    }
    @Override
    public FireArrowRenderState createRenderState()
    {
        return new FireArrowRenderState();
    }
    @Override
    public void extractRenderState(@NotNull final AbstractArrow entity,
                                   @NotNull final FireArrowRenderState state,
                                   final float partialTicks)
    {
        super.extractRenderState(entity, state, partialTicks);
        state.animationTick = entity.tickCount;
    }
    public static class FireArrowRenderState extends ArrowRenderState
    {
        public int animationTick;
    }
}
