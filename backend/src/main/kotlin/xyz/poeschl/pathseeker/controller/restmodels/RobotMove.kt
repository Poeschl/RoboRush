package xyz.poeschl.pathseeker.controller.restmodels

import xyz.poeschl.pathseeker.models.Direction

data class RobotMove(val robotIndex: Int, val direction: Direction)
