package com.neelnetwork.matcher
import com.neelnetwork.matcher.model.{LevelAgg, LimitOrder}
import com.neelnetwork.matcher.model.MatcherModel.{Level, Price}

package object api {
  def aggregateLevel(l: (Price, Level[LimitOrder])) = LevelAgg(l._2.view.map(_.amount).sum, l._1)
}
