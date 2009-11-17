#!/bin/bash

port=8889
l="DXO DXD DDM FAZ GS C BAC JPM MS AXP COF SPY AAPL GOOG AMZN RTH EDC IYY DIG ERX ERY XLE XLV XOM CVX FAS SMN DRV FXP TZA QID QLD TYH TYP SRS URE SKF AYI GLD IAU QQQQ TBT UYM INTC SDS REW SSO UUP UDN PCLN"

case $1 in
indexes)
curl http://localhost:${port}/se -d c=tws -d m=ind -d profile=prod -d index=INDU -d exchange=NYSE
curl http://localhost:${port}/se -d c=tws -d m=ind -d profile=prod -d index=VIX -d exchange=CBOE
curl http://localhost:${port}/se -d c=tws -d m=ind -d profile=prod -d index=SPX -d exchange=CBOE
;;
ticks)
for i in $l; do 
    curl http://localhost:${port}/se -d c=tws -d m=ticks -d profile=prod -d symbol=$i
done
;;
doms)
curl http://localhost:${port}/se -d c=tws -d m=dom -d profile=prod -d symbol=FAS
curl http://localhost:${port}/se -d c=tws -d m=dom -d profile=prod -d symbol=CVX
curl http://localhost:${port}/se -d c=tws -d m=dom -d profile=prod -d symbol=XOM
;;
bars)
for i in $l; do 
    curl http://localhost:${port}/se -d c=tws -d m=bars -d profile=prod -d symbol=$i -d barSize=5 -d barType=TRADES -d rth=false 
done
;;
esac