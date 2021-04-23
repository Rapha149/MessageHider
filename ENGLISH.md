# English tutorial

## Overview

1. [General](#general)
2. [Commands](#commands)
3. [Config](#config)
	- [Presets](#presets)
		+ [Idle timeout](#idle-timeout)
		+ [Gamemode change](#gamemode-change)
		+ [Only self commands](#only-self-commands)
	- [Custom filters](#custom-filters)
		+ [Examples](#examples)
	
## Allgemein

First of all: Thank you for using this plugin and reading this tutorial!
A few information in advance:

1. In general, if you want to send me anything, please create an issue here. Please get in touch in the following cases:
	- If you want to translate this tutorial into another language, which would be very cool! 
		However, I don't think the plugin needs to be translated, since it has very few messages.
	- If you have any suggestions on how to improve this guide.
	- If you experience errors, have questions or general suggestions for improvement.
2. This plugin collects anonymous server stats with [bStats](https://bstats.org), an open-source statistics service for Minecraft software. If you want you can deactivate this in `plugins/bStats/config.yml`.

## Commands

This plugin has only one command: `/messagehider` (Alias: `/mh`)

### Sub commands

- `/messagehider log <start|stop>` - Starts or stops logging of messages you receive. This is useful if you want to create new filters but don't know exactly what the message (in JSON) looks like.  
	The log is saved at `plugins/MessageHider/logs/PLAYERNAME.log` and will be deleted if you start logging again. Of course you can also delete the log manually.  
	Filtered messages will be logged, too.
- `/messagehider create` - Creates an empty new filter. This is useful so that you do not forget to configure something.
- `/messagehider reload` - Reloads the config.

### Permissions

- `/messagehider log` - messagehider.log
- `/messagehider create` - messagehider.create
- `/messagehider reload` - messagehider.reload

## Config

The config is located at `plugins/MessageHider/config.yml`.

### Voreinstellungen

When the config is generated for the first time, it looks like this:

```yml
messageFilters: [  
]  
presets:  
  gamemodeChange: false  
  idleTimeout: false  
  onlySelfCommands: false
```

If you only need the presets, you don't need to worry about `messageFilters`.  
To enable the presets, you can simply change the `false` to a `true` for the respective preset.

#### IdleTimeout

If this preset is enabled, you will no longer see messages like  
*\[Server: The player idle timeout is now X minutes\]*  
If you change the idle timeout by yourself you will receive the feedback message, but not if the server (the console) or another player changes it. This is extremely useful if you play on a hosting provider like [PloudOS](https://ploudos.com) and don't always want to get this message there.

#### Gamemode-Change

If this preset is enabled, you will no longer receive the messages when other players change their game mode.

#### Only Self Commands

If this preset is enabled, you only receive feedback messages from your own commands. Unfortunately, this only works in 1.16+, because the method for this does not yet exist in Spigot. 

**For plugin programmers**  
I do this via the `PacketPlayOutChat` packet. Only starting from the 1.16 there is the field `c` of the type `UUID`. If someone knows how to do this even in deeper versions, feel free to write me!

### Eigene Filter

If the preset options are not enough, you can create your own filters. The best way to do this is to start with `/messagehider create`, which will create an empty filter.

There are the following settings: (They are sorted differently here than in the config, because it is alphabetical in the config)

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
- `senders (Liste)` - Wenn leer gelassen wird es ignoriert. Wenn mindestens ein Spieler angegeben ist, werden nur Nachrichten, die von den angegebenen Spielern gesendet wurden, mit diesem Filter gefiltert. (1.16+)  
	Es kann entweder der Spielername, die UUID oder `CONSOLE` für die Konsole angegeben werden.
- `excludedSenders (Liste)` - So wie `senders`, nur andersrum. Von jedem, der hier drin steht, werden gesendete Nachrichten mit diesem Filter nicht gefiltert. (1.16+)  
	Es kann entweder der Spielername, die UUID oder `CONSOLE` für die Konsole angegeben werden.
- `receivers (Liste)` - Wenn leer gelassen wird es ignoriert. Wenn mindestens ein Spieler angegeben ist, wird eine Nachricht nur für die angegebenen Spieler gefiltert.  
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

Weitere Infos:

- Wenn du wirklich der Spieler namens `CONSOLE` bist, musst du leider deine UUID angeben.
- Ich empfehle die UUIDs anzugeben, da das Plugin bei Namen die UUIDs aus dem Internet abrufen muss. Das kann das Neuladen der Config verlangsamen oder das Plugin unbrauchbar machen, wenn das Plugin aus irgendeinem Grund nicht auf das Internet oder die Website [MC-Heads](https://www.mc-heads.net/) zugreifen kann.
- Dieses Beispiel ist nicht logisch, da `excludedReceivers` und `receivers` (auch `excludedSenders` und `senders`) angegeben wurde. Es ist nur zu Beispielzwecken gedacht.

## Schluss

Ich hoffe diese Anleitung war verständlich, sonst melde dich, wie gesagt, gerne via Issue.  
Viel Spaß mit dem Plugin :)
