package com.jlgm.structurepreview.event;


import com.schematica.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Set;

/**
 * EventHandler used to display the schematics on the client.
 */
public class RenderEventHandler
{

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        if(Settings.instance.getActiveStructure() != null)
        {
            BlockPos size = Settings.instance.getActiveStructure().getSize(getRotation(Settings.instance.getRotation()));
            BlockPos position = Settings.instance.pos;

            if(Settings.instance.getRotation() == 1)
            {
                size = new BlockPos(-size.getX(), size.getY(), size.getZ());
            }
            if(Settings.instance.getRotation() == 2)
            {
                size = new BlockPos(-size.getX(), size.getY(), -size.getZ());
            }
            if(Settings.instance.getRotation() == 3)
            {
                size = new BlockPos(size.getX(), size.getY(), -size.getZ());
            }


            if(position.getX() < 0)
            {
                position = position.add(size.getX()/2, 0, 0);
            }
            else
            {
                position = position.add(-size.getX()/2, 0, 0);
            }

            if(position.getZ() < 0)
            {
                position = position.add(0, 0, -size.getZ()/2);
            }
            else
            {
                position = position.add(size.getX()/2, 0, size.getZ()/2);
            }


            Settings.instance.getActiveStructure().renderStructure(position, Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer, 0);
        }
	}

    /**
     * Updates the rotation of the structure depending on the input.
     * @param rotation the rotation to be set.
     */
    private Rotation getRotation(final int rotation)
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
