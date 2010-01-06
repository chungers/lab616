package com.lab616.app

import com.lab616.trading.platform.Platform
import com.lab616.omnibus.event.{EventEngine, EventWatcher}
import com.lab616.ib.api.TWSClientManager

object TestTrader {

  def main(args: Array[String]): Unit = {
    // Create the platform
    val platform = new Platform
    platform.run(args)
    
    // Get the event engine
    val engine = platform.getInstance(classOf[EventEngine], 1000)
    println("Got engine = " + engine + " in state = " + engine.running)
    
    println("Got client manager = " + platform.getInstance(classOf[TWSClientManager], 1000))
    
  }
}
