package fr.irtx.lead.matsim_noise_connector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contribs.discrete_mode_choice.replanning.time_interpreter.EndTimeThenDurationInterpreter;
import org.matsim.contribs.discrete_mode_choice.replanning.time_interpreter.TimeInterpreter;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.CoordUtils;

import com.google.common.base.Verify;

import fr.irtx.lead.matsim_noise_connector.data.ConfigurationData;
import fr.irtx.lead.matsim_noise_connector.data.ModeMappingData;

public class NoiseCollector {
	private final QuadTree<Receiver> receiverIndex;
	private final List<Receiver> receivers = new LinkedList<>();
	private final IdMap<Link, Road> roads = new IdMap<>(Link.class);

	private final double totalDuration = 24 * 3600.0;

	static public class Road {
		public Link link;
		public double speed;
		public double[] flow = new double[4];
	}

	static public class Receiver {
		public Coord location;
		public Link closestLink;
		public double closestLinkDistance;
		public double persons;
		public double speed;
	}

	public NoiseCollector(Network network) {
		double bounds[] = NetworkUtils.getBoundingBox(network.getNodes().values());
		receiverIndex = new QuadTree<>(bounds[0], bounds[1], bounds[2], bounds[3]);
	}

	public void run(Scenario scenario, ConfigurationData config) {
		Population population = scenario.getPopulation();

		Network network = NetworkUtils.createNetwork();
		new TransportModeNetworkFilter(scenario.getNetwork()).filter(network, Collections.singleton("car"));

		for (Person person : population.getPersons().values()) {
			Plan plan = person.getSelectedPlan();

			for (Leg leg : TripStructureUtils.getLegs(plan)) {
				ModeMappingData mapping = config.modeMapping.get(leg.getMode());

				if (mapping != null) {
					Verify.verify(leg.getRoute() instanceof NetworkRoute);
					NetworkRoute route = (NetworkRoute) leg.getRoute();

					for (Id<Link> linkId : route.getLinkIds()) {
						Link link = network.getLinks().get(linkId);

						Road road = roads.computeIfAbsent(linkId, id -> {
							Road internal = new Road();
							internal.link = link;
							internal.speed = link.getFreespeed() * 3.6;
							return internal;
						});

						int category = mapping.noiseCategory - 1;
						Verify.verify(category >= 0);
						Verify.verify(category < 4);
						road.flow[category] += 1.0 / mapping.samplingRate;
					}
				}
			}

			TimeInterpreter timeInterpreter = new EndTimeThenDurationInterpreter(0.0, true);

			for (PlanElement element : plan.getPlanElements()) {
				if (element instanceof Activity) {
					Activity activity = (Activity) element;

					if (activity.getMaximumDuration().isUndefined() && activity.getEndTime().isUndefined()) {
						activity.setEndTime(totalDuration);
					}
				}

				double startTime = timeInterpreter.getCurrentTime();
				timeInterpreter.addPlanElement(element);
				double endTime = timeInterpreter.getCurrentTime();

				startTime = Math.min(startTime, totalDuration);
				endTime = Math.min(endTime, totalDuration);

				if (endTime > startTime) {
					if (element instanceof Activity) {
						Activity activity = (Activity) element;
						Coord location = activity.getCoord();

						Receiver receiver = receiverIndex.getClosest(location.getX(), location.getY());

						if (receiver == null || CoordUtils.calcEuclideanDistance(location, receiver.location) > 10.0) {
							receiver = new Receiver();
							receiver.closestLink = NetworkUtils.getNearestLink(network, location);
							receiver.closestLinkDistance = CoordUtils.calcEuclideanDistance(location,
									receiver.closestLink.getCoord());
							receiver.location = location;
							receiver.speed = receiver.closestLink.getFreespeed() * 3.6;

							receivers.add(receiver);
							receiverIndex.put(location.getX(), location.getY(), receiver);
						}

						receiver.persons += (endTime - startTime) / totalDuration;
					}
				}
			}
		}
	}

	public List<Receiver> getReceivers() {
		return receivers;
	}

	public List<Road> getRoads() {
		return new ArrayList<>(roads.values());
	}
}
