package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.vecmath.Vector3d;
import crazypants.enderio.EnderIO;
import crazypants.enderio.machine.travel.BlockTravelAnchor;
import crazypants.enderio.machine.travel.TileTravelAnchor;
import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelEntitySpecialRenderer());
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        TileTravelAnchor anchor = (TileTravelAnchor) te;

        String toRender = anchor.getLabel();
        if (toRender == null || toRender.isEmpty()) {
            return;
        }

        Vector3d pos = new Vector3d(x + 0.5, y + 1.5, z + 0.5);

        final Minecraft mc = Minecraft.getMinecraft();
        final FontRenderer fr = mc.fontRenderer;

        GL11.glPushMatrix();
        try {
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

            // фон-подложка (как в оригинале EnderIO)
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.25F);
            GL11.glVertex3f(baseX - 2, -1, 0.0F);
            GL11.glVertex3f(baseX - 2, 8, 0.0F);
            GL11.glVertex3f(baseX + textW + 2, 8, 0.0F);
            GL11.glVertex3f(baseX + textW + 2, -1, 0.0F);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            // сам текст
            fr.drawString(toRender, baseX, 0, 0xFFFFFFFF);

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
        } finally {
            GL11.glPopMatrix();
        }
    }

    // ==========================
    // Достаём иконки через рефлексию
    // ==========================
    private static IIcon getSelectedOverlayIcon() {
        try {
            java.lang.reflect.Field f = BlockTravelAnchor.class.getDeclaredField("selectedOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private static IIcon getHighlightOverlayIcon() {
        try {
            java.lang.reflect.Field f = BlockTravelAnchor.class.getDeclaredField("highlightOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}
