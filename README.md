# PixelProtect
Grief Protection plugin designed for the Pixel Origins server

## Installation (Wait for release)

Add the jar to your plugins/ folder. This plugin works with spigot 1.15.2.

## How to use

All commands are accessible by typing /pr or /protect.

The first parameter specifies the command.

The second parameter specifies the name of the protection (usually). If you leave this out, it uses your "default" protection name, which is usually your username.

### Creating protections

```/pr create``` Is the command for creating protections. You will need to type ```/pr confirm``` to confirm your purchase.

#### Examples

```/pr create <name> <size>``` Creates a protection with a name. The size is the radius in number of blocks from where you are standing.

```/pr create <size>``` Creates a protection with your username.

```/pr create <name> s<size> n<size> w<size> e<size>``` You can also define how far the protection extends in all directions by typing the command like this. (North/South/East/West)

```/pr create <name> ns<size> ew<size>``` You can define how far the protection extends for east/west and north/south separately like this.

```/pr create <name> auto``` Using the "auto" keyword for an automatic size. This creates the largest possible protection size you can, given how much currency you have and any surrounding land you can't claim. This will try to maximise the expansion of all directions rather than prioritising one direction. 

```/pr create <name> nse<size> wauto``` The auto keyword can also be used for specific directions. In this example, you expand north, south and east a fixed amount, then you expand west as far as you are allowed.

### Updating protections

* ```/pr shift``` to shift the protection.
* ```/pr expand``` to expand/shrink your protection.
* ```/pr move``` to move the protection to where you are standing.
* ```/pr rename``` to rename the protection.
* ```/pr remove``` to remove the protection.

You will need to type ```/pr confirm``` to confirm your update.

If the protection's home is outside of the bounds of the protection after updating, it will update to where you are standing, or the centre of the protection if you aren't standing in your new boundaries. This will be notified to you before you type ```/pr confirm```

#### Examples

```/pr shift <name> e<size>``` Moves the protection <size> blocks in an east direction.

```/pr shift <name> n<size> w<size>``` Moves the protection a number of blocks north and a number of blocks west.

```/pr shift <name> nw<size>``` Similiar to the previous command, but both north and west directions are equal.

```/pr expand <name> <size>``` Expand in all directions a given amount. Note that you can make the size negative to decrease the size of your protection.

```/pr expand <name> auto``` Expand in all directions as much as possible, given your currency and surrounding claims.

```/pr expand <name> ns<size> ewauto``` You can write the parameters similar to the creation. This expands a fixed amount north/south and automatically in other directions. If you make the expansion in one direction more than you can afford, the automatic expansion may go into negatives, and shrink your protection.

```/pr move <name> ne``` Moves the north-eastern corner of your protection to where you are standing.

```/pr move <name> n``` Moves the northern-centre of your protection to where you are standing.

```/pr move <name>``` Moves the centre of your protection to where you are standing.

```/pr move <name> relhome``` Moves the protection relative to where the original home is set. For example, if you stand 200 blocks east of your protections home and type this, your protection will move 200 blocks east. Note that this will only update the home of your protection if the home is outside of the bounds of the protection.

### Protection home

A protection may have multiple homes, but the default is called "home". All homes must be set within the boundaries of the protection. If the protection is updated and the home becomes outside, the home(s) will be moved.

It can be configured that multiple homes cost extra currency, or a limit can be set.

* ```/pr home``` Teleport to a home
* ```/pr sethome``` Set a protection's home

#### Examples

```/pr sethome <name>``` Set the default home for a protection.

```/pr home <name>``` Teleport to the default home for a protection.

```/pr sethome <name> <home name>``` Set a home for a protection. (non-default)

```/pr home <name> <home name>``` Teleport to a home for a protection. (non-default)

### Permissions

Players can have one of three permission levels. Member, admin or owner. You can set players to these permission levels using the commands below:

* ```/pr setperm <name> <username> member``` Sets a player as member.
* ```/pr setperm <name> <username> admin``` Sets a player as admin.
* ```/pr setperm <name> <username> owner``` Sets a player as owner. Only 1 owner per protection. If the owner types this, the owner status is revoked from the original owner.
* ```/pr setperm <name> <username> none``` Removes member status.

You can also set specific permissions of a player using this command:

```/pr setperm <name> <username> <perm> true/false```

You can also update the specific permissions of a permission level using this command:

```/pr setperm <name> <permission level> <perm> true/false```

Note that you can only update the permissions of players and levels that are lower or equal to your current permission level.

```/pr perms``` lets you list all player permissions.

#### Specific permissions

* *home* ability to teleport to the home(s) of the protection: Default: member.
* *build* ability to build in the protection (both breaking and placing): Default: member.
* *interact* ability to interact with buttons/repeaters/pressure plates: Default: member.
* *chest* ability to interact with chests: Default: member.
* *update* ability to use /pr move, /pr shift, /pr expand, /pr rename: Default: admin.
* *remove* ability to use /pr remove: Default: owner.
* *sethome* ability to set homes: Default: admin.
* *setperms* ability to set other players permissions: Default: admin.
* *config* ability to configure the protection: Default: admin.

#### Examples

```/pr setperm Protection1 User123 member``` Set a player as a member. The most common use of this command.

```/pr setperm Protection1 User123 interact true``` Imagine User123 is not a member of the protection, you can let them open doors/press buttons using this command.

```/pr setperm Protection1 none interact true``` Players with no member status can interact with this protection.

```/pr setperm Protection1 User456 update false``` Imagine User456 is an admin, you can revoke the ability to update the size of the protection.

### Config

```/pr config <name> <configuration> <value>``` let's you configure different settings about the protection that aren't related to permissions.

#### All configurations

* *creeper_damage* lets creepers explode, default: false
* *tnt_damage* lets tnts explode, default: false
* *piston_damage* lets pistons push blocks into the protection, default: false
* *colour* colour on the dynmap, default: #ff0000
* TODO: add more here

### Miscellaneous commands

```/pr list``` List all protections you have access to (and their respective perms)
