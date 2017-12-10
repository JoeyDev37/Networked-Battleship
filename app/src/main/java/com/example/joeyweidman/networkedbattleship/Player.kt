package com.example.joeyweidman.networkedbattleship

/**
 * Created by Joey Weidman on 12/10/2017.
 */
class Player {
    var shipsRemaining: Int

    var destroyerHealth: Int
    var cruiserHealth: Int
    var submarineHealth: Int
    var battleshipHealth: Int
    var carrierHealth: Int

    init {
        shipsRemaining = 5

        destroyerHealth = 2
        cruiserHealth = 3
        submarineHealth = 3
        battleshipHealth = 4
        carrierHealth = 5
    }
}