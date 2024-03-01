package com.github.zomb_676.cargo_hologram.selector

class StringAnalyzer(private val str: String) {
//        var index = 0
//            private set
//        val stringBuilder = StringBuilder()
//        private fun canStillAnalyze() = index < str.length
//        private fun assertRemain() {
//            if(!canStillAnalyze()) throw RuntimeException("can't continue read")
//        }
//        private fun <T> noRemainSpace() =  Result.failure<T>(RuntimeException("no remain space for read"))
//
//        fun init(): StringAnalyzer {
//            if (str.isEmpty()) throw RuntimeException("analyze str is empty")
//            return this
//        }
//        fun expectInt() : Result<Int> {
//            while (canStillAnalyze()) {
//                val char = str[index]
//                if (char.isDigit()) {
//                    stringBuilder.append(char)
//                    index++
//                } else {
//                    return Result.failure(NumberFormatException("find $char when expecting a number,index:$index, already read:$stringBuilder"))
//                }
//                return try {
//                    Result.success(Integer.valueOf(stringBuilder.toString()))
//                } catch (e : NumberFormatException) {
//                    Result.failure(e)
//                }
//            }
//            return noRemainSpace()
//        }
//        fun optionalString(optionalStr : String) : Result<Boolean> {
//            if (str.length < optionalStr.length + index) return Result.failure(RuntimeException("remain:${str.substring(index)} shorter than expect:$optionalStr"))
//            optionalStr.forEach { checkChar ->
//                if (checkChar != str[index]) return Result.success(false)
//                index++
//            }
//            return Result.success(true)
//        }
//        fun consumeString(optionalStr : String) :  {
//            if (str.length < optionalStr.length + index) return Result.failure(RuntimeException("remain:${str.substring(index)} shorter than expect:$optionalStr"))
//            optionalStr.forEach { checkChar ->
//                if (checkChar != str[index]) return Result.success(false)
//                index++
//            }
//            return Result.success(true)
//        }
}