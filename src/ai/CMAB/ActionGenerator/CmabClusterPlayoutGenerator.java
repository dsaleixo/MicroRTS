/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.CMAB.ActionGenerator;

import ai.abstraction.WorkerHarvestRush;
import ai.abstraction.partialobservability.POHeavyRush;
import ai.abstraction.partialobservability.POLightRush;
import ai.abstraction.partialobservability.PORangedRush;
import ai.abstraction.pathfinding.AStarPathFinding;
//import ai.abstraction.partialobservability.POWorkerRush;
import ai.cluster.CIA_Enemy;
import ai.cluster.MST.Graph;
import ai.cluster.core.hdbscanstar.HDBSCANStarObject;
import ai.cluster.core.hdbscanstar.UndirectedGraph;
import ai.configurablescript.BasicExpandedConfigurableScript;
import ai.core.AI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.UnitActionAssignment;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;

/**
 *
 * @author rubens
 */
public class CmabClusterPlayoutGenerator implements ICMAB_ActionGenerator{
    private final List<AI> scripts;
    private final GameState gs_to_start_from;
    PhysicalGameState pgs;
    ResourceUsage base_ru;
    private final int playerForThisComputation;
    private final List<Pair<Unit, List<UnitAction>>> choices;
    private long size = 1;  // this will be capped at Long.MAX_VALUE;    
    HashSet<Unit> unitsControled = new HashSet<>();
    ArrayList<ArrayList<Unit>> clusters;
    int minSize;
    int minPoint;
    AI playoutAI1;
    AI playoutAI2;
    Graph graph;

    public CmabClusterPlayoutGenerator( GameState gs_to_start_from, int playerForThisComputation, UnitTypeTable a_utt, int minSize, int minPoint) throws Exception{
        this.scripts = new ArrayList<>();
        this.gs_to_start_from = gs_to_start_from;
        this.playerForThisComputation = playerForThisComputation;
        this.minSize = minSize;
        this.minPoint = minPoint;
        this.clusters = new ArrayList<>();
        //this.behaviorAbs = new ManagerClosest(playerForThisComputation, 2);
        choices = new ArrayList<>();
        size = 1;
        playoutAI1 = new POLightRush(a_utt);
        playoutAI2 = new POLightRush(a_utt);
        graph = new Graph();
        
        buildPortfolio(a_utt);
        //all moves by units controled by behavior
        generatedMovesAsymmetric();
        //moves generated by scripts
        generatedMovesAbstractic();
        
        //System.out.println("size = " + size);
        //System.out.println("Choices = " + choices.toString());
        
         if (choices.isEmpty()) {
            System.err.println("Problematic game state:");
            System.err.println(gs_to_start_from);
            throw new Exception("Move generator for player " + playerForThisComputation + " created with no units that can execute actions! (status: " + gs_to_start_from.canExecuteAnyAction(0) + ", " + gs_to_start_from.canExecuteAnyAction(1) + ")");
        }
    }
    
    private void generatedMovesAbstractic() throws Exception{
        
        
        ArrayList<PlayerAction> playerActions = new ArrayList<>();
        for (AI script : scripts) {
            playerActions.add(script.getAction(playerForThisComputation, gs_to_start_from));
        }
        PlayerAction pa = new PlayerAction();
        for (Unit u : gs_to_start_from.getUnits()) {
            if (u.getPlayer() == playerForThisComputation && !unitsControled.contains(u)) {
                if (gs_to_start_from.getUnitActions().get(u) == null) {
                    List<UnitAction> l = getUnitActions(u, playerActions);
                    if (l.size() > 0 ) {
                        choices.add(new Pair<>(u, l));
                        // make sure we don't overflow:
                        long tmp = l.size();
                        if (Long.MAX_VALUE / size <= tmp) {
                            size = Long.MAX_VALUE;
                        } else {
                            size *= (long) l.size();
                        }
//                    System.out.println("size = " + size);
                    }
                }
            }
        }
        //System.out.println("---"+choices.toString());
    }
    
    protected final void generatedMovesAsymmetric() throws Exception{
        //unidades.clear();
        findBestClusters();
        filterClusters();
        removeEnemyClusters();
        selectUnitsControled();
        //System.out.println("units"+ unitsControled.toString()); //removes
        // Generate the reserved resources:
        base_ru = new ResourceUsage();
        GameState gs = gs_to_start_from;
        pgs = gs.getPhysicalGameState();
        
        for(Unit u:pgs.getUnits()) {
            UnitActionAssignment uaa = gs.getUnitActions().get(u);
            if (uaa!=null) {
                ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                base_ru.merge(ru);
            }
        }
                
        for(Unit u:unitsControled) {
            if (u.getPlayer()==playerForThisComputation) {
                if (gs.getUnitActions().get(u)==null) {
                    List<UnitAction> l = u.getUnitActions(gs);
                    choices.add(new Pair<>(u,l));
                    // make sure we don't overflow:
                    long tmp = l.size();
                    if (Long.MAX_VALUE/size <= tmp) {
                        size = Long.MAX_VALUE;
                    } else {
                        size*=(long)l.size();
                    }
//                    System.out.println("size = " + size);
                }
            }
        }  
        //System.out.println("---Asymmetric--"+choices.toString());

    }
    protected final void buildPortfolio(UnitTypeTable utt) {
        //this.scripts.add(new POLightRush(utt));
        //this.scripts.add(new PORangedRush(utt)); 
        //this.scripts.add(new POHeavyRush(utt));
        //this.scripts.add(new POWorkerRush(utt));     
        
        //this.scripts.add(new BasicExpandedConfigurableScript(utt, new AStarPathFinding(), 18,0,0,1,2,2,-1,-1,3)); //wr
        this.scripts.add(new BasicExpandedConfigurableScript(utt, new AStarPathFinding(), 18,0,0,1,2,2,-1,-1,4)); //lr
        this.scripts.add(new BasicExpandedConfigurableScript(utt, new AStarPathFinding(), 18,0,0,1,2,2,-1,-1,5)); //HR
        this.scripts.add(new BasicExpandedConfigurableScript(utt, new AStarPathFinding(), 18,0,0,1,2,2,-1,-1,6)); //RR
        this.scripts.add(new WorkerHarvestRush(utt));
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public List<Pair<Unit, List<UnitAction>>> getChoices() {
        return this.choices;
    }

    /**
     * make one collection of unitActions for the unit using all Actions
     *
     * @param u
     * @param playerActions
     * @return
     */
    private List<UnitAction> getUnitActions(Unit u, ArrayList<PlayerAction> playerActions) {
        HashSet<UnitAction> unAction = new HashSet<>();
        for (PlayerAction playerAction : playerActions) {
            unAction.add(playerAction.getAction(u));
        }
        //inserted wait action to fix move problem
        //unAction.add(new UnitAction(UnitAction.TYPE_NONE, 10));
        return new ArrayList<>(unAction);
    }
    
    private void findBestClusters() throws Exception{
        UndirectedGraph mst = contructMSTByPlayout();
        if(mst==null){
            this.clusters.clear();
            return;
        }
        try {
            int[] clusterInt = HDBSCANStarObject.runHDBSCANGraph(graph.getTotalNodos(),mst, minPoint, minSize,  true);
            //System.out.println(Arrays.toString(clusterInt));
            buildClusters(graph.generateDataSet(gs_to_start_from), clusterInt, graph.getUnitsOrdered(gs_to_start_from));
        } catch (IOException ex) {
            Logger.getLogger(CIA_Enemy.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    protected UndirectedGraph contructMSTByPlayout() throws Exception {
        HashMap<GameState, List<PlayerAction>> listActionByState = new HashMap<>();
        int time = 100;
        GameState gs = gs_to_start_from.clone();

        boolean gameover = false;

        do {
            if (gs.isComplete()) {
                gameover = gs.cycle();
            } else {
                ArrayList<PlayerAction> actions = new ArrayList<>();

                PlayerAction p1 = playoutAI1.getAction(0, gs);
                PlayerAction p2 = playoutAI2.getAction(1, gs);

                if (p1.getActions().size() > 0) {
                    actions.add(p1);
                }
                if (p2.getActions().size() > 0) {
                    actions.add(p2);
                }
                if (actions.size() > 0) {
                    listActionByState.put(gs.clone(), actions);
                }

                gs.issue(p1);
                gs.issue(p2);
            }
        } while (!gameover && gs.getTime() < time);

        UndirectedGraph ret = graph.build(listActionByState, playerForThisComputation);
        return ret;

    }
    
    private ArrayList<Unit> getUnits(int player) {
        ArrayList<Unit> unitsPlayer = new ArrayList<>();
        for (Unit u : gs_to_start_from.getUnits()) {
            if (u.getPlayer() == player) {
                unitsPlayer.add(u);
            }
        }
        return unitsPlayer;
    }
    
    
    private void buildClusters(double[][] dataSet, int[] clusterInt, ArrayList<Unit> unitsCl) {
        this.clusters.clear();
        HashSet<Integer> labels = new HashSet<>();
        for (int i = 0; i < clusterInt.length; i++) {
            labels.add(clusterInt[i]);
        }
        labels.remove(0);
        
        for (Integer label : labels) {
            ArrayList<Unit> cluster = new ArrayList<>();

            for (int i = 0; i < clusterInt.length; i++) {
                if (clusterInt[i] == label) {
                    double[] tPos = dataSet[i];
                    Unit untC = getUnitByPos(tPos, unitsCl);
                    cluster.add(untC);
                }
            }

            this.clusters.add(cluster);
        }
    }
    
    /**
     * Remove clusters just with enemy units
     */
    private void removeEnemyClusters() {
        ArrayList<ArrayList<Unit>> remCluster = new ArrayList<>();
        for (ArrayList<Unit> cluster : clusters) {
            if (playerCluster(cluster, (1 - playerForThisComputation))) {
                remCluster.add(cluster);
            }
        }
        for (ArrayList<Unit> enC : remCluster) {
            this.clusters.remove(enC);
        }

    }
    
    private boolean playerCluster(ArrayList<Unit> cluster, int playerEv) {
        for (Unit unit : cluster) {
            if (unit.getPlayer() != playerEv) {
                return false;
            }
        }
        return true;
    }
    
    private Unit getUnitByPos(double[] tPos, ArrayList<Unit> unitsCl) {
        for (Unit unit : unitsCl) {
            if (unit.getX() == tPos[0] && unit.getY() == tPos[1]) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Follow these steps: 1 - Join clusters where you don't have an enemy
     * present with a enemy cluster. 2 - Remove cluster that haven't our units.
     */
    private void filterClusters() {
        ArrayList<ArrayList<Unit>> newClusters = new ArrayList<>();
        for (ArrayList<Unit> cluster : clusters) {
            if (!playerCluster(cluster, playerForThisComputation)) {
                newClusters.add(cluster);
            } 
        }
        this.clusters = newClusters;
    }
    
    private Unit getEnemyClosestByCentroid(ArrayList<Unit> cluster) {
        ArrayList<Unit> unidades = new ArrayList<>();
        for (Unit unit : cluster) {
            if (unit.getPlayer() == playerForThisComputation) {
                unidades.add(unit);
            }
        }
        //find the centroid
        int x = 0, y = 0;
        for (Unit un : unidades) {
            x += un.getX();
            y += un.getY();
        }
        x = x / unidades.size();
        y = y / unidades.size();
        return getEnemyClosest(x, y);
    }
    
    private Unit getEnemyClosest(int xCentroid, int yCentroid) {
        Unit Enbase = getClosestEnemyUnit(xCentroid, yCentroid, gs_to_start_from, playerForThisComputation);
        return Enbase;

    }

    private Unit getClosestEnemyUnit(int xCent, int yCent, GameState state, int player) {
        PhysicalGameState pgs = state.getPhysicalGameState();
        Unit closestEnemy = null;
        int closestDistance = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() != player) {
                int d = Math.abs(u2.getX() - xCent) + Math.abs(u2.getY() - yCent);
                if (closestEnemy == null || d < closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            }
        }
        return closestEnemy;
    }

    private void selectUnitsControled() {
        for (ArrayList<Unit> cluster : clusters) {
            unitsControled.addAll(cluster);
        }
    }
    
}
