package com.schematica.core.client.renderer;

public final class GeometryMasks
{
    public static final class Quad
    {
        public static final int DOWN = 0x01;
        public static final int UP = 0x02;
        public static final int NORTH = 0x04;
        public static final int SOUTH = 0x08;
        public static final int WEST = 0x10;
        public static final int EAST = 0x20;
        public static final int ALL = DOWN | UP | NORTH | SOUTH | WEST | EAST;
    }

    public static final class Line
    {
        public static final int DOWN_WEST = 0x11;
        public static final int UP_WEST = 0x12;
        public static final int DOWN_EAST = 0x21;
        public static final int UP_EAST = 0x22;
        public static final int DOWN_NORTH = 0x05;
        public static final int UP_NORTH = 0x06;
        public static final int DOWN_SOUTH = 0x09;
        public static final int UP_SOUTH = 0x0A;
        public static final int NORTH_WEST = 0x14;
        public static final int NORTH_EAST = 0x24;
        public static final int SOUTH_WEST = 0x18;
        public static final int SOUTH_EAST = 0x28;
        public static final int ALL = DOWN_WEST | UP_WEST | DOWN_EAST | UP_EAST | DOWN_NORTH | UP_NORTH | DOWN_SOUTH | UP_SOUTH | NORTH_WEST | NORTH_EAST | SOUTH_WEST | SOUTH_EAST;
    }
}
