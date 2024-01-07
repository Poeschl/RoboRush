package xyz.poeschl.pathseeker.controller.restmodels

import xyz.poeschl.pathseeker.models.Direction

data class Move(val direction: Direction)
data class Scan(val distance: Int)
