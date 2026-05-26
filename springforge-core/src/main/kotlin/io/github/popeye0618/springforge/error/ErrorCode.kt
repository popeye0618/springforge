package io.github.popeye0618.springforge.error

interface ErrorCode {
    val code: String
    val message: String
    val status: Int
}