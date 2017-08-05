package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import com.godaddy.icfp2017.models.State;
import org.jgrapht.graph.SimpleWeightedGraph;

public class AdjacentToEdgesAlgorithm implements GraphAlgorithm {
    private SimpleWeightedGraph<Site, River> graph;

    public AdjacentToEdgesAlgorithm(final State state) {
        this.graph = state.getMap();
    }

    @Override
    public void run() {
        for (Site site : this.graph.vertexSet()) {
            int openEdges = 0;
            int edgesClaimedByMe = 0;
            int edgesClaimedByOthers = 0;

            for (River river : this.graph.edgesOf(site)) {
                if (river.punter_id === mine) {
                    openEdges+
                }

            }


        }
        this.graph.vertexSet()
                .forEach(site -> {
                    this.graph.edgesOf(site)
                            .forEach(river -> {
                                if (site.isMine()) {
                                    river.getAlgorithmWeights().put(Algorithms.Adjacent, Weights.Max);
                                }
                                else {
                                    river.getAlgorithmWeights().put(Algorithms.Adjacent, Weights.Identity);
                                }
                            });
                });
    }
}
