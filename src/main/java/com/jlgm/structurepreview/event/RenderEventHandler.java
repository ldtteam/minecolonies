package com.jlgm.structurepreview.event;

import com.jlgm.structurepreview.helpers.Settings;
import com.jlgm.structurepreview.helpers.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Rotation;
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
     * @param event Object containing event details.
     */
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        Structure structure = Settings.instance.getActiveStructure();
        if (structure != null)
        {
            BlockPos size = structure.getSize(getRotation(Settings.instance.getRotation()));
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
            BlockPos offset = Settings.instance.getOffset(new PlacementSettings().setRotation(getRotation(Settings.instance.getRotation())));

            if(offset.equals(new BlockPos(0,0,0)))
            {
                position = position.add(-size.getX() / 2, 0, 0 -size.getZ() / 2);
            }
            else
            {
                position = position.subtract(offset);
            }
            structure.renderStructure(position, Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer, event.getPartialTicks());
        }
    }

    /**
     * Updates the rotation of the structure depending on the input.
     *
     * @param rotation the rotation to be set.
     * @return returns the Rotation object.
     */
    private static Rotation getRotation(final int rotation)
    {
        switch (rotation)
        {
            case 1:
                return Rotation.CLOCKWISE_90;
            case 2:
                return Rotation.CLOCKWISE_180;
            case 3:
                return Rotation.COUNTERCLOCKWISE_90;
            default:
                return Rotation.NONE;
        }
    }
}
