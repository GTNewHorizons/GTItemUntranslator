package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import com.gtnewhorizons.angelica.client.render.font.BatchingFontRenderer;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Loader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;

public class TravelAnchorFixRenderer extends TileEntitySpecialRenderer {

    private BatchingFontRenderer batched;

    public static void register() {
        // TileTravelAnchor — класс EnderIO
        try {
            Class<?> teClass = Class.forName("crazypants.enderio.teleport.anchor.TileTravelAnchor");
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

            // Рефлексия для приватных полей FontRenderer в 1.7.10
            Field fGlyphWidth = FontRenderer.class.getDeclaredField("glyphWidth");
            fGlyphWidth.setAccessible(true);
            byte[] glyphWidth = (byte[]) fGlyphWidth.get(fr);

            Field fCharWidth = FontRenderer.class.getDeclaredField("charWidth");
            fCharWidth.setAccessible(true);
            int[] charWidth = (int[]) fCharWidth.get(fr);

            Field fLocFont = FontRenderer.class.getDeclaredField("locationFontTexture");
            fLocFont.setAccessible(true);
            ResourceLocation fontTex = (ResourceLocation) fLocFont.get(fr);

            // Конструктор Angelica для 1.7.10
            batched = new BatchingFontRenderer(fr, charWidth, glyphWidth, fontTex);
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
        BatchingFontRenderer bfr = getBatchedFontRenderer();

        // Вся остальная логика рендера Travel Anchor берётся из оригинального TravelEntitySpecialRenderer
        // Здесь упрощённо только текст
        String name = "Travel Anchor"; // В оригинале достаётся из TE

        // Пример вызова из RenderUtil
        if (bfr != null) {
            RenderUtil.drawBillboardedText(
                    new org.lwjgl.util.vector.Vector3f((float) x + 0.5F, (float) y + 1.2F, (float) z + 0.5F),
                    name,
                    1.0F,
                    new org.lwjgl.util.vector.Vector4f(1, 1, 1, 1),
                    false,
                    null,
                    true,
                    new org.lwjgl.util.vector.Vector4f(0, 0, 0, 0.25F)
            );
        } else {
            mc.fontRenderer.drawString(name, 0, 0, 0xFFFFFF);
        }
    }
}
