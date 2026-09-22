package com.putzwirk.artifacts_merging_multiloader.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.putzwirk.artifacts_merging_multiloader.Constants;
import com.putzwirk.artifacts_merging_multiloader.client.ClientIconCache;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MergeConfigScreen extends Screen {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int ITEM_H = 22;
    private static final int VIEW_TOP = 86;
    private final Screen parent;
    private final List<WorkingGroup> working = new ArrayList<>();
    private int selected = 0;
    private int scroll = 0;
    private int maxScroll = 0;
    private boolean barDrag = false;
    private EditBox countField;
    private EditBox newItemField;
    private Button addItemBtn;
    private final List<ItemRow> itemRows = new ArrayList<>();

    public static Screen create(Screen parent) {
        return new MergeConfigScreen(parent);
    }

    private MergeConfigScreen(Screen parent) {
        super(Component.translatable("artifactsmerging.config.title"));
        this.parent = parent;
        Path root = MergeConfigManager.root();
        for (MergeEntry entry : MergeConfigManager.groups()) {
            Path file = entry.sourceDir == null ? null
                : entry.sourceDir.resolve(entry.id.substring(entry.id.indexOf('/') + 1) + ".json");
            working.add(new WorkingGroup(file, new MergeEntry(entry.id, entry.icon, entry.count,
                entry.items, entry.names, entry.sourceDir)));
        }
        if (working.isEmpty() && root != null) {
            Path dir = root.resolve("custom");
            working.add(new WorkingGroup(dir.resolve("custom_0.json"),
                new MergeEntry("custom/custom_0", "", 2, new ArrayList<>(), new LinkedHashMap<>(), dir)));
        }
    }

    private WorkingGroup current() {
        if (working.isEmpty()) {
            return null;
        }
        if (selected < 0) {
            selected = 0;
        }
        if (selected >= working.size()) {
            selected = working.size() - 1;
        }
        return working.get(selected);
    }

    private void flushCurrent() {
        WorkingGroup group = current();
        if (group == null) {
            return;
        }
        if (countField != null) {
            group.entry.count = parseCount(countField.getValue(), group.entry.count);
        }
        List<String> out = new ArrayList<>();
        for (ItemRow row : itemRows) {
            String value = row.field.getValue().trim();
            if (!value.isEmpty() && !out.contains(value)) {
                out.add(value);
            }
        }
        if (newItemField != null) {
            String fresh = newItemField.getValue().trim();
            if (!fresh.isEmpty() && !out.contains(fresh)) {
                out.add(fresh);
            }
        }
        group.entry.items = out;
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        itemRows.clear();
        int tabX = 14;
        for (int i = 0; i < working.size(); i++) {
            final int idx = i;
            Button tab = Button.builder(Component.literal(String.valueOf(i + 1)), button -> selectTab(idx))
                .bounds(tabX, 34, 30, 20).build();
            tab.active = idx != selected;
            addRenderableWidget(tab);
            tabX += 34;
        }
        addRenderableWidget(Button.builder(Component.literal("+"), button -> addGroup()).bounds(tabX, 34, 30, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("artifactsmerging.config.save"), button -> saveAndClose())
            .bounds(width - 192, 12, 92, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("artifactsmerging.config.cancel"), button -> closeToParent())
            .bounds(width - 96, 12, 92, 20).build());
        WorkingGroup group = current();
        if (group == null) {
            layoutRows();
            return;
        }
        addRenderableWidget(Button.builder(Component.literal("X"), button -> deleteGroup()).bounds(width - 30, 58, 20, 18).build());
        countField = new EditBox(font, 0, 0, 44, 18, Component.empty());
        countField.setMaxLength(1);
        countField.setValue(String.valueOf(group.entry.count));
        addRenderableWidget(countField);
        for (String id : group.entry.items) {
            ItemRow row = new ItemRow(id);
            itemRows.add(row);
            addRenderableWidget(row.field);
            addRenderableWidget(row.del);
        }
        newItemField = new EditBox(font, 0, 0, 260, 18, Component.empty());
        newItemField.setMaxLength(128);
        addRenderableWidget(newItemField);
        addItemBtn = Button.builder(Component.translatable("artifactsmerging.config.add"), button -> {
            flushCurrent();
            init();
        }).bounds(0, 0, 50, 18).build();
        addRenderableWidget(addItemBtn);
        layoutRows();
    }

    private void selectTab(int idx) {
        flushCurrent();
        selected = idx;
        scroll = 0;
        init();
    }

    private void addGroup() {
        flushCurrent();
        Path root = MergeConfigManager.root();
        Path file = root == null ? null : root.resolve("custom/custom_" + working.size() + ".json");
        working.add(new WorkingGroup(file, new MergeEntry("custom/custom_" + working.size(), "", 2, new ArrayList<>(), new LinkedHashMap<>(), root == null ? null : root.resolve("custom"))));
        selected = working.size() - 1;
        scroll = 0;
        init();
    }

    private void deleteGroup() {
        if (working.size() <= 1) {
            return;
        }
        working.remove(selected);
        if (selected >= working.size()) {
            selected = working.size() - 1;
        }
        scroll = 0;
        init();
    }

    private void layoutRows() {
        int bottom = height - 20;
        int y = VIEW_TOP - scroll;
        if (countField != null) {
            setBox(countField, 62, y, bottom);
        }
        y += 24;
        y = layoutItems(y, bottom);
        int contentH = y + scroll - VIEW_TOP + 20;
        int viewH = Math.max(0, bottom - VIEW_TOP);
        maxScroll = Math.max(0, contentH - viewH);
        if (scroll > maxScroll) {
            scroll = maxScroll;
        }
        if (scroll < 0) {
            scroll = 0;
        }
    }

    private int layoutItems(int y, int bottom) {
        y += 14;
        for (ItemRow row : itemRows) {
            setBox(row.field, 48, y, bottom);
            setBtn(row.del, width - 44, y, bottom);
            y += ITEM_H;
        }
        if (newItemField != null) {
            setBox(newItemField, 48, y, bottom);
            if (addItemBtn != null) {
                setBtn(addItemBtn, 314, y, bottom);
            }
            y += ITEM_H;
        }
        return y;
    }

    private void scrollTo(double mouseY) {
        int bottom = height - 20;
        int viewH = Math.max(1, bottom - VIEW_TOP);
        int contentH = viewH + maxScroll;
        int thumbH = Math.max(20, viewH * viewH / Math.max(1, contentH));
        int travel = Math.max(1, viewH - thumbH);
        double ratio = (mouseY - VIEW_TOP - thumbH / 2.0) / travel;
        ratio = Math.max(0.0, Math.min(1.0, ratio));
        scroll = (int) (ratio * maxScroll);
        layoutRows();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.button() == 0 && maxScroll > 0 && mouseX >= width - 16 && mouseX <= width - 6 && mouseY >= VIEW_TOP && mouseY <= height - 20) {
            barDrag = true;
            scrollTo(mouseY);
            return true;
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (barDrag) {
            scrollTo(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        barDrag = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseY >= VIEW_TOP && mouseY <= height - 20) {
            scroll = (int) Math.max(0, Math.min(maxScroll, scroll - (long) (scrollY * ITEM_H)));
            layoutRows();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void saveAndClose() {
        flushCurrent();
        for (WorkingGroup group : working) {
            if (group.file == null) {
                continue;
            }
            try {
                Files.createDirectories(group.file.getParent());
                Map<String, Object> json = new LinkedHashMap<>();
                if (group.entry.hasIconReference()) {
                    json.put("icon", group.entry.icon);
                }
                json.put("count", Math.max(1, Math.min(9, group.entry.count)));
                json.put("items", group.entry.items);
                if (!group.entry.names.isEmpty()) {
                    json.put("translations", group.entry.names);
                }
                try (Writer writer = Files.newBufferedWriter(group.file)) {
                    GSON.toJson(json, writer);
                }
            } catch (Exception e) {
                Constants.LOG.warn("Failed to save merge config {}", group.file, e);
            }
        }
        Path root = MergeConfigManager.root();
        if (root != null && root.getParent() != null) {
            MergeConfigManager.load(root.getParent());
        }
        ClientIconCache.invalidate();
        closeToParent();
    }

    @Override
    public void onClose() {
        closeToParent();
    }

    private void closeToParent() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private int parseCount(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value.trim());
            return Math.max(1, Math.min(9, parsed));
        } catch (Exception e) {
            return fallback;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(font, Component.translatable("artifactsmerging.config.title"), 14, 12, 0xFFFFFFFF, false);
        WorkingGroup group = current();
        if (group == null) {
            return;
        }
        Component groupLabel = Component.literal("Group " + (selected + 1));
        int labelWidth = font.width(groupLabel);
        graphics.drawString(font, groupLabel, width - 30 - labelWidth - 8, 62, 0xFFFFFFFF, false);
        graphics.enableScissor(0, VIEW_TOP, width, height - 20);
        int y = VIEW_TOP - scroll;
        graphics.drawString(font, Component.translatable("artifactsmerging.config.count").append(":"), 14, y + 4, 0xFFFFFFFF, false);
        y += 24;
        paintSection(graphics, y, "artifactsmerging.config.items", itemRows.size(), height - 20);
        graphics.disableScissor();
        if (maxScroll > 0) {
            int bottom = height - 20;
            int viewH = Math.max(1, bottom - VIEW_TOP);
            int contentH = viewH + maxScroll;
            int thumbH = Math.max(20, viewH * viewH / contentH);
            int thumbY = VIEW_TOP + (viewH - thumbH) * scroll / Math.max(1, maxScroll);
            int barLeft = width - 14;
            int barRight = width - 8;
            graphics.fill(barLeft, VIEW_TOP, barRight, bottom, 0xFF101010);
            graphics.fill(barLeft, thumbY, barRight, thumbY + thumbH, 0xFF808080);
            graphics.fill(barLeft, thumbY, barRight - 1, thumbY + thumbH - 1, 0xFFA0A0A0);
        }
    }

    private int paintSection(GuiGraphics graphics, int y, String key, int rows, int bottom) {
        graphics.drawString(font, Component.translatable(key).append(":"), 14, y, 0xFFFFFFFF, false);
        y += 14;
        for (int k = 0; k < rows; k++) {
            if (y >= VIEW_TOP && y <= bottom) {
                graphics.drawString(font, Component.literal((k + 1) + "."), 24, y + 5, 0xFF777777, false);
            }
            y += ITEM_H;
        }
        y += ITEM_H;
        return y;
    }

    private void setBox(EditBox widget, int x, int y, int bottom) {
        widget.setX(x);
        widget.setY(y);
        boolean visible = y >= VIEW_TOP && y <= bottom;
        widget.visible = visible;
        widget.active = visible;
    }

    private void setBtn(Button widget, int x, int y, int bottom) {
        widget.setX(x);
        widget.setY(y);
        boolean visible = y >= VIEW_TOP && y <= bottom;
        widget.visible = visible;
        widget.active = visible;
    }

    private static class WorkingGroup {
        @Nullable
        final Path file;
        final MergeEntry entry;

        WorkingGroup(@Nullable Path file, MergeEntry entry) {
            this.file = file;
            this.entry = entry;
        }
    }

    private class ItemRow {
        final String first;
        final EditBox field;
        final Button del;

        ItemRow(String id) {
            first = id;
            field = new EditBox(font, 0, 0, 260, 18, Component.empty());
            field.setMaxLength(128);
            field.setValue(id);
            del = Button.builder(Component.literal("X"), button -> removeMe()).bounds(0, 0, 20, 18).build();
        }

        void removeMe() {
            flushCurrent();
            WorkingGroup group = current();
            if (group != null) {
                group.entry.items.remove(first);
                group.entry.items.remove(field.getValue().trim());
            }
            init();
        }
    }
}
