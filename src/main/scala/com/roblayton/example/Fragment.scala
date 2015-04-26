package com.roblayton.spray

import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{ read, write, writePretty }

trait Fragment

case class MineralFragment(name: String, kind: String, weight: Double) extends Fragment

object Fragment {
  var fragments = List[Fragment](
    MineralFragment("amorphous", "Graphite", 0.01),
    MineralFragment("flake", "Graphite", 0.3),
    MineralFragment("vein", "Graphite", 0.55))

  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[Fragment])))
  def toJson(fragments: List[Fragment]): String = writePretty(fragments)
  def toJson(fragment: Fragment): String = writePretty(fragment)
}

