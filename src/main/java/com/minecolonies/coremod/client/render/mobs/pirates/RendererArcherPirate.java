package com.minecolonies.coremod.client.render.mobs.pirates;

import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityPirate;
import net.minecraft.client.renderer.entity.RenderManager;

/**
 * Renderer used for Barbarians And Archer Barbarians.
 */
public class RendererArcherPirate extends AbstractRendererPirate
{
    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererArcherPirate(final RenderManager renderManagerIn)
    {
        super(renderManagerIn, EntityArcherPirate.class);
    }
}
