package com.example.joeyweidman.networkedbattleship

/**
 * Created by Joey Weidman
 */
enum class Ship(val size: Int) {
    DESTROYER(2),
    SUBMARINE(3),
    CRUISER(3),
    BATTLESHIP(4),
    CARRIER(5),
    NONE(0)
}