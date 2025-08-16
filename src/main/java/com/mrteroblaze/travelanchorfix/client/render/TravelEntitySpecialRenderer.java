package com.mrteroblaze.travelanchorfix.client.render;

import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TravelEntitySpecialRenderer extends Render {

    private final FontRenderer vanillaFont;
    private final BatchingFontRenderer batchingFont;
    private final boolean useBatching;

    public TravelEntitySpecialRenderer() {
        this.vanillaFont = Minecraft.getMinecraft().fontRenderer;
        if (Loader.isModLoaded("angelica")) {
            this.batchingFont = BatchingFontRenderer.INSTANCE;
            this.useBatching = true;
            System.out.println("[TravelAnchorFix] Using Angelica BatchingFontRenderer");
        } else {
            this.batchingFont = null;
            this.useBatching = false;
            System.out.println("[TravelAnchorFix] Angelica not found, using vanilla FontRenderer");
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null; // у якорей нет текстуры
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        // Подсветка якоря (рамка)
        GL11.glPushMatrix();
        GL11.glTranslated(x, y + 1.2, z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.016F, -0.016F, 0.016F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);

        String text = entity.getCommandSenderName(); // имя якоря
        int width;

        if (useBatching) {
            width = batchingFont.getStringWidth(text);
        } else {
            width = vanillaFont.getStringWidth(text);
        }

        int halfWidth = width / 2;

        // фон
        Tessellator tess = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tess.startDrawingQuads();
        tess.setColorRGBA_F(0, 0, 0, 0.25f);
        tess.addVertex(-halfWidth - 2, -2, 0.0D);
        tess.addVertex(-halfWidth - 2, 9, 0.0D);
        tess.addVertex(halfWidth + 2, 9, 0.0D);
        tess.addVertex(halfWidth + 2, -2, 0.0D);
        tess.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // текст
        if (useBatching) {
            batchingFont.drawString(text, -halfWidth, 0, 0xFFFFFFFF, false, false, null, 0, 0);
        } else {
            vanillaFont.drawString(text, -halfWidth, 0, 0xFFFFFFFF);
        }

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
