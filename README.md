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

```/pr create <name> <size>``` Creates a protection with a name. The size is the radius in number of blocks from where you are standing

```/pr create <size>``` Creates a protection with your username.

```/pr create <name> s<size> n<size> w<size> e<size>``` You can also define the expansion in all directions by typing the command like this. (North/South/East/West)

```/pr create <name> ns<size> ew<size>``` You can define the expansion for east/west and north/south separately like this.

```/pr create <name> auto``` Using the "auto" keyword for an automatic size. This creates the largest possible protection size you can, given how much currency you have and any surrounding land you can't claim. This will try to maximise the expansion of all directions rather than prioritising one direction. 

```/pr create <name> nse<size> wauto``` The auto keyword can also be used for specific directions. In this example, you expand north, south and east a fixed amount, then you expand west as far as you are allowed.

### Updating protections

```/pr shift``` to shift the protection
```/pr expand``` to expand/shrink your protection
```/pr move``` to move the protection to where you are standing
```/pr remove``` to remove the protection

You will need to type ```/pr confirm``` to confirm your update.

If the protection's home is outside of the bounds of the protection after updating, it will update to where you are standing, or the centre of the protection if you aren't standing in your new boundaries.

#### Examples

```/pr shift <name> e<size>``` Moves the protection <size> blocks in an east direction.

```/pr shift <name> n<size> w<size>``` Moves the protection a number of blocks north and a number of blocks west.

```/pr shift <name> nw<size>``` Similiar to the previous command, but both north and west directions are equal.

```/pr expand <name> <size>``` Expand in all directions a given amount. Note that you can make the size negative to decrease the size of your protection.

```/pr expand <name> auto``` Expand in all directions as much as possible, given your currency and surrounding claims.

```/pr expand <name> ns<size> ewauto``` You can write the parameters similar to the creation. This expands a fixed amount north/south and automatically in other directions. If you make the expansion in one direction more than you can afford, the automatic expansion may go into negatives, and shrink your

```/pr move <name> ne``` Moves the north-eastern corner of your protection to where you are standing

```/pr move <name> n``` Moves the northern-centre of your protection to where you are standing

```/pr move <name>``` Moves the centre of your protection to where you are standing

```/pr move <name> relhome``` Moves the protection relative to where the original home is set. For example, if you stand 200 blocks east of your protections home and type this, your protection will move 200 blocks east. Note that this will only update the home of your protection if the home is outside of the bounds of the protection.
