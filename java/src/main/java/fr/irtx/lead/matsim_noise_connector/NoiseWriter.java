package fr.irtx.lead.matsim_noise_connector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.matsim.core.utils.io.IOUtils;

import fr.irtx.lead.matsim_noise_connector.NoiseCollector.Receiver;
import fr.irtx.lead.matsim_noise_connector.NoiseCollector.Road;

public class NoiseWriter {
	private final File receiversPath;
	private final File volumesPath;
	private final NoiseFormat format;

	public NoiseWriter(File receiversPath, File volumesPath, NoiseFormat format) {
		this.receiversPath = receiversPath;
		this.volumesPath = volumesPath;
		this.format = format;
	}

	public void write(NoiseCollector collector) throws IOException {
		{ // Receivers
			BufferedWriter writer = IOUtils.getBufferedWriter(receiversPath.getPath());

			writer.write(String.join(",", Arrays.asList( //
					format.receiverId, format.closestRoadId, format.residents, //
					format.closestRoadDistance, format.speed, format.unused(0))) + "\n");

			int receiverIndex = 0;

			for (Receiver receiver : collector.getReceivers()) {
				writer.write(String.join(",", Arrays.asList( //
						String.valueOf(receiverIndex++), //
						receiver.closestLink.getId().toString(), //
						String.valueOf(receiver.persons), //
						String.valueOf(receiver.closestLinkDistance), //
						String.valueOf(receiver.speed), //
						String.valueOf(0) //
				)) + "\n");
			}

			writer.close();
		}

		{ // Volumes
			BufferedWriter writer = IOUtils.getBufferedWriter(volumesPath.getPath());

			writer.write(String.join(",", Arrays.asList( //
					format.roadId, format.unused(0), format.unused(1), //
					format.unused(2), format.speed, format.intensity(1), //
					format.intensity(2), format.intensity(3), format.intensity(4))) + "\n");

			for (Road road : collector.getRoads()) {
				writer.write(String.join(",", Arrays.asList( //
						road.link.getId().toString(), //
						String.valueOf(0), //
						String.valueOf(0), //
						String.valueOf(0), //
						String.valueOf(road.speed), //
						String.valueOf(road.flow[0]), //
						String.valueOf(road.flow[1]), //
						String.valueOf(road.flow[2]), //
						String.valueOf(road.flow[3]) //
				)) + "\n");
			}

			writer.close();
		}
	}
}
