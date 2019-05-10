/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.ScriptsGenerator.GPCompiler;

import ai.ScriptsGenerator.Command.BasicAction.AttackBasic;
import ai.ScriptsGenerator.Command.BasicAction.BuildBasic;
import ai.ScriptsGenerator.Command.BasicAction.HarvestBasic;
import ai.ScriptsGenerator.Command.BasicAction.MoveToCoordinatesBasic;
import ai.ScriptsGenerator.Command.BasicAction.MoveToUnitBasic;
import ai.ScriptsGenerator.Command.BasicAction.TrainBasic;
import ai.ScriptsGenerator.Command.Enumerators.EnumPlayerTarget;
import ai.ScriptsGenerator.Command.Enumerators.EnumPositionType;
import ai.ScriptsGenerator.CommandInterfaces.ICommand;
import ai.ScriptsGenerator.IParameters.IParameters;
import ai.ScriptsGenerator.ParametersConcrete.ClosestEnemy;
import ai.ScriptsGenerator.ParametersConcrete.CoordinatesParam;
import ai.ScriptsGenerator.ParametersConcrete.FarthestEnemy;
import ai.ScriptsGenerator.ParametersConcrete.LessHealthyEnemy;
import ai.ScriptsGenerator.ParametersConcrete.MostHealthyEnemy;
import ai.ScriptsGenerator.ParametersConcrete.PlayerTargetParam;
import ai.ScriptsGenerator.ParametersConcrete.PriorityPositionParam;
import ai.ScriptsGenerator.ParametersConcrete.QuantityParam;
import ai.ScriptsGenerator.ParametersConcrete.RandomEnemy;
import ai.ScriptsGenerator.ParametersConcrete.StrongestEnemy;
import ai.ScriptsGenerator.ParametersConcrete.TypeConcrete;
import ai.ScriptsGenerator.ParametersConcrete.WeakestEnemy;
import ai.ScriptsGenerator.TableGenerator.TableCommandsGenerator;
import java.util.ArrayList;
import java.util.List;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens
 */
public class FunctionGPCompiler implements ICompiler {

    @Override
    public List<ICommand> CompilerCode(String code, UnitTypeTable utt) {
        List<ICommand> commands = new ArrayList<>();

        ICommand tFunction = buildFunctionByCode(code, utt);

        commands.add(tFunction);
        return commands;
    }

    public static ICommand getFunctionByID(int ID, UnitTypeTable utt) {
        TableCommandsGenerator tcg = TableCommandsGenerator.getInstance(utt);
        return tcg.getCommandByID(ID);
    }

    public static int getLastPositionForBasicFunction(int initialPosition, String[] fragments) {
        int contOpen = 0, contClosed = 0;

        for (int i = initialPosition; i < fragments.length; i++) {
            String fragment = fragments[i];
            contOpen += countCaracter(fragment, "(");
            contClosed += countCaracter(fragment, ")");
            if (contOpen == contClosed) {
                return i;
            }
        }

        return fragments.length;
    }

    public static int countCaracter(String fragment, String toFind) {
        int total = 0;
        for (int i = 0; i < fragment.length(); i++) {
            char ch = fragment.charAt(i);
            String x1 = String.valueOf(ch);
            if (x1.equalsIgnoreCase(toFind)) {
                total = total + 1;
            }
        }
        return total;
    }

    private ICommand buildFunctionByCode(String code, UnitTypeTable utt) {
        if (code.contains("build")) {
            return buildCommand(code, utt);
        }
        if (code.contains("attack")) {
            return attackCommand(code, utt);
        }
        if (code.contains("harvest")) {
            return harvestCommand(code, utt);
        }
        if (code.contains("moveToCoord")) {
            return moveToCoordCommand(code, utt);
        }
        if (code.contains("moveToUnit")) {
            return moveToUnitCommand(code, utt);
        }
        if (code.contains("train")) {
            return trainCommand(code, utt);
        }

        return null;
    }

    private ICommand buildCommand(String code, UnitTypeTable utt) {
        code = code.replace("build(", "");
        code = code.replace(")", "").replace(",", "");
        String[] params = code.split(" ");
        BuildBasic build = new BuildBasic();
        if (params[0].equals("Base")) {
            build.addParameter(TypeConcrete.getTypeBase()); //add unit construct type
        } else if (params[0].equals("Barrack")) {
            build.addParameter(TypeConcrete.getTypeBarracks()); //add unit construct type
        } else {
            build.addParameter(TypeConcrete.getTypeConstruction());
        }
        build.addParameter(new QuantityParam(Integer.decode(params[1]))); //add qtd unit
        return build;
    }

    private ICommand harvestCommand(String code, UnitTypeTable utt) {
        code = code.replace("harvest(", "");
        code = code.replace(")", "").replace(",", "");
        String[] params = code.split(" ");
        HarvestBasic harverst = new HarvestBasic();
        harverst.addParameter(TypeConcrete.getTypeWorker()); //add unit type
        harverst.addParameter(new QuantityParam(Integer.decode(params[0]))); //add qtd unit

        return harverst;
    }

    private IParameters getBehaviorByName(String i) {
        switch (i) {
            case "closest":
                return new ClosestEnemy();
            case "farthest":
                return new FarthestEnemy();
            case "lessHealthy":
                return new LessHealthyEnemy();
            case "mostHealthy":
                return new MostHealthyEnemy();
            case "strongest":
                return new StrongestEnemy();
            case "weakest":
                return new WeakestEnemy();
            default:
                return new RandomEnemy();

        }
    }

    private IParameters getTypeUnitByString(String j) {
        switch (j) {
            case "Worker":
                return TypeConcrete.getTypeWorker();
            case "Light":
                return TypeConcrete.getTypeLight();
            case "Ranged":
                return TypeConcrete.getTypeRanged();
            case "Heavy":
                return TypeConcrete.getTypeHeavy();
            default:
                return TypeConcrete.getTypeUnits();

        }
    }

    private ICommand attackCommand(String code, UnitTypeTable utt) {
        code = code.replace("attack(", "");
        code = code.replace(")", "").replace(",", "");
        String[] params = code.split(" ");
        AttackBasic attack = new AttackBasic();
        attack.addParameter(getTypeUnitByString(params[0])); //add unit type
        PlayerTargetParam pt = new PlayerTargetParam();
        pt.addPlayer(EnumPlayerTarget.Enemy);
        attack.addParameter(pt);
        attack.addParameter(getBehaviorByName(params[1])); //add behavior

        return attack;
    }

    private ICommand moveToCoordCommand(String code, UnitTypeTable utt) {
        code = code.replace("moveToCoord(", "");
        code = code.replace(")", "").replace(",", "");
        String[] params = code.split(" ");

        MoveToCoordinatesBasic moveToCoordinates = new MoveToCoordinatesBasic();
        int x = Integer.decode(params[1]);
        int y = Integer.decode(params[2]);
        moveToCoordinates.addParameter(new CoordinatesParam(x, y));
        moveToCoordinates.addParameter(getTypeUnitByString(params[0]));//add unit type

        return moveToCoordinates;
    }

    private ICommand moveToUnitCommand(String code, UnitTypeTable utt) {
        code = code.replace("moveToUnit(", "");
        code = code.replace(")", "").replace(",", "");
        String[] params = code.split(" ");

        MoveToUnitBasic moveToUnit = new MoveToUnitBasic();
        moveToUnit.addParameter(getTypeUnitByString(params[0])); //add unit type
        PlayerTargetParam pt = new PlayerTargetParam();
        pt.addPlayer(getPlayerTargetByNumber(params[1]));
        moveToUnit.addParameter(pt);
        moveToUnit.addParameter(getBehaviorByName(params[2])); //add behavior

        return moveToUnit;
    }

    private EnumPlayerTarget getPlayerTargetByNumber(String p) {
        if (p.equals("Ally")) {
            return EnumPlayerTarget.Ally;
        }
        return EnumPlayerTarget.Enemy;
    }

    private ICommand trainCommand(String code, UnitTypeTable utt) {
        code = code.replace("train(", "");
        code = code.replace(")", "").replace(",", "");
        String[] params = code.split(" ");
        
        TrainBasic train = new TrainBasic();
        train.addParameter(getTypeConstructByName(params[1])); //add unit construct type
        train.addParameter(getTypeUnitByString(params[0])); //add unit Type
        //train.addParameter(TypeConcrete.getTypeWorker()); //add unit Type
        train.addParameter(new QuantityParam(Integer.decode(params[2]))); //add qtd unit
        PriorityPositionParam pos = new PriorityPositionParam();
        //for (Integer position : Permutation.getPermutation(j)) {
        pos.addPosition(EnumPositionType.byName(params[3]));
        //}

        train.addParameter(pos);
        return train;
    }      
    

    private IParameters getTypeConstructByName(String param) {
        if (param.equals("Base")) {
            return TypeConcrete.getTypeBase(); //add unit construct type
        } else if (param.equals("Barrack")) {
            return TypeConcrete.getTypeBarracks(); //add unit construct type
        } else {
            return TypeConcrete.getTypeConstruction();
        }
    }

}
