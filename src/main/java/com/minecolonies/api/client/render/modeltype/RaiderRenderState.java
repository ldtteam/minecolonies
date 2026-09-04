package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class RaiderRenderState extends HumanoidRenderState
{
    private AbstractEntityMinecoloniesMonster raider;
    private int textureId;

    public AbstractEntityMinecoloniesMonster getRaider()
    {
        return raider;
    }

    public void setRaider(final AbstractEntityMinecoloniesMonster raider)
    {
        this.raider = raider;
        this.textureId = raider.getTextureId();
    }

    public int getTextureId()
    {
        return textureId;
    }
}
