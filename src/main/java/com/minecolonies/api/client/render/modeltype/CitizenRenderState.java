package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class CitizenRenderState extends HumanoidRenderState
{
    private AbstractEntityCitizen citizen;
    public boolean customHeadHidden;
    public boolean working;
    public boolean studying;
    public boolean bookVisible;
    public boolean flowersVisible;
    public boolean potionVisible;
    public boolean carrotVisible;
    public boolean logsVisible;
    public boolean arrowVisible;
    public boolean bucketVisible;
    public boolean backpackVisible;
    public boolean mainHandEmpty;
    public boolean chestEquipmentAbsent;
    public boolean stoneLidHidden;
    public boolean torchesVisible;
    public boolean shovelVisible;
    public boolean pickaxeVisible;
    public boolean fishingPoleVisible;
    public boolean fishVisible;
    public float actualBodyRotation;

    public AbstractEntityCitizen getCitizen()
    {
        return citizen;
    }

    public void setCitizen(final AbstractEntityCitizen citizen)
    {
        this.citizen = citizen;
    }
}
