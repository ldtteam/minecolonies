package com.minecolonies.coremod.client.render.mobs.barbarians;

import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityChiefBarbarian;
import net.minecraft.client.renderer.entity.RenderManager;

/**
 * Renderer used for Chief Barbarians.
 */
public class RendererChiefBarbarian extends AbstractRendererBarbarian
{
    /**
     * Constructor method for renderer
     *
     * @param renderManagerIn the renderManager
     */
    public RendererChiefBarbarian(final RenderManager renderManagerIn)
    {
        super(renderManagerIn, EntityChiefBarbarian.class);
    }
}
