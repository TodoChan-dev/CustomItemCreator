package jp.tproject.customItemCreator.model;

import jp.tproject.customItemCreator.CustomItemCreator;
import jp.tproject.customItemCreator.util.TranslationUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;
import java.util.*;

/**
 * カスタムアイテムを表すクラス
 * ItemStackのラッパーとして機能し、追加情報を管理
 */
public class CustomItem {

    private ItemStack itemStack;
    private String itemId;
    private Rarity rarity;

    /**
     * 新しいカスタムアイテムを作成
     * @param material アイテムの素材
     */
    public CustomItem(Material material) {
        this.itemStack = new ItemStack(material);
        this.itemId = UUID.randomUUID().toString();
        this.rarity = Rarity.COMMON;

        // デフォルト設定を適用
        applyDefaults();
    }

    /**
     * 既存のItemStackからカスタムアイテムを作成
     * @param itemStack 既存のアイテム
     */
    public CustomItem(ItemStack itemStack) {
        this.itemStack = itemStack.clone();

        // アイテムIDを取得または新規作成
        ItemMeta meta = this.itemStack.getItemMeta();
        NamespacedKey key = CustomItemCreator.getInstance().getCustomItemKey();

        if (meta != null && meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            this.itemId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        } else {
            this.itemId = UUID.randomUUID().toString();
            if (meta != null) {
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, this.itemId);
                this.itemStack.setItemMeta(meta);
            }
        }

        // レア度を判定（表示名から）
        this.rarity = determineRarity();
    }

    /**
     * アイテムの初期設定を適用
     */
    private void applyDefaults() {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            // アイテムIDを保存
            NamespacedKey key = CustomItemCreator.getInstance().getCustomItemKey();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, itemId);

            // デフォルトのフラグを設定
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            itemStack.setItemMeta(meta);
        }
    }

    /**
     * アイテムのレア度を表示名から判定
     * @return 判定されたレア度
     */
    private Rarity determineRarity() {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();
            for (Rarity rarity : Rarity.values()) {
                if (displayName.startsWith(rarity.getDisplayName())) {
                    return rarity;
                }
            }
        }
        return Rarity.COMMON;
    }

    /**
     * アイテムのIDを取得
     * @return アイテムID
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * アイテムのレア度を取得
     * @return レア度
     */
    public Rarity getRarity() {
        return rarity;
    }

    /**
     * アイテムのレア度を設定
     * @param rarity 新しいレア度
     */
    public void setRarity(Rarity rarity) {
        this.rarity = rarity;

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            // 現在の表示名を取得（レア度部分を除去）
            String currentName = meta.hasDisplayName() ? meta.getDisplayName() : itemStack.getType().name();
            for (Rarity r : Rarity.values()) {
                if (currentName.startsWith(r.getDisplayName())) {
                    currentName = currentName.substring(r.getDisplayName().length()).trim();
                    break;
                }
            }

            // 新しいレア度を適用
            meta.setDisplayName(rarity.getColor() + currentName);
            itemStack.setItemMeta(meta);
        }
    }

    /**
     * アイテム名を設定
     * @param name 新しいアイテム名
     */
    public void setName(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            // カラーコードを変換
            name = ChatColor.translateAlternateColorCodes('&', name);

            // レア度を保持
            meta.setDisplayName(rarity.getDisplayName() + " " + name);
            itemStack.setItemMeta(meta);
        }
    }

    /**
     * アイテムの説明文を設定
     * @param lore 説明文のリスト
     */
    public void setLore(List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            // カラーコードを変換
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }

            meta.setLore(coloredLore);
            itemStack.setItemMeta(meta);
        }
    }

    /**
     * カスタムモデルデータを設定
     * @param modelData モデルデータの値
     */
    public void setCustomModelData(int modelData) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(modelData);
            itemStack.setItemMeta(meta);
        }
    }

    /**
     * エンチャントを追加
     * @param enchantment エンチャントの種類
     * @param level エンチャントのレベル
     */
    public void addEnchant(Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);

        // エンチャントの説明をロアに追加
        //updateEnchantmentLore();
    }

    /**
     * 全てのエンチャントの説明をロアに追加する
     */
    private void updateEnchantmentLore() {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        // 既存のエンチャント説明を削除
        lore.removeIf(line -> line.startsWith(ChatColor.GRAY + "【エンチャント】"));

        // エンチャントがある場合のみ処理
        if (!meta.getEnchants().isEmpty()) {
            lore.add(ChatColor.GRAY + "【エンチャント】");

            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                Enchantment enchant = entry.getKey();
                int level = entry.getValue();

                String enchantKey = enchant.getKey().getKey();
                String jaName = TranslationUtil.getEnchantmentJaName(enchantKey);
                String desc = TranslationUtil.getEnchantmentDesc(enchantKey);

                lore.add(ChatColor.BLUE + jaName + " " + getRomanNumeral(level));
                if (!desc.isEmpty()) {
                    lore.add(ChatColor.GRAY + "  " + desc);
                }
            }
        }

        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    /**
     * 属性を追加
     * @param attribute 属性の種類
     * @param value 属性値
     * @param slot 装備スロット
     * @param operation 操作タイプ
     */
    @SuppressWarnings("removal")
    public void addAttribute(Attribute attribute, double value, EquipmentSlot slot, AttributeModifier.Operation operation) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            // 既存の同じ属性+スロットのmodifierを削除
            if (meta.hasAttributeModifiers() && meta.getAttributeModifiers(attribute) != null) {
                for (AttributeModifier modifier : meta.getAttributeModifiers(attribute)) {
                    if (modifier.getSlot() == slot) {
                        meta.removeAttributeModifier(attribute, modifier);
                    }
                }
            }

            AttributeModifier modifier = new AttributeModifier(
                    UUID.randomUUID(),
                    attribute.name().toLowerCase(),
                    value,
                    operation,
                    slot
            );

            meta.addAttributeModifier(attribute, modifier);
            itemStack.setItemMeta(meta);

            // 属性の説明をロアに追加
            updateAttributeLore();
        }
    }

    /**
     * 全ての属性の説明をロアに追加する
     */
    private void updateAttributeLore() {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || !meta.hasAttributeModifiers()) return;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        // 既存の属性説明を削除
        lore.removeIf(line -> line.startsWith(ChatColor.GRAY + "【属性】"));

        // 属性がある場合のみ処理
        if (meta.getAttributeModifiers() != null && !meta.getAttributeModifiers().isEmpty()) {
            lore.add(ChatColor.GRAY + "【属性】");

            for (Attribute attribute : Attribute.values()) {
                Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(attribute);
                if (modifiers != null && !modifiers.isEmpty()) {
                    String attrName = TranslationUtil.getAttributeJaName(attribute);
                    String desc = TranslationUtil.getAttributeDesc(attribute);

                    for (AttributeModifier modifier : modifiers) {
                        DecimalFormat df = new DecimalFormat("#.##");
                        String value = df.format(modifier.getAmount());

                        String slotName = getSlotName(modifier.getSlot());
                        String opName = getOperationPrefix(modifier.getOperation());

                        lore.add(ChatColor.GREEN + attrName + ": " + opName + value + " (" + slotName + ")");
                        if (!desc.isEmpty()) {
                            lore.add(ChatColor.GRAY + "  " + desc);
                        }
                    }
                }
            }
        }

        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    /**
     * スロット名を取得
     * @param slot 装備スロット
     * @return スロット名
     */
    private String getSlotName(EquipmentSlot slot) {
        if (slot == null) return "任意";

        switch (slot) {
            case HAND: return "メインハンド";
            case OFF_HAND: return "オフハンド";
            case HEAD: return "頭";
            case CHEST: return "胴体";
            case LEGS: return "脚";
            case FEET: return "足";
            default: return "任意";
        }
    }

    /**
     * 操作タイプに応じた表示用の接頭辞を取得
     * @param operation 操作タイプ
     * @return 表示用接頭辞
     */
    private String getOperationPrefix(AttributeModifier.Operation operation) {
        switch (operation) {
            case ADD_NUMBER: return "";
            case ADD_SCALAR: return "+";
            case MULTIPLY_SCALAR_1: return "×";
            default: return "";
        }
    }

    /**
     * エンチャントと属性をリセット
     */
    public void resetEnchantmentsAndAttributes() {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            // エンチャントをリセット
            for (Enchantment enchantment : meta.getEnchants().keySet()) {
                meta.removeEnchant(enchantment);
            }

            // 属性をリセット
            for (Attribute attribute : Attribute.values()) {
                if (meta.hasAttributeModifiers() && meta.getAttributeModifiers(attribute) != null) {
                    meta.removeAttributeModifier(attribute);
                }
            }

            itemStack.setItemMeta(meta);

            // ロアから説明を削除
            updateEnchantmentLore();
            updateAttributeLore();
        }
    }

    /**
     * ItemStackを取得
     * @return アイテムのItemStackインスタンス
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * ItemStackを設定
     * @param itemStack 新しいItemStack
     */
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * 数値をローマ数字に変換
     * @param number 変換する数値
     * @return ローマ数字の文字列
     */
    private String getRomanNumeral(int number) {
        if (number <= 0) return "";
        if (number > 10) return String.valueOf(number);

        String[] romans = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return romans[number];
    }
}