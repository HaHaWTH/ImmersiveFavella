package immersive_favella.resources;

import net.minecraft.network.PacketBuffer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Melody extends MelodyDescriptor {
    public static final Melody DEFAULT = new Melody();

    private final List<Track> tracks = new LinkedList<Track>();

    public Melody() {
        super("unknown");
        addTrack(new Track("unknown", new LinkedList<Note>()));
    }

    public Melody(String name) {
        super(name);
    }

    public Melody(PacketBuffer b) {
        super(b);
        int trackCount = b.readInt();
        for (int i = 0; i < trackCount; i++) {
            tracks.add(new Track(b));
        }
    }

    public List<Track> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    public int getLength() {
        int length = 0;
        for (Track track : tracks) {
            length = Math.max(length, track.getLength());
        }
        return length;
    }

    public void addTrack(Track track) {
        tracks.add(track);
    }

    public void encode(PacketBuffer b) {
        super.encodeLite(b);
        b.writeInt(tracks.size());
        for (Track track : tracks) {
            track.encode(b);
        }
    }

    public void trim() {
        int offset = getFirstNoteTime();
        for (Track track : tracks) {
            List<Note> adjusted = new LinkedList<>();
            for (Note note : track.getNotes()) {
                adjusted.add(new Note(note.getNote(), note.getVelocity(), note.getTime() - offset, note.getLength(), note.getSustain()));
            }
            track.setNotes(adjusted);
        }
    }

    private int getFirstNoteTime() {
        int earliest = Integer.MAX_VALUE;
        for (Track track : tracks) {
            List<Note> notes = track.getNotes();
            if (!notes.isEmpty()) {
                earliest = Math.min(earliest, notes.get(0).getTime());
            }
        }
        return earliest == Integer.MAX_VALUE ? 0 : earliest;
    }
}
