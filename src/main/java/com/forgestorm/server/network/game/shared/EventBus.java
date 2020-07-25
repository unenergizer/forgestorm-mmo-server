package com.forgestorm.server.network.game.shared;

import com.forgestorm.server.network.game.packet.AllowNullPlayer;
import com.forgestorm.server.network.game.packet.in.PacketInCancelable;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.forgestorm.server.util.Log.println;

public class EventBus {

    private static final boolean PRINT_DEBUG = false;

    @AllArgsConstructor
    private class PacketListenerData {
        private PacketListener packetListener;
        private boolean ensureNonNullPlayer;
    }

    private final Map<Byte, PacketListenerData> packetListenerMap = new ConcurrentHashMap<>();

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
        boolean ensureNonNullPlayer = packetListener.getClass().getAnnotationsByType(AllowNullPlayer.class).length <= 0;
        if (opcodes.length != 1) throw new RuntimeException("Must have only one annotation.");
        packetListenerMap.put(opcodes[0].getOpcode(), new PacketListenerData(packetListener, ensureNonNullPlayer));
    }

    public void determineCanceling() {
        List<Class<? extends PacketListener>> allPacketListenerClasses = new ArrayList<>();
        packetListenerMap.values().forEach(packetListenerData -> allPacketListenerClasses.add(packetListenerData.packetListener.getClass()));
        for (PacketListenerData packetListenerData : packetListenerMap.values()) {
            if (packetListenerData instanceof PacketInCancelable) {

                // Determining which packets to exclude from the canceling method.
                List<Class<? extends PacketListener>> canceling = ((PacketInCancelable) packetListenerData).excludeCanceling();
                List<Class<? extends PacketListener>> exclusions = new ArrayList<>(allPacketListenerClasses);
                exclusions.removeIf(canceling::contains);

                for (Class<? extends PacketListener> excluded : exclusions) {
                    if (excluded.equals(packetListenerData.getClass())) continue;
                    if (!cancellingCallbacks.containsKey(excluded)) {
                        List<PacketListener> packetListeners = new ArrayList<>();
                        packetListeners.add(packetListenerData.packetListener);
                        cancellingCallbacks.put(excluded, packetListeners);
                    } else {
                        cancellingCallbacks.get(excluded).add(packetListenerData.packetListener);
                    }
                }
            }
        }
    }

    public void decodeListenerOnNetworkThread(byte opcode, ClientHandler clientHandler) {
        PacketListenerData packetListenerData = getPacketListenerData(opcode);
        println(getClass(), "PACKET IN: " + packetListenerData.packetListener, false, PRINT_DEBUG);
        if (packetListenerData == null) return;
        PacketData packetData = packetListenerData.packetListener.decodePacket(clientHandler);
        packetData.setOpcode(opcode);
        packetData.setClientHandler(clientHandler);
        decodedPackets.add(packetData);
    }

    private PacketListenerData getPacketListenerData(byte opcode) {
        PacketListenerData packetListenerData = packetListenerMap.get(opcode);
        if (packetListenerData == null)
            println(getClass(), "Callback io was null for " + opcode + ". Is the event registered?", true);
        return packetListenerData;
    }

    public void gameThreadPublish() {
        PacketData packetData;
        while ((packetData = decodedPackets.poll()) != null) {
            publishOnGameThread(packetData);
        }
    }

    @SuppressWarnings("unchecked")
    private void publishOnGameThread(PacketData packetData) {
        PacketListenerData packetListenerData = getPacketListenerData(packetData.getOpcode());
        if (packetListenerData == null) return;
        if (packetListenerData.ensureNonNullPlayer && packetData.getClientHandler().getPlayer() == null) return;
        if (!packetListenerData.packetListener.sanitizePacket(packetData)) return;
        List<PacketListener> cancelableListeners = cancellingCallbacks.get(packetListenerData.packetListener.getClass());

        // The listeners have request to be canceled for the current incoming
        // packet type.
        if (cancelableListeners != null) {
            if (cancelableListeners.contains(packetListenerData))
                ((PacketInCancelable) packetListenerData).onCancel(packetData.getClientHandler().getPlayer());
        }
        packetListenerData.packetListener.onEvent(packetData);
    }
}
