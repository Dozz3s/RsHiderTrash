RsHiderTrash - плагин, убирающий мусор из чата, сообщения о входе, кике, выходе, смерти + фильтрация чата.

Конфиг плагина:
​

[CODE=yaml]# Если true, то проверка фраз и regex будет учитывать регистр букв (т.е будет фильтровать в точности как написано в конфиге)

# Если false (рекомендуется) - проверка будет без учета регистра (т.е будет фильтровать такое же слово, но написанное по другому,

# например, если в конфиге "серв какащке", то плагин будет учивать такие типы: СеРв КаКаЩкЕ и т.д

case-sensitive: false



# Настройки фильтра фраз

block-phrases:

enabled: true

disabled-worlds: [] # Миры, где выключенна фильтрация

list:

- "reklama.com"

- "серв какащке"

message_to_player_enabled: true

message_to_player: "&cВ вашем сообщении была замечена запрещенная фраза"



# Настройки фильтра по regex

block-regex:

enabled: false

disabled-worlds: []

list:

- "(http|https)://[^\\s]+"  # URL

- "\\d{10,}"                # Длинные числа (телефоны)

- "[а-яА-Я]{20,}"           # Длинные русские тексты (спам)

message_to_player_enabled: false

message_to_player: "&cВ вашем сообщении была замечена запрещенная фраза"



# Настройки скрытия сообщений о входе

hide-join:

enabled: true

disabled-worlds:

- "primer" # Название мира



# Настройки скрытия сообщений о выходе

hide-quit:

enabled: true

disabled-worlds: [] # [] если нужно скрывать во всех мирах



# Настройки скрытия сообщений о смерти

hide-death:

enabled: true

disabled-worlds: []



# Настройки скрытия сообщений о кике

hide-kick:

enabled: true

disabled-worlds: [][/CODE]
