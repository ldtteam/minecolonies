package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.modules.building.WindowMineGuardModule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Miner guard assignment module.
 */
public class MinerGuardAssignModuleView extends AbstractBuildingModuleView
{
    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BOWindow getWindow()
    {
        return new WindowMineGuardModule(this);
    }

    @Override
    public ResourceLocation getIconResourceLocation()
    {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/modules/sword.png");
    }
    
    @Override
    public Component getDesc()
    {
        return Component.translatable("com.minecolonies.coremod.gui.miner.guardassign");
    }
}
