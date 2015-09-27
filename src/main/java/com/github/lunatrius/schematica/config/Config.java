package com.github.lunatrius.schematica.config;

public class Config {

        public static boolean enableAlpha = false;
        public static float alpha = 0.5f;
        public static boolean highlight = true;
        public static boolean highlightAir = true;
        public static float blockDelta = 0.005f;
        public static boolean drawQuads = true;
        public static boolean drawLines = true;

//    public Config() {
//
//        this.propEnableAlpha = get("general", "alphaEnabled", this.enableAlpha, "Enable transparent textures.");
//        this.propAlpha = get("general", "alpha", this.alpha, 0.0, 1.0, "Alpha value used when rendering the schematic (example: 1.0 = opaque, 0.5 = half transparent, 0.0 = transparent).");
//        this.propHighlight = get("general", "highlight", this.highlight, "Highlight invalid placed blocks and to be placed blocks.");
//        this.propHighlightAir = get("general", "highlightAir", this.highlightAir, "Highlight invalid placed blocks (where there should be no block).");
//        this.propBlockDelta = get("general", "blockDelta", this.blockDelta, 0.0, 0.5, "Delta value used for highlighting (if you're having issue with overlapping textures try setting this value higher).");
//        this.propPlaceDelay = get("general", "placeDelay", this.placeDelay, 0, 20, "Delay in ticks between placement attempts.");
//        this.propTimeout = get("general", "timeout", this.timeout, 0, 100, "Timeout before re-trying failed blocks.");
//        this.propPlaceInstantly = get("general", "placeInstantly", this.placeInstantly, "Place all blocks that can be placed in one tick.");
//        this.propPlaceAdjacent = get("general", "placeAdjacent", this.placeAdjacent, "Place blocks only if there is an adjacent block next to it.");
//        this.propDrawQuads = get("general", "drawQuads", this.drawQuads, "Draw surface areas.");
//        this.propDrawLines = get("general", "drawLines", this.drawLines, "Draw outlines.");
//        this.propSchematicDirectory = get("general", "schematicDirectory", directory, "Schematic directory.");
//    }
}