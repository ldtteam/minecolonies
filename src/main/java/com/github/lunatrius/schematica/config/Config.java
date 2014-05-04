package com.github.lunatrius.schematica.config;

import com.github.lunatrius.core.config.Configuration;
import com.github.lunatrius.core.lib.Reference;
import com.github.lunatrius.schematica.Schematica;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.io.IOException;

public class Config extends Configuration {
    public final Property propEnableAlpha;
    public final Property propAlpha;
    public final Property propHighlight;
    public final Property propHighlightAir;
    public final Property propBlockDelta;
    public final Property propPlaceDelay;
    public final Property propTimeout;
    public final Property propPlaceInstantly;
    public final Property propPlaceAdjacent;
    public final Property propDrawQuads;
    public final Property propDrawLines;
    public final Property propSchematicDirectory;

    public boolean enableAlpha        = false;
    public float   alpha              = 1.0f;
    public boolean highlight          = true;
    public boolean highlightAir       = true;
    public float   blockDelta         = 0.005f;
    public int     placeDelay         = 1;
    public int     timeout            = 10;
    public boolean placeInstantly     = false;
    public boolean placeAdjacent      = true;
    public boolean drawQuads          = true;
    public boolean drawLines          = true;
    public File    schematicDirectory = new File(Schematica.proxy.getDataDirectory(), "schematics");

    public Config(File file)
    {
        super(file);

        String directory;
        try
        {
            directory = this.schematicDirectory.getCanonicalPath();
        }
        catch(IOException e)
        {
            Reference.logger.info("Failed to get path!");
            directory = this.schematicDirectory.getAbsolutePath();
        }

        this.propEnableAlpha = get("general", "alphaEnabled", this.enableAlpha, "Enable transparent textures.");
		this.propAlpha = get("general", "alpha", this.alpha, 0.0, 1.0, "Alpha value used when rendering the schematic (example: 1.0 = opaque, 0.5 = half transparent, 0.0 = transparent).");
		this.propHighlight = get("general", "highlight", this.highlight, "Highlight invalid placed blocks and to be placed blocks.");
		this.propHighlightAir = get("general", "highlightAir", this.highlightAir, "Highlight invalid placed blocks (where there should be no block).");
		this.propBlockDelta = get("general", "blockDelta", this.blockDelta, 0.0, 0.5, "Delta value used for highlighting (if you're having issue with overlapping textures try setting this value higher).");
		this.propPlaceDelay = get("general", "placeDelay", this.placeDelay, 0, 20, "Delay in ticks between placement attempts.");
		this.propTimeout = get("general", "timeout", this.timeout, 0, 100, "Timeout before re-trying failed blocks.");
		this.propPlaceInstantly = get("general", "placeInstantly", this.placeInstantly, "Place all blocks that can be placed in one tick.");
		this.propPlaceAdjacent = get("general", "placeAdjacent", this.placeAdjacent, "Place blocks only if there is an adjacent block next to it.");
		this.propDrawQuads = get("general", "drawQuads", this.drawQuads, "Draw surface areas.");
		this.propDrawLines = get("general", "drawLines", this.drawLines, "Draw outlines.");
		this.propSchematicDirectory = get("general", "schematicDirectory", directory, "Schematic directory.");

		this.enableAlpha = this.propEnableAlpha.getBoolean(this.enableAlpha);
		this.alpha = (float) this.propAlpha.getDouble(this.alpha);
		this.highlight = this.propHighlight.getBoolean(this.highlight);
		this.highlightAir = this.propHighlightAir.getBoolean(this.highlightAir);
		this.blockDelta = (float) this.propBlockDelta.getDouble(this.blockDelta);
		this.placeDelay = this.propPlaceDelay.getInt(this.placeDelay);
		this.timeout = this.propTimeout.getInt(this.timeout);
		this.placeInstantly = this.propPlaceInstantly.getBoolean(this.placeInstantly);
		this.placeAdjacent = this.propPlaceAdjacent.getBoolean(this.placeAdjacent);
		this.drawQuads = this.propDrawQuads.getBoolean(this.drawQuads);
		this.drawLines = this.propDrawLines.getBoolean(this.drawLines);
		this.schematicDirectory = new File(this.propSchematicDirectory.getString());
	}
}
