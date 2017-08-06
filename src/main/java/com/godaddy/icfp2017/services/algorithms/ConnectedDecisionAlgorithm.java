package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import org.jgrapht.graph.SimpleWeightedGraph;

final public class ConnectedDecisionAlgorithm extends BaseAlgorithm {

    public ConnectedDecisionAlgorithm(
            final GraphAlgorithm.Getter getter,
            final GraphAlgorithm.Setter setter) {
        super(getter, setter);
    }

    @Override
    public void iterate(final State state) {
        final SimpleWeightedGraph<Site, River> graph = state.getGraph();
        graph.edgeSet()
                .forEach(river -> {
                    boolean sourceConnected = false;
                    boolean targetConnected = false;

                    Site source = graph.getEdgeSource(river);
                    for (River sourceRiver : graph.edgesOf(source)) {
                        if (sourceRiver.getClaimedBy() == state.getPunter()) {
                            sourceConnected = true;
                        }
                    }

                    Site target = graph.getEdgeTarget(river);
                    for (River targetRiver : graph.edgesOf(target)) {
                        if (targetRiver.getClaimedBy() == state.getPunter()) {
                            targetConnected = true;
                        }
                    }

                    if (sourceConnected && targetConnected) {
                      setter.apply(river, Weights.Undesireable);
                    }
                    else
                    {
                      setter.apply(river, Weights.Identity);
                    }
                });
    }
}