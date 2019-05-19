package com.example.tetris.piece

import java.util.ArrayList

class PieceS: Piece {

    override var block: String = "es"

    constructor(axe: Int){
        this.axe = axe
    }

    constructor(pieceList: ArrayList<Piece>, axe: Int){

        this.axe = axe

        this.cube1 = axe + 9
        this.cube2 = axe + 10
        this.cube3 = axe + 1

        pieceList[axe] = PieceS(axe)
        pieceList[axe + 9] = PieceS(cube1)
        pieceList[axe + 10] = PieceS(cube2)
        pieceList[axe + 1] = PieceS(cube3)
    }

    override fun rotation(pieceList: ArrayList<Piece>){

        this.cube1 = axe + 9
        this.cube2 = axe + 10
        this.cube3 = axe + 1
        this.cube4 = axe - 1
        this.cube5 = axe - 11

        if (this.rotation == 0 || this.rotation == 2){
            pieceList[cube4] = Piece()
            pieceList[cube5] = Piece()
            pieceList[cube1] = PieceS(cube1)
            pieceList[cube3] = PieceS(cube3)
        }
        if (this.rotation == 1 || this.rotation == 3){
            pieceList[cube1] = Piece()
            pieceList[cube3] = Piece()
            pieceList[cube4] = PieceS(cube4)
            pieceList[cube5] = PieceS(cube5)
        }
    }

    override fun checkRight(pieceList: ArrayList<Piece>) : Boolean {

        val rotation = this.rotation

        if (rotation == 0 || rotation == 2){

            // detect wall
            for (x in 9..199 step 10){

                if (this.cube3 == x){
                    return false
                }
            }
            // detect block
            if (pieceList[this.cube2 + 1].block == "" && pieceList[this.cube3 + 1].block == "")
            {
                return true
            }

        } else if (rotation == 1 || rotation == 3) {

            // detect wall
            for (x in 9..199 step 10){

                if (this.axe == x){
                    return false
                }
            }
            // detect block
            if (pieceList[this.axe + 1].block == "" && pieceList[this.cube2 + 1].block == "" && pieceList[this.cube5 + 1].block == "")
            {
                return true
            }

        }
        return false
    }

    override fun checkLeft(pieceList: ArrayList<Piece>) : Boolean {

        val rotation = this.rotation

        if (rotation == 0 || rotation == 2){

            // detect wall
            for (x in 0..190 step 10){

                if (this.cube1 == x){
                    return false
                }
            }
            // detect block
            if (pieceList[this.cube1 - 1].block == "" && pieceList[this.axe - 1].block == "")
            {
                return true
            }

        } else if (rotation == 1 || rotation == 3) {

            // detect wall
            for (x in 0..190 step 10){

                if (this.cube4 == x){
                    return false
                }
            }
            // detect block
            if (pieceList[this.cube2 - 1].block == "" && pieceList[this.cube4 - 1].block == "" && pieceList[this.cube5 - 1].block == "")
            {
                return true
            }
        }
        return false
    }

    override fun checkDown(pieceList: ArrayList<Piece>) : Boolean {

        val rotation = this.rotation

        if (rotation == 0 || rotation == 2){

            if (pieceList[this.cube1 + 10].block == "" && pieceList[this.cube2 + 10].block == "" && pieceList[this.cube3 + 10].block == "")
            {
                return true
            }

        } else if (rotation == 1 || rotation == 3) {

            if (pieceList[this.cube4 + 10].block == "" && pieceList[this.cube2 + 10].block == "")
            {
                return true
            }

        }
        return false
    }

    override fun moveRight(pieceList: ArrayList<Piece>){

        this.axe += 1
        pieceList[this.axe] = PieceS(this.axe)

        if (this.cube1 != 0) {
            this.cube1 += 1
            pieceList[cube1] = PieceS(cube1)
        }
        if (this.cube2 != 0) {
            this.cube2 += 1
            pieceList[cube2] = PieceS(cube2)
        }
        if (this.cube3 != 0) {
            this.cube3 += 1
            pieceList[cube3] = PieceS(cube3)
        }
        if (this.cube4 != 0) {
            this.cube4 += 1
            pieceList[cube4] = PieceS(cube4)
        }
        if (this.cube5 != 0) {
            this.cube5 += 1
            pieceList[cube5] = PieceS(cube5)
        }
    }

    override fun moveLeft(pieceList: ArrayList<Piece>){

        this.axe -= 1
        pieceList[this.axe] = PieceS(this.axe)

        if (this.cube1 != 0) {
            this.cube1 -= 1
            pieceList[cube1] = PieceS(cube1)
        }
        if (this.cube2 != 0) {
            this.cube2 -= 1
            pieceList[cube2] = PieceS(cube2)
        }
        if (this.cube3 != 0) {
            this.cube3 -= 1
            pieceList[cube3] = PieceS(cube3)
        }
        if (this.cube4 != 0) {
            this.cube4 -= 1
            pieceList[cube4] = PieceS(cube4)
        }
        if (this.cube5 != 0) {
            this.cube5 -= 1
            pieceList[cube5] = PieceS(cube5)
        }
    }

    override fun moveDown(pieceList: ArrayList<Piece>){

        this.axe += 10
        pieceList[this.axe] = PieceS(this.axe)

        if (this.cube1 != 0) {
            this.cube1 += 10
            pieceList[cube1] = PieceS(cube1)
        }
        if (this.cube2 != 0) {
            this.cube2 += 10
            pieceList[cube2] = PieceS(cube2)
        }
        if (this.cube3 != 0) {
            this.cube3 += 10
            pieceList[cube3] = PieceS(cube3)
        }
        if (this.cube4 != 0) {
            this.cube4 += 10
            pieceList[cube4] = PieceS(cube4)
        }
        if (this.cube5 != 0) {
            this.cube5 += 10
            pieceList[cube5] = PieceS(cube5)
        }
    }


}