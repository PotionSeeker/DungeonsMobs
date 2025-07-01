package net.firefoxsalesman.dungeonsmobs.lib.utils;

import com.google.common.collect.Multimap;
import net.firefoxsalesman.dungeonsmobs.lib.capabilities.builtinenchantments.BuiltInEnchantments;
import net.firefoxsalesman.dungeonsmobs.lib.capabilities.builtinenchantments.BuiltInEnchantmentsHelper;
import net.firefoxsalesman.dungeonsmobs.lib.items.artifacts.ArtifactItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger; // Use java.util.logging

import static net.firefoxsalesman.dungeonsmobs.DungeonsMobs.MOD_ID;
import static net.firefoxsalesman.dungeonsmobs.lib.items.gearconfig.MeleeGearConfigRegistry.GEAR_CONFIG_BUILTIN_RESOURCELOCATION;
import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class DescriptionHelper {
	private static final Logger LOGGER = Logger.getLogger("DungeonsMobs");

	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event) {
		if (ModList.get().isLoaded("customnpcs")) {
			LOGGER.fine("CustomNPCs detected, using minimal tooltip logic for item: " + event.getItemStack());
			return; // Skip tooltip modifications to avoid potential log4j conflicts
		}

		try {
			BuiltInEnchantments cap = BuiltInEnchantmentsHelper
					.getBuiltInEnchantmentsCapability(event.getItemStack());
			List<EnchantmentInstance> builtInEnchantments = cap
					.getBuiltInEnchantments(GEAR_CONFIG_BUILTIN_RESOURCELOCATION);
			builtInEnchantments.forEach(enchantmentInstance -> {
				event.getToolTip().add(enchantmentInstance.enchantment.getFullname(enchantmentInstance.level)
						.copy().withStyle(Style.EMPTY.withColor(TextColor.parseColor("#FF8100"))));
			});
		} catch (Exception e) {
			LOGGER.warning("Error generating tooltip for item " + event.getItemStack() + ": " + e.getMessage());
		}
	}

	public static void addLoreDescription(List<Component> list, ResourceLocation registryName) {
		try {
			list.add(Component.translatable(
							"lore." + registryName.getNamespace() + "." + registryName.getPath())
					.withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC));
		} catch (Exception e) {
			LOGGER.warning("Error adding lore description for " + registryName + ": " + e.getMessage());
		}
	}

	public static void addFullDescription(List<Component> list, ItemStack itemStack) {
		ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
		if (registryName != null) {
			addLoreDescription(list, registryName);
		}
	}

	public static void addArtifactDescription(List<Component> list, ItemStack itemStack) {
		ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
		if (registryName == null) {
			return;
		}
		addLoreDescription(list, registryName);
		addArtifactInfo(list, itemStack);
		addArtifactAttributeInfo(list, itemStack);
	}

	private static void addArtifactAttributeInfo(List<Component> list, ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ArtifactItem artifactItem)) {
			return;
		}
		try {
			Multimap<Attribute, AttributeModifier> multimap = artifactItem.getDefaultAttributeModifiers(0);
			if (!multimap.isEmpty()) {
				list.add(CommonComponents.EMPTY);
				list.add(Component.translatable("item.modifiers.artifact").withStyle(ChatFormatting.GRAY));

				for (Map.Entry<Attribute, AttributeModifier> entry : multimap.entries()) {
					AttributeModifier attributemodifier = entry.getValue();
					double d0 = attributemodifier.getAmount();

					double d1;
					if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE
							&& attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
						if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
							d1 = d0 * 10.0D;
						} else {
							d1 = d0;
						}
					} else {
						d1 = d0 * 100.0D;
					}

					if (d0 > 0.0D) {
						list.add(Component.translatable(
										"attribute.modifier.plus." + attributemodifier.getOperation().toValue(),
										ATTRIBUTE_MODIFIER_FORMAT.format(d1),
										Component.translatable(entry.getKey().getDescriptionId()))
								.withStyle(ChatFormatting.BLUE));
					} else if (d0 < 0.0D) {
						d1 *= -1.0D;
						list.add(Component.translatable(
										"attribute.modifier.take." + attributemodifier.getOperation().toValue(),
										ATTRIBUTE_MODIFIER_FORMAT.format(d1),
										Component.translatable(entry.getKey().getDescriptionId()))
								.withStyle(ChatFormatting.RED));
					}
				}
			}
		} catch (Exception e) {
			LOGGER.warning("Error adding artifact attribute info for " + itemStack + ": " + e.getMessage());
		}
	}

	public static void addArtifactInfo(List<Component> list, ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ArtifactItem)) {
			return;
		}
		try {
			list.add(Component.translatable("artifact.dungeons_libraries.base")
					.withStyle(ChatFormatting.DARK_AQUA));

			ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
			list.add(Component.translatable(
							"ability." + registryName.getNamespace() + "." + registryName.getPath())
					.withStyle(ChatFormatting.GREEN));

			ArtifactItem artifactItem = (ArtifactItem) itemStack.getItem();
			int durationInSeconds = artifactItem.getDurationInSeconds();
			int cooldownInSeconds = artifactItem.getCooldownInSeconds();

			if (durationInSeconds > 0) {
				list.add(Component.translatable(
								"artifact.dungeons_libraries.duration", durationInSeconds)
						.withStyle(ChatFormatting.BLUE));
			}
			if (cooldownInSeconds > 0) {
				list.add(Component.translatable(
								"artifact.dungeons_libraries.cooldown", cooldownInSeconds)
						.withStyle(ChatFormatting.BLUE));
			}
		} catch (Exception e) {
			LOGGER.warning("Error adding artifact info for " + itemStack + ": " + e.getMessage());
		}
	}
}
