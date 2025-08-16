package crazypants.enderio.teleport.anchor;

import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.TravelController;
import crazypants.util.RenderUtil;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer<TileTravelAnchor> {

    private EntityItem ei;

    @Override
    public void renderTileEntityAt(TileTravelAnchor te, double x, double y, double z, float partialTick, int destroyStage) {
        TravelController tc = TravelController.instance;

        if (tc.isBlocked(te)) {
            return;
        }

        List<TileTravelAnchor> allAnchors = tc.getAnchorsForPlayer(Minecraft.getMinecraft().thePlayer, false);
        if (!allAnchors.contains(te)) {
            return;
        }

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        RenderManager rm = RenderManager.instance;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(rm.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        String toRender = te.getLabel();
        if (toRender == null || toRender.trim().isEmpty()) {
            toRender = EnderIO.lang.localize("blockTravelAnchor.defaultName");
        }

        int textW = fr.getStringWidth(toRender) / 2;
        fr.drawString(toRender, -textW, 0, 0xFFFFFF, false);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }

    private void renderEntity(RenderManager rm, EntityItem ei, ItemStack item, boolean isBlock, float scale) {
        if (ei == null) {
            ei = new EntityItem(Minecraft.getMinecraft().theWorld, 0, 0, 0, item);
        } else {
            ei.setEntityItemStack(item);
        }
        ei.hoverStart = 0f;

        GL11.glPushMatrix();
        if (isBlock) {
            GL11.glTranslatef(0, -0.1f, 0);
            GL11.glScalef(0.6f, 0.6f, 0.6f);
        }
        rm.renderEntityWithPosYaw(ei, 0, 0, 0, 0, 0);
        GL11.glPopMatrix();
    }

}
