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
	- [Eigene Filter](#eigene-filter)
		+ [Beispiele](#beispiele)
	- [Weitere Infos](#weitere-infos)
	
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
Wenn die Config das erste mal generiert wird, sieht sie so aus:

```yml
checkForUpdates: true
messageFilters: [
  ]
prefix: '&8[&cMH&8] '
presets:
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

Wenn diese Voreinstellung aktiviert ist, bekommt man nur noch Nachrichten von eigenen Commands. Dies klappt leider nur in 1.16+, weil es dadrunter die Methode dafür noch nicht bei Spigot gibt. 

**Für Plugin-Programmierer**  
Ich mache das über das `PacketPlayOutChat` packet. Nur ab der 1.16 gibt es dort das Feld `c` vom Typ `UUID`. Wenn jemand weiß, wie das auch schon in tieferen Versionen geht, schreibt mich gerne an!

### Eigene Filter

Wenn einem die Voreinstellungs-Möglichkeiten nicht reichen, kann man sich selber Filter erstellen. Am besten startet man dafür mit `/messagehider create`, was einem einen leeren Filter erstellt.

Es gibt folgende Einstellungen: (Sie sind hier anders sortiert als in der Config, weil es in der Config alphabetisch ist)

- `json (true/false)` - Wenn aktiviert, wird die angegebene Filter-Nachricht als JSON interpretiert. Wenn nicht, wird sie als Plain-Message interpretiert, das kann auch eine Lang-Nachricht von Mojang sein. (z.B: `commands.setidletimeout.success`)
- `jsonPrecisionLevel (Nummer)` - Nur wenn JSON aktiviert, Nummern:
	+ `0`: Es werden nur die Schlüsselwörter überprüft, die auf beiden Seiten (in der angegebenen Nachricht und in der Nachricht, die gesendet wurde) existieren.
	+ `1`: Es werden nur die Schlüsselwörter überprüft, die auf der linken Seite (angegebene Nachricht) existieren. Wenn ein Schlüsselwort, was auf der linken Seite existiert, aber nicht auf der rechten Seite (gesendete Nachricht), schlägt der Filter nicht an.
	+ `2`: Es werden alle Schlüsselwörter überprüft, allerdings werden Schlüsselwörter, die auf der rechten Seite existieren, aber `false` sind, ignoriert.
	+ `3`: Alle Schlüsselwörter werden ausnahmslos überprüft.

  Wenn ein Schlüsselwort auf der linken Seite ignoriert werden soll, kann als Value `<ignore>` angegeben werden.
- `regex (true/false)` - Wenn aktiviert, wird die Plain-Message oder die JSON-Values nach Regex überprüft. Eine Guide zu Regex gibt es auf [RegExr](https://regexr.com/). Achtung: Satzzeichen wie Punkte werden, wenn Regex aktiviert ist, anderes interpretiert. Dies kann man mit einem `\` davor verhindern. Mehr Infos auf RegExr.
- `onlyHideForOtherPlayers (true/false)` - Wenn aktiviert, werden Nachrichten, die man selber gesendet hat, nicht für einen selber gefiltert. (1.16+)
- `message (Text)` - Die Nachricht, nach der gefiltert werden soll. Wenn JSON aktiviert, im JSON-Format.
- `senders (Liste)` - Wenn die Liste leer gelassen wird es ignoriert. Wenn mindestens ein Spieler angegeben ist, werden nur Nachrichten, die von den angegebenen Spielern gesendet wurden, mit diesem Filter gefiltert. (1.16+)  
  Es kann entweder der Spielername, die UUID oder `CONSOLE` für die Konsole angegeben werden.
- `excludedSenders (Liste)` - So wie `senders`, nur andersrum. Von jedem, der hier drin steht, werden gesendete Nachrichten mit diesem Filter nicht gefiltert. (1.16+)  
  Es kann entweder der Spielername, die UUID oder `CONSOLE` für die Konsole angegeben werden.
- `receivers (Liste)` - Wenn die Liste leer gelassen wird es ignoriert. Wenn mindestens ein Spieler angegeben ist, wird eine Nachricht nur für die angegebenen Spieler gefiltert.  
  Es kann entweder der Spielername, die UUID oder `CONSOLE` für die Konsole angegeben werden.
- `excludedReceivers (Liste)` - So wie `receivers`, nur andersrum. Die Nachricht wird für jeden, der hier drin steht, nicht gefiltert.  
  Es kann entweder der Spielername, die UUID oder `CONSOLE` für die Konsole angegeben werden.
	
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

Erklärung für JsonPrecisionLevel: Es ist `3`, weil diese Nachricht exakt so ist.  
Erklärung für `\\.`: Da hier Regex aktiviert ist, müssen wir den Punkt escapen (Ein Backslash davor tun). Und da es JSON ist, müssen wir das Backslash erneut escapen, also zwei Backslashes.

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

Erklärung für JsonPrecisionLevel: Es ist `1`, weil bei `with` die ersten Values des Arrays nur als `{}` angegeben wurden. Weil es `1` ist, wird das, was da eigentlich drin stände, ignoriert.

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

Erklärung:

- `excludedReceivers` - Hier wurden die Minecraft-Namen der Spieler angegeben.
- `excludedSenders` - Hier wurden die Spieler in eckige Klammern anstatt in eine YAML-Liste mit `-` geschrieben. Das kann man machen, allerdings ersetzt es das Plugin beim Neuladen der Config mit der Liste mit `-`.
- `receivers` - Hier wurden UUIDs anstelle der Spielernamen angegeben.
- `senders` - Hier wurde nochmal gezeigt, dass auch alle drei Möglichkeiten, Spieler anzugeben, gemeinsam genutzt werden können.

#### Weitere Infos

- Ich empfehle die UUIDs anzugeben, da das Plugin bei Namen die UUIDs aus dem Internet abrufen muss. Das kann das Neuladen der Config verlangsamen oder das Plugin unbrauchbar machen, wenn das Plugin aus irgendeinem Grund nicht auf das Internet oder die Website [MC-Heads](https://www.mc-heads.net/) zugreifen kann.
- Wenn der Onlinemode des Servers auf `false` ist, vergleicht das Plugin die Namen nicht mit UUIDs. Wenn bei UUIDs angegeben wurden, vergleicht es diese, wenn aber Namen angegeben wurden, werden Namen mit Namen verglichen.
- Dieses Beispiel ist nicht logisch, da `excludedReceivers` und `receivers` (auch `excludedSenders` und `senders`) angegeben wurde. Es ist nur zu Beispielzwecken gedacht.

## Schluss

Ich hoffe diese Anleitung war verständlich, sonst melde dich, wie gesagt, gerne via Issue.  
Viel Spaß mit dem Plugin :)
