package com.starschema.jampi
import org.scalatest._


class CartesianTopologyTest extends FunSuite {
  test("8x8 matrix should return the correct neighbors") {
    val t22 = CartesianTopology.getPosition(22, 64)
    assert( t22.coords == (2,6) )
    assert( t22.initial == GridLocation(20,16,6,38) )

    println(CartesianTopology.getPosition(22, 64))
    println(CartesianTopology.getPosition(23, 64))
    println(CartesianTopology.getPosition(59, 64))
    println(CartesianTopology.getPosition(0, 64))
    println(CartesianTopology.getPosition(63, 64))
  }
}
