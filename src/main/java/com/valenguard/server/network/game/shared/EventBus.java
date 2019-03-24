package com.valenguard.server.network.game.shared;

import com.valenguard.server.network.game.packet.in.PacketInCancelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.valenguard.server.util.Log.println;

public class EventBus {

    private final Map<Byte, PacketListener> packetListenerMap = new ConcurrentHashMap<>();

    private final Map<Class<? extends PacketListener>, List<PacketListener>> cancellingCallbacks = new ConcurrentHashMap<>();

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

    public void determineCanceling() {
        List<Class<? extends PacketListener>> allPacketListenerClasses = new ArrayList<>();
        packetListenerMap.values().forEach(packetListener ->  allPacketListenerClasses.add(packetListener.getClass()));
        for (PacketListener packetListener : packetListenerMap.values()) {
            if (packetListener instanceof PacketInCancelable) {

                // Determining which packets to exclude from the canceling method.
                List<Class<? extends PacketListener>> canceling = ((PacketInCancelable) packetListener).excludeCanceling();
                List<Class<? extends PacketListener>> exclusions = new ArrayList<>(allPacketListenerClasses);
                exclusions.removeIf(canceling::contains);

                for (Class<? extends PacketListener> excluded : exclusions) {
                    if (excluded.equals(packetListener.getClass())) continue;
                    if (!cancellingCallbacks.containsKey(excluded)) {
                        List<PacketListener> packetListeners = new ArrayList<>();
                        packetListeners.add(packetListener);
                        cancellingCallbacks.put(excluded, packetListeners);
                    } else {
                        cancellingCallbacks.get(excluded).add(packetListener);
                    }
                }
            }
        }
    }

    public void decodeListenerOnNetworkThread(byte opcode, ClientHandler clientHandler) {
        PacketListener packetListener = getPacketListener(opcode);
        if (packetListener == null) return;
        PacketData packetData = packetListener.decodePacket(clientHandler);
        packetData.setOpcode(opcode);
        packetData.setPlayer(clientHandler.getPlayer());
        decodedPackets.add(packetData);
    }

    private PacketListener getPacketListener(byte opcode) {
        PacketListener packetListener = packetListenerMap.get(opcode);
        if (packetListener == null)
            println(getClass(), "Callback data was null for " + opcode + ". Is the event registered?", true);
        return packetListener;
    }

    public void gameThreadPublish() {
        PacketData packetData;
        while ((packetData = decodedPackets.poll()) != null) {
            publishOnGameThread(packetData);
        }
    }


    @SuppressWarnings("unchecked")
    private void publishOnGameThread(PacketData packetData) {
        PacketListener packetListener = getPacketListener(packetData.getOpcode());
        if (packetListener == null) return;
        if (!packetListener.sanitizePacket(packetData)) return;
        List<PacketListener> cancelableListeners = cancellingCallbacks.get(packetListener.getClass());

        // The listeners have request to be canceled for the current incoming
        // packet type.
        if (cancelableListeners != null) {
            if (cancelableListeners.contains(packetListener))
                ((PacketInCancelable) packetListener).onCancel(packetData.getPlayer());
        }
        packetListener.onEvent(packetData);
    }
}
