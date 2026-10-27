# Beheading: аудит и граница первой реализации, 2026-10-27

## Подтверждено по локальным исходникам

- Цель: Minecraft 26.1.2, NeoForge 26.1.2.122, Java 25. Проверены gradle.properties портов и version.json игрового JAR.
- Silent Gear 4.2.3-fleet.1, HEAD 012ffa34, Silent Lib 11.2.0. Исходники: ../silent-stack/Silent-Gear. Лицензия SG MIT. Код сторонних модов в этот проект не копируется.
- TraitEffect, TraitEffectTypes, TraitBuilder, TraitHelper.getTraitLevel/getTraits, GetTraitsEvent, UpgradeGearPart/UpgradePartBuilder существуют. TraitInstance и эффекты имеют codec/stream codec. Применимость конкретных условий trait необходимо проверить перед адаптером.
- AttachDataComponentsTraitEffect применяет DataComponentPatch при recalculation; очистка при исчезновении trait ещё не доказана. Предпочтительно читать актуальный trait, а не хранить отдельную потенциально устаревшую копию бонуса.
- MISC_UPGRADE допускает до 256 частей, но UpgradeSmithingRecipe отклоняет одинаковую часть через hasPart. Его applyUpgrade изменяет локальную дополнительную копию, которую не возвращает. Это отдельный дефект текущего порта; здесь не исправлен. UpgradeGearPart.getType всегда MISC_UPGRADE, JSON part_type не даёт независимый слот.
- В NeoForge 26.1.2.122 LootTableLoadEvent содержит registry lookup и getTable/setTable; вызывается на сервере при загрузке и reload, включая datapack-таблицы.
- VanillaEntityLoot для WITHER_SKELETON_SKULL: один pool, killedByPlayer и randomChanceAndLootingBoost(0.025, 0.01). Condition учитывает EventHooks.getEntityLootEnchantmentLevel. Повторное чтение или моделирование Looting вне этой точки может изменить поведение сторонних модов.
- LivingDropsEvent доступен, но независимый дополнительный бросок в нём не удовлетворяет требованию единственного броска и не выбран для существующего vanilla head pool.
- В просмотренном .port-tools/srv26/mods нет Just Mob Heads. Это тестовая конфигурация, не доказательство отсутствия во всех сборках. Полная инвентаризация head-провайдеров остаётся открытой.

## Решение для первого инкремента

Независимый Java 25 core вычисляет только вероятность, без RNG, событий и выдачи предметов. Вход baseChance уже включает оригинальный Looting; калькулятор не увеличивает его повторно. Сочетание источников по умолчанию MAX. Предел ограничивает прибавку, не уменьшает уже существующую исходную вероятность, в том числе гарантированную голову. Это сохраняет оригинальную добычу при снижении cap в конфигурации.

Для будущего адаптера требуется замена именно существующего chance-condition head-only pool, с сохранением остальных предикатов/функций/entries и оригинального Looting hook. При неизвестной форме таблицы или внешнем обработчике дропа — отказ от бонуса с диагностикой до разработки проверенного адаптера. Не переписывать целиком vanilla loot table datapack-файлом. Charged-creeper death path не изменять.

## План и неизвестное

Этап 1 продолжается для head-провайдеров, eligibility/снарядов, enchantment definitions и API совместимости. Этап 2: безопасный контракт calculator зафиксирован. Этап 3 начат только для математического ядра. Enchantment, NeoForge entrypoint, конфигурационный файл, datapack registry, Silent Gear adapter/модули, рецепты, локализация интерфейса и runtime-проверки ещё не реализованы.

Следующий шаг: подтвердить структуру современного enchantment и безопасную замену loot condition; интеграционные тесты единственного RNG-вызова, гарантированной добычи и перезагрузки таблиц. Не считать core JAR устанавливаемым Minecraft-модом.
