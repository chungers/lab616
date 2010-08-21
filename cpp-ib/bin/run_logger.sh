#!/bin/bash

THIS=$(basename $0)
HOME=$(dirname $0)/..

TARGET=logger_main
BIN=$HOME/_build/bin/$TARGET
LOG_DIR=$HOME/src/ib/cl-logs
TICKERS_FILE=$HOME/conf/tickers
PID_FILE=$LOG_DIR/$TARGET.pid

case $1 in
--build)
  # Clean up and do a new build.
	TO_CLEAN=$(find $HOME -name $TARGET -type f)
	echo -e "Removing $TO_CLEAN"
	echo "$TO_CLEAN" | xargs rm -f
	pushd $HOME
	cmake .
	make install $TARGET
;;
--stop)
	l=$(ps -p $(cat $PID_FILE) -o command=)
	echo -e "Found: $l"
	if [[ $l != "" ]]; then
	    echo -e "Stopping $(cat $PID_FILE)"
	    kill -s TERM $(cat $PID_FILE)
	fi
	exit 1;
;;
esac
shift;

echo -e "Starting $BIN, LOG_DIR=$LOG_DIR"

TICKERS=""
for t in $(sort $TICKERS_FILE); do
    if [[ $TICKERS != "" ]]; then
	TICKERS="$TICKERS,$t"
    else
	TICKERS="$t"
    fi
done

echo -e "Tickers = $TICKERS"

CMD="$BIN --log_dir=$LOG_DIR \
--client_id=1000 --v=4 \
--tickdata_symbols=$TICKERS \
--option_symbol=SPY --option_strike=109 \
--option_month=8 --option_day=20 --option_year=2010
"
echo $CMD
$CMD &
PID=$!
echo $PID > $PID_FILE
echo -e "pid = $PID"
