package fr.irtx.lead.matsim_noise_connector;

public class NoiseFormat {
	private NoiseFormat() {
	}

	public String roadId;
	public String intensityPrefix;
	public String speed;

	public String receiverId;
	public String closestRoadId;
	public String closestRoadDistance;
	public String residents;

	public String unusedPrefix = "UNUSED";

	public String intensity(int slot) {
		return intensityPrefix + slot;
	}

	public String unused(int index) {
		return unusedPrefix + index;
	}

	public enum NoiseFormatType {
		documentation, examples
	}

	static public NoiseFormat from(NoiseFormatType type) {
		switch (type) {
		case documentation:
			return fromDocumentation();
		case examples:
			return fromExamples();
		default:
			throw new IllegalStateException();
		}
	}

	static public NoiseFormat fromDocumentation() {
		NoiseFormat format = new NoiseFormat();
		format.roadId = "ROAD_ID";
		format.intensityPrefix = "INTENSITY_CAT";
		format.speed = "SPEED";
		format.receiverId = "RECEIVER_ID";
		format.closestRoadId = "CLOSEST_ROAD";
		format.closestRoadDistance = "CLOSEST_DIST";
		format.residents = "RESIDENTS";
		return format;
	}

	static public NoiseFormat fromExamples() {
		NoiseFormat format = new NoiseFormat();
		format.roadId = "FID_EjesCa";
		format.intensityPrefix = "Q";
		format.speed = "kmh";
		format.receiverId = "FID";
		format.closestRoadId = "FID";
		format.closestRoadDistance = "NEAR_DIST";
		format.residents = "EINW";
		return format;
	}
}
