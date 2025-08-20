package com.teroblaze.gtitemuntranslator;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Materials;
import gregtech.api.objects.ItemData;
import gregtech.api.objects.MaterialStack;
import gregtech.api.util.GTLanguageManager;
import gregtech.api.util.GTOreDictUnificator;

public class TooltipEventHandler {

    // === DEBUG CONTROL ===
    private static final boolean DEBUG = false;

    private static void debug(String msg) {
        if (DEBUG) System.out.println(msg);
    }

    private static void error(String msg) {
        if (DEBUG) System.err.println(msg);
    }

    // Flag for /gtip on|off
    public static boolean TOOLTIPS_ENABLED = true;

    // === Конфигурация префиксов ===
    private static class PrefixRule {

        String prefix;
        boolean requireDotName;

        PrefixRule(String prefix, boolean requireDotName) {
            this.prefix = prefix;
            this.requireDotName = requireDotName;
        }
    }

    private static final PrefixRule[] PREFIX_RULES = { new PrefixRule("gt.bwMetaGenerated", false),
        new PrefixRule("gt.", true), new PrefixRule("gtplusplus.fluid.", false),
        new PrefixRule("gtplusplus.material.", false), new PrefixRule("gtplusplus.comb.", false),
        new PrefixRule("gtplusplus.drop.", false), new PrefixRule("gtplusplus.pollen.", false),
        new PrefixRule("gtplusplus.propolis.", false), new PrefixRule("gtplusplus.", true),
        new PrefixRule("MU-metaitem.", true), new PrefixRule("miscutils.", true), new PrefixRule("item.", true),
        new PrefixRule("comb.", false), new PrefixRule("bw.", false), new PrefixRule("\"gtplusplus.item", true),
        new PrefixRule("\"gtplusplus.material.", false), new PrefixRule("\"fluid.", false),
        new PrefixRule("fluid.", false), new PrefixRule("Material.", false), new PrefixRule("\"Material.", false),
        new PrefixRule("defc.casing.tip.", false), new PrefixRule("defc.", true), new PrefixRule("propolis.", false),
        new PrefixRule("\"Saccharomyces.", false), new PrefixRule("\"Pseudomonas.", false),
        new PrefixRule("drop.", false), new PrefixRule("block.", true), new PrefixRule("labModule.", true),
        new PrefixRule("tile.", true), new PrefixRule("GT_LESU.", true) };

    private PrefixRule matchPrefix(String key) {
        for (PrefixRule rule : PREFIX_RULES) {
            if (key.startsWith(rule.prefix)) {
                return rule;
            }
        }
        return null;
    }

    // === Universal Pattern Table for OreDict (BartWorks) ===
    private static final Map<String, String> BW_OREDICT_TEMPLATES = new HashMap<>();
    static {
        BW_OREDICT_TEMPLATES.put("dust", "%material Dust");
        BW_OREDICT_TEMPLATES.put("dustImpure", "Impure %material Dust");
        BW_OREDICT_TEMPLATES.put("dustPurified", "Purified %material Dust");
        BW_OREDICT_TEMPLATES.put("crushed", "Crushed %material Ore");
        BW_OREDICT_TEMPLATES.put("crushedPurified", "Purified Crushed %material Ore");
        BW_OREDICT_TEMPLATES.put("crushedCentrifuged", "Centrifuged Crushed %material Ore");
        BW_OREDICT_TEMPLATES.put("ingot", "%material Ingot");
        BW_OREDICT_TEMPLATES.put("nugget", "%material Nugget");
        BW_OREDICT_TEMPLATES.put("plate", "%material Plate");
        BW_OREDICT_TEMPLATES.put("rod", "%material Rod");
        BW_OREDICT_TEMPLATES.put("wire", "%material Wire");
        BW_OREDICT_TEMPLATES.put("block", "%material Block");
        BW_OREDICT_TEMPLATES.put("casing", "%material Casing");
        BW_OREDICT_TEMPLATES.put("ore", "%material Ore");
        BW_OREDICT_TEMPLATES.put("rawOre", "Raw %material Ore");
        BW_OREDICT_TEMPLATES.put("gem", "%material");
        BW_OREDICT_TEMPLATES.put("gemExquisite", "Exquisite %material");
        BW_OREDICT_TEMPLATES.put("gemFlawless", "Flawless %material");
        BW_OREDICT_TEMPLATES.put("gemFlawed", "Flawed %material");
        BW_OREDICT_TEMPLATES.put("gemChipped", "Chipped %material");
        BW_OREDICT_TEMPLATES.put("foil", "%material Foil");
        BW_OREDICT_TEMPLATES.put("stick", "%material Stick");
        BW_OREDICT_TEMPLATES.put("stickLong", "Long %material Stick");
        BW_OREDICT_TEMPLATES.put("toolHeadWrench", "%material Wrench Head");
        BW_OREDICT_TEMPLATES.put("toolHeadHammer", "%material Hammer Head");
        BW_OREDICT_TEMPLATES.put("toolHeadSaw", "%material Saw Head");
        BW_OREDICT_TEMPLATES.put("turbineBlade", "%material Turbine Blade");
        BW_OREDICT_TEMPLATES.put("gearGt", "%material Gear");
        BW_OREDICT_TEMPLATES.put("gearGtSmall", "Small %material Gear");
        BW_OREDICT_TEMPLATES.put("bolt", "%material Bolt");
        BW_OREDICT_TEMPLATES.put("screw", "%material Screw");
        BW_OREDICT_TEMPLATES.put("ring", "%material Ring");
        BW_OREDICT_TEMPLATES.put("spring", "%material Spring");
        BW_OREDICT_TEMPLATES.put("springSmall", "Small %material Spring");
        BW_OREDICT_TEMPLATES.put("rotor", "%material Rotor");
        BW_OREDICT_TEMPLATES.put("cell", "%material Cell");
        BW_OREDICT_TEMPLATES.put("cellMolten", "Molten %material Cell");
        BW_OREDICT_TEMPLATES.put("capsule", "%material Capsule");
        BW_OREDICT_TEMPLATES.put("capsuleMolten", "Molten %material Capsule");
    }

    private String prettifyMaterialName(String name) {
        if (name == null || name.isEmpty()) return name;
        return name.replaceAll("([a-z])([A-Z])", "$1 $2")
            .trim();
    }

    // === Getting the original English name ===
    private String getOriginalEnglishName(ItemStack itemStack, String localizationKey) {
        if (itemStack == null) {
            return null;
        }

        try {
            // === fluids (ItemFluidDisplay) ===
            if (itemStack.getItem() instanceof gregtech.common.items.ItemFluidDisplay) {
                int meta = itemStack.getItemDamage();
                net.minecraftforge.fluids.Fluid fluid = FluidRegistry.getFluid(meta);

                if (fluid != null) {
                    String regName = fluid.getName();
                    debug("[Fluid] meta=" + meta + ", fluid=" + regName);

                    String key1 = "fluid." + regName;
                    String key2 = "\"fluid." + regName + "\"";

                    String raw1 = OriginalLanguageStore.getOriginal(key1);
                    String raw2 = OriginalLanguageStore.getOriginal(key2);

                    if (raw1 != null && !raw1.equals(key1)) {
                        return raw1;
                    }
                    if (raw2 != null && !raw2.equals(key2)) {
                        return raw2;
                    }

                    if (regName != null && !regName.isEmpty()) {
                        String pretty = prettifyMaterialName(regName.replace('.', ' '));
                        return pretty;
                    }
                } else {
                    error("[Fluid] No fluid found for meta=" + meta);
                }
                return null;
            }

            // === 1. Lang file===
            if (localizationKey != null && !localizationKey.isEmpty()) {
                String rawTemplate = OriginalLanguageStore.getOriginal(localizationKey);
                if (rawTemplate != null && !rawTemplate.equals(localizationKey)) {
                    if (!rawTemplate.contains("%material")) {
                        return rawTemplate;
                    }
                    Materials material = null;
                    try {
                        Object assoc = GTOreDictUnificator.getAssociation(itemStack);
                        if (assoc instanceof ItemData) {
                            MaterialStack ms = ((ItemData) assoc).mMaterial;
                            if (ms != null) material = ms.mMaterial;
                        }
                    } catch (Throwable ignored) {}

                    if (material == null) {
                        int idx = itemStack.getItemDamage() % 1000;
                        if (idx >= 0 && idx < GregTechAPI.sGeneratedMaterials.length) {
                            material = GregTechAPI.sGeneratedMaterials[idx];
                        }
                    }
                    if (material != null && material.mName != null) {
                        return rawTemplate.replace("%material", material.mName);
                    } else {
                        return rawTemplate;
                    }
                }
            }

            // === 2. Werkstoff casings ===
            if (localizationKey != null && (localizationKey.startsWith("bw.werkstoffblockscasing.")
                || localizationKey.startsWith("bw.werkstoffblockscasingadvanced."))) {
                int meta = itemStack.getItemDamage();
                bartworks.system.material.Werkstoff w = bartworks.system.material.Werkstoff.werkstoffHashMap
                    .get((short) meta);
                if (w != null) {
                    String matName = w.getDefaultName();
                    return localizationKey.startsWith("bw.werkstoffblockscasingadvanced.")
                        ? "Advanced " + matName + " Casing"
                        : matName + " Casing";
                }
            }

            // === 3. Werkstoff blocks ===
            if (localizationKey != null && localizationKey.startsWith("bw.werkstoffblocks.")) {
                int[] ids = OreDictionary.getOreIDs(itemStack);
                for (int id : ids) {
                    String oreName = OreDictionary.getOreName(id);
                    for (Map.Entry<String, String> entry : BW_OREDICT_TEMPLATES.entrySet()) {
                        if (oreName.startsWith(entry.getKey())) {
                            String material = prettifyMaterialName(
                                oreName.substring(
                                    entry.getKey()
                                        .length()));
                            return entry.getValue()
                                .replace("%material", material);
                        }
                    }
                }
                bartworks.system.material.Werkstoff w = bartworks.system.material.Werkstoff.werkstoffHashMap
                    .get(itemStack.getItemDamage());
                if (w != null) return w.getDefaultName() + " Block";
            }

            // === 4. bwMetaGenerated ===
            if (localizationKey != null && localizationKey.startsWith("gt.bwMetaGenerated")) {
                int[] ids = OreDictionary.getOreIDs(itemStack);
                for (int id : ids) {
                    String oreName = OreDictionary.getOreName(id);
                    for (Map.Entry<String, String> entry : BW_OREDICT_TEMPLATES.entrySet()) {
                        if (oreName.startsWith(entry.getKey())) {
                            String material = prettifyMaterialName(
                                oreName.substring(
                                    entry.getKey()
                                        .length()));
                            return entry.getValue()
                                .replace("%material", material);
                        }
                    }
                }
            }

        } catch (Throwable t) {
            error("getOriginalEnglishName failed for key=" + localizationKey);
            t.printStackTrace();
        }
        return null;
    }

    // === Main Tooltip Handler ===
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!TOOLTIPS_ENABLED) return;

        ItemStack itemStack = event.itemStack;
        if (itemStack == null || itemStack.getItem() == null) return;

        try {
            // === Fluids ===
            if (itemStack.getItem() instanceof gregtech.common.items.ItemFluidDisplay) {
                int meta = itemStack.getItemDamage();
                debug(
                    "[Tooltip] Fluid item detected: class=" + itemStack.getItem()
                        .getClass()
                        .getName() + ", meta=" + meta);
                String originalEnglishName = getOriginalEnglishName(itemStack, null);
                if (originalEnglishName != null) {
                    event.toolTip.add(EnumChatFormatting.GRAY + "[EN] " + originalEnglishName);
                }
                return;
            }

            // === Regular items ===
            String unloc = itemStack.getUnlocalizedName();
            PrefixRule rule = matchPrefix(unloc);
            if (rule != null) {
                String key = rule.requireDotName ? unloc + ".name" : unloc;
                String originalEnglishName = getOriginalEnglishName(itemStack, key);
                String currentTranslation = GTLanguageManager.getTranslation(unloc);
                if (originalEnglishName != null && !originalEnglishName.equals(key)
                    && !originalEnglishName.equals(currentTranslation)) {
                    event.toolTip.add(EnumChatFormatting.GRAY + "[EN] " + originalEnglishName);
                }
            }
        } catch (Throwable t) {
            error("onItemTooltip exception:");
            t.printStackTrace();
        }
    }
}
