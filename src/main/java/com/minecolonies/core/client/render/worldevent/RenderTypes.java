package com.minecolonies.core.client.render.worldevent;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public class RenderTypes
{
    private RenderTypes()
    {
    }

    public static RenderType worldEntityIcon(final Identifier texture)
    {
        return com.ldtteam.structurize.client.rendertask.util.RenderTypes.worldEntityIcon(texture);
    }
}
