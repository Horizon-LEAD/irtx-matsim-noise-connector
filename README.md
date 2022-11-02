# IRTX MATSim to Noise connector

## Introduction

This model is a connector between the upstream MATSim model and the downstream
Noise analysis model.

The purpose of this connector is to process a given MATSim simulation output,
and aggregate the population to receiver points and prepare data on the flow
volumes in the road network to be used by the Noise model.

## Requirements

### Software requirements

To run the model, the environment needs to be prepared:

- A `Java` runtime needs to be present on the executing machine. It is recommended to set up an **Adoptium OpenJDK 11** (https://adoptium.net).

- A recent version of `maven` needs to be installed, version `3.6.3` has been tested: https://maven.apache.org/

It is recommended to set up the environment on a Linux machine, the following
executables should then be callable from the command line: `java`, `mvn`.

### Input / Output

#### Input

To run the model, a finished MATSim simulation must be available. Specifically,
the MATSim output directory should contain a file called `output_plans.xml.gz`
describing the experienced movements and activities of the population, as well
as `output_network.xml.gz` describing the road network.

#### Output

The output of the model are two CSV files (`receivers.csv` and `volumes.csv`) which
correspond to the two input files for the Noise model. In the respective documentation
they are also called **static** and **dynamic** input, respectively.

Note that as of 2 November 2022 the format of the CSV files differs in the Noise
model documentation and the its examples. The present connector provides the option
to select one of the two formats. They differ in the column names of the CSV files,
but should not affect the functioning of the Noise model as it accesses the columns
by index.

#### Configuration

The conversion process needs to be configured using a configuration file (`configuration.json`)
in JSON format. It is structured as follows:

```json
{
  "output_format": "examples",
  "mode_mapping": {
    "car": {
      "noise_category": 4,
      "sampling_rate": 0.05
    },
    "freight:van": {
      "noise_category": 1,
      "sampling_rate": 1.0
    }
  }
}
```

First, the output format can be defined which is either `examples` or `documentation`
depending on which column headers should be written based on the information of the
Noise model. After, a list of mode mappings is provided that links transport modes
from the MATSim simulation to the four analysis categories of the Noise model. Note
that JSprit vehicle types (when used as input to the MATSim simulation) are
translated into `freight:{vehicle_type}` while the other relevant contribution
to emissions is the standard `car` mode.

Attention, only modes that are defined in the configuration are considered in
the analysis.

Additionally, it is possible to define the sampling rate per mode which should
correspond to the sampling rate of the MATSim simulation. The relevant KPIs like
distance and vehicle count is scaled accordingly by the inverse of the sampling
rate.

## Building the model

The connector model is provided as Java code. To run it, it first needs to be built using
the Maven build system. For that purpose, one needs to enter the `java` directory
of the LEAD repository and call `mvn package`:

```bash
cd /irtx-matsim-noise-connector/java
mvn package
```

The build process should download all necessary Maven dependencies including
the MATSim and finish without errors. After, the built model should be
present in

```
/irtx-matsim-noise-connector/java/target/lead-matsim-1.0.0.jar
```

The `jar` file can be saved in a fixed location. As long as the model is not
changed, it can be reused for multiple model runs. To test whether the `jar` has
been build successfully, call

```bash
java -cp /irtx-matsim-noise-connector/java/target/lead-matsim-noise-connector-1.0.0.jar fr.irtx.lead.matsim.RunVerification
```

which should respond by the message `It works!`.

# Running the model

To run the connector, the respective jar needs to be built first. It can then be
started in the following way:

```bash
java -Xmx12g -cp /irtx-matsim-noise-connector/java/target/lead-matsim-noise-connector-1.0.0.jar fr.irtx.lead.matsim_noise_connector.RunNoiseConverter \
  --configuration-path /path/to/configuration.json \
  --network-path /path/to/output_network.xml.gz \
  --plans-path /path/to/output_plans.xml.gz \
  --receivers-path /path/to/receivers.csv \
  --volumes-path /path/to/volumes.csv
```

The first line is mandatory with the path to the built `jar` file that needs
to be adapted. The following lines represent parameters. The **mandatory**
parameters are detailed in the following table:

Parameter             | Values                            | Description
---                   | ---                               | ---
`--configuration-path`          | String                            | Path to the configuration file
`--network-path`         | String                            | Path to the MATSim output `output_network.xml.gz`
`--plans-path`         | String                            | Path to the MATSim output `output_plans.xml.gz`
`--receivers-path`         | String                            | Path to where the receiver results will be saved
`--volumes-path`         | String                            | Path to where the volumes results will be saved

Note that the memory available to Java can be configured using the `-Xmx` option and by appending a size of the format `1024M` to define the amount in megabytes or `12G` to define the amount in gigabytes.

## Standard scenarios

For the Lyon living lab, a configuration file has already been prepared in
`data/configuration_lyon.json`. It can be used to prepare Noise data for the
three main scenarios (Baseline 2022, UCC 2022, UCC 2030) as follows:

```bash
java -Xmx12g -cp /irtx-matsim-noise-connector/java/target/lead-matsim-noise-connector-1.0.0.jar fr.irtx.lead.matsim_noise_connector.RunNoiseConverter \
  --configuration-path data/configuration_lyon.json \
  --network-path /irtx-matsim/output/output_{scenario}/output_network.xml.gz \
  --plans-path /irtx-matsim/output/output_{scenario}/output_plans.xml.gz \
  --receivers-path /irtx-matsim-noise-connector/output/receivers_{scenario}.csv \
  --volumes-path /irtx-matsim-noise-connector/output/volumes_{scenario}.csv
```

Here, `{scenario} = baseline_2022 | ucc_2022 | ucc_2030`
according to the respective scenario.
