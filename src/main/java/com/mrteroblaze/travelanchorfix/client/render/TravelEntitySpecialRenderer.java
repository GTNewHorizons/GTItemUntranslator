package com.mrteroblaze.travelanchorfix.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

@SideOnly(Side.CLIENT)
public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation TEX_HIGHLIGHT = new ResourceLocation(
        "enderio",
        "textures/gui/travelAnchorHighlight.png");
    private static final ResourceLocation TEX_SELECTED = new ResourceLocation(
        "enderio",
        "textures/gui/travelAnchorSelected.png");

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }
        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String text = anchor.getLabel();

        if (text == null || text.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.2, z + 0.5);

        // Поворот к камере
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);

        // Масштабирование
        float scale = 0.016666668F * 1.6F;
        GL11.glScalef(-scale, -scale, scale);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);

        int width = fr.getStringWidth(text);
        int half = width / 2;

        // Подложка
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(0f, 0f, 0f, 0.25f);
        GL11.glVertex3f(-half - 2, -2, 0.0F);
        GL11.glVertex3f(-half - 2, 9, 0.0F);
        GL11.glVertex3f(half + 2, 9, 0.0F);
        GL11.glVertex3f(half + 2, -2, 0.0F);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Текст
        fr.drawString(text, -half, 0, 0xFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }

    private ResourceLocation getHighlightOverlayIcon() {
        return TEX_HIGHLIGHT;
    }

    private ResourceLocation getSelectedOverlayIcon() {
        return TEX_SELECTED;
    }
}
