package xyz.poeschl.roborush.controller.restmodels

import xyz.poeschl.roborush.models.Direction

data class Move(val direction: Direction)
data class Scan(val distance: Int)
