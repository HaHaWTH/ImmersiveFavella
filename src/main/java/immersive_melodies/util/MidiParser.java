package immersive_melodies.util;

import immersive_melodies.Common;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.Note;
import immersive_melodies.resources.Track;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MidiParser {
    public static Melody parseMidi(InputStream inputStream, String midiName) {
        Melody melody = new Melody(midiName);
        try {
            Sequence sequence = MidiSystem.getSequence(inputStream);

            List<MidiEvent> sharedEvents = new LinkedList<MidiEvent>();
            for (javax.sound.midi.Track track : sequence.getTracks()) {
                for (MidiEvent event : getEvents(track)) {
                    if (event.getMessage() instanceof MetaMessage) {
                        MetaMessage m = (MetaMessage) event.getMessage();
                        if (m.getType() == 0x51) {
                            sharedEvents.add(event);
                        }
                    }
                }
            }

            int trackNr = 1;
            for (javax.sound.midi.Track midiTrack : sequence.getTracks()) {
                List<MidiEvent> events = getEvents(midiTrack);
                events.addAll(0, sharedEvents);
                events.sort(new Comparator<MidiEvent>() {
                    @Override
                    public int compare(MidiEvent a, MidiEvent b) {
                        return (int) (a.getTick() - b.getTick());
                    }
                });

                double bpm = 120;
                long lastTick = 0;
                double time = 0;
                String name = "Track " + trackNr;
                List<Note> notes = new LinkedList<Note>();
                HashMap<Integer, Note.Builder> currentNotes = new HashMap<Integer, Note.Builder>();

                for (MidiEvent event : events) {
                    long tick = event.getTick();
                    double deltaMs = ((tick - lastTick) * 60000.0) / (sequence.getResolution() * bpm);
                    time += deltaMs;
                    lastTick = tick;
                    int ms = (int) time;

                    MidiMessage message = event.getMessage();

                    if (message instanceof MetaMessage) {
                        MetaMessage meta = (MetaMessage) message;
                        byte[] data = meta.getData();
                        int type = meta.getType();
                        if (type == 0x03) {
                            String newName = new String(data).trim();
                            if (!newName.isEmpty()) {
                                name = newName;
                            }
                        } else if (type == 0x51 && data.length >= 3) {
                            int microsecondsPerBeat = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
                            bpm = Math.round(60000000.0f / microsecondsPerBeat);
                        }
                    }

                    if (message instanceof ShortMessage) {
                        ShortMessage sm = (ShortMessage) message;
                        int command = sm.getCommand();
                        if (command == ShortMessage.NOTE_ON && sm.getData2() == 0) {
                            command = ShortMessage.NOTE_OFF;
                        }

                        if (command == ShortMessage.NOTE_ON) {
                            int note = sm.getData1();
                            int velocity = sm.getData2();

                            if (currentNotes.containsKey(note)) {
                                Note.Builder previous = currentNotes.get(note);
                                previous.sustain = ms - previous.time;
                            }

                            currentNotes.put(note, new Note.Builder(note, velocity, ms));
                        } else if (command == ShortMessage.NOTE_OFF) {
                            int note = sm.getData1();
                            Note.Builder builder = currentNotes.remove(note);
                            if (builder != null) {
                                builder.length = ms - builder.time;
                                notes.add(builder.build());
                            }
                        }
                    }
                }

                if (!notes.isEmpty()) {
                    trackNr++;
                    notes.sort(new Comparator<Note>() {
                        @Override
                        public int compare(Note a, Note b) {
                            return Integer.compare(a.getTime(), b.getTime());
                        }
                    });
                    melody.addTrack(new Track(name, notes));
                }
            }
        } catch (Exception e) {
            Common.LOGGER.error("Failed to parse MIDI", e);
        }

        melody.trim();
        return melody;
    }

    private static List<MidiEvent> getEvents(javax.sound.midi.Track track) {
        List<MidiEvent> events = new LinkedList<MidiEvent>();
        for (int i = 0; i < track.size(); i++) {
            events.add(track.get(i));
        }
        return events;
    }
}
