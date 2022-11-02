package fr.irtx.lead.matsim_noise_connector.data;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.irtx.lead.matsim_noise_connector.NoiseFormat.NoiseFormatType;

public class ConfigurationData {
	@JsonProperty("mode_mapping")
	public Map<String, ModeMappingData> modeMapping = new HashMap<>();

	@JsonProperty("output_format")
	public NoiseFormatType outputFormat = NoiseFormatType.examples;
}
