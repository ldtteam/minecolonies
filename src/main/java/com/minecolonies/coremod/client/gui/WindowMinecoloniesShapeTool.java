package com.minecolonies.coremod.client.gui;

import com.ldtteam.structurize.client.gui.WindowShapeTool;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.coremod.network.messages.server.BuildToolPlaceMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import org.jetbrains.annotations.Nullable;

public class WindowMinecoloniesShapeTool extends WindowShapeTool
{
    public WindowMinecoloniesShapeTool(@Nullable BlockPos pos)
    {
        super(pos);
    }

    @Override
    public boolean hasPermission()
    {
        return true;
    }

    @Override
    protected void place()
    {
        final StructureName sn = save();

        // rotation/mirroring already happened server side
        final BuildToolPlaceMessage msg = new BuildToolPlaceMessage(
                sn.toString(),
                Settings.instance.getShape().toString(),
                Settings.instance.getPosition(),
                0,
                false,
                Mirror.NONE,
                Blocks.AIR.defaultBlockState());

        Minecraft.getInstance().tell(new WindowBuildDecoration(msg, Settings.instance.getPosition(), sn)::open);
    }
}
