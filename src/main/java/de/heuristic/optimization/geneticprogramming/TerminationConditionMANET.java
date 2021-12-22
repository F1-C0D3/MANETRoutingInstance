package de.heuristic.optimization.geneticprogramming;

import de.jgraphlib.util.Tuple;
import de.manetmodel.units.Time;
import de.terministic.serein.api.EvolutionEnvironment;
import de.terministic.serein.api.FitnessFunction;
import de.terministic.serein.api.TerminationCondition;

/**
 * @author klement
 *
 */
public class TerminationConditionMANET implements TerminationCondition<PathComposition> {

	private int successiveEqualFitnessLevel;
	private int maxGenerations;
	private double minFitnessLevel;
	private FitnessFunction<PathComposition> fitnessFuntion;
	private Time maxTerminationTime;

	private Time elapsedTime;
	private int elapsedGenerations;
	
	private Tuple<Integer, Double> currentFitness;
	private int currentSuccessiveEqualFitnessLevel;
	
	private EvolutionEnvironment<PathComposition> environment;

	public TerminationConditionMANET(int successiveEqualFitnessLevel, int maxGenerations, double minFitnessLevel,
			Time maxTerminationTime) {
		this.successiveEqualFitnessLevel = successiveEqualFitnessLevel;
		this.maxGenerations = maxGenerations;
		this.minFitnessLevel = minFitnessLevel;
		this.maxTerminationTime = new Time(System.currentTimeMillis()+maxTerminationTime.getMillis());
		this.elapsedTime = new Time(System.currentTimeMillis());
		this.currentFitness = new Tuple<Integer, Double>(0, 0d);
		this.currentSuccessiveEqualFitnessLevel = 0;

	}
	
	

	public void setFitnessFuntion(FitnessFunction<PathComposition> fitnessFuntion) {
		this.fitnessFuntion = fitnessFuntion;
	}



	@Override
	public void setEnvironment(EvolutionEnvironment<PathComposition> environment) {

		this.environment = environment;
		

	}

	@Override
	public boolean doContinue() {
		
		boolean fitnessReached = false;
		boolean timeCriteriaReached = false;
		boolean maxGenerationReached = false;
		boolean successiveFitnessLevelReached = false;

		// Update fitness threshold criteria
			fitnessReached = minFitnessLevel > fitnessFuntion
					.getFitness(environment.getPopulation().getFittest(fitnessFuntion));

		// Update elapsed time criteria
		if (System.currentTimeMillis() > maxTerminationTime.getMillis()) {
			timeCriteriaReached = true;
		}

		// update generations criteria
		elapsedGenerations++;
		if (elapsedGenerations >= maxGenerations) {
			maxGenerationReached = true;
		}

		// successiveEqualFitnessLevelReached
		
		double fittestValue = fitnessFuntion.getFitness(environment.getPopulation().getFittest(fitnessFuntion));

		if (fittestValue == currentFitness.getSecond() && (currentFitness.getFirst() + 1) == elapsedGenerations&& fitnessReached) {

			currentSuccessiveEqualFitnessLevel++;
			
			if(currentSuccessiveEqualFitnessLevel>=successiveEqualFitnessLevel)
				successiveFitnessLevelReached = true;
		} else {
			currentSuccessiveEqualFitnessLevel=0;
		}
		
		currentFitness.setFirst(elapsedGenerations);
		currentFitness.setSecond(fittestValue);
		
		if(fitnessReached || timeCriteriaReached || maxGenerationReached || successiveFitnessLevelReached)
			return false;
		
		return true;
		
	}

}
