package com.rgbcursor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Cursor shapes. Besides the arrow, the first few reuse the images bundled with RuneLite's Custom Cursor plugin;
 * the rest are bundled with this plugin (from the community list in runelite/runelite discussion #16472).
 */
@Getter
@RequiredArgsConstructor
public enum CursorShape
{
	ARROW("Arrow", null, false, 0, 0),
	DRAGON_SCIMITAR("Dragon scimitar", "cursor-dragon-scimitar.png", true, 0, 0),
	DRAGON_DAGGER("Dragon dagger", "cursor-dragon-dagger.png", true, 0, 0),
	DRAGON_DAGGER_POISON("Dragon dagger (p)", "cursor-dragon-dagger-p.png", true, 0, 0),
	RS3_GOLD("RS3 gold", "cursor-rs3-gold.png", true, 0, 0),
	RS3_SILVER("RS3 silver", "cursor-rs3-silver.png", true, 0, 0),
	TROUT("Trout", "cursor-trout.png", true, 0, 0),
	COINS_300M("300m coins", "300m_coins.png", false, 6, 0),
	ABYSSAL_HEAD("Abyssal head", "abyssal_head.png", false, 2, 3),
	ABYSSAL_WHIP("Abyssal whip", "abyssal_whip.png", false, 0, 3),
	AGILITY_CAPE("Agility cape", "agility_cape.png", false, 3, 2),
	AHRIMS_HOOD("Ahrim's hood", "ahrims_hood.png", false, 14, 4),
	AHRIMS_ROBESKIRT("Ahrim's robeskirt", "ahrims_robeskirt.png", false, 14, 3),
	AHRIMS_ROBETOP("Ahrim's robetop", "ahrims_robetop.png", false, 13, 4),
	AHRIMS_STAFF("Ahrim's staff", "ahrims_staff.png", false, 5, 3),
	ANCIENT_STAFF("Ancient staff", "ancient_staff.png", false, 8, 1),
	ANTI_DRAGON_SHIELD("Anti-dragon shield", "anti_dragon_shield.png", false, 3, 8),
	ATTACK_CAPE("Attack cape", "attack_cape.png", false, 2, 2),
	BANDOS_GODSWORD("Bandos godsword", "bandos_godsword.png", false, 1, 1),
	BLACK_CAT("Black cat", "black_cat.png", false, 10, 3),
	BLACK_CAVALIER("Black cavalier", "black_cavalier.png", false, 2, 5),
	BLACK_MASK("Black mask", "black_mask.png", false, 12, 1),
	BLUE_HWEEN_MASK("Blue h'ween mask", "blue_hween_mask.png", false, 2, 1),
	BLUE_PARTYHAT("Blue partyhat", "blue_partyhat.png", false, 3, 1),
	CHRISTMAS_CRACKER("Christmas cracker", "christmas_cracker.png", false, 0, 3),
	CONSTRUCTION_CAPE("Construction cape", "construction_cape.png", false, 3, 2),
	COOKING_CAPE("Cooking cape", "cooking_cape.png", false, 2, 2),
	CRYSTAL_BOW("Crystal bow", "crystal_bow.png", false, 5, 6),
	DARK_BOW("Dark bow", "dark_bow.png", false, 1, 4),
	DEFENCE_CAPE("Defence cape", "defence_cape.png", false, 2, 2),
	DHAROKS_HELM("Dharok's helm", "dharoks_helm.png", false, 17, 3),
	DHAROKS_PLATEBODY("Dharok's platebody", "dharoks_platebody.png", false, 4, 3),
	DHAROKS_PLATELEGS("Dharok's platelegs", "dharoks_platelegs.png", false, 14, 2),
	DRAGON_AXE("Dragon axe", "dragon_axe.png", false, 2, 7),
	DRAGON_CHAINBODY("Dragon chainbody", "dragon_chainbody.png", false, 3, 1),
	DRAGON_DAGGER_ALT("Dragon dagger (alt)", "dragon_dagger_alt.png", false, 3, 1),
	DRAGON_FULL_HELM("Dragon full helm", "dragon_full_helm.png", false, 9, 1),
	DRAGON_HALBERD("Dragon halberd", "dragon_halberd.png", false, 1, 0),
	DRAGON_MACE("Dragon mace", "dragon_mace.png", false, 6, 6),
	DRAGON_MED_HELM("Dragon med helm", "dragon_med_helm.png", false, 1, 0),
	DRAGON_PLATELEGS("Dragon platelegs", "dragon_platelegs.png", false, 4, 1),
	DRAGON_PLATESKIRT("Dragon plateskirt", "dragon_plateskirt.png", false, 7, 1),
	DRAGON_SCIMITAR_ALT("Dragon scimitar (alt)", "dragon_scimitar_alt.png", false, 1, 2),
	DRAGON_SPEAR("Dragon spear", "dragon_spear.png", false, 13, 1),
	DRAGON_SQ_SHIELD("Dragon sq shield", "dragon_sq_shield.png", false, 20, 1),
	DRAGONFIRE_SHIELD("Dragonfire shield", "dragonfire_shield.png", false, 1, 8),
	DRAGONSTONE("Dragonstone", "dragonstone.png", false, 17, 1),
	EASTER_EGG("Easter egg", "easter_egg.png", false, 16, 6),
	FIRE_CAPE("Fire cape", "fire_cape.png", false, 5, 2),
	FLETCHING_CAPE("Fletching cape", "fletching_cape.png", false, 2, 2),
	GRANITE_MAUL("Granite maul", "granite_maul.png", false, 3, 2),
	GREEN_HWEEN_MASK("Green h'ween mask", "green_hween_mask.png", false, 2, 1),
	GREEN_PARTYHAT("Green partyhat", "green_partyhat.png", false, 3, 1),
	GUTHANS_CHAINSKIRT("Guthan's chainskirt", "guthans_chainskirt.png", false, 13, 2),
	GUTHANS_HELM("Guthan's helm", "guthans_helm.png", false, 2, 4),
	GUTHANS_PLATEBODY("Guthan's platebody", "guthans_platebody.png", false, 15, 3),
	GUTHANS_WARSPEAR("Guthan's warspear", "guthans_warspear.png", false, 2, 1),
	GUTHIX_STAFF("Guthix staff", "guthix_staff.png", false, 1, 2),
	HITPOINTS_CAPE("Hitpoints cape", "hitpoints_cape.png", false, 2, 2),
	HUNTER_CAPE("Hunter cape", "hunter_cape.png", false, 8, 1),
	INFINITY_HAT("Infinity hat", "infinity_hat.png", false, 16, 1),
	KARILS_COIF("Karil's coif", "karils_coif.png", false, 12, 1),
	KARILS_CROSSBOW("Karil's crossbow", "karils_crossbow.png", false, 10, 3),
	KARILS_LEATHERSKIRT("Karil's leatherskirt", "karils_leatherskirt.png", false, 12, 4),
	KARILS_LEATHERTOP("Karil's leathertop", "karils_leathertop.png", false, 12, 6),
	KEY("Key", "key.png", false, 9, 1),
	LOBSTER("Lobster", "lobster.png", false, 11, 9),
	MAGES_BOOK("Mage's book", "mages_book.png", false, 2, 6),
	MASTER_WAND("Master wand", "master_wand.png", false, 1, 5),
	MONKFISH("Monkfish", "monkfish.png", false, 0, 0),
	MYSTIC_MUD_STAFF("Mystic mud staff", "mystic_mud_staff.png", false, 4, 3),
	OBSIDIAN_MAUL("Obsidian maul", "obsidian_maul.png", false, 6, 1),
	PARTYHAT("Partyhat", "partyhat.png", false, 3, 5),
	PURPLE_PARTYHAT("Purple partyhat", "purple_partyhat.png", false, 3, 2),
	QUEST_POINT_CAPE("Quest point cape", "quest_point_cape.png", false, 1, 1),
	RANGING_CAPE("Ranging cape", "ranging_cape.png", false, 2, 2),
	RED_CHINCHOMPA("Red chinchompa", "red_chinchompa.png", false, 5, 6),
	RED_HWEEN_MASK("Red h'ween mask", "red_hween_mask.png", false, 1, 1),
	ROBIN_HOOD_HAT_1("Robin hood hat 1", "robin_hood_hat_1.png", false, 1, 13),
	ROBIN_HOOD_HAT_2("Robin hood hat 2", "robin_hood_hat_2.png", false, 1, 2),
	SANTA_HAT("Santa hat", "santa_hat.png", false, 6, 3),
	SARADOMIN_CROZIER("Saradomin crozier", "saradomin_crozier.png", false, 1, 1),
	SARADOMIN_STAFF("Saradomin staff", "saradomin_staff.png", false, 4, 1),
	SCYTHE("Scythe", "scythe.png", false, 12, 1),
	SHARK("Shark", "shark.png", false, 7, 1),
	STRENGTH_CAPE("Strength cape", "strength_cape.png", false, 2, 2),
	SUQAH_TOOTH("Suqah tooth", "suqah_tooth.png", false, 1, 7),
	TORAGS_HAMMERS("Torag's hammers", "torags_hammers.png", false, 2, 1),
	VERACS_BRASSARD("Verac's brassard", "veracs_brassard.png", false, 10, 8),
	VERACS_FLAIL("Verac's flail", "veracs_flail.png", false, 6, 1),
	VERACS_HELM("Verac's helm", "veracs_helm.png", false, 9, 5),
	VERACS_PLATESKIRT("Verac's plateskirt", "veracs_plateskirt.png", false, 12, 4),
	WHITE_PARTYHAT("White partyhat", "white_partyhat.png", false, 3, 2),
	YELLOW_PARTYHAT("Yellow partyhat", "yellow_partyhat.png", false, 3, 2),
	ZAMORAK_STAFF("Zamorak staff", "zamorak_staff.png", false, 8, 4),
	ZAMORAKIAN_SPEAR("Zamorakian spear", "zamorakian_spear.png", false, 1, 2);

	private final String name;
	private final String resource;
	/**
	 * Whether the image comes from the core Custom Cursor plugin rather than this plugin's own resources
	 */
	private final boolean core;
	private final int hotspotX;
	private final int hotspotY;

	@Override
	public String toString()
	{
		return name;
	}
}
