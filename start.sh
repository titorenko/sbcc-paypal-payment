export OPTIONS="-J-server -Dhttp.port=10000 -Dconfig.resource=application-prod.conf"

nohup target/universal/stage/bin/sbcc-shop $OPTIONS -Dpidfile.path=sbcc-shop.pid &

