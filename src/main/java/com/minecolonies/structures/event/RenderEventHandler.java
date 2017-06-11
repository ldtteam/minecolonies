package com.minecolonies.structures.event;

import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.structures.helpers.Settings;
import com.minecolonies.structures.helpers.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * EventHandler used to display the schematics on the client.
 */
public class RenderEventHandler
{

    /**
     * Event used to render the schematics. Only render the schematic if there is one in the settings.
     *
     * @param event Object containing event details.
     */
    @SubscribeEvent
    public void onRenderWorldLast(final RenderWorldLastEvent event)
    {
        final Structure structure = Settings.instance.getActiveStructure();
        if (structure != null)
        {
            BlockPos size = structure.getSize(BlockUtils.getRotation(Settings.instance.getRotation()));
            BlockPos position = Settings.instance.getPosition();

            final int x = size.getX();
            final int z = size.getZ();
            final int y = size.getY();

            if (Settings.instance.getRotation() == 1)
            {
                size = new BlockPos(-x, y, z);
            }
            if (Settings.instance.getRotation() == 2)
            {
                size = new BlockPos(-x, y, -z);
            }
            if (Settings.instance.getRotation() == 3)
            {
                size = new BlockPos(x, y, -z);
            }

            final BlockPos offset = Settings.instance.getOffset(
                    new PlacementSettings().setRotation(BlockUtils.getRotation(Settings.instance.getRotation())).setMirror(Settings.instance.getMirror()));

            if (offset.equals(new BlockPos(0, 0, 0)))
            {
                position = position.subtract(new BlockPos(size.getX() / 2, 0, size.getZ() / 2));
            }
            else
            {
                position = position.subtract(offset);
            }
            structure.renderStructure(position, Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer, event.getPartialTicks());
        }
    }
}
