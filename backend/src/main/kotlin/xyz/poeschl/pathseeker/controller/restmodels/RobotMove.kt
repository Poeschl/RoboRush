package xyz.poeschl.pathseeker.controller.restmodels

import xyz.poeschl.pathseeker.models.Direction

data class RobotMove(val robotId: Long, val direction: Direction)
