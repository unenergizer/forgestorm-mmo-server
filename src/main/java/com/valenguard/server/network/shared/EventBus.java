package com.valenguard.server.network.shared;

import com.valenguard.server.util.Log;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventBus {

    private final Map<Byte, PacketListener> packetListenerMap = new ConcurrentHashMap<>();
    private final Queue<PacketData> decodedPackets = new ConcurrentLinkedQueue<>();

    /**
     * Prepares the server to listen to a particular packet.
     * Registers Opcodes found via annotations in the PacketListener class.
     *
     * @param packetListener The PacketListener we will listen for.
     */
    public void registerListener(PacketListener packetListener) {
        Opcode[] opcodes = packetListener.getClass().getAnnotationsByType(Opcode.class);
        if (opcodes.length != 1) throw new RuntimeException("Must have only one annotation.");
        packetListenerMap.put(opcodes[0].getOpcode(), packetListener);
    }

    public void decodeListenerOnNetworkThread(byte opcode, ClientHandler clientHandler) {
        PacketListener packetListener = getPacketListener(opcode);
        if (packetListener == null) return;
        PacketData packetData = packetListener.decodePacket(clientHandler);
        packetData.setOpcode(opcode);
        packetData.setPlayer(clientHandler.getPlayer());
        decodedPackets.add(packetData);
    }

    private void publishOnGameThread(PacketData packetData) {
        PacketListener packetListener = getPacketListener(packetData.getOpcode());
        if (packetListener == null) return;
        //noinspection unchecked
        packetListener.onEvent(packetData);
    }

    private PacketListener getPacketListener(byte opcode) {
        PacketListener packetListener = packetListenerMap.get(opcode);
        if (packetListener == null)
            Log.println(getClass(), "Callback data was null for " + opcode + ". Is the event registered?", true);
        return packetListener;
    }

    /**
     * This code is ran on the game thread.
     */
    public void gameThreadPublish() {
        PacketData packetData;
        while ((packetData = decodedPackets.poll()) != null) {
            publishOnGameThread(packetData);
        }
    }
}
