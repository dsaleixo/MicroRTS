package ai.asymmetric.SSSDavid;
import ai.abstraction.AbstractAction;
import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.Harvest;
import ai.abstraction.RangedRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.core.AI;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.ParameterSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.*;


public class Ataca extends AbstractionLayerAID  {

	Random r = new Random();
    protected UnitTypeTable utt;
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType rangedType;

    
    
    
    public Ataca(UnitTypeTable a_utt) {
        this(a_utt, new AStarPathFinding());
    }


    public Ataca(UnitTypeTable a_utt, PathFinding a_pf) {
        super(a_pf);
        reset(a_utt);
    }

    public void reset() {
    	super.reset();
    }
    
    public void reset(UnitTypeTable a_utt) {
        utt = a_utt;
        workerType = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        rangedType = utt.getUnitType("Ranged");
    }

    public AI clone() {
        return new Ataca(utt);
    }

    public PlayerAction getAction(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);
//        System.out.println("LightRushAI for player " + player + " (cycle " + gs.getTime() + ")");
        List<Unit> workers = new LinkedList<Unit>();
        // behavior of bases:
        for (Unit u : pgs.getUnits()) {
            
            if (u.getType().canAttack && u.getHarvestAmount()>0
                    && u.getPlayer() == player
                    ) {
                meleeUnitBehavior(u, p, gs);
            }
        
           
        }

        return translateActions(player, gs);
    }
    
    
    public PlayerAction getAction(int player, GameState gs, List<Unit> Units,List<Unit> Units_aux, Information inf,
    																	HashMap<Unit, AbstractAction> act) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);
        actions = act;
//        System.out.println("LightRushAI for player " + player + " (cycle " + gs.getTime() + ")");
      
        // behavior of bases:
        for (Unit u : Units) {
           if (u.getType().canAttack 
                    && u.getPlayer() == player && u.getHarvestAmount()>0
                    && gs.getActionAssignment(u) == null) {
                meleeUnitBehavior(u, p, gs);
            }
        }
        
        
        for (Unit u : Units_aux) {
           
        
        // behavior of melee units:
     
           if (u.getType().canAttack 
                    && u.getPlayer() == player && u.getHarvestAmount()>0
                    && gs.getActionAssignment(u) == null
                 ) {
                meleeUnitBehavior(u, p, gs);
            }
        
        // behavior of workers:
       
        }
        
        
   

        actions = null;
        return null;
    }

  
    

    public void meleeUnitBehavior(Unit u, Player p, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit closestEnemy = null;
        int closestDistance = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() != p.getID()) {
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestEnemy == null || d < closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            }
        }
        if (closestEnemy != null) {
//            System.out.println("LightRushAI.meleeUnitBehavior: " + u + " attacks " + closestEnemy);
            attack(u, closestEnemy);
        }
    }

   
    @Override
    public List<ParameterSpecification> getParameters()
    {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }

}
