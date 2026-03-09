package immersive_favella.client.gui;

import immersive_favella.Common;
import immersive_favella.MidiListener;
import immersive_favella.item.InstrumentItem;
import immersive_favella.network.Network;
import immersive_favella.network.PacketSplitter;
import immersive_favella.network.c2s.ItemActionMessage;
import immersive_favella.network.c2s.MelodyDeleteMessage;
import immersive_favella.network.c2s.TrackToggleMessage;
import immersive_favella.resources.ClientMelodyManager;
import immersive_favella.resources.Melody;
import immersive_favella.resources.MelodyDescriptor;
import immersive_favella.resources.Track;
import immersive_favella.util.MidiParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class ImmersiveMelodiesScreen extends GuiScreen {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("immersive_favella", "textures/gui/paper.png");
    private final List<String> rows = new ArrayList<String>();
    private final List<String> trackRows = new ArrayList<String>();
    private final List<ResourceLocation> ids = new ArrayList<ResourceLocation>();
    private final Set<Integer> enabledTracks = new HashSet<Integer>();
    private String query = "";
    private int selectedTrack = -1;
    private int selectedIndex = -1;
    private int listScroll = 0;
    private int trackScroll = 0;
    private String status = "";
    private long statusAt = 0L;

    @Override
    public void initGui() {
        buttonList.clear();
        int centerX = width / 2;
        int centerY = height / 2;
        buttonList.add(new GuiButton(4, centerX - 75, centerY + 30, 73, 20, "Track +/-"));
        buttonList.add(new GuiButton(5, centerX + 2, centerY + 30, 73, 20, "Drop Upload"));
        buttonList.add(new GuiButton(6, centerX - 75, centerY + 54, 150, 20, "Upload .mid/.midi"));

        buttonList.add(new GuiButton(1, centerX - 75, centerY + 78, 36, 20, I18n.format("immersive_favella.play")));
        buttonList.add(new GuiButton(2, centerX - 37, centerY + 78, 36, 20, I18n.format("immersive_favella.pause")));
        buttonList.add(new GuiButton(3, centerX + 1, centerY + 78, 36, 20, I18n.format("immersive_favella.delete")));
        buttonList.add(new GuiButton(0, centerX + 39, centerY + 78, 36, 20, I18n.format("immersive_favella.close")));
        refreshPage();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            mc.displayGuiScreen(null);
        } else if (button.id == 1) {
            sendAction(ItemActionMessage.State.PLAY);
        } else if (button.id == 2) {
            sendAction(ItemActionMessage.State.PAUSE);
        } else if (button.id == 3) {
            if (selectedIndex >= 0 && selectedIndex < ids.size()) {
                Network.sendToServer(new MelodyDeleteMessage(ids.get(selectedIndex)));
            }
        } else if (button.id == 4) {
            if (selectedIndex >= 0 && selectedIndex < ids.size() && selectedTrack >= 0) {
                boolean enabled = !enabledTracks.contains(selectedTrack);
                Network.sendToServer(new TrackToggleMessage(ids.get(selectedIndex), selectedTrack, enabled));
                if (enabled) {
                    enabledTracks.add(selectedTrack);
                } else {
                    enabledTracks.remove(selectedTrack);
                }
                refreshTracks();
            }
        } else if (button.id == 5) {
            openDropUploadWindow();
        } else if (button.id == 6) {
            setStatus("Opening file picker...");
            uploadFromFileChooser();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        mc.getTextureManager().bindTexture(BACKGROUND);
        int panelW = 192;
        int panelH = 215;
        int x = (width - panelW) / 2;
        int y0 = (height - panelH) / 2;
        drawModalRectWithCustomSizedTexture(x, y0, 0, 0, panelW, panelH, 256, 256);

        drawCenteredString(fontRenderer, I18n.format("itemGroup.immersive_favella_tab"), width / 2, height / 2 - 90, 0xFFFFFF);
        drawCenteredString(fontRenderer, I18n.format("immersive_favella.keyboard"), width / 2, height / 2 - 70, 0xAAAAAA);
        drawString(fontRenderer, I18n.format("immersive_favella.search") + ": " + query, width / 2 - 90, height / 2 - 62, 0xCCCCCC);

        int listTop = height / 2 - 50;
        int listBottom = height / 2 + 20;
        int visibleRows = Math.max(1, (listBottom - listTop) / 10);
        int maxListScroll = Math.max(0, rows.size() - visibleRows);
        if (listScroll > maxListScroll) {
            listScroll = maxListScroll;
        }
        if (listScroll < 0) {
            listScroll = 0;
        }
        for (int i = 0; i < visibleRows; i++) {
            int rowIndex = i + listScroll;
            if (rowIndex >= rows.size()) {
                break;
            }
            int y = listTop + i * 10;
            int color = (rowIndex == selectedIndex) ? 0xFFFF55 : 0xDDDDDD;
            drawString(fontRenderer, rows.get(rowIndex), width / 2 - 90, y, color);
        }
        if (rows.size() > visibleRows) {
            drawString(fontRenderer, (listScroll + 1) + "/" + (maxListScroll + 1), width / 2 - 10, listBottom + 2, 0x888888);
        }

        int trackTop = height / 2 - 50;
        int trackBottom = height / 2 + 20;
        int visibleTracks = Math.max(1, (trackBottom - trackTop) / 10);
        int maxTrackScroll = Math.max(0, trackRows.size() - visibleTracks);
        if (trackScroll > maxTrackScroll) {
            trackScroll = maxTrackScroll;
        }
        if (trackScroll < 0) {
            trackScroll = 0;
        }
        for (int i = 0; i < visibleTracks; i++) {
            int trackIndex = i + trackScroll;
            if (trackIndex >= trackRows.size()) {
                break;
            }
            int ty = trackTop + i * 10;
            int color = enabledTracks.contains(trackIndex) ? 0x66FF66 : 0xAAAAAA;
            if (trackIndex == selectedTrack) {
                color = 0x55FFFF;
            }
            drawString(fontRenderer, trackRows.get(trackIndex), width / 2 + 20, ty, color);
        }

        if (status != null && !status.isEmpty() && System.currentTimeMillis() - statusAt < 5000L) {
            drawCenteredString(fontRenderer, status, width / 2, height / 2 + 84, 0xFFAA55);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void refreshPage() {
        rows.clear();
        ids.clear();
        List<Map.Entry<ResourceLocation, MelodyDescriptor>> list = new ArrayList<Map.Entry<ResourceLocation, MelodyDescriptor>>(ClientMelodyManager.getMelodiesList().entrySet());
        list.sort(new Comparator<Map.Entry<ResourceLocation, MelodyDescriptor>>() {
            @Override
            public int compare(Map.Entry<ResourceLocation, MelodyDescriptor> a, Map.Entry<ResourceLocation, MelodyDescriptor> b) {
                return a.getKey().toString().compareTo(b.getKey().toString());
            }
        });
        for (Map.Entry<ResourceLocation, MelodyDescriptor> entry : list) {
            String n = entry.getValue().getName();
            if (query.isEmpty() || n.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
                rows.add("- " + n);
                ids.add(entry.getKey());
            }
        }
        if (rows.isEmpty()) {
            rows.add("(no melodies)");
            selectedIndex = -1;
        } else if (selectedIndex >= rows.size()) {
            selectedIndex = rows.size() - 1;
        }

        refreshTracks();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int startY = height / 2 - 50;
        int endY = height / 2 + 20;
        int x0 = width / 2 - 90;
        int x1 = width / 2 + 10;
        if (mouseX >= x0 && mouseX <= x1 && mouseY >= startY && mouseY <= endY) {
            int idx = listScroll + (mouseY - startY) / 10;
            if (idx >= 0 && idx < ids.size()) {
                selectedIndex = idx;
                refreshTracks();
            }
        }

        int tx0 = width / 2 + 20;
        int tx1 = width / 2 + 110;
        if (mouseX >= tx0 && mouseX <= tx1 && mouseY >= startY && mouseY <= endY) {
            int idx = trackScroll + (mouseY - startY) / 10;
            if (idx >= 0 && idx < trackRows.size()) {
                selectedTrack = idx;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (keyCode == 14) {
            if (!query.isEmpty()) {
                query = query.substring(0, query.length() - 1);
                refreshPage();
            }
            return;
        }
        if (Character.isLetterOrDigit(typedChar) || typedChar == '_' || typedChar == ' ') {
            query += typedChar;
            refreshPage();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        super.handleKeyboardInput();
        int keyCode = Keyboard.getEventKey();
        if (keyCode == 0) {
            return;
        }
        if (Keyboard.getEventKeyState()) {
            MidiListener.keyPressed(keyCode);
        } else {
            MidiListener.keyReleased(keyCode);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel == 0) {
            return;
        }

        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int top = height / 2 - 50;
        int bottom = height / 2 + 20;
        int delta = dWheel > 0 ? -1 : 1;

        if (mx >= width / 2 - 90 && mx <= width / 2 + 10 && my >= top && my <= bottom) {
            listScroll += delta;
        } else if (mx >= width / 2 + 20 && mx <= width / 2 + 110 && my >= top && my <= bottom) {
            trackScroll += delta;
        }
    }

    private void sendAction(ItemActionMessage.State state) {
        if (Minecraft.getMinecraft().player == null) {
            return;
        }
        int slot = Minecraft.getMinecraft().player.inventory.currentItem;
        ResourceLocation melody = (selectedIndex >= 0 && selectedIndex < ids.size()) ? ids.get(selectedIndex) : new ResourceLocation("empty:empty");
        Network.sendToServer(new ItemActionMessage(slot, state, melody));
    }

    private void refreshTracks() {
        trackRows.clear();
        selectedTrack = -1;
        if (selectedIndex < 0 || selectedIndex >= ids.size()) {
            return;
        }

        enabledTracks.clear();
        if (mc != null && mc.player != null) {
            net.minecraft.item.ItemStack held = mc.player.getHeldItemMainhand();
            if (held.getItem() instanceof InstrumentItem) {
                enabledTracks.addAll(((InstrumentItem) held.getItem()).getEnabledTracks(held));
            }
        }

        Melody melody = ClientMelodyManager.getMelody(ids.get(selectedIndex));
        int idx = 0;
        for (Track track : melody.getTracks()) {
            boolean on = enabledTracks.isEmpty() || enabledTracks.contains(idx);
            trackRows.add((on ? "[ON] " : "[OFF] ") + idx + ": " + track.getName());
            idx++;
        }
    }

    private void uploadFromFileChooser() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (GraphicsEnvironment.isHeadless()) {
                        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                            @Override
                            public void run() {
                                setStatus("No desktop dialog support; using Drop Upload");
                                openDropUploadWindow();
                            }
                        });
                        return;
                    }

                    final java.util.concurrent.atomic.AtomicReference<File> selected = new java.util.concurrent.atomic.AtomicReference<File>();
                    EventQueue.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            Frame frame = new Frame();
                            frame.setAlwaysOnTop(true);
                            FileDialog dialog = new FileDialog(frame, "Select MIDI file", FileDialog.LOAD);
                            dialog.setFilenameFilter(new java.io.FilenameFilter() {
                                @Override
                                public boolean accept(File dir, String name) {
                                    String lower = name.toLowerCase(Locale.ROOT);
                                    return lower.endsWith(".mid") || lower.endsWith(".midi");
                                }
                            });
                            dialog.setAlwaysOnTop(true);
                            dialog.setVisible(true);

                            String file = dialog.getFile();
                            String dir = dialog.getDirectory();
                            dialog.dispose();
                            frame.dispose();

                            if (file != null && dir != null) {
                                selected.set(new File(dir, file));
                            }
                        }
                    });

                    final File file = selected.get();
                    Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                        @Override
                        public void run() {
                            if (file == null) {
                                setStatus("Upload canceled (or blocked by fullscreen)");
                                return;
                            }
                            uploadFromFile(file);
                        }
                    });
                } catch (Exception e) {
                    Common.LOGGER.error("Failed to open file dialog", e);
                    Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                        @Override
                        public void run() {
                            setStatus("Picker failed; opening Drop Upload");
                            openDropUploadWindow();
                        }
                    });
                }
            }
        }, "ImmersiveMelodies-FilePicker").start();
    }

    public void onFilesDrop(java.util.List<java.io.File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        File file = files.get(0);
        uploadFromFile(file);
    }

    private void uploadFromFile(File file) {
        try {
            String lower = file.getName().toLowerCase(Locale.ROOT);
            if (!(lower.endsWith(".mid") || lower.endsWith(".midi"))) {
                setStatus("File must be .mid/.midi");
                return;
            }
            InputStream in = Files.newInputStream(file.toPath());
            String name = file.getName();
            int dot = name.lastIndexOf('.');
            if (dot > 0) {
                name = name.substring(0, dot);
            }
            Melody melody = MidiParser.parseMidi(in, name);
            if (melody.getTracks().isEmpty()) {
                setStatus("Unable to parse file");
                return;
            }
            PacketSplitter.sendToServer(name, melody);
            setStatus("Dropped and uploaded: " + name);
        } catch (Exception e) {
            Common.LOGGER.error("Upload failed", e);
            setStatus("Upload failed: " + e.getClass().getSimpleName());
        }
    }

    private void openDropUploadWindow() {
        try {
            final Frame dropFrame = new Frame("Drop MIDI Here");
            dropFrame.setSize(360, 120);
            dropFrame.setAlwaysOnTop(true);
            dropFrame.setLocationRelativeTo(null);
            dropFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    dropFrame.dispose();
                }
            });

            new DropTarget(dropFrame, new DropTargetAdapter() {
                @Override
                public void drop(DropTargetDropEvent dtde) {
                    try {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        Transferable transferable = dtde.getTransferable();
                        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                            @SuppressWarnings("unchecked")
                            java.util.List<File> dropped = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                            if (!dropped.isEmpty()) {
                                uploadFromFile(dropped.get(0));
                            }
                        }
                        dtde.dropComplete(true);
                    } catch (Exception e) {
                        Common.LOGGER.error("Drop upload failed", e);
                        setStatus("Drop upload failed");
                        dtde.dropComplete(false);
                    }
                }

                @Override
                public void dragExit(DropTargetEvent dte) {
                    super.dragExit(dte);
                }
            });

            dropFrame.setVisible(true);
            setStatus("Drop window opened");
        } catch (Exception e) {
            Common.LOGGER.error("Failed to open drop window", e);
            setStatus("Drop window failed");
        }
    }

    private void setStatus(String text) {
        this.status = text;
        this.statusAt = System.currentTimeMillis();
    }
}
