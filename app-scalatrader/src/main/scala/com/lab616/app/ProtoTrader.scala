package com.lab616.app

import com.lab616.trading.platform.Platform
import com.lab616.omnibus.event.{EventEngine, EventWatcher}
import com.lab616.ib.api.TWSClientManager

object ProtoTrader {

  def main(args: Array[String]): Unit = {
    // Create the platform
    val platform = new Platform
    platform.run(args)
    
    // Get the event engine
    val engine = platform.getInstance(classOf[EventEngine])
    println("Got engine = " + engine + " in state = " + engine.isRunning)
    
    println("Got client manager = " + platform.getInstance(classOf[TWSClientManager]))
    
    
  }
}
