# Changelog / История изменений

All notable project changes are documented here. / Здесь фиксируются основные изменения проекта.

## [1.8.0] - 2026-11-09

### Added / Добавлено
- Store the planned care date separately from the actual completion date (`completedAt`). / Раздельное хранение плановой даты и фактической даты выполнения (`completedAt`).
- Add an already-completed watering event when a plant is created with a last-watered date. / При добавлении растения с датой последнего полива в календарь добавляется выполненная процедура.
- Preserve actual completion dates in care history and monthly statistics. / Фактические даты выполнения учитываются в истории и месячной статистике.
- Automatically schedule the next watering or fertilizing event based on the completion date. / Следующий полив или подкормка автоматически планируется от фактической даты выполнения.
- Update the app display name to Cashmere Botanica. / Обновлено отображаемое название приложения на Cashmere Botanica.

### Technical / Техническое
- Keep the existing Android application ID `com.greenhome.app` for update compatibility. / Сохранён прежний идентификатор приложения `com.greenhome.app` для совместимости обновлений.
- App version: `versionName = 1.8.0`, `versionCode = 2`.
