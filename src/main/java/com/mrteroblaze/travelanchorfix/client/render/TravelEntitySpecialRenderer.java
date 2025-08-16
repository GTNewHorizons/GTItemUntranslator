package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.vecmath.Vector3d;
import crazypants.enderio.EnderIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private final FontRenderer fr;

    public TravelEntitySpecialRenderer() {
        this.fr = Minecraft.getMinecraft().fontRenderer;
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!isTravelAnchor(te)) {
            return;
        }

        String toRender = getAnchorLabel(te);
        if (toRender == null || toRender.isEmpty()) {
            return;
        }

        Vector3d pos = new Vector3d(x + 0.5, y + 1.5, z + 0.5);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) pos.x, (float) pos.y, (float) pos.z);

        RenderManager rm = RenderManager.instance;
        GL11.glRotatef(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(rm.playerViewX, 1.0F, 0.0F, 0.0F);

        final float s = 0.025F * 2.0F;
        GL11.glScalef(-s, -s, s);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int textW = fr.getStringWidth(toRender);
        int baseX = -textW / 2;

        // фон рамки
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        RenderUtil.drawQuad2D(baseX - 1, -1, textW + 2, 8, 0x80000000);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // текст
        fr.drawString(toRender, baseX, 0, 0xFFFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }

    // ==========================
    // Совместимость через рефлексию
    // ==========================
    private static boolean isTravelAnchor(TileEntity te) {
        if (te == null) return false;
        String cn = te.getClass().getName();
        return cn.endsWith(".TileTravelAnchor");
    }

    private static String getAnchorLabel(TileEntity te) {
        try {
            java.lang.reflect.Method m = te.getClass().getMethod("getLabel");
            Object res = m.invoke(te);
            return res != null ? res.toString() : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static IIcon getSelectedOverlayIcon() {
        try {
            java.lang.reflect.Field f = EnderIO.blockTravelPlatform.getClass().getDeclaredField("selectedOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            return null;
        }
    }

    private static IIcon getHighlightOverlayIcon() {
        try {
            java.lang.reflect.Field f = EnderIO.blockTravelPlatform.getClass().getDeclaredField("highlightOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            return null;
        }
    }
}
