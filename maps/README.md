# RoboRush Maps

This folder contains the default maps which are available on the first start.
They are generated with a height map generator called [wgen](https://github.com/jice-nospam/wgen) and modified with Photoshop (or any other picture editor) to place spawn area pixels and the single target position pixel (described below).

## Generate your own map

RoboRush recognizes grayscale images as height maps.
Each pixel will be interpreted as a distinct tile.
Make sure the color values are equal for each pixel - notable exceptions are described below.
`Gray` values may range from 0 (for deep valleys) to 255 (for mountain peaks).
As mentioned above, `gray` pixels are expected to share the same value for each color channel.

To declare the __spawn area__ the `green` value of a pixel needs to be higher as `red` and `blue`.
Both, `red` and `blue`, still specify the height of the pixel.
The amount of spawn pixels determines also the max player count for a map.

To set the __single target position__ the `red` value of the pixel needs to be higher as `green` and `blue`.
The height is specified by the remaining colors.

For tile which can be used to __refuel__ the robot the `blue` value of the pixel needs to be higher then `red` and `green`.
The height is specified by the remaining colors.

> [!WARNING]
> Neither __spawn area__ nor the __single target position__ can be placed at height 255!

After creating your heightmap it can be uploaded on the config page, which can be accessed as admin user.

## Modify a map

A simple map editor can be found in the admin section of RoboRush.
It can change the tile types but not any heights.

## Export / Import

Existing maps can be exported via the map editor as well.
During that the map-specific settings are also saved in the image (via XMP metadata).

When an exported image is imported on the same or different RoboRush instance, the settings will be imported as well.
