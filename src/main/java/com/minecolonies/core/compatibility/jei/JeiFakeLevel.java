package com.minecolonies.core.compatibility.jei;

import com.ldtteam.common.fakelevel.SingleBlockFakeLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

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
