package com.minecolonies.core.compatibility.jei;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.fakelevel.FakeLevel;
import com.ldtteam.structurize.client.fakelevel.IFakeLevelLightProvider;
import net.minecraft.world.scores.Scoreboard;

public class JeiFakeLevel extends FakeLevel
{
    private static final Scoreboard SCOREBOARD = new Scoreboard();
    private static final Blueprint FAKE_BLOCK = new Blueprint((short) 1, (short) 1, (short) 1);

    public JeiFakeLevel()
    {
        super(FAKE_BLOCK, IFakeLevelLightProvider.USE_CLIENT_LEVEL, SCOREBOARD, false);
    }
}
