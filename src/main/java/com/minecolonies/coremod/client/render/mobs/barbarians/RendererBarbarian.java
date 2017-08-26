package com.minecolonies.coremod.client.render.mobs.barbarians;

import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityBarbarian;
import net.minecraft.client.renderer.entity.RenderManager;

/**
 * Renderer used for Barbarians And Archer Barbarians.
 */
public class RendererBarbarian extends AbstractRendererBarbarian
{
    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererBarbarian(final RenderManager renderManagerIn)
    {
        super(renderManagerIn, EntityBarbarian.class);
    }
}
