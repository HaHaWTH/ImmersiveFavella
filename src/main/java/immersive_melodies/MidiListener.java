package immersive_melodies;

import immersive_melodies.client.ClientPlayback;
import immersive_melodies.network.Network;
import immersive_melodies.network.c2s.NoteBroadcastMessage;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class MidiListener {
    private MidiListener() {
    }

    public static void launch() {
        Thread thread = new Thread(new ListenerTask(), "ImmersiveMelodies-MIDI");
        thread.setDaemon(true);
        thread.start();
    }

    private static class ListenerTask implements Runnable {
        private final Set<MidiDevice.Info> connected = Collections.synchronizedSet(new HashSet<MidiDevice.Info>());

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                try {
                    for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
                        if (connected.contains(info)) {
                            continue;
                        }
                        MidiDevice device = MidiSystem.getMidiDevice(info);
                        if (device.getMaxTransmitters() != 0) {
                            device.open();
                            Transmitter transmitter = device.getTransmitter();
                            transmitter.setReceiver(new MidiReceiver(connected, info));
                            connected.add(info);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static class MidiReceiver implements Receiver {
        private final Set<MidiDevice.Info> connected;
        private final MidiDevice.Info info;

        private MidiReceiver(Set<MidiDevice.Info> connected, MidiDevice.Info info) {
            this.connected = connected;
            this.info = info;
        }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                int cmd = sm.getCommand();
                if (cmd == ShortMessage.NOTE_ON) {
                    int note = sm.getData1();
                    int vel = sm.getData2();
                    if (ClientPlayback.playLocalNote(note, vel)) {
                        Network.sendToServer(new NoteBroadcastMessage(note, vel));
                    }
                } else if (cmd == ShortMessage.NOTE_OFF) {
                    int note = sm.getData1();
                    if (ClientPlayback.playLocalNote(note, 0)) {
                        Network.sendToServer(new NoteBroadcastMessage(note, 0));
                    }
                }
            }
        }

        @Override
        public void close() {
            connected.remove(info);
        }
    }

    public static void keyPressed(int keyCode) {
        Integer midi = Config.getInstance().scancodeToMidi.get(keyCode);
        if (midi != null && ClientPlayback.playLocalNote(midi, 127)) {
            Network.sendToServer(new NoteBroadcastMessage(midi, 127));
        }
    }

    public static void keyReleased(int keyCode) {
        Integer midi = Config.getInstance().scancodeToMidi.get(keyCode);
        if (midi != null && ClientPlayback.playLocalNote(midi, 0)) {
            Network.sendToServer(new NoteBroadcastMessage(midi, 0));
        }
    }
}
