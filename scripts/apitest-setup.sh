#!/bin/bash

tws_port=7496
port=8889
data_dir=/Users/david/lab616/runtime/test/data

curl http://localhost:${port}/se -d c=tws -d m=addp -d profile=prod -d host=localhost -d port=${tws_port}
curl http://localhost:${port}/varz

sleep 2
curl http://localhost:${port}/se -d c=tws -d m=start -d profile=prod
sleep 1
curl http://localhost:${port}/se -d c=tws -d m=start -d profile=prod
sleep 1
curl http://localhost:${port}/se -d c=tws -d m=start -d profile=prod
sleep 1
curl http://localhost:${port}/se -d c=tws -d m=start -d profile=prod

sleep 5
curl http://localhost:${port}/tws -d m=stats

sleep 1
curl http://localhost:${port}/se -d c=tws -d m=avro -d dir=${data_dir} -d profile=prod -d id=0
curl http://localhost:${port}/se -d c=tws -d m=avro -d dir=${data_dir} -d profile=prod -d id=1
curl http://localhost:${port}/se -d c=tws -d m=avro -d dir=${data_dir} -d profile=prod -d id=2
curl http://localhost:${port}/se -d c=tws -d m=avro -d dir=${data_dir} -d profile=prod -d id=3

sleep 2
curl http://localhost:${port}/se -d c=tws -d m=proto -d dir=${data_dir} -d profile=prod -d id=0
curl http://localhost:${port}/se -d c=tws -d m=proto -d dir=${data_dir} -d profile=prod -d id=1
curl http://localhost:${port}/se -d c=tws -d m=proto -d dir=${data_dir} -d profile=prod -d id=2
curl http://localhost:${port}/se -d c=tws -d m=proto -d dir=${data_dir} -d profile=prod -d id=3

sleep 2
curl http://localhost:${port}/se -d c=tws -d m=csv -d dir=${data_dir} -d profile=prod -d id=0
curl http://localhost:${port}/se -d c=tws -d m=csv -d dir=${data_dir} -d profile=prod -d id=1
curl http://localhost:${port}/se -d c=tws -d m=csv -d dir=${data_dir} -d profile=prod -d id=2
curl http://localhost:${port}/se -d c=tws -d m=csv -d dir=${data_dir} -d profile=prod -d id=3


sleep 5
curl http://localhost:${port}/tws -d m=stats


