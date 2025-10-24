# AGENTS.md


## TempBlock Mod 仕様書（Fabric／NeoForge／Forge対応）

---

## 📘 概要

プレイヤーが特定のブロックを設置すると、**数秒後に自動で破壊される**。
設定可能な項目：

* 機能のオン／オフ
* 対象ブロック
* 遅延秒数

設定方法はプラットフォームごとに異なる：

* **Fabric版**：ModMenu＋Cloth ConfigでGUI設定
* **NeoForge／Forge版**：ゲーム内コマンドで設定操作

---

## 🧩 基本情報

| 項目        | 内容                                            |
| :-------- | :-------------------------------------------- |
| Mod名      | Auto Break Block                              |
| 作者        | april carlson                                 |
| バージョン     | 1.0.0                                         |
| 対応環境      | Fabric / NeoForge / Forge                     |
| 対応MCバージョン | 1.21.10                                       |
| 設定ファイル    | `config/autobreakblock.json`                  |
| 依存Mod     | （Fabricのみ）Fabric API / Cloth Config / ModMenu |

---

## ⚙️ 主な機能

### 1️⃣ 自動破壊

* 対象ブロックを設置後、設定された秒数が経過すると自動的に破壊される。
* 通常のブロック破壊と同様にアイテムドロップを発生させる。
* マルチプレイ環境ではサーバー側で処理。

---

## ⚙️ 設定項目一覧

| 設定名            | 型       | 説明                            | 初期値                 |
| :------------- | :------ | :---------------------------- | :------------------ |
| `enabled`      | boolean | 機能オン／オフ                       | `true`              |
| `targetBlock`  | string  | 対象ブロックID（例：`minecraft:stone`） | `"minecraft:stone"` |
| `delaySeconds` | int     | 破壊までの秒数                       | `5`                 |

---

## 🧠 共通動作仕様

1. **ブロック設置イベント検知**

   * プレイヤーがブロックを設置するとトリガーされる。
   * 設置されたブロックの `ID` を取得。

2. **対象ブロック判定**

   * `targetBlock` 設定値と一致した場合のみ破壊処理予約。

3. **破壊スケジュール登録**

   * 現在のゲームティック＋`delaySeconds`に応じて破壊予定を登録。

4. **時間経過後の破壊処理**

   * 指定ティック到達時にブロックが自動で破壊される。
   * 設定がオフ（`enabled=false`）のときはスキップ。

---

## 🧩 設定保存形式（全環境共通）

`config/autobreakblock.json`

```json
{
  "enabled": true,
  "targetBlock": "minecraft:stone",
  "delaySeconds": 5
}
```

---

## 🪟 Fabric版：GUI設定仕様

### 使用技術

* **Fabric API**
* **Cloth Config API**
* **ModMenu**

### 設定UI項目

| 項目       | 操作タイプ          | 備考                     |
| :------- | :------------- | :--------------------- |
| 機能の有効化   | トグルスイッチ        | 即時反映                   |
| 対象ブロックID | テキスト＋ドロップダウン補完 | `Registry.BLOCK`から自動取得 |
| 秒数設定     | 数値入力           | 範囲制限なし（負値無効）           |

### ブロックID補完仕様

* `Registry.BLOCK` の登録リストを参照し、
  入力中の文字列から候補を動的表示。
* 表示形式：`namespace:block_name`
* 存在しないブロックIDはエラー表示で保存不可。

---

## 🧮 NeoForge／Forge版：コマンド設定仕様

### コマンド形式

```
/autobreak <subcommand> [value]
```

### サブコマンド一覧

| コマンド                          | 引数     | 内容           |
| :---------------------------- | :----- | :----------- |
| `/autobreak toggle`           | なし     | 機能のオン／オフ切替   |
| `/autobreak block <block_id>` | ブロックID | 対象ブロックを変更    |
| `/autobreak delay <seconds>`  | 数値     | 破壊までの遅延秒数を設定 |
| `/autobreak status`           | なし     | 現在設定の確認表示    |

### コマンドの権限

* シングルプレイ：全員使用可能
* マルチプレイ：OP権限必須

### 実行結果例

```
AutoBreakBlock enabled: true
Target block: minecraft:stone
Delay: 5 seconds
```

---

## 🧪 テストケース一覧

| ケース                  | 条件                                | 期待結果              |
| :------------------- | :-------------------------------- | :---------------- |
| 有効状態で対象ブロック設置        | enabled=true                      | 数秒後に破壊される         |
| 機能無効時                | enabled=false                     | 何も起きない            |
| 対象外ブロック設置            | targetBlock不一致                    | 無処理               |
| 秒数変更テスト              | delaySeconds=10                   | 約10秒後に破壊          |
| GUIでID補完             | "m"入力                             | `minecraft:`系候補表示 |
| Forgeコマンド設定          | `/autobreak block minecraft:dirt` | 設定が変更・保存される       |
| NeoForge／Fabricクロス環境 | 設定ファイル共通                          | 互換性維持             |

---

## 🔧 内部構成（共通方針）

| クラス／モジュール                      | 役割              |
| :----------------------------- | :-------------- |
| `AutoBreakBlockMod`            | 初期化・イベント登録      |
| `ConfigManager`                | JSON設定ファイルの読み書き |
| `BlockPlaceListener`           | ブロック設置検知        |
| `AutoBreakScheduler`           | 時間経過による破壊処理管理   |
| `CommandHandler`（Forge系のみ）     | コマンド登録と処理       |
| `ModMenuIntegration`（Fabricのみ） | GUI設定管理＋補完対応    |

---
