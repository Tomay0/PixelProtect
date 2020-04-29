# PixelProtect
Grief Protection plugin designed for the Pixel Origins server

## Installation (Wait for release)

Add the jar to your plugins/ folder. This plugin works with spigot 1.15.2.

## How to use

All commands are accessible by typing /pr or /protect.

### Creating protections

```/pr create <name> <size>``` Creates a protection with a name. The size is the radius in number of blocks from where you are standing

```/pr create <size>``` Creates a protection. The name is just your username.

```/pr create <name> s<size> n<size> w<size> e<size>``` You can also define the expansion in all directions by typing the command like this. (North/South/East/West)

```/pr create <name> ns<size> ew<size>``` You can define the expansion for east/west and north/south separately like this.

```/pr create <name> auto``` Using the "auto" keyword for an automatic size. This creates the largest possible protection size you can, given how much currency you have and any surrounding land you can't claim. This will try to maximise the expansion of all directions.

```/pr create <name> nse<size> wauto``` The auto keyword can also be used for specific directions. In this example, you expand north, south and east a fixed amount, then you expand west as far as you are allowed.
