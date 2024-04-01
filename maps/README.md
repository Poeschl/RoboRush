# PathSeeker Maps

This folder contains the default maps which are available on the first start.
They are generated with a height map generator called [wgen](https://github.com/jice-nospam/wgen) and modified with Photoshop to include tile data (described below).

## Generate my own map

PathSeeker recognizes every grayscale image as a height map and calculates the heights from it.
Make sure the height values are all grey, because we have some special methods to also get the start area and target tile.
For all `grey` values 0 is the deepest possible height and 255 the peak height.
As `grey` all rgb color values of a pixel are expected to have the same value.

To declare the __start area__ the `green` value of a pixel needs to be higher as `red` and `blue`.
Both, `red` and `blue`, still specify the height of the tile.

To set the __single target position__ the `red` value of the pixel needs to be higher as `green` and `blue`.
The height is specified by the two equal color values.

After creating your heightmap it can be uploaded on the config page, which can be accessed as admin user.
