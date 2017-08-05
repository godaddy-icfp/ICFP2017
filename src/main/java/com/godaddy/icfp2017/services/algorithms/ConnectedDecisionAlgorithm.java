package com.godaddy.icfp2017.services.algorithms;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import com.godaddy.icfp2017.services.Weights;
import com.godaddy.icfp2017.services.algorithms.GraphAlgorithm;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.alg.ConnectivityInspector;

final public class ConnectedDecisionAlgorithm extends BaseAlgorithm {
    private final Setter setter;

    public ConnectedDecisionAlgorithm(
            final GraphAlgorithm.Getter getter,
            final GraphAlgorithm.Setter setter) {
        this.setter = setter;
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

                        ConnectivityInspector<Site, River> inspector = new ConnectivityInspector<>(state.getClaimedGraph());
                        boolean redundantConnection;
                        redundantConnection = inspector.pathExists(source, target);

                        if (redundantConnection) {
                            setter.apply(river, Weights.Zero);
                        } else {
                            setter.apply(river, Weights.Max);
                        }
                    }
                });
    }
}