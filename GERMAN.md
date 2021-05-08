# German tutorial / Deutsche Erklärung

## Übersicht

1. [Allgemein](#allgemein)
2. [Commands](#commands)
3. [Config](#config)
	- [Voreinstellungen](#voreinstellungen)
		+ [IdleTimeout](#idletimeout)
		+ [Gamemode-Change](#gamemode-change)
		+ [Only Self Commands](#only-self-commands)
	- [Eigene Filter](#eigene-filter)
		+ [Beispiele](#beispiele)
	
## Allgemein

Erst mal: Danke, dass du dieses Plugin nutzt und auch diese Anleitung durchließt!  
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

- `/messagehider log <start|stop>` - Startet oder stoppt das Loggen von Nachrichten, die zu dir kommen. Das ist nützlich, wenn du neue Filter hinzufügen willst, aber nicht genau weißt, wie die Nachricht (in JSON) aussieht.  
Der Log wird bei `plugins/MessageHider/logs/SPIELERNAME.log` gespeichert und auch beim erneuten Ausführen gelöscht. Natürlich kann man den Log auch manuell löschen.  
Es werden auch gefilterte Nachrichten geloggt.
- `/messagehider create` - Erstellt einen neuen leeren Filter. Das ist nützlich, damit man nicht vergisst, etwas einzustellen.
- `/messagehider reload` - Lädt die Config neu.

### Permissions

- `/messagehider log` - messagehider.log
- `/messagehider create` - messagehider.create
- `/messagehider reload` - messagehider.reload

## Config

Die Config befindet sich bei `plugins/MessageHider/config.yml`.

### Voreinstellungen

Wenn die Config das erste mal generiert wird, sieht sie so aus:

```yml
messageFilters: [  
]  
presets:  
  gamemodeChange: false  
  idleTimeout: false  
  onlySelfCommands: false
```

Wenn du nur die Voreinstellungen (Presets) brauchst, brauchst du dich um `messageFilters` nicht zu kümmern.  
Um die Voreinstellungen zu aktivieren, kannst du einfach bei der jeweiligen Voreinstellung das `false` zu einem `true` machen.

#### IdleTimeout

Wenn diese Voreinstellung aktiviert ist, wirst du nicht mehr die Nachrichten wie  
*\[Server: The player idle timeout is now X minutes\]*  
bekommen. Wenn man den IdleTimeout selber ändert, bekommst du die Nachricht schon, aber nicht, wenn der Server (die Konsole) oder ein anderer Spieler sie ändert. Dies ist extrem nützlich, wenn du auf einem Hosting-Anbieter wie [PloudOS](https://ploudos.com) spielst und dort nicht immer diese Nachricht bekommen willst.

#### Gamemode-Change

Wenn diese Voreinstellung aktiviert ist, bekommst du nicht mehr die Nachrichten, wenn andere Spieler ihren Spielmodus ändern.

#### Only Self Commands

Wenn diese Voreinstellung aktiviert ist, bekommt man nur noch Nachrichten von eigenen Commands. Dies klappt leider nur in 1.16+, weil es dadrunter die Methode dafür noch nicht bei Spigot gibt. 

**Für Plugin-Programmierer**  
Ich mache das über das `PacketPlayOutChat` packet. Nur ab der 1.16 gibt es dort das Feld `c` vom Typ `UUID`. Wenn jemand weiß, wie das auch schon in tieferen Versionen geht, schreibt mich gerne an!

### Eigene Filter

Wenn einem die Voreinstellungs-Möglichkeiten nicht reichen, kann man sich selber Filter erstellen. Am besten startet man dafür mit `/messagehider create`, was einem einen leeren Filter erstellt.

Es gibt folgende Einstellungen: (Sie sind hier anders sortiert als in der Config, weil es in der Config alphabetisch ist)

- `json (true/false)` - If enabled, the specified filter message is interpreted as JSON. If not, it will be interpreted as a plain message, which can also be a language message from Minecraft. (e.g.: `commands.setidletimeout.success`)
- `jsonPrecisionLevel (number)` - Only if JSON enabled, numbers:
	+ `0`: Only the keywords that exist on both sides (in the specified message and in the message that was sent) will be inspected.  
	+ `1`: Only the keywords that exist on the left side (specified message) will be inspected. If a keyword exists on the left side, but not on the right side (sent message), the filter will not match.
	+ `2`: All keywords will be inspected, but keywords that exist on the right side but are `false` will be ignored.
	+ `3`: All keywords will beinspected without exception.
	
	If a keyword on the left side should be ignored, `<ignore>` can be provided as value.
- `regex (true/false)` - If enabled, the plain message or JSON values are inspected according to regex. A guide to regex is available at [RegExr](https://regexr.com/). Note: Punctuation characters like dots are interpreted differently when regex is enabled. You can prevent this with a `\` in front of it. More info on RegExr.
- `onlyHideForOtherPlayers (true/false)` - If enabled, messages you have sent yourself will not be filtered for you. (1.16+)
- `message (Text)` - The message to filter for. If JSON is enabled, in JSON format.
- `senders (Liste)` - If left blank it will be ignored. If at least one player is given, only messages sent by the given players will be filtered with this filter. (1.16+)  
	Either the player name, UUID or `CONSOLE` for the console may be provided.
- `excludedSenders (Liste)` - Just like `senders`, only the other way around. From anyone given, sent messages will not be filtered with this filter. (1.16+)  
	Either the player name, UUID or `CONSOLE` for the console may be provided.
- `receivers (Liste)` - If left blank it will be ignored. If at least one player is given, the message will be filtered only for the given players.  
		Either the player name, UUID or `CONSOLE` for the console may be provided.
- `excludedReceivers (Liste)` - Just like `receivers`, only the other way around. The message will not be filtered for anyone given.  
			Either the player name, UUID or `CONSOLE` for the console may be provided.
	
#### Beispiele

##### Voreinstellung: IdleTimeout (Version für 1.13+)

```yml
excludedReceivers: [
]
excludedSenders: [
]
json: true
jsonPrecisionLevel: 3
message: '{"italic": true, "color": "gray", "translate": "chat\\.type\\.admin", "with": [{"text": "Server"}, {"translate": "commands\\.setidletimeout\\.success", "with": ["\\d+"]}]}'
onlyHideForOtherPlayers: false
receivers: [
]
regex: true
senders: [
]
```

Explanation for json precision level: It is `3` because this message is exactly like that.  
Explanation for `\\.`: Since regex is enabled here, we need to escape the dot (Put a backslash in front of it). And since it is JSON, we need to escape the backslash again, so two backslashes.

##### Voreinstellung: Gamemode-Change

```yml
excludedReceivers: [
]
excludedSenders: [
]
json: true
jsonPrecisionLevel: 1
message: '{"italic": true, "color": "gray", "translate": "chat\\.type\\.admin", "with": [{},{"translate": "commands\\.gamemode\\.success\\.\\w+"}]}'
onlyHideForOtherPlayers: false
receivers: [
]
regex: true
senders: [
]
```

Explanation for json precision level: It is `1` because at `with` the first values of the array were given only as `{}`. Because it is `1`, what would actually be in there is ignored.

##### Voreinstellung: Only Self Commands

```yml
excludedReceivers: [
]
excludedSenders: [
]
json: true
jsonPrecisionLevel: 1
message: '{"italic": true, "color": "gray", "translate": "chat\\.type\\.admin", "with": [{},{"translate": "commands\\.(\\w|\\.)+"}]}'
onlyHideForOtherPlayers: false
receivers: [
]
regex: true
senders: [
]
```

##### Beispiel für Senders und Receivers

```yml
excludedReceivers:
  - Rapha149
  - Notch
excludedSenders: [robabla, Notch]
json: false
jsonPrecisionLevel: 2
message: 'Hallo :)'
onlyHideForOtherPlayers: false
receivers:
  - 073b1315-85ff-49d8-8041-51627214dae0
  - b3884719-7b05-47c7-82db-656bdcd99050
regex: false
senders: 
  - robabla
  - 073b1315-85ff-49d8-8041-51627214dae0
  - CONSOLE
```

Explanation:

- `excludedReceivers` - Here the Minecraft names of the players were given.
- `excludedSenders` - Here the players were written in square brackets instead of a YAML list with `-`. This can be done, but the plugin replaces it with the list with `-` when reloading the config.
- `receivers` - Here UUIDs were specified instead of player names.
- `senders` - Here it was shown that all three ways of specifying players can be used together.

Additional information:

- If you are really the player named 'CONSOLE', unfortunately you have to provide your UUID.
- I recommend providing the UUIDs, because the plugin has to retrieve the UUIDs from the internet when names are used. This can slow down the reloading of the config or make the plugin unusable if, for some reason, the plugin cannot access the internet or the website [MC-Heads](https://www.mc-heads.net/).
- This example is not logical because `excludedReceivers` and `receivers` (also `excludedSenders` and `senders`) were provided. It is intended for example purposes only.

## Schluss

I hope this tutorial was understandable, otherwise, as I said, feel free to contact me via an issue.  
Enjoy the plugin :)
