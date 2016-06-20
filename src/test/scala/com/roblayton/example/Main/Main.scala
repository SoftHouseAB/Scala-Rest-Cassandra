package com.roblayton.example.Main

/**
  * Created by jaswath on 18-06-2016.
  */

import org.scalatest._

abstract class UnitSpec extends FlatSpec with Matchers with OptionValues with Inside with Inspectors

class Main extends UnitSpec {

}
