#!/bin/bash

#Set fonts
NORM=`tput sgr0`
BOLD=`tput bold`
REV=`tput smso`

function show_usage () {
    echo -e "${BOLD}Basic usage:${NORM} entrypoint.sh [-vh] CONFIG PLANS NETWORK OUT_PATH"
}

function show_help () {
    echo -e "${BOLD}eentrypoint.sh${NORM}: Runs the Parcels Model"\\n
    show_usage
    echo -e "\n${BOLD}Required arguments:${NORM}"
    echo -e "${REV}CONFIG${NORM}\t the config json"
    echo -e "${REV}PLANS${NORM}\t the plans xml.gz describes the experienced movements and activities of the population"
    echo -e "${REV}NETWORK${NORM}\t the network xml.gz describes the road network"
    echo -e "${REV}OUT_PATH${NORM}\t the output path"\\n
    echo -e "${BOLD}Optional arguments:${NORM}"
    echo -e "${REV}-v${NORM}\tSets verbosity level"
    echo -e "${REV}-h${NORM}\tShows this message"
    echo -e "${BOLD}Examples:${NORM}"
    echo -e "entrypoint.sh -v sample-data/input/config.json sample-data/input/output_plans.xml.gz sample-data/input/output_network.xml.gz sample-data/output/"
}

##############################################################################
# GETOPTS                                                                    #
##############################################################################
# A POSIX variable
# Reset in case getopts has been used previously in the shell.
OPTIND=1

# Initialize vars:
verbose=0

# while getopts
while getopts 'hv' OPTION; do
    case "$OPTION" in
        h)
            show_help
            kill -INT $$
            ;;
        v)
            verbose=1
            ;;
        ?)
            show_usage >&2
            kill -INT $$
            ;;
    esac
done

shift "$(($OPTIND -1))"

leftovers=(${@})
CONFIG=${leftovers[0]}
PLANS=${leftovers[1]}
NETWORK=${leftovers[2]}
OUT_PATH=${leftovers[3]%/}

##############################################################################
# Input checks                                                               #
##############################################################################
if [ ! -f "${CONFIG}" ]; then
     echo -e "Give a ${BOLD}valid${NORM} config input file\n"; show_usage; exit 1
fi
if [ ! -f "${PLANS}" ]; then
     echo -e "Give a ${BOLD}valid${NORM} plans input file\n"; show_usage; exit 1
fi
if [ ! -f "${NETWORK}" ]; then
     echo -e "Give a ${BOLD}valid${NORM} network input file\n"; show_usage; exit 1
fi

if [ ! -d "${OUT_PATH}" ]; then
     echo -e "Give a ${BOLD}valid${NORM} output directory\n"; show_usage; exit 1
fi

##############################################################################
# Execution                                                                  #
##############################################################################
java -Xmx12g -cp /home/ubuntu/irtx-matsim-noise-connector/java/target/lead-matsim-noise-connector-1.0.0.jar fr.irtx.lead.matsim_noise_connector.RunNoiseConverter \
    --configuration-path ${CONFIG} \
    --network-path ${NETWORK} \
    --plans-path ${PLANS} \
    --receivers-path ${OUT_PATH}/receivers.csv \
    --volumes-path ${OUT_PATH}/volumes.csv
