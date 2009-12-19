package com.lab616.trading.quote

abstract class TickData

case class Bid(timestamp: Long, 
               symbol: String, 
               price: Double, 
               source: String) extends TickData
case class Ask(timestamp: Long, 
               symbol: String, 
               price: Double, 
               source: String) extends TickData
case class BidSize(timestamp: Long, 
               symbol: String, 
               size: Int, 
               source: String) extends TickData
case class AskSize(timestamp: Long, 
               symbol: String, 
               size: Int, 
               source: String) extends TickData
