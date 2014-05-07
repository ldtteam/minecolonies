package com.minecolonies.network.packets;

import ibxm.Player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Colton on 5/6/2014.
 */
public abstract class APacket {

    public abstract void handle(DataInputStream data, Player player);//Possibly change to EntityPlayer

    //Most packets will have a request and send method also

    public static String readString(DataInputStream dis) {
        String s = "";
        try {
            int length = dis.readInt();

            for (int i = 0; i < length; i++)
                s = s + dis.readChar();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return s;
    }

    public static void writeString(DataOutputStream dos, String s) {
        try {
            dos.writeInt(s.length());
            dos.writeChars(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}