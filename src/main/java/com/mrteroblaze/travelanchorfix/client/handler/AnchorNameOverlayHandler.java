package com.mrteroblaze.travelanchorfix.client.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import crazypants.enderio.teleport.TravelController;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

import java.util.List;

public class AnchorNameOverlayHandler {

    private static final ResourceLocation TEX = new ResourceLocation("enderio", "textures/blocks/travelAnchor.png");

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        // Получаем список доступных якорей из TravelController
        List<TileTravelAnchor> anchors = TravelController.instance.getActiveTravelAnchors(mc.thePlayer, mc.theWorld);
        if (anchors == null || anchors.isEmpty()) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        for (TileTravelAnchor anchor : anchors) {
            String name = anchor.getLabel();
            if (name == null || name.trim().isEmpty()) continue;

            double dx = anchor.xCoord + 0.5 - mc.thePlayer.posX;
            double dy = anchor.yCoord + 1.5 - mc.thePlayer.posY;
            double dz = anchor.zCoord + 0.5 - mc.thePlayer.posZ;
            double distSq = dx * dx + dy * dy + dz * dz;

            // Отрисовка только в пределах радиуса из конфига
            if (distSq > TravelController.instance.getMaxDistanceSq()) continue;

            drawNameplate(mc, name, anchor.xCoord + 0.5, anchor.yCoord + 1.5, anchor.zCoord + 0.5);
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    private void drawNameplate(Minecraft mc, String text, double x, double y, double z) {
        float viewerYaw = mc.renderViewEntity.rotationYaw;
        float viewerPitch = mc.renderViewEntity.rotationPitch;
        double dist = mc.renderViewEntity.getDistance(x, y, z);

        float scale = 0.016666668F * 1.6F;

        GL11.glPushMatrix();
        GL11.glTranslated(x - mc.getRenderManager().viewerPosX, y - mc.getRenderManager().viewerPosY, z - mc.getRenderManager().viewerPosZ);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(viewerPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-scale, -scale, scale);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        int strW = mc.fontRenderer.getStringWidth(text) / 2;

        mc.fontRenderer.drawString(text, -strW, 0, 0xFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }
}
