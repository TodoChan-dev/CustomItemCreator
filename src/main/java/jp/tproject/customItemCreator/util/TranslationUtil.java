package jp.tproject.customItemCreator.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

/**
 * 翻訳関連のユーティリティクラス
 */
public class TranslationUtil {

    // エンチャント名の日本語マッピング
    private static final Map<String, String> ENCHANTMENT_JA_MAP = new HashMap<>();
    // 属性名の日本語マッピング
    private static final Map<Attribute, String> ATTRIBUTE_JA_MAP = new HashMap<>();
    // 操作タイプの日本語マッピング
    private static final Map<AttributeModifier.Operation, String> OPERATION_JA_MAP = new HashMap<>();
    // エンチャントの説明マッピング
    private static final Map<String, String> ENCHANTMENT_DESC_MAP = new HashMap<>();
    // 属性の説明マッピング
    private static final Map<Attribute, String> ATTRIBUTE_DESC_MAP = new HashMap<>();

    static {
        // エンチャント名のマッピング初期化（Minecraftのデフォルトに合わせる）
        ENCHANTMENT_JA_MAP.put("protection", "ダメージ軽減");
        ENCHANTMENT_JA_MAP.put("fire_protection", "火災保護");
        ENCHANTMENT_JA_MAP.put("feather_falling", "落下耐性");
        ENCHANTMENT_JA_MAP.put("blast_protection", "爆発耐性");
        ENCHANTMENT_JA_MAP.put("projectile_protection", "飛び道具耐性");
        ENCHANTMENT_JA_MAP.put("respiration", "水中呼吸");
        ENCHANTMENT_JA_MAP.put("aqua_affinity", "水中採掘");
        ENCHANTMENT_JA_MAP.put("thorns", "棘の鎧");
        ENCHANTMENT_JA_MAP.put("depth_strider", "水中歩行");
        ENCHANTMENT_JA_MAP.put("frost_walker", "氷渡り");
        ENCHANTMENT_JA_MAP.put("binding_curse", "束縛の呪い");
        ENCHANTMENT_JA_MAP.put("sharpness", "ダメージ増加");
        ENCHANTMENT_JA_MAP.put("smite", "アンデッド特効");
        ENCHANTMENT_JA_MAP.put("bane_of_arthropods", "虫特効");
        ENCHANTMENT_JA_MAP.put("knockback", "ノックバック");
        ENCHANTMENT_JA_MAP.put("fire_aspect", "火属性");
        ENCHANTMENT_JA_MAP.put("looting", "ドロップ増加");
        ENCHANTMENT_JA_MAP.put("sweeping", "掃討の刃");
        ENCHANTMENT_JA_MAP.put("efficiency", "効率強化");
        ENCHANTMENT_JA_MAP.put("silk_touch", "シルクタッチ");
        ENCHANTMENT_JA_MAP.put("unbreaking", "耐久力");
        ENCHANTMENT_JA_MAP.put("fortune", "幸運");
        ENCHANTMENT_JA_MAP.put("power", "射撃ダメージ増加");
        ENCHANTMENT_JA_MAP.put("punch", "パンチ");
        ENCHANTMENT_JA_MAP.put("flame", "火矢");
        ENCHANTMENT_JA_MAP.put("infinity", "無限");
        ENCHANTMENT_JA_MAP.put("luck_of_the_sea", "宝釣り");
        ENCHANTMENT_JA_MAP.put("lure", "入れ食い");
        ENCHANTMENT_JA_MAP.put("loyalty", "忠誠");
        ENCHANTMENT_JA_MAP.put("impaling", "水生特効");
        ENCHANTMENT_JA_MAP.put("riptide", "激流");
        ENCHANTMENT_JA_MAP.put("channeling", "召雷");
        ENCHANTMENT_JA_MAP.put("multishot", "拡散");
        ENCHANTMENT_JA_MAP.put("quick_charge", "高速装填");
        ENCHANTMENT_JA_MAP.put("piercing", "貫通");
        ENCHANTMENT_JA_MAP.put("mending", "修繕");
        ENCHANTMENT_JA_MAP.put("vanishing_curse", "消滅の呪い");
        ENCHANTMENT_JA_MAP.put("soul_speed", "魂の速さ");
        ENCHANTMENT_JA_MAP.put("swift_sneak", "スニーク速度上昇");

        // 属性名のマッピング初期化
        ATTRIBUTE_JA_MAP.put(Attribute.GENERIC_MAX_HEALTH, "最大体力");
        ATTRIBUTE_JA_MAP.put(Attribute.GENERIC_FOLLOW_RANGE, "追跡範囲");
        ATTRIBUTE_JA_MAP.put(Attribute.GENERIC_KNOCKBACK_RESISTANCE, "ノックバック耐性");
        ATTRIBUTE_JA_MAP.put(Attribute.GENERIC_MOVEMENT_SPEED, "移動速度");
        ATTRIBUTE_JA_MAP.put(Attribute.GENERIC_FLYING_SPEED, "飛行速度");
        ATTRIBUTE_JA_MAP.put(Attribute.GENERIC_ATTACK_DAMAGE, "攻撃力");
        ATTRIBUTE_JA_MAP.put(Attribute.GENERIC_ATTACK_KNOCKBACK, "攻撃時ノックバック");
        ATTRIBUTE_JA_MAP.put(Attribute.GENERIC_ATTACK_SPEED, "攻撃速度");
        ATTRIBUTE_JA_MAP.put(Attribute.GENERIC_ARMOR, "防御力");
        ATTRIBUTE_JA_MAP.put(Attribute.GENERIC_ARMOR_TOUGHNESS, "防具強度");
        ATTRIBUTE_JA_MAP.put(Attribute.GENERIC_LUCK, "幸運");
        ATTRIBUTE_JA_MAP.put(Attribute.GENERIC_JUMP_STRENGTH, "馬ジャンプ力");
        ATTRIBUTE_JA_MAP.put(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS, "ゾンビ増援率");

        // 操作タイプのマッピング初期化
        OPERATION_JA_MAP.put(AttributeModifier.Operation.ADD_NUMBER, "数値追加");
        OPERATION_JA_MAP.put(AttributeModifier.Operation.ADD_SCALAR, "乗算（加算後）");
        OPERATION_JA_MAP.put(AttributeModifier.Operation.MULTIPLY_SCALAR_1, "乗算（元の値）");

        // エンチャントの説明マッピング初期化
        ENCHANTMENT_DESC_MAP.put("protection", "あらゆるダメージを軽減します");
        ENCHANTMENT_DESC_MAP.put("fire_protection", "火のダメージを軽減します");
        ENCHANTMENT_DESC_MAP.put("feather_falling", "落下ダメージを軽減します");
        ENCHANTMENT_DESC_MAP.put("blast_protection", "爆発のダメージを軽減します");
        ENCHANTMENT_DESC_MAP.put("projectile_protection", "飛び道具のダメージを軽減します");
        ENCHANTMENT_DESC_MAP.put("respiration", "水中での酸素持続時間を延ばします");
        ENCHANTMENT_DESC_MAP.put("aqua_affinity", "水中での採掘速度を上げます");
        ENCHANTMENT_DESC_MAP.put("thorns", "攻撃してきた相手にダメージを与えます");
        ENCHANTMENT_DESC_MAP.put("depth_strider", "水中での移動速度を上げます");
        ENCHANTMENT_DESC_MAP.put("frost_walker", "水上を歩くと氷ができます");
        ENCHANTMENT_DESC_MAP.put("binding_curse", "装備を外せなくなります");
        ENCHANTMENT_DESC_MAP.put("sharpness", "攻撃ダメージを増加させます");
        ENCHANTMENT_DESC_MAP.put("smite", "アンデッドに対するダメージを増加させます");
        ENCHANTMENT_DESC_MAP.put("bane_of_arthropods", "虫に対するダメージを増加させます");
        ENCHANTMENT_DESC_MAP.put("knockback", "ノックバックの威力を増加させます");
        ENCHANTMENT_DESC_MAP.put("fire_aspect", "攻撃した相手に炎上効果を与えます");
        ENCHANTMENT_DESC_MAP.put("looting", "モブのドロップ率を上げます");
        ENCHANTMENT_DESC_MAP.put("sweeping", "範囲攻撃のダメージを増加させます");
        ENCHANTMENT_DESC_MAP.put("efficiency", "採掘速度を上げます");
        ENCHANTMENT_DESC_MAP.put("silk_touch", "ブロックをそのままの状態でドロップさせます");
        ENCHANTMENT_DESC_MAP.put("unbreaking", "耐久値の減りを抑えます");
        ENCHANTMENT_DESC_MAP.put("fortune", "採掘時のドロップ数を増やします");
        ENCHANTMENT_DESC_MAP.put("power", "弓の攻撃力を増加させます");
        ENCHANTMENT_DESC_MAP.put("punch", "弓のノックバック威力を増加させます");
        ENCHANTMENT_DESC_MAP.put("flame", "矢に炎上効果を付与します");
        ENCHANTMENT_DESC_MAP.put("infinity", "矢を消費せずに発射できます");
        ENCHANTMENT_DESC_MAP.put("luck_of_the_sea", "釣りの際のアイテム入手率を上げます");
        ENCHANTMENT_DESC_MAP.put("lure", "魚が食いつくまでの時間を短縮します");
        ENCHANTMENT_DESC_MAP.put("loyalty", "トライデントが戻ってきます");
        ENCHANTMENT_DESC_MAP.put("impaling", "水生Mobへのダメージを増加させます");
        ENCHANTMENT_DESC_MAP.put("riptide", "トライデントと一緒に飛べます");
        ENCHANTMENT_DESC_MAP.put("channeling", "雷雨時に雷を落とせます");
        ENCHANTMENT_DESC_MAP.put("multishot", "矢を3本同時に発射します");
        ENCHANTMENT_DESC_MAP.put("quick_charge", "クロスボウの装填速度を上げます");
        ENCHANTMENT_DESC_MAP.put("piercing", "矢が敵を貫通します");
        ENCHANTMENT_DESC_MAP.put("mending", "経験値を吸収して耐久値を回復します");
        ENCHANTMENT_DESC_MAP.put("vanishing_curse", "死亡時にアイテムが消滅します");
        ENCHANTMENT_DESC_MAP.put("soul_speed", "ソウルサンド上での移動速度を上げます");
        ENCHANTMENT_DESC_MAP.put("swift_sneak", "スニーク時の移動速度を上げます");

        // 属性の説明マッピング初期化
        ATTRIBUTE_DESC_MAP.put(Attribute.GENERIC_MAX_HEALTH, "最大体力を変更します");
        ATTRIBUTE_DESC_MAP.put(Attribute.GENERIC_FOLLOW_RANGE, "Mobの追跡範囲を変更します");
        ATTRIBUTE_DESC_MAP.put(Attribute.GENERIC_KNOCKBACK_RESISTANCE, "ノックバック耐性を変更します");
        ATTRIBUTE_DESC_MAP.put(Attribute.GENERIC_MOVEMENT_SPEED, "移動速度を変更します");
        ATTRIBUTE_DESC_MAP.put(Attribute.GENERIC_FLYING_SPEED, "飛行速度を変更します");
        ATTRIBUTE_DESC_MAP.put(Attribute.GENERIC_ATTACK_DAMAGE, "攻撃力を変更します");
        ATTRIBUTE_DESC_MAP.put(Attribute.GENERIC_ATTACK_KNOCKBACK, "攻撃時のノックバック威力を変更します");
        ATTRIBUTE_DESC_MAP.put(Attribute.GENERIC_ATTACK_SPEED, "攻撃速度を変更します");
        ATTRIBUTE_DESC_MAP.put(Attribute.GENERIC_ARMOR, "防御力を変更します");
        ATTRIBUTE_DESC_MAP.put(Attribute.GENERIC_ARMOR_TOUGHNESS, "防具強度を変更します");
        ATTRIBUTE_DESC_MAP.put(Attribute.GENERIC_LUCK, "幸運を変更します");
        ATTRIBUTE_DESC_MAP.put(Attribute.GENERIC_JUMP_STRENGTH, "馬のジャンプ力を変更します");
        ATTRIBUTE_DESC_MAP.put(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS, "ゾンビの増援率を変更します");
    }

    /**
     * エンチャントの日本語名を取得
     * @param enchantmentKey エンチャントのキー名
     * @return 日本語名、マッピングがない場合は整形された英語名
     */
    public static String getEnchantmentJaName(String enchantmentKey) {
        return ENCHANTMENT_JA_MAP.getOrDefault(enchantmentKey, formatEnchantmentName(enchantmentKey));
    }

    /**
     * 属性の日本語名を取得
     * @param attribute 属性
     * @return 日本語名、マッピングがない場合は整形された英語名
     */
    public static String getAttributeJaName(Attribute attribute) {
        return ATTRIBUTE_JA_MAP.getOrDefault(attribute, formatAttributeName(attribute.name()));
    }

    /**
     * 操作タイプの日本語名を取得
     * @param operation 操作タイプ
     * @return 日本語名
     */
    public static String getOperationJaName(AttributeModifier.Operation operation) {
        return OPERATION_JA_MAP.getOrDefault(operation, operation.name());
    }

    /**
     * エンチャントの説明を取得
     * @param enchantmentKey エンチャントのキー名
     * @return 説明文
     */
    public static String getEnchantmentDesc(String enchantmentKey) {
        return ENCHANTMENT_DESC_MAP.getOrDefault(enchantmentKey, "");
    }

    /**
     * 属性の説明を取得
     * @param attribute 属性
     * @return 説明文
     */
    public static String getAttributeDesc(Attribute attribute) {
        return ATTRIBUTE_DESC_MAP.getOrDefault(attribute, "");
    }

    /**
     * AttributeModifier.Operationの値を取得
     * @param index インデックス
     * @return Operation
     */
    public static AttributeModifier.Operation getOperationByIndex(int index) {
        switch (index) {
            case 0:
                return AttributeModifier.Operation.ADD_NUMBER;
            case 1:
                return AttributeModifier.Operation.ADD_SCALAR;
            case 2:
                return AttributeModifier.Operation.MULTIPLY_SCALAR_1;
            default:
                return AttributeModifier.Operation.ADD_NUMBER;
        }
    }

    /**
     * エンチャント名を整形（snake_case → Title Case）
     * @param name エンチャント名（スネークケース）
     * @return 整形された名前
     */
    private static String formatEnchantmentName(String name) {
        StringBuilder formatted = new StringBuilder();
        String[] parts = name.split("_");

        for (String part : parts) {
            if (!part.isEmpty()) {
                formatted.append(part.substring(0, 1).toUpperCase())
                        .append(part.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return formatted.toString().trim();
    }

    /**
     * 属性名を整形（SNAKE_CASE → Title Case）
     * @param name 属性名（スネークケース）
     * @return 整形された名前
     */
    private static String formatAttributeName(String name) {
        StringBuilder formatted = new StringBuilder();
        String[] parts = name.split("_");

        for (String part : parts) {
            if (!part.isEmpty()) {
                formatted.append(part.substring(0, 1).toUpperCase())
                        .append(part.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return formatted.toString().trim();
    }
}