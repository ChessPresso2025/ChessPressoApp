package app.chesspresso.model

object Position {
    var positions : Array<Array<String?>> = Array(8) { Array(8) { null } }

    init{
        for(i in 65..72){
            for(j in 1..8){
                positions[i][j] = "${i.toChar()}$j"
            }
        }
    }

}