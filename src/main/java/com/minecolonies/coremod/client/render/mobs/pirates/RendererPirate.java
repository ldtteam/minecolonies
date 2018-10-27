package com.minecolonies.coremod.client.render.mobs.pirates;

import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityPirate;
import net.minecraft.client.renderer.entity.RenderManager;

/**
 * Renderer used for Barbarians And Archer Barbarians.
 */
public class RendererPirate extends AbstractRendererPirate
{
    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererPirate(final RenderManager renderManagerIn)
    {
        super(renderManagerIn, EntityPirate.class);
    }
}
