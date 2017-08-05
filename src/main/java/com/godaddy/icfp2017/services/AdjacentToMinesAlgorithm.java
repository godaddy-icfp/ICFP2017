package com.godaddy.icfp2017.services;

import com.godaddy.icfp2017.models.River;
import com.godaddy.icfp2017.models.Site;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * Created by tim on 8/4/17.
 */
public class AdjacentToMinesAlgorithm
{
    private SimpleWeightedGraph<Site, River> graph;

    public AdjacentToMinesAlgorithm(SimpleWeightedGraph<Site, River> graph)
    {
        this.graph = graph;
    }

    public void run()
    {
        this.graph.vertexSet().stream()
                .forEach(site -> {
                    this.graph.edgesOf(site).stream()
                            .forEach(river -> {
                                if (site.isMine()) river.getAlgorithmWeights().put("Adjacent", 10.0);
                                else river.getAlgorithmWeights().put("Adjacent", 1.0);
                            });
                });
    }
}
