package org.wikibrain.mapper.algorithms.conceptualign3;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import org.wikibrain.core.WikiBrainException;
import org.wikibrain.core.dao.LocalPageDao;
import org.wikibrain.core.lang.LocalId;

import java.util.*;

/**
 * Created by bjhecht on 5/20/14.
 */
public class Conceptualign3ConnectedComponentHandler implements ConnectedComponentHandler{

    private final double minVotesRatio;
    private final int maxVotesPerLang;
    private final boolean print;
    private int curUnivId;
    private final LocalPageDao lpDao;

    public Conceptualign3ConnectedComponentHandler(double minVotesRatio,
                                                   int maxVotesPerLang, boolean print, LocalPageDao lpDao) throws WikiBrainException {
        this.minVotesRatio = minVotesRatio;
        this.maxVotesPerLang = maxVotesPerLang;
        this.print = print;
        this.curUnivId = 0;
        this.lpDao = lpDao;

    }

    public int getCurUnivId(){
        curUnivId++;
        return curUnivId;
    }

    @Override
    public List<ClusterResult> handle(List<LocalId> curVertices, ILLGraph graph, int componentId)
            throws WikiBrainException {

        // if its unambiguous, revert to Conceptualign1
        ConceptualignHelper.ScanResult origScanResult = ConceptualignHelper.scanVerticesOfComponent(curVertices);
        boolean origNotAmbiguous = origScanResult.clarity.equals(1.0);
        if (origNotAmbiguous){
            List<ClusterResult> rVal = Lists.newArrayList();
            rVal.add(new ClusterResult(getCurUnivId(), curVertices));
            return rVal;
        }

        // if it is ambiguous... TODO: convert to multimap
        Map<LocalId, List<LocalId>> ills = new HashMap<LocalId, List<LocalId>>();
        for (LocalId curVertex : curVertices){
            Set<ILLEdge> edges = graph.outgoingEdgesOf(curVertex);
            List<LocalId> dests = new ArrayList<LocalId>();
            for (ILLEdge edge : edges){
                dests.add(edge.dest);
            }
            ills.put(curVertex, dests);
        }

        List<ClusterResult> rVal = new ArrayList<ClusterResult>();
        int minLangVotes = (int)Math.floor(minVotesRatio*origScanResult.langCount);
        Set<Set<LocalId>> clusters = ILLSplitter.split(ills, minLangVotes, maxVotesPerLang, print, lpDao);
        for (Set<LocalId> curCluster : clusters){
            int clusterUnivId = getCurUnivId();
            List<LocalId> vertexList = new ArrayList<LocalId>();
            vertexList.addAll(curCluster);
            ClusterResult clusterResult = new ClusterResult(clusterUnivId, vertexList);
            rVal.add(clusterResult);
        }
        return rVal;

    }



}
