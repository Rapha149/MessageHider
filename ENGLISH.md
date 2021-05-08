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

- In general, if you want to send me anything, please create an issue here. Please get in touch in the following cases:
	- If you want to translate this tutorial into another language, which would be very cool! 
		However, I don't think the plugin needs to be translated, since it has very few messages.
	- If you have any suggestions on how to improve this guide.
	- If you experience errors, have questions or general suggestions for improvement.
- This plugin collects anonymous server stats with [bStats](https://bstats.org), an open-source statistics service for Minecraft software. If you want you can deactivate this in `plugins/bStats/config.yml`.

Now a few more infos regarding the plugin:

- With this plugin you can hide certain messages from players. There are [Presets](#presets), but you can also create custom filters. I recommend reading this tutorial previously so that you don't accidentally deactivate all messages.
- You cannot hide messages from the console.
- If you receive a multiline message, unfortunately you can't hide them together, you have to create multiple filters. This is the case, for example, if you enter a Vanilla command incorrectly.

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

### Presets

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

#### Idle timeout

If this preset is enabled, you will no longer see messages like  
*\[Server: The player idle timeout is now X minutes\]*  
If you change the idle timeout by yourself you will receive the feedback message, but not if the server (the console) or another player changes it. This is extremely useful if you play on a hosting provider like [PloudOS](https://ploudos.com) and don't always want to get this message there.

#### Gamemode change

If this preset is enabled, you will no longer receive the messages when other players change their game mode.

#### Only self commands

If this preset is enabled, you only receive feedback messages from your own commands. Unfortunately, this only works in 1.16+, because the method for this does not yet exist in Spigot. 

**For plugin programmers**  
I do this via the `PacketPlayOutChat` packet. Only starting from the 1.16 there is the field `c` of the type `UUID`. If someone knows how to do this even in deeper versions, feel free to write me!

### Custom filters

If the preset options are not enough, you can create your own filters. The best way to do this is to start with `/messagehider create`, which will create an empty filter.

There are the following settings: (They are sorted differently here than in the config, because it is alphabetical in the config)

- `json (true/false)` - If enabled, the specified filter message is interpreted as JSON. If not, it will be interpreted as a plain message, which can also be a language message from Minecraft. (e.g.: `commands.setidletimeout.success`)
- `jsonPrecisionLevel (numbers)` - Only if JSON enabled, numbers:
	+ `0`: Only the keywords that exist on both sides (in the specified message and in the message that was sent) will be inspected.  
	+ `1`: Only the keywords that exist on the left side (specified message) will be inspected. If a keyword exists on the left side, but not on the right side (sent message), the filter will not match.
	+ `2`: All keywords will be inspected, but keywords that exist on the right side but are `false` will be ignored.
	+ `3`: All keywords will beinspected without exception.
	
	If a key on the left side should be ignored, `<ignore>` can be specified as value.
- `regex (true/false)` - If enabled, the plain message or JSON values are inspected according to regex. A guide to regex is available at [RegExr](https://regexr.com/). Note: Punctuation characters like dots are interpreted differently when regex is enabled. You can prevent this with a `\` in front of it. More info on RegExr.
- `onlyHideForOtherPlayers (true/false)` - If enabled, messages you have sent yourself will not be filtered for you. (1.16+)
- `message (text)` - The message to filter for. If JSON is enabled, in JSON format.
- `senders (list)` - If left empty it will be ignored. If at least one player is given, only messages sent by the given players will be filtered with this filter. (1.16+)  
	Either the player name, UUID or `<console>` for the console may be provided.
- `excludedSenders (list)` - Just like `senders`, only the other way around. From anyone given, sent messages will not be filtered with this filter. (1.16+)  
	Either the player name, UUID or `<console>` for the console may be provided.
- `receivers (list)` - If left empty it will be ignored. If at least one player is given, the message will be filtered only for the given players.  
   Either the player name, UUID or `<console>` for the console may be provided.
- `excludedReceivers (list)` - Just like `receivers`, only the other way around. The message will not be filtered for anyone given.  
   Either the player name, UUID or `<console>` for the console may be provided.
	
#### Examples

##### Preset: idle timeout (Version für 1.13+)

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

Explanation for JsonPrecisionLevel: It is `3` because this message is exactly like that.  
Explanation for `\\.`: Since regex is enabled here, we need to escape the dot (Put a backslash in front of it). And since it is JSON, we need to escape the backslash again, so two backslashes.

##### Preset: gamemode change

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

##### Preset: only self commands

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

##### Example for senders and receivers

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
- `senders` - Here it was shown that all three ways of specifying players can also be used together.

Additional information:

- I recommend providing the UUIDs, because the plugin has to retrieve the UUIDs from the internet when names are used. This can slow down the reloading of the config or make the plugin unusable if, for some reason, the plugin cannot access the internet or the website [MC-Heads](https://www.mc-heads.net/).
- If the server's online mode is set to `false` the plugin will not validate UUIDs. If UUIDs are provided it will compare these, but if names are provided it will compare names with names.
- This example is not logical because `excludedReceivers` and `receivers` (also `excludedSenders` and `senders`) were provided. It is intended for example purposes only.

## Final words

I hope this tutorial was understandable, otherwise, as I said, feel free to contact me via an issue.  
Enjoy the plugin :)
