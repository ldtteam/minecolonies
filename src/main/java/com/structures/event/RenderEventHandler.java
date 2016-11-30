package com.structures.event;

import com.minecolonies.util.BlockUtils;
import com.structures.helpers.Settings;
import com.structures.helpers.Structure;
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
            BlockPos position = Settings.instance.pos;

            if (Settings.instance.getRotation() == 1)
            {
                size = new BlockPos(-size.getX(), size.getY(), size.getZ());
            }
            if (Settings.instance.getRotation() == 2)
            {
                size = new BlockPos(-size.getX(), size.getY(), -size.getZ());
            }
            if (Settings.instance.getRotation() == 3)
            {
                size = new BlockPos(size.getX(), size.getY(), -size.getZ());
            }
            final BlockPos offset = Settings.instance.getOffset(new PlacementSettings().setRotation(BlockUtils.getRotation(Settings.instance.getRotation())));

            if (offset.equals(new BlockPos(0, 0, 0)))
            {
                position = position.add(-size.getX() / 2, 0, 0 - size.getZ() / 2);
            }
            else
            {
                position = position.subtract(offset);
            }
            structure.renderStructure(position, Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer, event.getPartialTicks());
        }
    }
}
