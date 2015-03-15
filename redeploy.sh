echo "Stopping play server"
./stop.sh
sleep 1
echo "Updating"
./update.sh
echo "Starting updated app"
./start.sh
echo "Redeploy done"
