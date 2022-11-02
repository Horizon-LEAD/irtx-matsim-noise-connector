package fr.irtx.lead.matsim_noise_connector.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModeMappingData {
	@JsonProperty("noise_category")
	public Integer noiseCategory = 0;

	@JsonProperty("sampling_rate")
	public double samplingRate = 1.0;
}
