package fr.irtx.lead.matsim_noise_connector;

import java.io.File;
import java.io.IOException;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.irtx.lead.matsim_noise_connector.data.ConfigurationData;

public class RunNoiseConverter {
	static public void main(String[] args)
			throws ConfigurationException, JsonParseException, JsonMappingException, IOException {
		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("network-path", "plans-path", "receivers-path", "volumes-path", "configuration-path") //
				.build();

		File networkPath = new File(cmd.getOptionStrict("network-path"));
		File plansPath = new File(cmd.getOptionStrict("plans-path"));
		File configurationPath = new File(cmd.getOptionStrict("configuration-path"));
		File receiversPath = new File(cmd.getOptionStrict("receivers-path"));
		File volumesPath = new File(cmd.getOptionStrict("volumes-path"));

		ConfigurationData configuration = new ObjectMapper().readValue(configurationPath, ConfigurationData.class);
		NoiseFormat format = NoiseFormat.from(configuration.outputFormat);

		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);

		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkPath.getPath());
		new PopulationReader(scenario).readFile(plansPath.getPath());

		NoiseCollector noiseCollector = new NoiseCollector(scenario.getNetwork());
		noiseCollector.run(scenario, configuration);

		new NoiseWriter(receiversPath, volumesPath, format).write(noiseCollector);
	}
}
