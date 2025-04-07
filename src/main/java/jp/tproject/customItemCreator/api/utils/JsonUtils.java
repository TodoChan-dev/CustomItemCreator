package jp.tproject.customItemCreator.api.utils;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * アイテムデータをJSON形式にシリアライズするユーティリティクラス
 */
public class JsonUtils {

    /**
     * アイテムマップをJSON配列にシリアライズ
     * @param items アイテムマップ
     * @return JSON文字列
     */
    public static String itemMapToJson(Map<String, ItemStack> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        boolean first = true;
        for (Map.Entry<String, ItemStack> entry : items.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;

            String itemId = entry.getKey();
            ItemStack item = entry.getValue();

            sb.append(itemToBasicJson(itemId, item));
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * アイテムの基本情報をJSONにシリアライズ（一覧表示用）
     * @param itemId アイテムID
     * @param item アイテムスタック
     * @return JSON文字列
     */
    public static String itemToBasicJson(String itemId, ItemStack item) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // 基本情報
        sb.append("\"id\":\"").append(escapeJson(itemId)).append("\",");
        sb.append("\"type\":\"").append(item.getType().name()).append("\",");
        sb.append("\"amount\":").append(item.getAmount()).append(",");

        // 表示名
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            sb.append("\"displayName\":\"").append(escapeJson(meta.getDisplayName())).append("\",");
        } else {
            sb.append("\"displayName\":\"").append(item.getType().name()).append("\",");
        }

        // カスタムモデルデータ
        if (meta != null && meta.hasCustomModelData()) {
            sb.append("\"customModelData\":").append(meta.getCustomModelData());
        } else {
            sb.append("\"customModelData\":0");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * アイテムの詳細情報をJSONにシリアライズ
     * @param itemId アイテムID
     * @param item アイテムスタック
     * @param customItemKey カスタムアイテムキー
     * @return JSON文字列
     */
    public static String itemToDetailJson(String itemId, ItemStack item, NamespacedKey customItemKey) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // 基本情報
        sb.append("\"id\":\"").append(escapeJson(itemId)).append("\",");
        sb.append("\"type\":\"").append(item.getType().name()).append("\",");
        sb.append("\"material\":\"").append(item.getType().name()).append("\",");
        sb.append("\"amount\":").append(item.getAmount()).append(",");

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // 表示名
            if (meta.hasDisplayName()) {
                sb.append("\"displayName\":\"").append(escapeJson(meta.getDisplayName())).append("\",");
            } else {
                sb.append("\"displayName\":\"").append(item.getType().name()).append("\",");
            }

            // ロア（説明文）
            appendLore(sb, meta);

            // エンチャント
            appendEnchantments(sb, meta);

            // 属性（アトリビュート）
            appendAttributes(sb, meta);

            // アイテムフラグ
            appendItemFlags(sb, meta);

            // カスタムモデルデータ
            if (meta.hasCustomModelData()) {
                sb.append("\"customModelData\":").append(meta.getCustomModelData()).append(",");
            } else {
                sb.append("\"customModelData\":0,");
            }

            // 耐久値
            if (meta.isUnbreakable()) {
                sb.append("\"unbreakable\":true,");
            } else {
                sb.append("\"unbreakable\":false,");
            }

            // PersistentDataContainer
            appendPersistentData(sb, meta, customItemKey);
        } else {
            // メタデータがない場合はデフォルト値を設定
            sb.append("\"displayName\":\"").append(item.getType().name()).append("\",");
            sb.append("\"lore\":[],");
            sb.append("\"enchantments\":[],");
            sb.append("\"attributes\":[],");
            sb.append("\"customModelData\":0,");
            sb.append("\"flags\":[],");
            sb.append("\"unbreakable\":false,");
            sb.append("\"persistentData\":{}");
        }

        // 最後のカンマを削除
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * ロア（説明文）をJSONに追加
     * @param sb JSONビルダー
     * @param meta アイテムメタ
     */
    private static void appendLore(StringBuilder sb, ItemMeta meta) {
        sb.append("\"lore\":[");
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (int i = 0; i < lore.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append("\"").append(escapeJson(lore.get(i))).append("\"");
            }
        }
        sb.append("],");
    }

    /**
     * エンチャントをJSONに追加
     * @param sb JSONビルダー
     * @param meta アイテムメタ
     */
    private static void appendEnchantments(StringBuilder sb, ItemMeta meta) {
        sb.append("\"enchantments\":[");
        boolean first = true;
        for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;

            Enchantment enchant = entry.getKey();
            int level = entry.getValue();

            sb.append("{");
            sb.append("\"name\":\"").append(enchant.getKey().getKey()).append("\",");
            sb.append("\"level\":").append(level);
            sb.append("}");
        }
        sb.append("],");
    }

    /**
     * 属性（アトリビュート）をJSONに追加
     * @param sb JSONビルダー
     * @param meta アイテムメタ
     */
    private static void appendAttributes(StringBuilder sb, ItemMeta meta) {
        sb.append("\"attributes\":[");
        if (meta.hasAttributeModifiers()) {
            boolean first = true;
            for (Attribute attribute : Attribute.values()) {
                Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(attribute);
                if (modifiers != null && !modifiers.isEmpty()) {
                    for (AttributeModifier modifier : modifiers) {
                        if (!first) {
                            sb.append(",");
                        }
                        first = false;

                        sb.append("{");
                        sb.append("\"name\":\"").append(attribute.name()).append("\",");

                        EquipmentSlot slot = modifier.getSlot();
                        sb.append("\"slot\":\"").append(slot != null ? slot.name() : "ALL").append("\",");

                        sb.append("\"amount\":").append(modifier.getAmount()).append(",");
                        sb.append("\"operation\":\"").append(modifier.getOperation().name()).append("\",");
                        sb.append("\"uuid\":\"").append(modifier.getUniqueId().toString()).append("\"");
                        sb.append("}");
                    }
                }
            }
        }
        sb.append("],");
    }

    /**
     * アイテムフラグをJSONに追加
     * @param sb JSONビルダー
     * @param meta アイテムメタ
     */
    private static void appendItemFlags(StringBuilder sb, ItemMeta meta) {
        sb.append("\"flags\":[");
        boolean first = true;
        for (ItemFlag flag : meta.getItemFlags()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(flag.name()).append("\"");
        }
        sb.append("],");
    }

    /**
     * PersistentDataContainerをJSONに追加
     * @param sb JSONビルダー
     * @param meta アイテムメタ
     * @param customItemKey カスタムアイテムキー
     */
    private static void appendPersistentData(StringBuilder sb, ItemMeta meta, NamespacedKey customItemKey) {
        sb.append("\"persistentData\":{");

        PersistentDataContainer container = meta.getPersistentDataContainer();
        List<String> keyValuePairs = new ArrayList<>();

        // カスタムアイテムIDを取得
        if (container.has(customItemKey, PersistentDataType.STRING)) {
            String customItemId = container.get(customItemKey, PersistentDataType.STRING);
            keyValuePairs.add("\"customItemId\":\"" + escapeJson(customItemId) + "\"");
        }

        sb.append(String.join(",", keyValuePairs));
        sb.append("}");
    }

    /**
     * JSON文字列をエスケープ
     * @param text エスケープする文字列
     * @return エスケープされた文字列
     */
    public static String escapeJson(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}