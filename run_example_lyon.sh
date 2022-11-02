set -e

## Prepare
cd /home/ubuntu/irtx-matsim-noise-connector
mkdir /home/ubuntu/irtx-matsim-noise-connector/output

## Build connector
cd /home/ubuntu/irtx-matsim-noise-connector/java
mvn package
cd /home/ubuntu/irtx-matsim-noise-connector

## Run scenarios
for scenario in baseline_2022 ucc_2022 ucc_2030; do
	java -Xmx12g -cp /home/ubuntu/irtx-matsim-noise-connector/java/target/lead-matsim-noise-connector-1.0.0.jar fr.irtx.lead.matsim_noise_connector.RunNoiseConverter \
	  --configuration-path /home/ubuntu/irtx-matsim-noise-connector/data/configuration_lyon.json \
	  --network-path /home/ubuntu/irtx-matsim/output/output_${scenario}/output_network.xml.gz \
	  --plans-path /home/ubuntu/irtx-matsim/output/output_${scenario}/output_plans.xml.gz \
	  --receivers-path /home/ubuntu/irtx-matsim-noise-connector/output/receivers_${scenario}.csv \
	  --volumes-path /home/ubuntu/irtx-matsim-noise-connector/output/volumes_${scenario}.csv
done
