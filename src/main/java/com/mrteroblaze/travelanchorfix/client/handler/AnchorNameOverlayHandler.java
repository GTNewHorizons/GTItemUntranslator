package com.mrteroblaze.travelanchorfix.client.handler;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import com.enderio.core.common.util.BlockCoord;

import crazypants.enderio.api.teleport.ITravelAccessable;
import crazypants.enderio.api.teleport.TravelSource;
import crazypants.enderio.teleport.TravelController;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@SideOnly(Side.CLIENT)
public class AnchorNameOverlayHandler {

    // Данные, накопленные на RenderWorldLastEvent и отрисовываемые в HUD
    private static class Label {
        final String text;
        final int screenX;
        final int screenY;
        final boolean selected;
        final double distSq;

        Label(String text, int x, int y, boolean selected, double distSq) {
            this.text = text;
            this.screenX = x;
            this.screenY = y;
            this.selected = selected;
            this.distSq = distSq;
        }
    }

    private final List<Label> toDraw = new ArrayList<Label>();

    // Считаем, что показывать метки надо РОВНО тогда, когда это задумано в TravelController:
    // игрок стоит на якоре или держит активный staff, и режим staff не скрывает цели.
    private boolean shouldShowTargets(EntityPlayer player) {
        return TravelController.instance.showTargets();
    }

    // Максимальная дистанция (в квадратах) — такая же логика, как в контроллере:
    // если стоим на якоре — дистанция для BLOCK, иначе — по типу активного айтема (STAFF / TELEPORT_STAFF).
    private int maxDistSqForPlayer(EntityPlayer player) {
        if (TravelController.instance.onBlockCoord != null) {
            return TravelSource.BLOCK.getMaxDistanceTravelledSq();
        }
        TravelSource src = TravelController.instance.getTravelItemTravelSource(player, false);
        if (src != null) {
            return src.getMaxDistanceTravelledSq();
        }
        // дефолт — как у BLOCK
        return TravelSource.BLOCK.getMaxDistanceTravelledSq();
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent evt) {
        toDraw.clear();

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (!shouldShowTargets(mc.thePlayer)) return;

        // Подготовим матрицы для gluProject
        FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
        FloatBuffer projection = BufferUtils.createFloatBuffer(16);
        IntBuffer viewport = BufferUtils.createIntBuffer(16);

        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

        double camX = RenderManager.renderPosX;
        double camY = RenderManager.renderPosY;
        double camZ = RenderManager.renderPosZ;

        EntityPlayer player = mc.thePlayer;
        int maxDistSq = maxDistSqForPlayer(player);

        // Перебираем загруженные TileEntity и собираем Travel Anchors, доступные игроку
        @SuppressWarnings("unchecked")
        List<TileEntity> tiles = (List<TileEntity>) mc.theWorld.loadedTileEntityList;
        for (TileEntity te : tiles) {
            if (!(te instanceof ITravelAccessable)) continue;

            ITravelAccessable ta = (ITravelAccessable) te;
            if (!ta.isTravelSource()) continue; // только источники (якоря/телепады)
            if (!ta.canBlockBeAccessed(player)) continue; // уважаем приват/пароль

            // фильтр по дистанции (как в контроллере — по расстоянию от глаз до центра)
            double dx = (te.xCoord + 0.5) - player.posX;
            double dy = (te.yCoord + 0.5) - (player.posY + player.getEyeHeight());
            double dz = (te.zCoord + 0.5) - player.posZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > maxDistSq) continue;

            // Текст — из TileTravelAnchor.getLabel(), если это именно якорь; иначе что-то нейтральное
            String name = null;
            if (te instanceof TileTravelAnchor) {
                name = ((TileTravelAnchor) te).getLabel();
            }
            if (name == null || name.trim().isEmpty()) {
                // Можно пропускать безымянные, чтобы не мусорить экраном
                continue;
            }

            // Проецируем 3D → 2D: позицию над блоком
            float objX = (float) (te.xCoord + 0.5 - camX);
            float objY = (float) (te.yCoord + 1.20 - camY); // слегка выше
            float objZ = (float) (te.zCoord + 0.5 - camZ);

            FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
            boolean ok = Project.gluProject(objX, objY, objZ, modelView, projection, viewport, screenCoords);
            if (!ok) continue;

            int screenW = mc.displayWidth;
            int screenH = mc.displayHeight;

            // GL возвращает Y с нижнего края — инвертируем
            int sx = (int) screenCoords.get(0);
            int sy = (int) (screenH - screenCoords.get(1));

            // Переводим из пикселей окна в пиксели GUI (учёт GUI-масштаба)
            GameSettings gs = mc.gameSettings;
            int scale = 1;
            int guiScale = gs.guiScale == 0 ? 1 : gs.guiScale;
            while (scale < guiScale && screenW / (scale + 1) >= 320 && screenH / (scale + 1) >= 240) {
                scale++;
            }
            int guiX = sx / scale;
            int guiY = sy / scale;

            // Выделяем выбранную цель (сравниваем с selectedCoord контроллера)
            BlockCoord sel = TravelController.instance.selectedCoord;
            boolean selected = sel != null && sel.x == te.xCoord && sel.y == te.yCoord && sel.z == te.zCoord;

            toDraw.add(new Label(name, guiX, guiY, selected, distSq));
        }
    }

    @SubscribeEvent
    public void onHudRender(RenderGameOverlayEvent.Post evt) {
        if (evt.type != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || toDraw.isEmpty()) return;

        // Рисуем обычным FontRenderer в 2D HUD — это как раз то, что не ломает Angelica при enableFontRenderer=false
        RenderHelper.disableStandardItemLighting();

        // Немного отступим текст вверх
        for (Label lbl : toDraw) {
            String text = lbl.selected ? "§e[" + lbl.text + "]§r" : lbl.text; // жёлтым и в скобках — если выделен
            int strW = mc.fontRendererObj.getStringWidth(text);
            int x = lbl.screenX - (strW / 2);
            int y = lbl.screenY - 10; // приподнять над точкой

            mc.fontRendererObj.drawStringWithShadow(text, x, y, lbl.selected ? 0xFFFF55 : 0xFFFFFF);
        }

        // На всякий случай очищаем список после кадра
        toDraw.clear();
    }
}
