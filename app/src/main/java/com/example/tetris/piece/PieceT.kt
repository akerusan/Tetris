package com.example.tetris.piece

import java.util.ArrayList

class PieceT : Piece {

    override var block: String = "ti"

    constructor(axe: Int){
        this.axe = axe
    }

    constructor(pieceList: ArrayList<Piece>, axe: Int){

        this.axe = axe

        this.cube1 = axe + 1
        this.cube2 = axe - 1
        this.cube4 = axe + 10
        
        pieceList[axe] = PieceT(axe)
        pieceList[axe+1] = PieceT(cube1)
        pieceList[axe-1] = PieceT(cube2)
        pieceList[axe+10] = PieceT(cube4)
    }

    override fun rotation(pieceList: ArrayList<Piece>){

        this.cube1 = axe + 1
        this.cube2 = axe - 1
        this.cube3 = axe - 10
        this.cube4 = axe + 10

        if (this.rotation == 0){
            pieceList[cube3] = Piece()
            pieceList[cube2] = PieceT(cube2)
        }
        if (this.rotation == 1){
            pieceList[cube1] = Piece()
            pieceList[cube3] = PieceT(cube3)
        }
        if (this.rotation == 2){
            pieceList[cube4] = Piece()
            pieceList[cube1] = PieceT(cube1)
        }
        if (this.rotation == 3){
            pieceList[cube2] = Piece()
            pieceList[cube4] = PieceT(cube4)
        }
    }

    override fun checkRight(pieceList: ArrayList<Piece>) : Boolean {

        val rotation = this.rotation

        if (rotation == 0){

            // detect wall
            for (x in 9..199 step 10){

                if (this.cube1 == x){
                    return false
                }
            }
            // detect block
            if (pieceList[this.cube1 + 1].block == "" && pieceList[this.cube4 + 1].block == "")
            {
                return true
            }

        } else if (rotation == 1) {

            // detect wall
            for (x in 9..199 step 10){

                if (this.axe == x){
                    return false
                }
            }
            // detect block
            if (pieceList[this.axe + 1].block == "" && pieceList[this.cube3 + 1].block == "" && pieceList[this.cube4 + 1].block == "")
            {
                return true
            }

        } else if (rotation == 2) {

            // detect wall
            for (x in 9..199 step 10){

                if (this.cube1 == x){
                    return false
                }
            }
            // detect block
            if (pieceList[this.cube1 + 1].block == "" && pieceList[this.cube3 + 1].block == "")
            {
                return true
            }

        } else if (rotation == 3) {

            // detect wall
            for (x in 9..199 step 10){

                if (this.cube1 == x){
                    return false
                }
            }
            // detect block
            if (pieceList[this.cube1 + 1].block == "" && pieceList[this.cube3 + 1].block == "" && pieceList[this.cube4 + 1].block == "")
            {
                return true
            }
        }

        return false
    }


    override fun checkLeft(pieceList: ArrayList<Piece>) : Boolean {

        val rotation = this.rotation

        if (rotation == 0){

            // detect wall
            for (x in 0..190 step 10){

                if (this.cube2 == x){
                    return false
                }
            }
            // detect block
            if (pieceList[this.cube2 - 1].block == "" && pieceList[this.cube4 - 1].block == "")
            {
                return true
            }

        } else if (rotation == 1) {

            // detect wall
            for (x in 0..190 step 10){

                if (this.cube2 == x){
                    return false
                }
            }
            // detect block
            if (pieceList[this.cube2 - 1].block == "" && pieceList[this.cube3 - 1].block == "" && pieceList[this.cube4 - 1].block == "")
            {
                return true
            }

        } else if (rotation == 2) {

            // detect wall
            for (x in 0..190 step 10){

                if (this.cube2 == x){
                    return false
                }
            }
            // detect block
            if (pieceList[this.cube2 - 1].block == "" && pieceList[this.cube3 - 1].block == "")
            {
                return true
            }

        } else if (rotation == 3) {

            // detect wall
            for (x in 0..190 step 10){

                if (this.axe == x){
                    return false
                }
            }
            // detect block
            if (pieceList[this.axe - 1].block == "" && pieceList[this.cube3 - 1].block == "" && pieceList[this.cube4 - 1].block == "")
            {
                return true
            }
        }

        return false
    }

    override fun checkDown(pieceList: ArrayList<Piece>) : Boolean {

        val rotation = this.rotation

        if (rotation == 0){

            if (pieceList[this.cube1 + 10].block == "" && pieceList[this.cube2 + 10].block == "" && pieceList[this.cube4 + 10].block == "")
            {
                return true
            }

        } else if (rotation == 1) {

            if (pieceList[this.cube2 + 10].block == "" && pieceList[this.cube4 + 10].block == "")
            {
                return true
            }

        } else if (rotation == 2) {

            if (pieceList[this.axe + 10].block == "" && pieceList[this.cube1 + 10].block == "" && pieceList[this.cube2 + 10].block == "")
            {
                return true
            }

        } else if (rotation == 3) {

            if (pieceList[this.cube1 + 10].block == "" && pieceList[this.cube4 + 10].block == "")
            {
                return true
            }
        }

        return false
    }


    override fun moveRight(pieceList: ArrayList<Piece>){

        this.axe += 1
        pieceList[this.axe] = PieceT(this.axe)

        if (this.cube1 != 0) {
            this.cube1 += 1
            pieceList[cube1] = PieceT(cube1)
        }
        if (this.cube2 != 0) {
            this.cube2 += 1
            pieceList[cube2] = PieceT(cube2)
        }
        if (this.cube3 != 0) {
            this.cube3 += 1
            pieceList[cube3] = PieceT(cube3)
        }
        if (this.cube4 != 0) {
            this.cube4 += 1
            pieceList[cube4] = PieceT(cube4)
        }

    }

    override fun moveLeft(pieceList: ArrayList<Piece>){

        this.axe -= 1
        pieceList[this.axe] = PieceT(this.axe)

        if (this.cube1 != 0) {
            this.cube1 -= 1
            pieceList[cube1] = PieceT(cube1)
        }
        if (this.cube2 != 0) {
            this.cube2 -= 1
            pieceList[cube2] = PieceT(cube2)
        }
        if (this.cube3 != 0) {
            this.cube3 -= 1
            pieceList[cube3] = PieceT(cube3)
        }
        if (this.cube4 != 0) {
            this.cube4 -= 1
            pieceList[cube4] = PieceT(cube4)
        }
    }

    override fun moveDown(pieceList: ArrayList<Piece>){

        this.axe += 10
        pieceList[this.axe] = PieceT(this.axe)

        if (this.cube1 != 0) {
            this.cube1 += 10
            pieceList[cube1] = PieceT(cube1)
        }
        if (this.cube2 != 0) {
            this.cube2 += 10
            pieceList[cube2] = PieceT(cube2)
        }
        if (this.cube3 != 0) {
            this.cube3 += 10
            pieceList[cube3] = PieceT(cube3)
        }
        if (this.cube4 != 0) {
            this.cube4 += 10
            pieceList[cube4] = PieceT(cube4)
        }
    }
}