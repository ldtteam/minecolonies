package com.minecolonies.coremod.client.render.mobs.pirates;

import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityChiefPirate;
import net.minecraft.client.renderer.entity.RenderManager;

/**
 * Renderer used for Chief Barbarians.
 */
public class RendererChiefPirate extends AbstractRendererPirate
{
    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererChiefPirate(final RenderManager renderManagerIn)
    {
        super(renderManagerIn, EntityChiefPirate.class);
    }
}
