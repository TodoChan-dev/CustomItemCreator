package jp.tproject.customItemCreator.model;

import org.bukkit.ChatColor;

/**
 * アイテムのレア度を表す列挙型
 */
public enum Rarity {
    COMMON(ChatColor.WHITE + "Common", ChatColor.WHITE),
    UNCOMMON(ChatColor.GREEN + "Uncommon", ChatColor.GREEN),
    RARE(ChatColor.BLUE + "Rare", ChatColor.BLUE),
    EPIC(ChatColor.DARK_PURPLE + "Epic", ChatColor.DARK_PURPLE),
    LEGENDARY(ChatColor.GOLD + "Legendary", ChatColor.GOLD),
    MYTHIC(ChatColor.YELLOW + "Mythic\n", ChatColor.YELLOW);

    private final String displayName;
    private final ChatColor color;

    Rarity(String displayName, ChatColor color) {
        this.displayName = displayName;
        this.color = color;
    }

    /**
     * 表示名を取得
     * @return レア度の表示名
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * レア度の色を取得
     * @return レア度のチャットカラー
     */
    public ChatColor getColor() {
        return color;
    }

    /**
     * 文字列からレア度を取得
     * @param name レア度の名前
     * @return レア度、見つからない場合はCOMMON
     */
    public static Rarity fromString(String name) {
        for (Rarity rarity : values()) {
            if (rarity.name().equalsIgnoreCase(name)) {
                return rarity;
            }
        }
        return COMMON;
    }
}