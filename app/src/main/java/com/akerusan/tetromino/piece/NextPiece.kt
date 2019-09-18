package com.akerusan.tetromino.piece

import java.util.ArrayList

class NextPiece : Piece {

    constructor(axe: Int, block: String){
        this.axe = axe
        this.block = block
    }

    constructor(nextPiece: ArrayList<Piece>, block: Int){

        var blockString = ""

        when (block) {
            1 -> {
                this.cube1 = 6
                this.cube2 = 7
                this.cube3 = 8
                this.cube4 = 12
                blockString = "ti"
            }
            2 -> {
                this.cube1 = 7
                this.cube2 = 8
                this.cube3 = 12
                this.cube4 = 13
                blockString = "square"
            }
            3 -> {
                this.cube1 = 6
                this.cube2 = 7
                this.cube3 = 8
                this.cube4 = 13
                blockString = "el"
            }
            4 -> {
                this.cube1 = 5
                this.cube2 = 6
                this.cube3 = 7
                this.cube4 = 8
                blockString = "line"
            }
            5 -> {
                this.cube1 = 6
                this.cube2 = 11
                this.cube3 = 12
                this.cube4 = 13
                blockString = "ji"
            }
            6 -> {
                this.cube1 = 6
                this.cube2 = 7
                this.cube3 = 12
                this.cube4 = 13
                blockString = "zi"
            }
            7 -> {
                this.cube1 = 7
                this.cube2 = 8
                this.cube3 = 11
                this.cube4 = 12
                blockString = "es"
            }
        }


        nextPiece[cube1] = NextPiece(cube1, blockString)
        nextPiece[cube2] = NextPiece(cube2, blockString)
        nextPiece[cube3] = NextPiece(cube3, blockString)
        nextPiece[cube4] = NextPiece(cube4, blockString)
    }

}