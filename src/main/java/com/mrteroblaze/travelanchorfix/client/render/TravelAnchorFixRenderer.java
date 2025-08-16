package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import com.gtnewhorizons.angelica.client.render.BatchingFontRenderer;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Loader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.lang.reflect.Field;

public class TravelAnchorFixRenderer extends TileEntitySpecialRenderer {

    private BatchingFontRenderer batched;

    public static void register() {
        try {
            Class<? extends TileEntity> teClass = (Class<? extends TileEntity>)
                    Class.forName("crazypants.enderio.teleport.anchor.TileTravelAnchor");
            ClientRegistry.bindTileEntitySpecialRenderer(teClass, new TravelAnchorFixRenderer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BatchingFontRenderer getBatchedFontRenderer() {
        if (batched != null) return batched;
        if (!Loader.isModLoaded("angelica")) return null;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            FontRenderer fr = mc.fontRenderer;

            Field fGlyphWidth = FontRenderer.class.getDeclaredField("glyphWidth");
            fGlyphWidth.setAccessible(true);
            byte[] glyphWidth = (byte[]) fGlyphWidth.get(fr);

            Field fCharWidth = FontRenderer.class.getDeclaredField("charWidth");
            fCharWidth.setAccessible(true);
            int[] charWidth = (int[]) fCharWidth.get(fr);

            Field fColorCode = FontRenderer.class.getDeclaredField("colorCode");
            fColorCode.setAccessible(true);
            int[] colorCode = (int[]) fColorCode.get(fr);

            Field fUniPages = FontRenderer.class.getDeclaredField("unicodePageLocations");
            fUniPages.setAccessible(true);
            ResourceLocation[] unicodePages = (ResourceLocation[]) fUniPages.get(fr);

            Field fLoc = FontRenderer.class.getDeclaredField("locationFontTexture");
            fLoc.setAccessible(true);
            ResourceLocation fontTex = (ResourceLocation) fLoc.get(fr);

            batched = new BatchingFontRenderer(fr, unicodePages, charWidth, glyphWidth, colorCode, fontTex);
            return batched;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (te == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;
        BatchingFontRenderer bfr = getBatchedFontRenderer();

        String text = "Travel Anchor"; // Заглушка — в реальности берём имя из TileEntity

        int textW = (bfr != null ? ((FontRenderer) bfr).getStringWidth(text) : fr.getStringWidth(text));

        // Центрируем текст
        float scale = 0.02F;
        float textX = -textW / 2F;

        RenderUtil.drawBillboardedText(
                new Vector3f((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F),
                text,
                scale,
                new Vector4f(1, 1, 1, 1),
                false,
                bfr != null ? (FontRenderer) bfr : fr,
                true,
                new Vector4f(0, 0, 0, 0.25F)
        );
    }
}
