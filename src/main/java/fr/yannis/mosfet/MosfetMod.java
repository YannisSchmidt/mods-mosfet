package fr.yannis.mosfet;

import java.util.function.Function;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import fr.yannis.mosfet.block.CmosMosfetBlock;
import fr.yannis.mosfet.block.NmosMosfetBlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MosfetMod implements ModInitializer {
	public static final String MOD_ID = "mosfet";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * MOSFET canal N : le drain prend la valeur de la source quand la grille (gate)
	 * est superieure ou egale a la source.
	 */
	public static final Block NMOS_MOSFET = registerBlock("nmos_mosfet", NmosMosfetBlock::new);

	/**
	 * MOSFET canal P (complementaire du precedent) : le drain prend la valeur de la
	 * source quand la grille (gate) est strictement inferieure a la source.
	 */
	public static final Block CMOS_MOSFET = registerBlock("cmos_mosfet", CmosMosfetBlock::new);

	@Override
	public void onInitialize() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.REDSTONE_BLOCKS).register((group, entries) -> {
			entries.accept(NMOS_MOSFET);
			entries.accept(CMOS_MOSFET);
		});

		LOGGER.info("MOSFET mod loaded: NMOS (canal N) et CMOS (canal P) prets a conduire la redstone.");
	}

	private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));

		// setId est obligatoire en 1.21.11 : sans lui la table de butin et le nom
		// traduit du bloc ne peuvent pas etre resolus.
		Block block = factory.apply(BlockBehaviour.Properties.of()
				.setId(blockKey)
				.mapColor(MapColor.METAL)
				.strength(0.4F)
				.sound(SoundType.METAL)
				// Un MOSFET n'est pas un conducteur : la puissance n'entrecroise pas les
				// bornes (pas de fuite entre grille, source et drain).
				.isRedstoneConductor((state, level, pos) -> false));

		Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
		BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
		Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

		return block;
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
