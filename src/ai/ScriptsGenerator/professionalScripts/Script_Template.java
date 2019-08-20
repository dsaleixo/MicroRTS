/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.ScriptsGenerator.professionalScripts;

import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import java.util.ArrayList;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens e julian
 */
public class Script_Template extends AbstractionLayerAI{
    
    protected UnitTypeTable utt;

    public Script_Template(UnitTypeTable t_utt) {
        this(t_utt, new AStarPathFinding());
    }
    
    public Script_Template(UnitTypeTable t_utt, PathFinding a_pf) {
        super(a_pf);
    }
    
    public void reset() {
        super.reset();
    }
    
    public void reset(UnitTypeTable a_utt) {
        utt = a_utt;
    }

    @Override
    public AI clone() {
        return new Script_Template(utt, pf);
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();
        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));
        return parameters;
    }
    
    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
