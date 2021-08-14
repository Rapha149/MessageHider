# German tutorial / Deutsche Erklärung

## Übersicht

1. [Allgemein](#allgemein)
2. [Commands](#commands)
3. [Config](#config)
    - [Verschiedenes](#verschiedenes)
	- [Voreinstellungen](#voreinstellungen)
		+ [IdleTimeout](#idletimeout)
		+ [Gamemode-Change](#gamemode-change)
		+ [Only Self Commands](#only-self-commands)
		+ [Console Commands](#console-commands)
	- [Eigene Filter](#eigene-filter)
		- [Commands](#commands-2)
		- [Beispiele](#beispiele)
	- [Weitere Infos](#weitere-infos)
	- [1.16+](#1.16+)

## Allgemein

Erst mal: Danke, dass du dieses Plugin nutzt und auch diese Anleitung durchliest!  
Ein paar Infos vorab:  

- Allgemein, wenn du mir etwas mitteilen möchtest, erstelle bitte ein Issue hier.
   Bitte melde dich in folgenden Fällen:
	- Wenn du diese Anleitung in eine andere Sprache übersetzen willst, was ich sehr cool fände!   
		Das Plugin muss aber denke ich nicht übersetzt werden, da es nur sehr wenige Nachrichten hat.
	- Wenn du Anregungen hast, wie man diese Anleitung verbessern kann.
	- Wenn du Fehler findest, Fragen oder allgemein Verbesserungsvorschläge hast.
- Dieses Plugin sammelt anonyme Serverstatistiken via [bStats](https://bstats.org), einen Open-Source-Statistikdienst für Minecraft-Software. Wenn du dies deaktivieren möchtest, kannst du die Datei `plugins/bStats/config.yml` bearbeiten.

Jetzt noch ein paar Infos zum Plugin:

- Mit diesem Plugin kannst du bestimmte Nachrichten vor Spielern verstecken. Dafür gibt es einerseits [Voreinstellungen](#voreinstellungen), du kannst aber auch eigene Filter erstellen. Vorher solltest du dir diese Anleitung aber gut durchlesen, damit du nicht versehentlich alle Nachrichten deaktivierst.
- Du kannst mit diesem Plugin keine Nachrichten vor der Konsole verstecken.
- Wenn du eine mehrzeilige Nachricht bekommst, kannst du diese leider nicht zusammen verstecken, sondern musst mehrere Filter erstellen. Dies ist zum Beispiel der Fall, wenn du einen Vanilla-Command falsch eingibst.

## Commands

Das Plugin an sich hat nur einen Command, nämlich: `/messagehider` (Alias: `/mh`)

### Sub-Commands

- `/messagehider reload` - Lädt die Config neu.
- `/messagehider log <start|stop>` - Startet oder stoppt das Loggen von Nachrichten, die zu dir kommen. Das ist nützlich, wenn du neue Filter hinzufügen willst, aber nicht genau weißt, wie die Nachricht (in JSON) aussieht.  
Der Log wird bei `plugins/MessageHider/logs/SPIELERNAME.log` gespeichert und auch beim erneuten Ausführen gelöscht. Natürlich kann man den Log auch manuell löschen.  
Es werden auch gefilterte Nachrichten geloggt.
- `/messagehider create` - Erstellt einen neuen leeren Filter. Das ist nützlich, damit man nicht vergisst, etwas einzustellen.
- `/messagehider check <json|plain> <Filter-IDs> <Nachricht>` - Mit diesem Sub-Command kannst du schauen, ob eine Nachricht versteckt werden würde. Hierzu kannst du angeben, ob du eine JSON oder eine Plain-Nachricht angibst und du kannst die IDs von den Filtern angeben, mit denen das Plugin die Nachricht überprüfen soll. Wenn die Nachricht durch alle Filter laufen soll, gebe einen Bindestrich an. Beachte, dass das Plugin in diesem Prozess nicht nach den Receivern und Sendern filtert, da dies unlogisch wäre.

### Permissions

- `/messagehider reload` - messagehider.reload
- `/messagehider log` - messagehider.log
- `/messagehider create` - messagehider.create
- `/messagehider check` - messagehider.check

## Config

Die Config befindet sich bei `plugins/MessageHider/config.yml`.
Wenn die Config das erste mal generiert wird, sieht sie so aus:

```yml
checkForUpdates: true
messageFilters: [
  ]
prefix: '&8[&cMH&8] '
presets:
  consoleCommands: false
  gamemodeChange: false
  idleTimeout: false
  onlySelfCommands: false
```

### Verschiedenes

- `checkForUpdates`: Wenn aktiviert wird beim Enablen des Plugins und wenn man als Operator dem Server joined geprüft, ob das Plugin aktuell ist. Wenn es nicht aktuell ist, wird eine Nachricht in die Konsole bzw. an den Spieler gesendet.
- `prefix`: Der Prefix des Plugins, wird allen Nachrichten vorangestellt.  
  Zum Beispiel: *[MH] The config was reloaded.*

### Voreinstellungen

Wenn du nur die Voreinstellungen (Presets) brauchst, brauchst du dich um `messageFilters` nicht zu kümmern.  
Um die Voreinstellungen zu aktivieren, kannst du einfach bei der jeweiligen Voreinstellung das `false` zu einem `true` machen.

#### IdleTimeout

Wenn diese Voreinstellung aktiviert ist, wirst du nicht mehr die Nachrichten wie  
*\[Server: The player idle timeout is now X minutes\]*  
bekommen. Wenn man den IdleTimeout selber ändert, bekommst du die Nachricht schon, aber nicht, wenn der Server (die Konsole) oder ein anderer Spieler sie ändert. Dies ist extrem nützlich, wenn du auf einem Hosting-Anbieter wie [PloudOS](https://ploudos.com) spielst und dort nicht immer diese Nachricht bekommen willst.

#### Gamemode-Change

Wenn diese Voreinstellung aktiviert ist, bekommst du nicht mehr die Nachrichten, wenn andere Spieler ihren Spielmodus ändern.

#### Only Self Commands

Wenn diese Voreinstellung aktiviert ist, bekommt man nur noch Nachrichten von eigenen Commands. (1.16+)

#### Console Commands

Wenn diese Voreinstellung aktiviert ist, bekommt man als Operator nicht mehr Nachrichten von Commands, die aus der Konsole gesendet wurden. (1.16+)

### Eigene Filter

Wenn einem die Voreinstellungs-Möglichkeiten nicht reichen, kann man sich selber Filter erstellen. Am besten startet man dafür mit `/messagehider create`, was einem einen leeren Filter erstellt.

Es gibt folgende Einstellungen: (Sie sind hier anders sortiert als in der Config, weil es in der Config alphabetisch ist)

- `id (Text)` - Die ID von dem Filter. Dies wird nur `/messagehider check` benutzt, wenn du die ID nicht brauchst, kannst du es einfach bei `null` lassen. Du kannst bei mehreren Filtern dieselbe ID angeben, allerdings ist dies nicht zu empfehlen. Für die ID dürfen nur normale Buchstaben, Zahlen und Unterstriche verwendet werden.

- `json (true/false)` - Wenn aktiviert, wird die angegebene Filter-Nachricht als JSON interpretiert. Wenn nicht, wird sie als Plain-Message interpretiert, das kann auch eine Lang-Nachricht von Mojang sein. (z.B: `commands.setidletimeout.success`)

- `jsonPrecisionLevel (Nummer)` - Nur wenn JSON aktiviert, Nummern:
	+ `0`: Es werden nur die Schlüsselwörter überprüft, die auf beiden Seiten (in der angegebenen Nachricht und in der Nachricht, die gesendet wurde) existieren.
	+ `1`: Es werden nur die Schlüsselwörter überprüft, die auf der linken Seite (angegebene Nachricht) existieren. Wenn ein Schlüsselwort, was auf der linken Seite existiert, aber nicht auf der rechten Seite (gesendete Nachricht), schlägt der Filter nicht an.
	+ `2`: Es werden alle Schlüsselwörter überprüft, allerdings werden Schlüsselwörter, die auf der rechten Seite existieren, aber `false` sind, ignoriert.
	+ `3`: Alle Schlüsselwörter werden ausnahmslos überprüft.

  Wenn ein Schlüsselwort auf der linken Seite ignoriert werden soll, kann als Value `<ignore>` angegeben werden.
  
- `regex (true/false)` - Wenn aktiviert, wird die Plain-Message oder die JSON-Values nach Regex überprüft. Eine Guide zu Regex gibt es auf [RegExr](https://regexr.com/). Achtung: Satzzeichen wie Punkte werden, wenn Regex aktiviert ist, anderes interpretiert. Dies kann man mit einem `\` davor verhindern. Mehr Infos auf RegExr.

- `ignoreCase (true/false)` - Wenn aktiviert, wird die Groß- und Kleinschreibung beim Filtern ignoriert. Wenn JSON aktiviert ist, zählt dies nur für die Werte, nicht für die Schlüsselwörter.

- `onlyHideForOtherPlayers (true/false)` - Wenn aktiviert, werden Nachrichten, die man selber gesendet hat, nicht für einen selber gefiltert. (1.16+)

- `priority (Nummer/null)` - Die Priorität nach der die Filter angewendet werden sollen. Je niedriger die Priorität eines Filters ist, desto früher wird er ausgeführt. Standardmäßig ist die Priorität `null`, hierbei wird der Filter als letztes ausgeführt. Die Priorität ist nur wirklich wichtig, wenn Nachrichten ersetzt werden, da es dann darauf ankommt, welcher Filter die Nachricht zuerst ersetzt.

- `message (Text)` - Die Nachricht, nach der gefiltert werden soll. Wenn JSON aktiviert, im JSON-Format.

- `replacement (Text)` - Der Text, durch den die Nachricht ersetzt werden soll, wenn der Filter passt. Der Text kann als JSON oder als normaler Text angegeben werden. Auch kann man mit &-Zeichen Farben benutzen. Wenn `regex` aktiviert ist, kann mit `$1`, `$2` und `$n` auf die erste, zweite und `n`te Gruppe des Patterns zugegriffen werden. Wenn `null` als Replacement angegeben wird (Standard), wird die Nachricht wie normal versteckt und nicht ersetzt. Wenn ein anderer Filter vorher schon die Nachricht versteckt hätte, wird die Nachricht trotzdem noch ersetzt, aber nachdem die Nachricht einmal ersetzt wurde, wird sie nicht weiter verändert.

- `commands (Liste)` - Eine Liste von Commands, die ausgeführt werden sollen, wenn der Filter passt. Mehr Infos weiter unten.

- `onlyExecuteCommands (true/false)` - Wenn aktiviert, wird die Nachricht nicht versteckt oder ersetzt, sondern es werden nur die Befehle ausgeführt. Außerdem werden weitere Filter angewendet, wenn dies aktiviert ist und der Filter passt.

- `senders (Liste)` - Wenn die Liste leer gelassen wird es ignoriert. Wenn mindestens ein Spieler angegeben ist, werden nur Nachrichten, die von den angegebenen Spielern gesendet wurden, mit diesem Filter gefiltert. (1.16+)  
  Es kann entweder der Spielername, die UUID oder `CONSOLE` für die Konsole angegeben werden.
  
- `excludedSenders (Liste)` - So wie `senders`, nur andersrum. Von jedem, der hier drin steht, werden gesendete Nachrichten mit diesem Filter nicht gefiltert. (1.16+)  
  Es kann entweder der Spielername, die UUID oder `CONSOLE` für die Konsole angegeben werden.
  
- `receivers (Liste)` - Wenn die Liste leer gelassen wird es ignoriert. Wenn mindestens ein Spieler angegeben ist, wird eine Nachricht nur für die angegebenen Spieler gefiltert.  
  Es kann entweder der Spielername, die UUID oder `CONSOLE` für die Konsole angegeben werden.
  
- `excludedReceivers (Liste)` - So wie `receivers`, nur andersrum. Die Nachricht wird für jeden, der hier drin steht, nicht gefiltert.  
  Es kann entweder der Spielername, die UUID oder `CONSOLE` für die Konsole angegeben werden.

#### Commands

Da die Befehle auch von Spieler und Nachricht abhängig sein sollen, gibt es bestimmte Sachen, die dort ersetzt werden können. Diese Placeholder werden vom Plugin bereitgestellt:

- `%mh_player_sender_name%` - Der Name des Spielers, der die Nachricht gesendet hat. `[CONSOLE]` wenn es die Konsole bzw. der Server war. (1.16+)
- `%mh_player_sender_uuid%` - Die UUID des Spielers, der die Nachricht gesendet hat. `[CONSOLE]` wenn es die Konsole bzw. der Server war. (1.16+)
- `%mh_player_receiver_name%` - Der Name des Spielers, der die Nachricht bekommt. 
- `%mh_player_receiver_uuid%` - Die UUID des Spielers, der die Nachricht bekommt.
- `%mh_message_sent_plain%` - Die Nachricht, die gesendet wurde, als Plain-Text.
- `%mh_message_sent_json%` - Die Nachricht, die gesendet wurde, als JSON-Text.
- `%mh_message_replaced_plain%` - Das Replacement, als Plain-Text. Wenn die Nachricht nicht ersetzt wurde, ist es die Nachricht, die gesendet wurde.
- `%mh_message_replaced_json%` - Das Replacement, als JSON-Text. Wenn die Nachricht nicht ersetzt wurde, ist es die Nachricht, die gesendet wurde. Wenn das Replacement nicht als JSON angegeben wurde, ist es ein Plain-Text.
- `%mh_regex_{Gruppe}%` - Eine Regex-Gruppe. Nur möglich, wenn Regex beim Filter aktiviert ist. (Nur mit der PlaceholderAPI möglich)

**Ohne PlaceholderAPI**
Auch wenn die [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) nicht installiert ist, klappen alle von diesem Plugin bereitgestellten Placeholder (Außer `%mh_regex_{Gruppe}%`) 

**Mit PlaceholderAPI**
Wenn die [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) installiert ist, können die oben genannten Placeholder und Placeholder, die von anderen Extensions/Plugins bereitgestellt wurden, in den Befehlen verwendet werden.

#### Beispiele

##### Voreinstellung: IdleTimeout (Version für 1.13+)

```yml
commands: [
]
excludedReceivers: [
]
excludedSenders: [
]
id: idle_timeout
ignoreCase: false
json: true
jsonPrecisionLevel: 3
message: '{"italic": true, "color": "gray", "translate": "chat\\.type\\.admin", "with": [{"text": "Server"}, {"translate": "commands\\.setidletimeout\\.success", "with": ["\\d+"]}]}'
onlyExecuteCommands: false
onlyHideForOtherPlayers: false
priority: null
receivers: [
]
regex: true
replacement: null
senders: [
]
```

Erklärung für JsonPrecisionLevel: Es ist `3`, weil diese Nachricht exakt so ist.  
Erklärung für `\\.`: Da hier Regex aktiviert ist, müssen wir den Punkt escapen (Ein Backslash davor tun). Und da es JSON ist, müssen wir das Backslash erneut escapen, also zwei Backslashes.

##### Voreinstellung: Gamemode-Change

```yml
commands: [
]
excludedReceivers: [
]
excludedSenders: [
]
id: gamemode_change
ignoreCase: false
json: true
jsonPrecisionLevel: 1
message: '{"italic": true, "color": "gray", "translate": "chat\\.type\\.admin", "with": [{},{"translate": "commands\\.gamemode\\.success\\.\\w+"}]}'
onlyExecuteCommands: false
onlyHideForOtherPlayers: false
priority: null
receivers: [
]
regex: true
replacement: null
senders: [
]
```

Erklärung für JsonPrecisionLevel: Es ist `1`, weil bei `with` die ersten Values des Arrays nur als `{}` angegeben wurden. Weil es `1` ist, wird das, was da eigentlich drin stände, ignoriert.

##### Voreinstellung: Only Self Commands

```yml
commands: [
]
excludedReceivers: [
]
excludedSenders: [
]
id: only_self_commands
ignoreCase: false
json: true
jsonPrecisionLevel: 1
message: '{"italic": true, "color": "gray", "translate": "chat\\.type\\.admin", "with": [{},{"translate": "commands\\.(\\w|\\.)+"}]}'
onlyExecuteCommands: false
onlyHideForOtherPlayers: false
priority: null
receivers: [
]
regex: true
replacement: null
senders: [
]
```

##### Voreinstellung: Console Commands

```yml
commands: [
]
excludedReceivers: [
]
excludedSenders: [
]
id: console_commands
ignoreCase: false
json: true
jsonPrecisionLevel: 1
message: '{"italic":true,"color":"gray","translate":"chat\\.type\\.admin","with":[{},{"translate":"commands\\.(\\w|\\.)+"}]}'
onlyExecuteCommands: false
onlyHideForOtherPlayers: false
priority: null
receivers: [
]
regex: true
replacement: null
senders: 
  - <console>
```

##### Beispiel für Senders und Receivers

```yml
commandsd: [
]
excludedReceivers:
  - Rapha149
  - Notch
excludedSenders: [robabla, Notch]
id: beispiel
ignoreCase: false
json: false
jsonPrecisionLevel: 2
message: 'Hallo :)'
onlyExecuteCommands: false
onlyHideForOtherPlayers: false
priority: null
receivers:
  - 073b1315-85ff-49d8-8041-51627214dae0
  - b3884719-7b05-47c7-82db-656bdcd99050
regex: false
replacement: null
senders: 
  - robabla
  - 073b1315-85ff-49d8-8041-51627214dae0
  - <console>
```

Erklärung:

- `excludedReceivers` - Hier wurden die Minecraft-Namen der Spieler angegeben.
- `excludedSenders` - Hier wurden die Spieler in eckige Klammern anstatt in eine YAML-Liste mit `-` geschrieben. Das kann man machen, allerdings ersetzt es das Plugin beim Neuladen der Config mit der Liste mit `-`.
- `receivers` - Hier wurden UUIDs anstelle der Spielernamen angegeben.
- `senders` - Hier wurde nochmal gezeigt, dass auch alle drei Möglichkeiten, Spieler anzugeben, gemeinsam genutzt werden können.

#### Weitere Infos

- Ich empfehle die UUIDs anzugeben, da das Plugin bei Namen die UUIDs aus dem Internet abrufen muss. Das kann das Neuladen der Config verlangsamen oder das Plugin unbrauchbar machen, wenn das Plugin aus irgendeinem Grund nicht auf das Internet oder die Website [MC-Heads](https://www.mc-heads.net/) zugreifen kann.
- Wenn der Onlinemode des Servers auf `false` ist, vergleicht das Plugin die Namen nicht mit UUIDs. Wenn bei UUIDs angegeben wurden, vergleicht es diese, wenn aber Namen angegeben wurden, werden Namen mit Namen verglichen.
- Dieses Beispiel ist nicht logisch, da `excludedReceivers` und `receivers` (auch `excludedSenders` und `senders`) angegeben wurde. Es ist nur zu Beispielzwecken gedacht.

#### 1.16+

Ein paar Filter und Funktionen wie `senders` sind ja nur für 1.16+ verfügbar. Dies ist so, weil man ab dieser Version herausfinden kann, wer eine Nachricht gesendet hat.

**Für Plugin-Programmierer**  
Ich mache das über das `PacketPlayOutChat` Packet. Nur ab der 1.16 gibt es dort das Feld `c` vom Typ `UUID`. Wenn jemand weiß, wie das auch schon in tieferen Versionen geht, schreibt mich gerne an!

## Schluss

Ich hoffe diese Anleitung war verständlich, sonst melde dich, wie gesagt, gerne via Issue.  
Viel Spaß mit dem Plugin :)