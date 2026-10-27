# Проверки первого инкремента — 2026-10-27

Java 25: scripts/build-core.sh PASS, 10277 assertions. Созданы BeheadingCalculator, BeheadingConfig, StackingMode, тестовый исполнитель и build script. Внешние игровые API в ядре не используются. Результат — библиотека build/beheading-core.jar, не загружаемый мод. Игровые тесты enchantment, loot, Silent Gear, persistence и dedicated server не выполнялись. Следующий этап: реальные enchantment/loot adapters и регрессионные сценарии.
