package com.minecolonies.core.compatibility.jei;

import com.ldtteam.structurize.client.fakelevel.SingleBlockFakeLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class JeiFakeLevel extends SingleBlockFakeLevel
{
    public JeiFakeLevel()
    {
        super(null);
        prepare(Blocks.AIR.defaultBlockState(), null, null);
    }

    @Override
    public Level realLevel()
    {
        return Minecraft.getInstance().level;
    }
}
