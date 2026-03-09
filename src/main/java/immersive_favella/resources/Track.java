package immersive_favella.resources;

import net.minecraft.network.PacketBuffer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Track {
    private final List<Note> notes;
    private final String name;

    public Track(String name, List<Note> notes) {
        this.name = name;
        this.notes = notes;
    }

    public Track(PacketBuffer b) {
        this.name = b.readString(32767);
        int count = b.readInt();
        this.notes = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            this.notes.add(new Note(b));
        }
    }

    public String getName() {
        return name;
    }

    public List<Note> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public void encode(PacketBuffer b) {
        b.writeString(name);
        b.writeInt(notes.size());
        for (Note note : notes) {
            note.encode(b);
        }
    }

    public int getLength() {
        if (notes.isEmpty()) {
            return 0;
        }
        Note last = notes.get(notes.size() - 1);
        return last.getTime() + last.getLength();
    }

    public void setNotes(List<Note> newNotes) {
        notes.clear();
        notes.addAll(newNotes);
    }
}
