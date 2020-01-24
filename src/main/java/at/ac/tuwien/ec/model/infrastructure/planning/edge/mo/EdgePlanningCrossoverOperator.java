package at.ac.tuwien.ec.model.infrastructure.planning.edge.mo;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.util.JMetalException;



public class EdgePlanningCrossoverOperator implements CrossoverOperator<EdgePlanningSolution> {

	private double crossoverProbability;
	Random randomGenerator;
	
	public EdgePlanningCrossoverOperator(double crossover)
	{
		crossoverProbability = crossover;
	}
	
	@Override
	public List<EdgePlanningSolution> execute(List<EdgePlanningSolution> solutions) {
		if (null == solutions) {
		      throw new JMetalException("Null parameter") ;
		    } else if (solutions.size() != 2) {
		      throw new JMetalException("There must be two parents instead of " + solutions.size()) ;
		    }

		    return doCrossover(crossoverProbability, solutions.get(0), solutions.get(1)) ;
	}
	
	
	private List<EdgePlanningSolution> doCrossover(double crossoverProbability,
			EdgePlanningSolution parent1,
			EdgePlanningSolution parent2)
	{
		List<EdgePlanningSolution> offsprings = new ArrayList<EdgePlanningSolution>(2);
		
		offsprings.add((EdgePlanningSolution) parent1.copy());
		offsprings.add((EdgePlanningSolution) parent2.copy());
		
		if(randomGenerator.nextDouble() < crossoverProbability)
		{
			for(int i = 0; i < parent1.getNumberOfVariables(); i++)
			{
				if(randomGenerator.nextBoolean())
				{
					Boolean b1 = parent1.getVariableValue(i);
					Boolean b2 = parent2.getVariableValue(i);
					offsprings.get(0).setVariableValue(i, b2);
					offsprings.get(1).setVariableValue(i, b1);
				}
			}
		}
		
		return offsprings;
	}
	
	@Override
	public int getNumberOfGeneratedChildren() {
		return 2;
	}

	@Override
	public int getNumberOfRequiredParents() {
		return 2;
	}

}