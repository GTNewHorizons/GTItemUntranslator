package com.mrteroblaze.travelanchorfix.client.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import crazypants.enderio.teleport.TravelController;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import com.enderio.core.common.util.BlockCoord;

import java.util.List;

public class AnchorNameOverlayHandler {

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        // Получаем список координат якорей, доступных игроку
        List<BlockCoord> coords = TravelController.instance.getActiveTravelBlock(mc.thePlayer);
        if (coords == null || coords.isEmpty()) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        for (BlockCoord bc : coords) {
            TileEntity te = mc.theWorld.getTileEntity(bc.x, bc.y, bc.z);
            if (!(te instanceof TileTravelAnchor)) continue;

            TileTravelAnchor anchor = (TileTravelAnchor) te;
            String name = anchor.getLabel();
            if (name == null || name.trim().isEmpty()) continue;

            drawNameplate(mc, name, bc.x + 0.5, bc.y + 1.5, bc.z + 0.5);
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    private void drawNameplate(Minecraft mc, String text, double x, double y, double z) {
        float viewerYaw = mc.renderViewEntity.rotationYaw;
        float viewerPitch = mc.renderViewEntity.rotationPitch;
        float scale = 0.016666668F * 1.6F;

        GL11.glPushMatrix();
        GL11.glTranslated(
                x - mc.getRenderManager().viewerPosX,
                y - mc.getRenderManager().viewerPosY,
                z - mc.getRenderManager().viewerPosZ
        );
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
