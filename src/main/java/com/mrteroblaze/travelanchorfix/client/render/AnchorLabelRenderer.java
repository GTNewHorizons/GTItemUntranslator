package com.mrteroblaze.travelanchorfix.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class AnchorLabelRenderer {

    // Радиус, в котором вообще рисуем подписи (можешь подправить)
    private static final double MAX_DIST_SQ = 64 * 64; // 64 блока

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        final FontRenderer fr = mc.fontRenderer;

        // Позиция камеры с интерполяцией
        double ix = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * event.partialTicks;
        double iy = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * event.partialTicks;
        double iz = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * event.partialTicks;

        for (Object o : mc.theWorld.loadedTileEntityList) {
            if (!(o instanceof TileTravelAnchor)) continue;
            TileTravelAnchor te = (TileTravelAnchor) o;

            String label = te.getLabel();
            if (label == null || label.isEmpty()) continue;

            double dx = te.xCoord + 0.5 - ix;
            double dy = te.yCoord + 1.5 - iy;
            double dz = te.zCoord + 0.5 - iz;

            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > MAX_DIST_SQ) continue;

            // можно слегка уменьшить прозрачность с расстоянием (по желанию)
            drawLabel(fr, label, dx, dy, dz);
        }
    }

    private static void drawLabel(FontRenderer fr, String text, double dx, double dy, double dz) {
        GL11.glPushMatrix();
        GL11.glTranslated(dx, dy, dz);

        // Билбординг на 1.7.10
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F);

        // Масштаб под табличку
        float scale = 0.016666668F * 1.6F;
        GL11.glScalef(-scale, -scale, scale);

        // Аккуратно меняем GL-стейт и возвращаем обратно
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);

        int width = fr.getStringWidth(text);
        int half = width / 2;

        // Подложка (чёрная полупрозрачная)
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0f, 0f, 0f, 0.25f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(-half - 2, -2, 0.0F);
        GL11.glVertex3f(-half - 2, 9, 0.0F);
        GL11.glVertex3f(half + 2, 9, 0.0F);
        GL11.glVertex3f(half + 2, -2, 0.0F);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Текст
        fr.drawString(text, -half, 0, 0xFFFFFFFF);

        // Восстановление стейта в обратном порядке
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glPopMatrix();
    }
}
