package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.vecmath.Vector3d;
import com.enderio.core.common.vecmath.Vector3f;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import crazypants.enderio.teleport.TravelController;
import crazypants.enderio.teleport.anchor.BlockTravelAnchor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelEntitySpecialRenderer());
    }

    private static IIcon getSelectedOverlayIcon() {
        try {
            Field f = BlockTravelAnchor.class.getDeclaredField("selectedOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private static IIcon getHighlightOverlayIcon() {
        try {
            Field f = BlockTravelAnchor.class.getDeclaredField("highlightOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        TravelController tc = TravelController.instance;

        // Подсветка (оставляем оригинальную логику)
        if (tc.isBlocked(anchor)) {
            return;
        }

        Vector3d pos = new Vector3d(x + 0.5, y + 1.5, z + 0.5);
        String toRender = anchor.getLabel();
        if (toRender == null || toRender.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;

        GL11.glPushMatrix();
        try {
            // Перемещаем к позиции
            GL11.glTranslated(pos.x, pos.y, pos.z);

            // Поворачиваем к игроку (billboard)
            RenderManager rm = RenderManager.instance;
            GL11.glRotatef(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(rm.playerViewX, 1.0F, 0.0F, 0.0F);

            // Масштаб текста
            float scale = 0.025F;
            GL11.glScalef(-scale, -scale, scale);

            // GL-состояния
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);

            // Центровка текста
            int textW = fr.getStringWidth(toRender);
            int baseX = -textW / 2;

            // Фон (чёрная подложка)
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.25F);
            GL11.glVertex3f(baseX - 1, -1, 0.0F);
            GL11.glVertex3f(baseX - 1, 8, 0.0F);
            GL11.glVertex3f(baseX + textW + 1, 8, 0.0F);
            GL11.glVertex3f(baseX + textW + 1, -1, 0.0F);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            // Сам текст
            fr.drawString(toRender, baseX, 0, 0xFFFFFFFF);

            // Вернуть GL-состояния
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
        } finally {
            GL11.glPopMatrix();
        }
    }
}
